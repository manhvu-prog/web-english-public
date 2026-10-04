package com.example.ankard.service;

import com.example.ankard.dto.StudySessionDTO;
import com.example.ankard.model.*;
import com.example.ankard.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service chính xử lý quy trình học tập (Study Flow).
 * Điều phối việc lấy thẻ cần học, xử lý kết quả ôn tập và phối hợp với SpacedRepetitionService.
 */
@Service
@RequiredArgsConstructor
public class StudyService {

        private final FlashcardRepository flashcardRepository;
        private final FlashcardProgressRepository progressRepository;
        private final ReviewHistoryRepository reviewHistoryRepository;
        private final UserRepository userRepository;
        private final DeckRepository deckRepository;
        private final SpacedRepetitionService srsService;

        /**
         * Lấy thẻ học tiếp theo trong bộ thẻ mà người dùng cần ôn tập.
         */
        public StudySessionDTO getNextCard(Integer deckId, Integer userId) {
                Deck deck = deckRepository.findById(deckId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy deck"));

                List<Flashcard> dueCards = flashcardRepository.findDueFlashcards(
                                deckId, userId, LocalDateTime.now(), FlashcardProgress.Status.MASTERED);

                if (dueCards.isEmpty()) {
                        return null; // Hoàn thành session
                }

                Flashcard card = dueCards.get(0);
                int remaining = dueCards.size();

                return StudySessionDTO.builder()
                                .deckId(deckId)
                                .deckTitle(deck.getTitle())
                                .flashcardId(card.getFlashcardId())
                                .frontContent(card.getFrontContent())
                                .backContent(card.getBackContent())
                                .exampleSentence(card.getExampleSentence())
                                .pronunciation(card.getPronunciation())
                                .imageUrl(card.getImageUrl())
                                .audioUrl(card.getAudioUrl())
                                .remainingCards(remaining)
                                .totalDue(remaining)
                                .showAnswer(false)
                                .build();
        }

        /**
         * Ghi nhận kết quả phản hồi của người dùng cho một thẻ.
         * Cập nhật tiến trình (Progress) bằng thuật toán SRS và lưu lịch sử.
         */
        @Transactional
        public void submitReview(Integer flashcardId, Integer userId,
                        ReviewHistory.ReviewResult result) {

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
                Flashcard card = flashcardRepository.findById(flashcardId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy flashcard"));

                // Lấy hoặc tạo mới progress
                FlashcardProgress progress = progressRepository
                                .findByUser_UserIdAndFlashcard_FlashcardId(userId, flashcardId)
                                .orElseGet(() -> createNewProgress(user, card));

                // Áp dụng thuật toán SRS
                srsService.applyReview(progress, result);
                progressRepository.save(progress);

                // Lưu lịch sử
                ReviewHistory history = ReviewHistory.builder()
                                .user(user)
                                .flashcard(card)
                                .reviewResult(result)
                                .reviewedAt(LocalDateTime.now())
                                .build();
                reviewHistoryRepository.save(history);
        }

        private FlashcardProgress createNewProgress(User user, Flashcard card) {
                return FlashcardProgress.builder()
                                .user(user)
                                .flashcard(card)
                                .reviewCount(0)
                                .correctCount(0)
                                .wrongCount(0)
                                .masteryLevel(0)
                                .easeFactor(new BigDecimal("2.50"))
                                .intervalDays(1)
                                .status(FlashcardProgress.Status.NEW)
                                .build();
        }

        /**
         * Lấy nhãn dự kiến cho từng nút phản hồi (ví dụ: "<10m", "3d", "5d").
         */
        public String[] getNextReviewLabels(Integer flashcardId, Integer userId) {
                FlashcardProgress progress = progressRepository
                                .findByUser_UserIdAndFlashcard_FlashcardId(userId, flashcardId)
                                .orElseGet(() -> {
                                        FlashcardProgress p = new FlashcardProgress();
                                        p.setIntervalDays(1);
                                        p.setEaseFactor(new BigDecimal("2.50"));
                                        return p;
                                });

                return new String[] {
                                srsService.getNextReviewLabel(progress, ReviewHistory.ReviewResult.again),
                                srsService.getNextReviewLabel(progress, ReviewHistory.ReviewResult.hard),
                                srsService.getNextReviewLabel(progress, ReviewHistory.ReviewResult.good),
                                srsService.getNextReviewLabel(progress, ReviewHistory.ReviewResult.easy)
                };
        }

        /** Lấy chi tiết nội dung của thẻ đang học. */
        public StudySessionDTO getCardDetail(Integer deckId, Integer flashcardId, Integer userId) {
                Deck deck = deckRepository.findById(deckId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy deck"));

                Flashcard card = flashcardRepository.findById(flashcardId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy flashcard"));

                // Tính số card còn lại
                List<Flashcard> dueCards = flashcardRepository.findDueFlashcards(
                                deckId, userId, LocalDateTime.now(), FlashcardProgress.Status.MASTERED);
                int remaining = dueCards.size();

                return StudySessionDTO.builder()
                                .deckId(deckId)
                                .deckTitle(deck.getTitle())
                                .flashcardId(card.getFlashcardId())
                                .frontContent(card.getFrontContent())
                                .backContent(card.getBackContent())
                                .exampleSentence(card.getExampleSentence())
                                .pronunciation(card.getPronunciation())
                                .imageUrl(card.getImageUrl())
                                .audioUrl(card.getAudioUrl())
                                .remainingCards(remaining)
                                .totalDue(remaining)
                                .showAnswer(false)
                                .build();
        }

        /**
         * Đặt lại (Reset) toàn bộ tiến trình của các thẻ trong bộ thẻ.
         */
        @Transactional
        public void resetDeckForReview(Integer deckId, Integer userId) {
                deckRepository.findById(deckId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy deck"));
                progressRepository.resetDeckProgress(deckId, userId, LocalDateTime.now());
        }
}

