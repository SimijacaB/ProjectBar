package com.app.projectbar.application.implementation;

import com.app.projectbar.application.interfaces.IBillService;
import com.app.projectbar.domain.Bill;
import com.app.projectbar.domain.dto.bill.BillRequestDTO;
import com.app.projectbar.domain.dto.bill.BillResponseDTO;
import com.app.projectbar.infra.repositories.IBillRepository;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

public class BillServiceImpl implements IBillService {

    private final IBillRepository billRepository;
    private final ModelMapper modelMapper;

    public BillServiceImpl(IBillRepository billRepository, ModelMapper modelMapper) {
        this.billRepository = billRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public BillResponseDTO findById(Long id) {
        Optional<Bill> billOptional = billRepository.findById(id);
        if(!billOptional.isPresent()){
            throw new RuntimeException("No bill found with id: " + id);
        }
        BillResponseDTO result = modelMapper.map(billOptional.get(), BillResponseDTO.class);
        return result;
    }

    @Override
    public BillResponseDTO findByNumber(Long number) {
        Optional<Bill> billOptional = billRepository.findByNumber(number);
        if(!billOptional.isPresent()){
            throw new RuntimeException("No bill found with number: " + number);
        }
        BillResponseDTO result = modelMapper.map(billOptional.get(), BillResponseDTO.class);
        return result;
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

    @Override
    public BillResponseDTO generateBillByTable(Integer tableNumber) {
        return null;
    }

    @Override
    public BillResponseDTO generateBillByClient(String clientName) {
        return null;
    }

    @Override
    public BillResponseDTO generateBillBySelection(List<Long> orderIds) {
        return null;
    }
}
