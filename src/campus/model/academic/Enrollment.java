package campus.model.academic;

import campus.enums.EnrollmentStatus;
import campus.model.person.Student;
import java.time.LocalDate;

/**
 * Links a Student to a Section they've registered into .
 */
public class Enrollment {
    private String enrollmentId;
    private Student student;
    private Section section;
    private LocalDate enrollmentDate;
    private EnrollmentStatus status;

    public Enrollment(String enrollmentId, Student student, Section section, LocalDate enrollmentDate) {
        this.enrollmentId = enrollmentId;
        this.student = student;
        this.section = section;
        this.enrollmentDate = enrollmentDate;
        this.status = EnrollmentStatus.ACTIVE;
    }

    public void cancel() {
        status = EnrollmentStatus.DROPPED;
    }

    // Not in the diagram's method list. The constructor always sets ACTIVE,
    // which is correct for a brand-new enrollment but wrong when
    // reconstructing an old one from a saved file — this restores the
    // enrollment's real saved status (DROPPED/COMPLETED/etc).
    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public Student getStudent() {
        return student;
    }

    // Not in the diagram's method list, but needed for anything that walks
    // from a Student's enrollments back to course/schedule info (e.g.
    // Student.calculateTotalCreditHours(), viewCourses(), viewTimetable()).
    public Section getSection() {
        return section;
    }

    public String getEnrollmentId() {
        return enrollmentId;
    }

    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }
}