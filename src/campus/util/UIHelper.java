/**
 * ============================================================
 * File        : UIHelper.java
 * Package     : campus.util
 * Author      : Campus Development Team
 * Course      : Software Construction & Development
 * Assignment  : Assignment 1 — Campus Management System
 * Description : CLI presentation utility for colors, banners, and table rendering
 *               Uses ASCII-safe box drawing for Windows console compatibility.
 * ============================================================
 */
package campus.util;

public class UIHelper {

    // ansi color and styling codes for terminal presentation
    public static final String RESET        = "\u001B[0m";
    public static final String BOLD         = "\u001B[1m";
    public static final String CYAN         = "\u001B[36m";
    public static final String BRIGHT_CYAN  = "\u001B[96m";
    public static final String GREEN        = "\u001B[32m";
    public static final String BRIGHT_GREEN = "\u001B[92m";
    public static final String YELLOW       = "\u001B[33m";
    public static final String BRIGHT_YELLOW= "\u001B[93m";
    public static final String RED          = "\u001B[31m";
    public static final String BRIGHT_RED   = "\u001B[91m";
    public static final String MAGENTA      = "\u001B[35m";
    public static final String BRIGHT_MAGENTA = "\u001B[95m";
    public static final String BLUE         = "\u001B[34m";
    public static final String WHITE        = "\u001B[37m";
    public static final String BRIGHT_WHITE = "\u001B[97m";
    public static final String GRAY         = "\u001B[90m";

    // strips ansi escape codes to calculate real visible length for border padding
    public static int visibleLength(String text) {
        if (text == null) return 0;
        return text.replaceAll("\u001B\\[[;\\d]*m", "").length();
    }

    // =========================================================
    // Banner & Section Titles  (ASCII-safe: + - | = only)
    // =========================================================

    // prints the main welcome banner using pure ASCII box chars
    public static void printMainBanner() {
        int w = 90;
        String top  = "+" + "=".repeat(w) + "+";
        String mid  = "+" + "-".repeat(w) + "+";
        System.out.println();
        System.out.println(CYAN + BOLD + top + RESET);
        printCenteredRow("FAST NATIONAL UNIVERSITY OF COMPUTER & EMERGING SCIENCES", w, BRIGHT_CYAN + BOLD);
        printCenteredRow("CAMPUS MANAGEMENT SYSTEM  -  ACADEMIC PORTAL", w, WHITE + BOLD);
        System.out.println(CYAN + BOLD + top + RESET);
        System.out.println();
    }

    // prints role header card for logged-in user
    public static void printUserHeader(String role, String name, String id) {
        int w = 90;
        String line = "-".repeat(w);
        System.out.println();
        System.out.println(BRIGHT_CYAN + "+" + line + "+" + RESET);
        String content = "  " + role.toUpperCase() + "  |  Logged in: " + name + "  (" + id + ")";
        int pad = w - visibleLength(content);
        if (pad < 0) pad = 0;
        System.out.println(BRIGHT_CYAN + "|" + BOLD + BRIGHT_WHITE + content + RESET
                + " ".repeat(pad) + BRIGHT_CYAN + "|" + RESET);
        System.out.println(BRIGHT_CYAN + "+" + line + "+" + RESET);
    }

    // prints single-bordered menu card with title and option lines
    public static void printMenuCard(String title, String[] options) {
        int w = 90;
        String hline = "-".repeat(w);
        System.out.println();
        System.out.println(CYAN + "+" + hline + "+" + RESET);

        // title row
        String titleContent = "  " + title.toUpperCase();
        int titlePad = w - visibleLength(titleContent);
        if (titlePad < 0) titlePad = 0;
        System.out.println(CYAN + "|" + BOLD + YELLOW + titleContent + RESET
                + " ".repeat(titlePad) + CYAN + "|" + RESET);

        System.out.println(CYAN + "+" + hline + "+" + RESET);

        // option rows
        for (String option : options) {
            String optContent = "  " + option;
            int optPad = w - visibleLength(optContent);
            if (optPad < 0) optPad = 0;
            System.out.println(CYAN + "|" + WHITE + optContent + RESET
                    + " ".repeat(optPad) + CYAN + "|" + RESET);
        }
        System.out.println(CYAN + "+" + hline + "+" + RESET);
    }

    // prints a sub-section header with separator lines
    public static void printSectionHeader(String title) {
        System.out.println();
        System.out.println(GRAY + "  " + "=".repeat(76) + RESET);
        System.out.println(BOLD + BRIGHT_CYAN + "  >> " + title.toUpperCase() + RESET);
        System.out.println(GRAY + "  " + "=".repeat(76) + RESET);
    }

    // helper: prints a text line centered inside a | bordered row of given width
    private static void printCenteredRow(String text, int w, String colorPrefix) {
        int visible = visibleLength(text);
        int totalPad = w - visible;
        if (totalPad < 0) totalPad = 0;
        int leftPad  = totalPad / 2;
        int rightPad = totalPad - leftPad;
        System.out.println(CYAN + BOLD + "|" + " ".repeat(leftPad)
                + colorPrefix + text + RESET
                + " ".repeat(rightPad) + CYAN + BOLD + "|" + RESET);
    }

    // =========================================================
    // Status Badges
    // =========================================================

    // prints green success badge with message
    public static void success(String message) {
        System.out.println("  " + BRIGHT_GREEN + BOLD + "[OK]" + RESET + "  " + message);
    }

    // prints yellow warning badge with message
    public static void warning(String message) {
        System.out.println("  " + BRIGHT_YELLOW + BOLD + "[!!]" + RESET + " " + message);
    }

    // prints red error badge with message
    public static void error(String message) {
        System.out.println("  " + BRIGHT_RED + BOLD + "[ERR]" + RESET + " " + message);
    }

    // prints cyan info badge with message
    public static void info(String message) {
        System.out.println("  " + BRIGHT_CYAN + "[INFO]" + RESET + " " + message);
    }

    // returns colored status badge string for inline use
    public static String statusBadge(String status) {
        if (status == null) return "[NONE]";
        String s = status.toUpperCase();
        return switch (s) {
            case "PENDING"                                 -> YELLOW        + "[PENDING]"   + RESET;
            case "APPROVED", "ACTIVE", "PRESENT",
                 "GRADED", "COMPLETED"                    -> GREEN         + "[" + s + "]" + RESET;
            case "REJECTED", "DROPPED", "ABSENT"          -> RED           + "[" + s + "]" + RESET;
            case "LATE"                                    -> BRIGHT_YELLOW + "[LATE]"      + RESET;
            case "SUBMITTED"                               -> BRIGHT_CYAN   + "[SUBMITTED]" + RESET;
            default                                        -> WHITE         + "[" + s + "]" + RESET;
        };
    }

    // builds a visual text progress bar for attendance or scoring display
    public static String progressBar(double percentage, int barLength) {
        if (percentage < 0)   percentage = 0;
        if (percentage > 100) percentage = 100;
        int filled = (int) Math.round((percentage / 100.0) * barLength);
        int empty  = barLength - filled;
        String color = percentage >= 80.0 ? BRIGHT_GREEN
                     : percentage >= 70.0 ? BRIGHT_YELLOW
                     : BRIGHT_RED;
        // use # for filled and . for empty (ASCII safe)
        String bar = "#".repeat(filled) + ".".repeat(empty);
        return color + "[" + bar + "] " + String.format("%.1f%%", percentage) + RESET;
    }

    // prints a simple horizontal divider line
    public static void divider() {
        System.out.println(GRAY + "  " + "-".repeat(80) + RESET);
    }
}
