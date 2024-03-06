// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.stress;

import com.azure.perf.test.core.PerfStressProgram;
import com.azure.storage.file.share.stress.utils.TelemetryHelper;

public class App {
    public static void main(String[] args) {
        TelemetryHelper.init();
        PerfStressProgram.run(new Class<?>[]{
            Download.class,
            DownloadToFile.class,
            GetFileSeekableByteChannelRead.class,
            OpenInputStream.class
        }, args);
    }
}
