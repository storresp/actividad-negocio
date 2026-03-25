package com.actividad.store_service.infrastructure.controller;

import com.actividad.store_service.domain.Item;
import com.actividad.store_service.dto.PurchaseRequest;
import com.actividad.store_service.infrastructure.client.PlayerClient;
import com.actividad.store_service.infrastructure.repository.InventoryRepository;
import com.actividad.store_service.infrastructure.repository.ItemRepository;
import com.actividad.store_service.infrastructure.repository.PurchaseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PurchaseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @MockBean
    private PlayerClient playerClient;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        itemRepository.deleteAll();
        purchaseRepository.deleteAll();
        inventoryRepository.deleteAll();
    }

    @Test
    void buyItem_Success() throws Exception {
        // Arrange
        Item item = Item.builder().name("Magic Wand").price(200).build();
        item = itemRepository.save(item);

        String playerId = "player-123";

        // Mock external player service response
        when(playerClient.playerExists(playerId)).thenReturn(true);

        PurchaseRequest request = PurchaseRequest.builder()
                .playerId(playerId)
                .itemId(item.getId())
                .build();

        // Act & Assert
        mockMvc.perform(post("/purchases")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(playerClient).applyPurchase(playerId, item.getId(), 1, item.getPrice());
    }
}
