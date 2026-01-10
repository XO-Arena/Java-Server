package dto;

import enums.UserGender;
import enums.UserState;
import models.User;

public class PlayerDTO {

    private String username;
    private UserGender gender;
    private int score;
    private UserState state;

    public PlayerDTO() {
    }

    public PlayerDTO(String username, UserGender gender, int score, UserState state) {
        this.username = username;
        this.gender = gender;
        this.score = score;
        this.state = state;
    }

    public static PlayerDTO fromUser(User user) {
        return new PlayerDTO(
                user.getUsername(),
                user.getGender(),
                user.getScore(),
                user.getState()
        );
    }

    public String getUsername() {
        return username;
    }

    public UserGender getGender() {
        return gender;
    }

    public int getScore() {
        return score;
    }

    public UserState getState() {
        return state;
    }
}
