package com.example.ankard.service;

import com.example.ankard.model.Deck;
import com.example.ankard.model.DeckRating;
import com.example.ankard.model.User;
import com.example.ankard.repository.DeckRatingRepository;
import com.example.ankard.repository.DeckRepository;
import com.example.ankard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service xử lý các nghiệp vụ liên quan đến việc đánh giá bộ thẻ (Rating).
 */
@Service
@RequiredArgsConstructor
public class RatingService {

    private final DeckRatingRepository ratingRepository;
    private final DeckRepository deckRepository;
    private final UserRepository userRepository;

    /**
     * Tổng hợp tất cả thông tin liên quan đến đánh giá của một bộ thẻ để hiển thị.
     * Bao gồm: danh sách bình luận, tổng số lượt, điểm trung bình, phân bố sao và đánh giá cá nhân của user.
     */
    public Map<String, Object> getDeckRatingInfo(Integer deckId, Integer currentUserId) {
        // 1. Lấy danh sách toàn bộ đánh giá
        List<DeckRating> ratings = ratingRepository.findByDeck_DeckIdOrderByCreatedAtDesc(deckId);
        // 2. Tính tổng số và điểm trung bình
        long totalRatings = ratingRepository.countByDeck_DeckId(deckId);
        double avgStars   = ratingRepository.avgStarsByDeckId(deckId);

        // 3. Xử lý phân bố sao (từ 5 sao về 1 sao)
        Map<Integer, Long> dist = new LinkedHashMap<>();
        for (int i = 5; i >= 1; i--) dist.put(i, 0L); // Khởi tạo mặc định bằng 0
        for (Object[] row : ratingRepository.starDistribution(deckId)) {
            dist.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }

        // 4. Lấy đánh giá của chính người đang xem (nếu đã đăng nhập) để hiển thị/chỉnh sửa
        DeckRating myRating = null;
        if (currentUserId != null) {
            myRating = ratingRepository.findByDeck_DeckIdAndUser_UserId(deckId, currentUserId).orElse(null);
        }

        // 5. Đóng gói dữ liệu vào Map để trả về cho Controller
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("ratings",      ratings);
        info.put("totalRatings", totalRatings);
        info.put("avgStars",     Math.round(avgStars * 10.0) / 10.0);  // Làm tròn đến 1 chữ số thập phân
        info.put("starDist",     dist);
        info.put("myRating",     myRating);
        return info;
    }

    /**
     * Thêm mới hoặc cập nhật đánh giá của người dùng.
     * Sử dụng cơ chế "Upsert": nếu đã có thì cập nhật, nếu chưa thì tạo mới.
     */
    @Transactional
    public DeckRating saveRating(Integer deckId, Integer userId, int stars, String comment) {
        // Kiểm tra tính hợp lệ của dữ liệu
        if (stars < 1 || stars > 5) throw new IllegalArgumentException("Số sao phải từ 1 đến 5.");

        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy deck."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user."));

        // Kiểm tra xem User này đã từng đánh giá Deck này chưa
        Optional<DeckRating> existing = ratingRepository.findByDeck_DeckIdAndUser_UserId(deckId, userId);
        DeckRating rating;
        if (existing.isPresent()) {
            // Trường hợp cập nhật đánh giá cũ
            rating = existing.get();
            rating.setStars(stars);
            rating.setComment(comment != null ? comment.trim() : null);
        } else {
            // Trường hợp tạo mới đánh giá
            rating = DeckRating.builder()
                    .deck(deck)
                    .user(user)
                    .stars(stars)
                    .comment(comment != null ? comment.trim() : null)
                    .build();
        }
        return ratingRepository.save(rating); // JPA tự động quyết định là INSERT hay UPDATE
    }

    /**
     * Xóa một đánh giá cụ thể.
     * Kiểm tra quyền: chỉ chủ nhân của đánh giá hoặc Admin mới có quyền xóa.
     */
    @Transactional
    public void deleteRating(Integer ratingId, Integer requestUserId, boolean isAdmin) {
        DeckRating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá."));
        
        // Kiểm tra quyền xóa
        if (!isAdmin && !rating.getUser().getUserId().equals(requestUserId)) {
            throw new RuntimeException("Bạn không có quyền xóa đánh giá này.");
        }
        ratingRepository.delete(rating);
    }
}
