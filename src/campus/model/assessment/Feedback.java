package campus.model.assessment;

import campus.model.person.Evaluator;
import java.time.LocalDate;

/**
 * An evaluator's written comments on a Submission .
 */
public class Feedback {
    private String feedbackId;
    private Evaluator evaluator;
    private String comments;
    private LocalDate date;
    // Not in the diagram's field list — Feedback's own diagram fields have
    // no way to say which Submission it belongs to, but a repository needs
    // this to reattach a reloaded Feedback to the right Submission.
    private String submissionId;

    public Feedback(String feedbackId, Evaluator evaluator, String comments, LocalDate date, String submissionId) {
        this.feedbackId = feedbackId;
        this.evaluator = evaluator;
        this.comments = comments;
        this.date = date;
        this.submissionId = submissionId;
    }

    public String getComments() {
        return comments;
    }

    public String getFeedbackId() {
        return feedbackId;
    }

    public Evaluator getEvaluator() {
        return evaluator;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getSubmissionId() {
        return submissionId;
    }
}