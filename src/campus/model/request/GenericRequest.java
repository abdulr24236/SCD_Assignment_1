package campus.model.request;

import campus.enums.RequestCategory;
import campus.model.person.Student;

/**
 * A student's request for anything that isn't a course clash — grading,
 * a classmate/professor issue, or something else (diagram 3.3).
 */
public class GenericRequest extends Request {
    private RequestCategory category;

    public GenericRequest(String requestId, String description, int priority,
                          RequestCategory category, Student submittedBy) {
        super(requestId, description, priority, submittedBy);
        this.category = category;
    }

    public RequestCategory getCategory() {
        return category;
    }

    @Override
    public String getDetails() {
        return "[" + category + "] " + getDescription();
    }
}