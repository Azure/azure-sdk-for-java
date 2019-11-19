// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.storage.common.StorageSharedKeyCredential;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * This example shows how to start using the Azure Storage Data Lake SDK for Java.
 */
public class BasicExample {

    /**
     * Entry point into the basic examples for Storage datalake.
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
        String endpoint = String.format(Locale.ROOT, "https://%s.dfs.core.windows.net", accountName);

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
         * Create a directory in the filesystem
         */
        DataLakeDirectoryClient directoryClient = dataLakeFileSystemClient.createDirectory("myDirectory");

        /*
         * Create a file and sub directory in the directory
         */
        DataLakeFileClient fileUnderDirectory = directoryClient.createFile("myFileName");
        DataLakeDirectoryClient subDirectory = directoryClient.createSubdirectory("mySubDirectory");

        System.out.println("File under myDirectory is " + fileUnderDirectory.getFileName());
        System.out.println("Directory under myDirectory is " + subDirectory.getDirectoryName());


        /*
         * Create a client that references a to-be-created file in your Azure Storage account's file system.
         * This returns a DataLakeFileClient object that wraps the file's endpoint, credential and a request pipeline
         * (inherited from dataLakeFileSystemClient). Note that file names can be mixed case.
         */
        DataLakeFileClient fileClient = dataLakeFileSystemClient.getFileClient("HelloWorld.txt");

        String data = "Hello world!";
        InputStream dataStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));

        /*
         * Create the file with string (plain text) content.
         */
        fileClient.create();
        fileClient.append(dataStream, 0, data.length());
        fileClient.flush(data.length());

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
        if (!data.equals(new String(outputStream.toByteArray(), StandardCharsets.UTF_8))) {
            throw new RuntimeException("The downloaded data does not match the uploaded data.");
        }

        /*
         * Create more files (maybe even a few directories) before listing.
         */
        for (int i = 0; i < 3; i++) {
            String sampleData = "Samples";
            InputStream dataInFiles = new ByteArrayInputStream(sampleData.getBytes(Charset.defaultCharset()));
            DataLakeFileClient fClient = dataLakeFileSystemClient.getFileClient("myfilesforlisting" + System.currentTimeMillis());
            fClient.create();
            fClient.append(dataInFiles, 0, sampleData.length());
            fClient.flush(sampleData.length());
            dataInFiles.close();
            dataLakeFileSystemClient.getDirectoryClient("mydirsforlisting" + System.currentTimeMillis()).create();
        }

        /*
         * List the path(s) in our file system.
         */
        dataLakeFileSystemClient.listPaths()
            .forEach(pathItem -> System.out.println("Path name: " + pathItem.getName()));

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
