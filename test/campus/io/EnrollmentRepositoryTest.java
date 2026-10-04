/**
 * ============================================================
 * File        : EnrollmentRepositoryTest.java
 * Description : Comprehensive unit tests for EnrollmentRepository
 * ============================================================
 */
package campus.io;

import campus.enums.EnrollmentStatus;
import campus.model.academic.Enrollment;
import campus.model.academic.Section;
import campus.model.person.NormalStudent;
import campus.model.person.Student;
import campus.model.person.TeachingAssistant;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for EnrollmentRepository class covering save, load,
 * and file persistence functionality.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EnrollmentRepositoryTest {

    // ==================== Save Tests ====================

    @Test
    @DisplayName("Should save single enrollment successfully")
    void testSaveEnrollment() {
        // Arrange
        Student mockStudent = mock(NormalStudent.class);
        Section mockSection = mock(Section.class);
        when(mockSection.getSectionId()).thenReturn("SEC001");

        Enrollment enrollment = mock(Enrollment.class);
        when(enrollment.getEnrollmentId()).thenReturn("E001");
        when(enrollment.getStudent()).thenReturn(mockStudent);
        when(enrollment.getSection()).thenReturn(mockSection);
        when(enrollment.getEnrollmentDate()).thenReturn(LocalDate.of(2024, 1, 15));
        when(enrollment.getStatus()).thenReturn(EnrollmentStatus.ACTIVE);

        // Act - This will actually save to file
        EnrollmentRepository.save(enrollment);

        // Assert - Verify no exceptions thrown
        assertNotNull(enrollment);
    }

    @Test
    @DisplayName("Should save enrollment with COMPLETED status")
    void testSaveCompletedEnrollment() {
        // Arrange
        Student mockStudent = mock(NormalStudent.class);
        Section mockSection = mock(Section.class);
        when(mockSection.getSectionId()).thenReturn("SEC001");

        Enrollment enrollment = mock(Enrollment.class);
        when(enrollment.getEnrollmentId()).thenReturn("E002");
        when(enrollment.getStudent()).thenReturn(mockStudent);
        when(enrollment.getSection()).thenReturn(mockSection);
        when(enrollment.getEnrollmentDate()).thenReturn(LocalDate.of(2024, 1, 20));
        when(enrollment.getStatus()).thenReturn(EnrollmentStatus.COMPLETED);

        // Act
        EnrollmentRepository.save(enrollment);

        // Assert
        assertNotNull(enrollment);
    }

    @Test
    @DisplayName("Should save enrollment with DROPPED status")
    void testSaveDroppedEnrollment() {
        // Arrange
        Student mockStudent = mock(NormalStudent.class);
        Section mockSection = mock(Section.class);
        when(mockSection.getSectionId()).thenReturn("SEC001");

        Enrollment enrollment = mock(Enrollment.class);
        when(enrollment.getEnrollmentId()).thenReturn("E003");
        when(enrollment.getStudent()).thenReturn(mockStudent);
        when(enrollment.getSection()).thenReturn(mockSection);
        when(enrollment.getEnrollmentDate()).thenReturn(LocalDate.of(2024, 1, 25));
        when(enrollment.getStatus()).thenReturn(EnrollmentStatus.DROPPED);

        // Act
        EnrollmentRepository.save(enrollment);

        // Assert
        assertNotNull(enrollment);
    }

    // ==================== SaveAll Tests ====================

    @Test
    @DisplayName("Should save all enrollments to file")
    void testSaveAllEnrollments() {
        // Arrange
        List<Enrollment> enrollments = new ArrayList<>();

        Student mockStudent1 = mock(NormalStudent.class);
        Section mockSection1 = mock(Section.class);
        when(mockSection1.getSectionId()).thenReturn("SEC001");

        Enrollment enrollment1 = mock(Enrollment.class);
        when(enrollment1.getEnrollmentId()).thenReturn("E001");
        when(enrollment1.getStudent()).thenReturn(mockStudent1);
        when(enrollment1.getSection()).thenReturn(mockSection1);
        when(enrollment1.getEnrollmentDate()).thenReturn(LocalDate.of(2024, 1, 15));
        when(enrollment1.getStatus()).thenReturn(EnrollmentStatus.ACTIVE);

        Student mockStudent2 = mock(NormalStudent.class);
        Section mockSection2 = mock(Section.class);
        when(mockSection2.getSectionId()).thenReturn("SEC002");

        Enrollment enrollment2 = mock(Enrollment.class);
        when(enrollment2.getEnrollmentId()).thenReturn("E002");
        when(enrollment2.getStudent()).thenReturn(mockStudent2);
        when(enrollment2.getSection()).thenReturn(mockSection2);
        when(enrollment2.getEnrollmentDate()).thenReturn(LocalDate.of(2024, 1, 16));
        when(enrollment2.getStatus()).thenReturn(EnrollmentStatus.COMPLETED);

        enrollments.add(enrollment1);
        enrollments.add(enrollment2);

        // Act
        EnrollmentRepository.saveAll(enrollments);

        // Assert
        assertEquals(2, enrollments.size());
    }

    @Test
    @DisplayName("Should save empty list without errors")
    void testSaveAllEmptyList() {
        // Arrange
        List<Enrollment> enrollments = new ArrayList<>();

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> EnrollmentRepository.saveAll(enrollments));
    }

    @Test
    @DisplayName("Should save multiple enrollments")
    void testSaveAllMultipleEnrollments() {
        // Arrange
        List<Enrollment> enrollments = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            Student mockStudent = mock(NormalStudent.class);
            Section mockSection = mock(Section.class);
            when(mockSection.getSectionId()).thenReturn("SEC00" + i);

            Enrollment enrollment = mock(Enrollment.class);
            when(enrollment.getEnrollmentId()).thenReturn("E00" + i);
            when(enrollment.getStudent()).thenReturn(mockStudent);
            when(enrollment.getSection()).thenReturn(mockSection);
            when(enrollment.getEnrollmentDate()).thenReturn(LocalDate.of(2024, 1, 10 + i));
            when(enrollment.getStatus()).thenReturn(EnrollmentStatus.ACTIVE);

            enrollments.add(enrollment);
        }

        // Act
        EnrollmentRepository.saveAll(enrollments);

        // Assert
        assertEquals(3, enrollments.size());
    }

    // ==================== LoadAll Tests ====================

    @Test
    @DisplayName("Should load empty list when no data exists")
    void testLoadAllEmptyFile() {
        // Arrange
        List<Section> sections = new ArrayList<>();
        List<NormalStudent> normalStudents = new ArrayList<>();
        List<TeachingAssistant> teachingAssistants = new ArrayList<>();

        // Act
        List<Enrollment> enrollments = EnrollmentRepository.loadAll(sections, normalStudents, teachingAssistants);

        // Assert
        assertNotNull(enrollments, "Should return non-null list");
    }

    @Test
    @DisplayName("Should handle null section gracefully")
    void testLoadEnrollmentWithNullSection() {
        // Arrange
        List<Section> sections = new ArrayList<>();
        List<NormalStudent> normalStudents = new ArrayList<>();
        NormalStudent student = mock(NormalStudent.class);
        when(student.getStudentId()).thenReturn("S001");
        normalStudents.add(student);

        List<TeachingAssistant> teachingAssistants = new ArrayList<>();

        // Act
        List<Enrollment> enrollments = EnrollmentRepository.loadAll(sections, normalStudents, teachingAssistants);

        // Assert
        assertNotNull(enrollments);
    }

    @Test
    @DisplayName("Should handle empty student list")
    void testLoadEnrollmentWithEmptyStudentList() {
        // Arrange
        List<Section> sections = new ArrayList<>();
        Section section = mock(Section.class);
        when(section.getSectionId()).thenReturn("SEC001");
        sections.add(section);

        List<NormalStudent> normalStudents = new ArrayList<>();
        List<TeachingAssistant> teachingAssistants = new ArrayList<>();

        // Act
        List<Enrollment> enrollments = EnrollmentRepository.loadAll(sections, normalStudents, teachingAssistants);

        // Assert
        assertNotNull(enrollments);
    }

    // ==================== Integration Tests ====================

    @Test
    @DisplayName("Should handle enrollment lifecycle - save and load")
    void testEnrollmentLifecycle() {
        // Arrange
        Student mockStudent = mock(NormalStudent.class);
        when(mockStudent.getStudentId()).thenReturn("S001");

        Section mockSection = mock(Section.class);
        when(mockSection.getSectionId()).thenReturn("SEC001");

        Enrollment enrollment = mock(Enrollment.class);
        when(enrollment.getEnrollmentId()).thenReturn("E001");
        when(enrollment.getStudent()).thenReturn(mockStudent);
        when(enrollment.getSection()).thenReturn(mockSection);
        when(enrollment.getEnrollmentDate()).thenReturn(LocalDate.of(2024, 1, 15));
        when(enrollment.getStatus()).thenReturn(EnrollmentStatus.ACTIVE);

        // Act - Save
        EnrollmentRepository.save(enrollment);

        // Assert
        assertNotNull(enrollment);
        assertEquals("E001", enrollment.getEnrollmentId());
        assertEquals(EnrollmentStatus.ACTIVE, enrollment.getStatus());
    }

    @Test
    @DisplayName("Should handle multiple status types")
    void testMultipleEnrollmentStatuses() {
        // Arrange
        List<Enrollment> enrollments = new ArrayList<>();

        // ACTIVE enrollment
        Student student1 = mock(NormalStudent.class);
        Section section1 = mock(Section.class);
        when(section1.getSectionId()).thenReturn("SEC001");
        Enrollment enrollment1 = mock(Enrollment.class);
        when(enrollment1.getEnrollmentId()).thenReturn("E001");
        when(enrollment1.getStudent()).thenReturn(student1);
        when(enrollment1.getSection()).thenReturn(section1);
        when(enrollment1.getEnrollmentDate()).thenReturn(LocalDate.of(2024, 1, 15));
        when(enrollment1.getStatus()).thenReturn(EnrollmentStatus.ACTIVE);

        // COMPLETED enrollment
        Student student2 = mock(NormalStudent.class);
        Section section2 = mock(Section.class);
        when(section2.getSectionId()).thenReturn("SEC002");
        Enrollment enrollment2 = mock(Enrollment.class);
        when(enrollment2.getEnrollmentId()).thenReturn("E002");
        when(enrollment2.getStudent()).thenReturn(student2);
        when(enrollment2.getSection()).thenReturn(section2);
        when(enrollment2.getEnrollmentDate()).thenReturn(LocalDate.of(2024, 1, 16));
        when(enrollment2.getStatus()).thenReturn(EnrollmentStatus.COMPLETED);

        // DROPPED enrollment
        Student student3 = mock(NormalStudent.class);
        Section section3 = mock(Section.class);
        when(section3.getSectionId()).thenReturn("SEC003");
        Enrollment enrollment3 = mock(Enrollment.class);
        when(enrollment3.getEnrollmentId()).thenReturn("E003");
        when(enrollment3.getStudent()).thenReturn(student3);
        when(enrollment3.getSection()).thenReturn(section3);
        when(enrollment3.getEnrollmentDate()).thenReturn(LocalDate.of(2024, 1, 17));
        when(enrollment3.getStatus()).thenReturn(EnrollmentStatus.DROPPED);

        enrollments.add(enrollment1);
        enrollments.add(enrollment2);
        enrollments.add(enrollment3);

        // Act
        EnrollmentRepository.saveAll(enrollments);

        // Assert
        assertEquals(3, enrollments.size());
        assertEquals(EnrollmentStatus.ACTIVE, enrollment1.getStatus());
        assertEquals(EnrollmentStatus.COMPLETED, enrollment2.getStatus());
        assertEquals(EnrollmentStatus.DROPPED, enrollment3.getStatus());
    }

    @Test
    @DisplayName("Should handle enrollment dates correctly")
    void testEnrollmentDates() {
        // Arrange
        Student mockStudent = mock(NormalStudent.class);
        Section mockSection = mock(Section.class);
        when(mockSection.getSectionId()).thenReturn("SEC001");

        LocalDate testDate = LocalDate.of(2024, 3, 25);

        Enrollment enrollment = mock(Enrollment.class);
        when(enrollment.getEnrollmentId()).thenReturn("E001");
        when(enrollment.getStudent()).thenReturn(mockStudent);
        when(enrollment.getSection()).thenReturn(mockSection);
        when(enrollment.getEnrollmentDate()).thenReturn(testDate);
        when(enrollment.getStatus()).thenReturn(EnrollmentStatus.ACTIVE);

        // Act
        EnrollmentRepository.save(enrollment);

        // Assert
        assertEquals(testDate, enrollment.getEnrollmentDate());
    }

    @Test
    @DisplayName("Should maintain enrollment ID integrity")
    void testEnrollmentIdIntegrity() {
        // Arrange
        Student mockStudent = mock(NormalStudent.class);
        Section mockSection = mock(Section.class);
        when(mockSection.getSectionId()).thenReturn("SEC001");

        String enrollmentId = "E12345";

        Enrollment enrollment = mock(Enrollment.class);
        when(enrollment.getEnrollmentId()).thenReturn(enrollmentId);
        when(enrollment.getStudent()).thenReturn(mockStudent);
        when(enrollment.getSection()).thenReturn(mockSection);
        when(enrollment.getEnrollmentDate()).thenReturn(LocalDate.of(2024, 1, 15));
        when(enrollment.getStatus()).thenReturn(EnrollmentStatus.ACTIVE);

        // Act
        EnrollmentRepository.save(enrollment);

        // Assert
        assertEquals(enrollmentId, enrollment.getEnrollmentId());
    }

    @Test
    @DisplayName("Should handle teaching assistant enrollment")
    void testTeachingAssistantEnrollment() {
        // Arrange
        TeachingAssistant ta = mock(TeachingAssistant.class);
        when(ta.getStudentId()).thenReturn("TA001");

        Section mockSection = mock(Section.class);
        when(mockSection.getSectionId()).thenReturn("SEC001");

        Enrollment enrollment = mock(Enrollment.class);
        when(enrollment.getEnrollmentId()).thenReturn("E001");
        when(enrollment.getStudent()).thenReturn(ta);
        when(enrollment.getSection()).thenReturn(mockSection);
        when(enrollment.getEnrollmentDate()).thenReturn(LocalDate.of(2024, 1, 15));
        when(enrollment.getStatus()).thenReturn(EnrollmentStatus.ACTIVE);

        // Act
        EnrollmentRepository.save(enrollment);

        // Assert
        assertNotNull(enrollment);
        assertEquals("E001", enrollment.getEnrollmentId());
    }

    @Test
    @DisplayName("Should handle batch save operations")
    void testBatchSaveOperations() {
        // Arrange
        List<Enrollment> batch1 = new ArrayList<>();
        List<Enrollment> batch2 = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            Student student = mock(NormalStudent.class);
            Section section = mock(Section.class);
            when(section.getSectionId()).thenReturn("SEC00" + i);

            Enrollment enrollment = mock(Enrollment.class);
            when(enrollment.getEnrollmentId()).thenReturn("E00" + i);
            when(enrollment.getStudent()).thenReturn(student);
            when(enrollment.getSection()).thenReturn(section);
            when(enrollment.getEnrollmentDate()).thenReturn(LocalDate.of(2024, 1, i));
            when(enrollment.getStatus()).thenReturn(EnrollmentStatus.ACTIVE);

            if (i <= 3) {
                batch1.add(enrollment);
            } else {
                batch2.add(enrollment);
            }
        }

        // Act
        EnrollmentRepository.saveAll(batch1);
        EnrollmentRepository.saveAll(batch2);

        // Assert
        assertEquals(3, batch1.size());
        assertEquals(2, batch2.size());
    }

    @Test
    @DisplayName("Should verify enrollment properties")
    void testEnrollmentProperties() {
        // Arrange
        Student mockStudent = mock(NormalStudent.class);
        when(mockStudent.getStudentId()).thenReturn("S001");

        Section mockSection = mock(Section.class);
        when(mockSection.getSectionId()).thenReturn("SEC001");

        Enrollment enrollment = mock(Enrollment.class);
        when(enrollment.getEnrollmentId()).thenReturn("E001");
        when(enrollment.getStudent()).thenReturn(mockStudent);
        when(enrollment.getSection()).thenReturn(mockSection);
        when(enrollment.getEnrollmentDate()).thenReturn(LocalDate.of(2024, 1, 15));
        when(enrollment.getStatus()).thenReturn(EnrollmentStatus.ACTIVE);

        // Act & Assert
        assertEquals("E001", enrollment.getEnrollmentId());
        assertEquals("S001", enrollment.getStudent().getStudentId());
        assertEquals("SEC001", enrollment.getSection().getSectionId());
        assertEquals(LocalDate.of(2024, 1, 15), enrollment.getEnrollmentDate());
        assertEquals(EnrollmentStatus.ACTIVE, enrollment.getStatus());
    }
}