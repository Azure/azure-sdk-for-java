// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.logging.ServiceLogger;
import com.azure.storage.file.models.AccessPolicy;
import com.azure.storage.file.models.ShareProperties;
import com.azure.storage.file.models.ShareSnapshotInfo;
import com.azure.storage.file.models.ShareStatistics;
import com.azure.storage.file.models.SignedIdentifier;
import com.azure.storage.file.models.StorageErrorException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ShareClientTests extends ShareClientTestsBase {
    private final ServiceLogger logger = new ServiceLogger(ShareClient.class);

    private ShareClient client;

    @Override
    public void beforeTest() {
        shareName = getShareName();
        helper = new TestHelpers();

        if (interceptorManager.isPlaybackMode()) {
            client = helper.setupClient((connectionString, endpoint) -> ShareClient.builder()
                .connectionString(connectionString)
                .endpoint(endpoint)
                .shareName(shareName)
                .httpClient(interceptorManager.getPlaybackClient())
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .buildSync(), true, logger);
        } else {
            client = helper.setupClient((connectionString, endpoint) -> ShareClient.builder()
                .connectionString(connectionString)
                .endpoint(endpoint)
                .shareName(shareName)
                .httpClient(HttpClient.createDefault().wiretap(true))
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .addPolicy(interceptorManager.getRecordPolicy())
                .buildSync(), false, logger);
        }
    }

    @Override
    public void afterTest() {
        try {
            client.delete();
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
        helper.assertResponseStatusCode(client.create(), 201);
    }

    @Override
    public void createTwiceSameMetadata() {
        Map<String, String> metadata = Collections.singletonMap("test", "metadata");

        helper.assertResponseStatusCode(client.create(metadata, 2), 201);

        helper.assertExceptionStatusCode(() -> client.create(metadata, 2), 409);
    }

    @Override
    public void createTwiceDifferentMetadata() {
        Map<String, String> metadata = Collections.singletonMap("test", "metadata");

        helper.assertResponseStatusCode(client.create(), 201);

        helper.assertExceptionStatusCode(() -> client.create(metadata, 2), 409);
    }

    @Override
    public void createInvalidQuota() {
        helper.assertExceptionStatusCode(() -> client.create(null, -1), 400);

        helper.assertExceptionStatusCode(() -> client.create(null, 0), 400);
    }

    @Override
    public void delete() {
        helper.assertResponseStatusCode(client.create(), 201);

        helper.assertResponseStatusCode(client.delete(), 202);
    }

    @Override
    public void deleteDoesNotExist() {
        helper.assertExceptionStatusCode(() -> client.delete(), 404);
    }

    @Override
    public void deleteThenCreate() {
        helper.assertResponseStatusCode(client.create(), 201);

        helper.assertResponseStatusCode(client.delete(), 202);

        helper.sleep(Duration.ofSeconds(45));

        helper.assertResponseStatusCode(client.create(), 201);
    }

    @Override
    public void deleteThenCreateTooSoon() {
        helper.assertResponseStatusCode(client.create(), 201);

        helper.assertResponseStatusCode(client.delete(), 202);

        helper.assertExceptionStatusCode(() -> client.create(), 409);
    }

    @Override
    public void snapshot() {
        helper.assertResponseStatusCode(client.create(), 201);

        helper.assertResponseStatusCode(client.createSnapshot(), 201);
    }

    @Override
    public void deleteSnapshot() {
        helper.assertResponseStatusCode(client.create(), 201);

        Response<ShareSnapshotInfo> snapshotInfoResponse = client.createSnapshot();
        helper.assertResponseStatusCode(snapshotInfoResponse, 201);

        helper.assertResponseStatusCode(client.delete(snapshotInfoResponse.value().snapshot()), 202);

        helper.assertResponseStatusCode(client.createSnapshot(), 201);
    }

    @Override
    public void snapshotSameMetadata() {
        Map<String, String> metadata = Collections.singletonMap("test", "metadata");

        helper.assertResponseStatusCode(client.create(metadata, 2), 201);

        Response<ShareSnapshotInfo> snapshotInfoResponse = client.createSnapshot(metadata);
        helper.assertResponseStatusCode(snapshotInfoResponse, 201);

        Response<ShareProperties> propertiesResponse = client.getProperties(snapshotInfoResponse.value().snapshot());
        helper.assertResponseStatusCode(propertiesResponse, 200);
        assertEquals(metadata, propertiesResponse.value().metadata());
    }

    @Override
    public void snapshotDifferentMetadata() {
        Map<String, String> createMetadata = Collections.singletonMap("create", "metadata");

        helper.assertResponseStatusCode(client.create(createMetadata, 2), 201);

        Map<String, String> updateMetadata = Collections.singletonMap("update", "metadata");
        Response<ShareSnapshotInfo> snapshotInfoResponse = client.createSnapshot(updateMetadata);
        helper.assertResponseStatusCode(snapshotInfoResponse, 201);

        Response<ShareProperties> propertiesResponse = client.getProperties();
        helper.assertResponseStatusCode(propertiesResponse, 200);
        assertEquals(createMetadata, propertiesResponse.value().metadata());

        propertiesResponse = client.getProperties(snapshotInfoResponse.value().snapshot());
        helper.assertResponseStatusCode(propertiesResponse, 200);
        assertEquals(updateMetadata, propertiesResponse.value().metadata());
    }

    @Override
    public void snapshotDoesNotExist() {
        helper.assertExceptionStatusCode(() -> client.createSnapshot(), 404);
    }

    @Override
    public void getProperties() {
        final int quotaInGB = 2;
        Map<String, String> metadata = Collections.singletonMap("test", "metadata");

        helper.assertResponseStatusCode(client.create(metadata, quotaInGB), 201);

        Response<ShareProperties> propertiesResponse = client.getProperties();
        helper.assertResponseStatusCode(propertiesResponse, 200);
        assertEquals(quotaInGB, propertiesResponse.value().quota());
        assertEquals(metadata, propertiesResponse.value().metadata());
    }

    @Override
    public void getSnapshotProperties() {
        final int quotaInGB = 2;
        Map<String, String> snapshotMetadata = Collections.singletonMap("snapshot", "metadata");

        helper.assertResponseStatusCode(client.create(null, quotaInGB), 201);

        Response<ShareSnapshotInfo> snapshotInfoResponse = client.createSnapshot(snapshotMetadata);
        helper.assertResponseStatusCode(snapshotInfoResponse, 201);

        Response<ShareProperties> propertiesResponse = client.getProperties(snapshotInfoResponse.value().snapshot());
        helper.assertResponseStatusCode(propertiesResponse, 200);
        assertEquals(quotaInGB, propertiesResponse.value().quota());
        assertEquals(snapshotMetadata, propertiesResponse.value().metadata());
    }

    @Override
    public void getPropertiesDoesNotExist() {
        helper.assertExceptionStatusCode(() -> client.getProperties(), 404);
    }

    @Override
    public void getSnapshotPropertiesDoesNotExist() {
        helper.assertResponseStatusCode(client.create(), 201);

        helper.assertExceptionStatusCode(() -> client.getProperties("snapshot"), 400);
    }

    @Override
    public void setProperties() {
        final int initialQuoteInGB = 2;

        helper.assertResponseStatusCode(client.create(null, initialQuoteInGB), 201);

        Response<ShareProperties> propertiesResponse = client.getProperties();
        helper.assertResponseStatusCode(propertiesResponse, 200);
        assertEquals(initialQuoteInGB, propertiesResponse.value().quota());

        final int updatedQuotaInGB = 4;
        helper.assertResponseStatusCode(client.setQuota(updatedQuotaInGB), 200);

        propertiesResponse = client.getProperties();
        helper.assertResponseStatusCode(propertiesResponse, 200);
        assertEquals(updatedQuotaInGB, propertiesResponse.value().quota());
    }

    @Override
    public void setPropertiesInvalidQuota() {
        helper.assertResponseStatusCode(client.create(), 201);

        helper.assertExceptionStatusCode(() -> client.setQuota(-1), 400);
        helper.assertExceptionStatusCode(() -> client.setQuota(9999), 400);
    }

    @Override
    public void setPropertiesDoesNotExist() {
        helper.assertExceptionStatusCode(() -> client.setQuota(2), 404);
    }

    @Override
    public void getMetadata() {
        Map<String, String> metadata = Collections.singletonMap("test", "metadata");
        helper.assertResponseStatusCode(client.create(metadata, 2), 201);

        Response<ShareProperties> propertiesResponse = client.getProperties();
        helper.assertResponseStatusCode(propertiesResponse, 200);
        assertEquals(metadata, propertiesResponse.value().metadata());
    }

    @Override
    public void getSnapshotMetadata() {
        Map<String, String> metadata = Collections.singletonMap("test", "metadata");

        helper.assertResponseStatusCode(client.create(metadata, 2), 201);

        Response<ShareSnapshotInfo> snapshotInfoResponse = client.createSnapshot();
        helper.assertResponseStatusCode(snapshotInfoResponse, 201);

        Response<ShareProperties> propertiesResponse = client.getProperties(snapshotInfoResponse.value().snapshot());
        helper.assertResponseStatusCode(propertiesResponse, 200);
        assertEquals(metadata, propertiesResponse.value().metadata());
    }

    @Override
    public void getMetadataDoesNotExist() {
        helper.assertExceptionStatusCode(() -> client.getProperties(), 404);
    }

    @Override
    public void getSnapshotMetadataDoesNotExist() {
        helper.assertResponseStatusCode(client.create(), 201);

        helper.assertExceptionStatusCode(() -> client.getProperties("snapshot"), 400);
    }

    @Override
    public void setMetadata() {
        helper.assertResponseStatusCode(client.create(), 201);

        Map<String, String> metadata = Collections.singletonMap("setting", "metadata");
        helper.assertResponseStatusCode(client.setMetadata(metadata), 200);

        Response<ShareProperties> propertiesResponse = client.getProperties();
        helper.assertResponseStatusCode(propertiesResponse, 200);
        assertEquals(metadata, propertiesResponse.value().metadata());
    }

    @Override
    public void setMetadataInvalidMetadata() {
        helper.assertResponseStatusCode(client.create(), 201);

        Map<String, String> metadata = Collections.singletonMap("", "metadata");
        helper.assertExceptionStatusCode(() -> client.setMetadata(metadata), 400);
    }

    @Override
    public void setMetadataDoesNotExist() {
        Map<String, String> metadata = Collections.singletonMap("test", "metadata");
        helper.assertExceptionStatusCode(() -> client.setMetadata(metadata), 404);
    }

    @Override
    public void getPolicies() {
        helper.assertResponseStatusCode(client.create(), 201);

        Iterator<SignedIdentifier> accessPolicies = client.getAccessPolicy().iterator();
        assertFalse(accessPolicies.hasNext());
    }

    @Override
    public void getPoliciesDoesNotExist() {
        helper.assertExceptionStatusCode(() -> client.getAccessPolicy().iterator().hasNext(), 404);
    }

    @Override
    public void setPolicies() {
        helper.assertResponseStatusCode(client.create(), 201);

        AccessPolicy policy = new AccessPolicy().permission("r")
            .start(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .expiry(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC));

        SignedIdentifier permission = new SignedIdentifier().id("test")
            .accessPolicy(policy);

        helper.assertResponseStatusCode(client.setAccessPolicy(Collections.singletonList(permission)), 200);

        Iterator<SignedIdentifier> permissions = client.getAccessPolicy().iterator();
        helper.assertPermissionsAreEqual(permission, permissions.next());
        assertFalse(permissions.hasNext());
    }

    @Override
    public void setPoliciesInvalidPermission() {
        helper.assertResponseStatusCode(client.create(), 201);

        AccessPolicy policy = new AccessPolicy().permission("abcdefg")
            .start(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .expiry(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC));

        SignedIdentifier permission = new SignedIdentifier().id("test")
            .accessPolicy(policy);

        helper.assertExceptionStatusCode(() -> client.setAccessPolicy(Collections.singletonList(permission)), 400);
    }

    @Override
    public void setPoliciesTooManyPermissions() {
        helper.assertResponseStatusCode(client.create(), 201);

        List<SignedIdentifier> permissions = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            AccessPolicy policy = new AccessPolicy().permission("r")
                .start(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
                .expiry(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC));

            permissions.add(new SignedIdentifier().id("test" + i).accessPolicy(policy));
        }

        helper.assertExceptionStatusCode(() -> client.setAccessPolicy(permissions), 400);
    }

    @Override
    public void setPoliciesDoesNotExist() {
        AccessPolicy policy = new AccessPolicy().permission("r")
            .start(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .expiry(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC));

        SignedIdentifier permission = new SignedIdentifier().id("test")
            .accessPolicy(policy);

        helper.assertExceptionStatusCode(() -> client.setAccessPolicy(Collections.singletonList(permission)), 404);
    }

    @Override
    public void getStats() {
        helper.assertResponseStatusCode(client.create(), 201);

        Response<ShareStatistics> statisticsResponse = client.getStatistics();
        helper.assertResponseStatusCode(statisticsResponse, 200);
        assertEquals(0, statisticsResponse.value().getGhareUsageInGB());
    }

    @Override
    public void getStatsDoesNotExist() {
        helper.assertExceptionStatusCode(() -> client.getStatistics(), 404);
    }
}
