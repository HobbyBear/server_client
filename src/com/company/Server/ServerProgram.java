package com.company.Server;

import java.net.ServerSocket;
import java.net.Socket;

public class ServerProgram {
    private final static int port = 8888;

    public static void main(String[] args) {
        RunServer();
    }

    private static final Market market = new Market();

    private static void RunServer() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Waiting for incoming connections...");
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ClientHandler(socket, market)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}