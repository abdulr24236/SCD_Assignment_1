package campus.model.fyp;

import java.time.LocalDate;

/**
 * A supervisor's score and feedback on an FYP idea, submitted separately
 * from creation via evaluate()/addFeedback() .
 */
public class FYPEvaluation {
    private String evaluationId;
    private LocalDate evaluationDate;
    private double score;
    private String feedback;
    // Not in the diagram's field list — needed to reattach a reloaded
    // FYPEvaluation to the right FYPGroup, same gap as FYPMeeting's groupId.
    private String groupId;

    public FYPEvaluation(String evaluationId, LocalDate evaluationDate, String groupId) {
        this.evaluationId = evaluationId;
        this.evaluationDate = evaluationDate;
        this.groupId = groupId;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getEvaluationId() {
        return evaluationId;
    }

    public LocalDate getEvaluationDate() {
        return evaluationDate;
    }

    public void evaluate(double score) {
        this.score = score;
    }

    public void addFeedback(String feedback) {
        this.feedback = feedback;
    }

    public double getScore() {
        return score;
    }

    public String getFeedback() {
        return feedback;
    }
}