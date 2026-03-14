package com.example.NgoDangKhoa_2280601515.repository;

import com.example.NgoDangKhoa_2280601515.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    List<Voucher> findByActiveTrue();
    List<Voucher> findByActiveTrueAndRemainingQuantityGreaterThan(int quantity);
}
