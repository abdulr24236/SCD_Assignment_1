package campus.model.assessment;

import campus.model.academic.Section;
import campus.model.person.TeachingAssistant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A single assignment within a Section, created by a TeachingAssistant
 */
public class Assignment extends Assessment {
    private Section section;
    private TeachingAssistant createdBy;
    private List<Submission> submissions = new ArrayList<>();

    public Assignment(String id, String title, String description, LocalDate deadline,
                      double totalMarks, Section section, TeachingAssistant createdBy) {
        super(id, title, description, deadline, totalMarks);
        this.section = section;
        this.createdBy = createdBy;
    }

    public void addSubmission(Submission submission) {
        submissions.add(submission);
    }

    public List<Submission> getSubmissions() {
        return submissions;
    }

    public boolean isDeadlinePassed() {
        return LocalDate.now().isAfter(getDeadline());
    }

    // Not in the diagram's method list, but a repository needs these to
    // serialize/reference which Section and TeachingAssistant this
    // assignment belongs to.
    public Section getSection() {
        return section;
    }

    public TeachingAssistant getCreatedBy() {
        return createdBy;
    }
}