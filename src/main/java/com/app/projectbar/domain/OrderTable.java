package com.app.projectbar.domain;

import com.app.projectbar.domain.enums.OrderTableStatus;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad que representa una mesa física del restaurante.
 * 
 * La relación con Order es por referencia: Order.tableNumber coincide con OrderTable.number
 * No hay relación JPA directa entre las entidades.
 * 
 * Para saber las órdenes de una mesa, usar: orderRepository.findByTableNumber(number)
 */
@Setter
@Getter
@Builder
@Entity
@Table(name = "order_tables")
@AllArgsConstructor
@NoArgsConstructor
public class OrderTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Integer number;

    @Column(nullable = false)
    private Integer capacity;

    /**
     * Ubicación de la mesa dentro del bar (ej. "terraza", "patio", "comedor", "barra").
     */
    private String location;

    /**
     * Estado de la mesa:
     * - FREE: Disponible para nuevos clientes
     * - OCCUPIED: Con clientes y órdenes activas
     * - RESERVED: Reservada para un cliente específico
     * 
     * El estado se actualiza automáticamente:
     * - FREE → OCCUPIED: Al crear una orden en la mesa
     * - OCCUPIED → FREE: Al facturar todas las órdenes de la mesa
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderTableStatus status = OrderTableStatus.FREE;

    private String notes;

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = OrderTableStatus.FREE;
        }
    }
}
