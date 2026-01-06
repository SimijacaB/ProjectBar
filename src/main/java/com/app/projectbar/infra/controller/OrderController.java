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
@RequestMapping("/api/order")
public class OrderController {

    private final IOrderService orderService;

    @PostMapping("/save")
    @Transactional
    public ResponseEntity<OrderResponseDTO> save(@RequestBody @Valid OrderRequestDTO orderRequestDTO){
        return ResponseEntity.ok(orderService.save(orderRequestDTO));
    }

    @GetMapping("/all")
    public ResponseEntity<List<OrderForListResponseDTO>> findAll(){
        return ResponseEntity.ok(orderService.findAll());
    }

    @GetMapping("/find-by-client-name/{name}")
    public ResponseEntity<List<OrderForListResponseDTO>> findByClientName(@PathVariable String name){
        return ResponseEntity.ok(orderService.findByClientName(name));
    }

    @GetMapping("/find-by-table-number/{numberTable}")
    public ResponseEntity<List<OrderForListResponseDTO>> findByTableNumber(@PathVariable Integer numberTable){
        return ResponseEntity.ok(orderService.findByTableNumber(numberTable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/find-by-waiter-id/{id}")
    public ResponseEntity<List<OrderForListResponseDTO>> findByWaiterId(@PathVariable String id){
        return ResponseEntity.ok(orderService.findByWaiterId(id));
    }

    /**
     * Endpoint para que el mesero autenticado vea sus propias órdenes.
     * No necesita pasar ID, el sistema detecta quién está logueado.
     */
    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderForListResponseDTO>> findMyOrders(){
        return ResponseEntity.ok(orderService.findMyOrders());
    }

    /**
     * Endpoint para que el mesero autenticado vea sus órdenes filtradas por rango de fechas.
     * @param startDate Fecha de inicio (formato: yyyy-MM-dd)
     * @param endDate Fecha de fin (formato: yyyy-MM-dd)
     */
    @GetMapping("/my-orders/date-range")
    public ResponseEntity<List<OrderForListResponseDTO>> findMyOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ){
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59, 999999999);
        return ResponseEntity.ok(orderService.findMyOrdersByDateRange(startDateTime, endDateTime));
    }

    @GetMapping("/find-by-date/{date}")
    public ResponseEntity<List<OrderForListResponseDTO>> findByDate(@PathVariable LocalDate date){
        return ResponseEntity.ok(orderService.findByDate(date));
    }

    /**
     * Endpoint para filtrar órdenes por rango de fechas (Admin).
     * @param startDate Fecha de inicio (formato: yyyy-MM-dd)
     * @param endDate Fecha de fin (formato: yyyy-MM-dd)
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<OrderForListResponseDTO>> findByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ){
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59, 999999999);
        return ResponseEntity.ok(orderService.findByDateRange(startDateTime, endDateTime));
    }

    @GetMapping("/table/{tableNumber}/grouped-by-client")
    public ResponseEntity<Map<String, List<OrderForListResponseDTO>>> getOrdersByClient(
            @PathVariable Integer tableNumber
    ) {
        return ResponseEntity.ok(orderService.findPendingOrdersByTableGroupedByClient(tableNumber));
    }

    @PutMapping("/update")
    @Transactional
    public ResponseEntity<OrderResponseDTO> update(@RequestBody @Valid UpdateOrderDTO updateOrderDTO){
        return ResponseEntity.ok(orderService.updateOrder(updateOrderDTO));
    }

    @GetMapping("/find-by-id/{id}")
    public ResponseEntity<OrderResponseDTO> findById(@PathVariable Long id){
        return ResponseEntity.ok(orderService.findById(id));
    }

    @PatchMapping("/add-order-item/{idOrder}")
    @Transactional
    public ResponseEntity<OrderResponseDTO> addOrderItem( @PathVariable Long idOrder, @RequestBody @Valid OrderItemRequestDTO itemRequestDTO){
        return ResponseEntity.ok(orderService.addOrderItem(idOrder, itemRequestDTO));
    }

    @PutMapping("/remove-order-item/{idOrder}/{idOrderItem}/{quantityToRemove}")
    @Transactional
    public ResponseEntity<OrderResponseDTO> removeOrderItem( @PathVariable Long idOrder, @PathVariable Long idOrderItem, @PathVariable Integer quantityToRemove){
        return ResponseEntity.ok(orderService.removeOrderItem(idOrder, idOrderItem, quantityToRemove));
    }

    @PatchMapping("/change-status/{idOrder}/{status}")
    @Transactional
    public ResponseEntity<OrderResponseDTO> changeStatusOrder( @PathVariable Long idOrder, @PathVariable String status){
        return ResponseEntity.ok(orderService.changeStatus(idOrder, status));
    }

    @DeleteMapping("/delete/{id}")
    @Transactional
    public ResponseEntity <?> delete(@PathVariable Long id){
        orderService.deleteOrder(id);
        return ResponseEntity.ok().build();
    }







}
