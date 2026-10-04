package campus.exceptions;

/**
 * Root of the Campus Management System exception hierarchy.
 */
public abstract class CampusException extends Exception {

    public CampusException(String message) {
        super(message);
    }
}