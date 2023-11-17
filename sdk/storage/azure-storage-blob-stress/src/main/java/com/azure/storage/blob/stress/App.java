package com.azure.storage.blob.stress;

import com.azure.perf.test.core.PerfStressProgram;

public class App {
    public static void main(String[] args) {
        PerfStressProgram.run(new Class<?>[]{
            DownloadToFileStressScenario.class,
        }, args);
    }
}
