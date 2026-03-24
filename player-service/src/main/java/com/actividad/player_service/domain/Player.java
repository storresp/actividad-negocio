package com.actividad.player_service.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "players")
public class Player {

    @Id
    private String id;

    private String name;

    private int coins;

    @Builder.Default
    private PlayerStatus status = PlayerStatus.ACTIVO;

    @Builder.Default
    private List<InventoryItem> inventory = new ArrayList<>();
}
