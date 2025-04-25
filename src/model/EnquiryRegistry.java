package model;

import java.util.*;

/**
* Registry for storing and retrieving all enquiries submitted by applicants.
* 
* Provides static methods to manage the enquiry lifecycle including loading from file,
* adding new enquiries, searching by applicant or project, and deletion.
* 
* Data is stored in memory via a static list and intended to be loaded once at startup.
* 
* @author Javier
* @version 1.0
*/
public class EnquiryRegistry {
    private static final List<Enquiry> enquiryList = new ArrayList<>();

    /**
    * Loads a list of enquiries into the registry at application startup.
    * Clears any existing entries before adding the new ones.
    *
    * @param enquiries The list of enquiries to load.
    */
    public static void loadEnquiries(List<Enquiry> enquiries) {
        enquiryList.clear();
        for (Enquiry enquiry : enquiries) {
            enquiryList.add(enquiry);
        }
    }

    /**
    * Adds a new enquiry to the registry.
    *
    * @param e The enquiry to add.
    */
    public static void addEnquiry(Enquiry e) {
        e.setEnquiryId(enquiryList.size() + 1); // set sequential id
        enquiryList.add(e);
    }

    /**
    * Retrieves all enquiries currently stored in the registry.
    *
    * @return A new list containing all enquiries.
    */
    public static List<Enquiry> getAllEnquiries() {
        return new ArrayList<>(enquiryList);
    }

    /**
    * Retrieves all enquiries submitted by a specific user.
    *
    * @param nric The NRIC of the applicant.
    * @return A list of enquiries made by the applicant.
    */
    public static List<Enquiry> getEnquiriesByUser(String nric) {
        List<Enquiry> result = new ArrayList<>();
        for (Enquiry e : enquiryList) {
            if (e.getSenderNRIC().equals(nric)) {
                result.add(e);
            }
        }
        return result;
    }

    /**
    * Retrieves all enquiries submitted for a specific project.
    *
    * @param projectName The name of the project.
    * @return A list of enquiries about the given project.
    */
    public static List<Enquiry> getEnquiriesByProject(String projectName) {
        List<Enquiry> result = new ArrayList<>();
        for (Enquiry e : enquiryList) {
            if (e.getProjectName().equalsIgnoreCase(projectName)) {
                result.add(e);
            }
        }
        return result;
    }

    /**
    * Retrieves an enquiry by its unique ID.
    *
    * @param id The ID of the enquiry.
    * @return The matching enquiry, or null if not found.
    */
    public static Enquiry getById(int id) {
        for (Enquiry e : enquiryList) {
            if (e.getEnquiryId() == id) return e;
        }
        return null;
    }

    /**
    * Deletes an enquiry by its ID if it belongs to the specified sender.
    *
    * @param id The ID of the enquiry to delete.
    * @param senderNRIC The NRIC of the sender for validation.
    * @return True if the enquiry was found and deleted; false otherwise.
    */
    public static boolean deleteById(int id, String senderNRIC) {
        boolean deleted = false;

        // Delete the matching enquiry
        Iterator<Enquiry> it = enquiryList.iterator();
        while (it.hasNext()) {
            Enquiry e = it.next();
            if (e.getEnquiryId() == id && e.getSenderNRIC().equals(senderNRIC)) {
                it.remove();
                deleted = true;
                break;
            }
        }

        // Shift enquiry IDs if deletion happened
        if (deleted) {
            int newId = 1;
            for (Enquiry e : enquiryList) {
                e.setEnquiryId(newId++); // update IDs to be sequential
            }
        }

        return deleted;
    }
}
