package com.azure.digitaltwins.core.helpers;

public class ConsoleLogger {

    // Foreground colors
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_CYAN = "\u001B[36m";

    public static void PrintHeader(String message) {
        System.out.println(ANSI_CYAN + "\n===" + message.toUpperCase() + "===\n" + ANSI_RESET);
    }

    public static void PrintSuccess(String message) {
        System.out.println(ANSI_GREEN + message + ANSI_RESET);
    }

    public static void PrintWarning(String message) {
        System.out.println(ANSI_YELLOW + message + ANSI_RESET);
    }

    public static void PrintFatal(String message) {
        System.out.println(ANSI_RED + message + ANSI_RESET);
    }
}
