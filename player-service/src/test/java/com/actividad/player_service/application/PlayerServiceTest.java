package com.actividad.player_service.application;

import com.actividad.player_service.domain.InventoryItem;
import com.actividad.player_service.domain.Player;
import com.actividad.player_service.domain.PlayerStatus;
import com.actividad.player_service.dto.CreatePlayerRequest;
import com.actividad.player_service.dto.PurchaseRequest;
import com.actividad.player_service.exception.BusinessException;
import com.actividad.player_service.exception.ResourceNotFoundException;
import com.actividad.player_service.infrastructure.repository.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerService playerService;

    @Test
    void shouldCreatePlayerWithActiveStatus() {
        CreatePlayerRequest request = new CreatePlayerRequest();
        request.setName("Laura");
        request.setCoins(120);

        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Player createdPlayer = playerService.createPlayer(request);

        ArgumentCaptor<Player> playerCaptor = ArgumentCaptor.forClass(Player.class);
        verify(playerRepository).save(playerCaptor.capture());

        Player savedPlayer = playerCaptor.getValue();
        assertEquals("Laura", savedPlayer.getName());
        assertEquals(120, savedPlayer.getCoins());
        assertEquals(PlayerStatus.ACTIVO, savedPlayer.getStatus());
        assertNotNull(savedPlayer.getInventory());
        assertSame(savedPlayer, createdPlayer);
    }

    @Test
    void shouldReturnPlayerWhenIdExists() {
        Player player = buildPlayer("player-1", 90, PlayerStatus.ACTIVO, new ArrayList<>());
        when(playerRepository.findById("player-1")).thenReturn(Optional.of(player));

        Player result = playerService.getPlayerById("player-1");

        assertSame(player, result);
    }

    @Test
    void shouldThrowWhenPlayerDoesNotExist() {
        when(playerRepository.findById("missing-player")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> playerService.getPlayerById("missing-player")
        );

        assertEquals("Jugador no encontrado con id: missing-player", exception.getMessage());
    }

    @Test
    void shouldCreditCoinsForActivePlayer() {
        Player player = buildPlayer("player-1", 50, PlayerStatus.ACTIVO, new ArrayList<>());
        when(playerRepository.findById("player-1")).thenReturn(Optional.of(player));
        when(playerRepository.save(player)).thenReturn(player);

        Player updatedPlayer = playerService.creditCoins("player-1", 25);

        assertEquals(75, updatedPlayer.getCoins());
        verify(playerRepository).save(player);
    }

    @Test
    void shouldRejectCoinCreditForInactivePlayer() {
        Player player = buildPlayer("player-1", 50, PlayerStatus.BLOQUEADO, new ArrayList<>());
        when(playerRepository.findById("player-1")).thenReturn(Optional.of(player));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> playerService.creditCoins("player-1", 25)
        );

        assertEquals("El jugador no está habilitado para operar", exception.getMessage());
        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    void shouldApplyPurchaseAndAddNewInventoryItem() {
        Player player = buildPlayer("player-1", 100, PlayerStatus.ACTIVO, new ArrayList<>());
        PurchaseRequest request = new PurchaseRequest();
        request.setItemId("sword");
        request.setQuantity(2);
        request.setTotalPrice(40);

        when(playerRepository.findById("player-1")).thenReturn(Optional.of(player));
        when(playerRepository.save(player)).thenReturn(player);

        Player updatedPlayer = playerService.applyPurchase("player-1", request);

        assertEquals(60, updatedPlayer.getCoins());
        assertEquals(1, updatedPlayer.getInventory().size());
        assertEquals("sword", updatedPlayer.getInventory().get(0).getItemId());
        assertEquals(2, updatedPlayer.getInventory().get(0).getQuantity());
        verify(playerRepository).save(player);
    }

    @Test
    void shouldApplyPurchaseAndIncreaseExistingInventoryItemQuantity() {
        List<InventoryItem> inventory = new ArrayList<>();
        inventory.add(InventoryItem.builder().itemId("shield").quantity(1).build());
        Player player = buildPlayer("player-1", 100, PlayerStatus.ACTIVO, inventory);

        PurchaseRequest request = new PurchaseRequest();
        request.setItemId("shield");
        request.setQuantity(3);
        request.setTotalPrice(30);

        when(playerRepository.findById("player-1")).thenReturn(Optional.of(player));
        when(playerRepository.save(player)).thenReturn(player);

        Player updatedPlayer = playerService.applyPurchase("player-1", request);

        assertEquals(70, updatedPlayer.getCoins());
        assertEquals(1, updatedPlayer.getInventory().size());
        assertEquals(4, updatedPlayer.getInventory().get(0).getQuantity());
    }

    @Test
    void shouldRejectPurchaseWhenPlayerHasInsufficientCoins() {
        Player player = buildPlayer("player-1", 20, PlayerStatus.ACTIVO, new ArrayList<>());
        PurchaseRequest request = new PurchaseRequest();
        request.setItemId("armor");
        request.setQuantity(1);
        request.setTotalPrice(50);

        when(playerRepository.findById("player-1")).thenReturn(Optional.of(player));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> playerService.applyPurchase("player-1", request)
        );

        assertEquals("El jugador no tiene monedas suficientes", exception.getMessage());
        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    void shouldRejectPurchaseForInactivePlayer() {
        Player player = buildPlayer("player-1", 100, PlayerStatus.BLOQUEADO, new ArrayList<>());
        PurchaseRequest request = new PurchaseRequest();
        request.setItemId("armor");
        request.setQuantity(1);
        request.setTotalPrice(50);

        when(playerRepository.findById("player-1")).thenReturn(Optional.of(player));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> playerService.applyPurchase("player-1", request)
        );

        assertEquals("El jugador no está habilitado para operar", exception.getMessage());
        verify(playerRepository, never()).save(any(Player.class));
    }

    private Player buildPlayer(String id, int coins, PlayerStatus status, List<InventoryItem> inventory) {
        return Player.builder()
                .id(id)
                .name("Test Player")
                .coins(coins)
                .status(status)
                .inventory(inventory)
                .build();
    }
}
