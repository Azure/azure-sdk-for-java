// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import com.azure.perf.test.core.PerfStressProgram;

import java.util.ArrayList;
import java.util.List;

public class App {
    public static void main(String[] args) {
        Class<?>[] testClasses;


        List<String> strings = new ArrayList<>();

        try {
            testClasses = new Class<?>[] {
                Class.forName("com.azure.storage.blob.perf.DownloadBlobTest"),
                Class.forName("com.azure.storage.blob.perf.GetBlobsTest"),
                Class.forName("com.azure.storage.blob.perf.UploadBlockBlobTest"),
                Class.forName("com.azure.storage.blob.perf.UploadFromFileTest"),
                Class.forName("com.azure.storage.blob.perf.UploadOutputStreamTest"),
                Class.forName("com.azure.storage.blob.perf.UploadBlobTest"),
            };
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        PerfStressProgram.run(testClasses, args);
    }
}
