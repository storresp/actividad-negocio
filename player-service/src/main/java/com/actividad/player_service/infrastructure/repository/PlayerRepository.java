package com.actividad.player_service.infrastructure.repository;

import com.actividad.player_service.domain.Player;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlayerRepository extends MongoRepository<Player, String> {
}
