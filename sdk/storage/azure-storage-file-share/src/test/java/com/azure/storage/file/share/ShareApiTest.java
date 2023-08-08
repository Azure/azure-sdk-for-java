// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.Response;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.file.share.implementation.util.ModelHelper;
import com.azure.storage.file.share.models.NtfsFileAttributes;
import com.azure.storage.file.share.models.ShareAccessPolicy;
import com.azure.storage.file.share.models.ShareAccessTier;
import com.azure.storage.file.share.models.ShareDirectoryInfo;
import com.azure.storage.file.share.models.ShareErrorCode;
import com.azure.storage.file.share.models.ShareFileHttpHeaders;
import com.azure.storage.file.share.models.ShareFileInfo;
import com.azure.storage.file.share.models.ShareFileItem;
import com.azure.storage.file.share.models.ShareInfo;
import com.azure.storage.file.share.models.ShareProperties;
import com.azure.storage.file.share.models.ShareProtocols;
import com.azure.storage.file.share.models.ShareRequestConditions;
import com.azure.storage.file.share.models.ShareRootSquash;
import com.azure.storage.file.share.models.ShareSignedIdentifier;
import com.azure.storage.file.share.models.ShareSnapshotInfo;
import com.azure.storage.file.share.models.ShareSnapshotsDeleteOptionType;
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
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.condition.EnabledIf;
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
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ShareApiTest extends FileShareTestBase {
    private ShareClient primaryShareClient;
    private String shareName;
    private static Map<String, String> testMetadata;
    private FileSmbProperties smbProperties;
    private static final String filePermission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-188441444-3053964)S:NO_ACCESS_CONTROL";

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
        assertExceptionStatusCodeAndMessage(e, 403, ShareErrorCode.AUTHENTICATION_FAILED);
    }

    @Test
    public void createShare() {
        assertResponseStatusCode(primaryShareClient.createWithResponse(null, null, null, null), 201);
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("createShareWithArgsSupplier")
    public void createShareWithArgs(Map<String, String> metadata, Integer quota, ShareAccessTier accessTier) {
        assertResponseStatusCode(primaryShareClient.createWithResponse(new ShareCreateOptions()
            .setMetadata(metadata).setQuotaInGb(quota).setAccessTier(accessTier), null, null), 201);
    }

    private static Stream<Arguments> createShareWithArgsSupplier() {
        return Stream.of(Arguments.of(null, null, null), Arguments.of(null, 1, null),
            Arguments.of(testMetadata, null, null), Arguments.of(null, null, ShareAccessTier.HOT),
            Arguments.of(testMetadata, 1, ShareAccessTier.HOT));
    }

    @ParameterizedTest
    @MethodSource("createShareWithInvalidArgsSupplier")
    public void createShareWithInvalidArgs(Map<String, String> metadata, Integer quota, ShareErrorCode errMessage) {
        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
            primaryShareClient.createWithResponse(metadata, quota, null, null));
        assertExceptionStatusCodeAndMessage(e, 400, errMessage);
    }

    private static Stream<Arguments> createShareWithInvalidArgsSupplier() {
        return Stream.of(Arguments.of(Collections.singletonMap("", "value"), 1, ShareErrorCode.EMPTY_METADATA_KEY),
            Arguments.of(Collections.singletonMap("metadata!", "value"), 1, ShareErrorCode.INVALID_METADATA),
            Arguments.of(testMetadata, 6000, ShareErrorCode.INVALID_HEADER_VALUE));
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20221102ServiceVersion")
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

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20210410ServiceVersion")
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
        assertResponseStatusCode(primaryShareClient.createIfNotExistsWithResponse(null, null, null), 201);
    }

    @Test
    public void createIfNotExistsShareThatAlreadyExists() {
        ShareClient client = premiumFileServiceClient.getShareClient(generateShareName());
        Response<ShareInfo> initialResponse = client.createIfNotExistsWithResponse(new ShareCreateOptions(), null, null);
        Response<ShareInfo> secondResponse = client.createIfNotExistsWithResponse(new ShareCreateOptions(), null, null);
        assertEquals(initialResponse.getStatusCode(), 201);
        assertEquals(secondResponse.getStatusCode(), 409);
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("createIfNotExistsShareWithArgsSupplier")
    public void createIfNotExistsShareWithArgs(Map<String, String> metadata, Integer quota, ShareAccessTier accessTier) {
        assertResponseStatusCode(primaryShareClient.createIfNotExistsWithResponse(new ShareCreateOptions()
            .setMetadata(metadata).setQuotaInGb(quota).setAccessTier(accessTier), null, null), 201);
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

        assertExceptionStatusCodeAndMessage(e, 400, errMessage);
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
        assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.SHARE_NOT_FOUND);
    }

    @Test
    public void createSnapshotMetadata() {
        primaryShareClient.create();
        String shareSnapshotName = generatePathName();

        Response<ShareSnapshotInfo> createSnapshotResponse = primaryShareClient.createSnapshotWithResponse(testMetadata, null, null);
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
        assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.EMPTY_METADATA_KEY);
    }

    @Test
    public void deleteShare() {
        primaryShareClient.create();
        assertResponseStatusCode(primaryShareClient.deleteWithResponse(null, null), 202);
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

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20200210ServiceVersion")
    @Test
    public void deleteShareLease() {
        primaryShareClient.create();
        String leaseID = setupShareLeaseCondition(primaryShareClient, RECEIVED_LEASE_ID);

        assertResponseStatusCode(primaryShareClient.deleteWithResponse(new ShareDeleteOptions()
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
        assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.SHARE_NOT_FOUND);
    }

    @Test
    public void deleteIfExistsShare() {
        primaryShareClient.create();
        assertResponseStatusCode(primaryShareClient.deleteIfExistsWithResponse(null, null, null), 202);
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

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20200210ServiceVersion")
    @Test
    public void deleteIfExistsShareLease() {
        primaryShareClient.create();
        String leaseID = setupShareLeaseCondition(primaryShareClient, RECEIVED_LEASE_ID);

        assertResponseStatusCode(primaryShareClient.deleteIfExistsWithResponse(new ShareDeleteOptions()
            .setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID)), null, null), 202);
    }

    @Test
    public void deleteIfExistsShareLeaseError() {
        primaryShareClient.create();
        String leaseID = setupShareLeaseCondition(primaryShareClient, GARBAGE_LEASE_ID);
        assertThrows(ShareStorageException.class, () -> primaryShareClient.deleteIfExistsWithResponse(
            new ShareDeleteOptions().setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID)), null, null));
    }

    @Test
    public void deleteIfExistsShareThatDoesNotExists() {
        ShareClient client = premiumFileServiceClient.getShareClient(generateShareName());
        Response<Boolean> response = client.deleteIfExistsWithResponse(null, null, null);
        assertFalse(response.getValue());
        assertResponseStatusCode(response, 404);
        assertFalse(client.exists());
    }

    @Test
    public void deleteIfExistsDirThatWasAlreadyDeleted() {
        ShareClient client = premiumFileServiceClient.getShareClient(generateShareName());
        client.create();
        Response<Boolean> initialResponse = client.deleteIfExistsWithResponse(null, null, null);
        // Calling delete again after garbage collection is completed
        sleepIfRecord(45000);
        Response<Boolean> secondResponse = client.deleteIfExistsWithResponse(null, null, null);

        assertResponseStatusCode(initialResponse, 202);
        assertResponseStatusCode(secondResponse, 404);
        assertTrue(initialResponse.getValue());
        assertFalse(secondResponse.getValue());
    }


    @Test
    public void getProperties() {
        primaryShareClient.createWithResponse(testMetadata, 1, null, null);
        Response<ShareProperties> getPropertiesResponse = primaryShareClient.getPropertiesWithResponse(null, null);

        assertResponseStatusCode(getPropertiesResponse, 200);
        assertEquals(testMetadata, getPropertiesResponse.getValue().getMetadata());
        assertEquals(getPropertiesResponse.getValue().getQuota(), 1);
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20200210ServiceVersion")
    @Test
    public void getPropertiesLease() {
        primaryShareClient.create();
        String leaseID = setupShareLeaseCondition(primaryShareClient, RECEIVED_LEASE_ID);
        assertResponseStatusCode(primaryShareClient.getPropertiesWithResponse(new ShareGetPropertiesOptions()
            .setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID)), null, null), 200);
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20200210ServiceVersion")
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
        assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.SHARE_NOT_FOUND);
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20210410ServiceVersion")
    @Test
    public void getPropertiesOAuthError() {
        primaryShareClient.create();
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareClient shareClient = oAuthServiceClient.getShareClient(shareName);
        // only APIs supported by ShareWithtoken authentication are createPermission and getPermission
        assertThrows(ShareStorageException.class, () -> shareClient.getProperties());
    }

    @EnabledIf("com.azure.storage.file.share.FileShareTestBase#isPlaybackMode")
    @ParameterizedTest
    @MethodSource("getPropertiesPremiumSupplier")
    public void getPropertiesPremium(String protocol, ShareRootSquash rootSquash) {
        ShareProtocols enabledProtocol = ModelHelper.parseShareProtocols(protocol);

        ShareClient premiumShareClient = premiumFileServiceClient.createShareWithResponse(generateShareName(),
                new ShareCreateOptions().setMetadata(testMetadata).setProtocols(enabledProtocol)
                    .setRootSquash(rootSquash), null, null).getValue();

        Response<ShareProperties> getPropertiesResponse = premiumShareClient.getPropertiesWithResponse(null, null);
        ShareProperties shareProperties = getPropertiesResponse.getValue();

        assertResponseStatusCode(getPropertiesResponse, 200);
        assertEquals(testMetadata, shareProperties.getMetadata());
        assertNotNull(shareProperties.getQuota());
        assertNotNull(shareProperties.getNextAllowedQuotaDowngradeTime());
        assertNotNull(shareProperties.getProvisionedEgressMBps());
        assertNotNull(shareProperties.getProvisionedIngressMBps());
        assertNotNull(shareProperties.getProvisionedIops());
        assertNotNull(shareProperties.getProvisionedBandwidthMiBps());
        assertEquals(shareProperties.getProtocols().toString(), enabledProtocol.toString());
        assertEquals(shareProperties.getRootSquash(), rootSquash);
    }

    private static Stream<Arguments> getPropertiesPremiumSupplier() {
        return Stream.of(
            Arguments.of(Constants.HeaderConstants.SMB_PROTOCOL, null),
            Arguments.of(Constants.HeaderConstants.NFS_PROTOCOL, ShareRootSquash.ALL_SQUASH),
            Arguments.of(Constants.HeaderConstants.NFS_PROTOCOL, ShareRootSquash.NO_ROOT_SQUASH),
            Arguments.of(Constants.HeaderConstants.NFS_PROTOCOL, ShareRootSquash.ROOT_SQUASH)
        );
    }

    @ParameterizedTest
    @EnabledIf("com.azure.storage.file.share.FileShareTestBase#isPlaybackMode")
    public void setPremiumProperties() {
        List<ShareRootSquash> rootSquashes = List.of(
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

        List<ShareSignedIdentifier> ids = List.of(identifier);
        primaryShareClient.setAccessPolicy(ids);
        assertEquals(primaryShareClient.getAccessPolicy().iterator().next().getId(), "0000");
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
        List<ShareSignedIdentifier> ids = List.of(identifier, identifier2);

        Response<ShareInfo> response = primaryShareClient.setAccessPolicyWithResponse(ids, null, null);
        Iterator<ShareSignedIdentifier> receivedIdentifiers = primaryShareClient.getAccessPolicy().iterator();

        assertResponseStatusCode(response, 200);
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

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20200210ServiceVersion")
    @Test
    public void setAccessPolicyLease() {
        primaryShareClient.create();
        String leaseID = setupShareLeaseCondition(primaryShareClient, RECEIVED_LEASE_ID);

        assertResponseStatusCode(primaryShareClient.setAccessPolicyWithResponse(
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

        List<ShareSignedIdentifier> ids = List.of(identifier);
        primaryShareClient.setAccessPolicy(ids);

        ShareSignedIdentifier id = primaryShareClient.getAccessPolicy().iterator().next();

        assertEquals(id.getId(), identifier.getId());
        assertEquals(id.getAccessPolicy().getStartsOn(), identifier.getAccessPolicy().getStartsOn());
        assertEquals(id.getAccessPolicy().getExpiresOn(), identifier.getAccessPolicy().getExpiresOn());
        assertEquals(id.getAccessPolicy().getPermissions(), identifier.getAccessPolicy().getPermissions());
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20200210ServiceVersion")
    @Test
    public void getAccessPolicyLease() {
        setup:
        primaryShareClient.create()
        def leaseID = setupShareLeaseCondition(primaryShareClient, RECEIVED_LEASE_ID)

        when:
        def response = primaryShareClient.getAccessPolicy(new ShareGetAccessPolicyOptions().setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID)))

        then:
        !response.iterator().hasNext()
    }

    public void getAccess() policy lease error() {
        setup:
        primaryShareClient.create()
        def leaseID = setupShareLeaseCondition(primaryShareClient, GARBAGE_LEASE_ID)

        when:
        primaryShareClient.getAccessPolicy(new ShareGetAccessPolicyOptions().setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID))).iterator().hasNext()

        then:
        assertThrows(ShareStorageException.class, () ->
    }

    public void getAccess() policy error() {
        when:
        primaryShareClient.getAccessPolicy().iterator().hasNext()

        then:
        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
        assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.SHARE_NOT_FOUND)
    }

    public void setProperties() quota() {
        given:
        primaryShareClient.createWithResponse(null, 1, null, null)

        when:
        def getQuotaBeforeResponse = primaryShareClient.getProperties()
        def setQuotaResponse = primaryShareClient.setQuotaWithResponse(2, null, null)
        def getQuotaAfterResponse = primaryShareClient.getProperties()

        then:
        getQuotaBeforeResponse.getQuota(), 1
        assertResponseStatusCode(setQuotaResponse, 200)
        getQuotaAfterResponse.getQuota(), 2
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20191212ServiceVersion")
    public void setProperties() access tier() {
        given:
        primaryShareClient.createWithResponse(new ShareCreateOptions().setAccessTier(ShareAccessTier.HOT), null, null)
        def time = testResourceNamer.now().truncatedTo(ChronoUnit.SECONDS)

        when:
        def getAccessTierBeforeResponse = primaryShareClient.getProperties()
        def setAccessTierResponse = primaryShareClient.setPropertiesWithResponse(new ShareSetPropertiesOptions().setAccessTier(ShareAccessTier.TRANSACTION_OPTIMIZED), null, null)
        def getAccessTierAfterResponse = primaryShareClient.getProperties()

        then:
        getAccessTierBeforeResponse.getAccessTier(), ShareAccessTier.HOT.toString()
        assertResponseStatusCode(setAccessTierResponse, 200)
        getAccessTierAfterResponse.getAccessTier(), ShareAccessTier.TRANSACTION_OPTIMIZED.toString()
        getAccessTierAfterResponse.getAccessTierChangeTime().isEqual(time) || getAccessTierAfterResponse.getAccessTierChangeTime().isAfter(time.minusSeconds(1))
        getAccessTierAfterResponse.getAccessTierChangeTime().isBefore(time.plusMinutes(1))
        getAccessTierAfterResponse.getAccessTierTransitionState(), "pending-from-hot"
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20200210ServiceVersion")
    public void setProperties() lease() {
        given:
        primaryShareClient.createWithResponse(new ShareCreateOptions().setAccessTier(ShareAccessTier.HOT), null, null)
        def leaseID = setupShareLeaseCondition(primaryShareClient, RECEIVED_LEASE_ID)

        when:
        def setAccessTierResponse = primaryShareClient.setPropertiesWithResponse(
            new ShareSetPropertiesOptions().setAccessTier(ShareAccessTier.COOL).setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID)), null, null)

        then:
        assertResponseStatusCode(setAccessTierResponse, 200)
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20200210ServiceVersion")
    public void setProperties() lease error() {
        given:
        primaryShareClient.createWithResponse(new ShareCreateOptions().setAccessTier(ShareAccessTier.HOT), null, null)
        def leaseID = setupShareLeaseCondition(primaryShareClient, GARBAGE_LEASE_ID)

        when:
        def setAccessTierResponse = primaryShareClient.setPropertiesWithResponse(
            new ShareSetPropertiesOptions().setAccessTier(ShareAccessTier.COOL).setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID)), null, null)

        then:
        assertThrows(ShareStorageException.class, () ->
    }

    public void setPropertiesError() {
        when:
        primaryShareClient.setProperties(new ShareSetPropertiesOptions())
        then:
        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
        assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.SHARE_NOT_FOUND)
    }

    public void setMetadata() {
        given:
        primaryShareClient.createWithResponse(testMetadata, null, null, null)
        def metadataAfterSet = Collections.singletonMap("afterset", "value")

        when:
        def getMetadataBeforeResponse = primaryShareClient.getProperties()
        def setMetadataResponse = primaryShareClient.setMetadataWithResponse(metadataAfterSet, null, null)
        def getMetadataAfterResponse = primaryShareClient.getProperties()

        then:
        testMetadata, getMetadataBeforeResponse.getMetadata()
        assertResponseStatusCode(setMetadataResponse, 200)
        metadataAfterSet, getMetadataAfterResponse.getMetadata()
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20200210ServiceVersion")
    public void setMetadata() lease() {
        given:
        primaryShareClient.createWithResponse(null, 1, null, null)
        def leaseID = setupShareLeaseCondition(primaryShareClient, RECEIVED_LEASE_ID)

        when:
        def resp = primaryShareClient.setMetadataWithResponse(new ShareSetMetadataOptions().setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID)), null, null)

        then:
        assertResponseStatusCode(resp, 200)
    }

    public void setMetadata() lease error() {
        given:
        primaryShareClient.createWithResponse(null, 1, null, null)
        def leaseID = setupShareLeaseCondition(primaryShareClient, GARBAGE_LEASE_ID)

        when:
        primaryShareClient.setMetadataWithResponse(
            new ShareSetMetadataOptions().setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID)), null, null)

        then:
        assertThrows(ShareStorageException.class, () ->
    }

    public void setMetadataError() {
        when:
        primaryShareClient.setMetadata(testMetadata)

        then:
        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
        assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.SHARE_NOT_FOUND)
    }

    @ParameterizedTest
    public void getStatistics() {
        setup:
        primaryShareClient.create()
        primaryShareClient.createFile("tempFile", (long) size)

        when:
        def resp = primaryShareClient.getStatisticsWithResponse(null, null)

        then:
        assertResponseStatusCode(resp, 200)
        resp.getValue().getShareUsageInBytes(), size
        resp.getValue().getShareUsageInGB(), gigabytes

        where:
        size                    || gigabytes
        0                       || 0
        Constants.KB            || 1
        Constants.GB            || 1
        (long) 3 * Constants.GB || 3
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20200210ServiceVersion")
    public void getStatistics() lease() {
        setup:
        primaryShareClient.create()
        def leaseID = setupShareLeaseCondition(primaryShareClient, RECEIVED_LEASE_ID)

        when:
        def resp = primaryShareClient.getStatisticsWithResponse(new ShareGetStatisticsOptions().setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID)), null, null)

        then:
        assertResponseStatusCode(resp, 200)
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20200210ServiceVersion")
    public void getStatistics() lease error() {
        setup:
        primaryShareClient.create()
        def leaseID = setupShareLeaseCondition(primaryShareClient, GARBAGE_LEASE_ID)

        when:
        def resp = primaryShareClient.getStatisticsWithResponse(new ShareGetStatisticsOptions().setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID)), null, null)

        then:
        assertThrows(ShareStorageException.class, () ->
    }

    public void getStatisticsError() {
        when:
        primaryShareClient.getStatistics()

        then:
        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
        assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.SHARE_NOT_FOUND)
    }

    public void createDirectory() {
        given:
        primaryShareClient.create()

        expect:
        assertResponseStatusCode(
            primaryShareClient.createDirectoryWithResponse("testCreateDirectory", null, null, null, null, null), 201)
    }

    public void createDirectory() file permission() {
        given:
        primaryShareClient.create()
        expect:
        assertResponseStatusCode(
            primaryShareClient.createDirectoryWithResponse("testCreateDirectory", null, filePermission, null, null, null), 201)
    }

    public void createDirectory() file permission key() {
        given:
        primaryShareClient.create()
        def filePermissionKey = primaryShareClient.createPermission(filePermission)
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setFilePermissionKey(filePermissionKey)
        expect:
        assertResponseStatusCode(
            primaryShareClient.createDirectoryWithResponse("testCreateDirectory", smbProperties, null, null, null, null), 201)
    }

    public void createDirectory() invalid name() {
        given:
        primaryShareClient.create()

        when:
        primaryShareClient.createDirectory("test/directory")

        then:
        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
        assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.PARENT_NOT_FOUND)
    }

    public void createDirectory() metadata() {
        given:
        primaryShareClient.create()

        expect:
        assertResponseStatusCode(
            primaryShareClient.createDirectoryWithResponse("testCreateDirectory", null, null, testMetadata, null, null), 201)
    }

    public void createDirectory() metadata error() {
        given:
        primaryShareClient.create()

        when:
        primaryShareClient.createDirectoryWithResponse("testdirectory", null, null, Collections.singletonMap("", "value"), null, null)

        then:
        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
        assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.EMPTY_METADATA_KEY)
    }

    public void createIf() NotExists directory() {
        given:
        primaryShareClient.create()

        expect:
        assertResponseStatusCode(
            primaryShareClient.createDirectoryIfNotExistsWithResponse("testCreateDirectory", new ShareDirectoryCreateOptions(), null, null), 201)
    }

    public void createIf() NotExists directory file permission() {
        given:
        primaryShareClient.create()
        expect:
        assertResponseStatusCode(
            primaryShareClient.createDirectoryIfNotExistsWithResponse("testCreateDirectory", new ShareDirectoryCreateOptions().setFilePermission(filePermission), null, null), 201)
    }

    public void createIf() not exist directory file permission key() {
        given:
        primaryShareClient.create()
        def filePermissionKey = primaryShareClient.createPermission(filePermission)
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setFilePermissionKey(filePermissionKey)
        expect:
        assertResponseStatusCode(
            primaryShareClient.createDirectoryIfNotExistsWithResponse("testCreateDirectory", new ShareDirectoryCreateOptions().setSmbProperties(smbProperties), null, null), 201)
    }

    public void createIf() NotExists directory invalid name() {
        given:
        primaryShareClient.create()

        when:
        primaryShareClient.createDirectoryIfNotExists("test/directory")

        then:
        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
        assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.PARENT_NOT_FOUND)
    }

    public void createIf() NotExists directory metadata() {
        given:
        primaryShareClient.create()

        expect:
        assertResponseStatusCode(
            primaryShareClient.createDirectoryIfNotExistsWithResponse("testCreateDirectory", new ShareDirectoryCreateOptions().setMetadata( testMetadata), null, null), 201)
    }

    public void createIf() NotExists directory metadata error() {
        given:
        primaryShareClient.create()

        when:
        primaryShareClient.createDirectoryIfNotExistsWithResponse("testdirectory", new ShareDirectoryCreateOptions().setMetadata(Collections.singletonMap("", "value")), null, null)

        then:
        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
        assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.EMPTY_METADATA_KEY)
    }

    public void createIf() NotExists directory that already exists() {
        setup:
        def client = premiumFileServiceClient.getShareClient(generateShareName())
        client.create()
        def initialResponse = client.createDirectoryIfNotExistsWithResponse("testCreateDirectory", new ShareDirectoryCreateOptions(), null, null)

        when:
        def secondResponse = client.createDirectoryIfNotExistsWithResponse("testCreateDirectory", new ShareDirectoryCreateOptions(), null, null)

        then:
        initialResponse.getStatusCode(), 201
        secondResponse.getStatusCode(), 409
    }

    public void createFile() {
        given:
        primaryShareClient.create()

        expect:
        assertResponseStatusCode(
            primaryShareClient.createFileWithResponse("testCreateFile", 1024, null, null, null, null, null, null), 201)
    }

    public void createFile() file permission() {
        given:
        primaryShareClient.create()
        expect:
        assertResponseStatusCode(
            primaryShareClient.createFileWithResponse("testCreateFile", 1024, null, null, filePermission, null, null, null), 201)
    }

    public void createFile() file permission key() {
        given:
        primaryShareClient.create()
        def filePermissionKey = primaryShareClient.createPermission(filePermission)
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setFilePermissionKey(filePermissionKey)

        expect:
        assertResponseStatusCode(
            primaryShareClient.createFileWithResponse("testCreateFile", 1024, null, smbProperties, null, null, null, null), 201)
    }

    @ParameterizedTest
    public void createFile() invalid args() {
        given:
        primaryShareClient.create()

        when:
        primaryShareClient.createFileWithResponse(fileName, maxSize, null, null, null, null, null, null)
        then:
        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
        assertExceptionStatusCodeAndMessage(e, statusCode, errMsg)

        where:
        fileName    | maxSize | statusCode | errMsg
        "testfile:" | 1024    | 400        | ShareErrorCode.INVALID_RESOURCE_NAME
        "fileName"  | -1      | 400        | ShareErrorCode.OUT_OF_RANGE_INPUT

    }

    public void createFile() maxOverload() {
        given:
        primaryShareClient.create()
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
        expect:
        assertResponseStatusCode(
            primaryShareClient.createFileWithResponse("testCreateFile", 1024, null, smbProperties, filePermission, testMetadata, null, null), 201)
    }

    @ParameterizedTest
    public void createFile() maxOverload invalid args() {
        given:
        primaryShareClient.create()

        when:
        primaryShareClient.createFileWithResponse(fileName, maxSize, httpHeaders, null, null, metadata, null, null)

        then:
        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
        assertExceptionStatusCodeAndMessage(e, 400, errMsg)

        where:
        fileName    | maxSize | httpHeaders                                           | metadata                              | errMsg
        "testfile:" | 1024    | null                                                  | testMetadata                          | ShareErrorCode.INVALID_RESOURCE_NAME
        "fileName"  | -1      | null                                                  | testMetadata                          | ShareErrorCode.OUT_OF_RANGE_INPUT
        "fileName"  | 1024    | new ShareFileHttpHeaders().setContentMd5(new byte[0]) | testMetadata                          | ShareErrorCode.INVALID_HEADER_VALUE
        "fileName"  | 1024    | null                                                  | Collections.singletonMap("", "value") | ShareErrorCode.EMPTY_METADATA_KEY
    }

    public void createFile() in root directory() {
        given:
        primaryShareClient.create()
        ShareDirectoryClient directoryClient = primaryShareClient.getRootDirectoryClient();

        expect:
        assertResponseStatusCode(
            directoryClient.createFileWithResponse("testCreateFile", 1024, null, null, null, null, null, null), 201)
    }

    public void deleteDirectory() {
        given:
        def directoryName = "testCreateDirectory"
        primaryShareClient.create()
        primaryShareClient.createDirectory(directoryName)

        expect:
        assertResponseStatusCode(primaryShareClient.deleteDirectoryWithResponse(directoryName, null, null), 202)
    }

    public void deleteDirectoryError() {
        given:
        primaryShareClient.create()

        when:
        primaryShareClient.deleteDirectory("testdirectory")

        then:
        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
        assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND)
    }

    public void deleteIf() exists directory() {
        given:
        def directoryName = "testCreateDirectory"
        primaryShareClient.create()
        primaryShareClient.createDirectory(directoryName)

        expect:
        assert assertResponseStatusCode(primaryShareClient.deleteDirectoryIfExistsWithResponse(directoryName, null, null), 202)
    }

    public void deleteIf() exists directory min() {
        given:
        def directoryName = "testCreateDirectory"
        primaryShareClient.create()
        primaryShareClient.createDirectory(directoryName)

        expect:
        assert primaryShareClient.deleteDirectoryIfExists(directoryName)
    }

    public void deleteIf() exists directory that DoesNot exist() {
        given:
        def directoryName = "testCreateDirectory"
        primaryShareClient.create()

        when:
        def response = primaryShareClient.deleteDirectoryIfExistsWithResponse(directoryName, null, null)

        then:
        !response.getValue()
        response.getStatusCode(), 404
    }

    public void deleteFile() {
        given:
        def fileName = "testCreateFile"
        primaryShareClient.create()
        primaryShareClient.createFile(fileName, 1024)

        expect:
        assertResponseStatusCode(
            primaryShareClient.deleteFileWithResponse(fileName, null, null), 202)
    }

    public void deleteFileError() {
        given:
        primaryShareClient.create()

        when:
        primaryShareClient.deleteFile("testdirectory")

        then:
        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
        assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND)
    }

    public void deleteIf() exists file() {
        given:
        def fileName = "testCreateFile"
        primaryShareClient.create()
        primaryShareClient.createFile(fileName, 1024)

        expect:
        assertResponseStatusCode(
            primaryShareClient.deleteFileIfExistsWithResponse(fileName, null, null, null), 202)
    }

    public void deleteIf() exists file min() {
        given:
        def fileName = "testCreateFile"
        primaryShareClient.create()
        primaryShareClient.createFile(fileName, 1024)

        expect:
        primaryShareClient.deleteFileIfExists(fileName)
    }

    @Test
    public void deleteIf() exists file that DoesNot exist() {
        given:
        primaryShareClient.create()

        when:
        def response = primaryShareClient.deleteFileIfExistsWithResponse("testCreateFile", null, null, null)

        then:
        !response.getValue()
        assertResponseStatusCode(response, 404);
    }

    @Test
    public void createPermission() {
        primaryShareClient.create();
        assertResponseStatusCode(primaryShareClient.createPermissionWithResponse(filePermission, null), 201);
    }

    @Test
    public void createAndGetPermission() {
        primaryShareClient.create();
        String permissionKey = primaryShareClient.createPermission(filePermission);
        String permission = primaryShareClient.getPermission(permissionKey);
        assertEquals(permission, filePermission);
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20210410ServiceVersion")
    @Test
    public void createAndGetPermissionOAuth() {
        primaryShareClient.create();
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareClient shareClient = oAuthServiceClient.getShareClient(shareName);
        String permissionKey = shareClient.createPermission(filePermission);
        String permission = shareClient.getPermission(permissionKey);
        assertEquals(permission, filePermission);
    }

    @Test
    public void createPermissionError() {
        primaryShareClient.create();
        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
            primaryShareClient.createPermissionWithResponse("abcde", null));
        assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.fromString("FileInvalidPermission"));
    }

    @Test
    public void getPermissionError() {
        primaryShareClient.create();
        ShareStorageException e = assertThrows(ShareStorageException.class, () -> primaryShareClient.getPermissionWithResponse("abcde", null));
        assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.INVALID_HEADER_VALUE);
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
    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials and auth would fail because we changed a signed header.
    public void perCallPolicy() {
        primaryShareClient.create();
        ShareClient shareClient = shareBuilderHelper(primaryShareClient.getShareName())
            .addPolicy(getPerCallVersionPolicy()).buildClient();

        Response<ShareProperties> response = shareClient.getPropertiesWithResponse(null, null);
        assertEquals(response.getHeaders().getValue(X_MS_VERSION), "2017-11-09");
    }


}
