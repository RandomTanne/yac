package ch.gibb.yac.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import ch.gibb.yac.dtos.chat.ChatMessageRequestDTO;
import ch.gibb.yac.dtos.chat.TargetUsernameDTO;
import ch.gibb.yac.exceptions.*;
import ch.gibb.yac.handlers.ChatWebSocketHandler;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;

/**
 * Tests for ChatController to verify chat request, acceptance, and message sending based on user stories.
 */
public class ChatControllerTest {

    private ChatController chatController;

    @Mock private ChatWebSocketHandler chatWebSocketHandler;

    private static final String UUID1 = "550e8400-e29b-41d4-a716-446655440000";
    private static final String UUID2 = "660e8400-e29b-41d4-a716-446655440001";
    private static final String UUID3 = "770e8400-e29b-41d4-a716-446655440002";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        chatController = new ChatController(chatWebSocketHandler);
    }

    // ==================== requestChat Tests ====================

    @Test
    void shouldSuccessfullyRequestChatWithValidUsername() throws Exception {
        // User Story: Chat anfragen
        // Als Benutzer kann ich eine Konversation von einem anderen Benutzer mit seiner UUID
        // anfragen
        User requestingUser = new User(UUID1, "password", List.of());
        TargetUsernameDTO targetDTO = new TargetUsernameDTO(UUID2);

        doNothing().when(chatWebSocketHandler).requestChat(UUID1, UUID2);

        ResponseEntity<String> result = chatController.requestChat(requestingUser, targetDTO);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().contains("successfully requested a chat"));
        verify(chatWebSocketHandler).requestChat(UUID1, UUID2);
    }

    @Test
    void shouldReturnErrorWhenRequestingChatWithInvalidUsername() throws Exception {
        // User Story: Chat anfragen mit ungültiger UUID
        // Als Benutzer möchte ich eine Fehlermeldung erhalten, wenn ich einen Chat mit einer UUID
        // anfrage,
        // die gar nicht mit einem aktiven Benutzer assoziiert ist
        User requestingUser = new User(UUID1, "password", List.of());
        TargetUsernameDTO targetDTO = new TargetUsernameDTO("nonexistent-uuid");

        doThrow(new UserNotConnectedException("The requested user is not connected"))
                .when(chatWebSocketHandler)
                .requestChat(UUID1, "nonexistent-uuid");

        ResponseEntity<String> result = chatController.requestChat(requestingUser, targetDTO);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertTrue(result.getBody().contains("is not connected"));
    }

    @Test
    void shouldReturnErrorWhenUserAlreadyHasRequestedChat() throws Exception {
        // User Story: Mehrere Chatanfragen - aber max 1 aktive pro User
        User user = new User(UUID1, "password", List.of());
        TargetUsernameDTO targetDTO = new TargetUsernameDTO(UUID2);

        doThrow(new AlreadyHasRequestedChatException("Already requested"))
                .when(chatWebSocketHandler)
                .requestChat(UUID1, UUID2);

        ResponseEntity<String> result = chatController.requestChat(user, targetDTO);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertTrue(result.getBody().contains("cannot request more than one chat"));
    }

    @Test
    void shouldReturnErrorWhenTryingToStartChatWithSelf() throws Exception {
        User user = new User(UUID1, "password", List.of());
        TargetUsernameDTO targetDTO = new TargetUsernameDTO(UUID1);

        doThrow(new CannotStartChatWithYourselfException("Cannot chat with yourself"))
                .when(chatWebSocketHandler)
                .requestChat(UUID1, UUID1);

        ResponseEntity<String> result = chatController.requestChat(user, targetDTO);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertTrue(result.getBody().contains("cannot start a chat with yourself"));
    }

    @Test
    void shouldSuccessfullyAcceptChat() throws Exception {
        User user = new User("user1", "password", List.of());
        TargetUsernameDTO targetDTO = new TargetUsernameDTO("user2");

        doNothing().when(chatWebSocketHandler).acceptChat("user1", "user2");

        ResponseEntity<String> result = chatController.acceptChat(user, targetDTO);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().contains("successfully accepted a chat"));
        verify(chatWebSocketHandler).acceptChat("user1", "user2");
    }

    @Test
    void shouldReturnErrorWhenAcceptingChatNotRequested() throws Exception {
        User user = new User("user1", "password", List.of());
        TargetUsernameDTO targetDTO = new TargetUsernameDTO("user2");

        doThrow(new ChatNotRequestedException("Chat not requested"))
                .when(chatWebSocketHandler)
                .acceptChat("user1", "user2");

        ResponseEntity<String> result = chatController.acceptChat(user, targetDTO);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertTrue(result.getBody().contains("has not requested a chat with you"));
    }

    @Test
    void shouldReturnErrorWhenAcceptingChatButUserNotConnected() throws Exception {
        User user = new User("user1", "password", List.of());
        TargetUsernameDTO targetDTO = new TargetUsernameDTO("user2");

        doThrow(new UserNotConnectedException("User not connected"))
                .when(chatWebSocketHandler)
                .acceptChat("user1", "user2");

        ResponseEntity<String> result = chatController.acceptChat(user, targetDTO);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertTrue(result.getBody().contains("is not connected"));
    }

    @Test
    void shouldReturnErrorWhenUserAlreadyHasOngoingChat() throws Exception {
        User user = new User("user1", "password", List.of());
        TargetUsernameDTO targetDTO = new TargetUsernameDTO("user2");

        doThrow(new AlreadyHasOngoingChatException("Already has chat"))
                .when(chatWebSocketHandler)
                .acceptChat("user1", "user2");

        ResponseEntity<String> result = chatController.acceptChat(user, targetDTO);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertTrue(result.getBody().contains("cannot accept more than one chat"));
    }

    @Test
    void shouldSuccessfullySendShortMessage() throws Exception {
        // User Story: Chat anfragen und Nachricht senden
        // so kann ich daraufhin eine Nachricht an den anderen Benutzer senden
        User user = new User(UUID1, "password", List.of());
        String messageContent = "Hello, this is a test message!";
        ChatMessageRequestDTO messageDTO = new ChatMessageRequestDTO(UUID2, messageContent);

        doNothing().when(chatWebSocketHandler).sendChat(UUID1, UUID2, messageContent);

        ResponseEntity<String> result = chatController.sendChat(user, messageDTO);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().contains("Message was sent successfully"));
        verify(chatWebSocketHandler).sendChat(UUID1, UUID2, messageContent);
    }

    @Test
    void shouldSuccessfullySendLongMessage() throws Exception {
        // User Story: Lange Textnachricht senden
        // Als Benutzer möchte ich eine Nachricht mit einer Länge von 4096 senden und empfangen
        // können
        User user = new User(UUID1, "password", List.of());
        String longMessage = "x".repeat(4096); // 4096 character message
        ChatMessageRequestDTO messageDTO = new ChatMessageRequestDTO(UUID2, longMessage);

        doNothing().when(chatWebSocketHandler).sendChat(UUID1, UUID2, longMessage);

        ResponseEntity<String> result = chatController.sendChat(user, messageDTO);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().contains("Message was sent successfully"));
        verify(chatWebSocketHandler).sendChat(UUID1, UUID2, longMessage);
    }

    @Test
    void shouldReturnErrorWhenSendingChatNoOngoingChat() throws Exception {
        User user = new User("user1", "password", List.of());
        ChatMessageRequestDTO messageDTO = new ChatMessageRequestDTO("user2", "Hello!");

        doThrow(new NoOngoingChatException("No ongoing chat"))
                .when(chatWebSocketHandler)
                .sendChat("user1", "user2", "Hello!");

        ResponseEntity<String> result = chatController.sendChat(user, messageDTO);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertTrue(result.getBody().contains("don't have an ongoing chat"));
    }

    @Test
    void shouldReturnErrorWhenSendingChatTargetNotConnected() throws Exception {
        User user = new User("user1", "password", List.of());
        ChatMessageRequestDTO messageDTO = new ChatMessageRequestDTO("user2", "Hello!");

        doThrow(new UserNotConnectedException("User not connected"))
                .when(chatWebSocketHandler)
                .sendChat("user1", "user2", "Hello!");

        ResponseEntity<String> result = chatController.sendChat(user, messageDTO);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertTrue(result.getBody().contains("is not connected"));
    }

    @Test
    void shouldSuccessfullyCancelAllChats() throws Exception {
        User user = new User(UUID1, "password", List.of());

        doNothing().when(chatWebSocketHandler).cancelAllChats(UUID1);

        ResponseEntity<String> result = chatController.cancelAllChats(user);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().contains("Canceled all chats"));
        verify(chatWebSocketHandler).cancelAllChats(UUID1);
    }

    @Test
    void shouldSuccessfullyCancelAllChatsEvenIfExceptionThrown() throws Exception {
        User user = new User(UUID1, "password", List.of());

        doThrow(new IOException("IO error")).when(chatWebSocketHandler).cancelAllChats(UUID1);

        ResponseEntity<String> result = chatController.cancelAllChats(user);

        // Should still return OK even if exception was thrown
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().contains("Canceled all chats"));
    }

    // ==================== getChatRequests Tests ====================

    @Test
    void shouldReturnEmptyListWhenNoChatRequests() {
        // User Story: Mehrere Chatanfragen erhalten
        // Als Benutzer möchte ich Chatanfragen von mehreren Benutzern erhalten können
        User user = new User(UUID1, "password", List.of());

        when(chatWebSocketHandler.getChatRequests(UUID1)).thenReturn(List.of());

        ResponseEntity<List<String>> result = chatController.getChatRequests(user);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isEmpty());
    }

    @Test
    void shouldReturnMultipleChatRequests() {
        // User Story: Mehrere Chatanfragen erhalten
        User user = new User(UUID1, "password", List.of());
        List<String> expectedRequests = List.of(UUID2, UUID3);

        when(chatWebSocketHandler.getChatRequests(UUID1)).thenReturn(expectedRequests);

        ResponseEntity<List<String>> result = chatController.getChatRequests(user);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(2, result.getBody().size());
        assertTrue(result.getBody().contains(UUID2));
        assertTrue(result.getBody().contains(UUID3));
    }

    @Test
    void shouldReturnSingleChatRequest() {
        User user = new User(UUID1, "password", List.of());
        List<String> expectedRequests = List.of(UUID2);

        when(chatWebSocketHandler.getChatRequests(UUID1)).thenReturn(expectedRequests);

        ResponseEntity<List<String>> result = chatController.getChatRequests(user);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        assertEquals(UUID2, result.getBody().get(0));
    }
}
