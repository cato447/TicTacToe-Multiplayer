package networking;

import game.TicTacToe_Server;
import logging.LogType;
import logging.ServerLogger;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

public class MultiPlayerServer {
    private ServerSocket serverSocket;
    private HashMap<Integer, Socket> clients;
    private HashMap<Socket, String> clientNames;
    private HashMap<Socket, DataOutputStream> outstreams;
    private HashMap<Socket, DataInputStream> instreams;
    private TicTacToe_Server ticTacToe_server;
    private ServerLogger serverLogger;
    private Scanner scanner;
    private int requiredConnections;

    public MultiPlayerServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            clients = new HashMap<>();
            clientNames = new HashMap<>();
            outstreams = new HashMap<>();
            instreams = new HashMap<>();
            ticTacToe_server = new TicTacToe_Server();
            scanner = new Scanner(System.in);
            serverLogger = new ServerLogger();
            requiredConnections = 2;

            serverLogger.printLog("Server started successfully", LogType.Log);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connectClients() {
        try {
            int id = 0;
            serverLogger.printLog(String.format("Waiting for %d clients to connect ...", requiredConnections), LogType.Log);
            while (clients.size() < requiredConnections) {
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

    public void handshake() {
        for (Socket client : clients.values()) {
            try {
                int handshakeValue = instreams.get(client).readInt();
                if (handshakeValue == 165313125) {
                    outstreams.get(client).writeInt(200);
                    outstreams.get(client).flush();
                    clientNames.put(client, instreams.get(client).readUTF());
                    outstreams.get(client).writeUTF(serverSocket.getInetAddress().getHostAddress() + ":" + serverSocket.getLocalPort());
                    outstreams.get(client).flush();
                    serverLogger.printLog(String.format("%s got connected", clientNames.get(client)), LogType.Log);
                } else {
                    outstreams.get(client).writeInt(403);
                    outstreams.get(client).flush();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void ticTacToe_gameloop() {
        for (Socket client : clients.values()) {
            try {
                while (!client.isClosed()) {
                    String message = instreams.get(client).readUTF();
                    serverLogger.printLog(message, clientNames.get(client), LogType.Message);
                    outstreams.get(client).writeInt(200);
                    outstreams.get(client).flush();
                    serverLogger.printLog("Sent verification code", clientNames.get(client), LogType.Log);
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

    public void gameFlow(String input, Socket client) {
        switch (input) {
            case "gameState":
                try {
                    outstreams.get(client).writeUTF(ticTacToe_server.getGameState());
                    outstreams.get(client).flush();
                    serverLogger.printLog("Sent gameState", clientNames.get(client), LogType.Log);
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                }

            case "update":
                try {
                    String position = instreams.get(client).readUTF();
                    serverLogger.printLog(position, clientNames.get(client), LogType.Message);
                    outstreams.get(client).writeInt(200);
                    outstreams.get(client).flush();
                    serverLogger.printLog("Sent verification code", clientNames.get(client), LogType.Log);
                    int verificationCode = ticTacToe_server.makeClientMove(position);
                    if (verificationCode == 200) {
                        String gameState = ticTacToe_server.getGameState();
                        outstreams.get(client).writeUTF(gameState);
                        outstreams.get(client).flush();
                        serverLogger.printLog(String.format("Sent gameState: %s", gameState), clientNames.get(client), LogType.Log);
                        break;
                    } else {
                        outstreams.get(client).writeUTF(" ");
                        outstreams.get(client).flush();
                        serverLogger.printLog(String.format("Move is not allowed!"), clientNames.get(client), LogType.Error);
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            case "gameEnded":
                try {
                    boolean gameEnded = ticTacToe_server.gameEnded();
                    outstreams.get(client).writeBoolean(gameEnded);
                    if (gameEnded) {
                        String coordinates = "";
                        for (Point point : ticTacToe_server.getWinCoordinates()) {
                            coordinates += point.x + ";" + point.y + ";";
                        }
                        outstreams.get(client).writeUTF(coordinates);
                        break;
                    } else {
                        ticTacToe_server.makeServerMove();
                        String gameState = ticTacToe_server.getGameState();
                        outstreams.get(client).writeUTF(gameState);
                        outstreams.get(client).flush();
                        serverLogger.printLog(String.format("Sent gameState: %s", gameState), clientNames.get(client), LogType.Log);
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            case "exit()":
                try {
                    outstreams.get(client).writeInt(200);
                    outstreams.get(client).flush();
                    outstreams.get(client).close();
                    instreams.get(client).close();
                    client.close();
                    serverLogger.printLog(String.format("%s closed the connection", clientNames.get(client)), LogType.Log);
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    public static void main(String[] args) {
        MultiPlayerServer server = new MultiPlayerServer(2589);
        server.connectClients();
        server.handshake();
        server.ticTacToe_gameloop();
    }
}
