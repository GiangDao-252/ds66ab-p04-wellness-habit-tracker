package com.oop.project.ui;

import com.oop.project.util.InputValidator;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Scanner;

final class ConsoleInput {
    private final Scanner input;
    private final PrintStream output;

    ConsoleInput(Scanner input, PrintStream output) {
        this.input = input;
        this.output = output;
    }

    String read(String prompt) {
        output.print(prompt);
        return input.nextLine().strip();
    }

    long id(String prompt) { return InputValidator.id(read(prompt)); }
    LocalDate date(String prompt) { return InputValidator.date(read(prompt)); }
    BigDecimal measurement(String prompt) { return InputValidator.measurement(read(prompt)); }
}
