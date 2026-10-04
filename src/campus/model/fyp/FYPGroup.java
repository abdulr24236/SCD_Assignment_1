package campus.model.fyp;

import campus.model.person.PermanentInstructor;
import campus.model.person.Student;
import java.util.ArrayList;
import java.util.List;

/**
 * A Final Year Project group: its members, its supervisor, and the trail of
 * meetings and evaluations that happen over the project's lifetime

 */
public class FYPGroup {
    private String groupId;
    private String title;
    private String description;
    private List<Student> members = new ArrayList<>();
    private PermanentInstructor supervisor;
    private List<FYPMeeting> meetings = new ArrayList<>();
    private List<FYPEvaluation> evaluations = new ArrayList<>();

    public FYPGroup(String groupId, String title, String description) {
        this.groupId = groupId;
        this.title = title;
        this.description = description;
    }

    public void addMember(Student student) {
        members.add(student);
    }

    public void removeMember(Student student) {
        members.remove(student);
    }

    public List<Student> getMembers() {
        return members;
    }

    public void assignSupervisor(PermanentInstructor supervisor) {
        this.supervisor = supervisor;
        supervisor.addFypGroup(this);
    }

    public void addMeeting(FYPMeeting meeting) {
        meetings.add(meeting);
    }

    public void addEvaluation(FYPEvaluation evaluation) {
        evaluations.add(evaluation);
    }

    public String getDetails() {
        return title + " - " + description;
    }

    // Not in the diagram's method list, but needed for serialization.
    public String getGroupId() {
        return groupId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public PermanentInstructor getSupervisor() {
        return supervisor;
    }
}