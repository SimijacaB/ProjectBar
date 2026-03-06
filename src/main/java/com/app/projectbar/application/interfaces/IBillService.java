package com.app.projectbar.application.interfaces;

import com.app.projectbar.domain.dto.bill.BillDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface IBillService {

    BillDTO findById(Long id);

    BillDTO findByNumber(String number);

    List<BillDTO> findAll();

    /**
     * Obtiene facturas paginadas con filtros opcionales.
     *
     * @param clientName Filtro por nombre de cliente (parcial, insensible a
     *                   mayúsculas)
     * @param startDate  Fecha/hora de inicio del rango (puede ser null)
     * @param endDate    Fecha/hora de fin del rango (puede ser null)
     * @param pageable   Configuración de paginación
     * @return Página de facturas que cumplen los filtros
     */
    Page<BillDTO> findAll(String clientName, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    void delete(Long billNumber);

    BillDTO generateBillByTable(Integer tableNumber, String clientName);

    BillDTO generateBillByClient(String clientName);

    BillDTO generateBillBySelection(List<Long> orderIds);

}
