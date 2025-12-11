package ch.gibb.yac.exceptions;

import java.io.Serial;

public class ChatNotRequestedException extends Exception {
    @Serial private static final long serialVersionUID = 1L;

    public ChatNotRequestedException(String message) {
        super(message);
    }
}
