package campus.io;

import campus.enums.RequestStatus;
import campus.model.academic.Section;
import campus.model.person.NormalStudent;
import campus.model.request.CourseClashRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists CourseClashRequest objects. Needs Sections and NormalStudents
 * already loaded first — each request references two Sections (the
 * conflicting one and the requested one) and the student who submitted it,
 * all by ID.
 *
 * Line format: requestId::description::priority::conflictingSectionId::requestedSectionId::status::requestDate::submittedById
 */
public class CourseClashRequestRepository {
    private static final String FILE_NAME = "course_clash_requests.txt";

    public static void save(CourseClashRequest request) {
        FileManager.appendLine(FILE_NAME, toLine(request));
    }

    public static void saveAll(List<CourseClashRequest> requests) {
        List<String> lines = new ArrayList<>();
        for (CourseClashRequest request : requests) {
            lines.add(toLine(request));
        }
        FileManager.writeLines(FILE_NAME, lines);
    }

    public static List<CourseClashRequest> loadAll(List<Section> sections, List<NormalStudent> normalStudents) {
        List<CourseClashRequest> requests = new ArrayList<>();
        for (String line : FileManager.readLines(FILE_NAME)) {
            requests.add(fromLine(line, sections, normalStudents));
        }
        return requests;
    }

    private static String toLine(CourseClashRequest request) {
        String status = request.getStatus() == null ? "" : request.getStatus().name();
        String date = request.getRequestDate() == null ? "" : request.getRequestDate().toString();
        String submittedById = request.getSubmittedBy() == null ? "" : request.getSubmittedBy().getStudentId();
        return request.getRequestId() + FileManager.DELIMITER + request.getDescription() + FileManager.DELIMITER + request.getPriority()
                + FileManager.DELIMITER + request.getConflictingSection().getSectionId()
                + FileManager.DELIMITER + request.getRequestedSection().getSectionId()
                + FileManager.DELIMITER + status + FileManager.DELIMITER + date + FileManager.DELIMITER + submittedById;
    }

    private static CourseClashRequest fromLine(String line, List<Section> sections, List<NormalStudent> normalStudents) {
        String[] f = line.split(FileManager.DELIMITER, -1);
        String requestId = f[0];
        String description = f[1];
        int priority = Integer.parseInt(f[2]);
        String conflictingSectionId = f[3];
        String requestedSectionId = f[4];
        String status = f[5];
        String date = f[6];
        String submittedById = f.length > 7 ? f[7] : "";

        Section conflictingSection = findSectionById(sections, conflictingSectionId);
        Section requestedSection = findSectionById(sections, requestedSectionId);
        NormalStudent submittedBy = findStudentById(normalStudents, submittedById);

        CourseClashRequest request = new CourseClashRequest(
                requestId, description, priority, conflictingSection, requestedSection, submittedBy);
        if (!status.isEmpty()) {
            request.setStatus(RequestStatus.valueOf(status));
        }
        if (!date.isEmpty()) {
            request.setRequestDate(LocalDate.parse(date));
        }
        return request;
    }

    private static Section findSectionById(List<Section> sections, String sectionId) {
        for (Section section : sections) {
            if (section.getSectionId().equals(sectionId)) {
                return section;
            }
        }
        return null;
    }

    private static NormalStudent findStudentById(List<NormalStudent> students, String studentId) {
        for (NormalStudent student : students) {
            if (student.getStudentId().equals(studentId)) {
                return student;
            }
        }
        return null;
    }
}