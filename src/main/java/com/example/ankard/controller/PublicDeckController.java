package com.example.ankard.controller;

import com.example.ankard.config.DemoUserConfig;
import com.example.ankard.controller.AuthController;
import com.example.ankard.dto.DeckSummaryDTO;
import com.example.ankard.model.Deck;
import com.example.ankard.model.DeckStatus;
import com.example.ankard.model.Flashcard;
import com.example.ankard.service.DeckService;
import com.example.ankard.service.FlashcardService;
import com.example.ankard.service.RatingService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

/**
 * Controller điều hướng các yêu cầu liên quan đến trang Cộng đồng (Public Decks).
 * Phụ trách hiển thị danh sách, xem chi tiết, đánh giá và import bộ thẻ.
 */
/**
 * Controller xử lý các tính năng Cộng đồng: Xem, tìm kiếm, đánh giá và sao chép bộ thẻ công khai.
 */
@Controller
@RequestMapping("/public-decks")
@RequiredArgsConstructor
public class PublicDeckController {

    private static final Logger log = LoggerFactory.getLogger(PublicDeckController.class);
    private final DeckService deckService;
    private final FlashcardService flashcardService;
    private final DemoUserConfig demoUser;
    private final RatingService ratingService;

    /** Hiển thị trang Cộng đồng với danh sách các bộ thẻ công khai và tìm kiếm. */
    @GetMapping
    public String listPublicDecks(@RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "sort", required = false, defaultValue = "views") String sort,
            HttpSession session,
            Model model) {
        Integer userId = demoUser.getCurrentUserId();
        List<DeckSummaryDTO> publicDecks;

        if (query != null && !query.trim().isEmpty()) {
            // Xử lý tìm kiếm
            String trimmed = query.trim();
            log.info("PublicDeck search keyword='{}'", trimmed);
            publicDecks = deckService.searchPublicDecks(userId, trimmed);
            model.addAttribute("query", trimmed);
        } else {
            // Lấy toàn bộ danh sách mặc định
            log.info("PublicDeck list all public items, sort={}", sort);
            publicDecks = deckService.getPublicDecks(userId, sort);
        }

        log.info("Found {} public decks", publicDecks.size());
        String role = (String) session.getAttribute(AuthController.SESSION_ROLE);
        model.addAttribute("role", role);
        model.addAttribute("publicDecks", publicDecks);
        model.addAttribute("publicDeckCount", publicDecks.size());
        model.addAttribute("sort", sort);

        // Hiển thị TOP 3 xu hướng (Trending) nếu người dùng không tìm kiếm
        if (query == null || query.trim().isEmpty()) {
            List<DeckSummaryDTO> allByViews = deckService.getPublicDecks(userId, "views");
            model.addAttribute("trendingDecks", allByViews.stream().limit(3).toList());
        }
        return "public-deck/list";
    }

    /** Xem thông tin chi tiết một bộ thẻ công khai. */
    @GetMapping("/{deckId}")
    public String viewPublicDeck(@PathVariable Integer deckId, HttpSession session,
                                 Model model, RedirectAttributes redirectAttributes) {
        Deck deck = deckService.getPublicDeckById(deckId);

        // Kiểm tra quyền truy cập VIP
        if (deck.getStatus() == DeckStatus.VIP_ONLY) {
            Boolean isVip = (Boolean) session.getAttribute(AuthController.SESSION_IS_VIP);
            if (!Boolean.TRUE.equals(isVip)) {
                redirectAttributes.addFlashAttribute("vipDeckTitle", deck.getTitle());
                redirectAttributes.addFlashAttribute("info",
                        "👑 Deck này chỉ dành cho thành viên VIP. Hãy nâng cấp để truy cập!");
                return "redirect:/vip";
            }
        }

        // Tăng lượt xem bộ thẻ
        deckService.incrementViewCount(deckId);
        List<Flashcard> flashcards = flashcardService.getFlashcardsByDeck(deckId);

        // Lấy thông tin đánh giá (bình luận, sao trung bình,...)
        Integer currentUserId = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        Map<String, Object> ratingInfo = ratingService.getDeckRatingInfo(deckId, currentUserId);

        model.addAttribute("deck", deck);
        model.addAttribute("flashcards", flashcards);
        model.addAllAttributes(ratingInfo);
        return "public-deck/detail";
    }

    /** Nhận và lưu đánh giá của người dùng cho bộ thẻ. */
    @PostMapping("/{deckId}/rate")
    public String rateDeck(@PathVariable Integer deckId,
                           @RequestParam int stars,
                           @RequestParam(required = false) String comment,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        Integer userId = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn cần đăng nhập để đánh giá.");
            return "redirect:/login";
        }
        try {
            ratingService.saveRating(deckId, userId, stars, comment);
            redirectAttributes.addFlashAttribute("success", "⭐ Đánh giá của bạn đã được lưu!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/public-decks/" + deckId + "#reviews";
    }

    /** Xóa đánh giá (chỉ chủ sở hữu hoặc Admin). */
    @PostMapping("/{deckId}/delete-rating")
    public String deleteRating(@PathVariable Integer deckId,
                               @RequestParam Integer ratingId,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Integer userId = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn cần đăng nhập.");
            return "redirect:/login";
        }
        String role = (String) session.getAttribute(AuthController.SESSION_ROLE);
        boolean isAdmin = role != null && ("admin".equalsIgnoreCase(role) || "super_admin".equalsIgnoreCase(role));
        try {
            ratingService.deleteRating(ratingId, userId, isAdmin);
            redirectAttributes.addFlashAttribute("success", "Đã xóa đánh giá.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/public-decks/" + deckId + "#reviews";
    }

    /** Sao chép bộ thẻ công khai vào kho thẻ cá nhân. */
    @PostMapping("/{deckId}/import")
    public String importPublicDeck(@PathVariable Integer deckId, HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        // Kiểm tra đã đăng nhập chưa
        Integer userId = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/login?redirect=/public-decks/" + deckId;
        }

        // Kiểm tra quyền VIP trước khi cho phép import
        Deck deck = deckService.getDeckById(deckId);
        if (deck.getStatus() == DeckStatus.VIP_ONLY) {
            Boolean isVip = (Boolean) session.getAttribute(AuthController.SESSION_IS_VIP);
            if (!Boolean.TRUE.equals(isVip)) {
                redirectAttributes.addFlashAttribute("info",
                        "👑 Deck này chỉ dành cho thành viên VIP.");
                return "redirect:/vip";
            }
        }
        
        // Thực hiện nhân bản deck
        Deck imported = deckService.importPublicDeck(deckId, userId);
        redirectAttributes.addFlashAttribute("success",
                "Đã import deck thành công! Bạn có thể chỉnh sửa deck mới của mình.");
        return "redirect:/decks/" + imported.getDeckId();
    }

    /** Ẩn bộ thẻ khỏi cộng đồng (chủ yếu dùng cho Admin). */
    @PostMapping("/{deckId}/hide")
    public String hidePublicDeck(@PathVariable Integer deckId, HttpSession session,
            RedirectAttributes redirectAttributes) {
        String role = (String) session.getAttribute(AuthController.SESSION_ROLE);
        if (!("admin".equalsIgnoreCase(role) || "super_admin".equalsIgnoreCase(role))) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền thực hiện hành động này.");
            return "redirect:/public-decks";
        }
        deckService.hidePublicDeck(deckId);
        redirectAttributes.addFlashAttribute("success", "Đã ẩn deck khỏi danh sách công khai.");
        return "redirect:/public-decks";
    }
}
