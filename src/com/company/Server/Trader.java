package com.company.Server;

import java.util.concurrent.atomic.AtomicBoolean;

public class Trader {
    private final String traderID;
    private final AtomicBoolean haveStock = new AtomicBoolean();

    public Trader(String id) {
        traderID = id;
        haveStock.set(false);
    }

    public String getTraderID() {
        return traderID;
    }

    public boolean checkStock() {
        return haveStock.get();
    }

    public boolean removeStock() {//for transfer stock
        return haveStock.getAndSet(false);
    }

    public boolean obtainStock() {//for transfer stock
        return !haveStock.getAndSet(true);
    }

    public void receivedStock() {//get stock when this trader is the first to join
        haveStock.set(true);
    }
}