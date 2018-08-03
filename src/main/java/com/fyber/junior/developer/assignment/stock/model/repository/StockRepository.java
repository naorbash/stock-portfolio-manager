package com.fyber.junior.developer.assignment.stock.model.repository;

import com.fyber.junior.developer.assignment.stock.model.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StockRepository extends JpaRepository<Stock,Long> {

    List<Stock> findByClientId (long clientId);
    Stock findByStockSymbolAndClientId(String StockSymbol,Long clientId);
    void deleteByClientId(long clientId);
}
