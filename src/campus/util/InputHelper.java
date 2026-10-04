/**
 * ============================================================
 * File        : InputHelper.java
 * Package     : campus.util
 * Author      : Campus Development Team
 * Course      : Software Construction & Development
 * Assignment  : Assignment 1 — Campus Management System
 * Description : Centralized input processor with validation and EOF safety.
 *               Each prompt prints its label on one line, then waits for input
 *               on the NEXT line (prefixed with "  >> ") so prompts never
 *               appear squished together or "before" the user can type.
 * ============================================================
 */
package campus.util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class InputHelper {

    private final Scanner sc;

    public InputHelper() {
        this.sc = new Scanner(System.in);
    }

    public InputHelper(Scanner sc) {
        this.sc = sc;
    }

    // =========================================================
    // Core read methods
    // =========================================================

    /**
     * Reads an integer in [min, max].
     * Prompt label is printed first; input cursor on the next line.
     * Re-prompts silently on empty; shows warning on wrong range / non-numeric.
     */
    public int readInt(String label, int min, int max) {
        while (true) {
            // print the label, then move to the next line for input
            System.out.println();
            System.out.println(UIHelper.BOLD + "  " + label + UIHelper.RESET);
            System.out.print(UIHelper.CYAN + "  >> " + UIHelper.RESET);

            if (!sc.hasNextLine()) {
                System.out.println("\n  Input stream closed. Exiting.");
                System.exit(0);
            }
            String raw = sc.nextLine().trim();
            if (raw.isEmpty()) {
                // silently re-prompt — do NOT show "invalid" for empty
                continue;
            }
            try {
                int value = Integer.parseInt(raw);
                if (value < min || value > max) {
                    UIHelper.warning("Please enter a number between " + min + " and " + max + ".");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                // only warn when user actually typed something wrong
                UIHelper.warning("\"" + raw + "\" is not a valid number.");
            }
        }
    }

    /**
     * Reads a non-empty trimmed string.
     * Prompt label on its own line; "  >> " on the next line.
     * Silently re-prompts on empty — never shows a warning for blank input.
     */
    public String readLine(String label) {
        while (true) {
            System.out.println();
            System.out.println(UIHelper.BOLD + "  " + label + UIHelper.RESET);
            System.out.print(UIHelper.CYAN + "  >> " + UIHelper.RESET);

            if (!sc.hasNextLine()) {
                System.out.println("\n  Input stream closed. Exiting.");
                System.exit(0);
            }
            String raw = sc.nextLine().trim();
            if (!raw.isEmpty()) {
                return raw;
            }
            // empty — loop back silently
        }
    }

    /**
     * Reads a string with an optional default value shown in brackets.
     * If the user presses Enter without typing, the default is returned.
     * No warning is ever shown — blank input simply accepts the default.
     */
    public String readLine(String label, String defaultValue) {
        String suffix = (defaultValue == null || defaultValue.isEmpty())
                ? "" : "  [default: " + defaultValue + "]";
        System.out.println();
        System.out.println(UIHelper.BOLD + "  " + label + suffix + UIHelper.RESET);
        System.out.print(UIHelper.CYAN + "  >> " + UIHelper.RESET);

        if (!sc.hasNextLine()) {
            System.out.println("\n  Input stream closed. Exiting.");
            System.exit(0);
        }
        String raw = sc.nextLine().trim();
        return raw.isEmpty() ? defaultValue : raw;
    }

    /**
     * Reads a password (non-empty, minimum 4 characters).
     * Prompt is labeled as-is; re-prompts with a warning if too short.
     */
    public String readPassword(String label) {
        while (true) {
            String pass = readLine(label);
            if (pass.length() >= 4) {
                return pass;
            }
            UIHelper.warning("Password must be at least 4 characters.");
        }
    }

    /**
     * Reads a code string (uppercased automatically).
     */
    public String readCode(String label) {
        return readLine(label).toUpperCase();
    }

    /**
     * Reads and validates an email address (must contain '@' and '.').
     */
    public String readEmail(String label) {
        while (true) {
            String email = readLine(label).toLowerCase();
            if (email.contains("@") && email.contains(".")) {
                return email;
            }
            UIHelper.warning("Invalid email — must contain '@' and a domain (e.g. user@fast.edu).");
        }
    }

    /**
     * Reads a date in YYYY-MM-DD format.
     */
    public LocalDate readDate(String label) {
        while (true) {
            String raw = readLine(label + " (YYYY-MM-DD)");
            try {
                return LocalDate.parse(raw);
            } catch (DateTimeParseException e) {
                UIHelper.warning("Invalid date — use YYYY-MM-DD (e.g. 2026-10-15).");
            }
        }
    }

    /**
     * Reads a floating-point value in [min, max].
     */
    public double readDouble(String label, double min, double max) {
        while (true) {
            System.out.println();
            System.out.println(UIHelper.BOLD + "  " + label + UIHelper.RESET);
            System.out.print(UIHelper.CYAN + "  >> " + UIHelper.RESET);

            if (!sc.hasNextLine()) {
                System.out.println("\n  Input stream closed. Exiting.");
                System.exit(0);
            }
            String raw = sc.nextLine().trim();
            if (raw.isEmpty()) {
                continue;
            }
            try {
                double val = Double.parseDouble(raw);
                if (val < min || val > max) {
                    UIHelper.warning("Please enter a value between " + min + " and " + max + ".");
                    continue;
                }
                return val;
            } catch (NumberFormatException e) {
                UIHelper.warning("\"" + raw + "\" is not a valid number.");
            }
        }
    }

    /**
     * Reads a yes/no confirmation. Returns true for 'y'/'yes', false for 'n'/'no'.
     */
    public boolean readYesNo(String label) {
        while (true) {
            System.out.println();
            System.out.println(UIHelper.BOLD + "  " + label + " (y/n)" + UIHelper.RESET);
            System.out.print(UIHelper.CYAN + "  >> " + UIHelper.RESET);

            if (!sc.hasNextLine()) {
                System.out.println("\n  Input stream closed. Exiting.");
                System.exit(0);
            }
            String raw = sc.nextLine().trim().toLowerCase();
            if (raw.equals("y") || raw.equals("yes")) return true;
            if (raw.equals("n") || raw.equals("no"))  return false;
            UIHelper.warning("Please type 'y' or 'n'.");
        }
    }

    // =========================================================
    // Cancellable variants  (type 0 at any prompt to abort)
    // =========================================================

    /**
     * Prints a small tip reminding the user they can type 0 to cancel.
     * Call this once at the top of every registration / creation form.
     */
    public static void printCancelHint() {
        System.out.println(UIHelper.GRAY
                + "  (type 0 at any prompt to cancel and go back)"
                + UIHelper.RESET);
    }

    /**
     * Like readLine but returns null when the user types exactly "0".
     * Silently re-prompts on empty.
     */
    public String readCancellable(String label) {
        while (true) {
            System.out.println();
            System.out.println(UIHelper.BOLD + "  " + label + UIHelper.RESET);
            System.out.print(UIHelper.CYAN + "  >> " + UIHelper.RESET);
            if (!sc.hasNextLine()) { System.out.println("\n  Input stream closed. Exiting."); System.exit(0); }
            String raw = sc.nextLine().trim();
            if (raw.equals("0")) {
                UIHelper.info("Registration cancelled — returning to main menu.");
                return null;   // sentinel: caller must check for null
            }
            if (!raw.isEmpty()) return raw;
            // empty — loop silently
        }
    }

    /**
     * Like readLine(label, default) but returns null when the user types "0".
     * Blank input still accepts the default.
     */
    public String readCancellable(String label, String defaultValue) {
        String suffix = (defaultValue == null || defaultValue.isEmpty())
                ? "" : "  [default: " + defaultValue + "]";
        System.out.println();
        System.out.println(UIHelper.BOLD + "  " + label + suffix + UIHelper.RESET);
        System.out.print(UIHelper.CYAN + "  >> " + UIHelper.RESET);
        if (!sc.hasNextLine()) { System.out.println("\n  Input stream closed. Exiting."); System.exit(0); }
        String raw = sc.nextLine().trim();
        if (raw.equals("0")) {
            UIHelper.info("Registration cancelled — returning to main menu.");
            return null;
        }
        return raw.isEmpty() ? defaultValue : raw;
    }

    /**
     * Like readEmail but returns null when the user types "0".
     */
    public String readEmailCancellable(String label) {
        while (true) {
            String input = readCancellable(label);
            if (input == null) return null;          // user typed 0
            String email = input.toLowerCase();
            if (email.contains("@") && email.contains(".")) return email;
            UIHelper.warning("Invalid email — must contain '@' and a domain (e.g. user@fast.edu).");
        }
    }

    /**
     * Like readPassword but returns null when the user types "0".
     */
    public String readPasswordCancellable(String label) {
        while (true) {
            String pass = readCancellable(label);
            if (pass == null) return null;           // user typed 0
            if (pass.length() >= 4) return pass;
            UIHelper.warning("Password must be at least 4 characters.");
        }
    }

    // =========================================================
    // Utility
    // =========================================================

    /** Returns the underlying Scanner for code that still needs direct access. */
    public Scanner getScanner() {
        return sc;
    }

    /** Closes the underlying Scanner. */
    public void close() {
        sc.close();
    }
}
