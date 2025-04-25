// src/test/java/controller/ManagerControllerTest.java
package controller;

import model.*;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ManagerControllerTest {
    private AuthController authController;
    private ManagerController managerController;
    private HDBManager manager;
    private Project testProject;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        // capture System.out
        System.setOut(new PrintStream(outContent));

        // reset project registry
        ProjectRegistry.loadProjects(List.of());

        // wire up controllers
        authController    = new AuthController();
        managerController = new ManagerController(authController);

        // create and register a manager
        manager = new HDBManager("Manager A", "S1111111A", "pw", 35, "Married");
        authController.addUser(manager);

        // prepare a test project
        testProject = new Project(
                "TestProject",
                "Bukit Timah",
                LocalDate.now().minusDays(2),
                LocalDate.now().plusDays(2),
                true,
                2,
                manager.getName()
        );
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    // --- Scenario 17: Create BTO Project Listings ---
    @Test
    void testCreateProject_Success() {
        managerController.createProject(testProject, manager);

        assertTrue(ProjectRegistry.exists("TestProject"));
        assertEquals("TestProject", manager.getAssignedProject());
        assertTrue(outContent.toString().contains("Project created successfully."));
    }

    // --- Scenario 17: Create BTO Project Listings (Duplicate) ---
    @Test
    void testCreateProject_DuplicateName() {
        ProjectRegistry.addProject(testProject);
        manager.assignToProject("AnotherProject");

        outContent.reset();
        managerController.createProject(testProject, manager);
        assertTrue(outContent.toString().contains("Project with this name already exists."));
    }

    // --- Scenario 17: Edit BTO Project Listings (Neighborhood) ---
    @Test
    void testUpdateNeighborhood_Success() {
        ProjectRegistry.addProject(testProject);
        manager.addManagedProject("TestProject");

        boolean ok = managerController.updateNeighborhood(manager, "TestProject", "NewHood");
        assertTrue(ok);
        assertEquals("NewHood", ProjectRegistry.getProjectByName("TestProject").getNeighborhood());
    }

    // --- Scenario 17: Edit BTO Project Listings (Unauthorized) ---
    @Test
    void testUpdateNeighborhood_Unauthorized() {
        ProjectRegistry.addProject(testProject);
        assertFalse(managerController.updateNeighborhood(manager, "TestProject", "X"));
    }

    // --- Scenario 17: Delete BTO Project Listings ---
    @Test
    void testDeleteProject_Success() {
        ProjectRegistry.addProject(testProject);
        manager.addManagedProject("TestProject");
        manager.assignToProject("TestProject");

        outContent.reset();
        managerController.deleteProject(manager, "TestProject");

        assertFalse(ProjectRegistry.exists("TestProject"));
        assertNull(manager.getAssignedProject());
        assertTrue(outContent.toString().contains("Project deleted."));
    }

    // --- Scenario 17: Delete BTO Project Listings (Not Managing) ---
    @Test
    void testDeleteProject_NotManaging() {
        ProjectRegistry.addProject(testProject);
        outContent.reset();

        managerController.deleteProject(manager, "TestProject");
        assertTrue(outContent.toString().contains("You cannot delete a project that you are not managing."));
    }

    // --- Scenario 17: Edit & Delete BTO Project Listings (All Fields) ---
    @Test
    void testEditProjectDetails_AllFields() {
        // create project and register manager
        managerController.createProject(testProject, manager);
        manager.addManagedProject("TestProject");

        // update neighborhood
        assertTrue(managerController.updateNeighborhood(manager, "TestProject", "NewHood"));
        assertEquals("NewHood",
                ProjectRegistry.getProjectByName("TestProject").getNeighborhood());

        // update open/close dates
        LocalDate newOpen  = LocalDate.now().minusDays(3);
        LocalDate newClose = LocalDate.now().plusDays(8);
        assertTrue(managerController.updateOpenDate(manager, "TestProject", newOpen));
        assertTrue(managerController.updateCloseDate(manager, "TestProject", newClose));
        assertEquals(newOpen,
                ProjectRegistry.getProjectByName("TestProject").getOpenDate());
        assertEquals(newClose,
                ProjectRegistry.getProjectByName("TestProject").getCloseDate());

        // add and update flat units
        Project proj = ProjectRegistry.getProjectByName("TestProject");
        proj.addFlatType("3-Room", 4, 75000);
        assertTrue(managerController.updateFlatUnits(manager, "TestProject", "3-Room", 4, 75000));
        assertEquals(4,
                proj.getFlatType("3-Room").getTotalUnits());

        // update officer slots
        assertTrue(managerController.updateOfficerSlots(manager, "TestProject", 5));
        assertEquals(5,
                ProjectRegistry.getProjectByName("TestProject").getMaxOfficerSlots());
    }

    // --- Scenario 18: Single Project Management per Application Period ---
    @Test
    void testCreateProject_FailsWhenOverlapDates() {
        // create first project
        managerController.createProject(testProject, manager);

        // attempt creating a second overlapping project
        Project other = new Project(
                "OtherProj", "Somewhere",
                LocalDate.now().minusDays(2), LocalDate.now().plusDays(2),
                true, 1, manager.getName()
        );
        outContent.reset();
        managerController.createProject(other, manager);
        String output = outContent.toString();
        assertTrue(output.contains("overlaps with these dates"),
                "Should block creating a second project in the same period");
    }
}
