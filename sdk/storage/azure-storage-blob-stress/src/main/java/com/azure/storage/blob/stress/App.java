// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress;

import com.azure.perf.test.core.PerfStressProgram;
import com.azure.storage.blob.stress.utils.TelemetryHelper;

public class App {
    public static void main(String[] args) {
        TelemetryHelper.init();
        PerfStressProgram.run(new Class<?>[]{
            DownloadToFile.class,
            DownloadStream.class,
            DownloadContent.class,
            OpenInputStream.class,
            OpenSeekableByteChannelRead.class
        }, args);
    }
}
