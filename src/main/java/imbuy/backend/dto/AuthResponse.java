package imbuy.backend.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String accessToken;

    public AuthResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}
