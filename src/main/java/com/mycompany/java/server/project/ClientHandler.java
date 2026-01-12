package com.mycompany.java.server.project;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import dao.UserDAO;
import data.Request;
import data.Response;
import dto.InvitationDTO;
import dto.LoginDTO;
import dto.UserDTO;
import dto.MoveDTO;
import dto.RegisterDTO;
import enums.InvitationStatus;
import enums.RequestType;
import enums.ResponseType;
import enums.UserGender;
import enums.UserState;
import models.User;
import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import models.GameSession;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Gson gson;
    private UserDAO dao;
    private User loggedInUser;
    private volatile boolean running = true;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.dao = new UserDAO();
        this.gson = new Gson();

        try {
            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );
            out = new PrintWriter(
                    socket.getOutputStream(), true
            );
        } catch (IOException e) {
            running = false;
        }
    }

    @Override
    public void run() {
        try {
            listen();
        } finally {
            cleanup();
        }
    }

    private void listen() {
        while (running) {
            try {
                String line = in.readLine();
                if (line == null) {
                    break;
                }

                Request request = gson.fromJson(line, Request.class);
                handleRequest(request);

            } catch (JsonSyntaxException | IOException e) {
                running = false;
                break;
            }
        }
    }

    private void handleRequest(Request request) {
        if (request == null || request.getType() == null) {
            send(new Response(ResponseType.INVALID_DATA));
            return;
        }
        switch (request.getType()) {
            case LOGIN:
                handleLogin(request.getPayload());
                break;
            case REGISTER:
                handleRegister(request.getPayload());
                break;
            case GET_ONLINE_PLAYERS:
                handleOnlinePlayersRequest();
                break;
            case GET_LEADERBOARD:
                handleLeaderboardRequest();
                break;
            case INVITE:
                handleInvite(request);
                break;
            case REJECT:
                handleReject(request);
                break;
            case ACCEPT:
                handleAccept(request);
                break;
            case MAKE_MOVE:
                handleMakeMove(request.getPayload());
                break;
            case WATCH:
                handleWatch();
                break;
            case QUICK_GAME:
                handleQuickGame();
                break;
            case LEAVE_GAME:
                handleLeaveGame(request.getPayload());
                break;
            case LOGOUT:
                handleLogout();
                break;
            case LEAVE_QUEUE:
                handleLeaveQueue();
                break;
            case REMATCH_REQUEST:
                handleRematchRequest(request.getPayload());
                break;
            default:
                handleUnknownRequest(request.getType());
                break;
        }
    }

    private void handleRematchRequest(JsonElement payload) {
        if (payload == null || payload.isJsonNull()) {
            return;
        }
        try {
            String sessionId = payload.getAsString();
            GameSession session = ServerContext.getSession(sessionId);
            if (session != null) {
                session.requestRematch(loggedInUser.getUsername());
            }
        } catch (Exception e) {
            send(new Response(ResponseType.ERROR));
        }
    }

    private void handleLeaveQueue() {
        ServerContext.leaveMatchmaking(this);
    }

    private void handleRegister(JsonElement payload) {
        try {
            RegisterDTO dto = gson.fromJson(payload, RegisterDTO.class);

            String username = dto.getUsername();
            String password = dto.getPassword();
            UserGender gender = dto.getGender();

            if (username.isBlank() || password.isBlank() || gender == null) {
                send(new Response(ResponseType.INVALID_DATA));
                return;
            }

            boolean success = dao.register(username, password, gender);

            if (success) {
                send(new Response(ResponseType.REGISTER_SUCCESS));
                loggedInUser = new User(username, gender);
                ServerContext.broadcastOnlinePlayers(username);
            } else {
                send(new Response(ResponseType.USER_EXISTS));
            }

        } catch (JsonSyntaxException e) {
            send(new Response(ResponseType.ERROR));
        } catch (Exception e) {
            send(new Response(ResponseType.ERROR));
        }
    }

    private void handleLogin(JsonElement payload) {
        try {
            LoginDTO dto = gson.fromJson(payload, LoginDTO.class);
            String username = dto.getUsername();
            String password = dto.getPassword();
            if (username.isBlank() || password.isBlank()) {
                send(new Response(ResponseType.INVALID_DATA));
                return;
            }

            User user = dao.login(username, password);
            if (user == null) {
                send(new Response(ResponseType.LOGIN_FAILED));
                return;
            }

            if (!ServerContext.addClient(username, this)) {
                send(new Response(ResponseType.ALREADY_LOGGED_IN));
                return;
            }
            user.setState(UserState.ONLINE);
            loggedInUser = user;

            Response res = new Response(
                    ResponseType.LOGIN_SUCCESS,
                    gson.toJsonTree(user)
            );
            send(res);
            ServerContext.broadcastOnlinePlayers(username);
        } catch (JsonSyntaxException e) {
            send(new Response(ResponseType.ERROR));
        }
    }

    private void handleInvite(Request request) {

        InvitationDTO inviteDTO = gson.fromJson(request.getPayload(), InvitationDTO.class);
        String receiverName = inviteDTO.getReceiverUsername();

        ClientHandler receiverHandler = ServerContext.getClientHandler(receiverName);

        if (receiverHandler != null) {

            Response inviteResponse = new Response(ResponseType.GAME_INVITE, request.getPayload());

            receiverHandler.send(inviteResponse);

        } else {
            InvitationDTO errorDto = new InvitationDTO(inviteDTO.getReceiverUsername(), inviteDTO.getSenderUsername(), InvitationStatus.REJECTED);
            Response errorResponse = new Response(ResponseType.INVITE_REJECTED, gson.toJsonTree(errorDto));

            this.send(errorResponse);
        }
    }

    private void handleAccept(Request request) {

        InvitationDTO inviteDTO = gson.fromJson(request.getPayload(), InvitationDTO.class);
        String senderName = inviteDTO.getSenderUsername();
        String reciverName = inviteDTO.getReceiverUsername();

        ClientHandler senderHandler = ServerContext.getClientHandler(senderName);
        ClientHandler receiverHandler = ServerContext.getClientHandler(reciverName);

        if (senderHandler != null) {

            Response acceptResponse = new Response(ResponseType.INVITE_ACCEPTED, gson.toJsonTree(inviteDTO));

            senderHandler.send(acceptResponse);
            receiverHandler.send(acceptResponse);
            
            System.out.println("Match Started: " + inviteDTO.getSenderUsername() + " VS " + inviteDTO.getReceiverUsername());

        }
    }

    private void handleReject(Request request) {

        InvitationDTO inviteDTO = gson.fromJson(request.getPayload(), InvitationDTO.class);
        String senderName = inviteDTO.getSenderUsername();

        ClientHandler senderHandler = ServerContext.getClientHandler(senderName);

        if (senderHandler != null) {
            Response rejectResponse = new Response(ResponseType.INVITE_REJECTED, gson.toJsonTree(inviteDTO));
            senderHandler.send(rejectResponse);

            System.out.println("Match Rejected: " + inviteDTO.getSenderUsername() + " was rejected by " + inviteDTO.getReceiverUsername());
        }
    }

    private void handleMakeMove(JsonElement payload) {
        System.out.println("Received MAKE_MOVE from " + (loggedInUser != null ? loggedInUser.getUsername() : "null"));
        try {
            MoveDTO move = gson.fromJson(payload, MoveDTO.class);
            GameSession session = ServerContext.getSession(move.getSessionId());
            if (session != null) {
                if (session.getCurrentPlayer().getUsername().equals(loggedInUser.getUsername())) {
                    boolean success = session.playMove(move.getRow(), move.getCol(), move.getSymbol());
                    if (!success) {
                        System.out.println("Move failed for user: " + loggedInUser.getUsername());
                        send(new Response(ResponseType.ERROR, gson.toJsonTree("Invalid move")));
                    }
                } else {
                    System.out.println("Not turn for user: " + loggedInUser.getUsername());
                    send(new Response(ResponseType.ERROR, gson.toJsonTree("Not your turn")));
                }
            } else {
                System.out.println("Session not found for move from: " + loggedInUser.getUsername());
                send(new Response(ResponseType.ERROR, gson.toJsonTree("Session not found")));
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            send(new Response(ResponseType.ERROR));
        }
    }

    private void handleWatch() {
        // TODO: implement watch
    }

    private void handleQuickGame() {
        ServerContext.joinMatchmakingQueue(this);
    }

    private void handleLeaveGame(JsonElement payload) {
        if (payload == null || payload.isJsonNull()) {
            return;
        }
        try {
            String sessionId = payload.getAsString();
            GameSession session = ServerContext.getSession(sessionId);
            if (session != null) {
                session.handleDisconnect(loggedInUser.getUsername());
                if (session.isGameEnded()) {
                    ServerContext.removeSession(sessionId);
                }
            }
        } catch (Exception e) {
            send(new Response(ResponseType.ERROR));
        }
    }

    private void handleLogout() {
        if (loggedInUser == null) {
            return;
        }

        String username = loggedInUser.getUsername();

        ServerContext.removeClient(username);
        loggedInUser = null;
        ServerContext.broadcastOnlinePlayers(username);
    }

    private void handleUnknownRequest(RequestType request) {
        System.out.println("Received unknown request: " + request);
    }

    public synchronized void send(Response response) {
        String json = gson.toJson(response);
        // System.out.println("Sending to " + (loggedInUser != null ? loggedInUser.getUsername() : "unknown") + ": " + response.getType());
        out.println(json);
    }

    private void cleanup() {
        running = false;
        try {
            if (loggedInUser != null) {
                ServerContext.handleClientDisconnect(loggedInUser.getUsername());
                ServerContext.removeClient(loggedInUser.getUsername());
                ServerContext.broadcastOnlinePlayers(loggedInUser.getUsername());
                loggedInUser = null;
            }
            if (socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(User loggedInUser) {
        this.loggedInUser = loggedInUser;
    }

    private void handleOnlinePlayersRequest() {
        try {
            List<UserDTO> onlineUsers = ServerContext.getOnlineUsers(loggedInUser.getUsername());
            Response response = new Response(ResponseType.ONLINE_PLAYERS, gson.toJsonTree(onlineUsers));
            send(response);

        } catch (Exception e) {
            send(new Response(ResponseType.ERROR));
        }
    }

    private void handleLeaderboardRequest() {
        try {
            List<UserDTO> leaderboard = dao.getLeaderboard();

            Response response = new Response(ResponseType.LEADERBOARD, gson.toJsonTree(leaderboard));
            send(response);

        } catch (SQLException e) {
            send(new Response(ResponseType.ERROR));
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + Objects.hashCode(this.loggedInUser);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ClientHandler other = (ClientHandler) obj;
        return Objects.equals(this.loggedInUser, other.loggedInUser);
    }
}
