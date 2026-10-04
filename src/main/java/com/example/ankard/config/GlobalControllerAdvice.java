package com.example.ankard.config;

import com.example.ankard.controller.AuthController;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Lớp cấu hình chung áp dụng cho tất cả các Controller trong hệ thống.
 * Nhiệm vụ chính là đảm bảo thông tin người dùng trong session luôn khả dụng trong Model để hiển thị trên giao diện (Header, Sidebar).
 */
@ControllerAdvice
public class GlobalControllerAdvice {

    /**
     * Tự động thêm thông tin người dùng (username, role, status VIP) vào Model 
     * trước khi render bất kỳ trang HTML nào.
     */
    @ModelAttribute
    public void addUserInfoToModel(Model model, HttpSession session) {
        if (session != null) {
            Object username = session.getAttribute(AuthController.SESSION_USERNAME);
            Object role = session.getAttribute(AuthController.SESSION_ROLE);
            Object isVip = session.getAttribute(AuthController.SESSION_IS_VIP);
            
            model.addAttribute("username", username);
            model.addAttribute("role", role);
            model.addAttribute("isVip", Boolean.TRUE.equals(isVip));
        } else {
            // Trường hợp chưa đăng nhập, gán giá trị null/false
            model.addAttribute("username", null);
            model.addAttribute("role", null);
            model.addAttribute("isVip", false);
        }
    }
}
