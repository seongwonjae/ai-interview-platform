package com.portfolio.interview.api;

import com.portfolio.interview.api.dto.AuthDto;
import com.portfolio.interview.security.JwtTokenProvider;
import com.portfolio.interview.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider tokenProvider;

    public AuthController(AuthService authService, JwtTokenProvider tokenProvider) {
        this.authService = authService;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/register")
    public Object register(@RequestBody @Valid AuthDto.RegisterReq req) {
        var u = authService.register(req.email, req.password);
        return new Object() { public Long id = u.id; public String email = u.email; };
    }

    @PostMapping("/login")
    public AuthDto.LoginRes login(@RequestBody @Valid AuthDto.LoginReq req) {
        var u = authService.login(req.email, req.password);
        String token = tokenProvider.createToken(u.id, u.email);
        return new AuthDto.LoginRes(token);
    }
}
