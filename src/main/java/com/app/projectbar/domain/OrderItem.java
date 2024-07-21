package com.app.projectbar.domain;

import jakarta.persistence.*;
import lombok.*;


@Setter
@Getter
@Builder
@Entity
@Table(name = "order_item")
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productName;

    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;


}
