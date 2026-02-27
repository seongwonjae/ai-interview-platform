CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_settings (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  role ENUM('backend','frontend','data','pm') NOT NULL,
  difficulty ENUM('easy','medium','hard') NOT NULL,
  language ENUM('ko','en') NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_user_settings_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE interview_questions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  role ENUM('backend','frontend','data','pm') NOT NULL,
  difficulty ENUM('easy','medium','hard') NOT NULL,
  category VARCHAR(50) NOT NULL,
  text TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE interview_sessions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  role ENUM('backend','frontend','data','pm') NOT NULL,
  difficulty ENUM('easy','medium','hard') NOT NULL,
  language ENUM('ko','en') NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_sessions_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE interview_submissions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  session_id BIGINT NOT NULL,
  question_id BIGINT NOT NULL,
  answer_text TEXT NOT NULL,
  status ENUM('pending','processing','done','failed') NOT NULL DEFAULT 'pending',
  error_message TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_submissions_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_submissions_session FOREIGN KEY (session_id) REFERENCES interview_sessions(id),
  CONSTRAINT fk_submissions_question FOREIGN KEY (question_id) REFERENCES interview_questions(id)
);

CREATE TABLE ai_evaluations (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  submission_id BIGINT NOT NULL UNIQUE,
  prompt_version VARCHAR(20) NOT NULL,
  overall_score INT NOT NULL,
  score_structure INT NOT NULL,
  score_clarity INT NOT NULL,
  score_relevance INT NOT NULL,
  strengths_json JSON NOT NULL,
  improvements_json JSON NOT NULL,
  rewritten_answer TEXT NOT NULL,
  raw_response_json JSON NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_eval_submission FOREIGN KEY (submission_id) REFERENCES interview_submissions(id)
);

-- seed questions (MVP용 몇 개)
INSERT INTO interview_questions(role, difficulty, category, text) VALUES
('BACKEND','EASY','CS','트랜잭션이란 무엇인가요?'),
('BACKEND','MEDIUM','DB','ACID의 각 요소를 설명해보세요.'),
('BACKEND','HARD','ARCH','MSA에서 분산 트랜잭션을 어떻게 다룰 수 있나요?'),
('FRONTEND','EASY','JS','클로저(closure)란 무엇인가요?'),
('DATA','MEDIUM','SQL','인덱스가 성능에 미치는 영향과 주의점은?');
