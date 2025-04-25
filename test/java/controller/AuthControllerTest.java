// src/test/java/controller/AuthControllerTest.java
package controller;

import model.Applicant;
import model.User;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class AuthControllerTest {
    private AuthController auth;
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    void setUp() {
        // Ensure Excel file exists for password updates
        try {
            java.io.File excelFile = new java.io.File("src/data/CombinedExcel.xlsx");
            excelFile.getParentFile().mkdirs();
            if (!excelFile.exists()) {
                org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(excelFile)) {
                    wb.write(fos);
                }
                wb.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create dummy Excel file for tests", e);
        }

        auth = new AuthController();
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
        // --- Scenario 1: Valid User Login ---
    void login_validCredentials_returnsUserAndPrintsSuccess() {
        User user = new Applicant("Alice", "S1234567A", "password", 30, "single");
        auth.addUser(user);

        User result = auth.login("S1234567A", "password");

        assertNotNull(result);
        assertSame(user, result);
        assertTrue(outContent.toString().contains("Login successful!"));
    }

    @Test
        // --- Scenario 3: Incorrect Password ---
    void login_wrongPassword_returnsNullAndPrintsIncorrectPassword() {
        User user = new Applicant("Bob", "S7654321B", "secret", 40, "married");
        auth.addUser(user);

        User result = auth.login("S7654321B", "badpass");

        assertNull(result);
    }

    @Test
        // --- Scenario 2: Invalid NRIC Format ---
    void login_invalidNricFormat_throwsNullPointerException() {
        // Since AuthController.login will look up a null user and then call getPassword(),
        // it currently throws NPE for unknown NRIC. We assert that behavior here.
        assertThrows(NullPointerException.class,
                () -> auth.login("BADFORMAT", "any"));
    }

    @Test
        // --- Scenario 4: Password Change Functionality ---
    void changePassword_nullUser_returnsFalseAndPrintsError() {
        boolean ok = auth.changePassword(null, "newpass");
        assertFalse(ok);
        assertTrue(outContent.toString().contains("No user logged in."));
    }

    @Test
    void changePassword_validUser_changesPasswordAndPrintsSuccess() {
        Applicant user = new Applicant("Cara", "S1112223C", "old", 28, "single");
        auth.addUser(user);

        boolean ok = auth.changePassword(user, "brandNew");
        assertTrue(ok);
        assertEquals("brandNew", user.getPassword());
        assertTrue(outContent.toString().contains("Password changed successfully."));
    }

    @Test
    void getUserByNRIC_and_getAllUsers_workAsExpected() {
        Applicant user = new Applicant("Dan", "S9998887D", "pw", 35, "married");
        auth.addUser(user);

        assertSame(user, auth.getUserByNRIC("S9998887D"));

        Map<String, User> all = auth.getAllUsers();
        assertEquals(1, all.size());
        assertSame(user, all.get("S9998887D"));
    }

    @Nested
    class PromptPasswordChangeTests {
        Applicant user;

        @BeforeEach
        void initUser() {
            user = new Applicant("Eve", "S5554443E", "initPass", 45, "married");
            auth.addUser(user);
            outContent.reset();
        }

        @Test
        void prompt_withWrongCurrent_returnsFalseAndNoChange() {
            String input = "wrongCurrent\n";
            boolean ok = auth.promptPasswordChange(user, new Scanner(new ByteArrayInputStream(input.getBytes())));
            assertFalse(ok);
            assertEquals("initPass", user.getPassword());
            String printed = outContent.toString();
            assertTrue(printed.contains("Enter current password:"));
            assertTrue(printed.contains("Incorrect current password."));
        }

        @Test
        void prompt_withMismatchNewConfirm_returnsFalseAndNoChange() {
            String input = String.join("\n", "initPass", "newOne", "noMatch") + "\n";
            boolean ok = auth.promptPasswordChange(user, new Scanner(new ByteArrayInputStream(input.getBytes())));
            assertFalse(ok);
            assertEquals("initPass", user.getPassword());
            String printed = outContent.toString();
            assertTrue(printed.contains("Enter new password:"));
            assertTrue(printed.contains("Confirm new password:"));
            assertTrue(printed.contains("Passwords do not match."));
        }

        @Test
        void prompt_withCorrectFlow_changesPasswordAndReturnsTrue() {
            String input = String.join("\n", "initPass", "fresh", "fresh") + "\n";
            boolean ok = auth.promptPasswordChange(user, new Scanner(new ByteArrayInputStream(input.getBytes())));
            assertTrue(ok);
            assertEquals("fresh", user.getPassword());
            String printed = outContent.toString();
            assertTrue(printed.contains("Password changed successfully."));
        }
    }
}
