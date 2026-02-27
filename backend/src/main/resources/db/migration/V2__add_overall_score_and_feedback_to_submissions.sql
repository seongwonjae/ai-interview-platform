ALTER TABLE interview_submissions
  ADD COLUMN overall_score INT NULL AFTER status,
  ADD COLUMN feedback TEXT NULL AFTER overall_score;
