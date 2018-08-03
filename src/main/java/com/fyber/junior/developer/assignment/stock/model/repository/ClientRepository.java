package com.fyber.junior.developer.assignment.stock.model.repository;

import com.fyber.junior.developer.assignment.stock.model.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client,Long> {
    Client findByClientId(Long clientId);
    boolean existsByClientId(Long clientId);

}
