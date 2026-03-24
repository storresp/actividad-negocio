package com.actividad.store_service.infrastructure.controller;

import com.actividad.store_service.domain.Item;
import com.actividad.store_service.dto.CreateItemRequest;
import com.actividad.store_service.infrastructure.repository.ItemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ItemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        itemRepository.deleteAll();
    }

    @Test
    void createItem_Success() throws Exception {
        CreateItemRequest request = CreateItemRequest.builder()
                .name("Health Potion")
                .price(50)
                .build();

        mockMvc.perform(post("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Health Potion"))
                .andExpect(jsonPath("$.price").value(50));
    }

    @Test
    void listItems_Success() throws Exception {
        Item item1 = Item.builder().name("Sword").price(100).build();
        Item item2 = Item.builder().name("Shield").price(150).build();
        itemRepository.save(item1);
        itemRepository.save(item2);

        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Sword"))
                .andExpect(jsonPath("$[1].name").value("Shield"));
    }
}
