package com.actividad.store_service.infrastructure.controller;

import com.actividad.store_service.domain.Item;
import com.actividad.store_service.dto.CreateItemRequest;
import com.actividad.store_service.dto.ItemResponse;
import com.actividad.store_service.infrastructure.repository.ItemRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemRepository itemRepository;

    @PostMapping
    public ResponseEntity<ItemResponse> createItem(@Valid @RequestBody CreateItemRequest request) {
        Item item = Item.builder()
                .name(request.getName())
                .price(request.getPrice())
                .build();
        
        Item savedItem = itemRepository.save(item);
        
        ItemResponse response = ItemResponse.builder()
                .id(savedItem.getId())
                .name(savedItem.getName())
                .price(savedItem.getPrice())
                .build();
                
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ItemResponse>> listItems() {
        List<ItemResponse> items = itemRepository.findAll().stream()
                .map(item -> ItemResponse.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .price(item.getPrice())
                        .build())
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(items);
    }
}
