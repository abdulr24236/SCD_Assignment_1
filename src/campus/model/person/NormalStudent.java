package campus.model.person;

/**
 * A regular enrolled student. All the substantive behavior lives in Student —
 * this class just fixes the role label (diagram 3.1).
 */
public class NormalStudent extends Student {

    public NormalStudent(String name, String email, String phone, String studentId) {
        super(name, email, phone, studentId);
    }

    @Override
    public String getRole() {
        return "Normal Student";
    }
}