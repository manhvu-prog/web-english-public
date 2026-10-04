package com.example.ankard.repository;

import com.example.ankard.model.UserDeck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository quản lý quan hệ giữa người dùng và các bộ thẻ (Bảng liên kết).
 */
@Repository
public interface UserDeckRepository extends JpaRepository<UserDeck, Integer> {

    /** Tìm bản ghi liên kết giữa một user cụ thể và một bộ thẻ cụ thể. */
    Optional<UserDeck> findByUser_UserIdAndDeck_DeckId(Integer userId, Integer deckId);

    /** Kiểm tra xem một bộ thẻ đã có trong thư viện của người dùng hay chưa. */
    boolean existsByUser_UserIdAndDeck_DeckId(Integer userId, Integer deckId);

    /** Xóa tất cả các liên kết bộ thẻ của một người dùng. */
    void deleteByUser_UserId(Integer userId);
}
