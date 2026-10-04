package campus.model.academic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A course offered by the university, with one or more Sections and any
 * prerequisite Courses .
 */
public class Course {
    private String courseCode;
    private String title;
    private int creditHours;
    private Set<Course> prerequisites = new HashSet<>();
    private List<Section> sections = new ArrayList<>();

    public Course(String courseCode, String title, int creditHours) {
        this.courseCode = courseCode;
        this.title = title;
        this.creditHours = creditHours;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getTitle() {
        return title;
    }

    public int getCreditHours() {
        return creditHours;
    }

    public void addPrerequisite(Course course) {
        prerequisites.add(course);
    }

    public void addSection(Section section) {
        sections.add(section);
    }

    public List<Section> getSections() {
        return sections;
    }
}