package com.app.projectbar.infra.repositories;


import com.app.projectbar.domain.Order;
import com.app.projectbar.domain.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface IOrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByClientName(String clientName);

    List<Order> findByTableNumber(Integer tableNumber);

    List<Order> findByWaiterUserName(String username);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByTableNumberAndStatusNot(Integer tableNumber, OrderStatus status);

    /**
     * Busca órdenes por rango de fechas (inclusive)
     * @param startDate Fecha/hora de inicio
     * @param endDate Fecha/hora de fin
     * @return Lista de órdenes en el rango
     */
    @Query("SELECT o FROM Order o WHERE o.date >= :startDate AND o.date <= :endDate ORDER BY o.date DESC")
    List<Order> findByDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Busca órdenes de un mesero específico por rango de fechas
     * @param username Username del mesero
     * @param startDate Fecha/hora de inicio
     * @param endDate Fecha/hora de fin
     * @return Lista de órdenes del mesero en el rango
     */
    @Query("SELECT o FROM Order o WHERE o.waiterUserName = :username AND o.date >= :startDate AND o.date <= :endDate ORDER BY o.date DESC")
    List<Order> findByWaiterUserNameAndDateBetween(
            @Param("username") String username,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

}
