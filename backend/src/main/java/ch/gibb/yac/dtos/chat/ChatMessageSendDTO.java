package ch.gibb.yac.dtos.chat;

public class ChatMessageSendDTO {
    private final String message;

    public ChatMessageSendDTO(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return "message";
    }
}
