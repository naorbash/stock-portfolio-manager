package com.fyber.junior.developer.assignment.stock.model.repository;

import com.fyber.junior.developer.assignment.stock.model.entity.Client;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends CrudRepository<Client,Long> {
    Client findByClientId(long clientId);

}
