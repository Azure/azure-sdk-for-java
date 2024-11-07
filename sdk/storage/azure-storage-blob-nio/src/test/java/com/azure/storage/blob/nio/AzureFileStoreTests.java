// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AzureFileStoreTests extends BlobNioTestBase {
    private AzureFileSystem fs;

    // Just need one fs instance for creating the stores.
    @Override
    public void beforeTest() {
        super.beforeTest();
        Map<String, Object> config = initializeConfigMap();
        config.put(AzureFileSystem.AZURE_STORAGE_SHARED_KEY_CREDENTIAL, ENV.getPrimaryAccount().getCredential());
        config.put(AzureFileSystem.AZURE_STORAGE_FILE_STORES, generateContainerName() + "," + generateContainerName());
        try {
            fs = new AzureFileSystem(new AzureFileSystemProvider(), ENV.getPrimaryAccount().getBlobEndpoint(), config);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // The constructor is implicitly tested by creating a file system.
    @Test
    public void name() throws IOException {
        String name = generateContainerName();

        assertEquals(name, new AzureFileStore(fs, name, false).name());
    }

    @Test
    public void type() {
        assertEquals("AzureBlobContainer", fs.getFileStores().iterator().next().type());
    }

    @Test
    public void isReadOnly() {
        assertFalse(fs.getFileStores().iterator().next().isReadOnly());
    }

    @Test
    public void space() throws IOException {
        FileStore store = fs.getFileStores().iterator().next();

        assertEquals(Long.MAX_VALUE, store.getTotalSpace());
        assertEquals(Long.MAX_VALUE, store.getUsableSpace());
        assertEquals(Long.MAX_VALUE, store.getUnallocatedSpace());
    }

    @ParameterizedTest
    @MethodSource("supportsFileAttributeViewSupplier")
    public void supportsFileAttributeView(Class<? extends FileAttributeView> view, String viewName, boolean supports) {
        FileStore store = fs.getFileStores().iterator().next();

        assertEquals(supports, store.supportsFileAttributeView(view));
        assertEquals(supports, store.supportsFileAttributeView(viewName));
    }

    private static Stream<Arguments> supportsFileAttributeViewSupplier() {
        return Stream.of(Arguments.of(BasicFileAttributeView.class, "basic", true),
            Arguments.of(AzureBlobFileAttributeView.class, "azureBlob", true),
            Arguments.of(AzureBasicFileAttributeView.class, "azureBasic", true),
            Arguments.of(PosixFileAttributeView.class, "posix", false));
    }

    @Test
    public void getFileStoreAttributeView() {
        FileStore store = fs.getFileStores().iterator().next();

        assertNull(store.getFileStoreAttributeView(FileStoreAttributeView.class));
        assertThrows(UnsupportedOperationException.class, () -> store.getAttribute("basic:size"));
    }
}
