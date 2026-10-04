/**
 * ============================================================
 * File        : InputHelper.java
 * Package     : util
 * Author      : Campus Development Team
 * Course      : Software Construction & Development
 * Assignment  : Assignment 1 — Campus Management System
 * Description : Root util package alias for campus.util.InputHelper.
 *               Kept in sync with campus.util.InputHelper — same prompt style.
 * ============================================================
 */
package util;

import campus.util.UIHelper;
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

    // reads an integer in [min, max]; prompt on own line, cursor on next line
    public int readInt(String label, int min, int max) {
        while (true) {
            System.out.println();
            System.out.println(UIHelper.BOLD + "  " + label + UIHelper.RESET);
            System.out.print(UIHelper.CYAN + "  >> " + UIHelper.RESET);
            if (!sc.hasNextLine()) { System.out.println("\n  Input stream closed."); System.exit(0); }
            String raw = sc.nextLine().trim();
            if (raw.isEmpty()) continue;
            try {
                int value = Integer.parseInt(raw);
                if (value < min || value > max) {
                    UIHelper.warning("Please enter a number between " + min + " and " + max + ".");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                UIHelper.warning("\"" + raw + "\" is not a valid number.");
            }
        }
    }

    // reads non-empty string; prompt on own line, cursor on next line
    public String readLine(String label) {
        while (true) {
            System.out.println();
            System.out.println(UIHelper.BOLD + "  " + label + UIHelper.RESET);
            System.out.print(UIHelper.CYAN + "  >> " + UIHelper.RESET);
            if (!sc.hasNextLine()) { System.out.println("\n  Input stream closed."); System.exit(0); }
            String raw = sc.nextLine().trim();
            if (!raw.isEmpty()) return raw;
        }
    }

    // reads string with optional default; blank input returns default, no warning
    public String readLine(String label, String defaultValue) {
        String suffix = (defaultValue == null || defaultValue.isEmpty())
                ? "" : "  [default: " + defaultValue + "]";
        System.out.println();
        System.out.println(UIHelper.BOLD + "  " + label + suffix + UIHelper.RESET);
        System.out.print(UIHelper.CYAN + "  >> " + UIHelper.RESET);
        if (!sc.hasNextLine()) { System.out.println("\n  Input stream closed."); System.exit(0); }
        String raw = sc.nextLine().trim();
        return raw.isEmpty() ? defaultValue : raw;
    }

    // reads password with minimum 4-character validation
    public String readPassword(String label) {
        while (true) {
            String pass = readLine(label);
            if (pass.length() >= 4) return pass;
            UIHelper.warning("Password must be at least 4 characters.");
        }
    }

    // reads uppercased code string
    public String readCode(String label) {
        return readLine(label).toUpperCase();
    }

    // reads and validates email address containing '@' and '.'
    public String readEmail(String label) {
        while (true) {
            String email = readLine(label).toLowerCase();
            if (email.contains("@") && email.contains(".")) return email;
            UIHelper.warning("Invalid email — must contain '@' and a domain (e.g. user@fast.edu).");
        }
    }

    // reads date in YYYY-MM-DD format
    public LocalDate readDate(String label) {
        while (true) {
            String raw = readLine(label + " (YYYY-MM-DD)");
            try { return LocalDate.parse(raw); }
            catch (DateTimeParseException e) {
                UIHelper.warning("Invalid date — use YYYY-MM-DD (e.g. 2026-10-15).");
            }
        }
    }

    // reads floating-point value in [min, max]; prompt on own line
    public double readDouble(String label, double min, double max) {
        while (true) {
            System.out.println();
            System.out.println(UIHelper.BOLD + "  " + label + UIHelper.RESET);
            System.out.print(UIHelper.CYAN + "  >> " + UIHelper.RESET);
            if (!sc.hasNextLine()) { System.out.println("\n  Input stream closed."); System.exit(0); }
            String raw = sc.nextLine().trim();
            if (raw.isEmpty()) continue;
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

    // reads yes/no boolean confirmation
    public boolean readYesNo(String label) {
        while (true) {
            System.out.println();
            System.out.println(UIHelper.BOLD + "  " + label + " (y/n)" + UIHelper.RESET);
            System.out.print(UIHelper.CYAN + "  >> " + UIHelper.RESET);
            if (!sc.hasNextLine()) { System.out.println("\n  Input stream closed."); System.exit(0); }
            String raw = sc.nextLine().trim().toLowerCase();
            if (raw.equals("y") || raw.equals("yes")) return true;
            if (raw.equals("n") || raw.equals("no"))  return false;
            UIHelper.warning("Please type 'y' or 'n'.");
        }
    }

    public Scanner getScanner() { return sc; }

    public void close() { sc.close(); }
}
