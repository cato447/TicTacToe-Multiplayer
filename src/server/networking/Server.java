package server.networking;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

public class Server {
    private ServerSocket serverSocket;
    private HashMap<Integer, Socket> clients;
    private HashMap<Socket, DataOutputStream> outstreams;
    private HashMap<Socket, DataInputStream> instreams;
    private Scanner scanner;
    private int requiredConnections;

    public Server(int port){
        try {
            serverSocket = new ServerSocket(port);
            clients = new HashMap<>();
            outstreams = new HashMap<>();
            instreams = new HashMap<>();
            scanner = new Scanner(System.in);
            requiredConnections = 2;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connectClients(){
        try {
            int id = 0;
            System.out.printf("Waiting for %d clients to connect%n", requiredConnections);
            while(clients.size() < requiredConnections) {
                Socket momentaryClient = serverSocket.accept();
                clients.put(id, momentaryClient);
                outstreams.put(momentaryClient, new DataOutputStream(momentaryClient.getOutputStream()));
                instreams.put(momentaryClient, new DataInputStream(momentaryClient.getInputStream()));
                id++;
                System.out.printf("networking.Client %d got connected%n", id);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handshake(){
        for (Socket client: clients.values()) {
            try {
                String handshakeValue = instreams.get(client).readUTF();
                if (handshakeValue.equals("849465467842158")) {
                    outstreams.get(client).writeInt(200);
                } else {
                    outstreams.get(client).writeInt(403);
                }
                outstreams.get(client).flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (Socket client: clients.values()) {
            try {
                String response = instreams.get(client).readUTF();
                System.out.println(response);
                if (response.equals("1")){
                    outstreams.get(client).writeUTF("Connection confirmed :)");
                    outstreams.get(client).writeUTF("Send me a message ...");
                    System.out.println("Connection confirmed :)");
                } else {
                    outstreams.get(client).writeUTF("Connection failed");
                    System.out.println("Connection failed");
                    client.close();
                }
                outstreams.get(client).flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void getMessage(){
        for (Socket client: clients.values()) {
            try {
                while (true) {
                    String message = instreams.get(client).readUTF();
                    System.out.println(message);
                    if(message.equalsIgnoreCase("exit()"))
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server(2589);
        System.out.println("networking.Server got started");
        server.connectClients();
        server.handshake();
        server.getMessage();
    }
}