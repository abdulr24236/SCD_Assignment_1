package campus.model.person;

import campus.enums.RequestStatus;
import campus.model.academic.Course;
import campus.model.academic.Schedule;
import campus.model.academic.Section;
import campus.model.request.Request;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the academic structure of the university: courses, sections,
 * instructor/room assignment, and academic request handling

 */
public class AcademicOfficeAdmin extends Administrator {
    private List<Course> courses = new ArrayList<>();
    private List<Request> requests = new ArrayList<>();

    public AcademicOfficeAdmin(String name, String email, String phone, String adminId) {
        super(name, email, phone, adminId);
    }

    public void createCourse(Course course) {
        courses.add(course);
    }

    public void updateCourse(Course course) {
        // Genuinely open: Course's diagram methods are all getters
        // (getCourseCode/getTitle/getCreditHours) with no setters, so there's
        // nothing to actually mutate yet. Would need setTitle()/setCreditHours()
        // added to Course first — flagging rather than guessing at that.
    }

    public Course searchCourse(String courseCode) {
        for (Course course : courses) {
            if (course.getCourseCode().equals(courseCode)) {
                return course;
            }
        }
        return null;
    }

    public void createSection(Section section) {
        section.getCourse().addSection(section);
    }

    public void updateSection(Section section) {
        // Genuinely open: Section has no setters for sectionId (and shouldn't —
        // an ID shouldn't change after creation). setCapacity() and assignRoom()
        // below already cover the two things that legitimately change on an
        // existing section. Leaving this as a no-op unless there's a field I'm
        // missing that should be editable here.
    }

    public void setCapacity(Section section, int capacity) {
        section.setCapacity(capacity);
    }

    public void assignRoom(Section section, Schedule schedule) {
        section.setSchedule(schedule);
    }

    public void assignInstructor(Section section, Instructor instructor) {
        section.assignInstructor(instructor);
    }

    public List<Request> viewRequests() {
        return requests;
    }

    public void approveRequest(Request request) {
        request.setStatus(RequestStatus.APPROVED);
    }

    public void rejectRequest(Request request) {
        request.setStatus(RequestStatus.REJECTED);
    }
}