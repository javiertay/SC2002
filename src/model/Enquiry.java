package model;

public class Enquiry {
    private static int nextId = 1; // for unique IDs (optional)

    private int enquiryId;
    private String projectName;
    private String senderNRIC;
    private String content;
    private String reply;

    public Enquiry(String senderNRIC, String projectName, String content) {
        this.enquiryId = nextId++;
        this.senderNRIC = senderNRIC;
        this.projectName = projectName;
        this.content = content;
        this.reply = null; // no reply yet
    }

    public int getEnquiryId() {
        return enquiryId;
    }

    public String getSenderNRIC() {
        return senderNRIC;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String newContent) {
        this.content = newContent;
    }

    public boolean hasReply() {
        return reply != null;
    }

    public String getReply() {
        return reply;
    }

    public void reply(String replyContent) {
        this.reply = replyContent;
    }

    @Override
    public String toString() {
        return "Enquiry ID: " + enquiryId +
               "\nProject: " + projectName +
               "\nFrom: " + senderNRIC +
               "\nContent: " + content +
               "\nReply: " + (reply == null ? "No reply yet" : reply);
    }
}
