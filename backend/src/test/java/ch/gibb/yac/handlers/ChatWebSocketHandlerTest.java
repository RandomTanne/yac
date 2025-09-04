package ch.gibb.yac.handlers;

import static org.junit.jupiter.api.Assertions.assertThrows;

import ch.gibb.yac.exceptions.ChatNotRequestedException;
import ch.gibb.yac.exceptions.NoOngoingChatException;
import ch.gibb.yac.exceptions.UserNotConnectedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

/**
 * Tests for the ChatWebSocketHandler to ensure its proper functionality
 * @author Jannik Pulfer
 * @version 1.0
 * @since 2025-05-23
 */
public class ChatWebSocketHandlerTest {

  private final ChatWebSocketHandler handler = new ChatWebSocketHandler(new ObjectMapper());

  @Test
  void shouldThrowExceptionWhenRequestingChatUserNotConnected() {
    assertThrows(
        UserNotConnectedException.class,
        () -> handler.requestChat("Test Request Username", "Test Target Username"));
  }

  @Test
  void shouldThrowExceptionWhenAcceptingChatNotRequested() {
    assertThrows(
        ChatNotRequestedException.class,
        () -> handler.acceptChat("Test Request Username", "Test Target Username"));
  }

  @Test
  void shouldThrowExceptionWhenSendingChatNotOngoing() {
    assertThrows(
        NoOngoingChatException.class,
        () -> handler.sendChat("Test Request Username", "Test Target Username", "Test Message"));
  }
}
