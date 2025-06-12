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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

        //Se intenta obtener el usuario autenticado que crea la order, hay que tener en
        // cuenta que los demás usuarios que estén autenticados pueden agregar orderItems
        // a la order, los lo tanto, para nuestra lógica podemos considerar que solo el mesero
        // que la creo pueda agregar más orderItems.
        String waiterId = SecurityContextHolder.getContext().getAuthentication().getName();

        Order newOrder = modelMapper.map(orderRequest, Order.class);
        newOrder.setWaiterUserName(waiterId);
        newOrder = orderRepository.save(newOrder);
        return modelMapper.map(newOrder, OrderResponseDTO.class);
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
        return null;
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
                    .product(product)
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
    }

    @Override
    public OrderResponseDTO removeOrderItem(Long id, Long idOrderItem) {
        Optional<Order> orderOptional = orderRepository.findById(id);
        if (orderOptional.isEmpty()) {
            throw new RuntimeException("Order with id " + id + " not found");
        }
        Order order = orderOptional.get();

        OrderItem orderItem = order.getOrderItems()
                .stream()
                .filter(findOrderItem -> findOrderItem.getId().equals(idOrderItem))
                .findFirst().get();


        order.getOrderItems().remove(modelMapper.map(orderItem, OrderItem.class));
        order.setOrderItems(order.getOrderItems());

        // 7. Guardar la orden actualizada
        Order updatedOrder = orderRepository.save(order);

        List<OrderItemResponseDTO> orderItemDTOs = getOrderItemResponse(updatedOrder);

        // 8. Mapear la orden a OrderResponseDTO e incluir la lista de OrderItemResponseDTO
        OrderResponseDTO responseDTO = modelMapper.map(updatedOrder, OrderResponseDTO.class);
        responseDTO.setOrderItemList(orderItemDTOs);
        return responseDTO;

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

    // Método que se encargará de validar si todas las ordenes que se van a facturar, no están ya facturadas
    public void validateIfOrderCanBeBilled(List<Order> orders) {

        List<Long> alreadyBilledOrderIds = orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.READY)
                .map(Order::getId)
                .toList();

        if(!alreadyBilledOrderIds.isEmpty()){
            String ids = alreadyBilledOrderIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
            throw new OrdersAlreadyBilledException(ErrorMessagesService.ORDER_ALREADY_BILLED_EXCEPTION + ids);
        }
    }

    // método que se encargará de settear el OrderStatus de la Orders que se vayan a facturar a READY
    public void setOrdersAsReady(List<Order> orders){
        for (Order order : orders){
            order.setStatus(OrderStatus.READY);
        }
        orderRepository.saveAll(orders);
    }

    // Este método se encarga de comprobar que las ordenes a facturar, existan
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

        return existingOrders;
    }
}



