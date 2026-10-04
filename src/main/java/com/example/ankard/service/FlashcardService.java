package com.example.ankard.service;

import com.example.ankard.dto.FlashcardFormDTO;
import com.example.ankard.model.Deck;
import com.example.ankard.model.DeckStatus;
import com.example.ankard.model.Flashcard;
import com.example.ankard.repository.DeckRepository;
import com.example.ankard.repository.FlashcardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service xử lý các nghiệp vụ liên quan đến quản lý thẻ học (Flashcard).
 * Bao gồm: Thêm, sửa, xóa và truy vấn nội dung thẻ.
 */
@Service
@RequiredArgsConstructor
public class FlashcardService {

    private final FlashcardRepository flashcardRepository;
    private final DeckRepository deckRepository;

    /** Lấy danh sách thẻ học thuộc một bộ thẻ. */
    public List<Flashcard> getFlashcardsByDeck(Integer deckId) {
        return flashcardRepository.findByDeck_DeckId(deckId);
    }

    /** Lấy thông tin chi tiết một thẻ học theo ID. */
    public Flashcard getFlashcardById(Integer flashcardId) {
        return flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy flashcard id=" + flashcardId));
    }

    /** 
     * Tạo mới một thẻ học trong một bộ thẻ.
     * Cho phép thêm thẻ ngay cả khi bộ thẻ đang chờ admin duyệt.
     */
    @Transactional
    public Flashcard createFlashcard(Integer deckId, FlashcardFormDTO form) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy deck id=" + deckId));

        Flashcard card = Flashcard.builder()
                .deck(deck)
                .frontContent(form.getFrontContent())
                .backContent(form.getBackContent())
                .exampleSentence(form.getExampleSentence())
                .pronunciation(form.getPronunciation())
                .imageUrl(form.getImageUrl())
                .audioUrl(form.getAudioUrl())
                .build();

        return flashcardRepository.save(card);
    }

    /** Cập nhật nội dung thẻ học. */
    @Transactional
    public Flashcard updateFlashcard(Integer flashcardId, FlashcardFormDTO form) {
        Flashcard card = getFlashcardById(flashcardId);
        // Ràng buộc: Không được sửa card nếu deck đang ở trạng thái PENDING
        assertDeckEditableForUpdateOrDelete(card.getDeck());
        
        card.setFrontContent(form.getFrontContent());
        card.setBackContent(form.getBackContent());
        card.setExampleSentence(form.getExampleSentence());
        card.setPronunciation(form.getPronunciation());
        card.setImageUrl(form.getImageUrl());
        card.setAudioUrl(form.getAudioUrl());
        return flashcardRepository.save(card);
    }

    /** Xóa thẻ học. */
    @Transactional
    public void deleteFlashcard(Integer flashcardId) {
        Flashcard card = getFlashcardById(flashcardId);
        // Ràng buộc: Không được xóa card nếu deck đang ở trạng thái PENDING
        assertDeckEditableForUpdateOrDelete(card.getDeck());
        flashcardRepository.delete(card);
    }

    /** Kiểm tra quyền chỉnh sửa dựa trên trạng thái của bộ thẻ. */
    private void assertDeckEditableForUpdateOrDelete(Deck deck) {
        if (deck.getStatus() == DeckStatus.PENDING) {
            throw new RuntimeException("Deck đang chờ duyệt, bạn không thể chỉnh sửa lúc này.");
        }
    }
}