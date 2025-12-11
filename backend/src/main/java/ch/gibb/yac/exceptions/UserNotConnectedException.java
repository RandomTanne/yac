package ch.gibb.yac.exceptions;

import java.io.Serial;

public class UserNotConnectedException extends Exception {
    @Serial private static final long serialVersionUID = 1L;

    public UserNotConnectedException(String message) {
        super(message);
    }
}
