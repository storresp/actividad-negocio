package com.actividad.player_service.infrastructure;

import com.actividad.player_service.application.PlayerService;
import com.actividad.player_service.domain.InventoryItem;
import com.actividad.player_service.domain.Player;
import com.actividad.player_service.domain.PlayerStatus;
import com.actividad.player_service.exception.GlobalExceptionHandler;
import com.actividad.player_service.infrastructure.controller.PlayerController;
import com.actividad.player_service.infrastructure.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PlayerControllerIntegrationTest {

    private MockMvc mockMvc;

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerService playerService;

    @BeforeEach
    void setUp() {
        PlayerController playerController = new PlayerController(playerService);
        mockMvc = MockMvcBuilders.standaloneSetup(playerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldCreatePlayerFromHttpRequest() throws Exception {
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> {
            Player player = invocation.getArgument(0);
            player.setId("player-1");
            return player;
        });

        mockMvc.perform(post("/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Laura",
                                  "coins": 120
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("player-1"))
                .andExpect(jsonPath("$.name").value("Laura"))
                .andExpect(jsonPath("$.coins").value(120))
                .andExpect(jsonPath("$.status").value("ACTIVO"))
                .andExpect(jsonPath("$.inventory").isArray())
                .andExpect(jsonPath("$.inventory").isEmpty());
    }

    @Test
    void shouldReturnPlayerById() throws Exception {
        when(playerRepository.findById("player-1"))
                .thenReturn(Optional.of(buildPlayer("player-1", 90, List.of())));

        mockMvc.perform(get("/players/player-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("player-1"))
                .andExpect(jsonPath("$.name").value("Test Player"))
                .andExpect(jsonPath("$.coins").value(90))
                .andExpect(jsonPath("$.status").value("ACTIVO"));
    }

    @Test
    void shouldReturnNotFoundWhenPlayerDoesNotExist() throws Exception {
        when(playerRepository.findById("missing-player")).thenReturn(Optional.empty());

        mockMvc.perform(get("/players/missing-player"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Jugador no encontrado con id: missing-player"));
    }

    @Test
    void shouldRejectInvalidPlayerCreationRequest() throws Exception {
        mockMvc.perform(post("/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "coins": -5
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("El nombre es obligatorio"))
                .andExpect(jsonPath("$.coins").value("Las monedas no pueden ser negativas"));
    }

    @Test
    void shouldCreditCoinsThroughWalletEndpoint() throws Exception {
        Player existingPlayer = buildPlayer("player-1", 80, List.of());

        when(playerRepository.findById("player-1")).thenReturn(Optional.of(existingPlayer));
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(patch("/players/player-1/wallet/credit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("player-1"))
                .andExpect(jsonPath("$.coins").value(100));

        verify(playerRepository).save(existingPlayer);
    }

    @Test
    void shouldApplyPurchaseAndUpdateInventory() throws Exception {
        Player existingPlayer = buildPlayer("player-1", 100, new ArrayList<>());

        when(playerRepository.findById("player-1")).thenReturn(Optional.of(existingPlayer));
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/players/player-1/purchases/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "itemId": "sword",
                                  "quantity": 2,
                                  "totalPrice": 40
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coins").value(60))
                .andExpect(jsonPath("$.inventory[0].itemId").value("sword"))
                .andExpect(jsonPath("$.inventory[0].quantity").value(2));

        verify(playerRepository).save(existingPlayer);
    }

    private Player buildPlayer(String id, int coins, List<InventoryItem> inventory) {
        return Player.builder()
                .id(id)
                .name("Test Player")
                .coins(coins)
                .status(PlayerStatus.ACTIVO)
                .inventory(inventory)
                .build();
    }
}
