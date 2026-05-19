package com.cxy.travelaiagent.auth;

import java.time.LocalDateTime;

public record UserAccount(
        Long id,
        String username,
        String nickname,
        String phone,
        String email,
        String passwordHash,
        LocalDateTime createdAt
) {
}
