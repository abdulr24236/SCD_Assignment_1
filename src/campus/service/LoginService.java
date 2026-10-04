/**
 * ============================================================
 * File        : LoginService.java
 */
package campus.service;

import campus.io.FileManager;
import campus.logging.AppLogger;
import campus.model.person.AcademicOfficeAdmin;
import campus.model.person.NormalStudent;
import campus.model.person.PermanentInstructor;
import campus.model.person.Person;
import campus.model.person.TeachingAssistant;
import campus.model.person.VisitingInstructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginService {
    public record Credential(String password, String role, String id, String email) {
        // legacy compatibility constructor
        public Credential(String password, String role, String id) {
            this(password, role, id, "");
        }
    }

    private static final String CREDENTIALS_FILE = "credentials.txt";
    private final Map<String, Credential> credentials = new HashMap<>();
    private final DataStore dataStore;

    public LoginService(DataStore dataStore) {
        this.dataStore = dataStore;
        // seed standard demo credentials
        registerSeedCredential("admin", "admin123", "ADMIN", "ADM001", "admin@fast.edu");
        registerSeedCredential("s001", "pass123", "STUDENT", "S001", "ahmad@fast.edu");
        registerSeedCredential("s002", "pass123", "STUDENT", "S002", "bilal@fast.edu");
        registerSeedCredential("s003", "pass123", "STUDENT", "S003", "saim@fast.edu");
        registerSeedCredential("s004", "pass123", "STUDENT", "S004", "ayesha@fast.edu");
        registerSeedCredential("s005", "pass123", "STUDENT", "S005", "hina@fast.edu");
        registerSeedCredential("s006", "pass123", "STUDENT", "S006", "usman@fast.edu");
        registerSeedCredential("ta001", "pass123", "TA", "TA001", "ayesha@fast.edu");
        registerSeedCredential("ta002", "pass123", "TA", "TA002", "hina@fast.edu");
        registerSeedCredential("vi001", "pass123", "VISITING_INSTRUCTOR", "VI001", "faizan@fast.edu");
        registerSeedCredential("vi002", "pass123", "VISITING_INSTRUCTOR", "VI002", "zara@fast.edu");
        registerSeedCredential("pi001", "pass123", "PERMANENT_INSTRUCTOR", "PI001", "arslan@fast.edu");
        registerSeedCredential("pi002", "pass123", "PERMANENT_INSTRUCTOR", "PI002", "nadia@fast.edu");

        // restore dynamically registered credentials from disk
        loadPersistedCredentials();
    }

    // indexes single credential under username, id, and email for fast flexible login
    private void registerSeedCredential(String username, String password, String role, String id, String email) {
        Credential cred = new Credential(password, role, id, email);
        if (username != null && !username.isEmpty()) {
            credentials.put(username.toLowerCase(), cred);
        }
        if (id != null && !id.isEmpty()) {
            credentials.put(id.toLowerCase(), cred);
        }
        if (email != null && !email.isEmpty()) {
            credentials.put(email.toLowerCase(), cred);
        }
    }

    // loads dynamically registered credentials saved across restarts
    private void loadPersistedCredentials() {
        List<String> lines = FileManager.readLines(CREDENTIALS_FILE);
        for (String line : lines) {
            String[] parts = line.split(FileManager.DELIMITER, -1);
            if (parts.length >= 4) {
                String username = parts[0];
                String password = parts[1];
                String role = parts[2];
                String id = parts[3];
                String email = parts.length > 4 ? parts[4] : "";
                registerSeedCredential(username, password, role, id, email);
            }
        }
    }

    // registers newly created user credentials into memory and appends to credentials file
    public synchronized boolean registerUser(String username, String password, String role, String id, String email) {
        Credential cred = new Credential(password, role, id, email);
        // map identifiers to credential
        if (username != null && !username.isEmpty()) {
            credentials.put(username.toLowerCase(), cred);
        }
        if (id != null && !id.isEmpty()) {
            credentials.put(id.toLowerCase(), cred);
        }
        if (email != null && !email.isEmpty()) {
            credentials.put(email.toLowerCase(), cred);
        }

        // serialize to disk so account survives application restarts
        String record = username + FileManager.DELIMITER + password + FileManager.DELIMITER
                + role + FileManager.DELIMITER + id + FileManager.DELIMITER + email;
        FileManager.appendLine(CREDENTIALS_FILE, record);
        AppLogger.info("Persisted credentials for user: " + id + " (" + role + ")");
        return true;
    }

    // authenticates user by username, email, or id
    public Person authenticate(String identifier, String password) {
        if (identifier == null || password == null) {
            return null;
        }
        String key = identifier.trim().toLowerCase();
        Credential credential = credentials.get(key);

        // fallback lookup across datastore entities if not in credential map directly
        if (credential == null) {
            credential = findCredentialFromDataStore(key, password);
        }

        if (credential == null || !credential.password().equals(password)) {
            return null;
        }

        return switch (credential.role()) {
            case "ADMIN" -> dataStore.findAdminById(credential.id());
            case "STUDENT" -> {
                TeachingAssistant ta = dataStore.findTaById(credential.id());
                if (ta != null) {
                    yield ta;
                }
                yield dataStore.findStudentById(credential.id());
            }
            case "TA" -> dataStore.findTaById(credential.id());
            case "VISITING_INSTRUCTOR" -> dataStore.findVisitingById(credential.id());
            case "PERMANENT_INSTRUCTOR" -> dataStore.findPermanentById(credential.id());
            default -> null;
        };
    }

    // checks if any datastore person matches email or id with default credentials
    private Credential findCredentialFromDataStore(String key, String password) {
        // check teaching assistants first so promoted students get their TA role
        for (TeachingAssistant ta : dataStore.getTeachingAssistants()) {
            if (ta.getEmail().equalsIgnoreCase(key) || ta.getTaId().equalsIgnoreCase(key) || ta.getStudentId().equalsIgnoreCase(key)) {
                return new Credential(password, "TA", ta.getTaId(), ta.getEmail());
            }
        }
        // check students
        for (NormalStudent student : dataStore.getNormalStudents()) {
            if (student.getEmail().equalsIgnoreCase(key) || student.getStudentId().equalsIgnoreCase(key)) {
                return new Credential(password, "STUDENT", student.getStudentId(), student.getEmail());
            }
        }
        // check admins
        for (AcademicOfficeAdmin admin : dataStore.getAdmins()) {
            if (admin.getEmail().equalsIgnoreCase(key) || admin.getAdminId().equalsIgnoreCase(key)) {
                return new Credential(password, "ADMIN", admin.getAdminId(), admin.getEmail());
            }
        }
        // check visiting instructors
        for (VisitingInstructor vi : dataStore.getVisitingInstructors()) {
            if (vi.getEmail().equalsIgnoreCase(key) || vi.getTeacherId().equalsIgnoreCase(key)) {
                return new Credential(password, "VISITING_INSTRUCTOR", vi.getTeacherId(), vi.getEmail());
            }
        }
        // check permanent instructors
        for (PermanentInstructor pi : dataStore.getPermanentInstructors()) {
            if (pi.getEmail().equalsIgnoreCase(key) || pi.getTeacherId().equalsIgnoreCase(key)) {
                return new Credential(password, "PERMANENT_INSTRUCTOR", pi.getTeacherId(), pi.getEmail());
            }
        }
        return null;
    }

    // registers or updates credentials for a teaching assistant (maps taId, studentId, and email)
    public synchronized void registerTaCredentials(String taId, String studentId, String email, String password) {
        Credential cred = new Credential(password, "TA", taId, email);
        if (taId != null && !taId.isEmpty()) {
            credentials.put(taId.toLowerCase(), cred);
        }
        if (studentId != null && !studentId.isEmpty()) {
            credentials.put(studentId.toLowerCase(), cred);
        }
        if (email != null && !email.isEmpty()) {
            credentials.put(email.toLowerCase(), cred);
        }
        String record = taId.toLowerCase() + FileManager.DELIMITER + password + FileManager.DELIMITER
                + "TA" + FileManager.DELIMITER + taId + FileManager.DELIMITER + email;
        FileManager.appendLine(CREDENTIALS_FILE, record);
    }

    // verifies if email is already taken anywhere in system
    public boolean isEmailRegistered(String email) {
        if (email == null) return false;
        String e = email.trim().toLowerCase();
        for (NormalStudent s : dataStore.getNormalStudents()) {
            if (s.getEmail().equalsIgnoreCase(e)) return true;
        }
        for (AcademicOfficeAdmin a : dataStore.getAdmins()) {
            if (a.getEmail().equalsIgnoreCase(e)) return true;
        }
        for (VisitingInstructor vi : dataStore.getVisitingInstructors()) {
            if (vi.getEmail().equalsIgnoreCase(e)) return true;
        }
        for (PermanentInstructor pi : dataStore.getPermanentInstructors()) {
            if (pi.getEmail().equalsIgnoreCase(e)) return true;
        }
        for (TeachingAssistant ta : dataStore.getTeachingAssistants()) {
            if (ta.getEmail().equalsIgnoreCase(e)) return true;
        }
        return false;
    }

    // verifies if id is already taken anywhere in system
    public boolean isIdRegistered(String id) {
        if (id == null) return false;
        String key = id.trim().toLowerCase();
        if (credentials.containsKey(key)) return true;
        for (NormalStudent s : dataStore.getNormalStudents()) {
            if (s.getStudentId().equalsIgnoreCase(key)) return true;
        }
        for (AcademicOfficeAdmin a : dataStore.getAdmins()) {
            if (a.getAdminId().equalsIgnoreCase(key)) return true;
        }
        for (VisitingInstructor vi : dataStore.getVisitingInstructors()) {
            if (vi.getTeacherId().equalsIgnoreCase(key)) return true;
        }
        for (PermanentInstructor pi : dataStore.getPermanentInstructors()) {
            if (pi.getTeacherId().equalsIgnoreCase(key)) return true;
        }
        for (TeachingAssistant ta : dataStore.getTeachingAssistants()) {
            if (ta.getTaId().equalsIgnoreCase(key) || ta.getStudentId().equalsIgnoreCase(key)) return true;
        }
        return false;
    }

    // generates next sequential student id
    public String getNextStudentId() {
        int max = 0;
        for (NormalStudent s : dataStore.getNormalStudents()) {
            String sid = s.getStudentId();
            if (sid != null && sid.toUpperCase().startsWith("S")) {
                try {
                    int num = Integer.parseInt(sid.substring(1));
                    if (num > max) max = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("S%03d", max + 1);
    }

    // generates next sequential visiting instructor id
    public String getNextVisitingId() {
        int max = 0;
        for (VisitingInstructor vi : dataStore.getVisitingInstructors()) {
            String id = vi.getTeacherId();
            if (id != null && id.toUpperCase().startsWith("VI")) {
                try {
                    int num = Integer.parseInt(id.substring(2));
                    if (num > max) max = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("VI%03d", max + 1);
    }

    // generates next sequential permanent instructor id
    public String getNextPermanentId() {
        int max = 0;
        for (PermanentInstructor pi : dataStore.getPermanentInstructors()) {
            String id = pi.getTeacherId();
            if (id != null && id.toUpperCase().startsWith("PI")) {
                try {
                    int num = Integer.parseInt(id.substring(2));
                    if (num > max) max = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("PI%03d", max + 1);
    }

    // generates next sequential admin id
    public String getNextAdminId() {
        int max = 0;
        for (AcademicOfficeAdmin a : dataStore.getAdmins()) {
            String id = a.getAdminId();
            if (id != null && id.toUpperCase().startsWith("ADM")) {
                try {
                    int num = Integer.parseInt(id.substring(3));
                    if (num > max) max = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("ADM%03d", max + 1);
    }

    // generates next sequential teaching assistant id
    public String getNextTaId() {
        int max = 0;
        for (TeachingAssistant ta : dataStore.getTeachingAssistants()) {
            String id = ta.getTaId();
            if (id != null && id.toUpperCase().startsWith("TA")) {
                try {
                    int num = Integer.parseInt(id.substring(2));
                    if (num > max) max = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("TA%03d", max + 1);
    }
}