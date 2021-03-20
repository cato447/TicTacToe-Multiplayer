import javafx.application.Platform;
import networking.Client;
import render.Engine;

import java.util.Arrays;
import java.util.LinkedList;

public class TicTacToe_Client {

    private Engine renderEngine;
    private Client client;
    private boolean isSingleServer;
    private static String clientName;

    public TicTacToe_Client(){
        renderEngine = Engine.waitForEngine();
    }

    private void ticTacToe_gameloop(){
        //Setup
        client = new Client("server", 2589, clientName);
        client.handshake();
        isSingleServer = client.getServerType();
        if (isSingleServer){
            this.setWindowTitle(client.isPlayerOne());
            client.sendToServer("ready");
            while (client.isConnected() && !renderEngine.isWindowClosed()) {
                String message = client.getResponse();
                //Check if message is gamestate
                if (message.charAt(0) == 'x' || message.charAt(0) == '-' || message.charAt(0) == 'o') {
                    this.drawBoard(message);
                    this.gameFlow("userInput");
                }
                //Handle everything else
                else {
                    this.gameFlow(message);
                }
            }
            try {
                client.exitProcess();
            } catch (Exception e){
                e.printStackTrace();
            }
        } else {
            this.setWindowTitle(client.isPlayerOne());
        }
    }

    private void gameFlow(String input){
        switch (input) {
            case "userInput":
                while(!renderEngine.isMouseClicked()){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                renderEngine.setMouseClicked(false);
                this.userInput();
                break;
        }
    }




   private void userInput(){
       //Send command
       client.sendToServer("clientMove");
       //Send position
       client.sendToServer(String.format("%f|%f", renderEngine.getCoordinates().getX(), renderEngine.getCoordinates().getY()));
       //Get gameState
       String gameState = client.getResponse();
       if (gameState.length() == 9) {
           this.drawBoard(gameState);
           //Send command
           if (!client.getGameEnded()) {
               client.sendToServer("computerMove");
               this.drawBoard(client.getResponse());
           } else {
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
           }
       } else {
           int column = (int) renderEngine.getCoordinates().getX() / 300;
           int row = (int) renderEngine.getCoordinates().getY() / 300;
           System.err.printf("You are not allowed to place at %d|%d%n", column, row);
       }
   }

    private void drawBoard(String gameState) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                renderEngine.drawBoard(gameState);
            }
        });
    }

    private void setWindowTitle(boolean isClientOne){
        if (isClientOne){
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
        new Thread(){
            @Override
            public void run(){
                javafx.application.Application.launch(Engine.class);
            }
        }.start();
        TicTacToe_Client test = new TicTacToe_Client();
        try{
            clientName = args[0];
        } catch (Exception e){
            clientName = "testing";
        }
        test.ticTacToe_gameloop();
    }

}
