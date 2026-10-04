package com.example.ankard.repository;

import com.example.ankard.model.FlashcardProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository quản lý tiến trình học tập của người dùng đối với từng thẻ học.
 */
@Repository
public interface FlashcardProgressRepository extends JpaRepository<FlashcardProgress, Integer> {

    /** Tìm thông tin tiến trình của một người dùng cho một thẻ cụ thể. */
    Optional<FlashcardProgress> findByUser_UserIdAndFlashcard_FlashcardId(
        Integer userId, Integer flashcardId
    );

    /** Xóa toàn bộ tiến trình học tập của một người dùng. */
    void deleteByUser_UserId(Integer userId);

    /**
     * Đếm số lượng thẻ "Mới" (chưa bao giờ ôn tập) trong một bộ thẻ của người dùng.
     */
    @Query("""
        SELECT COUNT(f) FROM Flashcard f
        WHERE f.deck.deckId = :deckId
          AND f.flashcardId NOT IN (
              SELECT fp.flashcard.flashcardId FROM FlashcardProgress fp
              WHERE fp.user.userId = :userId
          )
    """)
    long countNewCards(@Param("deckId") Integer deckId, @Param("userId") Integer userId);

    /** Đếm số lượng thẻ theo trạng thái cụ thể (LEARNING, REVIEW,...) trong một bộ thẻ. */
    long countByFlashcard_Deck_DeckIdAndUser_UserIdAndStatus(
        Integer deckId,
        Integer userId,
        FlashcardProgress.Status status
    );

    /** Phương thức hỗ trợ đếm thẻ đang trong giai đoạn học (LEARNING). */
    default long countLearningCards(Integer deckId, Integer userId) {
        return countByFlashcard_Deck_DeckIdAndUser_UserIdAndStatus(deckId, userId, FlashcardProgress.Status.LEARNING);
    }

    /** Phương thức hỗ trợ đếm thẻ tới hạn ôn tập (REVIEW). */
    default long countReviewCards(Integer deckId, Integer userId) {
        return countByFlashcard_Deck_DeckIdAndUser_UserIdAndStatus(deckId, userId, FlashcardProgress.Status.REVIEW);
    }

    /** Lấy tất cả bản ghi tiến trình của một người dùng trong một bộ thẻ. */
    @Query("""
        SELECT fp FROM FlashcardProgress fp
        WHERE fp.flashcard.deck.deckId = :deckId
          AND fp.user.userId = :userId
    """)
    List<FlashcardProgress> findAllByDeckAndUser(
        @Param("deckId") Integer deckId,
        @Param("userId") Integer userId
    );

    /**
     * Đặt lại tiến trình của một bộ thẻ (Dùng khi người dùng muốn học lại từ đầu).
     * Tất cả thẻ đã thuộc (MASTERED) sẽ quay lại trạng thái REVIEW.
     */
    @Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("""
        UPDATE FlashcardProgress fp
        SET fp.nextReview = :now,
            fp.status = 'REVIEW'
        WHERE fp.flashcard.deck.deckId = :deckId
          AND fp.user.userId = :userId
    """)
    int resetDeckProgress(
        @Param("deckId") Integer deckId,
        @Param("userId") Integer userId,
        @Param("now") LocalDateTime now
    );

    // ========== THỐNG KÊ (STATS) ==========

    /** Tổng số thẻ mà người dùng đã từng ôn tập. */
    @Query("SELECT COUNT(fp) FROM FlashcardProgress fp WHERE fp.user.userId = :userId")
    long countByUser(@Param("userId") Integer userId);

    /** Số lượng thẻ người dùng đã thực sự làm chủ (Mastered). */
    @Query("SELECT COUNT(fp) FROM FlashcardProgress fp WHERE fp.user.userId = :userId AND fp.status = 'MASTERED'")
    long countMasteredByUser(@Param("userId") Integer userId);

    /** Tổng số lượt ôn tập trên toàn hệ thống. */
    @Query("SELECT COALESCE(SUM(fp.reviewCount), 0) FROM FlashcardProgress fp")
    long sumTotalReviews();

    /** Tổng số lượt ôn tập của một người dùng. */
    @Query("SELECT COALESCE(SUM(fp.reviewCount), 0) FROM FlashcardProgress fp WHERE fp.user.userId = :userId")
    long sumReviewsByUser(@Param("userId") Integer userId);

    /** Tổng số câu trả lời Đúng của người dùng. */
    @Query("SELECT COALESCE(SUM(fp.correctCount), 0) FROM FlashcardProgress fp WHERE fp.user.userId = :userId")
    long sumCorrectByUser(@Param("userId") Integer userId);

    /** Tổng số câu trả lời Sai của người dùng. */
    @Query("SELECT COALESCE(SUM(fp.wrongCount), 0) FROM FlashcardProgress fp WHERE fp.user.userId = :userId")
    long sumWrongByUser(@Param("userId") Integer userId);
}
