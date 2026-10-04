package campus.model.academic;

import campus.model.person.Instructor;
import campus.model.person.Student;
import campus.model.person.TeachingAssistant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A specific offering of a Course: one instructor, a room/time slot, a
 * capacity, and the students enrolled in it .
 */
public class Section {
    private String sectionId;
    private int capacity;
    private Course course;
    private Instructor instructor;
    private TeachingAssistant teachingAssistant;
    private Schedule schedule;
    private List<Enrollment> enrollments = new ArrayList<>();
    // Not in the diagram's field list — Instructor.calculateAttendancePercentage()
    // needs a place to look up every Attendance record for this section.
    private List<Attendance> attendanceRecords = new ArrayList<>();

    public Section(String sectionId, int capacity, Course course) {
        this.sectionId = sectionId;
        this.capacity = capacity;
        this.course = course;
    }

    public void enroll(Student student) {
        // A random UUID rather than sectionId + studentId: that scheme gave a
        // dropped-then-re-registered enrollment the same ID as the old one.
        Enrollment enrollment = new Enrollment(
                UUID.randomUUID().toString(), student, this, java.time.LocalDate.now());
        enrollments.add(enrollment);
        student.addEnrollment(enrollment);
    }

    public void drop(Student student) {
        Enrollment match = null;
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getStudent() == student) {
                match = enrollment;
                break;
            }
        }
        if (match != null) {
            match.cancel();
            enrollments.remove(match);
            student.removeEnrollment(match);
        }
    }

    public boolean isFull() {
        return enrollments.size() >= capacity;
    }

    public int getAvailableSeats() {
        return capacity - enrollments.size();
    }

    public void assignInstructor(Instructor instructor) {
        this.instructor = instructor;
        if (instructor != null) {
            instructor.addAssignedSection(this);
        }
    }

    public void assignTA(TeachingAssistant ta) {
        this.teachingAssistant = ta;
    }

    // Not in the diagram's method list. assignTA() silently overwrites, so
    // anything promoting a TA needs a way to check first whether the section
    // already has one (the diagram gives a section at most one TA).
    public TeachingAssistant getTeachingAssistant() {
        return teachingAssistant;
    }

    public List<Student> getEnrolledStudents() {
        List<Student> students = new ArrayList<>();
        for (Enrollment e : enrollments) {
            students.add(e.getStudent());
        }
        return students;
    }

    // Not in the diagram's method list. register()/enroll() correctly create
    // and link Enrollments internally, but neither hands the Enrollment
    // object back to the caller (matching the diagram's void return types) —
    // so anything that needs to persist a just-created Enrollment (like
    // DataStore's seeding) has no way to get it without this.
    public List<Enrollment> getEnrollments() {
        return enrollments;
    }

    // Not in the diagram's method list, but a repository needs these to
    // serialize a Section at all.
    public String getSectionId() {
        return sectionId;
    }

    public int getCapacity() {
        return capacity;
    }

    public Instructor getInstructor() {
        return instructor;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public boolean hasClash(Section other) {
        return schedule != null && other.schedule != null && schedule.hasClash(other.schedule);
    }

    // Not in the diagram's method list, but AcademicOfficeAdmin.createSection()/
    // Student.viewCourses()/Instructor.viewCourses() all need to walk from a
    // Section back to its Course.
    public Course getCourse() {
        return course;
    }

    // Not in the diagram's method list, but AcademicOfficeAdmin.setCapacity()
    // has nothing to call otherwise.
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    // Not in the diagram's method list, but AcademicOfficeAdmin.assignRoom()
    // has nothing to call otherwise.
    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public void addAttendanceRecord(Attendance attendance) {
        attendanceRecords.add(attendance);
    }

    // Not in the diagram's method list. enroll() always creates a brand-new
    // Enrollment dated today — wrong for reloading a saved one with its
    // real original date/status. This just appends an already-constructed
    // Enrollment to this section's list, for reload use only.
    public void registerLoadedEnrollment(Enrollment enrollment) {
        enrollments.add(enrollment);
    }

    public List<Attendance> getAttendanceRecords() {
        return attendanceRecords;
    }
}