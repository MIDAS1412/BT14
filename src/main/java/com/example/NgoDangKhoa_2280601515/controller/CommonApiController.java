package com.example.NgoDangKhoa_2280601515.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/Common")
public class CommonApiController {

    @GetMapping("/ViewSearchKeywordHistory")
    public ResponseEntity<Map<String, Object>> getSearchKeywordHistory(
            @RequestParam(required = false) Boolean isSuggestSearch) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", new ArrayList<>());
        
        return ResponseEntity.ok(response);
    }
}
