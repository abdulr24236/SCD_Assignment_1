/**
 * ============================================================
 * File        : AdminMenu.java
 */
package campus.service;

import campus.enums.Day;
import campus.enums.RequestStatus;
import campus.io.CourseClashRequestRepository;
import campus.io.GenericRequestRepository;
import campus.io.Repositories;
import campus.io.SectionRepository;
import campus.logging.AppLogger;
import campus.model.academic.Course;
import campus.model.academic.Schedule;
import campus.model.academic.Section;
import campus.model.person.AcademicOfficeAdmin;
import campus.model.person.Instructor;
import campus.model.person.PermanentInstructor;
import campus.model.person.VisitingInstructor;
import campus.model.request.CourseClashRequest;
import campus.model.request.GenericRequest;
import campus.model.request.Request;
import campus.util.InputHelper;
import campus.util.UIHelper;
import java.time.LocalTime;
import java.util.Scanner;

public class AdminMenu {
    private final AcademicOfficeAdmin admin;
    private final Scanner scanner;
    private final InputHelper inputHelper;
    private final DataStore dataStore;

    public AdminMenu(AcademicOfficeAdmin admin, Scanner scanner, DataStore dataStore) {
        this.admin = admin;
        this.scanner = scanner;
        this.inputHelper = new InputHelper(scanner);
        this.dataStore = dataStore;
    }

    public void show() {
        boolean loggedIn = true;
        while (loggedIn) {
            UIHelper.printUserHeader("Academic Office Admin", admin.getName(), admin.getAdminId());
            String[] options = {
                    "[1]  Create a course",
                    "[2]  Search for a course",
                    "[3]  Create a section",
                    "[4]  Set section capacity",
                    "[5]  Assign room and schedule to a section",
                    "[6]  Assign instructor to a section",
                    "[7]  View all pending academic requests",
                    "[8]  Approve a request",
                    "[9]  Reject a request",
                    "[10] Log out"
            };
            UIHelper.printMenuCard("Administrative Management Operations", options);

            int choice = inputHelper.readInt("Choose an option [1-10]: ", 1, 10);
            switch (choice) {
                case 1 -> createCourse();
                case 2 -> searchCourse();
                case 3 -> createSection();
                case 4 -> setSectionCapacity();
                case 5 -> assignRoom();
                case 6 -> assignInstructor();
                case 7 -> viewAllRequests();
                case 8 -> approveRequest();
                case 9 -> rejectRequest();
                case 10 -> {
                    loggedIn = false;
                    UIHelper.info("Logging out from Admin Portal.");
                }
            }
        }
    }

    private void createCourse() {
        UIHelper.printSectionHeader("Create New Course");
        String code = inputHelper.readCode("Course code (e.g. CS301): ");
        if (dataStore.findCourseByCode(code) != null) {
            UIHelper.warning("A course with code " + code + " already exists in the system.");
            return;
        }
        String title = inputHelper.readLine("Course title: ");
        int creditHours = inputHelper.readInt("Credit hours (1-6): ", 1, 6);

        Course course = new Course(code, title, creditHours);
        admin.createCourse(course);
        dataStore.getCourses().add(course);
        Repositories.COURSES.save(course);
        UIHelper.success("Course created successfully: " + code + " — " + title + " (" + creditHours + " credit hours)");
        AppLogger.info(admin.getAdminId() + " created course " + code);
    }

    private void searchCourse() {
        UIHelper.printSectionHeader("Search Course Records");
        String code = inputHelper.readCode("Enter course code to search: ");
        Course course = dataStore.findCourseByCode(code);
        if (course == null) {
            UIHelper.warning("No course found with code: " + code);
            return;
        }

        System.out.println();
        System.out.println(UIHelper.BRIGHT_CYAN + "  ┌────────────────────────────────────────────────────────────┐" + UIHelper.RESET);
        System.out.println(UIHelper.BRIGHT_CYAN + "  │ " + UIHelper.BOLD + "COURSE DETAILS" + UIHelper.RESET + "                                             │");
        System.out.println(UIHelper.BRIGHT_CYAN + "  ├────────────────────────────────────────────────────────────┤" + UIHelper.RESET);
        System.out.printf(UIHelper.BRIGHT_CYAN + "  │ " + UIHelper.RESET + "Code         : %-46s" + UIHelper.BRIGHT_CYAN + "│\n" + UIHelper.RESET, course.getCourseCode());
        System.out.printf(UIHelper.BRIGHT_CYAN + "  │ " + UIHelper.RESET + "Title        : %-46s" + UIHelper.BRIGHT_CYAN + "│\n" + UIHelper.RESET, course.getTitle());
        System.out.printf(UIHelper.BRIGHT_CYAN + "  │ " + UIHelper.RESET + "Credit Hours : %-46d" + UIHelper.BRIGHT_CYAN + "│\n" + UIHelper.RESET, course.getCreditHours());
        System.out.printf(UIHelper.BRIGHT_CYAN + "  │ " + UIHelper.RESET + "Sections     : %-46d" + UIHelper.BRIGHT_CYAN + "│\n" + UIHelper.RESET, course.getSections().size());
        System.out.println(UIHelper.BRIGHT_CYAN + "  └────────────────────────────────────────────────────────────┘" + UIHelper.RESET);

        if (!course.getSections().isEmpty()) {
            System.out.println(UIHelper.GRAY + "  Associated Sections:" + UIHelper.RESET);
            for (Section s : course.getSections()) {
                String sched = s.getSchedule() != null ? s.getSchedule().getScheduleInfo() : "No schedule set";
                System.out.println("   • Section " + s.getSectionId() + " | Seats: " + s.getAvailableSeats() + "/" + s.getCapacity() + " | " + sched);
            }
        }
    }

    private void createSection() {
        UIHelper.printSectionHeader("Create Course Section");
        String sectionId = inputHelper.readCode("Section ID (e.g. CS101-B): ");
        if (dataStore.findSectionById(sectionId) != null) {
            UIHelper.warning("A section with ID " + sectionId + " already exists.");
            return;
        }
        String courseCode = inputHelper.readCode("Associated course code: ");
        Course course = dataStore.findCourseByCode(courseCode);
        if (course == null) {
            UIHelper.warning("No course found with code: " + courseCode);
            return;
        }
        int capacity = inputHelper.readInt("Section capacity (1-500): ", 1, 500);

        Section section = new Section(sectionId, capacity, course);
        admin.createSection(section);
        dataStore.getSections().add(section);
        SectionRepository.save(section);
        UIHelper.success("Section created successfully: " + sectionId + " for course " + course.getCourseCode());
        AppLogger.info(admin.getAdminId() + " created section " + sectionId);
    }

    private void setSectionCapacity() {
        UIHelper.printSectionHeader("Modify Section Capacity");
        Section section = promptForSection();
        if (section == null) {
            return;
        }
        System.out.println("  Current capacity for " + section.getSectionId() + " is " + section.getCapacity()
                + " (Enrolled: " + section.getEnrollments().size() + ")");
        int capacity = inputHelper.readInt("Enter new capacity: ", 1, 500);
        admin.setCapacity(section, capacity);
        SectionRepository.saveAll(dataStore.getSections());
        UIHelper.success("Capacity of section " + section.getSectionId() + " updated to " + capacity + ".");
        AppLogger.info(admin.getAdminId() + " set capacity of " + section.getSectionId() + " to " + capacity);
    }

    private void assignRoom() {
        UIHelper.printSectionHeader("Assign Room & Timeslot to Section");
        Section section = promptForSection();
        if (section == null) {
            return;
        }

        System.out.println("  Available Days: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY");
        Day day;
        while (true) {
            String dayStr = inputHelper.readCode("Day of week: ");
            try {
                day = Day.valueOf(dayStr);
                break;
            } catch (IllegalArgumentException e) {
                UIHelper.warning("Invalid day name. Please enter a valid day (e.g. MONDAY).");
            }
        }

        LocalTime start;
        LocalTime end;
        while (true) {
            String startStr = inputHelper.readLine("Start time (HH:MM in 24hr format, e.g. 09:00): ");
            try {
                start = LocalTime.parse(startStr);
                break;
            } catch (Exception e) {
                UIHelper.warning("Invalid time format. Please use HH:MM.");
            }
        }

        while (true) {
            String endStr = inputHelper.readLine("End time (HH:MM in 24hr format, e.g. 10:30): ");
            try {
                end = LocalTime.parse(endStr);
                if (!end.isAfter(start)) {
                    UIHelper.warning("End time must be strictly after start time.");
                    continue;
                }
                break;
            } catch (Exception e) {
                UIHelper.warning("Invalid time format. Please use HH:MM.");
            }
        }

        String room = inputHelper.readCode("Room ID / Number (e.g. A-101, C-303): ");

        Schedule newSchedule = new Schedule(day, start, end, room);
        Section roomConflict = findRoomConflict(section, newSchedule);
        if (roomConflict != null) {
            UIHelper.error("Room " + room + " is already booked for section " + roomConflict.getSectionId()
                    + " at an overlapping time (" + roomConflict.getSchedule().getScheduleInfo() + ").");
            return;
        }

        if (section.getInstructor() != null) {
            Section instructorConflict = findInstructorConflict(section, section.getInstructor(), newSchedule);
            if (instructorConflict != null) {
                UIHelper.error("Assigned instructor " + section.getInstructor().getName()
                        + " is already teaching section " + instructorConflict.getSectionId() + " during this timeslot.");
                return;
            }
        }

        admin.assignRoom(section, newSchedule);
        SectionRepository.saveAll(dataStore.getSections());
        UIHelper.success("Schedule assigned: " + section.getSectionId() + " -> " + newSchedule.getScheduleInfo());
        AppLogger.info(admin.getAdminId() + " assigned room " + room + " to " + section.getSectionId());
    }

    private void assignInstructor() {
        UIHelper.printSectionHeader("Assign Instructor to Section");
        Section section = promptForSection();
        if (section == null) {
            return;
        }

        System.out.println("  (Enter Teacher ID, e.g. VI001, PI001)");
        String id = inputHelper.readCode("Instructor ID: ");
        Instructor instructor = findInstructorById(id);
        if (instructor == null) {
            UIHelper.warning("No visiting or permanent instructor found with ID: " + id);
            return;
        }

        if (section.getSchedule() != null) {
            Section conflict = findInstructorConflict(section, instructor, section.getSchedule());
            if (conflict != null) {
                UIHelper.error("Instructor " + instructor.getName() + " is already scheduled for section "
                        + conflict.getSectionId() + " at an overlapping time.");
                return;
            }
        }

        admin.assignInstructor(section, instructor);
        SectionRepository.saveAll(dataStore.getSections());
        UIHelper.success("Instructor " + instructor.getName() + " (" + id + ") successfully assigned to section " + section.getSectionId() + ".");
        AppLogger.info(admin.getAdminId() + " assigned instructor " + id + " to " + section.getSectionId());
    }

    private Instructor findInstructorById(String id) {
        VisitingInstructor vi = dataStore.findVisitingById(id);
        if (vi != null) {
            return vi;
        }
        return dataStore.findPermanentById(id);
    }

    private void viewAllRequests() {
        UIHelper.printSectionHeader("All Pending Academic Requests");
        boolean any = false;

        System.out.println(UIHelper.BOLD + "  Course Clash Requests:" + UIHelper.RESET);
        for (CourseClashRequest request : dataStore.getCourseClashRequests()) {
            if (request.getStatus() == RequestStatus.PENDING) {
                any = true;
                System.out.println("   • " + UIHelper.statusBadge("PENDING") + " ID: " + request.getRequestId()
                        + " | Student: " + (request.getSubmittedBy() != null ? request.getSubmittedBy().getName() : "N/A")
                        + " | Details: " + request.getDetails());
            }
        }

        System.out.println();
        System.out.println(UIHelper.BOLD + "  General Inquiries & Requests:" + UIHelper.RESET);
        for (GenericRequest request : dataStore.getGenericRequests()) {
            if (request.getStatus() == RequestStatus.PENDING) {
                any = true;
                System.out.println("   • " + UIHelper.statusBadge("PENDING") + " ID: " + request.getRequestId()
                        + " | Student: " + (request.getSubmittedBy() != null ? request.getSubmittedBy().getName() : "N/A")
                        + " | Category: " + request.getCategory()
                        + " | Details: " + request.getDetails());
            }
        }

        if (!any) {
            UIHelper.info("No pending requests found in the system.");
        }
    }

    private void approveRequest() {
        processRequestDecision(true);
    }

    private void rejectRequest() {
        processRequestDecision(false);
    }

    private void processRequestDecision(boolean approve) {
        String actionName = approve ? "Approve" : "Reject";
        UIHelper.printSectionHeader(actionName + " Academic Request");
        String id = inputHelper.readCode("Enter request ID: ");

        // check course clash requests
        for (CourseClashRequest request : dataStore.getCourseClashRequests()) {
            if (request.getRequestId().equalsIgnoreCase(id)) {
                applyDecision(request, approve);
                CourseClashRequestRepository.saveAll(dataStore.getCourseClashRequests());
                return;
            }
        }

        // check generic requests
        for (GenericRequest request : dataStore.getGenericRequests()) {
            if (request.getRequestId().equalsIgnoreCase(id)) {
                applyDecision(request, approve);
                GenericRequestRepository.saveAll(dataStore.getGenericRequests());
                return;
            }
        }

        UIHelper.warning("No request found with ID: " + id);
    }

    private void applyDecision(Request request, boolean approve) {
        if (approve) {
            admin.approveRequest(request);
            UIHelper.success("Request " + request.getRequestId() + " has been APPROVED.");
        } else {
            admin.rejectRequest(request);
            UIHelper.info("Request " + request.getRequestId() + " has been REJECTED.");
        }
        AppLogger.info(admin.getAdminId() + " " + (approve ? "approved" : "rejected") + " request " + request.getRequestId());
    }

    private Section findRoomConflict(Section section, Schedule newSchedule) {
        for (Section other : dataStore.getSections()) {
            if (other == section) {
                continue;
            }
            Schedule otherSchedule = other.getSchedule();
            if (otherSchedule != null && otherSchedule.getRoom().equalsIgnoreCase(newSchedule.getRoom())
                    && newSchedule.hasClash(otherSchedule)) {
                return other;
            }
        }
        return null;
    }

    private Section findInstructorConflict(Section section, Instructor instructor, Schedule schedule) {
        for (Section other : dataStore.getSections()) {
            if (other == section || other.getInstructor() != instructor || other.getSchedule() == null) {
                continue;
            }
            if (schedule.hasClash(other.getSchedule())) {
                return other;
            }
        }
        return null;
    }

    private Section promptForSection() {
        String id = inputHelper.readCode("Enter section ID: ");
        Section section = dataStore.findSectionById(id);
        if (section == null) {
            UIHelper.warning("No section found with ID: " + id);
        }
        return section;
    }
}