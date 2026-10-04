package com.example.ankard.service;

import com.example.ankard.model.FlashcardProgress;
import com.example.ankard.model.ReviewHistory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Service thực hiện thuật toán Spaced Repetition (Lặp lại ngắt quãng) dựa trên SM-2.
 * Thuật toán này giúp tối ưu hóa việc ghi nhớ bằng cách tính toán thời điểm ôn tập tiếp theo 
 * dựa trên độ khó của thẻ và mức độ phản hồi của người dùng.
 *
 * Quy tắc cốt lõi:
 * - again: Quên hoàn toàn -> Quay lại bước học ban đầu (<1 phút), giảm Ease Factor.
 * - hard: Nhớ mang máng -> Ôn lại sớm (<10 phút), giảm Ease Factor nhẹ.
 * - good: Nhớ tốt -> Tăng khoảng cách ôn tập (Interval) theo Ease Factor hiện tại.
 * - easy: Rất dễ -> Tăng mạnh khoảng cách ôn tập, tăng Ease Factor.
 */
@Service
public class SpacedRepetitionService {

    private static final BigDecimal MIN_EASE = new BigDecimal("1.30"); // Ease Factor tối thiểu để tránh interval quá ngắn
    private static final BigDecimal EASE_BONUS = new BigDecimal("0.15"); // Thưởng khi chọn Easy
    private static final BigDecimal EASE_PENALTY = new BigDecimal("0.20"); // Phạt khi chọn Again
    private static final int AGAIN_MINUTES = 1; // Thời gian quay lại khi quên
    private static final int HARD_MINUTES = 10; // Thời gian quay lại khi thấy khó

    /**
     * Áp dụng kết quả ôn tập vào tiến trình hiện tại của thẻ.
     */
    public void applyReview(FlashcardProgress progress, ReviewHistory.ReviewResult result) {
        LocalDateTime now = LocalDateTime.now();
        progress.setLastReview(now);
        progress.setReviewCount(progress.getReviewCount() + 1);

        switch (result) {
            case again -> applyAgain(progress, now);
            case hard -> applyHard(progress, now);
            case good -> applyGood(progress, now);
            case easy -> applyEasy(progress, now);
        }
    }

    // ========== CHI TIẾT LOGIC CHO TỪNG PHẢN HỒI ==========

    /** Xử lý khi chọn 'Again' (Quên): Reset interval, đặt lại giai đoạn học tập. */
    private void applyAgain(FlashcardProgress p, LocalDateTime now) {
        p.setWrongCount(p.getWrongCount() + 1);
        p.setIntervalDays(1);
        p.setStatus(FlashcardProgress.Status.LEARNING);
        p.setNextReview(now.plusMinutes(AGAIN_MINUTES));

        // Giảm Ease Factor (phạt vì quên)
        BigDecimal newEase = p.getEaseFactor().subtract(EASE_PENALTY);
        p.setEaseFactor(newEase.max(MIN_EASE).setScale(2, RoundingMode.HALF_UP));
    }

    /** Xử lý khi chọn 'Hard': Giảm Ease Factor, cho gặp lại sau 10p. */
    private void applyHard(FlashcardProgress p, LocalDateTime now) {
        p.setWrongCount(p.getWrongCount() + 1);
        p.setStatus(FlashcardProgress.Status.LEARNING);
        p.setNextReview(now.plusMinutes(HARD_MINUTES));

        // Giảm Ease Factor nhẹ hơn trường hợp Again
        BigDecimal newEase = p.getEaseFactor().subtract(new BigDecimal("0.15"));
        p.setEaseFactor(newEase.max(MIN_EASE).setScale(2, RoundingMode.HALF_UP));
    }

    /** Xử lý khi chọn 'Good': Tăng interval theo công thức SM-2. */
    private void applyGood(FlashcardProgress p, LocalDateTime now) {
        p.setCorrectCount(p.getCorrectCount() + 1);

        int newInterval;
        if (p.getIntervalDays() <= 1) {
            newInterval = 1; // Nếu card đang rất mới, interval là 1 ngày
        } else {
            // Công thức: Interval mới = Interval cũ * Ease Factor
            newInterval = (int) Math.round(
                    p.getIntervalDays() * p.getEaseFactor().doubleValue());
        }

        p.setIntervalDays(newInterval);
        p.setNextReview(now.plusDays(newInterval));

        // Nếu interval đạt 21 ngày, thẻ được coi là đã làm chủ (MASTERED)
        if (newInterval >= 21) {
            p.setStatus(FlashcardProgress.Status.MASTERED);
            p.setMasteryLevel(p.getMasteryLevel() + 1);
        } else {
            p.setStatus(FlashcardProgress.Status.REVIEW);
        }
    }

    /** Xử lý khi chọn 'Easy': Tăng Ease Factor và nhảy vọt khoảng cách ôn tập. */
    private void applyEasy(FlashcardProgress p, LocalDateTime now) {
        p.setCorrectCount(p.getCorrectCount() + 1);

        // Tăng Ease Factor (thưởng vì nhớ quá tốt)
        BigDecimal newEase = p.getEaseFactor().add(EASE_BONUS).setScale(2, RoundingMode.HALF_UP);
        p.setEaseFactor(newEase);

        // Công thức Easy: Interval mới = Interval cũ * Ease Factor * Hệ số thưởng 1.3
        int newInterval = (int) Math.round(
                p.getIntervalDays() * newEase.doubleValue() * 1.3);
        if (newInterval < 4)
            newInterval = 4; // Tối thiểu 4 ngày khi chọn Easy

        p.setIntervalDays(newInterval);
        p.setNextReview(now.plusDays(newInterval));
        p.setStatus(FlashcardProgress.Status.REVIEW);
        p.setMasteryLevel(p.getMasteryLevel() + 1);
    }

    // ========== HELPER: Tính toán nhãn hiển thị cho các nút ôn tập ==========

    /**
     * Dự đoán thời gian ôn tập tiếp theo để hiển thị lên UI (ví dụ: "3d", "5d", "<1m").
     */
    public String getNextReviewLabel(FlashcardProgress progress, ReviewHistory.ReviewResult result) {
        return switch (result) {
            case again -> "<" + AGAIN_MINUTES + "m";
            case hard -> "<" + HARD_MINUTES + "m";
            case good -> {
                int interval = progress.getIntervalDays() <= 1 ? 1
                        : (int) Math.round(progress.getIntervalDays() * progress.getEaseFactor().doubleValue());
                yield interval + "d";
            }
            case easy -> {
                double easeAfter = progress.getEaseFactor().add(EASE_BONUS).doubleValue();
                int interval = (int) Math.round(progress.getIntervalDays() * easeAfter * 1.3);
                if (interval < 4)
                    interval = 4;
                yield interval + "d";
            }
        };
    }
}
