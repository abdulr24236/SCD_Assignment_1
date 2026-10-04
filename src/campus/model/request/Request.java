package campus.model.request;

import campus.enums.RequestStatus;
import campus.model.person.Student;
import java.time.LocalDate;

/**
 * Base for an academic request a student can file — a course clash or a
 * more general concern (diagram 3.3).
 */
public abstract class Request {
    private String requestId;
    private LocalDate requestDate;
    private String description;
    private RequestStatus status;
    private int priority;
    // Records who filed a request — accommodates both NormalStudent and TeachingAssistant
    private Student submittedBy;

    protected Request(String requestId, String description, int priority, Student submittedBy) {
        this.requestId = requestId;
        this.description = description;
        this.priority = priority;
        this.submittedBy = submittedBy;
    }

    public Student getSubmittedBy() {
        return submittedBy;
    }

    /** Marks this request as filed: timestamps it and sets status to PENDING. */
    public void submit() {
        this.requestDate = LocalDate.now();
        this.status = RequestStatus.PENDING;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    // Not in the diagram's method list. submit() always stamps today's date,
    // which is correct for a brand-new request but wrong when reconstructing
    // an old request from a saved file — this lets a repository restore the
    // request's real original date instead.
    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }

    public int getPriority() {
        return priority;
    }

    // Not in the diagram's method list, but a repository has no way to
    // serialize a request at all without being able to read back its ID.
    public String getRequestId() {
        return requestId;
    }

    // Not in the diagram's method list for Request, but both
    // CourseClashRequest and GenericRequest need the description text to
    // build their getDetails() output.
    public String getDescription() {
        return description;
    }

    // Not in the diagram's method list, but RequestDateComparator needs to
    // actually read this to sort by it.
    public LocalDate getRequestDate() {
        return requestDate;
    }

    public abstract String getDetails();
}