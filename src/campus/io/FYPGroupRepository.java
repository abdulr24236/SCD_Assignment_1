package campus.io;

import campus.model.fyp.FYPGroup;
import campus.model.person.NormalStudent;
import campus.model.person.PermanentInstructor;
import campus.model.person.Student;
import campus.model.person.TeachingAssistant;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists FYPGroup objects. Needs PermanentInstructors, NormalStudents, and
 * TeachingAssistants already loaded first.
 *
 * Line format: groupId::title::description::supervisorId::member1;member2;...
 *
 * members is multi-valued (a group can have several students), so it needs
 * a second delimiter — semicolons separate individual members, each still
 * using PersonLookup's "type:id" tag internally. An empty members section
 * (no semicolons) means no members yet.
 */
public class FYPGroupRepository {
    private static final String FILE_NAME = "fyp_groups.txt";

    public static void save(FYPGroup group) {
        FileManager.appendLine(FILE_NAME, toLine(group));
    }

    public static void saveAll(List<FYPGroup> groups) {
        List<String> lines = new ArrayList<>();
        for (FYPGroup group : groups) {
            lines.add(toLine(group));
        }
        FileManager.writeLines(FILE_NAME, lines);
    }

    public static List<FYPGroup> loadAll(List<PermanentInstructor> permanentInstructors,
                                         List<NormalStudent> normalStudents,
                                         List<TeachingAssistant> teachingAssistants) {
        List<FYPGroup> groups = new ArrayList<>();
        for (String line : FileManager.readLines(FILE_NAME)) {
            groups.add(fromLine(line, permanentInstructors, normalStudents, teachingAssistants));
        }
        return groups;
    }

    private static String toLine(FYPGroup group) {
        String supervisorId = group.getSupervisor() == null ? "" : group.getSupervisor().getTeacherId();
        StringBuilder members = new StringBuilder();
        for (Student student : group.getMembers()) {
            if (members.length() > 0) {
                members.append(";");
            }
            members.append(PersonLookup.tagStudent(student));
        }
        return group.getGroupId() + FileManager.DELIMITER + group.getTitle() + FileManager.DELIMITER + group.getDescription()
                + FileManager.DELIMITER + supervisorId + FileManager.DELIMITER + members;
    }

    private static FYPGroup fromLine(String line, List<PermanentInstructor> permanentInstructors,
                                     List<NormalStudent> normalStudents, List<TeachingAssistant> teachingAssistants) {
        String[] f = line.split(FileManager.DELIMITER, -1);
        String groupId = f[0];
        String title = f[1];
        String description = f[2];
        String supervisorId = f[3];
        String membersField = f.length > 4 ? f[4] : "";

        FYPGroup group = new FYPGroup(groupId, title, description);

        if (!supervisorId.isEmpty()) {
            PermanentInstructor supervisor = findPermanentById(permanentInstructors, supervisorId);
            if (supervisor != null) {
                group.assignSupervisor(supervisor);
            }
        }

        if (!membersField.isEmpty()) {
            for (String memberTag : membersField.split(";")) {
                Student student = PersonLookup.findStudent(memberTag, normalStudents, teachingAssistants);
                if (student != null) {
                    group.addMember(student);
                }
            }
        }

        return group;
    }

    private static PermanentInstructor findPermanentById(List<PermanentInstructor> instructors, String id) {
        for (PermanentInstructor instructor : instructors) {
            if (instructor.getTeacherId().equals(id)) {
                return instructor;
            }
        }
        return null;
    }
}