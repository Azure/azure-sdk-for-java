// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file;

import com.azure.core.util.Configuration;
import com.azure.storage.file.models.CopyStatusType;
import com.azure.storage.file.models.FileCopyInfo;
import com.azure.storage.file.models.FileProperties;
import com.azure.storage.file.models.StorageException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

/**
 * Sample demonstrates how to create, copy and delete a file and how to get and set properties.
 */
public class FileSample {
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_STORAGE_FILE_ENDPOINT");

    // This is the helper method to generate random name.
    private static String generateRandomName() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * The main method shows how to do the base operation using file sync client.
     *
     * @param args No args needed for the main method.
     * @throws RuntimeException If error occurs when make storage API call.
     */
    public static void main(String[] args) {
        String shareName = generateRandomName();
        ShareClient shareClient = new ShareClientBuilder().endpoint(ENDPOINT).shareName(shareName).buildClient();
        shareClient.create();
        String parentDirName = generateRandomName();
        shareClient.createDirectory(parentDirName);

        // Create a source file client
        String srcFileName = generateRandomName();
        FileClient srcFileClient = new FileClientBuilder().endpoint(ENDPOINT).shareName(shareName)
            .resourcePath(parentDirName + "/" + srcFileName).buildFileClient();

        // Create a source file
        try {
            srcFileClient.create(1024);
        } catch (StorageException e) {
            System.out.println("Failed to create source client. Reasons: " + e.getMessage());
        }

        // Upload some data bytes to the src file.
        byte[] data = "Hello, file client sample!".getBytes(StandardCharsets.UTF_8);
        InputStream uploadData = new ByteArrayInputStream(data);
        try {
            srcFileClient.upload(uploadData, data.length);
        } catch (StorageException e) {
            System.out.println("Failed to upload the data. Reasons: " + e.getMessage());
        }
        // Create a destination file client.
        String destFileName = generateRandomName();
        FileClient destFileClient = new FileClientBuilder().endpoint(ENDPOINT).shareName(shareName)
            .resourcePath(parentDirName + "/" + destFileName).buildFileClient();
        destFileClient.create(1024);

        // Copy the file from source file to destination file.
        String clientURL = srcFileClient.getFileUrl();

        String sourceURL = clientURL + "/" + shareName + "/" + parentDirName + "/" + srcFileName;

        FileCopyInfo copyResponse;
        try {
            copyResponse = destFileClient.startCopy(sourceURL, null);
        } catch (StorageException e) {
            throw new RuntimeException("Failed to start the copy of source file. Reasons: " + e.getMessage());
        }

        // Abort the copy if the status is pending.
        if (copyResponse.getCopyStatus() == CopyStatusType.PENDING) {
            try {
                destFileClient.abortCopy(copyResponse.getCopyId());
            } catch (StorageException e) {
                System.out.println("Failed to abort the copy. Reasons: " + e.getMessage());
            }
        }

        // Upload a local file to the storage.
        String filePath = "C:/resourcePath/";
        String uploadPath = filePath + "testfiles/" + "uploadSample.txt";

        try {
            srcFileClient.uploadFromFile(uploadPath);
        } catch (StorageException e) {
            System.out.println("Failed to upload file to storage. Reasons: " + e.getMessage());
        }

        // Download storage file to local file.
        String downloadPath = filePath + "testfiles/" + "downloadSample.txt";
        File downloadFile = new File(downloadPath);
        try {
            if (!Files.exists(downloadFile.toPath()) && !downloadFile.createNewFile()) {
                throw new RuntimeException("Failed to create new upload file.");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create new upload file.");
        }
        try {
            srcFileClient.downloadToFile(downloadPath);
        } catch (StorageException e) {
            System.out.println("Failed to download file from storage. Reasons: " + e.getMessage());
        }

        if (Files.exists(downloadFile.toPath()) && !downloadFile.delete()) {
            System.out.println("Failed to delete download file.");
        }

        // Get the file properties
        try {
            FileProperties propertiesResponse = srcFileClient.getProperties();
            System.out.printf("This is the eTag: %s of the file. File type is : %s.", propertiesResponse.getETag(), propertiesResponse.getFileType());
        } catch (StorageException e) {
            System.out.println("Failed to get file properties. Reasons: " + e.getMessage());
        }

        // Delete the source file.
        try {
            srcFileClient.delete();
        } catch (StorageException e) {
            System.out.println("Failed to delete the src file. Reasons: " + e.getMessage());
        }

        // Delete the share
        shareClient.delete();
    }
}
