package com.example.NgoDangKhoa_2280601515.controller;

import com.example.NgoDangKhoa_2280601515.model.RewardPointTransaction;
import com.example.NgoDangKhoa_2280601515.model.Voucher;
import com.example.NgoDangKhoa_2280601515.service.EmailService;
import com.example.NgoDangKhoa_2280601515.service.RewardService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Controller
@RequestMapping("/rewards")
public class RewardController {

    @Autowired
    private RewardService rewardService;

    @Autowired
    private EmailService emailService;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 2;

    // ==================== ĐĂNG XUẤT ====================

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("rewardEmail");
        session.removeAttribute("rewardName");
        session.removeAttribute("rewardUserId");
        return "redirect:/rewards/login";
    }

    // ==================== TRANG ĐỔI ĐIỂM ====================

    @GetMapping
    public String rewardPage(HttpSession session, Model model) {
        String email = (String) session.getAttribute("rewardEmail");
        String name = (String) session.getAttribute("rewardName");

        if (email == null) {
            return "redirect:/rewards/login";
        }

        rewardService.initSampleVouchers();

        int totalPoints = rewardService.getTotalPoints(email);
        List<Voucher> vouchers = rewardService.getAvailableVouchers();
        List<RewardPointTransaction> history = rewardService.getTransactionHistory(email);
        List<RewardPointTransaction> redeemedVouchers = rewardService.getRedeemedVouchers(email);

        model.addAttribute("email", email);
        model.addAttribute("name", name);
        model.addAttribute("totalPoints", totalPoints);
        model.addAttribute("vouchers", vouchers);
        model.addAttribute("history", history);
        model.addAttribute("redeemedVouchers", redeemedVouchers);

        return "rewards/redeem";
    }

    // ==================== OTP ====================

    private String generateOTP() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    @PostMapping("/otp/send")
    @ResponseBody
    public Map<String, Object> sendOtp(@RequestParam("voucherId") Long voucherId,
                                        HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        String email = (String) session.getAttribute("rewardEmail");

        if (email == null) {
            response.put("success", false);
            response.put("message", "Bạn chưa đăng nhập!");
            return response;
        }

        String voucherName = "Voucher";
        try {
            Voucher voucher = rewardService.getAvailableVouchers().stream()
                    .filter(v -> v.getId().equals(voucherId))
                    .findFirst().orElse(null);
            if (voucher != null) {
                voucherName = voucher.getName();
            }
        } catch (Exception ignored) {}

        String otp = generateOTP();

        session.setAttribute("otp_code", otp);
        session.setAttribute("otp_expiry", LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        session.setAttribute("otp_voucherId", voucherId);

        boolean emailSent = emailService.sendOtpEmail(email, otp, voucherName);

        if (emailSent) {
            response.put("success", true);
            response.put("message", "Mã OTP đã được gửi đến email " + email);
        } else {
            response.put("success", true);
            response.put("message", "Mã OTP đã được tạo. Kiểm tra email " + email);
        }
        response.put("expiryMinutes", OTP_EXPIRY_MINUTES);
        response.put("emailSent", emailSent);

        return response;
    }

    @PostMapping("/otp/verify")
    @ResponseBody
    public Map<String, Object> verifyOtpAndRedeem(@RequestParam("otp") String inputOtp,
                                                    @RequestParam("voucherId") Long voucherId,
                                                    HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        String email = (String) session.getAttribute("rewardEmail");

        if (email == null) {
            response.put("success", false);
            response.put("message", "Bạn chưa đăng nhập!");
            return response;
        }

        String sessionOtp = (String) session.getAttribute("otp_code");
        LocalDateTime otpExpiry = (LocalDateTime) session.getAttribute("otp_expiry");
        Long sessionVoucherId = (Long) session.getAttribute("otp_voucherId");

        if (sessionOtp == null || otpExpiry == null || sessionVoucherId == null) {
            response.put("success", false);
            response.put("message", "Bạn chưa yêu cầu mã OTP! Vui lòng gửi OTP trước.");
            return response;
        }

        if (!sessionVoucherId.equals(voucherId)) {
            response.put("success", false);
            response.put("message", "Mã OTP không hợp lệ cho voucher này! Vui lòng gửi lại OTP.");
            return response;
        }

        if (LocalDateTime.now().isAfter(otpExpiry)) {
            session.removeAttribute("otp_code");
            session.removeAttribute("otp_expiry");
            session.removeAttribute("otp_voucherId");

            response.put("success", false);
            response.put("message", "Mã OTP đã hết hạn! Vui lòng gửi lại mã OTP mới.");
            response.put("expired", true);
            return response;
        }

        if (!sessionOtp.equals(inputOtp.trim())) {
            response.put("success", false);
            response.put("message", "Mã OTP không chính xác! Vui lòng kiểm tra lại.");
            return response;
        }

        try {
            RewardPointTransaction transaction = rewardService.redeemVoucher(email, voucherId);

            session.removeAttribute("otp_code");
            session.removeAttribute("otp_expiry");
            session.removeAttribute("otp_voucherId");

            response.put("success", true);
            response.put("message", "Đổi voucher thành công!");
            response.put("voucherCode", transaction.getVoucherCode());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    @PostMapping("/redeem/{voucherId}")
    public String redeemVoucher(@PathVariable Long voucherId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("rewardEmail");
        if (email == null) {
            return "redirect:/rewards/login";
        }
        redirectAttributes.addFlashAttribute("error", "Vui lòng xác thực OTP để đổi voucher!");
        return "redirect:/rewards";
    }

    @PostMapping("/earn")
    @ResponseBody
    public String earnPoints(@RequestParam("email") String email,
                             @RequestParam("points") int points,
                             @RequestParam(value = "description", defaultValue = "Tích điểm mua hàng") String description) {
        rewardService.earnPoints(email, points, description);
        return "OK";
    }

    @PostMapping("/api/apply-voucher")
    @ResponseBody
    public Map<String, Object> applyVoucher(@RequestParam("code") String code, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        String email = (String) session.getAttribute("rewardEmail");
        if (email == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập để sử dụng voucher!");
            return response;
        }

        try {
            List<RewardPointTransaction> redeemedVouchers = rewardService.getRedeemedVouchers(email);
            RewardPointTransaction appliedTx = redeemedVouchers.stream()
                .filter(tx -> code.equalsIgnoreCase(tx.getVoucherCode()))
                .findFirst()
                .orElse(null);

            if (appliedTx == null) {
                response.put("success", false);
                response.put("message", "Mã voucher không hợp lệ hoặc không thuộc về bạn!");
                return response;
            }

            Voucher v = appliedTx.getVoucher();
            response.put("success", true);
            response.put("message", "Áp dụng voucher " + v.getName() + " thành công!");
            response.put("discountAmount", v.getDiscountAmount());
            response.put("discountPercent", v.getDiscountPercent());
            response.put("maxDiscount", v.getMaxDiscount());
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi xử lý voucher: " + e.getMessage());
            return response;
        }
    }
}
