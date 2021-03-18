package networking;

import logging.ClientLogger;
import logging.LogType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket serverSocket;
    private DataOutputStream out;
    private DataInputStream in;
    private static Scanner scanner;
    private ClientLogger clientLogger;
    private static String name;
    private String serverName;
    private boolean success;

    public Client(String ip, int port, String name) {
        try {
            scanner = new Scanner(System.in);
            serverSocket = new Socket(ip, port);
            serverName = serverSocket.getRemoteSocketAddress().toString();
            out = new DataOutputStream(serverSocket.getOutputStream());
            in = new DataInputStream(serverSocket.getInputStream());
            clientLogger = new ClientLogger();
            success = true;
            this.name = name;
            clientLogger.printLog(String.format("Client with the name %s successfully initialized", name), success, LogType.Log);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handshake() {
        try {
            out.writeInt(165313125);
            out.flush();
            success = in.readInt() == 200;
            if (success) {
                out.writeUTF(name);
                out.flush();
                clientLogger.printLog("You successfully connected to me", serverName, success, LogType.Log);
            } else {
                clientLogger.printLog("Connection failed try again", success, LogType.Log);
                System.exit(1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendToServer(String message) {
        try {
            out.writeUTF(message);
            out.flush();
            success = in.readInt() == 200;
            clientLogger.printLog(String.format("Sent the message: %s", message), serverName, success, LogType.Output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendConfirmation(){
        try {
            out.writeInt(200);
            out.flush();
            clientLogger.printLog("Sent verification code", serverName, true, LogType.Output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getResponse() {
        try {
            String message = in.readUTF();
            clientLogger.printLog(String.format("Message recieved: %s", message), serverName, true, LogType.Input);
            return message;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public boolean getBooleanResponse(String message) {
        try {
            boolean state = in.readBoolean();
            clientLogger.printLog(String.format("%s: %b", message, state), serverName, true, LogType.Input);
            return state;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isPlayerOne(){
        this.sendToServer("isClientOne");
        boolean isClientOne = this.getBooleanResponse("isClientOne");
        return isClientOne;
    }

    public boolean getServerType(){
        this.sendToServer("serverType");
        boolean serverType = this.getBooleanResponse("isSingleServer");
        return serverType;
    }

    public String getGameState() {
        this.sendToServer("gameState");
        String gameState = this.getResponse();
        return gameState;
    }

    public boolean getGameEnded() {
        this.sendToServer("gameEnded");
        boolean gameEnded = false;
        try {
            gameEnded = in.readBoolean();
            clientLogger.printLog(String.format("Game ended: %b", gameEnded), serverName, gameEnded, LogType.Input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gameEnded;
    }

    public void waitForInput() {

    }

    public void exitProcess(){
        try {
            out.writeUTF("exit");
            out.flush();
            success = in.readInt()==200;
            clientLogger.printLog("Closing connection to server", serverName, success, LogType.Log);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void resetBoard(){
        try {
            out.writeUTF("reset");
            out.flush();
            success = in.readInt()==200;
            clientLogger.printLog("Resetting board", serverName, success, LogType.Log);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public boolean isConnected(){
        return serverSocket.isConnected();
    }


}