package com.actividad.store_service.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "purchases")
public class Purchase {
    @Id
    private String id;
    private String playerId;
    private String itemId;
    private Integer price;
    private LocalDateTime timestamp;
}
