// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.stress;

import com.azure.perf.test.core.PerfStressProgram;
import com.azure.storage.stress.TelemetryHelper;

public class App {
    public static void main(String[] args) {
        TelemetryHelper.init();
        PerfStressProgram.run(new Class<?>[]{
            Append.class,
            DataLakeOutputStream.class,
            Flush.class,
            Read.class,
            ReadToFile.class,
            OpenInputStream.class,
            Upload.class,
            UploadFromFile.class
        }, args);
    }
}
