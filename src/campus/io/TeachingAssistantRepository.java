package campus.io;

import campus.model.academic.Section;
import campus.model.person.TeachingAssistant;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists TeachingAssistant objects. Needs Sections already loaded first
 * (SectionRepository.loadAll()), since each line references its assigned
 * Section by ID.
 *
 * Line format: studentId::taId::name::email::phone::sectionId
 *
 * After reconstructing each TeachingAssistant, this also calls
 * section.assignTA(ta) — completing the bidirectional link that
 * SectionRepository intentionally left out (Section -> TeachingAssistant
 * and TeachingAssistant -> Section reference each other, so one of the two
 * directions has to be filled in after both objects exist).
 */
public class TeachingAssistantRepository {
    private static final String FILE_NAME = "teaching_assistants.txt";

    public static void save(TeachingAssistant ta) {
        FileManager.appendLine(FILE_NAME, toLine(ta));
    }

    public static void saveAll(List<TeachingAssistant> tas) {
        List<String> lines = new ArrayList<>();
        for (TeachingAssistant ta : tas) {
            lines.add(toLine(ta));
        }
        FileManager.writeLines(FILE_NAME, lines);
    }

    public static List<TeachingAssistant> loadAll(List<Section> sections) {
        List<TeachingAssistant> tas = new ArrayList<>();
        for (String line : FileManager.readLines(FILE_NAME)) {
            TeachingAssistant ta = fromLine(line, sections);
            tas.add(ta);
            if (ta.getAssignedSection() != null) {
                ta.getAssignedSection().assignTA(ta);
            }
        }
        return tas;
    }

    private static String toLine(TeachingAssistant ta) {
        String sectionId = ta.getAssignedSection() == null ? "" : ta.getAssignedSection().getSectionId();
        return ta.getStudentId() + FileManager.DELIMITER + ta.getTaId() + FileManager.DELIMITER + ta.getName() + FileManager.DELIMITER
                + ta.getEmail() + FileManager.DELIMITER + ta.getPhone() + FileManager.DELIMITER + sectionId;
    }

    private static TeachingAssistant fromLine(String line, List<Section> sections) {
        String[] f = line.split(FileManager.DELIMITER, -1);
        String studentId = f[0];
        String taId = f[1];
        String name = f[2];
        String email = f[3];
        String phone = f[4];
        String sectionId = f[5];

        Section section = findSectionById(sections, sectionId);
        return new TeachingAssistant(name, email, phone, studentId, section, taId);
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