package com.example.NgoDangKhoa_2280601515.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vouchers")
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;
    private String name;
    private String description;
    private String image;

    private int pointsRequired;       // Số điểm cần để đổi
    private double discountAmount;     // Số tiền giảm giá (VND)
    private int discountPercent;       // % giảm giá (0 nếu giảm cố định)
    private double maxDiscount;        // Giảm tối đa (nếu dùng %)

    private int totalQuantity;         // Tổng số lượng voucher
    private int remainingQuantity;     // Số lượng còn lại

    private LocalDateTime expiryDate;  // Ngày hết hạn
    private boolean active = true;     // Trạng thái hoạt động

    private LocalDateTime createdAt = LocalDateTime.now();
}
