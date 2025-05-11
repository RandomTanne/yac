package ch.gibb.yac.config;

import ch.gibb.yac.handlers.ChatRequestWebSocketHandler;
import ch.gibb.yac.repositories.PersonRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    PersonRepository personRepository;

    public WebSocketConfig(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Bean
    public ChatRequestWebSocketHandler chatRequestWebSocketHandler() {
        return new ChatRequestWebSocketHandler(personRepository);
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatRequestWebSocketHandler(), "/sockets/chatrequests").setAllowedOrigins("*");
    }
}