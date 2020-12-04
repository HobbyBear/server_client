package com.company.client;

import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ClientProgram {
    public static void main(String[] args) throws Exception {
        final String traderID = UUID.randomUUID().toString();
        System.out.println("Type [exit] in any way to exit the trading market");

        try (final Client client = new Client(traderID)) {
            if (client.addTrader(traderID)){
                System.out.println(traderID + " Logged in successfully.");
            }else {
                System.out.println(traderID + " Logged in fail.");
                return;
            }

            Thread updateThread = new Thread(new Runnable() {
                public void run() {
                    String[] traderInfo = client.getTraders();

                    System.out.println("Traders list and stock ownership");
                    System.out.println("-------------------------------------------------");
                    for (String s : traderInfo) {
                        System.out.println(s);
                    }
                    System.out.println("-------------------------------------------------");
                    System.out.println(" ");

                    while (true) {
                        StringBuilder temp = new StringBuilder();
                        if (traderInfo.length == 1) {
                            temp.append(traderInfo[0]);
                        } else {
                            for (int i = 0; i < traderInfo.length - 1; i++) {
                                temp.append(traderInfo[i]);
                                temp.append(" ");
                            }
                            temp.append(traderInfo[traderInfo.length - 1]);
                        }

                        if (client.checkUpdate(temp.toString().trim())) {//OBSOLETE
                            if (client.checkStock(traderID)) {
                                traderInfo = client.getTraders();

                                System.out.println("Up to date Traders list and stock ownership");
                                System.out.println("-------------------------------------------------");
                                for (String s : traderInfo) {
                                    System.out.println(s);
                                }
                                System.out.println("-------------------------------------------------");
                                System.out.println("Choose from above to transfer your stock");

                            } else {
                                traderInfo = client.getTraders();

                                System.out.println("Up to date Traders list and stock ownership");
                                System.out.println("-------------------------------------------------");
                                for (String s : traderInfo) {
                                    System.out.println(s);
                                }
                                System.out.println("-------------------------------------------------");
                                System.out.println(" ");
                            }
                        }
                        try {
                            TimeUnit.MILLISECONDS.sleep(1000);
                        } catch (InterruptedException exception) {
                            exception.printStackTrace();
                        }
                    }
                }
            });

            Thread opThread = new Thread(new Runnable() {
                public void run() {
                    boolean haveStock = false;
                    boolean printed = false;
                    Scanner scanner = new Scanner(System.in);
                    try {//wait for first loop to populate
                        TimeUnit.MILLISECONDS.sleep(500);
                    } catch (InterruptedException exception) {
                        exception.printStackTrace();
                    }

                    while (true) {
                        if (client.checkStock(traderID) && !printed) {//check stock
                            haveStock = true;
                            System.out.println("Choose from the following to transfer your stock");
                            System.out.println("-------------------------------------------------");
                            String[] traderInfo = client.getTraders();
                            for (String s : traderInfo) {
                                System.out.println(s);
                            }
                            System.out.println("-------------------------------------------------");
                            printed = true;
                        }

                        String userInput = scanner.nextLine();

                        if (userInput.equals("")) {
                            System.out.println("The input cannot be null.");
                        } else {//if something was typed
                            if (userInput.equals("exit")) {
                                client.exit(traderID);
                                System.exit(0);
                            } else if (haveStock) {//if have stock
                                if (userInput.length() == traderID.length()) {//if input format correct
                                    if (traderID.equals(userInput.trim())) {
                                        client.transfer(traderID, userInput);
                                        System.out.println("you gave the stock to yourself");
                                        System.out.println("Choose from the following to transfer your stock");
                                        System.out.println("-------------------------------------------------");
                                        String[] traderInfo = client.getTraders();
                                        for (String s : traderInfo) {
                                            System.out.println(s);
                                        }
                                        System.out.println("-------------------------------------------------");
                                    } else if (client.transfer(traderID, userInput)) {
                                        System.out.println("Trade succeed");
                                        haveStock = client.checkStock(traderID);
                                    } else {
                                        System.out.println("Trade failed, check your entry");
                                    }
                                } else {
                                    System.out.println("Check your entry");
                                }
                            }
                        }
                    }
                }
            });

            updateThread.start();
            opThread.start();

            updateThread.join();
            opThread.join();
        }
    }
}