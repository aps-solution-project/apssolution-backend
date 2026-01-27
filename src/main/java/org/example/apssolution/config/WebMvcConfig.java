package org.example.apssolution.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final AdminInterceptor adminInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")          // 적용할 경로
                .excludePathPatterns("/api/accounts/login",
                        "/api/notices/files/download/**");


        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/accounts/login",
                        "/api/accounts/*/edit",
                        "/api/accounts/*/pw",
                        "/api/accounts",
                        "/api/notices/**",
                        "/api/notices/files/download/**"
                );
    }
}
