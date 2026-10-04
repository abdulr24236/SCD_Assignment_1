/**
 * ============================================================
 * File        : StudentMenu.java
 */
package campus.service;

import campus.enums.RequestCategory;
import campus.exceptions.CourseClashException;
import campus.exceptions.CourseFullException;
import campus.io.CourseClashRequestRepository;
import campus.io.EnrollmentRepository;
import campus.io.GenericRequestRepository;
import campus.io.SubmissionRepository;
import campus.io.TeachingAssistantRepository;
import campus.logging.AppLogger;
import campus.model.academic.Attendance;
import campus.model.academic.Course;
import campus.model.academic.Enrollment;
import campus.model.academic.Schedule;
import campus.model.academic.Section;
import campus.model.assessment.Assignment;
import campus.model.assessment.Submission;
import campus.model.fyp.FYPGroup;
import campus.model.person.Student;
import campus.model.person.TeachingAssistant;
import campus.model.request.CourseClashRequest;
import campus.model.request.GenericRequest;
import campus.model.request.Request;
import campus.util.InputHelper;
import campus.util.UIHelper;
import java.util.Scanner;
import java.util.UUID;

public class StudentMenu {
    private final Student student;
    private final Scanner scanner;
    private final InputHelper inputHelper;
    private final DataStore dataStore;

    public StudentMenu(Student student, Scanner scanner, DataStore dataStore) {
        this.student = student;
        this.scanner = scanner;
        this.inputHelper = new InputHelper(scanner);
        this.dataStore = dataStore;
    }

    public void show() {
        boolean loggedIn = true;
        while (loggedIn) {
            UIHelper.printUserHeader("Student Portal", student.getName(), student.getStudentId());
            boolean isTa = dataStore.findTaById(student.getStudentId()) != null;
            String taOption = isTa ? "[13] Switch to Teaching Assistant Portal" : "[13] Register as Teaching Assistant";
            String[] options = {
                    "[1]  View available courses and sections",
                    "[2]  Register for a section",
                    "[3]  Drop a registered section",
                    "[4]  View my registered courses & credits",
                    "[5]  View my weekly timetable",
                    "[6]  View assignments for enrolled courses",
                    "[7]  Submit an assignment",
                    "[8]  View attendance records",
                    "[9]  View attendance percentage & exam eligibility",
                    "[10] Submit a course clash request",
                    "[11] Submit a general academic request",
                    "[12] View my submitted requests & status",
                    taOption,
                    "[14] View my FYP group details & members",
                    "[15] Log out"
            };
            UIHelper.printMenuCard("Student Self-Service Portal", options);

            int choice = inputHelper.readInt("Choose an option [1-15]: ", 1, 15);
            switch (choice) {
                case 1 -> viewAvailableCourses();
                case 2 -> registerForSection();
                case 3 -> dropSection();
                case 4 -> viewRegisteredCourses();
                case 5 -> viewTimetable();
                case 6 -> viewAssignments();
                case 7 -> submitAssignment();
                case 8 -> viewAttendance();
                case 9 -> viewAttendancePercentage();
                case 10 -> submitCourseClashRequest();
                case 11 -> submitGeneralRequest();
                case 12 -> viewMyRequests();
                case 13 -> {
                    if (isTa) {
                        TeachingAssistant ta = dataStore.findTaById(student.getStudentId());
                        new TeachingAssistantMenu(ta, scanner, dataStore).show();
                    } else {
                        registerAsTeachingAssistant();
                    }
                }
                case 14 -> viewMyFypGroup();
                case 15 -> {
                    loggedIn = false;
                    UIHelper.info("Logging out from Student Portal.");
                }
            }
        }
    }

    void viewAvailableCourses() {
        UIHelper.printSectionHeader("Available Courses & Sections");
        if (dataStore.getCourses().isEmpty()) {
            UIHelper.info("No courses currently offered.");
            return;
        }

        for (Course course : dataStore.getCourses()) {
            System.out.println(UIHelper.BOLD + UIHelper.BRIGHT_CYAN + "  Course: " + course.getCourseCode()
                    + " — " + course.getTitle() + " (" + course.getCreditHours() + " Credits)" + UIHelper.RESET);
            if (course.getSections().isEmpty()) {
                System.out.println(UIHelper.GRAY + "    [No active sections available for this course]" + UIHelper.RESET);
            } else {
                for (Section section : course.getSections()) {
                    String seatInfo = section.isFull()
                            ? UIHelper.RED + "[FULL — 0 Seats]" + UIHelper.RESET
                            : UIHelper.GREEN + "[" + section.getAvailableSeats() + " / " + section.getCapacity() + " Seats Available]" + UIHelper.RESET;
                    String sched = section.getSchedule() != null ? section.getSchedule().getScheduleInfo() : "Schedule TBA";
                    String instructor = section.getInstructor() != null ? section.getInstructor().getName() : "Instructor TBA";
                    System.out.println("    • Section " + UIHelper.BOLD + section.getSectionId() + UIHelper.RESET + " | " + seatInfo
                            + " | " + sched + " | " + instructor);
                }
            }
            System.out.println();
        }
    }

    void registerForSection() {
        UIHelper.printSectionHeader("Register for Course Section");
        String sectionId = inputHelper.readCode("Enter section ID (e.g. CS101-A): ");
        Section section = dataStore.findSectionById(sectionId);
        if (section == null) {
            UIHelper.warning("No section found with ID: " + sectionId);
            return;
        }

        if (section.getEnrolledStudents().contains(student)) {
            UIHelper.warning("You are already registered for section " + sectionId + ".");
            return;
        }

        try {
            student.register(section);
            persistNewEnrollment(section);
            int credits = student.calculateTotalCreditHours();
            UIHelper.success("Registered successfully for section " + sectionId + "! (Enrolled Credits: " + credits + " / 21)");
            AppLogger.info(student.getStudentId() + " registered for " + sectionId);
        } catch (CourseFullException | CourseClashException e) {
            UIHelper.error("Registration failed: " + e.getMessage());
            AppLogger.error("Registration failed for " + student.getStudentId() + ": " + e.getMessage());
        }
    }

    private void persistNewEnrollment(Section section) {
        for (Enrollment enrollment : section.getEnrollments()) {
            if (enrollment.getStudent() == student && !dataStore.getEnrollments().contains(enrollment)) {
                dataStore.getEnrollments().add(enrollment);
                EnrollmentRepository.save(enrollment);
            }
        }
    }

    void dropSection() {
        UIHelper.printSectionHeader("Drop Registered Section");
        String sectionId = inputHelper.readCode("Enter section ID to drop: ");
        Section section = dataStore.findSectionById(sectionId);
        if (section == null) {
            UIHelper.warning("No section found with ID: " + sectionId);
            return;
        }

        student.drop(section);
        EnrollmentRepository.saveAll(dataStore.getEnrollments());
        int credits = student.calculateTotalCreditHours();
        UIHelper.success("Dropped section " + sectionId + " successfully. (Remaining Credits: " + credits + " / 21)");
        AppLogger.info(student.getStudentId() + " dropped " + sectionId);
    }

    void viewRegisteredCourses() {
        UIHelper.printSectionHeader("My Registered Courses & Load");
        if (student.viewCourses().isEmpty()) {
            UIHelper.info("You have not registered for any courses yet.");
            return;
        }

        System.out.println(UIHelper.CYAN + "  ┌────────────┬─────────────────────────────┬─────────┐" + UIHelper.RESET);
        System.out.println(UIHelper.CYAN + "  │ " + UIHelper.BOLD + "Course" + UIHelper.RESET + "     │ " + UIHelper.BOLD + "Course Title" + UIHelper.RESET + "                │ " + UIHelper.BOLD + "Credits" + UIHelper.RESET + " │");
        System.out.println(UIHelper.CYAN + "  ├────────────┼─────────────────────────────┼─────────┤" + UIHelper.RESET);
        for (Course course : student.viewCourses()) {
            System.out.printf("  │ %-10s │ %-27s │ %-7d │\n",
                    course.getCourseCode(),
                    course.getTitle().length() > 27 ? course.getTitle().substring(0, 24) + "..." : course.getTitle(),
                    course.getCreditHours());
        }
        System.out.println(UIHelper.CYAN + "  └────────────┴─────────────────────────────┴─────────┘" + UIHelper.RESET);
        int totalCredits = student.calculateTotalCreditHours();
        System.out.println("  " + UIHelper.BOLD + "Total Registered Credit Hours: " + UIHelper.BRIGHT_GREEN + totalCredits + " / 21" + UIHelper.RESET);
    }

    void viewTimetable() {
        UIHelper.printSectionHeader("My Weekly Academic Timetable");
        boolean hasSchedule = false;
        for (Schedule schedule : student.viewTimetable()) {
            if (schedule != null) {
                hasSchedule = true;
                System.out.println("   • " + schedule.getScheduleInfo());
            }
        }
        if (!hasSchedule) {
            UIHelper.info("No class schedules found for your registered sections.");
        }
    }

    void viewAssignments() {
        UIHelper.printSectionHeader("Course Assignments");
        boolean any = false;
        for (Assignment assignment : dataStore.getAssignments()) {
            if (student.viewCourses().contains(assignment.getSection().getCourse())) {
                any = true;
                System.out.println("   • Assignment ID: " + UIHelper.BOLD + assignment.getId() + UIHelper.RESET
                        + " | Title: " + assignment.getTitle()
                        + " | Course: " + assignment.getSection().getCourse().getCourseCode()
                        + " | Section: " + assignment.getSection().getSectionId()
                        + " | Deadline: " + assignment.getDeadline());
            }
        }
        if (!any) {
            UIHelper.info("No assignments posted for your enrolled courses.");
        }
    }

    void submitAssignment() {
        UIHelper.printSectionHeader("Submit Assignment");
        String assignmentId = inputHelper.readCode("Enter assignment ID: ");
        Assignment assignment = findAssignmentById(assignmentId);
        if (assignment == null) {
            UIHelper.warning("No assignment found with ID: " + assignmentId);
            return;
        }

        if (!assignment.getSection().getEnrolledStudents().contains(student)) {
            UIHelper.error("You are not enrolled in the section for this assignment.");
            return;
        }

        for (Submission existing : assignment.getSubmissions()) {
            if (existing.getStudent() == student) {
                UIHelper.warning("You have already submitted assignment " + assignmentId + " (Status: " + existing.getStatus() + ").");
                return;
            }
        }

        String content = inputHelper.readLine("Enter submission text / notes / repository link: ");
        Submission submission = student.submitAssignment(assignment, content);
        dataStore.getSubmissions().add(submission);
        SubmissionRepository.save(submission);
        UIHelper.success("Assignment submitted successfully! Status: " + UIHelper.statusBadge(submission.getStatus().name()));
        AppLogger.info(student.getStudentId() + " submitted assignment " + assignmentId);
    }

    private Assignment findAssignmentById(String id) {
        for (Assignment assignment : dataStore.getAssignments()) {
            if (assignment.getId().equalsIgnoreCase(id)) {
                return assignment;
            }
        }
        return null;
    }

    void viewAttendance() {
        UIHelper.printSectionHeader("My Attendance Records");
        boolean any = false;
        for (Attendance record : dataStore.getAttendanceRecords()) {
            if (record.getStudent() == student) {
                any = true;
                System.out.println("   • Date: " + record.getDate() + " | Section: " + record.getSection().getSectionId()
                        + " | Status: " + UIHelper.statusBadge(record.getStatus().name()));
            }
        }
        if (!any) {
            UIHelper.info("No attendance records marked yet.");
        }
    }

    void viewAttendancePercentage() {
        UIHelper.printSectionHeader("Attendance Rate & Examination Eligibility");
        String sectionId = inputHelper.readCode("Enter section ID to calculate: ");
        Section section = dataStore.findSectionById(sectionId);
        if (section == null || section.getInstructor() == null) {
            UIHelper.warning("No section (or no assigned instructor) found with ID: " + sectionId);
            return;
        }

        double percentage = section.getInstructor().calculateAttendancePercentage(student, section);
        System.out.println("  Section        : " + sectionId + " (" + section.getCourse().getTitle() + ")");
        System.out.println("  Attendance Bar : " + UIHelper.progressBar(percentage, 24));
        if (percentage >= 80.0) {
            System.out.println("  Eligibility    : " + UIHelper.GREEN + "✔ SATISFACTORY — Eligible to sit for final examinations." + UIHelper.RESET);
        } else {
            System.out.println("  Eligibility    : " + UIHelper.RED + "⚠ ATTENDANCE WARNING — Below required 80% threshold." + UIHelper.RESET);
        }
    }

    void submitCourseClashRequest() {
        UIHelper.printSectionHeader("Submit Course Clash Request");
        String conflictingId = inputHelper.readCode("Conflicting section ID (currently enrolled in): ");
        Section conflicting = dataStore.findSectionById(conflictingId);
        String requestedId = inputHelper.readCode("Requested section ID (desired new section): ");
        Section requested = dataStore.findSectionById(requestedId);

        if (conflicting == null || requested == null) {
            UIHelper.warning("One or both specified sections do not exist.");
            return;
        }

        String description = inputHelper.readLine("Explain the schedule overlap: ");
        CourseClashRequest request = new CourseClashRequest(
                UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                description, 1, conflicting, requested, student);
        request.submit();
        dataStore.getCourseClashRequests().add(request);
        CourseClashRequestRepository.save(request);
        UIHelper.success("Course clash request submitted successfully! Request ID: " + request.getRequestId());
        AppLogger.info(student.getStudentId() + " submitted course clash request " + request.getRequestId());
    }

    void submitGeneralRequest() {
        UIHelper.printSectionHeader("Submit General Academic Request");
        System.out.println("  Categories: COURSE, GRADING, TEACHING, PROFESSOR, CLASSMATE, OTHER");
        RequestCategory category;
        while (true) {
            String catStr = inputHelper.readCode("Category: ");
            try {
                category = RequestCategory.valueOf(catStr);
                break;
            } catch (IllegalArgumentException e) {
                UIHelper.warning("Invalid category. Choose from: COURSE, GRADING, TEACHING, PROFESSOR, CLASSMATE, OTHER.");
            }
        }

        String description = inputHelper.readLine("Describe your query / request: ");
        GenericRequest request = new GenericRequest(
                UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                description, 1, category, student);
        request.submit();
        dataStore.getGenericRequests().add(request);
        GenericRequestRepository.save(request);
        UIHelper.success("General request submitted successfully! Request ID: " + request.getRequestId());
        AppLogger.info(student.getStudentId() + " submitted general request " + request.getRequestId() + " (" + category + ")");
    }

    void viewMyRequests() {
        UIHelper.printSectionHeader("My Submitted Academic Requests");
        boolean any = false;

        for (Request r : dataStore.getCourseClashRequests()) {
            if (r.getSubmittedBy() == student) {
                any = true;
                System.out.println("   • [CLASH] ID: " + r.getRequestId() + " | Status: " + UIHelper.statusBadge(r.getStatus().name())
                        + " | Details: " + r.getDetails());
            }
        }
        for (Request r : dataStore.getGenericRequests()) {
            if (r.getSubmittedBy() == student) {
                any = true;
                System.out.println("   • [GENERAL] ID: " + r.getRequestId() + " | Status: " + UIHelper.statusBadge(r.getStatus().name())
                        + " | Details: " + r.getDetails());
            }
        }
        if (!any) {
            UIHelper.info("You have not submitted any academic requests.");
        }
    }

    void registerAsTeachingAssistant() {
        UIHelper.printSectionHeader("Register as Teaching Assistant");
        TeachingAssistant existing = dataStore.findTaById(student.getStudentId());
        if (existing != null) {
            UIHelper.warning("You are already registered as a Teaching Assistant (" + existing.getTaId() + ").");
            return;
        }

        System.out.println("  Student Name : " + student.getName());
        System.out.println("  Student ID   : " + student.getStudentId());
        System.out.println("  Student Email: " + student.getEmail());
        System.out.println();
        String confirm = inputHelper.readLine("Do you want to register as a Teaching Assistant? (y/n): ").trim().toLowerCase();
        if (!confirm.startsWith("y")) {
            UIHelper.info("TA registration cancelled.");
            return;
        }

        int maxTa = 0;
        for (TeachingAssistant ta : dataStore.getTeachingAssistants()) {
            String tid = ta.getTaId();
            if (tid != null && tid.toUpperCase().startsWith("TA")) {
                try {
                    int num = Integer.parseInt(tid.substring(2));
                    if (num > maxTa) maxTa = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        String taId = String.format("TA%03d", maxTa + 1);

        TeachingAssistant newTa = new TeachingAssistant(student.getName(), student.getEmail(), student.getPhone(),
                student.getStudentId(), null, taId);
        for (Enrollment e : student.getEnrollments()) {
            newTa.addEnrollment(e);
        }

        dataStore.getTeachingAssistants().add(newTa);
        TeachingAssistantRepository.save(newTa);

        // persist credentials so TA login works
        String credLine = taId.toLowerCase() + "::pass123::TA::" + taId + "::" + student.getEmail();
        campus.io.FileManager.appendLine("credentials.txt", credLine);
        String studentTaCred = student.getStudentId().toLowerCase() + "::pass123::TA::" + taId + "::" + student.getEmail();
        campus.io.FileManager.appendLine("credentials.txt", studentTaCred);

        AppLogger.info("Student " + student.getStudentId() + " registered self as TA " + taId);
        UIHelper.success("Successfully registered as Teaching Assistant (" + taId + ")!");
        UIHelper.info("A Permanent Instructor can now assign you to assist a course section.");

        String openNow = inputHelper.readLine("Open Teaching Assistant portal now? (y/n): ").trim().toLowerCase();
        if (openNow.startsWith("y")) {
            new TeachingAssistantMenu(newTa, scanner, dataStore).show();
        }
    }

    void viewMyFypGroup() {
        UIHelper.printSectionHeader("My FYP Group & Members Roster");
        FYPGroup myGroup = null;
        for (FYPGroup g : dataStore.getFypGroups()) {
            for (Student m : g.getMembers()) {
                if (m.getStudentId().equalsIgnoreCase(student.getStudentId())) {
                    myGroup = g;
                    break;
                }
            }
            if (myGroup != null) break;
        }

        if (myGroup == null) {
            UIHelper.info("You are currently not enrolled in any Final Year Project (FYP) group.");
            return;
        }

        System.out.println("  +--------------------------------------------------------------+");
        System.out.println("  | FINAL YEAR PROJECT (FYP) GROUP DETAILS                       |");
        System.out.println("  +--------------------------------------------------------------+");
        System.out.println("    Group ID      : " + myGroup.getGroupId());
        System.out.println("    Project Title : " + myGroup.getTitle());
        System.out.println("    Description   : " + myGroup.getDescription());
        System.out.println("    Supervisor    : " + (myGroup.getSupervisor() != null ? myGroup.getSupervisor().getName() : "None Assigned"));
        System.out.println("  +--------------------------------------------------------------+");
        System.out.println("  | ALL GROUP MEMBERS (" + myGroup.getMembers().size() + "):");
        int idx = 1;
        for (Student member : myGroup.getMembers()) {
            boolean isSelf = member.getStudentId().equalsIgnoreCase(student.getStudentId());
            String tag = isSelf ? " (You)" : "";
            System.out.println("    [" + idx++ + "] " + member.getName() + " (" + member.getStudentId() + ")" + tag
                    + " — " + member.getEmail());
        }
        System.out.println("  +--------------------------------------------------------------+");
    }
}