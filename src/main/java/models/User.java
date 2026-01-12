package models;

import enums.UserGender;
import enums.UserState;
import java.util.Objects;

public class User {

    private String username;
    private UserGender gender;
    private int score;
    private UserState state;

    public User(String username, UserGender gender) {
        this.username = username;
        this.gender = gender;
        this.score = 300;
        state = UserState.OFFLINE;
    }
        
     public User(String username, UserGender gender,int score, UserState state) {
        this.username = username;
        this.gender = gender;
        this.score = score;
        this.state = state;
    }

    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }

    public UserGender getGender() {
        return gender;
    }

    public void setGender(UserGender gender) {
        this.gender = gender;
    }

    public UserState getState() {
        return state;
    }

    public void setState(UserState state) {
        this.state = state;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void updateScore(int points) {
        score = (score + points < 0) ? 0 : (score + points);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.username);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final User other = (User) obj;
        return Objects.equals(this.username, other.username);
    }
    
    
}
