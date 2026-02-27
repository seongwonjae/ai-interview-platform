package com.portfolio.interview.service;

import com.portfolio.interview.domain.user.UserEntity;
import com.portfolio.interview.repo.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public AuthService(UserRepository userRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    public UserEntity register(String email, String password) {
        userRepository.findByEmail(email).ifPresent(u -> {
            // ✅ 이메일 중복은 409 Conflict
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already used");
        });

        UserEntity u = new UserEntity();
        u.email = email;
        u.passwordHash = encoder.encode(password);
        return userRepository.save(u);
    }

    public UserEntity login(String email, String password) {
        UserEntity u = userRepository.findByEmail(email)
            // ✅ 로그인 실패는 401 Unauthorized
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!encoder.matches(password, u.passwordHash)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return u;
    }
}
