// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage;

import com.azure.perf.test.core.PerfStressProgram;
import com.azure.storage.blob.perf.*;

/**
 * Runs the Storage performance test.
 *
 * <p>To run from command line. Package the project into a jar with dependencies via mvn clean package.
 * Then run the program via java -jar 'compiled-jar-with-dependencies-path' </p>
 *
 * <p> To run from IDE, set all the required environment variables in IntelliJ via Run -&gt; EditConfigurations
 * section.
 * Then run the App's main method via IDE.</p>
 */
public class App {
    public static void main(String[] args) {
        PerfStressProgram.run(new Class<?>[]{
            DownloadBlobTest.class,
            ListBlobsTest.class,
            UploadBlobTest.class,
            UploadBlockBlobTest.class,
            UploadFromFileTest.class,
            UploadOutputStreamTest.class
        }, args);
    }
}
