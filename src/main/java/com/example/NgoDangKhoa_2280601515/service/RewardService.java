package com.example.NgoDangKhoa_2280601515.service;

import com.example.NgoDangKhoa_2280601515.model.RewardPointTransaction;
import com.example.NgoDangKhoa_2280601515.model.Voucher;
import com.example.NgoDangKhoa_2280601515.repository.RewardPointTransactionRepository;
import com.example.NgoDangKhoa_2280601515.repository.VoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RewardService {

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private RewardPointTransactionRepository transactionRepository;

    /**
     * Lấy tổng điểm tích lũy của user
     */
    public int getTotalPoints(String email) {
        return transactionRepository.getTotalPointsByEmail(email);
    }

    /**
     * Lấy danh sách voucher còn khả dụng
     */
    public List<Voucher> getAvailableVouchers() {
        return voucherRepository.findByActiveTrueAndRemainingQuantityGreaterThan(0);
    }

    /**
     * Lấy lịch sử giao dịch của user
     */
    public List<RewardPointTransaction> getTransactionHistory(String email) {
        return transactionRepository.findByCustomerEmailOrderByCreatedAtDesc(email);
    }

    /**
     * Lấy danh sách voucher đã đổi của user
     */
    public List<RewardPointTransaction> getRedeemedVouchers(String email) {
        return transactionRepository.findByCustomerEmailAndType(email, RewardPointTransaction.TransactionType.REDEEM);
    }

    /**
     * Đổi điểm lấy voucher
     */
    @Transactional
    public RewardPointTransaction redeemVoucher(String email, Long voucherId) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new IllegalArgumentException("Voucher không tồn tại!"));

        // Kiểm tra voucher còn hoạt động
        if (!voucher.isActive()) {
            throw new IllegalStateException("Voucher đã ngừng hoạt động!");
        }

        // Kiểm tra số lượng còn lại
        if (voucher.getRemainingQuantity() <= 0) {
            throw new IllegalStateException("Voucher đã hết số lượng!");
        }

        // Kiểm tra hạn sử dụng
        if (voucher.getExpiryDate() != null && voucher.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Voucher đã hết hạn!");
        }

        // Kiểm tra điểm tích lũy
        int currentPoints = getTotalPoints(email);
        if (currentPoints < voucher.getPointsRequired()) {
            throw new IllegalStateException("Bạn không đủ điểm để đổi voucher này! Cần " 
                    + voucher.getPointsRequired() + " điểm, hiện có " + currentPoints + " điểm.");
        }

        // Tạo mã voucher duy nhất cho user
        String uniqueCode = voucher.getCode() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Tạo giao dịch đổi điểm
        RewardPointTransaction transaction = new RewardPointTransaction();
        transaction.setCustomerEmail(email);
        transaction.setType(RewardPointTransaction.TransactionType.REDEEM);
        transaction.setPoints(-voucher.getPointsRequired());
        transaction.setDescription("Đổi voucher: " + voucher.getName());
        transaction.setVoucher(voucher);
        transaction.setVoucherCode(uniqueCode);
        transaction.setCreatedAt(LocalDateTime.now());

        // Giảm số lượng voucher
        voucher.setRemainingQuantity(voucher.getRemainingQuantity() - 1);
        voucherRepository.save(voucher);

        return transactionRepository.save(transaction);
    }

    /**
     * Tích điểm khi thanh toán thành công
     */
    @Transactional
    public RewardPointTransaction earnPoints(String email, int points, String description) {
        RewardPointTransaction transaction = new RewardPointTransaction();
        transaction.setCustomerEmail(email);
        transaction.setType(RewardPointTransaction.TransactionType.EARN);
        transaction.setPoints(points);
        transaction.setDescription(description);
        transaction.setCreatedAt(LocalDateTime.now());
        return transactionRepository.save(transaction);
    }

    /**
     * Khởi tạo dữ liệu voucher mẫu nếu chưa có
     */
    @Transactional
    public void initSampleVouchers() {
        if (voucherRepository.count() == 0) {
            Voucher v1 = new Voucher();
            v1.setCode("GIAM50K");
            v1.setName("Giảm 50.000₫");
            v1.setDescription("Áp dụng cho đơn hàng từ 500.000₫. Giảm trực tiếp 50.000₫ vào tổng đơn.");
            v1.setImage("https://cdn-icons-png.flaticon.com/512/726/726476.png");
            v1.setPointsRequired(10);
            v1.setDiscountAmount(50000);
            v1.setDiscountPercent(0);
            v1.setMaxDiscount(50000);
            v1.setTotalQuantity(100);
            v1.setRemainingQuantity(100);
            v1.setExpiryDate(LocalDateTime.of(2026, 12, 31, 23, 59));
            v1.setActive(true);
            voucherRepository.save(v1);

            Voucher v2 = new Voucher();
            v2.setCode("GIAM100K");
            v2.setName("Giảm 100.000₫");
            v2.setDescription("Áp dụng cho đơn hàng từ 1.000.000₫. Giảm trực tiếp 100.000₫ vào tổng đơn.");
            v2.setImage("https://cdn-icons-png.flaticon.com/512/726/726488.png");
            v2.setPointsRequired(20);
            v2.setDiscountAmount(100000);
            v2.setDiscountPercent(0);
            v2.setMaxDiscount(100000);
            v2.setTotalQuantity(50);
            v2.setRemainingQuantity(50);
            v2.setExpiryDate(LocalDateTime.of(2026, 12, 31, 23, 59));
            v2.setActive(true);
            voucherRepository.save(v2);

            Voucher v3 = new Voucher();
            v3.setCode("GIAM10PT");
            v3.setName("Giảm 10%");
            v3.setDescription("Giảm 10% tổng đơn hàng, tối đa 200.000₫. Áp dụng cho mọi đơn hàng.");
            v3.setImage("https://cdn-icons-png.flaticon.com/512/726/726495.png");
            v3.setPointsRequired(15);
            v3.setDiscountAmount(0);
            v3.setDiscountPercent(10);
            v3.setMaxDiscount(200000);
            v3.setTotalQuantity(80);
            v3.setRemainingQuantity(80);
            v3.setExpiryDate(LocalDateTime.of(2026, 12, 31, 23, 59));
            v3.setActive(true);
            voucherRepository.save(v3);

            Voucher v4 = new Voucher();
            v4.setCode("FREESHIP");
            v4.setName("Miễn phí vận chuyển");
            v4.setDescription("Miễn phí vận chuyển cho đơn hàng bất kỳ. Tiết kiệm đến 30.000₫.");
            v4.setImage("https://cdn-icons-png.flaticon.com/512/2769/2769339.png");
            v4.setPointsRequired(5);
            v4.setDiscountAmount(30000);
            v4.setDiscountPercent(0);
            v4.setMaxDiscount(30000);
            v4.setTotalQuantity(200);
            v4.setRemainingQuantity(200);
            v4.setExpiryDate(LocalDateTime.of(2026, 12, 31, 23, 59));
            v4.setActive(true);
            voucherRepository.save(v4);

            Voucher v5 = new Voucher();
            v5.setCode("GIAM200K");
            v5.setName("Giảm 200.000₫");
            v5.setDescription("Siêu voucher giảm 200.000₫ cho đơn hàng từ 2.000.000₫. Số lượng có hạn!");
            v5.setImage("https://cdn-icons-png.flaticon.com/512/726/726476.png");
            v5.setPointsRequired(40);
            v5.setDiscountAmount(200000);
            v5.setDiscountPercent(0);
            v5.setMaxDiscount(200000);
            v5.setTotalQuantity(30);
            v5.setRemainingQuantity(30);
            v5.setExpiryDate(LocalDateTime.of(2026, 12, 31, 23, 59));
            v5.setActive(true);
            voucherRepository.save(v5);

            Voucher v6 = new Voucher();
            v6.setCode("GIAM20PT");
            v6.setName("Giảm 20%");
            v6.setDescription("Giảm 20% tổng đơn hàng, tối đa 500.000₫. Dành cho khách hàng VIP!");
            v6.setImage("https://cdn-icons-png.flaticon.com/512/726/726495.png");
            v6.setPointsRequired(50);
            v6.setDiscountAmount(0);
            v6.setDiscountPercent(20);
            v6.setMaxDiscount(500000);
            v6.setTotalQuantity(20);
            v6.setRemainingQuantity(20);
            v6.setExpiryDate(LocalDateTime.of(2026, 12, 31, 23, 59));
            v6.setActive(true);
            voucherRepository.save(v6);
        }
    }
}
