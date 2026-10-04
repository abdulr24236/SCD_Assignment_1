package campus.io;

import campus.model.academic.Section;
import campus.model.assessment.Assignment;
import campus.model.person.TeachingAssistant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists Assignment objects. Needs both Sections and TeachingAssistants
 * already loaded first — an Assignment references a Section (which class
 * it's for) and the TeachingAssistant who created it, by ID.
 *
 * Line format: id::title::description::deadline::totalMarks::sectionId::taId
 *
 * Submissions are deliberately left out of this line — they're a Phase 3
 * concern, reconstructed from their own file and re-attached to their
 * Assignment via addSubmission() afterward.
 */
public class AssignmentRepository {
    private static final String FILE_NAME = "assignments.txt";

    public static void save(Assignment assignment) {
        FileManager.appendLine(FILE_NAME, toLine(assignment));
    }

    public static void saveAll(List<Assignment> assignments) {
        List<String> lines = new ArrayList<>();
        for (Assignment assignment : assignments) {
            lines.add(toLine(assignment));
        }
        FileManager.writeLines(FILE_NAME, lines);
    }

    public static List<Assignment> loadAll(List<Section> sections, List<TeachingAssistant> teachingAssistants) {
        List<Assignment> assignments = new ArrayList<>();
        for (String line : FileManager.readLines(FILE_NAME)) {
            assignments.add(fromLine(line, sections, teachingAssistants));
        }
        return assignments;
    }

    private static String toLine(Assignment assignment) {
        return assignment.getId() + FileManager.DELIMITER + assignment.getTitle() + FileManager.DELIMITER + assignment.getDescription()
                + FileManager.DELIMITER + assignment.getDeadline() + FileManager.DELIMITER + assignment.getTotalMarks()
                + FileManager.DELIMITER + assignment.getSection().getSectionId() + FileManager.DELIMITER + assignment.getCreatedBy().getTaId();
    }

    private static Assignment fromLine(String line, List<Section> sections, List<TeachingAssistant> teachingAssistants) {
        String[] f = line.split(FileManager.DELIMITER, -1);
        String id = f[0];
        String title = f[1];
        String description = f[2];
        LocalDate deadline = LocalDate.parse(f[3]);
        double totalMarks = Double.parseDouble(f[4]);
        String sectionId = f[5];
        String taId = f[6];

        Section section = findSectionById(sections, sectionId);
        TeachingAssistant createdBy = findTaById(teachingAssistants, taId);

        return new Assignment(id, title, description, deadline, totalMarks, section, createdBy);
    }

    private static Section findSectionById(List<Section> sections, String sectionId) {
        for (Section section : sections) {
            if (section.getSectionId().equals(sectionId)) {
                return section;
            }
        }
        return null;
    }

    private static TeachingAssistant findTaById(List<TeachingAssistant> teachingAssistants, String taId) {
        for (TeachingAssistant ta : teachingAssistants) {
            if (ta.getTaId().equals(taId)) {
                return ta;
            }
        }
        return null;
    }
}