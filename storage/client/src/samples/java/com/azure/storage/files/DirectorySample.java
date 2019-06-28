package com.azure.storage.files;

import com.azure.core.http.rest.Response;
import com.azure.core.util.configuration.ConfigurationManager;
import com.azure.storage.file.DirectoryClient;
import com.azure.storage.file.ShareClient;
import com.azure.storage.file.models.DirectoryProperties;
import com.azure.storage.file.models.StorageErrorException;
import java.util.UUID;

/**
 * Sample demonstrates how to create, list and delete directory and its subdirectories and files
 *  and how to get and set properties.
 */
public class DirectorySample {
    private static final String ENDPOINT = ConfigurationManager.getConfiguration().get("AZURE_STORAGE_FILE_ENDPOINT");

    // This is the helper method to generate random name.
    private static String generateRandomName() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public static void main(String[] args) {
        String shareName = generateRandomName();
        ShareClient shareClient = ShareClient.builder().endpoint(ENDPOINT).shareName(shareName).build();
        shareClient.create();
        // Build up a directory client
        DirectoryClient directoryClient = DirectoryClient.builder().endpoint(ENDPOINT).shareName(generateRandomName())
                                            .shareName(shareName)
                                            .directoryName(generateRandomName()).build();
        // Create a parent directory
        try {
            directoryClient.create();
        } catch (StorageErrorException e) {
            System.out.println("Failed to create a directory. Reasons: " + e.getMessage());
        }

        // Create a child directory.
        String childDirectoryName = generateRandomName();
        try {
            directoryClient.createSubDirectory(childDirectoryName);
        } catch (StorageErrorException e) {
            System.out.println("Failed to create sub directory. Reasons: " + e.getMessage());
        }

        // Create a 1KB file under the child directory.
        DirectoryClient childDirClient = null;
        String fileName = generateRandomName();
        try {
            childDirClient = directoryClient.getSubDirectoryClient(childDirectoryName);
            childDirClient.createFile(fileName, 1024);
        } catch (StorageErrorException e) {
            System.out.println("Failed to create a file under the child directory. Reasons: " + e.getMessage());
        }

        // Delete the child directory. The operation will fail because storage service only allowed to delete the empty directory.
        try{
            childDirClient.delete();
        } catch (StorageErrorException e) {
            System.out.println("This is expected as the child directory is not empty.");
        }

        // List all the sub directories and files.
        try {
            directoryClient.listFilesAndDirectories().forEach(
                fileRef -> {
                    System.out.printf("Is the resource a directory? %b. The resource name is: ", fileRef.isDirectory(),
                        fileRef.name());
                }
            );
        } catch (StorageErrorException e) {
            System.out.println("Failed to list all the subdirectories and files. Reasons: " + e.getMessage());
        }

        // Get the parent directory properties.
        try {
            Response<DirectoryProperties> propertiesResponse = directoryClient.getProperties();
            System.out.printf("This is the eTag %s of the directory: ", propertiesResponse.value().eTag());
        } catch (StorageErrorException e) {
            System.out.println("Failed to get the properties of the parent directory");
        }

        // Delete the file.
        try {
            childDirClient.deleteFile(fileName);
        } catch (StorageErrorException e) {
            System.out.println("Failed to delete the file. Reasons: " + e.getMessage());
        }

        // Delete the child folder
        try {
            directoryClient.deleteSubDirectory(childDirectoryName);
        } catch (StorageErrorException e) {
            System.out.println("Failed to delete the child directory. Reasons: " + e.getMessage());
        }

        // Delete the parent folder
        try {
            directoryClient.delete();
        } catch (StorageErrorException e) {
            System.out.println("Failed to delete the parent directory. Reasons: " + e.getMessage());
        }

        shareClient.delete();
    }

}
