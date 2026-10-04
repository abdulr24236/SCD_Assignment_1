package campus.exceptions;

/** Thrown when a student tries to enroll in a section that is already at capacity. */
public class CourseFullException extends CourseException {

    public CourseFullException(String message) {
        super(message);
    }
}