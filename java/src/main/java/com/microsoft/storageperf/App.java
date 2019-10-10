package com.microsoft.storageperf;

import com.microsoft.storageperf.core.PerfStressProgram;

public class App {
    public static void main(String[] args) {
        Class<?>[] testClasses;

        try {
            testClasses = new Class<?>[] { Class.forName("com.microsoft.storageperf.GetBlobsTest") };
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        PerfStressProgram.Run(testClasses, args);
    }
}
