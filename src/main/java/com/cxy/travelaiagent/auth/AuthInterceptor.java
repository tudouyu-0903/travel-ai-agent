package com.cxy.travelaiagent.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String CURRENT_USER_ATTRIBUTE = "currentUser";

    private final AuthService authService;
    private final ObjectMapper objectMapper;

    private final List<String> protectedPaths = List.of(
            "/api/ai/manus/chat",
            "/api/ai/travel_app/chat/tools",
            "/api/ai/travel_app/chat/mcp"
    );

    public AuthInterceptor(AuthService authService, ObjectMapper objectMapper) {
        this.authService = authService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        if (!requiresLogin(request)) {
            return true;
        }

        String token = extractToken(request.getHeader("Authorization"));
        if (token == null) {
            writeUnauthorized(response);
            return false;
        }

        return authService.authenticate(token)
                .map(user -> {
                    request.setAttribute(CURRENT_USER_ATTRIBUTE, user);
                    return true;
                })
                .orElseGet(() -> {
                    try {
                        writeUnauthorized(response);
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                    return false;
                });
    }

    private boolean requiresLogin(HttpServletRequest request) {
        String path = request.getRequestURI();
        return protectedPaths.stream().anyMatch(path::startsWith);
    }

    private String extractToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring(7).trim();
    }

    private void writeUnauthorized(HttpServletResponse response) throws Exception {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(new AuthDtos.ApiMessage("请先登录后再使用任务型访问")));
    }
}
