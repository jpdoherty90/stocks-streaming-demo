package com.doherty.financialdataprocessor;

public class BalanceSheet {

    public BalanceSheet() {
    }

    public String date;
    public String symbol;
    public String reportedCurrency;
    public String fillingDate;
    public String calendarYear;
    public String period;
    public long cashAndCashEquivalents;
    public long cashAndShortTermInvestments;
    public long inventory;
    public long totalAssets;
    public long totalLiabilities;
    public long totalEquity;
    public long totalDebt;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getReportedCurrency() {
        return reportedCurrency;
    }

    public void setReportedCurrency(String reportedCurrency) {
        this.reportedCurrency = reportedCurrency;
    }

    public String getFillingDate() {
        return fillingDate;
    }

    public void setFillingDate(String fillingDate) {
        this.fillingDate = fillingDate;
    }

    public String getCalendarYear() {
        return calendarYear;
    }

    public void setCalendarYear(String calendarYear) {
        this.calendarYear = calendarYear;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public long getCashAndCashEquivalents() {
        return cashAndCashEquivalents;
    }

    public void setCashAndCashEquivalents(long cashAndCashEquivalents) {
        this.cashAndCashEquivalents = cashAndCashEquivalents;
    }

    public long getCashAndShortTermInvestments() {
        return cashAndShortTermInvestments;
    }

    public void setCashAndShortTermInvestments(long cashAndShortTermInvestments) {
        this.cashAndShortTermInvestments = cashAndShortTermInvestments;
    }

    public long getInventory() {
        return inventory;
    }

    public void setInventory(long inventory) {
        this.inventory = inventory;
    }

    public long getTotalAssets() {
        return totalAssets;
    }

    public void setTotalAssets(long totalAssets) {
        this.totalAssets = totalAssets;
    }

    public long getTotalLiabilities() {
        return totalLiabilities;
    }

    public void setTotalLiabilities(long totalLiabilities) {
        this.totalLiabilities = totalLiabilities;
    }

    public long getTotalEquity() {
        return totalEquity;
    }

    public void setTotalEquity(long totalEquity) {
        this.totalEquity = totalEquity;
    }

    public long getTotalDebt() {
        return totalDebt;
    }

    public void setTotalDebt(long totalDebt) {
        this.totalDebt = totalDebt;
    }

}
