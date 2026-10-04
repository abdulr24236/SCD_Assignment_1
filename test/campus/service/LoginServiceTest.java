/**
 * ============================================================
 * File        : LoginServiceTest.java
 * Description : Comprehensive unit tests for LoginService
 * ============================================================
 */
package campus.service;

import campus.model.person.*;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for LoginService class covering authentication,
 * registration, validation, and ID generation functionality.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LoginServiceTest {

    @Mock
    private DataStore mockDataStore;

    private LoginService loginService;
    private AutoCloseable closeable;

    @BeforeAll
    void setUpAll() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterAll
    void tearDownAll() throws Exception {
        closeable.close();
    }

    @BeforeEach
    void setUp() {
        // Initialize mock DataStore with empty lists
        when(mockDataStore.getNormalStudents()).thenReturn(new ArrayList<>());
        when(mockDataStore.getAdmins()).thenReturn(new ArrayList<>());
        when(mockDataStore.getVisitingInstructors()).thenReturn(new ArrayList<>());
        when(mockDataStore.getPermanentInstructors()).thenReturn(new ArrayList<>());
        when(mockDataStore.getTeachingAssistants()).thenReturn(new ArrayList<>());

        loginService = new LoginService(mockDataStore);
    }

    // ==================== Authentication Tests ====================

    @Test
    @DisplayName("Should authenticate admin with valid credentials using username")
    void testAuthenticateAdminWithUsername() {
        // Arrange
        AcademicOfficeAdmin mockAdmin = mock(AcademicOfficeAdmin.class);
        when(mockDataStore.findAdminById("ADM001")).thenReturn(mockAdmin);

        // Act
        Person result = loginService.authenticate("admin", "admin123");

        // Assert
        assertNotNull(result, "Authentication should succeed with valid admin credentials");
        assertEquals(mockAdmin, result, "Should return the correct admin object");
    }

    @Test
    @DisplayName("Should authenticate student with valid credentials using student ID")
    void testAuthenticateStudentWithId() {
        // Arrange
        NormalStudent mockStudent = mock(NormalStudent.class);
        when(mockDataStore.findStudentById("S001")).thenReturn(mockStudent);
        when(mockDataStore.findTaById("S001")).thenReturn(null);

        // Act
        Person result = loginService.authenticate("s001", "pass123");

        // Assert
        assertNotNull(result, "Authentication should succeed with valid student credentials");
        assertEquals(mockStudent, result, "Should return the correct student object");
    }

    @Test
    @DisplayName("Should authenticate teaching assistant with valid credentials")
    void testAuthenticateTaWithId() {
        // Arrange
        TeachingAssistant mockTa = mock(TeachingAssistant.class);
        when(mockDataStore.findTaById("TA001")).thenReturn(mockTa);

        // Act
        Person result = loginService.authenticate("ta001", "pass123");

        // Assert
        assertNotNull(result, "Authentication should succeed with valid TA credentials");
        assertEquals(mockTa, result, "Should return the correct TA object");
    }

    @Test
    @DisplayName("Should authenticate visiting instructor with valid credentials")
    void testAuthenticateVisitingInstructor() {
        // Arrange
        VisitingInstructor mockVI = mock(VisitingInstructor.class);
        when(mockDataStore.findVisitingById("VI001")).thenReturn(mockVI);

        // Act
        Person result = loginService.authenticate("vi001", "pass123");

        // Assert
        assertNotNull(result, "Authentication should succeed with valid VI credentials");
        assertEquals(mockVI, result, "Should return the correct visiting instructor object");
    }

    @Test
    @DisplayName("Should authenticate permanent instructor with valid credentials")
    void testAuthenticatePermanentInstructor() {
        // Arrange
        PermanentInstructor mockPI = mock(PermanentInstructor.class);
        when(mockDataStore.findPermanentById("PI001")).thenReturn(mockPI);

        // Act
        Person result = loginService.authenticate("pi001", "pass123");

        // Assert
        assertNotNull(result, "Authentication should succeed with valid PI credentials");
        assertEquals(mockPI, result, "Should return the correct permanent instructor object");
    }

    @Test
    @DisplayName("Should fail authentication with invalid password")
    void testAuthenticateWithInvalidPassword() {
        // Act
        Person result = loginService.authenticate("admin", "wrongpassword");

        // Assert
        assertNull(result, "Authentication should fail with invalid password");
    }

    @Test
    @DisplayName("Should fail authentication with null identifier")
    void testAuthenticateWithNullIdentifier() {
        // Act
        Person result = loginService.authenticate(null, "admin123");

        // Assert
        assertNull(result, "Authentication should fail with null identifier");
    }

    @Test
    @DisplayName("Should fail authentication with null password")
    void testAuthenticateWithNullPassword() {
        // Act
        Person result = loginService.authenticate("admin", null);

        // Assert
        assertNull(result, "Authentication should fail with null password");
    }

    @Test
    @DisplayName("Should fail authentication with non-existent user")
    void testAuthenticateWithNonExistentUser() {
        // Act
        Person result = loginService.authenticate("nonexistent", "password");

        // Assert
        assertNull(result, "Authentication should fail for non-existent user");
    }

    @Test
    @DisplayName("Should authenticate with case-insensitive identifier")
    void testAuthenticateWithCaseInsensitiveIdentifier() {
        // Arrange
        AcademicOfficeAdmin mockAdmin = mock(AcademicOfficeAdmin.class);
        when(mockDataStore.findAdminById("ADM001")).thenReturn(mockAdmin);

        // Act
        Person result = loginService.authenticate("ADMIN", "admin123");

        // Assert
        assertNotNull(result, "Authentication should be case-insensitive");
    }

    @Test
    @DisplayName("Should authenticate student by email from DataStore")
    void testAuthenticateStudentByEmailFromDataStore() {
        // Arrange
        NormalStudent mockStudent = mock(NormalStudent.class);
        when(mockStudent.getEmail()).thenReturn("ahmad@fast.edu");
        when(mockStudent.getStudentId()).thenReturn("S001");

        List<NormalStudent> students = new ArrayList<>();
        students.add(mockStudent);
        when(mockDataStore.getNormalStudents()).thenReturn(students);
        when(mockDataStore.findStudentById("S001")).thenReturn(mockStudent);
        when(mockDataStore.findTaById("S001")).thenReturn(null);

        // Act
        Person result = loginService.authenticate("ahmad@fast.edu", "pass123");

        // Assert
        assertNotNull(result, "Should authenticate student by email from DataStore");
        assertEquals(mockStudent, result);
    }

    // ==================== Registration Tests ====================

    @Test
    @DisplayName("Should successfully register new user")
    void testRegisterUser() {
        // Act
        boolean result = loginService.registerUser("newuser", "password123", "STUDENT", "S999", "newuser@fast.edu");

        // Assert
        assertTrue(result, "User registration should succeed");
    }

    @Test
    @DisplayName("Should register user and allow authentication")
    void testRegisterUserAndAuthenticate() {
        // Arrange
        NormalStudent mockStudent = mock(NormalStudent.class);
        when(mockDataStore.findStudentById("S999")).thenReturn(mockStudent);
        when(mockDataStore.findTaById("S999")).thenReturn(null);

        // Act
        loginService.registerUser("newuser", "password123", "STUDENT", "S999", "newuser@fast.edu");
        Person result = loginService.authenticate("newuser", "password123");

        // Assert
        assertNotNull(result, "Should authenticate after registration");
        assertEquals(mockStudent, result);
    }

    @Test
    @DisplayName("Should register TA credentials with multiple identifiers")
    void testRegisterTaCredentials() {
        // Arrange
        TeachingAssistant mockTa = mock(TeachingAssistant.class);
        when(mockDataStore.findTaById("TA999")).thenReturn(mockTa);

        // Act
        loginService.registerTaCredentials("TA999", "S999", "ta@fast.edu", "tapass123");
        Person resultByTaId = loginService.authenticate("TA999", "tapass123");
        Person resultByStudentId = loginService.authenticate("S999", "tapass123");
        Person resultByEmail = loginService.authenticate("ta@fast.edu", "tapass123");

        // Assert
        assertNotNull(resultByTaId, "Should authenticate TA by TA ID");
        assertNotNull(resultByStudentId, "Should authenticate TA by Student ID");
        assertNotNull(resultByEmail, "Should authenticate TA by email");
    }

    // ==================== Email Validation Tests ====================

    @Test
    @DisplayName("Should detect registered student email")
    void testIsEmailRegisteredForStudent() {
        // Arrange
        NormalStudent mockStudent = mock(NormalStudent.class);
        when(mockStudent.getEmail()).thenReturn("student@fast.edu");
        List<NormalStudent> students = new ArrayList<>();
        students.add(mockStudent);
        when(mockDataStore.getNormalStudents()).thenReturn(students);

        // Act
        boolean result = loginService.isEmailRegistered("student@fast.edu");

        // Assert
        assertTrue(result, "Should detect registered student email");
    }

    @Test
    @DisplayName("Should detect registered admin email")
    void testIsEmailRegisteredForAdmin() {
        // Arrange
        AcademicOfficeAdmin mockAdmin = mock(AcademicOfficeAdmin.class);
        when(mockAdmin.getEmail()).thenReturn("admin@fast.edu");
        List<AcademicOfficeAdmin> admins = new ArrayList<>();
        admins.add(mockAdmin);
        when(mockDataStore.getAdmins()).thenReturn(admins);

        // Act
        boolean result = loginService.isEmailRegistered("admin@fast.edu");

        // Assert
        assertTrue(result, "Should detect registered admin email");
    }

    @Test
    @DisplayName("Should detect registered TA email")
    void testIsEmailRegisteredForTA() {
        // Arrange
        TeachingAssistant mockTa = mock(TeachingAssistant.class);
        when(mockTa.getEmail()).thenReturn("ta@fast.edu");
        List<TeachingAssistant> tas = new ArrayList<>();
        tas.add(mockTa);
        when(mockDataStore.getTeachingAssistants()).thenReturn(tas);

        // Act
        boolean result = loginService.isEmailRegistered("ta@fast.edu");

        // Assert
        assertTrue(result, "Should detect registered TA email");
    }

    @Test
    @DisplayName("Should return false for unregistered email")
    void testIsEmailRegisteredForUnregisteredEmail() {
        // Act
        boolean result = loginService.isEmailRegistered("unregistered@fast.edu");

        // Assert
        assertFalse(result, "Should return false for unregistered email");
    }

    @Test
    @DisplayName("Should return false for null email")
    void testIsEmailRegisteredForNullEmail() {
        // Act
        boolean result = loginService.isEmailRegistered(null);

        // Assert
        assertFalse(result, "Should return false for null email");
    }

    @Test
    @DisplayName("Should perform case-insensitive email check")
    void testIsEmailRegisteredCaseInsensitive() {
        // Arrange
        NormalStudent mockStudent = mock(NormalStudent.class);
        when(mockStudent.getEmail()).thenReturn("student@fast.edu");
        List<NormalStudent> students = new ArrayList<>();
        students.add(mockStudent);
        when(mockDataStore.getNormalStudents()).thenReturn(students);

        // Act
        boolean result = loginService.isEmailRegistered("STUDENT@FAST.EDU");

        // Assert
        assertTrue(result, "Email check should be case-insensitive");
    }

    // ==================== ID Validation Tests ====================

    @Test
    @DisplayName("Should detect registered student ID")
    void testIsIdRegisteredForStudent() {
        // Arrange
        NormalStudent mockStudent = mock(NormalStudent.class);
        when(mockStudent.getStudentId()).thenReturn("S001");
        List<NormalStudent> students = new ArrayList<>();
        students.add(mockStudent);
        when(mockDataStore.getNormalStudents()).thenReturn(students);

        // Act
        boolean result = loginService.isIdRegistered("S001");

        // Assert
        assertTrue(result, "Should detect registered student ID");
    }

    @Test
    @DisplayName("Should detect registered admin ID")
    void testIsIdRegisteredForAdmin() {
        // Arrange
        AcademicOfficeAdmin mockAdmin = mock(AcademicOfficeAdmin.class);
        when(mockAdmin.getAdminId()).thenReturn("ADM001");
        List<AcademicOfficeAdmin> admins = new ArrayList<>();
        admins.add(mockAdmin);
        when(mockDataStore.getAdmins()).thenReturn(admins);

        // Act
        boolean result = loginService.isIdRegistered("ADM001");

        // Assert
        assertTrue(result, "Should detect registered admin ID");
    }

    @Test
    @DisplayName("Should detect registered TA ID")
    void testIsIdRegisteredForTA() {
        // Arrange
        TeachingAssistant mockTa = mock(TeachingAssistant.class);
        when(mockTa.getTaId()).thenReturn("TA001");
        when(mockTa.getStudentId()).thenReturn("S001");
        List<TeachingAssistant> tas = new ArrayList<>();
        tas.add(mockTa);
        when(mockDataStore.getTeachingAssistants()).thenReturn(tas);

        // Act
        boolean resultTaId = loginService.isIdRegistered("TA001");
        boolean resultStudentId = loginService.isIdRegistered("S001");

        // Assert
        assertTrue(resultTaId, "Should detect registered TA ID");
        assertTrue(resultStudentId, "Should detect TA's student ID");
    }

    @Test
    @DisplayName("Should return false for unregistered ID")
    void testIsIdRegisteredForUnregisteredId() {
        // Act
        boolean result = loginService.isIdRegistered("UNREGISTERED999");

        // Assert
        assertFalse(result, "Should return false for unregistered ID");
    }

    @Test
    @DisplayName("Should return false for null ID")
    void testIsIdRegisteredForNullId() {
        // Act
        boolean result = loginService.isIdRegistered(null);

        // Assert
        assertFalse(result, "Should return false for null ID");
    }

    @Test
    @DisplayName("Should perform case-insensitive ID check")
    void testIsIdRegisteredCaseInsensitive() {
        // Arrange
        NormalStudent mockStudent = mock(NormalStudent.class);
        when(mockStudent.getStudentId()).thenReturn("S001");
        List<NormalStudent> students = new ArrayList<>();
        students.add(mockStudent);
        when(mockDataStore.getNormalStudents()).thenReturn(students);

        // Act
        boolean result = loginService.isIdRegistered("s001");

        // Assert
        assertTrue(result, "ID check should be case-insensitive");
    }

    // ==================== ID Generation Tests ====================

    @Test
    @DisplayName("Should generate first student ID as S001")
    void testGetNextStudentIdFirst() {
        // Act
        String nextId = loginService.getNextStudentId();

        // Assert
        assertEquals("S001", nextId, "First student ID should be S001");
    }

    @Test
    @DisplayName("Should generate next student ID sequentially")
    void testGetNextStudentIdSequential() {
        // Arrange
        NormalStudent mockStudent1 = mock(NormalStudent.class);
        when(mockStudent1.getStudentId()).thenReturn("S001");
        NormalStudent mockStudent2 = mock(NormalStudent.class);
        when(mockStudent2.getStudentId()).thenReturn("S005");

        List<NormalStudent> students = new ArrayList<>();
        students.add(mockStudent1);
        students.add(mockStudent2);
        when(mockDataStore.getNormalStudents()).thenReturn(students);

        // Act
        String nextId = loginService.getNextStudentId();

        // Assert
        assertEquals("S006", nextId, "Should generate next sequential student ID");
    }

    @Test
    @DisplayName("Should generate first visiting instructor ID as VI001")
    void testGetNextVisitingIdFirst() {
        // Act
        String nextId = loginService.getNextVisitingId();

        // Assert
        assertEquals("VI001", nextId, "First visiting instructor ID should be VI001");
    }

    @Test
    @DisplayName("Should generate next visiting instructor ID sequentially")
    void testGetNextVisitingIdSequential() {
        // Arrange
        VisitingInstructor mockVI = mock(VisitingInstructor.class);
        when(mockVI.getTeacherId()).thenReturn("VI003");

        List<VisitingInstructor> vis = new ArrayList<>();
        vis.add(mockVI);
        when(mockDataStore.getVisitingInstructors()).thenReturn(vis);

        // Act
        String nextId = loginService.getNextVisitingId();

        // Assert
        assertEquals("VI004", nextId, "Should generate next sequential VI ID");
    }

    @Test
    @DisplayName("Should generate first permanent instructor ID as PI001")
    void testGetNextPermanentIdFirst() {
        // Act
        String nextId = loginService.getNextPermanentId();

        // Assert
        assertEquals("PI001", nextId, "First permanent instructor ID should be PI001");
    }

    @Test
    @DisplayName("Should generate next permanent instructor ID sequentially")
    void testGetNextPermanentIdSequential() {
        // Arrange
        PermanentInstructor mockPI = mock(PermanentInstructor.class);
        when(mockPI.getTeacherId()).thenReturn("PI007");

        List<PermanentInstructor> pis = new ArrayList<>();
        pis.add(mockPI);
        when(mockDataStore.getPermanentInstructors()).thenReturn(pis);

        // Act
        String nextId = loginService.getNextPermanentId();

        // Assert
        assertEquals("PI008", nextId, "Should generate next sequential PI ID");
    }

    @Test
    @DisplayName("Should generate first admin ID as ADM001")
    void testGetNextAdminIdFirst() {
        // Act
        String nextId = loginService.getNextAdminId();

        // Assert
        assertEquals("ADM001", nextId, "First admin ID should be ADM001");
    }

    @Test
    @DisplayName("Should generate next admin ID sequentially")
    void testGetNextAdminIdSequential() {
        // Arrange
        AcademicOfficeAdmin mockAdmin = mock(AcademicOfficeAdmin.class);
        when(mockAdmin.getAdminId()).thenReturn("ADM002");

        List<AcademicOfficeAdmin> admins = new ArrayList<>();
        admins.add(mockAdmin);
        when(mockDataStore.getAdmins()).thenReturn(admins);

        // Act
        String nextId = loginService.getNextAdminId();

        // Assert
        assertEquals("ADM003", nextId, "Should generate next sequential admin ID");
    }

    @Test
    @DisplayName("Should generate first TA ID as TA001")
    void testGetNextTaIdFirst() {
        // Act
        String nextId = loginService.getNextTaId();

        // Assert
        assertEquals("TA001", nextId, "First TA ID should be TA001");
    }

    @Test
    @DisplayName("Should generate next TA ID sequentially")
    void testGetNextTaIdSequential() {
        // Arrange
        TeachingAssistant mockTa = mock(TeachingAssistant.class);
        when(mockTa.getTaId()).thenReturn("TA010");

        List<TeachingAssistant> tas = new ArrayList<>();
        tas.add(mockTa);
        when(mockDataStore.getTeachingAssistants()).thenReturn(tas);

        // Act
        String nextId = loginService.getNextTaId();

        // Assert
        assertEquals("TA011", nextId, "Should generate next sequential TA ID");
    }

    @Test
    @DisplayName("Should handle non-numeric ID suffixes gracefully")
    void testGetNextStudentIdWithInvalidFormat() {
        // Arrange
        NormalStudent mockStudent = mock(NormalStudent.class);
        when(mockStudent.getStudentId()).thenReturn("SINVALID");

        List<NormalStudent> students = new ArrayList<>();
        students.add(mockStudent);
        when(mockDataStore.getNormalStudents()).thenReturn(students);

        // Act
        String nextId = loginService.getNextStudentId();

        // Assert
        assertEquals("S001", nextId, "Should default to S001 when encountering invalid format");
    }

    // ==================== Credential Record Tests ====================

    @Test
    @DisplayName("Should create credential with all parameters")
    void testCredentialCreationFull() {
        // Act
        LoginService.Credential credential = new LoginService.Credential("pass123", "STUDENT", "S001", "student@fast.edu");

        // Assert
        assertEquals("pass123", credential.password());
        assertEquals("STUDENT", credential.role());
        assertEquals("S001", credential.id());
        assertEquals("student@fast.edu", credential.email());
    }

    @Test
    @DisplayName("Should create credential with legacy constructor")
    void testCredentialCreationLegacy() {
        // Act
        LoginService.Credential credential = new LoginService.Credential("pass123", "STUDENT", "S001");

        // Assert
        assertEquals("pass123", credential.password());
        assertEquals("STUDENT", credential.role());
        assertEquals("S001", credential.id());
        assertEquals("", credential.email());
    }

    // ==================== Edge Cases and Integration Tests ====================

    @Test
    @DisplayName("Should prioritize TA role over student role during authentication")
    void testAuthenticationPrioritizesTaOverStudent() {
        // Arrange
        TeachingAssistant mockTa = mock(TeachingAssistant.class);
        when(mockTa.getEmail()).thenReturn("promoted@fast.edu");
        when(mockTa.getTaId()).thenReturn("TA999");
        when(mockTa.getStudentId()).thenReturn("S999");

        List<TeachingAssistant> tas = new ArrayList<>();
        tas.add(mockTa);
        when(mockDataStore.getTeachingAssistants()).thenReturn(tas);
        when(mockDataStore.findTaById("TA999")).thenReturn(mockTa);

        // Act
        Person result = loginService.authenticate("promoted@fast.edu", "pass123");

        // Assert
        assertNotNull(result, "Should authenticate promoted student as TA");
        assertEquals(mockTa, result, "Should return TA object, not student");
    }

    @Test
    @DisplayName("Should handle whitespace in identifiers")
    void testAuthenticateWithWhitespaceInIdentifier() {
        // Arrange
        AcademicOfficeAdmin mockAdmin = mock(AcademicOfficeAdmin.class);
        when(mockDataStore.findAdminById("ADM001")).thenReturn(mockAdmin);

        // Act
        Person result = loginService.authenticate("  admin  ", "admin123");

        // Assert
        assertNotNull(result, "Should handle whitespace in identifier");
    }

    @Test
    @DisplayName("Should handle multiple students with different IDs")
    void testGetNextStudentIdWithMultipleStudents() {
        // Arrange
        NormalStudent s1 = mock(NormalStudent.class);
        when(s1.getStudentId()).thenReturn("S001");
        NormalStudent s2 = mock(NormalStudent.class);
        when(s2.getStudentId()).thenReturn("S003");
        NormalStudent s3 = mock(NormalStudent.class);
        when(s3.getStudentId()).thenReturn("S002");

        List<NormalStudent> students = new ArrayList<>();
        students.add(s1);
        students.add(s2);
        students.add(s3);
        when(mockDataStore.getNormalStudents()).thenReturn(students);

        // Act
        String nextId = loginService.getNextStudentId();

        // Assert
        assertEquals("S004", nextId, "Should find maximum ID and increment");
    }
}