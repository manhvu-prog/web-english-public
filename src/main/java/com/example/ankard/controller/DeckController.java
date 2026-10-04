package com.example.ankard.controller;

import com.example.ankard.config.DemoUserConfig;
import com.example.ankard.dto.DeckFormDTO;
import com.example.ankard.dto.FlashcardFormDTO;
import com.example.ankard.model.Deck;
import com.example.ankard.model.Flashcard;
import com.example.ankard.service.DeckService;
import com.example.ankard.service.FlashcardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller xử lý các thao tác liên quan đến Bộ thẻ (Deck) và Thẻ (Flashcard) của người dùng.
 * Các chức năng chính: Tạo deck, xem chi tiết, sửa, xóa, quản lý chia sẻ, và thêm/sửa/xóa thẻ.
 */
@Controller
@RequestMapping("/decks")
@RequiredArgsConstructor
public class DeckController {

    private final DeckService deckService;
    private final FlashcardService flashcardService;
    private final DemoUserConfig demoUser;

    // =====================================================
    // DECK: Tạo mới
    // =====================================================

    /**
     * Hiển thị form tạo bộ thẻ mới.
     * GET /decks/new
     */
    @GetMapping("/new")
    public String newDeckForm(Model model) {
        model.addAttribute("deckForm", new DeckFormDTO());
        return "deck/form"; // templates/deck/form.html
    }

    /**
     * Xử lý lưu bộ thẻ mới vào cơ sở dữ liệu.
     * POST /decks
     */
    @PostMapping
    public String createDeck(@Valid @ModelAttribute("deckForm") DeckFormDTO form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "deck/form";
        }
        Deck deck = deckService.createDeck(form, demoUser.getCurrentUserId());
        redirectAttributes.addFlashAttribute("success", "Tạo bộ thẻ thành công!");
        return "redirect:/decks/" + deck.getDeckId();
    }

    /**
     * Xem danh sách tất cả các thẻ trong một bộ thẻ cụ thể.
     * GET /decks/{deckId}
     */
    @GetMapping("/{deckId}")
    public String viewDeck(@PathVariable Integer deckId, Model model) {
        Deck deck = deckService.getDeckById(deckId);
        List<Flashcard> flashcards = flashcardService.getFlashcardsByDeck(deckId);

        model.addAttribute("deck", deck);
        model.addAttribute("flashcards", flashcards);
        model.addAttribute("flashcardForm", new FlashcardFormDTO()); // Form để thêm thẻ nhanh
        return "deck/detail"; // templates/deck/detail.html
    }

    // =====================================================
    // DECK: Sửa
    // =====================================================

    /**
     * Hiển thị form để chỉnh sửa thông tin bộ thẻ.
     * GET /decks/{deckId}/edit
     */
    @GetMapping("/{deckId}/edit")
    public String editDeckForm(@PathVariable Integer deckId, Model model) {
        Deck deck = deckService.getDeckById(deckId);
        try {
            // Kiểm tra trạng thái deck có cho phép sửa không (ví dụ: không cho sửa khi đang chờ duyệt)
            deckService.ensureDeckEditable(deckId);
        } catch (RuntimeException ex) {
            model.addAttribute("deck", deck);
            model.addAttribute("flashcards", flashcardService.getFlashcardsByDeck(deckId));
            model.addAttribute("flashcardForm", new FlashcardFormDTO());
            model.addAttribute("error", ex.getMessage());
            return "deck/detail";
        }
        DeckFormDTO form = new DeckFormDTO();
        form.setTitle(deck.getTitle());
        form.setDescription(deck.getDescription());

        model.addAttribute("deck", deck);
        model.addAttribute("deckForm", form);
        return "deck/form";
    }

    /**
     * Xử lý cập nhật thông tin bộ thẻ.
     * POST /decks/{deckId}/edit
     */
    @PostMapping("/{deckId}/edit")
    public String updateDeck(@PathVariable Integer deckId,
            @Valid @ModelAttribute("deckForm") DeckFormDTO form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("deck", deckService.getDeckById(deckId));
            return "deck/form";
        }
        try {
            deckService.updateDeck(deckId, form);
            redirectAttributes.addFlashAttribute("success", "Cập nhật bộ thẻ thành công!");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/decks/" + deckId;
    }

    /** Yêu cầu công khai/chia sẻ bộ thẻ lên Community. */
    @PostMapping("/{deckId}/submit-share")
    public String submitShare(@PathVariable Integer deckId, RedirectAttributes redirectAttributes) {
        try {
            deckService.submitDeckForReview(deckId, demoUser.getCurrentUserId());
            redirectAttributes.addFlashAttribute("success", "Đã gửi yêu cầu xét duyệt chia sẻ.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/decks/" + deckId;
    }

    @PostMapping("/{deckId}/cancel-share-review")
    public String cancelShareReview(@PathVariable Integer deckId, RedirectAttributes redirectAttributes) {
        try {
            deckService.cancelDeckReview(deckId, demoUser.getCurrentUserId());
            redirectAttributes.addFlashAttribute("success", "Đã hủy yêu cầu duyệt chia sẻ.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/decks/" + deckId;
    }

    /** Ngừng chia sẻ bộ thẻ đang công khai. */
    @PostMapping("/{deckId}/stop-share")
    public String stopShare(@PathVariable Integer deckId, RedirectAttributes redirectAttributes) {
        try {
            deckService.stopSharingDeck(deckId, demoUser.getCurrentUserId());
            redirectAttributes.addFlashAttribute("success", "Đã ngừng chia sẻ bộ thẻ.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/decks/" + deckId;
    }

    // =====================================================
    // DECK: Xóa
    // =====================================================

    /** Xử lý xóa toàn bộ bộ thẻ. */
    @PostMapping("/{deckId}/delete")
    public String deleteDeck(@PathVariable Integer deckId,
            RedirectAttributes redirectAttributes) {
        try {
            deckService.deleteDeck(deckId);
            redirectAttributes.addFlashAttribute("success", "Đã xóa bộ thẻ thành công.");
            return "redirect:/";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/decks/" + deckId;
        }
    }

    // =====================================================
    // FLASHCARD: Thêm vào deck
    // =====================================================

    /** Thêm một thẻ mới vào bộ thẻ. */
    @PostMapping("/{deckId}/flashcards")
    public String addFlashcard(@PathVariable Integer deckId,
            @Valid @ModelAttribute("flashcardForm") FlashcardFormDTO form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            Deck deck = deckService.getDeckById(deckId);
            List<Flashcard> flashcards = flashcardService.getFlashcardsByDeck(deckId);
            model.addAttribute("deck", deck);
            model.addAttribute("flashcards", flashcards);
            return "deck/detail";
        }
        try {
            flashcardService.createFlashcard(deckId, form);
            redirectAttributes.addFlashAttribute("success", "Thêm thẻ mới thành công!");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/decks/" + deckId;
    }

    /**
     * API POST /decks/{deckId}/flashcards/api-add — Thêm flashcard không reload trang
     */
    @PostMapping("/{deckId}/flashcards/api-add")
    @ResponseBody
    public ResponseEntity<?> apiAddFlashcard(@PathVariable Integer deckId,
            @Valid @RequestBody FlashcardFormDTO form) {
        try {
            Flashcard newCard = flashcardService.createFlashcard(deckId, form);
            return ResponseEntity.ok(newCard);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(500).body(ex.getMessage());
        }
    }

    // =====================================================
    // FLASHCARD: Sửa
    // =====================================================

    /** GET /decks/{deckId}/flashcards/{flashcardId}/edit */
    @GetMapping("/{deckId}/flashcards/{flashcardId}/edit")
    public String editFlashcardForm(@PathVariable Integer deckId,
            @PathVariable Integer flashcardId,
            Model model) {
        deckService.ensureDeckEditable(deckId);
        Flashcard card = flashcardService.getFlashcardById(flashcardId);
        FlashcardFormDTO form = new FlashcardFormDTO();
        form.setFrontContent(card.getFrontContent());
        form.setBackContent(card.getBackContent());
        form.setExampleSentence(card.getExampleSentence());
        form.setPronunciation(card.getPronunciation());
        form.setImageUrl(card.getImageUrl());
        form.setAudioUrl(card.getAudioUrl());

        model.addAttribute("deckId", deckId);
        model.addAttribute("flashcardId", flashcardId);
        model.addAttribute("flashcardForm", form);
        return "deck/flashcard-form"; // templates/deck/flashcard-form.html
    }

    /** Xử lý cập nhật nội dung thẻ. */
    @PostMapping("/{deckId}/flashcards/{flashcardId}/edit")
    public String updateFlashcard(@PathVariable Integer deckId,
            @PathVariable Integer flashcardId,
            @Valid @ModelAttribute("flashcardForm") FlashcardFormDTO form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("deckId", deckId);
            model.addAttribute("flashcardId", flashcardId);
            return "deck/flashcard-form";
        }
        try {
            flashcardService.updateFlashcard(flashcardId, form);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thẻ thành công!");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/decks/" + deckId;
    }

    // =====================================================
    // FLASHCARD: Xóa
    // =====================================================

    /** Xử lý xóa thẻ ra khỏi bộ thẻ. */
    @PostMapping("/{deckId}/flashcards/{flashcardId}/delete")
    public String deleteFlashcard(@PathVariable Integer deckId,
            @PathVariable Integer flashcardId,
            RedirectAttributes redirectAttributes) {
        try {
            flashcardService.deleteFlashcard(flashcardId);
            redirectAttributes.addFlashAttribute("success", "Đã xóa thẻ.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/decks/" + deckId;
    }

    // =====================================================
    // THÊM MỚI: FLASHCARD - Nhập hàng loạt bằng FILE CSV/TXT
    // =====================================================

    /**
     * Nhập thẻ hàng loạt từ tệp CSV hoặc TXT.
     */
    @PostMapping("/{deckId}/flashcards/upload-csv")
    @ResponseBody
    public ResponseEntity<String> uploadCSV(@PathVariable Integer deckId, @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File trống, vui lòng kiểm tra lại!");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int successCount = 0;

            // Đọc từng dòng của file cho đến khi hết
            while ((line = reader.readLine()) != null) {
                // Bỏ qua dòng trống
                if (line.trim().isEmpty()) continue;

                // Tách cột bằng dấu phẩy
                String[] tokens = line.split(",");

                // Bỏ qua dòng tiêu đề nếu người dùng có ghi
                if (tokens[0].trim().equalsIgnoreCase("mặt trước") || tokens[0].trim().equalsIgnoreCase("front")) {
                    continue;
                }

                // Cần ít nhất 2 cột: Mặt trước và Mặt sau
                if (tokens.length >= 2) {
                    FlashcardFormDTO formDTO = new FlashcardFormDTO();
                    formDTO.setFrontContent(tokens[0].trim());
                    formDTO.setBackContent(tokens[1].trim());

                    // Các trường phụ
                    if (tokens.length >= 3) formDTO.setPronunciation(tokens[2].trim());
                    if (tokens.length >= 4) formDTO.setExampleSentence(tokens[3].trim());

                    // Gọi hàm tạo thẻ để lưu vào database
                    flashcardService.createFlashcard(deckId, formDTO);
                    successCount++;
                }
            }

            return ResponseEntity.ok("Đã nạp thành công " + successCount + " thẻ từ tệp dữ liệu!");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi hệ thống khi đọc cấu trúc tệp: " + e.getMessage());
        }
    }
}