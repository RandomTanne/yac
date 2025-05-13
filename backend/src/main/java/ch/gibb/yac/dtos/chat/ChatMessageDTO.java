package ch.gibb.yac.dtos.chat;

import jakarta.validation.constraints.NotNull;

public record ChatMessageDTO(@NotNull(message = "Target username must not be null") String targetUsername, @NotNull(message = "Message must not be null") String message) {
}
