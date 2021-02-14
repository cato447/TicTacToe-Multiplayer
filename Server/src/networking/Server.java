package networking;

import logging.LogType;
import logging.ServerLogger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

public class Server {
    private ServerSocket serverSocket;
    private HashMap<Integer, Socket> clients;
    private HashMap<Socket, String> clientNames;
    private HashMap<Socket, DataOutputStream> outstreams;
    private HashMap<Socket, DataInputStream> instreams;
    private ServerLogger logger;
    private Scanner scanner;
    private int requiredConnections;

    public Server(int port){
        try {
            serverSocket = new ServerSocket(port);
            clients = new HashMap<>();
            clientNames = new HashMap<>();
            outstreams = new HashMap<>();
            instreams = new HashMap<>();
            scanner = new Scanner(System.in);
            logger = new ServerLogger();
            requiredConnections = 1;

            logger.printLog("Server started successfully", LogType.Log);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connectClients(){
        try {
            int id = 0;
            logger.printLog(String.format("Waiting for %d clients to connect ...", requiredConnections), LogType.Log);
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
                    outstreams.get(client).writeUTF(serverSocket.getInetAddress().getHostAddress()+":"+serverSocket.getLocalPort());
                    outstreams.get(client).flush();
                    logger.printLog(String.format("%s got connected", clientNames.get(client)), LogType.Log);
                } else {
                    outstreams.get(client).writeInt(403);
                    outstreams.get(client).flush();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void getMessages(){
        for (Socket client: clients.values()) {
            try {
                while (true) {
                    String message = instreams.get(client).readUTF();
                    if (!message.equalsIgnoreCase("exit()")) {
                        logger.printLog(message, clientNames.get(client), LogType.Message);
                    } else {
                        outstreams.get(client).writeInt(200);
                        logger.printLog(String.format("%s closed the connection",clientNames.get(client)), LogType.Log);
                        break;
                    }
                    outstreams.get(client).writeInt(200);
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
        clients.clear();
        System.out.println("Do you want to keep the server alive and wait for other clients ? [y]/[n]");
        if (scanner.nextLine().equalsIgnoreCase("y")){
            this.connectClients();
        }
    }

    public static void main(String[] args) {
        Server server = new Server(2589);
        server.connectClients();
        server.handshake();
        server.getMessages();
    }
}