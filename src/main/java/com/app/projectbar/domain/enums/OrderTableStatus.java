package com.app.projectbar.domain.enums;

/**
 * Estados de una mesa en el restaurante.
 */
public enum OrderTableStatus {
    /**
     * Mesa disponible para nuevos clientes.
     */
    FREE,
    
    /**
     * Mesa ocupada por clientes con órdenes activas.
     */
    OCCUPIED,
    
    /**
     * Mesa reservada para un cliente específico.
     */
    RESERVED
}
