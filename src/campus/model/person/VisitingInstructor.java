package campus.model.person;

/**
 * Teaches assigned sections and manages attendance, but — unlike
 * PermanentInstructor — cannot assign a TA or supervise FYP groups
 * (diagram 3.1 / user flow for Faizan).
 */
public class VisitingInstructor extends Instructor {

    public VisitingInstructor(String name, String email, String phone, String teacherId) {
        super(name, email, phone, teacherId);
    }

    @Override
    public String getRole() {
        return "Visiting Instructor";
    }
}