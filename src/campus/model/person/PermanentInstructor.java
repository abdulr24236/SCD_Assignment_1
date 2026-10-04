package campus.model.person;

import campus.model.academic.Section;
import campus.model.fyp.FYPEvaluation;
import campus.model.fyp.FYPGroup;
import campus.model.fyp.FYPMeeting;
import java.util.ArrayList;
import java.util.List;

/**
 * Teaches sections like any Instructor, but additionally can assign a
 * NormalStudent as a TA and supervise Final Year Project groups
 * (diagram 3.1 / user flow for Arslan Asif).
 */
public class PermanentInstructor extends Instructor implements Evaluator {
    private List<FYPGroup> fypGroups = new ArrayList<>();

    public PermanentInstructor(String name, String email, String phone, String teacherId) {
        super(name, email, phone, teacherId);
    }

    /**
     * Promotes a NormalStudent to TeachingAssistant for the given section.
     * Per the class diagram, TeachingAssistant is a separate Student
     * subclass rather than a role flag, so we construct a new
     * TeachingAssistant carrying over the NormalStudent's identity fields.
     */
    public TeachingAssistant assignTA(NormalStudent student, Section section) {
        TeachingAssistant ta = new TeachingAssistant(
                student.getName(), student.getEmail(), student.getPhone(),
                student.getStudentId(), section);
        for (campus.model.academic.Enrollment e : student.getEnrollments()) {
            ta.addEnrollment(e);
        }
        section.assignTA(ta);
        return ta;
    }

    public void assignExistingTA(TeachingAssistant ta, Section section) {
        ta.setAssignedSection(section);
        section.assignTA(ta);
    }

    public List<FYPGroup> viewFYPGroups() {
        return fypGroups;
    }

    // Not in the diagram's method list — called by FYPGroup.assignSupervisor()
    // so viewFYPGroups() actually has something to return.
    public void addFypGroup(FYPGroup group) {
        fypGroups.add(group);
    }

    public String viewFYPGroupDetails(FYPGroup group) {
        return group.getDetails();
    }

    public void scheduleFYPMeeting(FYPGroup group, FYPMeeting meeting) {
        group.addMeeting(meeting);
    }

    public void evaluateFYPIdea(FYPGroup group, FYPEvaluation evaluation) {
        group.addEvaluation(evaluation);
    }

    public void provideFYPFeedback(FYPEvaluation evaluation, String feedback) {
        evaluation.addFeedback(feedback);
    }

    /**
     * Same tension as TeachingAssistant.evaluate(): Evaluator's contract
     * takes no arguments, but the real work (evaluateFYPIdea) needs a group
     * and an evaluation to act on. Left intentionally empty — actual
     * evaluation happens through evaluateFYPIdea()/provideFYPFeedback() above.
     */
    @Override
    public void evaluate() {
    }

    @Override
    public String getRole() {
        return "Permanent Instructor";
    }
}