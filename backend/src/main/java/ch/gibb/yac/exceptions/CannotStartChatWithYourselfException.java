package ch.gibb.yac.exceptions;

import java.io.Serial;

public class CannotStartChatWithYourselfException extends Exception {
    @Serial private static final long serialVersionUID = 1L;

    public CannotStartChatWithYourselfException(String message) {
        super(message);
    }
}
