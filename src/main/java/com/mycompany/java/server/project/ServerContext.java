package com.mycompany.java.server.project;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ServerContext {

    // ------------------------------
    // Online clients (username -> ClientHandler)
    // ------------------------------
    private static final ConcurrentHashMap<String, ClientHandler> onlineClients = new ConcurrentHashMap<>();

    // Add a user if not already online
    public static boolean addClient(String username, ClientHandler handler) {
        return onlineClients.putIfAbsent(username, handler) == null;
    }

    // Remove a user when logout/disconnect
    public static void removeClient(String username) {
        onlineClients.remove(username);
    }

    // Get a ClientHandler by username
    public static ClientHandler getClientHandler(String username) {
        return onlineClients.get(username);
    }

    // Broadcast to all online clients
    public static void broadcast(Object message) {
        for (ClientHandler handler : onlineClients.values()) {
            handler.send(message);
        }
    }

}
