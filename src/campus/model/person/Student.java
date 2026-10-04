package campus.model.person;

import campus.enums.EnrollmentStatus;
import campus.exceptions.CourseClashException;
import campus.exceptions.CourseFullException;
import campus.model.academic.Course;
import campus.model.academic.Enrollment;
import campus.model.academic.Schedule;
import campus.model.academic.Section;
import campus.model.assessment.Assignment;
import campus.model.assessment.Submission;
import campus.model.request.CourseClashRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Base for both kinds of student in the system: NormalStudent and
 * TeachingAssistant (diagram 3.1). Holds enrollment/credit-hour bookkeeping
 * shared by both.
 */
public abstract class Student extends Person {
    private String studentId;
    private int totalCreditHours;
    private List<Enrollment> enrollments = new ArrayList<>();

    public Student(String name, String email, String phone, String studentId) {
        super(name, email, phone);
        this.studentId = studentId;
    }

    public String getStudentId() {
        return studentId;
    }

    /**
     * Registers this student into a section, throwing if the section is full
     * or clashes with an already-registered section.
     */
    public void register(Section section) throws CourseFullException, CourseClashException {
        if (section.isFull()) {
            throw new CourseFullException("Section " + section + " has no available seats.");
        }
        for (Enrollment existing : enrollments) {
            if (existing.getStatus() == EnrollmentStatus.ACTIVE
                    && section.hasClash(existing.getSection())) {
                throw new CourseClashException(
                        "Section " + section + " clashes with an already-registered section.");
            }
        }
        section.enroll(this);
    }

    public void drop(Section section) {
        section.drop(this);
    }

    // Not in the diagram's method list — Section.enroll()/drop() need a way
    // to keep this student's own enrollments list in sync, since
    // Section.enroll(student):void has no return value to hand an
    // Enrollment back through.
    public void addEnrollment(Enrollment enrollment) {
        enrollments.add(enrollment);
    }

    public void removeEnrollment(Enrollment enrollment) {
        enrollments.remove(enrollment);
    }

    public List<Enrollment> getEnrollments() {
        return enrollments;
    }

    public int calculateTotalCreditHours() {
        int total = 0;
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
                total += enrollment.getSection().getCourse().getCreditHours();
            }
        }
        this.totalCreditHours = total;
        return total;
    }

    public List<Course> viewCourses() {
        List<Course> courses = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            courses.add(enrollment.getSection().getCourse());
        }
        return courses;
    }

    public List<Schedule> viewTimetable() {
        List<Schedule> schedules = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            schedules.add(enrollment.getSection().getSchedule());
        }
        return schedules;
    }

    public Submission submitAssignment(Assignment assignment, String content) {
        String submissionId = assignment.getId() + "-" + studentId;
        Submission submission = new Submission(submissionId, assignment, this, content);
        assignment.addSubmission(submission);
        return submission;
    }

    public void submitCourseClashRequest(CourseClashRequest request) {
        request.submit();
    }
}