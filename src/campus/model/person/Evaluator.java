package campus.model.person;

/**
 * Implemented by any role that can score and comment on someone else's work —
 * a TeachingAssistant grading a Submission, or a PermanentInstructor grading
 * an FYP idea .
 */
public interface Evaluator {
    void evaluate();
}