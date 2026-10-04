package com.example.ankard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO chứa dữ liệu để tạo mới hoặc cập nhật thông tin một bộ thẻ (Deck).
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeckFormDTO {

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 100, message = "Tiêu đề tối đa 100 ký tự")
    private String title; // Tiêu đề của bộ thẻ

    private String description; // Mô tả ngắn gọn về bộ thẻ
}
