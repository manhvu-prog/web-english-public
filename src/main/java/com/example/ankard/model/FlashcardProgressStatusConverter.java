package com.example.ankard.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Bộ chuyển đổi (Converter) dùng để ánh xạ Enum Status trong Java sang String trong Database và ngược lại.
 * Giúp lưu trữ các giá trị thân thiện như "new", "learning" vào cột status thay vì số thứ tự.
 */
@Converter(autoApply = false)
public class FlashcardProgressStatusConverter implements AttributeConverter<FlashcardProgress.Status, String> {

    /**
     * Chuyển từ Enum sang String để lưu vào DB.
     */
    @Override
    public String convertToDatabaseColumn(FlashcardProgress.Status attribute) {
        if (attribute == null) return null;
        return attribute.getValue(); // Trả về "new", "learning",...
    }

    /**
     * Chuyển từ String trong DB ngược lại thành Enum trong Java.
     */
    @Override
    public FlashcardProgress.Status convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        String normalized = dbData.trim().toLowerCase();
        for (FlashcardProgress.Status s : FlashcardProgress.Status.values()) {
            if (s.getValue().equals(normalized)) return s;
            if (s.name().equalsIgnoreCase(normalized)) return s; // Hỗ trợ cả trường hợp viết hoa "LEARNING"
        }
        throw new IllegalArgumentException("Giá trị trạng thái không hợp lệ: " + dbData);
    }
}
