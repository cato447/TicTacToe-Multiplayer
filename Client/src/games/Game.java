package games;

import javax.swing.*;

public class Game {

    private int[] playfield;
    private boolean turnTaken = false;
    private int winningX1,winningY1,winningX2,winningY2;

    public Game(){
        playfield = new int[9];
    }

    public void place(int position, int player){
        if (playfield[position] == 0){
            playfield[position] = player;
            if (player == 1) {
                turnTaken = true;
            }
        } else {
            JOptionPane.showInternalMessageDialog(null,"Tile is already taken");
        }
    }

    public void computersTurn(){
        boolean isPlaced = false;
        try {
            Thread.sleep(750);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while(!isPlaced){
            int random = (int) (Math.random() * 9);
            // if field is free
            if (playfield[random] == 0) {
                place(random, -1);
                isPlaced = true;
            }
        }
    }


    public boolean checkWin() {
        //only check if winning is possible
        if (emptyTiles() < 5) {
            for (int i = 0; i < 3; i++) {
                //horizontal
                if ((playfield[i] == playfield[i + 3] && playfield[i] != 0) && (playfield[i] == playfield[i + 6])) {
                    winningX1 = 75;
                    winningX2 = 825;
                    winningY1 = winningY2 = i * 300 + 150;
                    return true;
                }
                //vertical
                else if ((playfield[i * 3] == playfield[i * 3 + 1] && playfield[i * 3] != 0) && (playfield[i * 3] == playfield[i * 3 + 2])) {
                    winningY1 = 75;
                    winningY2 = 825;
                    winningX1 = winningX2 = i * 300 + 150;
                    return true;
                }
            }
            //diagonal
            if ((playfield[2] == playfield[4] && playfield[2] != 0) && (playfield[2] == playfield[6])){
                winningX2 = winningY1 = 75;
                winningX1 = winningY2 = 825;
                return true;
            } else if ((playfield[0] == playfield[4] && playfield[0] != 0) && (playfield[0] == playfield[8])){
                winningX1 = winningY1 = 75;
                winningX2 = winningY2 = 825;
                return true;
            }
        }
        return false;
    }
    
    public int emptyTiles(){
        int n = 9;
        for (int i = 0; i < playfield.length; i++){
            if (playfield[i] != 0){
                n -= 1;
            }
        }
        return n;
    }

    public boolean isTurnTaken() {
        return turnTaken;
    }

    public void setTurnTaken(boolean turnTaken) {
        this.turnTaken = turnTaken;
    }

    public void setPlayfield(int position, int value) {
        playfield[position] = value;
    }

    public int[] getPlayfield() {
        return playfield;
    }

    public int getWinningX1() {
        return winningX1;
    }

    public int getWinningX2() {
        return winningX2;
    }

    public int getWinningY1() {
        return winningY1;
    }

    public int getWinningY2() {
        return winningY2;
    }
}
