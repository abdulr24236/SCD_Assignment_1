package campus.io;

import campus.enums.Day;
import campus.model.academic.Course;
import campus.model.academic.Schedule;
import campus.model.academic.Section;
import campus.model.person.Instructor;
import campus.model.person.PermanentInstructor;
import campus.model.person.VisitingInstructor;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists Section objects. Not a plain Repository<T> like Phase 1, because
 * reconstructing a Section needs to look up its Course and its Instructor
 * (which could be either concrete Instructor subtype) from already-loaded
 * lists — context a bare Function<String, Section> has no way to receive.
 *
 * Line format: sectionId::capacity::courseCode::instructorType::instructorId::day::startTime::endTime::room
 * instructorType is "V" (VisitingInstructor), "P" (PermanentInstructor), or "NONE".
 * day/startTime/endTime/room are "NONE" if no schedule has been assigned yet.
 *
 * teachingAssistant and enrollments/attendanceRecords are deliberately left
 * out of this line — TeachingAssistant is loaded afterward and links itself
 * back in (see TeachingAssistantRepository), and enrollments/attendance are
 * Phase 3 concerns reconstructed from their own files.
 */
public class SectionRepository {
    private static final String FILE_NAME = "sections.txt";

    public static void save(Section section) {
        FileManager.appendLine(FILE_NAME, toLine(section));
    }

    public static void saveAll(List<Section> sections) {
        List<String> lines = new ArrayList<>();
        for (Section section : sections) {
            lines.add(toLine(section));
        }
        FileManager.writeLines(FILE_NAME, lines);
    }

    public static List<Section> loadAll(List<Course> courses,
                                        List<VisitingInstructor> visitingInstructors,
                                        List<PermanentInstructor> permanentInstructors) {
        List<Section> sections = new ArrayList<>();
        for (String line : FileManager.readLines(FILE_NAME)) {
            sections.add(fromLine(line, courses, visitingInstructors, permanentInstructors));
        }
        return sections;
    }

    private static String toLine(Section section) {
        Instructor instructor = section.getInstructor();
        String instructorType = "NONE";
        String instructorId = "";
        if (instructor instanceof VisitingInstructor visiting) {
            instructorType = "V";
            instructorId = visiting.getTeacherId();
        } else if (instructor instanceof PermanentInstructor permanent) {
            instructorType = "P";
            instructorId = permanent.getTeacherId();
        }

        Schedule schedule = section.getSchedule();
        String day = schedule == null ? "NONE" : schedule.getDay().name();
        String start = schedule == null ? "NONE" : schedule.getStartTime().toString();
        String end = schedule == null ? "NONE" : schedule.getEndTime().toString();
        String room = schedule == null ? "NONE" : schedule.getRoom();

        return section.getSectionId() + FileManager.DELIMITER + section.getCapacity() + FileManager.DELIMITER + section.getCourse().getCourseCode()
                + FileManager.DELIMITER + instructorType + FileManager.DELIMITER + instructorId + FileManager.DELIMITER + day + FileManager.DELIMITER + start + FileManager.DELIMITER + end + FileManager.DELIMITER + room;
    }

    private static Section fromLine(String line, List<Course> courses,
                                    List<VisitingInstructor> visitingInstructors,
                                    List<PermanentInstructor> permanentInstructors) {
        String[] f = line.split(FileManager.DELIMITER, -1);
        String sectionId = f[0];
        int capacity = Integer.parseInt(f[1]);
        String courseCode = f[2];
        String instructorType = f[3];
        String instructorId = f[4];
        String day = f[5];
        String start = f[6];
        String end = f[7];
        String room = f[8];

        Course course = findCourseByCode(courses, courseCode);
        Section section = new Section(sectionId, capacity, course);

        if (instructorType.equals("V")) {
            section.assignInstructor(findVisitingById(visitingInstructors, instructorId));
        } else if (instructorType.equals("P")) {
            section.assignInstructor(findPermanentById(permanentInstructors, instructorId));
        }

        if (!day.equals("NONE")) {
            section.setSchedule(new Schedule(Day.valueOf(day), LocalTime.parse(start), LocalTime.parse(end), room));
        }

        return section;
    }

    private static Course findCourseByCode(List<Course> courses, String code) {
        for (Course course : courses) {
            if (course.getCourseCode().equals(code)) {
                return course;
            }
        }
        return null;
    }

    private static VisitingInstructor findVisitingById(List<VisitingInstructor> instructors, String id) {
        for (VisitingInstructor instructor : instructors) {
            if (instructor.getTeacherId().equals(id)) {
                return instructor;
            }
        }
        return null;
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