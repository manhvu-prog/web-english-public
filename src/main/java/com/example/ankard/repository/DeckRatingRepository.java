package com.example.ankard.repository;

import com.example.ankard.model.DeckRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository xử lý các thao tác database cho thực thể DeckRating (Đánh giá bộ thẻ).
 */
@Repository
public interface DeckRatingRepository extends JpaRepository<DeckRating, Integer> {

    /**
     * Lấy danh sách tất cả đánh giá của một bộ thẻ, sắp xếp theo thời gian mới nhất lên đầu.
     */
    List<DeckRating> findByDeck_DeckIdOrderByCreatedAtDesc(Integer deckId);

    /**
     * Tìm đánh giá của một người dùng cụ thể cho một bộ thẻ cụ thể.
     * Thường dùng để kiểm tra xem user đã đánh giá chưa hoặc để lấy ra chỉnh sửa.
     */
    Optional<DeckRating> findByDeck_DeckIdAndUser_UserId(Integer deckId, Integer userId);

    /**
     * Xóa toàn bộ đánh giá của một người dùng.
     */
    void deleteByUser_UserId(Integer userId);

    /**
     * Đếm tổng số lượng lượt đánh giá của một bộ thẻ.
     */
    long countByDeck_DeckId(Integer deckId);

    /**
     * Tính điểm sao trung bình của một bộ thẻ.
     * COALESCE(..., 0) đảm bảo nếu chưa có đánh giá nào thì trả về 0 thay vì null.
     */
    @Query("SELECT COALESCE(AVG(r.stars), 0) FROM DeckRating r WHERE r.deck.deckId = :deckId")
    double avgStarsByDeckId(@Param("deckId") Integer deckId);

    /**
     * Thống kê phân bố số lượng sao (5 sao có bao nhiêu lượt, 4 sao bao nhiêu,...).
     * Trả về danh sách mảng Object: row[0] là số sao, row[1] là số lượng.
     */
    @Query("SELECT r.stars, COUNT(r) FROM DeckRating r WHERE r.deck.deckId = :deckId GROUP BY r.stars ORDER BY r.stars DESC")
    List<Object[]> starDistribution(@Param("deckId") Integer deckId);
}
