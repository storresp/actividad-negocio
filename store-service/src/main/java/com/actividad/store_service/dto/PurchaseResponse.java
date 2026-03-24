package com.actividad.store_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseResponse {
    private String id;
    private String playerId;
    private String itemId;
    private Integer price;
    private LocalDateTime timestamp;
}
