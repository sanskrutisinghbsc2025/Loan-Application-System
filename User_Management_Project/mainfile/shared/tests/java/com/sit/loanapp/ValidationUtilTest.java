package com.sit.loanapp;

import com.sit.loanapp.util.ValidationUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Validation Utility Unit Tests")
public class ValidationUtilTest {

    @Test
    @DisplayName("Blank String Validation")
    void testIsBlank() {
        assertTrue(ValidationUtil.isBlank(null));
        assertTrue(ValidationUtil.isBlank(""));
        assertTrue(ValidationUtil.isBlank("   "));
        assertFalse(ValidationUtil.isBlank("a"));
        assertFalse(ValidationUtil.isBlank(" valid "));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "amruta.patil@example.com",
        "user_name@domain.co.in",
        "john.doe+filter@sub.bank.org",
        "test123@xyz.net"
    })
    @DisplayName("Valid Email Formats")
    void testValidEmails(String email) {
        assertTrue(ValidationUtil.isValidEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "plainaddress",
        "@missingusername.com",
        "username@.com",
        "username@com",
        "user space@domain.com",
        ""
    })
    @DisplayName("Invalid Email Formats")
    void testInvalidEmails(String email) {
        assertFalse(ValidationUtil.isValidEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "9876543210",
        "9123456780",
        "8000000001",
        "7654321098"
    })
    @DisplayName("Valid 10-Digit Mobile Numbers")
    void testValidMobile(String mobile) {
        assertTrue(ValidationUtil.isValidMobile(mobile));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "12345",         // Too short
        "98765432101",   // 11 digits (too long)
        "987654321a",   // Contains letters
        "abcdefghij",   // Non-numeric
        ""              // Empty
    })
    @DisplayName("Invalid Mobile Numbers")
    void testInvalidMobile(String mobile) {
        assertFalse(ValidationUtil.isValidMobile(mobile));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Password@123",
        "Secure#2024",
        "Admin$99!",
        "Complex_Pass1"
    })
    @DisplayName("Valid Password Policy Enforcement")
    void testValidPasswords(String password) {
        assertTrue(ValidationUtil.isValidPassword(password));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Pass@1",          // Under 8 characters
        "password@123",    // Missing uppercase
        "PASSWORD@123",    // Missing lowercase
        "Password@",       // Missing digit
        "Password1234",    // Missing special character
        ""
    })
    @DisplayName("Invalid Passwords Violating Policy")
    void testInvalidPasswords(String password) {
        assertFalse(ValidationUtil.isValidPassword(password));
    }
}
