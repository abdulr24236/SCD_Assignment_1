package campus.io;

import campus.enums.RequestCategory;
import campus.enums.RequestStatus;
import campus.model.person.NormalStudent;
import campus.model.request.GenericRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists GenericRequest objects. Needs NormalStudents already loaded
 * first, since each request references the student who submitted it by ID.
 *
 * Line format: requestId::description::priority::category::status::requestDate::submittedById
 */
public class GenericRequestRepository {
    private static final String FILE_NAME = "generic_requests.txt";

    public static void save(GenericRequest request) {
        FileManager.appendLine(FILE_NAME, toLine(request));
    }

    public static void saveAll(List<GenericRequest> requests) {
        List<String> lines = new ArrayList<>();
        for (GenericRequest request : requests) {
            lines.add(toLine(request));
        }
        FileManager.writeLines(FILE_NAME, lines);
    }

    public static List<GenericRequest> loadAll(List<NormalStudent> normalStudents) {
        List<GenericRequest> requests = new ArrayList<>();
        for (String line : FileManager.readLines(FILE_NAME)) {
            requests.add(fromLine(line, normalStudents));
        }
        return requests;
    }

    private static String toLine(GenericRequest request) {
        String status = request.getStatus() == null ? "" : request.getStatus().name();
        String date = request.getRequestDate() == null ? "" : request.getRequestDate().toString();
        String submittedById = request.getSubmittedBy() == null ? "" : request.getSubmittedBy().getStudentId();
        return request.getRequestId() + FileManager.DELIMITER + request.getDescription() + FileManager.DELIMITER + request.getPriority()
                + FileManager.DELIMITER + request.getCategory() + FileManager.DELIMITER + status + FileManager.DELIMITER + date + FileManager.DELIMITER + submittedById;
    }

    private static GenericRequest fromLine(String line, List<NormalStudent> normalStudents) {
        String[] f = line.split(FileManager.DELIMITER, -1);
        String requestId = f[0];
        String description = f[1];
        int priority = Integer.parseInt(f[2]);
        RequestCategory category = RequestCategory.valueOf(f[3]);
        String status = f[4];
        String date = f[5];
        String submittedById = f.length > 6 ? f[6] : "";

        NormalStudent submittedBy = findStudentById(normalStudents, submittedById);
        GenericRequest request = new GenericRequest(requestId, description, priority, category, submittedBy);
        if (!status.isEmpty()) {
            request.setStatus(RequestStatus.valueOf(status));
        }
        if (!date.isEmpty()) {
            request.setRequestDate(LocalDate.parse(date));
        }
        return request;
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