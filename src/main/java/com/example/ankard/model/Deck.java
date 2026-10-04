package com.example.ankard.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Thực thể (Entity) đại diện cho một bộ thẻ học (Deck).
 */
@Entity
@Table(name = "decks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deck_id")
    private Integer deckId; // Khóa chính

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy; // Người tạo bộ thẻ

    @Column(name = "title", nullable = false, length = 100)
    private String title; // Tiêu đề bộ thẻ

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // Mô tả bộ thẻ

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DeckStatus status = DeckStatus.PRIVATE; // Trạng thái chia sẻ (mặc định là riêng tư)

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now(); // Thời điểm tạo

    @Builder.Default
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now(); // Thời điểm cập nhật cuối cùng

    @Builder.Default
    @Column(name = "view_count", nullable = false)
    private long viewCount = 0; // Số lượt xem (dành cho bộ thẻ công khai)

    @OneToMany(mappedBy = "deck", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Flashcard> flashcards; // Danh sách các thẻ có trong bộ này

    /**
     * Tự động cập nhật thời gian 'updated_at' trước khi lưu vào DB.
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

