package com.app.projectbar.application.implementation;

import com.app.projectbar.application.exception.ErrorMessagesService;
import com.app.projectbar.application.exception.ResourceNotFoundException;
import com.app.projectbar.application.exception.bill.BillNotFoundByIdException;
import com.app.projectbar.application.exception.bill.BillNotFoundByNumberException;
import com.app.projectbar.application.interfaces.IBillReportService;
import com.app.projectbar.application.interfaces.IBillService;
import com.app.projectbar.application.interfaces.IOrderService;
import com.app.projectbar.domain.Bill;
import com.app.projectbar.domain.Order;
import com.app.projectbar.domain.OrderItem;
import com.app.projectbar.domain.dto.bill.BillDTO;
import com.app.projectbar.domain.dto.bill.BillReportDTO;
import com.app.projectbar.domain.dto.orderItem.ItemReportDTO;
import com.app.projectbar.infra.repositories.IBillRepository;
import com.app.projectbar.infra.repositories.IOrderRepository;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class BillServiceImpl implements IBillService, IBillReportService {

    private static final Logger log = LoggerFactory.getLogger(BillServiceImpl.class);

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
        Optional<Bill> bill = billRepository.findById(billNumber);
        if (bill.isEmpty()) {
            throw new BillNotFoundByIdException(ErrorMessagesService.BILL_NOT_FOUND_BY_ID_EXCEPTION.getMessage());
        }
        billRepository.deleteById(billNumber);

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



        try {

            // Obtener la factura y convertirla a DTO
            Bill bill = billRepository.findById(billId)
                    .orElseThrow(() -> {
                        log.error("Bill not found with id: {}", billId);
                        return new ResourceNotFoundException("Bill not found with id: " + billId);
                    });

            BillReportDTO billDTO = convertToBillReportDTO(bill);

            // Cargar el template del reporte
            InputStream reportStream = getClass().getResourceAsStream("/reports/templates/invoice.jrxml");
            if (reportStream == null) {
                throw new RuntimeException("Report template not found");
            }

            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            // Preparar los parámetros
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("billNumber", billDTO.getBillNumber());
            parameters.put("clientName", billDTO.getClientName());
            parameters.put("createdBy", billDTO.getCreatedBy());
            parameters.put("billingDate", billDTO.getBillingDate());
            parameters.put("totalAmount", billDTO.getTotalAmount());

            // Cargar el logo
            InputStream logoStream = getClass().getResourceAsStream("/static/images/logoEmpresa.jpg");
            if (logoStream == null) {
                log.warn("Logo image not found, proceeding without logo");
            }
            parameters.put("logoEmpresa", logoStream);

            // Crear el DataSource usando los ItemReportDTO
            JRBeanCollectionDataSource itemsDataSource = new JRBeanCollectionDataSource(billDTO.getItems());
            parameters.put("itemsDataSource", itemsDataSource);
            log.debug("DataSource created with {} items", billDTO.getItems().size());

            try {
                // Generar el reporte usando el itemsDataSource como fuente de datos principal
                String destinationPath = "src" + File.separator +
                        "main" + File.separator +
                        "resources" + File.separator +
                        "static" + File.separator +
                        "Factura_" + billId + ".pdf";
                InputStream jasperStream = getClass().getResourceAsStream("/reports/templates/invoice.jasper");
                JasperPrint print = JasperFillManager.fillReport(jasperStream, parameters, new JREmptyDataSource());
                JasperExportManager.exportReportToPdfFile(print, destinationPath);
                System.out.println("Report Created Successfully");



                // Retornar el PDF como bytes
                byte[] pdfBytes = JasperExportManager.exportReportToPdf(print);
                log.debug("PDF generated successfully with {} bytes", pdfBytes.length);
                return pdfBytes;

            } catch (JRException e) {
                log.error("Error during report generation: {}", e.getMessage(), e);
                throw new RuntimeException("Error generating PDF: " + e.getMessage(), e);
            }

        } catch (Exception e) {
            log.error("Unexpected error during PDF generation: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating PDF", e);
        }
    }

    private BillReportDTO convertToBillReportDTO(Bill bill) {
        List<ItemReportDTO> items = bill.getOrders().stream()
            .flatMap(order -> order.getOrderItems().stream()
                .map(item -> ItemReportDTO.builder()
                    .productName(item.getProduct().getName())
                    .quantity(item.getQuantity())
                    .price(item.getProduct().getPrice())
                    .subtotal(item.getQuantity() * item.getProduct().getPrice())
                    .build()))
            .collect(Collectors.toList());

        return BillReportDTO.builder()
            .id(bill.getId())
            .clientName(bill.getClientName())
            .billingDate(bill.getBillingDate())
            .billNumber(bill.getBillNumber())
            .totalAmount(bill.getTotalAmount())
            .createdBy(bill.getCreatedBy())
            .items(items)
            .build();
    }
}