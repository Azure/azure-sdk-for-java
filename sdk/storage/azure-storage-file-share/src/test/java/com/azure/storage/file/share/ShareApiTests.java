// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.extensions.PlaybackOnly;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import com.azure.storage.file.share.implementation.util.ModelHelper;
import com.azure.storage.file.share.models.FilePermissionFormat;
import com.azure.storage.file.share.models.NtfsFileAttributes;
import com.azure.storage.file.share.models.ShareAccessPolicy;
import com.azure.storage.file.share.models.ShareAccessTier;
import com.azure.storage.file.share.models.ShareAudience;
import com.azure.storage.file.share.models.ShareDirectoryInfo;
import com.azure.storage.file.share.models.ShareErrorCode;
import com.azure.storage.file.share.models.ShareFileHttpHeaders;
import com.azure.storage.file.share.models.ShareFileInfo;
import com.azure.storage.file.share.models.ShareFileItem;
import com.azure.storage.file.share.models.ShareFilePermission;
import com.azure.storage.file.share.models.ShareInfo;
import com.azure.storage.file.share.models.ShareProperties;
import com.azure.storage.file.share.models.ShareProtocols;
import com.azure.storage.file.share.models.ShareRequestConditions;
import com.azure.storage.file.share.models.ShareRootSquash;
import com.azure.storage.file.share.models.ShareSignedIdentifier;
import com.azure.storage.file.share.models.ShareSnapshotInfo;
import com.azure.storage.file.share.models.ShareSnapshotsDeleteOptionType;
import com.azure.storage.file.share.models.ShareStatistics;
import com.azure.storage.file.share.models.ShareStorageException;
import com.azure.storage.file.share.models.ShareTokenIntent;
import com.azure.storage.file.share.options.ShareCreateOptions;
import com.azure.storage.file.share.options.ShareDeleteOptions;
import com.azure.storage.file.share.options.ShareDirectoryCreateOptions;
import com.azure.storage.file.share.options.ShareGetAccessPolicyOptions;
import com.azure.storage.file.share.options.ShareGetPropertiesOptions;
import com.azure.storage.file.share.options.ShareGetStatisticsOptions;
import com.azure.storage.file.share.options.ShareSetAccessPolicyOptions;
import com.azure.storage.file.share.options.ShareSetMetadataOptions;
import com.azure.storage.file.share.options.ShareSetPropertiesOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ShareApiTests extends FileShareTestBase {
    private ShareClient primaryShareClient;
    private String shareName;
    private static Map<String, String> testMetadata;
    private FileSmbProperties smbProperties;
    private static final String FILE_PERMISSION = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-188441444-3053964)S:NO_ACCESS_CONTROL";

    @BeforeEach
    public void setup() {
        shareName = generateShareName();
        primaryFileServiceClient = fileServiceBuilderHelper().buildClient();
        primaryShareClient = primaryFileServiceClient.getShareClient(shareName);
        testMetadata = Collections.singletonMap("testmetadata", "value");
        smbProperties = new FileSmbProperties().setNtfsFileAttributes(
            EnumSet.<NtfsFileAttributes> of(NtfsFileAttributes.NORMAL));
    }

    @Test
    public void getShareURL() {
        String accountName = StorageSharedKeyCredential
            .fromConnectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString()).getAccountName();
        String expectURL = String.format("https://%s.file.core.windows.net/%s", accountName, shareName);

        String shareURL = primaryShareClient.getShareUrl();

        assertEquals(expectURL, shareURL);
    }

    @Test
    public void getShareSnapshotURL() {
        String accountName = StorageSharedKeyCredential
            .fromConnectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString()).getAccountName();
        String expectURL = String.format("https://%s.file.core.windows.net/%s", accountName, shareName);
        primaryShareClient.create();
        ShareSnapshotInfo shareSnapshotInfo = primaryShareClient.createSnapshot();
        expectURL = expectURL + "?sharesnapshot=" + shareSnapshotInfo.getSnapshot();
        ShareClient newShareClient = shareBuilderHelper(shareName).snapshot(shareSnapshotInfo.getSnapshot())
            .buildClient();
        String shareURL = newShareClient.getShareUrl();

        assertEquals(expectURL, shareURL);

        String snapshotEndpoint = String.format("https://%s.file.core.windows.net/%s?sharesnapshot=%s", accountName,
            shareName, shareSnapshotInfo.getSnapshot());
        ShareClient client = getShareClientBuilder(snapshotEndpoint).credential(StorageSharedKeyCredential
            .fromConnectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString())).buildClient();

        assertEquals(client.getShareUrl(), snapshotEndpoint);
    }

    @Test
    public void getRootDirectoryClient() {
        ShareDirectoryClient directoryClient = primaryShareClient.getRootDirectoryClient();
        assertInstanceOf(ShareDirectoryClient.class, directoryClient);
    }

    public void getFileClientDoesNotCreateAFile() {
        ShareFileClient fileClient = primaryShareClient.getFileClient("testFile");
        assertInstanceOf(ShareFileClient.class, fileClient);
    }

    @Test
    public void exists() {
        primaryShareClient.create();
        assertTrue(primaryShareClient.exists());
    }

    @Test
    public void doesNotExist() {
        assertFalse(primaryShareClient.exists());
    }

    @Test
    public void existsError() {
        primaryShareClient = shareBuilderHelper(shareName).sasToken("sig=dummyToken").buildClient();

        ShareStorageException e = assertThrows(ShareStorageException.class, () -> primaryShareClient.exists());
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 403, ShareErrorCode.AUTHENTICATION_FAILED);
    }

    @Test
    public void createShare() {
        FileShareTestHelper.assertResponseStatusCode(primaryShareClient.createWithResponse(null, null, null, null),
            201);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-08-04")
    @Test
    public void createShareSasError() {
        ShareServiceClient unauthorizedServiceClient = fileServiceBuilderHelper()
            .sasToken("sig=dummyToken")
            .buildClient();

        ShareClient share = unauthorizedServiceClient.getShareClient(generateShareName());

        ShareStorageException e = assertThrows(ShareStorageException.class, share::create);
        assertEquals(ShareErrorCode.AUTHENTICATION_FAILED, e.getErrorCode());
        assertTrue(e.getServiceMessage().contains("AuthenticationErrorDetail"));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @Test
    public void createShareOAuth() {
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareClient shareClient = oAuthServiceClient.getShareClient(shareName);

        FileShareTestHelper.assertResponseStatusCode(shareClient.createWithResponse(null, null,
            null, null), 201);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("createShareWithArgsSupplier")
    public void createShareWithArgs(Map<String, String> metadata, Integer quota, ShareAccessTier accessTier) {
        FileShareTestHelper.assertResponseStatusCode(primaryShareClient.createWithResponse(new ShareCreateOptions()
            .setMetadata(metadata).setQuotaInGb(quota).setAccessTier(accessTier), null, null), 201);
    }

    private static Stream<Arguments> createShareWithArgsSupplier() {
        return Stream.of(
            Arguments.of(null, null, null),
            Arguments.of(null, 1, null),
            Arguments.of(testMetadata, null, null),
            Arguments.of(null, null, ShareAccessTier.HOT),
            Arguments.of(testMetadata, 1, ShareAccessTier.HOT),
            Arguments.of(testMetadata, 6000, null));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2019-12-12")
    @Test
    public void createShareAccessTierPremium() {
        ShareClient client = premiumFileServiceClient.getShareClient(generateShareName());
        ShareCreateOptions options = new ShareCreateOptions().setAccessTier(ShareAccessTier.PREMIUM);

        client.createWithResponse(options, null, null);

        ShareProperties response = client.getProperties();
        assertEquals(ShareAccessTier.PREMIUM.toString(), response.getAccessTier());
    }

    @ParameterizedTest
    @MethodSource("createShareWithInvalidArgsSupplier")
    public void createShareWithInvalidArgs(Map<String, String> metadata, Integer quota, ShareErrorCode errMessage) {
        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
            primaryShareClient.createWithResponse(metadata, quota, null, null));
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 400, errMessage);
    }

    private static Stream<Arguments> createShareWithInvalidArgsSupplier() {
        return Stream.of(Arguments.of(Collections.singletonMap("", "value"), 1, ShareErrorCode.EMPTY_METADATA_KEY),
            Arguments.of(Collections.singletonMap("metadata!", "value"), 1, ShareErrorCode.INVALID_METADATA));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2022-11-02")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void createDirectoryAndFileTrailingDot(boolean allowTrailingDot) {
        ShareServiceClient serviceClient = fileServiceBuilderHelper().allowTrailingDot(allowTrailingDot).buildClient();
        ShareClient shareClient = serviceClient.getShareClient(shareName);
        shareClient.create();
        ShareDirectoryClient rootDirectory = shareClient.getRootDirectoryClient();
        String dirName = generatePathName();
        String dirNameWithDot = dirName + ".";
        ShareDirectoryClient dirClient = shareClient.getDirectoryClient(dirNameWithDot);
        dirClient.create();

        String fileName = generatePathName();
        String fileNameWithDot = fileName + ".";
        ShareFileClient fileClient = rootDirectory.getFileClient(fileNameWithDot);
        fileClient.create(1024);

        List<String> foundDirectories = new ArrayList<>();
        List<String> foundFiles = new ArrayList<>();
        for (ShareFileItem fileRef : rootDirectory.listFilesAndDirectories()) {
            if (fileRef.isDirectory()) {
                foundDirectories.add(fileRef.getName());
            } else {
                foundFiles.add(fileRef.getName());
            }
        }

        assertEquals(foundDirectories.size(), 1);
        assertEquals(foundFiles.size(), 1);
        if (allowTrailingDot) {
            assertEquals(foundDirectories.get(0), dirNameWithDot);
            assertEquals(foundFiles.get(0), fileNameWithDot);
        } else {
            assertEquals(foundDirectories.get(0), dirName);
            assertEquals(foundFiles.get(0), fileName);
        }

    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void createFileAndDirectoryOAuth() {
        primaryShareClient.create();
        ShareClient oAuthShareClient = getOAuthShareClient(new ShareClientBuilder().shareName(shareName)
            .shareTokenIntent(ShareTokenIntent.BACKUP));
        String dirName = generatePathName();
        ShareDirectoryClient dirClient = oAuthShareClient.getDirectoryClient(dirName);

        Response<ShareDirectoryInfo> result = dirClient.createWithResponse(null, null, null, null, null);

        assertEquals(dirClient.getShareName(), shareName);
        assertEquals(dirClient.getDirectoryPath(), dirName);
        assertEquals(result.getValue().getETag(), result.getHeaders().getValue(HttpHeaderName.ETAG));

        String fileName = generatePathName();
        ShareFileClient fileClient = dirClient.getFileClient(fileName);
        Response<ShareFileInfo> response = fileClient.createWithResponse(Constants.KB, null, null, null, null, null,
            null);

        assertEquals(fileClient.getShareName(), shareName);
        String[] filePath = fileClient.getFilePath().split("/");
        assertEquals(fileName, filePath[1]); // compareWithfilename
        assertEquals(response.getValue().getETag(), response.getHeaders().getValue(HttpHeaderName.ETAG));
    }

    @Test
    public void createIfNotExistsShare() {
        FileShareTestHelper.assertResponseStatusCode(primaryShareClient.createIfNotExistsWithResponse(null, null, null),
            201);
    }

    @Test
    public void createIfNotExistsShareThatAlreadyExists() {
        ShareClient client = premiumFileServiceClient.getShareClient(generateShareName());
        Response<ShareInfo> initialResponse = client.createIfNotExistsWithResponse(new ShareCreateOptions(), null,
            null);
        Response<ShareInfo> secondResponse = client.createIfNotExistsWithResponse(new ShareCreateOptions(), null, null);
        assertEquals(initialResponse.getStatusCode(), 201);
        assertEquals(secondResponse.getStatusCode(), 409);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("createIfNotExistsShareWithArgsSupplier")
    public void createIfNotExistsShareWithArgs(Map<String, String> metadata, Integer quota,
        ShareAccessTier accessTier) {
        FileShareTestHelper.assertResponseStatusCode(primaryShareClient.createIfNotExistsWithResponse(
            new ShareCreateOptions().setMetadata(metadata).setQuotaInGb(quota).setAccessTier(accessTier), null, null),
            201);
    }

    private static Stream<Arguments> createIfNotExistsShareWithArgsSupplier() {
        return Stream.of(Arguments.of(null, null, null),
            Arguments.of(null, 1, null),
            Arguments.of(testMetadata, null, null),
            Arguments.of(null, null, ShareAccessTier.HOT),
            Arguments.of(testMetadata, 1, ShareAccessTier.HOT));
    }

    @ParameterizedTest
    @MethodSource("createShareWithInvalidArgsSupplier")
    public void createIfNotExistsShareWithinvalidArgs(Map<String, String> metadata, Integer quota,
        ShareErrorCode errMessage) {
        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
            primaryShareClient.createIfNotExistsWithResponse(new ShareCreateOptions().setMetadata(metadata)
                .setQuotaInGb(quota), null, null));

        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 400, errMessage);
    }

    @Test
    public void createSnapshot() {
        primaryShareClient.create();
        String shareSnapshotName = generatePathName();

        Response<ShareSnapshotInfo> createSnapshotResponse = primaryShareClient
            .createSnapshotWithResponse(null, null, null);

        ShareClient shareSnapshotClient = new ShareClientBuilder().shareName(shareSnapshotName)
            .connectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString())
            .snapshot(createSnapshotResponse.getValue().getSnapshot())
            .buildClient();
        assertEquals(createSnapshotResponse.getValue().getSnapshot(), shareSnapshotClient.getSnapshotId());
    }

    @Test
    public void createSnapshotError() {
        ShareStorageException e = assertThrows(ShareStorageException.class, () -> primaryShareClient.createSnapshot());
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.SHARE_NOT_FOUND);
    }

    @Test
    public void createSnapshotMetadata() {
        primaryShareClient.create();
        String shareSnapshotName = generatePathName();

        Response<ShareSnapshotInfo> createSnapshotResponse = primaryShareClient.createSnapshotWithResponse(testMetadata,
            null, null);
        ShareClient shareSnapshotClient = new ShareClientBuilder().shareName(shareSnapshotName)
            .connectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString())
            .snapshot(createSnapshotResponse.getValue().getSnapshot())
            .buildClient();
        assertEquals(createSnapshotResponse.getValue().getSnapshot(), shareSnapshotClient.getSnapshotId());
    }

    @Test
    public void createSnapshotMetadataError() {
        primaryShareClient.create();
        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
            primaryShareClient.createSnapshotWithResponse(Collections.singletonMap("", "value"), null, null));
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.EMPTY_METADATA_KEY);
    }

    @Test
    public void deleteShare() {
        primaryShareClient.create();
        FileShareTestHelper.assertResponseStatusCode(primaryShareClient.deleteWithResponse(null, null), 202);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @Test
    public void deleteShareOAuth() {
        primaryShareClient.create();
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareClient shareClient = oAuthServiceClient.getShareClient(shareName);

        FileShareTestHelper.assertResponseStatusCode(shareClient.deleteWithResponse(null, null), 202);
    }

    @Test
    public void deleteShareDeleteSnapshotOptions() {
        primaryShareClient.create();
        String snap1 = primaryShareClient.createSnapshot().getSnapshot();
        String snap2 = primaryShareClient.createSnapshot().getSnapshot();

        primaryShareClient.deleteWithResponse(new ShareDeleteOptions()
            .setDeleteSnapshotsOptions(ShareSnapshotsDeleteOptionType.INCLUDE), null, null);

        assertFalse(primaryShareClient.getSnapshotClient(snap1).exists());
        assertFalse(primaryShareClient.getSnapshotClient(snap2).exists());
    }

    @Test
    public void deleteShareDeleteSnapshotOptionsError() {
        primaryShareClient.create();
        primaryShareClient.createSnapshot().getSnapshot();
        primaryShareClient.createSnapshot().getSnapshot();
        assertThrows(ShareStorageException.class, () ->
            primaryShareClient.deleteWithResponse(new ShareDeleteOptions(), null, null));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2020-02-10")
    @Test
    public void deleteShareLease() {
        primaryShareClient.create();
        String leaseID = setupShareLeaseCondition(primaryShareClient, RECEIVED_LEASE_ID);

        FileShareTestHelper.assertResponseStatusCode(primaryShareClient.deleteWithResponse(new ShareDeleteOptions()
            .setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID)), null, null), 202);
    }

    @Test
    public void deleteShareLeaseError() {
        primaryShareClient.create();
        String leaseID = setupShareLeaseCondition(primaryShareClient, GARBAGE_LEASE_ID);
        assertThrows(ShareStorageException.class, () ->
            primaryShareClient.deleteWithResponse(new ShareDeleteOptions().setRequestConditions(
                new ShareRequestConditions().setLeaseId(leaseID)), null, null));

    }

    public void deleteShareError() {
        ShareStorageException e = assertThrows(ShareStorageException.class, () -> primaryShareClient.delete());
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.SHARE_NOT_FOUND);
    }

    @Test
    public void deleteIfExistsShare() {
        primaryShareClient.create();
        FileShareTestHelper.assertResponseStatusCode(primaryShareClient.deleteIfExistsWithResponse(null, null, null),
            202);
    }

    @Test
    public void deleteIfExistsShareMin() {
        primaryShareClient.create();
        assertTrue(primaryShareClient.deleteIfExists());
    }

    @Test
    public void deleteIfExistsShareDeleteSnapshotOptions() {
        primaryShareClient.create();
        String snap1 = primaryShareClient.createSnapshot().getSnapshot();
        String snap2 = primaryShareClient.createSnapshot().getSnapshot();

        primaryShareClient.deleteIfExistsWithResponse(new ShareDeleteOptions()
            .setDeleteSnapshotsOptions(ShareSnapshotsDeleteOptionType.INCLUDE), null, null);

        assertFalse(primaryShareClient.getSnapshotClient(snap1).exists());
        assertFalse(primaryShareClient.getSnapshotClient(snap2).exists());
    }

    @Test
    public void deleteIfExistsShareDeleteSnapshotOptionsError() {
        primaryShareClient.create();
        primaryShareClient.createSnapshot().getSnapshot();
        primaryShareClient.createSnapshot().getSnapshot();
        assertThrows(ShareStorageException.class, () ->
            primaryShareClient.deleteIfExistsWithResponse(new ShareDeleteOptions(), null, null));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2020-02-10")
    @Test
    public void deleteIfExistsShareLease() {
        primaryShareClient.create();
        String leaseID = setupShareLeaseCondition(primaryShareClient, RECEIVED_LEASE_ID);

        FileShareTestHelper.assertResponseStatusCode(primaryShareClient.deleteIfExistsWithResponse(
            new ShareDeleteOptions().setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID)), null,
            null), 202);
    }

    @Test
    public void deleteIfExistsShareLeaseError() {
        primaryShareClient.create();
        String leaseID = setupShareLeaseCondition(primaryShareClient, GARBAGE_LEASE_ID);
        assertThrows(ShareStorageException.class, () -> primaryShareClient.deleteIfExistsWithResponse(
            new ShareDeleteOptions().setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID)), null,
            null));
    }

    @Test
    public void deleteIfExistsShareThatDoesNotExists() {
        ShareClient client = premiumFileServiceClient.getShareClient(generateShareName());
        Response<Boolean> response = client.deleteIfExistsWithResponse(null, null, null);
        assertFalse(response.getValue());
        FileShareTestHelper.assertResponseStatusCode(response, 404);
        assertFalse(client.exists());
    }

    @Test
    public void getProperties() {
        primaryShareClient.createWithResponse(testMetadata, 1, null, null);
        Response<ShareProperties> getPropertiesResponse = primaryShareClient.getPropertiesWithResponse(null, null);

        FileShareTestHelper.assertResponseStatusCode(getPropertiesResponse, 200);
        assertEquals(testMetadata, getPropertiesResponse.getValue().getMetadata());
        assertEquals(getPropertiesResponse.getValue().getQuota(), 1);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @Test
    public void getPropertiesOAuth() {
        primaryShareClient.createWithResponse(testMetadata, 1, null, null);
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareClient shareClient = oAuthServiceClient.getShareClient(shareName);

        Response<ShareProperties> getPropertiesResponse = shareClient.getPropertiesWithResponse(null, null);

        FileShareTestHelper.assertResponseStatusCode(getPropertiesResponse, 200);
        assertEquals(testMetadata, getPropertiesResponse.getValue().getMetadata());
        assertEquals(getPropertiesResponse.getValue().getQuota(), 1);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2020-02-10")
    @Test
    public void getPropertiesLease() {
        primaryShareClient.create();
        String leaseID = setupShareLeaseCondition(primaryShareClient, RECEIVED_LEASE_ID);
        FileShareTestHelper.assertResponseStatusCode(primaryShareClient.getPropertiesWithResponse(
            new ShareGetPropertiesOptions().setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID)),
            null, null), 200);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2020-02-10")
    @Test
    public void getPropertiesLeaseError() {
        primaryShareClient.create();
        String leaseID = setupShareLeaseCondition(primaryShareClient, GARBAGE_LEASE_ID);
        assertThrows(ShareStorageException.class, () -> primaryShareClient.getPropertiesWithResponse(
            new ShareGetPropertiesOptions().setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID)),
            null, null));
    }

    @Test
    public void getPropertiesError() {
        ShareStorageException e = assertThrows(ShareStorageException.class, () -> primaryShareClient.getProperties());
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.SHARE_NOT_FOUND);
    }

    @PlaybackOnly
    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#getPropertiesPremiumSupplier")
    public void getPropertiesPremium(String protocol, ShareRootSquash rootSquash) {
        ShareProtocols enabledProtocol = ModelHelper.parseShareProtocols(protocol);
        ShareClient premiumShareClient = premiumFileServiceClient.createShareWithResponse(generateShareName(),
                new ShareCreateOptions().setMetadata(testMetadata).setProtocols(enabledProtocol)
                    .setRootSquash(rootSquash), null, null).getValue();

        Response<ShareProperties> getPropertiesResponse = premiumShareClient.getPropertiesWithResponse(null, null);
        ShareProperties shareProperties = getPropertiesResponse.getValue();

        FileShareTestHelper.assertResponseStatusCode(getPropertiesResponse, 200);
        assertEquals(testMetadata, shareProperties.getMetadata());
        assertNotNull(shareProperties.getProvisionedIops());
        assertNotNull(shareProperties.getProvisionedBandwidthMiBps());
        assertNotNull(shareProperties.getNextAllowedQuotaDowngradeTime());
        assertEquals(shareProperties.getProtocols().toString(), enabledProtocol.toString());
        assertEquals(shareProperties.getRootSquash(), rootSquash);
    }

    @PlaybackOnly
    @Test
    public void setPremiumProperties() {
        List<ShareRootSquash> rootSquashes = Arrays.asList(
            ShareRootSquash.ALL_SQUASH,
            ShareRootSquash.NO_ROOT_SQUASH,
            ShareRootSquash.ROOT_SQUASH);

        for (ShareRootSquash rootSquash : rootSquashes) {
            ShareClient premiumShareClient = premiumFileServiceClient.createShareWithResponse(generateShareName(),
                new ShareCreateOptions().setProtocols(new ShareProtocols().setNfsEnabled(true)), null, null).getValue();
            premiumShareClient.setProperties(new ShareSetPropertiesOptions().setRootSquash(rootSquash));
            assertEquals(premiumShareClient.getProperties().getRootSquash(), rootSquash);
        }

    }

    @Test
    public void setAccessPolicy() {
        primaryShareClient.create();
        ShareSignedIdentifier identifier = new ShareSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new ShareAccessPolicy()
                .setStartsOn(testResourceNamer.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime())
                .setExpiresOn(testResourceNamer.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime()
                    .plusDays(1))
                .setPermissions("r"));

        List<ShareSignedIdentifier> ids = Arrays.asList(identifier);
        primaryShareClient.setAccessPolicy(ids);
        assertEquals(primaryShareClient.getAccessPolicy().iterator().next().getId(), "0000");
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @Test
    public void setAccessPolicyOAuth() {
        primaryShareClient.create();
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareClient shareClient = oAuthServiceClient.getShareClient(shareName);

        ShareSignedIdentifier identifier = new ShareSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new ShareAccessPolicy()
                .setStartsOn(testResourceNamer.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime())
                .setExpiresOn(testResourceNamer.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime()
                    .plusDays(1))
                .setPermissions("r"));

        List<ShareSignedIdentifier> ids = Arrays.asList(identifier);
        shareClient.setAccessPolicy(ids);
        assertEquals(shareClient.getAccessPolicy().iterator().next().getId(), "0000");
    }

    @Test
    public void setAccessPolicyIds() {
        primaryShareClient.create();
        ShareSignedIdentifier identifier = new ShareSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new ShareAccessPolicy()
                .setStartsOn(testResourceNamer.now())
                .setExpiresOn(testResourceNamer.now().plusDays(1))
                .setPermissions("r"));
        ShareSignedIdentifier identifier2 = new ShareSignedIdentifier()
            .setId("0001")
            .setAccessPolicy(new ShareAccessPolicy()
                .setStartsOn(testResourceNamer.now())
                .setExpiresOn(testResourceNamer.now().plusDays(2))
                .setPermissions("w"));
        List<ShareSignedIdentifier> ids = Arrays.asList(identifier, identifier2);

        Response<ShareInfo> response = primaryShareClient.setAccessPolicyWithResponse(ids, null, null);
        Iterator<ShareSignedIdentifier> receivedIdentifiers = primaryShareClient.getAccessPolicy().iterator();

        FileShareTestHelper.assertResponseStatusCode(response, 200);
        validateBasicHeaders(response.getHeaders());
        ShareSignedIdentifier id0 = receivedIdentifiers.next();
        assertEquals(id0.getAccessPolicy().getExpiresOn(), identifier.getAccessPolicy().getExpiresOn());
        assertEquals(id0.getAccessPolicy().getStartsOn(), identifier.getAccessPolicy().getStartsOn());
        assertEquals(id0.getAccessPolicy().getPermissions(), identifier.getAccessPolicy().getPermissions());
        ShareSignedIdentifier id1 = receivedIdentifiers.next();
        assertEquals(id1.getAccessPolicy().getExpiresOn(), identifier2.getAccessPolicy().getExpiresOn());
        assertEquals(id1.getAccessPolicy().getStartsOn(), identifier2.getAccessPolicy().getStartsOn());
        assertEquals(id1.getAccessPolicy().getPermissions(), identifier2.getAccessPolicy().getPermissions());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2020-02-10")
    @Test
    public void setAccessPolicyLease() {
        primaryShareClient.create();
        String leaseID = setupShareLeaseCondition(primaryShareClient, RECEIVED_LEASE_ID);

        FileShareTestHelper.assertResponseStatusCode(primaryShareClient.setAccessPolicyWithResponse(
            new ShareSetAccessPolicyOptions().setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID)),
            null, null), 200);
    }

    @Test
    public void setAccessPolicyLeaseError() {
        primaryShareClient.create();
        String leaseID = setupShareLeaseCondition(primaryShareClient, GARBAGE_LEASE_ID);

        assertThrows(ShareStorageException.class, () -> primaryShareClient.setAccessPolicyWithResponse(
            new ShareSetAccessPolicyOptions().setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID)),
                null, null));
    }

    @Test
    public void setAccessPolicyError() {
        assertThrows(ShareStorageException.class, () -> primaryShareClient.setAccessPolicy(null));
    }

    @Test
    public void getAccessPolicy() {
        primaryShareClient.create();
        ShareSignedIdentifier identifier = new ShareSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new ShareAccessPolicy()
                .setStartsOn(testResourceNamer.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime())
                .setExpiresOn(testResourceNamer.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime()
                    .plusDays(1))
                .setPermissions("r"));

        List<ShareSignedIdentifier> ids = Arrays.asList(identifier);
        primaryShareClient.setAccessPolicy(ids);

        ShareSignedIdentifier id = primaryShareClient.getAccessPolicy().iterator().next();

        assertEquals(id.getId(), identifier.getId());
        assertEquals(id.getAccessPolicy().getStartsOn(), identifier.getAccessPolicy().getStartsOn());
        assertEquals(id.getAccessPolicy().getExpiresOn(), identifier.getAccessPolicy().getExpiresOn());
        assertEquals(id.getAccessPolicy().getPermissions(), identifier.getAccessPolicy().getPermissions());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @Test
    public void getAccessPolicyOAuth() {
        primaryShareClient.create();
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareClient shareClient = oAuthServiceClient.getShareClient(shareName);


        ShareSignedIdentifier identifier = new ShareSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new ShareAccessPolicy()
                .setStartsOn(testResourceNamer.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime())
                .setExpiresOn(testResourceNamer.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime()
                    .plusDays(1))
                .setPermissions("r"));

        List<ShareSignedIdentifier> ids = Arrays.asList(identifier);
        shareClient.setAccessPolicy(ids);

        ShareSignedIdentifier id = shareClient.getAccessPolicy().iterator().next();

        assertEquals(id.getId(), identifier.getId());
        assertEquals(id.getAccessPolicy().getStartsOn(), identifier.getAccessPolicy().getStartsOn());
        assertEquals(id.getAccessPolicy().getExpiresOn(), identifier.getAccessPolicy().getExpiresOn());
        assertEquals(id.getAccessPolicy().getPermissions(), identifier.getAccessPolicy().getPermissions());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2020-02-10")
    @Test
    public void getAccessPolicyLease() {
        primaryShareClient.create();
        String leaseID = setupShareLeaseCondition(primaryShareClient, RECEIVED_LEASE_ID);
        PagedIterable<ShareSignedIdentifier> response = primaryShareClient.getAccessPolicy(
            new ShareGetAccessPolicyOptions().setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID)));
        assertFalse(response.iterator().hasNext());
    }

    @Test
    public void getAccessPolicyLeaseError() {
        primaryShareClient.create();
        String leaseID = setupShareLeaseCondition(primaryShareClient, GARBAGE_LEASE_ID);
        assertThrows(ShareStorageException.class, () ->
            primaryShareClient.getAccessPolicy(new ShareGetAccessPolicyOptions()
                .setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID))).iterator().hasNext());
    }

    @Test
    public void getAccessPolicyError() {
        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
            primaryShareClient.getAccessPolicy().iterator().hasNext());
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.SHARE_NOT_FOUND);
    }

    @Test
    public void setPropertiesQuota() {
        primaryShareClient.createWithResponse(null, 1, null, null);

        ShareProperties getQuotaBeforeResponse = primaryShareClient.getProperties();
        Response<ShareInfo> setQuotaResponse = primaryShareClient.setQuotaWithResponse(2, null, null);
        ShareProperties getQuotaAfterResponse = primaryShareClient.getProperties();

        assertEquals(getQuotaBeforeResponse.getQuota(), 1);
        FileShareTestHelper.assertResponseStatusCode(setQuotaResponse, 200);
        assertEquals(getQuotaAfterResponse.getQuota(), 2);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2019-12-12")
    @Test
    public void setPropertiesAccessTier() {
        primaryShareClient.createWithResponse(new ShareCreateOptions().setAccessTier(ShareAccessTier.HOT), null, null);
        OffsetDateTime time = testResourceNamer.now().truncatedTo(ChronoUnit.SECONDS);

        ShareProperties getAccessTierBeforeResponse = primaryShareClient.getProperties();
        Response<ShareInfo> setAccessTierResponse = primaryShareClient.setPropertiesWithResponse(
            new ShareSetPropertiesOptions().setAccessTier(ShareAccessTier.TRANSACTION_OPTIMIZED), null, null);
        ShareProperties getAccessTierAfterResponse = primaryShareClient.getProperties();

        assertEquals(getAccessTierBeforeResponse.getAccessTier(), ShareAccessTier.HOT.toString());
        FileShareTestHelper.assertResponseStatusCode(setAccessTierResponse, 200);
        assertEquals(getAccessTierAfterResponse.getAccessTier(), ShareAccessTier.TRANSACTION_OPTIMIZED.toString());
        assertNotNull(getAccessTierAfterResponse.getAccessTierChangeTime());
        assertTrue(getAccessTierAfterResponse.getAccessTierChangeTime().isEqual(time)
            || getAccessTierAfterResponse.getAccessTierChangeTime().isAfter(time.minusSeconds(1)));
        assertTrue(getAccessTierAfterResponse.getAccessTierChangeTime().isBefore(time.plusMinutes(1)));
        assertEquals(getAccessTierAfterResponse.getAccessTierTransitionState(), "pending-from-hot");
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2020-02-10")
    @Test
    public void setPropertiesLease() {
        primaryShareClient.createWithResponse(new ShareCreateOptions().setAccessTier(ShareAccessTier.HOT), null, null);
        String leaseID = setupShareLeaseCondition(primaryShareClient, RECEIVED_LEASE_ID);

        Response<ShareInfo> setAccessTierResponse = primaryShareClient.setPropertiesWithResponse(
            new ShareSetPropertiesOptions().setAccessTier(ShareAccessTier.COOL)
                .setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID)), null, null);

        FileShareTestHelper.assertResponseStatusCode(setAccessTierResponse, 200);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2020-02-10")
    @Test
    public void setPropertiesLeaseError() {
        primaryShareClient.createWithResponse(new ShareCreateOptions().setAccessTier(ShareAccessTier.HOT), null, null);
        String leaseID = setupShareLeaseCondition(primaryShareClient, GARBAGE_LEASE_ID);

        assertThrows(ShareStorageException.class, () -> primaryShareClient.setPropertiesWithResponse(
            new ShareSetPropertiesOptions()
                .setAccessTier(ShareAccessTier.COOL)
                .setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID)), null, null));
    }

    @Test
    public void setPropertiesError() {
        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
            primaryShareClient.setProperties(new ShareSetPropertiesOptions()));
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.SHARE_NOT_FOUND);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @Test
    public void setPropertiesOAuth() {
        primaryShareClient.createWithResponse(new ShareCreateOptions().setAccessTier(ShareAccessTier.HOT), null, null);
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareClient shareClient = oAuthServiceClient.getShareClient(shareName);

        Response<ShareInfo> setAccessTierResponse = shareClient.setPropertiesWithResponse(
            new ShareSetPropertiesOptions().setAccessTier(ShareAccessTier.COOL), null, null);

        FileShareTestHelper.assertResponseStatusCode(setAccessTierResponse, 200);
    }

    @Test
    public void setMetadata() {
        primaryShareClient.createWithResponse(testMetadata, null, null, null);
        Map<String, String> metadataAfterSet = Collections.singletonMap("afterset", "value");

        ShareProperties getMetadataBeforeResponse = primaryShareClient.getProperties();
        Response<ShareInfo> setMetadataResponse = primaryShareClient.setMetadataWithResponse(metadataAfterSet, null,
            null);
        ShareProperties getMetadataAfterResponse = primaryShareClient.getProperties();

        assertEquals(testMetadata, getMetadataBeforeResponse.getMetadata());
        FileShareTestHelper.assertResponseStatusCode(setMetadataResponse, 200);
        assertEquals(metadataAfterSet, getMetadataAfterResponse.getMetadata());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @Test
    public void setMetadataOAuth() {
        primaryShareClient.createWithResponse(testMetadata, null, null, null);
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareClient shareClient = oAuthServiceClient.getShareClient(shareName);

        Map<String, String> metadataAfterSet = Collections.singletonMap("afterset", "value");

        ShareProperties getMetadataBeforeResponse = shareClient.getProperties();
        Response<ShareInfo> setMetadataResponse = shareClient.setMetadataWithResponse(metadataAfterSet, null,
            null);
        ShareProperties getMetadataAfterResponse = shareClient.getProperties();

        assertEquals(testMetadata, getMetadataBeforeResponse.getMetadata());
        FileShareTestHelper.assertResponseStatusCode(setMetadataResponse, 200);
        assertEquals(metadataAfterSet, getMetadataAfterResponse.getMetadata());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2020-02-10")
    @Test
    public void setMetadataLease() {
        primaryShareClient.createWithResponse(null, 1, null, null);
        String leaseID = setupShareLeaseCondition(primaryShareClient, RECEIVED_LEASE_ID);

        Response<ShareInfo> resp = primaryShareClient.setMetadataWithResponse(new ShareSetMetadataOptions()
            .setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID)), null, null);

        FileShareTestHelper.assertResponseStatusCode(resp, 200);
    }

    @Test
    public void setMetadataLeaseError() {
        primaryShareClient.createWithResponse(null, 1, null, null);
        String leaseID = setupShareLeaseCondition(primaryShareClient, GARBAGE_LEASE_ID);

        assertThrows(ShareStorageException.class, () -> primaryShareClient.setMetadataWithResponse(
            new ShareSetMetadataOptions().setRequestConditions(
                new ShareRequestConditions().setLeaseId(leaseID)), null, null));
    }

    @Test
    public void setMetadataError() {
        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
            primaryShareClient.setMetadata(testMetadata));
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.SHARE_NOT_FOUND);
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#getStatisticsSupplier")
    public void getStatistics(long size, int gigabytes) {
        primaryShareClient.create();
        primaryShareClient.createFile("tempFile", size);
        Response<ShareStatistics> resp = primaryShareClient.getStatisticsWithResponse(null, null);
        FileShareTestHelper.assertResponseStatusCode(resp, 200);
        assertEquals(resp.getValue().getShareUsageInBytes(), size);
        assertEquals(resp.getValue().getShareUsageInGB(), gigabytes);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2020-02-10")
    @Test
    public void getStatisticsLease() {
        primaryShareClient.create();
        String leaseID = setupShareLeaseCondition(primaryShareClient, RECEIVED_LEASE_ID);
        Response<ShareStatistics> resp = primaryShareClient.getStatisticsWithResponse(new ShareGetStatisticsOptions()
            .setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID)), null, null);
        FileShareTestHelper.assertResponseStatusCode(resp, 200);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2020-02-10")
    @Test
    public void getStatisticsLeaseError() {
        primaryShareClient.create();
        String leaseID = setupShareLeaseCondition(primaryShareClient, GARBAGE_LEASE_ID);
        assertThrows(ShareStorageException.class, () -> primaryShareClient.getStatisticsWithResponse(
            new ShareGetStatisticsOptions()
                .setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID)), null, null));
    }

    @Test
    public void getStatisticsError() {
        ShareStorageException e = assertThrows(ShareStorageException.class, () -> primaryShareClient.getStatistics());
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.SHARE_NOT_FOUND);
    }

    @Test
    public void createDirectory() {
        primaryShareClient.create();
        FileShareTestHelper.assertResponseStatusCode(
            primaryShareClient.createDirectoryWithResponse("testCreateDirectory", null, null, null, null, null), 201);
    }

    @Test
    public void createDirectoryFilePermission() {
        primaryShareClient.create();
        FileShareTestHelper.assertResponseStatusCode(primaryShareClient.createDirectoryWithResponse(
            "testCreateDirectory", null, FILE_PERMISSION, null, null, null), 201);
    }

    @Test
    public void createDirectoryFilePermissionKey() {
        primaryShareClient.create();
        String filePermissionKey = primaryShareClient.createPermission(FILE_PERMISSION);
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setFilePermissionKey(filePermissionKey);
        FileShareTestHelper.assertResponseStatusCode(
            primaryShareClient.createDirectoryWithResponse("testCreateDirectory", smbProperties, null, null, null,
                null), 201);
    }

    @Test
    public void createDirectoryInvalidName() {
        primaryShareClient.create();
        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
            primaryShareClient.createDirectory("test/directory"));
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.PARENT_NOT_FOUND);
    }

    @Test
    public void createDirectoryMetadata() {
        primaryShareClient.create();
        FileShareTestHelper.assertResponseStatusCode(primaryShareClient.createDirectoryWithResponse(
            "testCreateDirectory", null, null, testMetadata, null, null), 201);
    }

    @Test
    public void createDirectoryMetadataError() {
        primaryShareClient.create();
        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
            primaryShareClient.createDirectoryWithResponse("testdirectory", null, null,
                Collections.singletonMap("", "value"), null, null));
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.EMPTY_METADATA_KEY);
    }

    @Test
    public void createIfNotExistsDirectory() {
        primaryShareClient.create();
        FileShareTestHelper.assertResponseStatusCode(primaryShareClient.createDirectoryIfNotExistsWithResponse(
            "testCreateDirectory", new ShareDirectoryCreateOptions(), null, null), 201);
    }

    @Test
    public void createIfNotExistsDirectoryFilePermission() {
        primaryShareClient.create();
        FileShareTestHelper.assertResponseStatusCode(primaryShareClient.createDirectoryIfNotExistsWithResponse(
            "testCreateDirectory", new ShareDirectoryCreateOptions().setFilePermission(FILE_PERMISSION), null, null),
            201);
    }

    @Test
    public void createIfNotExistsDirectoryFilePermissionKey() {
        primaryShareClient.create();
        String filePermissionKey = primaryShareClient.createPermission(FILE_PERMISSION);
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setFilePermissionKey(filePermissionKey);
        FileShareTestHelper.assertResponseStatusCode(primaryShareClient.createDirectoryIfNotExistsWithResponse(
            "testCreateDirectory", new ShareDirectoryCreateOptions().setSmbProperties(smbProperties), null, null), 201);
    }

    @Test
    public void createIfNotExistsDirectoryInvalidName() {
        primaryShareClient.create();
        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
            primaryShareClient.createDirectoryIfNotExists("test/directory"));
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.PARENT_NOT_FOUND);
    }

    @Test
    public void createIfNotExistsDirectoryMetadata() {
        primaryShareClient.create();
        FileShareTestHelper.assertResponseStatusCode(primaryShareClient.createDirectoryIfNotExistsWithResponse(
            "testCreateDirectory", new ShareDirectoryCreateOptions().setMetadata(testMetadata), null, null), 201);
    }

    @Test
    public void createIfNotExistsDirectoryMetadataError() {
        primaryShareClient.create();
        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
            primaryShareClient.createDirectoryIfNotExistsWithResponse("testdirectory",
                new ShareDirectoryCreateOptions().setMetadata(Collections.singletonMap("", "value")), null, null));
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.EMPTY_METADATA_KEY);
    }

    @Test
    public void createIfNotExistsDirectoryThatAlreadyExists() {
        ShareClient client = premiumFileServiceClient.getShareClient(generateShareName());
        client.create();
        Response<ShareDirectoryClient> initialResponse = client.createDirectoryIfNotExistsWithResponse(
            "testCreateDirectory", new ShareDirectoryCreateOptions(), null, null);
        Response<ShareDirectoryClient> secondResponse = client.createDirectoryIfNotExistsWithResponse(
            "testCreateDirectory", new ShareDirectoryCreateOptions(), null, null);

        FileShareTestHelper.assertResponseStatusCode(initialResponse, 201);
        FileShareTestHelper.assertResponseStatusCode(secondResponse, 409);
    }

    @Test
    public void createFile() {
        primaryShareClient.create();
        FileShareTestHelper.assertResponseStatusCode(
            primaryShareClient.createFileWithResponse("testCreateFile", 1024, null, null, null, null, null, null), 201);
    }

    @Test
    public void createFileFilePermission() {
        primaryShareClient.create();
        FileShareTestHelper.assertResponseStatusCode(primaryShareClient.createFileWithResponse("testCreateFile", 1024,
            null, null, FILE_PERMISSION, null, null, null), 201);
    }

    @Test
    public void createFileFilePermissionKey() {
        primaryShareClient.create();
        String filePermissionKey = primaryShareClient.createPermission(FILE_PERMISSION);
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setFilePermissionKey(filePermissionKey);

        FileShareTestHelper.assertResponseStatusCode(primaryShareClient.createFileWithResponse("testCreateFile", 1024,
            null, smbProperties, null, null, null, null), 201);
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#createFileInvalidArgsSupplier")
    public void createFileInvalidArgs(String fileName, long maxSize, int statusCode, ShareErrorCode errMsg) {
        primaryShareClient.create();
        ShareStorageException e = assertThrows(ShareStorageException.class,
            () -> primaryShareClient.createFileWithResponse(fileName, maxSize, null, null, null, null, null, null));
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, statusCode, errMsg);
    }

    @Test
    public void createFileMaxOverload() {
        primaryShareClient.create();
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now());
        FileShareTestHelper.assertResponseStatusCode(primaryShareClient.createFileWithResponse("testCreateFile", 1024,
            null, smbProperties, FILE_PERMISSION, testMetadata, null, null), 201);
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#createFileMaxOverloadInvalidArgsSupplier")
    public void createFileMaxOverloadInvalidArgs(String fileName, long maxSize, ShareFileHttpHeaders httpHeaders,
        Map<String, String> metadata, ShareErrorCode errMsg) {
        primaryShareClient.create();
        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
            primaryShareClient.createFileWithResponse(fileName, maxSize, httpHeaders, null, null, metadata, null,
                null));
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 400, errMsg);
    }

    @Test
    public void createFileInRootDirectory() {
        primaryShareClient.create();
        ShareDirectoryClient directoryClient = primaryShareClient.getRootDirectoryClient();
        FileShareTestHelper.assertResponseStatusCode(
            directoryClient.createFileWithResponse("testCreateFile", 1024, null, null, null, null, null, null), 201);
    }

    @Test
    public void deleteDirectory() {
        String directoryName = "testCreateDirectory";
        primaryShareClient.create();
        primaryShareClient.createDirectory(directoryName);

        FileShareTestHelper.assertResponseStatusCode(primaryShareClient.deleteDirectoryWithResponse(directoryName, null,
            null), 202);
    }

    @Test
    public void deleteDirectoryError() {
        primaryShareClient.create();
        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
            primaryShareClient.deleteDirectory("testdirectory"));
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    public void deleteIfExistsDirectory() {
        String directoryName = "testCreateDirectory";
        primaryShareClient.create();
        primaryShareClient.createDirectory(directoryName);
        FileShareTestHelper.assertResponseStatusCode(primaryShareClient.deleteDirectoryIfExistsWithResponse(
            directoryName, null, null), 202);
    }

    @Test
    public void deleteIfExistsDirectoryMin() {
        String directoryName = "testCreateDirectory";
        primaryShareClient.create();
        primaryShareClient.createDirectory(directoryName);
        assertTrue(primaryShareClient.deleteDirectoryIfExists(directoryName));
    }

    @Test
    public void deleteIfExistsDirectoryThatDoesNotExist() {
        String directoryName = "testCreateDirectory";
        primaryShareClient.create();
        Response<Boolean> response = primaryShareClient.deleteDirectoryIfExistsWithResponse(directoryName, null, null);
        assertFalse(response.getValue());
        FileShareTestHelper.assertResponseStatusCode(response, 404);
    }

    @Test
    public void deleteFile() {
        String fileName = "testCreateFile";
        primaryShareClient.create();
        primaryShareClient.createFile(fileName, 1024);

        FileShareTestHelper.assertResponseStatusCode(
            primaryShareClient.deleteFileWithResponse(fileName, null, null), 202);
    }

    @Test
    public void deleteFileError() {
        primaryShareClient.create();
        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
            primaryShareClient.deleteFile("testdirectory"));
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    public void deleteIfExistsFile() {
        String fileName = "testCreateFile";
        primaryShareClient.create();
        primaryShareClient.createFile(fileName, 1024);

        FileShareTestHelper.assertResponseStatusCode(
            primaryShareClient.deleteFileIfExistsWithResponse(fileName, null, null, null), 202);
    }

    @Test
    public void deleteIfExistsFileMin() {
        String fileName = "testCreateFile";
        primaryShareClient.create();
        primaryShareClient.createFile(fileName, 1024);
        assertTrue(primaryShareClient.deleteFileIfExists(fileName));
    }

    @Test
    public void deleteIfExistsFileThatDoesNotExist() {
        primaryShareClient.create();
        Response<Boolean> response = primaryShareClient.deleteFileIfExistsWithResponse("testCreateFile", null, null,
            null);

        assertFalse(response.getValue());
        FileShareTestHelper.assertResponseStatusCode(response, 404);
    }

    @Test
    public void createPermission() {
        primaryShareClient.create();
        FileShareTestHelper.assertResponseStatusCode(primaryShareClient.createPermissionWithResponse(FILE_PERMISSION,
            null), 201);
    }

    @Test
    public void createAndGetPermission() {
        primaryShareClient.create();
        String permissionKey = primaryShareClient.createPermission(FILE_PERMISSION);
        String permission = primaryShareClient.getPermission(permissionKey);
        assertEquals(permission, FILE_PERMISSION);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void createAndGetPermissionOAuth() {
        primaryShareClient.create();
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareClient shareClient = oAuthServiceClient.getShareClient(shareName);
        String permissionKey = shareClient.createPermission(FILE_PERMISSION);
        String permission = shareClient.getPermission(permissionKey);
        assertEquals(permission, FILE_PERMISSION);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#filePermissionFormatSupplier")
    public void createAndGetPermissionFilePermissionFormat(FilePermissionFormat filePermissionFormat) {
        primaryShareClient.create();

        String permission = FileShareTestHelper.getPermissionFromFormat(filePermissionFormat);

        ShareFilePermission filePermission = new ShareFilePermission().setPermission(permission)
                .setPermissionFormat(filePermissionFormat);

        String permissionKey = primaryShareClient.createPermission(filePermission);
        String permissionResponse = primaryShareClient.getPermission(permissionKey, filePermissionFormat);

        assertEquals(permissionResponse, permission);
    }

    @Test
    public void createPermissionError() {
        primaryShareClient.create();
        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
            primaryShareClient.createPermissionWithResponse("abcde", null));
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.fromString(
            "FileInvalidPermission"));
    }

    @Test
    public void getPermissionError() {
        primaryShareClient.create();
        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
            primaryShareClient.getPermissionWithResponse("abcde", null));
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.INVALID_HEADER_VALUE);
    }

    @Test
    public void getSnapshot() {
        primaryShareClient.create();
        String snapshotId = primaryShareClient.createSnapshot().getSnapshot();
        ShareClient snapClient = primaryShareClient.getSnapshotClient(snapshotId);

        assertEquals(snapClient.getSnapshotId(), snapshotId);
        assertTrue(snapClient.getShareUrl().contains("sharesnapshot="));
        assertEquals(primaryShareClient.getSnapshotId(), null);
    }

    @Test
    public void getSnapshotId() {
        String snapshot = OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 1, 1), ZoneOffset.UTC).toString();

        ShareClient shareSnapshotClient = shareBuilderHelper(shareName).snapshot(snapshot).buildClient();

        assertEquals(snapshot, shareSnapshotClient.getSnapshotId());
    }

    @Test
    public void getShareName() {
        assertEquals(shareName, primaryShareClient.getShareName());
    }

    @Test
    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials
    // and auth would fail because we changed a signed header.
    public void perCallPolicy() {
        primaryShareClient.create();
        ShareClient shareClient = shareBuilderHelper(primaryShareClient.getShareName())
            .addPolicy(getPerCallVersionPolicy()).buildClient();

        Response<ShareProperties> response = shareClient.getPropertiesWithResponse(null, null);
        assertEquals(response.getHeaders().getValue(X_MS_VERSION), "2017-11-09");
    }

    @Test
    public void defaultAudience() {
        primaryShareClient.create();
        ShareClient aadShareClient = getOAuthShareClientBuilder(new ShareClientBuilder().shareName(shareName)
            .shareTokenIntent(ShareTokenIntent.BACKUP))
            .audience(null) // should default to "https://storage.azure.com/"
            .buildClient();

        String permission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-"
            + "1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-"
            + "188441444-3053964)S:NO_ACCESS_CONTROL";

        String infoPermission = aadShareClient.createPermission(permission);
        assertNotNull(infoPermission);
    }

    @Test
    public void storageAccountAudience() {
        primaryShareClient.create();
        ShareClient aadShareClient = getOAuthShareClientBuilder(new ShareClientBuilder())
            .shareName(shareName)
            .shareTokenIntent(ShareTokenIntent.BACKUP)
            .audience(ShareAudience.createShareServiceAccountAudience(primaryShareClient.getAccountName()))
            .buildClient();

        String permission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-"
            + "1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-"
            + "188441444-3053964)S:NO_ACCESS_CONTROL";

        String infoPermission = aadShareClient.createPermission(permission);
        assertNotNull(infoPermission);
    }

    @Test
    public void audienceError() {
        primaryShareClient.create();
        ShareClient aadShareClient = getOAuthShareClientBuilder(new ShareClientBuilder())
            .shareName(shareName)
            .shareTokenIntent(ShareTokenIntent.BACKUP)
            .audience(ShareAudience.createShareServiceAccountAudience("badaudience"))
            .buildClient();

        String permission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-"
            + "1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-"
            + "188441444-3053964)S:NO_ACCESS_CONTROL";

        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
            aadShareClient.createPermission(permission));
        assertEquals(ShareErrorCode.INVALID_AUTHENTICATION_INFO, e.getErrorCode());
    }
    @Test
    public void audienceFromString() {
        String url = String.format("https://%s.file.core.windows.net/", primaryShareClient.getAccountName());
        ShareAudience audience = ShareAudience.fromString(url);

        primaryShareClient.create();
        ShareClient aadShareClient = getOAuthShareClientBuilder(new ShareClientBuilder())
            .shareName(shareName)
            .shareTokenIntent(ShareTokenIntent.BACKUP)
            .audience(audience)
            .buildClient();

        String permission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-"
            + "1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-"
            + "188441444-3053964)S:NO_ACCESS_CONTROL";

        String infoPermission = aadShareClient.createPermission(permission);
        assertNotNull(infoPermission);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-05-04")
    @ParameterizedTest
    @MethodSource("createEnableSnapshotVirtualDirectoryAccessSupplier")
    public void createEnableSnapshotVirtualDirectoryAccess(Boolean enableSnapshotVirtualDirectoryAccess) {
        ShareCreateOptions options = new ShareCreateOptions();
        ShareProtocols protocols = ModelHelper.parseShareProtocols(Constants.HeaderConstants.NFS_PROTOCOL);
        options.setProtocols(protocols);
        options.setSnapshotVirtualDirectoryAccessEnabled(enableSnapshotVirtualDirectoryAccess);

        premiumFileServiceClient.getShareClient(shareName).createWithResponse(options, null, null);

        ShareProperties response = premiumFileServiceClient.getShareClient(shareName).getProperties();
        assertEquals(protocols.toString(), response.getProtocols().toString());
        if (enableSnapshotVirtualDirectoryAccess == null || enableSnapshotVirtualDirectoryAccess) {
            assertTrue(response.isSnapshotVirtualDirectoryAccessEnabled());
        } else {
            assertFalse(response.isSnapshotVirtualDirectoryAccessEnabled());
        }
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

        premiumFileServiceClient.getShareClient(shareName).createWithResponse(options, null, null);

        ShareSetPropertiesOptions setPropertiesOptions = new ShareSetPropertiesOptions();
        setPropertiesOptions.setSnapshotVirtualDirectoryAccessEnabled(enableSnapshotVirtualDirectoryAccess);

        premiumFileServiceClient.getShareClient(shareName).setProperties(setPropertiesOptions);

        ShareProperties response = premiumFileServiceClient.getShareClient(shareName).getProperties();
        assertEquals(protocols.toString(), response.getProtocols().toString());
        if (enableSnapshotVirtualDirectoryAccess == null || enableSnapshotVirtualDirectoryAccess) {
            assertTrue(response.isSnapshotVirtualDirectoryAccessEnabled());
        } else {
            assertFalse(response.isSnapshotVirtualDirectoryAccessEnabled());
        }
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @Test
    public void createSharePaidBursting() {
        ShareCreateOptions options = new ShareCreateOptions()
            .setPaidBurstingEnabled(true)
            .setPaidBurstingMaxIops(5000L)
            .setPaidBurstingMaxBandwidthMibps(1000L);

        premiumFileServiceClient.getShareClient(shareName).createWithResponse(options, null, null);

        ShareProperties response = premiumFileServiceClient.getShareClient(shareName).getProperties();

        assertTrue(response.isPaidBurstingEnabled());
        assertEquals(5000L, response.getPaidBurstingMaxIops());
        assertEquals(1000L, response.getPaidBurstingMaxBandwidthMibps());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @Test
    public void createSharePaidBurstingInvalidOptions() {
        ShareCreateOptions options = new ShareCreateOptions()
            .setPaidBurstingEnabled(false)
            .setPaidBurstingMaxIops(5000L)
            .setPaidBurstingMaxBandwidthMibps(1000L);

        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
            premiumFileServiceClient.getShareClient(shareName).createWithResponse(options, null, null));
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.fromString(
            "InvalidHeaderValue"));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @Test
    public void setPropertiesSharePaidBursting() {
        premiumFileServiceClient.getShareClient(shareName).createWithResponse(null, null, null);

        ShareSetPropertiesOptions options = new ShareSetPropertiesOptions()
            .setPaidBurstingEnabled(true)
            .setPaidBurstingMaxIops(5000L)
            .setPaidBurstingMaxBandwidthMibps(1000L);

        premiumFileServiceClient.getShareClient(shareName).setProperties(options);

        ShareProperties response = premiumFileServiceClient.getShareClient(shareName).getProperties();

        assertTrue(response.isPaidBurstingEnabled());
        assertEquals(5000L, response.getPaidBurstingMaxIops());
        assertEquals(1000L, response.getPaidBurstingMaxBandwidthMibps());
    }
}
