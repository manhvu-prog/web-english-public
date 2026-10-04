package com.example.ankard.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Thực thể lưu lại lịch sử mỗi lần người dùng trả lời một thẻ.
 * Dùng để thống kê hiệu quả học tập và phục vụ thuật toán SRS.
 */
@Entity
@Table(name = "review_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Integer reviewId; // Khóa chính

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Người thực hiện ôn tập

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flashcard_id", nullable = false)
    private Flashcard flashcard; // Thẻ đã ôn tập

    @Enumerated(EnumType.STRING)
    @Column(name = "review_result", nullable = false)
    private ReviewResult reviewResult; // Kết quả lựa chọn (Quên, Khó, Tốt, Dễ)

    @Builder.Default
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt = LocalDateTime.now(); // Thời điểm ôn tập

    /**
     * Các mức độ đánh giá câu trả lời của người dùng.
     */
    public enum ReviewResult {
        again, // Quên hoàn toàn (học lại từ đầu)
        hard,  // Nhớ mang máng nhưng rất khó khăn
        good,  // Nhớ tốt, phản xạ bình thường
        easy   // Nhớ cực tốt, phản xạ tức thì
    }
}
