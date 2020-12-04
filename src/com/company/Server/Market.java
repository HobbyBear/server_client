package com.company.Server;

import java.util.ArrayList;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Market {
    private ArrayList<Trader> clients = new ArrayList<>();

    public ReentrantReadWriteLock getLock() {
        return lock;
    }

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();


    public void addTrader(Trader input) {
        lock.writeLock().lock();
        clients.add(input);
        if (clients.size() == 1) {
            input.receivedStock();
        }
        lock.writeLock().unlock();
    }

    public void removeTrader(String input) {
        lock.writeLock().lock();
        Trader target = findTraderWithNoLock(input);
        boolean haveStock = target.checkStock();
        if (haveStock && clients.size() > 1) {// have stock not the last
            System.out.println(input + " is leaving but have stock. The stock will be randomly reassigned");

            clients.remove(target);

            int newOwner = (int) (Math.random() * (clients.size() - 1));

            clients.get(newOwner).obtainStock();

            System.out.println("Trader " + clients.get(newOwner).getTraderID() + " will have the stock");

        } else if (!haveStock && clients.size() > 1){
            clients.remove(target);
            System.out.println("trader is leaving, the stock will be  hold by exiting trader");

        }else {//last, will always have stock

            clients.remove(target);
            System.out.println("last trader is leaving, the stock will be temporarily hold by market");
        }

        lock.writeLock().unlock();

    }

    public ArrayList<Trader> getClients() {
        return clients;
    }

    public Trader findTrader(String target) {
        lock.readLock().lock();
        for (Trader currentTrader : clients) {
            if (currentTrader.getTraderID().equals(target)) {
                lock.readLock().unlock();
                return currentTrader;
            }
        }
        lock.readLock().unlock();
        return null;
    }

    public Trader findTraderWithNoLock(String target) {
        for (Trader currentTrader : clients) {
            if (currentTrader.getTraderID().equals(target)) {
                return currentTrader;
            }
        }
        return null;
    }

    public void transfer(String origin, String recipient) throws Exception {
        lock.writeLock().lock();
        Trader sender = findTraderWithNoLock(origin);
        Trader receiver = findTraderWithNoLock(recipient);
        if (sender != null && receiver != null) {
            if (sender.removeStock() && receiver.obtainStock()) {
                System.out.println(origin + " gave stock to " + recipient);
                lock.writeLock().unlock();
            } else {
                sender.obtainStock();
                receiver.removeStock();
                lock.writeLock().unlock();
                throw new Exception("Transfer failed, stock status will not change");
            }
        }
    }
}