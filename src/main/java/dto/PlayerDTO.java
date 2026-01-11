/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dto;

import enums.UserGender;
import enums.UserState;
import enums.PlayerSymbol;
import models.User;

/**
 *
 * @author mohannad
 */
public class PlayerDTO extends UserDTO {
    private PlayerSymbol symbol;

    public PlayerDTO(String username, UserGender gender, int score, UserState state, PlayerSymbol symbol) {
        super(username, gender, score, state);
        this.symbol = symbol;
    }
    
    public static PlayerDTO fromUser(User user, PlayerSymbol symbol) {
        return new PlayerDTO(
                user.getUsername(),
                user.getGender(),
                user.getScore(),
                user.getState(),
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

