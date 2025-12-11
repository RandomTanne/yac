package ch.gibb.yac.exceptions;

import java.io.Serial;

public class AlreadyHasOngoingChatException extends Exception {
    @Serial private static final long serialVersionUID = 1L;

    public AlreadyHasOngoingChatException(String message) {
        super(message);
    }
}
