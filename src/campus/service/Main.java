
package campus.service;

import campus.io.Repositories;
import campus.io.TeachingAssistantRepository;
import campus.logging.AppLogger;
import campus.model.person.AcademicOfficeAdmin;
import campus.model.person.NormalStudent;
import campus.model.person.PermanentInstructor;
import campus.model.person.Person;
import campus.model.person.Student;
import campus.model.person.TeachingAssistant;
import campus.model.person.VisitingInstructor;
import campus.util.InputHelper;
import campus.util.UIHelper;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        InputHelper inputHelper = new InputHelper(scanner);
        DataStore dataStore = new DataStore();

        UIHelper.printMainBanner();
        System.out.println("  Loading system repositories and database files...");
        dataStore.loadAll();
        dataStore.seedDemoDataIfMissing();
        UIHelper.success("System initialized. All records and repositories ready.\n");

        LoginService loginService = new LoginService(dataStore);

        boolean running = true;
        while (running) {
            String[] authOptions = {
                    "[1] Login",
                    "[2] Register as Student",
                    "[3] Register as Instructor",
                    "[4] Register as Academic Office Admin",
                    "[5] Register as Teaching Assistant",
                    "[6] View Demo Accounts & Guide",
                    "[0] Exit Application"
            };
            UIHelper.printMenuCard("Authentication & Portal Gateway", authOptions);

            int choice = inputHelper.readInt("Your choice [0-6]", 0, 6);
            switch (choice) {
                case 1 -> handleLogin(inputHelper, loginService, dataStore);
                case 2 -> registerStudent(inputHelper, loginService, dataStore);
                case 3 -> registerInstructor(inputHelper, loginService, dataStore);
                case 4 -> registerAdmin(inputHelper, loginService, dataStore);
                case 5 -> registerTeachingAssistant(inputHelper, loginService, dataStore);
                case 6 -> showDemoAccountsGuide();
                case 0 -> {
                    running = false;
                    System.out.println();
                    UIHelper.info("Exiting Campus Management System. Thank you!");
                    AppLogger.info("Shutdown");
                }
            }
            System.out.println();
        }

        scanner.close();
    }

    // handles user login by username, email, or id
    private static void handleLogin(InputHelper inputHelper, LoginService loginService, DataStore dataStore) {
        UIHelper.printSectionHeader("User Login");
        String identifier = inputHelper.readLine("Enter email or ID    : ");
        String password = inputHelper.readLine("Enter password       : ");

        Person person = loginService.authenticate(identifier, password);
        if (person == null) {
            UIHelper.error("Invalid credentials.");
            AppLogger.error("Failed login: " + identifier);
            return;
        }

        UIHelper.success("Welcome, " + person.getName() + " (Role: " + person.getRole() + ")");
        AppLogger.info("Login successful: " + identifier + " (" + person.getRole() + ")");

        // route to role specific menu
        routeToMenu(person, inputHelper.getScanner(), dataStore);
        AppLogger.info("Logout: " + identifier);
    }

    // registers student directly with name, email, password, rollno
    private static void registerStudent(InputHelper inputHelper, LoginService loginService, DataStore dataStore) {
        UIHelper.printSectionHeader("Register as Student");
        InputHelper.printCancelHint();

        String name = inputHelper.readCancellable("Enter name     : ");
        if (name == null) return;

        String email = inputHelper.readEmailCancellable("Enter email    : ");
        if (email == null) return;

        if (loginService.isEmailRegistered(email)) {
            UIHelper.warning("Email already registered: " + email);
            AppLogger.error("Email already registered: " + email);
            return;
        }

        String password = inputHelper.readPasswordCancellable("Enter password : ");
        if (password == null) return;

        String suggestedId = loginService.getNextStudentId();
        String rollNo = inputHelper.readCancellable("Enter rollNo", suggestedId);
        if (rollNo == null) return;
        rollNo = rollNo.toUpperCase();

        if (loginService.isIdRegistered(rollNo)) {
            UIHelper.warning("Roll number already exists: " + rollNo);
            AppLogger.error("Roll number already exists: " + rollNo);
            return;
        }

        NormalStudent student = new NormalStudent(name, email, "0300-0000000", rollNo);
        dataStore.getNormalStudents().add(student);
        Repositories.NORMAL_STUDENTS.save(student);
        loginService.registerUser(rollNo.toLowerCase(), password, "STUDENT", rollNo, email);
        AppLogger.info("Student registered: " + rollNo);
        UIHelper.success("Student registered: " + rollNo);
    }

    // registers instructor directly with name, email, password, type, teacherid
    private static void registerInstructor(InputHelper inputHelper, LoginService loginService, DataStore dataStore) {
        UIHelper.printSectionHeader("Register as Instructor");
        InputHelper.printCancelHint();

        String name = inputHelper.readCancellable("Enter name     : ");
        if (name == null) return;

        String email = inputHelper.readEmailCancellable("Enter email    : ");
        if (email == null) return;

        if (loginService.isEmailRegistered(email)) {
            UIHelper.warning("Email already registered: " + email);
            AppLogger.error("Email already registered: " + email);
            return;
        }

        String password = inputHelper.readPasswordCancellable("Enter password : ");
        if (password == null) return;

        // read type with 0-to-cancel support
        String type = null;
        while (true) {
            String raw = inputHelper.readCancellable("Enter type (PERMANENT / VISITING) [or 0 to cancel]: ");
            if (raw == null) return;           // user typed 0 inside readCancellable
            raw = raw.toUpperCase();
            if (raw.equals("PERMANENT") || raw.equals("P") || raw.equals("VISITING") || raw.equals("V")) {
                type = raw;
                break;
            }
            UIHelper.warning("Please enter 'PERMANENT' or 'VISITING'.");
        }

        if (type.startsWith("P")) {
            String suggestedId = loginService.getNextPermanentId();
            String teacherId = inputHelper.readCancellable("Enter teacherId", suggestedId);
            if (teacherId == null) return;
            teacherId = teacherId.toUpperCase();
            if (loginService.isIdRegistered(teacherId)) {
                UIHelper.warning("Instructor ID already exists: " + teacherId);
                AppLogger.error("Instructor ID already exists: " + teacherId);
                return;
            }
            PermanentInstructor pi = new PermanentInstructor(name, email, "0300-0000000", teacherId);
            dataStore.getPermanentInstructors().add(pi);
            Repositories.PERMANENT_INSTRUCTORS.save(pi);
            loginService.registerUser(teacherId.toLowerCase(), password, "PERMANENT_INSTRUCTOR", teacherId, email);
            AppLogger.info("Instructor registered: " + teacherId);
            UIHelper.success("Permanent Instructor registered: " + teacherId);
        } else {
            String suggestedId = loginService.getNextVisitingId();
            String teacherId = inputHelper.readCancellable("Enter teacherId", suggestedId);
            if (teacherId == null) return;
            teacherId = teacherId.toUpperCase();
            if (loginService.isIdRegistered(teacherId)) {
                UIHelper.warning("Instructor ID already exists: " + teacherId);
                AppLogger.error("Instructor ID already exists: " + teacherId);
                return;
            }
            VisitingInstructor vi = new VisitingInstructor(name, email, "0300-0000000", teacherId);
            dataStore.getVisitingInstructors().add(vi);
            Repositories.VISITING_INSTRUCTORS.save(vi);
            loginService.registerUser(teacherId.toLowerCase(), password, "VISITING_INSTRUCTOR", teacherId, email);
            AppLogger.info("Instructor registered: " + teacherId);
            UIHelper.success("Visiting Instructor registered: " + teacherId);
        }
    }

    // registers academic office administrator directly
    private static void registerAdmin(InputHelper inputHelper, LoginService loginService, DataStore dataStore) {
        UIHelper.printSectionHeader("Register as Academic Office Admin");
        InputHelper.printCancelHint();

        String name = inputHelper.readCancellable("Enter name     : ");
        if (name == null) return;

        String email = inputHelper.readEmailCancellable("Enter email    : ");
        if (email == null) return;

        if (loginService.isEmailRegistered(email)) {
            UIHelper.warning("Email already registered: " + email);
            AppLogger.error("Email already registered: " + email);
            return;
        }

        String password = inputHelper.readPasswordCancellable("Enter password : ");
        if (password == null) return;

        String suggestedId = loginService.getNextAdminId();
        String adminId = inputHelper.readCancellable("Enter adminId  ", suggestedId);
        if (adminId == null) return;
        adminId = adminId.toUpperCase();

        if (loginService.isIdRegistered(adminId)) {
            UIHelper.warning("Admin ID already exists: " + adminId);
            AppLogger.error("Admin ID already exists: " + adminId);
            return;
        }

        AcademicOfficeAdmin admin = new AcademicOfficeAdmin(name, email, "0300-0000000", adminId);
        dataStore.getAdmins().add(admin);
        Repositories.ACADEMIC_OFFICE_ADMINS.save(admin);
        loginService.registerUser(adminId.toLowerCase(), password, "ADMIN", adminId, email);
        AppLogger.info("Admin registered: " + adminId);
        UIHelper.success("Admin registered: " + adminId);
    }

    // registers teaching assistant (allows existing students to register as TA, or creates new TA)
    private static void registerTeachingAssistant(InputHelper inputHelper, LoginService loginService, DataStore dataStore) {
        UIHelper.printSectionHeader("Register as Teaching Assistant");
        InputHelper.printCancelHint();

        String email = inputHelper.readEmailCancellable("Enter student email : ");
        if (email == null) return;

        // Check if an existing student is registering as TA
        NormalStudent existingStudent = null;
        for (NormalStudent s : dataStore.getNormalStudents()) {
            if (s.getEmail().equalsIgnoreCase(email)) {
                existingStudent = s;
                break;
            }
        }

        if (existingStudent != null) {
            TeachingAssistant alreadyTa = dataStore.findTaById(existingStudent.getStudentId());
            if (alreadyTa != null) {
                UIHelper.warning(existingStudent.getName() + " (" + existingStudent.getStudentId()
                        + ") is already registered as a Teaching Assistant (" + alreadyTa.getTaId() + ").");
                return;
            }

            UIHelper.info("Registered student found: " + existingStudent.getName() + " (" + existingStudent.getStudentId() + ")");
            String password = inputHelper.readPasswordCancellable("Enter account password : ");
            if (password == null) return;

            String suggestedTaId = loginService.getNextTaId();
            String taId = inputHelper.readCancellable("Enter TA ID        ", suggestedTaId);
            if (taId == null) return;
            taId = taId.toUpperCase();

            TeachingAssistant ta = new TeachingAssistant(existingStudent.getName(), existingStudent.getEmail(),
                    existingStudent.getPhone(), existingStudent.getStudentId(), null, taId);
            for (campus.model.academic.Enrollment e : existingStudent.getEnrollments()) {
                ta.addEnrollment(e);
            }

            dataStore.getTeachingAssistants().add(ta);
            TeachingAssistantRepository.save(ta);
            loginService.registerTaCredentials(taId, existingStudent.getStudentId(), existingStudent.getEmail(), password);
            AppLogger.info("Student " + existingStudent.getStudentId() + " registered as TA " + taId);
            UIHelper.success("Student " + existingStudent.getName() + " (" + existingStudent.getStudentId()
                    + ") successfully registered as TA " + taId + "!");
            UIHelper.info("An instructor can now assign you to assist a course section.");
            return;
        }

        // New student registering as Teaching Assistant from scratch
        String name = inputHelper.readCancellable("Enter full name    : ");
        if (name == null) return;

        String password = inputHelper.readPasswordCancellable("Enter password     : ");
        if (password == null) return;

        String suggestedStudentId = loginService.getNextStudentId();
        String studentId = inputHelper.readCancellable("Enter studentId    ", suggestedStudentId);
        if (studentId == null) return;
        studentId = studentId.toUpperCase();

        String suggestedTaId = loginService.getNextTaId();
        String taId = inputHelper.readCancellable("Enter taId         ", suggestedTaId);
        if (taId == null) return;
        taId = taId.toUpperCase();

        NormalStudent student = new NormalStudent(name, email, "0300-0000000", studentId);
        dataStore.getNormalStudents().add(student);
        campus.io.Repositories.NORMAL_STUDENTS.save(student);

        TeachingAssistant ta = new TeachingAssistant(name, email, "0300-0000000", studentId, null, taId);
        dataStore.getTeachingAssistants().add(ta);
        TeachingAssistantRepository.save(ta);
        loginService.registerTaCredentials(taId, studentId, email, password);
        AppLogger.info("New TA registered: " + taId + " with student ID " + studentId);
        UIHelper.success("Teaching Assistant registered: " + taId + " (Student ID: " + studentId + ")!");
        UIHelper.info("An instructor can now assign you to assist a course section.");
    }

    // displays pre-seeded demo accounts table using pure ASCII borders
    private static void showDemoAccountsGuide() {
        UIHelper.printSectionHeader("Pre-Seeded Demo Accounts & Credentials");
        String sep = "  +-----------------------+--------------+-------------+-----------------+-----------+";
        System.out.println(UIHelper.CYAN + sep + UIHelper.RESET);
        System.out.println(UIHelper.CYAN + "  | " + UIHelper.BOLD + "Role                  " + UIHelper.RESET
                + UIHelper.CYAN + "| " + UIHelper.BOLD + "Name         " + UIHelper.RESET
                + UIHelper.CYAN + "| " + UIHelper.BOLD + "Login ID    " + UIHelper.RESET
                + UIHelper.CYAN + "| " + UIHelper.BOLD + "Email           " + UIHelper.RESET
                + UIHelper.CYAN + "| " + UIHelper.BOLD + "Password  " + UIHelper.RESET
                + UIHelper.CYAN + "|" + UIHelper.RESET);
        System.out.println(UIHelper.CYAN + sep + UIHelper.RESET);
        System.out.println("  | Academic Admin        | Fateh Khan   | admin / ADM | admin@fast.edu  | admin123  |");
        System.out.println("  | Permanent Instructor  | Arslan Asif  | pi001 / PI  | arslan@fast.edu | pass123   |");
        System.out.println("  | Permanent Instructor  | Nadia Farooq | pi002 / PI  | nadia@fast.edu  | pass123   |");
        System.out.println("  | Visiting Instructor   | Faizan Sheikh| vi001 / VI  | faizan@fast.edu | pass123   |");
        System.out.println("  | Visiting Instructor   | Zara Iqbal   | vi002 / VI  | zara@fast.edu   | pass123   |");
        System.out.println("  | Teaching Assistant    | Ayesha Noor  | ta001 / TA  | ayesha@fast.edu | pass123   |");
        System.out.println("  | Teaching Assistant    | Hina Malik   | ta002 / TA  | hina@fast.edu   | pass123   |");
        System.out.println("  | Normal Student        | Ahmad Ali    | s001 / S001 | ahmad@fast.edu  | pass123   |");
        System.out.println("  | Normal Student        | Bilal Khan   | s002 / S002 | bilal@fast.edu  | pass123   |");
        System.out.println("  | Normal Student        | Saim Arif    | s003 / S003 | saim@fast.edu   | pass123   |");
        System.out.println("  | Normal Student        | Usman Tariq  | s006 / S006 | usman@fast.edu  | pass123   |");
        System.out.println(UIHelper.CYAN + sep + UIHelper.RESET);
        System.out.println(UIHelper.GRAY
                + "  * Tip: Log in using the Login ID, full User ID, or Email address."
                + UIHelper.RESET);
    }

    // routes to correct role portal checking child classes first
    private static void routeToMenu(Person person, Scanner scanner, DataStore dataStore) {
        if (person instanceof AcademicOfficeAdmin admin) {
            new AdminMenu(admin, scanner, dataStore).show();
        } else if (person instanceof TeachingAssistant ta) {
            new TeachingAssistantMenu(ta, scanner, dataStore).show();
        } else if (person instanceof Student student) {
            new StudentMenu(student, scanner, dataStore).show();
        } else if (person instanceof PermanentInstructor pi) {
            new PermanentInstructorMenu(pi, scanner, dataStore).show();
        } else if (person instanceof VisitingInstructor vi) {
            new VisitingInstructorMenu(vi, scanner, dataStore).show();
        }
    }
}