package com.app.projectbar.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Prefijo para los destinos de mensajes que van hacia los clientes
        // Los clientes se suscriben a /topic/orders, /user/queue/notifications, etc.
        config.enableSimpleBroker("/topic", "/queue");
        
        // Prefijo para los mensajes que vienen del cliente hacia el servidor
        config.setApplicationDestinationPrefixes("/app");
        
        // Para mensajes específicos de usuario (para respuestas privadas)
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint principal de WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        
        // Endpoint sin SockJS (para producción o testing)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }
}