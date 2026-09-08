CREATE DATABASE IF NOT EXISTS hopebridge CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hopebridge;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, email VARCHAR(160) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL, full_name VARCHAR(120) NOT NULL, phone VARCHAR(20),
  role VARCHAR(20) NOT NULL DEFAULT 'USER', created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS schemes (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(180) NOT NULL, category VARCHAR(80) NOT NULL,
  provider VARCHAR(120) NOT NULL, description TEXT NOT NULL, eligibility TEXT,
  official_url VARCHAR(500), active BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE IF NOT EXISTS wallets (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, user_id BIGINT NOT NULL UNIQUE, balance INT NOT NULL DEFAULT 0,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE TABLE IF NOT EXISTS cases (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, title VARCHAR(180) NOT NULL, category VARCHAR(80) NOT NULL,
  description TEXT NOT NULL, district VARCHAR(120), status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
  admin_note TEXT, applicant_id BIGINT NOT NULL, assigned_reviewer_id BIGINT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (applicant_id) REFERENCES users(id),
  FOREIGN KEY (assigned_reviewer_id) REFERENCES users(id)
);
CREATE TABLE IF NOT EXISTS evidence (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, case_file_id BIGINT NOT NULL, file_name VARCHAR(180) NOT NULL,
  content_type VARCHAR(100) NOT NULL, size_bytes BIGINT NOT NULL, content LONGBLOB NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING', review_note TEXT,
  requested_points INT NOT NULL DEFAULT 0, approved_points INT NULL, reviewed_by BIGINT NULL,
  reviewed_at TIMESTAMP NULL, uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (case_file_id) REFERENCES cases(id) ON DELETE CASCADE,
  FOREIGN KEY (reviewed_by) REFERENCES users(id)
);
CREATE TABLE IF NOT EXISTS reward_transactions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, user_id BIGINT NOT NULL, points INT NOT NULL,
  reason VARCHAR(180) NOT NULL, type VARCHAR(30) NOT NULL DEFAULT 'EVIDENCE_APPROVED',
  admin_id BIGINT NULL, case_file_id BIGINT NULL, evidence_id BIGINT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id), FOREIGN KEY (admin_id) REFERENCES users(id),
  FOREIGN KEY (case_file_id) REFERENCES cases(id), FOREIGN KEY (evidence_id) REFERENCES evidence(id)
);
CREATE TABLE IF NOT EXISTS audit_logs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, admin_id BIGINT NOT NULL, action VARCHAR(80) NOT NULL,
  entity_type VARCHAR(80) NOT NULL, entity_id BIGINT NOT NULL, user_id BIGINT NULL, points INT NULL, remarks TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (admin_id) REFERENCES users(id)
);

-- The API seed runner creates this account with a BCrypt password at startup:
-- admin@hopebridge.in / Admin@12345 (change it before production).
INSERT INTO schemes (name, category, provider, description, eligibility, official_url)
SELECT 'PM-KISAN Samman Nidhi','Agriculture','Government of India','Income support for eligible landholding farmer families.','Small and marginal farmers with cultivable land.','https://pmkisan.gov.in'
WHERE NOT EXISTS (SELECT 1 FROM schemes WHERE name='PM-KISAN Samman Nidhi');
