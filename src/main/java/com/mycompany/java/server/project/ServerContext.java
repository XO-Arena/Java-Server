package com.mycompany.java.server.project;
import dao.UserDAO;
import com.google.gson.Gson;
import data.Response;
import dto.GameSessionDTO;
import dto.PlayerDTO;
import dto.UserDTO;
import enums.PlayerSymbol;
import enums.ResponseType;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;
import models.GameSession;
import models.MatchEntry;

public class ServerContext {

    private static final ConcurrentHashMap<String, ClientHandler> onlineClients = new ConcurrentHashMap<>();
    private static final PriorityBlockingQueue<MatchEntry> matchmakingQueue = new PriorityBlockingQueue<>(10, Comparator.comparingInt(MatchEntry::getScore));
    private static final ConcurrentHashMap<String, GameSession> activeSessions = new ConcurrentHashMap<>();

    public static boolean addClient(String username, ClientHandler handler) {
        return onlineClients.putIfAbsent(username, handler) == null;
    }

    public static void removeClient(String username) {
        onlineClients.remove(username);
    }

    public static ClientHandler getClientHandler(String username) {
        return onlineClients.get(username);
    }
    
    
    public static boolean isClientExist(String username) {
       
        return onlineClients.containsKey(username);
    }

    public static List<UserDTO> getOnlineUsers(String username) {
        return onlineClients.values().stream()
                .map(ClientHandler::getLoggedInUser)
                .filter(Objects::nonNull)
                .filter(user -> !user.getUsername().equals(username))
                .map(user -> new UserDTO(
                user.getUsername(),
                user.getGender(),
                user.getScore(),
                user.getState()
        ))
                .collect(Collectors.toList());
    }
    
    public static List<UserDTO> getOnlineUsers() {
        return onlineClients.values().stream()
                .map(ClientHandler::getLoggedInUser)
                .filter(Objects::nonNull)
                .map(user -> new UserDTO(
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
   public static void broadcastOnlinePlayers(String loggedoutUsername) {
        onlineClients.forEach((username, client) -> {
            if (!username.equals(loggedoutUsername)) {
                List<UserDTO> listForClient
                        = getOnlineUsers(username);
                Response response = new Response(
                        ResponseType.ONLINE_PLAYERS,
                        new Gson().toJsonTree(listForClient)
                );

                client.send(response);
            }
        });
    }
    public static void broadcastOnlinePlayers() {
        onlineClients.forEach((username, client) -> {

            List<UserDTO> listForClient
                    = getOnlineUsers(username);

            Response response = new Response(
                    ResponseType.ONLINE_PLAYERS,
                    new Gson().toJsonTree(listForClient)
            );

            client.send(response);
        });
    }

    public static boolean joinMatchmakingQueue(ClientHandler client) {
        Gson gson = new Gson();
        if (matchmakingQueue.add(new MatchEntry(client))) {
            try {
                if (matchmakingQueue.size() >= 2) {
                    ClientHandler client1 = matchmakingQueue.take().getClient();
                    ClientHandler client2 = matchmakingQueue.take().getClient();

                    GameSession session = createGameSession(client1, client2);
                    GameSessionDTO dto = GameSessionDTO.fromModel(session);

                    Response response = new Response(ResponseType.GAME_STARTED, gson.toJsonTree(dto));
                    client1.send(response);
                    client2.send(response);

                    leaveMatchmaking(client1);
                    leaveMatchmaking(client2);
                }
                return true;
            } catch (InterruptedException ex) {
                System.getLogger(ClientHandler.class.getName()).log(System.Logger.Level.ERROR, "", ex);
            }
        }

        return false;
    }

    public static void leaveMatchmaking(ClientHandler client) {
        matchmakingQueue.removeIf((match) -> match.getClient().equals(client));
        System.out.println(matchmakingQueue);
    }

    public static PriorityBlockingQueue<MatchEntry> getMatchmakingQueue() {
        return matchmakingQueue;
    }

    public static GameSession createGameSession(ClientHandler p1, ClientHandler p2) {
        GameSession session = new GameSession(p1, p2);
        activeSessions.put(session.getSessionId(), session);
        return session;
    }

    public static GameSession getSession(String sessionId) {
        return activeSessions.get(sessionId);
    }

    public static void removeSession(String sessionId) {
        activeSessions.remove(sessionId);
    }
}
