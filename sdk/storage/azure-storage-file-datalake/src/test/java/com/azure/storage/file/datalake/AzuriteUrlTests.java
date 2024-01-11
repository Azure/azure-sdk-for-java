// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.datalake;

import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.file.datalake.specialized.DataLakeLeaseClient;
import com.azure.storage.file.datalake.specialized.DataLakeLeaseClientBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AzuriteUrlTests extends DataLakeTestBase {
    private static final String AZURITE_ENDPOINT = "http://127.0.0.1:10000/devstoreaccount1";

    // The credential information for Azurite is static and documented in numerous locations, therefore it is okay to
    // have this "secret" written into public code.
    private static final StorageSharedKeyCredential AZURITE_CREDENTIAL = new StorageSharedKeyCredential(
        "devstoreaccount1", "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==");

    private DataLakeServiceClient getAzuriteServiceClient() {
        return instrument(new DataLakeServiceClientBuilder()
            .endpoint(AZURITE_ENDPOINT)
            .credential(AZURITE_CREDENTIAL))
            .buildClient();
    }

    private static void validatePathClient(DataLakePathClient client, String accountName, String fileSystemName,
        String pathName, String pathUrl) {
        assertEquals(accountName, client.getAccountName());
        assertEquals(fileSystemName, client.getFileSystemName());
        assertEquals(pathName, client.getObjectName());
        assertEquals(pathUrl, client.getPathUrl());
    }

    @ParameterizedTest
    @MethodSource("azuriteUrlParserSupplier")
    public void azuriteUrlParser(String endpoint, String scheme, String host, String accountName, String fileSystem,
        String path, String expectedUrl) {
        BlobUrlParts parts = BlobUrlParts.parse(endpoint);

        assertEquals(scheme, parts.getScheme());
        assertEquals(host, parts.getHost());
        assertEquals(accountName, parts.getAccountName());
        assertEquals(fileSystem, parts.getBlobContainerName());
        assertEquals(path, parts.getBlobName());
        assertEquals(expectedUrl, parts.toUrl().toString());
    }

    private static Stream<Arguments> azuriteUrlParserSupplier() {
        return Stream.of(
            // endpoint | scheme | host | accountName | fileSystem | path | expectedUrl
            Arguments.of("http://127.0.0.1:10000/devstoreaccount1", "http", "127.0.0.1:10000", "devstoreaccount1", null,
                null, "http://127.0.0.1:10000/devstoreaccount1"),
            Arguments.of("http://127.0.0.1:10000/devstoreaccount1/fileSystem", "http", "127.0.0.1:10000",
                "devstoreaccount1", "fileSystem", null, "http://127.0.0.1:10000/devstoreaccount1/fileSystem"),
            Arguments.of("http://127.0.0.1:10000/devstoreaccount1/fileSystem/path", "http", "127.0.0.1:10000",
                "devstoreaccount1", "fileSystem", "path", "http://127.0.0.1:10000/devstoreaccount1/fileSystem/path"),
            Arguments.of("http://localhost:10000/devstoreaccount1", "http", "localhost:10000", "devstoreaccount1", null,
                null, "http://localhost:10000/devstoreaccount1"),
            Arguments.of("http://localhost:10000/devstoreaccount1/fileSystem", "http", "localhost:10000",
                "devstoreaccount1", "fileSystem", null, "http://localhost:10000/devstoreaccount1/fileSystem"),
            Arguments.of("http://localhost:10000/devstoreaccount1/fileSystem/path", "http", "localhost:10000",
                "devstoreaccount1", "fileSystem", "path", "http://localhost:10000/devstoreaccount1/fileSystem/path"),
            Arguments.of("http://localhost:10000/devstoreaccount1/fileSystem/path/to]a path", "http", "localhost:10000",
                "devstoreaccount1", "fileSystem", "path/to]a path",
                "http://localhost:10000/devstoreaccount1/fileSystem/path%2Fto%5Da%20path"),
            Arguments.of("http://localhost:10000/devstoreaccount1/fileSystem/path%2Fto%5Da%20path", "http",
                "localhost:10000", "devstoreaccount1", "fileSystem", "path/to]a path",
                "http://localhost:10000/devstoreaccount1/fileSystem/path%2Fto%5Da%20path"),
            Arguments.of("http://localhost:10000/devstoreaccount1/fileSystem/斑點", "http", "localhost:10000",
                "devstoreaccount1", "fileSystem", "斑點",
                "http://localhost:10000/devstoreaccount1/fileSystem/%E6%96%91%E9%BB%9E"),
            Arguments.of("http://localhost:10000/devstoreaccount1/fileSystem/%E6%96%91%E9%BB%9E", "http",
                "localhost:10000", "devstoreaccount1", "fileSystem", "斑點",
                "http://localhost:10000/devstoreaccount1/fileSystem/%E6%96%91%E9%BB%9E")
        );
    }

    @Test
    public void useDevelopmentStorageTrue() {
        String originalUseDevelopmentStorage = System.getProperty("UseDevelopmentStorage");
        try {
            System.setProperty("UseDevelopmentStorage", "true");

            DataLakeServiceClient serviceClient = new DataLakeServiceClientBuilder()
                .endpoint(AZURITE_ENDPOINT)
                .credential(AZURITE_CREDENTIAL)
                .buildClient();

            assertEquals("http://127.0.0.1:10000/devstoreaccount1", serviceClient.getAccountUrl());
            assertEquals("devstoreaccount1", serviceClient.getAccountName());
        } finally {
            if (originalUseDevelopmentStorage != null) {
                System.setProperty("UseDevelopmentStorage", originalUseDevelopmentStorage);
            } else {
                System.clearProperty("UseDevelopmentStorage");
            }
        }
    }

    @Test
    public void azuriteUrlConstructingServiceClient() {
        DataLakeServiceClient serviceClient = getAzuriteServiceClient();

        assertEquals("http://127.0.0.1:10000/devstoreaccount1", serviceClient.getAccountUrl());
        assertEquals("devstoreaccount1", serviceClient.getAccountName());
    }

    @Test
    public void azuriteUrlGetFileSystemClient() {
        DataLakeFileSystemClient fileSystemClient = getAzuriteServiceClient().getFileSystemClient("fileSystem");

        assertEquals("http://127.0.0.1:10000/devstoreaccount1/fileSystem", fileSystemClient.getFileSystemUrl());
        assertEquals("devstoreaccount1", fileSystemClient.getAccountName());
        assertEquals("fileSystem", fileSystemClient.getFileSystemName());
    }

    @Test
    public void azuriteUrlConstructContainerClient() {
        DataLakeFileSystemClient fileSystemClient = new DataLakeFileSystemClientBuilder()
            .endpoint("http://127.0.0.1:10000/devstoreaccount1/fileSystem")
            .credential(AZURITE_CREDENTIAL)
            .buildClient();

        assertEquals("http://127.0.0.1:10000/devstoreaccount1/fileSystem", fileSystemClient.getFileSystemUrl());
        assertEquals("devstoreaccount1", fileSystemClient.getAccountName());
        assertEquals("fileSystem", fileSystemClient.getFileSystemName());
    }

    @Test
    public void azuriteUrlGetFileClient() {
        DataLakeFileClient pathClient = getAzuriteServiceClient()
            .getFileSystemClient("fileSystem")
            .getFileClient("file");

        validatePathClient(pathClient, "devstoreaccount1", "fileSystem", "file",
            "http://127.0.0.1:10000/devstoreaccount1/fileSystem/file");
    }

    @Test
    public void azuriteUrlGetDirectoryClient() {
        DataLakeDirectoryClient pathClient = getAzuriteServiceClient()
            .getFileSystemClient("fileSystem")
            .getDirectoryClient("directory");

        validatePathClient(pathClient, "devstoreaccount1", "fileSystem", "directory",
            "http://127.0.0.1:10000/devstoreaccount1/fileSystem/directory");
    }

    @Test
    public void azuriteUrlConstructFileClient() {
        DataLakeFileClient pathClient = new DataLakePathClientBuilder()
            .endpoint("http://127.0.0.1:10000/devstoreaccount1/fileSystem/file")
            .credential(AZURITE_CREDENTIAL)
            .buildFileClient();

        validatePathClient(pathClient, "devstoreaccount1", "fileSystem", "file",
            "http://127.0.0.1:10000/devstoreaccount1/fileSystem/file");
    }

    @Test
    public void azuriteUrlConstructDirectoryClient() {
        DataLakeDirectoryClient pathClient = new DataLakePathClientBuilder()
            .endpoint("http://127.0.0.1:10000/devstoreaccount1/fileSystem/directory")
            .credential(AZURITE_CREDENTIAL)
            .buildDirectoryClient();

        validatePathClient(pathClient, "devstoreaccount1", "fileSystem", "directory",
            "http://127.0.0.1:10000/devstoreaccount1/fileSystem/directory");
    }

    @Test
    public void azuriteUrlGetLeaseClient() {
        DataLakeFileSystemClient fileSystemClient = getAzuriteServiceClient().getFileSystemClient("fileSystem");
        DataLakeFileClient pathClient = fileSystemClient.getFileClient("file");

        DataLakeLeaseClient fileSystemLeaseClient = new DataLakeLeaseClientBuilder()
            .fileSystemClient(fileSystemClient)
            .buildClient();

        assertEquals("http://127.0.0.1:10000/devstoreaccount1/fileSystem", fileSystemLeaseClient.getResourceUrl());
        assertEquals("devstoreaccount1", fileSystemLeaseClient.getAccountName());

        DataLakeLeaseClient pathLeaseClient = new DataLakeLeaseClientBuilder()
            .fileClient(pathClient)
            .buildClient();

        assertEquals("http://127.0.0.1:10000/devstoreaccount1/fileSystem/file", pathLeaseClient.getResourceUrl());
        assertEquals("devstoreaccount1", pathLeaseClient.getAccountName());
    }
}
