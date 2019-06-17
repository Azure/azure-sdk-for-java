// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.logging.ServiceLogger;
import com.azure.storage.file.models.ShareSnapshotInfo;
import com.azure.storage.file.models.StorageErrorException;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

public class ShareAsyncClientTests extends ShareClientTestsBase {
    private final ServiceLogger logger = new ServiceLogger(ShareAsyncClientTests.class);

    private ShareAsyncClient client;

    @Override
    public void beforeTest() {
        shareName = getShareName();
        helper = new TestHelpers();

        if (interceptorManager.isPlaybackMode()) {
            client = helper.setupClient((connectionString, endpoint) -> ShareAsyncClient.builder()
                .connectionString(connectionString)
                .endpoint(endpoint)
                .httpClient(interceptorManager.getPlaybackClient())
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .buildAsync(), logger);
        } else {
            client = helper.setupClient((connectionString, endpoint) -> ShareAsyncClient.builder()
                .connectionString(connectionString)
                .endpoint(endpoint)
                .httpClient(HttpClient.createDefault().wiretap(true))
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .addPolicy(interceptorManager.getRecordPolicy())
                .buildAsync(), logger);
        }
    }

    @Override
    public void afterTest() {
        try {
            client.delete().block();
        } catch (StorageErrorException ex) {
            // Ignore the exception as the share is already deleted and that is what we wanted.
        }
    }

    @Override
    public void getRootDirectoryDoesNotCreateADirectory() {

    }

    @Override
    public void getDirectoryDoesNotCreateADirectory() {

    }

    @Override
    public void createDirectory() {

    }

    @Override
    public void createDirectoryOnSnapshotIsNotAllowed() {

    }

    @Override
    public void createDirectoryInvalidName() {

    }

    @Override
    public void createShare() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();
    }

    @Override
    public void createShareTwiceSameMetadata() {
        Map<String, String> metadata = Collections.singletonMap("test", "metadata");

        StepVerifier.create(client.create(metadata, 2))
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.create(metadata, 2))
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 409));
    }

    @Override
    public void createShareTwiceDifferentMetadata() {
        Map<String, String> metadata = Collections.singletonMap("test", "metadata");

        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.create(metadata, 2))
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 409));
    }

    @Override
    public void createShareInvalidQuota() {
        StepVerifier.create(client.create(null, -1))
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 400));

        StepVerifier.create(client.create(null, 0))
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 400));
    }

    @Override
    public void deleteShare() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.delete())
            .assertNext(response -> helper.assertResponseStatusCode(response, 202))
            .verifyComplete();
    }

    @Override
    public void deleteShareDoesNotExist() {
        StepVerifier.create(client.delete())
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 404));
    }

    @Override
    public void deleteThenCreateShare() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.delete())
            .assertNext(response -> helper.assertResponseStatusCode(response, 202))
            .verifyComplete();

        helper.sleep(Duration.ofSeconds(45));

        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();
    }

    @Override
    public void deleteThenCreateShareTooSoon() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.delete())
            .assertNext(response -> helper.assertResponseStatusCode(response, 202))
            .verifyComplete();

        StepVerifier.create(client.create())
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 409));
    }

    @Override
    public void snapshotShare() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.createSnapshot())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();
    }

    @Override
    public void deleteShareSnapshot() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        Response<ShareSnapshotInfo> snapshotInfoResponse = client.createSnapshot().block();
        helper.assertResponseStatusCode(snapshotInfoResponse, 201);

        StepVerifier.create(client.delete(snapshotInfoResponse.value().snapshot()))
            .assertNext(response -> helper.assertResponseStatusCode(response, 202))
            .verifyComplete();

        StepVerifier.create(client.createSnapshot())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();
    }

    @Override
    public void snapshotShareSameMetadata() {

    }

    @Override
    public void snapshotShareDifferentMetadata() {

    }

    @Override
    public void snapshotShareDoesNotExist() {
        StepVerifier.create(client.createSnapshot())
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 404));
    }

    @Override
    public void getShareProperties() {

    }

    @Override
    public void getSnapshotShareProperties() {

    }

    @Override
    public void getSharePropertiesDoesNotExist() {

    }

    @Override
    public void getSnapshotSharePropertiesDoesNotExist() {

    }

    @Override
    public void setShareProperties() {

    }

    @Override
    public void setSnapshotSharePropertiesIsNotAllowed() {

    }

    @Override
    public void setSharePropertiesInvalidQuota() {

    }

    @Override
    public void setSharePropertiesDoesNotExist() {

    }

    @Override
    public void getShareMetadata() {

    }

    @Override
    public void getSnapshotShareMetadata() {

    }

    @Override
    public void getShareMetadataDoesNotExist() {

    }

    @Override
    public void getSnapshotShareMetadataDoesNotExist() {

    }

    @Override
    public void setShareMetadata() {

    }

    @Override
    public void setSnapshotShareMetadataIsNotAllowed() {

    }

    @Override
    public void setShareMetadataInvalidMetadata() {

    }

    @Override
    public void setShareMetadataDoesNotExist() {

    }

    @Override
    public void getSharePolicies() {

    }

    @Override
    public void getSnapshotSharePoliciesIsNotAllowed() {

    }

    @Override
    public void getSharePoliciesDoesNotExist() {

    }

    @Override
    public void setSharePolicies() {

    }

    @Override
    public void setSnapshotSharePoliciesIsNotAllowed() {

    }

    @Override
    public void setSharePoliciesInvalidPermission() {

    }

    @Override
    public void setSharePoliciesTooManyPermissions() {

    }

    @Override
    public void setSharePoliciesDoesNotExist() {

    }

    @Override
    public void getShareStats() {

    }

    @Override
    public void getSnapshotShareStatsIsNotAllowed() {

    }

    @Override
    public void getShareStatsDoesNotExist() {

    }
}
