package com.app.projectbar.domain.enums;

/**
 * Estados del ciclo de vida de una orden.
 * 
 * Flujo para pedidos de CLIENTE (QR):
 * CREATED → ASSIGNED → IN_PROGRESS → READY → DELIVERED → BILLED
 * 
 * Flujo para pedidos de MESERO:
 * IN_PROGRESS → READY → DELIVERED → BILLED
 */
public enum OrderStatus {

    /**
     * Pedido creado por cliente vía QR. Sin mesero asignado.
     * Solo aplica a pedidos de clientes.
     */
    CREATED,
    
    /**
     * ADMIN asignó un mesero al pedido.
     * Transición desde CREATED.
     */
    ASSIGNED,
    
    /**
     * Pedido en preparación/atención.
     * - Para pedidos de cliente: transición desde ASSIGNED
     * - Para pedidos de mesero: estado inicial
     */
    IN_PROGRESS,
    
    /**
     * Pedido listo para entregar al cliente.
     */
    READY,
    
    /**
     * Pedido entregado al cliente.
     */
    DELIVERED,
    
    /**
     * Pedido cancelado.
     */
    CANCELLED,
    
    /**
     * Pedido facturado.
     */
    BILLED

}
