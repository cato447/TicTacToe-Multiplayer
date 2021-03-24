import javafx.application.Platform;
import logging.LogType;
import networking.Client;
import render.Engine;

import java.util.Arrays;
import java.util.LinkedList;

public class TicTacToe_Client {

    private Engine renderEngine;
    private Client client;
    private static String clientName;
    private boolean isPlayerOne;
    private boolean isAllowedToMove;

    public TicTacToe_Client() {
        renderEngine = Engine.waitForEngine();
        client = new Client("81.169.149.143", 2589, clientName);
        client.handshake();
        isPlayerOne = client.isPlayerOne();
        isAllowedToMove = isPlayerOne;
        //this.setWindowTitle(isPlayerOne);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                renderEngine.updateTitle(clientName);
            }
        });

        client.sendToServer("ready");
    }

    private void ticTacToe_gameloop() {
        while (client.isConnected() && !renderEngine.isWindowClosed()) {
            client.printLog("Waiting for data", true, LogType.Log);
            String message = client.getResponse();
            //Check if message is gamestate
            if (message.matches("([xo-]){9}")) {
                client.printLog("Board updated", true, LogType.Log);
                this.drawBoard(message);
                if (isAllowedToMove){
                    this.gameFlow("userInput");
                }
            }
            //Handle everything else
            else {
                this.gameFlow(message);
            }
        }
        try {
            client.exitProcess();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void gameFlow(String input) {
        switch (input) {
            case "opponentMove":
                //if opponent makes move allow a move
                isAllowedToMove = true;
                break;

            case "userInput":
                client.sendToServer("test");
                client.printLog("Waiting for userInput", true, LogType.Log);
                while (!renderEngine.isMouseClicked()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                renderEngine.setMouseClicked(false);
                isAllowedToMove = false;
                this.userInput();
                break;

            case "invalidInput":
                isAllowedToMove = true;
                this.onInvalidInput();
                break;

            case "gameEnded":
                this.onGameEnd();
                break;
        }
    }

    private void onGameEnd(){
        LinkedList<Integer> winCoordinates = new LinkedList<>();
        //Get winning fields
        String response = client.getResponse();
        for (String s : Arrays.copyOfRange(response.split(";"), 0, 4)) {
            winCoordinates.add(Integer.valueOf(s) * 300);
        }
        //this.drawWinningLine(winCoordinates);
        //client.exitProcess();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        client.resetBoard();
        isAllowedToMove = isPlayerOne;
    }

    private void onInvalidInput(){
        int column = (int) renderEngine.getCoordinates().getX() / 300;
        int row = (int) renderEngine.getCoordinates().getY() / 300;
        System.err.printf("You are not allowed to place at %d|%d%n", column, row);
        this.gameFlow("userInput");
    }

    private void userInput() {
        client.sendToServer("clientMove");
        client.sendToServer(String.format("%f|%f", renderEngine.getCoordinates().getX(), renderEngine.getCoordinates().getY()));
    }

    private void drawBoard(String gameState) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                renderEngine.drawBoard(gameState);
            }
        });
    }

    private void setWindowTitle(boolean isClientOne) {
        if (isClientOne) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    renderEngine.updateTitle("Client One");
                }
            });
        } else {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    renderEngine.updateTitle("Client Two");
                }
            });
        }
    }

    public static void main(String[] args) {
        new Thread() {
            @Override
            public void run() {
                javafx.application.Application.launch(Engine.class);
            }
        }.start();
        try {
            clientName = args[0];
        } catch (Exception e) {
            clientName = "testing";
        }
        TicTacToe_Client test = new TicTacToe_Client();
        test.ticTacToe_gameloop();
    }

}
