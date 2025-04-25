// src/test/java/controller/ApplicationControllerTest.java
package controller;

import model.*;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationControllerTest {
    private ApplicationController appController;
    private Project projA, projB;
    private Applicant single35, married30;
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    void setUp() {
        // Clear registries
        ProjectRegistry.loadProjects(List.of());
        ApplicationRegistry.loadApplications(List.of());

        appController = new ApplicationController();

        // Seed projects
        projA = new Project("ProjA","Yishun",
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(1),
                true, 5, "Mgr"
        );
        projA.addFlatType("2-Room", 2, 50_000);

        projB = new Project("ProjB","BoonLay",
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(1),
                true, 5, "Mgr"
        );
        projB.addFlatType("3-Room", 3, 80_000);

        ProjectRegistry.loadProjects(List.of(projA, projB));

        single35   = new Applicant("Sng35","S100001A","pw",35,"Single");
        married30  = new Applicant("Mrd30","S100002B","pw",30,"Married");

        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    // --- Scenario 5: Project Visibility Based on User Group and Toggle ---
    @Test
    void getAllAvailableProjects_filtersCorrectly() {
        var list1 = appController.getAllAvailableProjects(single35);
        assertEquals(1, list1.size());
        assertEquals("ProjA", list1.get(0).getName());

        var list2 = appController.getAllAvailableProjects(married30);
        assertEquals(2, list2.size());
    }

    // --- Scenario 6: Project Application ---
    @Test
    void submitApplication_and_viewApplicationStatus_outputsTable() {
        assertTrue(appController.submitApplication(married30, "ProjB","3-Room"));

        var apps = ApplicationRegistry.getApplicationByNRIC("S100002B");
        assertEquals(1, apps.size());
        assertEquals(Application.Status.PENDING, apps.get(0).getStatus());

        appController.viewApplicationStatus(married30);
        String out = outContent.toString();
        assertTrue(out.contains("PENDING"));
    }

    // --- Scenario 22: Withdrawal Request Flows ---
    @Test
    void reqToWithdrawApp_variousFlows() {
        assertFalse(appController.reqToWithdrawApp(single35));
        assertTrue(outContent.toString().contains("No application found"));
        outContent.reset();

        assertTrue(appController.submitApplication(married30, "ProjB","3-Room"));
        assertTrue(appController.reqToWithdrawApp(married30));
        assertTrue(outContent.toString().contains("Withdrawal request submitted"));
        outContent.reset();

        assertFalse(appController.reqToWithdrawApp(married30));
        assertTrue(outContent.toString().contains("already requested"));
    }

    // --- Scenario 7: Viewing Application Status after Visibility Toggle Off ---
    @Test
    void viewApplicationStatus_afterVisibilityToggleOff_showsClosed() {
        // Submit an application for married30 to projA
        assertTrue(appController.submitApplication(married30, "ProjA", "2-Room"));
        // Toggle visibility off
        new ManagerController(new AuthController()).toggleProjectVisibility("ProjA");

        // View status again
        outContent.reset();
        appController.viewApplicationStatus(married30);
        String printed = outContent.toString();
        assertTrue(printed.contains("Closed"),
                "After toggling off, the status column should show 'Closed'.");
    }

    // --- Scenario 22: Approve or Reject BTO Applications and Withdrawals ---
    @Test
    void testApproveRejectWithdrawal_SuccessAndFailure() {
        // 1) Submit application and request withdrawal
        assertTrue(appController.submitApplication(married30, "ProjA", "2-Room"));
        assertTrue(appController.reqToWithdrawApp(married30));
        outContent.reset();

        // 2) Manager setup and approval
        HDBManager mgr = new HDBManager("Mgr","S200009Z","pw",40,"Married");
        mgr.addManagedProject("ProjA");
        mgr.assignToProject("ProjA");
        boolean approved = appController.approveWithdrawal(mgr, married30.getNric());
        assertTrue(approved, "Manager should approve withdrawal");
        assertTrue(outContent.toString().contains("Application withdrawn successfully."));

        // 3) Prepare new withdrawal request
        assertTrue(appController.submitApplication(married30, "ProjA", "2-Room"));
        assertTrue(appController.reqToWithdrawApp(married30));
        outContent.reset();

        // 4) Test rejection
        boolean rejected = appController.rejectWithdrawal(mgr, married30.getNric());
        assertTrue(rejected, "Manager should reject withdrawal");
        assertTrue(outContent.toString().contains("Withdrawal request rejected."));
    }
    // --- New: Submit to non-existent project ---
    @Test
    void submitApplication_nonexistentProject_fails() {
        boolean ok = appController.submitApplication(married30, "NoSuchProj", "2-Room");
        assertFalse(ok);
        assertTrue(outContent.toString().contains("Project not found."));
    }

    // --- New: Submit with invalid flat type ---
    @Test
    void submitApplication_invalidFlatType_fails() {
        assertFalse(appController.submitApplication(married30, "ProjB", "5-Room"));
        assertTrue(outContent.toString().contains("Flat type does not exist in this project."));
    }

    // --- New: Duplicate application ---
    @Test
    void submitApplication_duplicateApplication_fails() {
        assertTrue(appController.submitApplication(married30, "ProjA", "2-Room"));
        outContent.reset();
        // second apply without withdraw/reject
        boolean ok2 = appController.submitApplication(married30, "ProjA", "2-Room");
        assertFalse(ok2);
        assertTrue(outContent.toString().contains(
                "You already have an active application. Withdraw or wait for rejection to reapply."));
    }

    // --- New: Apply when visibility is off ---
    @Test
    void submitApplication_visibilityOff_fails() {
        ProjectRegistry.getProjectByName("ProjA").setVisibility(false);
        assertFalse(appController.submitApplication(married30, "ProjA", "2-Room"));
        assertTrue(outContent.toString().contains("This project is no longer visible to applicants."));
    }

    // --- New: Ineligible applicant (under-age or wrong status) ---
    @Test
    void submitApplication_ineligibleApplicant_fails() {
        // single, age 30 < 35, flatType "2-Room"
        Applicant youngSingle = new Applicant("Yng","S100010X","pw",30,"Single");
        assertFalse(appController.submitApplication(youngSingle, "ProjA", "2-Room"));
        assertTrue(outContent.toString().contains("You are not eligible to apply for this flat type."));
    }
}
