package com.mycompany.java.server.project;

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
                Object obj = in.readObject();

                if (!(obj instanceof RequestType)) {
                    System.out.println("Unknown request type from client: " + socket.getRemoteSocketAddress());
                    continue;
                }

                RequestType request = (RequestType) obj;
                handleRequest(request);

            } catch (EOFException e) {
                // client closed connection gracefully
                System.out.println("Client disconnected: " + socket.getRemoteSocketAddress());
                running = false;
            } catch (Exception e) {
                System.out.println("Error handling client request: " + e.getMessage());
                running = false;
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
    private void handleLogin() {
        // TODO: implement login
    }

    private void handleRegister() {
        // TODO: implement register
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
