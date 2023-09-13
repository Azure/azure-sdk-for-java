// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.datalake;

import com.azure.storage.common.StorageSharedKeyCredential;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UrlTests {
    private static final StorageSharedKeyCredential CREDENTIAL = new StorageSharedKeyCredential("accountname",
        "accountkey");

    @ParameterizedTest
    @ValueSource(strings = {"https://www.customstorageurl.com", "https://account.core.windows.net",
        "https://0.0.0.0/account", "https://account.file.core.windows.net", "https://www.customdfsstorageurl.com",
        "https://dfsaccount.core.windows.net", "https://0.0.0.0/dfsaccount",
        "https://dfsaccount.file.core.windows.net"})
    public void testUrlsThatShouldNotChangeForDatalake(String endpoint) {
        DataLakeServiceClient client = new DataLakeServiceClientBuilder()
            .endpoint(endpoint)
            .credential(CREDENTIAL)
            .buildClient();

        assertEquals(client.getAccountUrl(), client.blobServiceClient.getAccountUrl());
    }

    @ParameterizedTest
    @CsvSource({
        "https://account.blob.core.windows.net,https://account.blob.core.windows.net,https://account.dfs.core.windows.net",
        "https://dfsaccount.blob.core.windows.net,https://dfsaccount.blob.core.windows.net,https://dfsaccount.dfs.core.windows.net",
        "https://account.dfs.core.windows.net,https://account.blob.core.windows.net,https://account.dfs.core.windows.net",
        "https://dfsaccount.dfs.core.windows.net,https://dfsaccount.blob.core.windows.net,https://dfsaccount.dfs.core.windows.net"
    })
    public void testCorrectServiceUrlSet(String url, String expectedBlobUrl, String expectedDfsUrl) {
        DataLakeServiceClient serviceClient = new DataLakeServiceClientBuilder()
            .endpoint(url)
            .credential(CREDENTIAL)
            .buildClient();
        DataLakeFileSystemClient fileSystemClient = new DataLakeFileSystemClientBuilder()
            .endpoint(url + "/container")
            .credential(CREDENTIAL)
            .buildClient();
        DataLakeFileClient pathClient = new DataLakePathClientBuilder()
            .endpoint(url + "/container/blob")
            .credential(CREDENTIAL)
            .buildFileClient();

        // In either case the dfs url should be set to the dfs client and blob url set to the blob client
        assertEquals(expectedDfsUrl, serviceClient.getAccountUrl());
        assertEquals(expectedBlobUrl, serviceClient.blobServiceClient.getAccountUrl());
        assertEquals(expectedDfsUrl + "/container", fileSystemClient.getFileSystemUrl());
        assertEquals(expectedBlobUrl + "/container", fileSystemClient.blobContainerClient.getBlobContainerUrl());
        assertEquals(expectedDfsUrl + "/container/blob", pathClient.getPathUrl());
        assertEquals(expectedBlobUrl + "/container/blob", pathClient.blockBlobClient.getBlobUrl());
    }
}
