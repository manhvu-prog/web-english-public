package com.example.ankard.dto;

import com.example.ankard.model.ReviewHistory;
import lombok.*;

/**
 * DTO gửi kết quả ôn tập một thẻ từ phía Frontend lên Server.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ReviewResultDTO {
    private Integer flashcardId; // ID của thẻ vừa ôn
    private Integer deckId;      // ID của bộ thẻ chứa thẻ đó
    private ReviewHistory.ReviewResult result; // Kết quả tự đánh giá: again | hard | good | easy
}
