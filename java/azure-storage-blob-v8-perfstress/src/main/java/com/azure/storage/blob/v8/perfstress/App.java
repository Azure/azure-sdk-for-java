package com.azure.storage.blob.v8.perfstress;

import com.azure.perfstress.PerfStressProgram;

public class App {
    public static void main(String[] args) {
        Class<?>[] testClasses;

        try {
            testClasses = new Class<?>[] {
                Class.forName("com.azure.storage.blob.v8.perfstress.DownloadTest"),
                Class.forName("com.azure.storage.blob.v8.perfstress.GetBlobsTest"),
                Class.forName("com.azure.storage.blob.v8.perfstress.UploadFromFileTest"),
                Class.forName("com.azure.storage.blob.v8.perfstress.UploadOutputStreamTest"),
                Class.forName("com.azure.storage.blob.v8.perfstress.UploadTest"),
            };
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        PerfStressProgram.Run(testClasses, args);
    }
}
