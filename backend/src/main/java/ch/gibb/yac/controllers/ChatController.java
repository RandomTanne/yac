package ch.gibb.yac.controllers;

import ch.gibb.yac.exceptions.*;
import ch.gibb.yac.handlers.ChatWebSocketHandler;
import ch.gibb.yac.dtos.ChatMessageDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatWebSocketHandler handler;

    public ChatController(ChatWebSocketHandler handler) {
        this.handler = handler;
    }

    @PostMapping("/request")
    public ResponseEntity<String> requestChat(@AuthenticationPrincipal User user, @RequestBody @NotNull(message = "Target username must not be null") @Valid String targetUsername) {
        try {
            handler.requestChat(user.getUsername(), targetUsername);
            return ResponseEntity.ok("You have successfully requested a chat with " + targetUsername);
        } catch (IOException e) {
            return new ResponseEntity<>("Something went wrong while trying to request a chat with the user " + targetUsername, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (UserNotConnectedException e) {
            return new ResponseEntity<>("The user " + targetUsername + " is not connected", HttpStatus.NOT_FOUND);
        } catch (AlreadyHasRequestedChatException e) {
            return new ResponseEntity<>("You cannot request more than one chat at once", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/accept")
    public ResponseEntity<String> acceptChat(@AuthenticationPrincipal User user, @RequestBody @NotNull(message = "Target username must not be null") @Valid String targetUsername) {
        try {
            handler.acceptChat(user.getUsername(), targetUsername);
            return ResponseEntity.ok("You have successfully accepted a chat with " + targetUsername);
        } catch (IOException e) {
            return new ResponseEntity<>("Something went wrong while trying to accept a chat with the user " + targetUsername, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (UserNotConnectedException e) {
            return new ResponseEntity<>("The user " + targetUsername + " is not connected", HttpStatus.NOT_FOUND);
        } catch (ChatNotRequestedException e) {
            return new ResponseEntity<>("The user " + targetUsername + " has not requested a chat with you", HttpStatus.NOT_FOUND);
        } catch (AlreadyHasOngoingChatException e) {
            return new ResponseEntity<>("You cannot accept more than one chat at once", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendChat(@AuthenticationPrincipal User user, @RequestBody @Valid ChatMessageDTO message) {
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
}
