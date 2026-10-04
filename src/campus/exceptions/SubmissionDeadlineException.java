package campus.exceptions;

/** Thrown when a student attempts to submit an assignment after its deadline has passed. */
public class SubmissionDeadlineException extends AssessmentException {

    public SubmissionDeadlineException(String message) {
        super(message);
    }
}