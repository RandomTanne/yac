package ch.gibb.yac.controllers;

import ch.gibb.yac.exceptions.ChatNotRequestedException;
import ch.gibb.yac.exceptions.UserNotConnectedException;
import ch.gibb.yac.handlers.ChatRequestWebSocketHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatRequestWebSocketHandler handler;

    public ChatController(ChatRequestWebSocketHandler handler) {
        this.handler = handler;
    }

    @PostMapping("/request")
    public ResponseEntity<String> requestChat(@AuthenticationPrincipal User user, @RequestBody String targetUsername) {
        try {
            handler.requestChat(user.getUsername(), targetUsername);
            return ResponseEntity.ok("You have successfully requested a chat with " + targetUsername);
        } catch (IOException e) {
            return new ResponseEntity<>("Something went wrong while trying to request a chat with the user " + targetUsername, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (UserNotConnectedException e) {
            return new ResponseEntity<>("The user " + targetUsername + " is not connected!", HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/accept")
    public ResponseEntity<String> acceptChat(@AuthenticationPrincipal User user, @RequestBody String targetUsername) {
        try {
            handler.acceptChat(user.getUsername(), targetUsername);
            return ResponseEntity.ok("You have successfully accepted a chat with " + targetUsername);
        } catch (IOException e) {
            return new ResponseEntity<>("Something went wrong while trying to accept a chat with the user " + targetUsername, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (UserNotConnectedException e) {
            return new ResponseEntity<>("The user " + targetUsername + " is not connected!", HttpStatus.NOT_FOUND);
        } catch (ChatNotRequestedException e) {
            return new ResponseEntity<>("The user " + targetUsername + " has not requested a chat with you!", HttpStatus.NOT_FOUND);
        }
    }
}
