package com.example.Ngogiaphuong_2280602523.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/cart/api/cart")
public class CartApiController {

    @GetMapping("/v2/get")
    public ResponseEntity<Map<String, Object>> getCart() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> items = new ArrayList<>();
        
        Map<String, Object> item = new HashMap<>();
        item.put("productId", 123456);
        item.put("productName", "iPhone 15 Pro Max 256GB");
        item.put("price", 29990000);
        item.put("quantity", 1);
        item.put("productCode", "0131491003185");
        item.put("categoryName", "Điện thoại");
        item.put("image", "https://cdn.tgdd.vn/Products/Images/42/305658/iphone-15-pro-max-blue-1-600x600.jpg");
        
        items.add(item);
        data.put("items", items);
        data.put("totalItems", 1);
        data.put("totalPrice", 29990000);
        
        response.put("data", data);
        
        return ResponseEntity.ok(response);
    }
}
