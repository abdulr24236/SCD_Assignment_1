package campus.exceptions;

/** Thrown when a logged-in user attempts an action outside their role's permissions. */
public class UnauthorizedActionException extends UserException {

    public UnauthorizedActionException(String message) {
        super(message);
    }
}