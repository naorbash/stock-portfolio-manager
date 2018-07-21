package com.fyber.junior.developer.assignment.stock.model.entity;

import javax.persistence.Id;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
@Table(name = "CLIENTS")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull
    @Column(name="CLIENT_ID")
    private long clientId;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "CLIENT_ID")
    private List<Stock> stocksList;

    public long getClientId()
    {
        return clientId;
    }

    public void setClientId(long clientId)
    {
        this.clientId = clientId;
    }

    public List<Stock> getStocksList()
    {
        return stocksList;
    }

    public void setStocksList(List<Stock> stocksList)
    {
        this.stocksList = stocksList;
    }
}
