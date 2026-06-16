package org.example.stady.interceptor;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.crypto.SecretKey;

public class LoginIntercepter implements HandlerInterceptor {

    private static final SecretKey KEY = Keys.hmacShaKeyFor(
            "this-is-my-secret-key-for-jwt-1234567890".getBytes()
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(401);
            return false;
        }

        token = token.substring(7);
        try {
            Jwts.parser()
                    .verifyWith(KEY)
                    .build()
                    .parseSignedClaims(token);
        }catch (Exception e){
            response.setStatus(401);
            return false;
        }

        return true ;


    }

}
