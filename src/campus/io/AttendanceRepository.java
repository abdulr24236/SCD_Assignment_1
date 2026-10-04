package campus.io;

import campus.enums.AttendanceStatus;
import campus.model.academic.Attendance;
import campus.model.academic.Section;
import campus.model.person.NormalStudent;
import campus.model.person.Student;
import campus.model.person.TeachingAssistant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists Attendance objects. Needs Sections, NormalStudents, and
 * TeachingAssistants already loaded first.
 *
 * Line format: studentTag::sectionId::date::status
 *
 * Unlike Enrollment/Submission, Attendance's constructor already takes the
 * date and status directly (no auto-derived "today" logic) and already
 * self-registers into its Section — so the normal constructor works fine
 * for reconstruction with no special-casing needed.
 */
public class AttendanceRepository {
    private static final String FILE_NAME = "attendance.txt";

    public static void save(Attendance attendance) {
        FileManager.appendLine(FILE_NAME, toLine(attendance));
    }

    public static void saveAll(List<Attendance> records) {
        List<String> lines = new ArrayList<>();
        for (Attendance record : records) {
            lines.add(toLine(record));
        }
        FileManager.writeLines(FILE_NAME, lines);
    }

    public static List<Attendance> loadAll(List<Section> sections, List<NormalStudent> normalStudents,
                                           List<TeachingAssistant> teachingAssistants) {
        List<Attendance> records = new ArrayList<>();
        for (String line : FileManager.readLines(FILE_NAME)) {
            records.add(fromLine(line, sections, normalStudents, teachingAssistants));
        }
        return records;
    }

    private static String toLine(Attendance attendance) {
        return PersonLookup.tagStudent(attendance.getStudent()) + FileManager.DELIMITER + attendance.getSection().getSectionId()
                + FileManager.DELIMITER + attendance.getDate() + FileManager.DELIMITER + attendance.getStatus();
    }

    private static Attendance fromLine(String line, List<Section> sections, List<NormalStudent> normalStudents,
                                       List<TeachingAssistant> teachingAssistants) {
        String[] f = line.split(FileManager.DELIMITER, -1);
        String studentTag = f[0];
        String sectionId = f[1];
        LocalDate date = LocalDate.parse(f[2]);
        AttendanceStatus status = AttendanceStatus.valueOf(f[3]);

        Student student = PersonLookup.findStudent(studentTag, normalStudents, teachingAssistants);
        Section section = findSectionById(sections, sectionId);

        // Constructor auto-registers into section's attendanceRecords list —
        // no manual re-linking needed here, unlike Enrollment.
        return new Attendance(student, section, date, status);
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