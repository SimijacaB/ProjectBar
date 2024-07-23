package com.app.projectbar.domain.dto;


import com.app.projectbar.domain.OrderItem;
import com.app.projectbar.domain.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponseDTO {

    private Long id;

    private String clientName;

    private Integer tableNumber;

    private String waiterUserName;

    private String notes;

    private Status status;

    private LocalDateTime date;

    private List<OrderItem> orderProducts;

    private Double valueToPay;

}
