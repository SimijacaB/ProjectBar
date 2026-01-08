package com.app.projectbar.application.implementation;

import com.app.projectbar.application.exception.ErrorMessagesService;
import com.app.projectbar.application.exception.orders.OrderNotFoundByIdException;
import com.app.projectbar.application.exception.orders.OrdersAlreadyBilledException;
import com.app.projectbar.application.exception.orders.OrdersNotFoundByStatusException;
import com.app.projectbar.application.interfaces.IInventoryService;
import com.app.projectbar.application.interfaces.IOrderService;
import com.app.projectbar.domain.*;
import com.app.projectbar.domain.dto.order.OrderForListResponseDTO;
import com.app.projectbar.domain.dto.order.OrderRequestDTO;
import com.app.projectbar.domain.dto.order.OrderResponseDTO;
import com.app.projectbar.domain.dto.order.UpdateOrderDTO;
import com.app.projectbar.domain.dto.orderItem.OrderItemRequestDTO;
import com.app.projectbar.domain.dto.orderItem.OrderItemResponseDTO;
import com.app.projectbar.domain.enums.OrderStatus;
import com.app.projectbar.infra.repositories.IOrderRepository;
import com.app.projectbar.infra.repositories.IOrderTableRepository;
import com.app.projectbar.infra.repositories.IProductRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.app.projectbar.domain.enums.OrderTableStatus.*;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {

    private final IOrderRepository orderRepository;
    private final IProductRepository productRepository;
    private final IInventoryService inventoryService;
    private final IOrderTableRepository orderTableRepository;

    private final ModelMapper modelMapper;



    @Override
    public OrderResponseDTO save(OrderRequestDTO orderRequest) {
        // Validar que la orden tenga al menos un producto
        if (orderRequest.getOrderProducts() == null || orderRequest.getOrderProducts().isEmpty()) {
            throw new RuntimeException("La orden debe tener al menos un producto");
        }

        /*
        Dos escenarios según los requerimientos:
        1. Cliente hace pedido vía QR (sin autenticación) 
           → estado CREATED, waiterUserName = null
        2. Mesero autenticado toma la orden 
           → estado IN_PROGRESS, waiterUserName = mesero
         */

        String waiterId = null;
        boolean isWaiterOrder = false;
        
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() 
                    && !"anonymousUser".equals(authentication.getPrincipal())) {
                waiterId = authentication.getName();
                isWaiterOrder = true;
            }
        } catch (Exception e) {
            // Si no hay autenticación, es un pedido directo del cliente
            waiterId = null;
            isWaiterOrder = false;
        }

        // Determinar estado inicial según el tipo de pedido
        OrderStatus initialStatus;
        if (isWaiterOrder) {
            // Pedido de mesero: inicia directamente en IN_PROGRESS
            initialStatus = OrderStatus.IN_PROGRESS;
        } else {
            // Pedido de cliente QR: inicia en CREATED (sin mesero)
            initialStatus = OrderStatus.CREATED;
            waiterId = null; // Asegurar que no tiene mesero
        }

        // Buscar la mesa asociada o crearla automáticamente si no existe
        OrderTable orderTable = orderTableRepository.findByNumber(orderRequest.getTableNumber())
                .orElseGet(() -> {
                    // Crear mesa automáticamente si no existe (compatibilidad con flujo anterior)
                    OrderTable newTable = OrderTable.builder()
                            .number(orderRequest.getTableNumber())
                            .capacity(4) // Capacidad por defecto
                            .status(FREE)
                            .build();
                    return orderTableRepository.save(newTable);
                });

        // Cambiar el estado de la mesa a OCCUPIED cuando se crea la orden
        // (permite múltiples órdenes en la misma mesa - varios clientes)
        if (orderTable.getStatus() == FREE) {
            orderTable.setStatus(OCCUPIED);
            orderTableRepository.save(orderTable);
        }

        // Crear la orden sin los items primero
        Order newOrder = Order.builder()
                .clientName(orderRequest.getClientName())
                .tableNumber(orderRequest.getTableNumber())
                .notes(orderRequest.getNotes())
                .waiterUserName(waiterId)
                .status(initialStatus)
                .orderItems(new ArrayList<>())
                .valueToPay(0.0)
                .build();

        // Procesar los productos si existen
        if (orderRequest.getOrderProducts() != null && !orderRequest.getOrderProducts().isEmpty()) {
            double totalValue = 0.0;

            for (OrderItemRequestDTO itemRequest : orderRequest.getOrderProducts()) {
                // Buscar el producto por ID o por nombre
                Product product;
                if (itemRequest.getIdProduct() != null) {
                    product = productRepository.findById(itemRequest.getIdProduct())
                            .orElseThrow(() -> new RuntimeException("Product not found with id: " + itemRequest.getIdProduct()));
                } else if (itemRequest.getProductName() != null) {
                    product = productRepository.findOneByName(itemRequest.getProductName())
                            .orElseThrow(() -> new RuntimeException("Product not found with name: " + itemRequest.getProductName()));
                } else {
                    throw new RuntimeException("Product ID or name is required");
                }

                // Crear el OrderItem
                OrderItem orderItem = OrderItem.builder()
                        .product(product)
                        .productName(product.getName())
                        .quantity(itemRequest.getQuantity())
                        .price(product.getPrice())
                        .order(newOrder)
                        .build();

                newOrder.getOrderItems().add(orderItem);
                totalValue += product.getPrice() * itemRequest.getQuantity();
            }

            newOrder.setValueToPay(totalValue);
        }

        newOrder = orderRepository.save(newOrder);
        return buildOrderResponseDTO(newOrder);
    }

    @Override
    public List<OrderForListResponseDTO> findAll() {
        var orders = orderRepository.findAll();
        return orders.stream()
                .map(order -> modelMapper.map(order, OrderForListResponseDTO.class)).toList();
    }

        @Override
        public OrderResponseDTO findById(Long id) {
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new OrderNotFoundByIdException(ErrorMessagesService.ORDER_NOT_FOUND_BY_ID_EXCEPTION.getMessage()));

            //Se mapea la lista de orderItems y se cambia la lista de OrderResponse de OrderItem a OrderItemResponseDTO

            OrderResponseDTO orderResponseDTO = modelMapper.map(order, OrderResponseDTO.class);
            List<OrderItemResponseDTO> orderItemDTOs = order.getOrderItems().stream()
                    .map(orderItem -> {
                        OrderItemResponseDTO dto = new OrderItemResponseDTO();
                        dto.setId(orderItem.getId());
                        dto.setProductName(orderItem.getProduct().getName());
                        dto.setQuantity(orderItem.getQuantity());
                        dto.setUnitPrice(orderItem.getProduct().getPrice());
                        dto.setTotalPrice(orderItem.getProduct().getPrice() * orderItem.getQuantity());
                        return dto;
                    })
                    .collect(Collectors.toList());
            orderResponseDTO.setOrderItemList(orderItemDTOs);

            return orderResponseDTO;
        }

    @Override
    public OrderResponseDTO updateOrder(UpdateOrderDTO updateOrderDTO) {
        var order = orderRepository.findById(updateOrderDTO.getId())
                .orElseThrow(() -> new OrderNotFoundByIdException(ErrorMessagesService.ORDER_NOT_FOUND_BY_ID_EXCEPTION.getMessage()));

        order.setClientName(updateOrderDTO.getClientName());
        order.setTableNumber(updateOrderDTO.getTableNumber());
        order.setNotes(updateOrderDTO.getNotes());

        orderRepository.save(order);

        return modelMapper.map(order, OrderResponseDTO.class);
    }

    @Override
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    @Override
    public List<OrderForListResponseDTO> findByClientName(String name) {
        List<Order> orders = orderRepository.findByClientName(name);
        return orders.stream().map(order -> modelMapper.map(order, OrderForListResponseDTO.class)).toList();
    }

    @Override
    public List<OrderForListResponseDTO> findByTableNumber(Integer tableNumber) {
        List<Order> orders = orderRepository.findByTableNumber(tableNumber);
        return orders.stream().map(order -> modelMapper.map(order, OrderForListResponseDTO.class)).toList();
    }

    @Override
    public List<OrderForListResponseDTO> findByWaiterId(String id) {

        List<Order> orders = orderRepository.findByWaiterUserName(id);
        return orders.stream().map(order -> modelMapper.map(order, OrderForListResponseDTO.class)).toList();
    }

    @Override
    public List<OrderForListResponseDTO> findMyOrders() {
        // Obtener el username del mesero autenticado
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new RuntimeException("User must be authenticated to view their orders");
        }

        String waiterUsername = authentication.getName();
        List<Order> orders = orderRepository.findByWaiterUserName(waiterUsername);
        return orders.stream()
                .map(order -> modelMapper.map(order, OrderForListResponseDTO.class))
                .toList();
    }

    @Override
    public List<OrderForListResponseDTO> findMyOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        // Obtener el username del mesero autenticado
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new RuntimeException("User must be authenticated to view their orders");
        }

        String waiterUsername = authentication.getName();
        List<Order> orders = orderRepository.findByWaiterUserNameAndDateBetween(waiterUsername, startDate, endDate);
        return orders.stream()
                .map(order -> modelMapper.map(order, OrderForListResponseDTO.class))
                .toList();
    }

    @Override
    public List<OrderForListResponseDTO> findByDate(LocalDate date) {
        // Convertir LocalDate a rango de LocalDateTime (inicio y fin del día)
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59, 999999999);
        
        List<Order> orders = orderRepository.findByDateBetween(startOfDay, endOfDay);
        return orders.stream().map(order -> modelMapper.map(order, OrderForListResponseDTO.class)).toList();
    }

    @Override
    public List<OrderForListResponseDTO> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderRepository.findByDateBetween(startDate, endDate);
        return orders.stream()
                .map(order -> modelMapper.map(order, OrderForListResponseDTO.class))
                .toList();
    }

    @Override
    public List<OrderForListResponseDTO> findByStatus(OrderStatus status) {
        List<Order> orders = orderRepository.findByStatus(status);
        if(orders.isEmpty()){
            throw new OrdersNotFoundByStatusException(ErrorMessagesService.ORDERS_NOT_FOUND_BY_STATUS_EXCEPTION.getMessage());
        }
        return orders.stream().map(order -> modelMapper.map(order, OrderForListResponseDTO.class)).toList();
    }

    @Override
    public Map<String, List<OrderForListResponseDTO>> findPendingOrdersByTableGroupedByClient(Integer tableNumber) {
        // 1. Obtener todas las órdenes de la mesa que NO estén facturadas
        List<Order> orders = orderRepository.findByTableNumberAndStatusNot(
                tableNumber,
                OrderStatus.BILLED
        );

        // 2. Agrupar por nombre del cliente
        return orders.stream()
                .map(order -> modelMapper.map(order, OrderForListResponseDTO.class))
                .collect(Collectors.groupingBy(OrderForListResponseDTO::getClientName));

    }

    @Override
    public OrderResponseDTO addOrderItem(Long orderId, OrderItemRequestDTO orderItemToAdd) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        // ✅ VALIDACIÓN: No permitir agregar items a órdenes entregadas
        if (OrderStatus.DELIVERED.equals(order.getStatus())) {
            throw new RuntimeException("Cannot add items to a delivered order. Please create a new order.");
        }

        Product product = productRepository.findOneByName(orderItemToAdd.getProductName())
                .orElseThrow(() -> new RuntimeException("Product not found with name: " + orderItemToAdd.getProductName()));

        // Key: ProductID, Value: OrderItem
        // Esto evita recorrer la lista con un stream filter cada vez.
        Map<Long, OrderItem> productToItemMap = new HashMap<>();

        // Llenamos el mapa (O(n) una sola vez)
        for (OrderItem item : order.getOrderItems()) {
            productToItemMap.put(item.getProduct().getId(), item);
        }

        // Verificación O(1) - Instantánea
        if (productToItemMap.containsKey(product.getId())) {
            // Escenario: El producto YA existe, sumamos cantidad
            OrderItem existingItem = productToItemMap.get(product.getId());
            existingItem.setQuantity(existingItem.getQuantity() + orderItemToAdd.getQuantity());
            existingItem.setPrice(product.getPrice());
        } else {
            // Escenario: Producto nuevo, creamos y agregamos
            OrderItem newOrderItem = OrderItem.builder()
                    .product(product) // Importante setear la entidad Product completa
                    .productName(product.getName())
                    .quantity(orderItemToAdd.getQuantity())
                    .price(product.getPrice())
                    .order(order)
                    .build();

            order.getOrderItems().add(newOrderItem);
        }

        // Recalcular total (Streams son buenos aquí para legibilidad)
        updateOrderTotalValue(order);

        Order updatedOrder = orderRepository.save(order);

        // Construcción de respuesta optimizada
        return buildOrderResponseDTO(updatedOrder);
    }

    @Override
    public OrderResponseDTO removeOrderItem(Long id, Long idOrderItem, Integer quantityToRemove) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order with id " + id + " not found"));

        OrderItem orderItem = order.getOrderItems().stream()
                .filter(item -> item.getId().equals(idOrderItem))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("OrderItem with id " + idOrderItem + " not found"));

        if (quantityToRemove != null && quantityToRemove < orderItem.getQuantity()) {
            orderItem.setQuantity(orderItem.getQuantity() - quantityToRemove);
        } else {
            order.getOrderItems().remove(orderItem);
        }

        updateOrderTotalValue(order);

        Order updatedOrder = orderRepository.save(order);

        return buildOrderResponseDTO(updatedOrder);
    }

    @Override
    public OrderResponseDTO changeStatus(Long id, String newStatus) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundByIdException(ErrorMessagesService.ORDER_NOT_FOUND_BY_ID_EXCEPTION.getMessage()));

        OrderStatus targetStatus = OrderStatus.valueOf(newStatus);
        
        // Validaciones de transición de estado según reglas de negocio
        validateStatusTransition(order, targetStatus);

        // Solo entra aquí si el estado a cambiar es "DELIVERED"
        if (targetStatus == OrderStatus.DELIVERED) {
            for (OrderItem item : order.getOrderItems()) {
                inventoryService.deductStock(item.getQuantity(), item.getProduct().getCode());
            }
        }

        order.setStatus(targetStatus);

        return buildOrderResponseDTO(orderRepository.save(order));
    }

    /**
     * Valida que la transición de estado sea válida según las reglas de negocio.
     */
    private void validateStatusTransition(Order order, OrderStatus newStatus) {
        OrderStatus currentStatus = order.getStatus();
        
        // Regla: No se puede pasar a IN_PROGRESS sin mesero asignado
        if (newStatus == OrderStatus.IN_PROGRESS && order.getWaiterUserName() == null) {
            throw new RuntimeException("Cannot change to IN_PROGRESS without an assigned waiter. Current status: " + currentStatus);
        }
        
        // Regla: CREATED solo puede ir a ASSIGNED (vía assignWaiter) o CANCELLED
        if (currentStatus == OrderStatus.CREATED) {
            if (newStatus != OrderStatus.ASSIGNED && newStatus != OrderStatus.CANCELLED) {
                throw new RuntimeException("Order in CREATED status can only be ASSIGNED or CANCELLED. Use assignWaiter endpoint.");
            }
        }
        
        // Regla: ASSIGNED solo puede ir a IN_PROGRESS o CANCELLED
        if (currentStatus == OrderStatus.ASSIGNED) {
            if (newStatus != OrderStatus.IN_PROGRESS && newStatus != OrderStatus.CANCELLED) {
                throw new RuntimeException("Order in ASSIGNED status can only move to IN_PROGRESS or CANCELLED");
            }
        }
    }

    @Override
    public OrderResponseDTO assignWaiter(Long orderId, String waiterUsername) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundByIdException(ErrorMessagesService.ORDER_NOT_FOUND_BY_ID_EXCEPTION.getMessage()));
        
        // Validar que la orden esté en estado CREATED
        if (order.getStatus() != OrderStatus.CREATED) {
            throw new RuntimeException("Only orders in CREATED status can be assigned. Current status: " + order.getStatus());
        }
        
        // Asignar mesero y cambiar estado
        order.setWaiterUserName(waiterUsername);
        order.setStatus(OrderStatus.ASSIGNED);
        
        return buildOrderResponseDTO(orderRepository.save(order));
    }

    @Override
    public List<OrderForListResponseDTO> findUnassignedOrders() {
        List<Order> orders = orderRepository.findByStatus(OrderStatus.CREATED);
        return orders.stream()
                .map(order -> modelMapper.map(order, OrderForListResponseDTO.class))
                .toList();
    }

    @Override
    public List<OrderForListResponseDTO> findMyAssignedOrders() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new RuntimeException("User must be authenticated to view assigned orders");
        }
        
        String waiterUsername = authentication.getName();
        List<Order> orders = orderRepository.findByWaiterUserNameAndStatus(waiterUsername, OrderStatus.ASSIGNED);
        return orders.stream()
                .map(order -> modelMapper.map(order, OrderForListResponseDTO.class))
                .toList();
    }


    private List<OrderItemResponseDTO> getOrderItemResponse(Order updatedOrder){
        // 7. Mapear la lista de OrderItems a OrderItemResponseDTO

        return updatedOrder.getOrderItems().stream()
                .map(orderItem -> OrderItemResponseDTO.builder()
                        .id(orderItem.getId())
                        .productName(orderItem.getProduct().getName())
                        .quantity(orderItem.getQuantity())
                        .unitPrice(orderItem.getProduct().getPrice())
                        .totalPrice(orderItem.getProduct().getPrice() * orderItem.getQuantity())
                        .build())
                .collect(Collectors.toList());
    }

    public void validateIfOrderCanBeBilled(List<Order> orders) {
        // Usamos Set para evitar duplicados si la lista 'orders' viniera sucia
        // y para optimizar la recolección de IDs.
        Set<Long> invalidOrderIds = orders.stream()
                // Filtro: Nos interesan las que NO son DELIVERED (Lógica inversa para detectar error rápido)
                .filter(order -> !OrderStatus.DELIVERED.equals(order.getStatus()))
                .map(Order::getId)
                .collect(Collectors.toSet());

        if (!invalidOrderIds.isEmpty()) {
            // String.join es más eficiente que Collectors.joining para colecciones simples
            String ids = String.join(", ",
                    invalidOrderIds.stream().map(String::valueOf).collect(Collectors.toList()));

            throw new OrdersAlreadyBilledException(ErrorMessagesService.ORDER_ALREADY_BILLED_EXCEPTION + ids);
        }
    }


    // método que se encargará de settear el OrderStatus de la Orders que se vayan a facturar a READY
    public void setOrdersAsReady(List<Order> orders){
        for (Order order : orders){
            //order.setStatus(OrderStatus.READY); Se cambia el estado de las ordenes, ya que para facturar una orden, esta debe estar entregada
            order.setStatus(OrderStatus.DELIVERED);
        }
        orderRepository.saveAll(orders);
    }
    // Reemplaza tu método actual con este
    public List<Order> getExistingOrdersOrThrow(List<Long> orderIds) {
        // 1. Ir a la base de datos UNA sola vez (Batch Query)
        List<Order> foundOrders = orderRepository.findAllById(orderIds);

        // Si encontramos todas, retornamos rápido
        if (foundOrders.size() == orderIds.size()) {
            return foundOrders;
        }

        // 2. ESTRATEGIA SENIOR: Usar un HashSet para búsquedas O(1)
        // Convertimos las órdenes encontradas a un Set de IDs
        Set<Long> foundIds = foundOrders.stream()
                .map(Order::getId)
                .collect(Collectors.toSet());

        // 3. Detectar cuáles faltan
        // Recorremos la lista original y verificamos contra el Set (O(1) por item)
        List<Long> notFoundIds = new ArrayList<>();
        for (Long id : orderIds) {
            if (!foundIds.contains(id)) { // Esto es O(1) gracias al Hash
                notFoundIds.add(id);
            }
        }

        if (!notFoundIds.isEmpty()) {
            throw new RuntimeException("The following Order IDs do not exist: " + notFoundIds);
        }

        return foundOrders;

    }

    private void updateOrderTotalValue(Order order) {
        double total = order.getOrderItems().stream()
                .mapToDouble(item -> item.getQuantity() * item.getProduct().getPrice())
                .sum();
        order.setValueToPay(total);
    }

    private OrderResponseDTO buildOrderResponseDTO(Order order) {
        List<OrderItemResponseDTO> orderItemDTOs = getOrderItemResponse(order);
        OrderResponseDTO responseDTO = modelMapper.map(order, OrderResponseDTO.class);
        responseDTO.setOrderItemList(orderItemDTOs);
        return responseDTO;
    }
}
