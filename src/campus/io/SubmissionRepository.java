package campus.io;

import campus.enums.SubmissionStatus;
import campus.model.assessment.Assignment;
import campus.model.assessment.Submission;
import campus.model.person.NormalStudent;
import campus.model.person.Student;
import campus.model.person.TeachingAssistant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists Submission objects. Needs Assignments, NormalStudents, and
 * TeachingAssistants already loaded first.
 *
 * Line format: submissionId::assignmentId::studentTag::content::submissionDate::status::marks
 *
 * Uses Submission's reconstruction constructor (the one taking explicit
 * date/status/marks) rather than the normal one, which would recompute
 * status against today's date and lose any assigned marks. Also manually
 * re-links into the Assignment's submissions list, since neither
 * constructor does that automatically (unlike Attendance's self-registration).
 */
public class SubmissionRepository {
    private static final String FILE_NAME = "submissions.txt";

    public static void save(Submission submission) {
        FileManager.appendLine(FILE_NAME, toLine(submission));
    }

    public static void saveAll(List<Submission> submissions) {
        List<String> lines = new ArrayList<>();
        for (Submission submission : submissions) {
            lines.add(toLine(submission));
        }
        FileManager.writeLines(FILE_NAME, lines);
    }

    public static List<Submission> loadAll(List<Assignment> assignments, List<NormalStudent> normalStudents,
                                           List<TeachingAssistant> teachingAssistants) {
        List<Submission> submissions = new ArrayList<>();
        for (String line : FileManager.readLines(FILE_NAME)) {
            submissions.add(fromLine(line, assignments, normalStudents, teachingAssistants));
        }
        return submissions;
    }

    private static String toLine(Submission submission) {
        return submission.getSubmissionId() + FileManager.DELIMITER + submission.getAssignment().getId() + FileManager.DELIMITER
                + PersonLookup.tagStudent(submission.getStudent()) + FileManager.DELIMITER + submission.getContent()
                + FileManager.DELIMITER + submission.getSubmissionDate() + FileManager.DELIMITER + submission.getStatus() + FileManager.DELIMITER
                + submission.getMarks();
    }

    private static Submission fromLine(String line, List<Assignment> assignments, List<NormalStudent> normalStudents,
                                       List<TeachingAssistant> teachingAssistants) {
        String[] f = line.split(FileManager.DELIMITER, -1);
        String submissionId = f[0];
        String assignmentId = f[1];
        String studentTag = f[2];
        String content = f[3];
        LocalDate submissionDate = LocalDate.parse(f[4]);
        SubmissionStatus status = SubmissionStatus.valueOf(f[5]);
        double marks = Double.parseDouble(f[6]);

        Assignment assignment = findAssignmentById(assignments, assignmentId);
        Student student = PersonLookup.findStudent(studentTag, normalStudents, teachingAssistants);

        Submission submission = new Submission(
                submissionId, assignment, student, content, submissionDate, status, marks);

        if (assignment != null) {
            assignment.addSubmission(submission);
        }

        return submission;
    }

    private static Assignment findAssignmentById(List<Assignment> assignments, String assignmentId) {
        for (Assignment assignment : assignments) {
            if (assignment.getId().equals(assignmentId)) {
                return assignment;
            }
        }
        return null;
    }
}