package org.example.apssolution.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.apssolution.dto.response.config.FilterResponse;
import org.example.apssolution.service.account.JwtProviderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JWTVerifyFilter extends OncePerRequestFilter {
    private final ObjectMapper objectMapper;

    public JWTVerifyFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Value("${algorithmSecret}")
    private String algorithmSecret;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) throws ServletException {
        String uri = req.getRequestURI();
        return req.getMethod().equals("OPTIONS") || uri.startsWith("/api/accounts/login")
                || uri.startsWith("/swagger") || uri.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain filterChain) throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        FilterResponse fr = new FilterResponse(false);

        if (header == null || !header.startsWith("Bearer ")) {
            fr.setMessage("Missing Authorization Header");
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json");
            resp.getWriter().write(objectMapper.writeValueAsString(fr));
            return;
        }

        String token = header.substring(7);


        if (token.isEmpty()) {
            fr.setMessage("Token is empty");
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json");
            resp.getWriter().write(objectMapper.writeValueAsString(fr));
            return;
        }

        DecodedJWT jwt;
        try {
            jwt = JWT.require(Algorithm.HMAC256(algorithmSecret))
                    .withIssuer("apssolution").build().verify(token);
        } catch (Exception e) {
            e.printStackTrace();
            fr.setMessage("Invalid Token");
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().println(objectMapper.writeValueAsString(fr));
            return;
        }

        String sub = jwt.getSubject();
        String role = jwt.getClaim("role").asString();
        req.setAttribute("tokenId", String.valueOf(sub));
        req.setAttribute("role", role);
        filterChain.doFilter(req, resp);
    }
}
