package com.app.projectbar.application.interfaces;

import com.app.projectbar.domain.dto.bill.BillDTO;

import java.util.List;

public interface IBillService {

    BillDTO findById(Long id);

    BillDTO findByNumber(Long number);

    BillDTO save(BillDTO billRequest);

    List<BillDTO> findAll();

    void delete(Long billNumber);

    void generateBillByTable(Integer tableNumber, String clientName);

    BillDTO generateBillByClient(String clientName);

    BillDTO generateBillBySelection(List<Long> orderIds);

}
