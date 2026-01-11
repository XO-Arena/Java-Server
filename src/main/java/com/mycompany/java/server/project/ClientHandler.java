package com.mycompany.java.server.project;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import dao.UserDAO;
import data.Request;
import data.Response;
import dto.LoginDTO;
import dto.RegisterDTO;
import enums.RequestType;
import enums.ResponseType;
import enums.UserGender;
import models.User;

import java.io.*;
import java.net.Socket;

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
            case INVITE:
                handleInvite();
                break;
            case REJECT:
                handleReject();
                break;
            case MAKE_MOVE:
                handleMakeMove();
                break;
            case WATCH:
                handleWatch();
                break;
            case QUICK_GAME:
                handleQuickGame();
                break;
            case LEAVE_GAME:
                handleLeaveGame();
                break;
            case LOGOUT:
                handleLogout();
                break;
            default:
                handleUnknownRequest(request.getType());
                break;
        }
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

            loggedInUser = user;
            Response res = new Response(
                    ResponseType.LOGIN_SUCCESS,
                    gson.toJsonTree(user)
            );

            // ✅ تأكيد الإرسال
            System.out.println("SENT TO CLIENT: " + gson.toJson(res));

            send(res);
        } catch (JsonSyntaxException e) {
            send(new Response(ResponseType.ERROR));
        }
    }

    private void handleInvite() {
        // TODO: implement invite
    }

    private void handleReject() {
        // TODO: implement reject
    }

    private void handleMakeMove() {
        // TODO: implement make move
    }

    private void handleWatch() {
        // TODO: implement watch
    }

    private void handleQuickGame() {
        // TODO: implement quick game
    }

    private void handleLeaveGame() {
        // TODO: implement leave game
    }

    private void handleLogout() {
        // TODO: implement logout
        running = false;
    }

    private void handleUnknownRequest(RequestType request) {
        System.out.println("Received unknown request: " + request);
    }

    public synchronized void send(Response response) {
        String json = gson.toJson(response);
        out.println(json);
    }

    private void cleanup() {
        running = false;
        try {
            if (loggedInUser != null) {
                ServerContext.removeClient(loggedInUser.getUsername());
            }
            if (!socket.isClosed()) {
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
}
