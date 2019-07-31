// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.rest.Response;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.file.models.AccessPolicy;
import com.azure.storage.file.models.DirectoryProperties;
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
import org.junit.Assert;

import static com.azure.storage.file.FileTestHelpers.createShareClientWithSnapshot;
import static com.azure.storage.file.FileTestHelpers.setupClient;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ShareClientTests extends ShareClientTestBase {
    private final ClientLogger shareLogger = new ClientLogger(ShareClient.class);

    private ShareClient shareClient;

    @Override
    public void beforeTest() {
        shareName = getShareName();

        if (interceptorManager.isPlaybackMode()) {
            shareClient = setupClient((connectionString, endpoint) -> new ShareClientBuilder()
                .connectionString(connectionString)
                .shareName(shareName)
                .httpClient(interceptorManager.getPlaybackClient())
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .buildClient(), true, shareLogger);
        } else {
            shareClient = setupClient((connectionString, endpoint) -> new ShareClientBuilder()
                .connectionString(connectionString)
                .shareName(shareName)
                .httpClient(HttpClient.createDefault().wiretap(true))
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .addPolicy(interceptorManager.getRecordPolicy())
                .buildClient(), false, shareLogger);
        }
    }

    @Override
    public void afterTest() {
        try {
            shareClient.delete();
        } catch (StorageErrorException ex) {
            // Ignore the exception as the share is already deleted and that is what we wanted.
        }
    }

    @Override
    public void getRootDirectoryDoesNotCreateADirectory() {
        shareClient.create();
        DirectoryClient directoryClient = shareClient.getRootDirectoryClient();
        Response<DirectoryProperties> response = directoryClient.getProperties();
        Assert.assertEquals(response.statusCode(), 200);
        Assert.assertNotNull(response.value().eTag());
    }

    @Override
    public void getDirectoryDoesNotCreateADirectory() {
        shareClient.create();
        DirectoryClient directoryClient = shareClient.getDirectoryClient("testshare");
        Assert.assertNotNull(directoryClient);
        thrown.expect(StorageErrorException.class);
        thrown.expectMessage("ResourceNotFound");
        directoryClient.getProperties();
    }

    @Override
    public void getFileClientDoesNotCreateAFile() {
        shareClient.create();
        FileClient fileClient = shareClient.getFileClient("testfile");
        Assert.assertNotNull(fileClient);
        thrown.expect(HttpResponseException.class);
        fileClient.getProperties();
    }

    @Override
    public void createDirectoryFromShareClient() {
        shareClient.create();
        FileTestHelpers.assertResponseStatusCode(shareClient.createDirectory("testshare"), 201);
    }

    @Override
    public void createDirectoryInvalidNameFromShareClient() {
        shareClient.create();
        thrown.expect(StorageErrorException.class);
        thrown.expectMessage("ParentNotFound");
        shareClient.createDirectory("test/share");
    }

    @Override
    public void createDirectoryAlreadyExistsFromShareClient() {
        shareClient.create();
        shareClient.createDirectory("testshare");
        thrown.expect(StorageErrorException.class);
        thrown.expectMessage("ResourceAlreadyExists");
        shareClient.createDirectory("testshare");
    }

    @Override
    public void deleteDirectoryFromShareClient() {
        shareClient.create();
        shareClient.createDirectory("testshare");
        FileTestHelpers.assertResponseStatusCode(shareClient.deleteDirectory("testshare"), 202);
    }

    @Override
    public void deleteDirectoryDoesNotExistFromShareClient() {
        shareClient.create();
        thrown.expect(StorageErrorException.class);
        thrown.expectMessage("ResourceNotFound");
        shareClient.deleteDirectory("testshare");
    }

    @Override
    public void createFileFromShareClient() {
        shareClient.create();
        FileTestHelpers.assertResponseStatusCode(shareClient.createFile("myFile", 1024), 201);
    }

    @Override
    public void createFileInvalidNameFromShareClient() {
        shareClient.create();
        thrown.expect(StorageErrorException.class);
        thrown.expectMessage("ParentNotFound");
        FileTestHelpers.assertResponseStatusCode(shareClient.createFile("my/File", 1024), 201);
    }

    @Override
    public void createFileAlreadyExistsFromShareClient() {
        shareClient.create();
        shareClient.createFile("myFile", 1024);
        FileTestHelpers.assertResponseStatusCode(shareClient.createFile("myFile", 1024), 201);
    }

    @Override
    public void deleteFileFromShareClient() {
        shareClient.create();
        FileTestHelpers.assertResponseStatusCode(shareClient.createFile("myFile", 1024), 201);
        FileTestHelpers.assertResponseStatusCode(shareClient.deleteFile("myFile"), 202);
    }

    @Override
    public void deleteFileDoesNotExistFromShareClient() {
        shareClient.create();
        thrown.expect(StorageErrorException.class);
        thrown.expectMessage("ResourceNotFound");
        shareClient.deleteFile("myFile");
    }

    @Override
    public void createFromShareClient() {
        FileTestHelpers.assertResponseStatusCode(shareClient.create(), 201);
    }

    @Override
    public void createTwiceSameMetadataFromShareClient() {
        Map<String, String> metadata = Collections.singletonMap("test", "metadata");

        FileTestHelpers.assertResponseStatusCode(shareClient.create(metadata, 2), 201);

        FileTestHelpers.assertExceptionStatusCode(() -> shareClient.create(metadata, 2), 409);
    }

    @Override
    public void createTwiceDifferentMetadataFromShareClient() {
        Map<String, String> metadata = Collections.singletonMap("test", "metadata");

        FileTestHelpers.assertResponseStatusCode(shareClient.create(), 201);

        FileTestHelpers.assertExceptionStatusCode(() -> shareClient.create(metadata, 2), 409);
    }

    @Override
    public void createInvalidQuotaFromShareClient() {
        FileTestHelpers.assertExceptionStatusCode(() -> shareClient.create(null, -1), 400);

        FileTestHelpers.assertExceptionStatusCode(() -> shareClient.create(null, 0), 400);
    }

    @Override
    public void deleteFromShareClient() {
        FileTestHelpers.assertResponseStatusCode(shareClient.create(), 201);

        FileTestHelpers.assertResponseStatusCode(shareClient.delete(), 202);
    }

    @Override
    public void deleteDoesNotExistFromShareClient() {
        FileTestHelpers.assertExceptionStatusCode(() -> shareClient.delete(), 404);
    }

    @Override
    public void deleteThenCreateFromShareClient() {
        FileTestHelpers.assertResponseStatusCode(shareClient.create(), 201);

        FileTestHelpers.assertResponseStatusCode(shareClient.delete(), 202);

        FileTestHelpers.sleepInRecordMode(Duration.ofSeconds(45));

        FileTestHelpers.assertResponseStatusCode(shareClient.create(), 201);
    }

    @Override
    public void deleteThenCreateTooSoonFromShareClient() {
        FileTestHelpers.assertResponseStatusCode(shareClient.create(), 201);

        FileTestHelpers.assertResponseStatusCode(shareClient.delete(), 202);

        FileTestHelpers.assertExceptionStatusCode(() -> shareClient.create(), 409);
    }

    @Override
    public void snapshot() {
        FileTestHelpers.assertResponseStatusCode(shareClient.create(), 201);

        FileTestHelpers.assertResponseStatusCode(shareClient.createSnapshot(), 201);
    }

    @Override
    public void deleteSnapshotFromShareClient() {
        FileTestHelpers.assertResponseStatusCode(shareClient.create(), 201);

        Response<ShareSnapshotInfo> snapshotInfoResponse = shareClient.createSnapshot();
        FileTestHelpers.assertResponseStatusCode(snapshotInfoResponse, 201);

        ShareClient shareClientWithSnapshot = createShareClientWithSnapshot(interceptorManager, shareName,
            snapshotInfoResponse.value().snapshot()).buildClient();
        FileTestHelpers.assertResponseStatusCode(shareClientWithSnapshot.delete(), 202);

        FileTestHelpers.assertResponseStatusCode(shareClient.createSnapshot(), 201);
    }

    @Override
    public void snapshotSameMetadata() {
        Map<String, String> metadata = Collections.singletonMap("test", "metadata");

        FileTestHelpers.assertResponseStatusCode(shareClient.create(metadata, 2), 201);

        Response<ShareSnapshotInfo> snapshotInfoResponse = shareClient.createSnapshot(metadata);
        FileTestHelpers.assertResponseStatusCode(snapshotInfoResponse, 201);

        ShareClient shareClientWithSnapshot = createShareClientWithSnapshot(interceptorManager, shareName,
            snapshotInfoResponse.value().snapshot()).buildClient();
        Response<ShareProperties> propertiesResponse = shareClientWithSnapshot.getProperties();
        FileTestHelpers.assertResponseStatusCode(propertiesResponse, 200);
        assertEquals(metadata, propertiesResponse.value().metadata());
    }

    @Override
    public void snapshotDifferentMetadata() {
        Map<String, String> createMetadata = Collections.singletonMap("create", "metadata");

        FileTestHelpers.assertResponseStatusCode(shareClient.create(createMetadata, 2), 201);

        Map<String, String> updateMetadata = Collections.singletonMap("update", "metadata");
        Response<ShareSnapshotInfo> snapshotInfoResponse = shareClient.createSnapshot(updateMetadata);
        FileTestHelpers.assertResponseStatusCode(snapshotInfoResponse, 201);

        Response<ShareProperties> propertiesResponse = shareClient.getProperties();
        FileTestHelpers.assertResponseStatusCode(propertiesResponse, 200);
        assertEquals(createMetadata, propertiesResponse.value().metadata());

        ShareClient shareClientWithSnapshot = createShareClientWithSnapshot(interceptorManager, shareName,
            snapshotInfoResponse.value().snapshot()).buildClient();
        propertiesResponse = shareClientWithSnapshot.getProperties();
        FileTestHelpers.assertResponseStatusCode(propertiesResponse, 200);
        assertEquals(updateMetadata, propertiesResponse.value().metadata());
    }

    @Override
    public void snapshotDoesNotExist() {
        FileTestHelpers.assertExceptionStatusCode(() -> shareClient.createSnapshot(), 404);
    }

    @Override
    public void getPropertiesFromShareClient() {
        final int quotaInGB = 2;
        Map<String, String> metadata = Collections.singletonMap("test", "metadata");

        FileTestHelpers.assertResponseStatusCode(shareClient.create(metadata, quotaInGB), 201);

        Response<ShareProperties> propertiesResponse = shareClient.getProperties();
        FileTestHelpers.assertResponseStatusCode(propertiesResponse, 200);
        assertEquals(quotaInGB, propertiesResponse.value().quota());
        assertEquals(metadata, propertiesResponse.value().metadata());
    }

    @Override
    public void getSnapshotPropertiesFromShareClient() {
        final int quotaInGB = 2;
        Map<String, String> snapshotMetadata = Collections.singletonMap("snapshot", "metadata");

        FileTestHelpers.assertResponseStatusCode(shareClient.create(null, quotaInGB), 201);

        Response<ShareSnapshotInfo> snapshotInfoResponse = shareClient.createSnapshot(snapshotMetadata);
        FileTestHelpers.assertResponseStatusCode(snapshotInfoResponse, 201);

        ShareClient shareClientWithSnapshot = createShareClientWithSnapshot(interceptorManager, shareName,
            snapshotInfoResponse.value().snapshot()).buildClient();
        Response<ShareProperties> propertiesResponse = shareClientWithSnapshot.getProperties();
        FileTestHelpers.assertResponseStatusCode(propertiesResponse, 200);
        assertEquals(quotaInGB, propertiesResponse.value().quota());
        assertEquals(snapshotMetadata, propertiesResponse.value().metadata());
    }

    @Override
    public void getPropertiesDoesNotExistFromShareClient() {
        FileTestHelpers.assertExceptionStatusCode(() -> shareClient.getProperties(), 404);
    }

    @Override
    public void getSnapshotPropertiesDoesNotExist() {
        FileTestHelpers.assertResponseStatusCode(shareClient.create(), 201);

        ShareClient shareClientWithSnapshot = createShareClientWithSnapshot(interceptorManager, shareName,
            "snapshot").buildClient();
        FileTestHelpers.assertExceptionStatusCode(() -> shareClientWithSnapshot.getProperties(), 400);
    }

    @Override
    public void setPropertiesFromShareClient() {
        final int initialQuoteInGB = 2;

        FileTestHelpers.assertResponseStatusCode(shareClient.create(null, initialQuoteInGB), 201);

        Response<ShareProperties> propertiesResponse = shareClient.getProperties();
        FileTestHelpers.assertResponseStatusCode(propertiesResponse, 200);
        assertEquals(initialQuoteInGB, propertiesResponse.value().quota());

        final int updatedQuotaInGB = 4;
        FileTestHelpers.assertResponseStatusCode(shareClient.setQuota(updatedQuotaInGB), 200);

        propertiesResponse = shareClient.getProperties();
        FileTestHelpers.assertResponseStatusCode(propertiesResponse, 200);
        assertEquals(updatedQuotaInGB, propertiesResponse.value().quota());
    }

    @Override
    public void setPropertiesInvalidQuotaFromShareClient() {
        FileTestHelpers.assertResponseStatusCode(shareClient.create(), 201);

        FileTestHelpers.assertExceptionStatusCode(() -> shareClient.setQuota(-1), 400);
        FileTestHelpers.assertExceptionStatusCode(() -> shareClient.setQuota(9999), 400);
    }

    @Override
    public void setPropertiesDoesNotExistFromShareClient() {
        FileTestHelpers.assertExceptionStatusCode(() -> shareClient.setQuota(2), 404);
    }

    @Override
    public void getMetadataFromShareClient() {
        Map<String, String> metadata = Collections.singletonMap("test", "metadata");
        FileTestHelpers.assertResponseStatusCode(shareClient.create(metadata, 2), 201);

        Response<ShareProperties> propertiesResponse = shareClient.getProperties();
        FileTestHelpers.assertResponseStatusCode(propertiesResponse, 200);
        assertEquals(metadata, propertiesResponse.value().metadata());
    }

    @Override
    public void getSnapshotMetadataFromShareClient() {
        Map<String, String> metadata = Collections.singletonMap("test", "metadata");

        FileTestHelpers.assertResponseStatusCode(shareClient.create(metadata, 2), 201);

        Response<ShareSnapshotInfo> snapshotInfoResponse = shareClient.createSnapshot();
        FileTestHelpers.assertResponseStatusCode(snapshotInfoResponse, 201);

        ShareClient shareClientWithSnapshot = createShareClientWithSnapshot(interceptorManager, shareName,
            snapshotInfoResponse.value().snapshot()).buildClient();
        Response<ShareProperties> propertiesResponse = shareClientWithSnapshot.getProperties();
        FileTestHelpers.assertResponseStatusCode(propertiesResponse, 200);
        assertEquals(metadata, propertiesResponse.value().metadata());
    }

    @Override
    public void getMetadataDoesNotExistFromShareClient() {
        FileTestHelpers.assertExceptionStatusCode(() -> shareClient.getProperties(), 404);
    }

    @Override
    public void getSnapshotMetadataDoesNotExistFromShareClient() {
        FileTestHelpers.assertResponseStatusCode(shareClient.create(), 201);

        ShareClient shareClientWithSnapshot = createShareClientWithSnapshot(interceptorManager, shareName,
            "snapshot").buildClient();
        FileTestHelpers.assertExceptionStatusCode(() -> shareClientWithSnapshot.getProperties(), 400);
    }

    @Override
    public void setMetadataFromShareClient() {
        FileTestHelpers.assertResponseStatusCode(shareClient.create(), 201);

        Map<String, String> metadata = Collections.singletonMap("setting", "metadata");
        FileTestHelpers.assertResponseStatusCode(shareClient.setMetadata(metadata), 200);

        Response<ShareProperties> propertiesResponse = shareClient.getProperties();
        FileTestHelpers.assertResponseStatusCode(propertiesResponse, 200);
        assertEquals(metadata, propertiesResponse.value().metadata());
    }

    @Override
    public void setMetadataInvalidMetadataFromShareClient() {
        FileTestHelpers.assertResponseStatusCode(shareClient.create(), 201);

        Map<String, String> metadata = Collections.singletonMap("", "metadata");
        FileTestHelpers.assertExceptionStatusCode(() -> shareClient.setMetadata(metadata), 400);
    }

    @Override
    public void setMetadataDoesNotExistFromShareClient() {
        Map<String, String> metadata = Collections.singletonMap("test", "metadata");
        FileTestHelpers.assertExceptionStatusCode(() -> shareClient.setMetadata(metadata), 404);
    }

    @Override
    public void getPolicies() {
        FileTestHelpers.assertResponseStatusCode(shareClient.create(), 201);

        Iterator<SignedIdentifier> accessPolicies = shareClient.getAccessPolicy().iterator();
        assertFalse(accessPolicies.hasNext());
    }

    @Override
    public void getPoliciesDoesNotExist() {
        FileTestHelpers.assertExceptionStatusCode(() -> shareClient.getAccessPolicy().iterator().hasNext(), 404);
    }

    @Override
    public void setPolicies() {
        FileTestHelpers.assertResponseStatusCode(shareClient.create(), 201);

        AccessPolicy policy = new AccessPolicy().permission("r")
            .start(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .expiry(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC));

        SignedIdentifier permission = new SignedIdentifier().id("test")
            .accessPolicy(policy);

        FileTestHelpers.assertResponseStatusCode(shareClient.setAccessPolicy(Collections.singletonList(permission)), 200);

        Iterator<SignedIdentifier> permissions = shareClient.getAccessPolicy().iterator();
        FileTestHelpers.assertPermissionsAreEqual(permission, permissions.next());
        assertFalse(permissions.hasNext());
    }

    @Override
    public void setPoliciesInvalidPermission() {
        FileTestHelpers.assertResponseStatusCode(shareClient.create(), 201);

        AccessPolicy policy = new AccessPolicy().permission("abcdefg")
            .start(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .expiry(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC));

        SignedIdentifier permission = new SignedIdentifier().id("test")
            .accessPolicy(policy);

        FileTestHelpers.assertExceptionStatusCode(() -> shareClient.setAccessPolicy(Collections.singletonList(permission)), 400);
    }

    @Override
    public void setPoliciesTooManyPermissions() {
        FileTestHelpers.assertResponseStatusCode(shareClient.create(), 201);

        List<SignedIdentifier> permissions = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            AccessPolicy policy = new AccessPolicy().permission("r")
                .start(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
                .expiry(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC));

            permissions.add(new SignedIdentifier().id("test" + i).accessPolicy(policy));
        }

        FileTestHelpers.assertExceptionStatusCode(() -> shareClient.setAccessPolicy(permissions), 400);
    }

    @Override
    public void setPoliciesDoesNotExist() {
        AccessPolicy policy = new AccessPolicy().permission("r")
            .start(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .expiry(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC));

        SignedIdentifier permission = new SignedIdentifier().id("test")
            .accessPolicy(policy);

        FileTestHelpers.assertExceptionStatusCode(() -> shareClient.setAccessPolicy(Collections.singletonList(permission)), 404);
    }

    @Override
    public void getStats() {
        FileTestHelpers.assertResponseStatusCode(shareClient.create(), 201);

        Response<ShareStatistics> statisticsResponse = shareClient.getStatistics();
        FileTestHelpers.assertResponseStatusCode(statisticsResponse, 200);
        assertEquals(0, statisticsResponse.value().getShareUsageInGB());
    }

    @Override
    public void getStatsDoesNotExist() {
        FileTestHelpers.assertExceptionStatusCode(() -> shareClient.getStatistics(), 404);
    }

    @Override
    public void getSnapshotId() {
        shareClient.create();
        String actualSnapshot = shareClient.createSnapshot().value().snapshot();
        ShareClient shareClientWithSnapshot = createShareClientWithSnapshot(interceptorManager,
            shareName, actualSnapshot).buildClient();
        Assert.assertEquals(actualSnapshot, shareClientWithSnapshot.getSnapshotId());
    }
}
