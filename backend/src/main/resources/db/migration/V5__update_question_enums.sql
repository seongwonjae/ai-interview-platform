-- 1) 기존 데이터 소문자 -> 대문자 변환
UPDATE interview_questions
SET role = UPPER(role),
    difficulty = UPPER(difficulty);

-- 2) 컬럼 enum 정의도 대문자 버전으로 변경
ALTER TABLE interview_questions
  MODIFY COLUMN role ENUM('BACKEND','FRONTEND','DATA','PM') NOT NULL,
  MODIFY COLUMN difficulty ENUM('EASY','MEDIUM','HARD') NOT NULL;