package com.example.NgoDangKhoa_2280601515.controller;

import com.example.NgoDangKhoa_2280601515.model.Category;
import com.example.NgoDangKhoa_2280601515.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API Controller để test dữ liệu danh mục
 */
@RestController
@RequestMapping("/api/test")
public class TestCategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/categories")
    public String testCategories() {
        List<Category> categories = categoryRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("Số lượng danh mục: ").append(categories.size()).append("<br><br>");
        
        for (Category category : categories) {
            sb.append("ID: ").append(category.getId())
              .append(" | Name: ").append(category.getName())
              .append(" | ParentId: ").append(category.getParentId())
              .append("<br>");
        }
        
        return sb.toString();
    }
}
