// src/test/java/controller/EnquiryControllerTest.java
package controller;

import model.*;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EnquiryControllerTest {
    private EnquiryController enquiryController;
    private Applicant applicant;
    private HDBOfficer officer;
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    void setUp() {
        EnquiryRegistry.loadEnquiries(List.of());

        enquiryController = new EnquiryController();

        applicant = new Applicant("App","S300001A","pw",30,"Married");
        officer   = new HDBOfficer("Off","S300002B","pw",35,"Single");
        officer.assignToProject("ProjA");

        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    // --- Scenario 9: Applicant’s Enquiries Management ---
    @Test
    void submitAndGetEnquiriesByUser() {
        enquiryController.submitEnquiry(applicant.getNric(),"ProjA","Hi?");
        assertTrue(outContent.toString().contains("Enquiry submitted"));

        List<Enquiry> list = enquiryController.getEnquiriesByUser(applicant.getNric());
        assertEquals(1, list.size());
        assertEquals("Hi?", list.get(0).getContent());
        outContent.reset();
    }

    // --- Scenario 9: Applicant’s Enquiries Management ---
    @Test
    void updateAndDeleteEnquiry() {
        enquiryController.submitEnquiry(applicant.getNric(),"ProjA","Orig");
        int id = EnquiryRegistry.getEnquiriesByUser(applicant.getNric()).get(0).getEnquiryId();
        outContent.reset();

        assertTrue(enquiryController.updateEnquiry(id,"New","S300001A"));
        assertTrue(outContent.toString().contains("Enquiry successfully updated"));
        assertEquals("New", EnquiryRegistry.getById(id).getContent());
        outContent.reset();

        assertTrue(enquiryController.deleteEnquiry(id,"S300001A"));
        assertNull(EnquiryRegistry.getById(id));
    }

    // --- Scenario 14: Response to Project Enquiries by Officer ---
    @Test
    void replyToEnquiry_and_invalidCases() {
        enquiryController.submitEnquiry(applicant.getNric(),"ProjA","Question");
        int id = EnquiryRegistry.getEnquiriesByUser(applicant.getNric()).get(0).getEnquiryId();
        outContent.reset();

        assertTrue(enquiryController.replyToEnquiry(id,"Answer", officer));
        assertEquals("Answer", EnquiryRegistry.getById(id).getReply());
        outContent.reset();

        assertFalse(enquiryController.replyToEnquiry(id,"Again", officer));
        Applicant someoneElse = new Applicant("X","S300003C","pw",28,"Single");
        assertFalse(enquiryController.replyToEnquiry(id,"X", someoneElse));
    }

    // --- Scenario 14: Response to Project Enquiries by Manager ---
    @Test
    void testManagerCanReplyToEnquiry_ForManagedProject() {
        // Applicant submits enquiry
        enquiryController.submitEnquiry(applicant.getNric(), "ProjA", "Need info");
        int id = EnquiryRegistry.getEnquiriesByUser(applicant.getNric()).get(0).getEnquiryId();

        // Manager set-up and reply
        HDBManager manager = new HDBManager("Mgr","S400001M","pw",45,"Married");
        manager.addManagedProject("ProjA");
        assertTrue(enquiryController.replyToEnquiry(id, "Here it is", manager));
        assertEquals("Here it is", EnquiryRegistry.getById(id).getReply());
    }

    // --- Scenario 14: Response to Project Enquiries by Manager ---
    @Test
    void testManagerCannotReplyIfNotManaging() {
        // Applicant submits enquiry
        enquiryController.submitEnquiry(applicant.getNric(), "ProjA", "Need info");
        int id = EnquiryRegistry.getEnquiriesByUser(applicant.getNric()).get(0).getEnquiryId();

        // Manager without managed project attempts reply
        HDBManager other = new HDBManager("Other","S400002N","pw",50,"Single");
        outContent.reset();
        assertFalse(enquiryController.replyToEnquiry(id, "Nope", other));
        assertTrue(outContent.toString().contains("You can only reply to enquiries for your assigned project."));
    }
}
