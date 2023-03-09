package com.doherty.financialdataprocessor;

public class InstitutionalOwnership {

    public InstitutionalOwnership() {
    }

    public String symbol;
    public String date;
    public double ownershipPercent;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getOwnershipPercent() {
        return ownershipPercent;
    }

    public void setOwnershipPercent(double ownershipPercent) {
        this.ownershipPercent = ownershipPercent;
    }

}
