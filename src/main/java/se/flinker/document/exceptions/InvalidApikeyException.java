package se.flinker.document.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(HttpStatus.FORBIDDEN)
public class InvalidApikeyException extends DocumentServiceAppRuntimeException {

    public InvalidApikeyException(String message, String tx) {
        super(message, tx);
    }

    public InvalidApikeyException(String message, String tx, Throwable cause) {
        super(message, tx, cause);
    }

    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.FORBIDDEN;
    }

}
