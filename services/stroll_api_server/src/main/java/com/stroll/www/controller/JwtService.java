package com.stroll.www.controller;

import com.stroll.www.property.JwtProps;
import io.jsonwebtoken.Claims;
import org.apache.commons.codec.DecoderException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Date;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;


@Service
public class JwtService {

    @Autowired
    private JwtProps jwtProps;

    private SecretKey getKey() throws DecoderException {
        String secretB64 = jwtProps.getSecret(); // Base64 문자열
        byte[] keyBytes = Base64.getDecoder().decode(secretB64); // 또는 getUrlDecoder() 참고(아래)
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String userId, String petType) throws DecoderException {
        return Jwts.builder()
                .subject(userId) // 토큰 주체 (보통 사용자 ID)
                .issuedAt(new Date()) // 발급 시각
                .expiration(new Date(System.currentTimeMillis() + jwtProps.getExpiration())) // 만료 시각
                .claim("petType", petType)
                .signWith(getKey()) // 서명
                .compact();
    }

    //오로지 filter에서만 사용
    public boolean isValid(String token) {
        try {
            Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public Claims parseClaims(String token) throws DecoderException {
        return Jwts.parser().verifyWith(getKey()).build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
