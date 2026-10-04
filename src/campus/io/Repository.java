package campus.io;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Generic base for a text-file-backed repository. Handles all the actual
 * file I/O once; each concrete entity only has to supply two small
 * functions — how to turn one object into a line, and how to turn one line
 * back into an object.
 *
 * Example (replaces the hand-written StudentRepository entirely):
 *
 *   Repository<NormalStudent> studentRepo = new Repository<>(
 *       "students.txt",
 *       student -> student.getStudentId() + FileManager.DELIMITER + student.getName()
 *               + FileManager.DELIMITER + student.getEmail() + FileManager.DELIMITER + student.getPhone(),
 *       line -> {
 *           String[] f = line.split(FileManager.DELIMITER, -1);
 *           return new NormalStudent(f[1], f[2], f[3], f[0]);
 *       }
 *   );
 *   studentRepo.save(newStudent);
 *   List<NormalStudent> all = studentRepo.loadAll();
 *
 * Every other entity (Course, Request, Assignment, FYPGroup, ...) follows
 * the identical two-line setup — only the serializer/deserializer differs.
 */
public class Repository<T> {
    private final String fileName;
    private final Function<T, String> serializer;
    private final Function<String, T> deserializer;

    public Repository(String fileName, Function<T, String> serializer, Function<String, T> deserializer) {
        this.fileName = fileName;
        this.serializer = serializer;
        this.deserializer = deserializer;
    }

    public void save(T item) {
        FileManager.appendLine(fileName, serializer.apply(item));
    }

    public void saveAll(List<T> items) {
        List<String> lines = new ArrayList<>();
        for (T item : items) {
            lines.add(serializer.apply(item));
        }
        FileManager.writeLines(fileName, lines);
    }

    public List<T> loadAll() {
        List<T> items = new ArrayList<>();
        for (String line : FileManager.readLines(fileName)) {
            items.add(deserializer.apply(line));
        }
        return items;
    }
}