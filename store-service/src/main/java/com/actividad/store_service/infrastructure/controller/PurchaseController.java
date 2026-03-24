package com.actividad.store_service.infrastructure.controller;

import com.actividad.store_service.application.PurchaseService;
import com.actividad.store_service.dto.PurchaseRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @PostMapping
    public ResponseEntity<Void> buyItem(@Valid @RequestBody PurchaseRequest request) {
        purchaseService.buyItem(request.getPlayerId(), request.getItemId());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
