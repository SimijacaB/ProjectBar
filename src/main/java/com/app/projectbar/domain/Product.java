package com.app.projectbar.domain;

import com.app.projectbar.domain.enums.Category;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Builder
@Entity
@Table(name = "products")
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String code;

    @Column(unique = true)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(name = "photo_id")
    private Long photoId;

    @Column(nullable = false)
    private Double price;

    @Column(name = "is_prepared")
    private Boolean isPrepared;

    @Enumerated(EnumType.STRING)
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductIngredient> productIngredients = new ArrayList<>(); ;

}
