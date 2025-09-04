package ch.gibb.yac.handlers;

import ch.gibb.yac.dtos.chat.WebSocketResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * The central websocket handler for informing the clients about chat events.
 * @author Jannik Pulfer
 * @version 1.0
 * @since 2025-05-06
 */
public class ChatWebSocketHandler extends TextWebSocketHandler {
  ObjectMapper objectMapper;

  public ChatWebSocketHandler(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  private final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
  private final ConcurrentHashMap<String, String> chatRequests = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, String> ongoingChats = new ConcurrentHashMap<>();

  /**
   * Saves all newly created sessions for further access.
   */
  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    sessions.add(session);
  }

  /**
   * Cancels all active chats and removes the session when it is closed.
   */
  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    try {
      cancelAllChats(session.getPrincipal().getName());
    } catch (UserNotConnectedException | IOException ignored) {
    }
    sessions.remove(session);
  }

  /**
   * Tries to send a message to the session with the targetUsername.
   */
  private void sendToUser(String targetUsername, String message)
      throws UserNotConnectedException, IOException {
    Optional<WebSocketSession> targetSession =
        sessions.stream()
            .filter(
                session -> {
                  if (!session.isOpen()) return false;
                  return targetUsername.equals(session.getPrincipal().getName());
                })
            .findFirst();

    if (targetSession.isPresent()) {
      targetSession.get().sendMessage(new TextMessage(message));
    } else {
      throw new UserNotConnectedException("The requested user is not connected");
    }
  }

  /**
   * Checks if the person with the username requestUsername has requested a chat from the person with the username targetUsername.
   */
  private boolean chatWasRequested(String requestUsername, String targetUsername) {
    return chatRequests.containsKey(requestUsername)
        && chatRequests.get(requestUsername).equals(targetUsername);
  }

  /**
   * Checks if the person with the username requestUsername has an ongoing chat with the person with the username targetUsername.
   */
  private boolean chatIsOngoing(String requestUsername, String targetUsername) {
    return (ongoingChats.containsKey(requestUsername)
            && ongoingChats.get(requestUsername).equals(targetUsername))
        || (ongoingChats.containsKey(targetUsername)
            && ongoingChats.get(targetUsername).equals(requestUsername));
  }

  /**
   * Checks if the person with the username requestUsername already has requested a chat from someone.
   */
  private boolean alreadyHasRequestedChat(String requestUsername) {
    return chatRequests.containsKey(requestUsername);
  }

  /**
   * Checks if the person with the username requestUsername already has an ongoing chat with someone.
   */
  private boolean alreadyHasOngoingChat(String requestUsername) {
    return ongoingChats.containsKey(requestUsername);
  }

  /**
   * Tries to request a chat from the person with the username requestUsername.
   */
  public void requestChat(String requestUsername, String targetUsername)
      throws UserNotConnectedException,
          IOException,
          AlreadyHasRequestedChatException,
          CannotStartChatWithYourselfException {
    if (requestUsername.equals(targetUsername)) {
      throw new CannotStartChatWithYourselfException("The user cannot start a chat with himself");
    }
    if (alreadyHasRequestedChat(requestUsername)) {
      throw new AlreadyHasRequestedChatException("The user already has requested a chat");
    }

    String requestMessage =
        objectMapper.writeValueAsString(new WebSocketResponseDTO("request", null));
    sendToUser(targetUsername, requestMessage);
    chatRequests.put(requestUsername, targetUsername);
  }

  /**
   * Tries to accept a chat request from the person with the username targetUsername.
   */
  public void acceptChat(String requestUsername, String targetUsername)
      throws ChatNotRequestedException,
          UserNotConnectedException,
          IOException,
          AlreadyHasOngoingChatException {
    if (!chatWasRequested(targetUsername, requestUsername)) {
      throw new ChatNotRequestedException("The user has not requested a chat with you");
    }

    if (alreadyHasOngoingChat(requestUsername)) {
      throw new AlreadyHasOngoingChatException("The user already has an ongoing chat");
    }

    String acceptMessage =
        objectMapper.writeValueAsString(new WebSocketResponseDTO("accept", requestUsername));
    sendToUser(targetUsername, acceptMessage);

    chatRequests.remove(requestUsername);
    chatRequests.remove(targetUsername);
    ongoingChats.put(requestUsername, targetUsername);
    ongoingChats.put(targetUsername, requestUsername);
  }

  /**
   * Tries to send a chat to the person with the username targetUsername.
   */
  public void sendChat(String requestUsername, String targetUsername, String message)
      throws NoOngoingChatException, UserNotConnectedException, IOException {
    if (!chatIsOngoing(requestUsername, targetUsername)) {
      throw new NoOngoingChatException("The user has no ongoing chat with you");
    }

    String sendChatMessage =
        objectMapper.writeValueAsString(new WebSocketResponseDTO("message", message));
    sendToUser(targetUsername, sendChatMessage);
  }

  /**
   * Cancels all chat requests and ongoing chats and also informs the clients about this.
   */
  public void cancelAllChats(String requestUsername) throws IOException, UserNotConnectedException {
    String cancelMessage =
        objectMapper.writeValueAsString(new WebSocketResponseDTO("cancel", null));

    String ongoingChat = ongoingChats.get(requestUsername);
    if (ongoingChat != null) {
      ongoingChats.remove(requestUsername);
      ongoingChats.remove(ongoingChat);
      sendToUser(ongoingChat, cancelMessage);
    }

    String requestedUser = chatRequests.remove(requestUsername);
    if (requestedUser != null) {
      sendToUser(requestedUser, cancelMessage);
    }
  }

  /**
   * Returns the usernames of all people that requested a chat from you.
   */
  public List<String> getChatRequests(String requestUsername) {
    return chatRequests.entrySet().stream()
        .filter(e -> e.getValue().equals(requestUsername))
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());
  }
}
