package campus.io;

import campus.model.fyp.FYPGroup;
import campus.model.fyp.FYPMeeting;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists FYPMeeting objects. Needs FYPGroups already loaded first.
 *
 * Line format: meetingId::meetingDate::agenda::notes::groupId
 *
 * notes isn't a constructor parameter (only set later via updateNotes()),
 * so it's restored separately after construction. Re-links into the
 * FYPGroup's meetings list via addMeeting() afterward, since the
 * constructor itself has no way to know which group it belongs to at
 * construction time — only the groupId field does.
 */
public class FYPMeetingRepository {
    private static final String FILE_NAME = "fyp_meetings.txt";

    public static void save(FYPMeeting meeting) {
        FileManager.appendLine(FILE_NAME, toLine(meeting));
    }

    public static void saveAll(List<FYPMeeting> meetings) {
        List<String> lines = new ArrayList<>();
        for (FYPMeeting meeting : meetings) {
            lines.add(toLine(meeting));
        }
        FileManager.writeLines(FILE_NAME, lines);
    }

    public static List<FYPMeeting> loadAll(List<FYPGroup> groups) {
        List<FYPMeeting> meetings = new ArrayList<>();
        for (String line : FileManager.readLines(FILE_NAME)) {
            meetings.add(fromLine(line, groups));
        }
        return meetings;
    }

    private static String toLine(FYPMeeting meeting) {
        String notes = meeting.getNotes() == null ? "" : meeting.getNotes();
        return meeting.getMeetingId() + FileManager.DELIMITER + meeting.getMeetingDate() + FileManager.DELIMITER + meeting.getAgenda()
                + FileManager.DELIMITER + notes + FileManager.DELIMITER + meeting.getGroupId();
    }

    private static FYPMeeting fromLine(String line, List<FYPGroup> groups) {
        String[] f = line.split(FileManager.DELIMITER, -1);
        String meetingId = f[0];
        LocalDate meetingDate = LocalDate.parse(f[1]);
        String agenda = f[2];
        String notes = f[3];
        String groupId = f[4];

        FYPMeeting meeting = new FYPMeeting(meetingId, meetingDate, agenda, groupId);
        if (!notes.isEmpty()) {
            meeting.updateNotes(notes);
        }

        FYPGroup group = findGroupById(groups, groupId);
        if (group != null) {
            group.addMeeting(meeting);
        }

        return meeting;
    }

    private static FYPGroup findGroupById(List<FYPGroup> groups, String groupId) {
        for (FYPGroup group : groups) {
            if (group.getGroupId().equals(groupId)) {
                return group;
            }
        }
        return null;
    }
}