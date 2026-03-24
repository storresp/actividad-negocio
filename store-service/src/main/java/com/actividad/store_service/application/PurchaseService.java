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

        // Aplicar la compra directamente en el player-service (valida saldo, descuenta
        // y añade al inventario del player)
        playerClient.applyPurchase(playerId, itemId, 1, item.getPrice());

        // Registrar la transacción de compra localmente en el historial del
        // store-service
        Purchase purchase = Purchase.builder()
                .playerId(playerId)
                .itemId(itemId)
                .price(item.getPrice())
                .timestamp(LocalDateTime.now())
                .build();
        purchaseRepository.save(purchase);
    }
}
