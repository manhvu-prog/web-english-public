package com.example.ankard.dto;

import lombok.*;

/**
 * DTO chứa thông tin về một phiên học (Study Session).
 * Cung cấp dữ liệu về thẻ hiện tại và tiến độ trong session đó.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudySessionDTO {
    private Integer deckId; // ID của bộ thẻ đang học
    private String deckTitle; // Tiêu đề bộ thẻ
    private Integer flashcardId; // ID của thẻ hiện tại
    private String frontContent; // Nội dung mặt trước thẻ
    private String backContent; // Nội dung mặt sau thẻ
    private String exampleSentence; // Ví dụ
    private String pronunciation; // Phiên âm
    private String imageUrl; // URL ảnh
    private String audioUrl; // URL âm thanh
    private int remainingCards;   // Số lượng thẻ còn lại trong phiên học này
    private int totalDue;         // Tổng số thẻ cần ôn tập trong ngày
    private boolean showAnswer;   // Cờ xác định đang hiển thị mặt trước hay mặt sau
}
