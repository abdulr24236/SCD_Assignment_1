package campus.comparators;

import campus.model.request.Request;
import java.util.Comparator;

/**
 * Orders requests by priority, highest priority first. Priority 1 is the
 * highest, so this is a plain ascending sort on the priority number
 */
public class RequestPriorityComparator implements Comparator<Request> {

    @Override
    public int compare(Request r1, Request r2) {
        return Integer.compare(r1.getPriority(), r2.getPriority());
    }
}