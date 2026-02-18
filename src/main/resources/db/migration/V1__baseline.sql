CREATE TABLE IF NOT EXISTS `member` (
  `_id` BIGINT NOT NULL AUTO_INCREMENT,
  `member_uuid` BINARY(16),
  `name` VARCHAR(255),
  `email` VARCHAR(255),
  `status` ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  `created_utc` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_utc` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`_id`),
  UNIQUE KEY `uk_member_email` (`email`)
);

CREATE TABLE IF NOT EXISTS `book` (
  `_id` BIGINT NOT NULL AUTO_INCREMENT,
  `book_uuid` BINARY(16),
  `title` VARCHAR(255),
  `author` VARCHAR(255),
  `isbn` VARCHAR(255),
  `total_copies` BIGINT,
  `available_copies` BIGINT,
  `created_utc` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_utc` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`_id`),
  UNIQUE KEY `uk_book_isbn` (`isbn`)
);

CREATE TABLE IF NOT EXISTS `loan` (
  `_id` BIGINT NOT NULL AUTO_INCREMENT,
  `loan_uuid` BINARY(16),
  `status` ENUM('ACTIVE', 'COMPLETED', 'COMPLETED_LATE', 'LOST') NOT NULL DEFAULT 'ACTIVE',
  `book_id` BIGINT,
  `member_id` BIGINT,
  `borrowed_utc` DATETIME(6),
  `due_utc` DATETIME(6),
  `completed_utc` DATETIME(6),
  `created_utc` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_utc` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`_id`),
  KEY `idx_loan_book_id` (`book_id`),
  KEY `idx_loan_member_id` (`member_id`),
  CONSTRAINT `fk_loan_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`_id`),
  CONSTRAINT `fk_loan_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`_id`)
);
