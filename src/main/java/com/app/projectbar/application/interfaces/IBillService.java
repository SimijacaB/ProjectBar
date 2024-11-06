package com.app.projectbar.application.interfaces;

import com.app.projectbar.domain.dto.bill.BillRequestDTO;
import com.app.projectbar.domain.dto.bill.BillResponseDTO;
import com.app.projectbar.domain.dto.ingredient.IngredientRequestDTO;
import com.app.projectbar.domain.dto.ingredient.IngredientResponseDTO;

import java.util.List;

public interface IBillService {

    BillResponseDTO findById(Long id);

    BillResponseDTO findByNumber(Long number);

    BillResponseDTO save(BillRequestDTO billRequest);

    List<BillResponseDTO> findAll();

    void delete(Long billNumber);

    BillResponseDTO generateBillByTable(Integer tableNumber);

    BillResponseDTO generateBillByClient(String clientName);

    BillResponseDTO generateBillBySelection(List<Long> orderIds);

}
