// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class StackTraceUtil {
    private StackTraceUtil() {}

    /** Returns the current thread's call stack as a string (one frame per line). */
    public static String currentCallStack() {
        return currentCallStack(0, Integer.MAX_VALUE, true);
    }

    /**
     * Returns the current thread's call stack as a string.
     *
     * @param skipFrames how many top frames to skip (in addition to this method & getStackTrace)
     * @param maxFrames max number of frames to include
     * @param trimInternal whether to drop the first 2 internal frames of this utility
     */
    public static String currentCallStack(int skipFrames, int maxFrames, boolean trimInternal) {
        StackTraceElement[] raw = Thread.currentThread().getStackTrace();
        int drop = (trimInternal ? 2 : 0) + Math.max(0, skipFrames); // drop getStackTrace + this method
        return Arrays.stream(raw)
                     .skip(drop)
                     .limit(maxFrames)
                     .map(StackTraceUtil::formatFrame)
                     .collect(Collectors.joining(System.lineSeparator()));
    }

    /** Formats a Throwable's stack trace into a single string (without the exception message). */
    public static String toString(Throwable t) {
        return Arrays.stream(t.getStackTrace())
                     .map(StackTraceUtil::formatFrame)
                     .collect(Collectors.joining(System.lineSeparator()));
    }

    private static String formatFrame(StackTraceElement e) {
        // e.g. "com.example.MyClass.myMethod(MyClass.java:42)"
        String file = e.getFileName() == null ? "Unknown Source" : e.getFileName();
        String loc = e.getLineNumber() >= 0 ? (file + ":" + e.getLineNumber()) : file;
        return e.getClassName() + "." + e.getMethodName() + "(" + loc + ")";
    }

    // Quick demo
    public static void main(String[] args) {
        System.out.println("Current call stack:\n" + StackTraceUtil.currentCallStack());
        try {
            boom();
        } catch (Exception ex) {
            System.out.println("\nFrom Throwable:\n" + StackTraceUtil.toString(ex));
        }
    }

    private static void boom() { throw new RuntimeException("kaboom"); }
}

