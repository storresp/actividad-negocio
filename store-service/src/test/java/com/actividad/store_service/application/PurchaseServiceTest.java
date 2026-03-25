package com.actividad.store_service.application;

import com.actividad.store_service.domain.Item;
import com.actividad.store_service.exception.ItemNotFoundException;
import com.actividad.store_service.exception.PlayerNotFoundException;
import com.actividad.store_service.infrastructure.client.PlayerClient;
import com.actividad.store_service.infrastructure.repository.InventoryRepository;
import com.actividad.store_service.infrastructure.repository.ItemRepository;
import com.actividad.store_service.infrastructure.repository.PurchaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private PlayerClient playerClient;

    @InjectMocks
    private PurchaseService purchaseService;

    private Item sampleItem;
    private String playerId = "player-1";

    @BeforeEach
    void setUp() {
        sampleItem = Item.builder()
                .id("item-1")
                .name("Sword")
                .price(100)
                .build();
    }

    @Test
    void buyItem_Success() {
        // Arrange
        when(itemRepository.findById(sampleItem.getId())).thenReturn(Optional.of(sampleItem));
        when(playerClient.playerExists(playerId)).thenReturn(true);

        // Act
        purchaseService.buyItem(playerId, sampleItem.getId());

        // Assert
        verify(playerClient).applyPurchase(playerId, sampleItem.getId(), 1, sampleItem.getPrice());
        verify(purchaseRepository).save(any());
    }

    @Test
    void buyItem_ThrowsItemNotFoundException() {
        // Arrange
        when(itemRepository.findById("invalid-item")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ItemNotFoundException.class, () -> purchaseService.buyItem(playerId, "invalid-item"));
        verifyNoInteractions(playerClient);
        verifyNoInteractions(purchaseRepository);
    }

    @Test
    void buyItem_ThrowsPlayerNotFoundException() {
        // Arrange
        when(itemRepository.findById(sampleItem.getId())).thenReturn(Optional.of(sampleItem));
        when(playerClient.playerExists(playerId)).thenReturn(false);

        // Act & Assert
        assertThrows(PlayerNotFoundException.class, () -> purchaseService.buyItem(playerId, sampleItem.getId()));
        verify(playerClient, never()).applyPurchase(anyString(), anyString(), anyInt(), anyInt());
        verifyNoInteractions(purchaseRepository);
    }

    @Test
    void buyItem_ThrowsInsufficientCoinsException() {
        // Arrange
        when(itemRepository.findById(sampleItem.getId())).thenReturn(Optional.of(sampleItem));
        when(playerClient.playerExists(playerId)).thenReturn(true);
        doThrow(new RuntimeException("Error o saldo insuficiente al aplicar compra para el jugador: " + playerId))
                .when(playerClient).applyPurchase(playerId, sampleItem.getId(), 1, sampleItem.getPrice());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> purchaseService.buyItem(playerId, sampleItem.getId()));
        verifyNoInteractions(purchaseRepository);
    }
}
