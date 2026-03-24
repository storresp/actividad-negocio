package com.actividad.player_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatePlayerRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @Min(value = 0, message = "Las monedas no pueden ser negativas")
    private int coins;
}
