package com.app.projectbar.infra.repositories;

import com.app.projectbar.domain.Order;
import com.app.projectbar.domain.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface IOrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByClientName(String clientName);

    List<Order> findByTableNumber(Integer tableNumber);

    List<Order> findByWaiterUserName(String username);

    Page<Order> findByWaiterUserName(String username, Pageable pageable);

    List<Order> findByStatus(OrderStatus status);

    /**
     * Busca órdenes por mesero y estado específico.
     * Útil para obtener órdenes asignadas a un mesero.
     */
    List<Order> findByWaiterUserNameAndStatus(String username, OrderStatus status);

    List<Order> findByTableNumberAndStatusNot(Integer tableNumber, OrderStatus status);

    /**
     * Busca órdenes por rango de fechas (inclusive)
     * 
     * @param startDate Fecha/hora de inicio
     * @param endDate   Fecha/hora de fin
     * @return Lista de órdenes en el rango
     */
    @Query("SELECT o FROM Order o WHERE o.date >= :startDate AND o.date <= :endDate ORDER BY o.date DESC")
    List<Order> findByDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Busca órdenes por rango de fechas con paginación
     */
    @Query(value = "SELECT o FROM Order o WHERE o.date >= :startDate AND o.date <= :endDate", countQuery = "SELECT COUNT(o) FROM Order o WHERE o.date >= :startDate AND o.date <= :endDate")
    Page<Order> findPagedByDateBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Busca órdenes de un mesero específico por rango de fechas
     * 
     * @param username  Username del mesero
     * @param startDate Fecha/hora de inicio
     * @param endDate   Fecha/hora de fin
     * @return Lista de órdenes del mesero en el rango
     */
    @Query("SELECT o FROM Order o WHERE o.waiterUserName = :username AND o.date >= :startDate AND o.date <= :endDate ORDER BY o.date DESC")
    List<Order> findByWaiterUserNameAndDateBetween(
            @Param("username") String username,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Busca órdenes de un mesero por rango de fechas con paginación
     */
    @Query(value = "SELECT o FROM Order o WHERE o.waiterUserName = :username AND o.date >= :startDate AND o.date <= :endDate", countQuery = "SELECT COUNT(o) FROM Order o WHERE o.waiterUserName = :username AND o.date >= :startDate AND o.date <= :endDate")
    Page<Order> findPagedByWaiterUserNameAndDateBetween(
            @Param("username") String username,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Cuenta las órdenes activas de un mesero (estados: ASSIGNED, IN_PROGRESS,
     * READY)
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.waiterUserName = :username AND o.status IN :statuses")
    int countByWaiterUserNameAndStatusIn(@Param("username") String username,
            @Param("statuses") List<OrderStatus> statuses);

}
