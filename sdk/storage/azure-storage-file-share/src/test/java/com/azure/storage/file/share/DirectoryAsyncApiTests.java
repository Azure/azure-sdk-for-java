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
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import com.azure.storage.file.share.models.CloseHandlesInfo;
import com.azure.storage.file.share.models.FilePermissionFormat;
import com.azure.storage.file.share.models.HandleItem;
import com.azure.storage.file.share.models.NtfsFileAttributes;
import com.azure.storage.file.share.models.ShareAudience;
import com.azure.storage.file.share.models.ShareDirectoryInfo;
import com.azure.storage.file.share.models.ShareDirectoryProperties;
import com.azure.storage.file.share.models.ShareDirectorySetMetadataInfo;
import com.azure.storage.file.share.models.ShareErrorCode;
import com.azure.storage.file.share.models.ShareFileHttpHeaders;
import com.azure.storage.file.share.models.ShareFileInfo;
import com.azure.storage.file.share.models.ShareFileItem;
import com.azure.storage.file.share.models.ShareFilePermission;
import com.azure.storage.file.share.models.ShareRequestConditions;
import com.azure.storage.file.share.models.ShareSnapshotInfo;
import com.azure.storage.file.share.models.ShareStorageException;
import com.azure.storage.file.share.models.ShareTokenIntent;
import com.azure.storage.file.share.options.ShareCreateOptions;
import com.azure.storage.file.share.options.ShareDirectoryCreateOptions;
import com.azure.storage.file.share.options.ShareDirectorySetPropertiesOptions;
import com.azure.storage.file.share.options.ShareFileRenameOptions;
import com.azure.storage.file.share.options.ShareListFilesAndDirectoriesOptions;
import com.azure.storage.file.share.sas.ShareFileSasPermission;
import com.azure.storage.file.share.sas.ShareServiceSasSignatureValues;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.retry.Retry;

import java.io.File;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    public void getShareSnapshotUrl() {
        String accountName
            = StorageSharedKeyCredential.fromConnectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString())
                .getAccountName();
        String expectURLPattern = String.format(
            "https://%s.file.core.windows.net/%s/%s\\?sharesnapshot=\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{7}Z",
            accountName, shareName, directoryPath);

        Mono<ShareSnapshotInfo> createSnapshotMono = Mono.fromCallable(() -> shareClient.createSnapshot());
        Mono<String> directoryUrlMono = createSnapshotMono.flatMap(shareSnapshotInfo -> {
            String snapshotUrl = expectURLPattern.replace("\\", "") + shareSnapshotInfo.getSnapshot();
            ShareDirectoryAsyncClient newDirClient
                = shareBuilderHelper(shareName).snapshot(shareSnapshotInfo.getSnapshot())
                    .buildAsyncClient()
                    .getDirectoryClient(directoryPath);
            return Mono.just(newDirClient.getDirectoryUrl()).map(url -> {
                assertTrue(url.matches(expectURLPattern));
                return snapshotUrl;
            });
        });

        Mono<ShareDirectoryAsyncClient> clientMono = createSnapshotMono.flatMap(shareSnapshotInfo -> {
            String snapshotEndpoint = String.format("https://%s.file.core.windows.net/%s/%s?sharesnapshot=%s",
                accountName, shareName, directoryPath, shareSnapshotInfo.getSnapshot());
            return Mono.just(directoryBuilderHelper(shareName, directoryPath).snapshot(shareSnapshotInfo.getSnapshot())
                .buildDirectoryAsyncClient());
        });

        StepVerifier.create(clientMono.flatMap(client -> directoryUrlMono.map(url -> {
            assertTrue(client.getDirectoryUrl().matches(expectURLPattern));
            return client.getDirectoryUrl();
        }))).expectNextCount(1).verifyComplete();
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

    private static Stream<Arguments> getNonEncodedFileNameSupplier() {
        return Stream.of(Arguments.of("test%test"), Arguments.of("%Россия 한국 中国!"), Arguments.of("%E6%96%91%E9%BB%9E"),
            Arguments.of("斑點"));
    }

    @ParameterizedTest
    @MethodSource("getNonEncodedFileNameSupplier")
    public void getNonEncodedFileName(String fileName) {
        StepVerifier.create(primaryDirectoryAsyncClient.create()
            .then(primaryDirectoryAsyncClient.getFileClient(fileName).create(1024))
            .then(primaryDirectoryAsyncClient.getFileClient(fileName).exists())).expectNext(true).verifyComplete();
    }

    @Test
    public void exists() {
        StepVerifier.create(primaryDirectoryAsyncClient.create().then(primaryDirectoryAsyncClient.exists()))
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    public void doesNotExist() {
        StepVerifier.create(primaryDirectoryAsyncClient.exists()).expectNext(false).verifyComplete();
    }

    @Test
    public void existsError() {
        ShareDirectoryAsyncClient primaryDirectoryAsyncClient
            = directoryBuilderHelper(shareName, directoryPath).sasToken("sig=dummyToken").buildDirectoryAsyncClient();

        StepVerifier.create(primaryDirectoryAsyncClient.exists()).verifyErrorSatisfies(throwable -> {
            ShareStorageException e = assertInstanceOf(ShareStorageException.class, throwable);
            FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 403, ShareErrorCode.AUTHENTICATION_FAILED);
        });
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
    public void createDirectoryWithFilePermissionKey() {
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
    public void createDirectoryWithNtfsAttributes() {
        String filePermissionKey = shareClient.createPermission(FILE_PERMISSION);
        EnumSet<NtfsFileAttributes> attributes = EnumSet.of(NtfsFileAttributes.HIDDEN, NtfsFileAttributes.DIRECTORY);
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setFilePermissionKey(filePermissionKey)
            .setNtfsFileAttributes(attributes);

        StepVerifier.create(primaryDirectoryAsyncClient.createWithResponse(smbProperties, null, null))
            .assertNext(resp -> {
                assertEquals(201, resp.getStatusCode());
                assertNotNull(resp.getValue().getSmbProperties().getFilePermissionKey());
                assertNotNull(resp.getValue().getSmbProperties().getNtfsFileAttributes());
                assertNotNull(resp.getValue().getSmbProperties().getFileLastWriteTime());
                assertNotNull(resp.getValue().getSmbProperties().getFileCreationTime());
                assertNotNull(resp.getValue().getSmbProperties().getFileChangeTime());
                assertNotNull(resp.getValue().getSmbProperties().getParentId());
                assertNotNull(resp.getValue().getSmbProperties().getFileId());
            })
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-06-08")
    @Test
    public void createChangeTime() {
        OffsetDateTime changeTime = testResourceNamer.now();
        Mono<Response<ShareDirectoryInfo>> createResponse = primaryDirectoryAsyncClient
            .createWithResponse(new FileSmbProperties().setFileChangeTime(changeTime), null, null);

        StepVerifier.create(createResponse.flatMap(response -> {
            assertEquals(201, response.getStatusCode());
            return primaryDirectoryAsyncClient.getProperties();
        })).assertNext(properties -> {
            assertTrue(FileShareTestHelper.compareDatesWithPrecision(properties.getSmbProperties().getFileChangeTime(),
                changeTime));
        }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("permissionAndKeySupplier")
    public void createDirectoryPermissionAndKeyError(String filePermissionKey, String permission) {
        FileSmbProperties properties = new FileSmbProperties().setFilePermissionKey(filePermissionKey);
        StepVerifier.create(primaryDirectoryAsyncClient.createWithResponse(properties, permission, null))
            .verifyErrorSatisfies(throwable -> {
                IllegalArgumentException e = assertInstanceOf(IllegalArgumentException.class, throwable);
            });
    }

    private static Stream<Arguments> permissionAndKeySupplier() {
        return Stream.of(Arguments.of("filePermissionKey", FILE_PERMISSION),
            Arguments.of(null, FileShareTestHelper.getRandomString(9 * Constants.KB)));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2022-11-02")
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void createTrailingDot(boolean allowTrailingDot) {
        ShareAsyncClient shareClient = getShareAsyncClient(shareName, allowTrailingDot, null);
        ShareDirectoryAsyncClient rootDirectoryAsyncClient = shareClient.getRootDirectoryClient();
        String dirName = generatePathName();
        String dirNameWithDot = dirName + ".";
        ShareDirectoryAsyncClient dirClient = shareClient.getDirectoryClient(dirNameWithDot);

        Mono<ShareDirectoryInfo> createDirMono = dirClient.create();
        Mono<List<String>> listDirectoriesMono
            = rootDirectoryAsyncClient.listFilesAndDirectories().map(ShareFileItem::getName).collectList();

        StepVerifier.create(createDirMono.then(listDirectoriesMono)).assertNext(foundDirectories -> {
            assertEquals(1, foundDirectories.size());
            if (allowTrailingDot) {
                assertEquals(dirNameWithDot, foundDirectories.get(0));
            } else {
                assertEquals(dirName, foundDirectories.get(0));
            }
        }).verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void createDirectoryOAuth() {
        ShareServiceAsyncClient oAuthServiceClient
            = getOAuthServiceAsyncClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        String dirName = generatePathName();
        ShareDirectoryAsyncClient dirClient
            = oAuthServiceClient.getShareAsyncClient(shareName).getDirectoryClient(dirName);

        Mono<Response<ShareDirectoryInfo>> resultMono = dirClient.createWithResponse(null, null, null, null, null);

        StepVerifier.create(resultMono).assertNext(result -> {
            assertEquals(shareName, dirClient.getShareName());
            assertEquals(dirName, dirClient.getDirectoryPath());
            assertEquals(result.getValue().getETag(), result.getHeaders().getValue(HttpHeaderName.ETAG));
        }).verifyComplete();
    }

    @Test
    public void createIfNotExistsDirectoryMin() {
        StepVerifier.create(primaryDirectoryAsyncClient.createIfNotExists())
            .assertNext(response -> assertNotNull(response))
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
    public void createIfNotExistsDirectoryWithFilePermissionKey() {
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
    public void createIfNotExistsDirectoryWithNtfsAttributes() {
        String filePermissionKey = shareClient.createPermission(FILE_PERMISSION);
        EnumSet<NtfsFileAttributes> attributes = EnumSet.of(NtfsFileAttributes.HIDDEN, NtfsFileAttributes.DIRECTORY);
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setFilePermissionKey(filePermissionKey)
            .setNtfsFileAttributes(attributes);

        ShareDirectoryCreateOptions options = new ShareDirectoryCreateOptions().setSmbProperties(smbProperties);
        Mono<Response<ShareDirectoryInfo>> responseMono
            = primaryDirectoryAsyncClient.createIfNotExistsWithResponse(options);

        StepVerifier.create(responseMono).assertNext(resp -> {
            FileShareTestHelper.assertResponseStatusCode(resp, 201);
            assertNotNull(resp.getValue().getSmbProperties());
            assertNotNull(resp.getValue().getSmbProperties().getFilePermissionKey());
            assertNotNull(resp.getValue().getSmbProperties().getNtfsFileAttributes());
            assertNotNull(resp.getValue().getSmbProperties().getFileLastWriteTime());
            assertNotNull(resp.getValue().getSmbProperties().getFileCreationTime());
            assertNotNull(resp.getValue().getSmbProperties().getFileChangeTime());
            assertNotNull(resp.getValue().getSmbProperties().getParentId());
            assertNotNull(resp.getValue().getSmbProperties().getFileId());
        }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("permissionAndKeySupplier")
    public void createIfNotExistsDirectoryPermissionAndKeyError(String filePermissionKey, String permission) {
        FileSmbProperties properties = new FileSmbProperties().setFilePermissionKey(filePermissionKey);
        ShareDirectoryCreateOptions options
            = new ShareDirectoryCreateOptions().setSmbProperties(properties).setFilePermission(permission);

        StepVerifier.create(primaryDirectoryAsyncClient.createIfNotExistsWithResponse(options))
            .verifyErrorSatisfies(throwable -> {
                IllegalArgumentException e = assertInstanceOf(IllegalArgumentException.class, throwable);
            });
    }

    @Test
    public void deleteDirectory() {
        StepVerifier.create(primaryDirectoryAsyncClient.create().then(primaryDirectoryAsyncClient.deleteWithResponse()))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 202))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2022-11-02")
    @Test
    public void deleteTrailingDot() {
        ShareAsyncClient shareAsyncClient = getShareAsyncClient(shareName, true, null);
        ShareDirectoryAsyncClient directoryClient = shareAsyncClient.getDirectoryClient(generatePathName() + ".");
        StepVerifier.create(directoryClient.create().then(directoryClient.deleteWithResponse()))
            .assertNext(response -> FileShareTestHelper.assertResponseStatusCode(response, 202))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void deleteDirectoryOAuth() {
        ShareServiceAsyncClient oAuthServiceClient
            = getOAuthServiceAsyncClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        String dirName = generatePathName();
        ShareDirectoryAsyncClient dirClient
            = oAuthServiceClient.getShareAsyncClient(shareName).getDirectoryClient(dirName);

        StepVerifier.create(dirClient.create().then(dirClient.deleteWithResponse())).assertNext(response -> {
            FileShareTestHelper.assertResponseStatusCode(response, 202);
            assertNotNull(response.getHeaders().get(HttpHeaderName.X_MS_CLIENT_REQUEST_ID));
        }).verifyComplete();
    }

    @Test
    public void deleteDirectoryError() {
        StepVerifier.create(primaryDirectoryAsyncClient.delete())
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404,
                ShareErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    public void deleteIfExistsDirectory() {
        StepVerifier
            .create(primaryDirectoryAsyncClient.create().then(primaryDirectoryAsyncClient.deleteIfExistsWithResponse()))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 202))
            .verifyComplete();
    }

    @Test
    public void deleteIfExistsDirectoryMin() {
        StepVerifier.create(primaryDirectoryAsyncClient.create().then(primaryDirectoryAsyncClient.deleteIfExists()))
            .assertNext(result -> assertEquals(Boolean.TRUE, result))
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

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2022-11-02")
    @Test
    public void getPropertiesTrailingDot() {
        ShareAsyncClient shareAsyncClient = getShareAsyncClient(shareName, true, null);

        ShareDirectoryAsyncClient directoryClient = shareAsyncClient.getDirectoryClient(generatePathName() + ".");
        Mono<ShareDirectoryInfo> createResponseMono = directoryClient.createIfNotExists();
        Mono<Response<ShareDirectoryProperties>> propertiesResponseMono = createResponseMono
            .flatMap(createResponse -> directoryClient.getPropertiesWithResponse().doOnNext(propertiesResponse -> {
                FileShareTestHelper.assertResponseStatusCode(propertiesResponse, 200);
                assertEquals(createResponse.getETag(), propertiesResponse.getValue().getETag());
                assertEquals(createResponse.getLastModified(), propertiesResponse.getValue().getLastModified());

                FileSmbProperties createSmbProperties = createResponse.getSmbProperties();
                FileSmbProperties getPropertiesSmbProperties = propertiesResponse.getValue().getSmbProperties();
                assertEquals(createSmbProperties.getFilePermissionKey(),
                    getPropertiesSmbProperties.getFilePermissionKey());
                assertEquals(createSmbProperties.getNtfsFileAttributes(),
                    getPropertiesSmbProperties.getNtfsFileAttributes());
                assertEquals(createSmbProperties.getFileLastWriteTime(),
                    getPropertiesSmbProperties.getFileLastWriteTime());
                assertEquals(createSmbProperties.getFileCreationTime(),
                    getPropertiesSmbProperties.getFileCreationTime());
                assertEquals(createSmbProperties.getFileChangeTime(), getPropertiesSmbProperties.getFileChangeTime());
                assertEquals(createSmbProperties.getParentId(), getPropertiesSmbProperties.getParentId());
                assertEquals(createSmbProperties.getFileId(), getPropertiesSmbProperties.getFileId());
            }));
        StepVerifier.create(propertiesResponseMono).expectNextCount(1).verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void getPropertiesOAuth() {
        ShareServiceAsyncClient oAuthServiceClient
            = getOAuthServiceAsyncClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        String dirName = generatePathName();
        ShareDirectoryAsyncClient dirClient
            = oAuthServiceClient.getShareAsyncClient(shareName).getDirectoryClient(dirName);

        Mono<ShareDirectoryInfo> createInfoMono = dirClient.create();
        Mono<ShareDirectoryProperties> propertiesMono = createInfoMono.then(dirClient.getProperties());

        StepVerifier.create(Mono.zip(createInfoMono, propertiesMono)).assertNext(tuple -> {
            ShareDirectoryInfo createInfo = tuple.getT1();
            ShareDirectoryProperties properties = tuple.getT2();
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
        }).verifyComplete();
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

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-06-08")
    @Test
    public void setHttpHeadersChangeTime() {
        Mono<ShareDirectoryInfo> createMono = primaryDirectoryAsyncClient.create();
        String filePermissionKey = shareClient.createPermission(FILE_PERMISSION);
        OffsetDateTime changeTime = testResourceNamer.now();
        smbProperties.setFileChangeTime(testResourceNamer.now()).setFilePermissionKey(filePermissionKey);

        Mono<Response<ShareDirectoryInfo>> setPropertiesMono = createMono.then(primaryDirectoryAsyncClient
            .setPropertiesWithResponse(new FileSmbProperties().setFileChangeTime(changeTime), null));

        StepVerifier.create(setPropertiesMono.then(primaryDirectoryAsyncClient.getProperties()))
            .assertNext(properties -> {
                FileShareTestHelper.compareDatesWithPrecision(properties.getSmbProperties().getFileChangeTime(),
                    changeTime);
            })
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2022-11-02")
    @Test
    public void setHttpHeadersTrailingDot() {
        ShareAsyncClient shareAsyncClient = getShareAsyncClient(shareName, true, null);

        ShareDirectoryAsyncClient directoryClient = shareAsyncClient.getDirectoryClient(generatePathName() + ".");
        Mono<ShareDirectoryInfo> createMono = directoryClient.createIfNotExists();
        Mono<Response<ShareDirectoryInfo>> setPropertiesMono
            = createMono.then(directoryClient.setPropertiesWithResponse(new FileSmbProperties(), null, null, null));

        StepVerifier.create(setPropertiesMono)
            .assertNext(res -> FileShareTestHelper.assertResponseStatusCode(res, 200))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void setHttpHeadersOAuth() {
        ShareServiceAsyncClient oAuthServiceClient
            = getOAuthServiceAsyncClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        String dirName = generatePathName();
        ShareDirectoryAsyncClient dirClient
            = oAuthServiceClient.getShareAsyncClient(shareName).getDirectoryClient(dirName);

        Mono<Response<ShareDirectoryInfo>> createAndSetPropertiesMono
            = dirClient.create().then(dirClient.setPropertiesWithResponse(new FileSmbProperties(), null, null, null));

        StepVerifier.create(createAndSetPropertiesMono)
            .assertNext(res -> FileShareTestHelper.assertResponseStatusCode(res, 200))
            .verifyComplete();
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

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2022-11-02")
    @Test
    public void setMetadataTrailingDot() {
        ShareAsyncClient shareAsyncClient = getShareAsyncClient(shareName, true, null);

        ShareDirectoryAsyncClient directoryClient = shareAsyncClient.getDirectoryClient(generatePathName() + ".");

        Mono<Boolean> deleteIfExistsMono = directoryClient.deleteIfExists();
        Mono<Response<ShareDirectoryInfo>> createResponseMono = deleteIfExistsMono.then(
            directoryClient.createIfNotExistsWithResponse(new ShareDirectoryCreateOptions().setMetadata(testMetadata)));
        Map<String, String> updatedMetadata = Collections.singletonMap("update", "value");

        Mono<ShareDirectoryProperties> getPropertiesBeforeMono
            = createResponseMono.then(directoryClient.getProperties());
        Mono<Response<ShareDirectorySetMetadataInfo>> setPropertiesResponseMono
            = getPropertiesBeforeMono.then(directoryClient.setMetadataWithResponse(updatedMetadata));
        Mono<ShareDirectoryProperties> getPropertiesAfterMono
            = setPropertiesResponseMono.then(directoryClient.getProperties());

        StepVerifier
            .create(deleteIfExistsMono.then(getPropertiesBeforeMono.zipWith(getPropertiesAfterMono, (before, after) -> {
                assertEquals(testMetadata, before.getMetadata());
                assertEquals(updatedMetadata, after.getMetadata());
                return true;
            })))
            .expectNext(true)
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void setMetadataOAuth() {
        ShareServiceAsyncClient oAuthServiceClient
            = getOAuthServiceAsyncClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        String dirName = generatePathName();
        ShareDirectoryAsyncClient dirClient
            = oAuthServiceClient.getShareAsyncClient(shareName).getDirectoryClient(dirName);
        Map<String, String> updatedMetadata = Collections.singletonMap("update", "value");

        Mono<ShareDirectoryProperties> getPropertiesBeforeMono
            = dirClient.createWithResponse(null, null, testMetadata).flatMap(response -> dirClient.getProperties());
        Mono<Response<ShareDirectorySetMetadataInfo>> setPropertiesResponseMono
            = getPropertiesBeforeMono.then(dirClient.setMetadataWithResponse(updatedMetadata));
        Mono<ShareDirectoryProperties> getPropertiesAfterMono
            = setPropertiesResponseMono.then(dirClient.getProperties());

        StepVerifier.create(getPropertiesBeforeMono.zipWith(setPropertiesResponseMono, (before, setResponse) -> {
            FileShareTestHelper.assertResponseStatusCode(setResponse, 200);
            return before;
        }).zipWith(getPropertiesAfterMono, (before, after) -> {
            assertEquals(testMetadata, before.getMetadata());
            assertEquals(updatedMetadata, after.getMetadata());
            return true;
        })).expectNext(true).verifyComplete();
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

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2020-10-02")
    @ParameterizedTest
    @CsvSource(
        value = {
            "false,false,false,false",
            "true,false,false,false",
            "false,true,false,false",
            "false,false,true,false",
            "false,false,false,true",
            "true,true,true,true" })
    public void listFilesAndDirectoriesExtendedInfoArgs(boolean timestamps, boolean etag, boolean attributes,
        boolean permissionKey) {

        String dirPrefix = generatePathName();
        List<String> expectedNames = new ArrayList<>();

        Mono<Void> createDirectoriesAndFiles
            = primaryDirectoryAsyncClient.create().thenMany(Flux.range(0, 2).flatMap(i -> {
                ShareDirectoryAsyncClient subDirClient
                    = primaryDirectoryAsyncClient.getSubdirectoryClient(dirPrefix + i);
                return subDirClient.create().thenMany(Flux.range(0, 2).flatMap(j -> {
                    int num = i * 2 + j + 3;
                    return subDirClient.createFile(dirPrefix + num, 1024);
                }));
            })).then(primaryDirectoryAsyncClient.createFile(dirPrefix + 2, 1024)).then();

        for (int i = 0; i < 3; i++) {
            expectedNames.add(dirPrefix + i);
        }

        ShareListFilesAndDirectoriesOptions options = new ShareListFilesAndDirectoriesOptions().setPrefix(dirPrefix)
            .setIncludeExtendedInfo(true)
            .setIncludeTimestamps(timestamps)
            .setIncludeETag(etag)
            .setIncludeAttributes(attributes)
            .setIncludePermissionKey(permissionKey);

        StepVerifier.create(createDirectoriesAndFiles
            .thenMany(primaryDirectoryAsyncClient.listFilesAndDirectories(options).collectList())
            .doOnNext(returnedFileList -> {
                List<String> returnedNames
                    = returnedFileList.stream().map(ShareFileItem::getName).collect(Collectors.toList());

                assertTrue(returnedNames.containsAll(expectedNames),
                    "Returned names do not match expected names: " + expectedNames);
                assertTrue(expectedNames.containsAll(returnedNames),
                    "Expected names do not match returned names: " + returnedNames);
            })).expectNextCount(1).verifyComplete();
    }

    @Test
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-12-02")
    public void listFilesAndDirectoriesEncoded() {
        Mono<ShareDirectoryInfo> createPrimaryDirMono = primaryDirectoryAsyncClient.create();
        Mono<ShareDirectoryAsyncClient> parentDirMono = createPrimaryDirMono
            .then(Mono.just(primaryDirectoryAsyncClient.getDirectoryAsyncClient(generatePathName())));
        String specialCharDirectoryName = "directory\uFFFE";
        String specialCharFileName = "file\uFFFE";

        Mono<ShareDirectoryAsyncClient> createMono = parentDirMono.flatMap(parentDir -> parentDir.deleteIfExists()
            .then(parentDir.create())
            .then(parentDir.getSubdirectoryClient(specialCharDirectoryName).deleteIfExists())
            .then(parentDir.getFileClient(specialCharFileName).deleteIfExists())
            .then(parentDir.createSubdirectory(specialCharDirectoryName))
            .then(parentDir.createFile(specialCharFileName, 1024))
            .then(Mono.just(parentDir)));
        Mono<List<ShareFileItem>> listFilesAndDirectoriesMono
            = createMono.then(parentDirMono.flatMap(parentDir -> parentDir.listFilesAndDirectories().collectList()));

        StepVerifier.create(listFilesAndDirectoriesMono).assertNext(shareFileItems -> {
            assertEquals(2, shareFileItems.size());
            assertTrue(shareFileItems.get(0).isDirectory());
            assertEquals(specialCharDirectoryName, shareFileItems.get(0).getName());
            assertFalse(shareFileItems.get(1).isDirectory());
            assertEquals(specialCharFileName, shareFileItems.get(1).getName());
        }).verifyComplete();
    }

    @Test
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-12-02")
    public void listFilesAndDirectoriesEncodedContinuationToken() {
        String specialCharFileName0 = "file0\uFFFE";
        String specialCharFileName1 = "file1\uFFFE";

        Mono<ShareDirectoryAsyncClient> parentDirMono = primaryDirectoryAsyncClient.create()
            .then(Mono.just(primaryDirectoryAsyncClient.getDirectoryAsyncClient(generatePathName())));

        Mono<Void> createFilesMono = parentDirMono.flatMap(parentDir -> parentDir.deleteIfExists()
            .then(parentDir.create())
            .then(parentDir.getFileClient(specialCharFileName0).deleteIfExists())
            .then(parentDir.getFileClient(specialCharFileName1).deleteIfExists())
            .then(parentDir.createFile(specialCharFileName0, 1024).then())
            .then(parentDir.createFile(specialCharFileName1, 1024).then()));

        Mono<List<ShareFileItem>> listFilesMono = createFilesMono
            .then(parentDirMono.flatMap(parentDir -> parentDir.listFilesAndDirectories().collectList()));

        StepVerifier.create(listFilesMono).assertNext(shareFileItems -> {
            assertEquals(specialCharFileName0, shareFileItems.get(0).getName());
            assertEquals(specialCharFileName1, shareFileItems.get(1).getName());
        }).verifyComplete();
    }

    @Test
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-12-02")
    public void listFilesAndDirectoriesEncodedPrefix() {
        String specialCharDirectoryName = "directory\uFFFE";

        Mono<ShareDirectoryAsyncClient> createMono = primaryDirectoryAsyncClient.create()
            .then(primaryDirectoryAsyncClient.createSubdirectory(specialCharDirectoryName)
                .thenReturn(primaryDirectoryAsyncClient));

        Mono<List<ShareFileItem>> listFilesAndDirectoriesMono
            = createMono.then(primaryDirectoryAsyncClient.listFilesAndDirectories().collectList());

        StepVerifier.create(listFilesAndDirectoriesMono).assertNext(shareFileItems -> {
            assertEquals(1, shareFileItems.size());
            assertTrue(shareFileItems.get(0).isDirectory());
            assertEquals(specialCharDirectoryName, shareFileItems.get(0).getName());
        }).verifyComplete();
    }

    @Test
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    public void testListFilesAndDirectoriesOAuth() {
        ShareDirectoryAsyncClient dirClient
            = getOAuthServiceAsyncClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP))
                .getShareAsyncClient(shareName)
                .getDirectoryClient(generatePathName());

        Mono<ShareDirectoryInfo> createDirMono = dirClient.create();

        List<String> fileNames = IntStream.range(0, 11).mapToObj(i -> generatePathName()).collect(Collectors.toList());
        List<String> dirNames = IntStream.range(0, 5).mapToObj(i -> generatePathName()).collect(Collectors.toList());

        Mono<Void> createFilesMono
            = Flux.fromIterable(fileNames).flatMap(fileName -> dirClient.createFile(fileName, Constants.KB)).then();

        Mono<Void> createDirsMono
            = Flux.fromIterable(dirNames).flatMap(dirName -> dirClient.createSubdirectory(dirName)).then();

        Mono<List<ShareFileItem>> listFilesAndDirsMono = createDirMono.then(createFilesMono)
            .then(createDirsMono)
            .then(dirClient.listFilesAndDirectories().collectList());

        StepVerifier.create(listFilesAndDirsMono).assertNext(shareFileItems -> {
            List<String> foundFiles = shareFileItems.stream()
                .filter(item -> !item.isDirectory())
                .map(ShareFileItem::getName)
                .collect(Collectors.toList());

            List<String> foundDirectories = shareFileItems.stream()
                .filter(ShareFileItem::isDirectory)
                .map(ShareFileItem::getName)
                .collect(Collectors.toList());

            assertTrue(fileNames.containsAll(foundFiles));
            assertTrue(dirNames.containsAll(foundDirectories));
        }).verifyComplete();
    }

    @Test
    public void listMaxResultsByPage() {
        String dirPrefix = generatePathName();

        Mono<Void> createDirectoriesAndFiles
            = primaryDirectoryAsyncClient.create().thenMany(Flux.range(0, 2).flatMap(i -> {
                ShareDirectoryAsyncClient subDirClient
                    = primaryDirectoryAsyncClient.getSubdirectoryClient(dirPrefix + i);
                return subDirClient.create().thenMany(Flux.range(0, 2).flatMap(j -> {
                    int num = i * 2 + j + 3;
                    return subDirClient.createFile(dirPrefix + num, 1024);
                }));
            })).then();

        StepVerifier
            .create(createDirectoriesAndFiles.thenMany(primaryDirectoryAsyncClient.listFilesAndDirectories(null, null)
                .byPage(4)
                .flatMapIterable(PagedResponse::getValue)))
            .expectNextCount(2)
            .verifyComplete();
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
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2022-11-02")
    public void listHandlesTrailingDot() {
        ShareAsyncClient shareAsyncClient = getShareAsyncClient(shareName, true, null);
        String directoryName = generatePathName() + ".";
        ShareDirectoryAsyncClient directoryClient = shareAsyncClient.getDirectoryClient(directoryName);

        Mono<ShareDirectoryInfo> createMono = directoryClient.create();
        Mono<List<HandleItem>> listHandlesMono
            = createMono.then(directoryClient.listHandles(null, false).collectList());

        StepVerifier.create(listHandlesMono).assertNext(handles -> assertEquals(0, handles.size())).verifyComplete();
    }

    @Test
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    public void listHandlesOAuth() {
        ShareServiceAsyncClient oAuthServiceClient
            = getOAuthServiceAsyncClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareDirectoryAsyncClient dirClient
            = oAuthServiceClient.getShareAsyncClient(shareName).getDirectoryClient(generatePathName());

        Mono<ShareDirectoryInfo> createMono = dirClient.create();
        Mono<List<HandleItem>> listHandlesMono = createMono.then(dirClient.listHandles(2, true).collectList());

        StepVerifier.create(listHandlesMono).assertNext(handles -> assertEquals(0, handles.size())).verifyComplete();
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

    @Test
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    public void forceCloseHandleOAuth() {
        ShareServiceAsyncClient oAuthServiceClient
            = getOAuthServiceAsyncClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareDirectoryAsyncClient dirClient
            = oAuthServiceClient.getShareAsyncClient(shareName).getDirectoryClient(generatePathName());

        Mono<CloseHandlesInfo> closeHandlesInfoMono = dirClient.create().then(dirClient.forceCloseHandle("1"));

        StepVerifier.create(closeHandlesInfoMono).assertNext(handlesClosedInfo -> {
            assertEquals(0, handlesClosedInfo.getClosedHandles());
            assertEquals(0, handlesClosedInfo.getFailedHandles());
        }).verifyComplete();
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
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2022-11-02")
    public void forceCloseAllHandlesTrailingDot() {
        ShareAsyncClient shareAsyncClient = getShareAsyncClient(shareName, true, null);
        String directoryName = generatePathName() + ".";
        ShareDirectoryAsyncClient directoryClient = shareAsyncClient.getDirectoryClient(directoryName);

        Mono<ShareDirectoryInfo> createMono = directoryClient.create();
        Mono<CloseHandlesInfo> closeHandlesInfoMono = createMono.then(directoryClient.forceCloseAllHandles(false));

        StepVerifier.create(closeHandlesInfoMono).assertNext(handlesClosedInfo -> {
            assertEquals(0, handlesClosedInfo.getClosedHandles());
            assertEquals(0, handlesClosedInfo.getFailedHandles());
        }).verifyComplete();
    }

    @Test
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    public void renameMin() {
        StepVerifier
            .create(primaryDirectoryAsyncClient.create()
                .then(primaryDirectoryAsyncClient.rename(generatePathName()).then()))
            .verifyComplete();
    }

    @Test
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    public void renameWithResponse() {
        Mono<ShareDirectoryInfo> createMono = primaryDirectoryAsyncClient.create();
        Mono<Response<ShareDirectoryAsyncClient>> renameMono = createMono
            .then(primaryDirectoryAsyncClient.renameWithResponse(new ShareFileRenameOptions(generatePathName()), null));

        StepVerifier.create(renameMono).assertNext(resp -> {
            ShareDirectoryAsyncClient renamedClient = resp.getValue();
            StepVerifier.create(renamedClient.getProperties()).expectNextCount(1).verifyComplete();
            StepVerifier.create(primaryDirectoryAsyncClient.getProperties()).verifyError(ShareStorageException.class);
        }).verifyComplete();
    }

    @Test
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    public void renameDifferentDirectory() {
        ShareAsyncClient shareAsyncClient = getShareAsyncClient(shareName, true, null);
        Mono<ShareDirectoryAsyncClient> createPrimaryDirMono
            = primaryDirectoryAsyncClient.create().thenReturn(primaryDirectoryAsyncClient);
        String destinationDirName = generatePathName();
        Mono<ShareDirectoryAsyncClient> createDestinationDirMono
            = shareAsyncClient.getDirectoryClient(destinationDirName)
                .create()
                .thenReturn(shareAsyncClient.getDirectoryClient(destinationDirName));

        Mono<Void> renameMono = createPrimaryDirMono.zipWith(createDestinationDirMono).flatMap(tuple -> {
            ShareDirectoryAsyncClient primaryDir = tuple.getT1();
            ShareDirectoryAsyncClient destinationDir = tuple.getT2();
            String destinationPath = destinationDir.getFileClient(generatePathName()).getFilePath();
            return primaryDir.rename(destinationPath).then();
        });

        StepVerifier.create(renameMono).verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void renameReplaceIfExists(boolean replaceIfExists) {
        Mono<ShareDirectoryInfo> createMono = primaryDirectoryAsyncClient.create();
        ShareFileAsyncClient destination = primaryDirectoryAsyncClient.getFileClient(generatePathName());
        Mono<ShareFileInfo> createFileMono = destination.create(512L);

        Mono<Boolean> renameMono = createMono.then(createFileMono)
            .then(primaryDirectoryAsyncClient
                .renameWithResponse(
                    new ShareFileRenameOptions(destination.getFilePath()).setReplaceIfExists(replaceIfExists), null)
                .thenReturn(replaceIfExists)
                .onErrorReturn(ShareStorageException.class, replaceIfExists));

        StepVerifier.create(renameMono).expectNext(replaceIfExists).verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void renameIgnoreReadOnly(boolean ignoreReadOnly) {
        Mono<ShareDirectoryInfo> createDirMono = primaryDirectoryAsyncClient.create();
        FileSmbProperties props
            = new FileSmbProperties().setNtfsFileAttributes(EnumSet.of(NtfsFileAttributes.READ_ONLY));
        ShareFileAsyncClient destinationFile = primaryDirectoryAsyncClient.getFileClient(generatePathName());
        Mono<ShareFileInfo> createFileMono
            = destinationFile.createWithResponse(512L, null, props, null, null, null, null, null)
                .map(Response::getValue);
        ShareFileRenameOptions options
            = new ShareFileRenameOptions(destinationFile.getFilePath()).setIgnoreReadOnly(ignoreReadOnly)
                .setReplaceIfExists(true);

        Mono<Boolean> renameMono = createDirMono.then(createFileMono)
            .then(primaryDirectoryAsyncClient.renameWithResponse(options, null)
                .thenReturn(!ignoreReadOnly)
                .onErrorReturn(ShareStorageException.class, !ignoreReadOnly));

        StepVerifier.create(renameMono).expectNext(!ignoreReadOnly).verifyComplete();
    }

    @Test
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    public void renameFilePermission() {
        Mono<ShareDirectoryInfo> createMono = primaryDirectoryAsyncClient.create();
        String filePermission
            = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-188441444-3053964)";
        ShareFileRenameOptions options
            = new ShareFileRenameOptions(generatePathName()).setFilePermission(filePermission);

        Mono<ShareDirectoryAsyncClient> renameMono
            = createMono.then(primaryDirectoryAsyncClient.renameWithResponse(options, null).map(Response::getValue));

        StepVerifier
            .create(renameMono.flatMap(renamedClient -> renamedClient.getProperties()
                .map(properties -> properties.getSmbProperties().getFilePermissionKey())))
            .assertNext(it -> assertNotNull(it))
            .verifyComplete();
    }

    @Test
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    public void renameFilePermissionAndKeySet() {
        Mono<ShareDirectoryInfo> createMono = primaryDirectoryAsyncClient.create();
        String filePermission
            = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-188441444-3053964)";
        ShareFileRenameOptions options
            = new ShareFileRenameOptions(generatePathName()).setFilePermission(filePermission)
                .setSmbProperties(new FileSmbProperties().setFilePermissionKey("filePermissionkey"));

        StepVerifier.create(createMono.then(primaryDirectoryAsyncClient.renameWithResponse(options, null)))
            .verifyError(ShareStorageException.class);
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
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    public void renameFileSmbProperties() {
        Mono<ShareDirectoryInfo> createMono = primaryDirectoryAsyncClient.create();
        String filePermission
            = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-188441444-3053964)";

        String permissionKey = shareClient.createPermission(filePermission);
        FileSmbProperties smbProperties
            = new FileSmbProperties().setNtfsFileAttributes(EnumSet.of(NtfsFileAttributes.DIRECTORY))
                .setFileCreationTime(testResourceNamer.now().minusDays(5))
                .setFileLastWriteTime(testResourceNamer.now().minusYears(2))
                .setFileChangeTime(testResourceNamer.now())
                .setFilePermissionKey(permissionKey);

        ShareFileRenameOptions options = new ShareFileRenameOptions(generatePathName()).setSmbProperties(smbProperties);

        Mono<ShareDirectoryAsyncClient> renameMono
            = createMono.then(primaryDirectoryAsyncClient.renameWithResponse(options, null).map(Response::getValue));

        StepVerifier.create(renameMono).assertNext(renamedClient -> {
            renamedClient.getProperties().subscribe(properties -> {
                FileSmbProperties destSmbProperties = properties.getSmbProperties();
                assertEquals(EnumSet.of(NtfsFileAttributes.DIRECTORY), destSmbProperties.getNtfsFileAttributes());
                assertNotNull(destSmbProperties.getFileCreationTime());
                assertNotNull(destSmbProperties.getFileLastWriteTime());
                FileShareTestHelper.compareDatesWithPrecision(destSmbProperties.getFileChangeTime(),
                    testResourceNamer.now());
            });
        }).verifyComplete();
    }

    @Test
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    public void renameMetadata() {
        Mono<ShareDirectoryInfo> createMono = primaryDirectoryAsyncClient.create();
        String key = "update";
        String value = "value";
        Map<String, String> updatedMetadata = Collections.singletonMap(key, value);
        ShareFileRenameOptions options = new ShareFileRenameOptions(generatePathName()).setMetadata(updatedMetadata);

        Mono<ShareDirectoryProperties> propertiesMono
            = createMono.then(primaryDirectoryAsyncClient.renameWithResponse(options, null)
                .map(Response::getValue)
                .flatMap(ShareDirectoryAsyncClient::getProperties));

        StepVerifier.create(propertiesMono).assertNext(properties -> {
            assertNotNull(properties.getMetadata().get(key));
            assertEquals(value, properties.getMetadata().get(key));
        }).verifyComplete();
    }

    @Test
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    public void renameOAuth() {
        ShareServiceAsyncClient oAuthServiceClient
            = getOAuthServiceAsyncClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareDirectoryAsyncClient dirClient
            = oAuthServiceClient.getShareAsyncClient(shareName).getDirectoryClient(generatePathName());

        Mono<ShareDirectoryAsyncClient> createMono = dirClient.create().thenReturn(dirClient);

        String dirRename = generatePathName();
        ShareFileRenameOptions options = new ShareFileRenameOptions(dirRename);

        Mono<ShareDirectoryAsyncClient> renameMono
            = createMono.flatMap(client -> client.renameWithResponse(options, null).map(Response::getValue));

        StepVerifier.create(renameMono).assertNext(renamedClient -> {
            assertDoesNotThrow(renamedClient::getProperties);
            assertEquals(dirRename, renamedClient.getDirectoryPath());
            assertThrows(ShareStorageException.class, dirClient::getProperties);
        }).verifyComplete();
    }

    @Test
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    public void renameError() {
        ShareDirectoryAsyncClient primaryDirectoryAsyncClient
            = getShareAsyncClient(shareName, null, null).getDirectoryClient(generatePathName());
        StepVerifier.create(primaryDirectoryAsyncClient.rename(generatePathName()))
            .verifyError(ShareStorageException.class);
    }

    @Test
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    public void renameDestAC() {
        // Create the primary directory asynchronously
        Mono<ShareDirectoryInfo> createDirectoryMono = primaryDirectoryAsyncClient.create();

        // Generate a unique path name for the new directory
        String newPathName = generatePathName();

        // Create the destination file asynchronously
        Mono<ShareFileAsyncClient> destFileMono = createDirectoryMono
            .then(Mono.defer(() -> Mono.just(primaryDirectoryAsyncClient.getFileClient(newPathName))));

        // Create the destination file
        Mono<ShareFileInfo> createFileMono = destFileMono.flatMap(destFile -> destFile.create(512));

        // Set up the lease condition after creating the file
        Mono<String> leaseIDMono = createFileMono
            .then(destFileMono
                .flatMap(destFile -> Mono.defer(() -> setupFileLeaseCondition(destFile, RECEIVED_LEASE_ID))))
            .onErrorResume(e -> Mono.empty());

        // Define the source request conditions with the lease ID
        Mono<ShareRequestConditions> srcMono
            = leaseIDMono.map(leaseID -> new ShareRequestConditions().setLeaseId(leaseID));

        // Perform the rename operation with retry logic
        Mono<Response<ShareDirectoryAsyncClient>> renameMono
            = srcMono.flatMap(src -> primaryDirectoryAsyncClient.renameWithResponse(
                new ShareFileRenameOptions(newPathName).setDestinationRequestConditions(src).setReplaceIfExists(true),
                null));

        // Verify the response status code
        StepVerifier.create(renameMono).assertNext(response -> {
            // Check the response status code, this should be 200 if the rename is successful
            FileShareTestHelper.assertResponseStatusCode(response, 200);
        }).expectComplete().verify();
    }

    @Test
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    public void renameDestACFail() {
        Mono<ShareDirectoryInfo> createMono = primaryDirectoryAsyncClient.create();
        String pathName = generatePathName();
        ShareFileAsyncClient destFile = primaryDirectoryAsyncClient.getFileClient(pathName);
        Mono<ShareFileInfo> createFileMono = destFile.create(512);
        Mono<String> leaseIDMono = createFileMono.then(setupFileLeaseCondition(destFile, GARBAGE_LEASE_ID));
        ShareRequestConditions src = new ShareRequestConditions().setLeaseId(GARBAGE_LEASE_ID);

        StepVerifier.create(createMono.then(leaseIDMono)
            .then(primaryDirectoryAsyncClient.renameWithResponse(
                new ShareFileRenameOptions(pathName).setDestinationRequestConditions(src).setReplaceIfExists(true),
                null)))
            .verifyError(ShareStorageException.class);
    }

    @Test
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-02-12")
    public void testRenameSASToken() {
        ShareFileSasPermission permissions = new ShareFileSasPermission().setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true);

        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);

        ShareServiceSasSignatureValues sasValues = new ShareServiceSasSignatureValues(expiryTime, permissions);

        Mono<ShareDirectoryInfo> createParentDirMono = primaryDirectoryAsyncClient.create();
        String sas = createParentDirMono.thenReturn(primaryDirectoryAsyncClient.generateSas(sasValues)).block();

        ShareDirectoryAsyncClient client = primaryDirectoryAsyncClient.getSubdirectoryClient(sas);

        Mono<ShareDirectoryAsyncClient> createMono = createParentDirMono.then(Mono.just(client));
        String directoryName = generatePathName();
        Mono<ShareDirectoryAsyncClient> renameMono = createMono.flatMap(dirClient -> dirClient.rename(directoryName));

        StepVerifier.create(renameMono).assertNext(destClient -> {
            assertNotNull(destClient);
            destClient.getProperties().subscribe(properties -> {
                assertEquals(directoryName, destClient.getDirectoryPath());
            });
        }).verifyComplete();
    }

    @Test
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2022-11-02")
    public void renameTrailingDot() {
        ShareAsyncClient shareClient = getShareAsyncClient(shareName, true, true);

        String directoryName = generatePathName() + ".";
        ShareDirectoryAsyncClient directoryClient = shareClient.getDirectoryClient(directoryName);

        Mono<ShareDirectoryAsyncClient> createAndRenameMono
            = directoryClient.create().then(directoryClient.rename(directoryName).thenReturn(directoryClient));

        StepVerifier.create(createAndRenameMono)
            .assertNext(client -> assertEquals(directoryName, client.getDirectoryPath()))
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
    public void createSubDirectoryFilePermissionKey() {
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
    public void createIfNotExistsSubDirectoryAlreadyExists() {
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
    public void createIfNotExistsSubDirectoryInvalidName() {
        StepVerifier
            .create(primaryDirectoryAsyncClient.create()
                .then(primaryDirectoryAsyncClient.createSubdirectoryIfNotExists("test/subdirectory")))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404,
                ShareErrorCode.PARENT_NOT_FOUND));
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
    public void testCreateIfNotExistsSubDirectoryFilePermissionKey() {
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
    public void testDeleteSubDirectory() {
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
    public void deleteFileError() {
        StepVerifier
            .create(primaryDirectoryAsyncClient.create()
                .then(primaryDirectoryAsyncClient.deleteFileWithResponse("testfile")))
            .verifyErrorSatisfies(e -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(
                (ShareStorageException) e, 404, ShareErrorCode.RESOURCE_NOT_FOUND));
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
    public void getSnapshotId() {
        String snapshot = OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 1, 1), ZoneOffset.UTC).toString();
        ShareDirectoryAsyncClient shareSnapshotClient
            = directoryBuilderHelper(shareName, directoryPath).snapshot(snapshot).buildDirectoryAsyncClient();
        assertEquals(snapshot, shareSnapshotClient.getShareSnapshotId());
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
    public void testPerCallPolicy() {
        Mono<ShareDirectoryInfo> createMono = primaryDirectoryAsyncClient.create();

        ShareDirectoryAsyncClient directoryClient = directoryBuilderHelper(primaryDirectoryAsyncClient.getShareName(),
            primaryDirectoryAsyncClient.getDirectoryPath()).addPolicy(getPerCallVersionPolicy())
                .buildDirectoryAsyncClient();

        Mono<Response<ShareDirectoryProperties>> responseMono
            = createMono.then(directoryClient.getPropertiesWithResponse());

        StepVerifier.create(responseMono)
            .assertNext(response -> assertEquals("2017-11-09", response.getHeaders().getValue(X_MS_VERSION)))
            .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "/" })
    public void rootDirectorySupport(String rootDirPath) {
        ShareAsyncClient shareAsyncClient = getShareAsyncClient(shareName, true, null);

        String dir1Name = "dir1";
        String dir2Name = "dir2";
        Mono<ShareDirectoryAsyncClient> createDirsMono = shareAsyncClient.createDirectory(dir1Name)
            .then(shareAsyncClient.getDirectoryClient(dir1Name).createSubdirectory(dir2Name));

        ShareDirectoryAsyncClient rootDirectory = shareAsyncClient.getDirectoryClient(rootDirPath);

        StepVerifier.create(createDirsMono.then(rootDirectory.exists()))
            .assertNext(exists -> assertTrue(exists))
            .verifyComplete();

        StepVerifier.create(rootDirectory.getSubdirectoryClient(dir1Name).exists())
            .assertNext(exists -> assertTrue(exists))
            .verifyComplete();
    }

    @Test
    public void createShareWithSmallTimeoutsFailForServiceAsyncClient() {
        int maxRetries = 5;
        long retryDelayMillis = 1000;

        Mono<Response<ShareAsyncClient>> testMono = Mono.defer(() -> {
            HttpClientOptions clientOptions = new HttpClientOptions().setApplicationId("client-options-id")
                .setResponseTimeout(Duration.ofNanos(1))
                .setReadTimeout(Duration.ofNanos(1))
                .setWriteTimeout(Duration.ofNanos(1))
                .setConnectTimeout(Duration.ofNanos(1));

            ShareServiceClientBuilder clientBuilder
                = new ShareServiceClientBuilder().endpoint(ENVIRONMENT.getPrimaryAccount().getBlobEndpoint())
                    .credential(ENVIRONMENT.getPrimaryAccount().getCredential())
                    .retryOptions(new RequestRetryOptions(null, 1, (Integer) null, null, null, null))
                    .clientOptions(clientOptions);

            ShareServiceAsyncClient serviceAsyncClient = clientBuilder.buildAsyncClient();

            return serviceAsyncClient.createShareWithResponse(generateShareName(), null).doOnSuccess(response -> {
                throw new RuntimeException("Expected exception not thrown");
            }).onErrorResume(e -> Mono.empty());
        });

        StepVerifier.create(testMono.retryWhen(Retry.fixedDelay(maxRetries, Duration.ofMillis(retryDelayMillis))))
            .verifyComplete();
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

    @Test
    public void audienceError() {
        String dirName = generatePathName();
        ShareDirectoryAsyncClient dirClient = directoryBuilderHelper(shareName, dirName).buildDirectoryAsyncClient();
        Mono<ShareDirectoryInfo> createDirMono = dirClient.create();

        ShareServiceAsyncClient oAuthServiceClient
            = getOAuthServiceAsyncClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP)
                .audience(ShareAudience.createShareServiceAccountAudience("badAudience")));

        ShareDirectoryAsyncClient aadDirClient
            = oAuthServiceClient.getShareAsyncClient(shareName).getDirectoryClient(dirName);

        StepVerifier.create(createDirMono.then(aadDirClient.exists())).verifyErrorSatisfies(r -> {
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
        Mono<ShareDirectoryInfo> createDirMono = dirClient.create();
        ShareServiceAsyncClient oAuthServiceClient = getOAuthServiceAsyncClient(
            new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP).audience(audience));

        ShareDirectoryAsyncClient aadDirClient
            = oAuthServiceClient.getShareAsyncClient(shareName).getDirectoryClient(dirName);

        StepVerifier.create(createDirMono.then(aadDirClient.exists())).expectNext(true).verifyComplete();
    }
}
