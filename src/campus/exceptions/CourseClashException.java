package campus.exceptions;

/** Thrown when a student's requested section schedule clashes with an existing enrollment. */
public class CourseClashException extends CourseException {

    public CourseClashException(String message) {
        super(message);
    }
}