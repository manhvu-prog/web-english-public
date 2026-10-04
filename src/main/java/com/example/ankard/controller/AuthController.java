package com.example.ankard.controller;

import com.example.ankard.dto.AccountFormDTO;
import com.example.ankard.dto.LoginFormDTO;
import com.example.ankard.dto.SignupFormDTO;
import com.example.ankard.model.User;
import com.example.ankard.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller xử lý các luồng xác thực: Đăng nhập, Đăng ký, Đăng xuất và quản lý Tài khoản.
 */
@Controller
@RequiredArgsConstructor
public class AuthController {

    public static final String SESSION_USER_ID = "userId";
    public static final String SESSION_USERNAME = "username";
    public static final String SESSION_ROLE = "role";
    public static final String SESSION_IS_VIP = "isVip";

    private final AuthService authService;

    /** Hiển thị form đăng nhập. */
    @GetMapping("/login")
    public String loginForm(@RequestParam(name = "redirect", required = false) String redirect,
            HttpSession session, Model model) {
        if (redirect != null && !redirect.isBlank()) {
            session.setAttribute("loginRedirectUrl", redirect);
        }
        model.addAttribute("loginForm", new LoginFormDTO());
        return "auth/login";
    }

    /** Xử lý yêu cầu đăng nhập. */
    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginForm") LoginFormDTO form,
            BindingResult bindingResult,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/login";
        }
        try {
            User user = authService.authenticate(form.getUsername(), form.getPassword());
            session.setAttribute(SESSION_USER_ID, user.getUserId());
            session.setAttribute(SESSION_USERNAME, user.getUsername());
            session.setAttribute(SESSION_ROLE, user.getRole() != null ? user.getRole().name() : null);
            session.setAttribute(SESSION_IS_VIP, user.isVip());
            redirectAttributes.addFlashAttribute("success", "Đăng nhập thành công!");
            
            String redirectUrl = (String) session.getAttribute("loginRedirectUrl");
            session.removeAttribute("loginRedirectUrl");
            if (redirectUrl != null && !redirectUrl.isBlank()) {
                return "redirect:" + redirectUrl;
            }
            return "redirect:/";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/login";
        }
    }

    /** Hiển thị form đăng ký tài khoản. */
    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("signupForm", new SignupFormDTO());
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute("signupForm") SignupFormDTO form,
            BindingResult bindingResult,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/signup";
        }
        try {
            User user = authService.signup(form);
            session.setAttribute(SESSION_USER_ID, user.getUserId());
            session.setAttribute(SESSION_USERNAME, user.getUsername());
            redirectAttributes.addFlashAttribute("success", "Tạo tài khoản thành công!");
            return "redirect:/";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/signup";
        }
    }

    /** Trang thông tin tài khoản cá nhân. */
    @GetMapping("/account")
    public String accountPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Integer userId = (Integer) session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn cần đăng nhập để truy cập trang tài khoản.");
            return "redirect:/login";
        }
        User user = authService.getUserById(userId);
        model.addAttribute("user", user);
        model.addAttribute("accountForm", new AccountFormDTO(user.getEmail(), "", "", ""));
        return "auth/account";
    }

    @PostMapping("/account/update-email")
    public String updateEmail(@ModelAttribute("accountForm") AccountFormDTO accountForm,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Integer userId = (Integer) session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn cần đăng nhập để thực hiện hành động này.");
            return "redirect:/login";
        }
        try {
            authService.updateEmail(userId, accountForm.getCurrentPassword(), accountForm.getEmail());
            redirectAttributes.addFlashAttribute("success", "Email đã được cập nhật.");
            return "redirect:/account";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/account";
        }
    }

    @PostMapping("/account/change-password")
    public String changePassword(@ModelAttribute("accountForm") AccountFormDTO accountForm,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Integer userId = (Integer) session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn cần đăng nhập để thực hiện hành động này.");
            return "redirect:/login";
        }
        try {
            authService.updatePassword(userId, accountForm.getCurrentPassword(), accountForm.getNewPassword(),
                    accountForm.getConfirmNewPassword());
            redirectAttributes.addFlashAttribute("success", "Mật khẩu đã được cập nhật.");
            return "redirect:/account";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/account";
        }
    }

    @PostMapping("/account/delete")
    public String deleteAccount(HttpSession session, RedirectAttributes redirectAttributes) {
        Integer userId = (Integer) session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn cần đăng nhập để thực hiện hành động này.");
            return "redirect:/login";
        }
        authService.deleteAccount(userId);
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "Tài khoản đã được xóa.");
        return "redirect:/signup";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "Đã đăng xuất.");
        return "redirect:/login";
    }

    // ========== VIP ==========

    @GetMapping("/vip")
    public String vipPage(HttpSession session, Model model) {
        Boolean isVip = (Boolean) session.getAttribute(SESSION_IS_VIP);
        model.addAttribute("isVip", Boolean.TRUE.equals(isVip));
        Integer userId = (Integer) session.getAttribute(SESSION_USER_ID);
        if (userId != null) {
            User user = authService.getUserById(userId);
            model.addAttribute("vipExpiresAt", user.getVipExpiresAt());
        }
        return "vip/register";
    }

    @PostMapping("/vip/upgrade")
    public String upgradeVip(HttpSession session, RedirectAttributes redirectAttributes) {
        Integer userId = (Integer) session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn cần đăng nhập để nâng cấp VIP.");
            return "redirect:/login";
        }
        User user = authService.upgradeToVip(userId);
        session.setAttribute(SESSION_ROLE, user.getRole().name());
        session.setAttribute(SESSION_IS_VIP, user.isVip());
        redirectAttributes.addFlashAttribute("success",
                "👑 Chúc mừng! Bạn đã trở thành thành viên VIP trong 30 ngày!");
        return "redirect:/vip";
    }
}
