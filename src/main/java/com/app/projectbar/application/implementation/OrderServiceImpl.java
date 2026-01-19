package com.app.projectbar.application.implementation;

import com.app.projectbar.application.exception.ErrorMessagesService;
import com.app.projectbar.application.exception.orders.OrderNotFoundByIdException;
import com.app.projectbar.application.exception.orders.OrdersAlreadyBilledException;
import com.app.projectbar.application.exception.orders.OrdersNotFoundByStatusException;
import com.app.projectbar.application.interfaces.IInventoryService;
import com.app.projectbar.application.interfaces.IOrderService;
import com.app.projectbar.application.mapper.OrderMapper;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final OrderMapper orderMapper;



    @Override
    @Transactional
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

                // Descontar ingredientes del inventario si el producto requiere preparación
                deductIngredientsFromInventory(product, itemRequest.getQuantity());

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

        return orderMapper.toResponseDTO(orderRepository.save(newOrder));
    }

    /**
     * Descuenta del inventario según el tipo de producto:
     * - Si el producto tiene ingredientes (isPrepared = true): descuenta los ingredientes
     * - Si el producto NO tiene ingredientes (isPrepared = false o null): descuenta el producto directamente
     *
     * @param product El producto a descontar
     * @param quantity La cantidad de productos pedidos
     */
    private void deductIngredientsFromInventory(Product product, Integer quantity) {
        // Si el producto requiere preparación y tiene ingredientes, descontar ingredientes
        if (product.getIsPrepared() != null && product.getIsPrepared()
                && product.getProductIngredients() != null && !product.getProductIngredients().isEmpty()) {
            
            for (var productIngredient : product.getProductIngredients()) {
                String ingredientCode = productIngredient.getIngredient().getCode();
                // La cantidad a descontar es: amount del ingrediente * cantidad de productos
                int amountToDeduct = (int) Math.ceil(productIngredient.getAmount() * quantity);
                
                try {
                    inventoryService.deductStock(amountToDeduct, ingredientCode);
                } catch (RuntimeException e) {
                    // Si no hay suficiente inventario, lanzar excepción con mensaje descriptivo
                    throw new RuntimeException(
                        "No hay suficiente inventario del ingrediente '" + 
                        productIngredient.getIngredient().getName() + 
                        "' para el producto '" + product.getName() + "'. " + e.getMessage()
                    );
                }
            }
        } else {
            // Si el producto NO tiene ingredientes, descontar el producto directamente del inventario
            try {
                inventoryService.deductStock(quantity, product.getCode());
            } catch (RuntimeException e) {
                throw new RuntimeException(
                    "No hay suficiente inventario del producto '" +
                    product.getName() + "'. " + e.getMessage()
                );
            }
        }
    }

    @Override
    public List<OrderForListResponseDTO> findAll() {
        var orders = orderRepository.findAll();
        return orderMapper.toListDTOList(orders);
    }

    @Override
    public OrderResponseDTO findById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundByIdException(
                        ErrorMessagesService.ORDER_NOT_FOUND_BY_ID_EXCEPTION.getMessage()));

        return orderMapper.toResponseDTO(order);
    }


    @Override
    public OrderResponseDTO updateOrder(UpdateOrderDTO updateOrderDTO) {
        var order = orderRepository.findById(updateOrderDTO.getId())
                .orElseThrow(() -> new OrderNotFoundByIdException(ErrorMessagesService.ORDER_NOT_FOUND_BY_ID_EXCEPTION.getMessage()));

        order.setClientName(updateOrderDTO.getClientName());
        order.setTableNumber(updateOrderDTO.getTableNumber());
        order.setNotes(updateOrderDTO.getNotes());

        return orderMapper.toResponseDTO(orderRepository.save(order));
    }

    @Override
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    @Override
    public List<OrderForListResponseDTO> findByClientName(String name) {
        List<Order> orders = orderRepository.findByClientName(name);
        return orderMapper.toListDTOList(orders);
    }

    @Override
    public List<OrderForListResponseDTO> findByTableNumber(Integer tableNumber) {
        List<Order> orders = orderRepository.findByTableNumber(tableNumber);
        return orderMapper.toListDTOList(orders);
    }

    @Override
    public List<OrderForListResponseDTO> findByWaiterId(String id) {

        List<Order> orders = orderRepository.findByWaiterUserName(id);
        return orderMapper.toListDTOList(orders);
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
        return orderMapper.toListDTOList(orders);
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
        return orderMapper.toListDTOList(orders);
    }

    @Override
    public List<OrderForListResponseDTO> findByDate(LocalDate date) {
        // Convertir LocalDate a rango de LocalDateTime (inicio y fin del día)
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59, 999999999);
        
        List<Order> orders = orderRepository.findByDateBetween(startOfDay, endOfDay);
        return orderMapper.toListDTOList(orders);
    }

    @Override
    public List<OrderForListResponseDTO> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderRepository.findByDateBetween(startDate, endDate);
        return orderMapper.toListDTOList(orders);
    }

    @Override
    public List<OrderForListResponseDTO> findByStatus(OrderStatus status) {
        List<Order> orders = orderRepository.findByStatus(status);
        if(orders.isEmpty()){
            throw new OrdersNotFoundByStatusException(ErrorMessagesService.ORDERS_NOT_FOUND_BY_STATUS_EXCEPTION.getMessage());
        }
        return orderMapper.toListDTOList(orders);
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
                .map(orderMapper::toListDTO)
                .collect(Collectors.groupingBy(OrderForListResponseDTO::getClientName));

    }

    @Override
    public OrderResponseDTO addOrderItem(Long orderId, OrderItemRequestDTO orderItemToAdd) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        // VALIDACIÓN: No permitir agregar items a órdenes entregadas
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


        // Construcción de respuesta optimizada
        return orderMapper.toResponseDTO(orderRepository.save(order));
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

        return orderMapper.toResponseDTO(orderRepository.save(order));
    }

    @Override
    public OrderResponseDTO changeStatus(Long id, String newStatus) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundByIdException(ErrorMessagesService.ORDER_NOT_FOUND_BY_ID_EXCEPTION.getMessage()));

        OrderStatus targetStatus = OrderStatus.valueOf(newStatus);

        // Validaciones de transición de estado según reglas de negocio
        validateStatusTransition(order, targetStatus);

        // CORRECCIÓN: Se eliminó el descuento duplicado de inventario
        // El inventario ya se descuenta al crear la orden (línea 135)
        // No se debe descontar nuevamente al cambiar a DELIVERED

        order.setStatus(targetStatus);

        return orderMapper.toResponseDTO(orderRepository.save(order));
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
        
        return orderMapper.toResponseDTO(orderRepository.save(order));
    }

    @Override
    public List<OrderForListResponseDTO> findUnassignedOrders() {
        List<Order> orders = orderRepository.findByStatus(OrderStatus.CREATED);
        return orderMapper.toListDTOList(orders);
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
        return orderMapper.toListDTOList(orders);
    }


    /**
     * Valida que las órdenes puedan ser facturadas.
     * Una orden puede facturarse SOLO si está en estado DELIVERED.
     * Una orden NO puede facturarse si ya fue facturada (tiene bill_id).
     */
    public void validateIfOrderCanBeBilled(List<Order> orders) {
        Set<Long> notDeliveredOrderIds = new HashSet<>();
        Set<Long> alreadyBilledOrderIds = new HashSet<>();

        for (Order order : orders) {
            // Validar que la orden esté entregada
            if (!OrderStatus.DELIVERED.equals(order.getStatus())) {
                notDeliveredOrderIds.add(order.getId());
            }

            // Validar que la orden no haya sido facturada previamente
            if (order.getBill() != null) {
                alreadyBilledOrderIds.add(order.getId());
            }
        }

        if (!notDeliveredOrderIds.isEmpty()) {
            String ids = String.join(", ",
                    notDeliveredOrderIds.stream().map(String::valueOf).collect(Collectors.toList()));
            throw new RuntimeException("Cannot bill orders that are not DELIVERED. Order IDs: " + ids);
        }

        if (!alreadyBilledOrderIds.isEmpty()) {
            String ids = String.join(", ",
                    alreadyBilledOrderIds.stream().map(String::valueOf).collect(Collectors.toList()));
            throw new OrdersAlreadyBilledException(
                    ErrorMessagesService.ORDER_ALREADY_BILLED_EXCEPTION + ids);
        }
    }


    /**
     * Marca las órdenes como facturadas (BILLED).
     * IMPORTANTE: Este método solo debe llamarse después de validar que las órdenes
     * estén en estado DELIVERED.
     */
    public void setOrdersAsBilled(List<Order> orders){
        for (Order order : orders){
            // Cambiar a BILLED solo si está DELIVERED
            if (OrderStatus.DELIVERED.equals(order.getStatus())) {
                order.setStatus(OrderStatus.BILLED);
            }
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


}
