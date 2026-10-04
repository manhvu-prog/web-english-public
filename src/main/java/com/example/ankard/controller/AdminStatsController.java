package com.example.ankard.controller;

import com.example.ankard.service.StatsService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

/**
 * Controller dành cho quản trị viên để xem số liệu thống kê toàn hệ thống.
 */
@Controller
@RequestMapping("/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final StatsService statsService;

    /** Hiển thị trang dashboard thống kê cho Admin. */
    @GetMapping
    public String stats(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String role = (String) session.getAttribute(AuthController.SESSION_ROLE);
        if (role == null || (!role.equalsIgnoreCase("admin") && !role.equalsIgnoreCase("super_admin"))) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập trang này.");
            return "redirect:/";
        }

        Map<String, Object> stats = statsService.getAdminStats();
        model.addAllAttributes(stats);
        return "admin/stats";
    }
}
