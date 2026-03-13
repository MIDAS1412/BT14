package com.example.Ngogiaphuong_2280602523.repository;

import com.example.Ngogiaphuong_2280602523.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByPromotionTrue();

    List<Product> findByPromotionFalse();
}
