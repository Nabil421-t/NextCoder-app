package com.cuet.dsa.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private static final String BRAND_NAME = "HelloBye";
    private static final String ACCENT_COLOR = "#4f46e5"; // Indigo 600

    // ─── Verification Email ──────────────────────────────────────────
    @Async
    public void sendVerificationEmail(String to, String name, String token) {
        String link = frontendUrl + "/verify-email?token=" + token;
        send(to,
                BRAND_NAME + " | Verify Your Email",
                buildEmail(name,
                        "Verify Your Email Address",
                        "Welcome to the family! We're excited to have you join HelloBye. Click the button below to verify your email and start exploring the latest in smart gadgets.",
                        link,
                        "Verify Email",
                        "This link expires in 24 hours. If you didn't create an account with us, please ignore this email.")
        );
    }

    // ─── Welcome Email ───────────────────────────────────────────────
    @Async
    public void sendWelcomeEmail(String to, String name) {
        send(to,
                "Welcome to " + BRAND_NAME + "!",
                buildEmail(name,
                        "Account Activated",
                        "Success! Your email has been verified. Your HelloBye account is now fully active. You can now track orders, save favorites, and checkout faster.",
                        frontendUrl + "/login",
                        "Start Shopping",
                        "Need help? Our support team is always here for you.")
        );
    }

    // ─── Forgot Password Email ───────────────────────────────────────
    @Async
    public void sendPasswordResetEmail(String to, String name, String token) {
        String link = frontendUrl + "/reset-password?token=" + token;
        send(to,
                BRAND_NAME + " | Reset Your Password",
                buildEmail(name,
                        "Password Reset Request",
                        "We received a request to reset your password for your HelloBye account. No worries, it happens! Click the button below to set a new one.",
                        link,
                        "Reset Password",
                        "This link expires in 1 hour. If you didn't request this, you can safely ignore this email.")
        );
    }

    // ─── Password Changed Notification ──────────────────────────────
    @Async
    public void sendPasswordChangedEmail(String to, String name) {
        send(to,
                BRAND_NAME + " | Security Update",
                buildEmail(name,
                        "Password Changed",
                        "This is a confirmation that your password was recently changed. If you did not make this change, please contact our security team immediately.",
                        frontendUrl + "/forgot-password",
                        "Secure Account",
                        "For your protection, we've logged you out of all other devices.")
        );
    }

    // ─── Order Confirmation (Example) ────────────────────────────────
    @Async
    public void sendOrderConfirmationEmail(String to, String name, String orderId) {
        send(to,
                BRAND_NAME + " | Order Confirmed #" + orderId,
                buildEmail(name,
                        "Your Order is Confirmed!",
                        "Great news! Your order <strong>#" + orderId + "</strong> has been received and is being prepared for shipment. We'll notify you once it's on its way.",
                        frontendUrl + "/account/orders",
                        "Track Order",
                        "Thank you for choosing HelloBye for your tech needs!")
        );
    }

    // ─── Private Helpers ─────────────────────────────────────────────
    private void send(String to, String subject, String html) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper h = new MimeMessageHelper(msg, true, "UTF-8");
            h.setFrom(fromEmail, BRAND_NAME);
            h.setTo(to);
            h.setSubject(subject);
            h.setText(html, true);
            mailSender.send(msg);
            log.info("Email sent to [{}] subject: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to [{}]: {}", to, e.getMessage());
        }
    }

    private String buildEmail(String name, String title, String body,
                              String ctaUrl, String ctaLabel, String note) {
        return "<!DOCTYPE html><html>"
                + "<head><meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1'>"
                + "<style>"
                + "  @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap');"
                + "  body { margin:0; padding:0; background-color:#f8fafc; font-family:'Inter',Arial,sans-serif; -webkit-font-smoothing:antialiased; }"
                + "  .wrapper { width:100%; table-layout:fixed; background-color:#f8fafc; padding-bottom:40px; }"
                + "  .main { background-color:#ffffff; margin:0 auto; width:100%; max-width:600px; border-spacing:0; color:#1e293b; border-radius:16px; overflow:hidden; margin-top:40px; box-shadow:0 4px 6px -1px rgba(0,0,0,0.1); }"
                + "  .header { background-color:#ffffff; padding:32px; text-align:center; border-bottom:1px solid #f1f5f9; }"
                + "  .content { padding:40px 32px; text-align:left; }"
                + "  .button { background-color:" + ACCENT_COLOR + "; color:#ffffff !important; padding:14px 32px; text-decoration:none; border-radius:12px; font-weight:700; font-size:14px; display:inline-block; margin:24px 0; }"
                + "  .footer { padding:32px; text-align:center; color:#64748b; font-size:12px; }"
                + "</style></head>"
                + "<body>"
                + "<div class='wrapper'>"
                + "  <table class='main'>"
                + "    <tr><td class='header'>"
                + "      <div style='font-size:24px; font-weight:800; color:#0f172a; letter-spacing:-0.5px;'>" + BRAND_NAME + "<span style='color:" + ACCENT_COLOR + ";'>.</span></div>"
                + "    </td></tr>"
                + "    <tr><td class='content'>"
                + "      <p style='font-size:16px; margin-bottom:8px;'>Hello " + name + ",</p>"
                + "      <h1 style='font-size:24px; font-weight:700; color:#0f172a; margin-top:0; margin-bottom:16px;'>" + title + "</h1>"
                + "      <p style='font-size:15px; line-height:1.6; color:#475569;'>" + body + "</p>"
                + "      <div style='text-align:center;'><a href='" + ctaUrl + "' class='button'>" + ctaLabel + "</a></div>"
                + "      <p style='font-size:13px; color:#94a3b8; line-height:1.5; margin-top:24px;'>" + note + "</p>"
                + "    </td></tr>"
                + "    <tr><td class='footer'>"
                + "      <p style='margin-bottom:8px;'>&copy; 2026 " + BRAND_NAME + " - Smart Gadget Shop. All rights reserved.</p>"
                + "      <p>Chattogram, Bangladesh</p>"
                + "    </td></tr>"
                + "  </table>"
                + "</div>"
                + "</body></html>";
    }
}