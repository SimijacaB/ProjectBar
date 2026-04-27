package com.app.projectbar.application.implementation;

import com.app.projectbar.application.interfaces.IOrderNotificationService;
import com.app.projectbar.domain.Order;
import com.app.projectbar.domain.OrderItem;
import com.app.projectbar.domain.dto.notification.OrderItemNotificationDTO;
import com.app.projectbar.domain.dto.notification.OrderNotificationDTO;
import com.app.projectbar.domain.dto.notification.OrderNotificationDTO.NotificationType;
import com.app.projectbar.domain.enums.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderNotificationServiceImpl implements IOrderNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(OrderNotificationServiceImpl.class);
    
    private final SimpMessagingTemplate messagingTemplate;

    public OrderNotificationServiceImpl(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Convierte los OrderItems a DTOs para la notificación
     */
    private List<OrderItemNotificationDTO> convertItemsToDTO(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        return items.stream()
                .map(item -> OrderItemNotificationDTO.builder()
                        .id(item.getId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void notifyNewOrder(Order order) {
        logger.info("Notificando nueva orden: {} para mesa {}", order.getId(), order.getTableNumber());
        
        List<OrderItemNotificationDTO> items = convertItemsToDTO(order.getOrderItems());
        
        OrderNotificationDTO notification = OrderNotificationDTO.builder()
                .orderId(order.getId())
                .clientName(order.getClientName())
                .tableNumber(order.getTableNumber())
                .valueToPay(order.getValueToPay())
                .status(order.getStatus())
                .waiterUserName(order.getWaiterUserName())
                .notes(order.getNotes())
                .date(order.getDate())
                .type(NotificationType.NEW_ORDER)
                .message("Nueva orden en mesa " + order.getTableNumber())
                .assignedTo("ADMIN")
                .items(items)
                .build();

        // Notificar a todos los administradores
        messagingTemplate.convertAndSend("/topic/admin/orders", notification);
        
        // Notificar a todos los meseros (cualquier mesero puede tomar la orden)
        messagingTemplate.convertAndSend("/topic/waiter/orders", notification);
        
        // Notificar también a Bartender y Chef para que preparen las bebidas/comida
        messagingTemplate.convertAndSend("/topic/bartender/orders", notification);
        messagingTemplate.convertAndSend("/topic/chef/orders", notification);
        
        logger.info("Notificación de nueva orden enviada exitosamente");
    }

    @Override
    public void notifyOrderAssigned(Order order, String waiterUsername) {
        logger.info("Notificando orden {} asignada al mesero {}", order.getId(), waiterUsername);
        
        List<OrderItemNotificationDTO> items = convertItemsToDTO(order.getOrderItems());
        
        OrderNotificationDTO notification = OrderNotificationDTO.builder()
                .orderId(order.getId())
                .clientName(order.getClientName())
                .tableNumber(order.getTableNumber())
                .valueToPay(order.getValueToPay())
                .status(order.getStatus())
                .waiterUserName(order.getWaiterUserName())
                .notes(order.getNotes())
                .date(order.getDate())
                .type(NotificationType.ORDER_ASSIGNED)
                .message("Orden asignada a mesa " + order.getTableNumber())
                .assignedTo(waiterUsername)
                .items(items)
                .build();

        // Notificar al mesero específico
        messagingTemplate.convertAndSend("/topic/waiter/" + waiterUsername + "/orders", notification);
        
        // Notificar al bartender y chef
        messagingTemplate.convertAndSend("/topic/bartender/orders", notification);
        messagingTemplate.convertAndSend("/topic/chef/orders", notification);
        
        logger.info("Notificación de orden asignada enviada al mesero: {}", waiterUsername);
    }

    @Override
    public void notifyOrderStatusChanged(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        logger.info("Notificando cambio de estado de orden {}: {} -> {}", 
                order.getId(), oldStatus, newStatus);
        
        String message;
        switch (newStatus) {
            case IN_PROGRESS:
                message = "La orden de la mesa " + order.getTableNumber() + " está en preparación";
                break;
            case READY:
                message = "La orden de la mesa " + order.getTableNumber() + " está lista";
                break;
            case DELIVERED:
                message = "La orden de la mesa " + order.getTableNumber() + " fue entregada";
                break;
            case CANCELLED:
                message = "La orden de la mesa " + order.getTableNumber() + " fue cancelada";
                break;
            default:
                message = "Estado de orden actualizado: " + newStatus;
        }
        
        List<OrderItemNotificationDTO> items = convertItemsToDTO(order.getOrderItems());
        
        OrderNotificationDTO notification = OrderNotificationDTO.builder()
                .orderId(order.getId())
                .clientName(order.getClientName())
                .tableNumber(order.getTableNumber())
                .valueToPay(order.getValueToPay())
                .status(order.getStatus())
                .waiterUserName(order.getWaiterUserName())
                .notes(order.getNotes())
                .date(order.getDate())
                .type(NotificationType.ORDER_STATUS_CHANGED)
                .message(message)
                .items(items)
                .build();

        // Notificar a todos los topic relevantes
        messagingTemplate.convertAndSend("/topic/admin/orders", notification);
        
        if (order.getWaiterUserName() != null && !order.getWaiterUserName().isEmpty()) {
            messagingTemplate.convertAndSend(
                    "/topic/waiter/" + order.getWaiterUserName() + "/orders", notification);
        }
        
        messagingTemplate.convertAndSend("/topic/waiter/orders", notification);
        
        logger.info("Notificación de cambio de estado enviada");
    }

    @Override
    public void notifyOrderCancelled(Order order) {
        logger.info("Notificando orden {} cancelada", order.getId());
        
        List<OrderItemNotificationDTO> items = convertItemsToDTO(order.getOrderItems());
        
        OrderNotificationDTO notification = OrderNotificationDTO.builder()
                .orderId(order.getId())
                .clientName(order.getClientName())
                .tableNumber(order.getTableNumber())
                .valueToPay(order.getValueToPay())
                .status(OrderStatus.CANCELLED)
                .waiterUserName(order.getWaiterUserName())
                .notes(order.getNotes())
                .date(LocalDateTime.now())
                .type(NotificationType.ORDER_CANCELLED)
                .message("Orden de la mesa " + order.getTableNumber() + " fue cancelada")
                .items(items)
                .build();

        messagingTemplate.convertAndSend("/topic/admin/orders", notification);
        messagingTemplate.convertAndSend("/topic/waiter/orders", notification);
        
        logger.info("Notificación de orden cancelada enviada");
    }
}