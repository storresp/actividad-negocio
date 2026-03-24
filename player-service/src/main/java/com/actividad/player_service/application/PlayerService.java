package com.actividad.player_service.application;

import com.actividad.player_service.dto.CreatePlayerRequest;
import com.actividad.player_service.dto.PurchaseRequest;
import com.actividad.player_service.exception.BusinessException;
import com.actividad.player_service.exception.ResourceNotFoundException;
import com.actividad.player_service.domain.InventoryItem;
import com.actividad.player_service.domain.Player;
import com.actividad.player_service.domain.PlayerStatus;
import com.actividad.player_service.infrastructure.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;

    public Player createPlayer(CreatePlayerRequest request) {
        Player player = Player.builder()
                .name(request.getName())
                .coins(request.getCoins())
                .status(PlayerStatus.ACTIVO)
                .build();

        return playerRepository.save(player);
    }

    public Player getPlayerById(String playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado con id: " + playerId));
    }

    public Player creditCoins(String playerId, int amount) {
        Player player = getPlayerById(playerId);
        validatePlayerActive(player);

        player.setCoins(player.getCoins() + amount);
        return playerRepository.save(player);
    }

    public Player applyPurchase(String playerId, PurchaseRequest request) {
        Player player = getPlayerById(playerId);

        validatePlayerActive(player);

        if (player.getCoins() < request.getTotalPrice()) {
            throw new BusinessException("El jugador no tiene monedas suficientes");
        }

        player.setCoins(player.getCoins() - request.getTotalPrice());
        addItemToInventory(player, request.getItemId(), request.getQuantity());

        return playerRepository.save(player);
    }

    private void addItemToInventory(Player player, String itemId, int quantity) {
        for (InventoryItem item : player.getInventory()) {
            if (item.getItemId().equals(itemId)) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }

        player.getInventory().add(
                InventoryItem.builder()
                        .itemId(itemId)
                        .quantity(quantity)
                        .build()
        );
    }

    private void validatePlayerActive(Player player) {
        if (player.getStatus() != PlayerStatus.ACTIVO) {
            throw new BusinessException("El jugador no está habilitado para operar");
        }
    }
}
