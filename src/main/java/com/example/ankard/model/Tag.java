package com.example.ankard.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Thực thể đại diện cho nhãn (Tag) dùng để phân loại thẻ học.
 */
@Entity
@Table(name = "tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Integer tagId; // Khóa chính

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name; // Tên nhãn (ví dụ: "Vocabulary", "Grammar")
}
