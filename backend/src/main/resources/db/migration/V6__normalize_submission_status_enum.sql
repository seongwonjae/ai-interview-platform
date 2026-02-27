-- 1) 기존 데이터 소문자 -> 대문자 변환
UPDATE interview_submissions SET status = 'PENDING'    WHERE status = 'pending';
UPDATE interview_submissions SET status = 'PROCESSING' WHERE status = 'processing';
UPDATE interview_submissions SET status = 'DONE'       WHERE status = 'done';
UPDATE interview_submissions SET status = 'FAILED'     WHERE status = 'failed';

-- 2) enum 정의 자체를 대문자로 변경
ALTER TABLE interview_submissions
  MODIFY COLUMN status ENUM('PENDING','PROCESSING','DONE','FAILED')
  NOT NULL;