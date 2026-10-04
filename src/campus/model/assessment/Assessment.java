package campus.model.assessment;

import java.time.LocalDate;

/**
 * Base for gradeable work  currently just Assignment, but structured so a
 * future assessment type (e.g. a quiz) could extend it too .
 */
public abstract class Assessment {
    private String id;
    private String title;
    private String description;
    private LocalDate deadline;
    private double totalMarks;

    protected Assessment(String id, String title, String description, LocalDate deadline, double totalMarks) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.totalMarks = totalMarks;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public double getTotalMarks() {
        return totalMarks;
    }
}