package com.example.smartlearning.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // Dùng @Controller (không phải Rest) để trả về HTML
public class PageController {

    /**
     * Hiển thị trang login
     * Khi user truy cập /login
     * @return Tên file HTML (login.html)
     */
    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // Trả về "login.html" trong thư mục templates
    }

    /**
     * Hiển thị trang register
     * Khi user truy cập /register
     */
    @GetMapping("/register")
    public String showRegisterPage() {
        return "register"; // Trả về "register.html"
    }

    /**
     * Hiển thị trang dashboard (trang chính)
     * Khi user truy cập /dashboard
     */
    @GetMapping("/dashboard")
    public String showDashboardPage() {
        return "dashboard"; // Chúng ta sẽ tạo file dashboard.html ở bước sau
    }

    /**
     * Chuyển hướng trang chủ ("/") về trang login
     */
    @GetMapping("/")
    public String redirectToLogin() {
        return "redirect:/login";
    }
}