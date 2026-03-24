package com.actividad.player_service.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CreditCoinsRequest {

    @Min(value = 1, message = "La cantidad debe ser mayor que 0")
    private int amount;
}
