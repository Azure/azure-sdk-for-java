// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.Context;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.file.share.models.ShareFileRange;
import com.azure.storage.file.share.models.ShareFileUploadOptions;
import com.azure.storage.file.share.models.ShareFileUploadRangeOptions;
import com.azure.storage.file.share.options.ShareFileCopyOptions;
import com.azure.storage.file.share.options.ShareFileCreateHardLinkOptions;
import com.azure.storage.file.share.options.ShareFileCreateOptions;
import com.azure.storage.file.share.options.ShareFileCreateSymbolicLinkOptions;
import com.azure.storage.file.share.options.ShareFileDownloadOptions;
import com.azure.storage.file.share.options.ShareFileListRangesDiffOptions;
import com.azure.storage.file.share.options.ShareFileListRangesOptions;
import com.azure.storage.file.share.options.ShareFileRenameOptions;
import com.azure.storage.file.share.options.ShareFileSetPropertiesOptions;
import com.azure.storage.file.share.options.ShareFileUploadRangeFromUrlOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Temporary file-ID restrictions; remove the corresponding assertions as operations become supported.
@SuppressWarnings("deprecation")
class FileIdUnsupportedOperationsTests {
    @DoNotRecord
    @Test
    public void fileIdClientsRejectPathOperations() {
        AtomicReference<Boolean> requestSent = new AtomicReference<>(false);
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(request -> {
            requestSent.set(true);
            return Mono.just(new MockHttpResponse(request, 200));
        }).build();

        ShareClient testShareClient = new ShareServiceClientBuilder().endpoint(FileIdTestHelper.ENDPOINT)
            .pipeline(pipeline)
            .serviceVersion(ShareServiceVersion.V2027_03_07)
            .buildClient()
            .getShareClient(FileIdTestHelper.SHARE_NAME);
        ShareFileClient fileClient = testShareClient.getFileClientByFileId(FileIdTestHelper.FILE_ID);

        Assertions.assertThrows(IllegalStateException.class, fileClient::exists);
        IllegalStateException createException
            = Assertions.assertThrows(IllegalStateException.class, () -> fileClient.create(1024));
        Assertions.assertEquals("create is not supported for a file-ID-addressed client.",
            createException.getMessage());
        Assertions.assertThrows(IllegalStateException.class, fileClient::delete);
        Assertions.assertThrows(IllegalStateException.class, () -> fileClient.setMetadata(null));
        Assertions.assertThrows(IllegalStateException.class, () -> fileClient.rename("destination"));
        Assertions.assertThrows(IllegalStateException.class, fileClient::getFileOutputStream);
        Assertions.assertThrows(IllegalStateException.class, () -> fileClient.generateSas(null));
        assertThrows(IllegalStateException.class, () -> fileClient.existsWithResponse(null, Context.NONE));
        assertThrows(IllegalStateException.class,
            () -> fileClient.createWithResponse(new ShareFileCreateOptions(1024), null, Context.NONE));
        assertThrows(IllegalStateException.class, () -> fileClient.deleteWithResponse(null, null, Context.NONE));
        assertThrows(IllegalStateException.class, fileClient::deleteIfExists);
        assertThrows(IllegalStateException.class,
            () -> fileClient.deleteIfExistsWithResponse(null, null, Context.NONE));
        String sourceUrl = FileIdTestHelper.ENDPOINT + "/" + FileIdTestHelper.SHARE_NAME + "/source";
        assertThrows(IllegalStateException.class,
            () -> fileClient.beginCopy(sourceUrl, Collections.emptyMap(), null).poll());
        assertThrows(IllegalStateException.class,
            () -> fileClient.beginCopy(sourceUrl, new ShareFileCopyOptions(), null).poll());
        assertThrows(IllegalStateException.class, () -> fileClient.abortCopy("copy-id"));
        assertThrows(IllegalStateException.class,
            () -> fileClient.abortCopyWithResponse("copy-id", null, null, Context.NONE));
        assertThrows(IllegalStateException.class, () -> fileClient.setProperties(1024, null, null, null));
        assertThrows(IllegalStateException.class,
            () -> fileClient.setPropertiesWithResponse(new ShareFileSetPropertiesOptions(1024), null, Context.NONE));
        assertThrows(IllegalStateException.class,
            () -> fileClient.setMetadataWithResponse(null, null, null, Context.NONE));
        assertThrows(IllegalStateException.class, () -> fileClient.uploadRangeFromUrl(1, 0, 0, sourceUrl));
        assertThrows(IllegalStateException.class, () -> fileClient
            .uploadRangeFromUrlWithResponse(new ShareFileUploadRangeFromUrlOptions(1, sourceUrl), null, Context.NONE));
        assertThrows(IllegalStateException.class, () -> fileClient.clearRange(1));
        assertThrows(IllegalStateException.class,
            () -> fileClient.clearRangeWithResponse(1, 0, null, null, Context.NONE));
        assertThrows(IllegalStateException.class, () -> fileClient.listRanges().iterator().hasNext());
        assertThrows(IllegalStateException.class,
            () -> fileClient.listRanges(new ShareFileRange(0, 0L), null, null, Context.NONE).iterator().hasNext());
        assertThrows(IllegalStateException.class, () -> fileClient.listRangesDiff("snapshot"));
        assertThrows(IllegalStateException.class, () -> fileClient
            .listRangesDiffWithResponse(new ShareFileListRangesDiffOptions("snapshot"), null, Context.NONE));
        assertThrows(IllegalStateException.class, () -> fileClient.listAllRanges().iterator().hasNext());
        assertThrows(IllegalStateException.class,
            () -> fileClient.listAllRanges(new ShareFileListRangesOptions(), null, Context.NONE).iterator().hasNext());
        assertThrows(IllegalStateException.class, () -> fileClient.listAllRangesDiff("snapshot").iterator().hasNext());
        assertThrows(IllegalStateException.class,
            () -> fileClient.listAllRangesDiff(new ShareFileListRangesDiffOptions("snapshot"), null, Context.NONE)
                .iterator()
                .hasNext());
        assertThrows(IllegalStateException.class, () -> fileClient.listHandles().iterator().hasNext());
        assertThrows(IllegalStateException.class,
            () -> fileClient.listHandles(1, null, Context.NONE).iterator().hasNext());
        assertThrows(IllegalStateException.class, () -> fileClient.forceCloseHandle("handle"));
        assertThrows(IllegalStateException.class,
            () -> fileClient.forceCloseHandleWithResponse("handle", null, Context.NONE));
        assertThrows(IllegalStateException.class, () -> fileClient.forceCloseAllHandles(null, Context.NONE));
        assertThrows(IllegalStateException.class,
            () -> fileClient.renameWithResponse(new ShareFileRenameOptions("destination"), null, Context.NONE));
        assertThrows(IllegalStateException.class, () -> fileClient.createHardLink("target"));
        assertThrows(IllegalStateException.class, () -> fileClient
            .createHardLinkWithResponse(new ShareFileCreateHardLinkOptions("target"), null, Context.NONE));
        assertThrows(IllegalStateException.class, () -> fileClient.createSymbolicLink("target"));
        assertThrows(IllegalStateException.class, () -> fileClient
            .createSymbolicLinkWithResponse(new ShareFileCreateSymbolicLinkOptions("target"), null, Context.NONE));
        assertThrows(IllegalStateException.class, fileClient::getSymbolicLink);
        assertThrows(IllegalStateException.class, () -> fileClient.getSymbolicLinkWithResponse(null, Context.NONE));
        assertThrows(IllegalStateException.class, () -> fileClient.generateSas(null, null, Context.NONE));
        assertThrows(IllegalStateException.class, () -> fileClient.generateUserDelegationSas(null, null));
        assertThrows(IllegalStateException.class,
            () -> fileClient.generateUserDelegationSas(null, null, null, Context.NONE));
        assertThrows(IllegalStateException.class, fileClient::openInputStream);
        assertThrows(IllegalStateException.class, () -> fileClient.openInputStream(new ShareFileRange(0, 0L)));
        assertThrows(IllegalStateException.class, () -> fileClient.getFileOutputStream(0));
        assertThrows(IllegalStateException.class, () -> fileClient.getFileSeekableByteChannelRead(null));
        assertThrows(IllegalStateException.class, () -> fileClient.getFileSeekableByteChannelWrite(null));
        Assertions.assertFalse(requestSent.get());
    }

    @DoNotRecord
    @Test
    public void asyncFileIdClientsRejectPathOperations() {
        AtomicReference<Boolean> requestSent = new AtomicReference<>(false);
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(request -> {
            requestSent.set(true);
            return Mono.just(new MockHttpResponse(request, 200));
        }).build();

        ShareAsyncClient testShareClient = new ShareServiceClientBuilder().endpoint(FileIdTestHelper.ENDPOINT)
            .pipeline(pipeline)
            .serviceVersion(ShareServiceVersion.V2027_03_07)
            .buildAsyncClient()
            .getShareAsyncClient(FileIdTestHelper.SHARE_NAME);
        ShareFileAsyncClient fileClient = testShareClient.getFileClientByFileId(FileIdTestHelper.FILE_ID);

        IllegalStateException createException
            = Assertions.assertThrows(IllegalStateException.class, () -> fileClient.create(1024).block());
        Assertions.assertEquals("create is not supported for a file-ID-addressed client.",
            createException.getMessage());
        Assertions.assertThrows(IllegalStateException.class, () -> fileClient.delete().block());
        Assertions.assertThrows(IllegalStateException.class, () -> fileClient.exists().block());
        Assertions.assertThrows(IllegalStateException.class, () -> fileClient.download().blockFirst());
        Assertions.assertThrows(IllegalStateException.class, () -> fileClient.listRanges().blockFirst());
        Assertions.assertThrows(IllegalStateException.class, () -> fileClient.setMetadata(null).block());
        Assertions.assertThrows(IllegalStateException.class, () -> fileClient.rename("destination").block());
        Assertions.assertThrows(IllegalStateException.class, () -> fileClient.forceCloseAllHandles().block());
        StepVerifier.create(fileClient.existsWithResponse()).verifyError(IllegalStateException.class);
        StepVerifier.create(fileClient.createWithResponse(new ShareFileCreateOptions(1024)))
            .verifyError(IllegalStateException.class);
        StepVerifier.create(fileClient.deleteWithResponse(null)).verifyError(IllegalStateException.class);
        StepVerifier.create(fileClient.deleteIfExists()).verifyError(IllegalStateException.class);
        StepVerifier.create(fileClient.deleteIfExistsWithResponse(null)).verifyError(IllegalStateException.class);
        String sourceUrl = FileIdTestHelper.ENDPOINT + "/" + FileIdTestHelper.SHARE_NAME + "/source";
        Assertions.assertThrows(IllegalStateException.class,
            () -> fileClient.beginCopy(sourceUrl, Collections.emptyMap(), null));
        Assertions.assertThrows(IllegalStateException.class,
            () -> fileClient.beginCopy(sourceUrl, new ShareFileCopyOptions(), null));
        StepVerifier.create(fileClient.abortCopy("copy-id")).verifyError(IllegalStateException.class);
        StepVerifier.create(fileClient.abortCopyWithResponse("copy-id", null)).verifyError(IllegalStateException.class);
        StepVerifier.create(fileClient.setProperties(1024, null, null, null)).verifyError(IllegalStateException.class);
        StepVerifier.create(fileClient.setPropertiesWithResponse(new ShareFileSetPropertiesOptions(1024)))
            .verifyError(IllegalStateException.class);
        StepVerifier.create(fileClient.setMetadataWithResponse(null, null)).verifyError(IllegalStateException.class);
        StepVerifier.create(fileClient.uploadRangeFromUrl(1, 0, 0, sourceUrl)).verifyError(IllegalStateException.class);
        StepVerifier
            .create(fileClient.uploadRangeFromUrlWithResponse(new ShareFileUploadRangeFromUrlOptions(1, sourceUrl)))
            .verifyError(IllegalStateException.class);
        StepVerifier.create(fileClient.clearRange(1)).verifyError(IllegalStateException.class);
        StepVerifier.create(fileClient.clearRangeWithResponse(1, 0, null)).verifyError(IllegalStateException.class);
        StepVerifier.create(fileClient.listRanges(new ShareFileRange(0, 0L), null))
            .verifyError(IllegalStateException.class);
        StepVerifier.create(fileClient.listRangesDiff("snapshot")).verifyError(IllegalStateException.class);
        StepVerifier.create(fileClient.listRangesDiffWithResponse(new ShareFileListRangesDiffOptions("snapshot")))
            .verifyError(IllegalStateException.class);
        StepVerifier.create(fileClient.listAllRanges()).verifyError(IllegalStateException.class);
        StepVerifier.create(fileClient.listAllRanges(new ShareFileListRangesOptions()))
            .verifyError(IllegalStateException.class);
        StepVerifier.create(fileClient.listAllRangesDiff("snapshot")).verifyError(IllegalStateException.class);
        StepVerifier.create(fileClient.listAllRangesDiff(new ShareFileListRangesDiffOptions("snapshot")))
            .verifyError(IllegalStateException.class);
        StepVerifier.create(fileClient.listHandles()).verifyError(IllegalStateException.class);
        StepVerifier.create(fileClient.listHandles(1)).verifyError(IllegalStateException.class);
        StepVerifier.create(fileClient.forceCloseHandle("handle")).verifyError(IllegalStateException.class);
        StepVerifier.create(fileClient.forceCloseHandleWithResponse("handle")).verifyError(IllegalStateException.class);
        StepVerifier.create(fileClient.renameWithResponse(new ShareFileRenameOptions("destination")))
            .verifyError(IllegalStateException.class);
        StepVerifier.create(fileClient.createHardLink("target")).verifyError(IllegalStateException.class);
        StepVerifier.create(fileClient.createHardLinkWithResponse(new ShareFileCreateHardLinkOptions("target")))
            .verifyError(IllegalStateException.class);
        StepVerifier.create(fileClient.createSymbolicLink("target")).verifyError(IllegalStateException.class);
        StepVerifier.create(fileClient.createSymbolicLinkWithResponse(new ShareFileCreateSymbolicLinkOptions("target")))
            .verifyError(IllegalStateException.class);
        StepVerifier.create(fileClient.getSymbolicLink()).verifyError(IllegalStateException.class);
        StepVerifier.create(fileClient.getSymbolicLinkWithResponse()).verifyError(IllegalStateException.class);
        Assertions.assertThrows(IllegalStateException.class, () -> fileClient.generateSas(null));
        Assertions.assertThrows(IllegalStateException.class, () -> fileClient.generateSas(null, null, Context.NONE));
        Assertions.assertThrows(IllegalStateException.class, () -> fileClient.generateUserDelegationSas(null, null));
        Assertions.assertThrows(IllegalStateException.class,
            () -> fileClient.generateUserDelegationSas(null, null, null, Context.NONE));
        Assertions.assertFalse(requestSent.get());
    }

    @DoNotRecord
    @ParameterizedTest
    @ValueSource(ints = { -1, 0, 3 })
    public void fileIdTransfersRejectBeforeAccessingData(int fileSize, @TempDir Path directory) throws IOException {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(request -> {
            throw new AssertionError("File-ID transfers must not send a request.");
        }).build();
        ShareFileClient fileClient = new ShareFileClientBuilder().endpoint(FileIdTestHelper.ENDPOINT)
            .shareName(FileIdTestHelper.SHARE_NAME)
            .fileId(FileIdTestHelper.FILE_ID)
            .pipeline(pipeline)
            .buildFileClient();
        InputStream input = new InputStream() {
            @Override
            public int read() {
                throw new AssertionError("Rejected uploads must not read input.");
            }
        };
        assertThrows(IllegalStateException.class, () -> fileClient.upload(input, 1));
        assertThrows(IllegalStateException.class, () -> fileClient.upload(input, 1, new ParallelTransferOptions()));
        assertThrows(IllegalStateException.class,
            () -> fileClient.uploadWithResponse(input, 1, 0L, null, null, Context.NONE));
        assertThrows(IllegalStateException.class,
            () -> fileClient.uploadWithResponse(new ShareFileUploadOptions(input, 1), null, Context.NONE));
        assertThrows(IllegalStateException.class, () -> fileClient.uploadRange(input, 1));
        assertThrows(IllegalStateException.class,
            () -> fileClient.uploadRangeWithResponse(new ShareFileUploadRangeOptions(input, 1), null, Context.NONE));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertThrows(IllegalStateException.class, () -> fileClient.download(output));
        assertThrows(IllegalStateException.class,
            () -> fileClient.downloadWithResponse(output, new ShareFileDownloadOptions(), null, Context.NONE));
        assertEquals(0, output.size());

        Path path = directory.resolve("transfer");
        byte[] content = new byte[Math.max(fileSize, 0)];
        Arrays.fill(content, (byte) 42);
        if (fileSize >= 0) {
            Files.write(path, content);
        }
        assertThrows(IllegalStateException.class, () -> fileClient.downloadToFile(path.toString()));
        assertThrows(IllegalStateException.class,
            () -> fileClient.downloadToFileWithResponse(path.toString(), null, null, null, Context.NONE));
        assertThrows(IllegalStateException.class, () -> fileClient.uploadFromFile(path.toString()));
        assertThrows(IllegalStateException.class, () -> fileClient.uploadFromFile(path.toString(), null));
        if (fileSize < 0) {
            assertFalse(Files.exists(path));
        } else {
            assertArrayEquals(content, Files.readAllBytes(path));
        }
    }

    @DoNotRecord
    @ParameterizedTest
    @ValueSource(ints = { -1, 0, 3 })
    public void asyncFileIdTransfersRejectBeforeAccessingData(int fileSize, @TempDir Path directory)
        throws IOException {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(request -> {
            throw new AssertionError("File-ID transfers must not send a request.");
        }).build();
        ShareFileAsyncClient fileClient = new ShareFileClientBuilder().endpoint(FileIdTestHelper.ENDPOINT)
            .shareName(FileIdTestHelper.SHARE_NAME)
            .fileId(FileIdTestHelper.FILE_ID)
            .pipeline(pipeline)
            .buildFileAsyncClient();
        Flux<ByteBuffer> input = Flux.defer(() -> {
            throw new AssertionError("Rejected uploads must not subscribe to input.");
        });
        StepVerifier.create(fileClient.upload(input, 1)).verifyError(IllegalStateException.class);
        StepVerifier.create(fileClient.upload(input, new ParallelTransferOptions()))
            .verifyError(IllegalStateException.class);
        StepVerifier.create(fileClient.uploadWithResponse(input, 1, 0L, null)).verifyError(IllegalStateException.class);
        StepVerifier.create(fileClient.uploadWithResponse(new ShareFileUploadOptions(input)))
            .verifyError(IllegalStateException.class);
        StepVerifier.create(fileClient.uploadRange(input, 1)).verifyError(IllegalStateException.class);
        StepVerifier.create(fileClient.uploadRangeWithResponse(new ShareFileUploadRangeOptions(input, 1)))
            .verifyError(IllegalStateException.class);
        StepVerifier.create(fileClient.downloadWithResponse(new ShareFileDownloadOptions()))
            .verifyError(IllegalStateException.class);

        Path path = directory.resolve("transfer");
        byte[] content = new byte[Math.max(fileSize, 0)];
        Arrays.fill(content, (byte) 42);
        if (fileSize >= 0) {
            Files.write(path, content);
        }
        StepVerifier.create(fileClient.downloadToFile(path.toString())).verifyError(IllegalStateException.class);
        StepVerifier.create(fileClient.downloadToFileWithResponse(path.toString(), null, null))
            .verifyError(IllegalStateException.class);
        StepVerifier.create(fileClient.uploadFromFile(path.toString())).verifyError(IllegalStateException.class);
        StepVerifier.create(fileClient.uploadFromFile(path.toString(), null)).verifyError(IllegalStateException.class);
        if (fileSize < 0) {
            assertFalse(Files.exists(path));
        } else {
            assertArrayEquals(content, Files.readAllBytes(path));
        }
    }

    @DoNotRecord
    @Test
    public void directoryFileIdClientsRejectPathOperations() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(request -> {
            throw new AssertionError("Path operations must be rejected before sending a request.");
        }).build();
        ShareDirectoryClient directoryClient = new ShareServiceClientBuilder().endpoint(FileIdTestHelper.ENDPOINT)
            .pipeline(pipeline)
            .buildClient()
            .getShareClient(FileIdTestHelper.SHARE_NAME)
            .getDirectoryClientByFileId(FileIdTestHelper.FILE_ID);

        IllegalStateException existsException = assertThrows(IllegalStateException.class, directoryClient::exists);
        assertEquals("exists is not supported for a file-ID-addressed client.", existsException.getMessage());
        assertThrows(IllegalStateException.class, () -> directoryClient.existsWithResponse(null, Context.NONE));
        IllegalStateException exception
            = assertThrows(IllegalStateException.class, () -> directoryClient.getFileClient("child"));
        assertEquals("getFileClient is not supported for a file-ID-addressed client.", exception.getMessage());
        assertThrows(IllegalStateException.class, () -> directoryClient.getSubdirectoryClient("child"));
        assertThrows(IllegalStateException.class, directoryClient::create);
        assertThrows(IllegalStateException.class, directoryClient::createIfNotExists);
        assertThrows(IllegalStateException.class, directoryClient::delete);
        assertThrows(IllegalStateException.class, directoryClient::deleteIfExists);
        assertThrows(IllegalStateException.class, () -> directoryClient.setProperties(null, null));
        assertThrows(IllegalStateException.class, () -> directoryClient.setMetadata(null));
        assertThrows(IllegalStateException.class, () -> directoryClient.listFilesAndDirectories().iterator().hasNext());
        assertThrows(IllegalStateException.class,
            () -> directoryClient.listHandles(null, true, null, Context.NONE).iterator().hasNext());
        assertThrows(IllegalStateException.class, () -> directoryClient.forceCloseHandle("handle"));
        assertThrows(IllegalStateException.class, () -> directoryClient.forceCloseAllHandles(true, null, Context.NONE));
        assertThrows(IllegalStateException.class, () -> directoryClient.rename("destination"));
        assertThrows(IllegalStateException.class, () -> directoryClient.generateSas(null));
        assertThrows(IllegalStateException.class, () -> directoryClient.createSubdirectory("child"));
        assertThrows(IllegalStateException.class, () -> directoryClient.createSubdirectoryIfNotExists("child"));
        assertThrows(IllegalStateException.class, () -> directoryClient.deleteSubdirectory("child"));
        assertThrows(IllegalStateException.class, () -> directoryClient.deleteSubdirectoryIfExists("child"));
        assertThrows(IllegalStateException.class, () -> directoryClient.createFile("child", 1024));
        assertThrows(IllegalStateException.class, () -> directoryClient.deleteFile("child"));
        assertThrows(IllegalStateException.class, () -> directoryClient.deleteFileIfExists("child"));
    }

    @DoNotRecord
    @Test
    public void asyncDirectoryFileIdClientsRejectPathOperations() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(request -> {
            throw new AssertionError("Path operations must be rejected before sending a request.");
        }).build();
        ShareDirectoryAsyncClient directoryClient = new ShareServiceClientBuilder().endpoint(FileIdTestHelper.ENDPOINT)
            .pipeline(pipeline)
            .buildAsyncClient()
            .getShareAsyncClient(FileIdTestHelper.SHARE_NAME)
            .getDirectoryClientByFileId(FileIdTestHelper.FILE_ID);

        StepVerifier.create(directoryClient.exists()).verifyErrorSatisfies(error -> {
            assertInstanceOf(IllegalStateException.class, error);
            assertEquals("exists is not supported for a file-ID-addressed client.", error.getMessage());
        });
        StepVerifier.create(directoryClient.existsWithResponse()).verifyError(IllegalStateException.class);
        IllegalStateException exception
            = Assertions.assertThrows(IllegalStateException.class, () -> directoryClient.getFileClient("child"));
        assertEquals("getFileClient is not supported for a file-ID-addressed client.", exception.getMessage());
        Assertions.assertThrows(IllegalStateException.class, () -> directoryClient.getSubdirectoryClient("child"));
        StepVerifier.create(directoryClient.create()).verifyError(IllegalStateException.class);
        StepVerifier.create(directoryClient.createIfNotExists()).verifyError(IllegalStateException.class);
        StepVerifier.create(directoryClient.delete()).verifyError(IllegalStateException.class);
        StepVerifier.create(directoryClient.deleteIfExists()).verifyError(IllegalStateException.class);
        StepVerifier.create(directoryClient.setProperties(null, null)).verifyError(IllegalStateException.class);
        StepVerifier.create(directoryClient.setMetadata(null)).verifyError(IllegalStateException.class);
        StepVerifier.create(directoryClient.listFilesAndDirectories()).verifyError(IllegalStateException.class);
        StepVerifier.create(directoryClient.listHandles(null, true)).verifyError(IllegalStateException.class);
        StepVerifier.create(directoryClient.forceCloseHandle("handle")).verifyError(IllegalStateException.class);
        StepVerifier.create(directoryClient.forceCloseAllHandles(true)).verifyError(IllegalStateException.class);
        StepVerifier.create(directoryClient.rename("destination")).verifyError(IllegalStateException.class);
        Assertions.assertThrows(IllegalStateException.class, () -> directoryClient.generateSas(null));
        StepVerifier.create(directoryClient.createSubdirectory("child")).verifyError(IllegalStateException.class);
        StepVerifier.create(directoryClient.createSubdirectoryIfNotExists("child"))
            .verifyError(IllegalStateException.class);
        StepVerifier.create(directoryClient.deleteSubdirectory("child")).verifyError(IllegalStateException.class);
        StepVerifier.create(directoryClient.deleteSubdirectoryIfExists("child"))
            .verifyError(IllegalStateException.class);
        StepVerifier.create(directoryClient.createFile("child", 1024)).verifyError(IllegalStateException.class);
        StepVerifier.create(directoryClient.deleteFile("child")).verifyError(IllegalStateException.class);
        StepVerifier.create(directoryClient.deleteFileIfExists("child")).verifyError(IllegalStateException.class);
    }

    @DoNotRecord
    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void directoryClientBuilderRejectsFileId(boolean fromEndpoint) {
        AtomicReference<Boolean> requestSent = new AtomicReference<>(false);
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(request -> {
            requestSent.set(true);
            return Mono.just(new MockHttpResponse(request, 200));
        }).build();

        ShareFileClientBuilder builder = new ShareFileClientBuilder().endpoint(FileIdTestHelper.ENDPOINT)
            .shareName(FileIdTestHelper.SHARE_NAME)
            .pipeline(pipeline);
        if (fromEndpoint) {
            builder.endpoint(FileIdTestHelper.ENDPOINT + "/" + FileIdTestHelper.SHARE_NAME + "/directory?fileid="
                + FileIdTestHelper.FILE_ID);
        } else {
            builder.resourcePath("directory").fileId(FileIdTestHelper.FILE_ID);
        }
        IllegalStateException exception
            = Assertions.assertThrows(IllegalStateException.class, builder::buildDirectoryClient);
        Assertions.assertEquals("buildDirectoryClient is not supported for a file-ID-addressed client.",
            exception.getMessage());
        Assertions.assertEquals(FileIdTestHelper.FILE_ID, builder.buildFileClient().getFileId());
        Assertions.assertFalse(requestSent.get());
    }

    @DoNotRecord
    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void asyncDirectoryClientBuilderRejectsFileId(boolean fromEndpoint) {
        AtomicReference<Boolean> requestSent = new AtomicReference<>(false);
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(request -> {
            requestSent.set(true);
            return Mono.just(new MockHttpResponse(request, 200));
        }).build();

        ShareFileClientBuilder builder = new ShareFileClientBuilder().endpoint(FileIdTestHelper.ENDPOINT)
            .shareName(FileIdTestHelper.SHARE_NAME)
            .pipeline(pipeline);
        if (fromEndpoint) {
            builder.endpoint(FileIdTestHelper.ENDPOINT + "/" + FileIdTestHelper.SHARE_NAME + "/directory?fileid="
                + FileIdTestHelper.FILE_ID);
        } else {
            builder.resourcePath("directory").fileId(FileIdTestHelper.FILE_ID);
        }
        IllegalStateException exception
            = Assertions.assertThrows(IllegalStateException.class, builder::buildDirectoryAsyncClient);
        Assertions.assertEquals("buildDirectoryAsyncClient is not supported for a file-ID-addressed client.",
            exception.getMessage());
        Assertions.assertEquals(FileIdTestHelper.FILE_ID, builder.buildFileAsyncClient().getFileId());
        Assertions.assertFalse(requestSent.get());
    }
}
