package game;

import java.nio.charset.StandardCharsets;

public class TicTacToe_Server {

    private String gameState;

    public TicTacToe_Server(){
        gameState = "---------";
    }

    public void setGameState(String gameState) {
        this.gameState = gameState;
    }

    public String getGameState() {
        return gameState;
    }

    public int makeClientMove(String input) {
        int column = Double.valueOf(input.split("\\|")[0]).intValue() / 300;
        int row = Double.valueOf(input.split("\\|")[1]).intValue() / 300;
        int index = column * 3 + row;

        if (isLegalMove(column, row)) {
            byte[] gameStateBytes = gameState.getBytes();
            gameStateBytes[index] = (byte) 'x';
            gameState = new String(gameStateBytes, StandardCharsets.UTF_8);
            return 200;
        } else {
            return 404;
        }
    }

    private boolean isLegalMove(int column, int row){
        int index = column * 3 + row;
        return gameState.charAt(index) == '-';
    }

}
