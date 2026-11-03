package com.oop.project.ui;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

public final class Test {
    public static void main(String[] args) {
        var output = new ByteArrayOutputStream();
        var input = "bad\n8\n1\n2026-02-30\n8\n1\n2026-02-01\n-2\n0\n";
        new ConsoleApp().run(new Scanner(input), new PrintStream(output));

        String log = output.toString();
        if (!log.contains("Choose a menu number")
                || !log.contains("Enter a valid date")
                || !log.contains("Measurement cannot be negative")) {
            throw new AssertionError(log);
        }

        System.out.println("CLI validation and recovery passed.");
    }
}
