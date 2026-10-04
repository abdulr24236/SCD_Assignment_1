package campus.comparators;

import campus.model.assessment.Assignment;
import java.util.Comparator;

/** Chronological order by deadline, soonest first . */
public class AssignmentDeadlineComparator implements Comparator<Assignment> {

    @Override
    public int compare(Assignment a1, Assignment a2) {
        return a1.getDeadline().compareTo(a2.getDeadline());
    }
}