package com.example.ankard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO chứa dữ liệu cho biểu mẫu cập nhật thông tin tài khoản và đổi mật khẩu.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountFormDTO {
    private String email; // Email mới của người dùng
    private String currentPassword; // Mật khẩu hiện tại (để xác thực)
    private String newPassword; // Mật khẩu mới
    private String confirmNewPassword; // Xác nhận lại mật khẩu mới
}
