package campus.model.assessment;

import campus.enums.SubmissionStatus;
import campus.model.person.Student;
import java.time.LocalDate;

/**
 * A student's submitted work for one Assignment .
 */
public class Submission {
    private String submissionId;
    private Assignment assignment;
    private Student student;
    private LocalDate submissionDate;
    private String content;
    private double marks;
    private Feedback feedback;
    private SubmissionStatus status;

    public Submission(String submissionId, Assignment assignment, Student student, String content) {
        this.submissionId = submissionId;
        this.assignment = assignment;
        this.student = student;
        this.content = content;
        this.submissionDate = LocalDate.now();
        this.status = SubmissionStatus.PENDING;
        submit();
    }

    /**
     * Reconstruction constructor — used when loading a Submission back from
     * a saved file. The normal constructor always stamps today's date and
     * derives status by comparing against today, which would silently
     * recompute an old on-time submission as LATE and lose any assigned
     * marks. This restores the real saved state directly instead.
     */
    public Submission(String submissionId, Assignment assignment, Student student, String content,
                      LocalDate submissionDate, SubmissionStatus status, double marks) {
        this.submissionId = submissionId;
        this.assignment = assignment;
        this.student = student;
        this.content = content;
        this.submissionDate = submissionDate;
        this.status = status;
        this.marks = marks;
    }

    /** Marks this submission as SUBMITTED, or LATE if the assignment's deadline has already passed. */
    public void submit() {
        status = isLate() ? SubmissionStatus.LATE : SubmissionStatus.SUBMITTED;
    }

    public boolean isLate() {
        return assignment != null && submissionDate != null
                && submissionDate.isAfter(assignment.getDeadline());
    }

    public void assignMarks(double marks) {
        this.marks = marks;
        status = SubmissionStatus.EVALUATED;
    }

    public void addFeedback(Feedback feedback) {
        this.feedback = feedback;
    }

    public double getMarks() {
        return marks;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    // Not in the diagram's method list, but a repository needs all of these
    // to serialize/reference a Submission fully.
    public String getSubmissionId() {
        return submissionId;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public Student getStudent() {
        return student;
    }

    public LocalDate getSubmissionDate() {
        return submissionDate;
    }

    public String getContent() {
        return content;
    }

    // Not in the diagram's method list. giveFeedback() attaches a Feedback
    // internally but has no way to hand it back (void return, matching the
    // diagram) — this lets a caller retrieve it afterward to persist it.
    public Feedback getFeedback() {
        return feedback;
    }
}