CREATE DATABASE IF NOT EXISTS `english_learning_flashcard` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `english_learning_flashcard`;

-- Xóa các bảng cũ nếu tồn tại (để reset)
DROP TABLE IF EXISTS `deck_ratings`;
DROP TABLE IF EXISTS `review_history`;
DROP TABLE IF EXISTS `flashcard_progress`;
DROP TABLE IF EXISTS `user_decks`;
DROP TABLE IF EXISTS `flashcards`;
DROP TABLE IF EXISTS `decks`;
DROP TABLE IF EXISTS `users`;

-- 1. Bảng users
CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password_hash` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` enum('admin','super_admin','user','vip') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `vip_expires_at` date DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `UK_6dotkpttghp5g2b0q51d6981o` (`email`),
  UNIQUE KEY `UK_r43af9ap4edm43mmtq01oddj6` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Bảng decks
CREATE TABLE `decks` (
  `deck_id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `status` enum('APPROVED','PENDING','PRIVATE','REJECTED','VIP_ONLY') COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `view_count` bigint NOT NULL,
  `created_by` int NOT NULL,
  PRIMARY KEY (`deck_id`),
  KEY `FKnt0qys2y9r2u7c70v27k7r8ch` (`created_by`),
  CONSTRAINT `FKnt0qys2y9r2u7c70v27k7r8ch` FOREIGN KEY (`created_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Bảng flashcards
CREATE TABLE `flashcards` (
  `flashcard_id` int NOT NULL AUTO_INCREMENT,
  `back_content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `example_sentence` text COLLATE utf8mb4_unicode_ci,
  `front_content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `pronunciation` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deck_id` int NOT NULL,
  PRIMARY KEY (`flashcard_id`),
  KEY `FKn81hsqx8d27376t2aivc66g4q` (`deck_id`),
  CONSTRAINT `FKn81hsqx8d27376t2aivc66g4q` FOREIGN KEY (`deck_id`) REFERENCES `decks` (`deck_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Bảng flashcard_progress
CREATE TABLE `flashcard_progress` (
  `progress_id` int NOT NULL AUTO_INCREMENT,
  `correct_count` int DEFAULT NULL,
  `ease_factor` decimal(5,2) DEFAULT NULL,
  `interval_days` int DEFAULT NULL,
  `last_review` datetime(6) DEFAULT NULL,
  `mastery_level` int DEFAULT NULL,
  `next_review` datetime(6) DEFAULT NULL,
  `review_count` int DEFAULT NULL,
  `status` enum('learning','mastered','new','review') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `wrong_count` int DEFAULT NULL,
  `flashcard_id` int NOT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`progress_id`),
  KEY `FKnsqv04400e9iifp7p204lmtv5` (`flashcard_id`),
  KEY `FKt19318889xeq12w0s7hcdlcyb` (`user_id`),
  CONSTRAINT `FKnsqv04400e9iifp7p204lmtv5` FOREIGN KEY (`flashcard_id`) REFERENCES `flashcards` (`flashcard_id`),
  CONSTRAINT `FKt19318889xeq12w0s7hcdlcyb` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Bảng review_history
CREATE TABLE `review_history` (
  `review_id` int NOT NULL AUTO_INCREMENT,
  `review_result` enum('again','easy','good','hard') COLLATE utf8mb4_unicode_ci NOT NULL,
  `reviewed_at` datetime(6) DEFAULT NULL,
  `flashcard_id` int NOT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`review_id`),
  KEY `FKe3210v1x087k5589w89p7p374` (`flashcard_id`),
  KEY `FKn12d9c05e1j2l9g6w9w1y3x19` (`user_id`),
  CONSTRAINT `FKe3210v1x087k5589w89p7p374` FOREIGN KEY (`flashcard_id`) REFERENCES `flashcards` (`flashcard_id`),
  CONSTRAINT `FKn12d9c05e1j2l9g6w9w1y3x19` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Bảng deck_ratings
CREATE TABLE `deck_ratings` (
  `rating_id` int NOT NULL AUTO_INCREMENT,
  `comment` text COLLATE utf8mb4_unicode_ci,
  `created_at` datetime(6) DEFAULT NULL,
  `stars` int NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deck_id` int NOT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`rating_id`),
  UNIQUE KEY `UK9a91y718j84g0p98c1y7u70m1` (`deck_id`,`user_id`),
  KEY `FK5v7v28p1k0846v4x1y1c7s7c1` (`user_id`),
  CONSTRAINT `FK5v7v28p1k0846v4x1y1c7s7c1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `FKo62f2k9e9n6c4r0h1c5u2h6i7` FOREIGN KEY (`deck_id`) REFERENCES `decks` (`deck_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Bảng user_decks
CREATE TABLE `user_decks` (
  `user_deck_id` int NOT NULL AUTO_INCREMENT,
  `saved_at` datetime(6) DEFAULT NULL,
  `deck_id` int NOT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`user_deck_id`),
  KEY `FKg2t8d61y5h3t0m8a0c2s5s5s5` (`deck_id`),
  KEY `FKb1n9v6p3x8j1v8n3s7v6v6v6v` (`user_id`),
  CONSTRAINT `FKb1n9v6p3x8j1v8n3s7v6v6v6v` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `FKg2t8d61y5h3t0m8a0c2s5s5s5` FOREIGN KEY (`deck_id`) REFERENCES `decks` (`deck_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ==========================================
-- INSERT DỮ LIỆU MẪU
-- (Mật khẩu của tất cả các tài khoản đều là: 123456)
-- Hash: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIvi
-- ==========================================

INSERT INTO `users` (`user_id`, `created_at`, `email`, `password_hash`, `role`, `username`, `vip_expires_at`) VALUES
(1, NOW(), 'admin@ankard.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIvi', 'super_admin', 'admin', NULL),
(2, NOW(), 'vip@ankard.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIvi', 'vip', 'vipuser', '2027-01-01'),
(3, NOW(), 'user@ankard.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIvi', 'user', 'user', NULL);

-- Thêm 2 bộ thẻ mẫu
INSERT INTO `decks` (`deck_id`, `created_at`, `description`, `status`, `title`, `updated_at`, `view_count`, `created_by`) VALUES
(1, NOW(), 'Bộ thẻ tiếng Anh cơ bản cho người mới bắt đầu.', 'APPROVED', 'Tiếng Anh Cơ Bản', NOW(), 125, 1),
(2, NOW(), 'Bộ từ vựng IELTS chuyên sâu chỉ dành cho thành viên VIP.', 'VIP_ONLY', 'IELTS Advanced Vocab', NOW(), 34, 1);

-- Thêm thẻ cho deck 1
INSERT INTO `flashcards` (`flashcard_id`, `back_content`, `created_at`, `example_sentence`, `front_content`, `pronunciation`, `updated_at`, `deck_id`) VALUES
(1, 'Xin chào', NOW(), 'Hello, how are you?', 'Hello', '/həˈləʊ/', NOW(), 1),
(2, 'Thế giới', NOW(), 'The world is big.', 'World', '/wɜːld/', NOW(), 1),
(3, 'Quả táo', NOW(), 'I eat an apple.', 'Apple', '/ˈæpl/', NOW(), 1);

-- Thêm thẻ cho deck 2 (VIP)
INSERT INTO `flashcards` (`flashcard_id`, `back_content`, `created_at`, `example_sentence`, `front_content`, `pronunciation`, `updated_at`, `deck_id`) VALUES
(4, 'Phổ biến, ở khắp mọi nơi', NOW(), 'Computers are ubiquitous in modern society.', 'Ubiquitous', '/juːˈbɪkwɪtəs/', NOW(), 2),
(5, 'Ngắn gọn, súc tích', NOW(), 'Please keep your summary ephemeral.', 'Ephemeral', '/ɪˈfemərəl/', NOW(), 2);

-- Thêm một vài đánh giá mẫu
INSERT INTO `deck_ratings` (`rating_id`, `comment`, `created_at`, `stars`, `updated_at`, `deck_id`, `user_id`) VALUES
(1, 'Bộ thẻ rất hay và dễ học!', NOW(), 5, NOW(), 1, 2),
(2, 'Từ vựng khá căn bản.', NOW(), 4, NOW(), 1, 3),
(3, 'Chất lượng VIP tuyệt vời.', NOW(), 5, NOW(), 2, 2);
