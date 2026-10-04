package campus.exceptions;

/** Thrown when an FYP evaluation is given an invalid score or malformed feedback. */
public class InvalidFYPEvaluationException extends FYPException {

    public InvalidFYPEvaluationException(String message) {
        super(message);
    }
}