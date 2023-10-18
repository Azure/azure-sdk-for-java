// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.HttpClientOptions;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.file.share.models.CloseHandlesInfo;
import com.azure.storage.file.share.models.HandleItem;
import com.azure.storage.file.share.models.NtfsFileAttributes;
import com.azure.storage.file.share.models.ShareAudience;
import com.azure.storage.file.share.models.ShareDirectoryInfo;
import com.azure.storage.file.share.models.ShareDirectoryProperties;
import com.azure.storage.file.share.models.ShareDirectorySetMetadataInfo;
import com.azure.storage.file.share.models.ShareErrorCode;
import com.azure.storage.file.share.models.ShareFileHttpHeaders;
import com.azure.storage.file.share.models.ShareFileItem;
import com.azure.storage.file.share.models.ShareRequestConditions;
import com.azure.storage.file.share.models.ShareSnapshotInfo;
import com.azure.storage.file.share.models.ShareStorageException;
import com.azure.storage.file.share.models.ShareTokenIntent;
import com.azure.storage.file.share.options.ShareDirectoryCreateOptions;
import com.azure.storage.file.share.options.ShareFileRenameOptions;
import com.azure.storage.file.share.options.ShareListFilesAndDirectoriesOptions;
import com.azure.storage.file.share.sas.ShareFileSasPermission;
import com.azure.storage.file.share.sas.ShareServiceSasSignatureValues;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DirectoryApiTests extends FileShareTestBase {
    private ShareDirectoryClient primaryDirectoryClient;
    private ShareClient shareClient;
    private String directoryPath;
    private String shareName;
    private static Map<String, String> testMetadata;
    private FileSmbProperties smbProperties;
    private static final String FILE_PERMISSION = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-188441444-3053964)S:NO_ACCESS_CONTROL";


    @BeforeEach
    public void setup() {
        shareName = generateShareName();
        directoryPath = generatePathName();
        shareClient = shareBuilderHelper(shareName).buildClient();
        shareClient.create();
        primaryDirectoryClient = directoryBuilderHelper(shareName, directoryPath).buildDirectoryClient();
        testMetadata = Collections.singletonMap("testmetadata", "value");
        smbProperties = new FileSmbProperties().setNtfsFileAttributes(EnumSet.<NtfsFileAttributes>of(NtfsFileAttributes.NORMAL));
    }

    @Test
    public void getDirectoryUrl() {
        String accountName = StorageSharedKeyCredential
            .fromConnectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString()).getAccountName();
        String expectURL = String.format("https://%s.file.core.windows.net/%s/%s", accountName, shareName,
            directoryPath);
        String directoryURL = primaryDirectoryClient.getDirectoryUrl();
        assertEquals(expectURL, directoryURL);
    }

    @Test
    public void getShareSnapshotUrl() {
        String accountName = StorageSharedKeyCredential
            .fromConnectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString()).getAccountName();
        String expectURL = String.format("https://%s.file.core.windows.net/%s/%s", accountName, shareName,
            directoryPath);

        ShareSnapshotInfo shareSnapshotInfo = shareClient.createSnapshot();
        expectURL = expectURL + "?sharesnapshot=" + shareSnapshotInfo.getSnapshot();
        ShareDirectoryClient newDirClient = shareBuilderHelper(shareName).snapshot(shareSnapshotInfo.getSnapshot())
            .buildClient().getDirectoryClient(directoryPath);
        String directoryURL = newDirClient.getDirectoryUrl();
        assertEquals(expectURL, directoryURL);

        String snapshotEndpoint = String.format("https://%s.file.core.windows.net/%s/%s?sharesnapshot=%s", accountName,
            shareName, directoryPath, shareSnapshotInfo.getSnapshot());
        ShareDirectoryClient client = getDirectoryClient(StorageSharedKeyCredential
            .fromConnectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString()), snapshotEndpoint);
        assertEquals(client.getDirectoryUrl(), snapshotEndpoint);
    }

    @Test
    public void getSubDirectoryClient() {
        ShareDirectoryClient subDirectoryClient = primaryDirectoryClient.getSubdirectoryClient("testSubDirectory");
        assertInstanceOf(ShareDirectoryClient.class, subDirectoryClient);
    }

    @Test
    public void getFileClient() {
        ShareFileClient fileClient = primaryDirectoryClient.getFileClient("testFile");
        assertInstanceOf(ShareFileClient.class, fileClient);
    }

    @Test
    public void exists() {
        primaryDirectoryClient.create();
        assertTrue(primaryDirectoryClient.exists());
    }

    @Test
    public void doesNotExist() {
        assertFalse(primaryDirectoryClient.exists());
    }

    @Test
    public void existsError() {
        primaryDirectoryClient = directoryBuilderHelper(shareName, directoryPath)
            .sasToken("sig=dummyToken").buildDirectoryClient();

        assertThrows(ShareStorageException.class, () -> primaryDirectoryClient.exists());

        ShareStorageException e = assertThrows(ShareStorageException.class, () -> primaryDirectoryClient.exists());
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 403, ShareErrorCode.AUTHENTICATION_FAILED);
    }

    @Test
    public void createDirectory() {
        assertEquals(201, primaryDirectoryClient.createWithResponse(null, null, null, null, null).getStatusCode());
    }

    @Test
    public void createDirectoryError() {
        String testShareName = generateShareName();

        ShareStorageException e = assertThrows(ShareStorageException.class,
            () -> directoryBuilderHelper(testShareName, directoryPath).buildDirectoryClient().create());
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.SHARE_NOT_FOUND);
    }

    @Test
    public void createDirectoryWithMetadata() {
        assertEquals(201, primaryDirectoryClient.createWithResponse(null, null, testMetadata, null, null)
            .getStatusCode());
    }

    @Test
    public void createDirectoryWithFilePermission() {
        Response<ShareDirectoryInfo> resp =
            primaryDirectoryClient.createWithResponse(null, FILE_PERMISSION, null, null, null);

        assertEquals(201, resp.getStatusCode());
        assertNotNull(resp.getValue().getSmbProperties().getFilePermissionKey());
        assertNotNull(resp.getValue().getSmbProperties().getNtfsFileAttributes());
        assertNotNull(resp.getValue().getSmbProperties().getFileLastWriteTime());
        assertNotNull(resp.getValue().getSmbProperties().getFileCreationTime());
        assertNotNull(resp.getValue().getSmbProperties().getFileChangeTime());
        assertNotNull(resp.getValue().getSmbProperties().getParentId());
        assertNotNull(resp.getValue().getSmbProperties().getFileId());
    }

    @Test
    public void createDirectoryWithFilePermissionKey() {
        String filePermissionKey = shareClient.createPermission(FILE_PERMISSION);
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setFilePermissionKey(filePermissionKey);

        Response<ShareDirectoryInfo> resp =
            primaryDirectoryClient.createWithResponse(smbProperties, null, null, null, null);

        assertEquals(201, resp.getStatusCode());
        assertNotNull(resp.getValue().getSmbProperties().getFilePermissionKey());
        assertNotNull(resp.getValue().getSmbProperties().getNtfsFileAttributes());
        assertNotNull(resp.getValue().getSmbProperties().getFileLastWriteTime());
        assertNotNull(resp.getValue().getSmbProperties().getFileCreationTime());
        assertNotNull(resp.getValue().getSmbProperties().getFileChangeTime());
        assertNotNull(resp.getValue().getSmbProperties().getParentId());
        assertNotNull(resp.getValue().getSmbProperties().getFileId());
    }

    @Test
    public void createDirectoryWithNtfsAttributes() {
        String filePermissionKey = shareClient.createPermission(FILE_PERMISSION);
        EnumSet<NtfsFileAttributes> attributes =
            EnumSet.of(NtfsFileAttributes.HIDDEN, NtfsFileAttributes.DIRECTORY);
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setFilePermissionKey(filePermissionKey)
            .setNtfsFileAttributes(attributes);

        Response<ShareDirectoryInfo> resp = primaryDirectoryClient.createWithResponse(smbProperties, null, null, null,
            null);

        assertEquals(201, resp.getStatusCode());
        assertNotNull(resp.getValue().getSmbProperties().getFilePermissionKey());
        assertNotNull(resp.getValue().getSmbProperties().getNtfsFileAttributes());
        assertNotNull(resp.getValue().getSmbProperties().getFileLastWriteTime());
        assertNotNull(resp.getValue().getSmbProperties().getFileCreationTime());
        assertNotNull(resp.getValue().getSmbProperties().getFileChangeTime());
        assertNotNull(resp.getValue().getSmbProperties().getParentId());
        assertNotNull(resp.getValue().getSmbProperties().getFileId());
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20210608ServiceVersion")
    @Test
    public void createChangeTime() {
        OffsetDateTime changeTime = testResourceNamer.now();
        primaryDirectoryClient.createWithResponse(new FileSmbProperties().setFileChangeTime(changeTime), null, null,
            null, null);

        assertTrue(FileShareTestHelper.compareDatesWithPrecision(
            primaryDirectoryClient.getProperties().getSmbProperties().getFileChangeTime(), changeTime));
    }

    @ParameterizedTest
    @MethodSource("permissionAndKeySupplier")
    public void createDirectoryPermissionAndKeyError(String filePermissionKey, String permission) {
        FileSmbProperties properties = new FileSmbProperties().setFilePermissionKey(filePermissionKey);
        assertThrows(IllegalArgumentException.class, () ->
            primaryDirectoryClient.createWithResponse(properties, permission, null, null, null));
    }

    private static Stream<String[]> permissionAndKeySupplier() {
        return Stream.of(new String[]{"filePermissionKey", FILE_PERMISSION},
            new String[]{null, new String(FileShareTestHelper.getRandomBuffer(9 * Constants.KB))});
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20221102ServiceVersion")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void createTrailingDot(boolean allowTrailingDot) {
        ShareClient shareClient = getShareClient(shareName, allowTrailingDot, null);

        ShareDirectoryClient rootDirectory = shareClient.getRootDirectoryClient();
        String dirName = generatePathName();
        String dirNameWithDot = dirName + ".";
        ShareDirectoryClient dirClient = shareClient.getDirectoryClient(dirNameWithDot);
        dirClient.create();


        List<String> foundDirectories = new ArrayList<>();
        for (ShareFileItem fileRef : rootDirectory.listFilesAndDirectories()) {
            foundDirectories.add(fileRef.getName());
        }

        assertEquals(1, foundDirectories.size());
        if (allowTrailingDot) {
            assertEquals(dirNameWithDot, foundDirectories.get(0));
        } else {
            assertEquals(dirName, foundDirectories.get(0));
        }
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20210410ServiceVersion")
    @Test
    public void createDirectoryOAuth() {
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP));
        String dirName = generatePathName();
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName).getDirectoryClient(dirName);

        Response<ShareDirectoryInfo> result = dirClient.createWithResponse(null, null, null, null, null);

        assertEquals(shareName, dirClient.getShareName());
        assertEquals(dirName, dirClient.getDirectoryPath());
        assertEquals(result.getValue().getETag(), result.getHeaders().getValue(HttpHeaderName.ETAG));
    }

    @Test
    public void createIfNotExistsDirectoryMin() {
        assertNotNull(primaryDirectoryClient.createIfNotExists());
    }

    @Test
    public void createIfNotExistsDirectory() {
        assertEquals(201, primaryDirectoryClient
            .createIfNotExistsWithResponse(new ShareDirectoryCreateOptions(), null, null).getStatusCode());
    }

    @Test
    public void createIfNotExistsDirectoryError() {
        String testShareName = generateShareName();
        ShareStorageException e = assertThrows(ShareStorageException.class,
            () -> directoryBuilderHelper(testShareName, directoryPath).buildDirectoryClient().createIfNotExists());
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.SHARE_NOT_FOUND);
    }

    @Test
    public void createIfNotExistsDirectoryThatAlreadyExists() {
        ShareDirectoryCreateOptions options = new ShareDirectoryCreateOptions();
        ShareDirectoryClient primaryDirectoryClient = shareClient.getDirectoryClient(generatePathName());

        Response<ShareDirectoryInfo> initialResponse =
            primaryDirectoryClient.createIfNotExistsWithResponse(options, null, null);
        Response<ShareDirectoryInfo> secondResponse =
            primaryDirectoryClient.createIfNotExistsWithResponse(options, null, null);

        FileShareTestHelper.assertResponseStatusCode(initialResponse, 201);
        FileShareTestHelper.assertResponseStatusCode(secondResponse, 409);
    }

    @Test
    public void createIfNotExistsDirectoryWithMetadata() {
        ShareDirectoryCreateOptions options = new ShareDirectoryCreateOptions().setMetadata(testMetadata);
        FileShareTestHelper.assertResponseStatusCode(primaryDirectoryClient.createIfNotExistsWithResponse(options,
            null, null), 201);
    }

    @Test
    public void createIfNotExistsDirectoryWithFilePermission() {
        ShareDirectoryCreateOptions options = new ShareDirectoryCreateOptions().setFilePermission(FILE_PERMISSION);
        Response<ShareDirectoryInfo> resp = primaryDirectoryClient.createIfNotExistsWithResponse(options, null, null);

        FileShareTestHelper.assertResponseStatusCode(resp, 201);
        assertNotNull(resp.getValue().getSmbProperties());
        assertNotNull(resp.getValue().getSmbProperties().getFilePermissionKey());
        assertNotNull(resp.getValue().getSmbProperties().getNtfsFileAttributes());
        assertNotNull(resp.getValue().getSmbProperties().getFileLastWriteTime());
        assertNotNull(resp.getValue().getSmbProperties().getFileCreationTime());
        assertNotNull(resp.getValue().getSmbProperties().getFileChangeTime());
        assertNotNull(resp.getValue().getSmbProperties().getParentId());
        assertNotNull(resp.getValue().getSmbProperties().getFileId());
    }

    @Test
    public void createIfNotExistsDirectoryWithFilePermissionKey() {
        String filePermissionKey = shareClient.createPermission(FILE_PERMISSION);
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setFilePermissionKey(filePermissionKey);

        ShareDirectoryCreateOptions options = new ShareDirectoryCreateOptions().setSmbProperties(smbProperties);
        Response<ShareDirectoryInfo> resp = primaryDirectoryClient.createIfNotExistsWithResponse(options, null, null);

        FileShareTestHelper.assertResponseStatusCode(resp, 201);
        assertNotNull(resp.getValue().getSmbProperties());
        assertNotNull(resp.getValue().getSmbProperties().getFilePermissionKey());
        assertNotNull(resp.getValue().getSmbProperties().getNtfsFileAttributes());
        assertNotNull(resp.getValue().getSmbProperties().getFileLastWriteTime());
        assertNotNull(resp.getValue().getSmbProperties().getFileCreationTime());
        assertNotNull(resp.getValue().getSmbProperties().getFileChangeTime());
        assertNotNull(resp.getValue().getSmbProperties().getParentId());
        assertNotNull(resp.getValue().getSmbProperties().getFileId());
    }

    @Test
    public void createIfNotExistsDirectoryWithNtfsAttributes() {
        String filePermissionKey = shareClient.createPermission(FILE_PERMISSION);
        EnumSet<NtfsFileAttributes> attributes = EnumSet.of(NtfsFileAttributes.HIDDEN, NtfsFileAttributes.DIRECTORY);
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setFilePermissionKey(filePermissionKey)
            .setNtfsFileAttributes(attributes);

        ShareDirectoryCreateOptions options = new ShareDirectoryCreateOptions().setSmbProperties(smbProperties);
        Response<ShareDirectoryInfo> resp = primaryDirectoryClient.createIfNotExistsWithResponse(options, null, null);

        FileShareTestHelper.assertResponseStatusCode(resp, 201);
        assertNotNull(resp.getValue().getSmbProperties());
        assertNotNull(resp.getValue().getSmbProperties().getFilePermissionKey());
        assertNotNull(resp.getValue().getSmbProperties().getNtfsFileAttributes());
        assertNotNull(resp.getValue().getSmbProperties().getFileLastWriteTime());
        assertNotNull(resp.getValue().getSmbProperties().getFileCreationTime());
        assertNotNull(resp.getValue().getSmbProperties().getFileChangeTime());
        assertNotNull(resp.getValue().getSmbProperties().getParentId());
        assertNotNull(resp.getValue().getSmbProperties().getFileId());
    }

    @ParameterizedTest
    @MethodSource("permissionAndKeySupplier")
    public void createIfNotExistsDirectoryPermissionAndKeyError(String filePermissionKey, String permission) {
        FileSmbProperties properties = new FileSmbProperties().setFilePermissionKey(filePermissionKey);
        ShareDirectoryCreateOptions options = new ShareDirectoryCreateOptions()
            .setSmbProperties(properties)
            .setFilePermission(permission);

        assertThrows(IllegalArgumentException.class, () ->
            primaryDirectoryClient.createIfNotExistsWithResponse(options, null, null));
    }

    @Test
    public void deleteDirectory() {
        primaryDirectoryClient.create();
        FileShareTestHelper.assertResponseStatusCode(primaryDirectoryClient.deleteWithResponse(null, null), 202);
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20221102ServiceVersion")
    @Test
    public void deleteTrailingDot() {
        shareClient = getShareClient(shareName, true, null);
        ShareDirectoryClient directoryClient = shareClient.getDirectoryClient(generatePathName() + ".");
        directoryClient.create();
        FileShareTestHelper.assertResponseStatusCode(directoryClient.deleteWithResponse(null, null), 202);
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20210410ServiceVersion")
    @Test
    public void deleteDirectoryOAuth() {
        ShareServiceClient oAuthServiceClient =
            getOAuthServiceClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        String dirName = generatePathName();
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName).getDirectoryClient(dirName);
        dirClient.create();

        Response<Void> response = dirClient.deleteWithResponse(null, null);
        FileShareTestHelper.assertResponseStatusCode(response, 202);
        assertNotNull(response.getHeaders().getValue(HttpHeaderName.X_MS_CLIENT_REQUEST_ID));
    }

    @Test
    public void deleteDirectoryError() {
        ShareStorageException e = assertThrows(ShareStorageException.class, () -> primaryDirectoryClient.delete());
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    public void deleteIfExistsDirectory() {
        primaryDirectoryClient.create();
        FileShareTestHelper.assertResponseStatusCode(primaryDirectoryClient.deleteIfExistsWithResponse(null, null),
            202);
    }

    @Test
    public void deleteIfExistsDirectoryMin() {
        primaryDirectoryClient.create();
        assertTrue(primaryDirectoryClient.deleteIfExists());
    }

    @Test
    public void deleteIfExistsDirectoryThatDoesNotExist() {
        primaryDirectoryClient = shareClient.getDirectoryClient(generatePathName());
        Response<Boolean> response = primaryDirectoryClient.deleteIfExistsWithResponse(null, null);
        assertFalse(response.getValue());
        FileShareTestHelper.assertResponseStatusCode(response, 404);
        assertFalse(primaryDirectoryClient.exists());
    }

    @Test
    public void deleteIfExistsDirectoryThatWasAlreadyDeleted() {
        primaryDirectoryClient.create();
        Response<Boolean> initialResponse = primaryDirectoryClient.deleteIfExistsWithResponse(null, null);
        Response<Boolean> secondResponse = primaryDirectoryClient.deleteIfExistsWithResponse(null, null);
        assertEquals(202, initialResponse.getStatusCode());
        assertEquals(404, secondResponse.getStatusCode());
        assertTrue(initialResponse.getValue());
        assertFalse(secondResponse.getValue());
    }

    @Test
    public void getProperties() {
        primaryDirectoryClient.create();
        Response<ShareDirectoryProperties> resp = primaryDirectoryClient.getPropertiesWithResponse(null, null);

        FileShareTestHelper.assertResponseStatusCode(resp, 200);
        assertNotNull(resp.getValue().getETag());
        assertNotNull(resp.getValue().getSmbProperties());
        assertNotNull(resp.getValue().getSmbProperties().getFilePermissionKey());
        assertNotNull(resp.getValue().getSmbProperties().getNtfsFileAttributes());
        assertNotNull(resp.getValue().getSmbProperties().getFileLastWriteTime());
        assertNotNull(resp.getValue().getSmbProperties().getFileCreationTime());
        assertNotNull(resp.getValue().getSmbProperties().getFileChangeTime());
        assertNotNull(resp.getValue().getSmbProperties().getParentId());
        assertNotNull(resp.getValue().getSmbProperties().getFileId());
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20221102ServiceVersion")
    @Test
    public void getPropertiesTrailingDot() {
        shareClient = getShareClient(shareName, true, null);

        ShareDirectoryClient directoryClient = shareClient.getDirectoryClient(generatePathName() + ".");
        ShareDirectoryInfo createResponse = directoryClient.createIfNotExists();
        Response<ShareDirectoryProperties> propertiesResponse = directoryClient.getPropertiesWithResponse(null, null);

        FileShareTestHelper.assertResponseStatusCode(propertiesResponse, 200);
        assertEquals(createResponse.getETag(), propertiesResponse.getValue().getETag());
        assertEquals(createResponse.getLastModified(), propertiesResponse.getValue().getLastModified());

        FileSmbProperties createSmbProperties = createResponse.getSmbProperties();
        FileSmbProperties getPropertiesSmbProperties = propertiesResponse.getValue().getSmbProperties();
        assertEquals(createSmbProperties.getFilePermissionKey(), getPropertiesSmbProperties.getFilePermissionKey());
        assertEquals(createSmbProperties.getNtfsFileAttributes(), getPropertiesSmbProperties.getNtfsFileAttributes());
        assertEquals(createSmbProperties.getFileLastWriteTime(), getPropertiesSmbProperties.getFileLastWriteTime());
        assertEquals(createSmbProperties.getFileCreationTime(), getPropertiesSmbProperties.getFileCreationTime());
        assertEquals(createSmbProperties.getFileChangeTime(), getPropertiesSmbProperties.getFileChangeTime());
        assertEquals(createSmbProperties.getParentId(), getPropertiesSmbProperties.getParentId());
        assertEquals(createSmbProperties.getFileId(), getPropertiesSmbProperties.getFileId());

    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20210410ServiceVersion")
    @Test
    public void getPropertiesOAuth() {
        ShareServiceClient oAuthServiceClient =
            getOAuthServiceClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        String dirName = generatePathName();
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName).getDirectoryClient(dirName);

        ShareDirectoryInfo createInfo = dirClient.create();
        ShareDirectoryProperties properties = dirClient.getProperties();

        assertEquals(createInfo.getETag(), properties.getETag());
        assertEquals(createInfo.getLastModified(), properties.getLastModified());
        assertEquals(createInfo.getSmbProperties().getFilePermissionKey(),
            properties.getSmbProperties().getFilePermissionKey());
        assertEquals(createInfo.getSmbProperties().getNtfsFileAttributes(),
            properties.getSmbProperties().getNtfsFileAttributes());
        assertEquals(createInfo.getSmbProperties().getFileLastWriteTime(),
            properties.getSmbProperties().getFileLastWriteTime());
        assertEquals(createInfo.getSmbProperties().getFileCreationTime(),
            properties.getSmbProperties().getFileCreationTime());
        assertEquals(createInfo.getSmbProperties().getFileChangeTime(),
            properties.getSmbProperties().getFileChangeTime());
        assertEquals(createInfo.getSmbProperties().getParentId(), properties.getSmbProperties().getParentId());
        assertEquals(createInfo.getSmbProperties().getFileId(), properties.getSmbProperties().getFileId());
    }

    @Test
    public void getPropertiesError() {
        ShareStorageException e = assertThrows(ShareStorageException.class,
            () -> primaryDirectoryClient.getPropertiesWithResponse(null, null));
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    public void setPropertiesFilePermission() {
        primaryDirectoryClient.create();
        Response<ShareDirectoryInfo> resp = primaryDirectoryClient.setPropertiesWithResponse(null, FILE_PERMISSION,
            null, null);

        FileShareTestHelper.assertResponseStatusCode(resp, 200);
        assertNotNull(resp.getValue().getSmbProperties());
        assertNotNull(resp.getValue().getSmbProperties().getFilePermissionKey());
        assertNotNull(resp.getValue().getSmbProperties().getNtfsFileAttributes());
        assertNotNull(resp.getValue().getSmbProperties().getFileLastWriteTime());
        assertNotNull(resp.getValue().getSmbProperties().getFileCreationTime());
        assertNotNull(resp.getValue().getSmbProperties().getFileChangeTime());
        assertNotNull(resp.getValue().getSmbProperties().getParentId());
        assertNotNull(resp.getValue().getSmbProperties().getFileId());
    }

    @Test
    public void setPropertiesFilePermissionKey() {
        String filePermissionKey = shareClient.createPermission(FILE_PERMISSION);
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setFilePermissionKey(filePermissionKey);

        primaryDirectoryClient.create();
        Response<ShareDirectoryInfo> resp = primaryDirectoryClient.setPropertiesWithResponse(smbProperties, null, null,
            null);
        FileShareTestHelper.assertResponseStatusCode(resp, 200);
        assertNotNull(resp.getValue().getSmbProperties());
        assertNotNull(resp.getValue().getSmbProperties().getFilePermissionKey());
        assertNotNull(resp.getValue().getSmbProperties().getNtfsFileAttributes());
        assertNotNull(resp.getValue().getSmbProperties().getFileLastWriteTime());
        assertNotNull(resp.getValue().getSmbProperties().getFileCreationTime());
        assertNotNull(resp.getValue().getSmbProperties().getFileChangeTime());
        assertNotNull(resp.getValue().getSmbProperties().getParentId());
        assertNotNull(resp.getValue().getSmbProperties().getFileId());
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20210608ServiceVersion")
    @Test
    public void setHttpHeadersChangeTime() {
        primaryDirectoryClient.create();
        String filePermissionKey = shareClient.createPermission(FILE_PERMISSION);
        OffsetDateTime changeTime = testResourceNamer.now();
        smbProperties.setFileChangeTime(testResourceNamer.now())
            .setFilePermissionKey(filePermissionKey);

        primaryDirectoryClient.setProperties(new FileSmbProperties().setFileChangeTime(changeTime), null);
        FileShareTestHelper.compareDatesWithPrecision(primaryDirectoryClient.getProperties().getSmbProperties()
            .getFileChangeTime(), changeTime);
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20221102ServiceVersion")
    @Test
    public void setHttpHeadersTrailingDot() {
        shareClient = getShareClient(shareName, true, null);

        ShareDirectoryClient directoryClient = shareClient.getDirectoryClient(generatePathName() + ".");
        directoryClient.createIfNotExists();
        Response<ShareDirectoryInfo> res = directoryClient.setPropertiesWithResponse(new FileSmbProperties(), null,
            null, null);
        FileShareTestHelper.assertResponseStatusCode(res, 200);
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20210410ServiceVersion")
    @Test
    public void setHttpHeadersOAuth() {
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP));
        String dirName = generatePathName();
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName).getDirectoryClient(dirName);
        dirClient.create();
        Response<ShareDirectoryInfo> res = dirClient.setPropertiesWithResponse(new FileSmbProperties(), null, null,
            null);
        FileShareTestHelper.assertResponseStatusCode(res, 200);
    }

    @ParameterizedTest
    @MethodSource("permissionAndKeySupplier")
    public void setPropertiesError(String filePermissionKey, String permission) {
        FileSmbProperties properties = new FileSmbProperties().setFilePermissionKey(filePermissionKey);
        primaryDirectoryClient.create();

        assertThrows(IllegalArgumentException.class, () ->
            primaryDirectoryClient.setPropertiesWithResponse(properties, permission, null, null));
    }

    @Test
    public void setMetadata() {
        primaryDirectoryClient.createWithResponse(null, null, testMetadata, null, null);
        Map<String, String> updatedMetadata = Collections.singletonMap("update", "value");

        ShareDirectoryProperties getPropertiesBefore = primaryDirectoryClient.getProperties();
        Response<ShareDirectorySetMetadataInfo> setPropertiesResponse =
            primaryDirectoryClient.setMetadataWithResponse(updatedMetadata, null, null);
        ShareDirectoryProperties getPropertiesAfter = primaryDirectoryClient.getProperties();

        assertEquals(testMetadata, getPropertiesBefore.getMetadata());
        FileShareTestHelper.assertResponseStatusCode(setPropertiesResponse, 200);
        assertEquals(updatedMetadata, getPropertiesAfter.getMetadata());
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20221102ServiceVersion")
    @Test
    public void setMetadataTrailingDot() {
        shareClient = getShareClient(shareName, true, null);

        ShareDirectoryClient directoryClient = shareClient.getDirectoryClient(generatePathName() + ".");

        directoryClient.createWithResponse(null, null, testMetadata, null, null);
        Map<String, String> updatedMetadata = Collections.singletonMap("update", "value");

        ShareDirectoryProperties getPropertiesBefore = directoryClient.getProperties();
        Response<ShareDirectorySetMetadataInfo> setPropertiesResponse =
            directoryClient.setMetadataWithResponse(updatedMetadata, null, null);
        ShareDirectoryProperties getPropertiesAfter = directoryClient.getProperties();

        assertEquals(testMetadata, getPropertiesBefore.getMetadata());
        FileShareTestHelper.assertResponseStatusCode(setPropertiesResponse, 200);
        assertEquals(updatedMetadata, getPropertiesAfter.getMetadata());
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20210410ServiceVersion")
    @Test
    public void setMetadataOAuth() {
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP));
        String dirName = generatePathName();
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName).getDirectoryClient(dirName);
        dirClient.createWithResponse(null, null, testMetadata, null, null);
        Map<String, String> updatedMetadata = Collections.singletonMap("update", "value");

        ShareDirectoryProperties getPropertiesBefore = dirClient.getProperties();
        Response<ShareDirectorySetMetadataInfo> setPropertiesResponse =
            dirClient.setMetadataWithResponse(updatedMetadata, null, null);
        ShareDirectoryProperties getPropertiesAfter = dirClient.getProperties();

        assertEquals(testMetadata, getPropertiesBefore.getMetadata());
        FileShareTestHelper.assertResponseStatusCode(setPropertiesResponse, 200);
        assertEquals(updatedMetadata, getPropertiesAfter.getMetadata());
    }

    @Test
    public void setMetadataError() {
        primaryDirectoryClient.create();
        Map<String, String> errorMetadata = Collections.singletonMap("", "value");
        ShareStorageException e = assertThrows(ShareStorageException.class,
            () -> primaryDirectoryClient.setMetadata(errorMetadata));
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.EMPTY_METADATA_KEY);
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#listFilesAndDirectoriesSupplier")
    public void listFilesAndDirectories(String[] expectedFiles, String[] expectedDirectories) {
        primaryDirectoryClient.create();

        for (String expectedFile : expectedFiles) {
            primaryDirectoryClient.createFile(expectedFile, 2);
        }

        for (String expectedDirectory : expectedDirectories) {
            primaryDirectoryClient.createSubdirectory(expectedDirectory);
        }

        List<String> foundFiles = new ArrayList<>();
        List<String> foundDirectories = new ArrayList<>();
        for (ShareFileItem fileRef : primaryDirectoryClient.listFilesAndDirectories()) {
            if (fileRef.isDirectory()) {
                foundDirectories.add(fileRef.getName());
            } else {
                foundFiles.add(fileRef.getName());
            }
        }

        assertArrayEquals(expectedFiles, foundFiles.toArray());
        assertArrayEquals(expectedDirectories, foundDirectories.toArray());
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20201002ServiceVersion")
    @ParameterizedTest
    @MethodSource("listFilesAndDirectoriesArgsSupplier")
    public void listFilesAndDirectoriesArgs(String extraPrefix, Integer maxResults, int numOfResults) {
        primaryDirectoryClient.create();
        List<String> nameList = new ArrayList<>();
        String dirPrefix = generatePathName();
        for (int i = 0; i < 2; i++) {
            ShareDirectoryClient subDirClient = primaryDirectoryClient.getSubdirectoryClient(dirPrefix + i);
            subDirClient.create();
            for (int j = 0; j < 2; j++) {
                int num = i * 2 + j + 3;
                subDirClient.createFile(dirPrefix + num, 1024);
            }
        }
        primaryDirectoryClient.createFile(dirPrefix + 2, 1024);
        for (int i = 0; i < 3; i++) {
            nameList.add(dirPrefix + i);
        }

        Iterator<ShareFileItem> fileRefIter = primaryDirectoryClient
            .listFilesAndDirectories(prefix + extraPrefix, maxResults, null, null).iterator();

        for (int i = 0; i < numOfResults; i++) {
            assertEquals(nameList.get(i), fileRefIter.next().getName());
        }
        assertFalse(fileRefIter.hasNext());
    }

    private static Stream<Arguments> listFilesAndDirectoriesArgsSupplier() {
        return Stream.of(
            Arguments.of("", null, 3),
            Arguments.of("", 1, 3),
            Arguments.of("noOp", 3, 0));
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20201002ServiceVersion")
    @ParameterizedTest
    @CsvSource(value = {"false,false,false,false", "true,false,false,false", "false,true,false,false",
        "false,false,true,false", "false,false,false,true", "true,true,true,true"})
    public void listFilesAndDirectoriesExtendedInfoArgs(boolean timestamps, boolean etag, boolean attributes,
        boolean permissionKey) {
        primaryDirectoryClient.create();
        List<String> nameList = new ArrayList<>();
        String dirPrefix = generatePathName();
        for (int i = 0; i < 2; i++) {
            ShareDirectoryClient subDirClient = primaryDirectoryClient.getSubdirectoryClient(dirPrefix + i);
            subDirClient.create();
            for (int j = 0; j < 2; j++) {
                int num = i * 2 + j + 3;
                subDirClient.createFile(dirPrefix + num, 1024);
            }
        }
        primaryDirectoryClient.createFile(dirPrefix + 2, 1024);
        for (int i = 0; i < 3; i++) {
            nameList.add(dirPrefix + i);
        }

        ShareListFilesAndDirectoriesOptions options = new ShareListFilesAndDirectoriesOptions()
            .setPrefix(prefix)
            .setIncludeExtendedInfo(true)
            .setIncludeTimestamps(timestamps)
            .setIncludeETag(etag)
            .setIncludeAttributes(attributes)
            .setIncludePermissionKey(permissionKey);
        List<ShareFileItem> returnedFileList = primaryDirectoryClient.listFilesAndDirectories(options, null, null)
            .stream().collect(Collectors.toList());

        for (int i = 0; i < nameList.size(); i++) {
            assertEquals(nameList.get(i), returnedFileList.get(i).getName());
        }
    }

    @Test
    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20201002ServiceVersion")
    public void listFilesAndDirectoriesExtendedInfoResults() {
        ShareDirectoryClient parentDir = primaryDirectoryClient;
        parentDir.create();
        ShareFileClient file = parentDir.createFile(generatePathName(), 1024);
        ShareDirectoryClient dir = parentDir.createSubdirectory(generatePathName());

        List<ShareFileItem> listResults = parentDir.listFilesAndDirectories(
                new ShareListFilesAndDirectoriesOptions()
                    .setIncludeExtendedInfo(true)
                    .setIncludeTimestamps(true)
                    .setIncludePermissionKey(true)
                    .setIncludeETag(true)
                    .setIncludeAttributes(true),
                null, null)
            .stream().collect(Collectors.toList());

        ShareFileItem dirListItem;
        ShareFileItem fileListItem;
        if (listResults.get(0).isDirectory()) {
            dirListItem = listResults.get(0);
            fileListItem = listResults.get(1);
        } else {
            dirListItem = listResults.get(1);
            fileListItem = listResults.get(0);
        }

        assertEquals(dirListItem.getName(), new File(dir.getDirectoryPath()).getName());
        assertTrue(dirListItem.isDirectory());
        assertNotNull(dirListItem.getId());
        assertFalse(FileShareTestHelper.isAllWhitespace(dirListItem.getId()));

        assertEquals(EnumSet.of(NtfsFileAttributes.DIRECTORY), dirListItem.getFileAttributes());
        assertNotNull(dirListItem.getPermissionKey());
        assertFalse(FileShareTestHelper.isAllWhitespace(dirListItem.getPermissionKey()));
        assertNotNull(dirListItem.getProperties().getCreatedOn());
        assertNotNull(dirListItem.getProperties().getLastAccessedOn());
        assertNotNull(dirListItem.getProperties().getLastWrittenOn());
        assertNotNull(dirListItem.getProperties().getChangedOn());
        assertNotNull(dirListItem.getProperties().getLastModified());
        assertNotNull(dirListItem.getProperties().getETag());
        assertFalse(FileShareTestHelper.isAllWhitespace(dirListItem.getProperties().getETag()));
        assertEquals(fileListItem.getName(), new File(file.getFilePath()).getName());
        assertFalse(fileListItem.isDirectory());
        assertNotNull(fileListItem.getId());
        assertFalse(FileShareTestHelper.isAllWhitespace(fileListItem.getId()));
        assertEquals(EnumSet.of(NtfsFileAttributes.ARCHIVE), fileListItem.getFileAttributes());
        assertNotNull(fileListItem.getPermissionKey());
        assertFalse(FileShareTestHelper.isAllWhitespace(fileListItem.getPermissionKey()));
        assertNotNull(fileListItem.getProperties().getCreatedOn());
        assertNotNull(fileListItem.getProperties().getLastAccessedOn());
        assertNotNull(fileListItem.getProperties().getLastWrittenOn());
        assertNotNull(fileListItem.getProperties().getChangedOn());
        assertNotNull(fileListItem.getProperties().getLastModified());
        assertNotNull(fileListItem.getProperties().getETag());
        assertFalse(FileShareTestHelper.isAllWhitespace(fileListItem.getProperties().getETag()));
    }

    @Test
    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20211202ServiceVersion")
    public void listFilesAndDirectoriesEncoded() {
        String specialCharDirectoryName = "directory\uFFFE";
        String specialCharFileName = "file\uFFFE";

        primaryDirectoryClient.create();
        primaryDirectoryClient.createSubdirectory(specialCharDirectoryName);
        primaryDirectoryClient.createFile(specialCharFileName, 1024);

        List<ShareFileItem> shareFileItems = primaryDirectoryClient.listFilesAndDirectories().stream()
            .collect(Collectors.toList());

        assertEquals(2, shareFileItems.size());
        assertTrue(shareFileItems.get(0).isDirectory());
        assertEquals(specialCharDirectoryName, shareFileItems.get(0).getName());
        assertFalse(shareFileItems.get(1).isDirectory());
        assertEquals(specialCharFileName, shareFileItems.get(1).getName());
    }

    @Test
    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20211202ServiceVersion")
    public void listFilesAndDirectoriesEncodedContinuationToken() {
        // Test implementation
        String specialCharFileName0 = "file0\uFFFE";
        String specialCharFileName1 = "file1\uFFFE";

        primaryDirectoryClient.create();
        primaryDirectoryClient.createFile(specialCharFileName0, 1024);
        primaryDirectoryClient.createFile(specialCharFileName1, 1024);

        List<ShareFileItem> shareFileItems = new ArrayList<>();
        for (PagedResponse<ShareFileItem> page : primaryDirectoryClient.listFilesAndDirectories().iterableByPage(1)) {
            shareFileItems.addAll(page.getValue());
        }

        assertEquals(specialCharFileName0, shareFileItems.get(0).getName());
        assertEquals(specialCharFileName1, shareFileItems.get(1).getName());
    }

    @Test
    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20211202ServiceVersion")
    public void listFilesAndDirectoriesEncodedPrefix() {
        String specialCharDirectoryName = "directory\uFFFE";

        primaryDirectoryClient.create();
        primaryDirectoryClient.createSubdirectory(specialCharDirectoryName);

        List<ShareFileItem> shareFileItems = primaryDirectoryClient.listFilesAndDirectories().stream()
            .collect(Collectors.toList());

        assertEquals(1, shareFileItems.size());
        assertTrue(shareFileItems.get(0).isDirectory());
        assertEquals(specialCharDirectoryName, shareFileItems.get(0).getName());
    }

    @Test
    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20210410ServiceVersion")
    public void testListFilesAndDirectoriesOAuth() {
        ShareDirectoryClient dirClient = getOAuthServiceClient(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP))
            .getShareClient(shareName)
            .getDirectoryClient(generatePathName());
        dirClient.create();

        List<String> fileNames = new ArrayList<>();
        List<String> dirNames = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            fileNames.add(generatePathName());
        }
        for (int i = 0; i < 5; i++) {
            dirNames.add(generatePathName());
        }

        for (String file : fileNames) {
            dirClient.createFile(file, Constants.KB);
        }
        for (String directory : dirNames) {
            dirClient.createSubdirectory(directory);
        }

        List<String> foundFiles = new ArrayList<>();
        List<String> foundDirectories = new ArrayList<>();
        for (ShareFileItem fileRef : dirClient.listFilesAndDirectories()) {
            if (fileRef.isDirectory()) {
                foundDirectories.add(fileRef.getName());
            } else {
                foundFiles.add(fileRef.getName());
            }
        }

        assertTrue(fileNames.containsAll(foundFiles));
        assertTrue(dirNames.containsAll(foundDirectories));
    }

    @Test
    public void listMaxResultsByPage() {
        primaryDirectoryClient.create();
        String dirPrefix = generatePathName();
        for (int i = 0; i < 2; i++) {
            ShareDirectoryClient subDirClient = primaryDirectoryClient.getSubdirectoryClient(dirPrefix + i);
            subDirClient.create();
            for (int j = 0; j < 2; j++) {
                int num = i * 2 + j + 3;
                subDirClient.createFile(dirPrefix + num, 1024);
            }
        }

        for (PagedResponse<ShareFileItem> page
            : primaryDirectoryClient.listFilesAndDirectories(prefix, null, null, null).iterableByPage(1)) {
            assertEquals(1, page.getValue().size());
        }
    }

    @ParameterizedTest
    @MethodSource("listHandlesSupplier")
    public void listHandles(Integer maxResults, boolean recursive) {
        primaryDirectoryClient.create();

        List<HandleItem> handles = primaryDirectoryClient.listHandles(maxResults, recursive, null, null).stream()
            .collect(Collectors.toList());

        assertEquals(0, handles.size());
    }

    private static Stream<Arguments> listHandlesSupplier() {
        return Stream.of(
            Arguments.of(2, true),
            Arguments.of(null, false));
    }

    @Test
    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20221102ServiceVersion")
    public void listHandlesTrailingDot() {
        shareClient = getShareClient(shareName, true, null);
        String directoryName = generatePathName() + ".";
        ShareDirectoryClient directoryClient = shareClient.getDirectoryClient(directoryName);
        directoryClient.create();

        List<HandleItem> handles = directoryClient.listHandles(null, false, null, null).stream()
            .collect(Collectors.toList());
        assertEquals(0, handles.size());
    }

    @Test
    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20210410ServiceVersion")
    public void listHandlesOAuth() {
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName)
            .getDirectoryClient(generatePathName());

        dirClient.create();

        List<HandleItem> handles = dirClient.listHandles(2, true, null, null).stream().collect(Collectors.toList());
        assertEquals(0, handles.size());
    }

    @Test
    public void listHandlesError() {
        Exception e = assertThrows(ShareStorageException.class,
            () -> primaryDirectoryClient.listHandles(null, true, null, null).iterator().hasNext());
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20190707ServiceVersion")
    public void forceCloseHandleMin() {
        primaryDirectoryClient.create();
        CloseHandlesInfo handlesClosedInfo = primaryDirectoryClient.forceCloseHandle("1");
        assertEquals(0, handlesClosedInfo.getClosedHandles());
        assertEquals(0, handlesClosedInfo.getFailedHandles());
    }

    @Test
    public void forceCloseHandleInvalidHandleId() {
        primaryDirectoryClient.create();
        assertThrows(ShareStorageException.class, () -> primaryDirectoryClient.forceCloseHandle("invalidHandleId"));
    }

    @Test
    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20210410ServiceVersion")
    public void forceCloseHandleOAuth() {
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName)
            .getDirectoryClient(generatePathName());

        dirClient.create();
        CloseHandlesInfo handlesClosedInfo = dirClient.forceCloseHandle("1");
        assertEquals(0, handlesClosedInfo.getClosedHandles());
        assertEquals(0, handlesClosedInfo.getFailedHandles());
    }

    @Test
    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20190707ServiceVersion")
    public void forceCloseAllHandlesMin() {
        primaryDirectoryClient.create();
        CloseHandlesInfo handlesClosedInfo = primaryDirectoryClient.forceCloseAllHandles(false, null, null);
        assertEquals(0, handlesClosedInfo.getClosedHandles());
        assertEquals(0, handlesClosedInfo.getFailedHandles());
    }

    @Test
    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20221102ServiceVersion")
    public void forceCloseAllHandlesTrailingDot() {
        shareClient = getShareClient(shareName, true, null);
        ShareDirectoryClient directoryClient = shareClient.getDirectoryClient(generatePathName() + ".");
        directoryClient.create();
        CloseHandlesInfo handlesClosedInfo = directoryClient.forceCloseAllHandles(false, null, null);
        assertEquals(0, handlesClosedInfo.getClosedHandles());
        assertEquals(0, handlesClosedInfo.getFailedHandles());
    }

    @Test
    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20210410ServiceVersion")
    public void renameMin() {
        primaryDirectoryClient.create();
        assertDoesNotThrow(() -> primaryDirectoryClient.rename(generatePathName()));
    }

    @Test
    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20210410ServiceVersion")
    public void renameWithResponse() {
        primaryDirectoryClient.create();
        Response<ShareDirectoryClient> resp = primaryDirectoryClient.renameWithResponse(
            new ShareFileRenameOptions(generatePathName()), null, null);
        ShareDirectoryClient renamedClient = resp.getValue();
        assertDoesNotThrow(renamedClient::getProperties);
        assertThrows(ShareStorageException.class, () -> primaryDirectoryClient.getProperties());
    }

    @Test
    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20210410ServiceVersion")
    public void renameDifferentDirectory() {
        primaryDirectoryClient.create();
        ShareDirectoryClient destinationClient = shareClient.getDirectoryClient(generatePathName());
        destinationClient.create();
        String destinationPath = destinationClient.getFileClient(generatePathName()).getFilePath();
        ShareDirectoryClient resultClient = primaryDirectoryClient.rename(destinationPath);
        assertTrue(resultClient.exists());
        assertFalse(primaryDirectoryClient.exists());
        assertEquals(destinationPath, resultClient.getDirectoryPath());
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20210410ServiceVersion")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void renameReplaceIfExists(boolean replaceIfExists) {
        primaryDirectoryClient.create();
        ShareFileClient destination = shareClient.getFileClient(generatePathName());
        destination.create(512L);
        boolean exception = false;
        try {
            primaryDirectoryClient.renameWithResponse(new ShareFileRenameOptions(destination.getFilePath())
                .setReplaceIfExists(replaceIfExists), null, null);
        } catch (ShareStorageException ignored) {
            exception = true;
        }
        assertEquals(replaceIfExists, !exception);
    }

    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20210410ServiceVersion")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void renameIgnoreReadOnly(boolean ignoreReadOnly) {
        primaryDirectoryClient.create();
        FileSmbProperties props = new FileSmbProperties().setNtfsFileAttributes(
            EnumSet.of(NtfsFileAttributes.READ_ONLY));
        ShareFileClient destinationFile = shareClient.getFileClient(generatePathName());
        destinationFile.createWithResponse(512L, null, props, null, null, null, null, null);
        ShareFileRenameOptions options = new ShareFileRenameOptions(destinationFile.getFilePath())
            .setIgnoreReadOnly(ignoreReadOnly).setReplaceIfExists(true);
        boolean exception = false;
        try {
            primaryDirectoryClient.renameWithResponse(options, null, null);
        } catch (ShareStorageException ignored) {
            exception = true;
        }
        assertEquals(!ignoreReadOnly, exception);
    }

    @Test
    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20210410ServiceVersion")
    public void renameFilePermission() {
        primaryDirectoryClient.create();
        String filePermission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-188441444-3053964)";
        ShareFileRenameOptions options = new ShareFileRenameOptions(generatePathName())
            .setFilePermission(filePermission);
        ShareDirectoryClient destClient = primaryDirectoryClient.renameWithResponse(options, null, null).getValue();
        assertNotNull(destClient.getProperties().getSmbProperties().getFilePermissionKey());
    }

    @Test
    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20210410ServiceVersion")
    public void renameFilePermissionAndKeySet() {
        primaryDirectoryClient.create();
        String filePermission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-188441444-3053964)";
        ShareFileRenameOptions options = new ShareFileRenameOptions(generatePathName())
            .setFilePermission(filePermission)
            .setSmbProperties(new FileSmbProperties()
                .setFilePermissionKey("filePermissionkey"));
        assertThrows(ShareStorageException.class, () ->
            primaryDirectoryClient.renameWithResponse(options, null, null).getValue());
    }

    @Test
    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20210410ServiceVersion")
    public void renameFileSmbProperties() {
        primaryDirectoryClient.create();
        String filePermission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-188441444-3053964)";
        String permissionKey = shareClient.createPermission(filePermission);
        FileSmbProperties smbProperties = new FileSmbProperties()
            .setFilePermissionKey(permissionKey)
            .setNtfsFileAttributes(EnumSet.of(NtfsFileAttributes.DIRECTORY))
            .setFileCreationTime(testResourceNamer.now().minusDays(5))
            .setFileLastWriteTime(testResourceNamer.now().minusYears(2))
            .setFileChangeTime(testResourceNamer.now());

        ShareFileRenameOptions options = new ShareFileRenameOptions(generatePathName()).setSmbProperties(smbProperties);
        ShareDirectoryClient destClient = primaryDirectoryClient.renameWithResponse(options, null, null).getValue();
        FileSmbProperties destSmbProperties = destClient.getProperties().getSmbProperties();
        assertEquals(EnumSet.of(NtfsFileAttributes.DIRECTORY), destSmbProperties.getNtfsFileAttributes());
        assertNotNull(destSmbProperties.getFileCreationTime());
        assertNotNull(destSmbProperties.getFileLastWriteTime());
        FileShareTestHelper.compareDatesWithPrecision(destSmbProperties.getFileChangeTime(), testResourceNamer.now());
    }

    @Test
    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20210410ServiceVersion")
    public void renameMetadata() {
        primaryDirectoryClient.create();
        String key = "update";
        String value = "value";
        Map<String, String> updatedMetadata = Collections.singletonMap(key, value);
        ShareFileRenameOptions options = new ShareFileRenameOptions(generatePathName())
            .setMetadata(updatedMetadata);
        ShareDirectoryClient renamedClient = primaryDirectoryClient.renameWithResponse(options, null, null).getValue();
        ShareDirectoryProperties properties = renamedClient.getProperties();
        // assert that the key exists in the metadata
        assertNotNull(properties.getMetadata().get(key));
        assertEquals(value, renamedClient.getProperties().getMetadata().get(key));
    }

    @Test
    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20210410ServiceVersion")
    public void renameOAuth() {
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName)
            .getDirectoryClient(generatePathName());
        dirClient.create();

        String dirRename = generatePathName();

        ShareFileRenameOptions options = new ShareFileRenameOptions(dirRename);
        ShareDirectoryClient renamedClient = dirClient.renameWithResponse(options, null, null).getValue();
        assertDoesNotThrow(renamedClient::getProperties);
        assertEquals(dirRename, renamedClient.getDirectoryPath());
        assertThrows(ShareStorageException.class, dirClient::getProperties);
    }

    @Test
    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20210410ServiceVersion")
    public void renameError() {
        primaryDirectoryClient = shareClient.getDirectoryClient(generatePathName());
        assertThrows(ShareStorageException.class, () -> primaryDirectoryClient.rename(generatePathName()));
    }

    @Test
    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20210410ServiceVersion")
    public void renameDestAC() {
        primaryDirectoryClient.create();
        String pathName = generatePathName();
        ShareFileClient destFile = shareClient.getFileClient(pathName);
        destFile.create(512);
        String leaseID = setupFileLeaseCondition(destFile, RECEIVED_LEASE_ID);
        ShareRequestConditions src = new ShareRequestConditions().setLeaseId(leaseID);

        FileShareTestHelper.assertResponseStatusCode(primaryDirectoryClient.renameWithResponse(
            new ShareFileRenameOptions(pathName).setDestinationRequestConditions(src).setReplaceIfExists(true), null,
            null), 200);
    }

    @Test
    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20210410ServiceVersion")
    public void renameDestACFail() {
        primaryDirectoryClient.create();
        String pathName = generatePathName();
        ShareFileClient destFile = shareClient.getFileClient(pathName);
        destFile.create(512);
        setupFileLeaseCondition(destFile, GARBAGE_LEASE_ID);
        ShareRequestConditions src = new ShareRequestConditions().setLeaseId(GARBAGE_LEASE_ID);

        // should be throwing ShareStorageException, but test-proxy causes an error with mismatched requests
        assertThrows(RuntimeException.class,
            () -> primaryDirectoryClient.renameWithResponse(new ShareFileRenameOptions(pathName)
            .setDestinationRequestConditions(src).setReplaceIfExists(true), null, null));
    }

    @Test
    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20210212ServiceVersion")
    public void testRenameSASToken() {
        ShareFileSasPermission permissions = new ShareFileSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true);

        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);

        ShareServiceSasSignatureValues sasValues = new ShareServiceSasSignatureValues(expiryTime, permissions);

        String sas = shareClient.generateSas(sasValues);
        ShareDirectoryClient client = getDirectoryClient(sas, primaryDirectoryClient.getDirectoryUrl());
        primaryDirectoryClient.create();

        String directoryName = generatePathName();
        ShareDirectoryClient destClient = client.rename(directoryName);

        assertNotNull(destClient);
        destClient.getProperties();
        assertEquals(directoryName, destClient.getDirectoryPath());
    }

    @Test
    @DisabledIf("com.azure.storage.file.share.FileShareTestBase#olderThan20221102ServiceVersion")
    public void renameTrailingDot() {
        shareClient = getShareClient(shareName, true, true);

        String directoryName = generatePathName() + ".";
        ShareDirectoryClient directoryClient = shareClient.getDirectoryClient(directoryName);
        directoryClient.create();

        assertDoesNotThrow(() -> directoryClient.rename(directoryName));
    }

    @Test
    public void createSubDirectory() {
        primaryDirectoryClient.create();
        FileShareTestHelper.assertResponseStatusCode(primaryDirectoryClient.createSubdirectoryWithResponse(
            "testCreateSubDirectory", null, null, null, null, null), 201);
    }

    @Test
    public void createSubDirectoryInvalidName() {
        primaryDirectoryClient.create();
        ShareStorageException e = assertThrows(ShareStorageException.class,
            () -> primaryDirectoryClient.createSubdirectory("test/subdirectory"));
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.PARENT_NOT_FOUND);
    }

    @Test
    public void createSubDirectoryMetadata() {
        primaryDirectoryClient.create();
        FileShareTestHelper.assertResponseStatusCode(primaryDirectoryClient.createSubdirectoryWithResponse(
            "testCreateSubDirectory", null, null, testMetadata, null, null), 201);
    }

    @Test
    public void createSubDirectoryMetadataError() {
        primaryDirectoryClient.create();
        ShareStorageException e = assertThrows(ShareStorageException.class,
            () -> primaryDirectoryClient.createSubdirectoryWithResponse("testsubdirectory", null, null,
                Collections.singletonMap("", "value"), null, null));
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.EMPTY_METADATA_KEY);
    }

    @Test
    public void createSubDirectoryFilePermission() {
        primaryDirectoryClient.create();

        FileShareTestHelper.assertResponseStatusCode(
            primaryDirectoryClient.createSubdirectoryWithResponse("testCreateSubDirectory",
                null, FILE_PERMISSION, null, null, null), 201);
    }

    @Test
    public void createSubDirectoryFilePermissionKey() {
        primaryDirectoryClient.create();
        String filePermissionKey = shareClient.createPermission(FILE_PERMISSION);
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setFilePermissionKey(filePermissionKey);

        FileShareTestHelper.assertResponseStatusCode(
            primaryDirectoryClient.createSubdirectoryWithResponse("testCreateSubDirectory", smbProperties, null, null,
                null, null), 201);
    }

    @Test
    public void createIfNotExistsSubDirectory() {
        primaryDirectoryClient.create();
        FileShareTestHelper.assertResponseStatusCode(primaryDirectoryClient.createSubdirectoryIfNotExistsWithResponse(
            "testCreateSubDirectory", new ShareDirectoryCreateOptions(), null, null), 201);
    }

    @Test
    public void createIfNotExistsSubDirectoryAlreadyExists() {
        String subdirectoryName = generatePathName();
        primaryDirectoryClient = shareClient.getDirectoryClient(generatePathName());
        primaryDirectoryClient.create();
        int initialResponseCode = primaryDirectoryClient.createSubdirectoryIfNotExistsWithResponse(
                subdirectoryName,
                new ShareDirectoryCreateOptions(),
                null, null)
            .getStatusCode();

        int secondResponseCode = primaryDirectoryClient.createSubdirectoryIfNotExistsWithResponse(
                subdirectoryName,
                new ShareDirectoryCreateOptions(),
                null, null)
            .getStatusCode();

        assertEquals(201, initialResponseCode);
        assertEquals(409, secondResponseCode);
    }

    @Test
    public void createIfNotExistsSubDirectoryInvalidName() {
        primaryDirectoryClient.create();
        ShareStorageException e = assertThrows(ShareStorageException.class,
            () -> primaryDirectoryClient.createSubdirectoryIfNotExists("test/subdirectory"));
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.PARENT_NOT_FOUND);
    }

    @Test
    public void createIfNotExistsSubDirectoryMetadata() {
        primaryDirectoryClient.create();
        FileShareTestHelper.assertResponseStatusCode(primaryDirectoryClient.createSubdirectoryIfNotExistsWithResponse(
            "testCreateSubDirectory", new ShareDirectoryCreateOptions().setMetadata(testMetadata), null, null), 201);
    }

    @Test
    public void createIfNotExistsSubDirectoryMetadataError() {
        primaryDirectoryClient.create();
        ShareStorageException e = assertThrows(ShareStorageException.class,
            () -> primaryDirectoryClient.createSubdirectoryIfNotExistsWithResponse(
                "testsubdirectory",
                new ShareDirectoryCreateOptions()
                    .setMetadata(Collections.singletonMap("", "value")),
                null,
                null));
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.EMPTY_METADATA_KEY);
    }

    @Test
    public void createIfNotExistsSubDirectoryFilePermission() {
        primaryDirectoryClient.create();
        FileShareTestHelper.assertResponseStatusCode(primaryDirectoryClient.createSubdirectoryIfNotExistsWithResponse(
            "testCreateSubDirectory", new ShareDirectoryCreateOptions().setFilePermission(FILE_PERMISSION), null, null),
            201);
    }

    @Test
    public void testCreateIfNotExistsSubDirectoryFilePermissionKey() {
        primaryDirectoryClient.create();
        String filePermissionKey = shareClient.createPermission(FILE_PERMISSION);
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setFilePermissionKey(filePermissionKey);

        FileShareTestHelper.assertResponseStatusCode(primaryDirectoryClient.createSubdirectoryIfNotExistsWithResponse(
            "testCreateSubDirectory", new ShareDirectoryCreateOptions().setSmbProperties(smbProperties), null, null),
            201);
    }

    @Test
    public void testDeleteSubDirectory() {
        // Test implementation
        String subDirectoryName = "testSubCreateDirectory";
        primaryDirectoryClient.create();
        primaryDirectoryClient.createSubdirectory(subDirectoryName);

        FileShareTestHelper.assertResponseStatusCode(primaryDirectoryClient.deleteSubdirectoryWithResponse(
            subDirectoryName, null, null), 202);
    }

    @Test
    public void deleteSubDirectoryError() {
        primaryDirectoryClient.create();
        ShareStorageException e = assertThrows(ShareStorageException.class,
            () -> primaryDirectoryClient.deleteSubdirectory("testsubdirectory"));
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    public void deleteIfExistsSubDirectory() {
        String subDirectoryName = "testSubCreateDirectory";
        primaryDirectoryClient.create();
        primaryDirectoryClient.createSubdirectory(subDirectoryName);

        FileShareTestHelper.assertResponseStatusCode(primaryDirectoryClient
            .deleteSubdirectoryIfExistsWithResponse(subDirectoryName, null, null), 202);
    }

    @Test
    public void deleteIfExistsSubDirectoryMin() {
        String subDirectoryName = "testSubCreateDirectory";
        primaryDirectoryClient.create();
        primaryDirectoryClient.createSubdirectory(subDirectoryName);
        assertTrue(primaryDirectoryClient.deleteSubdirectoryIfExists(subDirectoryName));
    }

    @Test
    public void deleteIfExistsSubDirectoryThatDoesNotExist() {
        primaryDirectoryClient.create();
        Response<Boolean> response = primaryDirectoryClient.deleteSubdirectoryIfExistsWithResponse("testsubdirectory",
            null, null);

        assertEquals(404, response.getStatusCode());
        assertFalse(response.getValue());
    }

    @Test
    public void createFile() {
        primaryDirectoryClient.create();
        FileShareTestHelper.assertResponseStatusCode(
            primaryDirectoryClient.createFileWithResponse("testCreateFile", 1024, null, null, null, null, null, null),
            201);
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#createFileInvalidArgsSupplier")
    public void createFileInvalidArgs(String fileName, long maxSize, int statusCode, ShareErrorCode errMsg) {
        primaryDirectoryClient.create();
        ShareStorageException e = assertThrows(ShareStorageException.class,
            () -> primaryDirectoryClient.createFileWithResponse(fileName, maxSize, null, null, null, null, null, null));
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, statusCode, errMsg);
    }

    @Test
    public void createFileMaxOverload() {
        primaryDirectoryClient.create();
        ShareFileHttpHeaders httpHeaders = new ShareFileHttpHeaders().setContentType("txt");
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now());

        FileShareTestHelper.assertResponseStatusCode(
            primaryDirectoryClient.createFileWithResponse("testCreateFile", 1024, httpHeaders, smbProperties,
                FILE_PERMISSION, testMetadata, null, null), 201);
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#createFileMaxOverloadInvalidArgsSupplier")
    public void createFileMaxOverloadInvalidArgs(String fileName, long maxSize, ShareFileHttpHeaders httpHeaders,
        Map<String, String> metadata, ShareErrorCode errMsg) {
        primaryDirectoryClient.create();
        ShareStorageException e = assertThrows(ShareStorageException.class,
            () -> primaryDirectoryClient.createFileWithResponse(fileName, maxSize, httpHeaders, null, null, metadata,
                null, null));

        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 400, errMsg);
    }

    @Test
    public void deleteFile() {
        String fileName = "testCreateFile";
        primaryDirectoryClient.create();
        primaryDirectoryClient.createFile(fileName, 1024);

        FileShareTestHelper.assertResponseStatusCode(
            primaryDirectoryClient.deleteFileWithResponse(fileName, null, null), 202);
    }

    @Test
    public void deleteFileError() {
        primaryDirectoryClient.create();

        ShareStorageException e = assertThrows(ShareStorageException.class,
            () -> primaryDirectoryClient.deleteFileWithResponse("testfile", null, null));
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    public void deleteIfExistsFileMin() {
        String fileName = "testCreateFile";
        primaryDirectoryClient.create();
        primaryDirectoryClient.createFile(fileName, 1024);

        assertTrue(primaryDirectoryClient.deleteFileIfExists(fileName));
    }

    @Test
    public void deleteIfExistsFile() {
        String fileName = "testCreateFile";
        primaryDirectoryClient.create();
        primaryDirectoryClient.createFile(fileName, 1024);

        FileShareTestHelper.assertResponseStatusCode(primaryDirectoryClient.deleteFileIfExistsWithResponse(fileName,
            null, null), 202);
    }

    @Test
    public void deleteIfExistsFileThatDoesNotExist() {
        primaryDirectoryClient.create();
        Response<Boolean> response = primaryDirectoryClient.deleteFileIfExistsWithResponse("testfile", null, null);

        assertEquals(404, response.getStatusCode());
        assertFalse(response.getValue());
    }

    @Test
    public void getSnapshotId() {
        String snapshot = OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 1, 1), ZoneOffset.UTC).toString();
        ShareDirectoryClient shareSnapshotClient = directoryBuilderHelper(shareName, directoryPath).snapshot(snapshot)
            .buildDirectoryClient();
        assertEquals(snapshot, shareSnapshotClient.getShareSnapshotId());
    }

    @Test
    public void getShareName() {
        assertEquals(shareName, primaryDirectoryClient.getShareName());
    }

    @Test
    public void getDirectoryPath() {
        assertEquals(directoryPath, primaryDirectoryClient.getDirectoryPath());
    }

    @Test
    public void testPerCallPolicy() {
        primaryDirectoryClient.create();

        ShareDirectoryClient directoryClient = directoryBuilderHelper(primaryDirectoryClient.getShareName(),
            primaryDirectoryClient.getDirectoryPath())
            .addPolicy(getPerCallVersionPolicy()).buildDirectoryClient();
        Response<ShareDirectoryProperties> response = directoryClient.getPropertiesWithResponse(null, null);

        assertDoesNotThrow(() -> response.getHeaders().getValue("x-ms-version").equals("2017-11-09"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void rootDirectorySupport(String rootDirPath) {
        // share:/dir1/dir2
        String dir1Name = "dir1";
        String dir2Name = "dir2";
        shareClient.createDirectory(dir1Name).createSubdirectory(dir2Name);
        ShareDirectoryClient rootDirectory = shareClient.getDirectoryClient(rootDirPath);

        assertTrue(rootDirectory.exists());
        assertTrue(rootDirectory.getSubdirectoryClient(dir1Name).exists());
    }

    @Test
    public void createShareWithSmallTimeoutsFailForServiceClient() {
        int maxRetries = 5;
        long retryDelayMillis = 1000;

        for (int i = 0; i < maxRetries; i++) {
            try {
                HttpClientOptions clientOptions = new HttpClientOptions()
                    .setApplicationId("client-options-id")
                    .setResponseTimeout(Duration.ofNanos(1))
                    .setReadTimeout(Duration.ofNanos(1))
                    .setWriteTimeout(Duration.ofNanos(1))
                    .setConnectTimeout(Duration.ofNanos(1));

                ShareServiceClientBuilder clientBuilder = new ShareServiceClientBuilder()
                    .endpoint(ENVIRONMENT.getPrimaryAccount().getBlobEndpoint())
                    .credential(ENVIRONMENT.getPrimaryAccount().getCredential())
                    .retryOptions(new RequestRetryOptions(null, 1, (Integer) null, null, null, null))
                    .clientOptions(clientOptions);

                ShareServiceClient serviceClient = clientBuilder.buildClient();
                assertThrows(RuntimeException.class, () -> serviceClient.createShareWithResponse(generateShareName(),
                    null, Duration.ofSeconds(10), null));
                // If the method above doesn't throw an exception, the test passes
                return;
            } catch (Exception e) {
                // Test failed; wait before retrying
                try {
                    Thread.sleep(retryDelayMillis);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Test
    public void defaultAudience() {
        String dirName = generatePathName();
        ShareDirectoryClient dirClient = directoryBuilderHelper(shareName, dirName).buildDirectoryClient();
        dirClient.create();
        ShareServiceClient oAuthServiceClient =
            getOAuthServiceClient(new ShareServiceClientBuilder()
                .shareTokenIntent(ShareTokenIntent.BACKUP)
                .audience(null) /* should default to "https://storage.azure.com/" */);

        ShareDirectoryClient aadDirClient = oAuthServiceClient.getShareClient(shareName).getDirectoryClient(dirName);
        assertTrue(aadDirClient.exists());
    }

    @Test
    public void storageAccountAudience() {
        String dirName = generatePathName();
        ShareDirectoryClient dirClient = directoryBuilderHelper(shareName, dirName).buildDirectoryClient();
        dirClient.create();
        ShareServiceClient oAuthServiceClient =
            getOAuthServiceClient(new ShareServiceClientBuilder()
                .shareTokenIntent(ShareTokenIntent.BACKUP)
                .audience(ShareAudience.createShareServiceAccountAudience(primaryDirectoryClient.getAccountName())));

        ShareDirectoryClient aadDirClient = oAuthServiceClient.getShareClient(shareName).getDirectoryClient(dirName);
        assertTrue(aadDirClient.exists());
    }

    @Test
    public void audienceError() {
        String dirName = generatePathName();
        ShareDirectoryClient dirClient = directoryBuilderHelper(shareName, dirName).buildDirectoryClient();
        dirClient.create();
        ShareServiceClient oAuthServiceClient =
            getOAuthServiceClient(new ShareServiceClientBuilder()
                .shareTokenIntent(ShareTokenIntent.BACKUP)
                .audience(ShareAudience.createShareServiceAccountAudience("badAudience")));

        ShareDirectoryClient aadDirClient = oAuthServiceClient.getShareClient(shareName).getDirectoryClient(dirName);
        ShareStorageException e = assertThrows(ShareStorageException.class, aadDirClient::exists);
        assertEquals(ShareErrorCode.AUTHENTICATION_FAILED, e.getErrorCode());
    }

    @Test
    public void audienceFromString() {
        String url = String.format("https://%s.file.core.windows.net/", primaryDirectoryClient.getAccountName());
        ShareAudience audience = ShareAudience.fromString(url);

        String dirName = generatePathName();
        ShareDirectoryClient dirClient = directoryBuilderHelper(shareName, dirName).buildDirectoryClient();
        dirClient.create();
        ShareServiceClient oAuthServiceClient =
            getOAuthServiceClient(new ShareServiceClientBuilder()
                .shareTokenIntent(ShareTokenIntent.BACKUP)
                .audience(audience));

        ShareDirectoryClient aadDirClient = oAuthServiceClient.getShareClient(shareName).getDirectoryClient(dirName);
        assertTrue(aadDirClient.exists());
    }
}
