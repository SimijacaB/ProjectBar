package com.app.projectbar.application.implementation;

import com.app.projectbar.application.exception.ErrorMessagesService;
import com.app.projectbar.application.exception.bill.BillNotFoundByIdException;
import com.app.projectbar.application.exception.bill.BillNotFoundByNumberException;
import com.app.projectbar.application.interfaces.IBillService;
import com.app.projectbar.application.interfaces.IOrderService;
import com.app.projectbar.application.mapper.BillMapper;
import com.app.projectbar.domain.Bill;
import com.app.projectbar.domain.Order;
import com.app.projectbar.domain.OrderItem;
import com.app.projectbar.domain.dto.bill.BillDTO;
import com.app.projectbar.domain.enums.OrderStatus;
import com.app.projectbar.domain.enums.OrderTableStatus;
import com.app.projectbar.infra.repositories.IBillRepository;
import com.app.projectbar.infra.repositories.IOrderRepository;
import com.app.projectbar.infra.repositories.IOrderTableRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillServiceImpl implements IBillService {

    private static final Logger log = LoggerFactory.getLogger(BillServiceImpl.class);

    private final IBillRepository billRepository;
    private final IOrderRepository orderRepository;
    private final BillMapper billMapper;
    private final IOrderService orderService;
    private final IOrderTableRepository orderTableRepository;

    @Override
    public BillDTO findById(Long id) {
        var bill = billRepository.findById(id)
                .orElseThrow(() -> new BillNotFoundByIdException(
                        ErrorMessagesService.BILL_NOT_FOUND_BY_ID_EXCEPTION.getMessage()));
        return billMapper.toDTO(bill);
    }

    @Override
    public BillDTO findByNumber(String number) {
        var bill = billRepository.findByBillNumber(number)
                .orElseThrow(() -> new BillNotFoundByNumberException(
                        ErrorMessagesService.BILL_NOT_FOUND_BY_NUMBER_EXCEPTION.getMessage()));
        return billMapper.toDTO(bill);
    }

    @Override
    public List<BillDTO> findAll() {
        return billMapper.toDTOList(billRepository.findAll());
    }

    @Override
    public Page<BillDTO> findAll(String clientName, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return billRepository.findByFilters(clientName, startDate, endDate, pageable)
                .map(billMapper::toDTO);
    }

    @Override
    public void delete(Long billNumber) {
        billRepository.findById(billNumber)
                .orElseThrow(() -> new BillNotFoundByIdException(
                        ErrorMessagesService.BILL_NOT_FOUND_BY_ID_EXCEPTION.getMessage()));
        billRepository.deleteById(billNumber);
    }

    @Override
    @Transactional
    public BillDTO generateBillByTable(Integer tableNumber, String clientName) {
        List<Order> ordersByTable = orderRepository.findByTableNumber(tableNumber);
        List<Long> orderIds = ordersByTable.stream().map(Order::getId).toList();
        List<Order> selectedOrders = orderService.getExistingOrdersOrThrow(orderIds);
        return processBill(selectedOrders, clientName);
    }

    @Override
    @Transactional
    public BillDTO generateBillByClient(String clientName) {
        List<Order> ordersByClientName = orderRepository.findByClientName(clientName);
        List<Long> orderIds = ordersByClientName.stream().map(Order::getId).toList();
        List<Order> selectedOrders = orderService.getExistingOrdersOrThrow(orderIds);
        return processBill(selectedOrders, clientName);
    }

    @Override
    @Transactional
    public BillDTO generateBillBySelection(List<Long> orderIds) {
        List<Order> selectedOrders = orderService.getExistingOrdersOrThrow(orderIds);
        String clientName = selectedOrders.get(0).getClientName();
        return processBill(selectedOrders, clientName);
    }

    /**
     * Common billing flow shared by all generateBill* methods.
     * Validates, marks orders as BILLED, saves the bill, and frees tables.
     */
    private BillDTO processBill(List<Order> selectedOrders, String clientName) {
        orderService.validateIfOrderCanBeBilled(selectedOrders);
        selectedOrders.forEach(order -> order.setStatus(OrderStatus.BILLED));

        BillDTO billResponse = generateItemForBill(selectedOrders);
        billResponse.setClientName(clientName);

        BillDTO savedBill = saveBill(billMapper.toEntity(billResponse), selectedOrders);
        freeTablesIfAllOrdersBilled(selectedOrders);
        return savedBill;
    }

    public BillDTO generateItemForBill(List<Order> orders) {
        Map<Long, OrderItem> productMap = new HashMap<>();

        for (Order order : orders) {
            for (OrderItem item : order.getOrderItems()) {
                Long productId = item.getProduct().getId();
                if (productMap.containsKey(productId)) {
                    OrderItem existingItem = productMap.get(productId);
                    existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
                } else {
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

        Bill bill = Bill.builder()
                .billingDate(LocalDateTime.now())
                .orders(orders)
                .totalAmount(calculateTotalAmount(consolidatedItems))
                .createdBy(SecurityContextHolder.getContext().getAuthentication().getName())
                .build();

        return billMapper.toDTO(bill);
    }

    private Double calculateTotalAmount(List<OrderItem> orderItems) {
        return orderItems.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    private BillDTO saveBill(Bill bill, List<Order> orders) {
        orders.forEach(order -> {
            order.setBill(bill);
            order.getOrderItems().forEach(item -> item.setOrder(order));
        });
        bill.setOrders(orders);
        Bill savedBill = billRepository.save(bill);

        orderRepository.saveAll(orders);
        orderRepository.flush();

        String formattedBillNumber = String.format("FE-%06d", savedBill.getId());
        savedBill.setBillNumber(formattedBillNumber);

        return billMapper.toDTO(savedBill);
    }

    private void freeTablesIfAllOrdersBilled(List<Order> billedOrders) {
        Map<Integer, List<Order>> ordersByTable = billedOrders.stream()
                .filter(order -> order.getTableNumber() != null)
                .collect(Collectors.groupingBy(Order::getTableNumber));

        log.info("Checking {} tables for potential release", ordersByTable.size());
        ordersByTable.forEach(this::freeOrderTableIfAllOrdersBilled);
    }

    private void freeOrderTableIfAllOrdersBilled(Integer tableNumber, List<Order> billedOrdersFromTable) {
        if (tableNumber == null) {
            log.debug("Table number is null, skipping table free operation");
            return;
        }

        orderTableRepository.findByNumber(tableNumber).ifPresent(orderTable -> {
            log.info("Checking if table {} should be freed", tableNumber);
            List<Order> allOrdersInTable = orderRepository.findByTableNumber(tableNumber);

            if (allOrdersInTable.isEmpty()) {
                log.info("No orders found for table {}", tableNumber);
                return;
            }

            List<Order> notBilledOrders = allOrdersInTable.stream()
                    .filter(order -> {
                        boolean isJustBilled = billedOrdersFromTable.stream()
                                .anyMatch(billedOrder -> billedOrder.getId().equals(order.getId()));
                        if (isJustBilled) {
                            return false;
                        }
                        return order.getStatus() != OrderStatus.BILLED;
                    })
                    .toList();

            boolean allOrdersBilled = notBilledOrders.isEmpty();

            log.info("Table {}: All orders billed = {}, Not billed orders count = {}",
                    tableNumber, allOrdersBilled, notBilledOrders.size());

            if (allOrdersBilled && orderTable.getStatus() == OrderTableStatus.OCCUPIED) {
                orderTable.setStatus(OrderTableStatus.FREE);
                orderTableRepository.save(orderTable);
                orderTableRepository.flush();
                log.info("Table {} released automatically - all orders are BILLED", tableNumber);
            } else if (!allOrdersBilled) {
                log.info("Not all orders in table {} are billed yet. Remaining: {}",
                        tableNumber, notBilledOrders.stream().map(Order::getId).toList());
            }
        });
    }
}
