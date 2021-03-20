package networking;

import logging.LogType;
import logging.ServerLogger;
import res.TicTacToe_GameRules;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class TicTacToe_Server {

    private ServerSocket serverSocket;
    private HashMap<Integer, Socket> clients;
    private HashMap<Socket, Integer> clientIds;
    private HashMap<Socket, String> clientNames;
    private HashMap<Socket, Boolean> clientMoveAuthorizations;
    private HashMap<Socket, DataOutputStream> outstreams;
    private HashMap<Socket, DataInputStream> instreams;
    private TicTacToe_GameRules ticTacToe_gameRules;
    private ServerLogger serverLogger;
    private int requiredConnections;
    private boolean[] clientsReady;


    public TicTacToe_Server(int port, int requiredConnections) {
        try {
            serverSocket = new ServerSocket(port);
            clients = new HashMap<>();
            clientNames = new HashMap<>();
            clientIds = new HashMap<>();
            clientMoveAuthorizations = new HashMap<>();
            outstreams = new HashMap<>();
            instreams = new HashMap<>();
            ticTacToe_gameRules = new TicTacToe_GameRules();
            serverLogger = new ServerLogger();
            this.requiredConnections = requiredConnections;
            clientsReady = new boolean[requiredConnections];

            serverLogger.printLog("Server started successfully", LogType.Log);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isSingleServer() {
        serverLogger.printLog(""+ requiredConnections + (requiredConnections == 1), LogType.Error);
        return requiredConnections == 1;
    }

    public void connectClients() {
        try {
            int id = 0;
            serverLogger.printLog(String.format("Waiting for %d clients to connect ...", requiredConnections), LogType.Log);
            while (clients.size() < requiredConnections) {
                Socket momentaryClient = serverSocket.accept();
                clients.put(id, momentaryClient);
                clientIds.put(clients.get(id), id);
                outstreams.put(momentaryClient, new DataOutputStream(momentaryClient.getOutputStream()));
                instreams.put(momentaryClient, new DataInputStream(momentaryClient.getInputStream()));
                id++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handshake() {
        for (Socket client : clients.values()) {
            try {
                int handshakeValue = instreams.get(client).readInt();
                if (handshakeValue == 165313125) {
                    outstreams.get(client).writeInt(200);
                    outstreams.get(client).flush();
                    clientNames.put(client, instreams.get(client).readUTF());
                    serverLogger.printLog(String.format("Client \"%s\" got connected", clientNames.get(client)), LogType.Log);
                } else {
                    outstreams.get(client).writeInt(403);
                    outstreams.get(client).flush();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendGameState() {
        for (Socket client : clients.values()) {
            sendGameState(client);
        }
    }

    public void sendGameState(Socket client) {
        try {
            String gameState = ticTacToe_gameRules.getGameState();
            outstreams.get(client).writeUTF(gameState);
            outstreams.get(client).flush();
            serverLogger.printLog("Sent gameState", gameState, clientNames.get(client), LogType.Log);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String handleInput(Socket client) {
        String message = null;
        try {
            message = instreams.get(client).readUTF();
            serverLogger.printLog(message, clientNames.get(client), LogType.Message);
            outstreams.get(client).writeInt(200);
            outstreams.get(client).flush();
            serverLogger.printLog("Sent verification code", "200", clientNames.get(client), LogType.Log);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return message;
    }

    public void ticTacToe_gameloop() {
        while (clients.size() == requiredConnections) {
            for (Socket client : clients.values()) {
                gameFlow(handleInput(client), client);
            }
        }
    }

    public void gameFlow(String input, Socket client) {
        switch (input) {
            case "ready":
                if (isSingleServer()) {
                    sendGameState();
                } else {
                    sendGameState(client);
                }
                clientsReady[clientIds.get(client)] = true;
                break;

            case "gameState":
                sendGameState();
                break;

            case "clientMove":
                try {
                    //Get position (X|Y)
                    String position = handleInput(client);
                    boolean moveAllowed = ticTacToe_gameRules.makeClientMove(position, clientIds.get(client));
                    if (moveAllowed) {
                        sendGameState();
                    } else {
                        outstreams.get(client).writeUTF("invalidInput");
                        outstreams.get(client).flush();
                        serverLogger.printLog(String.format("Move is not allowed!"), clientNames.get(client), LogType.Error);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case "opponentMove":
                if (isSingleServer()) {
                    ticTacToe_gameRules.makeComputerMove();
                    serverLogger.printLog("Made computer move", LogType.Log);
                } else {
                    //request move from other player
                    try {
                        outstreams.get(1-clientIds.get(client)).writeUTF("userInput");
                        outstreams.get(client).flush();
                        serverLogger.printLog("Requested opponent userInput", clientNames.get(1-clientIds.get(client)), LogType.Log);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                sendGameState();
                if (isSingleServer()) {
                    try {
                        outstreams.get(client).writeUTF("userInput");
                        outstreams.get(client).flush();
                        serverLogger.printLog("Requested userInput", LogType.Log);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;

            case "serverType":
                try {
                    outstreams.get(client).writeBoolean(isSingleServer());
                    outstreams.get(client).flush();
                    serverLogger.printLog("Sent serverType", Boolean.toString(true), clientNames.get(client), LogType.Log);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case "isClientOne":
                boolean isClientOne = clientIds.get(client) == 0;
                try {
                    outstreams.get(client).writeBoolean(isClientOne);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case "gameEnded":
                try {
                    boolean gameEnded = ticTacToe_gameRules.gameEnded();
                    outstreams.get(client).writeBoolean(gameEnded);
                    if (gameEnded) {
                        //send coordinates
                        String coordinates = "";
                        for (Point point : ticTacToe_gameRules.getWinCoordinates()) {
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
                    outstreams.get(client).close();
                    instreams.get(client).close();
                    client.close();
                    serverLogger.printLog(String.format("%s closed the connection", clientNames.get(client)), LogType.Log);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case "reset":
                ticTacToe_gameRules.resetGameState();
                this.sendGameState();
                break;
        }
    }

    public static void main(String[] args) {
        TicTacToe_Server server = new TicTacToe_Server(2589, Integer.valueOf(args[0]));
        server.connectClients();
        server.handshake();
        server.ticTacToe_gameloop();
    }

}
