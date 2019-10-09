package com.microsoft.storageperf;

import com.microsoft.storageperf.core.PerfStressProgram;

public class App {
    public static void main(String[] args) {
        CountOptions options = new CountOptions();
        options.Count = 5;

        try {
            PerfStressProgram.Run(Class.forName("com.microsoft.storageperf.GetBlobsTest"), options);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
