package com.app.projectbar.infra.controller;

import com.app.projectbar.application.interfaces.IBillService;
import com.app.projectbar.domain.dto.bill.BillDTO;
import com.app.projectbar.domain.dto.bill.OrdersForBillDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/bill")
public class BillController {

    private final IBillService billService;

    @PostMapping("/save/by-table/{numberTable}/{clientName}")
    @Transactional
    public ResponseEntity<BillDTO> generateByTable(@PathVariable Integer numberTable, @PathVariable String clientName){
        return ResponseEntity.ok(billService.generateBillByTable(numberTable, clientName));
    }

    @PostMapping("/save/by-client/{clientName}")
    @Transactional
    public ResponseEntity<BillDTO> generateByClient(@PathVariable String clientName){
        return ResponseEntity.ok(billService.generateBillByClient(clientName));
    }

    @PostMapping("/save/by-selection")
    @Transactional
    public ResponseEntity<BillDTO> generateBySelection(@RequestBody OrdersForBillDto requestDto){
        return ResponseEntity.ok(billService.generateBillBySelection(requestDto.getOrdersId()));
    }

    @GetMapping("/all")
    public ResponseEntity<List<BillDTO>> findAll(){
        return ResponseEntity.ok(billService.findAll());
    }
}
