package campus.exceptions;

public abstract class RequestException extends CampusException {

    public RequestException(String message) {
        super(message);
    }
}