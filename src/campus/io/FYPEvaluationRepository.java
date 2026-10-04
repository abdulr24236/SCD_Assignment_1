package campus.io;

import campus.model.fyp.FYPEvaluation;
import campus.model.fyp.FYPGroup;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists FYPEvaluation objects. Needs FYPGroups already loaded first.
 *
 * Line format: evaluationId::evaluationDate::groupId::score::feedback
 *
 * score and feedback aren't constructor parameters (only set later via
 * evaluate()/addFeedback()), so both are restored after construction.
 * Re-links into the FYPGroup's evaluations list via addEvaluation()
 * afterward, same reasoning as FYPMeetingRepository.
 */
public class FYPEvaluationRepository {
    private static final String FILE_NAME = "fyp_evaluations.txt";

    public static void save(FYPEvaluation evaluation) {
        FileManager.appendLine(FILE_NAME, toLine(evaluation));
    }

    public static void saveAll(List<FYPEvaluation> evaluations) {
        List<String> lines = new ArrayList<>();
        for (FYPEvaluation evaluation : evaluations) {
            lines.add(toLine(evaluation));
        }
        FileManager.writeLines(FILE_NAME, lines);
    }

    public static List<FYPEvaluation> loadAll(List<FYPGroup> groups) {
        List<FYPEvaluation> evaluations = new ArrayList<>();
        for (String line : FileManager.readLines(FILE_NAME)) {
            evaluations.add(fromLine(line, groups));
        }
        return evaluations;
    }

    private static String toLine(FYPEvaluation evaluation) {
        String feedback = evaluation.getFeedback() == null ? "" : evaluation.getFeedback();
        return evaluation.getEvaluationId() + FileManager.DELIMITER + evaluation.getEvaluationDate() + FileManager.DELIMITER
                + evaluation.getGroupId() + FileManager.DELIMITER + evaluation.getScore() + FileManager.DELIMITER + feedback;
    }

    private static FYPEvaluation fromLine(String line, List<FYPGroup> groups) {
        String[] f = line.split(FileManager.DELIMITER, -1);
        String evaluationId = f[0];
        LocalDate evaluationDate = LocalDate.parse(f[1]);
        String groupId = f[2];
        double score = Double.parseDouble(f[3]);
        String feedback = f.length > 4 ? f[4] : "";

        FYPEvaluation evaluation = new FYPEvaluation(evaluationId, evaluationDate, groupId);
        evaluation.evaluate(score);
        if (!feedback.isEmpty()) {
            evaluation.addFeedback(feedback);
        }

        FYPGroup group = findGroupById(groups, groupId);
        if (group != null) {
            group.addEvaluation(evaluation);
        }

        return evaluation;
    }

    private static FYPGroup findGroupById(List<FYPGroup> groups, String groupId) {
        for (FYPGroup group : groups) {
            if (group.getGroupId().equals(groupId)) {
                return group;
            }
        }
        return null;
    }
}