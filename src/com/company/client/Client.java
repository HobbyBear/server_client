package com.company.client;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Client implements AutoCloseable {
    final int port = 8888;
    private final Scanner reader;
    private final PrintWriter writer;
    private Lock lock = new ReentrantLock();

    public Client(String traderID) throws Exception {
        Socket socket = new Socket("localhost", port);// Connect to server, creating objects for communications
        reader = new Scanner(socket.getInputStream());

        writer = new PrintWriter(socket.getOutputStream(), true);// Auto flush stream with every command

        writer.println(traderID);// Sending customer ID

        String line = reader.nextLine();// Parse response
        if (line.trim().compareToIgnoreCase("success") != 0)
            throw new Exception(line);
    }

    public boolean addTrader(String traderID) {
        lock.lock();
        writer.println("ADD " + traderID);
        String line = reader.nextLine();//response
        lock.unlock();
        return line.trim().compareToIgnoreCase("success") == 0;
    }

    public String[] getTraders() {
        lock.lock();
        writer.println("TRADERS");

        String line = reader.nextLine();//Num of traders
        System.out.println("client-------------" + line + "---------TRADERS");
        String[] substrings = line.split(" ");
        int numberOfAccounts = substrings.length;

        String[] accounts = new String[numberOfAccounts];// Read traders id
        for (int i = 0; i < numberOfAccounts; i++) {
            accounts[i] = substrings[i];
        }
        lock.unlock();
        return accounts;
    }

    public boolean checkUpdate(String s) {
        lock.lock();
        writer.println("UPDATECHECK " + s);

        String line = reader.nextLine();// Read response
        lock.unlock();
        return line.trim().compareToIgnoreCase("obsolete") == 0;//true for update available
    }

    public boolean checkStock(String traderID) {//check if this trader have stock
        lock.lock();
        writer.println("CHECKSTOCK " + traderID);

        String line = reader.nextLine();//response
        lock.unlock();
        return line.trim().compareToIgnoreCase("true") == 0;
    }

    public boolean transfer(String fromID, String toID) {
        lock.lock();
        writer.println("TRANSFER " + fromID + " " + toID);

        String line = reader.nextLine();// Read response
        lock.unlock();
        return line.trim().compareToIgnoreCase("success") == 0;
    }

    public void exit(String traderID) {
        writer.println("EXIT " + traderID);
    }

    @Override
    public void close() {
        reader.close();
        writer.close();
    }
}