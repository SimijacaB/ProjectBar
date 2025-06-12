package com.app.projectbar.application.implementation;

import com.app.projectbar.application.exception.ErrorMessagesService;
import com.app.projectbar.application.exception.bill.BillNotFoundByIdException;
import com.app.projectbar.application.exception.bill.BillNotFoundByNumberException;
import com.app.projectbar.application.interfaces.IBillReportService;
import com.app.projectbar.application.interfaces.IBillService;
import com.app.projectbar.application.interfaces.IOrderService;
import com.app.projectbar.domain.Bill;
import com.app.projectbar.domain.Order;
import com.app.projectbar.domain.OrderItem;
import com.app.projectbar.domain.dto.bill.BillDTO;
import com.app.projectbar.infra.repositories.IBillRepository;
import com.app.projectbar.infra.repositories.IOrderRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BillServiceImpl implements IBillService, IBillReportService
{

    private final IBillRepository billRepository;
    private final IOrderRepository orderRepository;
    private final ModelMapper modelMapper;
    private final IOrderService orderService;

    @Override
    public BillDTO findById(Long id) {
        Optional<Bill> billOptional = billRepository.findById(id);
        if (billOptional.isEmpty()) {
            throw new BillNotFoundByIdException(ErrorMessagesService.BILL_NOT_FOUND_BY_ID_EXCEPTION.getMessage());
        }
        return modelMapper.map(billOptional.get(), BillDTO.class);
    }

    @Override
    public BillDTO findByNumber(String number) {
        Optional<Bill> billOptional = billRepository.findByBillNumber(number);
        if (billOptional.isPresent()) {
            return modelMapper.map(billOptional.get(), BillDTO.class);
        } else {
            throw new BillNotFoundByNumberException(
                    ErrorMessagesService.BILL_NOT_FOUND_BY_NUMBER_EXCEPTION.getMessage());
        }
    }

    private BillDTO save(Bill bill, List<Order> orders) {
        orders.forEach(order -> {
            order.setBill(bill); // Relacionar las órdenes con la factura
            order.getOrderItems().forEach(item -> item.setOrder(order)); // Relacionar los ítems con la orden
        });
        bill.setOrders(orders);
        Bill savedBill = billRepository.save(bill);

        String formattedBillNumber = String.format("FE-%06d", savedBill.getId());
        savedBill.setBillNumber(formattedBillNumber);

        return modelMapper.map(savedBill, BillDTO.class);
    }

    @Override
    public List<BillDTO> findAll() {
        List<Bill> listBill = billRepository.findAll();
        List<BillDTO> dtoList = new ArrayList<>();
        for (Bill bill : listBill) {
            BillDTO dto = modelMapper.map(bill, BillDTO.class);
            dtoList.add(dto);
        }
        return dtoList;
    }

    @Override
    public void delete(Long billNumber) {

    }

    @Override
    public BillDTO generateBillByTable(Integer tableNumber, String clientName) {

        List<Order> ordersByTable = orderRepository.findByTableNumber(tableNumber);

        List<Long> orderIds = ordersByTable.stream().map(Order::getId).toList();

        // Aquí válido que las órdenes existan
        List<Order> selectedOrders = orderService.getExistingOrdersOrThrow(orderIds);

        // Aquí válido si las órdenes ya están facturadas
        orderService.validateIfOrderCanBeBilled(selectedOrders);

        // Aquí las setteo todas a READY
        orderService.setOrdersAsReady(selectedOrders);

        BillDTO billResponse = generateItemForBill(ordersByTable);

        billResponse.setClientName(clientName);

        return this.save(modelMapper.map(billResponse, Bill.class), ordersByTable);

    }

    @Override
    public BillDTO generateBillByClient(String clientName) {
        List<Order> ordersByClientName = orderRepository.findByClientName(clientName);

        List<Long> orderIds = ordersByClientName.stream().map(order -> order.getId()).toList();

        // Aquí válido que las órdenes existan
        List<Order> selectedOrders = orderService.getExistingOrdersOrThrow(orderIds);

        // Aquí válido si las órdenes ya están facturadas
        orderService.validateIfOrderCanBeBilled(selectedOrders);

        // Aquí las setteo todas a READY
        orderService.setOrdersAsReady(selectedOrders);

        BillDTO billResponse = generateItemForBill(ordersByClientName);

        billResponse.setClientName(clientName);

        return this.save(modelMapper.map(billResponse, Bill.class), ordersByClientName);

    }

    @Override
    public BillDTO generateBillBySelection(List<Long> orderIds) {
        // Aquí válido que las órdenes existan
        List<Order> selectedOrders = orderService.getExistingOrdersOrThrow(orderIds);

        // Aquí válido si las órdenes ya están facturadas
        orderService.validateIfOrderCanBeBilled(selectedOrders);

        // Aquí las setteo todas a READY
        orderService.setOrdersAsReady(selectedOrders);

        BillDTO billResponse = generateItemForBill(selectedOrders);
        billResponse.setClientName(selectedOrders.get(0).getClientName());

        return this.save(modelMapper.map(billResponse, Bill.class), selectedOrders);
    }

    public BillDTO generateItemForBill(List<Order> orders) {

        // Mapa que usa el ID del producto como clave
        Map<Long, OrderItem> productMap = new HashMap<>();

        for (Order order : orders) {
            for (OrderItem item : order.getOrderItems()) {
                Long productId = item.getProduct().getId();

                if (productMap.containsKey(productId)) {
                    OrderItem existingItem = productMap.get(productId);
                    existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
                } else {
                    // Clonar el item para evitar modificar los originales
                    OrderItem newItem = OrderItem.builder()
                            .product(item.getProduct())
                            .quantity(item.getQuantity())
                            .price(item.getProduct().getPrice())
                            .build();
                    productMap.put(productId, newItem);
                }
            }
        }

        List<OrderItem> consolidatedItems = new ArrayList<>(productMap.values());

        // Crear la factura
        Bill bill = Bill.builder()
                .billingDate(LocalDateTime.now())
                .orders(orders)
                .totalAmount(calculateTotalAmount(consolidatedItems))
                .createdBy(SecurityContextHolder.getContext().getAuthentication().getName())
                .build();

        return modelMapper.map(bill, BillDTO.class);
    }

    private Double calculateTotalAmount(List<OrderItem> orderItems) {
        return orderItems.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    @Override
    public byte[] generateBillPDF(Long billId) {
        
        return new byte[0];
    }
}