package com.app.projectbar.application.interfaces;

import com.app.projectbar.domain.Bill;
import com.app.projectbar.domain.Order;
import com.app.projectbar.domain.dto.bill.BillDTO;

import java.util.List;

public interface IBillService {

    BillDTO findById(Long id);

    BillDTO findByNumber(Long number);

    List<BillDTO> findAll();

    void delete(Long billNumber);

    BillDTO generateBillByTable(Integer tableNumber, String clientName);

    BillDTO generateBillByClient(String clientName);

    BillDTO generateBillBySelection(List<Long> orderIds);

}
