package org.example.apssolution.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }

    // 사용할 대표 채널명
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메시지를 받을 때 (구독)
        registry.enableSimpleBroker("/topic");
        // 메시지를 보낼 때 (컨트롤러 매핑용 접두사)
        registry.setApplicationDestinationPrefixes("/app");
    }
}
