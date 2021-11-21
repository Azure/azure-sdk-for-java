// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage;

import com.azure.perf.test.core.PerfStressProgram;
import com.azure.storage.blob.perf.DownloadBlobTest;
import com.azure.storage.blob.perf.ListBlobsTest;
import com.azure.storage.blob.perf.UploadBlobNoLengthTest;
import com.azure.storage.blob.perf.UploadBlobTest;
import com.azure.storage.blob.perf.UploadBlockBlobTest;
import com.azure.storage.blob.perf.UploadFromFileTest;
import com.azure.storage.blob.perf.UploadOutputStreamTest;
import com.azure.storage.file.datalake.perf.AppendFileDatalakeTest;
import com.azure.storage.file.datalake.perf.ReadFileDatalakeTest;
import com.azure.storage.file.datalake.perf.UploadFileDatalakeTest;
import com.azure.storage.file.datalake.perf.UploadFromFileDatalakeTest;
import com.azure.storage.file.share.perf.DownloadFileShareTest;
import com.azure.storage.file.share.perf.DownloadToFileShareTest;
import com.azure.storage.file.share.perf.UploadFileShareTest;
import com.azure.storage.file.share.perf.UploadFromFileShareTest;

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
            UploadBlobNoLengthTest.class,
            UploadBlockBlobTest.class,
            UploadFromFileTest.class,
            UploadOutputStreamTest.class,
            DownloadFileShareTest.class,
            DownloadToFileShareTest.class,
            UploadFileShareTest.class,
            UploadFromFileShareTest.class,
            AppendFileDatalakeTest.class,
            ReadFileDatalakeTest.class,
            UploadFileDatalakeTest.class,
            UploadFromFileDatalakeTest.class
        }, args);
    }
}
