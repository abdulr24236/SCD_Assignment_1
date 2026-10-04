package campus.model.request;

import campus.model.academic.Section;
import campus.model.person.Student;

/**
 * A student's request flagging that a requested section clashes with one
 * they're already enrolled in (diagram 3.3).
 */
public class CourseClashRequest extends Request {
    private Section conflictingSection;
    private Section requestedSection;

    public CourseClashRequest(String requestId, String description, int priority,
                              Section conflictingSection, Section requestedSection, Student submittedBy) {
        super(requestId, description, priority, submittedBy);
        this.conflictingSection = conflictingSection;
        this.requestedSection = requestedSection;
    }

    public String getConflictDetails() {
        return "Requested section for " + requestedSection.getCourse().getTitle()
                + " clashes with existing section for " + conflictingSection.getCourse().getTitle();
    }

    // Not in the diagram's method list (only getConflictDetails() combines
    // these into a sentence), but a repository needs the actual Section
    // objects — specifically their IDs — to serialize/deserialize this
    // request, not a human-readable string.
    public Section getConflictingSection() {
        return conflictingSection;
    }

    public Section getRequestedSection() {
        return requestedSection;
    }

    @Override
    public String getDetails() {
        return getConflictDetails();
    }
}