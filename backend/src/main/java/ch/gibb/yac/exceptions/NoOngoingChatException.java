package ch.gibb.yac.exceptions;

import java.io.Serial;

public class NoOngoingChatException extends Exception {
    @Serial private static final long serialVersionUID = 1L;

    public NoOngoingChatException(String message) {
        super(message);
    }
}
