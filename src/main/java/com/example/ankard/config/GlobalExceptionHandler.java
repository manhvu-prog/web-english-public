package com.example.ankard.config;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Lớp xử lý ngoại lệ (Exception) tập trung cho toàn bộ ứng dụng.
 * Khi có lỗi xảy ra ở bất kỳ đâu, nó sẽ bắt lại và điều hướng tới trang lỗi thay vì hiện lỗi code cho người dùng.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Xử lý các lỗi RuntimeException (lỗi logic, lỗi DB,...)
     * @return Tên template HTML hiển thị thông báo lỗi.
     */
    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error"; // Trả về file templates/error.html
    }
}
