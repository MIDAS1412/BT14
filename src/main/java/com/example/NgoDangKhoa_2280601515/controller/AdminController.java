package com.example.NgoDangKhoa_2280601515.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping
    public String adminPage(HttpSession session) {
        String role = (String) session.getAttribute("userRole");
        if (role == null || (!role.equals("MANAGER") && !role.equals("ADMIN"))) {
            return "redirect:/login"; // Cần đăng nhập với quyền tối thiểu MANAGER
        }
        return "admin";
    }

    @GetMapping("/categories")
    public String adminCategoriesPage(HttpSession session) {
        String role = (String) session.getAttribute("userRole");
        if (role == null || !role.equals("ADMIN")) {
            return "redirect:/admin"; // Chỉ ADMIN mới được xem danh mục, Manager bị hạn chế
        }
        return "admin-categories";
    }
}
