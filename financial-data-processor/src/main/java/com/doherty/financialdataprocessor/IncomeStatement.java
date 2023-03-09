package com.doherty.financialdataprocessor;

import lombok.Data;

@Data
public class IncomeStatement {
    public IncomeStatement() {
    }
    public String date;
    public String symbol;
    public String reportedCurrency;
    public String fillingDate;
    public String acceptedDate;
    public String calendarYear;
    public String period;
    public long ebitda;



}
