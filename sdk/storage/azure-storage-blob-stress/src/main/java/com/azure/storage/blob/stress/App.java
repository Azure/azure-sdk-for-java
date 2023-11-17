package com.azure.storage.blob.stress;

import com.azure.core.util.logging.ClientLogger;
import com.azure.perf.test.core.PerfStressProgram;
import com.azure.storage.blob.stress.scenarios.DownloadToFileStressScenario;

public class BlobStorageStressRunner {
    public static void main(String[] args) {
        PerfStressProgram.run(new Class<?>[]{
            DownloadToFileStressScenario.class,
        }, args);
    }
}
