package com.example.ankard.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Thực thể đại diện cho một thẻ học (Flashcard).
 * Một Flashcard luôn thuộc về một bộ thẻ (Deck).
 */
@Entity
@Table(name = "flashcards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flashcard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flashcard_id")
    private Integer flashcardId; // Khóa chính

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_id", nullable = false)
    @JsonIgnore
    private Deck deck; // Bộ thẻ chứa thẻ này

    @Column(name = "front_content", nullable = false, columnDefinition = "TEXT")
    private String frontContent; // Nội dung mặt trước (thường là câu hỏi/từ vựng)

    @Column(name = "back_content", nullable = false, columnDefinition = "TEXT")
    private String backContent; // Nội dung mặt sau (thường là câu trả lời/nghĩa)

    @Column(name = "example_sentence", columnDefinition = "TEXT")
    private String exampleSentence; // Câu ví dụ minh họa

    @Column(name = "pronunciation", length = 255)
    private String pronunciation; // Cách phát âm (phiên âm)

    @Column(name = "image_url", length = 255)
    private String imageUrl; // Đường dẫn ảnh minh họa

    @Column(name = "audio_url", length = 255)
    private String audioUrl; // Đường dẫn file âm thanh

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now(); // Ngày tạo

    @Builder.Default
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now(); // Ngày cập nhật cuối

    @OneToMany(mappedBy = "flashcard", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<FlashcardTag> flashcardTags; // Các nhãn (tags) được gắn cho thẻ này

    /**
     * Tự động cập nhật thời gian 'updated_at' trước khi lưu vào DB.
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
