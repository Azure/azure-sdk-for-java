// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage;

import com.azure.perf.test.core.PerfStressProgram;
import com.microsoft.azure.storage.blob.perf.DownloadBlobTest;
import com.microsoft.azure.storage.blob.perf.ListBlobsTest;
import com.microsoft.azure.storage.blob.perf.UploadBlobTest;
import com.microsoft.azure.storage.blob.perf.UploadFromFileTest;
import com.microsoft.azure.storage.blob.perf.UploadOutputStreamTest;
import com.microsoft.azure.storage.file.share.perf.DownloadFileShareTest;
import com.microsoft.azure.storage.file.share.perf.DownloadToFileShareTest;
import com.microsoft.azure.storage.file.share.perf.UploadFileShareTest;
import com.microsoft.azure.storage.file.share.perf.UploadFromFileShareTest;

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
            UploadBlobTest.class,
            ListBlobsTest.class,
            UploadFromFileTest.class,
            UploadOutputStreamTest.class,
            DownloadFileShareTest.class,
            DownloadToFileShareTest.class,
            UploadFileShareTest.class,
            UploadFromFileShareTest.class
        }, args);
    }
}
