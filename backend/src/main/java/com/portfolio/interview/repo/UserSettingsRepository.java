package com.portfolio.interview.repo;

import com.portfolio.interview.domain.user.UserSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserSettingsRepository extends JpaRepository<UserSettingsEntity, Long> {
    Optional<UserSettingsEntity> findFirstByUserIdOrderByCreatedAtDesc(Long userId);
}
