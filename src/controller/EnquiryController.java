package controller;

import model.Enquiry;
import model.EnquiryRegistry;

import java.util.List;

public class EnquiryController {

    public void submitEnquiry(String senderNRIC, String projectName, String content) {
        Enquiry e = new Enquiry(senderNRIC, projectName, content);
        EnquiryRegistry.addEnquiry(e);
        System.out.println("✅ Enquiry submitted.");
    }

    public List<Enquiry> getUserEnquiries(String senderNRIC) {
        return EnquiryRegistry.getEnquiriesByUser(senderNRIC);
    }

    public boolean deleteEnquiry(int id, String senderNRIC) {
        return EnquiryRegistry.deleteById(id, senderNRIC);
    }

    public boolean replyToEnquiry(int enquiryId, String replyText) {
        Enquiry e = EnquiryRegistry.getById(enquiryId);
        if (e != null) {
            e.reply(replyText);
            return true;
        }
        return false;
    }

    public List<Enquiry> getProjectEnquiries(String projectName) {
        return EnquiryRegistry.getEnquiriesByProject(projectName);
    }
}
