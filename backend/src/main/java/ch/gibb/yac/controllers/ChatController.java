package ch.gibb.yac.controllers;

import ch.gibb.yac.dtos.chat.TargetUsernameDTO;
import ch.gibb.yac.exceptions.*;
import ch.gibb.yac.handlers.ChatWebSocketHandler;
import ch.gibb.yac.dtos.chat.ChatMessageRequestDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * The central controller for handling chats, requests and sending messages.
 * @author Jannik Pulfer
 * @version 1.0
 * @since 2025-05-07
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatWebSocketHandler handler;

    public ChatController(ChatWebSocketHandler handler) {
        this.handler = handler;
    }

    /**
     * Used to request a chat from the person with the username targetUsername.
     */
    @PostMapping("/request")
    public ResponseEntity<String> requestChat(@AuthenticationPrincipal User user, @RequestBody @Valid TargetUsernameDTO targetUsernameDTO) {
        try {
            handler.requestChat(user.getUsername(), targetUsernameDTO.targetUsername());
            return ResponseEntity.ok("You have successfully requested a chat with " + targetUsernameDTO.targetUsername());
        } catch (IOException e) {
            return new ResponseEntity<>("Something went wrong while trying to request a chat with the user " + targetUsernameDTO.targetUsername(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (UserNotConnectedException e) {
            return new ResponseEntity<>("The user " + targetUsernameDTO.targetUsername() + " is not connected", HttpStatus.NOT_FOUND);
        } catch (AlreadyHasRequestedChatException e) {
            return new ResponseEntity<>("You cannot request more than one chat at once", HttpStatus.BAD_REQUEST);
        } catch (CannotStartChatWithYourselfException e) {
            return new ResponseEntity<>("You cannot start a chat with yourself", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Used to accept a chat from the person with the username targetUsername.
     */
    @PostMapping("/accept")
    public ResponseEntity<String> acceptChat(@AuthenticationPrincipal User user, @RequestBody @Valid TargetUsernameDTO targetUsernameDTO) {
        try {
            handler.acceptChat(user.getUsername(), targetUsernameDTO.targetUsername());
            return ResponseEntity.ok("You have successfully accepted a chat with " + targetUsernameDTO.targetUsername());
        } catch (IOException e) {
            return new ResponseEntity<>("Something went wrong while trying to accept a chat with the user " + targetUsernameDTO.targetUsername(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (UserNotConnectedException e) {
            return new ResponseEntity<>("The user " + targetUsernameDTO.targetUsername() + " is not connected", HttpStatus.NOT_FOUND);
        } catch (ChatNotRequestedException e) {
            return new ResponseEntity<>("The user " + targetUsernameDTO.targetUsername() + " has not requested a chat with you", HttpStatus.NOT_FOUND);
        } catch (AlreadyHasOngoingChatException e) {
            return new ResponseEntity<>("You cannot accept more than one chat at once", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Used to send a chat to the person with the username targetUsername.
     */
    @PostMapping("/send")
    public ResponseEntity<String> sendChat(@AuthenticationPrincipal User user, @RequestBody @Valid ChatMessageRequestDTO message) {
        try {
            handler.sendChat(user.getUsername(), message.targetUsername(), message.message());
            return ResponseEntity.ok("Message was sent successfully to the user " + message.targetUsername());
        } catch (IOException e) {
            return new ResponseEntity<>("Something went wrong while trying to send a message to the user " + message.targetUsername(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (UserNotConnectedException e) {
            return new ResponseEntity<>("The user " + message.targetUsername() + " is not connected", HttpStatus.NOT_FOUND);
        } catch (NoOngoingChatException e) {
            return new ResponseEntity<>("You don't have an ongoing chat with the user " + message.targetUsername(), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Used to cancel all chat requests and ongoing chats.
     */
    @PostMapping("/cancel")
    public ResponseEntity<String> cancelAllChats(@AuthenticationPrincipal User user) {
        try {
            handler.cancelAllChats(user.getUsername());
        } catch (IOException | UserNotConnectedException ignored) {}

        return ResponseEntity.ok("Canceled all chats and chat requests");
    }

    /**
     * Used to get the usernames of all people that requested a chat from you.
     */
    @GetMapping("/requested")
    public ResponseEntity<List<String>> getChatRequests(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(handler.getChatRequests(user.getUsername()));
    }
}
