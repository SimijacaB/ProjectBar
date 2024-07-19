package com.app.projectbar.domain;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Builder
@Entity
@Table(name = "inventory")
@AllArgsConstructor
@NoArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    private String quantity;

}
