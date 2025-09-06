package com.app.projectbar.domain;


import com.app.projectbar.domain.enums.UnitOfMeasure;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Builder
@Entity
@Table(name = "ingredients")
@AllArgsConstructor
@NoArgsConstructor
public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String code;
    private String name;

    @Enumerated(EnumType.STRING)
    private UnitOfMeasure unitOfMeasure;


    @OneToMany(mappedBy = "ingredient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductIngredient> product = new ArrayList<>();
}
