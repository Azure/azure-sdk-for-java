// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Context;
import com.azure.storage.blob.models.ListBlobContainersOptions;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.options.FindBlobsOptions;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Isolated
public class TimeoutTests {
    /*
     * This test class is marked as isolated to ensure that resource related issues do not interfere with the timeouts being tested.
     *
     * In addition to isolating the tests, we are mocking the HTTP layer to return a fixed number of blobs in a paged manner.
     * This allows us to control the number of pages returned and the time taken for each page to be returned, ensuring
     * that the timeout is only on the page request and not the entire stream of pages.
     *
     * The custom http clients return a generic xml list of 5 blobs total.
     * The api call should return 2 pages, one page of 3 blobs and one page of 2 blobs.
     * Although each page is set to take 4 seconds to return, the timeout being set to 6 seconds should not cause the test to fail,
     * as the timeout should only be on the page request and not the entire stream of pages.
     */

    @Test
    public void listBlobsFlatWithTimeoutStillBackedByPagedStream() {
        BlobContainerClient containerClient
            = new BlobContainerClientBuilder().endpoint("https://account.blob.core.windows.net/")
                .credential(new MockTokenCredential())
                .containerName("foo")
                .httpClient(new BlobTestBase.PagingTimeoutTestClient().addListBlobsResponses(5, 3, false))
                .buildClient();

        assertEquals(2,
            containerClient.listBlobs(new ListBlobsOptions().setMaxResultsPerPage(3), Duration.ofSeconds(6))
                .streamByPage()
                .count());
    }

    @Test
    public void listBlobsHierWithTimeoutStillBackedByPagedStream() {
        BlobContainerClient containerClient
            = new BlobContainerClientBuilder().endpoint("https://account.blob.core.windows.net/")
                .credential(new MockTokenCredential())
                .containerName("foo")
                .httpClient(new BlobTestBase.PagingTimeoutTestClient().addListBlobsResponses(5, 3, true))
                .buildClient();

        assertEquals(2,
            containerClient
                .listBlobsByHierarchy("/", new ListBlobsOptions().setMaxResultsPerPage(3), Duration.ofSeconds(6))
                .streamByPage()
                .count());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-04-10")
    @Test
    public void findBlobsInContainerWithTimeoutStillBackedByPagedStream() {
        BlobContainerClient containerClient
            = new BlobContainerClientBuilder().endpoint("https://account.blob.core.windows.net/")
                .credential(new MockTokenCredential())
                .containerName("foo")
                .httpClient(new BlobTestBase.PagingTimeoutTestClient().addFindBlobsResponses(5, 3))
                .buildClient();

        assertEquals(2,
            containerClient.findBlobsByTags(
                new FindBlobsOptions(String.format("\"%s\"='%s'", "dummyKey", "dummyValue")).setMaxResultsPerPage(3),
                Duration.ofSeconds(6), Context.NONE).streamByPage().count());
    }

    @Test
    public void listContainersWithTimeoutStillBackedByPagedStream() {
        BlobServiceClient serviceClient
            = new BlobServiceClientBuilder().endpoint("https://account.blob.core.windows.net/")
                .credential(new MockTokenCredential())
                .httpClient(new BlobTestBase.PagingTimeoutTestClient().addListContainersResponses(5, 3))
                .buildClient();

        assertEquals(2,
            serviceClient
                .listBlobContainers(new ListBlobContainersOptions().setMaxResultsPerPage(3), Duration.ofSeconds(6))
                .streamByPage()
                .count());

    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void findBlobsWithTimeoutStillBackedByPagedStream() {
        BlobServiceClient serviceClient
            = new BlobServiceClientBuilder().endpoint("https://account.blob.core.windows.net/")
                .credential(new MockTokenCredential())
                .httpClient(new BlobTestBase.PagingTimeoutTestClient().addFindBlobsResponses(5, 3))
                .buildClient();

        assertEquals(2,
            serviceClient.findBlobsByTags(
                new FindBlobsOptions(String.format("\"%s\"='%s'", "dummyKey", "dummyValue")).setMaxResultsPerPage(3),
                Duration.ofSeconds(6), Context.NONE).streamByPage().count());
    }
}
