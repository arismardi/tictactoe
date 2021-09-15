package com.aris.mardiyanto.model;

import lombok.Data;

@Data
public class Game {

    private String gameId;
    private Player player1;
    private Player player2;
    private GameStatus status;
    private int[][] board;
    private TicToe winner;
    private String winnerName;
    private TicToe currentTurn;

}
