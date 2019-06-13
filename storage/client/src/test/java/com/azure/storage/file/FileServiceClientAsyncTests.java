// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.storage.file.models.ListSharesOptions;
import com.azure.storage.file.models.ShareItem;
import com.azure.storage.file.models.ShareProperties;
import com.azure.storage.file.models.StorageErrorException;
import org.junit.Assert;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

import static org.junit.Assert.fail;

public final class FileServiceClientAsyncTests extends FileServiceClientTestsBase {
    private FileServiceAsyncClient serviceClient;

    @Override
    public void setupTest() {
        shareName = getShareName();

        if (interceptorManager.isPlaybackMode()) {
            serviceClient = setupClient((connectionString, endpoint) -> FileServiceAsyncClient.builder()
                .connectionString(connectionString)
                .endpoint(endpoint)
                .httpClient(interceptorManager.getPlaybackClient())
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .buildAsync());
        } else {
            serviceClient = setupClient((connectionString, endpoint) -> FileServiceAsyncClient.builder()
                .connectionString(connectionString)
                .endpoint(endpoint)
                .httpClient(HttpClient.createDefault().wiretap(true))
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .addPolicy(interceptorManager.getRecordPolicy())
                .buildAsync());
        }
    }

    @Override
    public void teardownTest() {
        serviceClient.listShares(new ListSharesOptions().prefix(shareNamePrefix))
            .collectList()
            .block()
            .forEach(share -> {
                ShareAsyncClient client = serviceClient.getShareAsyncClient(share.name());
                try {
                    client.delete(null).block();
                } catch (StorageErrorException ex) {
                    // Share already deleted, that's what we wanted anyways.
                }
            });
    }

    @Override
    public void getShareDoesNotCreateAShare() {
        ShareAsyncClient client = serviceClient.getShareAsyncClient(shareName);
        try {
            client.getStatistics().block();
            fail("getShareAsyncClient shouldn't create a share in Azure.");
        } catch (Exception ex) {
            TestHelpers.assertExceptionStatusCode(ex, 404);
        }
    }

    @Override
    public void createShare() {
        StepVerifier.create(serviceClient.createShare(shareName))
            .assertNext(Assert::assertNotNull)
            .verifyComplete();
    }

    @Override
    public void createShareTwiceSameMetadata() {
        StepVerifier.create(serviceClient.createShare(shareName))
            .assertNext(Assert::assertNotNull)
            .verifyComplete();

        StepVerifier.create(serviceClient.createShare(shareName))
            .assertNext(Assert::assertNotNull)
            .verifyComplete();
    }

    @Override
    public void createShareTwiceDifferentMetadata() {
        Map<String, String> metadata = Collections.singletonMap("test", "metadata");

        StepVerifier.create(serviceClient.createShare(shareName))
            .assertNext(Assert::assertNotNull)
            .verifyComplete();

        StepVerifier.create(serviceClient.createShare(shareName, metadata, null))
            .verifyErrorSatisfies(throwable -> TestHelpers.assertExceptionStatusCode(throwable, 409));
    }

    @Override
    public void createShareInvalidQuota() {
        StepVerifier.create(serviceClient.createShare(shareName, null, -1))
            .verifyErrorSatisfies(throwable -> TestHelpers.assertExceptionStatusCode(throwable, 400));

        StepVerifier.create(serviceClient.createShare(shareName, null, 9999999))
            .verifyErrorSatisfies(throwable -> TestHelpers.assertExceptionStatusCode(throwable, 400));
    }

    @Override
    public void deleteShare() {
        StepVerifier.create(serviceClient.createShare(shareName))
            .assertNext(Assert::assertNotNull)
            .verifyComplete();

        StepVerifier.create(serviceClient.deleteShare(shareName))
            .verifyComplete();
    }

    @Override
    public void deleteShareDoesNotExist() {
        StepVerifier.create(serviceClient.deleteShare(shareName))
            .verifyErrorSatisfies(throwable -> TestHelpers.assertExceptionStatusCode(throwable, 404));
    }

    @Override
    public void deleteThenCreateShare() {
        StepVerifier.create(serviceClient.createShare(shareName))
            .assertNext(Assert::assertNotNull)
            .verifyComplete();

        StepVerifier.create(serviceClient.deleteShare(shareName))
            .verifyComplete();

        TestHelpers.sleep(Duration.ofSeconds(45));

        StepVerifier.create(serviceClient.createShare(shareName))
            .assertNext(Assert::assertNotNull)
            .verifyComplete();
    }

    @Override
    public void deleteThenCreateShareTooSoon() {
        StepVerifier.create(serviceClient.createShare(shareName))
            .assertNext(Assert::assertNotNull)
            .verifyComplete();

        StepVerifier.create(serviceClient.deleteShare(shareName))
            .verifyComplete();

        StepVerifier.create(serviceClient.deleteShare(shareName))
            .verifyErrorSatisfies(throwable -> TestHelpers.assertExceptionStatusCode(throwable, 409));
    }

    @Override
    public void listShares() {
        LinkedList<ShareItem> testShares = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            ShareItem share = new ShareItem().name(shareName + i)
                .properties(new ShareProperties().quota(i));

            testShares.add(share);
            StepVerifier.create(serviceClient.createShare(share.name(), share.metadata(), share.properties().quota()))
                .assertNext(Assert::assertNotNull)
                .verifyComplete();
        }

        StepVerifier.create(serviceClient.listShares(defaultOptions()))
            .assertNext(share -> TestHelpers.assertSharesAreEqual(testShares.pop(), share))
            .assertNext(share -> TestHelpers.assertSharesAreEqual(testShares.pop(), share))
            .assertNext(share -> TestHelpers.assertSharesAreEqual(testShares.pop(), share))
            .verifyComplete();
    }

    @Override
    public void listSharesInvalidMaxResults() {
        StepVerifier.create(serviceClient.listShares(defaultOptions().maxResults(-1)))
            .verifyErrorSatisfies(throwable -> TestHelpers.assertExceptionStatusCode(throwable, 400));

        StepVerifier.create(serviceClient.listShares(defaultOptions().maxResults(0)))
            .verifyErrorSatisfies(throwable -> TestHelpers.assertExceptionStatusCode(throwable, 400));
    }

    @Override
    public void listSharesIncludeMetadata() {
        Map<String, String> metadata = Collections.singletonMap("test", "metadata");

        LinkedList<ShareItem> testShares = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            ShareItem share = new ShareItem().name(shareName + i)
                .properties(new ShareProperties().quota(i));

            if (i % 2 == 0) {
                share.metadata(metadata);
            }

            testShares.add(share);
            StepVerifier.create(serviceClient.createShare(share.name(), share.metadata(), share.properties().quota()))
                .assertNext(Assert::assertNotNull)
                .verifyComplete();
        }

        StepVerifier.create(serviceClient.listShares(defaultOptions()))
            .assertNext(share -> TestHelpers.assertSharesAreEqual(testShares.pop(), share))
            .assertNext(share -> TestHelpers.assertSharesAreEqual(testShares.pop(), share))
            .assertNext(share -> TestHelpers.assertSharesAreEqual(testShares.pop(), share))
            .verifyComplete();
    }

    @Override
    public void listSharesIncludeSnapshots() {

    }

    @Override
    public void listSharesIncludeMetadataAndSnapshots() {
        Map<String, String> metadata = Collections.singletonMap("test", "metadata");

        LinkedList<ShareItem> testShares = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            ShareItem share = new ShareItem().name(shareName + i)
                .properties(new ShareProperties().quota(i));

            if (i % 2 == 0) {
                share.metadata(metadata);
            }

            ShareAsyncClient client = serviceClient.getShareAsyncClient(share.name());

            StepVerifier.create(client.create())
                .assertNext(response -> TestHelpers.assertResponseStatusCode(response, 201))
                .verifyComplete();

            if (i % 2 == 0) {
                StepVerifier.create(client.createSnapshot())
                    .assertNext(response -> {
                        share.snapshot(response.value().snapshot());
                        TestHelpers.assertResponseStatusCode(response, 201);
                    })
                    .verifyComplete();
            }

            testShares.add(share);
        }

        StepVerifier.create(serviceClient.listShares(defaultOptions()))
            .assertNext(share -> TestHelpers.assertSharesAreEqual(testShares.pop(), share))
            .assertNext(share -> TestHelpers.assertSharesAreEqual(testShares.pop(), share))
            .assertNext(share -> TestHelpers.assertSharesAreEqual(testShares.pop(), share))
            .verifyComplete();
    }

    @Override
    public void getProperties() {

    }

    @Override
    public void setProperties() {

    }

    @Override
    public void setPropertiesTooManyRules() {

    }

    @Override
    public void setPropertiesInvalidAllowedHeader() {

    }

    @Override
    public void setPropertiesInvalidExposedHeader() {

    }

    @Override
    public void setPropertiesInvalidAllowedOrigin() {

    }

    @Override
    public void setPropertiesInvalidAllowedMethod() {

    }
}
