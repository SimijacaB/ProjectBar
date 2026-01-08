package com.app.projectbar.infra.controller;

import com.app.projectbar.application.interfaces.IWaiterService;
import com.app.projectbar.domain.dto.waiter.WaiterWithOrdersDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/waiters")
public class WaiterController {

    private final IWaiterService waiterService;

    /**
     * Obtiene todos los meseros activos con el conteo de sus órdenes activas.
     * Ordenados por cantidad de órdenes (menor primero) para facilitar la asignación.
     * Solo accesible por ADMIN.
     */
    @GetMapping("/with-orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<WaiterWithOrdersDTO>> getWaitersWithActiveOrders() {
        return ResponseEntity.ok(waiterService.findAllWaitersWithActiveOrders());
    }
}
