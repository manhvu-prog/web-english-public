package com.example.ankard.config;

import com.example.ankard.controller.AuthController;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Thành phần hỗ trợ lấy thông tin người dùng hiện tại đang đăng nhập từ Session.
 * Trong giai đoạn phát triển, nếu SESSION trống, nó sẽ mặc định trả về ID = 1 để hệ thống không bị lỗi.
 */
@Component
public class DemoUserConfig {

    private final ObjectProvider<HttpServletRequest> requestProvider;

    public DemoUserConfig(ObjectProvider<HttpServletRequest> requestProvider) {
        this.requestProvider = requestProvider;
    }

    /**
     * Lấy ID của người dùng hiện tại từ Session.
     * @return ID người dùng (Integer) hoặc 1 (mặc định cho demo).
     */
    public Integer getCurrentUserId() {
        HttpServletRequest request = requestProvider.getIfAvailable();
        if (request != null) {
            Object userId = request.getSession(false) != null
                    ? request.getSession(false).getAttribute(AuthController.SESSION_USER_ID)
                    : null;
            if (userId instanceof Integer id) return id;
        }
        return 1; // Fallback cho môi trường demo hoặc khi chưa tích hợp Auth hoàn chỉnh
    }
}
