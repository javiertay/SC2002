// src/test/java/controller/OfficerControllerTest.java
package controller;

import model.*;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OfficerControllerTest {
    private ApplicationController appController;
    private OfficerController officerController;
    private HDBOfficer officer;
    private Project testProject;
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    void setUp() {
        // reset registries
        ProjectRegistry.loadProjects(List.of());
        ApplicationRegistry.loadApplications(List.of());

        // wire up controllers
        appController      = new ApplicationController();
        officerController  = new OfficerController(appController);

        // create project
        testProject = new Project(
                "TestProject",
                "Bukit Timah",
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(5),
                true,
                2,
                ""
        );
        testProject.addFlatType("2-Room", 1, 50000);
        ProjectRegistry.loadProjects(List.of(testProject));

        // create officer
        officer = new HDBOfficer("Officer A", "S0000000A", "pw", 30, "Single");

        // capture console output
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    // --- Scenario 10: HDB Officer Registration Eligibility ---
    @Test
    void testReqToHandleProject_Success() {
        boolean result = officerController.reqToHandleProject(officer, "TestProject");
        assertTrue(result);
        assertEquals(
                HDBOfficer.RegistrationStatus.PENDING,
                officer.getRegistrationStatus("TestProject")
        );
    }

    // --- Scenario 10: Already Pending Officer Registration ---
    @Test
    void testReqToHandleProject_AlreadyPending() {
        officer.setRegistrationStatus("TestProject", HDBOfficer.RegistrationStatus.PENDING);
        officer.assignToProject("TestProject");
        boolean result = officerController.reqToHandleProject(officer, "TestProject");
        assertFalse(result);
    }

    // --- Scenario 11: View Officer Registration Status ---
    @Test
    void testViewRegistrationStatus() {
        officer.setRegistrationStatus("TestProject", HDBOfficer.RegistrationStatus.APPROVED);
        HDBOfficer.RegistrationStatus status = officerController.viewRegistrationStatus(officer, "TestProject");
        assertEquals(HDBOfficer.RegistrationStatus.APPROVED, status);
    }

    // --- Scenario 12: Project Detail Access for HDB Officer ---
    @Test
    void testViewAssignedProjectDetails() {
        officer.assignToProject("TestProject");
        Project fetched = officerController.viewAssignedProjectDetails(officer);
        assertNotNull(fetched);
        assertEquals("TestProject", fetched.getName());
    }

    // --- Scenario 8 & 15: Single Flat Booking per Successful Application & Flat Selection/Booking Management ---
    @Test
    void testSingleFlatBooking_and_preventMultipleBookings() {
        // Officer approved and assigned
        officer.setRegistrationStatus("TestProject", HDBOfficer.RegistrationStatus.APPROVED);
        officer.assignToProject("TestProject");

        // Create applicant and successful application
        Applicant applicant = new Applicant("AppA", "S0000002Y", "pw", 40, "Married");
        Application app = new Application(applicant, testProject, "2-Room");
        app.setStatus(Application.Status.SUCCESSFUL);
        ApplicationRegistry.addApplication(applicant.getNric(), app);

        // First booking
        officerController.assignFlatToApplicant(officer, applicant.getNric());
        assertTrue(outContent.toString().contains("Flat assigned successfully"));

        // Second booking
        outContent.reset();
        officerController.assignFlatToApplicant(officer, applicant.getNric());
        assertTrue(outContent.toString().contains(
                "No successful application found for this applicant in your project."));
    }

    // --- Scenario 10: Fail When Officer Applied as Applicant ---
    @Test
    void testReqToHandleProject_FailsWhenAppliedAsApplicant() {
        ApplicationRegistry.addApplication(
                officer.getNric(), new Application(officer, testProject, "2-Room")
        );
        boolean result = officerController.reqToHandleProject(officer, "TestProject");
        assertFalse(result);
        assertTrue(outContent.toString().contains("You have already applied for this project as an applicant."));
    }

    // --- Scenario 10: Fail When Active Registration on Other Project ---
    @Test
    void testReqToHandleProject_FailsWhenActiveRegistrationOnOtherProject() {
        Project other = new Project(
                "OtherProj", "Zone",
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(5),
                true, 1, "Mgr"
        );
        other.addFlatType("2-Room", 1, 30000);
        ProjectRegistry.loadProjects(List.of(testProject, other));

        officer.setRegistrationStatus("OtherProj", HDBOfficer.RegistrationStatus.APPROVED);
        officer.assignToProject("OtherProj");

        boolean result = officerController.reqToHandleProject(officer, "TestProject");
        assertFalse(result);
        assertTrue(outContent.toString().contains("You have an active officer registration"));
    }

    // --- Scenario 12: View Assigned Project Details When Visibility Off ---
    @Test
    void testViewAssignedProjectDetails_WhenVisibilityOff() {
        officer.assignToProject("TestProject");
        new ManagerController(new AuthController()).toggleProjectVisibility("TestProject");
        Project fetched = officerController.viewAssignedProjectDetails(officer);
        assertNotNull(fetched);
    }

    // --- Scenario 16: Receipt Generation for Flat Booking ---
    @Test
    void testGenerateReceipt_forBookedApplicant_printsReceipt() {
        officer.assignToProject("TestProject");
        Applicant applicant = new Applicant("AppB", "S0000003Z", "pw", 28, "Single");
        Application application = new Application(applicant, testProject, "2-Room");
        application.setStatus(Application.Status.BOOKED);
        ApplicationRegistry.addApplication(applicant.getNric(), application);

        outContent.reset();
        officerController.generateReceipt(officer, applicant.getNric());
        String receipt = outContent.toString();

        assertTrue(receipt.contains("===== Flat Booking Receipt ====="));
        assertTrue(receipt.contains("Applicant Name: AppB"));
        assertTrue(receipt.contains("NRIC: S0000003Z"));
        assertTrue(receipt.contains("Flat Type: 2-Room"));
        assertTrue(receipt.contains("Project: TestProject"));
    }

    // --- Scenario 13: Restriction on Editing Project Details ---
    @Test
    void testOfficerCannotEditProjectDetails() throws NoSuchMethodException {
        // OfficerController should not expose project-editing methods
        assertThrows(NoSuchMethodException.class, () -> {
            OfficerController.class.getMethod("updateNeighborhood", String.class, String.class, String.class);
        });
    }
}
