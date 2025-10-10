// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AzureBlobFileAttributesTests extends BlobNioTestBase {
    private static AzureFileSystem fs;
    private static List<Path> createdResources = new ArrayList<>();

    @Override
    protected void beforeTest() {
        super.beforeTest();
        fs = createFS(initializeConfigMap());
        setupTestResources();
    }

    private void setupTestResources() {
        try {
            // Create test directory
            Path dirPath = fs.getPath(getNonDefaultRootDir(fs), "dir");
            Files.createDirectories(dirPath);
            createdResources.add(dirPath);

            // Create test file
            Path filePath = fs.getPath(getNonDefaultRootDir(fs), "dir", "test.txt");
            Files.write(filePath, "test content".getBytes(StandardCharsets.UTF_8));
            createdResources.add(filePath);

        } catch (IOException e) {
            throw new RuntimeException("Failed to setup test resources", e);
        }
    }

    @AfterAll
    public static void cleanup() {
        for (Path path : createdResources) {
            try {
                fs.provider().deleteIfExists(path);
            } catch (Exception e) {
                // Log and continue
                System.err.println("Failed to delete resource: " + path + " due to " + e.getMessage());
            }
        }
        createdResources.clear();
    }

    @Test
    public void getADirectory() throws Exception {

        Path path = fs.getPath(getNonDefaultRootDir(fs), "dir");

        boolean isDirectory = Files.isDirectory(path);

        assertDoesNotThrow(() -> {
            new AzureBlobFileAttributes(path);
        });
        assertTrue(isDirectory);
    }

    @Test
    public void getExistingBlob() throws Exception {
        // Test retrieving attributes for an existing file
        Path path = fs.getPath(getNonDefaultRootDir(fs), "dir", "test.txt");

        boolean isDirectory = Files.isDirectory(path);
        boolean isFile = Files.exists(path);

        assertDoesNotThrow(() -> new AzureBlobFileAttributes(path));
        assertFalse(isDirectory);
        assertTrue(isFile);
    }

    @Test
    public void getMissingFileInExistingDirectory() throws Exception {
        // Test retrieving attributes for a file that doesn't exist in an existing directory
        Path path = fs.getPath(getNonDefaultRootDir(fs), "dir1", "foo.xml");

        boolean isDirectory = Files.isDirectory(path);
        boolean isFile = Files.exists(path);

        IOException exception = assertThrows(IOException.class, () -> new AzureBlobFileAttributes(path));
        assertFalse(isDirectory);
        assertFalse(isFile);
        assertTrue(exception.getCause().toString().contains("404"));

    }

    @Test
    public void missingFileInMissingDirectory() throws Exception {
        // Test retrieving attributes for a file that doesn't exist in a directory that doesn't exist
        Path path = fs.getPath(getNonDefaultRootDir(fs), "not_a_dir", "file_not_exists.txt");

        boolean isDirectory = Files.isDirectory(path);
        boolean isFile = Files.exists(path);

        IOException exception = assertThrows(IOException.class, () -> new AzureBlobFileAttributes(path));
        assertFalse(isDirectory);
        assertFalse(isFile);
        assertTrue(exception.getCause().toString().contains("404"));

    }

    @Test
    public void nonFourOhFourError() throws Exception {
        String invalidDirectoryName = "very_long_directory_name";
        StringBuilder repeated = new StringBuilder(invalidDirectoryName);
        for (int i = 0; i < 1000; i++) {
            repeated.append(invalidDirectoryName);
        }
        invalidDirectoryName = repeated.toString();

        Path path = fs.getPath(getNonDefaultRootDir(fs), invalidDirectoryName, "invalid_file.txt");

        // Should throw IOException with an underlying cause other than 404
        IOException exception = assertThrows(IOException.class, () -> new AzureBlobFileAttributes(path));
        assertTrue(exception.getCause().toString().contains("400"));
    }
}
