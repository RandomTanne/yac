package ch.gibb.yac.handlers;

import ch.gibb.yac.exceptions.ChatNotRequestedException;
import ch.gibb.yac.exceptions.UserNotConnectedException;
import ch.gibb.yac.models.Person;
import ch.gibb.yac.repositories.PersonRepository;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatRequestWebSocketHandler extends TextWebSocketHandler {

    private final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<String, String> chatRequests = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    private void sendToUser(String targetUsername, String message) throws UserNotConnectedException, IOException {
        Optional<WebSocketSession> targetSession = sessions.stream()
                .filter(session ->
                        {
                            if (!session.isOpen()) return false;
                            return targetUsername.equals(session.getPrincipal().getName());
                        }
                )
                .findFirst();

        if(targetSession.isPresent()) {
            targetSession.get().sendMessage(new TextMessage(message));
        } else {
            throw new UserNotConnectedException("The requested user is not connected");
        }
    }

    private boolean chatWasRequested(String requestUsername, String targetUsername) {
        return chatRequests.containsKey(requestUsername) && chatRequests.get(requestUsername).equals(targetUsername);
    }

    public void requestChat(String requestUsername, String targetUsername) throws UserNotConnectedException, IOException {
        sendToUser(targetUsername, "User " + requestUsername + " has requested a chat with you!");
        chatRequests.put(requestUsername, targetUsername);
    }

    public void acceptChat(String requestUsername, String targetUsername) throws ChatNotRequestedException, UserNotConnectedException, IOException {
        if(!chatWasRequested(targetUsername, requestUsername)) {
            throw new ChatNotRequestedException("The user has not requested a chat with you");
        }

        sendToUser(targetUsername, "The user " + requestUsername + " has accepted your chatrequest");
    }
}
