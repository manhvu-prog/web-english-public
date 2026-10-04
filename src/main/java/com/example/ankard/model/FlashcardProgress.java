package com.example.ankard.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Thực thể lưu trữ tiến trình học tập của một người dùng đối với một thẻ cụ thể.
 * Đây là trung tâm của thuật toán lặp lại ngắt quãng (Spaced Repetition).
 */
@Entity
@Table(name = "flashcard_progress")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FlashcardProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "progress_id")
    private Integer progressId; // Khóa chính

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Người học

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flashcard_id", nullable = false)
    private Flashcard flashcard; // Thẻ đang học

    @Builder.Default
    @Column(name = "review_count")
    private Integer reviewCount = 0; // Tổng số lần đã ôn tập thẻ này

    @Builder.Default
    @Column(name = "correct_count")
    private Integer correctCount = 0; // Số lần trả lời đúng

    @Builder.Default
    @Column(name = "wrong_count")
    private Integer wrongCount = 0; // Số lần trả lời sai

    @Builder.Default
    @Column(name = "mastery_level")
    private Integer masteryLevel = 0; // Mức độ thông thạo (0-5)

    @Builder.Default
    @Column(name = "ease_factor", precision = 3, scale = 2)
    private BigDecimal easeFactor = new BigDecimal("2.50"); // Hệ số dễ (mặc định 2.5 theo thuật toán SM-2)

    @Builder.Default
    @Column(name = "interval_days")
    private Integer intervalDays = 1; // Khoảng cách ngày cho lần ôn tập kế tiếp

    @Column(name = "last_review")
    private LocalDateTime lastReview; // Lần ôn tập gần nhất

    @Column(name = "next_review")
    private LocalDateTime nextReview; // Ngày ôn tập dự kiến tiếp theo

    @Builder.Default
    @Convert(converter = FlashcardProgressStatusConverter.class)
    @Column(name = "status")
    private Status status = Status.NEW; // Trạng thái học tập (Mới, Đang học, Cần ôn, Đã thuộc)

    /**
     * Các trạng thái của một thẻ trong quá trình học.
     */
    public enum Status {
        NEW("new"),          // Thẻ mới chưa học
        LEARNING("learning"), // Đang trong giai đoạn học ban đầu
        REVIEW("review"),     // Đã vào giai đoạn ôn tập định kỳ
        MASTERED("mastered"); // Đã cực kỳ thông thạo

        private final String value;
        Status(String value) { this.value = value; }
        public String getValue() { return value; }
    }
}
