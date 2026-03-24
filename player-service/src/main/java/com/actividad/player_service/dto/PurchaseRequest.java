package com.actividad.player_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PurchaseRequest {

    @NotBlank(message = "El itemId es obligatorio")
    private String itemId;

    @Min(value = 1, message = "La cantidad debe ser mayor que 0")
    private int quantity;

    @Min(value = 0, message = "El precio total no puede ser negativo")
    private int totalPrice;
}
