package com.stock.rbac.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${rbac.jwt.secret}")
    private String secretKey;

    @Value("${rbac.jwt.expire}")
    private long expireSeconds;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String userGuid, Map<String, Object> claims) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expireSeconds * 1000L);

        return Jwts.builder()
                .setSubject(userGuid)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .addClaims(claims)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new RuntimeException("JWT token 解析失败: " + e.getMessage(), e);
        }
    }

    public String getUserGuidFromToken(String token) {
        return parseToken(token).getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public long getExpireTime() {
        return System.currentTimeMillis() + expireSeconds * 1000L;
    }
}
