package campus.io;

import campus.model.person.Evaluator;
import campus.model.person.NormalStudent;
import campus.model.person.PermanentInstructor;
import campus.model.person.Student;
import campus.model.person.TeachingAssistant;
import java.util.List;

/**
 * Several fields across the model are typed as an abstract class or
 * interface — Student (could be a NormalStudent or a TeachingAssistant) or
 * Evaluator (could be a TeachingAssistant or a PermanentInstructor). A text
 * file can't store "which concrete type", so every repository that touches
 * one of these fields needs to tag the type alongside the ID, and search
 * the right list back on reload. Written once here instead of duplicating
 * this search across EnrollmentRepository, AttendanceRepository,
 * SubmissionRepository, FeedbackRepository, and FYPGroupRepository.
 *
 * Tag format: "N:S001" (NormalStudent), "TA:TA001" (TeachingAssistant, used
 * for both Student and Evaluator contexts since it's both), "P:P001"
 * (PermanentInstructor, Evaluator context only), or "NONE:" if the
 * reference was null.
 */
public class PersonLookup {

    public static String tagStudent(Student student) {
        if (student instanceof TeachingAssistant ta) {
            return "TA:" + ta.getTaId();
        } else if (student instanceof NormalStudent ns) {
            return "N:" + ns.getStudentId();
        }
        return "NONE:";
    }

    public static Student findStudent(String tag, List<NormalStudent> normalStudents,
                                      List<TeachingAssistant> teachingAssistants) {
        String[] parts = tag.split(":", 2);
        String type = parts[0];
        String id = parts.length > 1 ? parts[1] : "";
        if (type.equals("TA")) {
            for (TeachingAssistant ta : teachingAssistants) {
                if (ta.getTaId().equals(id)) {
                    return ta;
                }
            }
        } else if (type.equals("N")) {
            for (NormalStudent ns : normalStudents) {
                if (ns.getStudentId().equals(id)) {
                    return ns;
                }
            }
        }
        return null;
    }

    public static String tagEvaluator(Evaluator evaluator) {
        if (evaluator instanceof TeachingAssistant ta) {
            return "TA:" + ta.getTaId();
        } else if (evaluator instanceof PermanentInstructor pi) {
            return "P:" + pi.getTeacherId();
        }
        return "NONE:";
    }

    public static Evaluator findEvaluator(String tag, List<TeachingAssistant> teachingAssistants,
                                          List<PermanentInstructor> permanentInstructors) {
        String[] parts = tag.split(":", 2);
        String type = parts[0];
        String id = parts.length > 1 ? parts[1] : "";
        if (type.equals("TA")) {
            for (TeachingAssistant ta : teachingAssistants) {
                if (ta.getTaId().equals(id)) {
                    return ta;
                }
            }
        } else if (type.equals("P")) {
            for (PermanentInstructor pi : permanentInstructors) {
                if (pi.getTeacherId().equals(id)) {
                    return pi;
                }
            }
        }
        return null;
    }
}