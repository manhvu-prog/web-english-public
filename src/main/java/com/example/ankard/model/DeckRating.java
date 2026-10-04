package com.example.ankard.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity ánh xạ tới bảng 'deck_ratings' trong Database.
 * Lưu trữ thông tin đánh giá (sao và bình luận) của người dùng cho các bộ thẻ công khai.
 */
@Entity
@Table(name = "deck_ratings",
       uniqueConstraints = @UniqueConstraint(columnNames = {"deck_id", "user_id"}))
// UniqueConstraint đảm bảo mỗi User chỉ có duy nhất 1 bản ghi đánh giá cho mỗi Deck.
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeckRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rating_id")
    private Integer ratingId; // Khóa chính tự tăng

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_id", nullable = false)
    private Deck deck; // Bộ thẻ được đánh giá (Mối quan hệ N-1)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Người dùng thực hiện đánh giá (Mối quan hệ N-1)

    /** Số sao đánh giá: từ 1 đến 5 */
    @Column(name = "stars", nullable = false)
    private int stars;

    /** Nội dung bình luận (không bắt buộc) */
    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now(); // Thời điểm tạo đánh giá

    @Builder.Default
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now(); // Thời điểm cập nhật cuối cùng

    /**
     * Tự động cập nhật thời gian 'updated_at' mỗi khi bản ghi được chỉnh sửa.
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
