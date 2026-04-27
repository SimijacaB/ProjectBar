package com.app.projectbar.application.interfaces;

import com.app.projectbar.domain.Order;
import com.app.projectbar.domain.enums.OrderStatus;

public interface IOrderNotificationService {
    
    /**
     * Notifica cuando se crea una nueva orden
     */
    void notifyNewOrder(Order order);
    
    /**
     * Notifica cuando una orden es asignada a un mesero
     */
    void notifyOrderAssigned(Order order, String waiterUsername);
    
    /**
     * Notifica cuando cambia el estado de una orden
     */
    void notifyOrderStatusChanged(Order order, OrderStatus oldStatus, OrderStatus newStatus);
    
    /**
     * Notifica cuando una orden es cancelada
     */
    void notifyOrderCancelled(Order order);
}