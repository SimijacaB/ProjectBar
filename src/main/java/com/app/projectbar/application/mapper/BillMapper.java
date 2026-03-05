package com.app.projectbar.application.mapper;

import com.app.projectbar.domain.Bill;
import com.app.projectbar.domain.Order;
import com.app.projectbar.domain.OrderItem;
import com.app.projectbar.domain.dto.bill.BillDTO;
import com.app.projectbar.domain.dto.bill.BillReportDTO;
import com.app.projectbar.domain.dto.orderItem.ItemReportDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface BillMapper {

    @Mapping(target = "items", source = "orders", qualifiedByName = "ordersToItems")
    BillDTO toDTO(Bill bill);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orders", ignore = true)
    Bill toEntity(BillDTO dto);

    List<BillDTO> toDTOList(List<Bill> bills);

    @Named("ordersToItems")
    default List<OrderItem> ordersToItems(List<Order> orders) {
        if (orders == null) {
            return new ArrayList<>();
        }
        List<OrderItem> items = new ArrayList<>();
        for (Order order : orders) {
            if (order.getOrderItems() != null) {
                items.addAll(order.getOrderItems());
            }
        }
        return items;
    }

    @Mapping(target = "items", source = "orders", qualifiedByName = "ordersToItemReportDTOs")
    BillReportDTO toReportDTO(Bill bill);

    @Named("ordersToItemReportDTOs")
    default List<ItemReportDTO> ordersToItemReportDTOs(List<Order> orders) {
        if (orders == null) {
            return new ArrayList<>();
        }
        return orders.stream()
                .flatMap(order -> order.getOrderItems() != null ? order.getOrderItems().stream() : new ArrayList<OrderItem>().stream())
                .map(item -> ItemReportDTO.builder()
                        .productName(item.getProduct() != null ? item.getProduct().getName() : "Unknown")
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .subtotal(item.getPrice() * item.getQuantity())
                        .build())
                .collect(Collectors.toList());
    }
}

