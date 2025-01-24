// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import com.azure.core.test.TestMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.ClosedFileSystemException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AzureDirectoryStreamTests extends BlobNioTestBase {
    private AzureFileSystem fs;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        fs = createFS(initializeConfigMap());
    }

    @ParameterizedTest
    @CsvSource(value = { "0,true", "5,true", "6000,true", "5,false" })
    public void listFiles(int numFiles, boolean absolute) throws IOException {
        if (numFiles > 50 && getTestMode() != TestMode.LIVE) {
            return; // Skip large data set in record and playback
        }
        String rootName = absolute ? getNonDefaultRootDir(fs) : "";
        String dirName = generateBlobName();
        Map<Path, AzureResource> resources = new ConcurrentHashMap<>();
        IntStream.range(0, numFiles).parallel().forEach(i -> {
            try {
                AzureResource resource = new AzureResource(fs.getPath(rootName, dirName, generateBlobName()));
                resource.getBlobClient().getBlockBlobClient().commitBlockList(Collections.emptyList());
                resources.put(resource.getPath(), resource);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        Iterator<Path> iterator
            = new AzureDirectoryStream((AzurePath) fs.getPath(rootName, dirName), entry -> true).iterator();

        if (numFiles > 0) {
            // Check that repeated hasNext calls returns true and doesn't affect the results of next()
            assertTrue(iterator.hasNext());
            assertTrue(iterator.hasNext());
        }

        for (int i = 0; i < numFiles; i++) {
            assertNotNull(resources.remove(iterator.next()));
        }

        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    // If listing results include directories, they should not be recursively listed. Only immediate children are
    // returned.
    @ParameterizedTest
    @CsvSource(value = { "true,false", "false,false", "false,true" })
    public void listDirectories(boolean virtual, boolean isEmpty) throws IOException {
        // The path to list against
        AzureResource listResource = new AzureResource(fs.getPath(getNonDefaultRootDir(fs), generateBlobName()));
        // The only expected result of the listing
        AzureResource listResultResource = new AzureResource(listResource.getPath().resolve(generateBlobName()));
        if (!virtual) {
            listResource.putDirectoryBlob(null);
            listResultResource.putDirectoryBlob(null);
        }

        // Put some children under listResultResource. These should not be returned.
        if (!isEmpty) {
            for (int i = 0; i < 3; i++) {
                ((AzurePath) listResultResource.getPath().resolve(generateBlobName())).toBlobClient()
                    .getBlockBlobClient()
                    .commitBlockList(Collections.emptyList());
            }
        }

        Iterator<Path> iterator = new AzureDirectoryStream(listResource.getPath(), path -> true).iterator();

        assertTrue(iterator.hasNext());
        assertEquals(listResultResource.getPath().toString(), iterator.next().toString());
        assertFalse(iterator.hasNext());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 3 })
    public void listFilesDepth(int depth) throws IOException {
        AzurePath listingPath = (AzurePath) fs.getPath(getNonDefaultRootDir(fs), getPathWithDepth(depth));

        AzureResource filePath = new AzureResource(listingPath.resolve(generateBlobName()));
        filePath.getBlobClient().getBlockBlobClient().commitBlockList(Collections.emptyList());

        AzureResource concreteDirEmptyPath = new AzureResource(listingPath.resolve(generateBlobName()));
        concreteDirEmptyPath.putDirectoryBlob(null);

        AzureResource concreteDirNonEmptyPath = new AzureResource(listingPath.resolve(generateBlobName()));
        concreteDirNonEmptyPath.putDirectoryBlob(null);

        AzureResource concreteDirChildPath
            = new AzureResource(concreteDirNonEmptyPath.getPath().resolve(generateBlobName()));
        concreteDirChildPath.getBlobClient().getBlockBlobClient().commitBlockList(Collections.emptyList());

        AzureResource virtualDirPath = new AzureResource(listingPath.resolve(generateBlobName()));
        AzureResource virtualDirChildPath = new AzureResource(virtualDirPath.getPath().resolve(generateBlobName()));
        virtualDirChildPath.getBlobClient().getBlockBlobClient().commitBlockList(Collections.emptyList());

        List<String> expectedListResults
            = new ArrayList<>(Arrays.asList(filePath.getPath().toString(), concreteDirEmptyPath.getPath().toString(),
                concreteDirNonEmptyPath.getPath().toString(), virtualDirPath.getPath().toString()));

        for (Path path : new AzureDirectoryStream(listingPath, path -> true)) {
            assertTrue(expectedListResults.remove(path.toString()));
        }
        assertEquals(0, expectedListResults.size());
    }

    @Test
    public void iteratorDuplicateCallsFail() throws IOException {
        AzureDirectoryStream stream
            = new AzureDirectoryStream((AzurePath) fs.getPath(generateBlobName()), path -> true);
        stream.iterator();

        assertThrows(IllegalStateException.class, stream::iterator);
    }

    @Test
    public void nextHasNextFailAfterClose() throws IOException {
        String rootName = getNonDefaultRootDir(fs);
        String dirName = generateBlobName();
        for (int i = 0; i < 3; i++) {
            new AzureResource(fs.getPath(rootName, dirName, generateBlobName())).getBlobClient()
                .getBlockBlobClient()
                .commitBlockList(Collections.emptyList());
        }

        DirectoryStream<Path> stream
            = new AzureDirectoryStream((AzurePath) fs.getPath(rootName, dirName), path -> true);
        Iterator<Path> iterator = stream.iterator();

        // There are definitely items we haven't returned from the iterator, but they are inaccessible after closing.
        stream.close();

        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    public void hasNextFailAfterFSClose() throws IOException {
        Path path = fs.getPath(generateBlobName());
        putDirectoryBlob(rootNameToContainerClient(getDefaultDir(fs)).getBlobClient(path.getFileName().toString())
            .getBlockBlobClient());
        DirectoryStream<Path> stream = fs.provider().newDirectoryStream(path, null);
        fs.close();

        assertThrows(ClosedFileSystemException.class, () -> stream.iterator().hasNext());
    }

    @Test
    public void filter() throws IOException {
        String rootName = getNonDefaultRootDir(fs);
        String dirName = generateBlobName();
        for (int i = 0; i < 3; i++) {
            new AzureResource(fs.getPath(rootName, dirName, i + generateBlobName())).getBlobClient()
                .getBlockBlobClient()
                .commitBlockList(Collections.emptyList());
        }

        Iterator<Path> iterator = new AzureDirectoryStream((AzurePath) fs.getPath(rootName, dirName),
            path -> path.getFileName().toString().startsWith("0")).iterator();

        assertTrue(iterator.hasNext());
        assertTrue(iterator.next().getFileName().toString().startsWith("0"));
        assertFalse(iterator.hasNext());
    }

    @Test
    public void filterException() throws IOException {
        String rootName = getNonDefaultRootDir(fs);
        String dirName = generateBlobName();
        for (int i = 0; i < 3; i++) {
            new AzureResource(fs.getPath(rootName, dirName, i + generateBlobName())).getBlobClient()
                .getBlockBlobClient()
                .commitBlockList(Collections.emptyList());
        }
        AzureDirectoryStream stream = new AzureDirectoryStream((AzurePath) fs.getPath(rootName, dirName), entry -> {
            throw new IOException("Test exception");
        });

        DirectoryIteratorException e
            = assertThrows(DirectoryIteratorException.class, () -> stream.iterator().hasNext());
        assertEquals("Test exception", e.getCause().getMessage());
    }
}
