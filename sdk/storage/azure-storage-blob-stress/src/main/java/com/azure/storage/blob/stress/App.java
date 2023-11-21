// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress;

import com.azure.perf.test.core.PerfStressProgram;

public class App {
    public static void main(String[] args) {
        PerfStressProgram.run(new Class<?>[]{
            DownloadToFile.class,
        }, args);
    }
}
