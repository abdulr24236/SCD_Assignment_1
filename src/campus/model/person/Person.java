package campus.model.person;

/**
 * Root of the actor hierarchy (diagram 3.1). Every role in the system —
 * students, instructors, and admins — is ultimately a Person.
 */
public abstract class Person {
    private String name;
    private String email;
    private String phone;

    public Person(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    /** Each concrete role reports its own label (e.g. "Normal Student", "Permanent Instructor"). */
    public abstract String getRole();
}