package com.mycompany.java.server.project;

import com.google.gson.Gson;
import data.Response;
import dto.PlayerDTO;
import enums.ResponseType;
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

    public static List<PlayerDTO> getOnlineUsers(String username) {
        return onlineClients.values().stream()
                .map(ClientHandler::getLoggedInUser)
                .filter(Objects::nonNull)
                .filter(user -> !user.getUsername().equals(username))
                .map(user -> new PlayerDTO(
                user.getUsername(),
                user.getGender(),
                user.getScore(),
                user.getState()
        ))
                .collect(Collectors.toList());
    }

   public static void broadcastOnlinePlayers() {
    onlineClients.forEach((username, client) -> {

        List<PlayerDTO> listForClient =
                getOnlineUsers(username);

        Response response = new Response(
                ResponseType.ONLINE_PLAYERS,
                new Gson().toJsonTree(listForClient)
        );

        client.send(response);
    });
}
}
