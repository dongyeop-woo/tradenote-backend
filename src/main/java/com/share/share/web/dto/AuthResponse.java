package com.share.share.web.dto;

import com.share.share.user.User;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuthResponse {
    private Long id;
    private String username;
    private String displayName;
    private LocalDateTime createdAt;

    public static AuthResponse from(User user) {
        AuthResponse response = new AuthResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setDisplayName(user.getDisplayName());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}

