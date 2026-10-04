package com.example.ankard.service;

import com.example.ankard.model.Deck;
import com.example.ankard.model.DeckStatus;
import com.example.ankard.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final UserRepository userRepository;
    private final DeckRepository deckRepository;
    private final FlashcardRepository flashcardRepository;
    private final FlashcardProgressRepository progressRepository;
    private final ReviewHistoryRepository reviewHistoryRepository;

    // ========== ADMIN STATS ==========

    public Map<String, Object> getAdminStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        // --- Người dùng ---
        long totalUsers    = userRepository.count();
        long regularUsers  = userRepository.countRegularUsers();
        long vipUsers      = userRepository.countVipUsers();
        long adminUsers    = userRepository.countAdminUsers();
        stats.put("totalUsers",   totalUsers);
        stats.put("regularUsers", regularUsers);
        stats.put("vipUsers",     vipUsers);
        stats.put("adminUsers",   adminUsers);

        // --- Deck ---
        long totalDecks    = deckRepository.count();
        long approvedDecks = deckRepository.countByStatus(DeckStatus.APPROVED);
        long vipDecks      = deckRepository.countByStatus(DeckStatus.VIP_ONLY);
        long pendingDecks  = deckRepository.countByStatus(DeckStatus.PENDING);
        long rejectedDecks = deckRepository.countByStatus(DeckStatus.REJECTED);
        long privateDecks  = deckRepository.countByStatus(DeckStatus.PRIVATE);
        stats.put("totalDecks",    totalDecks);
        stats.put("approvedDecks", approvedDecks);
        stats.put("vipDecks",      vipDecks);
        stats.put("pendingDecks",  pendingDecks);
        stats.put("rejectedDecks", rejectedDecks);
        stats.put("privateDecks",  privateDecks);

        // --- Flashcard ---
        long totalFlashcards = flashcardRepository.count();
        stats.put("totalFlashcards", totalFlashcards);

        // --- Lượt xem ---
        List<DeckStatus> publicStatuses = List.of(DeckStatus.APPROVED, DeckStatus.VIP_ONLY);
        long totalViews = deckRepository.sumViewCountByStatuses(publicStatuses);
        stats.put("totalViews", totalViews);

        // --- Lượt ôn ---
        long totalReviews = progressRepository.sumTotalReviews();
        stats.put("totalReviews", totalReviews);

        // --- Hoạt động 7 ngày qua ---
        LocalDateTime since7Days = LocalDateTime.now().minusDays(7);
        long recentActivity = reviewHistoryRepository.countSince(since7Days);
        stats.put("recentActivity", recentActivity);

        // --- Top 5 deck xem nhiều nhất ---
        List<Deck> topDecks = deckRepository.findTopByViewCount(publicStatuses, PageRequest.of(0, 5));
        stats.put("topDecks", topDecks);

        return stats;
    }

    // ========== USER PERSONAL STATS ==========

    public Map<String, Object> getUserStats(Integer userId) {
        Map<String, Object> stats = new LinkedHashMap<>();

        // --- Deck ---
        long myDecks = deckRepository.countByCreatedBy(userId);
        stats.put("myDecks", myDecks);

        // --- Flashcard đã học ---
        long learnedCards  = progressRepository.countByUser(userId);
        long masteredCards = progressRepository.countMasteredByUser(userId);
        stats.put("learnedCards",  learnedCards);
        stats.put("masteredCards", masteredCards);

        // --- Lượt ôn ---
        long totalReviews   = progressRepository.sumReviewsByUser(userId);
        long correctAnswers = progressRepository.sumCorrectByUser(userId);
        long wrongAnswers   = progressRepository.sumWrongByUser(userId);
        stats.put("totalReviews",   totalReviews);
        stats.put("correctAnswers", correctAnswers);
        stats.put("wrongAnswers",   wrongAnswers);

        // Tỉ lệ đúng (%)
        long accuracyPct = (totalReviews > 0)
                ? Math.round(100.0 * correctAnswers / totalReviews)
                : 0;
        stats.put("accuracyPct", accuracyPct);

        // --- Hôm nay ---
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long todayReviews = reviewHistoryRepository.countTodayByUser(userId, startOfDay);
        stats.put("todayReviews", todayReviews);

        // --- Biểu đồ 7 ngày (labels + data) ---
        LocalDateTime since7Days = LocalDateTime.now().minusDays(6).toLocalDate().atStartOfDay();
        List<Object[]> dailyRaw = reviewHistoryRepository.countDailyReviewsByUser(userId, since7Days);

        // Tạo map date -> count
        Map<String, Long> dailyMap = new LinkedHashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
        for (int i = 6; i >= 0; i--) {
            String label = LocalDate.now().minusDays(i).format(fmt);
            dailyMap.put(label, 0L);
        }
        for (Object[] row : dailyRaw) {
            // row[0] = date, row[1] = count
            if (row[0] != null) {
                String dateStr;
                if (row[0] instanceof java.sql.Date sqlDate) {
                    dateStr = sqlDate.toLocalDate().format(fmt);
                } else {
                    dateStr = row[0].toString().substring(0, 10); // yyyy-MM-dd
                    try {
                        dateStr = LocalDate.parse(dateStr).format(fmt);
                    } catch (Exception ignored) {}
                }
                if (dailyMap.containsKey(dateStr)) {
                    dailyMap.put(dateStr, ((Number) row[1]).longValue());
                }
            }
        }

        stats.put("chartLabels", new ArrayList<>(dailyMap.keySet()));
        stats.put("chartData",   new ArrayList<>(dailyMap.values()));

        return stats;
    }
}
