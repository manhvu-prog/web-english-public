package com.example.ankard.repository;

import com.example.ankard.model.Flashcard;
import com.example.ankard.model.FlashcardProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository quản lý dữ liệu cho thực thể Flashcard (Thẻ học).
 */
@Repository
public interface FlashcardRepository extends JpaRepository<Flashcard, Integer> {

    /** Lấy tất cả các thẻ có trong một bộ thẻ. */
    List<Flashcard> findByDeck_DeckId(Integer deckId);

    /** Đếm tổng số thẻ trong một bộ thẻ. */
    long countByDeck_DeckId(Integer deckId);

    /**
     * Truy vấn lấy danh sách các thẻ "Cần học" hoặc "Cần ôn tập" trong một bộ thẻ.
     * Logic:
     * - Thẻ chưa bao giờ học (fp IS NULL).
     * - Thẻ đã học nhưng chưa làm chủ (fp.status <> 'MASTERED').
     * - Thẻ đã đến hạn ôn tập (fp.nextReview <= now).
     * - Thẻ mới đang trong giai đoạn học nhanh (Status LEARNING).
     */
    @Query("""
                SELECT f FROM Flashcard f
                LEFT JOIN FlashcardProgress fp ON f.flashcardId = fp.flashcard.flashcardId AND fp.user.userId = :userId
                WHERE f.deck.deckId = :deckId
                  AND (fp IS NULL OR fp.status <> :masteredStatus)
                  AND (
                      fp IS NULL
                      OR fp.nextReview <= :now
                      OR fp.status = com.example.ankard.model.FlashcardProgress$Status.LEARNING
                  )
                ORDER BY
                    CASE
                        WHEN fp IS NULL THEN 0
                        WHEN fp.nextReview <= :now THEN 1
                        ELSE 2
                    END ASC,
                    fp.nextReview ASC
            """)
    List<Flashcard> findDueFlashcards(
            @Param("deckId") Integer deckId,
            @Param("userId") Integer userId,
            @Param("now") LocalDateTime now,
            @Param("masteredStatus") FlashcardProgress.Status masteredStatus);
}
