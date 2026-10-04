package com.example.ankard.config;

import com.example.ankard.model.User;
import com.example.ankard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Thành phần (Component) dùng để khởi tạo dữ liệu mẫu cho Database khi ứng dụng chạy lần đầu.
 * Giúp tạo sẵn các tài khoản Admin, VIP và User thường để kiểm thử.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) throws Exception {
        // Chỉ tạo tài khoản mẫu nếu database chưa có user nào (tránh duplicate)
        if (userRepository.count() == 0) {
            
            // 1. Tài khoản Super Admin - Có toàn quyền quản trị hệ thống
            User superAdmin = User.builder()
                    .username("admin")
                    .email("admin@ankard.com")
                    .passwordHash(passwordEncoder.encode("123456"))
                    .role(User.Role.super_admin)
                    .build();
            userRepository.save(superAdmin);

            // 2. Tài khoản VIP mẫu - Có quyền truy cập các bộ thẻ giới hạn
            User vipUser = User.builder()
                    .username("vipuser")
                    .email("vip@ankard.com")
                    .passwordHash(passwordEncoder.encode("123456"))
                    .role(User.Role.vip)
                    .vipExpiresAt(java.time.LocalDate.now().plusDays(30))
                    .build();
            userRepository.save(vipUser);

            // 3. Tài khoản User thường - Người dùng phổ thông
            User normalUser = User.builder()
                    .username("user")
                    .email("user@ankard.com")
                    .passwordHash(passwordEncoder.encode("123456"))
                    .role(User.Role.user)
                    .build();
            userRepository.save(normalUser);

            System.out.println("===========================================");
            System.out.println("✅ Đã tạo tự động các tài khoản mẫu:");
            System.out.println("1. Admin  - Tài khoản: admin   | Pass: 123456");
            System.out.println("2. VIP    - Tài khoản: vipuser | Pass: 123456");
            System.out.println("3. Thường - Tài khoản: user    | Pass: 123456");
            System.out.println("===========================================");
        }
    }
}
