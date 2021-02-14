package client.networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private DataOutputStream out;
    private DataInputStream in;
    private static Scanner scanner;
    private static String name;
    private boolean success;

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_CYAN = "\u001B[36m";


    public Client(String ip, int port, String name) {
        try {
            scanner = new Scanner(System.in);
            Socket serverSocket = new Socket(ip, port);
            out = new DataOutputStream(serverSocket.getOutputStream());
            in = new DataInputStream(serverSocket.getInputStream());
            success = true;
            this.name = name;
            System.out.println(name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handshake() {
        try {
            out.writeInt(165313125);
            out.flush();
            success = in.readInt() == 200;
            if (success){
                out.writeUTF(name);
                System.out.printf(ANSI_CYAN + "You successfully connected to %s%n"+ANSI_RESET, in.readUTF());
            }
            System.out.println(success);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void oneSidedMessage() {
        while (true) {
            System.out.print("input> ");
            String message = scanner.nextLine();
            try {
                out.writeUTF(message);
                System.out.println(in.readInt());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(message.equalsIgnoreCase("exit()"))
                break;
        }
    }

    public void sendToServer(String message) {
        try {
            out.writeUTF(message);
            out.flush();
            success = in.readInt() == 200;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean hasSucceeded() {
        return success;
    }

    public String getName(){return name;}


    public static void main(String[] args) {
        Client client;
        if (args.length > 0) {
            client = new Client("localhost", 2589, args[0]);
        } else {
            client = new Client("localhost", 2589, "GenericName");
        }
        client.handshake();
        client.oneSidedMessage();
    }
}