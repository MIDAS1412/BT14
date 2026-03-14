package com.example.NgoDangKhoa_2280601515.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private static final String FROM_EMAIL = "minhthe0359898246@gmail.com";
    private static final String FROM_NAME = "TGDD Rewards";

    /**
     * Gửi mã OTP qua email bằng Spring Boot Mail (SMTP)
     */
    public boolean sendOtpEmail(String toEmail, String otpCode, String voucherName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(FROM_EMAIL, FROM_NAME);
            helper.setTo(toEmail);
            helper.setSubject("🔐 Mã OTP xác thực đổi voucher - " + otpCode);

            String htmlContent = buildOtpEmailTemplate(otpCode, voucherName, toEmail);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("✅ OTP email sent successfully to: " + toEmail);
            return true;
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            System.err.println("❌ Failed to send OTP email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Tạo template email HTML đẹp cho OTP
     */
    private String buildOtpEmailTemplate(String otpCode, String voucherName, String email) {
        return "<!DOCTYPE html>" +
            "<html>" +
            "<head><meta charset='UTF-8'></head>" +
            "<body style='margin:0; padding:0; background-color:#0f172a; font-family:Arial,sans-serif;'>" +
            "<div style='max-width:500px; margin:0 auto; padding:40px 20px;'>" +

            // Card container
            "<div style='background-color:#1e293b; border-radius:20px; overflow:hidden; box-shadow:0 20px 50px rgba(0,0,0,0.3);'>" +

            // Header gradient bar
            "<div style='height:6px; background:linear-gradient(90deg, #f59e0b, #ef4444, #8b5cf6);'></div>" +

            // Logo section
            "<div style='text-align:center; padding:40px 30px 20px;'>" +
            "<div style='display:inline-block; width:64px; height:64px; background:linear-gradient(135deg, #6366f1, #8b5cf6); border-radius:16px; line-height:64px; margin-bottom:16px;'>" +
            "<span style='font-size:28px;'>🔐</span>" +
            "</div>" +
            "<h1 style='color:#ffffff; font-size:22px; margin:0 0 8px;'>Mã xác thực OTP</h1>" +
            "<p style='color:#94a3b8; font-size:14px; margin:0;'>Đổi voucher tại TGDD Rewards</p>" +
            "</div>" +

            // OTP Code box
            "<div style='padding:0 30px;'>" +
            "<div style='background:rgba(99,102,241,0.1); border:2px dashed rgba(99,102,241,0.3); border-radius:16px; padding:24px; text-align:center; margin-bottom:20px;'>" +
            "<p style='color:#94a3b8; font-size:13px; margin:0 0 12px; letter-spacing:1px;'>MÃ OTP CỦA BẠN</p>" +
            "<div style='font-size:36px; font-weight:bold; color:#a5b4fc; letter-spacing:12px; font-family:Courier New,monospace;'>" +
            otpCode +
            "</div>" +
            "</div>" +
            "</div>" +

            // Voucher info
            "<div style='padding:0 30px;'>" +
            "<div style='background:rgba(245,158,11,0.1); border:1px solid rgba(245,158,11,0.2); border-radius:12px; padding:16px; margin-bottom:20px;'>" +
            "<p style='color:#fbbf24; font-size:14px; margin:0; text-align:center;'>" +
            "🎫 Voucher: <strong>" + voucherName + "</strong>" +
            "</p>" +
            "</div>" +
            "</div>" +

            // Warning & Info
            "<div style='padding:0 30px 30px;'>" +
            "<div style='background:rgba(239,68,68,0.08); border:1px solid rgba(239,68,68,0.15); border-radius:10px; padding:14px; margin-bottom:16px;'>" +
            "<p style='color:#fca5a5; font-size:13px; margin:0; text-align:center;'>" +
            "⏰ Mã OTP có hiệu lực trong <strong>2 phút</strong>" +
            "</p>" +
            "</div>" +
            "<p style='color:#64748b; font-size:12px; text-align:center; margin:0; line-height:1.6;'>" +
            "Nếu bạn không thực hiện yêu cầu này,<br>vui lòng bỏ qua email này." +
            "</p>" +
            "</div>" +

            // Footer
            "<div style='border-top:1px solid rgba(255,255,255,0.06); padding:20px 30px; text-align:center;'>" +
            "<p style='color:#475569; font-size:11px; margin:0;'>" +
            "Email được gửi đến: " + email + "<br>" +
            "© 2026 TGDD Rewards System" +
            "</p>" +
            "</div>" +

            "</div>" + // end card
            "</div>" + // end container
            "</body></html>";
    }
}
