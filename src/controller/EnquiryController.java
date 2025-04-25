package controller;

import model.*;

import java.util.List;

/**
* Handles the submission, retrieval, editing, and replying of enquiries made by users.
* Enquiries are associated with specific projects and stored centrally in EnquiryRegistry.
*
* @author Javier 
* @version 1.0
*/
public class EnquiryController {
    /**
    * Submits a new enquiry for a specific project.
    *
    * @param senderNRIC   The NRIC of the user submitting the enquiry.
    * @param projectName  The name of the project the enquiry is about.
    * @param content      The enquiry content.
    */
    public void submitEnquiry(String senderNRIC, String projectName, String content) {
        Enquiry enquiry = new Enquiry(senderNRIC, projectName, content);
        EnquiryRegistry.addEnquiry(enquiry);
        System.out.println("Enquiry submitted.");
    }

    /**
    * Retrieves all enquiries submitted by a user.
    *
    * @param senderNRIC The NRIC of the user.
    * @return A list of the user's submitted enquiries.
    */
    public List<Enquiry> getEnquiriesByUser(String senderNRIC) {
        return EnquiryRegistry.getEnquiriesByUser(senderNRIC);
    }

    /**
    * Deletes a user's enquiry by ID if it belongs to them and is not yet replied to.
    *
    * @param id         The ID of the enquiry to delete.
    * @param senderNRIC The NRIC of the user requesting deletion.
    * @return True if deletion was successful, false otherwise.
    */
    public boolean deleteEnquiry(int id, String senderNRIC) {
        return EnquiryRegistry.deleteById(id, senderNRIC);
    }

    /**
    * Allows a staff member (officer or manager) to reply to an enquiry associated with a project they manage.
    *
    * @param enquiryId The ID of the enquiry to reply to.
    * @param replyText The reply content.
    * @param user      The staff user replying to the enquiry.
    * @return True if the reply was recorded successfully; false otherwise.
    */
    public boolean replyToEnquiry(int enquiryId, String replyText, User user) {
        Enquiry enquiry = EnquiryRegistry.getById(enquiryId);
        if (enquiry == null) return false;

        if (enquiry.hasReply() && !enquiry.getReply().isEmpty()) return false;

        String projectName = enquiry.getProjectName();

        if (user instanceof HDBOfficer officer) {
            if (officer.getAssignedProject() == null || 
                !officer.getAssignedProject().equals(projectName)) {
                System.out.println("You can only reply to enquiries for your assigned project.");
                return false;
            }
        }

        if (user instanceof HDBManager manager) {
            if (!manager.getManagedProjects().contains(projectName)) {
                System.out.println("You can only reply to enquiries for your assigned project.");
                return false;
            }
        }

        enquiry.reply(replyText);
        enquiry.setReplyBy(user.getName());

        return true;
    }

    /**
    * Updates an existing enquiry’s content, if it belongs to the user and has not been replied to.
    *
    * @param enquiryId   The ID of the enquiry to update.
    * @param newContent  The new enquiry content.
    * @param senderNRIC  The NRIC of the user making the update.
    * @return True if the enquiry was successfully updated; false otherwise.
    */
    public boolean updateEnquiry(int enquiryId, String newContent, String senderNRIC) {
        Enquiry enquiry = EnquiryRegistry.getById(enquiryId);
        if (enquiry == null) {
            System.out.println("Enquiry not found.");
            return false;
        }

        if (!enquiry.getSenderNRIC().equalsIgnoreCase(senderNRIC)) {
            System.out.println("You can only edit your own enquiries.");
            return false; // Not found or not the sender
        }

        if (enquiry.hasReply() && !enquiry.getReply().isEmpty()) {
            System.out.println("Your enquiry has already been replied to. Please submit a new enquiry.");
            return false;
        }

        enquiry.setContent(newContent);
        System.out.println("Enquiry successfully updated.");
        return true;
    }

    /**
    * Retrieves all enquiries submitted for a specific project.
    *
    * @param projectName The name of the project.
    * @return A list of enquiries related to the project.
    */
    public List<Enquiry> getProjectEnquiries(String projectName) {
        return EnquiryRegistry.getEnquiriesByProject(projectName);
    }
}
