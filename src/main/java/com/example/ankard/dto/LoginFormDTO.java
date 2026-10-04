package com.example.ankard.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO chứa thông tin đăng nhập của người dùng.
 */
@Getter
@Setter
@NoArgsConstructor
public class LoginFormDTO {
    @NotBlank(message = "Vui lòng nhập username")
    private String username; // Tên đăng nhập

    @NotBlank(message = "Vui lòng nhập mật khẩu")
    private String password; // Mật khẩu
}

