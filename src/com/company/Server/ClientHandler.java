package com.company.Server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class ClientHandler implements Runnable {
    private final Socket socket;
    public final Market market;
    private String clientID = null;

    public ClientHandler(Socket socket, Market market) {
        this.socket = socket;
        this.market = market;
    }

    public void updateServer(Market m) {
        market.getLock().readLock().lock();
        List<Trader> listOfAccounts = m.getClients();
        if (listOfAccounts.size() == 0) {
            System.out.println("Current traders list");
            System.out.println("-------------------------------------------------");
            System.out.println(" ");
            System.out.println("Empty");
            System.out.println(" ");
            System.out.println("-------------------------------------------------");
            System.out.println(" ");
        } else {
            System.out.println("Current traders list");
            System.out.println("-------------------------------------------------");
            for (Trader traderWithStock : listOfAccounts) {
                StringBuilder serverUpdate = new StringBuilder();
                serverUpdate.append(traderWithStock.getTraderID());
                if (traderWithStock.checkStock()) {
                    serverUpdate.append(" --- ");
                    serverUpdate.append(" has stock");
                }
                System.out.println(serverUpdate);
            }
            System.out.println("-------------------------------------------------");
            System.out.println(" ");
        }
        market.getLock().readLock().unlock();
    }

    @Override
    public void run() {
        try {
            try (Scanner scanner = new Scanner(socket.getInputStream());
                 PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
                try {
                    String clientReadID = scanner.nextLine();
                    clientID = clientReadID;
                    System.out.println("New connection; customer ID " + clientReadID);

                    writer.println("SUCCESS");

                    while (true) {
                        String line = scanner.nextLine();
                        String[] substrings = line.split(" ");
                        switch (substrings[0].toLowerCase()) {
                            case "add":
                                String traderID = substrings[1];
                                market.addTrader(new Trader(traderID));
                                writer.println("SUCCESS");

                                updateServer(market);
                                break;

                            case "exit":
                                String id = substrings[1];
                                market.removeTrader(id);

                                socket.close();
                                System.out.println("Customer " + clientID + " disconnected.");

                                updateServer(market);
                                Thread.currentThread().stop();
                                break;

                            case "traders":
                                market.getLock().readLock().lock();
                                List<Trader> listOfAccounts = market.getClients();

                                StringBuilder buffer = new StringBuilder();

                                for (Trader temp : listOfAccounts) {
                                    buffer.append(temp.getTraderID());
                                    buffer.append("---");
                                    buffer.append(temp.checkStock());
                                    buffer.append(" ");
                                }
                                market.getLock().readLock().unlock();
                                writer.println(buffer);
                                break;

                            case "checkstock":
                                Trader trader = market.findTrader(substrings[1]);
                                if (trader.checkStock()) {
                                    writer.println(trader.checkStock());
                                } else {
                                    writer.println(trader.checkStock());
                                }
                                break;

                            case "updatecheck":
                                market.getLock().readLock().lock();
                                List<Trader> allAccounts = market.getClients();

                                StringBuilder actual = new StringBuilder();
                                if (allAccounts.size() == 1) {
                                    actual.append(allAccounts.get(0).getTraderID());
                                    actual.append("---");
                                    actual.append(allAccounts.get(0).checkStock());
                                } else {
                                    for (int i = 0; i < allAccounts.size() - 1; i++) {
                                        actual.append(allAccounts.get(i).getTraderID());
                                        actual.append("---");
                                        actual.append(allAccounts.get(i).checkStock());
                                        actual.append(" ");
                                    }
                                    actual.append(allAccounts.get(allAccounts.size() - 1).getTraderID());
                                    actual.append("---");
                                    actual.append(allAccounts.get(allAccounts.size() - 1).checkStock());
                                }
                                market.getLock().readLock().unlock();
                                StringBuilder fromClient = new StringBuilder();
                                if (substrings.length == 2) {
                                    fromClient.append(substrings[substrings.length - 1]);
                                } else {
                                    for (int i = 1; i < substrings.length - 1; i++) {
                                        fromClient.append(substrings[i]);
                                        fromClient.append(" ");
                                    }
                                    fromClient.append(substrings[substrings.length - 1]);
                                }

                                if (actual.toString().equals(fromClient.toString())) {
                                    writer.println("UPTODATE");
                                } else {
                                    writer.println("OBSOLETE");
                                }
                                break;

                            case "transfer":
                                String toTrader = substrings[2];
                                market.transfer(clientReadID, toTrader);
                                writer.println("SUCCESS");

                                updateServer(market);
                                break;

                            default:
                                throw new Exception("Unknown command: " + line);
                        }
                    }
                } catch (Exception e) {
                    System.out.println(clientID + " left unexpectedly.");
                    market.removeTrader(clientID);
                    socket.close();
                    updateServer(market);
                    Thread.currentThread().stop();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}