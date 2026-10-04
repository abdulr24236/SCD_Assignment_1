package campus.model.fyp;

import java.time.LocalDate;

/**
 * A single supervisor meeting for an FYP group, with a running notes log
 */
public class FYPMeeting {
    private String meetingId;
    private LocalDate meetingDate;
    private String agenda;
    private String notes;

    // FYPMeeting to the right FYPGroup, same gap as Feedback's submissionId.
    private String groupId;

    public FYPMeeting(String meetingId, LocalDate meetingDate, String agenda, String groupId) {
        this.meetingId = meetingId;
        this.meetingDate = meetingDate;
        this.agenda = agenda;
        this.groupId = groupId;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getMeetingId() {
        return meetingId;
    }

    public String getAgenda() {
        return agenda;
    }

    public String getNotes() {
        return notes;
    }

    public String getMeetingDetails() {
        return meetingDate + " - " + agenda;
    }

    public void updateNotes(String notes) {
        this.notes = notes;
    }

    // Not in the diagram's method list, but FYPMeetingDateComparator needs
    // to actually read this to sort by it.
    public LocalDate getMeetingDate() {
        return meetingDate;
    }
}