package com.example.ankard.service;

import com.example.ankard.dto.SignupFormDTO;
import com.example.ankard.model.User;
import com.example.ankard.repository.DeckRatingRepository;
import com.example.ankard.repository.DeckRepository;
import com.example.ankard.repository.FlashcardProgressRepository;
import com.example.ankard.repository.ReviewHistoryRepository;
import com.example.ankard.repository.UserDeckRepository;
import com.example.ankard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service xử lý các nghiệp vụ liên quan đến xác thực và quản lý người dùng.
 * Bao gồm: Đăng nhập, đăng ký, cập nhật thông tin cá nhân và quản trị viên quản lý user.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final DeckRatingRepository deckRatingRepository;
    private final ReviewHistoryRepository reviewHistoryRepository;
    private final FlashcardProgressRepository flashcardProgressRepository;
    private final UserDeckRepository userDeckRepository;
    private final DeckRepository deckRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Xác thực người dùng dựa trên username và mật khẩu.
     * @return User nếu thành công, ném ngoại lệ nếu thất bại.
     */
    public User authenticate(String username, String rawPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Sai username hoặc mật khẩu"));
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new RuntimeException("Sai username hoặc mật khẩu");
        }
        return user;
    }

    /**
     * Đăng ký tài khoản người dùng mới.
     * Mặc định tài khoản mới sẽ có vai trò là 'user'.
     */
    @Transactional
    public User signup(SignupFormDTO form) {
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu nhập lại không khớp");
        }
        if (userRepository.findByUsername(form.getUsername()).isPresent()) {
            throw new RuntimeException("Username đã tồn tại");
        }
        if (userRepository.findByEmail(form.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã tồn tại");
        }

        User user = User.builder()
                .username(form.getUsername())
                .email(form.getEmail())
                .passwordHash(passwordEncoder.encode(form.getPassword()))
                .role(User.Role.user) // Mặc định là user thường
                .build();
        return userRepository.save(user);
    }

    /** Lấy thông tin người dùng theo ID. */
    public User getUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại."));
    }

    /** Tìm kiếm danh sách người dùng theo tên (dành cho Admin). */
    public List<User> findUsers(String query) {
        if (query == null || query.isBlank()) {
            return userRepository.findAll();
        }
        return userRepository.findByUsernameContainingIgnoreCaseOrderByUsernameAsc(query.trim());
    }

    /** Cập nhật vai trò (Role) cho người dùng. */
    @Transactional
    public void updateRole(Integer userId, String roleName) {
        if (roleName == null || roleName.isBlank()) {
            throw new RuntimeException("Vai trò không được để trống.");
        }
        User user = getUserById(userId);
        try {
            User.Role newRole = User.Role.valueOf(roleName.trim().toLowerCase());
            user.setRole(newRole);
            userRepository.save(user);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Vai trò không hợp lệ.");
        }
    }

    /**
     * Xóa tài khoản người dùng và dọn dẹp dữ liệu liên quan.
     */
    @Transactional
    public void deleteUser(Integer userId) {
        if (userId == 1) {
            throw new RuntimeException("Không thể xóa tài khoản Admin gốc của hệ thống.");
        }

        // Chuyển quyền sở hữu các bộ thẻ sang cho Admin để giữ lại nội dung cộng đồng
        User admin = userRepository.findById(1).orElseThrow(() -> new RuntimeException("Không tìm thấy admin gốc."));
        List<com.example.ankard.model.Deck> userDecks = deckRepository.findByCreatedBy_UserId(userId);
        for (com.example.ankard.model.Deck deck : userDecks) {
            deck.setCreatedBy(admin);
        }
        deckRepository.saveAll(userDecks);

        // Xóa các bảng phụ thuôc để đảm bảo toàn vẹn dữ liệu
        deckRatingRepository.deleteByUser_UserId(userId);
        reviewHistoryRepository.deleteByUser_UserId(userId);
        flashcardProgressRepository.deleteByUser_UserId(userId);
        userDeckRepository.deleteByUser_UserId(userId);
        userRepository.deleteById(userId);
    }

    /** Nâng cấp tài khoản lên VIP với thời hạn 30 ngày. */
    @Transactional
    public User upgradeToVip(Integer userId) {
        User user = getUserById(userId);
        user.setRole(User.Role.vip);
        user.setVipExpiresAt(LocalDate.now().plusDays(30));
        return userRepository.save(user);
    }

    /** Cập nhật địa chỉ email (yêu cầu mật khẩu xác nhận). */
    @Transactional
    public User updateEmail(Integer userId, String currentPassword, String email) {
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new RuntimeException("Mật khẩu hiện tại không được để trống.");
        }
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email không được để trống.");
        }
        User user = getUserById(userId);
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new RuntimeException("Mật khẩu hiện tại không đúng.");
        }
        if (userRepository.findByEmail(email).filter(u -> !u.getUserId().equals(userId)).isPresent()) {
            throw new RuntimeException("Email đã được sử dụng bởi tài khoản khác.");
        }
        user.setEmail(email);
        return userRepository.save(user);
    }

    /** Đổi mật khẩu người dùng. */
    @Transactional
    public void updatePassword(Integer userId, String currentPassword, String newPassword, String confirmPassword) {
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new RuntimeException("Mật khẩu hiện tại không được để trống.");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new RuntimeException("Mật khẩu mới không được để trống.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("Mật khẩu mới và nhập lại không khớp.");
        }
        User user = getUserById(userId);
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new RuntimeException("Mật khẩu hiện tại không đúng.");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /** Xóa tài khoản cá nhân (tương tự deleteUser nhưng dùng cho Client). */
    @Transactional
    public void deleteAccount(Integer userId) {
        deleteUser(userId); // Tái sử dụng logic xóa user
    }
}
