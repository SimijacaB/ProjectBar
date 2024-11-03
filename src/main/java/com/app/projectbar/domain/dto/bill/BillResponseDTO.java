package com.app.projectbar.domain.dto.bill;

import com.app.projectbar.domain.OrderItem;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BillResponseDTO {

    private String clientName;

    private LocalDateTime billingDate;

    private List<OrderItem> items;

    private Long billNumber;

    private Double totalAmount;

    private Double taxAmount;

    private Double discountAmount;

    private String createdBy;
}
