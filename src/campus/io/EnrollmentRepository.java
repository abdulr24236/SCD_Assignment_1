package campus.io;

import campus.enums.EnrollmentStatus;
import campus.model.academic.Enrollment;
import campus.model.academic.Section;
import campus.model.person.NormalStudent;
import campus.model.person.Student;
import campus.model.person.TeachingAssistant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists Enrollment objects. Needs Sections, NormalStudents, and
 * TeachingAssistants already loaded first.
 *
 * Line format: enrollmentId::studentTag::sectionId::enrollmentDate::status
 *
 * Deliberately bypasses Section.enroll()/Student.register(), since those
 * always create a brand-new enrollment dated today — this restores the
 * real saved date and status instead, then manually re-links the
 * Enrollment into both the Section's and the Student's own lists (which
 * enroll()/register() would normally have done automatically).
 */
public class EnrollmentRepository {
    private static final String FILE_NAME = "enrollments.txt";

    public static void save(Enrollment enrollment) {
        if (enrollment.getSection() == null || enrollment.getStudent() == null) {
            // Defensive guard — should never happen for a freshly created
            // enrollment, but never silently corrupt the file if it does.
            return;
        }
        FileManager.appendLine(FILE_NAME, toLine(enrollment));
    }

    public static void saveAll(List<Enrollment> enrollments) {
        List<String> lines = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getSection() == null || enrollment.getStudent() == null) {
                // Skip rather than crash — a corrupted enrollment (e.g. one
                // whose section no longer exists) shouldn't take down every
                // save action in the app.
                continue;
            }
            lines.add(toLine(enrollment));
        }
        FileManager.writeLines(FILE_NAME, lines);
    }

    public static List<Enrollment> loadAll(List<Section> sections, List<NormalStudent> normalStudents,
                                           List<TeachingAssistant> teachingAssistants) {
        List<Enrollment> enrollments = new ArrayList<>();
        for (String line : FileManager.readLines(FILE_NAME)) {
            Enrollment enrollment = fromLine(line, sections, normalStudents, teachingAssistants);
            if (enrollment != null) {
                enrollments.add(enrollment);
            }
        }
        return enrollments;
    }

    private static String toLine(Enrollment enrollment) {
        return enrollment.getEnrollmentId() + FileManager.DELIMITER + PersonLookup.tagStudent(enrollment.getStudent())
                + FileManager.DELIMITER + enrollment.getSection().getSectionId() + FileManager.DELIMITER + enrollment.getEnrollmentDate()
                + FileManager.DELIMITER + enrollment.getStatus();
    }

    /**
     * Returns null (instead of an Enrollment with a null section) if the
     * referenced section can't be found — this is the actual root-cause fix:
     * a null-section Enrollment used to get loaded anyway and would crash
     * every future save() call. Discarding it here is a lost line of
     * history, but better than corrupting every save from this point on.
     */
    private static Enrollment fromLine(String line, List<Section> sections, List<NormalStudent> normalStudents,
                                       List<TeachingAssistant> teachingAssistants) {
        String[] f = line.split(FileManager.DELIMITER, -1);
        String enrollmentId = f[0];
        String studentTag = f[1];
        String sectionId = f[2];
        LocalDate enrollmentDate = LocalDate.parse(f[3]);
        EnrollmentStatus status = EnrollmentStatus.valueOf(f[4]);

        Student student = PersonLookup.findStudent(studentTag, normalStudents, teachingAssistants);
        Section section = findSectionById(sections, sectionId);

        if (section == null || student == null) {
            return null;
        }

        Enrollment enrollment = new Enrollment(enrollmentId, student, section, enrollmentDate);
        enrollment.setStatus(status);

        section.registerLoadedEnrollment(enrollment);
        student.addEnrollment(enrollment);

        return enrollment;
    }

    private static Section findSectionById(List<Section> sections, String sectionId) {
        for (Section section : sections) {
            if (section.getSectionId().equals(sectionId)) {
                return section;
            }
        }
        return null;
    }
}