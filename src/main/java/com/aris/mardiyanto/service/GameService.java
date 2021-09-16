package com.aris.mardiyanto.service;

import com.aris.mardiyanto.exception.InvalidGameException;
import com.aris.mardiyanto.exception.InvalidParamException;
import com.aris.mardiyanto.exception.NotFoundException;
import com.aris.mardiyanto.model.Game;
import com.aris.mardiyanto.model.GameStatus;
import com.aris.mardiyanto.model.Player;
import com.aris.mardiyanto.model.TicToe;
import com.aris.mardiyanto.model.GamePlay;
import com.aris.mardiyanto.storage.GameStorage;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class GameService {
    @Value("${boardsize}")
    private Integer boardSize;

    public Game createGame(Player player) {
        int nSize = (null!=boardSize&&boardSize>0)?boardSize:3;
        Game game = new Game();
        game.setBoard(new int[nSize][nSize]);
        game.setGameId(UUID.randomUUID().toString());
        game.setPlayer1(player);
        game.setStatus(GameStatus.NEW);
        GameStorage.getInstance().setGame(game);
        return game;
    }

    public Game connectToGame(Player player2, String gameId) throws InvalidParamException, InvalidGameException {

        Game game = GameStorage.getInstance().getGames().entrySet().stream()
                .filter(entry -> entry.getKey().equals(gameId))
                .map(Map.Entry::getValue)
                .findFirst().orElseThrow(() -> new NotFoundException("Game not found"));

        //Game game = GameStorage.getInstance().getGames().get(gameId);

        if (game.getPlayer2() != null) {
            throw new InvalidGameException("Game is not valid anymore");
        }

        game.setPlayer2(player2);
        game.setStatus(GameStatus.IN_PROGRESS);
        GameStorage.getInstance().setGame(game);
        return game;
    }

    public Game connectToRandomGame(Player player2) throws NotFoundException {
        Game game = GameStorage.getInstance().getGames().values().stream()
                .filter(it -> it.getStatus().equals(GameStatus.NEW))
                .findFirst().orElseThrow(() -> new NotFoundException("Game not found"));
        game.setPlayer2(player2);
        game.setStatus(GameStatus.IN_PROGRESS);
        GameStorage.getInstance().setGame(game);
        return game;
    }

    public Game gamePlay(GamePlay gamePlay) throws NotFoundException, InvalidGameException {
        if (!GameStorage.getInstance().getGames().containsKey(gamePlay.getGameId())) {
            throw new NotFoundException("Game not found");
        }

        Game game = GameStorage.getInstance().getGames().get(gamePlay.getGameId());
        if (game.getStatus().equals(GameStatus.FINISHED)) {
            throw new InvalidGameException("Game is already finished");
        }

        if(null!=game.getCurrentTurn() && game.getCurrentTurn().equals(gamePlay.getType())){
            throw new InvalidGameException("wait for your turn");
        }
        game.setCurrentTurn(gamePlay.getType());
        
        int[][] board = game.getBoard();
        board[gamePlay.getCoordinateX()][gamePlay.getCoordinateY()] = gamePlay.getType().getValue();

        Boolean xWinner = checkWinner(gamePlay.getCoordinateX(), gamePlay.getCoordinateY(), game.getBoard(), TicToe.X);
        Boolean oWinner = checkWinner(gamePlay.getCoordinateX(), gamePlay.getCoordinateY(), game.getBoard(), TicToe.O);
        //Boolean xWinner = checkWinner(game.getBoard(), TicToe.X);
        //Boolean oWinner = checkWinner(game.getBoard(), TicToe.O);

        if (xWinner) {
            game.setWinner(TicToe.X);
            game.setWinnerName(game.getPlayer1().getLogin());
        } else if (oWinner) {
            game.setWinner(TicToe.O);
            game.setWinnerName(game.getPlayer2().getLogin());
        }

        GameStorage.getInstance().setGame(game);
        return game;
    }

    private Boolean checkWinner(int x, int y, int[][] board, TicToe ticToe){
        int n = board[0].length;
        //check col
        for(int i = 0; i < n; i++){
            if(board[x][i] != ticToe.getValue())
                break;
            if(i == n-1){
                return true;
            }
        }

        //check row
        for(int i = 0; i < n; i++){
            if(board[i][y] != ticToe.getValue())
                break;
            if(i == n-1){
                return true;
            }
        }

        //check diag
        if(x == y){
            //we're on a diagonal
            for(int i = 0; i < n; i++){
                if(board[i][i] != ticToe.getValue())
                    break;
                if(i == n-1){
                    return true;
                }
            }
        }

        //check anti diag (thanks rampion)
        if(x + y == n - 1){
            for(int i = 0; i < n; i++){
                if(board[i][(n-1)-i] != ticToe.getValue())
                    break;
                if(i == n-1){
                    return true;
                }
            }
        }
        return false;
    }

    private Boolean checkWinner10(int[][] board, TicToe ticToe) {
        int nSize = null!=boardSize&&boardSize>0 ? boardSize : 3;
        int[] boardArray = new int[nSize*nSize];
        int counterIndex = 0;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                boardArray[counterIndex] = board[i][j];
                counterIndex++;
            }
        }

        //int[][] winCombinations = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8}, {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, {0, 4, 8}, {2, 4, 6}};
        int[][] winCombinations =new int[nSize][nSize];

        counterIndex=0;
        for (int i = 0; i < nSize; i++) {
            for (int j = 0; j < nSize; j++) {
                winCombinations[counterIndex][j] = (i*nSize)+j;
            }
            counterIndex++;
        }

        for (int i = 0; i < winCombinations.length; i++) {
            int counter = 0;
            for (int j = 0; j < winCombinations[i].length; j++) {
                if (boardArray[winCombinations[i][j]] == ticToe.getValue()) {
                    counter++;
                    if (counter == 3) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
