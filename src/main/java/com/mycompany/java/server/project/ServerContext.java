package com.mycompany.java.server.project;

import dao.UserDAO;
import dto.PlayerDTO;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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

    public static List<PlayerDTO> getOnlineUsers() {
        return onlineClients.values().stream()
                .map(ClientHandler::getLoggedInUser)
                .filter(Objects::nonNull)
                .map(user -> new PlayerDTO(
                user.getUsername(),
                user.getGender(),
                user.getScore(),
                user.getState()
        ))
                .collect(Collectors.toList());
    }

    public static int getTotalRegisteredUsers() {
        UserDAO userDAO = new UserDAO();
        try {
            return userDAO.getTotalRegisteredUsers();
        } catch (Exception e) {
            System.err.println("Error getting total registered users: " + e.getMessage());
            return 0;
        }
    }
}
