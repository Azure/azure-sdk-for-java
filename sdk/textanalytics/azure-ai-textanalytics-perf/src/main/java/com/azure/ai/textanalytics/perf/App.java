package com.azure.ai.textanalytics.perf;

import com.azure.perf.test.core.PerfStressProgram;

public class App {
    public static void main(String[] args) {
        PerfStressProgram.run(
            new Class<?>[]{ DetectLanguageTest.class }, args);
    }
}
