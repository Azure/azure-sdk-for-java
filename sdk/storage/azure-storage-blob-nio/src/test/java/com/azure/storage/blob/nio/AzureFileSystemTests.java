// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.util.CoreUtils;
import com.azure.storage.common.sas.AccountSasPermission;
import com.azure.storage.common.sas.AccountSasResourceType;
import com.azure.storage.common.sas.AccountSasService;
import com.azure.storage.common.sas.AccountSasSignatureValues;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AzureFileSystemTests extends BlobNioTestBase {
    private Map<String, Object> config;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        config = initializeConfigMap();
    }

    // We do not have a meaningful way of testing the configurations for the ServiceClient.
    @ParameterizedTest
    @CsvSource(value = { "1,false,false", "3,false,true", "3,true,false", "3,true,true" })
    public void create(int numContainers, boolean createContainers, boolean sasToken) throws IOException {
        List<String> containerNames
            = IntStream.range(0, numContainers).mapToObj(i -> generateContainerName()).collect(Collectors.toList());
        config.put(AzureFileSystem.AZURE_STORAGE_FILE_STORES, CoreUtils.stringJoin(",", containerNames));
        if (!sasToken) {
            config.put(AzureFileSystem.AZURE_STORAGE_SHARED_KEY_CREDENTIAL, ENV.getPrimaryAccount().getCredential());
        } else {
            config.put(AzureFileSystem.AZURE_STORAGE_SAS_TOKEN_CREDENTIAL,
                new AzureSasCredential(primaryBlobServiceClient.generateAccountSas(new AccountSasSignatureValues(
                    testResourceNamer.now().plusDays(2), AccountSasPermission.parse("rwcdl"),
                    new AccountSasService().setBlobAccess(true), new AccountSasResourceType().setContainer(true)))));
        }

        AzureFileSystem fileSystem
            = new AzureFileSystem(new AzureFileSystemProvider(), ENV.getPrimaryAccount().getBlobEndpoint(), config);

        List<String> actualContainerNames = new ArrayList<>();
        fileSystem.getFileStores().forEach(fs -> actualContainerNames.add(fs.name()));

        assertEquals(containerNames.size(), actualContainerNames.size());
        for (String containerName : containerNames) {
            assertTrue(actualContainerNames.contains(containerName));
            assertTrue(primaryBlobServiceClient.getBlobContainerClient(containerName).exists());
        }
        assertEquals(primaryBlobServiceAsyncClient.getAccountUrl(), fileSystem.getFileSystemUrl());
    }

    @ParameterizedTest
    @CsvSource(value = { "true,false", "false,true" })
    public void createFailIa(boolean credential, boolean containers) {
        if (containers) {
            config.put(AzureFileSystem.AZURE_STORAGE_FILE_STORES, generateContainerName());
        }
        if (credential) {
            config.put(AzureFileSystem.AZURE_STORAGE_SHARED_KEY_CREDENTIAL, ENV.getPrimaryAccount().getKey());
        }

        assertThrows(IllegalArgumentException.class,
            () -> new AzureFileSystem(new AzureFileSystemProvider(), ENV.getPrimaryAccount().getName(), config));
    }

    @Test
    public void createFailContainerCheck() {
        config
            .put(AzureFileSystem.AZURE_STORAGE_SAS_TOKEN_CREDENTIAL,
                new AzureSasCredential(primaryBlobServiceClient.generateAccountSas(new AccountSasSignatureValues(
                    testResourceNamer.now().plusDays(2), AccountSasPermission.parse("d"),
                    new AccountSasService().setBlobAccess(true), new AccountSasResourceType().setContainer(true)))));
        config.put(AzureFileSystem.AZURE_STORAGE_FILE_STORES, generateContainerName());

        assertThrows(IOException.class, () -> new AzureFileSystem(new AzureFileSystemProvider(),
            ENV.getPrimaryAccount().getBlobEndpoint(), config));
    }

    @Test
    public void createSkipContainerCheck() {
        config
            .put(AzureFileSystem.AZURE_STORAGE_SAS_TOKEN_CREDENTIAL,
                new AzureSasCredential(primaryBlobServiceClient.generateAccountSas(new AccountSasSignatureValues(
                    testResourceNamer.now().plusDays(2), AccountSasPermission.parse("d"),
                    new AccountSasService().setBlobAccess(true), new AccountSasResourceType().setContainer(true)))));
        config.put(AzureFileSystem.AZURE_STORAGE_FILE_STORES, generateContainerName());
        config.put(AzureFileSystem.AZURE_STORAGE_SKIP_INITIAL_CONTAINER_CHECK, true);

        // This would fail, but we skipped the check
        assertDoesNotThrow(() -> new AzureFileSystem(new AzureFileSystemProvider(),
            ENV.getPrimaryAccount().getBlobEndpoint(), config));
    }

    @Test
    public void close() throws IOException {
        AzureFileSystemProvider provider = new AzureFileSystemProvider();
        URI uri = getFileSystemUri();
        config.put(AzureFileSystem.AZURE_STORAGE_FILE_STORES, generateContainerName());
        config.put(AzureFileSystem.AZURE_STORAGE_SHARED_KEY_CREDENTIAL, ENV.getPrimaryAccount().getCredential());
        FileSystem fileSystem = provider.newFileSystem(uri, config);
        fileSystem.close();

        assertFalse(fileSystem.isOpen());
        assertThrows(FileSystemNotFoundException.class, () -> provider.getFileSystem(uri));
        assertDoesNotThrow(fileSystem::close); // Closing twice should have no effect

        // Creating a file system with the same ID after the old one is closed should work.
        assertDoesNotThrow(() -> provider.newFileSystem(uri, config));
        assertNotNull(provider.getFileSystem(uri));
    }

    @ParameterizedTest
    @MethodSource("getPathSupplier")
    public void getPath(String path0, List<String> pathArr, String resultStr) {
        String[] arr = pathArr == null ? null : Arrays.copyOf(pathArr.toArray(), pathArr.size(), String[].class);

        assertEquals(resultStr, createFS(config).getPath(path0, arr).toString());
    }

    private static Stream<Arguments> getPathSupplier() {
        return Stream.of(Arguments.of("foo", null, "foo"), Arguments.of("foo/bar", null, "foo/bar"),
            Arguments.of("/foo/", null, "foo"), Arguments.of("/foo/bar/", null, "foo/bar"),
            Arguments.of("foo", Collections.singletonList("bar"), "foo/bar"),
            Arguments.of("foo/bar/fizz/buzz", null, "foo/bar/fizz/buzz"),
            Arguments.of("foo", Arrays.asList("bar", "fizz", "buzz"), "foo/bar/fizz/buzz"),
            Arguments.of("foo", Arrays.asList("bar/fizz", "buzz"), "foo/bar/fizz/buzz"),
            Arguments.of("foo", Arrays.asList("bar", "fizz/buzz"), "foo/bar/fizz/buzz"),
            Arguments.of("root:/foo", null, "root:/foo"),
            Arguments.of("root:/foo", Collections.singletonList("bar"), "root:/foo/bar"),
            Arguments.of("///root:////foo", Arrays.asList("//bar///fizz//", "buzz"), "root:/foo/bar/fizz/buzz"),
            Arguments.of("root:/", null, "root:"), Arguments.of("", null, ""));
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "root1:/dir1:",
            "root1:/d:ir",
            ":root1:/dir",
            "root1::/dir",
            "root:1/dir",
            "root1/dir:",
            "root1:/foo/bar/dir:" })
    public void getPathFail(String path) {
        assertThrows(InvalidPathException.class, () -> createFS(config).getPath(path));
    }

    @Test
    public void isReadOnlyGetSeparator() {
        AzureFileSystem fs = createFS(config);

        assertFalse(fs.isReadOnly());
        assertEquals("/", fs.getSeparator());
    }

    @Test
    public void getRootDirsGetFileStores() {
        AzureFileSystem fs = createFS(config);
        String[] containers = ((String) config.get(AzureFileSystem.AZURE_STORAGE_FILE_STORES)).split(",");
        List<String> fileStoreNames = new ArrayList<>();
        fs.getFileStores().forEach(store -> fileStoreNames.add(store.name()));
        List<Path> rootDirectories = new ArrayList<>();
        fs.getRootDirectories().forEach(rootDirectories::add);

        assertEquals(containers.length, rootDirectories.size());
        assertEquals(containers.length, fileStoreNames.size());
        for (String container : containers) {
            assertTrue(rootDirectories.contains(fs.getPath(container + ":")));
            assertTrue(fileStoreNames.contains(container));
        }
    }

    @ParameterizedTest
    @CsvSource(value = { "basic,true", "azureBasic,true", "azureBlob,true", "posix,false" })
    public void supportsFileAttributeView(String view, boolean supports) {
        assertEquals(supports, createFS(config).supportedFileAttributeViews().contains(view));
    }

    @Test
    public void getDefaultDirectory() {
        AzureFileSystem fs = createFS(config);

        assertEquals(
            ((String) config.get(AzureFileSystem.AZURE_STORAGE_FILE_STORES)).split(",")[0] + AzurePath.ROOT_DIR_SUFFIX,
            fs.getDefaultDirectory().toString());
    }
}
