package com.example.NgoDangKhoa_2280601515.repository;

import com.example.NgoDangKhoa_2280601515.model.RewardPointTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RewardPointTransactionRepository extends JpaRepository<RewardPointTransaction, Long> {
    List<RewardPointTransaction> findByCustomerEmailOrderByCreatedAtDesc(String email);

    @Query("SELECT COALESCE(SUM(r.points), 0) FROM RewardPointTransaction r WHERE r.customerEmail = :email")
    int getTotalPointsByEmail(@Param("email") String email);

    List<RewardPointTransaction> findByCustomerEmailAndType(String email, RewardPointTransaction.TransactionType type);

    java.util.Optional<RewardPointTransaction> findByCustomerEmailAndVoucherCode(String email, String voucherCode);
}
