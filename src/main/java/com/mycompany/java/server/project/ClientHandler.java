package com.mycompany.java.server.project;

import dao.UserDAO;
import enums.RequestType;
import models.User;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    private User loggedInUser;
    private volatile boolean running = true; // volatile for thread visibility

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            setupStreams();
            listen();
        } catch (IOException e) {
            System.out.println("Error initializing streams for client: " + socket.getRemoteSocketAddress());
        } finally {
            cleanup();
        }
    }

    private void listen() {
    while (running) {
        try {
            // السيرفر هنا ينتظر فقط "نوع الطلب" (مثل LOGIN أو REGISTER)
            Object obj = in.readObject();
            if (obj instanceof String command) {
                handleRequest(command); 
            }
        } catch (Exception e) {
            running = false;
            cleanup();
        }
    }
}

    private void handleRequest(RequestType request) {
        switch (request) {
            case LOGIN:
                handleLogin();
                break;
            case REGISTER:
                handleRegister();
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
                handleUnknownRequest(request);
                break;
        }
    }

    // ==============================
    // Request Handlers 
    // ==============================
    // Inside ClientHandler.java
    private UserDAO userDAO = new UserDAO(); // Initialize DAO

    private void handleRegister() {
        try {
            String username = (String) in.readObject();
            String password = (String) in.readObject();
            String genderStr = (String) in.readObject();

            if (username.isBlank() || password.isBlank() || genderStr == null) {
                send("INVALID_DATA");
                return;
            }

            UserDAO dao = new UserDAO();
            enums.UserGender gender
                    = enums.UserGender.valueOf(genderStr.toUpperCase());

            boolean success = dao.register(username, password, gender);

            if (success) {
                send("REGISTER_SUCCESS");
            } else {
                send("USER_EXISTS");
            }

        } catch (Exception e) {
            e.printStackTrace();
            send("SERVER_ERROR");
        }
    }

    private void handleLogin() {
        try {
            String username = (String) in.readObject();
            String password = (String) in.readObject();

            if (username.isBlank() || password.isBlank()) {
                send("INVALID_DATA");
                return;
            }

            UserDAO dao = new UserDAO();
            User user = dao.login(username, password);

            if (user == null) {
                send("LOGIN_FAILED");
                return;
            }

            if (!ServerContext.addClient(username, this)) {
                send("ALREADY_LOGGED_IN");
                return;
            }

            loggedInUser = user;
            send("LOGIN_SUCCESS");

        } catch (Exception e) {
            e.printStackTrace();
            send("SERVER_ERROR");
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

    // ==============================
    // Streams
    // ==============================
    private void setupStreams() throws IOException {
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
    }

    public synchronized void send(Object response) {
        try {
            out.writeObject(response);
            out.flush();
        } catch (IOException e) {
            System.out.println("Failed to send response to client: " + e.getMessage());
        }
    }

    // ==============================
    // Cleanup
    // ==============================
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

    // ==============================
    // Getters / Setters
    // ==============================
    public User getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(User loggedInUser) {
        this.loggedInUser = loggedInUser;
    }
}
