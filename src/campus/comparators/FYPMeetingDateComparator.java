package campus.comparators;

import campus.model.fyp.FYPMeeting;
import java.util.Comparator;

/** Chronological order by meeting date, soonest first . */
public class FYPMeetingDateComparator implements Comparator<FYPMeeting> {

    @Override
    public int compare(FYPMeeting m1, FYPMeeting m2) {
        return m1.getMeetingDate().compareTo(m2.getMeetingDate());
    }
}