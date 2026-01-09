package com.mycompany.java.server.project;

import java.util.concurrent.ConcurrentHashMap;

public class ServerContext {

    private static final ConcurrentHashMap<String, ClientHandler> onlineClients = new ConcurrentHashMap<>();

    public static boolean addClient(String username, ClientHandler handler) {
        return onlineClients.putIfAbsent(username, handler) == null;
    }

    public static void removeClient(String username) {
        onlineClients.remove(username);
    }

    public static ClientHandler getClientHandler(String username) {
        return onlineClients.get(username);
    }

}
