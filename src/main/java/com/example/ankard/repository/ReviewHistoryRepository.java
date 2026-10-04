package com.example.ankard.repository;

import com.example.ankard.model.ReviewHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository quản lý lịch sử ôn tập của người dùng.
 */
@Repository
public interface ReviewHistoryRepository extends JpaRepository<ReviewHistory, Integer> {

    /** Lấy lịch sử tất cả các lần ôn tập của một thẻ cụ thể bởi một người dùng, xếp theo thời gian mới nhất. */
    List<ReviewHistory> findByUser_UserIdAndFlashcard_FlashcardIdOrderByReviewedAtDesc(
        Integer userId, Integer flashcardId
    );

    /** Lấy toàn bộ lịch sử ôn tập của một người dùng. */
    List<ReviewHistory> findByUser_UserIdOrderByReviewedAtDesc(Integer userId);

    /** Xóa toàn bộ lịch sử ôn tập của một người dùng. */
    void deleteByUser_UserId(Integer userId);

    /** Đếm số lượt ôn tập mà người dùng đã thực hiện trong ngày hôm nay. */
    @Query("SELECT COUNT(rh) FROM ReviewHistory rh WHERE rh.user.userId = :userId AND rh.reviewedAt >= :startOfDay")
    long countTodayByUser(@Param("userId") Integer userId, @Param("startOfDay") LocalDateTime startOfDay);

    /** Đếm tổng số lượt ôn tập trên toàn hệ thống kể từ một thời điểm cụ thể. */
    @Query("SELECT COUNT(rh) FROM ReviewHistory rh WHERE rh.reviewedAt >= :since")
    long countSince(@Param("since") LocalDateTime since);

    /**
     * Thống kê số lượt ôn tập hàng ngày của người dùng tính từ một thời điểm (Dùng cho biểu đồ nhiệt hoặc biểu đồ cột).
     * Trả về danh sách mảng Object [ngày, số lượt].
     */
    @Query("""
        SELECT CAST(rh.reviewedAt AS date), COUNT(rh)
        FROM ReviewHistory rh
        WHERE rh.user.userId = :userId AND rh.reviewedAt >= :since
        GROUP BY CAST(rh.reviewedAt AS date)
        ORDER BY CAST(rh.reviewedAt AS date) ASC
    """)
    List<Object[]> countDailyReviewsByUser(@Param("userId") Integer userId, @Param("since") LocalDateTime since);
}
