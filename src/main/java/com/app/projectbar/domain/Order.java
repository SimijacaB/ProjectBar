package com.app.projectbar.domain;

import com.app.projectbar.domain.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@Builder
@Entity
@Table(name = "orders")
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_name")
    private String clientName;

    @Column(name = "table_number")
    private Integer tableNumber;

    @Column(name = "value_to_pay")
    private Double valueToPay;

    @Column(name = "waiter_id")
    private String waiterId;

    private String notes;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime date;


    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true )
    @JsonManagedReference
    private List<OrderItem> orderProducts;

    @PrePersist //Se ejecuta antes de que se guarde en la base de datos
    protected void onCreate() {
        if (status == null) {
            status = OrderStatus.PENDING;
            date = LocalDateTime.now();
        }
    }
}
