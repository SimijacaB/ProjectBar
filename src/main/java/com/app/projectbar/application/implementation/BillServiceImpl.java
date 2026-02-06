package com.app.projectbar.application.implementation;

import com.app.projectbar.application.exception.ErrorMessagesService;
import com.app.projectbar.application.exception.ResourceNotFoundException;
import com.app.projectbar.application.exception.bill.BillNotFoundByIdException;
import com.app.projectbar.application.exception.bill.BillNotFoundByNumberException;
import com.app.projectbar.application.interfaces.IBillReportService;
import com.app.projectbar.application.interfaces.IBillService;
import com.app.projectbar.application.interfaces.IOrderService;
import com.app.projectbar.application.mapper.BillMapper;
import com.app.projectbar.domain.Bill;
import com.app.projectbar.domain.Order;
import com.app.projectbar.domain.OrderItem;
import com.app.projectbar.domain.dto.bill.BillDTO;
import com.app.projectbar.domain.dto.bill.BillReportDTO;
import com.app.projectbar.domain.enums.OrderStatus;
import com.app.projectbar.domain.enums.OrderTableStatus;
import com.app.projectbar.infra.repositories.IBillRepository;
import com.app.projectbar.infra.repositories.IOrderRepository;
import com.app.projectbar.infra.repositories.IOrderTableRepository;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final BillMapper billMapper;
    private final IOrderService orderService;
    private final IOrderTableRepository orderTableRepository;

    @Override
    public BillDTO findById(Long id) {
        var bill = billRepository.findById(id)
                .orElseThrow(() -> new BillNotFoundByIdException(ErrorMessagesService.BILL_NOT_FOUND_BY_ID_EXCEPTION.getMessage()));
        return billMapper.toDTO(bill);
    }

    @Override
    public BillDTO findByNumber(String number) {
        var bill = billRepository.findByBillNumber(number)
                .orElseThrow(() -> new BillNotFoundByNumberException(ErrorMessagesService.BILL_NOT_FOUND_BY_NUMBER_EXCEPTION.getMessage()));
        return billMapper.toDTO(bill);
    }

    /**
     * Libera una mesa si todas sus órdenes están facturadas (estado BILLED).
     * La mesa solo se libera si:
     * 1. Existe al menos una orden en la mesa
     * 2. TODAS las órdenes de la mesa tienen estado BILLED
     *
     * @param tableNumber número de la mesa a verificar
     * @param billedOrdersFromTable órdenes que acabamos de facturar de esta mesa
     */
    private void freeOrderTableIfAllOrdersBilled(Integer tableNumber, List<Order> billedOrdersFromTable) {
        if (tableNumber == null) {
            log.debug("Table number is null, skipping table free operation");
            return;
        }

        // Buscar la mesa por número
        orderTableRepository.findByNumber(tableNumber).ifPresent(orderTable -> {
            log.info("Checking if table {} should be freed", tableNumber);

            // Obtener todas las órdenes de esta mesa desde la BD
            List<Order> allOrdersInTable = orderRepository.findByTableNumber(tableNumber);
            
            log.info("Table {} has {} orders total in database", tableNumber, allOrdersInTable.size());

            // Si no hay órdenes en la mesa, no hacer nada
            if (allOrdersInTable.isEmpty()) {
                log.info("No orders found for table {}", tableNumber);
                return;
            }
            
            // Verificar que TODAS las órdenes estén en estado BILLED
            // Usamos las órdenes de la BD, pero las que acabamos de facturar ya están en memoria con BILLED
            List<Order> notBilledOrders = allOrdersInTable.stream()
                    .filter(order -> {
                        // Si la orden está en las que acabamos de facturar, ya está BILLED
                        boolean isJustBilled = billedOrdersFromTable.stream()
                                .anyMatch(billedOrder -> billedOrder.getId().equals(order.getId()));

                        if (isJustBilled) {
                            return false; // Ya está facturada, no la contamos como "no facturada"
                        }

                        // Si no es de las recién facturadas, verificar su estado en BD
                        return order.getStatus() != OrderStatus.BILLED;
                    })
                    .toList();

            boolean allOrdersBilled = notBilledOrders.isEmpty();

            log.info("Table {}: All orders billed = {}, Not billed orders count = {}",
                    tableNumber, allOrdersBilled, notBilledOrders.size());

            // Solo liberar la mesa si todas las órdenes están facturadas y la mesa está ocupada
            if (allOrdersBilled && orderTable.getStatus() == OrderTableStatus.OCCUPIED) {
                orderTable.setStatus(OrderTableStatus.FREE);
                orderTableRepository.save(orderTable);
                orderTableRepository.flush(); // Asegurar que el cambio se persista inmediatamente
                log.info("✅ Mesa {} liberada automáticamente - todas sus órdenes están en estado BILLED", tableNumber);
            } else if (allOrdersBilled && orderTable.getStatus() != OrderTableStatus.OCCUPIED) {
                log.info("Table {} is not in OCCUPIED status, current status: {}", tableNumber, orderTable.getStatus());
            } else if (!allOrdersBilled) {
                log.info("Not all orders in table {} are billed yet. Remaining not billed: {}",
                        tableNumber, notBilledOrders.stream().map(Order::getId).toList());
            }
        });
    }

    /**
     * Verifica y libera las mesas de todas las órdenes facturadas.
     * Obtiene los números de mesa únicos de las órdenes y verifica cada una.
     */
    private void freeTablesIfAllOrdersBilled(List<Order> billedOrders) {
        // Agrupar las órdenes por número de mesa
        Map<Integer, List<Order>> ordersByTable = billedOrders.stream()
                .filter(order -> order.getTableNumber() != null)
                .collect(Collectors.groupingBy(Order::getTableNumber));

        log.info("Checking {} tables for potential release", ordersByTable.size());

        // Para cada mesa, verificar si debe ser liberada
        ordersByTable.forEach(this::freeOrderTableIfAllOrdersBilled);
    }

    private BillDTO save(Bill bill, List<Order> orders) {
        orders.forEach(order -> {
            order.setBill(bill); // Relacionar las órdenes con la factura
            order.getOrderItems().forEach(item -> item.setOrder(order)); // Relacionar los ítems con la orden
        });
        bill.setOrders(orders);
        Bill savedBill = billRepository.save(bill);

        // Guardar las órdenes actualizadas para asegurar que se persistan los cambios
        orderRepository.saveAll(orders);
        orderRepository.flush(); // Flush explícito para asegurar que los cambios se escriban en BD

        String formattedBillNumber = String.format("FE-%06d", savedBill.getId());
        savedBill.setBillNumber(formattedBillNumber);

        return billMapper.toDTO(savedBill);
    }

    @Override
    public List<BillDTO> findAll() {
        List<Bill> listBill = billRepository.findAll();
        return billMapper.toDTOList(listBill);
    }

    @Override
    public void delete(Long billNumber) {
        var bill = billRepository.findById(billNumber)
                .orElseThrow(() -> new BillNotFoundByIdException(ErrorMessagesService.BILL_NOT_FOUND_BY_ID_EXCEPTION.getMessage()));

        billRepository.deleteById(billNumber);

    }

    @Override
    @Transactional
    public BillDTO generateBillByTable(Integer tableNumber, String clientName) {

        List<Order> ordersByTable = orderRepository.findByTableNumber(tableNumber);

        List<Long> orderIds = ordersByTable.stream().map(Order::getId).toList();

        // Aquí válido que las órdenes existan
        List<Order> selectedOrders = orderService.getExistingOrdersOrThrow(orderIds);

        // Aquí válido si las órdenes pueden facturarse (DELIVERED y no facturadas)
        orderService.validateIfOrderCanBeBilled(selectedOrders);

        // Marcar las órdenes como BILLED ANTES de guardar la factura
        selectedOrders.forEach(order -> order.setStatus(OrderStatus.BILLED));

        // Generar los items de la factura CON las órdenes ya marcadas como BILLED
        BillDTO billResponse = generateItemForBill(selectedOrders);
        billResponse.setClientName(clientName);

        // Guardar la factura en BD con las órdenes BILLED (incluye flush)
        BillDTO savedBill = this.save(billMapper.toEntity(billResponse), selectedOrders);

        // Verificar y liberar las mesas de las órdenes facturadas
        // Usamos selectedOrders directamente ya que tienen el estado correcto después del flush
        freeTablesIfAllOrdersBilled(selectedOrders);

        return savedBill;

    }

    @Override
    @Transactional
    public BillDTO generateBillByClient(String clientName) {
        List<Order> ordersByClientName = orderRepository.findByClientName(clientName);

        List<Long> orderIds = ordersByClientName.stream().map(Order::getId).toList();

        // Aquí válido que las órdenes existan
        List<Order> selectedOrders = orderService.getExistingOrdersOrThrow(orderIds);

        // Aquí válido si las órdenes pueden facturarse (DELIVERED y no facturadas)
        orderService.validateIfOrderCanBeBilled(selectedOrders);

        // Marcar las órdenes como BILLED ANTES de guardar la factura
        selectedOrders.forEach(order -> order.setStatus(OrderStatus.BILLED));

        // Generar los items de la factura CON las órdenes ya marcadas como BILLED
        BillDTO billResponse = generateItemForBill(selectedOrders);
        billResponse.setClientName(clientName);

        // Guardar la factura en BD con las órdenes BILLED (incluye flush)
        BillDTO savedBill = this.save(billMapper.toEntity(billResponse), selectedOrders);

        // Verificar y liberar las mesas de las órdenes facturadas
        // Usamos selectedOrders directamente ya que tienen el estado correcto después del flush
        freeTablesIfAllOrdersBilled(selectedOrders);

        return savedBill;

    }

    @Override
    @Transactional
    public BillDTO generateBillBySelection(List<Long> orderIds) {
        // Aquí válido que las órdenes existan
        List<Order> selectedOrders = orderService.getExistingOrdersOrThrow(orderIds);

        // Aquí válido si las órdenes pueden facturarse (DELIVERED y no facturadas)
        orderService.validateIfOrderCanBeBilled(selectedOrders);

        // Marcar las órdenes como BILLED ANTES de guardar la factura
        selectedOrders.forEach(order -> order.setStatus(OrderStatus.BILLED));

        // Generar los items de la factura CON las órdenes ya marcadas como BILLED
        BillDTO billResponse = generateItemForBill(selectedOrders);
        billResponse.setClientName(selectedOrders.get(0).getClientName());

        // Guardar la factura en BD con las órdenes BILLED (incluye flush)
        BillDTO savedBill = this.save(billMapper.toEntity(billResponse), selectedOrders);

        // Verificar y liberar las mesas de las órdenes facturadas
        // Usamos selectedOrders directamente ya que tienen el estado correcto después del flush
        freeTablesIfAllOrdersBilled(selectedOrders);

        return savedBill;
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

        return billMapper.toDTO(bill);
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

            BillReportDTO billDTO = billMapper.toReportDTO(bill);

            // Cargar el template del reporte
            InputStream reportStream = getClass().getResourceAsStream("/reports/templates/invoice.jrxml");
            if (reportStream == null) {
                throw new RuntimeException("Report template not found");
            }


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


}