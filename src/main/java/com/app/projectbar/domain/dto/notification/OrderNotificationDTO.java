package com.app.projectbar.domain.dto.notification;

import com.app.projectbar.domain.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderNotificationDTO {
    
    private Long orderId;
    private String clientName;
    private Integer tableNumber;
    private Double valueToPay;
    private OrderStatus status;
    private String waiterUserName;
    private String notes;
    private LocalDateTime date;
    private NotificationType type;
    private String message;
    private String assignedTo; // Rol o username específico
    
    // Lista de productos en la orden - importante para meseros, bartender y chef
    private List<OrderItemNotificationDTO> items;
    
    public enum NotificationType {
        NEW_ORDER,           // Nueva orden creada
        ORDER_ASSIGNED,      // Orden asignada a un mesero
        ORDER_STATUS_CHANGED, // Cambio de estado
        ORDER_CANCELLED      // Orden cancelada
    }
}