package com.cxy.travelaiagent.auth;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            String username,
            String password,
            String nickname,
            String phone,
            String email
    ) {
    }

    public record LoginRequest(String username, String password) {
    }

    public record UserProfile(
            Long id,
            String username,
            String nickname,
            String phone,
            String email
    ) {
        public static UserProfile from(UserAccount user) {
            return new UserProfile(user.id(), user.username(), user.nickname(), user.phone(), user.email());
        }
    }

    public record AuthResponse(String token, long expiresIn, UserProfile user) {
    }

    public record ApiMessage(String message) {
    }
}
