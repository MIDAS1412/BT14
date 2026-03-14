package com.example.NgoDangKhoa_2280601515.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reward_point_transactions")
public class RewardPointTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerEmail;     // Email của khách hàng

    @Enumerated(EnumType.STRING)
    private TransactionType type;     // EARN hoặc REDEEM

    private int points;               // Số điểm (dương = tích, âm = đổi)
    private String description;       // Mô tả giao dịch

    @ManyToOne
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;          // Voucher đã đổi (null nếu type = EARN)

    private String voucherCode;       // Mã voucher được tạo cho user

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum TransactionType {
        EARN, REDEEM
    }
}
