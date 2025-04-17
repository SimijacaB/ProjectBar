package com.app.projectbar.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@Builder
@Entity
@Table(name = "bills")
@AllArgsConstructor
@NoArgsConstructor
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_name")
    private String clientName;

    @Column(name = "billing_date")
    private LocalDateTime billingDate;

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders;

    @Column(name = "bill_number", unique = true)
    private String billNumber;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(name = "created_by")
    private String createdBy;


}
