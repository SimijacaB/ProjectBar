package com.app.projectbar.infra.controller;

import com.app.projectbar.application.interfaces.IOrderService;
import com.app.projectbar.domain.dto.order.OrderForListResponseDTO;
import com.app.projectbar.domain.dto.order.OrderRequestDTO;
import com.app.projectbar.domain.dto.order.OrderResponseDTO;
import com.app.projectbar.domain.dto.order.UpdateOrderDTO;
import com.app.projectbar.domain.dto.orderItem.OrderItemRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final IOrderService orderService;

    @PostMapping
    @Transactional
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<OrderResponseDTO> save(@RequestBody @Valid OrderRequestDTO orderRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.save(orderRequestDTO));
    }

    @GetMapping
    public ResponseEntity<List<OrderForListResponseDTO>> findAll() {
        return ResponseEntity.ok(orderService.findAll());
    }

    @GetMapping("/client/{name}")
    public ResponseEntity<List<OrderForListResponseDTO>> findByClientName(@PathVariable String name) {
        return ResponseEntity.ok(orderService.findByClientName(name));
    }

    @GetMapping("/table/{numberTable}")
    public ResponseEntity<List<OrderForListResponseDTO>> findByTableNumber(@PathVariable Integer numberTable) {
        return ResponseEntity.ok(orderService.findByTableNumber(numberTable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/waiter/{id}")
    public ResponseEntity<List<OrderForListResponseDTO>> findByWaiterId(@PathVariable String id) {
        return ResponseEntity.ok(orderService.findByWaiterId(id));
    }

    /**
     * Endpoint para que el mesero autenticado vea sus propias órdenes.
     * No necesita pasar ID, el sistema detecta quién está logueado.
     */
    @GetMapping("/mine")
    public ResponseEntity<List<OrderForListResponseDTO>> findMyOrders() {
        return ResponseEntity.ok(orderService.findMyOrders());
    }

    /**
     * Endpoint para que el mesero autenticado vea sus órdenes filtradas por rango
     * de fechas.
     * 
     * @param startDate Fecha de inicio (formato: yyyy-MM-dd)
     * @param endDate   Fecha de fin (formato: yyyy-MM-dd)
     */
    @GetMapping("/mine/date-range")
    public ResponseEntity<List<OrderForListResponseDTO>> findMyOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59, 999999999);
        return ResponseEntity.ok(orderService.findMyOrdersByDateRange(startDateTime, endDateTime));
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<OrderForListResponseDTO>> findByDate(@PathVariable LocalDate date) {
        return ResponseEntity.ok(orderService.findByDate(date));
    }

    /**
     * Endpoint para filtrar órdenes por rango de fechas (Admin).
     * 
     * @param startDate Fecha de inicio (formato: yyyy-MM-dd)
     * @param endDate   Fecha de fin (formato: yyyy-MM-dd)
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<OrderForListResponseDTO>> findByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59, 999999999);
        return ResponseEntity.ok(orderService.findByDateRange(startDateTime, endDateTime));
    }

    @GetMapping("/table/{tableNumber}/grouped-by-client")
    public ResponseEntity<Map<String, List<OrderForListResponseDTO>>> getOrdersByClient(
            @PathVariable Integer tableNumber) {
        return ResponseEntity.ok(orderService.findPendingOrdersByTableGroupedByClient(tableNumber));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<OrderResponseDTO> update(@PathVariable Long id,
            @RequestBody @Valid UpdateOrderDTO updateOrderDTO) {
        return ResponseEntity.ok(orderService.updateOrder(updateOrderDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    @PatchMapping("/{id}/items")
    @Transactional
    public ResponseEntity<OrderResponseDTO> addOrderItem(@PathVariable Long id,
            @RequestBody @Valid OrderItemRequestDTO itemRequestDTO) {
        return ResponseEntity.ok(orderService.addOrderItem(id, itemRequestDTO));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    @Transactional
    public ResponseEntity<OrderResponseDTO> removeOrderItem(@PathVariable Long id, @PathVariable Long itemId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(orderService.removeOrderItem(id, itemId, quantity));
    }

    @PatchMapping("/{id}/status")
    @Transactional
    public ResponseEntity<OrderResponseDTO> changeStatusOrder(@PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        return ResponseEntity.ok(orderService.changeStatus(id, status));
    }

    /**
     * Asigna un mesero a una orden en estado CREATED.
     * Solo puede ser ejecutado por ADMIN.
     * Cambia el estado de CREATED a ASSIGNED.
     */
    @PatchMapping("/{id}/waiter/{waiterUsername}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<OrderResponseDTO> assignWaiter(
            @PathVariable Long id,
            @PathVariable String waiterUsername) {
        return ResponseEntity.ok(orderService.assignWaiter(id, waiterUsername));
    }

    /**
     * Obtiene todas las órdenes en estado CREATED (sin mesero asignado).
     * Solo para ADMIN - para gestionar asignaciones.
     */
    @GetMapping("/unassigned")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderForListResponseDTO>> findUnassignedOrders() {
        return ResponseEntity.ok(orderService.findUnassignedOrders());
    }

    /**
     * Obtiene las órdenes asignadas al mesero autenticado en estado ASSIGNED.
     * Estas son las órdenes que el mesero puede empezar a atender.
     */
    @GetMapping("/mine/assigned")
    public ResponseEntity<List<OrderForListResponseDTO>> findMyAssignedOrders() {
        return ResponseEntity.ok(orderService.findMyAssignedOrders());
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> delete(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.ok().build();
    }

}
