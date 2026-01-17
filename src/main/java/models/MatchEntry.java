/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

import com.mycompany.java.server.project.ClientHandler;
import java.time.Instant;

/**
 *
 * @author mohannad
 */
public class MatchEntry {
    private ClientHandler client;
    private int score;
    private Instant joinedAt;

    public MatchEntry(ClientHandler client) {
        this.client = client;
        this.score = client.getLoggedInUser().getScore();
        this.joinedAt = Instant.now();
    }

    public ClientHandler getClient() {
        return client;
    }

    public void setClient(ClientHandler client) {
        this.client = client;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }
}
