package com.actividad.store_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequest {
    @NotBlank(message = "playerId is required")
    private String playerId;

    @NotBlank(message = "itemId is required")
    private String itemId;
}
