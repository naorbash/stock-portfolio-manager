package com.fyber.junior.developer.assignment.stock.model.repository;

import com.fyber.junior.developer.assignment.stock.model.entity.Stock;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface StockRepository extends CrudRepository<Stock,Long> {

    Stock findByStockId(long stockId);
    List<Stock> findByClientId (long clientId);
    void deleteByClientId(long clientId);
}
