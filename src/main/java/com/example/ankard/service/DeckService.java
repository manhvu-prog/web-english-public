package com.example.ankard.service;

import com.example.ankard.dto.DeckFormDTO;
import com.example.ankard.dto.DeckSummaryDTO;
import com.example.ankard.model.Deck;
import com.example.ankard.model.DeckStatus;
import com.example.ankard.model.Flashcard;
import com.example.ankard.model.User;
import com.example.ankard.model.UserDeck;
import com.example.ankard.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Service xử lý các nghiệp vụ liên quan đến bộ thẻ (Deck).
 * Bao gồm: Quản lý deck cá nhân, duyệt deck công khai, import (copy) deck và tìm kiếm.
 */
@Service
@RequiredArgsConstructor
public class DeckService {

    private final DeckRepository deckRepository;
    private final UserRepository userRepository;
    private final UserDeckRepository userDeckRepository;
    private final FlashcardRepository flashcardRepository;
    private final FlashcardProgressRepository flashcardProgressRepository;

    // ========== TRANG CHỦ ==========

    /**
     * Lấy tất cả deck của user (do tạo + đã lưu), kèm thống kê số card
     */
    public List<DeckSummaryDTO> getDeckSummariesForUser(Integer userId) {
        List<Deck> decks = deckRepository.findAllDecksForUser(userId);
        return decks.stream()
                .map(deck -> buildDeckSummary(deck, userId))
                .collect(Collectors.toList());
    }

    /**
     * Chuyển đổi Entity Deck sang DTO DeckSummaryDTO để hiển thị.
     * Tính toán số lượng thẻ (mới, đang học, cần ôn) theo từng user.
     */
    private DeckSummaryDTO buildDeckSummary(Deck deck, Integer userId) {
        long total = flashcardRepository.countByDeck_DeckId(deck.getDeckId());
        long newCards = flashcardProgressRepository.countNewCards(deck.getDeckId(), userId);
        long learning = flashcardProgressRepository.countLearningCards(deck.getDeckId(), userId);
        long review = flashcardProgressRepository.countReviewCards(deck.getDeckId(), userId);

        return DeckSummaryDTO.builder()
                .deckId(deck.getDeckId())
                .title(deck.getTitle())
                .description(deck.getDescription())
                .status(deck.getStatus())
                .totalCards(total)
                .newCards(newCards)
                .learningCards(learning)
                .reviewCards(review)
                .vipOnly(deck.getStatus() == DeckStatus.VIP_ONLY)
                .viewCount(deck.getViewCount())
                .build();
    }

    // ========== CRUD DECK ==========

    /**
     * Lấy thông tin chi tiết của một bộ thẻ theo ID.
     */
    public Deck getDeckById(Integer deckId) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy deck id=" + deckId));
        normalizeLegacyShareStatus(deck);
        return deck;
    }

    /**
     * Lấy thông tin bộ thẻ công khai (yêu cầu trạng thái APPROVED hoặc VIP_ONLY).
     */
    public Deck getPublicDeckById(Integer deckId) {
        Deck deck = getDeckById(deckId);
        if (deck.getStatus() != DeckStatus.APPROVED && deck.getStatus() != DeckStatus.VIP_ONLY) {
            throw new RuntimeException("Deck này không được chia sẻ công khai.");
        }
        return deck;
    }

    /** Tăng lượt xem của bộ thẻ. */
    @Transactional
    public void incrementViewCount(Integer deckId) {
        deckRepository.incrementViewCount(deckId);
    }

    /** Kiểm tra xem deck có đang trong trạng thái cho phép chỉnh sửa không (không phải PENDING). */
    public void ensureDeckEditable(Integer deckId) {
        Deck deck = getDeckById(deckId);
        assertDeckEditable(deck);
    }

    /** Tạo mới một bộ thẻ cá nhân. */
    @Transactional
    public Deck createDeck(DeckFormDTO form, Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user id=" + userId));

        Deck deck = Deck.builder()
                .title(form.getTitle())
                .description(form.getDescription())
                .status(DeckStatus.PRIVATE)
                .createdBy(user)
                .build();

        return deckRepository.save(deck);
    }

    /** Cập nhật tiêu đề và mô tả của bộ thẻ. */
    @Transactional
    public Deck updateDeck(Integer deckId, DeckFormDTO form) {
        Deck deck = getDeckById(deckId);
        assertDeckEditable(deck);
        deck.setTitle(form.getTitle());
        deck.setDescription(form.getDescription());
        return deckRepository.save(deck);
    }

    /** Xóa bộ thẻ. */
    @Transactional
    public void deleteDeck(Integer deckId) {
        Deck deck = getDeckById(deckId);
        assertDeckEditable(deck);
        deckRepository.delete(deck);
    }

    // ========== LƯU DECK CÔNG KHAI ==========

    /** Lưu một bộ thẻ công khai vào thư viện cá nhân (danh sách hiển thị ở trang chủ). */
    @Transactional
    public void saveDeckForUser(Integer deckId, Integer userId) {
        if (userDeckRepository.existsByUser_UserIdAndDeck_DeckId(userId, deckId)) {
            return; // Đã lưu rồi thì không làm gì cả
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
        Deck deck = getDeckById(deckId);

        UserDeck userDeck = UserDeck.builder()
                .user(user)
                .deck(deck)
                .isFavorite(false)
                .build();
        userDeckRepository.save(userDeck);
    }

    /**
     * "Nhân bản" (Import) một bộ thẻ công khai vào kho tài liệu của người dùng hiện tại.
     * 1. Tạo một bộ thẻ mới thuộc về User hiện tại, trạng thái mặc định là PRIVATE.
     * 2. Lấy toàn bộ Flashcards từ bộ thẻ gốc.
     * 3. Tạo các bản sao Flashcard mới trỏ vào bộ thẻ vừa tạo.
     * 4. Lưu tất cả vào Database.
     */
    @Transactional
    public Deck importPublicDeck(Integer sourceDeckId, Integer userId) {
        // Lấy thông tin bộ thẻ gốc
        Deck sourceDeck = getPublicDeckById(sourceDeckId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user id=" + userId));

        // 1. Tạo deck mới là bản sao metadata của deck gốc
        Deck newDeck = Deck.builder()
                .createdBy(user)
                .title(sourceDeck.getTitle())
                .description(sourceDeck.getDescription())
                .status(DeckStatus.PRIVATE)
                .build();

        newDeck = deckRepository.save(newDeck);

        // 2. Lấy danh sách card từ deck gốc
        List<Flashcard> sourceCards = flashcardRepository.findByDeck_DeckId(sourceDeckId);
        List<Flashcard> copies = new ArrayList<>(sourceCards.size());
        
        // 3. Tạo bản sao cho từng flashcard
        for (Flashcard c : sourceCards) {
            copies.add(Flashcard.builder()
                    .deck(newDeck) // Gán vào deck mới của user
                    .frontContent(c.getFrontContent())
                    .backContent(c.getBackContent())
                    .exampleSentence(c.getExampleSentence())
                    .pronunciation(c.getPronunciation())
                    .imageUrl(c.getImageUrl())
                    .audioUrl(c.getAudioUrl())
                    .build());
        }
        // 4. Lưu hàng loạt để tối ưu hiệu năng
        flashcardRepository.saveAll(copies);

        return newDeck;
    }

    // ========== DECK CÔNG KHAI (KHÁM PHÁ) ==========

    /**
     * Lấy danh sách bộ thẻ công khai (Khám phá cộng đồng).
     * Chỉ lấy các bộ thẻ có trạng thái APPROVED hoặc VIP_ONLY.
     */
    public List<DeckSummaryDTO> getPublicDecks(Integer userId, String sort) {
        List<DeckStatus> publicStatuses = Arrays.asList(DeckStatus.APPROVED, DeckStatus.VIP_ONLY);
        List<Deck> publicDecks = deckRepository.findByStatusIn(publicStatuses);
        
        // Mặc định DB đã sort theo viewCount DESC. 
        // Nếu chọn sắp xếp "mới nhất" (newest), thực hiện sort lại bằng Stream.
        if ("newest".equalsIgnoreCase(sort)) {
            publicDecks = publicDecks.stream()
                    .sorted((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()))
                    .collect(Collectors.toList());
        }
        
        return publicDecks.stream()
                .map(deck -> buildDeckSummary(deck, userId))
                .collect(Collectors.toList());
    }

    /**
     * Tìm kiếm bộ thẻ công khai theo từ khóa.
     * Quy trình tìm kiếm:
     * 1. Tìm trong DB bằng từ khóa gốc (với SQL LIKE).
     * 2. Nếu không thấy kết quả, thực hiện tìm kiếm "nới lỏng" bằng Java (chuẩn hóa bỏ dấu tiếng Việt).
     */
    public List<DeckSummaryDTO> searchPublicDecks(Integer userId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getPublicDecks(userId, "views");
        }
        String query = keyword.trim();
        List<DeckStatus> publicStatuses = Arrays.asList(DeckStatus.APPROVED, DeckStatus.VIP_ONLY);

        // Bước 1: Tìm kiếm chính xác/gần đúng trong Database (nhanh)
        List<Deck> publicDecks = deckRepository.searchDecksByStatusInAndKeyword(publicStatuses, query);

        // Bước 2: Tìm kiếm "thông minh" (bỏ qua dấu tiếng Việt) nếu bước 1 không ra kết quả
        if (publicDecks.isEmpty()) {
            String normalizedQuery = normalize(query);
            publicDecks = deckRepository.findByStatusIn(publicStatuses).stream()
                    .filter(deck -> containsIgnoreCaseAndAccent(deck.getTitle(), query, normalizedQuery)
                            || containsIgnoreCaseAndAccent(deck.getDescription(), query, normalizedQuery))
                    .toList();
        }

        // Sắp xếp kết quả tìm kiếm theo lượt xem giảm dần (Trending)
        return publicDecks.stream()
                .sorted((a, b) -> Long.compare(b.getViewCount(), a.getViewCount()))
                .map(deck -> buildDeckSummary(deck, userId))
                .collect(Collectors.toList());
    }

    private boolean containsIgnoreCaseAndAccent(String value, String query, String normalizedQuery) {
        if (value == null) {
            return false;
        }
        String lowerValue = value.toLowerCase();
        if (lowerValue.contains(query.toLowerCase())) {
            return true;
        }
        String normalizedValue = normalize(value);
        return normalizedValue.contains(normalizedQuery);
    }

    /**
     * Chuẩn hóa chuỗi văn bản: Chuyển về chữ thường và loại bỏ tất cả các dấu tiếng Việt.
     * Ví dụ: "Tiếng Anh" -> "tieng anh"
     */
    private String normalize(String input) {
        if (input == null) {
            return "";
        }
        // Normalizer tách "ế" thành "e" và dấu sắc. \\p{M} sẽ khớp và thay thế dấu đó bằng rỗng.
        String normalized = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "").toLowerCase();
    }

    /** Ẩn một bộ thẻ công khai (dành cho Admin khi có vi phạm). */
    @Transactional
    public void hidePublicDeck(Integer deckId) {
        Deck deck = getDeckById(deckId);
        deck.setStatus(DeckStatus.REJECTED);
        deckRepository.save(deck);
    }

    /** Gửi yêu cầu duyệt để công khai bộ thẻ lên cộng đồng. */
    @Transactional
    public void submitDeckForReview(Integer deckId, Integer userId) {
        Deck deck = getDeckById(deckId);
        if (!deck.getCreatedBy().getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền gửi duyệt deck này.");
        }
        if (deck.getStatus() == DeckStatus.PENDING) {
            throw new RuntimeException("Deck này đang chờ admin duyệt.");
        }
        if (deck.getStatus() == DeckStatus.APPROVED) {
            throw new RuntimeException("Deck này đã được duyệt công khai.");
        }
        deck.setStatus(DeckStatus.PENDING);
        deckRepository.save(deck);
    }

    /** Hủy yêu cầu duyệt khi đang ở trạng thái PENDING. */
    @Transactional
    public void cancelDeckReview(Integer deckId, Integer userId) {
        Deck deck = getDeckById(deckId);
        assertDeckOwner(deck, userId);
        if (deck.getStatus() != DeckStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể hủy khi deck đang chờ admin duyệt.");
        }
        deck.setStatus(DeckStatus.PRIVATE);
        deckRepository.save(deck);
    }

    /** Ngừng chia sẻ một bộ thẻ đã được duyệt (Chuyển về PRIVATE). */
    @Transactional
    public void stopSharingDeck(Integer deckId, Integer userId) {
        Deck deck = getDeckById(deckId);
        assertDeckOwner(deck, userId);
        if (deck.getStatus() != DeckStatus.APPROVED) {
            throw new RuntimeException("Chỉ có thể ngừng chia sẻ khi deck đang public.");
        }
        deck.setStatus(DeckStatus.PRIVATE);
        deckRepository.save(deck);
    }

    /** Lấy danh sách các bộ thẻ đang chờ được duyệt (Admin). */
    public List<Deck> getPendingDecks() {
        return deckRepository.findByStatus(DeckStatus.PENDING);
    }

    /** Chấp nhận (Duyệt) một bộ thẻ được chia sẻ. */
    @Transactional
    public void approveDeck(Integer deckId) {
        Deck deck = getDeckById(deckId);
        deck.setStatus(DeckStatus.APPROVED);
        deckRepository.save(deck);
    }

    /** Từ chối duyệt một bộ thẻ. */
    @Transactional
    public void rejectDeck(Integer deckId) {
        Deck deck = getDeckById(deckId);
        deck.setStatus(DeckStatus.REJECTED);
        deckRepository.save(deck);
    }

    /** Ràng buộc: Không cho phép sửa deck khi đang chờ duyệt. */
    private void assertDeckEditable(Deck deck) {
        if (deck.getStatus() == DeckStatus.PENDING) {
            throw new RuntimeException("Deck đang chờ duyệt, bạn không thể chỉnh sửa lúc này.");
        }
    }

    // ========== QUẢN LÝ VIP DECK (ADMIN) ==========

    /** Lấy danh sách các bộ thẻ dành riêng cho VIP. */
    public List<Deck> getVipDecks() {
        return deckRepository.findByStatus(DeckStatus.VIP_ONLY);
    }

    /** Lấy tất cả các bộ thẻ hiển thị được ở cộng đồng (APPROVED + VIP_ONLY). */
    public List<Deck> getAllPublicAndVipDecks() {
        return deckRepository.findByStatusIn(Arrays.asList(DeckStatus.APPROVED, DeckStatus.VIP_ONLY));
    }

    /** Đánh dấu bộ thẻ là nội dung VIP. */
    @Transactional
    public void markDeckAsVip(Integer deckId) {
        Deck deck = getDeckById(deckId);
        if (deck.getStatus() != DeckStatus.APPROVED && deck.getStatus() != DeckStatus.VIP_ONLY) {
            throw new RuntimeException("Chỉ có thể đánh dấu VIP cho deck đã được duyệt công khai.");
        }
        deck.setStatus(DeckStatus.VIP_ONLY);
        deckRepository.save(deck);
    }

    /** Gỡ bỏ trạng thái VIP, quay về trạng thái công khai thường. */
    @Transactional
    public void unmarkDeckVip(Integer deckId) {
        Deck deck = getDeckById(deckId);
        if (deck.getStatus() != DeckStatus.VIP_ONLY) {
            throw new RuntimeException("Deck này chưa phải VIP.");
        }
        deck.setStatus(DeckStatus.APPROVED);
        deckRepository.save(deck);
    }

    /** Kiểm tra xem user có phải chủ sở hữu của deck không. */
    private void assertDeckOwner(Deck deck, Integer userId) {
        if (!deck.getCreatedBy().getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền thực hiện hành động này.");
        }
    }

    /**
     * Đồng bộ hóa các trạng thái cũ từ phiên bản trước (Legacy).
     * Rule: User có quyền hủy chia sẻ ngay lập tức mà không cần admin duyệt.
     */
    @Transactional
    protected void normalizeLegacyShareStatus(Deck deck) {
        if (deck.getStatus() == DeckStatus.UNSHARE_PENDING || deck.getStatus() == DeckStatus.UNSHARE_PENDI) {
            deck.setStatus(DeckStatus.PRIVATE);
            deckRepository.save(deck);
        }
    }
}
