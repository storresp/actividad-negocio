package com.actividad.store_service.infrastructure.repository;

import com.actividad.store_service.domain.Inventory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends MongoRepository<Inventory, String> {
    Optional<Inventory> findByPlayerId(String playerId);
}
