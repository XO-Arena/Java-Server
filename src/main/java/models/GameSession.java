/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

import com.google.gson.Gson;
import com.mycompany.java.server.project.ClientHandler;
import com.mycompany.java.server.project.ServerContext;
import dao.UserDAO;
import data.Response;
import dto.GameSessionDTO;
import enums.GameResult;
import enums.PlayerSymbol;
import enums.ResponseType;
import enums.UserState;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 *
 * @author mohannad
 */
public class GameSession {

    private String sessionId;

    private Game game;
    private ClientHandler player1Handler;
    private ClientHandler player2Handler;

    private Player player1;
    private Player player2;

    private UserDAO userDAO = new UserDAO();

    private int drawCount;
    private int player1Wins;
    private int player2Wins;

    private GameResult lastResult;
    private List<ClientHandler> spectatorsList;

    private boolean player1Rematch;
    private boolean player2Rematch;

    private boolean player1Left;
    private boolean player2Left;

    public GameSession(ClientHandler player1Handler, ClientHandler player2Handler) {
        sessionId = UUID.randomUUID().toString();

        this.player1Handler = player1Handler;
        this.player2Handler = player2Handler;
        player1 = Player.fromUser(player1Handler.getLoggedInUser(), PlayerSymbol.X);
        player2 = Player.fromUser(player2Handler.getLoggedInUser(), PlayerSymbol.O);

        this.game = new Game();
        this.lastResult = GameResult.NONE;
        this.player1Wins = this.player2Wins = this.drawCount = 0;

        this.player1Rematch = false;
        this.player2Rematch = false;

        this.player1Left = false;
        this.player2Left = false;

        this.spectatorsList = new ArrayList<>();
    }

    public synchronized void requestRematch(String username) {
        boolean isPlayer1 = player1.getUsername().equals(username);

        if (isPlayer1) {
            player1Rematch = true;
        } else {
            player2Rematch = true;
        }

        if (player1Rematch && player2Rematch) {
            startNewGame();
        } else {
            // Notify the other player
            ClientHandler other = isPlayer1 ? player2Handler : player1Handler;
            if (other != null) {
                other.send(new Response(ResponseType.REMATCH_REQUESTED));
            }
        }
    }

    public synchronized void startNewGame() {
        this.game = new Game();
        this.lastResult = GameResult.NONE;
        this.player1Rematch = false;
        this.player2Rematch = false;
        this.player1Left = false;
        this.player2Left = false;
        broadcastUpdate();
    }

    public String getSessionId() {
        return sessionId;
    }

    public Game getGame() {
        return game;
    }

    public int getPlayer1Wins() {
        return this.player1Wins;
    }

    public int getPlayer2Wins() {
        return this.player2Wins;
    }

    public synchronized void leaveMatch(String leavingUsername) {
        if (player1.getUsername().equals(leavingUsername)) {
            player1Left = true;
        } else if (player2.getUsername().equals(leavingUsername)) {
            player2Left = true;
        }

        if (game.hasEnded()) {
            broadcastUpdate();
            return;
        }
        game.setHasEnded(true);
        if (player1.getUsername().equals(leavingUsername)) {
            lastResult = (player1.getSymbol() == PlayerSymbol.X) ? GameResult.O_WIN : GameResult.X_WIN;
            player1Handler.getLoggedInUser().setState(UserState.ONLINE);
        } else {
            lastResult = (player1.getSymbol() == PlayerSymbol.X) ? GameResult.X_WIN : GameResult.O_WIN;
            player2Handler.getLoggedInUser().setState(UserState.ONLINE);
        }
        ServerContext.broadcastOnlinePlayers();
        handleGameEnd();
        broadcastUpdate();
    }

    public synchronized void handleDisconnect(String username) {
        if (!game.hasEnded()) {
            leaveMatch(username);
        }

        ClientHandler other = null;
        if (player1.getUsername().equals(username)) {
            other = player2Handler;
        } else if (player2.getUsername().equals(username)) {
            other = player1Handler;
        }

        if (other != null) {
            other.send(new Response(ResponseType.OPPONENT_LEFT));
        }
    }

    public synchronized boolean playMove(int row, int col, PlayerSymbol symbol) {
        if (game.hasEnded()) {
            return false;
        }
        boolean success = game.playMove(row, col, symbol);
        if (!success) {
            return false;
        }
        lastResult = game.checkResult();
        if (lastResult == GameResult.NONE) {
            game.switchPlayer();
        } else {
            handleGameEnd();
        }
        broadcastUpdate();
        return true;
    }

    private Player getPlayerWithSymbol(PlayerSymbol symbol) {
        return player1.getSymbol() == symbol ? player1 : player2;
    }

    private void handleGameEnd() {
        game.setHasEnded(true);
        int rewardPoints = 10;
        Player playerX = getPlayerWithSymbol(PlayerSymbol.X);
        Player playerO = getPlayerWithSymbol(PlayerSymbol.O);
        switch (lastResult) {
            case X_WIN:
                if (playerX.getScore() - playerO.getScore() > 50) {
                    rewardPoints /= 2;
                }
                playerX.updateScore(rewardPoints);
                userDAO.updateUserScore(playerX.getUsername(), playerX.getScore());
                playerO.updateScore(-rewardPoints);
                userDAO.updateUserScore(playerO.getUsername(), playerO.getScore());
                if (playerX.equals(player1)) {
                    player1Wins++;
                } else {
                    player2Wins++;
                }
                break;
            case O_WIN:
                if (playerO.getScore() - playerX.getScore() > 50) {
                    rewardPoints /= 2;
                }
                playerO.updateScore(rewardPoints);
                userDAO.updateUserScore(playerO.getUsername(), playerO.getScore());
                playerX.updateScore(-rewardPoints);
                userDAO.updateUserScore(playerX.getUsername(), playerX.getScore());
                if (playerO.equals(player1)) {
                    player1Wins++;
                } else {
                    player2Wins++;
                }
                break;
            case DRAW:
                drawCount++;
        }
    }

    public synchronized void switchPlayerSymbols() {
        PlayerSymbol s1 = player1.getSymbol();
        player1.setSymbol(player2.getSymbol());
        player2.setSymbol(s1);
        broadcastUpdate();
    }

    public synchronized void resetSession() {
        game.reset();
        lastResult = GameResult.NONE;
        broadcastUpdate();
    }

    private void broadcastUpdate() {
        Response response = new Response(ResponseType.GAME_UPDATE, new Gson().toJsonTree(GameSessionDTO.fromModel(this)));
        broadcast(response);
    }

    private void broadcast(Response response) {
        if (player1Handler != null) {
            player1Handler.send(response);
        }
        if (player2Handler != null) {
            player2Handler.send(response);
        }
        for (ClientHandler spectator : spectatorsList) {
            spectator.send(response);
        }
    }

    public synchronized void addSpectator(ClientHandler spectator) {
        spectator.getLoggedInUser().setState(UserState.SPECTATING);
        ServerContext.broadcastOnlinePlayers();
        spectatorsList.add(spectator);
        spectator.send(new Response(ResponseType.GAME_UPDATE, new Gson().toJsonTree(GameSessionDTO.fromModel(this))));
    }

    public synchronized void removeSpectator(ClientHandler spectator) {
        spectator.getLoggedInUser().setState(UserState.ONLINE);
        ServerContext.broadcastOnlinePlayers();
        spectatorsList.remove(spectator);
    }

    public Player getCurrentPlayer() {
        return game.getCurrentPlayer() == PlayerSymbol.X ? getPlayerWithSymbol(PlayerSymbol.X) : getPlayerWithSymbol(PlayerSymbol.O);
    }

    public GameResult getLastResult() {
        return lastResult;
    }

    public boolean isGameEnded() {
        return game.hasEnded();
    }

    public int getDrawCount() {
        return drawCount;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public List<Player> getSpectators() {
        return spectatorsList.stream()
                .map(handler -> Player.fromUser(handler.getLoggedInUser(), null))
                .collect(Collectors.toList());
    }

    public boolean isPlayer1Left() {
        return player1Left;
    }

    public boolean isPlayer2Left() {
        return player2Left;
    }
}
