package controller;

import model.*;

import java.util.List;

public class EnquiryController {

    public void submitEnquiry(String senderNRIC, String projectName, String content) {
        Enquiry enquiry = new Enquiry(senderNRIC, projectName, content);
        EnquiryRegistry.addEnquiry(enquiry);
        System.out.println("Enquiry submitted.");
    }

    public List<Enquiry> getEnquiriesByUser(String senderNRIC) {
        return EnquiryRegistry.getEnquiriesByUser(senderNRIC);
    }

    public boolean deleteEnquiry(int id, String senderNRIC) {
        return EnquiryRegistry.deleteById(id, senderNRIC);
    }

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
            if (manager.getAssignedProject() != null &&
                !manager.getAssignedProject().equals(projectName)) {
                System.out.println("You can only reply to enquiries for your assigned project.");
                return false;
            }
        }

        enquiry.reply(replyText);
        enquiry.setReplyBy(user.getName());

        return true;
    }

    public boolean updateEnquiry(int enquiryId, String newContent, String senderNRIC) {
        Enquiry enquiry = EnquiryRegistry.getById(enquiryId);
        if (enquiry == null) {
            System.out.println("Enquiry not found.");
            return false;
        }

        if (!enquiry.getSenderNRIC().equals(senderNRIC)) {
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

    public List<Enquiry> getProjectEnquiries(String projectName) {
        return EnquiryRegistry.getEnquiriesByProject(projectName);
    }
}
