package util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

import model.Applicant;
import model.Enquiry;
import model.FlatType;
import model.Project;

public class TableUtil {
    private static final Pattern ANSI_PATTERN = Pattern.compile("\u001B\\[[;\\d]*m");

    private static String stripAnsi(String input) {
        return ANSI_PATTERN.matcher(input).replaceAll("");
    }

    public static void printTable(List<String> headers, List<List<String>> rows) {
        final int pageSize = 5;
        int totalPages = (int) Math.ceil((double) rows.size() / pageSize);
        int currentPage = 0;
        Scanner scanner = new Scanner(System.in);

        int[] colWidths = new int[headers.size()];
        for (int i = 0; i < headers.size(); i++) {
            colWidths[i] = stripAnsi(headers.get(i)).length();
        }

        for (List<String> row : rows) {
            for (int i = 0; i < row.size(); i++) {
                colWidths[i] = Math.max(colWidths[i], stripAnsi(row.get(i)).length());
            }
        }

        StringBuilder formatBuilder = new StringBuilder();
        for (int width : colWidths) {
            formatBuilder.append("| %-").append(width).append("s ");
        }
        formatBuilder.append("|\n");
        String format = formatBuilder.toString();

        while (true) {
            int start = currentPage * pageSize;
            int end = Math.min(start + pageSize, rows.size());
            List<List<String>> pageRows = rows.subList(start, end);

            System.out.printf("\nPage %d of %d\n", currentPage + 1, totalPages);
            System.out.printf(format, headers.toArray()); // header

            for (int width : colWidths) {
                System.out.print("|" + "-".repeat(width + 2));
            }
            System.out.println("|");

            for (List<String> row : pageRows) {
                Object[] formattedRow = new Object[row.size()];
                for (int i = 0; i < row.size(); i++) {
                    String cell = row.get(i);
                    int pad = colWidths[i] - stripAnsi(cell).length();
                    formattedRow[i] = cell + " ".repeat(Math.max(0, pad));
                }
                System.out.printf(format, formattedRow);
            }

            if (totalPages == 1) break;

            System.out.print("\n[N]ext, [P]rev, [Q]uit: ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equalsIgnoreCase("n") && currentPage < totalPages - 1) {
                currentPage++;
            } else if (input.equalsIgnoreCase("p") && currentPage > 0) {
                currentPage--;
            } else if (input.equalsIgnoreCase("q")) {
                scanner.close();
                break;
            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    public static void printProjectTable(List<Project> projects, Applicant applicant) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
        List<String> headers = List.of("Project Name", "Neighborhood", "Open Date", "Close Date", "Flat Breakdown", "Project Status");
        List<List<String>> rows = new ArrayList<>();

        for (Project p : projects) {
            LocalDate openDate = p.getOpenDate();
            LocalDate closeDate = p.getCloseDate();
            
            if (LocalDate.now().isAfter(closeDate)) continue;

            String status = LocalDate.now().isBefore(openDate) ? "Upcoming" : "Opened";

            Map<String, FlatType> flatTypes = p.getFlatTypes();
            List<String> eligibleTypes = new ArrayList<>();

            for (Map.Entry<String, FlatType> entry : flatTypes.entrySet()) {
                String type = entry.getKey();

                // Inline eligibility check: If single & >= 35, only show 2-Room
                if (applicant != null &&
                    applicant.getMaritalStatus().equalsIgnoreCase("single") &&
                    applicant.getAge() >= 35 &&
                    !type.equalsIgnoreCase("2-Room")) {
                    continue; // Skip all except 2-Room
                }

                FlatType ft = entry.getValue();
                if (ft.getRemainingUnits() == 0) continue;
                eligibleTypes.add(type + ": " + ft.getRemainingUnits() + " left at $" + ft.getPrice() + " each");
            }

            if (eligibleTypes.isEmpty()) continue;

            String color = status.equals("Opened") ? "\u001B[32m" : "";
            String reset = color.isEmpty() ? "" : "\u001B[0m";

            rows.add(List.of(
                color + p.getName() + reset,
                color + p.getNeighborhood() + reset,
                color + openDate.format(formatter) + reset,
                color + closeDate.format(formatter) + reset,
                color + String.join(", ", eligibleTypes) + reset,
                color + status + reset
            ));
        }

        printTable(headers, rows); // Assuming this is your generic method
    }

    public static void printEnquiryTable(List<Enquiry> enquiries) {
        if (enquiries == null || enquiries.isEmpty()) {
            System.out.println("No enquiries to display.");
            return;
        }

        List<String> headers = List.of("ID", "Sender NRIC", "Project Name", "Message", "Reply", "Replied By");
        List<List<String>> rows = new ArrayList<>();

        for (Enquiry e : enquiries) {
            String msg = e.getContent().length() > 30
                ? e.getContent().substring(0, 27) + "..."
                : e.getContent();

            String reply = e.getReply() != null ? e.getReply() : "(no reply)";
            if (reply.length() > 30) {
                reply = reply.substring(0, 27) + "...";
            }

            String repliedBy = e.getReplyBy() != null ? e.getReplyBy() : "-";

            rows.add(List.of(
                String.valueOf(e.getEnquiryId()),
                e.getSenderNRIC(),
                e.getProjectName(),
                msg,
                reply,
                repliedBy
            ));
        }

        TableUtil.printTable(headers, rows);
    }
}
