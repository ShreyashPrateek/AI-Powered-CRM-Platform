package com.crm.auth.dto;

import lombok.*;

import java.util.List;

@Data @Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private String email;
    private List<String> roles;
}
