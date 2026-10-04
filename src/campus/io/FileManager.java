package campus.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic helper for reading and writing plain-text files under data/.
 * Every entity-specific repository (SectionRepository, EnrollmentRepository,
 * etc.) sits on top of this rather than each reimplementing file I/O.
 *
 * Files use a simple one-record-per-line format, fields joined by
 * DELIMITER. Originally this was a plain comma — but that breaks the
 * instant any free-text field (a title, a description, a comment) happens
 * to contain a comma itself, silently shifting every field after it out of
 * position. "::" is far less likely to appear in real user input and
 * doesn't need special regex escaping in split().
 */
public class FileManager {
    private static final String DATA_FOLDER = "data";
    public static final String DELIMITER = "::";

    /** Ensures data/ exists before any read/write attempt. */
    public static void ensureDataFolder() {
        Path folder = Paths.get(DATA_FOLDER);
        if (!Files.exists(folder)) {
            try {
                Files.createDirectories(folder);
            } catch (IOException e) {
                System.err.println("Could not create data folder: " + e.getMessage());
            }
        }
    }

    /** Reads every line of data/{fileName}. Returns an empty list if the file doesn't exist yet. */
    public static List<String> readLines(String fileName) {
        ensureDataFolder();
        List<String> lines = new ArrayList<>();
        Path filePath = Paths.get(DATA_FOLDER, fileName);
        if (!Files.exists(filePath)) {
            return lines;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Could not read " + fileName + ": " + e.getMessage());
        }
        return lines;
    }

    /** Overwrites data/{fileName} with the given lines (one record per line). */
    public static void writeLines(String fileName, List<String> lines) {
        ensureDataFolder();
        Path filePath = Paths.get(DATA_FOLDER, fileName);
        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            for (String line : lines) {
                writer.write(line);
                writer.write(System.lineSeparator());
            }
        } catch (IOException e) {
            System.err.println("Could not write " + fileName + ": " + e.getMessage());
        }
    }

    /** Appends a single line to data/{fileName} without rewriting the whole file. */
    public static void appendLine(String fileName, String line) {
        ensureDataFolder();
        Path filePath = Paths.get(DATA_FOLDER, fileName);
        try (FileWriter writer = new FileWriter(filePath.toFile(), true)) {
            writer.write(line);
            writer.write(System.lineSeparator());
        } catch (IOException e) {
            System.err.println("Could not append to " + fileName + ": " + e.getMessage());
        }
    }
}