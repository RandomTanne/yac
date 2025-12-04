package ch.gibb.yac.handlers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import ch.gibb.yac.exceptions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.security.Principal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * Tests for the ChatWebSocketHandler to ensure its proper functionality
 * @author Jannik Pulfer
 * @version 2.0
 * @since 2025-05-23
 */
public class ChatWebSocketHandlerTest {

    private ChatWebSocketHandler handler;
    private ObjectMapper objectMapper;

    @Mock private WebSocketSession mockSession1;
    @Mock private WebSocketSession mockSession2;
    @Mock private WebSocketSession mockSession3;
    @Mock private Principal mockPrincipal1;
    @Mock private Principal mockPrincipal2;
    @Mock private Principal mockPrincipal3;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        handler = new ChatWebSocketHandler(objectMapper);
    }

    private void setupSession(WebSocketSession session, Principal principal, String username) {
        when(session.getPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn(username);
        when(session.isOpen()).thenReturn(true);
    }

    @Test
    void shouldAddSessionWhenConnectionEstablished() throws IOException {
        setupSession(mockSession1, mockPrincipal1, "user1");

        handler.afterConnectionEstablished(mockSession1);

        // Verify the session was added by checking that we can request chat to this user
        setupSession(mockSession2, mockPrincipal2, "user2");
        handler.afterConnectionEstablished(mockSession2);

        // This should succeed without throwing UserNotConnectedException
        assertDoesNotThrow(() -> handler.requestChat("user2", "user1"));
    }

    @Test
    void shouldRemoveSessionWhenConnectionClosed() throws IOException, UserNotConnectedException {
        setupSession(mockSession1, mockPrincipal1, "user1");
        handler.afterConnectionEstablished(mockSession1);

        handler.afterConnectionClosed(mockSession1, CloseStatus.NORMAL);

        // Verify session is removed by attempting to request chat - should throw UserNotConnected
        assertThrows(UserNotConnectedException.class, () -> handler.requestChat("user2", "user1"));
    }

    @Test
    void shouldThrowExceptionWhenRequestingChatWithSelf() {
        assertThrows(
                CannotStartChatWithYourselfException.class,
                () -> handler.requestChat("user1", "user1"));
    }

    @Test
    void shouldThrowExceptionWhenUserAlreadyHasRequestedChat()
            throws UserNotConnectedException,
                    IOException,
                    AlreadyHasRequestedChatException,
                    CannotStartChatWithYourselfException {
        setupSession(mockSession1, mockPrincipal1, "user1");
        setupSession(mockSession2, mockPrincipal2, "user2");
        handler.afterConnectionEstablished(mockSession1);
        handler.afterConnectionEstablished(mockSession2);

        handler.requestChat("user1", "user2");

        // Should throw because user1 already has a pending request
        assertThrows(
                AlreadyHasRequestedChatException.class,
                () -> handler.requestChat("user1", "user3"));
    }

    @Test
    void shouldThrowExceptionWhenRequestingChatUserNotConnected() {
        assertThrows(
                UserNotConnectedException.class,
                () -> handler.requestChat("user1", "nonexistentUser"));
    }

    @Test
    void shouldSuccessfullySendChatRequestToConnectedUser()
            throws UserNotConnectedException,
                    IOException,
                    AlreadyHasRequestedChatException,
                    CannotStartChatWithYourselfException {
        setupSession(mockSession1, mockPrincipal1, "user1");
        setupSession(mockSession2, mockPrincipal2, "user2");
        handler.afterConnectionEstablished(mockSession1);
        handler.afterConnectionEstablished(mockSession2);

        handler.requestChat("user1", "user2");

        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(mockSession2).sendMessage(captor.capture());

        TextMessage sentMessage = captor.getValue();
        assertTrue(sentMessage.getPayload().contains("request"));
    }

    @Test
    void shouldThrowExceptionWhenAcceptingChatNotRequested() {
        assertThrows(ChatNotRequestedException.class, () -> handler.acceptChat("user1", "user2"));
    }

    @Test
    void shouldNotifyTargetUserOfChatRequest()
            throws UserNotConnectedException,
                    IOException,
                    AlreadyHasRequestedChatException,
                    CannotStartChatWithYourselfException {
        setupSession(mockSession1, mockPrincipal1, "user1");
        setupSession(mockSession2, mockPrincipal2, "user2");
        handler.afterConnectionEstablished(mockSession1);
        handler.afterConnectionEstablished(mockSession2);

        handler.requestChat("user1", "user2");

        // Verify that a message was sent to user2's session
        verify(mockSession2).sendMessage(any(TextMessage.class));
    }

    @Test
    void shouldSuccessfullyAcceptChat()
            throws UserNotConnectedException,
                    IOException,
                    AlreadyHasRequestedChatException,
                    CannotStartChatWithYourselfException,
                    ChatNotRequestedException,
                    AlreadyHasOngoingChatException {
        setupSession(mockSession1, mockPrincipal1, "user1");
        setupSession(mockSession2, mockPrincipal2, "user2");
        handler.afterConnectionEstablished(mockSession1);
        handler.afterConnectionEstablished(mockSession2);

        handler.requestChat("user1", "user2");
        handler.acceptChat("user2", "user1");

        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(mockSession1, times(1)).sendMessage(captor.capture());

        TextMessage sentMessage = captor.getValue();
        assertTrue(sentMessage.getPayload().contains("accept"));
    }

    @Test
    void shouldThrowExceptionWhenSendingChatNotOngoing() {
        assertThrows(
                NoOngoingChatException.class, () -> handler.sendChat("user1", "user2", "Hello"));
    }

    @Test
    void shouldSuccessfullySendChatMessage()
            throws UserNotConnectedException,
                    IOException,
                    AlreadyHasRequestedChatException,
                    CannotStartChatWithYourselfException,
                    ChatNotRequestedException,
                    AlreadyHasOngoingChatException,
                    NoOngoingChatException {
        setupSession(mockSession1, mockPrincipal1, "user1");
        setupSession(mockSession2, mockPrincipal2, "user2");
        handler.afterConnectionEstablished(mockSession1);
        handler.afterConnectionEstablished(mockSession2);

        handler.requestChat("user1", "user2");
        handler.acceptChat("user2", "user1");

        String testMessage = "Hello from user1";
        handler.sendChat("user1", "user2", testMessage);

        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(mockSession2, times(2)).sendMessage(captor.capture());

        TextMessage sentMessage = captor.getValue();
        assertTrue(sentMessage.getPayload().contains("message"));
        assertTrue(sentMessage.getPayload().contains(testMessage));
    }

    @Test
    void shouldSuccessfullyCancelOngoingChat()
            throws UserNotConnectedException,
                    IOException,
                    AlreadyHasRequestedChatException,
                    CannotStartChatWithYourselfException,
                    ChatNotRequestedException,
                    AlreadyHasOngoingChatException {
        setupSession(mockSession1, mockPrincipal1, "user1");
        setupSession(mockSession2, mockPrincipal2, "user2");
        handler.afterConnectionEstablished(mockSession1);
        handler.afterConnectionEstablished(mockSession2);

        handler.requestChat("user1", "user2");
        handler.acceptChat("user2", "user1");

        handler.cancelAllChats("user1");

        // Verify cancel message was sent to user2 (using atLeastOnce because there may be other
        // messages)
        verify(mockSession2, atLeastOnce()).sendMessage(any(TextMessage.class));
    }

    @Test
    void shouldSuccessfullyCancelPendingChatRequest()
            throws UserNotConnectedException,
                    IOException,
                    AlreadyHasRequestedChatException,
                    CannotStartChatWithYourselfException {
        setupSession(mockSession1, mockPrincipal1, "user1");
        setupSession(mockSession2, mockPrincipal2, "user2");
        handler.afterConnectionEstablished(mockSession1);
        handler.afterConnectionEstablished(mockSession2);

        handler.requestChat("user1", "user2");

        handler.cancelAllChats("user1");

        // Verify cancel message was sent to user2 (using atLeastOnce because there may be other
        // messages)
        verify(mockSession2, atLeastOnce()).sendMessage(any(TextMessage.class));
    }

    @Test
    void shouldReturnEmptyListWhenNoRequests() {
        var requests = handler.getChatRequests("user1");
        assertTrue(requests.isEmpty());
    }

    @Test
    void shouldReturnCorrectChatRequests()
            throws UserNotConnectedException,
                    IOException,
                    AlreadyHasRequestedChatException,
                    CannotStartChatWithYourselfException {
        setupSession(mockSession1, mockPrincipal1, "user1");
        setupSession(mockSession2, mockPrincipal2, "user2");
        setupSession(mockSession3, mockPrincipal2, "user3");
        handler.afterConnectionEstablished(mockSession1);
        handler.afterConnectionEstablished(mockSession2);
        handler.afterConnectionEstablished(mockSession3);

        handler.requestChat("user2", "user1");
        handler.requestChat("user3", "user1");

        var requests = handler.getChatRequests("user1");

        assertEquals(2, requests.size());
        assertTrue(requests.contains("user2"));
        assertTrue(requests.contains("user3"));
    }
}
