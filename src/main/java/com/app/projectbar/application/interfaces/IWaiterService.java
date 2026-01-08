package com.app.projectbar.application.interfaces;

import com.app.projectbar.domain.dto.waiter.WaiterWithOrdersDTO;

import java.util.List;

public interface IWaiterService {
    
    /**
     * Obtiene todos los meseros activos con el conteo de sus órdenes activas.
     * Los meseros se ordenan por cantidad de órdenes (menor primero).
     */
    List<WaiterWithOrdersDTO> findAllWaitersWithActiveOrders();
    
}
