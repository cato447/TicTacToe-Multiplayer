package tests;

import java.util.LinkedList;

public class GenerateAllGameStates {

    public static void main(String[] args) {
        GenerateAllGameStates testGame = new GenerateAllGameStates();
        LinkedList<String> gameStates = testGame.getAllPossibleBoards();
        System.out.printf("All possible boards: %d%n", gameStates.size());
        LinkedList<Integer[][]> gameStateBoards = new LinkedList<>();
        for (String gameState: gameStates) {
            gameStateBoards.add(testGame.convertToNumbers(gameState));
        }
    }

    private Integer[][] convertToNumbers(String gameState){
        Integer[][] board = new Integer[3][3];
        for (int i = 0; i < 3; i++){
            for (int k = 0; k < 3; k++){
                char currentChar = gameState.charAt(i*3+k);
                if (currentChar == 'x'){
                    board[i][k] = 1;
                } else if (currentChar == 'o'){
                    board[i][k] = -1;
                } else if (currentChar == '-'){
                    board[i][k] = 0;
                }
            }
        }
        return board;
    }

    private boolean diagonalWin(Integer[][] board){
        int sumLeftUp = 0;
        int sumLeftDown = 0;
        for (int i = 0; i < 3; i++){
            sumLeftUp += board[i][i];
            sumLeftDown += board[i][2-i];
        }
        return (sumLeftDown == 3 || sumLeftDown == -3) || (sumLeftUp == 3 || sumLeftUp == -3);
    }

    private boolean horizontalWin(Integer[][] board){
        for (int row = 0; row < 3; row++){
            int sum = 0;
            for (int column = 0; column < 3; column++){
                sum += board[row][column];
            }
            if (sum == 3 || sum == -3){
                return true;
            }
        }
        return false;
    }

    private boolean verticalWin(Integer[][] board){
        for (int column = 0; column < 3; column++){
            int sum = 0;
            for (int row = 0; row < 3; row++){
                sum += board[row][column];
            }
            if (sum == 3 || sum == -3){
                return true;
            }
        }
        return false;
    }

    public LinkedList<String> getAllPossibleBoards(){
        LinkedList<String> possibleGameStates = new LinkedList<>();

        String[] possibleChars = new String[]{"x","o","-"};

        for (int c1r1 = 0; c1r1 < 3; c1r1++){
            String s1 = possibleChars[c1r1];
            for (int c1r2 = 0; c1r2 < 3; c1r2++){
                String s2 = possibleChars[c1r2];
                for (int c1r3 = 0; c1r3 < 3; c1r3++){
                    String s3 = possibleChars[c1r3];
                    for (int c2r1 = 0; c2r1 < 3; c2r1++){
                        String s4 = possibleChars[c2r1];
                        for (int c2r2 = 0; c2r2 < 3; c2r2++){
                            String s5 = possibleChars[c2r2];
                            for(int c2r3 = 0; c2r3 <3; c2r3++){
                                String s6 = possibleChars[c2r3];
                                for (int c3r1 = 0; c3r1 < 3; c3r1++){
                                    String s7 = possibleChars[c3r1];
                                    for (int c3r2 = 0; c3r2 < 3; c3r2++){
                                        String s8 = possibleChars[c3r2];
                                        for (int c3r3 = 0; c3r3 < 3; c3r3++){
                                            String s9 = possibleChars[c3r3];
                                            String gameState = s1+s2+s3+s4+s5+s6+s7+s8+s9;
                                            possibleGameStates.add(gameState);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return possibleGameStates;
    }

}
