package com.example.ankard.repository;

import com.example.ankard.model.Deck;
import com.example.ankard.model.DeckStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository quản lý các thao tác dữ liệu cho thực thể Deck (Bộ thẻ).
 */
@Repository
public interface DeckRepository extends JpaRepository<Deck, Integer> {

    /**
     * Lấy danh sách các bộ thẻ do một người dùng cụ thể tạo ra.
     */
    List<Deck> findByCreatedBy_UserId(Integer userId);

    /**
     * Tìm các bộ thẻ dựa trên trạng thái (PRIVATE, APPROVED, PENDING,...).
     */
    List<Deck> findByStatus(DeckStatus status);

    /**
     * Tìm kiếm các bộ thẻ công khai theo từ khóa trong tiêu đề hoặc mô tả.
     */
    @Query("SELECT d FROM Deck d WHERE d.status = :status AND " +
            "(LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Deck> searchDecksByStatusAndKeyword(@Param("status") DeckStatus status, @Param("keyword") String keyword);

    /**
     * Tìm bộ thẻ theo danh sách các trạng thái và sắp xếp theo độ phổ biến (lượt xem).
     */
    @Query("SELECT d FROM Deck d WHERE d.status IN :statuses ORDER BY d.viewCount DESC, d.updatedAt DESC")
    List<Deck> findByStatusIn(@Param("statuses") List<DeckStatus> statuses);

    /**
     * Tăng số lượt xem của bộ thẻ lên 1 đơn vị.
     */
    @Modifying
    @Query("UPDATE Deck d SET d.viewCount = d.viewCount + 1 WHERE d.deckId = :deckId")
    void incrementViewCount(@Param("deckId") Integer deckId);

    /**
     * Tìm kiếm bộ thẻ trong danh sách các trạng thái cho phép.
     */
    @Query("SELECT d FROM Deck d WHERE d.status IN :statuses AND " +
            "(LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Deck> searchDecksByStatusInAndKeyword(@Param("statuses") List<DeckStatus> statuses, @Param("keyword") String keyword);

    /**
     * Lấy các bộ thẻ mà người dùng đã lưu vào thư viện của mình.
     */
    @Query("SELECT ud.deck FROM UserDeck ud WHERE ud.user.userId = :userId")
    List<Deck> findDecksByUserId(@Param("userId") Integer userId);

    /**
     * Lấy tất cả các bộ thẻ liên quan đến người dùng (tự tạo HOẶC đã lưu).
     */
    @Query("""
        SELECT DISTINCT d FROM Deck d
        WHERE d.createdBy.userId = :userId
           OR d.deckId IN (SELECT ud.deck.deckId FROM UserDeck ud WHERE ud.user.userId = :userId)
        ORDER BY d.updatedAt DESC
    """)
    List<Deck> findAllDecksForUser(@Param("userId") Integer userId);

    // ========== THỐNG KÊ (STATS) ==========

    /** Đếm số lượng bộ thẻ theo trạng thái. */
    @Query("SELECT COUNT(d) FROM Deck d WHERE d.status = :status")
    long countByStatus(@Param("status") DeckStatus status);

    /** Tính tổng lượt xem của các bộ thẻ thuộc danh sách trạng thái. */
    @Query("SELECT COALESCE(SUM(d.viewCount), 0) FROM Deck d WHERE d.status IN :statuses")
    long sumViewCountByStatuses(@Param("statuses") List<DeckStatus> statuses);

    /** Lấy Top N bộ thẻ có lượt xem cao nhất. */
    @Query("SELECT d FROM Deck d WHERE d.status IN :statuses AND d.viewCount > 0 ORDER BY d.viewCount DESC")
    List<Deck> findTopByViewCount(@Param("statuses") List<DeckStatus> statuses, org.springframework.data.domain.Pageable pageable);

    /** Đếm số bộ thẻ do một user tạo. */
    @Query("SELECT COUNT(d) FROM Deck d WHERE d.createdBy.userId = :userId")
    long countByCreatedBy(@Param("userId") Integer userId);
}
