package ch.gibb.yac.handlers;

import ch.gibb.yac.exceptions.UserNotConnectedException;
import ch.gibb.yac.models.Person;
import ch.gibb.yac.repositories.PersonRepository;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatRequestWebSocketHandler extends TextWebSocketHandler {

    private final PersonRepository personRepository;
    private final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    public ChatRequestWebSocketHandler(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    public void sendToUser(String username, String message) throws UserNotConnectedException {
        Optional<WebSocketSession> targetSession = sessions.stream()
                .filter(session ->
                        {
                            if (!session.isOpen()) return false;
                            return username.equals(session.getPrincipal().getName());
                        }
                )
                .findFirst();

        if(targetSession.isPresent()) {
            try {
                targetSession.get().sendMessage(new TextMessage(message));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            throw new UserNotConnectedException("The requested user is not connected");
        }
    }
}
