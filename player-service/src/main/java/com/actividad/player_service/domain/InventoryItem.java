package com.actividad.player_service.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem {

    @NotBlank(message = "El itemId es obligatorio")
    private String itemId;

    @Min(value = 1, message = "La cantidad debe ser mayor que 0")
    private int quantity;
}
