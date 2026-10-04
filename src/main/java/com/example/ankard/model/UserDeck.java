package com.example.ankard.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Thực thể trung gian lưu thông tin bộ thẻ của một người dùng (Bộ thẻ cá nhân hoặc bộ thẻ đã import).
 */
@Entity
@Table(name = "user_decks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDeck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_deck_id")
    private Integer userDeckId; // Khóa chính

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Chủ sở hữu bộ thẻ

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_id", nullable = false)
    private Deck deck; // Bộ thẻ được liên kết

    @Builder.Default
    @Column(name = "is_favorite")
    private Boolean isFavorite = false; // Đánh dấu bộ thẻ yêu thích

    @Builder.Default
    @Column(name = "added_at")
    private LocalDateTime addedAt = LocalDateTime.now(); // Thời điểm bộ thẻ được thêm vào thư viện cá nhân
}
