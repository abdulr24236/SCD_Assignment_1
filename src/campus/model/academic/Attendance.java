package campus.model.academic;

import campus.enums.AttendanceStatus;
import campus.model.person.Student;
import java.time.LocalDate;

/**
 * One attendance record: a Student's status on a given date for a given.
 */
public class Attendance {
    private Student student;
    private Section section;
    private LocalDate date;
    private AttendanceStatus status;

    /**
     * Registers itself into the Section's attendance records on creation.
     * Section.getAttendanceRecords() isn't in the diagram's own method list,
     * but calculateAttendancePercentage() has nowhere else to pull records
     * from, so this is the plumbing that makes that method actually work.
     */
    public Attendance(Student student, Section section, LocalDate date, AttendanceStatus status) {
        this.student = student;
        this.section = section;
        this.date = date;
        this.status = status;
        if (section != null) {
            section.addAttendanceRecord(this);
        }
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public Student getStudent() {
        return student;
    }

    public Section getSection() {
        return section;
    }

    public LocalDate getDate() {
        return date;
    }
}