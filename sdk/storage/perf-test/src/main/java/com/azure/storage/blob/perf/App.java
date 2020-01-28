// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import com.azure.core.test.perf.PerfStressProgram;

public class App {
    public static void main(String[] args) {
        Class<?>[] testClasses;

        try {
            testClasses = new Class<?>[] {
                Class.forName("com.azure.storage.blob.perf.DownloadTest"),
                Class.forName("com.azure.storage.blob.perf.GetBlobsTest"),
                Class.forName("com.azure.storage.blob.perf.UploadBlockBlobTest"),
                Class.forName("com.azure.storage.blob.perf.UploadFromFileTest"),
                Class.forName("com.azure.storage.blob.perf.UploadOutputStreamTest"),
                Class.forName("com.azure.storage.blob.perf.UploadTest"),
            };
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        PerfStressProgram.run(testClasses, args);
    }
}
