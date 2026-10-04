package campus.io;

import campus.model.academic.Course;
import campus.model.person.AcademicOfficeAdmin;
import campus.model.person.NormalStudent;
import campus.model.person.PermanentInstructor;
import campus.model.person.VisitingInstructor;

/**
 * Phase 1 repositories: entities with zero cross-references to other
 * entities. Course's prerequisites are intentionally skipped for now (a
 * scoping decision, not an oversight — see project notes on why). Each is a
 * one-line Repository<T> setup, per the pattern established in
 * Repository.java: no new class needed per entity, just a serializer and a
 * deserializer function.
 *
 * GenericRequest used to live here too, but once it gained a submittedBy
 * field (needed to answer "show my requests"), it needed NormalStudent
 * lookup context that a bare Function<String, T> deserializer can't
 * receive — so it moved to its own GenericRequestRepository, following the
 * same pattern as the Phase 2/3 custom repositories.
 *
 * Usage: Repositories.COURSES.save(course), or Repositories.COURSES.loadAll()
 * to restore everything on startup.
 */
public class Repositories {

    public static final Repository<Course> COURSES = new Repository<>(
            "courses.txt",
            course -> course.getCourseCode() + FileManager.DELIMITER + course.getTitle() + FileManager.DELIMITER + course.getCreditHours(),
            line -> {
                String[] f = line.split(FileManager.DELIMITER, -1);
                return new Course(f[0], f[1], Integer.parseInt(f[2]));
            }
    );

    public static final Repository<NormalStudent> NORMAL_STUDENTS = new Repository<>(
            "students.txt",
            student -> student.getStudentId() + FileManager.DELIMITER + student.getName() + FileManager.DELIMITER
                    + student.getEmail() + FileManager.DELIMITER + student.getPhone(),
            line -> {
                String[] f = line.split(FileManager.DELIMITER, -1);
                return new NormalStudent(f[1], f[2], f[3], f[0]);
            }
    );

    public static final Repository<VisitingInstructor> VISITING_INSTRUCTORS = new Repository<>(
            "visiting_instructors.txt",
            instructor -> instructor.getTeacherId() + FileManager.DELIMITER + instructor.getName() + FileManager.DELIMITER
                    + instructor.getEmail() + FileManager.DELIMITER + instructor.getPhone(),
            line -> {
                String[] f = line.split(FileManager.DELIMITER, -1);
                return new VisitingInstructor(f[1], f[2], f[3], f[0]);
            }
    );

    public static final Repository<PermanentInstructor> PERMANENT_INSTRUCTORS = new Repository<>(
            "permanent_instructors.txt",
            instructor -> instructor.getTeacherId() + FileManager.DELIMITER + instructor.getName() + FileManager.DELIMITER
                    + instructor.getEmail() + FileManager.DELIMITER + instructor.getPhone(),
            line -> {
                String[] f = line.split(FileManager.DELIMITER, -1);
                return new PermanentInstructor(f[1], f[2], f[3], f[0]);
            }
    );

    public static final Repository<AcademicOfficeAdmin> ACADEMIC_OFFICE_ADMINS = new Repository<>(
            "admins.txt",
            admin -> admin.getAdminId() + FileManager.DELIMITER + admin.getName() + FileManager.DELIMITER
                    + admin.getEmail() + FileManager.DELIMITER + admin.getPhone(),
            line -> {
                String[] f = line.split(FileManager.DELIMITER, -1);
                return new AcademicOfficeAdmin(f[1], f[2], f[3], f[0]);
            }
    );
}