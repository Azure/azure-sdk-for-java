// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.http.rest.Response;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import com.azure.storage.file.share.models.NfsFileType;
import com.azure.storage.file.share.models.FilePermissionFormat;
import com.azure.storage.file.share.models.FilePosixProperties;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
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

import static com.azure.storage.common.implementation.Constants.HeaderConstants.ERROR_CODE_HEADER_NAME;
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
    private static final String FILE_PERMISSION
        = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-188441444-3053964)S:NO_ACCESS_CONTROL";

    @BeforeEach
    public void setup() {
        shareName = generateShareName();
        directoryPath = generatePathName();
        shareClient = shareBuilderHelper(shareName).buildClient();
        shareClient.create();
        primaryDirectoryAsyncClient = directoryBuilderHelper(shareName, directoryPath).buildDirectoryAsyncClient();
        testMetadata = Collections.singletonMap("testmetadata", "value");
        smbProperties = new FileSmbProperties().setNtfsFileAttributes(EnumSet.of(NtfsFileAttributes.NORMAL));
    }

    @Test
    public void getDirectoryURL() {
        String accountName
            = StorageSharedKeyCredential.fromConnectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString())
                .getAccountName();
        String expectURL
            = String.format("https://%s.file.core.windows.net/%s/%s", accountName, shareName, directoryPath);
        String directoryURL = primaryDirectoryAsyncClient.getDirectoryUrl();
        assertEquals(expectURL, directoryURL);
    }

    @Test
    public void getSubDirectoryClient() {
        ShareDirectoryAsyncClient subDirectoryClient
            = primaryDirectoryAsyncClient.getSubdirectoryClient("testSubDirectory");
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
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();
    }

    @Test
    public void createDirectoryError() {
        String testShareName = generateShareName();
        StepVerifier.create(directoryBuilderHelper(testShareName, directoryPath).buildDirectoryAsyncClient().create())
            .verifyErrorSatisfies(
                it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.SHARE_NOT_FOUND));
    }

    @Test
    public void createDirectoryWithMetadata() {
        StepVerifier.create(primaryDirectoryAsyncClient.createWithResponse(null, null, testMetadata))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();
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
            })
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#filePermissionFormatSupplier")
    public void createDirectoryFilePermissionFormat(FilePermissionFormat filePermissionFormat) {
        String permission = FileShareTestHelper.getPermissionFromFormat(filePermissionFormat);
        ShareDirectoryCreateOptions options = new ShareDirectoryCreateOptions().setFilePermission(permission)
            .setFilePermissionFormat(filePermissionFormat);

        StepVerifier.create(primaryDirectoryAsyncClient.createWithResponse(options)).assertNext(r -> {
            FileShareTestHelper.assertResponseStatusCode(r, 201);
            assertNotNull(r.getValue().getSmbProperties().getFilePermissionKey());
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
            })
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsDirectory() {
        StepVerifier
            .create(primaryDirectoryAsyncClient.createIfNotExistsWithResponse(new ShareDirectoryCreateOptions()))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsDirectoryError() {
        String testShareName = generateShareName();
        StepVerifier
            .create(
                directoryBuilderHelper(testShareName, directoryPath).buildDirectoryAsyncClient().createIfNotExists())
            .verifyErrorSatisfies(
                it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.SHARE_NOT_FOUND));
    }

    @Test
    public void createIfNotExistsDirectoryThatAlreadyExists() {
        ShareDirectoryAsyncClient client = primaryDirectoryAsyncClient.getDirectoryAsyncClient(generatePathName());
        Mono<Response<ShareDirectoryInfo>> initialResponseMono
            = client.createIfNotExistsWithResponse(new ShareDirectoryCreateOptions());
        Mono<Response<ShareDirectoryInfo>> secondResponseMono
            = client.createIfNotExistsWithResponse(new ShareDirectoryCreateOptions());

        StepVerifier.create(initialResponseMono).assertNext(initialResponse -> {
            assertNotNull(initialResponse);
            FileShareTestHelper.assertResponseStatusCode(initialResponse, 201);
        }).verifyComplete();

        StepVerifier.create(secondResponseMono).assertNext(secondResponse -> {
            assertNotNull(secondResponse);
            FileShareTestHelper.assertResponseStatusCode(secondResponse, 409);
        }).verifyComplete();
    }

    @Test
    public void createIfNotExistsDirectoryWithMetadata() {
        StepVerifier
            .create(primaryDirectoryAsyncClient
                .createIfNotExistsWithResponse(new ShareDirectoryCreateOptions().setMetadata(testMetadata)))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsDirectoryWithFilePermission() {
        StepVerifier
            .create(primaryDirectoryAsyncClient.createIfNotExistsWithResponse(
                new ShareDirectoryCreateOptions().setFilePermission(FILE_PERMISSION).setMetadata(testMetadata)))
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
            })
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsDirectoryWithFilePermKey() {
        String filePermissionKey = shareClient.createPermission(FILE_PERMISSION);
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setFilePermissionKey(filePermissionKey);
        StepVerifier
            .create(primaryDirectoryAsyncClient
                .createIfNotExistsWithResponse(new ShareDirectoryCreateOptions().setSmbProperties(smbProperties)))
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
            })
            .verifyComplete();
    }

    @Test
    public void deleteDirectory() {
        StepVerifier.create(primaryDirectoryAsyncClient.create().then(primaryDirectoryAsyncClient.deleteWithResponse()))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 202))
            .verifyComplete();
    }

    @Test
    public void deleteDirectoryError() {
        StepVerifier.create(primaryDirectoryAsyncClient.delete())
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404,
                ShareErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    public void deleteIfExistsDirectoryMin() {
        StepVerifier.create(primaryDirectoryAsyncClient.create().then(primaryDirectoryAsyncClient.deleteIfExists()))
            .assertNext(result -> assertEquals(Boolean.TRUE, result))
            .verifyComplete();
    }

    @Test
    public void deleteIfExistsDirectory() {
        StepVerifier
            .create(primaryDirectoryAsyncClient.create().then(primaryDirectoryAsyncClient.deleteIfExistsWithResponse()))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 202))
            .verifyComplete();
    }

    @Test
    public void deleteIfExistsDirectoryThatDoesNotExist() {
        ShareDirectoryAsyncClient client = primaryDirectoryAsyncClient.getDirectoryAsyncClient(generatePathName());

        StepVerifier.create(client.deleteIfExistsWithResponse(null)).assertNext(response -> {
            assertNotNull(response);
            assertFalse(response.getValue());
            FileShareTestHelper.assertResponseStatusCode(response, 404);
        }).verifyComplete();

        StepVerifier.create(client.exists())
            .assertNext(exists -> assertTrue(exists != null && !exists))
            .verifyComplete();
    }

    @Test
    public void deleteIfExistsDirectoryThatWasAlreadyDeleted() {
        StepVerifier
            .create(primaryDirectoryAsyncClient.create().then(primaryDirectoryAsyncClient.deleteIfExistsWithResponse()))
            .assertNext(initialResponse -> {
                assertNotNull(initialResponse);
                FileShareTestHelper.assertResponseStatusCode(initialResponse, 202);
                assertTrue(initialResponse.getValue());
            })
            .verifyComplete();

        StepVerifier.create(primaryDirectoryAsyncClient.deleteIfExistsWithResponse()).assertNext(secondResponse -> {
            assertNotNull(secondResponse);
            FileShareTestHelper.assertResponseStatusCode(secondResponse, 404);
            assertFalse(secondResponse.getValue());
        }).verifyComplete();
    }

    @Test
    public void getProperties() {
        StepVerifier
            .create(primaryDirectoryAsyncClient.create().then(primaryDirectoryAsyncClient.getPropertiesWithResponse()))
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
            })
            .verifyComplete();
    }

    @Test
    public void getPropertiesError() {
        StepVerifier.create(primaryDirectoryAsyncClient.getPropertiesWithResponse())
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404,
                ShareErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    public void setPropertiesFilePermission() {
        StepVerifier.create(primaryDirectoryAsyncClient.createWithResponse(null, null, null)
            .then(primaryDirectoryAsyncClient.setPropertiesWithResponse(null, FILE_PERMISSION))).assertNext(it -> {
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

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#filePermissionFormatSupplier")
    public void setDirectoryHttpHeadersFilePermissionFormat(FilePermissionFormat filePermissionFormat) {
        String permission = FileShareTestHelper.getPermissionFromFormat(filePermissionFormat);

        ShareDirectorySetPropertiesOptions options = new ShareDirectorySetPropertiesOptions().setFilePermissions(
            new ShareFilePermission().setPermission(permission).setPermissionFormat(filePermissionFormat));

        Mono<Response<ShareDirectoryInfo>> bagResponse
            = primaryDirectoryAsyncClient.create().then(primaryDirectoryAsyncClient.setPropertiesWithResponse(options));

        StepVerifier.create(bagResponse).assertNext(r -> {
            FileShareTestHelper.assertResponseStatusCode(r, 200);
            assertNotNull(r.getValue().getSmbProperties().getFilePermissionKey());
        }).verifyComplete();
    }

    @Test
    public void setPropertiesFilePermissionKey() {
        String filePermissionKey = shareClient.createPermission(FILE_PERMISSION);
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setFilePermissionKey(filePermissionKey);
        StepVerifier.create(primaryDirectoryAsyncClient.createWithResponse(null, null, null)
            .then(primaryDirectoryAsyncClient.setPropertiesWithResponse(smbProperties, null))).assertNext(it -> {
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
        primaryDirectoryAsyncClient.createWithResponse(null, null, null);
        FileSmbProperties properties = new FileSmbProperties().setFilePermissionKey("filePermissionKey");
        StepVerifier.create(primaryDirectoryAsyncClient.setProperties(properties, FILE_PERMISSION))
            .verifyErrorSatisfies(it -> assertInstanceOf(IllegalArgumentException.class, it));

        StepVerifier
            .create(
                primaryDirectoryAsyncClient.setProperties(null, FileShareTestHelper.getRandomString(9 * Constants.KB)))
            .verifyErrorSatisfies(it -> assertInstanceOf(IllegalArgumentException.class, it));
    }

    @Test
    public void setMetadata() {
        StepVerifier.create(primaryDirectoryAsyncClient.createWithResponse(null, null, testMetadata))
            .assertNext(response -> FileShareTestHelper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        Map<String, String> updatedMetadata = Collections.singletonMap("update", "value");

        StepVerifier.create(primaryDirectoryAsyncClient.getPropertiesWithResponse()).assertNext(it -> {
            boolean allEqual = it.getValue()
                .getMetadata()
                .entrySet()
                .stream()
                .allMatch(e -> e.getValue().equals(testMetadata.get(e.getKey())));
            assertTrue(allEqual);
        }).verifyComplete();

        StepVerifier.create(primaryDirectoryAsyncClient.setMetadataWithResponse(updatedMetadata))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 200))
            .verifyComplete();

        StepVerifier.create(primaryDirectoryAsyncClient.getPropertiesWithResponse()).assertNext(it -> {
            boolean allEqual = it.getValue()
                .getMetadata()
                .entrySet()
                .stream()
                .allMatch(e -> e.getValue().equals(updatedMetadata.get(e.getKey())));
            assertTrue(allEqual);
        }).verifyComplete();
    }

    @Test
    public void setMetadataError() {
        StepVerifier
            .create(primaryDirectoryAsyncClient.create()
                .then(primaryDirectoryAsyncClient.setMetadata(Collections.singletonMap("", "value"))))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 400,
                ShareErrorCode.EMPTY_METADATA_KEY));
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#listFilesAndDirectoriesSupplier")
    public void listFilesAndDirectories(String[] expectedFiles, String[] expectedDirectories) {
        Mono<Void> createFilesAndDirectories = primaryDirectoryAsyncClient.create()
            .thenMany(Flux.fromArray(expectedFiles).flatMap(file -> primaryDirectoryAsyncClient.createFile(file, 2)))
            .thenMany(Flux.fromArray(expectedDirectories)
                .flatMap(directory -> primaryDirectoryAsyncClient.createSubdirectory(directory)))
            .then();

        StepVerifier
            .create(
                createFilesAndDirectories.thenMany(primaryDirectoryAsyncClient.listFilesAndDirectories().collectList()))
            .assertNext(fileItems -> {
                List<String> foundFiles = new ArrayList<>();
                List<String> foundDirectories = new ArrayList<>();
                for (ShareFileItem fileRef : fileItems) {
                    if (fileRef.isDirectory()) {
                        foundDirectories.add(fileRef.getName());
                    } else {
                        foundFiles.add(fileRef.getName());
                    }
                }
                assertArrayEquals(expectedFiles, foundFiles.toArray());
                assertArrayEquals(expectedDirectories, foundDirectories.toArray());
            })
            .verifyComplete();
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
        Queue<String> nameList = new LinkedList<>();
        String dirPrefix = generatePathName();

        Mono<Void> createDirectoriesAndFiles
            = primaryDirectoryAsyncClient.create().thenMany(Flux.range(0, 2).flatMap(i -> {
                ShareDirectoryAsyncClient subDirClient
                    = primaryDirectoryAsyncClient.getSubdirectoryClient(dirPrefix + i);
                return subDirClient.create().thenMany(Flux.range(0, 2).flatMap(j -> {
                    int num = i * 2 + j + 3;
                    return subDirClient.createFile(dirPrefix + num, 1024);
                }));
            }))
                .then(primaryDirectoryAsyncClient.createFile(dirPrefix + 2, 1024))
                .thenMany(Flux.range(0, 3).doOnNext(i -> nameList.add(dirPrefix + i)))
                .then();

        StepVerifier
            .create(createDirectoriesAndFiles
                .thenMany(primaryDirectoryAsyncClient.listFilesAndDirectories(prefix + extraPrefix, maxResults)))
            .thenConsumeWhile(it -> Objects.equals(it.getName(), nameList.remove()))
            .verifyComplete();

        for (int i = 0; i < 3 - numOfResults; i++) {
            nameList.remove();
        }
        assertTrue(nameList.isEmpty());
    }

    private static Stream<Arguments> listFilesAndDirectoriesArgsSupplier() {
        return Stream.of(Arguments.of("", null, 3), Arguments.of("", 1, 3), Arguments.of("noops", 3, 0));
    }

    @ParameterizedTest
    @MethodSource("listHandlesSupplier")
    public void listHandles(Integer maxResults, boolean recursive) {
        StepVerifier.create(primaryDirectoryAsyncClient.create()
            .thenMany(primaryDirectoryAsyncClient.listHandles(maxResults, recursive))).verifyComplete();
    }

    private static Stream<Arguments> listHandlesSupplier() {
        return Stream.of(Arguments.of(2, true), Arguments.of(null, false));
    }

    @Test
    public void listHandlesError() {
        StepVerifier.create(primaryDirectoryAsyncClient.listHandles(null, true))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404,
                ShareErrorCode.RESOURCE_NOT_FOUND));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2019-07-07")
    @Test
    public void forceCloseHandleMin() {
        StepVerifier
            .create(primaryDirectoryAsyncClient.create().then(primaryDirectoryAsyncClient.forceCloseHandle("1")))
            .assertNext(it -> {
                assertEquals(0, it.getClosedHandles());
                assertEquals(0, it.getFailedHandles());
            })
            .verifyComplete();
    }

    @Test
    public void forceCloseHandleInvalidHandleID() {
        StepVerifier
            .create(primaryDirectoryAsyncClient.create()
                .then(primaryDirectoryAsyncClient.forceCloseHandle("invalidHandleId")))
            .verifyErrorSatisfies(it -> assertInstanceOf(ShareStorageException.class, it));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2019-07-07")
    @Test
    public void forceCloseAllHandlesMin() {
        StepVerifier
            .create(primaryDirectoryAsyncClient.create().then(primaryDirectoryAsyncClient.forceCloseAllHandles(false)))
            .assertNext(it -> {
                assertEquals(0, it.getClosedHandles());
                assertEquals(0, it.getFailedHandles());
            })
            .verifyComplete();
    }

    @Test
    public void createSubDirectory() {
        StepVerifier
            .create(
                primaryDirectoryAsyncClient.create()
                    .then(primaryDirectoryAsyncClient.createSubdirectoryWithResponse("testCreateSubDirectory", null,
                        null, null)))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();
    }

    @Test
    public void createSubDirectoryInvalidName() {
        StepVerifier
            .create(primaryDirectoryAsyncClient.create()
                .then(primaryDirectoryAsyncClient.createSubdirectory("test/subdirectory")))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404,
                ShareErrorCode.PARENT_NOT_FOUND));
    }

    @Test
    public void createSubDirectoryMetadata() {
        StepVerifier
            .create(primaryDirectoryAsyncClient.create()
                .then(primaryDirectoryAsyncClient.createSubdirectoryWithResponse("testCreateSubDirectory", null, null,
                    testMetadata)))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();
    }

    @Test
    public void createSubDirectoryMetadataError() {
        StepVerifier
            .create(primaryDirectoryAsyncClient.create()
                .then(primaryDirectoryAsyncClient.createSubdirectoryWithResponse("testsubdirectory", null, null,
                    Collections.singletonMap("", "value"))))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 400,
                ShareErrorCode.EMPTY_METADATA_KEY));
    }

    @Test
    public void createSubDirectoryFilePermission() {
        StepVerifier
            .create(primaryDirectoryAsyncClient.create()
                .then(primaryDirectoryAsyncClient.createSubdirectoryWithResponse("testCreateSubDirectory", null,
                    FILE_PERMISSION, null)))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();
    }

    @Test
    public void createSubDirectoryFilePermKey() {
        String filePermissionKey = shareClient.createPermission(FILE_PERMISSION);
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setFilePermissionKey(filePermissionKey);

        StepVerifier
            .create(primaryDirectoryAsyncClient.create()
                .then(primaryDirectoryAsyncClient.createSubdirectoryWithResponse("testCreateSubDirectory",
                    smbProperties, null, null)))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsSubDirectory() {
        StepVerifier
            .create(primaryDirectoryAsyncClient.create()
                .then(primaryDirectoryAsyncClient.createSubdirectoryIfNotExistsWithResponse("testCreateSubDirectory",
                    new ShareDirectoryCreateOptions())))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsSubDirectoryInvalidName() {
        StepVerifier
            .create(primaryDirectoryAsyncClient.create()
                .then(primaryDirectoryAsyncClient.createSubdirectoryIfNotExists("test/subdirectory")))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404,
                ShareErrorCode.PARENT_NOT_FOUND));
    }

    @Test
    public void createIfNotExistsSubDirectoryThatAlreadyExists() {
        String subdirectoryName = generatePathName();
        ShareDirectoryAsyncClient client = primaryDirectoryAsyncClient.getDirectoryAsyncClient(generatePathName());

        Mono<Response<ShareDirectoryAsyncClient>> initialResponseMono
            = client.createSubdirectoryIfNotExistsWithResponse(subdirectoryName, new ShareDirectoryCreateOptions());

        Mono<Response<ShareDirectoryAsyncClient>> secondResponseMono
            = client.createSubdirectoryIfNotExistsWithResponse(subdirectoryName, new ShareDirectoryCreateOptions());

        StepVerifier.create(client.create().then(initialResponseMono)).assertNext(initialResponse -> {
            assertNotNull(initialResponse);
            FileShareTestHelper.assertResponseStatusCode(initialResponse, 201);
        }).verifyComplete();

        StepVerifier.create(secondResponseMono).assertNext(secondResponse -> {
            assertNotNull(secondResponse);
            FileShareTestHelper.assertResponseStatusCode(secondResponse, 409);
        }).verifyComplete();
    }

    @Test
    public void createIfNotExistsSubDirectoryMetadata() {
        StepVerifier
            .create(primaryDirectoryAsyncClient.create()
                .then(primaryDirectoryAsyncClient.createSubdirectoryIfNotExistsWithResponse("testCreateSubDirectory",
                    new ShareDirectoryCreateOptions().setMetadata(testMetadata))))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsSubDirectoryMetadataError() {
        StepVerifier
            .create(primaryDirectoryAsyncClient.create()
                .then(primaryDirectoryAsyncClient.createSubdirectoryIfNotExistsWithResponse("testsubdirectory",
                    new ShareDirectoryCreateOptions().setMetadata(Collections.singletonMap("", "value")))))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 400,
                ShareErrorCode.EMPTY_METADATA_KEY));
    }

    @Test
    public void createIfNotExistsSubDirectoryFilePermission() {
        StepVerifier
            .create(primaryDirectoryAsyncClient.create()
                .then(primaryDirectoryAsyncClient.createSubdirectoryIfNotExistsWithResponse("testCreateSubDirectory",
                    new ShareDirectoryCreateOptions().setFilePermission(FILE_PERMISSION))))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsSubDirectoryFilePermKey() {
        String filePermissionKey = shareClient.createPermission(FILE_PERMISSION);
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setFilePermissionKey(filePermissionKey);

        StepVerifier
            .create(primaryDirectoryAsyncClient.create()
                .then(primaryDirectoryAsyncClient.createSubdirectoryIfNotExistsWithResponse("testCreateSubDirectory",
                    new ShareDirectoryCreateOptions().setSmbProperties(smbProperties))))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();
    }

    @Test
    public void deleteSubDirectory() {
        String subDirectoryName = "testSubCreateDirectory";
        StepVerifier
            .create(primaryDirectoryAsyncClient.create()
                .then(primaryDirectoryAsyncClient.createSubdirectory(subDirectoryName))
                .then(primaryDirectoryAsyncClient.deleteSubdirectoryWithResponse(subDirectoryName)))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 202))
            .verifyComplete();
    }

    @Test
    public void deleteSubDirectoryError() {
        StepVerifier
            .create(primaryDirectoryAsyncClient.create()
                .then(primaryDirectoryAsyncClient.deleteSubdirectoryWithResponse("testsubdirectory")))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404,
                ShareErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    public void deleteIfExistsSubDirectory() {
        String subDirectoryName = "testSubCreateDirectory";
        StepVerifier
            .create(primaryDirectoryAsyncClient.create()
                .then(primaryDirectoryAsyncClient.createSubdirectory(subDirectoryName))
                .then(primaryDirectoryAsyncClient.deleteSubdirectoryIfExistsWithResponse(subDirectoryName)))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 202))
            .verifyComplete();
    }

    @Test
    public void deleteIfExistsSubDirectoryMin() {
        String subDirectoryName = "testSubCreateDirectory";
        StepVerifier
            .create(primaryDirectoryAsyncClient.create()
                .then(primaryDirectoryAsyncClient.createSubdirectory(subDirectoryName))
                .then(primaryDirectoryAsyncClient.deleteSubdirectoryIfExists(subDirectoryName)))
            .assertNext(result -> assertEquals(Boolean.TRUE, result))
            .verifyComplete();
    }

    @Test
    public void deleteIfExistsSubDirectoryThatDoesNotExist() {
        String subdirectoryName = generatePathName();
        ShareDirectoryAsyncClient client = primaryDirectoryAsyncClient.getDirectoryAsyncClient(generatePathName());

        StepVerifier.create(client.create()
            .then(client.deleteSubdirectoryIfExistsWithResponse(subdirectoryName, null))
            .flatMap(response -> {
                assertNotNull(response);
                assertFalse(response.getValue());
                FileShareTestHelper.assertResponseStatusCode(response, 404);
                return client.getSubdirectoryClient(subdirectoryName).exists();
            })).assertNext(exists -> assertNotEquals(Boolean.TRUE, exists)).verifyComplete();
    }

    @Test
    public void createFile() {
        StepVerifier
            .create(
                primaryDirectoryAsyncClient.create()
                    .then(primaryDirectoryAsyncClient.createFileWithResponse("testCreateFile", 1024, null, null, null,
                        null)))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#createFileInvalidArgsSupplier")
    public void createFileInvalidArgs(String fileName, long maxSize, int statusCode, ShareErrorCode errMsg) {
        StepVerifier
            .create(primaryDirectoryAsyncClient.create()
                .then(primaryDirectoryAsyncClient.createFileWithResponse(fileName, maxSize, null, null, null, null)))
            .verifyErrorSatisfies(
                it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, statusCode, errMsg));
    }

    @Test
    public void createFileMaxOverload() {
        ShareFileHttpHeaders httpHeaders = new ShareFileHttpHeaders().setContentType("txt");
        smbProperties.setFileCreationTime(testResourceNamer.now()).setFileLastWriteTime(testResourceNamer.now());

        StepVerifier
            .create(primaryDirectoryAsyncClient.create()
                .then(primaryDirectoryAsyncClient.createFileWithResponse("testCreateFile", 1024, httpHeaders,
                    smbProperties, FILE_PERMISSION, testMetadata)))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#createFileMaxOverloadInvalidArgsSupplier")
    public void createFileMaxOverloadInvalidArgs(String fileName, long maxSize, ShareFileHttpHeaders httpHeaders,
        Map<String, String> metadata, ShareErrorCode errMsg) {
        StepVerifier
            .create(primaryDirectoryAsyncClient.create()
                .then(primaryDirectoryAsyncClient.createFileWithResponse(fileName, maxSize, httpHeaders, null, null,
                    metadata)))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 400, errMsg));
    }

    @Test
    public void createFileLease() {
        StepVerifier
            .create(primaryDirectoryAsyncClient.create()
                .then(primaryDirectoryAsyncClient.getFileClient("testCreateFile").create(512))
                .then(createLeaseClient(primaryDirectoryAsyncClient.getFileClient("testCreateFile")).acquireLease())
                .flatMap(leaseId -> primaryDirectoryAsyncClient.createFileWithResponse("testCreateFile", 1024, null,
                    null, null, null, new ShareRequestConditions().setLeaseId(leaseId))))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void createFileLeaseFail() {
        StepVerifier
            .create(primaryDirectoryAsyncClient.create()
                .then(primaryDirectoryAsyncClient.getFileClient("testCreateFile").create(512))
                .then(createLeaseClient(primaryDirectoryAsyncClient.getFileClient("testCreateFile")).acquireLease())
                .then(primaryDirectoryAsyncClient.createFileWithResponse("testCreateFile", 1024, null, null, null, null,
                    new ShareRequestConditions().setLeaseId(testResourceNamer.randomUuid()))))
            .verifyError(ShareStorageException.class);
    }

    @Test
    public void deleteFile() {
        String fileName = "testCreateFile";
        StepVerifier
            .create(primaryDirectoryAsyncClient.create()
                .then(primaryDirectoryAsyncClient.createFile(fileName, 1024))
                .then(primaryDirectoryAsyncClient.deleteFileWithResponse(fileName)))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 202))
            .verifyComplete();
    }

    @Test
    public void deleteFileLease() {
        String fileName = "testCreateFile";
        StepVerifier.create(primaryDirectoryAsyncClient.create()
            .then(primaryDirectoryAsyncClient.createFile(fileName, 1024))
            .then(createLeaseClient(primaryDirectoryAsyncClient.getFileClient(fileName)).acquireLease())
            .flatMap(leaseId -> primaryDirectoryAsyncClient.deleteFileWithResponse(fileName,
                new ShareRequestConditions().setLeaseId(leaseId))))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void deleteFileLeaseFail() {
        String fileName = "testCreateFile";

        StepVerifier
            .create(primaryDirectoryAsyncClient.create()
                .then(primaryDirectoryAsyncClient.createFile(fileName, 1024))
                .then(createLeaseClient(primaryDirectoryAsyncClient.getFileClient(fileName)).acquireLease())
                .then(primaryDirectoryAsyncClient.deleteFileWithResponse(fileName,
                    new ShareRequestConditions().setLeaseId(testResourceNamer.randomUuid()))))
            .verifyError(ShareStorageException.class);
    }

    @Test
    public void deleteIfExistsFile() {
        String fileName = "testCreateFile";
        StepVerifier
            .create(primaryDirectoryAsyncClient.create()
                .then(primaryDirectoryAsyncClient.createFile(fileName, 1024))
                .then(primaryDirectoryAsyncClient.deleteFileIfExistsWithResponse(fileName)))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 202))
            .verifyComplete();
    }

    @Test
    public void deleteIfExistsFileMin() {
        String fileName = "testCreateFile";
        StepVerifier
            .create(primaryDirectoryAsyncClient.create()
                .then(primaryDirectoryAsyncClient.createFile(fileName, 1024))
                .then(primaryDirectoryAsyncClient.deleteFileIfExists(fileName)))
            .assertNext(result -> assertEquals(Boolean.TRUE, result))
            .verifyComplete();
    }

    @Test
    public void deleteIfExistsFileLease() {
        String fileName = "testCreateFile";
        StepVerifier.create(primaryDirectoryAsyncClient.create()
            .then(primaryDirectoryAsyncClient.createFile(fileName, 1024))
            .then(createLeaseClient(primaryDirectoryAsyncClient.getFileClient(fileName)).acquireLease())
            .flatMap(leaseId -> primaryDirectoryAsyncClient.deleteFileIfExistsWithResponse(fileName,
                new ShareRequestConditions().setLeaseId(leaseId))))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void deleteIfExistsFileLeaseFail() {
        String fileName = "testCreateFile";
        Mono<Void> createFileMono = primaryDirectoryAsyncClient.create()
            .then(primaryDirectoryAsyncClient.createFile(fileName, 1024))
            .then(createLeaseClient(primaryDirectoryAsyncClient.getFileClient(fileName)).acquireLease().then());

        StepVerifier
            .create(createFileMono.then(primaryDirectoryAsyncClient.deleteFileIfExistsWithResponse(fileName,
                new ShareRequestConditions().setLeaseId(testResourceNamer.randomUuid()))))
            .verifyError(ShareStorageException.class);
    }

    @Test
    public void deleteIfExistsFileThatDoesNotExist() {
        String subdirectoryName = generatePathName();
        ShareDirectoryAsyncClient client = primaryDirectoryAsyncClient.getDirectoryAsyncClient(generatePathName());

        StepVerifier.create(client.create().then(client.deleteFileIfExistsWithResponse(subdirectoryName, null)))
            .assertNext(response -> {
                assertNotNull(response);
                FileShareTestHelper.assertResponseStatusCode(response, 404);
                assertNotEquals(Boolean.TRUE, client.getSubdirectoryClient(subdirectoryName).exists());
            })
            .verifyComplete();
    }

    @Test
    public void getShareSnapshotId() {
        String snapshot = OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 1, 1), ZoneOffset.UTC).toString();
        ShareDirectoryAsyncClient shareSnapshotClient
            = directoryBuilderHelper(shareName, directoryPath).snapshot(snapshot).buildDirectoryAsyncClient();
        assertEquals(snapshot, shareSnapshotClient.getShareSnapshotId());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#filePermissionFormatSupplier")
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
            .assertNext(r -> assertNotNull(r.getSmbProperties().getFilePermissionKey()))
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

        Mono<ShareDirectoryInfo> createDirMono = dirClient.create();
        ShareServiceAsyncClient oAuthServiceClient = getOAuthServiceAsyncClient(
            new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP).audience(null));

        ShareDirectoryAsyncClient aadDirClient
            = oAuthServiceClient.getShareAsyncClient(shareName).getDirectoryClient(dirName);

        StepVerifier.create(createDirMono.then(aadDirClient.exists())).expectNext(true).verifyComplete();
    }

    @Test
    public void storageAccountAudience() {
        String dirName = generatePathName();
        ShareDirectoryAsyncClient dirClient = directoryBuilderHelper(shareName, dirName).buildDirectoryAsyncClient();
        Mono<ShareDirectoryInfo> createDirMono = dirClient.create();
        ShareServiceAsyncClient oAuthServiceClient = getOAuthServiceAsyncClient(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP)
            .audience(ShareAudience.createShareServiceAccountAudience(primaryDirectoryAsyncClient.getAccountName())));

        ShareDirectoryAsyncClient aadDirClient
            = oAuthServiceClient.getShareAsyncClient(shareName).getDirectoryClient(dirName);

        StepVerifier.create(createDirMono.then(aadDirClient.exists())).expectNext(true).verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @LiveOnly
    @Test
    public void audienceErrorBearerChallengeRetry() {
        String dirName = generatePathName();
        ShareDirectoryAsyncClient dirClient = directoryBuilderHelper(shareName, dirName).buildDirectoryAsyncClient();
        Mono<ShareDirectoryInfo> createDirMono = dirClient.create();

        ShareServiceAsyncClient oAuthServiceClient
            = getOAuthServiceAsyncClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP)
                .audience(ShareAudience.createShareServiceAccountAudience("badAudience")));

        ShareDirectoryAsyncClient aadDirClient
            = oAuthServiceClient.getShareAsyncClient(shareName).getDirectoryClient(dirName);

        StepVerifier.create(createDirMono.then(aadDirClient.exists()))
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();
    }

    @Test
    public void audienceFromString() {
        String url = String.format("https://%s.file.core.windows.net/", primaryDirectoryAsyncClient.getAccountName());
        ShareAudience audience = ShareAudience.fromString(url);

        String dirName = generatePathName();
        ShareDirectoryAsyncClient dirClient = directoryBuilderHelper(shareName, dirName).buildDirectoryAsyncClient();
        Mono<ShareDirectoryInfo> createDirMono = dirClient.create();
        ShareServiceAsyncClient oAuthServiceClient = getOAuthServiceAsyncClient(
            new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP).audience(audience));

        ShareDirectoryAsyncClient aadDirClient
            = oAuthServiceClient.getShareAsyncClient(shareName).getDirectoryClient(dirName);

        StepVerifier.create(createDirMono.then(aadDirClient.exists())).expectNext(true).verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2025-05-05")
    @Test
    public void createNFS() {
        ShareDirectoryCreateOptions options = new ShareDirectoryCreateOptions()
            .setPosixProperties(new FilePosixProperties().setOwner("345").setGroup("123").setFileMode("7777"));

        String shareName = generateShareName();
        Mono<Response<ShareDirectoryInfo>> create
            = getPremiumNFSShareAsyncClient(shareName).flatMap(premiumShareClient -> {
                ShareDirectoryAsyncClient premiumDirectoryClient
                    = premiumShareClient.getDirectoryClient(generatePathName());
                return premiumDirectoryClient.createWithResponse(options);
            });

        StepVerifier.create(create).assertNext(r -> {
            ShareDirectoryInfo response = r.getValue();
            assertEquals(NfsFileType.DIRECTORY, response.getPosixProperties().getFileType());
            assertEquals("345", response.getPosixProperties().getOwner());
            assertEquals("123", response.getPosixProperties().getGroup());
            assertEquals("7777", response.getPosixProperties().getFileMode());

            FileShareTestHelper.assertSmbPropertiesNull(response.getSmbProperties());
        }).verifyComplete();

        //cleanup
        premiumFileServiceAsyncClient.getShareAsyncClient(shareName).delete().block();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2025-05-05")
    @Test
    public void setPropertiesNFS() {
        ShareDirectorySetPropertiesOptions options = new ShareDirectorySetPropertiesOptions()
            .setPosixProperties(new FilePosixProperties().setOwner("345").setGroup("123").setFileMode("7777"));

        String shareName = generateShareName();
        Mono<Response<ShareDirectoryInfo>> create
            = getPremiumNFSShareAsyncClient(shareName).flatMap(premiumShareClient -> {
                ShareDirectoryAsyncClient premiumDirectoryClient
                    = premiumShareClient.getDirectoryClient(generatePathName());
                return premiumDirectoryClient.create().then(premiumDirectoryClient.setPropertiesWithResponse(options));
            });

        StepVerifier.create(create).assertNext(r -> {
            ShareDirectoryInfo response = r.getValue();
            assertEquals("345", response.getPosixProperties().getOwner());
            assertEquals("123", response.getPosixProperties().getGroup());
            assertEquals("7777", response.getPosixProperties().getFileMode());

            FileShareTestHelper.assertSmbPropertiesNull(response.getSmbProperties());
        }).verifyComplete();

        //cleanup
        premiumFileServiceAsyncClient.getShareAsyncClient(shareName).delete().block();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2025-05-05")
    @Test
    public void getPropertiesNFS() {
        String shareName = generateShareName();
        Mono<Response<ShareDirectoryProperties>> create
            = getPremiumNFSShareAsyncClient(shareName).flatMap(premiumShareClient -> {
                ShareDirectoryAsyncClient premiumDirectoryClient
                    = premiumShareClient.getDirectoryClient(generatePathName());
                return premiumDirectoryClient.create().then(premiumDirectoryClient.getPropertiesWithResponse());
            });

        StepVerifier.create(create).assertNext(r -> {
            ShareDirectoryProperties response = r.getValue();

            assertEquals(NfsFileType.DIRECTORY, response.getPosixProperties().getFileType());
            assertEquals("0", response.getPosixProperties().getOwner());
            assertEquals("0", response.getPosixProperties().getGroup());
            assertEquals("0755", response.getPosixProperties().getFileMode());

            FileShareTestHelper.assertSmbPropertiesNull(response.getSmbProperties());
        }).verifyComplete();

        //cleanup
        premiumFileServiceAsyncClient.getShareAsyncClient(shareName).delete().block();
    }

    @Test
    public void directoryExistsHandlesParentNotFound() {
        ShareAsyncClient shareClient = shareBuilderHelper(shareName).buildAsyncClient();
        ShareDirectoryAsyncClient directoryClient = shareClient.getDirectoryClient("fakeDir");
        ShareDirectoryAsyncClient subDirectoryClient = directoryClient.getSubdirectoryClient(generatePathName());

        StepVerifier.create(subDirectoryClient.existsWithResponse()).assertNext(r -> {
            assertFalse(r.getValue());
            assertEquals(ShareErrorCode.PARENT_NOT_FOUND.getValue(), r.getHeaders().getValue(ERROR_CODE_HEADER_NAME));
        }).verifyComplete();
    }

    /* PULLED FROM RELEASE
    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2026-02-06")
    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#filePropertySemanticsSupplier")
    public void createDirectoryFilePropertySemantics(FilePropertySemantics filePropertySemantics) {
        ShareDirectoryCreateOptions options
            = new ShareDirectoryCreateOptions().setFilePropertySemantics(filePropertySemantics);
    
        // For Create File and Directory with FilePropertySemantics == Restore,
        // the File Permission property must be provided, otherwise FilePropertySemantics will default to new.
        if (filePropertySemantics == FilePropertySemantics.RESTORE) {
            options.setFilePermission(FILE_PERMISSION);
        }
    
        StepVerifier.create(primaryDirectoryAsyncClient.createWithResponse(options)).assertNext(r -> {
            HttpHeader retrievedHeader = r.getRequest().getHeaders().get(X_MS_FILE_PROPERTY_SEMANTICS);
            if (filePropertySemantics != null) {
                assertEquals(filePropertySemantics.toString(), retrievedHeader.getValue());
            } else {
                assertNull(retrievedHeader);
            }
        }).verifyComplete();
    }
    
    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2026-02-06")
    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#filePropertySemanticsSupplier")
    public void createDirectoryIfNotExistsFilePropertySemantics(FilePropertySemantics filePropertySemantics) {
        ShareDirectoryCreateOptions options
            = new ShareDirectoryCreateOptions().setFilePropertySemantics(filePropertySemantics);
    
        // For Create File and Directory with FilePropertySemantics == Restore,
        // the File Permission property must be provided, otherwise FilePropertySemantics will default to new.
        if (filePropertySemantics == FilePropertySemantics.RESTORE) {
            options.setFilePermission(FILE_PERMISSION);
        }
    
        StepVerifier.create(primaryDirectoryAsyncClient.createIfNotExistsWithResponse(options)).assertNext(r -> {
            HttpHeader retrievedHeader = r.getRequest().getHeaders().get(X_MS_FILE_PROPERTY_SEMANTICS);
            if (filePropertySemantics != null) {
                assertEquals(filePropertySemantics.toString(), retrievedHeader.getValue());
            } else {
                assertNull(retrievedHeader);
            }
        }).verifyComplete();
    } */
}
