// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import com.azure.storage.blob.models.BlobStorageException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AzureBlobFileAttributesTests extends BlobNioTestBase {
    private AzureFileSystem fs;
    private final List<Path> createdResources = new ArrayList<>();

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

    @Test
    public void getADirectory() throws Exception {

        Path path = fs.getPath(getNonDefaultRootDir(fs), "dir");

        boolean isDirectory = Files.isDirectory(path);

        assertDoesNotThrow(() -> new AzureBlobFileAttributes(path));
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
        Path path = fs.getPath(getNonDefaultRootDir(fs), "dir", "foo.xml");

        boolean isDirectory = Files.isDirectory(path);
        boolean isFile = Files.exists(path);

        IOException exception = assertThrows(IOException.class, () -> new AzureBlobFileAttributes(path));
        assertFalse(isDirectory);
        assertFalse(isFile);
        BlobStorageException e = assertInstanceOf(BlobStorageException.class, exception.getCause());
        assertEquals(404, e.getStatusCode());
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
        BlobStorageException e = assertInstanceOf(BlobStorageException.class, exception.getCause());
        assertEquals(404, e.getStatusCode());
    }

    @Test
    public void nonFourOhFourError() throws Exception {
        String invalidDirectoryName = String.join("", Collections.nCopies(1001, "very_long_directory_name"));

        Path path = fs.getPath(getNonDefaultRootDir(fs), invalidDirectoryName, "invalid_file.txt");

        // Should throw IOException with an underlying cause other than 404
        IOException exception = assertThrows(IOException.class, () -> new AzureBlobFileAttributes(path));
        BlobStorageException e = assertInstanceOf(BlobStorageException.class, exception.getCause());
        assertNotEquals(404, e.getStatusCode());
    }
}
