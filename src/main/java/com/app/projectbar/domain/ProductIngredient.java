package com.app.projectbar.domain;

import jakarta.persistence.*;
import lombok.*;


@Setter
@Getter
@Builder
@Entity
@Table(name = "product_ingredients")
@AllArgsConstructor
@NoArgsConstructor
public class ProductIngredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    private Double amount;
}
