package com.actividad.player_service.infrastructure.controller;

import com.actividad.player_service.dto.CreatePlayerRequest;
import com.actividad.player_service.dto.CreditCoinsRequest;
import com.actividad.player_service.dto.PurchaseRequest;
import com.actividad.player_service.domain.Player;
import com.actividad.player_service.application.PlayerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    @PostMapping
    public ResponseEntity<Player> createPlayer(@Valid @RequestBody CreatePlayerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(playerService.createPlayer(request));
    }

    @GetMapping("/{playerId}")
    public ResponseEntity<Player> getPlayer(@PathVariable String playerId) {
        return ResponseEntity.ok(playerService.getPlayerById(playerId));
    }

    @PatchMapping("/{playerId}/wallet/credit")
    public ResponseEntity<Player> creditCoins(
            @PathVariable String playerId,
            @Valid @RequestBody CreditCoinsRequest request
    ) {
        return ResponseEntity.ok(playerService.creditCoins(playerId, request.getAmount()));
    }

    @PostMapping("/{playerId}/purchases/apply")
    public ResponseEntity<Player> applyPurchase(
            @PathVariable String playerId,
            @Valid @RequestBody PurchaseRequest request
    ) {
        return ResponseEntity.ok(playerService.applyPurchase(playerId, request));
    }
}
