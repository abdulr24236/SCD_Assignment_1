package campus.exceptions;

/** Thrown for invalid FYP group operations (e.g. adding a member that already exists). */
public class InvalidFYPGroupException extends FYPException {

    public InvalidFYPGroupException(String message) {
        super(message);
    }
}