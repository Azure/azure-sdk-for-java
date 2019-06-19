// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.logging.ServiceLogger;
import com.azure.storage.file.models.AccessPolicy;
import com.azure.storage.file.models.ShareSnapshotInfo;
import com.azure.storage.file.models.SignedIdentifier;
import com.azure.storage.file.models.StorageErrorException;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
                .shareName(shareName)
                .httpClient(interceptorManager.getPlaybackClient())
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .buildAsync(), true, logger);
        } else {
            client = helper.setupClient((connectionString, endpoint) -> ShareAsyncClient.builder()
                .connectionString(connectionString)
                .endpoint(endpoint)
                .shareName(shareName)
                .httpClient(HttpClient.createDefault().wiretap(true))
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .addPolicy(interceptorManager.getRecordPolicy())
                .buildAsync(), false, logger);
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
    public void createDirectoryAlreadyExists() {

    }

    @Override
    public void deleteDirectory() {

    }

    @Override
    public void deleteDirectoryDoesNotExist() {

    }

    @Override
    public void create() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();
    }

    @Override
    public void createTwiceSameMetadata() {
        Map<String, String> metadata = Collections.singletonMap("test", "metadata");

        StepVerifier.create(client.create(metadata, 2))
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.create(metadata, 2))
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 409));
    }

    @Override
    public void createTwiceDifferentMetadata() {
        Map<String, String> metadata = Collections.singletonMap("test", "metadata");

        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.create(metadata, 2))
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 409));
    }

    @Override
    public void createInvalidQuota() {
        StepVerifier.create(client.create(null, -1))
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 400));

        StepVerifier.create(client.create(null, 0))
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 400));
    }

    @Override
    public void delete() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.delete())
            .assertNext(response -> helper.assertResponseStatusCode(response, 202))
            .verifyComplete();
    }

    @Override
    public void deleteDoesNotExist() {
        StepVerifier.create(client.delete())
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 404));
    }

    @Override
    public void deleteThenCreate() {
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
    public void deleteThenCreateTooSoon() {
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
    public void snapshot() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.createSnapshot())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();
    }

    @Override
    public void deleteSnapshot() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        Response<ShareSnapshotInfo> snapshotInfoResponse = client.createSnapshot().block();
        assertNotNull(snapshotInfoResponse);
        helper.assertResponseStatusCode(snapshotInfoResponse, 201);

        StepVerifier.create(client.delete(snapshotInfoResponse.value().snapshot()))
            .assertNext(response -> helper.assertResponseStatusCode(response, 202))
            .verifyComplete();

        StepVerifier.create(client.createSnapshot())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();
    }

    @Override
    public void snapshotSameMetadata() {
        Map<String, String> metadata = Collections.singletonMap("test", "metadata");

        StepVerifier.create(client.create(metadata, 2))
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        Response<ShareSnapshotInfo> snapshotInfoResponse = client.createSnapshot(metadata).block();
        assertNotNull(snapshotInfoResponse);
        helper.assertResponseStatusCode(snapshotInfoResponse, 201);

        StepVerifier.create(client.getProperties(snapshotInfoResponse.value().snapshot()))
            .assertNext(response -> {
                helper.assertResponseStatusCode(response, 200);
                assertEquals(metadata, response.value().metadata());
            })
            .verifyComplete();
    }

    @Override
    public void snapshotDifferentMetadata() {
        Map<String, String> createMetadata = Collections.singletonMap("create", "metadata");

        StepVerifier.create(client.create(createMetadata, 2))
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        Map<String, String> updateMetadata = Collections.singletonMap("update", "metadata");
        Response<ShareSnapshotInfo> snapshotInfoResponse = client.createSnapshot(updateMetadata).block();
        assertNotNull(snapshotInfoResponse);
        helper.assertResponseStatusCode(snapshotInfoResponse, 201);

        StepVerifier.create(client.getProperties())
            .assertNext(response -> {
                helper.assertResponseStatusCode(response, 200);
                assertEquals(createMetadata, response.value().metadata());
            })
            .verifyComplete();

        StepVerifier.create(client.getProperties(snapshotInfoResponse.value().snapshot()))
            .assertNext(response -> {
                helper.assertResponseStatusCode(response, 200);
                assertEquals(updateMetadata, response.value().metadata());
            })
            .verifyComplete();
    }

    @Override
    public void snapshotDoesNotExist() {
        StepVerifier.create(client.createSnapshot())
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 404));
    }

    @Override
    public void getProperties() {
        final int quotaInGB = 2;
        Map<String, String> metadata = Collections.singletonMap("test", "metadata");

        StepVerifier.create(client.create(metadata, quotaInGB))
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.getProperties())
            .assertNext(response -> {
                helper.assertResponseStatusCode(response, 200);
                assertEquals(quotaInGB, response.value().quota());
                assertEquals(metadata, response.value().metadata());
            })
            .verifyComplete();
    }

    @Override
    public void getSnapshotProperties() {
        final int quotaInGB = 2;
        Map<String, String> snapshotMetadata = Collections.singletonMap("snapshot", "metadata");

        StepVerifier.create(client.create(null, quotaInGB))
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        Response<ShareSnapshotInfo> snapshotInfoResponse = client.createSnapshot(snapshotMetadata).block();
        assertNotNull(snapshotInfoResponse);
        helper.assertResponseStatusCode(snapshotInfoResponse, 201);

        StepVerifier.create(client.getProperties(snapshotInfoResponse.value().snapshot()))
            .assertNext(response -> {
                helper.assertResponseStatusCode(response, 200);
                assertEquals(quotaInGB, response.value().quota());
                assertEquals(snapshotMetadata, response.value().metadata());
            })
            .verifyComplete();
    }

    @Override
    public void getPropertiesDoesNotExist() {
        StepVerifier.create(client.getProperties())
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 404));
    }

    @Override
    public void getSnapshotPropertiesDoesNotExist() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.getProperties("snapshot"))
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 400));
    }

    @Override
    public void setProperties() {
        final int initialQuoteInGB = 2;

        StepVerifier.create(client.create(null, initialQuoteInGB))
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.getProperties())
            .assertNext(response -> {
                helper.assertResponseStatusCode(response, 200);
                assertEquals(initialQuoteInGB, response.value().quota());
            })
            .verifyComplete();

        final int updatedQuotaInGB = 4;
        StepVerifier.create(client.setQuota(updatedQuotaInGB))
            .assertNext(response -> helper.assertResponseStatusCode(response, 200))
            .verifyComplete();

        StepVerifier.create(client.getProperties())
            .assertNext(response -> {
                helper.assertResponseStatusCode(response, 200);
                assertEquals(updatedQuotaInGB, response.value().quota());
            })
            .verifyComplete();
    }

    @Override
    public void setPropertiesInvalidQuota() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.setQuota(-1))
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 400));

        StepVerifier.create(client.setQuota(9999))
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 400));
    }

    @Override
    public void setPropertiesDoesNotExist() {
        StepVerifier.create(client.setQuota(2))
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 404));
    }

    @Override
    public void getMetadata() {
        Map<String, String> metadata = Collections.singletonMap("test", "metadata");
        StepVerifier.create(client.create(metadata, 2))
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.getProperties())
            .assertNext(response -> {
                helper.assertResponseStatusCode(response, 200);
                assertEquals(metadata, response.value().metadata());
            })
            .verifyComplete();
    }

    @Override
    public void getSnapshotMetadata() {
        Map<String, String> metadata = Collections.singletonMap("test", "metadata");
        StepVerifier.create(client.create(metadata, 2))
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        Response<ShareSnapshotInfo> snapshotInfoResponse = client.createSnapshot().block();
        assertNotNull(snapshotInfoResponse);
        helper.assertResponseStatusCode(snapshotInfoResponse, 201);

        StepVerifier.create(client.getProperties(snapshotInfoResponse.value().snapshot()))
            .assertNext(response -> {
                helper.assertResponseStatusCode(response, 200);
                assertEquals(metadata, response.value().metadata());
            })
            .verifyComplete();
    }

    @Override
    public void getMetadataDoesNotExist() {
        StepVerifier.create(client.getProperties())
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 404));
    }

    @Override
    public void getSnapshotMetadataDoesNotExist() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.getProperties("snapshot"))
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 400));
    }

    @Override
    public void setMetadata() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        Map<String, String> metadata = Collections.singletonMap("setting", "metadata");
        StepVerifier.create(client.setMetadata(metadata))
            .assertNext(response -> helper.assertResponseStatusCode(response, 200))
            .verifyComplete();

        StepVerifier.create(client.getProperties())
            .assertNext(response -> {
                helper.assertResponseStatusCode(response, 200);
                assertEquals(metadata, response.value().metadata());
            })
            .verifyComplete();
    }

    @Override
    public void setMetadataInvalidMetadata() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        Map<String, String> metadata = Collections.singletonMap("", "metadata");
        StepVerifier.create(client.setMetadata(metadata))
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 400));
    }

    @Override
    public void setMetadataDoesNotExist() {
        Map<String, String> metadata = Collections.singletonMap("test", "metadata");
        StepVerifier.create(client.setMetadata(metadata))
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 404));
    }

    @Override
    public void getPolicies() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.getAccessPolicy())
            .expectNextCount(0)
            .verifyComplete();
    }

    @Override
    public void getPoliciesDoesNotExist() {
        StepVerifier.create(client.getAccessPolicy())
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 404));
    }

    @Override
    public void setPolicies() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        AccessPolicy policy = new AccessPolicy().permission("r")
            .start(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .expiry(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC));

        SignedIdentifier permission = new SignedIdentifier().id("test")
            .accessPolicy(policy);

        StepVerifier.create(client.setAccessPolicy(Collections.singletonList(permission)))
            .assertNext(response -> helper.assertResponseStatusCode(response, 200))
            .verifyComplete();

        StepVerifier.create(client.getAccessPolicy())
            .assertNext(responsePermission -> helper.assertPermissionsAreEqual(permission, responsePermission))
            .verifyComplete();
    }

    @Override
    public void setPoliciesInvalidPermission() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        AccessPolicy policy = new AccessPolicy().permission("abcdefg")
            .start(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .expiry(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC));

        SignedIdentifier permission = new SignedIdentifier().id("test")
            .accessPolicy(policy);

        StepVerifier.create(client.setAccessPolicy(Collections.singletonList(permission)))
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 400));
    }

    @Override
    public void setPoliciesTooManyPermissions() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        List<SignedIdentifier> permissions = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            AccessPolicy policy = new AccessPolicy().permission("r")
                .start(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
                .expiry(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC));

            permissions.add(new SignedIdentifier().id("test" + i).accessPolicy(policy));
        }

        StepVerifier.create(client.setAccessPolicy(permissions))
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 400));
    }

    @Override
    public void setPoliciesDoesNotExist() {
        AccessPolicy policy = new AccessPolicy().permission("r")
            .start(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .expiry(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC));

        SignedIdentifier permission = new SignedIdentifier().id("test")
            .accessPolicy(policy);

        StepVerifier.create(client.setAccessPolicy(Collections.singletonList(permission)))
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 404));
    }

    @Override
    public void getStats() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.getStatistics())
            .assertNext(response -> {
                helper.assertResponseStatusCode(response, 200);
                assertEquals(0, response.value().getShareUsageInGB());
            })
            .verifyComplete();
    }

    @Override
    public void getStatsDoesNotExist() {
        StepVerifier.create(client.getStatistics())
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 404));
    }
}
