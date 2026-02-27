package com.portfolio.interview.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthDto {
    public static class RegisterReq {
        @Email @NotBlank public String email;
        @NotBlank public String password;
    }

    public static class LoginReq {
        @Email @NotBlank public String email;
        @NotBlank public String password;
    }

    public static class LoginRes {
        public String access_token;
        public String token_type = "bearer";

        public LoginRes(String token) {
            this.access_token = token;
        }
    }
}
