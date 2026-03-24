package com.actividad.store_service.application;

import com.actividad.store_service.domain.Inventory;
import com.actividad.store_service.domain.Item;
import com.actividad.store_service.domain.Purchase;
import com.actividad.store_service.exception.InsufficientCoinsException;
import com.actividad.store_service.exception.ItemNotFoundException;
import com.actividad.store_service.exception.PlayerNotFoundException;
import com.actividad.store_service.infrastructure.client.PlayerClient;
import com.actividad.store_service.infrastructure.repository.InventoryRepository;
import com.actividad.store_service.infrastructure.repository.ItemRepository;
import com.actividad.store_service.infrastructure.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final ItemRepository itemRepository;
    private final PurchaseRepository purchaseRepository;
    private final InventoryRepository inventoryRepository;
    private final PlayerClient playerClient;

    public void buyItem(String playerId, String itemId) {
        // Consultar el item y validar que exista
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item not found with id: " + itemId));

        // Consultar al player-service: verificar jugador
        if (!playerClient.playerExists(playerId)) {
            throw new PlayerNotFoundException("Player not found with id: " + playerId);
        }

        // Consultar al player-service: obtener monedas
        Integer coins = playerClient.getPlayerCoins(playerId);

        // Validar monedas suficientes
        if (coins == null || coins < item.getPrice()) {
            throw new InsufficientCoinsException("Not enough coins to buy item: " + itemId);
        }

        // Descontar monedas vía REST
        playerClient.debitCoins(playerId, item.getPrice());

        // Registrar la compra
        Purchase purchase = Purchase.builder()
                .playerId(playerId)
                .itemId(itemId)
                .price(item.getPrice())
                .timestamp(LocalDateTime.now())
                .build();
        purchaseRepository.save(purchase);

        // Actualizar inventario
        Inventory inventory = inventoryRepository.findByPlayerId(playerId)
                .orElse(Inventory.builder()
                        .playerId(playerId)
                        .build());
        
        inventory.getItems().add(item.getId());
        inventoryRepository.save(inventory);
    }
}
