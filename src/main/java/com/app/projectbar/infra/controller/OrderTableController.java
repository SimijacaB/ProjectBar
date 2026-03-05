package com.app.projectbar.infra.controller;

import com.app.projectbar.application.interfaces.IOrderTableService;
import com.app.projectbar.domain.dto.orderTable.OrderTableRequestDTO;
import com.app.projectbar.domain.dto.orderTable.OrderTableResponseDTO;
import com.app.projectbar.domain.dto.orderTable.UpdateOrderTableStatusDTO;
import com.app.projectbar.domain.enums.OrderTableStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tables")
public class OrderTableController {

    private final IOrderTableService orderTableService;

    /**
     * Crea una nueva mesa.
     * Solo ADMIN puede crear mesas.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<OrderTableResponseDTO> create(@RequestBody @Valid OrderTableRequestDTO orderTableRequest) {
        return ResponseEntity.ok(orderTableService.create(orderTableRequest));
    }

    /**
     * Obtiene todas las mesas.
     */
    @GetMapping
    public ResponseEntity<List<OrderTableResponseDTO>> findAll() {
        return ResponseEntity.ok(orderTableService.findAll());
    }

    /**
     * Obtiene una mesa por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderTableResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(orderTableService.findById(id));
    }

    /**
     * Obtiene una mesa por su número.
     */
    @GetMapping("/number/{number}")
    public ResponseEntity<OrderTableResponseDTO> findByNumber(@PathVariable Integer number) {
        return ResponseEntity.ok(orderTableService.findByNumber(number));
    }

    /**
     * Actualiza una mesa.
     * Solo ADMIN puede actualizar mesas.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<OrderTableResponseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid OrderTableRequestDTO orderTableRequest) {
        return ResponseEntity.ok(orderTableService.update(id, orderTableRequest));
    }

    /**
     * Actualiza el estado de una mesa.
     * ADMIN y WAITER pueden cambiar el estado.
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAITER')")
    @Transactional
    public ResponseEntity<OrderTableResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestBody @Valid UpdateOrderTableStatusDTO updateStatusDTO) {
        return ResponseEntity.ok(orderTableService.updateStatus(id, updateStatusDTO));
    }

    /**
     * Obtiene todas las mesas por estado.
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderTableResponseDTO>> findByStatus(@PathVariable OrderTableStatus status) {
        return ResponseEntity.ok(orderTableService.findByStatus(status));
    }

    /**
     * Elimina una mesa.
     * Solo ADMIN puede eliminar mesas.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderTableService.delete(id);
        return ResponseEntity.ok().build();
    }
}
