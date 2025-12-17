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
import com.app.projectbar.infra.repositories.IProductRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {

    private final IOrderRepository orderRepository;
    private final ModelMapper modelMapper;
    private final IProductRepository productRepository;
    private final IInventoryService inventoryService;


    @Override
    public OrderResponseDTO save(OrderRequestDTO orderRequest) {
        /*
        Dos escenarios posibles:
        1. Mesero autenticado toma la orden - se guarda su username
        2. Cliente hace pedido directo vía QR (sin autenticación) - waiterUserName queda null o "SELF_SERVICE"
         */

        String waiterId = null;
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() 
                    && !"anonymousUser".equals(authentication.getPrincipal())) {
                waiterId = authentication.getName();
            }
        } catch (Exception e) {
            // Si no hay autenticación, es un pedido directo del cliente
            waiterId = null;
        }

        // Si no hay mesero autenticado, es un pedido directo del cliente (SELF_SERVICE)
        if (waiterId == null || waiterId.isEmpty()) {
            waiterId = "SELF_SERVICE";
        }

        // Crear la orden sin los items primero
        Order newOrder = Order.builder()
                .clientName(orderRequest.getClientName())
                .tableNumber(orderRequest.getTableNumber())
                .notes(orderRequest.getNotes())
                .waiterUserName(waiterId)
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
    public List<OrderForListResponseDTO> findByDate(LocalDate date) {

        List<Order> orders = orderRepository.findByDate(date);
        return orders.stream().map(order -> modelMapper.map(order, OrderForListResponseDTO.class)).toList();
    }

    @Override
    public List<OrderForListResponseDTO> findByStatus(OrderStatus status) {
        List<Order> orders = orderRepository.findByStatus(status);
        if(orders.isEmpty()){
            throw new OrdersNotFoundByStatusException(ErrorMessagesService.ORDERS_NOT_FOUND_BY_STATUS_EXCEPTION.getMessage());
        }
        return orders.stream().map(order -> modelMapper.map(order, OrderForListResponseDTO.class)).toList();
    }

    /*@Override
    public OrderResponseDTO addOrderItem(Long orderId, OrderItemRequestDTO orderItemToAdd) {
        // 1. Recuperar la orden existente
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        // 2. Validar el producto
        Product product = productRepository.findOneByName(orderItemToAdd.getProductName())
                .orElseThrow(() -> new RuntimeException("Product not found with name: " + orderItemToAdd.getProductName()));

        Optional<OrderItem> existingOrderItem = order.getOrderItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (existingOrderItem.isPresent()) {
            System.out.println("Product name: " + existingOrderItem.get().getProductName());
        } else {
            System.out.println("No matching OrderItem found for Product ID: " + product.getId());
        }

        if (existingOrderItem.isPresent()) {
            // 4. Actualizar la cantidad si ya existe
            OrderItem orderItem = existingOrderItem.get();
            orderItem.setQuantity(orderItem.getQuantity() + orderItemToAdd.getQuantity());
        } else {
            // 5. Crear un nuevo OrderItem y asociarlo a la orden
            OrderItem newOrderItem = OrderItem.builder()
                    .productName(product.getName())
                    .quantity(orderItemToAdd.getQuantity())
                    .price(product.getPrice()) // Asignar el precio actual del producto
                    .order(order)
                    .build();
            order.getOrderItems().add(newOrderItem);
        }

        //Se modifica el método para poder calcular correctamente el valor total de cada producto

        double total = order.getOrderItems().stream()
                .mapToDouble(item -> item.getQuantity() * item.getProduct().getPrice())
                .sum();
        order.setValueToPay(total);

        // 7. Guardar la orden actualizada
        Order updatedOrder = orderRepository.save(order);

        List<OrderItemResponseDTO> orderItemDTOs = getOrderItemResponse(updatedOrder);

        // 8. Mapear la orden a OrderResponseDTO e incluir la lista de OrderItemResponseDTO
        OrderResponseDTO responseDTO = modelMapper.map(updatedOrder, OrderResponseDTO.class);
        responseDTO.setOrderItemList(orderItemDTOs);
        return responseDTO;
    }*/
    @Override
    public OrderResponseDTO addOrderItem(Long orderId, OrderItemRequestDTO orderItemToAdd) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        Product product = productRepository.findOneByName(orderItemToAdd.getProductName())
                .orElseThrow(() -> new RuntimeException("Product not found with name: " + orderItemToAdd.getProductName()));

        // ESTRATEGIA SENIOR: Convertir List a Map para acceso O(1)
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
            System.out.println("Updated quantity for product: " + existingItem.getProductName());
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
        Optional<Order> orderOptional = orderRepository.findById(id);
        if (orderOptional.isEmpty()) {
            throw new RuntimeException("Order with id " + id + " not found");
        }
        Order order = orderOptional.get();

        // Solo entra aquí si el estado a cambiar es "DELIVERED"
        if (newStatus.equals(OrderStatus.DELIVERED.toString())) {
            for (OrderItem item : order.getOrderItems()) {
                inventoryService.deductStock(item.getQuantity(), item.getProduct().getCode());
            }
        }

        order.setStatus(OrderStatus.valueOf(newStatus));
        orderRepository.save(order);

        return modelMapper.map(orderRepository.save(order), OrderResponseDTO.class);
    }


    private List<OrderItemResponseDTO> getOrderItemResponse(Order updatedOrder){
        // 7. Mapear la lista de OrderItems a OrderItemResponseDTO

        return updatedOrder.getOrderItems().stream()
                .map(orderItem -> OrderItemResponseDTO.builder()
                        .id(orderItem.getId())
                        .productName(orderItem.getProduct().getName())
                        .quantity(orderItem.getQuantity())
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

/*    // Método que se encargará de validar si todas las ordenes que se van a facturar, no están ya facturadas
    public void validateIfOrderCanBeBilled(List<Order> orders) {

        List<Long> alreadyBilledOrderIds = orders.stream()
                //.filter(order -> order.getStatus() == OrderStatus.READY) Debe estar entregada la orden para ser facturada
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .map(Order::getId)
                .toList();

        if(!alreadyBilledOrderIds.isEmpty()){
            String ids = alreadyBilledOrderIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
            throw new OrdersAlreadyBilledException(ErrorMessagesService.ORDER_ALREADY_BILLED_EXCEPTION + ids);
        }
    }*/

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


    /*// Este método se encarga de comprobar que las ordenes a facturar, existan
    public List<Order> getExistingOrdersOrThrow(List<Long> orderIds) {
        List<Order> existingOrders = new ArrayList<>();
        List<Long> notFoundIds = new ArrayList<>();

        for (Long id : orderIds) {
            Optional<Order> maybeOrder = orderRepository.findById(id);
            if (maybeOrder.isPresent()) {
                existingOrders.add(maybeOrder.get());
            } else {
                notFoundIds.add(id);
            }
        }

        if (!notFoundIds.isEmpty()) {
            throw new RuntimeException("The following Order IDs do not exist: " + notFoundIds);
        }

        return existingOrders;*/
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
