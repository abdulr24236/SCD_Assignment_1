/**
 * ============================================================
 * File        : PermanentInstructorMenu.java
 * Package     : campus.service
 * Author      : Campus Development Team
 * Course      : Software Construction & Development
 * Assignment  : Assignment 1 — Campus Management System
 * Description : Permanent Instructor portal menu with styled CLI, attendance, TA assignment, and FYP
 * ============================================================
 */
package campus.service;

import campus.enums.AttendanceStatus;
import campus.io.AttendanceRepository;
import campus.io.FYPEvaluationRepository;
import campus.io.FYPGroupRepository;
import campus.io.FYPMeetingRepository;
import campus.io.SectionRepository;
import campus.io.TeachingAssistantRepository;
import campus.logging.AppLogger;
import campus.model.academic.Attendance;
import campus.model.academic.Course;
import campus.model.academic.Section;
import campus.model.fyp.FYPEvaluation;
import campus.model.fyp.FYPGroup;
import campus.model.fyp.FYPMeeting;
import campus.model.person.NormalStudent;
import campus.model.person.PermanentInstructor;
import campus.model.person.Student;
import campus.model.person.TeachingAssistant;
import campus.util.InputHelper;
import campus.util.UIHelper;
import java.time.LocalDate;
import java.util.Scanner;
import java.util.UUID;

public class PermanentInstructorMenu {
    private final PermanentInstructor instructor;
    private final Scanner scanner;
    private final InputHelper inputHelper;
    private final DataStore dataStore;

    public PermanentInstructorMenu(PermanentInstructor instructor, Scanner scanner, DataStore dataStore) {
        this.instructor = instructor;
        this.scanner = scanner;
        this.inputHelper = new InputHelper(scanner);
        this.dataStore = dataStore;
    }

    public void show() {
        boolean loggedIn = true;
        while (loggedIn) {
            UIHelper.printUserHeader("Permanent Instructor", instructor.getName(), instructor.getTeacherId());
            String[] options = {
                    "[1]  View assigned courses",
                    "[2]  View assigned sections",
                    "[3]  View enrolled students in a section",
                    "[4]  Mark daily student attendance",
                    "[5]  Update an existing attendance record",
                    "[6]  Calculate attendance rate & eligibility",
                    "[7]  Assign a Teaching Assistant to a section",
                    "[8]  Create a Final Year Project (FYP) group",
                    "[9]  View my supervised FYP groups",
                    "[10] View detailed FYP group information",
                    "[11] Schedule an FYP progress meeting",
                    "[12] Evaluate an FYP idea or milestone",
                    "[13] Log out"
            };
            UIHelper.printMenuCard("Faculty Portal — Permanent Instructor", options);

            int choice = inputHelper.readInt("Choose an option [1-13]: ", 1, 13);
            switch (choice) {
                case 1 -> viewAssignedCourses();
                case 2 -> viewAssignedSections();
                case 3 -> viewEnrolledStudents();
                case 4 -> markAttendance();
                case 5 -> updateAttendance();
                case 6 -> calculateAttendancePercentage();
                case 7 -> assignTeachingAssistant();
                case 8 -> createFypGroup();
                case 9 -> viewMyFypGroups();
                case 10 -> viewFypGroupDetails();
                case 11 -> scheduleFypMeeting();
                case 12 -> evaluateFypIdea();
                case 13 -> {
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
            System.out.println(studentIdOf(student) + " is not enrolled in " + section.getSectionId() + ".");
            return;
        }
        if (!section.getEnrolledStudents().contains(student)) {
            System.out.println(studentIdOf(student) + " is not enrolled in " + section.getSectionId() + ".");
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
        AppLogger.info(instructor.getTeacherId() + " marked attendance for " + studentIdOf(student));
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
        AppLogger.info(instructor.getTeacherId() + " updated attendance for " + studentIdOf(student));
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

    private void assignTeachingAssistant() {
        UIHelper.printSectionHeader("Assign Teaching Assistant");
        String id = inputHelper.readCode("Enter student ID or TA ID: ");

        TeachingAssistant existingTa = dataStore.findTaById(id);
        if (existingTa != null) {
            if (existingTa.getAssignedSection() != null) {
                UIHelper.warning("TA " + existingTa.getName() + " (" + existingTa.getTaId() + ") is already assigned to section "
                        + existingTa.getAssignedSection().getSectionId() + ".");
                return;
            }
            Section section = promptForSection();
            if (section == null) {
                return;
            }
            if (!instructor.viewSections().contains(section)) {
                UIHelper.warning("You are not assigned to instruct section " + section.getSectionId() + ".");
                return;
            }
            if (section.getTeachingAssistant() != null) {
                UIHelper.warning("Section " + section.getSectionId() + " already has TA: " + section.getTeachingAssistant().getName()
                        + " (" + section.getTeachingAssistant().getTaId() + ").");
                return;
            }
            if (section.getEnrolledStudents().contains(existingTa)) {
                UIHelper.warning(existingTa.getStudentId() + " is currently enrolled as a student in " + section.getSectionId() + " and cannot be its TA.");
                return;
            }

            existingTa.setAssignedSection(section);
            section.assignTA(existingTa);
            TeachingAssistantRepository.saveAll(dataStore.getTeachingAssistants());
            SectionRepository.saveAll(dataStore.getSections());
            UIHelper.success("Assigned Teaching Assistant " + existingTa.getName() + " (" + existingTa.getTaId()
                    + ") to section " + section.getSectionId() + "!");
            AppLogger.info(instructor.getTeacherId() + " assigned TA " + existingTa.getTaId() + " to section " + section.getSectionId());
            return;
        }

        NormalStudent student = dataStore.findStudentById(id);
        if (student != null) {
            Section section = promptForSection();
            if (section == null) {
                return;
            }
            if (!instructor.viewSections().contains(section)) {
                UIHelper.warning("You are not assigned to instruct section " + section.getSectionId() + ".");
                return;
            }
            if (section.getTeachingAssistant() != null) {
                UIHelper.warning("Section " + section.getSectionId() + " already has TA: " + section.getTeachingAssistant().getName()
                        + " (" + section.getTeachingAssistant().getTaId() + ").");
                return;
            }
            if (section.getEnrolledStudents().contains(student)) {
                UIHelper.warning(student.getStudentId() + " is currently enrolled in " + section.getSectionId() + " and cannot be its TA.");
                return;
            }

            TeachingAssistant ta = instructor.assignTA(student, section);
            dataStore.getTeachingAssistants().add(ta);
            TeachingAssistantRepository.save(ta);
            SectionRepository.saveAll(dataStore.getSections());

            String credLine = ta.getTaId().toLowerCase() + "::pass123::TA::" + ta.getTaId() + "::" + student.getEmail();
            campus.io.FileManager.appendLine("credentials.txt", credLine);
            String studentCred = student.getStudentId().toLowerCase() + "::pass123::TA::" + ta.getTaId() + "::" + student.getEmail();
            campus.io.FileManager.appendLine("credentials.txt", studentCred);

            UIHelper.success("Promoted " + student.getName() + " (" + student.getStudentId() + ") to TA " + ta.getTaId()
                    + " for section " + section.getSectionId() + "!");
            AppLogger.info(instructor.getTeacherId() + " promoted and assigned " + student.getStudentId() + " as TA " + ta.getTaId());
            return;
        }

        UIHelper.warning("No enrolled student or registered TA found with ID: " + id);
    }

    private void createFypGroup() {
        UIHelper.printSectionHeader("Create FYP Group");
        String title = inputHelper.readLine("Group Project Title: ");
        String description = inputHelper.readLine("Project Scope & Description: ");

        FYPGroup group = new FYPGroup(UUID.randomUUID().toString().substring(0, 8).toUpperCase(), title, description);
        group.assignSupervisor(instructor);

        System.out.println("  (Enter between 2 and 3 student IDs for the group)");
        while (group.getMembers().size() < 3) {
            String memberId = inputHelper.readLine("Student ID (or press Enter if done)", "").toUpperCase();
            if (memberId.isEmpty()) {
                break;
            }
            Student member = dataStore.findAnyStudentById(memberId);
            if (member != null) {
                if (group.getMembers().contains(member)) {
                    UIHelper.warning("Student is already added to this group.");
                } else {
                    group.addMember(member);
                    UIHelper.success("Added member: " + member.getName() + " (" + memberId + ")");
                }
            } else {
                UIHelper.warning("No student found with ID: " + memberId);
            }
        }

        if (group.getMembers().size() < 2) {
            UIHelper.error("An FYP group requires a minimum of 2 members. Group not created.");
            return;
        }

        dataStore.getFypGroups().add(group);
        FYPGroupRepository.save(group);
        UIHelper.success("FYP group '" + title + "' registered successfully with ID: " + group.getGroupId());
        AppLogger.info(instructor.getTeacherId() + " created FYP group " + group.getGroupId());

        // Display created group summary with all member names
        System.out.println();
        System.out.println("  +--------------------------------------------------------------+");
        System.out.println("  | CREATED FYP GROUP DETAILS                                    |");
        System.out.println("  +--------------------------------------------------------------+");
        System.out.println("    Group ID      : " + group.getGroupId());
        System.out.println("    Project Title : " + group.getTitle());
        System.out.println("    Description   : " + group.getDescription());
        System.out.println("    Supervisor    : " + instructor.getName());
        System.out.println("    Total Members : " + group.getMembers().size());
        System.out.println("  +--------------------------------------------------------------+");
        System.out.println("  | ALL GROUP MEMBERS:");
        int mIdx = 1;
        for (Student m : group.getMembers()) {
            System.out.println("    [" + mIdx++ + "] " + m.getName() + " (" + m.getStudentId() + ") — " + m.getEmail());
        }
        System.out.println("  +--------------------------------------------------------------+");
    }

    private void viewMyFypGroups() {
        UIHelper.printSectionHeader("Supervised FYP Groups");
        if (instructor.viewFYPGroups().isEmpty()) {
            UIHelper.info("You are currently not supervising any FYP groups.");
            return;
        }
        for (FYPGroup group : instructor.viewFYPGroups()) {
            System.out.println("   • Group ID   : " + UIHelper.BOLD + group.getGroupId() + UIHelper.RESET);
            System.out.println("     Title      : " + group.getTitle());
            System.out.println("     Description: " + group.getDescription());
            System.out.println("     All Members (" + group.getMembers().size() + "):");
            int i = 1;
            for (Student m : group.getMembers()) {
                System.out.println("       [" + i++ + "] " + m.getName() + " (" + m.getStudentId() + ") — " + m.getEmail());
            }
            System.out.println();
        }
    }

    private void viewFypGroupDetails() {
        UIHelper.printSectionHeader("FYP Group Details");
        FYPGroup group = promptForFypGroup();
        if (group == null) {
            return;
        }
        System.out.println("  +--------------------------------------------------------------+");
        System.out.println("  | FYP PROJECT DETAILS                                          |");
        System.out.println("  +--------------------------------------------------------------+");
        System.out.printf("  | Group ID    : %-47s|\n", group.getGroupId());
        System.out.printf("  | Title       : %-47s|\n", group.getTitle());
        String desc = group.getDescription().length() > 47 ? group.getDescription().substring(0, 44) + "..." : group.getDescription();
        System.out.printf("  | Description : %-47s|\n", desc);
        String sup = group.getSupervisor() != null ? group.getSupervisor().getName() : "None";
        System.out.printf("  | Supervisor  : %-47s|\n", sup);
        System.out.println("  +--------------------------------------------------------------+");
        System.out.println("  | ALL GROUP MEMBERS (" + group.getMembers().size() + "):");
        int memNum = 1;
        for (Student m : group.getMembers()) {
            System.out.println("    [" + memNum++ + "] " + m.getName() + " (" + m.getStudentId() + ") — " + m.getEmail());
        }
        System.out.println("  +--------------------------------------------------------------+");
    }

    private void scheduleFypMeeting() {
        UIHelper.printSectionHeader("Schedule FYP Progress Meeting");
        FYPGroup group = promptForFypGroup();
        if (group == null) {
            return;
        }
        String agenda = inputHelper.readLine("Meeting agenda & deliverables: ");
        LocalDate meetingDate = inputHelper.readDate("Meeting date");

        FYPMeeting meeting = new FYPMeeting(UUID.randomUUID().toString().substring(0, 8).toUpperCase(), meetingDate, agenda, group.getGroupId());
        instructor.scheduleFYPMeeting(group, meeting);
        dataStore.getFypMeetings().add(meeting);
        FYPMeetingRepository.save(meeting);
        UIHelper.success("FYP Meeting scheduled for group " + group.getGroupId() + " on " + meetingDate + "!");
        AppLogger.info(instructor.getTeacherId() + " scheduled meeting for FYP group " + group.getGroupId());
    }

    private void evaluateFypIdea() {
        UIHelper.printSectionHeader("Evaluate FYP Progress / Idea");
        FYPGroup group = promptForFypGroup();
        if (group == null) {
            return;
        }
        double score = inputHelper.readDouble("Score awarded (0 - 100): ", 0, 100);
        String comments = inputHelper.readLine("Evaluator comments / feedback: ");

        FYPEvaluation evaluation = new FYPEvaluation(UUID.randomUUID().toString().substring(0, 8).toUpperCase(), LocalDate.now(), group.getGroupId());
        evaluation.evaluate(score);
        if (!comments.isEmpty()) {
            evaluation.addFeedback(comments);
        }
        instructor.evaluateFYPIdea(group, evaluation);
        dataStore.getFypEvaluations().add(evaluation);
        FYPEvaluationRepository.save(evaluation);
        UIHelper.success("FYP Group " + group.getGroupId() + " evaluated successfully with score: " + score + " / 100");
        AppLogger.info(instructor.getTeacherId() + " evaluated FYP group " + group.getGroupId() + " with score " + score);
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

    private FYPGroup promptForFypGroup() {
        String id = inputHelper.readCode("Enter FYP Group ID: ");
        for (FYPGroup group : instructor.viewFYPGroups()) {
            if (group.getGroupId().equalsIgnoreCase(id)) {
                return group;
            }
        }
        UIHelper.warning("No FYP group found with ID: " + id + " under your supervision.");
        return null;
    }

    private String studentIdOf(Student student) {
        return student.getStudentId();
    }
}