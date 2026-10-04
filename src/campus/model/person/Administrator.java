package campus.model.person;

/**
 * Base for admin-side actors. Currently AcademicOfficeAdmin is the only
 * concrete subclass, but keeping this abstract matches the diagram and
 * leaves room for other admin roles later without touching Person.
 */
public abstract class Administrator extends Person {
    private String adminId;

    public Administrator(String name, String email, String phone, String adminId) {
        super(name, email, phone);
        this.adminId = adminId;
    }

    public String getAdminId() {
        return adminId;
    }

    @Override
    public String getRole() {
        return "Administrator";
    }
}