package com.actividad.store_service.infrastructure.client;

import com.actividad.store_service.exception.PlayerNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class PlayerClient {

    private final WebClient webClient;

    public PlayerClient(WebClient.Builder webClientBuilder, @Value("${player-service.url}") String playerServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(playerServiceUrl).build();
    }

    public boolean playerExists(String playerId) {
        try {
            this.webClient.get()
                    .uri("/players/{id}", playerId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, response -> {
                        throw new PlayerNotFoundException("Player not found with id: " + playerId);
                    })
                    .toBodilessEntity()
                    .block();
            return true;
        } catch (PlayerNotFoundException e) {
            throw e;
        } catch (Exception e) {
            return false;
        }
    }

    public Integer getPlayerCoins(String playerId) {
        return this.webClient.get()
                .uri("/players/{id}/coins", playerId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    throw new PlayerNotFoundException("Player not found with id: " + playerId);
                })
                .bodyToMono(Integer.class)
                .block();
    }

    public void debitCoins(String playerId, Integer amount) {
        this.webClient.patch()
                .uri(uriBuilder -> uriBuilder
                        .path("/players/{id}/coins/debit")
                        .queryParam("amount", amount)
                        .build(playerId))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    throw new RuntimeException("Error debiting coins from player: " + playerId);
                })
                .toBodilessEntity()
                .block();
    }
}
