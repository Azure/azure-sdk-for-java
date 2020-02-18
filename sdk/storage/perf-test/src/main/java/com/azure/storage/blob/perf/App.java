// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import com.azure.perf.test.core.PerfStressProgram;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs the Storage performance test.
 *
 * <p>To run from command line. Package the project into a jar with dependencies via mvn clean package.
 * Then run the program via java -jar 'compiled-jar-with-dependencies-path' </p>
 *
 * <p> To run from IDE, set all the required environment variables in IntelliJ via Run -> EditConfigurations section.
 * Then run the App's main method via IDE.</p>
 */
public class App {
    public static void main(String[] args) {
        Class<?>[] testClasses;


        List<String> strings = new ArrayList<>();

        try {
            testClasses = new Class<?>[] {
                Class.forName("com.azure.storage.blob.perf.DownloadBlobTest"),
                Class.forName("com.azure.storage.blob.perf.ListBlobsTest"),
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
