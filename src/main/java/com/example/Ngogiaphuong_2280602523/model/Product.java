package com.example.Ngogiaphuong_2280602523.model;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private double price;
    private String description;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String image; 

    @Column(name = "is_promotion")
    private Boolean promotion; 

    @Column(name = "flash_sale_quantity")
    private Integer flashSaleQuantity;

    @Column(name = "flash_sale_sold")
    private Integer flashSaleSold;

    private double oldPrice;
    private double discount;
    private String link;

    @Transient
    private String categoryName;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}
