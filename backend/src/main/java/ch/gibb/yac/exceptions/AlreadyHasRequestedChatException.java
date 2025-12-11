package ch.gibb.yac.exceptions;

import java.io.Serial;

public class AlreadyHasRequestedChatException extends Exception {
    @Serial private static final long serialVersionUID = 1L;

    public AlreadyHasRequestedChatException(String message) {
        super(message);
    }
}
