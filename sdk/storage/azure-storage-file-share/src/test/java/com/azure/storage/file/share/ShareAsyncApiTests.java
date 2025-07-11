// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.http.rest.Response;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import com.azure.storage.common.test.shared.extensions.PlaybackOnly;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import com.azure.storage.common.test.shared.policy.InvalidServiceVersionPipelinePolicy;
import com.azure.storage.file.share.implementation.util.ModelHelper;
import com.azure.storage.file.share.models.FilePermissionFormat;
import com.azure.storage.file.share.models.NtfsFileAttributes;
import com.azure.storage.file.share.models.ShareAccessTier;
import com.azure.storage.file.share.models.ShareAudience;
import com.azure.storage.file.share.models.ShareErrorCode;
import com.azure.storage.file.share.models.ShareFileHttpHeaders;
import com.azure.storage.file.share.models.ShareFilePermission;
import com.azure.storage.file.share.models.ShareInfo;
import com.azure.storage.file.share.models.ShareProperties;
import com.azure.storage.file.share.models.ShareProtocols;
import com.azure.storage.file.share.models.ShareRequestConditions;
import com.azure.storage.file.share.models.ShareRootSquash;
import com.azure.storage.file.share.models.ShareStorageException;
import com.azure.storage.file.share.models.ShareTokenIntent;
import com.azure.storage.file.share.options.ShareCreateOptions;
import com.azure.storage.file.share.options.ShareDirectoryCreateOptions;
import com.azure.storage.file.share.options.ShareSetPropertiesOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.azure.storage.common.implementation.StorageImplUtils.INVALID_VERSION_HEADER_MESSAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ShareAsyncApiTests extends FileShareTestBase {
    private ShareAsyncClient primaryShareAsyncClient;
    private String shareName;
    private static Map<String, String> testMetadata;
    private FileSmbProperties smbProperties;
    private static final String FILE_PERMISSION
        = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-188441444-3053964)S:NO_ACCESS_CONTROL";

    @BeforeEach
    public void setup() {
        shareName = generateShareName();
        primaryFileServiceAsyncClient = fileServiceBuilderHelper().buildAsyncClient();
        primaryShareAsyncClient = primaryFileServiceAsyncClient.getShareAsyncClient(shareName);
        testMetadata = Collections.singletonMap("testmetadata", "value");
        smbProperties = new FileSmbProperties().setNtfsFileAttributes(EnumSet.of(NtfsFileAttributes.NORMAL));
    }

    @Test
    public void getShareURL() {
        String accountName
            = StorageSharedKeyCredential.fromConnectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString())
                .getAccountName();
        String expectURL = String.format("https://%s.file.core.windows.net/%s", accountName, shareName);
        String shareURL = primaryShareAsyncClient.getShareUrl();
        assertEquals(expectURL, shareURL);
    }

    @Test
    public void getRootDirectoryClient() {
        ShareDirectoryAsyncClient directoryClient = primaryShareAsyncClient.getRootDirectoryClient();
        assertInstanceOf(ShareDirectoryAsyncClient.class, directoryClient);
    }

    @Test
    public void getFileClientDoesNotCreateAFile() {
        ShareFileAsyncClient fileClient = primaryShareAsyncClient.getFileClient("testFile");
        assertInstanceOf(ShareFileAsyncClient.class, fileClient);
    }

    @Test
    public void createShare() {
        StepVerifier.create(primaryShareAsyncClient.createWithResponse(null, (Integer) null))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-08-04")
    @Test
    public void createShareSasError() {
        ShareServiceAsyncClient unauthorizedServiceClient
            = fileServiceBuilderHelper().sasToken("sig=dummyToken").buildAsyncClient();

        ShareAsyncClient share = unauthorizedServiceClient.getShareAsyncClient(generateShareName());

        StepVerifier.create(share.create()).verifyErrorSatisfies(r -> {
            ShareStorageException e = assertInstanceOf(ShareStorageException.class, r);
            assertEquals(ShareErrorCode.AUTHENTICATION_FAILED, e.getErrorCode());
            assertTrue(e.getServiceMessage().contains("AuthenticationErrorDetail"));
        });
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @Test
    public void createShareOAuth() {
        ShareServiceAsyncClient oAuthServiceClient
            = getOAuthServiceAsyncClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareAsyncClient shareClient = oAuthServiceClient.getShareAsyncClient(shareName);

        StepVerifier.create(shareClient.createWithResponse(null, (Integer) null))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createShareWithArgsSupplier")
    public void createShareWithArgs(Map<String, String> metadata, Integer quota) {
        StepVerifier.create(primaryShareAsyncClient.createWithResponse(metadata, quota))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();
    }

    private static Stream<Arguments> createShareWithArgsSupplier() {
        return Stream.of(Arguments.of(null, null), Arguments.of(null, 1), Arguments.of(testMetadata, null),
            Arguments.of(testMetadata, 1), Arguments.of(testMetadata, 6000));
    }

    @ParameterizedTest
    @MethodSource("createShareWithInvalidArgsSupplier")
    public void createShareWithInvalidArgs(Map<String, String> metadata, Integer quota, int statusCode,
        ShareErrorCode errMessage) {
        StepVerifier.create(primaryShareAsyncClient.createWithResponse(metadata, quota))
            .verifyErrorSatisfies(
                it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, statusCode, errMessage));
    }

    private static Stream<Arguments> createShareWithInvalidArgsSupplier() {
        return Stream.of(Arguments.of(Collections.singletonMap("", "value"), 1, 400, ShareErrorCode.EMPTY_METADATA_KEY),
            Arguments.of(Collections.singletonMap("metadata!", "value"), 1, 400, ShareErrorCode.INVALID_METADATA));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2019-12-12")
    @Test
    public void createShareAccessTierPremium() {
        ShareAsyncClient client = premiumFileServiceAsyncClient.getShareAsyncClient(generateShareName());
        ShareCreateOptions options = new ShareCreateOptions().setAccessTier(ShareAccessTier.PREMIUM);

        StepVerifier.create(client.createWithResponse(options).then(client.getProperties()))
            .assertNext(r -> assertEquals(ShareAccessTier.PREMIUM.toString(), r.getAccessTier()))
            .verifyComplete();

        //cleanup
        client.delete().block();
    }

    @Test
    public void createSnapshot() {
        StepVerifier
            .create(primaryShareAsyncClient.create().then(primaryShareAsyncClient.createSnapshotWithResponse(null)))
            .assertNext(it -> {
                FileShareTestHelper.assertResponseStatusCode(it, 201);
                String shareSnapshotName = generatePathName();
                ShareClient shareSnapshotClient = new ShareClientBuilder().shareName(shareSnapshotName)
                    .connectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString())
                    .snapshot(it.getValue().getSnapshot())
                    .buildClient();
                assertEquals(it.getValue().getSnapshot(), shareSnapshotClient.getSnapshotId());
            })
            .verifyComplete();
    }

    @Test
    public void createSnapshotError() {
        StepVerifier.create(primaryShareAsyncClient.createSnapshot())
            .verifyErrorSatisfies(
                it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.SHARE_NOT_FOUND));
    }

    @Test
    public void createSnapshotMetadata() {
        String shareSnapshotName = generatePathName();
        StepVerifier
            .create(
                primaryShareAsyncClient.create().then(primaryShareAsyncClient.createSnapshotWithResponse(testMetadata)))
            .assertNext(it -> {
                FileShareTestHelper.assertResponseStatusCode(it, 201);
                ShareClient shareSnapshotClient = new ShareClientBuilder().shareName(shareSnapshotName)
                    .connectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString())
                    .snapshot(it.getValue().getSnapshot())
                    .buildClient();
                assertEquals(it.getValue().getSnapshot(), shareSnapshotClient.getSnapshotId());
            })
            .verifyComplete();
    }

    @Test
    public void createSnapshotMetadataError() {
        StepVerifier
            .create(primaryShareAsyncClient.create()
                .then(primaryShareAsyncClient.createSnapshotWithResponse(Collections.singletonMap("", "value"))))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 400,
                ShareErrorCode.EMPTY_METADATA_KEY));
    }

    @Test
    public void createIfNotExistsShare() {
        StepVerifier.create(primaryShareAsyncClient.createIfNotExistsWithResponse(null, null))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsShareThatAlreadyExists() {
        ShareAsyncClient client = premiumFileServiceAsyncClient.getShareAsyncClient(generateShareName());
        Mono<Response<ShareInfo>> initialResponse = client.createIfNotExistsWithResponse(new ShareCreateOptions());
        Mono<Response<ShareInfo>> secondResponse = client.createIfNotExistsWithResponse(new ShareCreateOptions());

        StepVerifier.create(initialResponse).assertNext(response -> {
            assertNotNull(response);
            FileShareTestHelper.assertResponseStatusCode(response, 201);
        }).verifyComplete();

        StepVerifier.create(secondResponse).assertNext(response -> {
            assertNotNull(response);
            FileShareTestHelper.assertResponseStatusCode(response, 409);
        }).verifyComplete();

        //cleanup
        client.delete().block();
    }

    @ParameterizedTest
    @MethodSource("createIfNotExistsShareWithArgsSupplier")
    public void createIfNotExistsShareWithArgs(Map<String, String> metadata, Integer quota) {
        StepVerifier
            .create(primaryShareAsyncClient
                .createIfNotExistsWithResponse(new ShareCreateOptions().setMetadata(metadata).setQuotaInGb(quota)))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();
    }

    private static Stream<Arguments> createIfNotExistsShareWithArgsSupplier() {
        return Stream.of(Arguments.of(null, null), Arguments.of(null, 1), Arguments.of(testMetadata, null),
            Arguments.of(testMetadata, 1));
    }

    @ParameterizedTest
    @MethodSource("createShareWithInvalidArgsSupplier")
    public void createIfNotExistsShareWithInvalidArgs(Map<String, String> metadata, Integer quota, int statusCode,
        ShareErrorCode errMessage) {
        StepVerifier
            .create(primaryShareAsyncClient
                .createIfNotExistsWithResponse(new ShareCreateOptions().setMetadata(metadata).setQuotaInGb(quota)))
            .verifyErrorSatisfies(
                it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, statusCode, errMessage));

    }

    @Test
    public void deleteShare() {
        StepVerifier.create(primaryShareAsyncClient.create().then(primaryShareAsyncClient.deleteWithResponse()))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 202))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @Test
    public void deleteShareOAuth() {
        ShareServiceAsyncClient oAuthServiceClient
            = getOAuthServiceAsyncClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareAsyncClient shareClient = oAuthServiceClient.getShareAsyncClient(shareName);

        StepVerifier.create(primaryShareAsyncClient.create().then(shareClient.deleteWithResponse()))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201));
    }

    @Test
    public void deleteShareError() {
        StepVerifier.create(primaryShareAsyncClient.delete())
            .verifyErrorSatisfies(
                it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.SHARE_NOT_FOUND));
    }

    @Test
    public void deleteIfExistsShare() {
        StepVerifier
            .create(primaryShareAsyncClient.create().then(primaryShareAsyncClient.deleteIfExistsWithResponse(null)))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 202))
            .verifyComplete();
    }

    @Test
    public void deleteIfExistsShareThatDoesNotExist() {
        ShareAsyncClient client = premiumFileServiceAsyncClient.getShareAsyncClient(generateShareName());

        StepVerifier.create(client.deleteIfExistsWithResponse(null, null)).assertNext(response -> {
            assertNotNull(response);
            assertFalse(response.getValue());
            FileShareTestHelper.assertResponseStatusCode(response, 404);
        }).verifyComplete();

        StepVerifier.create(client.exists())
            .assertNext(exists -> assertNotEquals(Boolean.TRUE, exists))
            .verifyComplete();
    }

    @Test
    public void deleteIfExistsSnapshotNotFound() {
        String snapshot = "2025-02-04T10:17:47.0000000Z";
        ShareAsyncClient snapshotClient = shareBuilderHelper(shareName).snapshot(snapshot).buildAsyncClient();

        StepVerifier.create(snapshotClient.deleteIfExistsWithResponse(null)).assertNext(response -> {
            assertFalse(response.getValue());
            FileShareTestHelper.assertResponseStatusCode(response, 404);
        }).verifyComplete();
    }

    @Test
    public void getProperties() {
        StepVerifier.create(primaryShareAsyncClient.createWithResponse(testMetadata, 1)
            .then(primaryShareAsyncClient.getPropertiesWithResponse())).assertNext(it -> {
                FileShareTestHelper.assertResponseStatusCode(it, 200);
                assertEquals(testMetadata, it.getValue().getMetadata());
                assertEquals(1, it.getValue().getQuota());
            }).verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @Test
    public void getPropertiesOAuth() {
        ShareServiceAsyncClient oAuthServiceClient
            = getOAuthServiceAsyncClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareAsyncClient shareClient = oAuthServiceClient.getShareAsyncClient(shareName);

        StepVerifier
            .create(primaryShareAsyncClient.createWithResponse(testMetadata, 1)
                .then(shareClient.getPropertiesWithResponse()))
            .assertNext(it -> {
                FileShareTestHelper.assertResponseStatusCode(it, 200);
                assertEquals(testMetadata, it.getValue().getMetadata());
                assertEquals(1, it.getValue().getQuota());
            })
            .verifyComplete();
    }

    @Test
    public void getPropertiesError() {
        StepVerifier.create(primaryShareAsyncClient.getProperties())
            .verifyErrorSatisfies(
                it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.SHARE_NOT_FOUND));
    }

    @PlaybackOnly
    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#getPropertiesPremiumSupplier")
    public void getPropertiesPremium(String protocol, ShareRootSquash rootSquash) {
        ShareProtocols enabledProtocol = ModelHelper.parseShareProtocols(protocol);

        String shareName = generateShareName();
        Mono<ShareAsyncClient> premiumShareMono = premiumFileServiceAsyncClient.createShareWithResponse(shareName,
            new ShareCreateOptions().setMetadata(testMetadata).setProtocols(enabledProtocol).setRootSquash(rootSquash),
            null).map(Response::getValue);

        StepVerifier.create(premiumShareMono.flatMap(premiumShare -> premiumShare.getPropertiesWithResponse()))
            .assertNext(it -> {
                FileShareTestHelper.assertResponseStatusCode(it, 200);
                assertNotNull(it.getValue());
                assertEquals(testMetadata, it.getValue().getMetadata());
                assertNotNull(it.getValue().getProvisionedIops());
                assertNotNull(it.getValue().getProvisionedBandwidthMiBps());
                assertNotNull(it.getValue().getNextAllowedQuotaDowngradeTime());
                assertEquals(enabledProtocol.toString(), it.getValue().getProtocols().toString());
                assertEquals(rootSquash, it.getValue().getRootSquash());
            })
            .verifyComplete();

        premiumFileServiceAsyncClient.getShareAsyncClient(shareName).delete().block();
    }

    @PlaybackOnly
    @Test
    public void setPremiumProperties() {
        List<ShareRootSquash> rootSquashes
            = Arrays.asList(ShareRootSquash.ALL_SQUASH, ShareRootSquash.NO_ROOT_SQUASH, ShareRootSquash.ROOT_SQUASH);

        for (ShareRootSquash rootSquash : rootSquashes) {
            String shareName = generateShareName();
            Mono<ShareAsyncClient> premiumShareClientMono = premiumFileServiceAsyncClient
                .createShareWithResponse(shareName,
                    new ShareCreateOptions().setProtocols(new ShareProtocols().setNfsEnabled(true)), null)
                .map(Response::getValue);

            StepVerifier
                .create(premiumShareClientMono.flatMap(premiumShareClient -> premiumShareClient
                    .setProperties(new ShareSetPropertiesOptions().setRootSquash(rootSquash))
                    .then(premiumShareClient.getProperties())))
                .assertNext(it -> assertEquals(rootSquash, it.getRootSquash()))
                .verifyComplete();

            //cleanup
            premiumFileServiceAsyncClient.getShareAsyncClient(shareName).delete().block();
        }
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @Test
    public void setPropertiesOAuth() {
        ShareServiceAsyncClient oAuthServiceClient
            = getOAuthServiceAsyncClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareAsyncClient shareClient = oAuthServiceClient.getShareAsyncClient(shareName);

        StepVerifier
            .create(
                primaryShareAsyncClient.createWithResponse(new ShareCreateOptions().setAccessTier(ShareAccessTier.HOT))
                    .then(shareClient.setPropertiesWithResponse(
                        new ShareSetPropertiesOptions().setAccessTier(ShareAccessTier.COOL))))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2019-12-12")
    @Test
    public void setPropertiesAccessTierPremium() {
        ShareAsyncClient client = premiumFileServiceAsyncClient.getShareAsyncClient(generateShareName());
        ShareSetPropertiesOptions options = new ShareSetPropertiesOptions().setAccessTier(ShareAccessTier.PREMIUM);

        StepVerifier.create(client.create().then(client.setProperties(options)).then(client.getProperties()))
            .assertNext(r -> assertEquals(ShareAccessTier.PREMIUM.toString(), r.getAccessTier()))
            .verifyComplete();

        //cleanup
        client.delete().block();
    }

    @Test
    public void setQuota() {
        StepVerifier
            .create(primaryShareAsyncClient.createWithResponse(null, 1).then(primaryShareAsyncClient.getProperties()))
            .assertNext(it -> assertEquals(1, it.getQuota()))
            .verifyComplete();
        StepVerifier.create(primaryShareAsyncClient.setQuotaWithResponse(2))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 200))
            .verifyComplete();
        StepVerifier.create(primaryShareAsyncClient.getProperties())
            .assertNext(it -> assertEquals(2, it.getQuota()))
            .verifyComplete();
    }

    @Test
    public void setQuotaError() {
        StepVerifier.create(primaryShareAsyncClient.setQuota(2))
            .verifyErrorSatisfies(
                it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.SHARE_NOT_FOUND));
    }

    @Test
    public void setMetadata() {
        StepVerifier.create(primaryShareAsyncClient.createWithResponse(testMetadata, null))
            .assertNext(response -> FileShareTestHelper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        Map<String, String> metadataAfterSet = Collections.singletonMap("afterset", "value");

        StepVerifier.create(primaryShareAsyncClient.getProperties())
            .assertNext(it -> assertEquals(testMetadata, it.getMetadata()))
            .verifyComplete();

        StepVerifier.create(primaryShareAsyncClient.setMetadataWithResponse(metadataAfterSet))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 200))
            .verifyComplete();

        StepVerifier.create(primaryShareAsyncClient.getProperties())
            .assertNext(it -> assertEquals(metadataAfterSet, it.getMetadata()))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @Test
    public void setMetadataOAuth() {
        ShareServiceAsyncClient oAuthServiceClient
            = getOAuthServiceAsyncClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareAsyncClient shareClient = oAuthServiceClient.getShareAsyncClient(shareName);

        Map<String, String> metadataAfterSet = Collections.singletonMap("afterset", "value");
        StepVerifier
            .create(primaryShareAsyncClient.createWithResponse(testMetadata, null).then(shareClient.getProperties()))
            .assertNext(it -> assertEquals(testMetadata, it.getMetadata()))
            .verifyComplete();
        StepVerifier.create(shareClient.setMetadataWithResponse(metadataAfterSet))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 200))
            .verifyComplete();
        StepVerifier.create(shareClient.getProperties())
            .assertNext(it -> assertEquals(metadataAfterSet, it.getMetadata()))
            .verifyComplete();
    }

    @Test
    public void setMetadataError() {
        StepVerifier.create(primaryShareAsyncClient.setMetadata(testMetadata))
            .verifyErrorSatisfies(
                it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.SHARE_NOT_FOUND));
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#getStatisticsSupplier")
    public void getStatistics(long size, int gigabytes) {
        StepVerifier.create(primaryShareAsyncClient.create()
            .then(primaryShareAsyncClient.createFile("tempFile", size))
            .then(primaryShareAsyncClient.getStatisticsWithResponse())).assertNext(it -> {
                FileShareTestHelper.assertResponseStatusCode(it, 200);
                assertEquals(it.getValue().getShareUsageInBytes(), size);
                assertEquals(it.getValue().getShareUsageInGB(), gigabytes);
            }).verifyComplete();
    }

    @Test
    public void getStatisticsError() {
        StepVerifier.create(primaryShareAsyncClient.getStatistics())
            .verifyErrorSatisfies(
                it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.SHARE_NOT_FOUND));
    }

    @Test
    public void createDirectory() {
        StepVerifier
            .create(primaryShareAsyncClient.create()
                .then(primaryShareAsyncClient.createDirectoryWithResponse("testCreateDirectory", null, null, null)))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();
    }

    @Test
    public void createDirectoryInvalidName() {
        StepVerifier
            .create(primaryShareAsyncClient.create().then(primaryShareAsyncClient.createDirectory("test/directory")))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404,
                ShareErrorCode.PARENT_NOT_FOUND));
    }

    @Test
    public void createDirectoryMetadata() {
        StepVerifier
            .create(primaryShareAsyncClient.create()
                .then(primaryShareAsyncClient.createDirectoryWithResponse("testCreateDirectory", null, null,
                    testMetadata)))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();
    }

    @Test
    public void createDirectoryFilePermission() {
        StepVerifier
            .create(
                primaryShareAsyncClient.create()
                    .then(primaryShareAsyncClient.createDirectoryWithResponse("testCreateDirectory", null,
                        FILE_PERMISSION, null)))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();
    }

    @Test
    public void createDirectoryFilePermissionKey() {
        Mono<String> permissionKeyMono
            = primaryShareAsyncClient.create().then(primaryShareAsyncClient.createPermission(FILE_PERMISSION));

        StepVerifier.create(permissionKeyMono.flatMap(permissionKey -> {
            smbProperties.setFileCreationTime(testResourceNamer.now())
                .setFileLastWriteTime(testResourceNamer.now())
                .setFilePermissionKey(permissionKey);
            return primaryShareAsyncClient.createDirectoryWithResponse("testCreateDirectory", smbProperties, null,
                null);
        })).assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201)).verifyComplete();
    }

    @Test
    public void createDirectoryMetadataError() {
        StepVerifier
            .create(primaryShareAsyncClient.create()
                .then(primaryShareAsyncClient.createDirectoryWithResponse("testdirectory", null, null,
                    Collections.singletonMap("", "value"))))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 400,
                ShareErrorCode.EMPTY_METADATA_KEY));
    }

    @Test
    public void createIfNotExistsDirectory() {
        StepVerifier
            .create(primaryShareAsyncClient.create()
                .then(primaryShareAsyncClient.createDirectoryIfNotExistsWithResponse("testCreateDirectory",
                    new ShareDirectoryCreateOptions())))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsDirectoryThatAlreadyExists() {
        ShareAsyncClient client = premiumFileServiceAsyncClient.getShareAsyncClient(generateShareName());

        Mono<Response<ShareDirectoryAsyncClient>> initialResponseMono = client.create()
            .then(client.createDirectoryIfNotExistsWithResponse("testCreateDirectory",
                new ShareDirectoryCreateOptions()));

        Mono<Response<ShareDirectoryAsyncClient>> secondResponseMono
            = client.createDirectoryIfNotExistsWithResponse("testCreateDirectory", new ShareDirectoryCreateOptions());

        StepVerifier.create(initialResponseMono).assertNext(initialResponse -> {
            assertNotNull(initialResponse);
            FileShareTestHelper.assertResponseStatusCode(initialResponse, 201);
        }).verifyComplete();

        StepVerifier.create(secondResponseMono).assertNext(secondResponse -> {
            assertNotNull(secondResponse);
            FileShareTestHelper.assertResponseStatusCode(secondResponse, 409);
        }).verifyComplete();

        // cleanup
        client.delete().block();
    }

    @Test
    public void createIfNotExistsDirectoryInvalidName() {
        StepVerifier
            .create(primaryShareAsyncClient.create()
                .then(primaryShareAsyncClient.createDirectoryIfNotExists("test/directory")))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404,
                ShareErrorCode.PARENT_NOT_FOUND));
    }

    @Test
    public void createIfNotExistsDirectoryMetadata() {
        StepVerifier
            .create(primaryShareAsyncClient.create()
                .then(primaryShareAsyncClient.createDirectoryIfNotExistsWithResponse("testCreateDirectory",
                    new ShareDirectoryCreateOptions().setMetadata(testMetadata))))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsDirectoryFilePermission() {
        StepVerifier
            .create(primaryShareAsyncClient.create()
                .then(primaryShareAsyncClient.createDirectoryIfNotExistsWithResponse("testCreateDirectory",
                    new ShareDirectoryCreateOptions().setFilePermission(FILE_PERMISSION))))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsDirectoryFilePermissionKey() {
        Mono<String> permissionKeyMono
            = primaryShareAsyncClient.create().then(primaryShareAsyncClient.createPermission(FILE_PERMISSION));

        StepVerifier.create(permissionKeyMono.flatMap(permissionKey -> {
            smbProperties.setFileCreationTime(testResourceNamer.now())
                .setFileLastWriteTime(testResourceNamer.now())
                .setFilePermissionKey(permissionKey);
            return primaryShareAsyncClient.createDirectoryIfNotExistsWithResponse("testCreateDirectory",
                new ShareDirectoryCreateOptions().setSmbProperties(smbProperties));
        })).assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201)).verifyComplete();
    }

    @Test
    public void createIfNotExistsDirectoryMetadataError() {
        StepVerifier
            .create(primaryShareAsyncClient.create()
                .then(primaryShareAsyncClient.createDirectoryIfNotExistsWithResponse("testdirectory",
                    new ShareDirectoryCreateOptions().setMetadata(Collections.singletonMap("", "value")))))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 400,
                ShareErrorCode.EMPTY_METADATA_KEY));
    }

    @Test
    public void createFile() {
        StepVerifier
            .create(primaryShareAsyncClient.create()
                .then(primaryShareAsyncClient.createFileWithResponse("testCreateFile", 1024, null, null, null, null)))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();
    }

    @Test
    public void createFileFilePermission() {
        StepVerifier
            .create(primaryShareAsyncClient.create()
                .then(primaryShareAsyncClient.createFileWithResponse("testCreateFile", 1024, null, null,
                    FILE_PERMISSION, null)))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();
    }

    @Test
    public void createFileFilePermissionKey() {
        Mono<String> permissionKeyMono
            = primaryShareAsyncClient.create().then(primaryShareAsyncClient.createPermission(FILE_PERMISSION));

        StepVerifier.create(permissionKeyMono.flatMap(permissionKey -> {
            smbProperties.setFileCreationTime(testResourceNamer.now())
                .setFileLastWriteTime(testResourceNamer.now())
                .setFilePermissionKey(permissionKey);
            return primaryShareAsyncClient.createFileWithResponse("testCreateFile", 1024, null, smbProperties, null,
                null);
        })).assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201)).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#createFileInvalidArgsSupplier")
    public void createFileInvalidArgs(String fileName, long maxSize, int statusCode, ShareErrorCode errMsg) {
        StepVerifier
            .create(primaryShareAsyncClient.create()
                .then(primaryShareAsyncClient.createFileWithResponse(fileName, maxSize, null, null, null, null)))
            .verifyErrorSatisfies(
                it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, statusCode, errMsg));
    }

    @Test
    public void createFileLease() {
        StepVerifier
            .create(
                primaryShareAsyncClient.create()
                    .then(primaryShareAsyncClient.getFileClient("testCreateFile").create(512))
                    .then(createLeaseClient(primaryShareAsyncClient.getFileClient("testCreateFile")).acquireLease())
                    .flatMap(leaseId -> primaryShareAsyncClient.createFileWithResponse("testCreateFile", 1024, null,
                        null, null, null, new ShareRequestConditions().setLeaseId(leaseId))))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void createFileLeaseFail() {
        StepVerifier
            .create(primaryShareAsyncClient.create()
                .then(primaryShareAsyncClient.getFileClient("testCreateFile").create(512))
                .then(createLeaseClient(primaryShareAsyncClient.getFileClient("testCreateFile")).acquireLease())
                .then(primaryShareAsyncClient.createFileWithResponse("testCreateFile", 1024, null, null, null, null,
                    new ShareRequestConditions().setLeaseId(testResourceNamer.randomUuid()))))
            .verifyError(ShareStorageException.class);
    }

    @Test
    public void createFileMaxOverload() {
        StepVerifier
            .create(primaryShareAsyncClient.create()
                .then(primaryShareAsyncClient.createFileWithResponse("testCreateFile", 1024,
                    new ShareFileHttpHeaders().setContentType("txt"),
                    smbProperties.setFileCreationTime(testResourceNamer.now())
                        .setFileLastWriteTime(testResourceNamer.now()),
                    FILE_PERMISSION, testMetadata)))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#createFileMaxOverloadInvalidArgsSupplier")
    public void createFileMaxOverloadInvalidArgs(String fileName, long maxSize, ShareFileHttpHeaders httpHeaders,
        Map<String, String> metadata, ShareErrorCode errMsg) {
        StepVerifier
            .create(primaryShareAsyncClient.create()
                .then(primaryShareAsyncClient.createFileWithResponse(fileName, maxSize, httpHeaders, null, null,
                    metadata)))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 400, errMsg));
    }

    @Test
    public void deleteDirectory() {
        String directoryName = "testCreateDirectory";
        StepVerifier
            .create(primaryShareAsyncClient.create()
                .then(primaryShareAsyncClient.createDirectory(directoryName))
                .then(primaryShareAsyncClient.deleteDirectoryWithResponse(directoryName)))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 202))
            .verifyComplete();
    }

    @Test
    public void deleteDirectoryError() {
        StepVerifier
            .create(primaryShareAsyncClient.create().then(primaryShareAsyncClient.deleteDirectory("testdirectory")))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404,
                ShareErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    public void deleteIfExistsDirectory() {
        String directoryName = "testCreateDirectory";
        StepVerifier
            .create(primaryShareAsyncClient.create()
                .then(primaryShareAsyncClient.createDirectory(directoryName))
                .then(primaryShareAsyncClient.deleteDirectoryIfExistsWithResponse(directoryName)))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 202))
            .verifyComplete();
    }

    @Test
    public void deleteIfExistsDirectoryThatDoesNotExist() {
        String directoryName = "testCreateDirectory";
        StepVerifier
            .create(primaryShareAsyncClient.create()
                .then(primaryShareAsyncClient.deleteDirectoryIfExistsWithResponse(directoryName)))
            .assertNext(response -> {
                assertNotNull(response);
                assertFalse(response.getValue());
                FileShareTestHelper.assertResponseStatusCode(response, 404);
            })
            .verifyComplete();
    }

    @Test
    public void deleteFile() {
        String fileName = "testCreateFile";
        StepVerifier
            .create(primaryShareAsyncClient.create()
                .then(primaryShareAsyncClient.createFile(fileName, 1024))
                .then(primaryShareAsyncClient.deleteFileWithResponse(fileName)))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 202))
            .verifyComplete();
    }

    @Test
    public void deleteFileLease() {
        String fileName = "testCreateFile";
        StepVerifier.create(primaryShareAsyncClient.create()
            .then(primaryShareAsyncClient.createFile(fileName, 1024))
            .then(createLeaseClient(primaryShareAsyncClient.getFileClient(fileName)).acquireLease())
            .flatMap(leaseId -> primaryShareAsyncClient.deleteFileWithResponse(fileName,
                new ShareRequestConditions().setLeaseId(leaseId))))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void deleteFileLeaseFail() {
        String fileName = "testCreateFile";
        StepVerifier
            .create(primaryShareAsyncClient.create()
                .then(primaryShareAsyncClient.createFile(fileName, 1024))
                .then(createLeaseClient(primaryShareAsyncClient.getFileClient(fileName)).acquireLease())
                .then(primaryShareAsyncClient.deleteFileWithResponse(fileName,
                    new ShareRequestConditions().setLeaseId(testResourceNamer.randomUuid()))))
            .verifyError(ShareStorageException.class);
    }

    @Test
    public void deleteFileError() {
        StepVerifier.create(primaryShareAsyncClient.create().then(primaryShareAsyncClient.deleteFile("testdirectory")))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404,
                ShareErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    public void deleteIfExistsFile() {
        String fileName = "testCreateFile";
        StepVerifier
            .create(primaryShareAsyncClient.create()
                .then(primaryShareAsyncClient.createFile(fileName, 1024))
                .then(primaryShareAsyncClient.deleteFileIfExistsWithResponse(fileName, null)))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 202))
            .verifyComplete();
    }

    @Test
    public void deleteIfExistsFileLease() {
        String fileName = "testCreateFile";
        StepVerifier.create(primaryShareAsyncClient.create()
            .then(primaryShareAsyncClient.createFile(fileName, 1024))
            .then(createLeaseClient(primaryShareAsyncClient.getFileClient(fileName)).acquireLease())
            .flatMap(leaseId -> primaryShareAsyncClient.deleteFileIfExistsWithResponse(fileName,
                new ShareRequestConditions().setLeaseId(leaseId))))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void deleteIfExistsFileThatDoesNotExist() {
        String fileName = "testCreateFile";
        StepVerifier.create(primaryShareAsyncClient.create()
            .then(primaryShareAsyncClient.deleteFileIfExistsWithResponse(fileName, null))).assertNext(response -> {
                assertNotNull(response);
                assertFalse(response.getValue());
                FileShareTestHelper.assertResponseStatusCode(response, 404);
            }).verifyComplete();
    }

    @Test
    public void deleteIfExistsFileLeaseFail() {
        String fileName = "testCreateFile";

        StepVerifier
            .create(primaryShareAsyncClient.create()
                .then(primaryShareAsyncClient.createFile(fileName, 1024))
                .then(createLeaseClient(primaryShareAsyncClient.getFileClient(fileName)).acquireLease())
                .then(primaryShareAsyncClient.deleteFileIfExistsWithResponse(fileName,
                    new ShareRequestConditions().setLeaseId(testResourceNamer.randomUuid()))))
            .verifyError(ShareStorageException.class);
    }

    @Test
    public void createPermission() {
        StepVerifier
            .create(primaryShareAsyncClient.create()
                .then(primaryShareAsyncClient.createPermissionWithResponse(FILE_PERMISSION)))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();
    }

    @Test
    public void createAndGetPermission() {
        StepVerifier
            .create(primaryShareAsyncClient.create()
                .then(primaryShareAsyncClient.createPermission(FILE_PERMISSION))
                .flatMap(filePermissionKey -> primaryShareAsyncClient.getPermissionWithResponse(filePermissionKey)))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 200))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#filePermissionFormatSupplier")
    public void createAndGetPermissionFilePermissionFormat(FilePermissionFormat filePermissionFormat) {
        String permission = FileShareTestHelper.getPermissionFromFormat(filePermissionFormat);

        ShareFilePermission filePermission
            = new ShareFilePermission().setPermission(permission).setPermissionFormat(filePermissionFormat);

        Mono<String> response = primaryShareAsyncClient.create()
            .then(primaryShareAsyncClient.createPermission(filePermission))
            .flatMap(r -> primaryShareAsyncClient.getPermission(r, filePermissionFormat));

        StepVerifier.create(response).assertNext(r -> assertEquals(r, permission)).verifyComplete();

    }

    @Test
    public void createPermissionError() {
        StepVerifier
            .create(
                primaryShareAsyncClient.create().then(primaryShareAsyncClient.createPermissionWithResponse("abcde")))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 400,
                ShareErrorCode.fromString("FileInvalidPermission")));
    }

    @Test
    public void getPermissionError() {
        StepVerifier
            .create(primaryShareAsyncClient.create().then(primaryShareAsyncClient.getPermissionWithResponse("abcde")))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 400,
                ShareErrorCode.INVALID_HEADER_VALUE));
    }

    @Test
    public void getSnapshotId() {
        String snapshot = OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 1, 1), ZoneOffset.UTC).toString();
        ShareAsyncClient shareSnapshotClient = shareBuilderHelper(shareName).snapshot(snapshot).buildAsyncClient();
        assertEquals(snapshot, shareSnapshotClient.getSnapshotId());
    }

    @Test
    public void getShareName() {
        assertEquals(shareName, primaryShareAsyncClient.getShareName());
    }

    @Test
    public void defaultAudience() {
        ShareAsyncClient aadShareClient = getOAuthShareClientBuilder(
            new ShareClientBuilder().shareName(shareName).shareTokenIntent(ShareTokenIntent.BACKUP)).audience(null) // should default to "https://storage.azure.com/"
                .buildAsyncClient();

        String permission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-"
            + "1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-"
            + "188441444-3053964)S:NO_ACCESS_CONTROL";

        StepVerifier.create(primaryShareAsyncClient.create().then(aadShareClient.createPermission(permission)))
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();
    }

    @Test
    public void storageAccountAudience() {
        ShareAsyncClient aadShareClient = getOAuthShareClientBuilder(new ShareClientBuilder()).shareName(shareName)
            .shareTokenIntent(ShareTokenIntent.BACKUP)
            .audience(ShareAudience.createShareServiceAccountAudience(primaryShareAsyncClient.getAccountName()))
            .buildAsyncClient();

        String permission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-"
            + "1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-"
            + "188441444-3053964)S:NO_ACCESS_CONTROL";

        StepVerifier.create(primaryShareAsyncClient.create().then(aadShareClient.createPermission(permission)))
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @LiveOnly
    @Test
    public void audienceErrorBearerChallengeRetry() {
        ShareAsyncClient aadShareClient = getOAuthShareClientBuilder(new ShareClientBuilder()).shareName(shareName)
            .shareTokenIntent(ShareTokenIntent.BACKUP)
            .audience(ShareAudience.createShareServiceAccountAudience("badaudience"))
            .buildAsyncClient();

        String permission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-"
            + "1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-"
            + "188441444-3053964)S:NO_ACCESS_CONTROL";

        StepVerifier.create(primaryShareAsyncClient.create().then(aadShareClient.createPermission(permission)))
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();
    }

    @Test
    public void audienceFromString() {
        String url = String.format("https://%s.file.core.windows.net/", primaryShareAsyncClient.getAccountName());
        ShareAudience audience = ShareAudience.fromString(url);

        ShareAsyncClient aadShareClient = getOAuthShareClientBuilder(new ShareClientBuilder()).shareName(shareName)
            .shareTokenIntent(ShareTokenIntent.BACKUP)
            .audience(audience)
            .buildAsyncClient();

        String permission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-"
            + "1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-"
            + "188441444-3053964)S:NO_ACCESS_CONTROL";

        StepVerifier.create(primaryShareAsyncClient.create().then(aadShareClient.createPermission(permission)))
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-05-04")
    @ParameterizedTest
    @MethodSource("createEnableSnapshotVirtualDirectoryAccessSupplier")
    public void createEnableSnapshotVirtualDirectoryAccess(Boolean enableSnapshotVirtualDirectoryAccess) {
        ShareCreateOptions options = new ShareCreateOptions();
        ShareProtocols protocols = ModelHelper.parseShareProtocols(Constants.HeaderConstants.NFS_PROTOCOL);
        options.setProtocols(protocols);
        options.setSnapshotVirtualDirectoryAccessEnabled(enableSnapshotVirtualDirectoryAccess);

        StepVerifier.create(premiumFileServiceAsyncClient.getShareAsyncClient(shareName)
            .createWithResponse(options)
            .then(premiumFileServiceAsyncClient.getShareAsyncClient(shareName).getProperties())).assertNext(r -> {
                assertEquals(protocols.toString(), r.getProtocols().toString());
                if (enableSnapshotVirtualDirectoryAccess == null || enableSnapshotVirtualDirectoryAccess) {
                    assertTrue(r.isSnapshotVirtualDirectoryAccessEnabled());
                } else {
                    assertFalse(r.isSnapshotVirtualDirectoryAccessEnabled());
                }
            }).verifyComplete();

        //cleanup
        premiumFileServiceAsyncClient.getShareAsyncClient(shareName).delete().block();
    }

    private static Stream<Arguments> createEnableSnapshotVirtualDirectoryAccessSupplier() {
        return Stream.of(Arguments.of(true), Arguments.of(false), Arguments.of((Boolean) null));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-08-04")
    @ParameterizedTest
    @MethodSource("createEnableSnapshotVirtualDirectoryAccessSupplier")
    public void setPropertiesEnableSnapshotVirtualDirectoryAccess(Boolean enableSnapshotVirtualDirectoryAccess) {
        ShareCreateOptions options = new ShareCreateOptions();
        ShareProtocols protocols = ModelHelper.parseShareProtocols(Constants.HeaderConstants.NFS_PROTOCOL);
        options.setProtocols(protocols);

        Mono<Void> createShare
            = premiumFileServiceAsyncClient.getShareAsyncClient(shareName).createWithResponse(options).then();

        ShareSetPropertiesOptions setPropertiesOptions = new ShareSetPropertiesOptions();
        setPropertiesOptions.setSnapshotVirtualDirectoryAccessEnabled(enableSnapshotVirtualDirectoryAccess);

        Mono<ShareInfo> setProperties = createShare
            .then(premiumFileServiceAsyncClient.getShareAsyncClient(shareName).setProperties(setPropertiesOptions));

        StepVerifier
            .create(setProperties.then(premiumFileServiceAsyncClient.getShareAsyncClient(shareName).getProperties()))
            .assertNext(r -> {
                assertEquals(protocols.toString(), r.getProtocols().toString());
                if (enableSnapshotVirtualDirectoryAccess == null || enableSnapshotVirtualDirectoryAccess) {
                    assertTrue(r.isSnapshotVirtualDirectoryAccessEnabled());
                } else {
                    assertFalse(r.isSnapshotVirtualDirectoryAccessEnabled());
                }
            })
            .verifyComplete();

        //cleanup
        premiumFileServiceAsyncClient.getShareAsyncClient(shareName).delete().block();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @Test
    public void createSharePaidBursting() {
        ShareCreateOptions options = new ShareCreateOptions().setPaidBurstingEnabled(true)
            .setPaidBurstingMaxIops(5000L)
            .setPaidBurstingMaxBandwidthMibps(1000L);

        Mono<ShareProperties> response = premiumFileServiceAsyncClient.getShareAsyncClient(shareName)
            .createWithResponse(options)
            .then(premiumFileServiceAsyncClient.getShareAsyncClient(shareName).getProperties());

        StepVerifier.create(response).assertNext(r -> {
            assertTrue(r.isPaidBurstingEnabled());
            assertEquals(5000L, r.getPaidBurstingMaxIops());
            assertEquals(1000L, r.getPaidBurstingMaxBandwidthMibps());
        }).verifyComplete();

        //cleanup
        premiumFileServiceAsyncClient.getShareAsyncClient(shareName).delete().block();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @Test
    public void createSharePaidBurstingInvalidOptions() {
        ShareCreateOptions options = new ShareCreateOptions().setPaidBurstingEnabled(false)
            .setPaidBurstingMaxIops(5000L)
            .setPaidBurstingMaxBandwidthMibps(1000L);

        StepVerifier.create(premiumFileServiceAsyncClient.getShareAsyncClient(shareName).createWithResponse(options))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 400,
                ShareErrorCode.fromString("InvalidHeaderValue")));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @Test
    public void setPropertiesSharePaidBursting() {
        ShareSetPropertiesOptions options = new ShareSetPropertiesOptions().setPaidBurstingEnabled(true)
            .setPaidBurstingMaxIops(5000L)
            .setPaidBurstingMaxBandwidthMibps(1000L);

        Mono<ShareProperties> response = premiumFileServiceAsyncClient.getShareAsyncClient(shareName)
            .createWithResponse(null)
            .then(premiumFileServiceAsyncClient.getShareAsyncClient(shareName).setProperties(options))
            .then(premiumFileServiceAsyncClient.getShareAsyncClient(shareName).getProperties());

        StepVerifier.create(response).assertNext(r -> {
            assertTrue(r.isPaidBurstingEnabled());
            assertEquals(5000L, r.getPaidBurstingMaxIops());
            assertEquals(1000L, r.getPaidBurstingMaxBandwidthMibps());
        }).verifyComplete();

        //cleanup
        premiumFileServiceAsyncClient.getShareAsyncClient(shareName).delete().block();
    }

    @PlaybackOnly
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2025-01-05")
    @Test
    public void createShareProvisionedV2() {
        ShareCreateOptions options
            = new ShareCreateOptions().setProvisionedMaxIops(501L).setProvisionedMaxBandwidthMibps(126L);

        StepVerifier.create(primaryFileServiceAsyncClient.getShareAsyncClient(shareName).createWithResponse(options))
            .assertNext(r -> {
                assertEquals("501", r.getHeaders().get(X_MS_SHARE_PROVISIONED_IOPS).getValue());
                assertEquals("126", r.getHeaders().get(X_MS_SHARE_PROVISIONED_BANDWIDTH_MIBPS).getValue());
            })
            .verifyComplete();
    }

    @PlaybackOnly
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2025-01-05")
    @Test
    public void setAndGetPropertiesShareProvisionedV2() {
        ShareAsyncClient ac = primaryFileServiceAsyncClient.getShareAsyncClient(shareName);

        ShareSetPropertiesOptions options
            = new ShareSetPropertiesOptions().setProvisionedMaxIops(501L).setProvisionedMaxBandwidthMibps(126L);

        Mono<ShareProperties> response = ac.create().then(ac.setProperties(options)).then(ac.getProperties());

        StepVerifier.create(response).assertNext(r -> {
            assertEquals(501, r.getProvisionedIops());
            assertEquals(126, r.getProvisionedBandwidthMiBps());
            assertNotNull(r.getIncludedBurstIops());
            assertNotNull(r.getMaxBurstCreditsForIops());
            assertNotNull(r.getNextAllowedProvisionedIopsDowngradeTime());
            assertNotNull(r.getNextAllowedProvisionedBandwidthDowngradeTime());
        }).verifyComplete();
    }

    @Test
    public void invalidServiceVersion() {
        ShareServiceAsyncClient serviceAsyncClient
            = instrument(new ShareServiceClientBuilder().endpoint(ENVIRONMENT.getPrimaryAccount().getFileEndpoint())
                .credential(ENVIRONMENT.getPrimaryAccount().getCredential())
                .addPolicy(new InvalidServiceVersionPipelinePolicy())).buildAsyncClient();

        ShareAsyncClient shareAsyncClient = serviceAsyncClient.getShareAsyncClient(generateShareName());

        StepVerifier.create(shareAsyncClient.createIfNotExists()).verifyErrorSatisfies(ex -> {
            ShareStorageException exception = assertInstanceOf(ShareStorageException.class, ex);
            assertEquals(400, exception.getStatusCode());
            assertTrue(exception.getMessage().contains(INVALID_VERSION_HEADER_MESSAGE));
        });
    }
}
