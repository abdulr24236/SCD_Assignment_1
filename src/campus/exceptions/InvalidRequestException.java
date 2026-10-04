package campus.exceptions;

/** Thrown when a submitted academic request is malformed or references invalid data. */
public class InvalidRequestException extends RequestException {

    public InvalidRequestException(String message) {
        super(message);
    }
}