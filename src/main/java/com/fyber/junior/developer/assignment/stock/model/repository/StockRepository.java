package com.fyber.junior.developer.assignment.stock.model.repository;

import com.fyber.junior.developer.assignment.stock.model.entity.Stock;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface StockRepository extends CrudRepository<Stock,Long> {

    Stock findByStockId(long stockId);
    List<Stock> findByClientId (long clientId);
    Stock findByStockSymbolAndClientId(String StockSymbol,Long clientId);
    void deleteByClientId(long clientId);
}
