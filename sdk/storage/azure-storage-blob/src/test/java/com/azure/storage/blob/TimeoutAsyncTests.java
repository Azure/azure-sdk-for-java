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
import reactor.test.StepVerifier;

import java.time.Duration;

@Isolated
public class TimeoutAsyncTests {
    /*
     * This test class is marked as isolated to ensure that resource related issues do not interfere with the timeouts being tested.
     *
     * The custom http clients return a generic xml list of 5 blobs total.
     * The api call should return 2 pages, one page of 3 blobs and one page of 2 blobs.
     * Although each page is set to take 4 seconds to return, the timeout being set to 6 seconds should not cause the test to fail,
     * as the timeout is only on the page request and not the entire stream of pages.
     */

    @Test
    public void listBlobsFlatWithTimeoutStillBackedByPagedFlux() {
        BlobContainerAsyncClient containerClient
            = new BlobContainerClientBuilder().endpoint("https://account.blob.core.windows.net/")
                .credential(new MockTokenCredential())
                .containerName("foo")
                .httpClient(new BlobTestBase.PagingTimeoutTestClient().addListBlobsResponses(5, 3, false))
                .buildAsyncClient();

        StepVerifier
            .create(
                containerClient
                    .listBlobsFlatWithOptionalTimeout(new ListBlobsOptions().setMaxResultsPerPage(3), null,
                        Duration.ofSeconds(6))
                    .byPage())
            .expectNextCount(2)
            .verifyComplete();
    }

    @Test
    public void listBlobsHierWithTimeoutStillBackedByPagedFlux() {
        BlobContainerAsyncClient containerClient
            = new BlobContainerClientBuilder().endpoint("https://account.blob.core.windows.net/")
                .credential(new MockTokenCredential())
                .containerName("foo")
                .httpClient(new BlobTestBase.PagingTimeoutTestClient().addListBlobsResponses(5, 3, true))
                .buildAsyncClient();

        StepVerifier
            .create(containerClient
                .listBlobsHierarchyWithOptionalTimeout("/", new ListBlobsOptions().setMaxResultsPerPage(3),
                    Duration.ofSeconds(6))
                .byPage())
            .expectNextCount(2)
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-04-10")
    @Test
    public void findBlobsInContainerWithTimeoutStillBackedByPagedFlux() {
        BlobContainerAsyncClient containerClient
            = new BlobContainerClientBuilder().endpoint("https://account.blob.core.windows.net/")
                .credential(new MockTokenCredential())
                .containerName("foo")
                .httpClient(new BlobTestBase.PagingTimeoutTestClient().addFindBlobsResponses(5, 3))
                .buildAsyncClient();

        StepVerifier.create(containerClient.findBlobsByTags(
            new FindBlobsOptions(String.format("\"%s\"='%s'", "dummyKey", "dummyValue")).setMaxResultsPerPage(3),
            Duration.ofSeconds(6), Context.NONE).byPage()).expectNextCount(2).verifyComplete();
    }

    @Test
    public void listContainersWithTimeoutStillBackedByPagedFlux() {
        BlobServiceAsyncClient serviceClient
            = new BlobServiceClientBuilder().endpoint("https://account.blob.core.windows.net/")
                .credential(new MockTokenCredential())
                .httpClient(new BlobTestBase.PagingTimeoutTestClient().addListContainersResponses(5, 3))
                .buildAsyncClient();

        StepVerifier
            .create(serviceClient
                .listBlobContainersWithOptionalTimeout(new ListBlobContainersOptions().setMaxResultsPerPage(3),
                    Duration.ofSeconds(6))
                .byPage())
            .expectNextCount(2)
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void findBlobsWithTimeoutStillBackedByPagedFlux() {
        BlobServiceAsyncClient serviceClient
            = new BlobServiceClientBuilder().endpoint("https://account.blob.core.windows.net/")
                .credential(new MockTokenCredential())
                .httpClient(new BlobTestBase.PagingTimeoutTestClient().addFindBlobsResponses(5, 3))
                .buildAsyncClient();

        StepVerifier.create(serviceClient.findBlobsByTags(
            new FindBlobsOptions(String.format("\"%s\"='%s'", "dummyKey", "dummyValue")).setMaxResultsPerPage(3),
            Duration.ofSeconds(6), Context.NONE).byPage()).expectNextCount(2).verifyComplete();
    }

}
