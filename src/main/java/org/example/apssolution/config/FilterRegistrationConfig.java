package org.example.apssolution.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FilterRegistrationConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public FilterRegistrationBean<JWTVerifyFilter> jwtFilter() {
        FilterRegistrationBean<JWTVerifyFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new JWTVerifyFilter(objectMapper));
        bean.addUrlPatterns("/api");
        bean.setOrder(1); // Interceptor보다 항상 먼저
        return bean;
    }
}
