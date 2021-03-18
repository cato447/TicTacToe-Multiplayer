package networking;

import res.TicTacToe_Server;
import logging.LogType;
import logging.ServerLogger;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

public class SinglePlayerServer {
    private ServerSocket serverSocket;
    private HashMap<Integer, Socket> clients;
    private HashMap<Socket, String> clientNames;
    private HashMap<Socket, DataOutputStream> outstreams;
    private HashMap<Socket, DataInputStream> instreams;
    private TicTacToe_Server ticTacToe_server;
    private ServerLogger serverLogger;
    private Scanner scanner;
    private int requiredConnections;

    public SinglePlayerServer(int port){
        try {
            serverSocket = new ServerSocket(port);
            clients = new HashMap<>();
            clientNames = new HashMap<>();
            outstreams = new HashMap<>();
            instreams = new HashMap<>();
            ticTacToe_server = new TicTacToe_Server();
            scanner = new Scanner(System.in);
            serverLogger = new ServerLogger();
            requiredConnections = 1;

            serverLogger.printLog("Server started successfully", LogType.Log);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connectClients(){
        try {
            int id = 0;
            serverLogger.printLog(String.format("Waiting for %d clients to connect ...", requiredConnections), LogType.Log);
            while(clients.size() < requiredConnections) {
                Socket momentaryClient = serverSocket.accept();
                clients.put(id, momentaryClient);
                outstreams.put(momentaryClient, new DataOutputStream(momentaryClient.getOutputStream()));
                instreams.put(momentaryClient, new DataInputStream(momentaryClient.getInputStream()));
                id++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handshake(){
        for (Socket client: clients.values()) {
            try {
                int handshakeValue = instreams.get(client).readInt();
                if (handshakeValue == 165313125) {
                    outstreams.get(client).writeInt(200);
                    outstreams.get(client).flush();
                    clientNames.put(client, instreams.get(client).readUTF());
                    serverLogger.printLog(String.format("Client: \"%s\" got connected", clientNames.get(client)), LogType.Log);
                } else {
                    outstreams.get(client).writeInt(403);
                    outstreams.get(client).flush();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void ticTacToe_gameloop(){
        for (Socket client: clients.values()) {
            try {
                while (!client.isClosed()) {
                    //Get instruction
                    String message = instreams.get(client).readUTF();
                    serverLogger.printLog(message, clientNames.get(client), LogType.Message);
                    outstreams.get(client).writeInt(200);
                    outstreams.get(client).flush();
                    serverLogger.printLog("Sent verification code", "200", clientNames.get(client), LogType.Log);
                    this.gameFlow(message, client);
                }
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    client.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }

    public void sendGameState(Socket client){
        try {
            String gameState = ticTacToe_server.getGameState();
            outstreams.get(client).writeUTF(gameState);
            outstreams.get(client).flush();
            serverLogger.printLog("Sent gameState", gameState, clientNames.get(client), LogType.Log);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void gameFlow(String input, Socket client){
        switch (input){
            case "ready":
                gameFlow("gameState", client);
                break;

            case "gameState":
                sendGameState(client);
                break;

            case "clientMove":
                try {
                    //Get position (X|Y)
                    String position = instreams.get(client).readUTF();
                    serverLogger.printLog(position, clientNames.get(client), LogType.Message);
                    //Send confirmation (2ßß)
                    outstreams.get(client).writeInt(200);
                    outstreams.get(client).flush();
                    serverLogger.printLog("Sent verification code", "200", clientNames.get(client), LogType.Log);
                    boolean moveAllowed = ticTacToe_server.makeClientMove(position);
                    if (moveAllowed) {
                        sendGameState(client);
                    } else {
                        //send " "
                        outstreams.get(client).writeUTF("userInput");
                        outstreams.get(client).flush();
                        serverLogger.printLog("Requested userInput", LogType.Log);
                        serverLogger.printLog(String.format("Move is not allowed!"), clientNames.get(client), LogType.Error);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case "computerMove":
                ticTacToe_server.makeComputerMove();
                sendGameState(client);
                try {
                    outstreams.get(client).writeUTF("userInput");
                    outstreams.get(client).flush();
                    serverLogger.printLog("Requested userInput", LogType.Log);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case "serverType":
                try {
                    outstreams.get(client).writeBoolean(true);
                    outstreams.get(client).flush();
                    serverLogger.printLog("Sent serverType", Boolean.toString(true), clientNames.get(client), LogType.Log);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case "isClientOne":
                for (Map.Entry<Integer, Socket> entry : clients.entrySet()) {
                    if (Objects.equals(client, entry.getValue())) {
                        try {
                            boolean isClientOne = entry.getKey() == 0;
                            outstreams.get(client).writeBoolean(isClientOne);
                            outstreams.get(client).flush();
                            serverLogger.printLog("Sent isPlayerOne", Boolean.toString(isClientOne), clientNames.get(client), LogType.Log);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        serverLogger.printLog("Current client not in clients!", LogType.Error);
                    }
                }
                break;

            case "gameEnded":
                try {
                    boolean gameEnded = ticTacToe_server.gameEnded();
                    outstreams.get(client).writeBoolean(gameEnded);
                    if (gameEnded) {
                        //send coordinates
                        String coordinates = "";
                        for (Point point: ticTacToe_server.getWinCoordinates()) {
                            coordinates += point.x + ";" + point.y + ";";
                        }
                        //send winning fields
                        outstreams.get(client).writeUTF(coordinates);
                        serverLogger.printLog("Winning coordinates got sent", coordinates, clientNames.get(client), LogType.Log);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case "exit":
                try {
                    outstreams.get(client).writeInt(200);
                    outstreams.get(client).flush();
                    outstreams.get(client).close();
                    instreams.get(client).close();
                    client.close();
                    serverLogger.printLog(String.format("%s closed the connection",clientNames.get(client)), LogType.Log);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case "reset":
                try {
                    outstreams.get(client).writeInt(200);
                    outstreams.get(client).flush();
                    ticTacToe_server.resetGameState();
                    this.gameFlow("gameState", client);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    public static void main(String[] args) {
        SinglePlayerServer server = new SinglePlayerServer(2589);
        server.connectClients();
        server.handshake();
        server.ticTacToe_gameloop();
    }
}