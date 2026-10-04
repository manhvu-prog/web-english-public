package com.example.ankard.model;

/**
 * Định nghĩa các trạng thái của một bộ thẻ (Deck).
 * Trạng thái này quyết định ai có thể xem và thực hiện các hành động trên bộ thẻ.
 */
public enum DeckStatus {
    PRIVATE,      // Bộ thẻ riêng tư, chỉ chủ sở hữu mới thấy và học được
    PENDING,      // Đã gửi yêu cầu công khai, đang chờ Admin duyệt
    APPROVED,     // Đã được duyệt, xuất hiện công khai trên trang cộng đồng cho mọi người
    REJECTED,     // Bị từ chối duyệt công khai hoặc bị Admin ẩn đi
    VIP_ONLY,     // Bộ thẻ công khai nhưng chỉ dành riêng cho người dùng VIP
    
    // Các trạng thái cũ (Legacy) giữ lại để tương thích với dữ liệu cũ trong DB
    UNSHARE_PENDING,
    UNSHARE_PENDI
}
