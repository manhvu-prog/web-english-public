package com.example.ankard.repository;

import com.example.ankard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository quản lý thông tin người dùng (Tài khoản).
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    
    /** Tìm người dùng theo tên đăng nhập. */
    Optional<User> findByUsername(String username);

    /** Tìm người dùng theo email. */
    Optional<User> findByEmail(String email);

    /** Tìm kiếm người dùng theo tên (không phân biệt hoa thường) để quản trị. */
    List<User> findByUsernameContainingIgnoreCaseOrderByUsernameAsc(String username);

    // ========== THỐNG KÊ (STATS) ==========

    /** Đếm số lượng người dùng phổ thông. */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'user'")
    long countRegularUsers();

    /** Đếm số lượng người dùng có quyền VIP. */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'vip'")
    long countVipUsers();

    /** Đếm số lượng quản trị viên. */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'admin' OR u.role = 'super_admin'")
    long countAdminUsers();
}
