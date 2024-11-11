    package com.app.projectbar.application.implementation;

    import com.app.projectbar.application.interfaces.IBillService;
    import com.app.projectbar.domain.Bill;
    import com.app.projectbar.domain.Order;
    import com.app.projectbar.domain.OrderItem;
    import com.app.projectbar.domain.dto.bill.BillDTO;
    import com.app.projectbar.infra.repositories.IBillRepository;
    import com.app.projectbar.infra.repositories.IOrderRepository;
    import com.app.projectbar.infra.repositories.IProductRepository;
    import org.modelmapper.ModelMapper;

    import java.time.LocalDateTime;
    import java.util.*;

    public class BillServiceImpl implements IBillService {

        private final IBillRepository billRepository;
        private final IOrderRepository orderRepository;
        private final ModelMapper modelMapper;
        private final IProductRepository productRepository;

        public BillServiceImpl(IBillRepository billRepository, IOrderRepository orderRepository, ModelMapper modelMapper, IProductRepository productRepository) {
            this.billRepository = billRepository;
            this.orderRepository = orderRepository;
            this.modelMapper = modelMapper;
            this.productRepository = productRepository;
        }

        @Override
        public BillDTO findById(Long id) {
            Optional<Bill> billOptional = billRepository.findById(id);
            if(!billOptional.isPresent()){
                throw new RuntimeException("No bill found with id: " + id);
            }
            BillDTO result = modelMapper.map(billOptional.get(), BillDTO.class);
            return result;
        }

        @Override
        public BillDTO findByNumber(Long number) {
            Optional<Bill> billOptional = billRepository.findByNumber(number);
            if(!billOptional.isPresent()){
                throw new RuntimeException("No bill found with number: " + number);
            }
            BillDTO result = modelMapper.map(billOptional.get(), BillDTO.class);
            return result;
        }

        @Override
        public BillDTO save(BillDTO billRequest) {
            billRepository.save(modelMapper.map(billRequest, Bill.class));
            return billRequest;
        }

        @Override
        public List<BillDTO> findAll() {
            return List.of();
        }

        @Override
        public void delete(Long billNumber) {

        }

        @Override
        public void generateBillByTable(Integer tableNumber, String clientName) {

            List<Order> ordersByTable = orderRepository.findByTableNumber(tableNumber);

            BillDTO billResponse = generateItemForBill(ordersByTable);

            billResponse.setClientName(clientName);

            this.save(billResponse);


        }


        @Override
        public BillDTO generateBillByClient(String clientName) {
            List<Order> ordersByTable = orderRepository.findByClientName(clientName);

            return null;
        }

        @Override
        public BillDTO generateBillBySelection(List<Long> orderIds) {
            return null;
        }


        public BillDTO generateItemForBill(List<Order> orders){
            Map<String, OrderItem> unifiedOrderItems = new HashMap<>();

            for (Order order : orders) {
                for (OrderItem item : order.getOrderProducts()) {
                    String productName = item.getProductName();
                    if (unifiedOrderItems.containsKey(productName)) {
                        OrderItem existingItem = unifiedOrderItems.get(productName);
                        existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
                    } else {
                        OrderItem orderItem = new OrderItem();
                        orderItem.setProductName(item.getProductName());
                        orderItem.setQuantity(item.getQuantity());
                        unifiedOrderItems.put(productName, orderItem);
                    }
                }
            }

            List<OrderItem> orderItems = new ArrayList<>(unifiedOrderItems.values());

            Bill bill = new Bill();
            //bill.setClientName(clientName);
            bill.setBillingDate(LocalDateTime.now());
            bill.setItems(orderItems);
            bill.setBillNumber(generateBillNumber());
            bill.setTotalAmount(calculateTotalAmount(orderItems));
            bill.setCreatedBy("Sistema");

            return modelMapper.map(bill, BillDTO.class);
        }





        private Long generateBillNumber() {
            return System.currentTimeMillis();
        }

        private Double calculateTotalAmount(List<OrderItem> orderItems) {
            return orderItems.stream()
                    .mapToDouble(item -> item.getQuantity() * productRepository.findByName(item.getProductName()).get().stream().findFirst().get().getPrice()).sum();
        }
    }
