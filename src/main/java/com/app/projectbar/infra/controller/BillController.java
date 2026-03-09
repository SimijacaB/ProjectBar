package com.app.projectbar.infra.controller;

import com.app.projectbar.application.interfaces.IBillReportService;
import com.app.projectbar.application.interfaces.IBillService;
import com.app.projectbar.domain.dto.bill.BillDTO;
import com.app.projectbar.domain.dto.bill.OrdersForBillDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bills")
public class BillController {

    private final IBillService billService;
    private final IBillReportService billReportService;

    @PostMapping("/table/{numberTable}/{clientName}")
    public ResponseEntity<BillDTO> generateByTable(@PathVariable Integer numberTable, @PathVariable String clientName) {
        return ResponseEntity.ok(billService.generateBillByTable(numberTable, clientName));
    }

    @PostMapping("/client/{clientName}")
    public ResponseEntity<BillDTO> generateByClient(@PathVariable String clientName) {
        return ResponseEntity.ok(billService.generateBillByClient(clientName));
    }

    @PostMapping("/selection")
    public ResponseEntity<BillDTO> generateBySelection(@RequestBody OrdersForBillDto requestDto) {
        return ResponseEntity.ok(billService.generateBillBySelection(requestDto.getOrdersId()));
    }

    /**
     * Obtiene facturas paginadas con filtros opcionales.
     *
     * @param clientName Filtro parcial por nombre de cliente (opcional)
     * @param startDate  Fecha de inicio del rango en formato ISO date (opcional)
     * @param endDate    Fecha de fin del rango en formato ISO date (opcional)
     * @param page       Número de página (0-indexed, por defecto 0)
     * @param size       Tamaño de página (por defecto 10)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<BillDTO>> findAll(
            @RequestParam(required = false) String clientName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59, 999999999) : null;

        PageRequest pageable = PageRequest.of(page, size, Sort.by("billingDate").descending());
        return ResponseEntity.ok(billService.findAll(clientName, startDateTime, endDateTime, pageable));
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadBillPdf(@PathVariable Long id) {
        try {
            byte[] pdfBytes = billReportService.generateBillPDF(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentLength(pdfBytes.length);

            String filename = "factura_" + id + ".pdf";
            headers.setContentDispositionFormData("attachment", filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF: " + e.getMessage(), e);
        }
    }
}
