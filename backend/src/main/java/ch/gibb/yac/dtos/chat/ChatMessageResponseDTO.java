package ch.gibb.yac.dtos.chat;

public class ChatMessageResponseDTO {
    private final String message;

    public ChatMessageResponseDTO(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return "message";
    }
}
