package model;

import java.util.*;

public class EnquiryRegistry {
    private static final List<Enquiry> enquiryList = new ArrayList<>();

    public static void addEnquiry(Enquiry e) {
        enquiryList.add(e);
    }

    public static List<Enquiry> getAllEnquiries() {
        return new ArrayList<>(enquiryList);
    }

    public static List<Enquiry> getEnquiriesByUser(String nric) {
        List<Enquiry> result = new ArrayList<>();
        for (Enquiry e : enquiryList) {
            if (e.getSenderNRIC().equals(nric)) {
                result.add(e);
            }
        }
        return result;
    }

    public static List<Enquiry> getEnquiriesByProject(String projectName) {
        List<Enquiry> result = new ArrayList<>();
        for (Enquiry e : enquiryList) {
            if (e.getProjectName().equalsIgnoreCase(projectName)) {
                result.add(e);
            }
        }
        return result;
    }

    public static Enquiry getById(int id) {
        for (Enquiry e : enquiryList) {
            if (e.getEnquiryId() == id) return e;
        }
        return null;
    }

    public static boolean deleteById(int id, String senderNRIC) {
        Iterator<Enquiry> it = enquiryList.iterator();
        while (it.hasNext()) {
            Enquiry e = it.next();
            if (e.getEnquiryId() == id && e.getSenderNRIC().equals(senderNRIC)) {
                it.remove();
                return true;
            }
        }
        return false;
    }
}
