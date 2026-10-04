package com.example.ankard.controller;

import com.example.ankard.model.User;
import com.example.ankard.service.AuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller dành cho quản trị viên để quản lý danh sách người dùng.
 * Bao gồm: Tra cứu user, phân quyền (Role) và xóa tài khoản.
 */
@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AuthService authService;

    /** Hiển thị danh sách người dùng, hỗ trợ tìm kiếm theo tên. */
    @GetMapping
    public String listUsers(@RequestParam(name = "q", required = false) String query,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (!isAdminOrSuperAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập trang quản lý tài khoản.");
            return "redirect:/";
        }

        List<User> users = authService.findUsers(query);
        model.addAttribute("users", users);
        model.addAttribute("query", query);
        return "admin/users";
    }

    /** Thay đổi vai trò của một người dùng cụ thể. */
    @PostMapping("/{userId}/role")
    public String changeRole(@PathVariable Integer userId,
            @RequestParam("role") String role,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (!isAdminOrSuperAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền thực hiện hành động này.");
            return "redirect:/";
        }
        User targetUser = authService.getUserById(userId);
        if (!isSuperAdmin(session)) {
            if (!"user".equalsIgnoreCase(role)) {
                redirectAttributes.addFlashAttribute("error", "Bạn không đủ quyền để cấp admin hoặc super_admin.");
                return "redirect:/admin/users";
            }
            if (!"user".equalsIgnoreCase(targetUser.getRole().name())) {
                redirectAttributes.addFlashAttribute("error",
                        "Bạn không đủ quyền để thay đổi vai trò của admin hoặc super_admin.");
                return "redirect:/admin/users";
            }
        }
        try {
            authService.updateRole(userId, role);
            redirectAttributes.addFlashAttribute("success", "Vai trò đã được cập nhật thành công.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{userId}/delete")
    public String deleteUser(@PathVariable Integer userId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (!isAdminOrSuperAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền thực hiện hành động này.");
            return "redirect:/";
        }
        User targetUser = authService.getUserById(userId);
        if (!isSuperAdmin(session) && !"user".equalsIgnoreCase(targetUser.getRole().name())) {
            redirectAttributes.addFlashAttribute("error", "Bạn không đủ quyền để xóa admin hoặc super_admin.");
            return "redirect:/admin/users";
        }
        try {
            authService.deleteUser(userId);
            redirectAttributes.addFlashAttribute("success", "Tài khoản đã được xóa.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    private boolean isAdminOrSuperAdmin(HttpSession session) {
        String role = (String) session.getAttribute(AuthController.SESSION_ROLE);
        return role != null && ("admin".equalsIgnoreCase(role) || "super_admin".equalsIgnoreCase(role));
    }

    private boolean isSuperAdmin(HttpSession session) {
        String role = (String) session.getAttribute(AuthController.SESSION_ROLE);
        return role != null && "super_admin".equalsIgnoreCase(role);
    }
}
