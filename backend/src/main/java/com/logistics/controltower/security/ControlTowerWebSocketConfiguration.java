package com.logistics.controltower.security;

import com.logistics.controltower.users.UserRepository;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class ControlTowerWebSocketConfiguration implements WebSocketMessageBrokerConfigurer {
    private final JwtService jwtService;
    private final TokenRevocationRepository revokedTokens;
    private final UserRepository users;
    private final String[] allowedOrigins;

    public ControlTowerWebSocketConfiguration(JwtService jwtService, TokenRevocationRepository revokedTokens,
                                              UserRepository users,
                                              @Value("${app.security.cors-origins}") String origins) {
        this.jwtService = jwtService;
        this.revokedTokens = revokedTokens;
        this.users = users;
        this.allowedOrigins = Arrays.stream(origins.split(",")).map(String::trim).toArray(String[]::new);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOrigins(allowedOrigins);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor == null || accessor.getCommand() == null) return message;
                if (accessor.getCommand() == StompCommand.CONNECT) {
                    authenticate(accessor);
                } else if (accessor.getCommand() == StompCommand.SUBSCRIBE) {
                    String destination = accessor.getDestination();
                    if (destination == null || !destination.matches("/user/queue/notifications")) {
                        throw new IllegalArgumentException("Subscription destination is not permitted");
                    }
                } else if (accessor.getCommand() == StompCommand.SEND) {
                    throw new IllegalArgumentException("Client messages are not supported");
                }
                return message;
            }
        });
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String header = accessor.getFirstNativeHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Bearer token required");
        }
        var claims = jwtService.parse(header.substring(7));
        if (revokedTokens.existsById(claims.tokenId())) {
            throw new IllegalArgumentException("Token is revoked");
        }
        var user = users.findByEmailIgnoreCase(claims.subject())
                .filter(candidate -> candidate.isActive())
                .orElseThrow(() -> new UsernameNotFoundException("Active user not found"));
        var authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), null,
                user.getRoles().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.name())).toList());
        accessor.setUser(authentication);
    }
}
