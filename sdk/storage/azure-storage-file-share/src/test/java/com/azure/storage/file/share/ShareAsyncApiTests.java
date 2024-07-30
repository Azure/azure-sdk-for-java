// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.http.rest.Response;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.extensions.PlaybackOnly;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import com.azure.storage.file.share.implementation.util.ModelHelper;
import com.azure.storage.file.share.models.NtfsFileAttributes;
import com.azure.storage.file.share.models.ShareAudience;
import com.azure.storage.file.share.models.ShareErrorCode;
import com.azure.storage.file.share.models.ShareFileHttpHeaders;
import com.azure.storage.file.share.models.ShareInfo;
import com.azure.storage.file.share.models.ShareProtocols;
import com.azure.storage.file.share.models.ShareRequestConditions;
import com.azure.storage.file.share.models.ShareRootSquash;
import com.azure.storage.file.share.models.ShareStorageException;
import com.azure.storage.file.share.models.ShareTokenIntent;
import com.azure.storage.file.share.options.ShareCreateOptions;
import com.azure.storage.file.share.options.ShareDirectoryCreateOptions;
import com.azure.storage.file.share.options.ShareSetPropertiesOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

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
    private static final String FILE_PERMISSION = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-188441444-3053964)S:NO_ACCESS_CONTROL";

    @BeforeEach
    public void setup() {
        shareName = generateShareName();
        primaryFileServiceAsyncClient = fileServiceBuilderHelper().buildAsyncClient();
        primaryShareAsyncClient = primaryFileServiceAsyncClient.getShareAsyncClient(shareName);
        testMetadata = Collections.singletonMap("testmetadata", "value");
        smbProperties = new FileSmbProperties().setNtfsFileAttributes(EnumSet.<NtfsFileAttributes>of(NtfsFileAttributes.NORMAL));
    }

    @Test
    public void getShareURL() {
        String accountName = StorageSharedKeyCredential.fromConnectionString(ENVIRONMENT.getPrimaryAccount()
            .getConnectionString()).getAccountName();
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
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201)).verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-08-04")
    @Test
    public void createShareSasError() {
        ShareServiceAsyncClient unauthorizedServiceClient = fileServiceBuilderHelper()
            .sasToken("sig=dummyToken")
            .buildAsyncClient();

        ShareAsyncClient share = unauthorizedServiceClient.getShareAsyncClient(generateShareName());

        StepVerifier.create(share.create())
            .verifyErrorSatisfies(r -> {
                ShareStorageException e = assertInstanceOf(ShareStorageException.class, r);
                assertEquals(ShareErrorCode.AUTHENTICATION_FAILED, e.getErrorCode());
                assertTrue(e.getServiceMessage().contains("AuthenticationErrorDetail"));
            });
    }

    @ParameterizedTest
    @MethodSource("createShareWithArgsSupplier")
    public void createShareWithArgs(Map<String, String> metadata, Integer quota) {
        StepVerifier.create(primaryShareAsyncClient.createWithResponse(metadata, quota))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201)).verifyComplete();
    }

    private static Stream<Arguments> createShareWithArgsSupplier() {
        return Stream.of(
            Arguments.of(null, null),
            Arguments.of(null, 1),
            Arguments.of(testMetadata, null),
            Arguments.of(testMetadata, 1),
            Arguments.of(testMetadata, 6000));
    }

    @ParameterizedTest
    @MethodSource("createShareWithInvalidArgsSupplier")
    public void createShareWithInvalidArgs(Map<String, String> metadata, Integer quota, int statusCode,
        ShareErrorCode errMessage) {
        StepVerifier.create(primaryShareAsyncClient.createWithResponse(metadata, quota)).verifyErrorSatisfies(it ->
            FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, statusCode, errMessage));
    }

    private static Stream<Arguments> createShareWithInvalidArgsSupplier() {
        return Stream.of(
            Arguments.of(Collections.singletonMap("", "value"), 1, 400, ShareErrorCode.EMPTY_METADATA_KEY),
            Arguments.of(Collections.singletonMap("metadata!", "value"), 1, 400, ShareErrorCode.INVALID_METADATA));
    }

    @Test
    public void createSnapshot() {
        primaryShareAsyncClient.create().block();
        String shareSnapshotName = generatePathName();
        StepVerifier.create(primaryShareAsyncClient.createSnapshotWithResponse(null)).assertNext(it -> {
            FileShareTestHelper.assertResponseStatusCode(it, 201);
            ShareClient shareSnapshotClient = new ShareClientBuilder()
                .shareName(shareSnapshotName)
                .connectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString())
                .snapshot(it.getValue().getSnapshot())
                .buildClient();
            assertEquals(it.getValue().getSnapshot(), shareSnapshotClient.getSnapshotId());
        }).verifyComplete();

    }

    @Test
    public void createSnapshotError() {
        StepVerifier.create(primaryShareAsyncClient.createSnapshot()).verifyErrorSatisfies(it ->
            FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.SHARE_NOT_FOUND));
    }

    @Test
    public void createSnapshotMetadata() {
        primaryShareAsyncClient.create().block();
        String shareSnapshotName = generatePathName();
        StepVerifier.create(primaryShareAsyncClient.createSnapshotWithResponse(testMetadata))
            .assertNext(it -> {
                FileShareTestHelper.assertResponseStatusCode(it, 201);
                ShareClient shareSnapshotClient = new ShareClientBuilder().shareName(shareSnapshotName)
                    .connectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString())
                    .snapshot(it.getValue().getSnapshot()).buildClient();
                assertEquals(it.getValue().getSnapshot(), shareSnapshotClient.getSnapshotId());
            }).verifyComplete();
    }

    @Test
    public void createSnapshotMetadataError() {
        primaryShareAsyncClient.create().block();
        StepVerifier.create(primaryShareAsyncClient.createSnapshotWithResponse(Collections.singletonMap("", "value")))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 400,
                ShareErrorCode.EMPTY_METADATA_KEY));
    }

    @Test
    public void createIfNotExistsShare() {
        StepVerifier.create(primaryShareAsyncClient.createIfNotExistsWithResponse(null, null))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201)).verifyComplete();
    }

    @Test
    public void createIfNotExistsShareThatAlreadyExists() {
        ShareAsyncClient client = premiumFileServiceAsyncClient.getShareAsyncClient(generateShareName());
        Response<ShareInfo> initialResponse = client.createIfNotExistsWithResponse(new ShareCreateOptions()).block();
        Response<ShareInfo> secondResponse = client.createIfNotExistsWithResponse(new ShareCreateOptions()).block();
        assertNotNull(initialResponse);
        assertNotNull(secondResponse);
        FileShareTestHelper.assertResponseStatusCode(initialResponse, 201);
        FileShareTestHelper.assertResponseStatusCode(secondResponse, 409);
    }

    @ParameterizedTest
    @MethodSource("createIfNotExistsShareWithArgsSupplier")
    public void createIfNotExistsShareWithArgs(Map<String, String> metadata, Integer quota) {
        StepVerifier.create(primaryShareAsyncClient.createIfNotExistsWithResponse(
            new ShareCreateOptions().setMetadata(metadata).setQuotaInGb(quota)))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201)).verifyComplete();
    }

    private static Stream<Arguments> createIfNotExistsShareWithArgsSupplier() {
        return Stream.of(
            Arguments.of(null, null),
            Arguments.of(null, 1),
            Arguments.of(testMetadata, null),
            Arguments.of(testMetadata, 1));
    }

    @ParameterizedTest
    @MethodSource("createShareWithInvalidArgsSupplier")
    public void createIfNotExistsShareWithInvalidArgs(Map<String, String> metadata, Integer quota, int statusCode,
        ShareErrorCode errMessage) {
        StepVerifier.create(primaryShareAsyncClient.createIfNotExistsWithResponse(
            new ShareCreateOptions().setMetadata(metadata).setQuotaInGb(quota)))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, statusCode,
                errMessage));

    }

    @Test
    public void deleteShare() {
        primaryShareAsyncClient.create().block();
        StepVerifier.create(primaryShareAsyncClient.deleteWithResponse())
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201));
    }

    @Test
    public void deleteShareError() {
        StepVerifier.create(primaryShareAsyncClient.delete())
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404,
                ShareErrorCode.SHARE_NOT_FOUND));
    }

    @Test
    public void deleteIfExistsShare() {
        primaryShareAsyncClient.create().block();
        StepVerifier.create(primaryShareAsyncClient.deleteIfExistsWithResponse(null))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201));
    }

    @Test
    public void deleteIfExistsShareThatDoesNotExist() {
        ShareAsyncClient client = premiumFileServiceAsyncClient.getShareAsyncClient(generateShareName());
        Response<Boolean> response = client.deleteIfExistsWithResponse(null, null).block();

        assertNotNull(response);
        assertFalse(response.getValue());
        FileShareTestHelper.assertResponseStatusCode(response, 404);
        assertNotEquals(Boolean.TRUE, client.exists().block());
    }

    @Test
    public void deleteIfExistsDirectoryThatWasAlreadyDeleted() {
        primaryShareAsyncClient.create().block();

        Response<Boolean> initialResponse = primaryShareAsyncClient.deleteIfExistsWithResponse(null, null).block();
        sleepIfRunningAgainstService(45000);
        // Calling delete again after garbage collection is completed
        Response<Boolean> secondResponse = primaryShareAsyncClient.deleteIfExistsWithResponse(null, null).block();

        assertNotNull(initialResponse);
        assertNotNull(secondResponse);
        FileShareTestHelper.assertResponseStatusCode(initialResponse, 202);
        FileShareTestHelper.assertResponseStatusCode(secondResponse, 404);
        assertTrue(initialResponse.getValue());
        assertFalse(secondResponse.getValue());
    }

    @Test
    public void getProperties() {
        primaryShareAsyncClient.createWithResponse(testMetadata, 1).block();
        StepVerifier.create(primaryShareAsyncClient.getPropertiesWithResponse()).assertNext(it -> {
            FileShareTestHelper.assertResponseStatusCode(it, 200);
            assertEquals(testMetadata, it.getValue().getMetadata());
            assertEquals(it.getValue().getQuota(), 1);
        }).verifyComplete();
    }

    @Test
    public void getPropertiesError() {
        StepVerifier.create(primaryShareAsyncClient.getProperties()).verifyErrorSatisfies(it ->
            FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.SHARE_NOT_FOUND));
    }

    @PlaybackOnly
    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#getPropertiesPremiumSupplier")
    public void getPropertiesPremium(String protocol, ShareRootSquash rootSquash) {
        ShareProtocols enabledProtocol = ModelHelper.parseShareProtocols(protocol);

        ShareAsyncClient premiumShare = Objects.requireNonNull(
            premiumFileServiceAsyncClient.createShareWithResponse(generateShareName(), new ShareCreateOptions()
                .setMetadata(testMetadata).setProtocols(enabledProtocol).setRootSquash(rootSquash), null)
                .block()).getValue();
        StepVerifier.create(premiumShare.getPropertiesWithResponse()).assertNext(it -> {
            FileShareTestHelper.assertResponseStatusCode(it, 200);
            assertNotNull(it.getValue());
            assertEquals(testMetadata, it.getValue().getMetadata());
            assertNotNull(it.getValue().getProvisionedIops());
            assertNotNull(it.getValue().getProvisionedBandwidthMiBps());
            assertNotNull(it.getValue().getNextAllowedQuotaDowngradeTime());
            assertEquals(enabledProtocol.toString(), it.getValue().getProtocols().toString());
            assertEquals(rootSquash, it.getValue().getRootSquash());
        }).verifyComplete();
    }

    @PlaybackOnly
    @Test
    public void setPremiumProperties() {
        List<ShareRootSquash> rootSquashes = Arrays.asList(
            ShareRootSquash.ALL_SQUASH,
            ShareRootSquash.NO_ROOT_SQUASH,
            ShareRootSquash.ROOT_SQUASH);

        for (ShareRootSquash rootSquash : rootSquashes) {
            ShareAsyncClient premiumShareClient = Objects.requireNonNull(
                premiumFileServiceAsyncClient.createShareWithResponse(generateShareName(),
                    new ShareCreateOptions().setProtocols(new ShareProtocols().setNfsEnabled(true)), null)
                    .block()).getValue();
            premiumShareClient.setProperties(new ShareSetPropertiesOptions().setRootSquash(rootSquash)).block();
            StepVerifier.create(premiumShareClient.getProperties()).assertNext(it ->
                assertEquals(rootSquash, it.getRootSquash())).verifyComplete();
        }
    }

    @Test
    public void setQuota() {
        primaryShareAsyncClient.createWithResponse(null, 1).block();
        StepVerifier.create(primaryShareAsyncClient.getProperties()).assertNext(it ->
            assertEquals(it.getQuota(), 1)).verifyComplete();
        StepVerifier.create(primaryShareAsyncClient.setQuotaWithResponse(2)).assertNext(it ->
            FileShareTestHelper.assertResponseStatusCode(it, 200)).verifyComplete();
        StepVerifier.create(primaryShareAsyncClient.getProperties()).assertNext(it ->
            assertEquals(it.getQuota(), 2)).verifyComplete();
    }

    @Test
    public void setQuotaError() {
        StepVerifier.create(primaryShareAsyncClient.setQuota(2)).verifyErrorSatisfies(it ->
            FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.SHARE_NOT_FOUND));
    }

    @Test
    public void setMetadata() {
        primaryShareAsyncClient.createWithResponse(testMetadata, null).block();
        Map<String, String> metadataAfterSet = Collections.singletonMap("afterset", "value");
        StepVerifier.create(primaryShareAsyncClient.getProperties()).assertNext(it ->
            assertEquals(testMetadata, it.getMetadata())).verifyComplete();
        StepVerifier.create(primaryShareAsyncClient.setMetadataWithResponse(metadataAfterSet))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 200)).verifyComplete();
        StepVerifier.create(primaryShareAsyncClient.getProperties()).assertNext(it ->
            assertEquals(metadataAfterSet, it.getMetadata())).verifyComplete();
    }

    @Test
    public void setMetadataError() {
        StepVerifier.create(primaryShareAsyncClient.setMetadata(testMetadata)).verifyErrorSatisfies(it ->
            FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.SHARE_NOT_FOUND));
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#getStatisticsSupplier")
    public void getStatistics(long size, int gigabytes) {
        primaryShareAsyncClient.create().block();
        primaryShareAsyncClient.createFile("tempFile", size).block();

        StepVerifier.create(primaryShareAsyncClient.getStatisticsWithResponse()).assertNext(it -> {
            FileShareTestHelper.assertResponseStatusCode(it, 200);
            assertEquals(it.getValue().getShareUsageInBytes(), size);
            assertEquals(it.getValue().getShareUsageInGB(), gigabytes);
        }).verifyComplete();
    }

    @Test
    public void getStatisticsError() {
        StepVerifier.create(primaryShareAsyncClient.getStatistics()).verifyErrorSatisfies(it ->
            FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.SHARE_NOT_FOUND));
    }

    @Test
    public void createDirectory() {
        primaryShareAsyncClient.create().block();
        StepVerifier.create(primaryShareAsyncClient.createDirectoryWithResponse("testCreateDirectory", null, null,
            null)).assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201)).verifyComplete();
    }

    @Test
    public void createDirectoryInvalidName() {
        primaryShareAsyncClient.create().block();
        StepVerifier.create(primaryShareAsyncClient.createDirectory("test/directory")).verifyErrorSatisfies(it ->
            FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.PARENT_NOT_FOUND));
    }

    @Test
    public void createDirectoryMetadata() {
        primaryShareAsyncClient.create().block();
        StepVerifier.create(primaryShareAsyncClient.createDirectoryWithResponse("testCreateDirectory", null, null,
                testMetadata)).assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();
    }

    @Test
    public void createDirectoryFilePermission() {
        primaryShareAsyncClient.create().block();
        StepVerifier.create(primaryShareAsyncClient.createDirectoryWithResponse("testCreateDirectory", null,
            FILE_PERMISSION, null)).assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();
    }

    @Test
    public void createDirectoryFilePermissionKey() {
        primaryShareAsyncClient.create().block();
        String permissionKey = primaryShareAsyncClient.createPermission(FILE_PERMISSION).block();
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setFilePermissionKey(permissionKey);
        StepVerifier.create(primaryShareAsyncClient.createDirectoryWithResponse("testCreateDirectory", smbProperties,
            null, null)).assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201)).verifyComplete();
    }

    @Test
    public void createDirectoryMetadataError() {
        primaryShareAsyncClient.create().block();
        StepVerifier.create(primaryShareAsyncClient.createDirectoryWithResponse("testdirectory", null, null,
            Collections.singletonMap("", "value"))).verifyErrorSatisfies(it ->
            FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 400, ShareErrorCode.EMPTY_METADATA_KEY));

    }

    @Test
    public void createIfNotExistsDirectory() {
        primaryShareAsyncClient.create().block();
        StepVerifier.create(primaryShareAsyncClient.createDirectoryIfNotExistsWithResponse("testCreateDirectory",
            new ShareDirectoryCreateOptions())).assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsDirectoryThatAlreadyExists() {
        ShareAsyncClient client = premiumFileServiceAsyncClient.getShareAsyncClient(generateShareName());
        client.create().block();
        Response<ShareDirectoryAsyncClient> initialResponse = client.createDirectoryIfNotExistsWithResponse(
            "testCreateDirectory", new ShareDirectoryCreateOptions()).block();
        Response<ShareDirectoryAsyncClient> secondResponse = client.createDirectoryIfNotExistsWithResponse(
            "testCreateDirectory", new ShareDirectoryCreateOptions()).block();

        assertNotNull(initialResponse);
        FileShareTestHelper.assertResponseStatusCode(initialResponse, 201);
        assertNotNull(secondResponse);
        FileShareTestHelper.assertResponseStatusCode(secondResponse, 409);
    }

    @Test
    public void createIfNotExistsDirectoryInvalidName() {
        primaryShareAsyncClient.create().block();
        StepVerifier.create(primaryShareAsyncClient.createDirectoryIfNotExists("test/directory"))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404,
                ShareErrorCode.PARENT_NOT_FOUND));
    }

    @Test
    public void createIfNotExistsDirectoryMetadata() {
        primaryShareAsyncClient.create().block();
        StepVerifier.create(primaryShareAsyncClient.createDirectoryIfNotExistsWithResponse("testCreateDirectory",
            new ShareDirectoryCreateOptions().setMetadata(testMetadata)))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201)).verifyComplete();
    }

    @Test
    public void createIfNotExistsDirectoryFilePermission() {
        primaryShareAsyncClient.create().block();
        StepVerifier.create(primaryShareAsyncClient.createDirectoryIfNotExistsWithResponse("testCreateDirectory",
            new ShareDirectoryCreateOptions().setFilePermission(FILE_PERMISSION)))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201)).verifyComplete();
    }

    @Test
    public void createIfNotExistsDirectoryFilePermissionKey() {
        primaryShareAsyncClient.create().block();
        String permissionKey = primaryShareAsyncClient.createPermission(FILE_PERMISSION).block();
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setFilePermissionKey(permissionKey);
        StepVerifier.create(primaryShareAsyncClient.createDirectoryIfNotExistsWithResponse("testCreateDirectory",
            new ShareDirectoryCreateOptions().setSmbProperties(smbProperties)))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201)).verifyComplete();
    }

    @Test
    public void createIfNotExistsDirectoryMetadataError() {
        primaryShareAsyncClient.create().block();
        StepVerifier.create(primaryShareAsyncClient.createDirectoryIfNotExistsWithResponse("testdirectory",
            new ShareDirectoryCreateOptions().setMetadata(Collections.singletonMap("", "value"))))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 400,
                ShareErrorCode.EMPTY_METADATA_KEY));
    }

    @Test
    public void createFile() {
        primaryShareAsyncClient.create().block();
        StepVerifier.create(primaryShareAsyncClient.createFileWithResponse("testCreateFile", 1024, null, null, null,
            null)).assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201)).verifyComplete();
    }

    @Test
    public void createFileFilePermission() {
        primaryShareAsyncClient.create().block();
        StepVerifier.create(primaryShareAsyncClient.createFileWithResponse("testCreateFile", 1024, null, null,
            FILE_PERMISSION, null)).assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();
    }

    @Test
    public void createFileFilePermissionKey() {
        primaryShareAsyncClient.create().block();
        String permissionKey = primaryShareAsyncClient.createPermission(FILE_PERMISSION).block();
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setFilePermissionKey(permissionKey);
        StepVerifier.create(primaryShareAsyncClient.createFileWithResponse("testCreateFile", 1024, null, smbProperties,
            null, null)).assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201)).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#createFileInvalidArgsSupplier")
    public void createFileInvalidArgs(String fileName, long maxSize, int statusCode, ShareErrorCode errMsg) {
        primaryShareAsyncClient.create().block();
        StepVerifier.create(primaryShareAsyncClient.createFileWithResponse(fileName, maxSize, null, null, null, null))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, statusCode,
                errMsg));

    }

    @Test
    public void createFileLease() {
        primaryShareAsyncClient.create().block();
        primaryShareAsyncClient.getFileClient("testCreateFile").create(512).block();
        String leaseId = createLeaseClient(primaryShareAsyncClient.getFileClient("testCreateFile")).acquireLease()
            .block();
        StepVerifier.create(primaryShareAsyncClient.createFileWithResponse("testCreateFile", 1024, null, null, null,
            null, new ShareRequestConditions().setLeaseId(leaseId))).expectNextCount(1).verifyComplete();
    }

    @Test
    public void createFileLeaseFail() {
        primaryShareAsyncClient.create().block();
        primaryShareAsyncClient.getFileClient("testCreateFile").create(512).block();
        createLeaseClient(primaryShareAsyncClient.getFileClient("testCreateFile")).acquireLease().block();
        StepVerifier.create(primaryShareAsyncClient.createFileWithResponse("testCreateFile", 1024, null, null, null,
            null, new ShareRequestConditions().setLeaseId(testResourceNamer.randomUuid())))
            .verifyError(ShareStorageException.class);
    }

    @Test
    public void createFileMaxOverload() {
        primaryShareAsyncClient.create().block();
        ShareFileHttpHeaders httpHeaders = new ShareFileHttpHeaders().setContentType("txt");
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now());
        StepVerifier.create(primaryShareAsyncClient.createFileWithResponse("testCreateFile", 1024, httpHeaders,
            smbProperties, FILE_PERMISSION, testMetadata)).assertNext(it ->
            FileShareTestHelper.assertResponseStatusCode(it, 201)).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#createFileMaxOverloadInvalidArgsSupplier")
    public void createFileMaxOverloadInvalidArgs(String fileName, long maxSize, ShareFileHttpHeaders httpHeaders,
        Map<String, String> metadata, ShareErrorCode errMsg) {
        primaryShareAsyncClient.create().block();
        StepVerifier.create(primaryShareAsyncClient.createFileWithResponse(fileName, maxSize, httpHeaders, null, null,
            metadata)).verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 400,
            errMsg));
    }

    @Test
    public void deleteDirectory() {
        String directoryName = "testCreateDirectory";
        primaryShareAsyncClient.create().block();
        primaryShareAsyncClient.createDirectory(directoryName).block();
        StepVerifier.create(primaryShareAsyncClient.deleteDirectoryWithResponse(directoryName))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 202)).verifyComplete();
    }

    @Test
    public void deleteDirectoryError() {
        primaryShareAsyncClient.create().block();
        StepVerifier.create(primaryShareAsyncClient.deleteDirectory("testdirectory"))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404,
                ShareErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    public void deleteIfExistsDirectory() {
        String directoryName = "testCreateDirectory";
        primaryShareAsyncClient.create().block();
        primaryShareAsyncClient.createDirectory(directoryName).block();
        StepVerifier.create(primaryShareAsyncClient.deleteDirectoryIfExistsWithResponse(directoryName))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 202)).verifyComplete();
    }

    @Test
    public void deleteIfExistsDirectoryThatDoesNotExist() {
        String directoryName = "testCreateDirectory";
        primaryShareAsyncClient.create().block();
        Response<Boolean> response = primaryShareAsyncClient.deleteDirectoryIfExistsWithResponse(directoryName).block();
        assertNotNull(response);
        assertFalse(response.getValue());
        FileShareTestHelper.assertResponseStatusCode(response, 404);
    }

    @Test
    public void deleteFile() {
        String fileName = "testCreateFile";
        primaryShareAsyncClient.create().block();
        primaryShareAsyncClient.createFile(fileName, 1024).block();
        StepVerifier.create(primaryShareAsyncClient.deleteFileWithResponse(fileName))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 202)).verifyComplete();

    }

    @Test
    public void deleteFileLease() {
        String fileName = "testCreateFile";
        primaryShareAsyncClient.create().block();
        primaryShareAsyncClient.createFile(fileName, 1024).block();
        String leaseId = createLeaseClient(primaryShareAsyncClient.getFileClient(fileName)).acquireLease().block();
        StepVerifier.create(primaryShareAsyncClient.deleteFileWithResponse(fileName,
                new ShareRequestConditions().setLeaseId(leaseId))).expectNextCount(1).verifyComplete();
    }

    @Test
    public void deleteFileLeaseFail() {
        String fileName = "testCreateFile";
        primaryShareAsyncClient.create().block();
        primaryShareAsyncClient.createFile(fileName, 1024).block();
        createLeaseClient(primaryShareAsyncClient.getFileClient(fileName)).acquireLease().block();
        StepVerifier.create(primaryShareAsyncClient.deleteFileWithResponse(fileName,
                new ShareRequestConditions().setLeaseId(testResourceNamer.randomUuid())))
            .verifyError(ShareStorageException.class);
    }

    @Test
    public void deleteFileError() {
        primaryShareAsyncClient.create().block();
        StepVerifier.create(primaryShareAsyncClient.deleteFile("testdirectory")).verifyErrorSatisfies(it ->
            FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    public void deleteIfExistsFile() {
        String fileName = "testCreateFile";
        primaryShareAsyncClient.create().block();
        primaryShareAsyncClient.createFile(fileName, 1024).block();
        StepVerifier.create(primaryShareAsyncClient.deleteFileIfExistsWithResponse(fileName, null))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 202)).verifyComplete();

    }

    @Test
    public void deleteIfExistsFileLease() {
        String fileName = "testCreateFile";
        primaryShareAsyncClient.create().block();
        primaryShareAsyncClient.createFile(fileName, 1024).block();
        String leaseId = createLeaseClient(primaryShareAsyncClient.getFileClient(fileName)).acquireLease().block();
        StepVerifier.create(primaryShareAsyncClient.deleteFileIfExistsWithResponse(fileName,
            new ShareRequestConditions().setLeaseId(leaseId))).expectNextCount(1).verifyComplete();
    }

    @Test
    public void deleteIfExistsFileThatDoesNotExist() {
        String fileName = "testCreateFile";
        primaryShareAsyncClient.create().block();
        Response<Boolean> response = primaryShareAsyncClient.deleteFileIfExistsWithResponse(fileName, null).block();
        assertNotNull(response);
        assertFalse(response.getValue());
        FileShareTestHelper.assertResponseStatusCode(response, 404);
    }

    @Test
    public void deleteIfExistsFileLeaseFail() {
        String fileName = "testCreateFile";
        primaryShareAsyncClient.create().block();
        primaryShareAsyncClient.createFile(fileName, 1024).block();
        createLeaseClient(primaryShareAsyncClient.getFileClient(fileName)).acquireLease().block();

        StepVerifier.create(primaryShareAsyncClient.deleteFileIfExistsWithResponse(fileName,
                new ShareRequestConditions().setLeaseId(testResourceNamer.randomUuid())))
            .verifyError(ShareStorageException.class);
    }

    @Test
    public void createPermission() {
        primaryShareAsyncClient.create().block();
        StepVerifier.create(primaryShareAsyncClient.createPermissionWithResponse(FILE_PERMISSION))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201)).verifyComplete();
    }

    @Test
    public void createAndGetPermission() {
        primaryShareAsyncClient.create().block();
        String filePermissionKey = primaryShareAsyncClient.createPermission(FILE_PERMISSION).block();
        StepVerifier.create(primaryShareAsyncClient.getPermissionWithResponse(filePermissionKey))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 200)).verifyComplete();
    }

    @Test
    public void createPermissionError() {
        primaryShareAsyncClient.create().block();
        // Invalid permission
        StepVerifier.create(primaryShareAsyncClient.createPermissionWithResponse("abcde")).verifyErrorSatisfies(it ->
            FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 400,
                ShareErrorCode.fromString("FileInvalidPermission")));
    }

    @Test
    public void getPermissionError() {
        primaryShareAsyncClient.create().block();
        // Invalid permission key
        StepVerifier.create(primaryShareAsyncClient.getPermissionWithResponse("abcde"))
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
        primaryShareAsyncClient.create().block();
        ShareAsyncClient aadShareClient = getOAuthShareClientBuilder(new ShareClientBuilder().shareName(shareName)
            .shareTokenIntent(ShareTokenIntent.BACKUP))
            .audience(null) // should default to "https://storage.azure.com/"
            .buildAsyncClient();

        String permission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-"
            + "1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-"
            + "188441444-3053964)S:NO_ACCESS_CONTROL";

        StepVerifier.create(aadShareClient.createPermission(permission))
            .assertNext(r -> assertNotNull(r))
            .verifyComplete();
    }

    @Test
    public void storageAccountAudience() {
        primaryShareAsyncClient.create().block();
        ShareAsyncClient aadShareClient = getOAuthShareClientBuilder(new ShareClientBuilder())
            .shareName(shareName)
            .shareTokenIntent(ShareTokenIntent.BACKUP)
            .audience(ShareAudience.createShareServiceAccountAudience(primaryShareAsyncClient.getAccountName()))
            .buildAsyncClient();

        String permission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-"
            + "1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-"
            + "188441444-3053964)S:NO_ACCESS_CONTROL";

        StepVerifier.create(aadShareClient.createPermission(permission))
            .assertNext(r -> assertNotNull(r))
            .verifyComplete();
    }

    @Test
    public void audienceError() {
        primaryShareAsyncClient.create().block();
        ShareAsyncClient aadShareClient = getOAuthShareClientBuilder(new ShareClientBuilder())
            .shareName(shareName)
            .shareTokenIntent(ShareTokenIntent.BACKUP)
            .audience(ShareAudience.createShareServiceAccountAudience("badaudience"))
            .buildAsyncClient();

        String permission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-"
            + "1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-"
            + "188441444-3053964)S:NO_ACCESS_CONTROL";

        StepVerifier.create(aadShareClient.createPermission(permission))
            .verifyErrorSatisfies(r -> {
                ShareStorageException e = assertInstanceOf(ShareStorageException.class, r);
                assertEquals(ShareErrorCode.INVALID_AUTHENTICATION_INFO, e.getErrorCode());
            });
    }
    @Test
    public void audienceFromString() {
        String url = String.format("https://%s.file.core.windows.net/", primaryShareAsyncClient.getAccountName());
        ShareAudience audience = ShareAudience.fromString(url);

        primaryShareAsyncClient.create().block();
        ShareAsyncClient aadShareClient = getOAuthShareClientBuilder(new ShareClientBuilder())
            .shareName(shareName)
            .shareTokenIntent(ShareTokenIntent.BACKUP)
            .audience(audience)
            .buildAsyncClient();

        String permission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-"
            + "1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-"
            + "188441444-3053964)S:NO_ACCESS_CONTROL";

        StepVerifier.create(aadShareClient.createPermission(permission))
            .assertNext(r -> assertNotNull(r))
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

        premiumFileServiceAsyncClient.getShareAsyncClient(shareName).createWithResponse(options).block();

        StepVerifier.create(premiumFileServiceAsyncClient.getShareAsyncClient(shareName).getProperties())
            .assertNext(r -> {
                assertEquals(protocols.toString(), r.getProtocols().toString());
                if (enableSnapshotVirtualDirectoryAccess == null || enableSnapshotVirtualDirectoryAccess) {
                    assertTrue(r.isSnapshotVirtualDirectoryAccessEnabled());
                } else {
                    assertFalse(r.isSnapshotVirtualDirectoryAccessEnabled());
                }
            })
            .verifyComplete();
    }

    private static Stream<Arguments> createEnableSnapshotVirtualDirectoryAccessSupplier() {
        return Stream.of(
            Arguments.of(true),
            Arguments.of(false),
            Arguments.of((Boolean) null));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-08-04")
    @ParameterizedTest
    @MethodSource("createEnableSnapshotVirtualDirectoryAccessSupplier")
    public void setPropertiesEnableSnapshotVirtualDirectoryAccess(Boolean enableSnapshotVirtualDirectoryAccess) {
        ShareCreateOptions options = new ShareCreateOptions();
        ShareProtocols protocols = ModelHelper.parseShareProtocols(Constants.HeaderConstants.NFS_PROTOCOL);
        options.setProtocols(protocols);

        premiumFileServiceAsyncClient.getShareAsyncClient(shareName).createWithResponse(options).block();

        ShareSetPropertiesOptions setPropertiesOptions = new ShareSetPropertiesOptions();
        setPropertiesOptions.setSnapshotVirtualDirectoryAccessEnabled(enableSnapshotVirtualDirectoryAccess);

        premiumFileServiceAsyncClient.getShareAsyncClient(shareName).setProperties(setPropertiesOptions).block();

        StepVerifier.create(premiumFileServiceAsyncClient.getShareAsyncClient(shareName).getProperties())
            .assertNext(r -> {
                assertEquals(protocols.toString(), r.getProtocols().toString());
                if (enableSnapshotVirtualDirectoryAccess == null || enableSnapshotVirtualDirectoryAccess) {
                    assertTrue(r.isSnapshotVirtualDirectoryAccessEnabled());
                } else {
                    assertFalse(r.isSnapshotVirtualDirectoryAccessEnabled());
                }
            })
            .verifyComplete();
    }

}
