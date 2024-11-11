package com.app.projectbar.infra.controller;

import com.app.projectbar.application.interfaces.IBillService;
import com.app.projectbar.domain.dto.bill.BillDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/bill")
public class BillController {

    private final IBillService billService;

    @PostMapping("/save")
    @Transactional
    public ResponseEntity<BillDTO> save(@RequestBody BillDTO billDTO){
        return ResponseEntity.ok(billService.save(billDTO));
    }
}
