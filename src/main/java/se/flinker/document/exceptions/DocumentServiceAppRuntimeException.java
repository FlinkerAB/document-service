package se.flinker.document.exceptions;

import org.springframework.http.HttpStatus;

@SuppressWarnings("serial")
public class DocumentServiceAppRuntimeException extends RuntimeException {

    public final String tx;
    
    public DocumentServiceAppRuntimeException(String message, String tx) {
        super(message);
        this.tx = tx;
    }
    
    public DocumentServiceAppRuntimeException(String message, String tx, Throwable cause) {
        super(message, cause);
        this.tx = tx;
    }
    
    public HttpStatus httpStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

}
