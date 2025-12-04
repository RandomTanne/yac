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
 * Tests for ChatController to verify chat request, acceptance, and message sending.
 */
public class ChatControllerTest {

    private ChatController chatController;

    @Mock private ChatWebSocketHandler chatWebSocketHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        chatController = new ChatController(chatWebSocketHandler);
    }

    @Test
    void shouldSuccessfullyRequestChat() throws Exception {
        User user = new User("user1", "password", List.of());
        TargetUsernameDTO targetDTO = new TargetUsernameDTO("user2");

        doNothing().when(chatWebSocketHandler).requestChat("user1", "user2");

        ResponseEntity<String> result = chatController.requestChat(user, targetDTO);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().contains("successfully requested a chat"));
        verify(chatWebSocketHandler).requestChat("user1", "user2");
    }

    @Test
    void shouldReturnErrorWhenRequestingChatWithUserNotConnected() throws Exception {
        User user = new User("user1", "password", List.of());
        TargetUsernameDTO targetDTO = new TargetUsernameDTO("user2");

        doThrow(new UserNotConnectedException("User not connected"))
                .when(chatWebSocketHandler)
                .requestChat("user1", "user2");

        ResponseEntity<String> result = chatController.requestChat(user, targetDTO);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertTrue(result.getBody().contains("is not connected"));
    }

    @Test
    void shouldReturnErrorWhenUserAlreadyHasRequestedChat() throws Exception {
        User user = new User("user1", "password", List.of());
        TargetUsernameDTO targetDTO = new TargetUsernameDTO("user2");

        doThrow(new AlreadyHasRequestedChatException("Already requested"))
                .when(chatWebSocketHandler)
                .requestChat("user1", "user2");

        ResponseEntity<String> result = chatController.requestChat(user, targetDTO);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertTrue(result.getBody().contains("cannot request more than one chat"));
    }

    @Test
    void shouldReturnErrorWhenTryingToStartChatWithSelf() throws Exception {
        User user = new User("user1", "password", List.of());
        TargetUsernameDTO targetDTO = new TargetUsernameDTO("user1");

        doThrow(new CannotStartChatWithYourselfException("Cannot chat with yourself"))
                .when(chatWebSocketHandler)
                .requestChat("user1", "user1");

        ResponseEntity<String> result = chatController.requestChat(user, targetDTO);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertTrue(result.getBody().contains("cannot start a chat with yourself"));
    }

    @Test
    void shouldReturnErrorWhenRequestingChatAndIOException() throws Exception {
        User user = new User("user1", "password", List.of());
        TargetUsernameDTO targetDTO = new TargetUsernameDTO("user2");

        doThrow(new IOException("IO error"))
                .when(chatWebSocketHandler)
                .requestChat("user1", "user2");

        ResponseEntity<String> result = chatController.requestChat(user, targetDTO);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertTrue(result.getBody().contains("Something went wrong"));
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
    void shouldReturnErrorWhenAcceptingChatAndIOException() throws Exception {
        User user = new User("user1", "password", List.of());
        TargetUsernameDTO targetDTO = new TargetUsernameDTO("user2");

        doThrow(new IOException("IO error"))
                .when(chatWebSocketHandler)
                .acceptChat("user1", "user2");

        ResponseEntity<String> result = chatController.acceptChat(user, targetDTO);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertTrue(result.getBody().contains("Something went wrong"));
    }

    @Test
    void shouldSuccessfullySendChat() throws Exception {
        User user = new User("user1", "password", List.of());
        ChatMessageRequestDTO messageDTO = new ChatMessageRequestDTO("user2", "Hello!");

        doNothing().when(chatWebSocketHandler).sendChat("user1", "user2", "Hello!");

        ResponseEntity<String> result = chatController.sendChat(user, messageDTO);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().contains("Message was sent successfully"));
        verify(chatWebSocketHandler).sendChat("user1", "user2", "Hello!");
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
    void shouldReturnErrorWhenSendingChatAndIOException() throws Exception {
        User user = new User("user1", "password", List.of());
        ChatMessageRequestDTO messageDTO = new ChatMessageRequestDTO("user2", "Hello!");

        doThrow(new IOException("IO error"))
                .when(chatWebSocketHandler)
                .sendChat("user1", "user2", "Hello!");

        ResponseEntity<String> result = chatController.sendChat(user, messageDTO);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertTrue(result.getBody().contains("Something went wrong"));
    }

    @Test
    void shouldSuccessfullyCancelAllChats() throws Exception {
        User user = new User("user1", "password", List.of());

        doNothing().when(chatWebSocketHandler).cancelAllChats("user1");

        ResponseEntity<String> result = chatController.cancelAllChats(user);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().contains("Canceled all chats"));
        verify(chatWebSocketHandler).cancelAllChats("user1");
    }

    @Test
    void shouldSuccessfullyCancelAllChatsEvenIfExceptionThrown() throws Exception {
        User user = new User("user1", "password", List.of());

        doThrow(new IOException("IO error")).when(chatWebSocketHandler).cancelAllChats("user1");

        ResponseEntity<String> result = chatController.cancelAllChats(user);

        // Should still return OK even if exception was thrown
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().contains("Canceled all chats"));
    }

    @Test
    void shouldReturnEmptyListWhenNoChatRequests() {
        User user = new User("user1", "password", List.of());

        when(chatWebSocketHandler.getChatRequests("user1")).thenReturn(List.of());

        ResponseEntity<List<String>> result = chatController.getChatRequests(user);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isEmpty());
    }

    @Test
    void shouldReturnChatRequests() {
        User user = new User("user1", "password", List.of());
        List<String> expectedRequests = List.of("user2", "user3", "user4");

        when(chatWebSocketHandler.getChatRequests("user1")).thenReturn(expectedRequests);

        ResponseEntity<List<String>> result = chatController.getChatRequests(user);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expectedRequests, result.getBody());
        assertEquals(3, result.getBody().size());
    }
}
