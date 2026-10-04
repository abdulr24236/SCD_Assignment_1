/**
 * ============================================================
 * File        : TeachingAssistantMenu.java
 */
package campus.service;

import campus.enums.SubmissionStatus;
import campus.io.AssignmentRepository;
import campus.io.FeedbackRepository;
import campus.io.SubmissionRepository;
import campus.logging.AppLogger;
import campus.model.academic.Section;
import campus.model.assessment.Assignment;
import campus.model.assessment.Submission;
import campus.model.person.Student;
import campus.model.person.TeachingAssistant;
import campus.util.InputHelper;
import campus.util.UIHelper;
import java.time.LocalDate;
import java.util.Scanner;

public class TeachingAssistantMenu {
    private final TeachingAssistant ta;
    private final Scanner scanner;
    private final InputHelper inputHelper;
    private final DataStore dataStore;
    private final StudentMenu studentMenu;

    public TeachingAssistantMenu(TeachingAssistant ta, Scanner scanner, DataStore dataStore) {
        this.ta = ta;
        this.scanner = scanner;
        this.inputHelper = new InputHelper(scanner);
        this.dataStore = dataStore;
        this.studentMenu = new StudentMenu(ta, scanner, dataStore);
    }

    public void show() {
        boolean loggedIn = true;
        while (loggedIn) {
            UIHelper.printUserHeader("Teaching Assistant & Student Portal", ta.getName(), ta.getTaId() + " / " + ta.getStudentId());
            String[] options = {
                    "--- TEACHING ASSISTANT DUTIES ---",
                    "[1]  View assigned section & course details",
                    "[2]  View enrolled student roster in assigned section",
                    "[3]  Create a new assignment for assigned section",
                    "[4]  View submissions for an assignment",
                    "[5]  Check late submissions for an assignment",
                    "[6]  Evaluate submission & assign marks",
                    "[7]  Add written feedback to a submission",
                    "--- STUDENT SELF-SERVICE (ACADEMIC) ---",
                    "[8]  View available courses and sections",
                    "[9]  Register for a section (as a student)",
                    "[10] Drop a registered section",
                    "[11] View my registered courses & credits",
                    "[12] View my weekly timetable",
                    "[13] View assignments for my enrolled courses",
                    "[14] Submit an assignment (as a student)",
                    "[15] View my attendance records",
                    "[16] View attendance percentage & exam eligibility",
                    "[17] Submit course clash request",
                    "[18] Submit general academic request",
                    "[19] View my submitted academic requests",
                    "[20] View my FYP group details & members",
                    "[21] Log out"
            };
            UIHelper.printMenuCard("Teaching Assistant Portal", options);

            int choice = inputHelper.readInt("Choose an option [1-21]: ", 1, 21);
            switch (choice) {
                case 1 -> viewAssignedSection();
                case 2 -> viewEnrolledStudents();
                case 3 -> createAssignment();
                case 4 -> viewSubmissions();
                case 5 -> checkLateSubmissions();
                case 6 -> evaluateSubmission();
                case 7 -> giveFeedback();
                case 8 -> studentMenu.viewAvailableCourses();
                case 9 -> studentMenu.registerForSection();
                case 10 -> studentMenu.dropSection();
                case 11 -> studentMenu.viewRegisteredCourses();
                case 12 -> studentMenu.viewTimetable();
                case 13 -> studentMenu.viewAssignments();
                case 14 -> studentMenu.submitAssignment();
                case 15 -> studentMenu.viewAttendance();
                case 16 -> studentMenu.viewAttendancePercentage();
                case 17 -> studentMenu.submitCourseClashRequest();
                case 18 -> studentMenu.submitGeneralRequest();
                case 19 -> studentMenu.viewMyRequests();
                case 20 -> studentMenu.viewMyFypGroup();
                case 21 -> {
                    loggedIn = false;
                    UIHelper.info("Logging out from Teaching Assistant Portal.");
                }
            }
        }
    }

    private void viewAssignedSection() {
        UIHelper.printSectionHeader("Assigned Section Information");
        Section section = ta.getAssignedSection();
        if (section == null) {
            UIHelper.info("You are currently not assigned to any section.");
            return;
        }

        System.out.println(UIHelper.BRIGHT_CYAN + "  ┌────────────────────────────────────────────────────────────┐" + UIHelper.RESET);
        System.out.println(UIHelper.BRIGHT_CYAN + "  │ " + UIHelper.BOLD + "SECTION DETAILS" + UIHelper.RESET + "                                            │");
        System.out.println(UIHelper.BRIGHT_CYAN + "  ├────────────────────────────────────────────────────────────┤" + UIHelper.RESET);
        System.out.printf(UIHelper.BRIGHT_CYAN + "  │ " + UIHelper.RESET + "Section ID   : %-46s" + UIHelper.BRIGHT_CYAN + "│\n" + UIHelper.RESET, section.getSectionId());
        System.out.printf(UIHelper.BRIGHT_CYAN + "  │ " + UIHelper.RESET + "Course       : %-46s" + UIHelper.BRIGHT_CYAN + "│\n" + UIHelper.RESET, section.getCourse().getCourseCode() + " — " + section.getCourse().getTitle());
        System.out.printf(UIHelper.BRIGHT_CYAN + "  │ " + UIHelper.RESET + "Capacity     : %-46s" + UIHelper.BRIGHT_CYAN + "│\n" + UIHelper.RESET, section.getEnrollments().size() + " / " + section.getCapacity() + " Enrolled");
        String sched = section.getSchedule() != null ? section.getSchedule().getScheduleInfo() : "TBA";
        System.out.printf(UIHelper.BRIGHT_CYAN + "  │ " + UIHelper.RESET + "Schedule     : %-46s" + UIHelper.BRIGHT_CYAN + "│\n" + UIHelper.RESET, sched);
        String prof = section.getInstructor() != null ? section.getInstructor().getName() : "None";
        System.out.printf(UIHelper.BRIGHT_CYAN + "  │ " + UIHelper.RESET + "Instructor   : %-46s" + UIHelper.BRIGHT_CYAN + "│\n" + UIHelper.RESET, prof);
        System.out.println(UIHelper.BRIGHT_CYAN + "  └────────────────────────────────────────────────────────────┘" + UIHelper.RESET);
    }

    private void viewEnrolledStudents() {
        UIHelper.printSectionHeader("Enrolled Students Roster");
        Section section = ta.getAssignedSection();
        if (section == null) {
            UIHelper.info("No assigned section.");
            return;
        }

        if (section.getEnrolledStudents().isEmpty()) {
            UIHelper.info("No students enrolled in section " + section.getSectionId() + " yet.");
            return;
        }

        System.out.println(UIHelper.CYAN + "  ┌──────────────┬─────────────────────────────┬─────────────────┐" + UIHelper.RESET);
        System.out.println(UIHelper.CYAN + "  │ " + UIHelper.BOLD + "Student ID" + UIHelper.RESET + "   │ " + UIHelper.BOLD + "Full Name" + UIHelper.RESET + "                   │ " + UIHelper.BOLD + "Email" + UIHelper.RESET + "           │");
        System.out.println(UIHelper.CYAN + "  ├──────────────┼─────────────────────────────┼─────────────────┤" + UIHelper.RESET);
        for (Student s : section.getEnrolledStudents()) {
            System.out.printf("  │ %-12s │ %-27s │ %-15s │\n",
                    s.getStudentId(),
                    s.getName().length() > 27 ? s.getName().substring(0, 24) + "..." : s.getName(),
                    s.getEmail().length() > 15 ? s.getEmail().substring(0, 12) + "..." : s.getEmail());
        }
        System.out.println(UIHelper.CYAN + "  └──────────────┴─────────────────────────────┴─────────────────┘" + UIHelper.RESET);
    }

    private void createAssignment() {
        UIHelper.printSectionHeader("Create New Assignment");
        Section section = ta.getAssignedSection();
        if (section == null) {
            UIHelper.warning("You must be assigned to a section to create assignments.");
            return;
        }

        String title = inputHelper.readLine("Title: ");
        String description = inputHelper.readLine("Instructions / Description: ");

        LocalDate deadline;
        while (true) {
            deadline = inputHelper.readDate("Submission deadline");
            if (deadline.isBefore(LocalDate.now())) {
                UIHelper.warning("Deadline must be in the future (today or later).");
                continue;
            }
            break;
        }

        double totalMarks = inputHelper.readDouble("Total marks (e.g. 20, 100): ", 1, 1000);
        Assignment assignment = ta.createAssignment(title, description, deadline, totalMarks);
        dataStore.getAssignments().add(assignment);
        AssignmentRepository.save(assignment);
        UIHelper.success("Assignment " + assignment.getId() + " created successfully for section " + section.getSectionId() + "!");
        AppLogger.info(ta.getTaId() + " created assignment " + assignment.getId());
    }

    private void viewSubmissions() {
        UIHelper.printSectionHeader("View Assignment Submissions");
        Assignment assignment = promptForAssignment();
        if (assignment == null) {
            return;
        }

        if (assignment.getSubmissions().isEmpty()) {
            UIHelper.info("No submissions received for assignment " + assignment.getId() + " yet.");
            return;
        }

        System.out.println(UIHelper.CYAN + "  ┌──────────────────┬──────────────┬─────────────┬──────────────┬───────────┐" + UIHelper.RESET);
        System.out.println(UIHelper.CYAN + "  │ " + UIHelper.BOLD + "Submission ID" + UIHelper.RESET + "    │ " + UIHelper.BOLD + "Student ID" + UIHelper.RESET + "   │ " + UIHelper.BOLD + "Date" + UIHelper.RESET + "        │ " + UIHelper.BOLD + "Status" + UIHelper.RESET + "      │ " + UIHelper.BOLD + "Marks" + UIHelper.RESET + "     │");
        System.out.println(UIHelper.CYAN + "  ├──────────────────┼──────────────┼─────────────┼──────────────┼───────────┤" + UIHelper.RESET);
        for (Submission s : assignment.getSubmissions()) {
            String marksStr = s.getMarks() < 0 ? "Not Graded" : String.format("%.1f", s.getMarks());
            System.out.printf("  │ %-16s │ %-12s │ %-11s │ %-12s │ %-9s │\n",
                    s.getSubmissionId(),
                    s.getStudent().getStudentId(),
                    s.getSubmissionDate(),
                    s.getStatus(),
                    marksStr);
        }
        System.out.println(UIHelper.CYAN + "  └──────────────────┴──────────────┴─────────────┴──────────────┴───────────┘" + UIHelper.RESET);
    }

    private void checkLateSubmissions() {
        UIHelper.printSectionHeader("Filter Late Submissions");
        Assignment assignment = promptForAssignment();
        if (assignment == null) {
            return;
        }

        boolean anyLate = false;
        for (Submission s : assignment.getSubmissions()) {
            if (s.isLate()) {
                anyLate = true;
                System.out.println("   • " + UIHelper.statusBadge("LATE") + " Submission " + s.getSubmissionId()
                        + " by Student: " + s.getStudent().getStudentId() + " (Date: " + s.getSubmissionDate() + ")");
            }
        }
        if (!anyLate) {
            UIHelper.info("No late submissions found for assignment " + assignment.getId() + ".");
        }
    }

    private void evaluateSubmission() {
        UIHelper.printSectionHeader("Evaluate & Grade Submission");
        Submission submission = promptForSubmission();
        if (submission == null) {
            return;
        }

        double marks = inputHelper.readDouble("Enter marks awarded: ", 0, 100);
        ta.evaluateSubmission(submission, marks);
        SubmissionRepository.saveAll(dataStore.getSubmissions());
        UIHelper.success("Submission " + submission.getSubmissionId() + " evaluated. Marks: " + marks);
        AppLogger.info(ta.getTaId() + " evaluated submission " + submission.getSubmissionId() + " with " + marks);
    }

    private void giveFeedback() {
        UIHelper.printSectionHeader("Add Written Feedback to Submission");
        Submission submission = promptForSubmission();
        if (submission == null) {
            return;
        }

        String comment = inputHelper.readLine("Enter comments / feedback: ");
        ta.giveFeedback(submission, comment);
        if (submission.getFeedback() != null) {
            dataStore.getFeedbackList().add(submission.getFeedback());
            FeedbackRepository.save(submission.getFeedback());
        }
        UIHelper.success("Feedback recorded for submission " + submission.getSubmissionId() + ".");
        AppLogger.info(ta.getTaId() + " provided feedback on " + submission.getSubmissionId());
    }

    private Assignment promptForAssignment() {
        String id = inputHelper.readCode("Enter assignment ID: ");
        for (Assignment assignment : dataStore.getAssignments()) {
            if (assignment.getId().equalsIgnoreCase(id)) {
                return assignment;
            }
        }
        UIHelper.warning("No assignment found with ID: " + id);
        return null;
    }

    private Submission promptForSubmission() {
        String id = inputHelper.readCode("Enter submission ID: ");
        for (Submission submission : dataStore.getSubmissions()) {
            if (submission.getSubmissionId().equalsIgnoreCase(id)) {
                return submission;
            }
        }
        UIHelper.warning("No submission found with ID: " + id);
        return null;
    }
}