package com.doherty.tradinghoursprocessor;

public class StockQuoteDTO {

    public StockQuoteDTO() {
    }

    public String symbol;
    public String name;
    public double price;
    public double changesPercentage;
    public long marketCap;
    public int volume;
    public int avgVolume;
    public double pe;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getChangesPercentage() {
        return changesPercentage;
    }

    public void setChangesPercentage(double changesPercentage) {
        this.changesPercentage = changesPercentage;
    }

    public long getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(long marketCap) {
        this.marketCap = marketCap;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getAvgVolume() {
        return avgVolume;
    }

    public void setAvgVolume(int avgVolume) {
        this.avgVolume = avgVolume;
    }

    public double getPe() {
        return pe;
    }

    public void setPe(double pe) {
        this.pe = pe;
    }
}
