package com.cxy.travelaiagent.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthDtos.AuthResponse register(@RequestBody AuthDtos.RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthDtos.AuthResponse login(@RequestBody AuthDtos.LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = extractToken(authorization);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthDtos.ApiMessage("请先登录"));
        }
        return authService.authenticate(token)
                .<ResponseEntity<?>>map(user -> ResponseEntity.ok(AuthDtos.UserProfile.from(user)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthDtos.ApiMessage("登录已失效，请重新登录")));
    }

    @PostMapping("/logout")
    public AuthDtos.ApiMessage logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = extractToken(authorization);
        if (token != null) {
            authService.logout(token);
        }
        return new AuthDtos.ApiMessage("已退出登录");
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(AuthService.AuthException.class)
    public ResponseEntity<AuthDtos.ApiMessage> handleAuthException(AuthService.AuthException exception) {
        return ResponseEntity.badRequest().body(new AuthDtos.ApiMessage(exception.getMessage()));
    }

    private String extractToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring(7).trim();
    }
}
