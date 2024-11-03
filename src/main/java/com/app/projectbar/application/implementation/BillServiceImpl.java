package com.app.projectbar.application.implementation;

import com.app.projectbar.application.interfaces.IBillService;
import com.app.projectbar.domain.dto.bill.BillRequestDTO;
import com.app.projectbar.domain.dto.bill.BillResponseDTO;

import java.util.List;

public class BillServiceImpl implements IBillService {
    @Override
    public BillResponseDTO findById(Long id) {
        return null;
    }

    @Override
    public BillResponseDTO findByNumber(Long number) {
        return null;
    }

    @Override
    public BillResponseDTO save(BillRequestDTO billRequest) {
        return null;
    }

    @Override
    public List<BillResponseDTO> findAll() {
        return List.of();
    }

    @Override
    public void delete(Long billNumber) {

    }
}
