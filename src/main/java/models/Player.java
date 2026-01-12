package models;

import dto.PlayerDTO;
import enums.PlayerSymbol;
import enums.UserGender;
import enums.UserState;

public class Player extends User {

    private PlayerSymbol symbol;

    public Player(String username, UserGender gender, int score, PlayerSymbol symbol) {
        super(username, gender, score, UserState.ONLINE);
        this.symbol = symbol;
    }

    public static Player fromPlayerDto(PlayerDTO playerDTO) {
        return new Player(
                playerDTO.getUsername(),
                playerDTO.getGender(),
                playerDTO.getScore(),
                playerDTO.getSymbol()
        );
    }
    
    public static Player fromUser(User user, PlayerSymbol symbol) {
        return new Player(
            user.getUsername(),
            user.getGender(),
            user.getScore(),
            symbol
        );
    }

    public PlayerSymbol getSymbol() {
        return symbol;
    }

    public void setSymbol(PlayerSymbol symbol) {
        this.symbol = symbol;
    }
}
