package com.example.Ngogiaphuong_2280602523.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên là bắt buộc")
    private String name;

    @Column(name = "parentId")
    private Long parentId;

    @Column(name = "imageurl", columnDefinition = "LONGTEXT")
    private String imageurl;

    private Boolean showInAccessories;

    @Transient
    private String parent; // Used for UI

    @Transient
    private String image; // Used for UI
}
