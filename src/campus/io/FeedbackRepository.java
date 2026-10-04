package campus.io;

import campus.model.assessment.Feedback;
import campus.model.assessment.Submission;
import campus.model.person.Evaluator;
import campus.model.person.PermanentInstructor;
import campus.model.person.TeachingAssistant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists Feedback objects. Needs Submissions, TeachingAssistants, and
 * PermanentInstructors already loaded first.
 *
 * Line format: feedbackId::evaluatorTag::comments::date::submissionId
 *
 * Feedback's own constructor already reconstructs correctly (no auto-derived
 * state), but still needs the submissionId to look up and re-link into the
 * right Submission's addFeedback() afterward.
 */
public class FeedbackRepository {
    private static final String FILE_NAME = "feedback.txt";

    public static void save(Feedback feedback) {
        FileManager.appendLine(FILE_NAME, toLine(feedback));
    }

    public static void saveAll(List<Feedback> feedbackList) {
        List<String> lines = new ArrayList<>();
        for (Feedback feedback : feedbackList) {
            lines.add(toLine(feedback));
        }
        FileManager.writeLines(FILE_NAME, lines);
    }

    public static List<Feedback> loadAll(List<Submission> submissions, List<TeachingAssistant> teachingAssistants,
                                         List<PermanentInstructor> permanentInstructors) {
        List<Feedback> feedbackList = new ArrayList<>();
        for (String line : FileManager.readLines(FILE_NAME)) {
            feedbackList.add(fromLine(line, submissions, teachingAssistants, permanentInstructors));
        }
        return feedbackList;
    }

    private static String toLine(Feedback feedback) {
        return feedback.getFeedbackId() + FileManager.DELIMITER + PersonLookup.tagEvaluator(feedback.getEvaluator())
                + FileManager.DELIMITER + feedback.getComments() + FileManager.DELIMITER + feedback.getDate() + FileManager.DELIMITER + feedback.getSubmissionId();
    }

    private static Feedback fromLine(String line, List<Submission> submissions,
                                     List<TeachingAssistant> teachingAssistants,
                                     List<PermanentInstructor> permanentInstructors) {
        String[] f = line.split(FileManager.DELIMITER, -1);
        String feedbackId = f[0];
        String evaluatorTag = f[1];
        String comments = f[2];
        LocalDate date = LocalDate.parse(f[3]);
        String submissionId = f[4];

        Evaluator evaluator = PersonLookup.findEvaluator(evaluatorTag, teachingAssistants, permanentInstructors);
        Feedback feedback = new Feedback(feedbackId, evaluator, comments, date, submissionId);

        Submission submission = findSubmissionById(submissions, submissionId);
        if (submission != null) {
            submission.addFeedback(feedback);
        }

        return feedback;
    }

    private static Submission findSubmissionById(List<Submission> submissions, String submissionId) {
        for (Submission submission : submissions) {
            if (submission.getSubmissionId().equals(submissionId)) {
                return submission;
            }
        }
        return null;
    }
}