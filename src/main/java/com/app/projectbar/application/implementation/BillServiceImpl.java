    package com.app.projectbar.application.implementation;

    import com.app.projectbar.application.interfaces.IBillService;
    import com.app.projectbar.domain.Bill;
    import com.app.projectbar.domain.Order;
    import com.app.projectbar.domain.OrderItem;
    import com.app.projectbar.domain.Product;
    import com.app.projectbar.domain.dto.bill.BillDTO;
    import com.app.projectbar.infra.repositories.IBillRepository;
    import com.app.projectbar.infra.repositories.IOrderRepository;
    import com.app.projectbar.infra.repositories.IProductRepository;
    import lombok.RequiredArgsConstructor;
    import org.modelmapper.ModelMapper;
    import org.springframework.stereotype.Service;

    import java.time.LocalDateTime;
    import java.util.*;

    @Service
    @RequiredArgsConstructor
    public class BillServiceImpl implements IBillService {

        private final IBillRepository billRepository;
        private final IOrderRepository orderRepository;
        private final ModelMapper modelMapper;
        private final IProductRepository productRepository;
        private final OrderServiceImpl orderService;
        private final InventoryServiceImpl inventoryService;



        @Override
        public BillDTO findById(Long id) {
            Optional<Bill> billOptional = billRepository.findById(id);
            if (!billOptional.isPresent()) {
                throw new RuntimeException("No bill found with id: " + id);
            }
            BillDTO result = modelMapper.map(billOptional.get(), BillDTO.class);
            return result;
        }

        @Override
        public BillDTO findByNumber(Long number) {
            Optional<Bill> billOptional = billRepository.findByBillNumber(number);
            if (billOptional.isPresent()) {
                return modelMapper.map(billOptional.get(), BillDTO.class);
            } else {
                throw new RuntimeException("No bill found with number: " + number);
            }
        }

        private BillDTO save(Bill bill, List<Order> orders) {
           orders.forEach(order -> {
               order.setBill(bill); // Relacionar las órdenes con la factura
               order.getOrderItems().forEach(item -> item.setOrder(order)); // Relacionar los ítems con la orden
           });
           bill.setOrders(orders);
           billRepository.save(bill);
           return modelMapper.map(bill, BillDTO.class);
       }

        @Override
        public List<BillDTO> findAll() {
            return List.of();
        }

        @Override
        public void delete(Long billNumber) {

        }

        @Override
        public BillDTO generateBillByTable(Integer tableNumber, String clientName) {

            List<Order> ordersByTable = orderRepository.findByTableNumber(tableNumber);

            BillDTO billResponse = generateItemForBill(ordersByTable);

            billResponse.setClientName(clientName);

            this.save(modelMapper.map(billResponse, Bill.class), ordersByTable);
            return billResponse;
        }


        @Override
        public BillDTO generateBillByClient(String clientName) {
            List<Order> ordersByClientName = orderRepository.findByClientName(clientName);

            BillDTO billResponse = generateItemForBill(ordersByClientName);

            billResponse.setClientName(clientName);

            this.save(modelMapper.map(billResponse, Bill.class), ordersByClientName);

            return billResponse;
        }

        @Override
        public BillDTO generateBillBySelection(List<Long> orderIds) {
            return null;
        }


        public BillDTO generateItemForBill(List<Order> orders) {
//
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

            for (OrderItem orderItem : consolidatedItems){
                String code = orderItem.getProduct().getCode();
                Integer quantity = orderItem.getQuantity();
                inventoryService.deductStock(quantity, code);
            }

            // Crear la factura
            Bill bill = Bill.builder()
                    .billingDate(LocalDateTime.now())
                    .orders(orders)
                    .billNumber(generateBillNumber())
                    .totalAmount(calculateTotalAmount(consolidatedItems))
                    .createdBy("Sistema")
                    .build();

            return modelMapper.map(bill, BillDTO.class);

        }


        private Long generateBillNumber() {
            return System.currentTimeMillis();
        }



        private Double calculateTotalAmount(List<OrderItem> orderItems) {
            return orderItems.stream()
                    .mapToDouble(item -> item.getPrice() * item.getQuantity())
                    .sum();
        }
    }