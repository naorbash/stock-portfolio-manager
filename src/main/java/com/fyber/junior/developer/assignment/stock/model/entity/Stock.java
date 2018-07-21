package com.fyber.junior.developer.assignment.stock.model.entity;

import javax.persistence.Id;
import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;


@Entity
@Table(name = "STOCKS")
public class Stock {

    //default constructor
    public Stock(){}

    public Stock(String stockSymbol){
        this.stockSymbol = stockSymbol;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="STOCK_ID")
    private long stockId;

    @NotNull
    @Size(min=2, max=6)
    @Pattern(regexp="^[A-Z]*$")
    @Column(name="STOCK_SYMBOL")
    private String stockSymbol;

    @NotNull
    @Min(1)
    @Column(name="STOCK_AMOUNT")
    private int stockAmount;

    @Column(name="CLIENT_ID")
    private long clientId;

    @Override
    public boolean equals(Object o) {

        //If the object is compared with itself then return true
        if (o == this) {
            return true;
        }

       //Checking if o is an instance of Stock or not
        if (!(o instanceof Stock)) {
            return false;
        }

        //typecast o to Stock
        Stock stock = (Stock)o;

        return(stock.getstockSymbol().equals(this.getstockSymbol()));
    }

    public long getStockId()
    {
        return stockId;
    }

    public void setStockId(long stockId)
    {
        this.stockId = stockId;
    }

    public String getstockSymbol()
    {
        return stockSymbol;
    }

    public void setstockSymbol(String stockSymbol)
    {
        this.stockSymbol = stockSymbol;
    }

    public int getStockAmount()
    {
        return stockAmount;
    }

    public void setStockAmount(int stockAmount)
    {
        this.stockAmount = stockAmount;
    }

    public long getClientId()
    {
        return clientId;
    }

    public void setClientId(long clientId)
    {
        this.clientId = clientId;
    }
}
