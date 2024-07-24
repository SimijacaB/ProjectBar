package com.app.projectbar.infra.controller;

import com.app.projectbar.application.IOrderItemService;
import com.app.projectbar.domain.dto.OrderItemRequestDTO;
import com.app.projectbar.domain.dto.OrderItemResponseDTO;
import com.app.projectbar.domain.dto.OrderRequestDTO;
import com.app.projectbar.domain.dto.OrderResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/order-item")
public class OrderItemController {

    private final IOrderItemService orderItemService;

    @PostMapping("/save")
    @Transactional
    public ResponseEntity<OrderItemResponseDTO> save(@RequestBody @Valid OrderItemRequestDTO orderItemRequestDTO){
        return ResponseEntity.ok(orderItemService.save(orderItemRequestDTO));
    }
}
