package com.example.NgoDangKhoa_2280601515.controller;

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
        data.put("items", new ArrayList<>());
        data.put("totalItems", 0);
        data.put("totalPrice", 0);
        
        response.put("data", data);
        
        return ResponseEntity.ok(response);
    }
}
