// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.rest.Response;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.file.models.AccessPolicy;
import com.azure.storage.file.models.ShareSnapshotInfo;
import com.azure.storage.file.models.SignedIdentifier;
import com.azure.storage.file.models.StorageErrorException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import reactor.test.StepVerifier;

import static com.azure.storage.file.FileTestHelpers.createShareClientWithSnapshot;
import static com.azure.storage.file.FileTestHelpers.setupClient;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ShareAsyncClientTests extends ShareClientTestBase {
    private final ClientLogger shareAsyncLogger = new ClientLogger(ShareAsyncClientTests.class);

    private ShareAsyncClient shareAsyncClient;

    @Override
    public void beforeTest() {
        shareName = getShareName();

        if (interceptorManager.isPlaybackMode()) {
            shareAsyncClient = setupClient((connectionString, endpoint) -> new ShareClientBuilder()
                .connectionString(connectionString)
                .shareName(shareName)
                .httpClient(interceptorManager.getPlaybackClient())
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .buildAsyncClient(), true, shareAsyncLogger);
        } else {
            shareAsyncClient = setupClient((connectionString, endpoint) -> new ShareClientBuilder()
                .connectionString(connectionString)
                .shareName(shareName)
                .httpClient(HttpClient.createDefault().wiretap(true))
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .addPolicy(interceptorManager.getRecordPolicy())
                .buildAsyncClient(), false, shareAsyncLogger);
        }
    }

    @Override
    public void afterTest() {
        try {
            shareAsyncClient.delete().block();
        } catch (StorageErrorException ex) {
            // Ignore the exception as the share is already deleted and that is what we wanted.
        }
    }

    @Override
    public void getRootDirectoryDoesNotCreateADirectory() {
        shareAsyncClient.create().block();
        DirectoryAsyncClient directoryAsyncClient = shareAsyncClient.getRootDirectoryClient();
        StepVerifier.create(directoryAsyncClient.getProperties())
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 200))
            .verifyComplete();
    }

    @Override
    public void getDirectoryDoesNotCreateADirectory() {
        shareAsyncClient.create().block();
        DirectoryAsyncClient directoryAsyncClient = shareAsyncClient.getDirectoryClient("testshare");
        Assert.assertNotNull(directoryAsyncClient);
        StepVerifier.create(directoryAsyncClient.getProperties())
            .verifyErrorSatisfies(response -> FileTestHelpers.assertExceptionStatusCode(response, 404));
    }

    @Override
    public void getFileClientDoesNotCreateAFile() {
        shareAsyncClient.create().block();
        FileAsyncClient fileAsyncClient = shareAsyncClient.getFileClient("testfile");
        Assert.assertNotNull(fileAsyncClient);
        StepVerifier.create(fileAsyncClient.getProperties())
            .verifyErrorSatisfies(response -> Assert.assertTrue(response instanceof HttpResponseException));
    }

    @Override
    public void createDirectoryFromShareClient() {
        shareAsyncClient.create().block();
        StepVerifier.create(shareAsyncClient.createDirectory("testshare"))
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();
    }

    @Override
    public void createDirectoryInvalidNameFromShareClient() {
        shareAsyncClient.create().block();
        StepVerifier.create(shareAsyncClient.createDirectory("test/share"))
            .verifyErrorSatisfies(response -> FileTestHelpers.assertExceptionStatusCode(response, 404));
    }

    @Override
    public void createDirectoryAlreadyExistsFromShareClient() {
        shareAsyncClient.create().block();
        shareAsyncClient.createDirectory("testshare").block();
        StepVerifier.create(shareAsyncClient.createDirectory("testshare"))
            .verifyErrorSatisfies(response -> FileTestHelpers.assertExceptionStatusCode(response, 409));
    }

    @Override
    public void deleteDirectoryFromShareClient() {
        shareAsyncClient.create().block();
        shareAsyncClient.createDirectory("testshare").block();
        StepVerifier.create(shareAsyncClient.deleteDirectory("testshare"))
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 202))
            .verifyComplete();
    }

    @Override
    public void deleteDirectoryDoesNotExistFromShareClient() {
        shareAsyncClient.create().block();
        StepVerifier.create(shareAsyncClient.deleteDirectory("testshare"))
            .verifyErrorSatisfies(response -> FileTestHelpers.assertExceptionStatusCode(response, 404));
    }

    @Override
    public void createFileFromShareClient() {
        shareAsyncClient.create().block();
        StepVerifier.create(shareAsyncClient.createFile("myFile", 1024))
            .assertNext(fileAsyncClientResponse -> FileTestHelpers.assertResponseStatusCode(fileAsyncClientResponse, 201))
            .verifyComplete();
    }

    @Override
    public void createFileInvalidNameFromShareClient() {
        shareAsyncClient.create().block();
        StepVerifier.create(shareAsyncClient.createFile("my/File", 1024))
            .verifyErrorSatisfies(response -> FileTestHelpers.assertExceptionStatusCode(response, 404));
    }

    @Override
    public void createFileAlreadyExistsFromShareClient() {
        shareAsyncClient.create().block();
        shareAsyncClient.createFile("myFile", 1024).block();
        StepVerifier.create(shareAsyncClient.createFile("myFile", 1024))
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();
    }

    @Override
    public void deleteFileFromShareClient() {
        shareAsyncClient.create().block();
        StepVerifier.create(shareAsyncClient.createFile("myFile", 1024))
            .assertNext(fileAsyncClientResponse -> FileTestHelpers.assertResponseStatusCode(fileAsyncClientResponse, 201))
            .verifyComplete();
        StepVerifier.create(shareAsyncClient.deleteFile("myFile"))
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 202))
            .verifyComplete();
    }

    @Override
    public void deleteFileDoesNotExistFromShareClient() {
        shareAsyncClient.create().block();
        StepVerifier.create(shareAsyncClient.deleteFile("myFile"))
            .verifyErrorSatisfies(response -> FileTestHelpers.assertExceptionStatusCode(response, 404));
    }

    @Override
    public void createFromShareClient() {
        StepVerifier.create(shareAsyncClient.create())
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();
    }

    @Override
    public void createTwiceSameMetadataFromShareClient() {
        Map<String, String> metadata = Collections.singletonMap("test", "metadata");

        StepVerifier.create(shareAsyncClient.create(metadata, 2))
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(shareAsyncClient.create(metadata, 2))
            .verifyErrorSatisfies(throwable -> FileTestHelpers.assertExceptionStatusCode(throwable, 409));
    }

    @Override
    public void createTwiceDifferentMetadataFromShareClient() {
        Map<String, String> metadata = Collections.singletonMap("test", "metadata");

        StepVerifier.create(shareAsyncClient.create())
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(shareAsyncClient.create(metadata, 2))
            .verifyErrorSatisfies(throwable -> FileTestHelpers.assertExceptionStatusCode(throwable, 409));
    }

    @Override
    public void createInvalidQuotaFromShareClient() {
        StepVerifier.create(shareAsyncClient.create(null, -1))
            .verifyErrorSatisfies(throwable -> FileTestHelpers.assertExceptionStatusCode(throwable, 400));

        StepVerifier.create(shareAsyncClient.create(null, 0))
            .verifyErrorSatisfies(throwable -> FileTestHelpers.assertExceptionStatusCode(throwable, 400));
    }

    @Override
    public void deleteFromShareClient() {
        StepVerifier.create(shareAsyncClient.create())
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(shareAsyncClient.delete())
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 202))
            .verifyComplete();
    }

    @Override
    public void deleteDoesNotExistFromShareClient() {
        StepVerifier.create(shareAsyncClient.delete())
            .verifyErrorSatisfies(throwable -> FileTestHelpers.assertExceptionStatusCode(throwable, 404));
    }

    @Override
    public void deleteThenCreateFromShareClient() {
        StepVerifier.create(shareAsyncClient.create())
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(shareAsyncClient.delete())
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 202))
            .verifyComplete();

        FileTestHelpers.sleepInRecordMode(Duration.ofSeconds(45));

        StepVerifier.create(shareAsyncClient.create())
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();
    }

    @Override
    public void deleteThenCreateTooSoonFromShareClient() {
        StepVerifier.create(shareAsyncClient.create())
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(shareAsyncClient.delete())
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 202))
            .verifyComplete();

        StepVerifier.create(shareAsyncClient.create())
            .verifyErrorSatisfies(throwable -> FileTestHelpers.assertExceptionStatusCode(throwable, 409));
    }

    @Override
    public void snapshot() {
        StepVerifier.create(shareAsyncClient.create())
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(shareAsyncClient.createSnapshot())
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();
    }

    @Override
    public void deleteSnapshotFromShareClient() {
        StepVerifier.create(shareAsyncClient.create())
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();

        Response<ShareSnapshotInfo> snapshotInfoResponse = shareAsyncClient.createSnapshot().block();
        assertNotNull(snapshotInfoResponse);
        FileTestHelpers.assertResponseStatusCode(snapshotInfoResponse, 201);

        ShareAsyncClient shareAsyncClientWithSnapshot = createShareClientWithSnapshot(
            interceptorManager, shareName, snapshotInfoResponse.value().snapshot()).buildAsyncClient();
        StepVerifier.create(shareAsyncClientWithSnapshot.delete())
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 202))
            .verifyComplete();

        StepVerifier.create(shareAsyncClient.createSnapshot())
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();
    }

    @Override
    public void snapshotSameMetadata() {
        Map<String, String> metadata = Collections.singletonMap("test", "metadata");

        StepVerifier.create(shareAsyncClient.create(metadata, 2))
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();

        Response<ShareSnapshotInfo> snapshotInfoResponse = shareAsyncClient.createSnapshot(metadata).block();
        assertNotNull(snapshotInfoResponse);
        FileTestHelpers.assertResponseStatusCode(snapshotInfoResponse, 201);

        ShareAsyncClient shareAsyncClientWithSnapshot = createShareClientWithSnapshot(
            interceptorManager, shareName, snapshotInfoResponse.value().snapshot()).buildAsyncClient();
        StepVerifier.create(shareAsyncClientWithSnapshot.getProperties())
            .assertNext(response -> {
                FileTestHelpers.assertResponseStatusCode(response, 200);
                assertEquals(metadata, response.value().metadata());
            })
            .verifyComplete();
    }

    @Override
    public void snapshotDifferentMetadata() {
        Map<String, String> createMetadata = Collections.singletonMap("create", "metadata");

        StepVerifier.create(shareAsyncClient.create(createMetadata, 2))
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();

        Map<String, String> updateMetadata = Collections.singletonMap("update", "metadata");
        Response<ShareSnapshotInfo> snapshotInfoResponse = shareAsyncClient.createSnapshot(updateMetadata).block();
        assertNotNull(snapshotInfoResponse);
        FileTestHelpers.assertResponseStatusCode(snapshotInfoResponse, 201);

        StepVerifier.create(shareAsyncClient.getProperties())
            .assertNext(response -> {
                FileTestHelpers.assertResponseStatusCode(response, 200);
                assertEquals(createMetadata, response.value().metadata());
            })
            .verifyComplete();
        ShareAsyncClient shareAsyncClientWithSnapshot = createShareClientWithSnapshot(interceptorManager,
            shareName, snapshotInfoResponse.value().snapshot()).buildAsyncClient();

        StepVerifier.create(shareAsyncClientWithSnapshot.getProperties())
            .assertNext(response -> {
                FileTestHelpers.assertResponseStatusCode(response, 200);
                assertEquals(updateMetadata, response.value().metadata());
            })
            .verifyComplete();
    }

    @Override
    public void snapshotDoesNotExist() {
        StepVerifier.create(shareAsyncClient.createSnapshot())
            .verifyErrorSatisfies(throwable -> FileTestHelpers.assertExceptionStatusCode(throwable, 404));
    }

    @Override
    public void getPropertiesFromShareClient() {
        final int quotaInGB = 2;
        Map<String, String> metadata = Collections.singletonMap("test", "metadata");

        StepVerifier.create(shareAsyncClient.create(metadata, quotaInGB))
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(shareAsyncClient.getProperties())
            .assertNext(response -> {
                FileTestHelpers.assertResponseStatusCode(response, 200);
                assertEquals(quotaInGB, response.value().quota());
                assertEquals(metadata, response.value().metadata());
            })
            .verifyComplete();
    }

    @Override
    public void getSnapshotPropertiesFromShareClient() {
        final int quotaInGB = 2;
        Map<String, String> snapshotMetadata = Collections.singletonMap("snapshot", "metadata");

        StepVerifier.create(shareAsyncClient.create(null, quotaInGB))
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();

        Response<ShareSnapshotInfo> snapshotInfoResponse = shareAsyncClient.createSnapshot(snapshotMetadata).block();
        assertNotNull(snapshotInfoResponse);
        FileTestHelpers.assertResponseStatusCode(snapshotInfoResponse, 201);
        ShareAsyncClient shareAsyncClientWithSnapshot = createShareClientWithSnapshot(interceptorManager,
            shareName, snapshotInfoResponse.value().snapshot()).buildAsyncClient();
        StepVerifier.create(shareAsyncClientWithSnapshot.getProperties())
            .assertNext(response -> {
                FileTestHelpers.assertResponseStatusCode(response, 200);
                assertEquals(quotaInGB, response.value().quota());
                assertEquals(snapshotMetadata, response.value().metadata());
            })
            .verifyComplete();
    }

    @Override
    public void getPropertiesDoesNotExistFromShareClient() {
        StepVerifier.create(shareAsyncClient.getProperties())
            .verifyErrorSatisfies(throwable -> FileTestHelpers.assertExceptionStatusCode(throwable, 404));
    }

    @Override
    public void getSnapshotPropertiesDoesNotExist() {
        StepVerifier.create(shareAsyncClient.create())
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();
        ShareAsyncClient shareAsyncClientWithSnapshot = createShareClientWithSnapshot(interceptorManager,
            shareName, "snapshot").buildAsyncClient();
        StepVerifier.create(shareAsyncClientWithSnapshot.getProperties())
            .verifyErrorSatisfies(throwable -> FileTestHelpers.assertExceptionStatusCode(throwable, 400));
    }

    @Override
    public void setPropertiesFromShareClient() {
        final int initialQuoteInGB = 2;

        StepVerifier.create(shareAsyncClient.create(null, initialQuoteInGB))
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(shareAsyncClient.getProperties())
            .assertNext(response -> {
                FileTestHelpers.assertResponseStatusCode(response, 200);
                assertEquals(initialQuoteInGB, response.value().quota());
            })
            .verifyComplete();

        final int updatedQuotaInGB = 4;
        StepVerifier.create(shareAsyncClient.setQuota(updatedQuotaInGB))
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 200))
            .verifyComplete();

        StepVerifier.create(shareAsyncClient.getProperties())
            .assertNext(response -> {
                FileTestHelpers.assertResponseStatusCode(response, 200);
                assertEquals(updatedQuotaInGB, response.value().quota());
            })
            .verifyComplete();
    }

    @Override
    public void setPropertiesInvalidQuotaFromShareClient() {
        StepVerifier.create(shareAsyncClient.create())
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(shareAsyncClient.setQuota(-1))
            .verifyErrorSatisfies(throwable -> FileTestHelpers.assertExceptionStatusCode(throwable, 400));

        StepVerifier.create(shareAsyncClient.setQuota(9999))
            .verifyErrorSatisfies(throwable -> FileTestHelpers.assertExceptionStatusCode(throwable, 400));
    }

    @Override
    public void setPropertiesDoesNotExistFromShareClient() {
        StepVerifier.create(shareAsyncClient.setQuota(2))
            .verifyErrorSatisfies(throwable -> FileTestHelpers.assertExceptionStatusCode(throwable, 404));
    }

    @Override
    public void getMetadataFromShareClient() {
        Map<String, String> metadata = Collections.singletonMap("test", "metadata");
        StepVerifier.create(shareAsyncClient.create(metadata, 2))
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(shareAsyncClient.getProperties())
            .assertNext(response -> {
                FileTestHelpers.assertResponseStatusCode(response, 200);
                assertEquals(metadata, response.value().metadata());
            })
            .verifyComplete();
    }

    @Override
    public void getSnapshotMetadataFromShareClient() {
        Map<String, String> metadata = Collections.singletonMap("test", "metadata");
        StepVerifier.create(shareAsyncClient.create(metadata, 2))
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();

        Response<ShareSnapshotInfo> snapshotInfoResponse = shareAsyncClient.createSnapshot().block();
        assertNotNull(snapshotInfoResponse);
        FileTestHelpers.assertResponseStatusCode(snapshotInfoResponse, 201);

        ShareAsyncClient shareAsyncClientWithSnapshot = createShareClientWithSnapshot(interceptorManager,
            shareName, snapshotInfoResponse.value().snapshot()).buildAsyncClient();
        StepVerifier.create(shareAsyncClientWithSnapshot.getProperties())
            .assertNext(response -> {
                FileTestHelpers.assertResponseStatusCode(response, 200);
                assertEquals(metadata, response.value().metadata());
            })
            .verifyComplete();
    }

    @Override
    public void getMetadataDoesNotExistFromShareClient() {
        StepVerifier.create(shareAsyncClient.getProperties())
            .verifyErrorSatisfies(throwable -> FileTestHelpers.assertExceptionStatusCode(throwable, 404));
    }

    @Override
    public void getSnapshotMetadataDoesNotExistFromShareClient() {
        StepVerifier.create(shareAsyncClient.create())
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();
        ShareAsyncClient shareAsyncClientWithSnapshot = createShareClientWithSnapshot(interceptorManager,
            shareName, "snapshot").buildAsyncClient();
        StepVerifier.create(shareAsyncClientWithSnapshot.getProperties())
            .verifyErrorSatisfies(throwable -> FileTestHelpers.assertExceptionStatusCode(throwable, 400));
    }

    @Override
    public void setMetadataFromShareClient() {
        StepVerifier.create(shareAsyncClient.create())
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();

        Map<String, String> metadata = Collections.singletonMap("setting", "metadata");
        StepVerifier.create(shareAsyncClient.setMetadata(metadata))
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 200))
            .verifyComplete();

        StepVerifier.create(shareAsyncClient.getProperties())
            .assertNext(response -> {
                FileTestHelpers.assertResponseStatusCode(response, 200);
                assertEquals(metadata, response.value().metadata());
            })
            .verifyComplete();
    }

    @Override
    public void setMetadataInvalidMetadataFromShareClient() {
        StepVerifier.create(shareAsyncClient.create())
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();

        Map<String, String> metadata = Collections.singletonMap("", "metadata");
        StepVerifier.create(shareAsyncClient.setMetadata(metadata))
            .verifyErrorSatisfies(throwable -> FileTestHelpers.assertExceptionStatusCode(throwable, 400));
    }

    @Override
    public void setMetadataDoesNotExistFromShareClient() {
        Map<String, String> metadata = Collections.singletonMap("test", "metadata");
        StepVerifier.create(shareAsyncClient.setMetadata(metadata))
            .verifyErrorSatisfies(throwable -> FileTestHelpers.assertExceptionStatusCode(throwable, 404));
    }

    @Override
    public void getPolicies() {
        StepVerifier.create(shareAsyncClient.create())
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(shareAsyncClient.getAccessPolicy())
            .expectNextCount(0)
            .verifyComplete();
    }

    @Override
    public void getPoliciesDoesNotExist() {
        StepVerifier.create(shareAsyncClient.getAccessPolicy())
            .verifyErrorSatisfies(throwable -> FileTestHelpers.assertExceptionStatusCode(throwable, 404));
    }

    @Override
    public void setPolicies() {
        StepVerifier.create(shareAsyncClient.create())
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();

        AccessPolicy policy = new AccessPolicy().permission("r")
            .start(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .expiry(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC));

        SignedIdentifier permission = new SignedIdentifier().id("test")
            .accessPolicy(policy);

        StepVerifier.create(shareAsyncClient.setAccessPolicy(Collections.singletonList(permission)))
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 200))
            .verifyComplete();

        StepVerifier.create(shareAsyncClient.getAccessPolicy())
            .assertNext(responsePermission -> FileTestHelpers.assertPermissionsAreEqual(permission, responsePermission))
            .verifyComplete();
    }

    @Override
    public void setPoliciesInvalidPermission() {
        StepVerifier.create(shareAsyncClient.create())
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();

        AccessPolicy policy = new AccessPolicy().permission("abcdefg")
            .start(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .expiry(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC));

        SignedIdentifier permission = new SignedIdentifier().id("test")
            .accessPolicy(policy);

        StepVerifier.create(shareAsyncClient.setAccessPolicy(Collections.singletonList(permission)))
            .verifyErrorSatisfies(throwable -> FileTestHelpers.assertExceptionStatusCode(throwable, 400));
    }

    @Override
    public void setPoliciesTooManyPermissions() {
        StepVerifier.create(shareAsyncClient.create())
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();

        List<SignedIdentifier> permissions = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            AccessPolicy policy = new AccessPolicy().permission("r")
                .start(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
                .expiry(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC));

            permissions.add(new SignedIdentifier().id("test" + i).accessPolicy(policy));
        }

        StepVerifier.create(shareAsyncClient.setAccessPolicy(permissions))
            .verifyErrorSatisfies(throwable -> FileTestHelpers.assertExceptionStatusCode(throwable, 400));
    }

    @Override
    public void setPoliciesDoesNotExist() {
        AccessPolicy policy = new AccessPolicy().permission("r")
            .start(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .expiry(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC));

        SignedIdentifier permission = new SignedIdentifier().id("test")
            .accessPolicy(policy);

        StepVerifier.create(shareAsyncClient.setAccessPolicy(Collections.singletonList(permission)))
            .verifyErrorSatisfies(throwable -> FileTestHelpers.assertExceptionStatusCode(throwable, 404));
    }

    @Override
    public void getStats() {
        StepVerifier.create(shareAsyncClient.create())
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(shareAsyncClient.getStatistics())
            .assertNext(response -> {
                FileTestHelpers.assertResponseStatusCode(response, 200);
                assertEquals(0, response.value().getShareUsageInGB());
            })
            .verifyComplete();
    }

    @Override
    public void getStatsDoesNotExist() {
        StepVerifier.create(shareAsyncClient.getStatistics())
            .verifyErrorSatisfies(throwable -> FileTestHelpers.assertExceptionStatusCode(throwable, 404));
    }

    @Override
    public void getSnapshotId() {
        shareAsyncClient.create().block();
        StepVerifier.create(shareAsyncClient.createSnapshot())
            .assertNext(response -> {
                ShareAsyncClient shareAsyncClientWithSnapshot = createShareClientWithSnapshot(interceptorManager,
                    shareName, response.value().snapshot()).buildAsyncClient();
                Assert.assertEquals(response.value().snapshot(), shareAsyncClientWithSnapshot.getSnapshotId());
            })
            .verifyComplete();

    }
}
