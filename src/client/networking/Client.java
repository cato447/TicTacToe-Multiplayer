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
    private boolean success;

    public Client(String ip, int port) {
        try {
            scanner = new Scanner(System.in);
            Socket serverSocket = new Socket(ip, port);
            out = new DataOutputStream(serverSocket.getOutputStream());
            in = new DataInputStream(serverSocket.getInputStream());
            System.out.println(in.readUTF());
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handshake() {
        try {
            out.writeUTF("849465467842158");
            out.flush();
            success = in.readInt() == 200;
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
                out.flush();
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

    public static void main(String[] args) {
        Client client = new Client("localhost", 2589);
        client.handshake();
        client.oneSidedMessage();
    }
}