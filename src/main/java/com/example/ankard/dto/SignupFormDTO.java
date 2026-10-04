package com.example.ankard.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO chứa thông tin để đăng ký tài khoản mới.
 */
@Getter
@Setter
@NoArgsConstructor
public class SignupFormDTO {
    @NotBlank(message = "Vui lòng nhập username")
    private String username; // Tên đăng nhập mong muốn

    @NotBlank(message = "Vui lòng nhập email")
    @Email(message = "Email không hợp lệ")
    private String email; // Địa chỉ email đăng ký

    @NotBlank(message = "Vui lòng nhập mật khẩu")
    @Size(min = 6, message = "Mật khẩu tối thiểu 6 ký tự")
    private String password; // Mật khẩu đăng ký

    @NotBlank(message = "Vui lòng nhập lại mật khẩu")
    private String confirmPassword; // Xác nhận lại mật khẩu
}

