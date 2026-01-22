package org.example.apssolution.service.account;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import org.example.apssolution.domain.entity.Account;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtProviderService {

    @Value("${algorithmSecret}")
    private String algorithmSecret;

    public String createToken(Account account) {
        return JWT.create()
                .withSubject(account.getId())
                .withIssuer("apssolution")
                .withClaim("role", account.getRole().name())
                .sign(Algorithm.HMAC256(algorithmSecret));
    }
}
