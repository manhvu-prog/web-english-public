package com.example.ankard.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * Thực thể đại diện cho người dùng (User) của hệ thống.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId; // Khóa chính

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username; // Tên đăng nhập (duy nhất)

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email; // Địa chỉ email (duy nhất)

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash; // Mật khẩu đã được mã hóa

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role = Role.user; // Vai trò của người dùng (mặc định là user)

    @Column(name = "vip_expires_at")
    private LocalDate vipExpiresAt; // Ngày hết hạn gói VIP (nếu có)

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now(); // Ngày gia nhập

    /**
     * Kiểm tra xem người dùng có quyền VIP hay không dựa trên role và ngày hết hạn.
     */
    public boolean isVip() {
        return (role == Role.vip || role == Role.admin || role == Role.super_admin)
                && (role == Role.admin || role == Role.super_admin
                    || (vipExpiresAt != null && !vipExpiresAt.isBefore(LocalDate.now())));
    }

    /**
     * Danh sách các vai trò trong hệ thống.
     */
    public enum Role {
        user, vip, admin, super_admin
    }
}
