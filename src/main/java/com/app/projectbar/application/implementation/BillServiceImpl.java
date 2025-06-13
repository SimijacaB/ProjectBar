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
import com.app.projectbar.domain.dto.bill.BillReportDTO;
import com.app.projectbar.domain.dto.orderItem.ItemReportDTO;
import com.app.projectbar.infra.repositories.IBillRepository;
import com.app.projectbar.infra.repositories.IOrderRepository;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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

        try {
            // 1. Obtener los datos de la factura
            BillDTO billDTO = findById(billId); // Reutilizamos el método existente
            Bill bill = modelMapper.map(billDTO, Bill.class); // Convertimos a entidad si es necesario para acceder a la lista de Orders

            // Consolidar los ítems para el reporte, similar a generateItemForBill
            Map<Long, ItemReportDTO> productMap = new HashMap<>();
            double totalCalculated = 0.0; // Recalcular el total aquí para asegurar coherencia

            if (bill.getOrders() != null) {
                for (Order order : bill.getOrders()) {
                    if (order.getOrderItems() != null) {
                        for (OrderItem item : order.getOrderItems()) {
                            Long productId = item.getProduct().getId();
                            double itemSubtotal = item.getQuantity() * item.getProduct().getPrice();

                            if (productMap.containsKey(productId)) {
                                ItemReportDTO existingItem = productMap.get(productId);
                                existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
                                existingItem.setSubtotal(existingItem.getSubtotal() + itemSubtotal);
                            } else {
                                ItemReportDTO newItem = ItemReportDTO.builder()
                                        .productName(item.getProduct().getName()) // Asegúrate de que Product tenga un 'name'
                                        .quantity(item.getQuantity())
                                        .price(item.getProduct().getPrice())
                                        .subtotal(itemSubtotal)
                                        .build();
                                productMap.put(productId, newItem);
                            }
                            totalCalculated += itemSubtotal;
                        }
                    }
                }
            }


            // 2. Crear el DTO de reporte
            List<ItemReportDTO> itemsReport = new ArrayList<>(productMap.values());

            BillReportDTO reportData = BillReportDTO.builder()
                    .id(bill.getId())
                    .billNumber(bill.getBillNumber())
                    .clientName(bill.getClientName())
                    .billingDate(bill.getBillingDate())
                    .totalAmount(totalCalculated) // Usar el total calculado
                    .createdBy(bill.getCreatedBy())
                    .items(itemsReport) // Lista de ítems consolidados
                    .build();

            // 3. Cargar la plantilla compilada (.jasper)
            // Asegúrate de que invoice_bill.jasper esté en src/main/resources/reports/
            InputStream reportStream = getClass().getResourceAsStream("/reports/invoice_bill.jasper");
            if (reportStream == null) {
                throw new JRException("El archivo invoice_bill.jasper no se encontró en src/main/resources/reports/");
            }

            // 4. Preparar los parámetros del reporte
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("billNumber", reportData.getBillNumber());
            parameters.put("clientName", reportData.getClientName());
            parameters.put("billingDate", reportData.getBillingDate());
            parameters.put("totalAmount", reportData.getTotalAmount());
            parameters.put("createdBy", reportData.getCreatedBy());

            // DataSource para los ítems de la factura (lista de ItemReportDTO)
            JRBeanCollectionDataSource itemsDataSource = new JRBeanCollectionDataSource(reportData.getItems());
            parameters.put("itemsDataSource", itemsDataSource); // Este es el nombre del parámetro JRDataSource en tu reporte

            // 5. Llenar el reporte
            JasperPrint jasperPrint = JasperFillManager.fillReport(reportStream, parameters, new JREmptyDataSource());
            // Se usa JREmptyDataSource aquí porque los datos principales (de la factura) se pasan como parámetros
            // y la lista de ítems se pasa como un JRDataSource separado para un sub reporte o tabla.


            // 6. Exportar a PDF
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

            SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
            exporter.setConfiguration(configuration);

            exporter.exportReport();

            return outputStream.toByteArray();

        } catch (JRException e) {
            // Manejo de errores de JasperReports
            e.printStackTrace();
            throw new RuntimeException("Error al generar el PDF de la factura: " + e.getMessage(), e);
        } catch (Exception e) {
            // Otros errores
            e.printStackTrace();
            throw new RuntimeException("Error inesperado al generar el PDF de la factura: " + e.getMessage(), e);
        }

    }
}