package com.example.ankard.controller;

import com.example.ankard.config.DemoUserConfig;
import com.example.ankard.dto.DeckSummaryDTO;
import com.example.ankard.service.DeckService;
import com.example.ankard.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

/**
 * Controller xử lý trang chủ (Dashboard) cho người dùng sau khi đăng nhập.
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final DeckService deckService;
    private final DemoUserConfig demoUser;
    private final StatsService statsService;

    /** Hiển thị trang chủ (Dashboard) của người dùng với danh sách bộ thẻ và thống kê. */
    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        Object sessionUserId = session != null ? session.getAttribute(AuthController.SESSION_USER_ID) : null;
        if (!(sessionUserId instanceof Integer)) {
            return "redirect:/login";
        }

        Integer userId = (Integer) sessionUserId;
        List<DeckSummaryDTO> decks = deckService.getDeckSummariesForUser(userId);

        // Thống kê cá nhân
        Map<String, Object> userStats = statsService.getUserStats(userId);

        model.addAttribute("decks", decks);
        model.addAttribute("userId", userId);
        model.addAttribute("username", session.getAttribute(AuthController.SESSION_USERNAME));
        model.addAllAttributes(userStats);
        return "index";
    }
}

