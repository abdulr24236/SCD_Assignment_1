package campus.comparators;

import campus.model.person.Student;
import java.util.Comparator;

/** Alphabetical order by name . */
public class StudentNameComparator implements Comparator<Student> {

    @Override
    public int compare(Student s1, Student s2) {
        return s1.getName().compareTo(s2.getName());
    }
}