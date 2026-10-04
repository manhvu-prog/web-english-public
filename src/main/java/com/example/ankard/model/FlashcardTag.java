package com.example.ankard.model;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

/**
 * Thực thể trung gian nối giữa Flashcard và Tag (Quan hệ N-N).
 * Một thẻ có thể có nhiều nhãn, và một nhãn có thể gắn cho nhiều thẻ.
 */
@Entity
@Table(name = "flashcard_tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(FlashcardTag.FlashcardTagId.class)
public class FlashcardTag {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flashcard_id")
    private Flashcard flashcard; // Thẻ được gắn nhãn

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private Tag tag; // Nhãn được gắn vào thẻ

    /**
     * Lớp định nghĩa khóa chính tổng hợp (Composite Key) cho bảng flashcard_tags.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlashcardTagId implements Serializable {
        private Integer flashcard; // ID của flashcard
        private Integer tag;       // ID của tag
    }
}
