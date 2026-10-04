package campus.model.person;

import campus.model.academic.Section;
import campus.model.assessment.Assignment;
import campus.model.assessment.Feedback;
import campus.model.assessment.Submission;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * A NormalStudent who has been assigned as a TA for a section (diagram 3.1).
 * Per the class diagram, this is a separate subclass of Student rather than
 * a flag — so when a PermanentInstructor assigns a TA, a new
 * TeachingAssistant is constructed from the NormalStudent's existing data
 * (see PermanentInstructor.assignTA()).
 */
public class TeachingAssistant extends Student implements Evaluator {
    private static int taCounter = 0;

    private Section assignedSection;
    // A student's original studentId (e.g. "S002") is kept via the inherited
    // getStudentId(). This is a second, separate ID for the TA role itself
    // (e.g. "TA001") — per your decision to track both identities rather
    // than reuse the student ID. Auto-incrementing counter, so note: this
    // resets to 0 each time the program restarts, which matters once we
    // persist TeachingAssistants — the counter will need to be seeded from
    // the highest existing TA ID already saved on disk, not start over.
    private String taId;

    public TeachingAssistant(String name, String email, String phone, String studentId, Section assignedSection) {
        super(name, email, phone, studentId);
        this.assignedSection = assignedSection;
        this.taId = generateTaId();
    }

    /**
     * Reconstruction constructor — used when loading a TeachingAssistant back
     * from a saved file, where the taId already exists and must be restored
     * exactly, not regenerated. Also seeds the counter so any *new* TA
     * created afterward won't collide with IDs that were loaded from disk.
     */
    public TeachingAssistant(String name, String email, String phone, String studentId,
                             Section assignedSection, String taId) {
        super(name, email, phone, studentId);
        this.assignedSection = assignedSection;
        this.taId = taId;
        seedCounterIfHigher(taId);
    }

    private static synchronized void seedCounterIfHigher(String loadedTaId) {
        try {
            int num = Integer.parseInt(loadedTaId.replaceAll("[^0-9]", ""));
            if (num > taCounter) {
                taCounter = num;
            }
        } catch (NumberFormatException e) {
            // Malformed/unexpected ID format — leave the counter as-is.
        }
    }

    private static synchronized String generateTaId() {
        taCounter++;
        return String.format("TA%03d", taCounter);
    }

    public String getTaId() {
        return taId;
    }

    public Section getAssignedSection() {
        return assignedSection;
    }

    public void setAssignedSection(Section assignedSection) {
        this.assignedSection = assignedSection;
    }

    public Assignment createAssignment(String title, String description, LocalDate deadline, double totalMarks) {
        // The diagram's signature has no id parameter, so we generate one —
        // a random UUID is the simplest way to guarantee uniqueness here.
        String id = UUID.randomUUID().toString();
        return new Assignment(id, title, description, deadline, totalMarks, assignedSection, this);
    }

    public List<Submission> viewSubmissions(Assignment assignment) {
        return assignment.getSubmissions();
    }

    public void evaluateSubmission(Submission submission, double marks) {
        submission.assignMarks(marks);
    }

    public void giveFeedback(Submission submission, String comments) {
        Feedback feedback = new Feedback(
                UUID.randomUUID().toString(), this, comments, LocalDate.now(), submission.getSubmissionId());
        submission.addFeedback(feedback);
    }

    /**
     * Evaluator's contract method takes no arguments, but every real
     * evaluation action here (evaluateSubmission) needs to know *which*
     * submission and *what* marks — information a no-arg method can't
     * carry. Left intentionally empty: the actual evaluation work happens
     * through evaluateSubmission()/giveFeedback() above. Worth raising with
     * your instructor if a specific behavior is expected here.
     */
    @Override
    public void evaluate() {
    }

    @Override
    public String getRole() {
        return "Teaching Assistant";
    }
}