package com.oop.project;

import com.oop.project.ui.ConsoleApp;
import java.util.Scanner;

public final class Main {
    public static void main(String[] args) {
        new ConsoleApp().run(new Scanner(System.in), System.out);
    }
}
