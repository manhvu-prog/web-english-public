package com.example.ankard.controller;

import com.example.ankard.config.DemoUserConfig;
import com.example.ankard.dto.StudySessionDTO;
import com.example.ankard.model.ReviewHistory;
import com.example.ankard.service.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller điều phối quy trình học tập (Study Session).
 * Xử lý việc hiển thị thẻ, lật thẻ và ghi nhận kết quả ôn tập từ người dùng.
 */
@Controller
@RequestMapping("/study")
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;
    private final DemoUserConfig demoUser;

    /**
     * Bắt đầu hoặc tiếp tục một phiên học.
     * Lấy thẻ tiếp theo cần học và hiển thị mặt trước.
     * GET /study/{deckId}
     */
    @GetMapping("/{deckId}")
    public String studyDeck(@PathVariable Integer deckId, Model model) {
        Integer userId = demoUser.getCurrentUserId();
        StudySessionDTO session = studyService.getNextCard(deckId, userId);

        if (session == null) {
            // Trường hợp không còn thẻ nào đến hạn ôn tập
            model.addAttribute("deckId", deckId);
            return "study/complete"; // templates/study/complete.html
        }

        // Lấy nhãn dự kiến cho các nút (chỉ dùng khi show answer)
        String[] labels = studyService.getNextReviewLabels(session.getFlashcardId(), userId);

        model.addAttribute("studySession", session);
        model.addAttribute("showAnswer", false);
        model.addAttribute("labels", labels); // [again, hard, good, easy]
        return "study/study"; // templates/study/study.html
    }

    /**
     * Xử lý hành động lật thẻ để xem đáp án.
     * Hiển thị mặt sau của thẻ và các nút đánh giá mức độ ghi nhớ.
     * POST /study/{deckId}/show-answer
     */
    @PostMapping("/{deckId}/show-answer")
    public String showAnswer(@PathVariable Integer deckId,
            @RequestParam Integer flashcardId,
            Model model) {
        Integer userId = demoUser.getCurrentUserId();
        StudySessionDTO session = studyService.getCardDetail(deckId, flashcardId, userId);

        String[] labels = studyService.getNextReviewLabels(flashcardId, userId);

        model.addAttribute("studySession", session);
        model.addAttribute("showAnswer", true);
        model.addAttribute("labels", labels);
        return "study/study";
    }

    /**
     * Ghi nhận kết quả phản hồi của người dùng cho thẻ vừa học.
     * Sau đó tự động chuyển hướng để lấy thẻ tiếp theo.
     * POST /study/{deckId}/review
     */
    @PostMapping("/{deckId}/review")
    public String submitReview(@PathVariable Integer deckId,
            @RequestParam Integer flashcardId,
            @RequestParam String result,
            RedirectAttributes redirectAttributes) {
        Integer userId = demoUser.getCurrentUserId();

        try {
            ReviewHistory.ReviewResult reviewResult = ReviewHistory.ReviewResult.valueOf(result.toLowerCase());
            studyService.submitReview(flashcardId, userId, reviewResult);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Kết quả không hợp lệ: " + result);
        }

        // Redirect về trang học để lấy card tiếp theo (PRG pattern)
        return "redirect:/study/" + deckId;
    }

    /**
     * Đặt lại toàn bộ tiến trình học tập của bộ thẻ để học lại từ đầu.
     * POST /study/{deckId}/reset-all
     */
    @PostMapping("/{deckId}/reset-all")
    public String resetAll(@PathVariable Integer deckId, RedirectAttributes redirectAttributes) {
        Integer userId = demoUser.getCurrentUserId();
        try {
            studyService.resetDeckForReview(deckId, userId);
            redirectAttributes.addFlashAttribute("success", "Đã đặt lại bộ thẻ. Bắt đầu ôn tập lại toàn bộ!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/study/" + deckId;
    }
}
