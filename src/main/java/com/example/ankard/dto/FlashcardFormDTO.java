package com.example.ankard.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO chứa dữ liệu để tạo mới hoặc cập nhật một thẻ học (Flashcard).
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FlashcardFormDTO {

    @NotBlank(message = "Mặt trước không được để trống")
    private String frontContent; // Nội dung câu hỏi/mặt trước

    @NotBlank(message = "Mặt sau không được để trống")
    private String backContent; // Nội dung câu trả lời/mặt sau

    private String exampleSentence; // Câu ví dụ minh họa
    private String pronunciation; // Phiên âm
    private String imageUrl; // URL ảnh minh họa
    private String audioUrl; // URL âm thanh
}
