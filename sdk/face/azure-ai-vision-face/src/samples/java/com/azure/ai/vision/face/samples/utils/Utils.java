// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.samples.utils;

import com.azure.core.util.BinaryData;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.function.Supplier;

public final class Utils {
    private static final SimpleDateFormat LOG_DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS");

    private Utils() {}

    public static void log(String str) {
        Date dt = new Date();
        String message = String.format("%s %d %s", LOG_DATE_FORMAT.format(dt), Thread.currentThread().getId(), str);
        System.out.println(message);
    }

    public static void pressAnyKeyToContinue(String message) {
        log(message);
        try (Scanner scanner = new Scanner(System.in)) {
            scanner.nextLine();
        }
    }

    public static void logObject(String message, Object object, boolean indentOutput) {
        log(message + toString(object, indentOutput));
    }


    public static void logObject(String message, Object object) {
        log(message + toString(object));
    }

    public static void logObject(Object object) {
        log(toString(object));
    }

    public static String toString(Object object) {
        return toString(object, false);
    }

    public static String toString(Object object, boolean indentOutput) {
        return BinaryData.fromObject(object).toString();
    }

    public static BinaryData loadFromFile(String pathString) {
        Path path = Paths.get(pathString);
        return BinaryData.fromFile(path);
    }

    public static <T> T safelyRun(Supplier<T> suppler) {
        return safelyRunWithExceptionCheck(new RuntimeException[1], "running operation", suppler);
    }

    public static void safelyRunWithExceptionCheck(
            RuntimeException[] exceptionContainer, String operation, Runnable  runnable) {
        safelyRunWithExceptionCheck(exceptionContainer, operation, () -> {
            runnable.run();
            return null;
        });
    }


    public static <T> T safelyRunWithExceptionCheck(
            RuntimeException[] exceptionContainer, String operation, Supplier<T> supplier) {
        Exception exceptions = exceptionContainer[0];
        if (exceptions != null) {
            return null;
        }

        try {
            return supplier.get();
        } catch (RuntimeException ex) {
            log("Error when " + operation);
            ex.printStackTrace();
            exceptionContainer[0] = ex;
        }

        return null;
    }

    public static void removeLastComma(StringBuilder sb) {
        int lastIndex = sb.length() - 1;

        if (lastIndex >= 0 && sb.charAt(lastIndex) == ',') {
            sb.deleteCharAt(lastIndex);
        }
    }
}
