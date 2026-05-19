package com.cxy.travelaiagent.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final JwtService jwtService;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final AuthProperties authProperties;

    public AuthService(UserRepository userRepository,
                       PasswordHasher passwordHasher,
                       JwtService jwtService,
                       StringRedisTemplate stringRedisTemplate,
                       ObjectMapper objectMapper,
                       AuthProperties authProperties) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.jwtService = jwtService;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.authProperties = authProperties;
    }

    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        validateUsernameAndPassword(request.username(), request.password());
        try {
            UserAccount user = userRepository.create(
                    request.username().trim(),
                    passwordHasher.hash(request.password()),
                    normalize(request.nickname()),
                    normalize(request.phone()),
                    normalize(request.email())
            );
            return createAuthResponse(user);
        } catch (DuplicateKeyException e) {
            throw new AuthException("用户名已存在");
        }
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        validateUsernameAndPassword(request.username(), request.password());
        UserAccount user = userRepository.findByUsername(request.username().trim())
                .orElseThrow(() -> new AuthException("用户名或密码错误"));
        if (!passwordHasher.matches(request.password(), user.passwordHash())) {
            throw new AuthException("用户名或密码错误");
        }
        return createAuthResponse(user);
    }

    public Optional<UserAccount> authenticate(String token) {
        JwtService.JwtClaims claims = jwtService.parse(token);
        if (claims == null) {
            return Optional.empty();
        }
        String session = stringRedisTemplate.opsForValue().get(sessionKey(claims.tokenId()));
        if (session == null) {
            return Optional.empty();
        }
        return userRepository.findById(claims.userId());
    }

    public void logout(String token) {
        JwtService.JwtClaims claims = jwtService.parse(token);
        if (claims != null) {
            stringRedisTemplate.delete(sessionKey(claims.tokenId()));
        }
    }

    private AuthDtos.AuthResponse createAuthResponse(UserAccount user) {
        JwtService.TokenData tokenData = jwtService.createToken(user);
        saveSession(user, tokenData);
        return new AuthDtos.AuthResponse(
                tokenData.token(),
                authProperties.getTokenTtlSeconds(),
                AuthDtos.UserProfile.from(user)
        );
    }

    private void saveSession(UserAccount user, JwtService.TokenData tokenData) {
        try {
            Map<String, Object> session = new LinkedHashMap<>();
            session.put("userId", user.id());
            session.put("username", user.username());
            session.put("createdAt", Instant.now().toString());
            stringRedisTemplate.opsForValue().set(
                    sessionKey(tokenData.tokenId()),
                    objectMapper.writeValueAsString(session),
                    Duration.ofSeconds(authProperties.getTokenTtlSeconds())
            );
        } catch (Exception e) {
            throw new IllegalStateException("保存登录会话失败", e);
        }
    }

    private String sessionKey(String tokenId) {
        return authProperties.getSessionKeyPrefix() + tokenId;
    }

    private void validateUsernameAndPassword(String username, String password) {
        if (username == null || username.trim().length() < 3 || username.trim().length() > 64) {
            throw new AuthException("用户名长度需要在 3 到 64 位之间");
        }
        if (password == null || password.length() < 6 || password.length() > 128) {
            throw new AuthException("密码长度需要在 6 到 128 位之间");
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public static class AuthException extends RuntimeException {
        public AuthException(String message) {
            super(message);
        }
    }
}
