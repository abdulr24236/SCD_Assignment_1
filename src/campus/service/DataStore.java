package campus.service;

import campus.exceptions.CourseClashException;
import campus.exceptions.CourseFullException;
import campus.io.AssignmentRepository;
import campus.io.AttendanceRepository;
import campus.io.CourseClashRequestRepository;
import campus.io.EnrollmentRepository;
import campus.io.FeedbackRepository;
import campus.io.FYPEvaluationRepository;
import campus.io.FYPGroupRepository;
import campus.io.FYPMeetingRepository;
import campus.io.GenericRequestRepository;
import campus.io.Repositories;
import campus.io.SectionRepository;
import campus.io.SubmissionRepository;
import campus.io.TeachingAssistantRepository;
import campus.logging.AppLogger;
import campus.model.academic.Attendance;
import campus.model.academic.Course;
import campus.model.academic.Enrollment;
import campus.model.academic.Schedule;
import campus.model.academic.Section;
import campus.model.assessment.Assignment;
import campus.model.assessment.Feedback;
import campus.model.assessment.Submission;
import campus.model.fyp.FYPEvaluation;
import campus.model.fyp.FYPGroup;
import campus.model.fyp.FYPMeeting;
import campus.model.person.AcademicOfficeAdmin;
import campus.model.person.NormalStudent;
import campus.model.person.PermanentInstructor;
import campus.model.person.TeachingAssistant;
import campus.model.person.VisitingInstructor;
import campus.model.request.CourseClashRequest;
import campus.model.request.GenericRequest;
import campus.enums.Day;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds every entity currently in memory and is the single place that
 * knows the required load order (see project notes: Phase 1 -> Sections ->
 * TeachingAssistants -> CourseClashRequests/Assignments ->
 * Enrollments/Attendance/Submissions -> Feedback -> FYPGroups ->
 * FYPMeetings/FYPEvaluations).
 *
 * Also seeds a small set of demo accounts/courses/sections on first run,
 * so the app isn't completely empty the first time it's started — but only
 * if they don't already exist (checked by ID), so re-running never creates
 * duplicates.
 */
public class DataStore {
    private List<Course> courses = new ArrayList<>();
    private List<NormalStudent> normalStudents = new ArrayList<>();
    private List<VisitingInstructor> visitingInstructors = new ArrayList<>();
    private List<PermanentInstructor> permanentInstructors = new ArrayList<>();
    private List<AcademicOfficeAdmin> admins = new ArrayList<>();
    private List<GenericRequest> genericRequests = new ArrayList<>();
    private List<Section> sections = new ArrayList<>();
    private List<TeachingAssistant> teachingAssistants = new ArrayList<>();
    private List<CourseClashRequest> courseClashRequests = new ArrayList<>();
    private List<Assignment> assignments = new ArrayList<>();
    private List<Enrollment> enrollments = new ArrayList<>();
    private List<Attendance> attendanceRecords = new ArrayList<>();
    private List<Submission> submissions = new ArrayList<>();
    private List<Feedback> feedbackList = new ArrayList<>();
    private List<FYPGroup> fypGroups = new ArrayList<>();
    private List<FYPMeeting> fypMeetings = new ArrayList<>();
    private List<FYPEvaluation> fypEvaluations = new ArrayList<>();

    public void loadAll() {
        courses = Repositories.COURSES.loadAll();
        normalStudents = Repositories.NORMAL_STUDENTS.loadAll();
        visitingInstructors = Repositories.VISITING_INSTRUCTORS.loadAll();
        permanentInstructors = Repositories.PERMANENT_INSTRUCTORS.loadAll();
        admins = Repositories.ACADEMIC_OFFICE_ADMINS.loadAll();
        genericRequests = GenericRequestRepository.loadAll(normalStudents);

        sections = SectionRepository.loadAll(courses, visitingInstructors, permanentInstructors);
        teachingAssistants = TeachingAssistantRepository.loadAll(sections);
        courseClashRequests = CourseClashRequestRepository.loadAll(sections, normalStudents);
        assignments = AssignmentRepository.loadAll(sections, teachingAssistants);

        enrollments = EnrollmentRepository.loadAll(sections, normalStudents, teachingAssistants);
        attendanceRecords = AttendanceRepository.loadAll(sections, normalStudents, teachingAssistants);
        submissions = SubmissionRepository.loadAll(assignments, normalStudents, teachingAssistants);
        feedbackList = FeedbackRepository.loadAll(submissions, teachingAssistants, permanentInstructors);
        fypGroups = FYPGroupRepository.loadAll(permanentInstructors, normalStudents, teachingAssistants);
        fypMeetings = FYPMeetingRepository.loadAll(fypGroups);
        fypEvaluations = FYPEvaluationRepository.loadAll(fypGroups);

        AppLogger.info("Data loaded: " + courses.size() + " courses, " + normalStudents.size()
                + " students, " + sections.size() + " sections.");
    }

    public void seedDemoDataIfMissing() {
        seedAdmin();
        seedStudents();
        seedInstructors();
        seedCoursesAndSections();
        seedTeachingAssistants();
    }

    private void seedAdmin() {
        if (findAdminById("ADM001") != null) {
            return;
        }
        AcademicOfficeAdmin admin = new AcademicOfficeAdmin("Fateh Khan", "admin@fast.edu", "0300-1111111", "ADM001");
        admins.add(admin);
        Repositories.ACADEMIC_OFFICE_ADMINS.save(admin);
        AppLogger.info("Seeded demo admin ADM001");
    }

    private void seedStudents() {
        String[][] demoStudents = {
                {"S001", "Ahmad Ali", "ahmad@fast.edu"},
                {"S002", "Bilal Khan", "bilal@fast.edu"},
                {"S003", "Saim Arif", "saim@fast.edu"},
                {"S004", "Ayesha Noor", "ayesha@fast.edu"},
                {"S005", "Hina Malik", "hina@fast.edu"},
                {"S006", "Usman Tariq", "usman@fast.edu"}
        };
        for (String[] data : demoStudents) {
            if (findStudentById(data[0]) != null) {
                continue;
            }
            NormalStudent student = new NormalStudent(data[1], data[2], "0300-2000000", data[0]);
            normalStudents.add(student);
            Repositories.NORMAL_STUDENTS.save(student);
        }
        AppLogger.info("Seeded demo students S001-S006 (any already present were skipped)");
    }

    private void seedInstructors() {
        if (findVisitingById("VI001") == null) {
            VisitingInstructor vi1 = new VisitingInstructor("Faizan Sheikh", "faizan@fast.edu", "0300-3000001", "VI001");
            visitingInstructors.add(vi1);
            Repositories.VISITING_INSTRUCTORS.save(vi1);
        }
        if (findVisitingById("VI002") == null) {
            VisitingInstructor vi2 = new VisitingInstructor("Zara Iqbal", "zara@fast.edu", "0300-3000002", "VI002");
            visitingInstructors.add(vi2);
            Repositories.VISITING_INSTRUCTORS.save(vi2);
        }
        if (findPermanentById("PI001") == null) {
            PermanentInstructor pi1 = new PermanentInstructor("Arslan Asif", "arslan@fast.edu", "0300-4000001", "PI001");
            permanentInstructors.add(pi1);
            Repositories.PERMANENT_INSTRUCTORS.save(pi1);
        }
        if (findPermanentById("PI002") == null) {
            PermanentInstructor pi2 = new PermanentInstructor("Nadia Farooq", "nadia@fast.edu", "0300-4000002", "PI002");
            permanentInstructors.add(pi2);
            Repositories.PERMANENT_INSTRUCTORS.save(pi2);
        }
        AppLogger.info("Seeded demo instructors VI001, VI002, PI001, PI002 (any already present were skipped)");
    }

    private void seedCoursesAndSections() {
        if (findCourseByCode("CS101") == null) {
            Course cs101 = new Course("CS101", "Intro to Programming", 3);
            courses.add(cs101);
            Repositories.COURSES.save(cs101);
        }
        if (findCourseByCode("SE201") == null) {
            Course se201 = new Course("SE201", "Software Engineering", 3);
            courses.add(se201);
            Repositories.COURSES.save(se201);
        }

        if (findSectionById("CS101-A") == null) {
            Section cs101a = new Section("CS101-A", 2, findCourseByCode("CS101")); // small capacity, for demoing CourseFullException
            cs101a.assignInstructor(findVisitingById("VI001"));
            cs101a.setSchedule(new Schedule(Day.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), "A1"));
            sections.add(cs101a);
            SectionRepository.save(cs101a);
        }

        if (findSectionById("SE201-A") == null) {
            Section se201a = new Section("SE201-A", 5, findCourseByCode("SE201"));
            se201a.assignInstructor(findPermanentById("PI001"));
            se201a.setSchedule(new Schedule(Day.TUESDAY, LocalTime.of(11, 0), LocalTime.of(12, 0), "B2"));
            sections.add(se201a);
            SectionRepository.save(se201a);
        }

        // Checked independently of section creation above (not nested inside
        // it) — so this still runs and self-heals even for a project that
        // already has CS101-A saved from before this fix existed, since its
        // reloaded enrollments list would still be empty either way.
        Section cs101aForSeeding = findSectionById("CS101-A");
        if (cs101aForSeeding != null && cs101aForSeeding.getEnrollments().isEmpty()) {
            try {
                findStudentById("S001").register(cs101aForSeeding);
                findStudentById("S002").register(cs101aForSeeding);
            } catch (CourseFullException | CourseClashException e) {
                AppLogger.error("Unexpected error seeding CS101-A enrollments: " + e.getMessage());
            }
            for (Enrollment enrollment : cs101aForSeeding.getEnrollments()) {
                EnrollmentRepository.save(enrollment);
            }
        }

        AppLogger.info("Seeded demo courses CS101/SE201 and sections CS101-A/SE201-A (any already present were skipped)");
    }

    private void seedTeachingAssistants() {
        if (findTaById("TA001") == null && findPermanentById("PI001") != null) {
            TeachingAssistant ta1 = findPermanentById("PI001").assignTA(findStudentById("S004"), findSectionById("CS101-A"));
            teachingAssistants.add(ta1);
            TeachingAssistantRepository.save(ta1);
        }
        if (findTaById("TA002") == null && findPermanentById("PI002") != null) {
            TeachingAssistant ta2 = findPermanentById("PI002").assignTA(findStudentById("S005"), findSectionById("SE201-A"));
            teachingAssistants.add(ta2);
            TeachingAssistantRepository.save(ta2);
        }
        AppLogger.info("Seeded demo TAs TA001 (from S004), TA002 (from S005) (any already present were skipped)");
    }

    // --- Getters, so menu classes can read and persist data ---

    public List<Course> getCourses() {
        return courses;
    }

    public List<NormalStudent> getNormalStudents() {
        return normalStudents;
    }

    public List<VisitingInstructor> getVisitingInstructors() {
        return visitingInstructors;
    }

    public List<PermanentInstructor> getPermanentInstructors() {
        return permanentInstructors;
    }

    public List<AcademicOfficeAdmin> getAdmins() {
        return admins;
    }

    public List<GenericRequest> getGenericRequests() {
        return genericRequests;
    }

    public List<Section> getSections() {
        return sections;
    }

    public List<TeachingAssistant> getTeachingAssistants() {
        return teachingAssistants;
    }

    public List<CourseClashRequest> getCourseClashRequests() {
        return courseClashRequests;
    }

    public List<Assignment> getAssignments() {
        return assignments;
    }

    public List<Enrollment> getEnrollments() {
        return enrollments;
    }

    public List<Attendance> getAttendanceRecords() {
        return attendanceRecords;
    }

    public List<Submission> getSubmissions() {
        return submissions;
    }

    public List<Feedback> getFeedbackList() {
        return feedbackList;
    }

    public List<FYPGroup> getFypGroups() {
        return fypGroups;
    }

    public List<FYPMeeting> getFypMeetings() {
        return fypMeetings;
    }

    public List<FYPEvaluation> getFypEvaluations() {
        return fypEvaluations;
    }

    // --- Lookup helpers, used both by seeding and by LoginService ---

    public AcademicOfficeAdmin findAdminById(String id) {
        for (AcademicOfficeAdmin admin : admins) {
            if (admin.getAdminId().equals(id)) {
                return admin;
            }
        }
        return null;
    }

    public NormalStudent findStudentById(String id) {
        if (id == null) return null;
        for (NormalStudent student : normalStudents) {
            if (student.getStudentId().equalsIgnoreCase(id)) {
                return student;
            }
        }
        return null;
    }

    public TeachingAssistant findTaById(String id) {
        if (id == null) return null;
        for (TeachingAssistant ta : teachingAssistants) {
            if (ta.getTaId().equalsIgnoreCase(id) || ta.getStudentId().equalsIgnoreCase(id)) {
                return ta;
            }
        }
        return null;
    }

    public campus.model.person.Student findAnyStudentById(String id) {
        if (id == null) return null;
        TeachingAssistant ta = findTaById(id);
        if (ta != null) return ta;
        return findStudentById(id);
    }

    public VisitingInstructor findVisitingById(String id) {
        for (VisitingInstructor vi : visitingInstructors) {
            if (vi.getTeacherId().equals(id)) {
                return vi;
            }
        }
        return null;
    }

    public PermanentInstructor findPermanentById(String id) {
        for (PermanentInstructor pi : permanentInstructors) {
            if (pi.getTeacherId().equals(id)) {
                return pi;
            }
        }
        return null;
    }

    public Course findCourseByCode(String code) {
        for (Course course : courses) {
            if (course.getCourseCode().equals(code)) {
                return course;
            }
        }
        return null;
    }

    public Section findSectionById(String id) {
        for (Section section : sections) {
            if (section.getSectionId().equals(id)) {
                return section;
            }
        }
        return null;
    }
}