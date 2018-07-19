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
