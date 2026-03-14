package com.example.NgoDangKhoa_2280601515.controller;

import com.example.NgoDangKhoa_2280601515.model.User;
import com.example.NgoDangKhoa_2280601515.repository.UserRepository;
import com.example.NgoDangKhoa_2280601515.service.RewardService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RewardService rewardService;

    private static final int WELCOME_BONUS_POINTS = 10000;

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (session.getAttribute("rewardEmail") != null) {
            return "redirect:/";
        }
        return "rewards/login"; // Tạm thời dùng chung giao diện đẹp bên rewards
    }

    @PostMapping("/login")
    public String login(@RequestParam("email") String email,
                        @RequestParam("password") String password,
                        @RequestParam(value = "redirectUrl", defaultValue = "/") String redirectUrl,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng nhập đầy đủ email và mật khẩu!");
            return "redirect:/login";
        }

        Optional<User> userOpt = userRepository.findByEmail(email.trim());
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Email chưa được đăng ký! Vui lòng tạo tài khoản.");
            return "redirect:/login";
        }

        User user = userOpt.get();
        if (!user.getPassword().equals(password)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu không chính xác!");
            return "redirect:/login";
        }

        if (!user.isActive()) {
            redirectAttributes.addFlashAttribute("error", "Tài khoản đã bị vô hiệu hóa!");
            return "redirect:/login";
        }

        // Lưu session dùng chung cho cả Hệ thống (Rewards, Cart, Admin)
        session.setAttribute("rewardEmail", user.getEmail());
        session.setAttribute("rewardName", user.getName());
        session.setAttribute("rewardUserId", user.getId());
        session.setAttribute("userRole", user.getRole());

        if (user.getRole().equals("MANAGER") || user.getRole().equals("ADMIN")) {
            return "redirect:/admin";
        }

        return "redirect:" + redirectUrl;
    }

    @GetMapping("/register")
    public String registerPage(HttpSession session) {
        if (session.getAttribute("rewardEmail") != null) {
            return "redirect:/";
        }
        return "rewards/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam("name") String name,
                           @RequestParam("email") String email,
                           @RequestParam("password") String password,
                           @RequestParam("confirmPassword") String confirmPassword,
                           @RequestParam(value = "phone", required = false) String phone,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        // Validate
        if (name == null || name.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng nhập họ tên!");
            return "redirect:/register";
        }
        if (email == null || email.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng nhập email!");
            return "redirect:/register";
        }
        if (password == null || password.length() < 4) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu phải có ít nhất 4 ký tự!");
            return "redirect:/register";
        }
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp!");
            return "redirect:/register";
        }
        if (userRepository.existsByEmail(email.trim())) {
            redirectAttributes.addFlashAttribute("error", "Email đã được đăng ký! Vui lòng đăng nhập.");
            return "redirect:/register";
        }

        // Tạo user mới
        User user = new User();
        user.setName(name.trim());
        user.setEmail(email.trim());
        user.setPassword(password);
        user.setPhone(phone != null ? phone.trim() : null);
        user.setActive(true);
        user.setRole("USER"); // Mặc định là USER
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Tặng điểm chào mừng
        rewardService.earnPoints(user.getEmail(), WELCOME_BONUS_POINTS,
                "🎉 Điểm chào mừng thành viên mới (" + WELCOME_BONUS_POINTS + " điểm)");

        // Auto đăng nhập sau khi đăng ký
        session.setAttribute("rewardEmail", user.getEmail());
        session.setAttribute("rewardName", user.getName());
        session.setAttribute("rewardUserId", user.getId());
        session.setAttribute("userRole", user.getRole());

        redirectAttributes.addFlashAttribute("success", "Đăng ký thành công! Nhận " + WELCOME_BONUS_POINTS + " điểm thưởng.");
        return "redirect:/rewards";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        return "redirect:/"; // Quay về trang chủ
    }
}
