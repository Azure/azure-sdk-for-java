// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.http.rest.Response;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import com.azure.storage.file.share.models.FilePermissionFormat;
import com.azure.storage.file.share.models.NtfsFileAttributes;
import com.azure.storage.file.share.models.ShareAudience;
import com.azure.storage.file.share.models.ShareDirectoryInfo;
import com.azure.storage.file.share.models.ShareDirectoryProperties;
import com.azure.storage.file.share.models.ShareErrorCode;
import com.azure.storage.file.share.models.ShareFileHttpHeaders;
import com.azure.storage.file.share.models.ShareFileItem;
import com.azure.storage.file.share.models.ShareFilePermission;
import com.azure.storage.file.share.models.ShareRequestConditions;
import com.azure.storage.file.share.models.ShareStorageException;
import com.azure.storage.file.share.models.ShareTokenIntent;
import com.azure.storage.file.share.options.ShareDirectoryCreateOptions;
import com.azure.storage.file.share.options.ShareDirectorySetPropertiesOptions;
import com.azure.storage.file.share.options.ShareFileRenameOptions;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DirectoryAsyncApiTests extends FileShareTestBase {

    private ShareDirectoryAsyncClient primaryDirectoryAsyncClient;
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
        primaryDirectoryAsyncClient = directoryBuilderHelper(shareName, directoryPath).buildDirectoryAsyncClient();
        testMetadata = Collections.singletonMap("testmetadata", "value");
        smbProperties = new FileSmbProperties().setNtfsFileAttributes(EnumSet.<NtfsFileAttributes>of(NtfsFileAttributes.NORMAL));
    }

    @Test
    public void getDirectoryURL() {
        String accountName = StorageSharedKeyCredential.fromConnectionString(ENVIRONMENT.getPrimaryAccount()
            .getConnectionString()).getAccountName();
        String expectURL = String.format("https://%s.file.core.windows.net/%s/%s", accountName, shareName,
            directoryPath);
        String directoryURL = primaryDirectoryAsyncClient.getDirectoryUrl();
        assertEquals(expectURL, directoryURL);
    }

    @Test
    public void getSubDirectoryClient() {
        ShareDirectoryAsyncClient subDirectoryClient =
            primaryDirectoryAsyncClient.getSubdirectoryClient("testSubDirectory");
        assertInstanceOf(ShareDirectoryAsyncClient.class, subDirectoryClient);
    }

    @Test
    public void getFileClient() {
        ShareFileAsyncClient fileClient = primaryDirectoryAsyncClient.getFileClient("testFile");
        assertInstanceOf(ShareFileAsyncClient.class, fileClient);
    }

    @Test
    public void createDirectory() {
        StepVerifier.create(primaryDirectoryAsyncClient.createWithResponse(null, null, null))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201)).verifyComplete();
    }

    @Test
    public void createDirectoryError() {
        String testShareName = generateShareName();
        StepVerifier.create(directoryBuilderHelper(testShareName, directoryPath).buildDirectoryAsyncClient().create())
            .verifyErrorSatisfies(it ->
            FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.SHARE_NOT_FOUND));
    }

    @Test
    public void createDirectoryWithMetadata() {
        StepVerifier.create(primaryDirectoryAsyncClient.createWithResponse(null, null, testMetadata))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201)).verifyComplete();
    }

    @Test
    public void createDirectoryWithFilePermission() {
        StepVerifier.create(primaryDirectoryAsyncClient.createWithResponse(null, FILE_PERMISSION, testMetadata))
            .assertNext(it -> {
                FileShareTestHelper.assertResponseStatusCode(it, 201);
                assertNotNull(it.getValue().getSmbProperties());
                assertNotNull(it.getValue().getSmbProperties().getFilePermissionKey());
                assertNotNull(it.getValue().getSmbProperties().getNtfsFileAttributes());
                assertNotNull(it.getValue().getSmbProperties().getFileLastWriteTime());
                assertNotNull(it.getValue().getSmbProperties().getFileCreationTime());
                assertNotNull(it.getValue().getSmbProperties().getFileChangeTime());
                assertNotNull(it.getValue().getSmbProperties().getParentId());
                assertNotNull(it.getValue().getSmbProperties().getFileId());
            }).verifyComplete();
    }

    @Test
    public void createDirectoryWithFilePermKey() {
        String filePermissionKey = shareClient.createPermission(FILE_PERMISSION);
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setFilePermissionKey(filePermissionKey);

        StepVerifier.create(primaryDirectoryAsyncClient.createWithResponse(smbProperties, null, null))
            .assertNext(it -> {
                FileShareTestHelper.assertResponseStatusCode(it, 201);
                assertNotNull(it.getValue().getSmbProperties());
                assertNotNull(it.getValue().getSmbProperties().getFilePermissionKey());
                assertNotNull(it.getValue().getSmbProperties().getNtfsFileAttributes());
                assertNotNull(it.getValue().getSmbProperties().getFileLastWriteTime());
                assertNotNull(it.getValue().getSmbProperties().getFileCreationTime());
                assertNotNull(it.getValue().getSmbProperties().getFileChangeTime());
                assertNotNull(it.getValue().getSmbProperties().getParentId());
                assertNotNull(it.getValue().getSmbProperties().getFileId());
            }).verifyComplete();
    }

    @Test
    public void createIfNotExistsDirectory() {
        StepVerifier.create(primaryDirectoryAsyncClient.createIfNotExistsWithResponse(
            new ShareDirectoryCreateOptions())).assertNext(it ->
            FileShareTestHelper.assertResponseStatusCode(it, 201)).verifyComplete();
    }

    @Test
    public void createIfNotExistsDirectoryError() {
        String testShareName = generateShareName();
        StepVerifier.create(directoryBuilderHelper(testShareName, directoryPath)
            .buildDirectoryAsyncClient().createIfNotExists())
            .verifyErrorSatisfies(it ->
                FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.SHARE_NOT_FOUND));
    }

    @Test
    public void createIfNotExistsDirectoryThatAlreadyExists() {
        ShareDirectoryAsyncClient client = primaryDirectoryAsyncClient.getDirectoryAsyncClient(generatePathName());
        Response<ShareDirectoryInfo> initialResponse =
            client.createIfNotExistsWithResponse(new ShareDirectoryCreateOptions()).block();

        Response<ShareDirectoryInfo> secondResponse =
            client.createIfNotExistsWithResponse(new ShareDirectoryCreateOptions()).block();

        assertNotNull(initialResponse);
        assertNotNull(secondResponse);
        FileShareTestHelper.assertResponseStatusCode(initialResponse, 201);
        FileShareTestHelper.assertResponseStatusCode(secondResponse, 409);
    }

    @Test
    public void createIfNotExistsDirectoryWithMetadata() {
        StepVerifier.create(primaryDirectoryAsyncClient.createIfNotExistsWithResponse(new ShareDirectoryCreateOptions()
                .setMetadata(testMetadata)))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201)).verifyComplete();
    }

    @Test
    public void createIfNotExistsDirectoryWithFilePermission() {
        StepVerifier.create(primaryDirectoryAsyncClient.createIfNotExistsWithResponse(new ShareDirectoryCreateOptions()
            .setFilePermission(FILE_PERMISSION).setMetadata(testMetadata)))
            .assertNext(it -> {
                FileShareTestHelper.assertResponseStatusCode(it, 201);
                assertNotNull(it.getValue().getSmbProperties());
                assertNotNull(it.getValue().getSmbProperties().getFilePermissionKey());
                assertNotNull(it.getValue().getSmbProperties().getNtfsFileAttributes());
                assertNotNull(it.getValue().getSmbProperties().getFileLastWriteTime());
                assertNotNull(it.getValue().getSmbProperties().getFileCreationTime());
                assertNotNull(it.getValue().getSmbProperties().getFileChangeTime());
                assertNotNull(it.getValue().getSmbProperties().getParentId());
                assertNotNull(it.getValue().getSmbProperties().getFileId());
            }).verifyComplete();
    }

    @Test
    public void createIfNotExistsDirectoryWithFilePermKey() {
        String filePermissionKey = shareClient.createPermission(FILE_PERMISSION);
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setFilePermissionKey(filePermissionKey);
        StepVerifier.create(primaryDirectoryAsyncClient.createIfNotExistsWithResponse(new ShareDirectoryCreateOptions()
            .setSmbProperties(smbProperties)))
            .assertNext(it -> {
                FileShareTestHelper.assertResponseStatusCode(it, 201);
                assertNotNull(it.getValue().getSmbProperties());
                assertNotNull(it.getValue().getSmbProperties().getFilePermissionKey());
                assertNotNull(it.getValue().getSmbProperties().getNtfsFileAttributes());
                assertNotNull(it.getValue().getSmbProperties().getFileLastWriteTime());
                assertNotNull(it.getValue().getSmbProperties().getFileCreationTime());
                assertNotNull(it.getValue().getSmbProperties().getFileChangeTime());
                assertNotNull(it.getValue().getSmbProperties().getParentId());
                assertNotNull(it.getValue().getSmbProperties().getFileId());
            }).verifyComplete();
    }

    @Test
    public void deleteDirectory() {
        primaryDirectoryAsyncClient.create().block();
        StepVerifier.create(primaryDirectoryAsyncClient.deleteWithResponse()).assertNext(it ->
            FileShareTestHelper.assertResponseStatusCode(it, 202)).verifyComplete();
    }

    @Test
    public void deleteDirectoryError() {
        StepVerifier.create(primaryDirectoryAsyncClient.delete())
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404,
                ShareErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    public void deleteIfExistsDirectoryMin() {
        primaryDirectoryAsyncClient.create().block();
        assertEquals(Boolean.TRUE, primaryDirectoryAsyncClient.deleteIfExists().block());
    }

    @Test
    public void deleteIfExistsDirectory() {
        primaryDirectoryAsyncClient.create().block();
        StepVerifier.create(primaryDirectoryAsyncClient.deleteIfExistsWithResponse())
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 202)).verifyComplete();
    }

    @Test
    public void deleteIfExistsDirectoryThatDoesNotExist() {
        ShareDirectoryAsyncClient client = primaryDirectoryAsyncClient.getDirectoryAsyncClient(generatePathName());

        Response<Boolean> response = client.deleteIfExistsWithResponse(null).block();
        assertNotNull(response);
        assertFalse(response.getValue());
        FileShareTestHelper.assertResponseStatusCode(response, 404);
        Boolean exists = client.exists().block();
        assertTrue(exists != null && !exists);
    }

    @Test
    public void deleteIfExistsDirectoryThatWasAlreadyDeleted() {
        primaryDirectoryAsyncClient.create().block();

        Response<Boolean> initialResponse = primaryDirectoryAsyncClient.deleteIfExistsWithResponse().block();
        Response<Boolean> secondResponse = primaryDirectoryAsyncClient.deleteIfExistsWithResponse().block();

        assertNotNull(initialResponse);
        assertNotNull(secondResponse);
        FileShareTestHelper.assertResponseStatusCode(initialResponse, 202);
        FileShareTestHelper.assertResponseStatusCode(secondResponse, 404);
        assertTrue(initialResponse.getValue());
        assertFalse(secondResponse.getValue());
    }

    @Test
    public void getProperties() {
        primaryDirectoryAsyncClient.create().block();
        StepVerifier.create(primaryDirectoryAsyncClient.getPropertiesWithResponse())
            .assertNext(it -> {
                FileShareTestHelper.assertResponseStatusCode(it, 200);
                assertNotNull(it.getValue().getSmbProperties());
                assertNotNull(it.getValue().getSmbProperties().getFilePermissionKey());
                assertNotNull(it.getValue().getSmbProperties().getNtfsFileAttributes());
                assertNotNull(it.getValue().getSmbProperties().getFileLastWriteTime());
                assertNotNull(it.getValue().getSmbProperties().getFileCreationTime());
                assertNotNull(it.getValue().getSmbProperties().getFileChangeTime());
                assertNotNull(it.getValue().getSmbProperties().getParentId());
                assertNotNull(it.getValue().getSmbProperties().getFileId());
            }).verifyComplete();
    }

    @Test
    public void getPropertiesError() {
        StepVerifier.create(primaryDirectoryAsyncClient.getPropertiesWithResponse()).verifyErrorSatisfies(it ->
            FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    public void setPropertiesFilePermission() {
        primaryDirectoryAsyncClient.createWithResponse(null, null, null).block();
        StepVerifier.create(primaryDirectoryAsyncClient.setPropertiesWithResponse(null, FILE_PERMISSION))
            .assertNext(it -> {
                FileShareTestHelper.assertResponseStatusCode(it, 200);
                assertNotNull(it.getValue().getSmbProperties());
                assertNotNull(it.getValue().getSmbProperties().getFilePermissionKey());
                assertNotNull(it.getValue().getSmbProperties().getNtfsFileAttributes());
                assertNotNull(it.getValue().getSmbProperties().getFileLastWriteTime());
                assertNotNull(it.getValue().getSmbProperties().getFileCreationTime());
                assertNotNull(it.getValue().getSmbProperties().getFileChangeTime());
                assertNotNull(it.getValue().getSmbProperties().getParentId());
                assertNotNull(it.getValue().getSmbProperties().getFileId());
            }).verifyComplete();
    }

    private static Stream<Arguments> setDirectoryHttpHeadersFilePermissionFormatSupplier() {
        return Stream.of(
            Arguments.of(FilePermissionFormat.SDDL),
            Arguments.of(FilePermissionFormat.BINARY),
            Arguments.of((Object) null));
    }
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @ParameterizedTest
    @MethodSource("setDirectoryHttpHeadersFilePermissionFormatSupplier")
    public void setDirectoryHttpHeadersFilePermissionFormat(FilePermissionFormat filePermissionFormat) {
        String permission = FileShareTestHelper.getPermissionFromFormat(filePermissionFormat);

        ShareDirectorySetPropertiesOptions options = new ShareDirectorySetPropertiesOptions()
            .setFilePermissions(new ShareFilePermission().setPermission(permission).setPermissionFormat(filePermissionFormat));

        Mono<Response<ShareDirectoryInfo>> bagResponse = primaryDirectoryAsyncClient.create()
            .then(primaryDirectoryAsyncClient.setPropertiesWithResponse(options));

        Mono<Response<ShareDirectoryInfo>> nonBagResponse = primaryDirectoryAsyncClient.setPropertiesWithResponse(null,
            permission, filePermissionFormat);

        StepVerifier.create(bagResponse)
            .assertNext(r -> {
                FileShareTestHelper.assertResponseStatusCode(r, 200);
                assertNotNull(r.getValue().getSmbProperties().getFilePermissionKey());
            })
            .verifyComplete();

        StepVerifier.create(nonBagResponse)
            .assertNext(r -> {
                FileShareTestHelper.assertResponseStatusCode(r, 200);
                assertNotNull(r.getValue().getSmbProperties().getFilePermissionKey());
            })
            .verifyComplete();
    }

    @Test
    public void setPropertiesFilePermissionKey() {
        String filePermissionKey = shareClient.createPermission(FILE_PERMISSION);
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setFilePermissionKey(filePermissionKey);
        primaryDirectoryAsyncClient.createWithResponse(null, null, null).block();
        StepVerifier.create(primaryDirectoryAsyncClient.setPropertiesWithResponse(smbProperties, null))
            .assertNext(it -> {
                FileShareTestHelper.assertResponseStatusCode(it, 200);
                assertNotNull(it.getValue().getSmbProperties());
                assertNotNull(it.getValue().getSmbProperties().getFilePermissionKey());
                assertNotNull(it.getValue().getSmbProperties().getNtfsFileAttributes());
                assertNotNull(it.getValue().getSmbProperties().getFileLastWriteTime());
                assertNotNull(it.getValue().getSmbProperties().getFileCreationTime());
                assertNotNull(it.getValue().getSmbProperties().getFileChangeTime());
                assertNotNull(it.getValue().getSmbProperties().getParentId());
                assertNotNull(it.getValue().getSmbProperties().getFileId());
            }).verifyComplete();
    }

    @Test
    public void setPropertiesError() {
        primaryDirectoryAsyncClient.createWithResponse(null, null, null).block();
        FileSmbProperties properties = new FileSmbProperties().setFilePermissionKey("filePermissionKey");
        StepVerifier.create(primaryDirectoryAsyncClient.setProperties(properties, FILE_PERMISSION))
            .verifyErrorSatisfies(it -> assertInstanceOf(IllegalArgumentException.class, it));

        StepVerifier.create(primaryDirectoryAsyncClient.setProperties(null,
            new String(FileShareTestHelper.getRandomBuffer(9 * Constants.KB))))
            .verifyErrorSatisfies(it -> assertInstanceOf(IllegalArgumentException.class, it));
    }

    public void setMetadata() {
        primaryDirectoryAsyncClient.createWithResponse(null, null, testMetadata).block();
        Map<String, String> updatedMetadata = Collections.singletonMap("update", "value");
        StepVerifier.create(primaryDirectoryAsyncClient.getPropertiesWithResponse())
            .assertNext(it -> {
                boolean allEqual = it.getValue().getMetadata().entrySet().stream()
                    .allMatch(e -> e.getValue().equals(testMetadata.get(e.getKey())));
                assertTrue(allEqual);
            }).verifyComplete();


        StepVerifier.create(primaryDirectoryAsyncClient.setMetadataWithResponse(updatedMetadata))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 200)).verifyComplete();
        StepVerifier.create(primaryDirectoryAsyncClient.getPropertiesWithResponse()).assertNext(it -> {
            boolean allEqual = it.getValue().getMetadata().entrySet().stream()
                .allMatch(e -> e.getValue().equals(updatedMetadata.get(e.getKey())));
            assertTrue(allEqual);
        }).verifyComplete();
    }

    @Test
    public void setMetadataError() {
        primaryDirectoryAsyncClient.create().block();
        Map<String, String> errorMetadata = Collections.singletonMap("", "value");
        StepVerifier.create(primaryDirectoryAsyncClient.setMetadata(errorMetadata))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 400,
                ShareErrorCode.EMPTY_METADATA_KEY));
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#listFilesAndDirectoriesSupplier")
    public void listFilesAndDirectories(String[] expectedFiles, String[] expectedDirectories) {
        primaryDirectoryAsyncClient.create().block();
        for (String expectedFile : expectedFiles) {
            primaryDirectoryAsyncClient.createFile(expectedFile, 2).block();
        }

        for (String expectedDirectory : expectedDirectories) {
            primaryDirectoryAsyncClient.createSubdirectory(expectedDirectory).block();
        }

        List<String> foundFiles = new ArrayList<>();
        List<String> foundDirectories = new ArrayList<>();
        for (ShareFileItem fileRef : primaryDirectoryAsyncClient.listFilesAndDirectories().toIterable()) {
            if (fileRef.isDirectory()) {
                foundDirectories.add(fileRef.getName());
            } else {
                foundFiles.add(fileRef.getName());
            }
        }

        assertArrayEquals(expectedFiles, foundFiles.toArray());
        assertArrayEquals(expectedDirectories, foundDirectories.toArray());
    }

    /**
     * The listing hierarchy:
     * share -> dir -> listOp0 (dir) -> listOp3 (file)
     *                               -> listOp4 (file)
     *              -> listOp1 (dir) -> listOp5 (file)
     *                               -> listOp6 (file)
     *              -> listOp2 (file)
     */
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2020-10-02")
    @ParameterizedTest
    @MethodSource("listFilesAndDirectoriesArgsSupplier")
    public void listFilesAndDirectoriesArgs(String extraPrefix, Integer maxResults, int numOfResults) {
        primaryDirectoryAsyncClient.create().block();
        Queue<String> nameList = new LinkedList<>();
        String dirPrefix = generatePathName();
        for (int i = 0; i < 2; i++) {
            ShareDirectoryAsyncClient subDirClient = primaryDirectoryAsyncClient
                .getSubdirectoryClient(dirPrefix + i);
            subDirClient.create().block();
            for (int j = 0; j < 2; j++) {
                int num = i * 2 + j + 3;
                subDirClient.createFile(dirPrefix + num, 1024).block();
            }
        }
        primaryDirectoryAsyncClient.createFile(dirPrefix + 2, 1024).block();
        for (int i = 0; i < 3; i++) {
            nameList.add(dirPrefix + i);
        }
        StepVerifier.create(primaryDirectoryAsyncClient.listFilesAndDirectories(
            prefix + extraPrefix, maxResults))
            .thenConsumeWhile(it -> Objects.equals(it.getName(), nameList.remove())).verifyComplete();

        for (int i = 0; i < 3 - numOfResults; i++) {
            nameList.remove();
        }
        assertTrue(nameList.isEmpty());
    }

    private static Stream<Arguments> listFilesAndDirectoriesArgsSupplier() {
        return Stream.of(
            Arguments.of("", null, 3),
            Arguments.of("", 1, 3),
            Arguments.of("noops", 3, 0));
    }

    @ParameterizedTest
    @MethodSource("listHandlesSupplier")
    public void listHandles(Integer maxResults, boolean recursive) {
        primaryDirectoryAsyncClient.create().block();
        StepVerifier.create(primaryDirectoryAsyncClient.listHandles(maxResults, recursive)).verifyComplete();
    }

    private static Stream<Arguments> listHandlesSupplier() {
        return Stream.of(
            Arguments.of(2, true),
            Arguments.of(null, false));
    }

    @Test
    public void listHandlesError() {
        StepVerifier.create(primaryDirectoryAsyncClient.listHandles(null, true))
            .verifyErrorSatisfies(it ->
                FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.RESOURCE_NOT_FOUND));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2019-07-07")
    @Test
    public void forceCloseHandleMin() {
        primaryDirectoryAsyncClient.create().block();
        StepVerifier.create(primaryDirectoryAsyncClient.forceCloseHandle("1"))
            .assertNext(it -> {
                assertEquals(it.getClosedHandles(), 0);
                assertEquals(it.getFailedHandles(), 0);
            }).verifyComplete();
    }

    @Test
    public void forceCloseHandleInvalidHandleID() {
        primaryDirectoryAsyncClient.create().block();

        StepVerifier.create(primaryDirectoryAsyncClient.forceCloseHandle("invalidHandleId"))
            .verifyErrorSatisfies(it -> assertInstanceOf(ShareStorageException.class, it));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2019-07-07")
    @Test
    public void forceCloseAllHandlesMin() {
        primaryDirectoryAsyncClient.create().block();

        StepVerifier.create(primaryDirectoryAsyncClient.forceCloseAllHandles(false))
            .assertNext(it -> {
                assertEquals(it.getClosedHandles(), 0);
                assertEquals(it.getFailedHandles(), 0);
            }).verifyComplete();
    }

    @Test
    public void createSubDirectory() {
        primaryDirectoryAsyncClient.create().block();
        StepVerifier.create(primaryDirectoryAsyncClient
            .createSubdirectoryWithResponse("testCreateSubDirectory", null, null, null))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201)).verifyComplete();
    }

    @Test
    public void createSubDirectoryInvalidName() {
        primaryDirectoryAsyncClient.create().block();
        StepVerifier.create(primaryDirectoryAsyncClient.createSubdirectory("test/subdirectory"))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404,
                ShareErrorCode.PARENT_NOT_FOUND));
    }

    @Test
    public void createSubDirectoryMetadata() {
        primaryDirectoryAsyncClient.create().block();
        StepVerifier.create(primaryDirectoryAsyncClient.createSubdirectoryWithResponse("testCreateSubDirectory", null,
                null, testMetadata))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201)).verifyComplete();
    }

    @Test
    public void createSubDirectoryMetadataError() {
        primaryDirectoryAsyncClient.create().block();
        StepVerifier.create(primaryDirectoryAsyncClient.createSubdirectoryWithResponse("testsubdirectory", null, null,
            Collections.singletonMap("", "value"))).verifyErrorSatisfies(
                it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 400,
                    ShareErrorCode.EMPTY_METADATA_KEY));

    }

    @Test
    public void createSubDirectoryFilePermission() {
        primaryDirectoryAsyncClient.create().block();
        StepVerifier.create(primaryDirectoryAsyncClient.createSubdirectoryWithResponse("testCreateSubDirectory", null,
            FILE_PERMISSION, null)).assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();
    }

    @Test
    public void createSubDirectoryFilePermKey() {
        String filePermissionKey = shareClient.createPermission(FILE_PERMISSION);
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setFilePermissionKey(filePermissionKey);
        primaryDirectoryAsyncClient.create().block();
        StepVerifier.create(primaryDirectoryAsyncClient.createSubdirectoryWithResponse("testCreateSubDirectory",
            smbProperties, null, null)).assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsSubDirectory() {
        primaryDirectoryAsyncClient.create().block();
        StepVerifier.create(primaryDirectoryAsyncClient.createSubdirectoryIfNotExistsWithResponse(
            "testCreateSubDirectory", new ShareDirectoryCreateOptions()))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201)).verifyComplete();
    }

    @Test
    public void createIfNotExistsSubDirectoryInvalidName() {
        primaryDirectoryAsyncClient.create().block();
        StepVerifier.create(primaryDirectoryAsyncClient.createSubdirectoryIfNotExists("test/subdirectory"))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404,
                ShareErrorCode.PARENT_NOT_FOUND));
    }

    @Test
    public void createIfNotExistsSubDirectoryThatAlreadyExists() {
        String subdirectoryName = generatePathName();
        ShareDirectoryAsyncClient client = primaryDirectoryAsyncClient.getDirectoryAsyncClient(generatePathName());
        client.create().block();
        Response<ShareDirectoryAsyncClient> initialResponse =
            client.createSubdirectoryIfNotExistsWithResponse(subdirectoryName,
                new ShareDirectoryCreateOptions()).block();

        Response<ShareDirectoryAsyncClient> secondResponse =
            client.createSubdirectoryIfNotExistsWithResponse(subdirectoryName,
                new ShareDirectoryCreateOptions()).block();

        assertNotNull(initialResponse);
        assertNotNull(secondResponse);
        FileShareTestHelper.assertResponseStatusCode(initialResponse, 201);
        FileShareTestHelper.assertResponseStatusCode(secondResponse, 409);
    }

    @Test
    public void createIfNotExistsSubDirectoryMetadata() {
        primaryDirectoryAsyncClient.create().block();
        StepVerifier.create(primaryDirectoryAsyncClient.createSubdirectoryIfNotExistsWithResponse(
            "testCreateSubDirectory", new ShareDirectoryCreateOptions().setMetadata(testMetadata)))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201)).verifyComplete();
    }

    @Test
    public void createIfNotExistsSubDirectoryMetadataError() {
        primaryDirectoryAsyncClient.create().block();
        StepVerifier.create(primaryDirectoryAsyncClient
            .createSubdirectoryIfNotExistsWithResponse("testsubdirectory", new ShareDirectoryCreateOptions()
                .setMetadata(Collections.singletonMap("", "value"))))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 400,
                ShareErrorCode.EMPTY_METADATA_KEY));
    }

    @Test
    public void createIfNotExistsSubDirectoryFilePermission() {
        primaryDirectoryAsyncClient.create().block();
        StepVerifier.create(primaryDirectoryAsyncClient.createSubdirectoryIfNotExistsWithResponse(
            "testCreateSubDirectory", new ShareDirectoryCreateOptions().setFilePermission(FILE_PERMISSION)))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201)).verifyComplete();
    }

    @Test
    public void createIfNotExistsSubDirectoryFilePermKey() {
        String filePermissionKey = shareClient.createPermission(FILE_PERMISSION);
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setFilePermissionKey(filePermissionKey);
        primaryDirectoryAsyncClient.create().block();
        StepVerifier.create(primaryDirectoryAsyncClient.createSubdirectoryIfNotExistsWithResponse(
            "testCreateSubDirectory", new ShareDirectoryCreateOptions().setSmbProperties(smbProperties)))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201)).verifyComplete();
    }

    @Test
    public void deleteSubDirectory() {
        String subDirectoryName = "testSubCreateDirectory";
        primaryDirectoryAsyncClient.create().block();
        primaryDirectoryAsyncClient.createSubdirectory(subDirectoryName).block();
        StepVerifier.create(primaryDirectoryAsyncClient.deleteSubdirectoryWithResponse(subDirectoryName))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 202)).verifyComplete();
    }

    @Test
    public void deleteSubDirectoryError() {
        primaryDirectoryAsyncClient.create().block();
        StepVerifier.create(primaryDirectoryAsyncClient.deleteSubdirectoryWithResponse("testsubdirectory"))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404,
                ShareErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    public void deleteIfExistsSubDirectory() {
        String subDirectoryName = "testSubCreateDirectory";
        primaryDirectoryAsyncClient.create().block();
        primaryDirectoryAsyncClient.createSubdirectory(subDirectoryName).block();
        StepVerifier.create(primaryDirectoryAsyncClient.deleteSubdirectoryIfExistsWithResponse(subDirectoryName))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 202)).verifyComplete();
    }

    @Test
    public void deleteIfExistsSubDirectoryMin() {
        String subDirectoryName = "testSubCreateDirectory";
        primaryDirectoryAsyncClient.create().block();
        primaryDirectoryAsyncClient.createSubdirectory(subDirectoryName).block();
        Boolean result = primaryDirectoryAsyncClient.deleteSubdirectoryIfExists(subDirectoryName).block();
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void deleteIfExistsSubDirectoryThatDoesNotExist() {
        String subdirectoryName = generatePathName();
        ShareDirectoryAsyncClient client = primaryDirectoryAsyncClient.getDirectoryAsyncClient(generatePathName());
        client.create().block();

        Response<Boolean> response = client.deleteSubdirectoryIfExistsWithResponse(subdirectoryName, null).block();
        assertNotNull(response);
        assertFalse(response.getValue());
        FileShareTestHelper.assertResponseStatusCode(response, 404);
        Boolean exists = client.getSubdirectoryClient(subdirectoryName).exists().block();
        assertNotEquals(Boolean.TRUE, exists);
    }

    @Test
    public void createFile() {
        primaryDirectoryAsyncClient.create().block();
        StepVerifier.create(primaryDirectoryAsyncClient.createFileWithResponse("testCreateFile", 1024, null, null, null,
                null)).assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201)).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#createFileInvalidArgsSupplier")
    public void createFileInvalidArgs(String fileName, long maxSize, int statusCode, ShareErrorCode errMsg) {
        primaryDirectoryAsyncClient.create().block();
        StepVerifier.create(primaryDirectoryAsyncClient.createFileWithResponse(fileName, maxSize, null, null, null,
                null)).verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it,
            statusCode, errMsg));
    }

    @Test
    public void createFileMaxOverload() {
        primaryDirectoryAsyncClient.create().block();
        ShareFileHttpHeaders httpHeaders = new ShareFileHttpHeaders()
            .setContentType("txt");
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now());

        StepVerifier.create(primaryDirectoryAsyncClient.createFileWithResponse("testCreateFile", 1024, httpHeaders,
                smbProperties, FILE_PERMISSION, testMetadata))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201)).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#createFileMaxOverloadInvalidArgsSupplier")
    public void createFileMaxOverloadInvalidArgs(String fileName, long maxSize, ShareFileHttpHeaders httpHeaders,
        Map<String, String> metadata, ShareErrorCode errMsg) {
        primaryDirectoryAsyncClient.create().block();
        StepVerifier.create(primaryDirectoryAsyncClient.createFileWithResponse(fileName, maxSize, httpHeaders, null,
            null, metadata)).verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 400,
            errMsg));
    }

    @Test
    public void createFileLease() {
        primaryDirectoryAsyncClient.create().block();
        primaryDirectoryAsyncClient.getFileClient("testCreateFile").create(512).block();
        String leaseId = createLeaseClient(primaryDirectoryAsyncClient.getFileClient("testCreateFile")).acquireLease()
            .block();

        StepVerifier.create(primaryDirectoryAsyncClient.createFileWithResponse("testCreateFile", 1024, null, null, null,
            null, new ShareRequestConditions().setLeaseId(leaseId))).expectNextCount(1).verifyComplete();
    }

    @Test
    public void createFileLeaseFail() {
        primaryDirectoryAsyncClient.create().block();
        primaryDirectoryAsyncClient.getFileClient("testCreateFile").create(512).block();
        createLeaseClient(primaryDirectoryAsyncClient.getFileClient("testCreateFile")).acquireLease().block();

        StepVerifier.create(primaryDirectoryAsyncClient.createFileWithResponse("testCreateFile", 1024, null, null, null,
            null, new ShareRequestConditions().setLeaseId(testResourceNamer.randomUuid())))
            .verifyError(ShareStorageException.class);
    }

    public void deleteFile() {
        String fileName = "testCreateFile";
        primaryDirectoryAsyncClient.create().block();
        primaryDirectoryAsyncClient.createFile(fileName, 1024).block();
        StepVerifier.create(primaryDirectoryAsyncClient.deleteFileWithResponse(fileName))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 202)).verifyComplete();
    }

    @Test
    public void deleteFileLease() {
        String fileName = "testCreateFile";
        primaryDirectoryAsyncClient.create().block();
        primaryDirectoryAsyncClient.createFile(fileName, 1024).block();
        String leaseId = createLeaseClient(primaryDirectoryAsyncClient.getFileClient(fileName)).acquireLease().block();

        StepVerifier.create(primaryDirectoryAsyncClient.deleteFileWithResponse(fileName,
                new ShareRequestConditions().setLeaseId(leaseId))).expectNextCount(1).verifyComplete();
    }

    @Test
    public void deleteFileLeaseFail() {
        String fileName = "testCreateFile";
        primaryDirectoryAsyncClient.create().block();
        primaryDirectoryAsyncClient.createFile(fileName, 1024).block();
        createLeaseClient(primaryDirectoryAsyncClient.getFileClient(fileName)).acquireLease().block();

        StepVerifier.create(primaryDirectoryAsyncClient.deleteFileWithResponse(fileName,
                new ShareRequestConditions().setLeaseId(testResourceNamer.randomUuid())))
            .verifyError(ShareStorageException.class);
    }

    @Test
    public void deleteIfExistsFile() {
        String fileName = "testCreateFile";
        primaryDirectoryAsyncClient.create().block();
        primaryDirectoryAsyncClient.createFile(fileName, 1024).block();
        StepVerifier.create(primaryDirectoryAsyncClient.deleteFileIfExistsWithResponse(fileName))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 202)).verifyComplete();
    }

    @Test
    public void deleteIfExistsFileMin() {
        String fileName = "testCreateFile";
        primaryDirectoryAsyncClient.create().block();
        primaryDirectoryAsyncClient.createFile(fileName, 1024).block();
        Boolean result = primaryDirectoryAsyncClient.deleteFileIfExists(fileName).block();
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void deleteIfExistsFileLease() {
        String fileName = "testCreateFile";
        primaryDirectoryAsyncClient.create().block();
        primaryDirectoryAsyncClient.createFile(fileName, 1024).block();
        String leaseId = createLeaseClient(primaryDirectoryAsyncClient.getFileClient(fileName)).acquireLease().block();

        StepVerifier.create(primaryDirectoryAsyncClient.deleteFileIfExistsWithResponse(fileName,
                new ShareRequestConditions().setLeaseId(leaseId))).expectNextCount(1).verifyComplete();
    }

    @Test
    public void deleteIfExistsFileLeaseFail() {
        String fileName = "testCreateFile";
        primaryDirectoryAsyncClient.create().block();
        primaryDirectoryAsyncClient.createFile(fileName, 1024).block();
        createLeaseClient(primaryDirectoryAsyncClient.getFileClient(fileName)).acquireLease().block();

        StepVerifier.create(primaryDirectoryAsyncClient.deleteFileIfExistsWithResponse(fileName,
            new ShareRequestConditions().setLeaseId(testResourceNamer.randomUuid())))
            .verifyError(ShareStorageException.class);
    }

    @Test
    public void deleteIfExistsFileThatDoesNotExist() {
        String subdirectoryName = generatePathName();
        ShareDirectoryAsyncClient client = primaryDirectoryAsyncClient.getDirectoryAsyncClient(generatePathName());
        client.create().block();

        Response<Boolean> response = client.deleteFileIfExistsWithResponse(subdirectoryName, null).block();
        assertNotNull(response);
        FileShareTestHelper.assertResponseStatusCode(response, 404);
        assertNotEquals(Boolean.TRUE, client.getSubdirectoryClient(subdirectoryName).exists().block());
    }

    @Test
    public void getShareSnapshotId() {
        String snapshot = OffsetDateTime.of(LocalDateTime.of(2000, 1, 1,
            1, 1), ZoneOffset.UTC).toString();
        ShareDirectoryAsyncClient shareSnapshotClient = directoryBuilderHelper(shareName, directoryPath)
            .snapshot(snapshot).buildDirectoryAsyncClient();
        assertEquals(snapshot, shareSnapshotClient.getShareSnapshotId());
    }

    private static Stream<Arguments> renameDirectoryFilePermissionFormatSupplier() {
        return Stream.of(
            Arguments.of(FilePermissionFormat.SDDL),
            Arguments.of(FilePermissionFormat.BINARY),
            Arguments.of((Object) null));
    }
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @ParameterizedTest
    @MethodSource("renameDirectoryFilePermissionFormatSupplier")
    public void renameDirectoryFilePermissionFormat(FilePermissionFormat filePermissionFormat) {
        String permission = FileShareTestHelper.getPermissionFromFormat(filePermissionFormat);

        ShareFileRenameOptions options = new ShareFileRenameOptions(generatePathName()).setFilePermission(permission)
            .setFilePermissionFormat(filePermissionFormat);

        Mono<ShareDirectoryProperties> response = primaryDirectoryAsyncClient.create()
            .then(primaryDirectoryAsyncClient.renameWithResponse(options))
            .flatMap(r -> {
                FileShareTestHelper.assertResponseStatusCode(r, 200);
                return r.getValue().getProperties();
            });

        StepVerifier.create(response)
            .assertNext(r -> {
                assertNotNull(r.getSmbProperties().getFilePermissionKey());
            })
            .verifyComplete();
    }

    @Test
    public void getShareName() {
        assertEquals(shareName, primaryDirectoryAsyncClient.getShareName());
    }

    @Test
    public void getDirectoryPath() {
        assertEquals(directoryPath, primaryDirectoryAsyncClient.getDirectoryPath());
    }

    @Test
    public void defaultAudience() {
        String dirName = generatePathName();
        ShareDirectoryAsyncClient dirClient = directoryBuilderHelper(shareName, dirName).buildDirectoryAsyncClient();
        dirClient.create().block();
        ShareServiceAsyncClient oAuthServiceClient =
            getOAuthServiceAsyncClient(new ShareServiceClientBuilder()
                .shareTokenIntent(ShareTokenIntent.BACKUP)
                .audience(null) /* should default to "https://storage.azure.com/" */);

        ShareDirectoryAsyncClient aadDirClient = oAuthServiceClient.getShareAsyncClient(shareName).getDirectoryClient(dirName);

        StepVerifier.create(aadDirClient.exists())
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    public void storageAccountAudience() {
        String dirName = generatePathName();
        ShareDirectoryAsyncClient dirClient = directoryBuilderHelper(shareName, dirName).buildDirectoryAsyncClient();
        dirClient.create().block();
        ShareServiceAsyncClient oAuthServiceClient =
            getOAuthServiceAsyncClient(new ShareServiceClientBuilder()
                .shareTokenIntent(ShareTokenIntent.BACKUP)
                .audience(ShareAudience.createShareServiceAccountAudience(primaryDirectoryAsyncClient.getAccountName())));

        ShareDirectoryAsyncClient aadDirClient = oAuthServiceClient.getShareAsyncClient(shareName).getDirectoryClient(dirName);

        StepVerifier.create(aadDirClient.exists())
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    public void audienceError() {
        String dirName = generatePathName();
        ShareDirectoryAsyncClient dirClient = directoryBuilderHelper(shareName, dirName).buildDirectoryAsyncClient();
        dirClient.create().block();
        ShareServiceAsyncClient oAuthServiceClient =
            getOAuthServiceAsyncClient(new ShareServiceClientBuilder()
                .shareTokenIntent(ShareTokenIntent.BACKUP)
                .audience(ShareAudience.createShareServiceAccountAudience("badAudience")));

        ShareDirectoryAsyncClient aadDirClient = oAuthServiceClient.getShareAsyncClient(shareName).getDirectoryClient(dirName);

        StepVerifier.create(aadDirClient.exists())
            .verifyErrorSatisfies(r -> {
                ShareStorageException e = assertInstanceOf(ShareStorageException.class, r);
                assertEquals(ShareErrorCode.INVALID_AUTHENTICATION_INFO, e.getErrorCode());
            });
    }

    @Test
    public void audienceFromString() {
        String url = String.format("https://%s.file.core.windows.net/", primaryDirectoryAsyncClient.getAccountName());
        ShareAudience audience = ShareAudience.fromString(url);

        String dirName = generatePathName();
        ShareDirectoryAsyncClient dirClient = directoryBuilderHelper(shareName, dirName).buildDirectoryAsyncClient();
        dirClient.create().block();
        ShareServiceAsyncClient oAuthServiceClient =
            getOAuthServiceAsyncClient(new ShareServiceClientBuilder()
                .shareTokenIntent(ShareTokenIntent.BACKUP)
                .audience(audience));

        ShareDirectoryAsyncClient aadDirClient = oAuthServiceClient.getShareAsyncClient(shareName).getDirectoryClient(dirName);

        StepVerifier.create(aadDirClient.exists())
            .expectNext(true)
            .verifyComplete();
    }
}
