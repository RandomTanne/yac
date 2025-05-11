package ch.gibb.yac.controllers;

import ch.gibb.yac.exceptions.UserNotConnectedException;
import ch.gibb.yac.handlers.ChatRequestWebSocketHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

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
            handler.sendToUser(targetUsername, "");
            return ResponseEntity.ok("You have successfully requested a chat with " + targetUsername);
        } catch (UserNotConnectedException e) {
            return new ResponseEntity<>("The requested user is not online!", HttpStatus.NOT_FOUND);
        }
    }
}
