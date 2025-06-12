package com.app.projectbar.application.implementation;


import com.app.projectbar.application.interfaces.IBillReportService;
import com.app.projectbar.domain.dto.bill.BillDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BillServiceReportImpl implements IBillReportService {

    private final BillServiceImpl billService;

    @Override
    public byte[] generateBillPDF(Long billId) {
        BillDTO bill = billService.findById(billId);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("billNumber", bill.getBillNumber());
        parameters.put("clientName", bill.getClientName());
        parameters.put("billingDate", bill.getBillingDate());
        parameters.put("totalAmount", bill.getTotalAmount());


        return new byte[0];
    }
}
