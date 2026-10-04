package campus.logging;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Writes timestamped application events to logs/app.log, satisfying the
 * assignment's logging requirement (instruction #8: registrations, course
 * operations, assignment operations, request processing, errors, etc.).
 *
 * Kept as static methods rather than an instantiated object since logging
 * is a cross-cutting concern every part of the app needs — the same
 * reasoning as System.out, just aimed at a file instead of the console.
 */
public class AppLogger {
    private static final String LOG_FOLDER = "logs";
    private static final String LOG_FILE = "app.log";
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static void ensureLogFolder() {
        Path folder = Paths.get(LOG_FOLDER);
        if (!Files.exists(folder)) {
            try {
                Files.createDirectories(folder);
            } catch (IOException e) {
                System.err.println("Could not create logs folder: " + e.getMessage());
            }
        }
    }

    /** Logs a general informational event, e.g. "Student S001 registered for CS101-A". */
    public static void info(String message) {
        write("INFO", message);
    }

    /** Logs an error or exception event, e.g. a caught CourseFullException's message. */
    public static void error(String message) {
        write("ERROR", message);
    }

    private static void write(String level, String message) {
        ensureLogFolder();
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String line = "[" + timestamp + "] [" + level + "] " + message;
        for (String file : new String[]{"app.log", "application.log"}) {
            Path logPath = Paths.get(LOG_FOLDER, file);
            try (FileWriter writer = new FileWriter(logPath.toFile(), true)) {
                writer.write(line);
                writer.write(System.lineSeparator());
            } catch (IOException e) {
                System.err.println("Could not write to log file: " + e.getMessage());
            }
        }
    }
}