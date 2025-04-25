package model;

/**
* Represents an enquiry submitted by an applicant regarding a specific BTO project.
* 
* Enquiries include a message, sender information, and optional replies from officers or managers.
* Each enquiry is assigned a unique ID for reference.
* 
* Used by both applicants and staff during the enquiry management process.
* 
* @author Javier
* @version 1.0
*/
public class Enquiry {
    private int enquiryId;
    private String projectName;
    private String senderNRIC;
    private String content;
    private String reply;
    private String replyBy;

    /**
    * Constructs a new enquiry for the given project and applicant.
    *
    * @param senderNRIC The NRIC of the applicant submitting the enquiry.
    * @param projectName The name of the project being enquired about.
    * @param content The message or question submitted by the applicant.
    */
    public Enquiry(String senderNRIC, String projectName, String content) {
        this.senderNRIC = senderNRIC;
        this.projectName = projectName;
        this.content = content;
        this.reply = null; // no reply yet
        this.replyBy = null; // no reply yet
    }

    /**
    * Returns the unique ID of this enquiry.
    *
    * @return The enquiry ID.
    */
    public int getEnquiryId() {
        return enquiryId;
    }

    /**
    * Sets the ID for this enquiry (used during loading).
    *
    * @param id The ID to assign.
    */
    public void setEnquiryId(int id) {
        this.enquiryId = id;
    }

    /**
    * Returns the NRIC of the applicant who submitted the enquiry.
    *
    * @return The sender's NRIC.
    */
    public String getSenderNRIC() {
        return senderNRIC;
    }

    /**
    * Returns the name of the project the enquiry is about.
    *
    * @return The project name.
    */
    public String getProjectName() {
        return projectName;
    }

    /**
    * Returns the content of the enquiry.
    *
    * @return The enquiry message.
    */
    public String getContent() {
        return content;
    }

    /**
    * Updates the content of the enquiry.
    *
    * @param newContent The new enquiry message.
    */
    public void setContent(String newContent) {
        this.content = newContent;
    }

    /**
    * Checks if this enquiry has been replied to.
    *
    * @return True if a reply exists, false otherwise.
    */
    public boolean hasReply() {
        return reply != null;
    }

    /**
    * Returns the reply content.
    *
    * @return The reply message, or null if none exists.
    */
    public String getReply() {
        return reply;
    }

    /**
    * Returns the name of the staff member who replied.
    *
    * @return The name of the replier, or null if none exists.
    */
    public String getReplyBy() {
        return replyBy;
    }

    /**
    * Sets the name of the staff member who replied.
    *
    * @param staffName The staff member's name.
    */
    public void setReplyBy(String staffName) {
        this.replyBy = staffName;
    }

    /**
    * Records a reply to this enquiry.
    *
    * @param replyContent The reply message to store.
    */
    public void reply(String replyContent) {
        this.reply = replyContent;
    }
}
