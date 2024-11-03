package com.app.projectbar.domain.dto.bill;

import com.app.projectbar.domain.OrderItem;

import java.time.LocalDateTime;
import java.util.List;

public class BillRequestDTO {

    private String clientName;

    private LocalDateTime billingDate;

    private List<OrderItem> items;

    private Long billNumber;

    private Double totalAmount;

    private Double taxAmount;

    private Double discountAmount;

    private String createdBy;

}
