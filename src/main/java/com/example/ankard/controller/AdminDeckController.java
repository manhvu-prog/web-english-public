package com.example.ankard.controller;

import com.example.ankard.model.Deck;
import com.example.ankard.model.Flashcard;
import com.example.ankard.service.DeckService;
import com.example.ankard.service.FlashcardService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller dành cho quản trị viên (Admin) để quản lý các bộ thẻ.
 * Chức năng chính: Xem danh sách chờ duyệt, phê duyệt hoặc từ chối chia sẻ bộ thẻ.
 */
@Controller
@RequestMapping("/admin/decks")
@RequiredArgsConstructor
public class AdminDeckController {

    private final DeckService deckService;
    private final FlashcardService flashcardService;

    /** Xem chi tiết một bộ thẻ bất kỳ (dành cho Admin). */
    @GetMapping("/{deckId}/view")
    public String viewDeck(@PathVariable Integer deckId, HttpSession session,
                           Model model, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập trang quản trị.");
            return "redirect:/";
        }
        Deck deck = deckService.getDeckById(deckId);
        List<Flashcard> flashcards = flashcardService.getFlashcardsByDeck(deckId);
        model.addAttribute("deck", deck);
        model.addAttribute("flashcards", flashcards);
        return "admin/deck-view";
    }

    /** Lấy danh sách các bộ thẻ đang chờ được phê duyệt. */
    @GetMapping("/pending")
    public String pendingDecks(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập trang quản trị.");
            return "redirect:/";
        }
        List<Deck> pendingDecks = deckService.getPendingDecks();
        model.addAttribute("pendingDecks", pendingDecks);
        return "admin/deck-pending";
    }

    /** Phê duyệt một bộ thẻ để công khai lên cộng đồng. */
    @PostMapping("/{deckId}/approve")
    public String approve(@PathVariable Integer deckId, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền thực hiện hành động này.");
            return "redirect:/";
        }
        deckService.approveDeck(deckId);
        redirectAttributes.addFlashAttribute("success", "Đã phê duyệt bộ thẻ thành công.");
        return "redirect:/admin/decks/pending";
    }

    /** Từ chối phê duyệt chia sẻ bộ thẻ. */
    @PostMapping("/{deckId}/reject")
    public String reject(@PathVariable Integer deckId, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền thực hiện hành động này.");
            return "redirect:/";
        }
        deckService.rejectDeck(deckId);
        redirectAttributes.addFlashAttribute("success", "Đã từ chối chia sẻ bộ thẻ.");
        return "redirect:/admin/decks/pending";
    }

    private boolean isAdmin(HttpSession session) {
        String role = (String) session.getAttribute(AuthController.SESSION_ROLE);
        return role != null && ("admin".equalsIgnoreCase(role) || "super_admin".equalsIgnoreCase(role));
    }

    // ========== VIP DECK MANAGEMENT ==========

    @GetMapping("/vip")
    public String vipDecks(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập trang admin.");
            return "redirect:/";
        }
        List<Deck> vipDecks = deckService.getVipDecks();
        List<Deck> eligibleDecks = deckService.getAllPublicAndVipDecks();
        model.addAttribute("vipDecks", vipDecks);
        model.addAttribute("eligibleDecks", eligibleDecks);
        return "admin/deck-vip";
    }

    @PostMapping("/{deckId}/mark-vip")
    public String markVip(@PathVariable Integer deckId, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền thực hiện hành động này.");
            return "redirect:/";
        }
        try {
            deckService.markDeckAsVip(deckId);
            redirectAttributes.addFlashAttribute("success", "👑 Đã đánh dấu deck thành VIP Only.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/decks/vip";
    }

    @PostMapping("/{deckId}/unmark-vip")
    public String unmarkVip(@PathVariable Integer deckId, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền thực hiện hành động này.");
            return "redirect:/";
        }
        try {
            deckService.unmarkDeckVip(deckId);
            redirectAttributes.addFlashAttribute("success", "Đã gỡ VIP khỏi deck, deck quay lại trạng thái công khai bình thường.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/decks/vip";
    }

    @PostMapping("/{deckId}/approve-as-vip")
    public String approveAsVip(@PathVariable Integer deckId, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền thực hiện hành động này.");
            return "redirect:/";
        }
        try {
            deckService.approveDeck(deckId);
            deckService.markDeckAsVip(deckId);
            redirectAttributes.addFlashAttribute("success", "👑 Đã duyệt và đánh dấu deck là VIP Only.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/decks/pending";
    }
}
