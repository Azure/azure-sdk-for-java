// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage;

import com.azure.storage.blob.BlobContainerAsyncClient;
import com.microsoft.azure.storage.service.BlobContainerService;
import com.microsoft.azure.storage.service.BlobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@SpringBootApplication
public class StorageSampleApplication implements CommandLineRunner {

    private static final String SOURCE_FILE = "storageTestFile.txt";

    @Autowired
    private BlobContainerAsyncClient blobContainerAsyncClient;

    public static void main(String[] args) {
        SpringApplication.run(StorageSampleApplication.class);
    }

    public void run(String... var1) throws IOException {
        final URL resource = this.getClass().getClassLoader().getResource(SOURCE_FILE);
        if (resource == null) {
            throw new FileNotFoundException("Please add the file [" + SOURCE_FILE + "] to the classpath");
        }

        final File sourceFile = new File(resource.getFile());
        final File downloadToFile = Files.createTempFile("azure-storage-test", null).toFile();

        BlobContainerService containerService = new BlobContainerService(blobContainerAsyncClient);
        BlobService blobService = new BlobService(blobContainerAsyncClient.getBlobAsyncClient(SOURCE_FILE));

        containerService.createContainerIfNotExists();

        System.out.println("Enter a command:");
        System.out.println("(P)utBlob | (G)etBlob | (D)eleteBlobs | (E)xitSample");
        final BufferedReader reader =
                new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8.name()));


        boolean isExit = false;
        while (!isExit) {
            System.out.println("Enter a command:");
            final String input = reader.readLine();
            if (input == null) {
                continue;
            }

            switch(input) {
                case "P":
                    blobService.uploadFile(sourceFile);
                    break;
                case "G":
                    blobService.downloadBlob(downloadToFile);
                    break;
                case "D":
                    blobService.deleteBlob();
                    break;
                case "L":
                    containerService.listBlobsInContainer();
                    break;
                case "E":
                    System.out.println("Cleaning up container and tmp file...");
                    containerService.deleteContainer();
                    downloadToFile.delete();
                    isExit = true;
                    break;
                default:
                    break;
            }
        }
    }
}
