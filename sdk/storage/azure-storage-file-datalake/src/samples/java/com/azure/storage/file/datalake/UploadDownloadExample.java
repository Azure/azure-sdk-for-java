// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.storage.common.StorageSharedKeyCredential;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * This example shows how to upload and download using the Azure Storage Data Lake SDK for Java.
 */
public class UploadDownloadExample {

    /**
     * Entry point into the upload download examples for Storage datalake.
     *
     * @param args Unused. Arguments to the program.
     * @throws IOException If an I/O error occurs
     * @throws RuntimeException If the downloaded data doesn't match the uploaded data
     */
    public static void main(String[] args) throws IOException {

        /*
         * From the Azure portal, get your Storage account's name and account key.
         */
        String accountName = SampleHelper.getAccountName();
        String accountKey = SampleHelper.getAccountKey();

        /*
         * Use your Storage account's name and key to create a credential object; this is used to access your account.
         */
        StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, accountKey);

        /*
         * From the Azure portal, get your Storage account dfs service URL endpoint.
         * The URL typically looks like this:
         */
        String endpoint = String.format(Locale.ROOT, "http://%s.dfs.core.windows.net", accountName);

        /*
         * Create a DataLakeServiceClient object that wraps the service endpoint, credential and a request pipeline.
         */
        DataLakeServiceClient storageClient = new DataLakeServiceClientBuilder().endpoint(endpoint).credential(credential).buildClient();

        /*
         * This example shows several common operations just to get you started.
         */

        /*
         * Create a client that references a to-be-created file system in your Azure Storage account. This returns a
         * FileSystem object that wraps the file system's endpoint, credential and a request pipeline (inherited from storageClient).
         * Note that file system names require lowercase.
         */
        DataLakeFileSystemClient dataLakeFileSystemClient = storageClient.getFileSystemClient("myjavafilesystembasic" + System.currentTimeMillis());

        /*
         * Create a file system in Storage datalake account.
         */
        dataLakeFileSystemClient.create();

        /*
         * Create a client that references a to-be-created file in your Azure Storage account's file system.
         * This returns a DataLakeFileClient object that wraps the file's endpoint, credential and a request pipeline
         * (inherited from dataLakeFileSystemClient). Note that file names can be mixed case.
         */
        DataLakeFileClient fileClient = dataLakeFileSystemClient.getFileClient("HelloWorld.txt");

        StringBuilder sb = new StringBuilder();

        String data = "Hello World!!!!!";
        sb.append(data);
        InputStream dataStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));

        /*
         * Create the file.
         */
        fileClient.create();

        /*
         * Append 16 bytes of random data to the file
         * The first parameter denotes the input stream to append
         * The second parameter denotes the offset at which to start appending the data.
         * The third parameter denotes the length of data being appended.
         * Data is not actually written until the flush command is called.
         */
        fileClient.append(dataStream, 0, 16);
        dataStream.close();

        /*
         * Flush the amount of data specified. This is when data is actually written to the service.
         */
        fileClient.flush(16);

        sb.append(data);
        dataStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        dataStream.close();

        /*
         * Append more data to the file after the data appended before. So we start appending at the end of the
         * last 16 bytes
         */
        fileClient.append(dataStream, 16, 16);

        sb.append(data);
        dataStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));

        /*
         * Append even more data before flushing.  Note, our offset parameter is now adjusted to append data only after
         * the first 32 bytes now.
         */
        fileClient.append(dataStream, 32, 16);

        /*
         * Flush all the data we just appended. This is 48 bytes since that's the last position of data we have appended
         * relative to the file.
         */
        fileClient.flush(48);

        dataStream.close();

        /*
         * Download the file's content to output stream.
         */
        int dataSize = (int) fileClient.getProperties().getFileSize();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(dataSize);
        fileClient.read(outputStream);
        outputStream.close();

        /*
         * Verify that the file data round-tripped correctly.
         */
        if (!sb.toString().equals(new String(outputStream.toByteArray(), StandardCharsets.UTF_8))) {
            throw new RuntimeException("The downloaded data does not match the uploaded data.");
        }

        /*
         * Delete the file we created earlier.
         */
        fileClient.delete();

        /*
         * Delete the file system we created earlier.
         */
        dataLakeFileSystemClient.delete();
    }
}
