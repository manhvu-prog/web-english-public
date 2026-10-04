package com.example.ankard.dto;

import com.example.ankard.model.DeckStatus;
import lombok.*;

/**
 * DTO (Data Transfer Object) dùng để chứa thông tin tóm tắt của một bộ thẻ.
 * Được sử dụng để truyền dữ liệu từ Backend ra Giao diện (Thymeleaf) một cách gọn nhẹ,
 * tránh truyền trực tiếp Entity để bảo mật và tối ưu hiệu năng.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeckSummaryDTO {
    private Integer deckId;        // ID của bộ thẻ
    private String title;          // Tiêu đề/Tên bộ thẻ
    private String description;    // Mô tả nội dung bộ thẻ
    private DeckStatus status;     // Trạng thái hiện tại (PRIVATE, APPROVED, VIP_ONLY,...)
    private long totalCards;       // Tổng số cards có trong bộ thẻ này
    private long newCards;         // Số card mới (chưa học) của người dùng hiện tại
    private long learningCards;    // Số card đang học (đang trong quá trình ghi nhớ)
    private long reviewCards;      // Số card cần ôn tập lại ngay
    private boolean vipOnly;       // Đánh dấu bộ thẻ này có giới hạn cho VIP hay không
    private long viewCount;        // Tổng số lượt xem bộ thẻ trên trang cộng đồng
}
