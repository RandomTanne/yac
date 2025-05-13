package ch.gibb.yac.dtos.chat;

import jakarta.validation.constraints.NotNull;

public class ChatRequestDTO {
    private final String requestUsername;

    public ChatRequestDTO(String requestUsername) {
        this.requestUsername = requestUsername;
    }

    public String getRequestUsername() {
        return requestUsername;
    }

    public String getType() {
        return "request";
    }
}
