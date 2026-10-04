package campus.model.academic;

import campus.enums.Day;
import java.time.LocalTime;

/**
 * A weekly recurring time slot for a section: which day, what time range,
 * and which room .
 */
public class Schedule {
    private Day day;
    private LocalTime startTime;
    private LocalTime endTime;
    private String room;

    public Schedule(Day day, LocalTime startTime, LocalTime endTime, String room) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
    }

    /**
     * Two schedules clash if they fall on the same day and their time
     * ranges overlap at all.
     */
    public boolean hasClash(Schedule other) {
        if (other == null || this.day != other.day) {
            return false;
        }
        return this.startTime.isBefore(other.endTime) && other.startTime.isBefore(this.endTime);
    }

    public String getScheduleInfo() {
        return day + " " + startTime + "-" + endTime + " @ " + room;
    }

    // Not in the diagram's method list (only getScheduleInfo() combines
    // these into one string), but a repository needs the individual fields
    // to serialize/deserialize a Schedule properly rather than trying to
    // parse a human-readable sentence back apart.
    public Day getDay() {
        return day;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public String getRoom() {
        return room;
    }
}