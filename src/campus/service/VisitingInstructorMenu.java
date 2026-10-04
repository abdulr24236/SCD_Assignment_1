/**
 * ============================================================
 * File        : VisitingInstructorMenu.java
 */
package campus.service;

import campus.enums.AttendanceStatus;
import campus.io.AttendanceRepository;
import campus.logging.AppLogger;
import campus.model.academic.Attendance;
import campus.model.academic.Course;
import campus.model.academic.Section;
import campus.model.person.NormalStudent;
import campus.model.person.Student;
import campus.model.person.TeachingAssistant;
import campus.model.person.VisitingInstructor;
import campus.util.InputHelper;
import campus.util.UIHelper;
import java.time.LocalDate;
import java.util.Scanner;

public class VisitingInstructorMenu {
    private final VisitingInstructor instructor;
    private final Scanner scanner;
    private final InputHelper inputHelper;
    private final DataStore dataStore;

    public VisitingInstructorMenu(VisitingInstructor instructor, Scanner scanner, DataStore dataStore) {
        this.instructor = instructor;
        this.scanner = scanner;
        this.inputHelper = new InputHelper(scanner);
        this.dataStore = dataStore;
    }

    public void show() {
        boolean loggedIn = true;
        while (loggedIn) {
            UIHelper.printUserHeader("Visiting Instructor", instructor.getName(), instructor.getTeacherId());
            String[] options = {
                    "[1] View assigned courses",
                    "[2] View assigned sections",
                    "[3] View enrolled students in a section",
                    "[4] Mark daily student attendance",
                    "[5] Update an existing attendance record",
                    "[6] Calculate attendance rate & eligibility",
                    "[7] Log out"
            };
            UIHelper.printMenuCard("Faculty Portal — Visiting Instructor", options);

            int choice = inputHelper.readInt("Choose an option [1-7]: ", 1, 7);
            switch (choice) {
                case 1 -> viewAssignedCourses();
                case 2 -> viewAssignedSections();
                case 3 -> viewEnrolledStudents();
                case 4 -> markAttendance();
                case 5 -> updateAttendance();
                case 6 -> calculateAttendancePercentage();
                case 7 -> {
                    loggedIn = false;
                    UIHelper.info("Logging out from Faculty Portal.");
                }
            }
        }
    }

    private void viewAssignedCourses() {
        UIHelper.printSectionHeader("Assigned Courses");
        if (instructor.viewCourses().isEmpty()) {
            UIHelper.info("No courses currently assigned to you.");
            return;
        }
        for (Course course : instructor.viewCourses()) {
            System.out.println("   • " + UIHelper.BOLD + course.getCourseCode() + UIHelper.RESET + " — " + course.getTitle()
                    + " (" + course.getCreditHours() + " credit hours)");
        }
    }

    private void viewAssignedSections() {
        UIHelper.printSectionHeader("Assigned Sections");
        if (instructor.viewSections().isEmpty()) {
            UIHelper.info("No sections currently assigned to you.");
            return;
        }
        for (Section section : instructor.viewSections()) {
            String sched = section.getSchedule() != null ? section.getSchedule().getScheduleInfo() : "Schedule TBA";
            System.out.println("   • Section " + UIHelper.BOLD + section.getSectionId() + UIHelper.RESET + " (" + section.getCourse().getTitle()
                    + ") | Enrolled: " + section.getEnrollments().size() + "/" + section.getCapacity()
                    + " | " + sched);
        }
    }

    private void viewEnrolledStudents() {
        UIHelper.printSectionHeader("Enrolled Students Roster");
        Section section = promptForSection();
        if (section == null) {
            return;
        }
        if (instructor.viewEnrolledStudents(section).isEmpty()) {
            UIHelper.info("No students enrolled in section " + section.getSectionId() + " yet.");
            return;
        }

        System.out.println(UIHelper.CYAN + "  ┌──────────────┬─────────────────────────────┬─────────────────┐" + UIHelper.RESET);
        System.out.println(UIHelper.CYAN + "  │ " + UIHelper.BOLD + "Student ID" + UIHelper.RESET + "   │ " + UIHelper.BOLD + "Full Name" + UIHelper.RESET + "                   │ " + UIHelper.BOLD + "Email" + UIHelper.RESET + "           │");
        System.out.println(UIHelper.CYAN + "  ├──────────────┼─────────────────────────────┼─────────────────┤" + UIHelper.RESET);
        for (Student s : instructor.viewEnrolledStudents(section)) {
            System.out.printf("  │ %-12s │ %-27s │ %-15s │\n",
                    s.getStudentId(),
                    s.getName().length() > 27 ? s.getName().substring(0, 24) + "..." : s.getName(),
                    s.getEmail().length() > 15 ? s.getEmail().substring(0, 12) + "..." : s.getEmail());
        }
        System.out.println(UIHelper.CYAN + "  └──────────────┴─────────────────────────────┴─────────────────┘" + UIHelper.RESET);
    }

    private void markAttendance() {
        UIHelper.printSectionHeader("Mark Daily Student Attendance");
        Section section = promptForSection();
        if (section == null) {
            return;
        }
        if (!instructor.viewSections().contains(section)) {
            UIHelper.warning("You are not assigned to instruct section " + section.getSectionId() + ".");
            return;
        }
        Student student = promptForStudent();
        if (student == null) {
            return;
        }
        if (!section.getEnrolledStudents().contains(student)) {
            System.out.println((student) + " is not enrolled in " + section.getSectionId() + ".");
            return;
        }
        AttendanceStatus status = promptForStatus();
        if (status == null) {
            return;
        }

        if (findAttendance(section, student, LocalDate.now()) != null) {
            UIHelper.warning("Attendance for " + student.getName() + " is already marked today. Use 'Update attendance' to modify it.");
            return;
        }

        Attendance attendance = new Attendance(student, section, LocalDate.now(), status);
        dataStore.getAttendanceRecords().add(attendance);
        AttendanceRepository.save(attendance);
        UIHelper.success("Attendance marked as " + UIHelper.statusBadge(status.name()) + " for student " + student.getName() + ".");
        AppLogger.info(instructor.getTeacherId() + " marked attendance for " + student.getStudentId());
    }

    private void updateAttendance() {
        UIHelper.printSectionHeader("Update Existing Attendance Record");
        Section section = promptForSection();
        if (section == null) {
            return;
        }
        if (!instructor.viewSections().contains(section)) {
            UIHelper.warning("You are not assigned to instruct section " + section.getSectionId() + ".");
            return;
        }

        Student student = promptForStudent();
        if (student == null) {
            return;
        }
        LocalDate date = inputHelper.readDate("Enter date of record to modify");
        Attendance attendance = findAttendance(section, student, date);
        if (attendance == null) {
            UIHelper.warning("No attendance record found for that student/section on " + date + ".");
            return;
        }

        AttendanceStatus status = promptForStatus();
        if (status == null) {
            return;
        }
        instructor.updateAttendance(attendance, status);
        AttendanceRepository.saveAll(dataStore.getAttendanceRecords());
        UIHelper.success("Attendance updated to " + UIHelper.statusBadge(status.name()) + " for " + student.getName() + " on " + date + ".");
        AppLogger.info(instructor.getTeacherId() + " updated attendance for " + student.getStudentId());
    }

    private void calculateAttendancePercentage() {
        UIHelper.printSectionHeader("Calculate Attendance Rate");
        Section section = promptForSection();
        if (section == null) {
            return;
        }
        Student student = promptForStudent();
        if (student == null) {
            return;
        }
        if (!section.getEnrolledStudents().contains(student)) {
            System.out.println((student) + " is not enrolled in " + section.getSectionId() + ".");
            return;
        }

        double percentage = instructor.calculateAttendancePercentage(student, section);
        System.out.println("  Student        : " + student.getName() + " (" + student.getStudentId() + ")");
        System.out.println("  Section        : " + section.getSectionId() + " (" + section.getCourse().getTitle() + ")");
        System.out.println("  Attendance Rate: " + UIHelper.progressBar(percentage, 24));
        if (percentage >= 80.0) {
            System.out.println("  Status         : " + UIHelper.GREEN + "Satisfactory (Exam Eligible)" + UIHelper.RESET);
        } else {
            System.out.println("  Status         : " + UIHelper.RED + "Shortage Warning (Below 80%)" + UIHelper.RESET);
        }
    }

    private Section promptForSection() {
        String id = inputHelper.readCode("Enter section ID: ");
        Section section = dataStore.findSectionById(id);
        if (section == null) {
            UIHelper.warning("No section found with ID: " + id);
        }
        return section;
    }

    private Student promptForStudent() {
        String id = inputHelper.readCode("Enter student ID: ");
        NormalStudent normal = dataStore.findStudentById(id);
        if (normal != null) {
            return normal;
        }
        TeachingAssistant ta = dataStore.findTaById(id);
        if (ta != null) {
            return ta;
        }
        UIHelper.warning("No student found with ID: " + id);
        return null;
    }

    private AttendanceStatus promptForStatus() {
        System.out.println("  Attendance Status: [1] PRESENT  [2] ABSENT  [3] LATE");
        int opt = inputHelper.readInt("Select status [1-3]: ", 1, 3);
        return switch (opt) {
            case 1 -> AttendanceStatus.PRESENT;
            case 2 -> AttendanceStatus.ABSENT;
            case 3 -> AttendanceStatus.LATE;
            default -> null;
        };
    }

    private Attendance findAttendance(Section section, Student student, LocalDate date) {
        for (Attendance record : dataStore.getAttendanceRecords()) {
            if (record.getSection() == section && record.getStudent() == student && record.getDate().equals(date)) {
                return record;
            }
        }
        return null;
    }
}