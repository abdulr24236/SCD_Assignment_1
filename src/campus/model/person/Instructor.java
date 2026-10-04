package campus.model.person;

import campus.enums.AttendanceStatus;
import campus.model.academic.Attendance;
import campus.model.academic.Course;
import campus.model.academic.Section;
import java.util.ArrayList;
import java.util.List;

/**
 * Base for VisitingInstructor and PermanentInstructor (diagram 3.1). Shared
 * behavior: viewing assigned courses/sections, and marking/updating
 * attendance for enrolled students.
 */
public abstract class Instructor extends Person {
    private String teacherId;
    private List<Section> assignedSections = new ArrayList<>();

    public Instructor(String name, String email, String phone, String teacherId) {
        super(name, email, phone);
        this.teacherId = teacherId;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public List<Course> viewCourses() {
        List<Course> courses = new ArrayList<>();
        for (Section section : assignedSections) {
            courses.add(section.getCourse());
        }
        return courses;
    }

    public List<Section> viewSections() {
        return assignedSections;
    }

    // Not in the diagram's method list. assignInstructor() on Section only
    // ever set the Section's own instructor field — nothing ever added the
    // Section into the instructor's own assignedSections, so viewCourses()/
    // viewSections() would always return empty otherwise.
    public void addAssignedSection(Section section) {
        assignedSections.add(section);
    }

    public List<Student> viewEnrolledStudents(Section section) {
        return section.getEnrolledStudents();
    }

    public void markAttendance(Attendance attendance, AttendanceStatus status) {
        attendance.setStatus(status);
    }

    public void updateAttendance(Attendance attendance, AttendanceStatus status) {
        attendance.setStatus(status);
    }

    /** Percentage of this student's attendance records in this section marked PRESENT or LATE. */
    public double calculateAttendancePercentage(Student student, Section section) {
        int total = 0;
        int attended = 0;
        for (Attendance record : section.getAttendanceRecords()) {
            if (record.getStudent() == student) {
                total++;
                if (record.getStatus() == AttendanceStatus.PRESENT || record.getStatus() == AttendanceStatus.LATE) {
                    attended++;
                }
            }
        }
        return total == 0 ? 0.0 : (attended * 100.0) / total;
    }
}