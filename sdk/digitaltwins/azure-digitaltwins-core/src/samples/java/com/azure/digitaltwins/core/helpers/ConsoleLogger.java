package com.azure.digitaltwins.core.helpers;

public class ConsoleLogger {

    // Foreground colors
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_CYAN = "\u001B[36m";

    /**
     * Prints a header with a new line before and after the message. Use this to create different sections.
     * @param message Header message to be printed for the section.
     */
    public static void printHeader(String message) {
        System.out.println(ANSI_CYAN + "\n===" + message.toUpperCase() + "===\n" + ANSI_RESET);
    }

    /**
     * Prints a message to console. Use this for logging verbose.
     * @param message Message to be printed
     */
    public static void print(String message) {
        System.out.println(ANSI_BLUE + message + ANSI_RESET);
    }

    /**
     * Prints a message to console. Use this for logging successful events.
     * @param message Message to be printed
     */
    public static void printSuccess(String message){
        System.out.println(ANSI_GREEN + message + ANSI_RESET);
    }

    /**
     * Prints a message to console. Use this for logging warnings.
     * @param message Message to be printed
     */
    public static void printWarning(String message) {

        System.out.println(ANSI_YELLOW + message + ANSI_RESET);
    }

    /**
     * Prints a message to console. Use this for logging fatal failures.
     * @param message Message to be printed
     */
    public static void printFatal(String message) {
        System.out.println(ANSI_RED + message + ANSI_RESET);
    }
}
