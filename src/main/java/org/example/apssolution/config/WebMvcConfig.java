package org.example.apssolution.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
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
                        "/api/notices/files/download/**",
                        "/api/chats/files/download/**",
                        "/ws/**");


        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/accounts/login",
                        "/api/accounts/*/edit",
                        "/api/accounts/*/password",
                        "/api/accounts",
                        "/api/tools",
                        "/api/notices/**",
                        "/api/notices/files/download/**",
                        "/api/chats/files/download/**",
                        "/api/chats/**",
                        "/api/scenarios/schedules/today",
                        "/api/scenarios/worker/unread",
                        "/api/calendars/**",
                        "/ws/**"
                );
    }
    // 채팅방에서 이미지 업로드하려고 하니까 크로스오리진 걸려서 지피티가 이거 추가하라고 해서 만들었어요
    // 만들었더니 일단 크로스오리진 문제는 사라짐
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
