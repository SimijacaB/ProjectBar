package com.app.projectbar.infra.controller;

import com.app.projectbar.application.interfaces.IOrderService;
import com.app.projectbar.domain.dto.order.OrderForListResponseDTO;
import com.app.projectbar.domain.dto.order.OrderRequestDTO;
import com.app.projectbar.domain.dto.order.OrderResponseDTO;
import com.app.projectbar.domain.dto.order.UpdateOrderDTO;
import com.app.projectbar.domain.dto.orderItem.OrderItemRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @PutMapping("/update")
    @Transactional
    public ResponseEntity<OrderResponseDTO> update(@RequestBody @Valid UpdateOrderDTO updateOrderDTO){
        return ResponseEntity.ok(orderService.updateOrder(updateOrderDTO));
    }

    @GetMapping("/find-by-id/{id}")
    public ResponseEntity<OrderResponseDTO> findById(@PathVariable Long id){
        return ResponseEntity.ok(orderService.findById(id));
    }

    @PutMapping("/add-order-item/{idOrder}")
    @Transactional
    public ResponseEntity<OrderResponseDTO> addOrderItem( @PathVariable Long idOrder, @RequestBody @Valid OrderItemRequestDTO itemRequestDTO){
        return ResponseEntity.ok(orderService.addOrderItem(idOrder, itemRequestDTO));
    }

    @PutMapping("/remove-order-item/{idOrder}/{idOrderItem}/{quantityToRemove}")
    @Transactional
    public ResponseEntity<OrderResponseDTO> removeOrderItem( @PathVariable Long idOrder, @PathVariable Long idOrderItem, @PathVariable Integer quantityToRemove){
        return ResponseEntity.ok(orderService.removeOrderItem(idOrder, idOrderItem, quantityToRemove));
    }

    @PutMapping("/change-status/{idOrder}/{status}")
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
