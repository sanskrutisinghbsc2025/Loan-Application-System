package com.sit.loanapp.ui;

import java.util.Scanner;

/**
 * Robust console input reader wrapping java.util.Scanner.
 * Prevents newline-skipping bugs and provides validation loops.
 */
public class InputReader {

    private final Scanner scanner;

    public InputReader(Scanner scanner) {
        this.scanner = scanner;
    }

    public String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public String readMandatoryString(String prompt, String fieldName) {
        while (true) {
            String val = readLine(prompt);
            if (!val.isEmpty()) {
                return val;
            }
            System.out.println(">> Error: " + fieldName + " is mandatory. Please enter a value.");
        }
    }

    public int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(input);
                if (val >= min && val <= max) {
                    return val;
                }
                System.out.println(">> Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println(">> Invalid number input. Please enter a valid integer.");
            }
        }
    }
}
