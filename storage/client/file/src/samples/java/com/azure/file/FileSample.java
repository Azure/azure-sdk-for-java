// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.file;

import com.azure.core.http.rest.Response;
import com.azure.core.util.configuration.ConfigurationManager;
import com.azure.storage.file.FileClient;
import com.azure.storage.file.FileClientBuilder;
import com.azure.storage.file.ShareClient;
import com.azure.storage.file.ShareClientBuilder;
import com.azure.storage.file.models.CopyStatusType;
import com.azure.storage.file.models.FileCopyInfo;
import com.azure.storage.file.models.FileProperties;
import com.azure.storage.file.models.StorageErrorException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

/**
 * Sample demonstrates how to create, copy and delete a file and how to get and set properties.
 */
public class FileSample {
    private static final String ENDPOINT = ConfigurationManager.getConfiguration().get("AZURE_STORAGE_FILE_ENDPOINT");

    // This is the helper method to generate random name.
    private static String generateRandomName() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * The main method shows how to do the base operation using file sync client.
     * @param args No args needed for the main method.
     * @throws RuntimeException If error occurs when make storage API call.
     * @throws Exception If there are any IO exception occurs.
     */
    public static void main(String[] args) throws Exception {
        String shareName = generateRandomName();
        ShareClient shareClient = new ShareClientBuilder().endpoint(ENDPOINT).shareName(shareName).buildClient();
        shareClient.create();
        String parentDirName = generateRandomName();
        shareClient.createDirectory(parentDirName);

        // Create a source file client
        String srcFileName = generateRandomName();
        FileClient srcFileClient = new FileClientBuilder().endpoint(ENDPOINT).shareName(shareName)
                                    .filePath(parentDirName + "/" + srcFileName).buildClient();

        // Create a source file
        try {
            srcFileClient.create(1024);
        } catch (StorageErrorException e) {
            System.out.println("Failed to create source client. Reasons: " + e.getMessage());
        }

        // Upload some data bytes to the src file.
        String dataText = "Hello, file client sample!";
        ByteBuf uploadData = Unpooled.wrappedBuffer(dataText.getBytes(StandardCharsets.UTF_8));
        try {
            srcFileClient.upload(uploadData, uploadData.readableBytes());
        } catch (StorageErrorException e) {
            System.out.println("Failed to upload the data. Reasons: " + e.getMessage());
        }
        // Create a destination file client.
        String destFileName = generateRandomName();
        FileClient destFileClient = new FileClientBuilder().endpoint(ENDPOINT).shareName(shareName)
                                        .filePath(parentDirName + "/" + destFileName).buildClient();
        destFileClient.create(1024);

        // Copy the file from source file to destination file.
        URL clientURL = null;
        clientURL = srcFileClient.getFileUrl();

        String sourceURL = clientURL.toString() + "/" + shareName + "/" + parentDirName + "/" + srcFileName;

        Response<FileCopyInfo> copyResponse = null;
        try {
            copyResponse = destFileClient.startCopy(sourceURL, null);
        } catch (StorageErrorException e) {
            throw new RuntimeException("Failed to start the copy of source file. Reasons: " + e.getMessage());
        }

        // Abort the copy if the status is pending.
        if (copyResponse.value().copyStatus() == CopyStatusType.PENDING) {
            try {
                destFileClient.abortCopy(copyResponse.value().copyId());
            } catch (StorageErrorException e) {
                System.out.println("Failed to abort the copy. Reasons: " + e.getMessage());
            }
        }

        // Upload a local file to the storage.
        URL fileFolder = FileSample.class.getClassLoader().getResource(".");
        String uploadPath = fileFolder.getPath() + "testfiles/" + "uploadSample.txt";

        try {
            srcFileClient.uploadFromFile(uploadPath);
        } catch (StorageErrorException e) {
            System.out.println("Failed to upload file to storage. Reasons: " + e.getMessage());
        }

        // Download storage file to local file.
        String downloadPath = fileFolder.getPath() + "testfiles/" + "downloadSample.txt";
        File downloadFile = new File(downloadPath);
        try {
            if (!Files.exists(downloadFile.toPath())) {
                downloadFile.createNewFile();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create new upload file.");
        }
        try {
            srcFileClient.downloadToFile(downloadPath);
        } catch (StorageErrorException e) {
            System.out.println("Failed to download file from storage. Reasons: " + e.getMessage());
        }

        if (!Files.exists(downloadFile.toPath())) {
            downloadFile.delete();
        }

        // Get the file properties
        try {
            Response<FileProperties> propertiesResponse = srcFileClient.getProperties();
            System.out.printf("This is the eTag: %s of the file. File type is : %s.", propertiesResponse.value().eTag(), propertiesResponse.value().fileType());
        } catch (StorageErrorException e) {
            System.out.println("Failed to get file properties. Reasons: " + e.getMessage());
        }

        // Delete the source file.
        try {
            srcFileClient.delete();
        } catch (StorageErrorException e) {
            System.out.println("Failed to delete the src file. Reasons: " + e.getMessage());
        }

        // Delete the share
        shareClient.delete();
    }
}
