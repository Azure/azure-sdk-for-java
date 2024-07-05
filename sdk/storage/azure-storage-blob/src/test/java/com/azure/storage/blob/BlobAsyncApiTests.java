// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.ProgressListener;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollerFlux;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.ArchiveStatus;
import com.azure.storage.blob.models.BlobAudience;
import com.azure.storage.blob.models.BlobBeginCopySourceRequestConditions;
import com.azure.storage.blob.models.BlobCopyInfo;
import com.azure.storage.blob.models.BlobDownloadHeaders;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.BlobType;
import com.azure.storage.blob.models.BlockListType;
import com.azure.storage.blob.models.CopyStatusType;
import com.azure.storage.blob.models.CustomerProvidedKey;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.models.DownloadRetryOptions;
import com.azure.storage.blob.models.LeaseStateType;
import com.azure.storage.blob.models.LeaseStatusType;
import com.azure.storage.blob.models.ObjectReplicationPolicy;
import com.azure.storage.blob.models.ObjectReplicationStatus;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.models.RehydratePriority;
import com.azure.storage.blob.options.BlobBeginCopyOptions;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import com.azure.storage.blob.options.BlobGetTagsOptions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.blob.options.BlobSetAccessTierOptions;
import com.azure.storage.blob.options.BlobSetTagsOptions;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.blob.specialized.AppendBlobAsyncClient;
import com.azure.storage.blob.specialized.BlobAsyncClientBase;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.blob.specialized.PageBlobAsyncClient;
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder;
import com.azure.storage.common.Utility;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.TestDataFactory;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import com.azure.storage.common.test.shared.extensions.PlaybackOnly;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import com.azure.storage.common.test.shared.policy.MockFailureResponsePolicy;
import com.azure.storage.common.test.shared.policy.MockRetryRangeResponsePolicy;
import com.azure.storage.common.test.shared.policy.TransientFailureInjectingHttpPipelinePolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlobAsyncApiTests extends BlobTestBase {
    private BlobAsyncClient bc;
    private final List<File> createdFiles = new ArrayList<>();

    @BeforeEach
    public void setup() {
        String blobName = generateBlobName();
        bc = ccAsync.getBlobAsyncClient(blobName);
        bc.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()).block();
    }

    @AfterEach
    public void cleanup() {
        createdFiles.forEach(File::delete);
    }

    @Test
    public void uploadFluxOverwriteFails() {
        StepVerifier.create(bc.upload(DATA.getDefaultFlux(), null))
            .verifyError(IllegalArgumentException.class);
    }

    @Test
    public void uploadBinaryDataOverwriteFails() {
        StepVerifier.create(bc.upload(DATA.getDefaultBinaryData()))
            .verifyError(IllegalArgumentException.class);
    }

    @Test
    public void uploadFluxOverwrite() {
        byte[] randomData = getRandomByteArray(Constants.KB);
        Flux<ByteBuffer> input = Flux.just(ByteBuffer.wrap(randomData));

        bc.upload(input, null, true).block();

        StepVerifier.create(bc.downloadStreamWithResponse(null, null, null,
            false)
            .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue())))
            .assertNext(r -> TestUtils.assertArraysEqual(r, randomData))
            .verifyComplete();
    }

    @Test
    public void uploadBinaryDataOverwrite() {
        byte[] randomData = getRandomByteArray(Constants.KB);

        bc.upload(BinaryData.fromBytes(randomData), true).block();

        StepVerifier.create(bc.downloadContent())
            .assertNext(r -> TestUtils.assertArraysEqual(r.toBytes(), randomData))
            .verifyComplete();
    }

    /* Tests an issue found where buffered upload would not deep copy buffers while determining what upload path to
    take. */
    @ParameterizedTest
    @ValueSource(ints = {
        Constants.KB, /* Less than copyToOutputStream buffer size, Less than maxSingleUploadSize */
        8 * Constants.KB, /* Equal to copyToOutputStream buffer size, Less than maxSingleUploadSize */
        20 * Constants.KB }) /* Greater than copyToOutputStream buffer size, Less than maxSingleUploadSize */
    public void uploadFluxSingleUpload() {
        byte[] randomData = getRandomByteArray(20 * Constants.KB);
        Flux<ByteBuffer> input = Flux.just(ByteBuffer.wrap(randomData));

        bc.upload(input, null, true).block();

        StepVerifier.create(bc.downloadStreamWithResponse(null, null, null,
            false)
            .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue())))
            .assertNext(r -> TestUtils.assertArraysEqual(r, randomData))
            .verifyComplete();
    }

    @LiveOnly
    @Test
    public void uploadFluxLargeData() {
        byte[] randomData = getRandomByteArray(20 * Constants.MB);
        Flux<ByteBuffer> input = Flux.just(ByteBuffer.wrap(randomData));

        ParallelTransferOptions pto = new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) Constants.MB);

        StepVerifier.create(bc.uploadWithResponse(input, pto, null, null, null, null))
            .expectNextCount(1)
            .verifyComplete();
    }

    @LiveOnly
    @ParameterizedTest
    @MethodSource("uploadNumBlocksSupplier")
    public void uploadNumBlocks(int size, Long maxUploadSize, long numBlocks) {
        byte[] randomData = getRandomByteArray(size);
        Flux<ByteBuffer> input = Flux.just(ByteBuffer.wrap(randomData));

        ParallelTransferOptions pto = new ParallelTransferOptions().setBlockSizeLong(maxUploadSize)
            .setMaxSingleUploadSizeLong(maxUploadSize);

        bc.uploadWithResponse(input, pto, null, null, null, null).block();

        StepVerifier.create(bc.getBlockBlobAsyncClient().listBlocks(BlockListType.ALL))
            .assertNext(r -> assertEquals(numBlocks, r.getCommittedBlocks().size()))
            .verifyComplete();
    }

    private static Stream<Arguments> uploadNumBlocksSupplier() {
        return Stream.of(
            Arguments.of(0, null, 0),
            Arguments.of(Constants.KB, null, 0), // default is MAX_UPLOAD_BYTES
            Arguments.of(Constants.MB, null, 0), // default is MAX_UPLOAD_BYTES
            Arguments.of(3 * Constants.MB, (long) Constants.MB, 3));
    }

    @Test
    public void uploadReturnValue() {
        StepVerifier.create(bc.uploadWithResponse(new BlobParallelUploadOptions(DATA.getDefaultInputStream())))
            .assertNext(r -> assertNotNull(r.getValue().getETag()))
            .verifyComplete();
    }

    @Test
    public void uploadReturnValueBinaryData() {
        StepVerifier.create(bc.uploadWithResponse(new BlobParallelUploadOptions(DATA.getDefaultBinaryData())))
            .assertNext(r -> assertNotNull(r.getValue().getETag()))
            .verifyComplete();
    }

    @Test
    public void uploadFluxMin() {
        bc = ccAsync.getBlobAsyncClient(generateBlobName());

        StepVerifier.create(bc.upload(DATA.getDefaultFlux(), null))
            .expectNextCount(1)
            .verifyComplete();

        StepVerifier.create(bc.downloadContent())
            .assertNext(r -> TestUtils.assertArraysEqual(r.toBytes(), DATA.getDefaultBytes()))
            .verifyComplete();
    }

    @Test
    public void uploadInputStreamNoLength() {
        StepVerifier.create(bc.uploadWithResponse(new BlobParallelUploadOptions(DATA.getDefaultInputStream())))
            .expectNextCount(1)
            .verifyComplete();

        StepVerifier.create(bc.downloadContent())
            .assertNext(r -> TestUtils.assertArraysEqual(r.toBytes(), DATA.getDefaultBytes()))
            .verifyComplete();
    }

    @Test
    public void uploadInputStreamBadLength() {
        long[] badLengths = {0, -100, DATA.getDefaultDataSize() - 1, DATA.getDefaultDataSize() + 1};
        for (long length : badLengths) {
            assertThrows(Exception.class, () -> bc.uploadWithResponse(
                new BlobParallelUploadOptions(DATA.getDefaultInputStream(), length)).block());
        }
    }

    @Test
    public void uploadSuccessfulRetry() {
        BlobAsyncClient clientWithFailure = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            bc.getBlobUrl(), new TransientFailureInjectingHttpPipelinePolicy());

        assertNotNull(clientWithFailure);
        clientWithFailure.uploadWithResponse(new BlobParallelUploadOptions(DATA.getDefaultInputStream())).block();
        StepVerifier.create(bc.downloadContent())
            .assertNext(r -> TestUtils.assertArraysEqual(r.toBytes(), DATA.getDefaultBytes()))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-12-02")
    @Test
    public void uploadStreamAccessTierCold() {
        byte[] randomData = getRandomByteArray(Constants.KB);
        Flux<ByteBuffer> input = Flux.just(ByteBuffer.wrap(randomData));
        BlobParallelUploadOptions blobUploadOptions = new BlobParallelUploadOptions(input).setTier(AccessTier.COLD);


        bc.uploadWithResponse(blobUploadOptions).block();

        StepVerifier.create(bc.getProperties())
            .assertNext(r -> assertEquals(AccessTier.COLD, r.getAccessTier()))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void downloadAllNull() {
        bc.setTags(Collections.singletonMap("foo", "bar")).block();
        StepVerifier.create(bc.downloadWithResponse(null, null,
                null, false)
                .flatMap(r -> {
                    BlobDownloadHeaders headers = r.getDeserializedHeaders();
                    assertTrue(CoreUtils.isNullOrEmpty(headers.getMetadata()));
                    assertEquals(1, headers.getTagCount());
                    assertNotNull(headers.getContentLength());
                    assertNotNull(headers.getContentType());
                    assertNull(headers.getContentRange());
                    assertNotNull(headers.getContentMd5());
                    assertNull(headers.getContentEncoding());
                    assertNull(headers.getCacheControl());
                    assertNull(headers.getContentDisposition());
                    assertNull(headers.getContentLanguage());
                    assertNull(headers.getBlobSequenceNumber());
                    assertEquals(BlobType.BLOCK_BLOB, headers.getBlobType());
                    assertNull(headers.getCopyCompletionTime());
                    assertNull(headers.getCopyStatusDescription());
                    assertNull(headers.getCopyId());
                    assertNull(headers.getCopyProgress());
                    assertNull(headers.getCopySource());
                    assertNull(headers.getCopyStatus());
                    assertNull(headers.getLeaseDuration());
                    assertEquals(LeaseStateType.AVAILABLE, headers.getLeaseState());
                    assertEquals(LeaseStatusType.UNLOCKED, headers.getLeaseStatus());
                    assertEquals("bytes", headers.getAcceptRanges());
                    assertNull(headers.getBlobCommittedBlockCount());
                    assertNotNull(headers.isServerEncrypted());
                    assertNull(headers.getBlobContentMD5());
                    assertNotNull(headers.getCreationTime());
                    //headers.getLastAccessedTime() /* TODO (gapra): re-enable when last access time enabled. */
                    return FluxUtil.collectBytesInByteBufferStream(r.getValue());
                }))
            .assertNext(r -> TestUtils.assertArraysEqual(DATA.getDefaultBytes(), r))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void downloadAllNullStreaming() {
        bc.setTags(Collections.singletonMap("foo", "bar")).block();
        StepVerifier.create(bc.downloadStreamWithResponse(null, null,
                    null, false)
                .flatMap(r -> {
                    BlobDownloadHeaders headers = r.getDeserializedHeaders();
                    assertTrue(CoreUtils.isNullOrEmpty(headers.getMetadata()));
                    assertEquals(1, headers.getTagCount());
                    assertNotNull(headers.getContentLength());
                    assertNotNull(headers.getContentType());
                    assertNull(headers.getContentRange());
                    assertNotNull(headers.getContentMd5());
                    assertNull(headers.getContentEncoding());
                    assertNull(headers.getCacheControl());
                    assertNull(headers.getContentDisposition());
                    assertNull(headers.getContentLanguage());
                    assertNull(headers.getBlobSequenceNumber());
                    assertEquals(BlobType.BLOCK_BLOB, headers.getBlobType());
                    assertNull(headers.getCopyCompletionTime());
                    assertNull(headers.getCopyStatusDescription());
                    assertNull(headers.getCopyId());
                    assertNull(headers.getCopyProgress());
                    assertNull(headers.getCopySource());
                    assertNull(headers.getCopyStatus());
                    assertNull(headers.getLeaseDuration());
                    assertEquals(LeaseStateType.AVAILABLE, headers.getLeaseState());
                    assertEquals(LeaseStatusType.UNLOCKED, headers.getLeaseStatus());
                    assertEquals("bytes", headers.getAcceptRanges());
                    assertNull(headers.getBlobCommittedBlockCount());
                    assertNotNull(headers.isServerEncrypted());
                    assertNull(headers.getBlobContentMD5());
                    assertNotNull(headers.getCreationTime());
                    //headers.getLastAccessedTime() /* TODO (gapra): re-enable when last access time enabled. */
                    return FluxUtil.collectBytesInByteBufferStream(r.getValue());
                }))
            .assertNext(r -> TestUtils.assertArraysEqual(DATA.getDefaultBytes(), r))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void downloadAllNullBinaryData() {
        bc.setTags(Collections.singletonMap("foo", "bar")).block();
        StepVerifier.create(bc.downloadContentWithResponse(null, null))
                .assertNext(r -> {
                    BlobDownloadHeaders headers = r.getDeserializedHeaders();
                    assertTrue(CoreUtils.isNullOrEmpty(headers.getMetadata()));
                    assertEquals(1, headers.getTagCount());
                    assertNotNull(headers.getContentLength());
                    assertNotNull(headers.getContentType());
                    assertNull(headers.getContentRange());
                    assertNotNull(headers.getContentMd5());
                    assertNull(headers.getContentEncoding());
                    assertNull(headers.getCacheControl());
                    assertNull(headers.getContentDisposition());
                    assertNull(headers.getContentLanguage());
                    assertNull(headers.getBlobSequenceNumber());
                    assertEquals(BlobType.BLOCK_BLOB, headers.getBlobType());
                    assertNull(headers.getCopyCompletionTime());
                    assertNull(headers.getCopyStatusDescription());
                    assertNull(headers.getCopyId());
                    assertNull(headers.getCopyProgress());
                    assertNull(headers.getCopySource());
                    assertNull(headers.getCopyStatus());
                    assertNull(headers.getLeaseDuration());
                    assertEquals(LeaseStateType.AVAILABLE, headers.getLeaseState());
                    assertEquals(LeaseStatusType.UNLOCKED, headers.getLeaseStatus());
                    assertEquals("bytes", headers.getAcceptRanges());
                    assertNull(headers.getBlobCommittedBlockCount());
                    assertNotNull(headers.isServerEncrypted());
                    assertNull(headers.getBlobContentMD5());
                    assertNotNull(headers.getCreationTime());
                    //headers.getLastAccessedTime() /* TODO (gapra): re-enable when last access time enabled. */
                    TestUtils.assertArraysEqual(DATA.getDefaultBytes(), r.getValue().toBytes());
                })
            .verifyComplete();
    }

    @Test
    public void downloadEmptyFile() {
        AppendBlobAsyncClient bc = ccAsync.getBlobAsyncClient("emptyAppendBlob").getAppendBlobAsyncClient();
        bc.create().block();
        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(bc.downloadStream()))
            .assertNext(r -> assertEquals(0, r.length))
            .verifyComplete();
    }

    /*
    This is to test the appropriate integration of DownloadResponse, including setting the correct range values on
    HttpGetterInfo.
     */

    @Test
    public void downloadWithRetryRange() {
        /*
        We are going to make a request for some range on a blob. The Flux returned will throw an exception, forcing
        a retry per the DownloadRetryOptions. The next request should have the same range header, which was generated
        from the count and offset values in HttpGetterInfo that was constructed on the initial call to download. We
        don't need to check the data here, but we want to ensure that the correct range is set each time. This will
        test the correction of a bug that was found which caused HttpGetterInfo to have an incorrect offset when it was
        constructed in BlobClient.download().
         */
        BlobAsyncClient bu2 = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(), bc.getBlobUrl(),
            new MockRetryRangeResponsePolicy("bytes=2-6"));

        BlobRange range = new BlobRange(2, 5L);
        DownloadRetryOptions options = new DownloadRetryOptions().setMaxRetryRequests(3);
        StepVerifier.create(bu2.downloadStreamWithResponse(range, options, null,
            false)
            .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue())))
            .verifyErrorSatisfies(r -> {
                /*
                   Because the dummy Flux always throws an error. This will also validate that an IllegalArgumentException is
                NOT thrown because the types would not match.
                */
                assertInstanceOf(IOException.class, r);
            });
    }

    @Test
    public void downloadMin() {
        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(bc.download()))
            .assertNext(r -> TestUtils.assertArraysEqual(DATA.getDefaultBytes(), r))
            .verifyComplete();
    }

    @Test
    public void downloadStreamingMin() {
        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(bc.downloadStream()))
            .assertNext(r -> TestUtils.assertArraysEqual(DATA.getDefaultBytes(), r))
            .verifyComplete();
    }

    @Test
    public void downloadBinaryDataMin() {
        StepVerifier.create(bc.downloadContent())
            .assertNext(r -> TestUtils.assertArraysEqual(DATA.getDefaultBytes(), r.toBytes()))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("downloadRangeSupplier")
    public void downloadRange(long offset, Long count, String expectedData) {
        BlobRange range = (count == null) ? new BlobRange(offset) : new BlobRange(offset, count);

        StepVerifier.create(bc.downloadStreamWithResponse(range, null, null, false)
            .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue())))
            .assertNext(r -> TestUtils.assertArraysEqual(expectedData.getBytes(), r))
            .verifyComplete();
    }

    private static Stream<Arguments> downloadRangeSupplier() {
        return Stream.of(Arguments.of(0, null, DATA.getDefaultText()),
            Arguments.of(0, 5L, DATA.getDefaultText().substring(0, 5)),
            Arguments.of(3, 2L, DATA.getDefaultText().substring(3, 3 + 2)));
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void downloadAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                           String leaseID, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t).block();
        match = setupBlobMatchCondition(bc, match);
        leaseID = setupBlobLeaseCondition(bc, leaseID);
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        StepVerifier.create(bc.downloadWithResponse(null, null, bac, false))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void downloadACStreaming(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                    String leaseID, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t).block();
        match = setupBlobMatchCondition(bc, match);
        leaseID = setupBlobLeaseCondition(bc, leaseID);
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        StepVerifier.create(bc.downloadStreamWithResponse(null, null, bac, false))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void downloadACBinaryData(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                     String leaseID, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t).block();
        match = setupBlobMatchCondition(bc, match);
        leaseID = setupBlobLeaseCondition(bc, leaseID);
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        StepVerifier.create(bc.downloadContentWithResponse(null, bac))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsFailSupplier")
    public void downloadACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                               String leaseID, String tags) {
        setupBlobLeaseCondition(bc, leaseID);
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupBlobMatchCondition(bc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        StepVerifier.create(bc.downloadWithResponse(null, null, bac, false))
                .verifyError(BlobStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsFailSupplier")
    public void downloadACFailStreaming(OffsetDateTime modified, OffsetDateTime unmodified, String match,
                                        String noneMatch, String leaseID, String tags) {
        setupBlobLeaseCondition(bc, leaseID);
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupBlobMatchCondition(bc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        StepVerifier.create(bc.downloadStreamWithResponse(null, null, bac, false))
            .verifyError(BlobStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsFailSupplier")
    public void downloadACFailBinaryData(OffsetDateTime modified, OffsetDateTime unmodified, String match,
                                         String noneMatch, String leaseID, String tags) {
        setupBlobLeaseCondition(bc, leaseID);
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupBlobMatchCondition(bc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        StepVerifier.create(bc.downloadContentWithResponse(null, bac))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void downloadMd5() throws NoSuchAlgorithmException {
        StepVerifier.create(bc.downloadStreamWithResponse(new BlobRange(0, 3L), null,
            null, true))
            .assertNext(r -> {
                byte[] contentMD5 = r.getDeserializedHeaders().getContentMd5();
                try {
                    TestUtils.assertArraysEqual(MessageDigest.getInstance("MD5")
                        .digest(DATA.getDefaultText().substring(0, 3).getBytes()), contentMD5);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            })
            .verifyComplete();
    }

    @Test
    public void downloadRetryDefault() {
        BlobAsyncClient failureBlobClient = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            bc.getBlobUrl(), new MockFailureResponsePolicy(5));

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(failureBlobClient.downloadStream()))
            .assertNext(r -> TestUtils.assertArraysEqual(DATA.getDefaultBytes(), r))
            .verifyComplete();
    }

    @Test
    public void downloadSnapshot() {
        byte[] originalStream = FluxUtil.collectBytesInByteBufferStream(bc.downloadStream()).block();

        BlobAsyncClientBase bc2 = bc.createSnapshot().block();
        new SpecializedBlobClientBuilder()
            .blobAsyncClient(bc)
            .buildBlockBlobClient()
            .upload(new ByteArrayInputStream("ABC".getBytes()), 3, true);

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(bc2.downloadStream()))
            .assertNext(r -> TestUtils.assertArraysEqual(originalStream, r))
            .verifyComplete();
    }

    @Test
    public void downloadSnapshotBinaryData() {

        BinaryData originalContent = bc.downloadContent().block();

        BlobAsyncClientBase bc2 = bc.createSnapshot().block();
        new SpecializedBlobClientBuilder()
            .blobAsyncClient(bc)
            .buildBlockBlobClient()
            .upload(new ByteArrayInputStream("ABC".getBytes()), 3, true);

        BinaryData snapshotContent = bc2.downloadContent().block();
        TestUtils.assertArraysEqual(originalContent.toBytes(), snapshotContent.toBytes());
    }

    @Test
    public void downloadToFileExists() throws IOException {
        File testFile = new File(generateResourceName(entityNo) + ".txt");
        if (!testFile.exists()) {
            assertTrue(testFile.createNewFile());
        }

        // Default Overwrite is false so this should fail
        StepVerifier.create(bc.downloadToFile(testFile.getPath()))
            .verifyErrorSatisfies(r -> {
                UncheckedIOException e = assertInstanceOf(UncheckedIOException.class, r);
                assertInstanceOf(FileAlreadyExistsException.class, e.getCause());
            });
        // cleanup:
        testFile.delete();
    }

    @Test
    public void downloadToFileExistsSucceeds() throws IOException {
        File testFile = new File(generateResourceName(entityNo) + ".txt");
        if (!testFile.exists()) {
            assertTrue(testFile.createNewFile());
        }

        bc.downloadToFile(testFile.getPath(), true).block();
        assertEquals(DATA.getDefaultText(), new String(Files.readAllBytes(testFile.toPath()), StandardCharsets.UTF_8));

        // cleanup:
        testFile.delete();
    }

    @Test
    public void downloadToFileDoesNotExist() throws IOException {
        File testFile = new File(generateResourceName(entityNo) + ".txt");
        if (testFile.exists()) {
            assertTrue(testFile.createNewFile());
        }

        bc.downloadToFile(testFile.getPath()).block();
        assertEquals(DATA.getDefaultText(), new String(Files.readAllBytes(testFile.toPath()), StandardCharsets.UTF_8));
        // cleanup:
        testFile.delete();
    }

    @Test
    public void downloadFileDoesNotExistOpenOptions() throws IOException {
        File testFile = new File(generateResourceName(entityNo) + ".txt");
        if (testFile.exists()) {
            assertTrue(testFile.createNewFile());
        }

        Set<OpenOption> openOptions = new HashSet<>();
        openOptions.add(StandardOpenOption.CREATE_NEW);
        openOptions.add(StandardOpenOption.READ);
        openOptions.add(StandardOpenOption.WRITE);
        bc.downloadToFileWithResponse(testFile.getPath(), null, null, null,
            null, false, openOptions).block();

        assertEquals(DATA.getDefaultText(), new String(Files.readAllBytes(testFile.toPath()), StandardCharsets.UTF_8));

        // cleanup:
        testFile.delete();
    }

    @Test
    public void downloadFileExistOpenOptions() throws IOException {
        File testFile = new File(generateResourceName(entityNo) + ".txt");
        if (!testFile.exists()) {
            assertTrue(testFile.createNewFile());
        }

        Set<OpenOption> openOptions = new HashSet<>();
        openOptions.add(StandardOpenOption.CREATE);
        openOptions.add(StandardOpenOption.TRUNCATE_EXISTING);
        openOptions.add(StandardOpenOption.READ);
        openOptions.add(StandardOpenOption.WRITE);
        bc.downloadToFileWithResponse(testFile.getPath(), null, null, null,
            null, false, openOptions).block();

        assertEquals(DATA.getDefaultText(), new String(Files.readAllBytes(testFile.toPath()), StandardCharsets.UTF_8));

        // cleanup:
        testFile.delete();
    }

    @LiveOnly
    @ParameterizedTest
    @ValueSource(ints = {
        0, // empty file
        20, // small file
        16 * 1024 * 1024, // medium file in several chunks
        8 * 1026 * 1024 + 10, // medium file not aligned to block
        50 * Constants.MB // large file requiring multiple requests
    })
    public void downloadFile(int fileSize) throws IOException {
        File file = getRandomFile(fileSize);
        file.deleteOnExit();
        createdFiles.add(file);

        bc.uploadFromFile(file.toPath().toString(), true).block();

        File outFile = new File(prefix + ".txt");
        createdFiles.add(outFile);
        outFile.deleteOnExit();
        Files.deleteIfExists(outFile.toPath());

        StepVerifier.create(bc.downloadToFileWithResponse(outFile.toPath().toString(), null,
            new ParallelTransferOptions().setBlockSizeLong(4L * 1024 * 1024), null, null,
            false))
            .assertNext(r -> {
                assertEquals(BlobType.BLOCK_BLOB, r.getValue().getBlobType());
                assertNotNull(r.getValue().getCreationTime());
            })
            .verifyComplete();
    }

    /*
     * Tests downloading a file using a default client that doesn't have a HttpClient passed to it.
     */
    @LiveOnly
    @ParameterizedTest
    @ValueSource(ints = {
        0, // empty file
        20, // small file
        16 * 1024 * 1024, // medium file in several chunks
        8 * 1026 * 1024 + 10, // medium file not aligned to block
        50 * Constants.MB // large file requiring multiple requests
    })
    public void downloadFileAsyncBufferCopy(int fileSize) throws IOException {
        String containerName = generateContainerName();
        BlobServiceAsyncClient blobServiceAsyncClient = new BlobServiceClientBuilder()
            .endpoint(ENVIRONMENT.getPrimaryAccount().getBlobEndpoint())
            .credential(ENVIRONMENT.getPrimaryAccount().getCredential())
            .buildAsyncClient();

        BlobAsyncClient blobAsyncClient = Objects.requireNonNull(blobServiceAsyncClient
            .createBlobContainer(containerName).block()).getBlobAsyncClient(generateBlobName());

        File file = getRandomFile(fileSize);
        file.deleteOnExit();
        createdFiles.add(file);

        blobAsyncClient.uploadFromFile(file.toPath().toString(), true).block();
        File outFile = new File(prefix + ".txt");
        createdFiles.add(outFile);
        outFile.deleteOnExit();

        Mono<Response<BlobProperties>> downloadMono = blobAsyncClient.downloadToFileWithResponse(
            outFile.toPath().toString(), null, new ParallelTransferOptions().setBlockSizeLong(4L * 1024 * 1024),
            null, null, false);

        StepVerifier.create(downloadMono)
            .assertNext(it -> assertEquals(BlobType.BLOCK_BLOB, it.getValue().getBlobType()))
            .verifyComplete();

        assertTrue(compareFiles(file, outFile, 0, fileSize));

        // cleanup:
        blobServiceAsyncClient.deleteBlobContainer(containerName);
    }

    @LiveOnly
    @ParameterizedTest
    @MethodSource("downloadFileRangeSupplier")
    public void downloadFileRange(BlobRange range) throws IOException {
        File file = getRandomFile(DATA.getDefaultDataSize());
        file.deleteOnExit();
        createdFiles.add(file);

        bc.uploadFromFile(file.toPath().toString(), true).block();
        File outFile = new File(generateBlobName());
        createdFiles.add(outFile);
        Files.deleteIfExists(outFile.toPath());

        bc.downloadToFileWithResponse(outFile.toPath().toString(), range, null, null,
            null, false).block();

        assertTrue(compareFiles(file, outFile, range.getOffset(), range.getCount()));
    }

    private static Stream<Arguments> downloadFileRangeSupplier() {
        /*
        The last case is to test a range much larger than the size of the file to ensure we don't accidentally
        send off parallel requests with invalid ranges.
         */
        return Stream.of(Arguments.of(new BlobRange(0, DATA.getDefaultDataSizeLong())), // Exact count
            Arguments.of(new BlobRange(1, DATA.getDefaultDataSizeLong() - 1)), // Offset and exact count
            Arguments.of(new BlobRange(3, 2L)), // Narrow range in middle
            Arguments.of(new BlobRange(0, DATA.getDefaultDataSizeLong() - 1)), // Count less than total
            Arguments.of(new BlobRange(0, 10L * 1024))); // Count much larger than remaining data
    }

    /*
    This is to exercise some additional corner cases and ensure there are no arithmetic errors that give false success.
     */

    @LiveOnly
    @Test
    public void downloadFileRangeFail() throws IOException {
        File file = getRandomFile(DATA.getDefaultDataSize());
        file.deleteOnExit();
        createdFiles.add(file);

        bc.uploadFromFile(file.toPath().toString(), true).block();
        File outFile = new File(prefix);
        createdFiles.add(outFile);
        Files.deleteIfExists(outFile.toPath());

        StepVerifier.create(bc.downloadToFileWithResponse(outFile.toPath().toString(),
            new BlobRange(DATA.getDefaultDataSize() + 1), null, null,
            null, false))
            .verifyError(BlobStorageException.class);
    }

    @LiveOnly
    @Test
    public void downloadFileCountNull() throws IOException {
        File file = getRandomFile(DATA.getDefaultDataSize());
        file.deleteOnExit();
        createdFiles.add(file);

        bc.uploadFromFile(file.toPath().toString(), true).block();
        File outFile = new File(generateBlobName());
        createdFiles.add(outFile);
        Files.deleteIfExists(outFile.toPath());

        bc.downloadToFileWithResponse(outFile.toPath().toString(), new BlobRange(0), null,
            null, null, false).block();

        assertTrue(compareFiles(file, outFile, 0, DATA.getDefaultDataSize()));
    }

    @LiveOnly
    @ParameterizedTest
    @MethodSource("downloadFileACSupplier")
    public void downloadFileAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                               String leaseID) throws IOException {
        File file = getRandomFile(DATA.getDefaultDataSize());
        file.deleteOnExit();
        createdFiles.add(file);

        bc.uploadFromFile(file.toPath().toString(), true).block();
        File outFile = new File(prefix);
        createdFiles.add(outFile);
        Files.deleteIfExists(outFile.toPath());

        match = setupBlobMatchCondition(bc, match);
        leaseID = setupBlobLeaseCondition(bc, leaseID);
        BlobRequestConditions bro = new BlobRequestConditions().setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified).setIfMatch(match).setIfNoneMatch(noneMatch)
            .setLeaseId(leaseID);

        StepVerifier.create(bc.downloadToFileWithResponse(outFile.toPath().toString(), null,
            null, null, bro, false))
            .expectNextCount(1)
            .verifyComplete();
    }

    private static Stream<Arguments> downloadFileACSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null, null),
            Arguments.of(OLD_DATE, null, null, null, null),
            Arguments.of(null, NEW_DATE, null, null, null),
            Arguments.of(null, null, RECEIVED_ETAG, null, null),
            Arguments.of(null, null, null, GARBAGE_ETAG, null),
            Arguments.of(null, null, null, null, RECEIVED_LEASE_ID)
        );
    }

    @LiveOnly
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#fileACFailSupplier")
    public void downloadFileACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                   String leaseID) throws IOException {
        File file = getRandomFile(DATA.getDefaultDataSize());
        file.deleteOnExit();
        createdFiles.add(file);

        bc.uploadFromFile(file.toPath().toString(), true).block();
        File outFile = new File(prefix);
        createdFiles.add(outFile);
        Files.deleteIfExists(outFile.toPath());

        noneMatch = setupBlobMatchCondition(bc, noneMatch);
        setupBlobLeaseCondition(bc, leaseID);
        BlobRequestConditions bro = new BlobRequestConditions().setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified).setIfMatch(match).setIfNoneMatch(noneMatch)
            .setLeaseId(leaseID);

        StepVerifier.create(bc.downloadToFileWithResponse(outFile.toPath().toString(), null,
            null, null, bro, false))
            .verifyErrorSatisfies(r -> {
                BlobStorageException e = assertInstanceOf(BlobStorageException.class, r);
                assertTrue(e.getErrorCode() == BlobErrorCode.CONDITION_NOT_MET
                    || e.getErrorCode() == BlobErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION);
            });
    }

    @LiveOnly
    @Test
    public void downloadFileETagLock() throws IOException {
        File file = getRandomFile(Constants.MB);
        file.deleteOnExit();
        createdFiles.add(file);

        bc.uploadFromFile(file.toPath().toString(), true).block();

        File outFile = new File(prefix);
        outFile.deleteOnExit();
        createdFiles.add(outFile);
        Files.deleteIfExists(outFile.toPath());

        AtomicInteger counter = new AtomicInteger();

        BlockBlobAsyncClient bacUploading = instrument(new BlobClientBuilder()
            .endpoint(bc.getBlobUrl())
            .credential(ENVIRONMENT.getPrimaryAccount().getCredential()))
            .buildAsyncClient()
            .getBlockBlobAsyncClient();
        TestDataFactory dataLocal = DATA;
        HttpPipelinePolicy policy = (context, next) -> next.process().flatMap(r -> {
            if (counter.incrementAndGet() == 1) {
                /*
                 * When the download begins trigger an upload to overwrite the downloading blob
                 * so that the download is able to get an ETag before it is changed.
                 */
                return bacUploading.upload(dataLocal.getDefaultFlux(), dataLocal.getDefaultDataSize(), true)
                    .thenReturn(r);
            }
            return Mono.just(r);
        });
        BlockBlobAsyncClient bacDownloading = instrument(new BlobClientBuilder()
            .addPolicy(policy)
            .endpoint(bc.getBlobUrl())
            .credential(ENVIRONMENT.getPrimaryAccount().getCredential()))
            .buildAsyncClient()
            .getBlockBlobAsyncClient();

        /*
         * Setup the download to happen in small chunks so many requests need to be sent, this will give the upload time
         * to change the ETag therefore failing the download.
         */
        ParallelTransferOptions options = new ParallelTransferOptions().setBlockSizeLong((long) Constants.KB);

        /*
         * This is done to prevent onErrorDropped exceptions from being logged at the error level. If no hook is
         * registered for onErrorDropped the error is logged at the ERROR level.
         *
         * onErrorDropped is triggered once the reactive stream has emitted one element, after that exceptions are
         * dropped.
         */
        Hooks.onErrorDropped(ignored -> /* do nothing with it */ { });
        StepVerifier.create(bacDownloading.downloadToFileWithResponse(outFile.toPath().toString(), null, options,
            null, null, false))
            .verifyErrorSatisfies(it -> {
            /*
             * If an operation is running on multiple threads and multiple return an exception Reactor will combine
             * them into a CompositeException which needs to be unwrapped. If there is only a single exception
             * 'Exceptions.unwrapMultiple' will return a singleton list of the exception it was passed.
             *
             * These exceptions may be wrapped exceptions where the exception we are expecting is contained within
             * ReactiveException that needs to be unwrapped. If the passed exception isn't a 'ReactiveException' it
             * will be returned unmodified by 'Exceptions.unwrap'.
             */
                assertTrue(Exceptions.unwrapMultiple(it).stream().anyMatch(it2 -> {
                    Throwable exception = Exceptions.unwrap(it2);
                    if (exception instanceof BlobStorageException) {
                        assertEquals(412, ((BlobStorageException) exception).getStatusCode());
                        return true;
                    }
                    return false;
                }));
            });

        // Give the file a chance to be deleted by the download operation before verifying its deletion
        sleepIfRunningAgainstService(500);
        assertFalse(outFile.exists());
    }

    @SuppressWarnings("deprecation")
    @LiveOnly
    @ParameterizedTest
    @ValueSource(ints = {100, 8 * 1026 * 1024 + 10})
    public void downloadFileProgressReceiver(int fileSize) throws IOException {
        File file = getRandomFile(fileSize);
        file.deleteOnExit();
        createdFiles.add(file);

        bc.uploadFromFile(file.toPath().toString(), true).block();
        File outFile = new File(prefix);
        outFile.deleteOnExit();
        createdFiles.add(outFile);
        Files.deleteIfExists(outFile.toPath());

        MockReceiver mockReceiver = new MockReceiver();

        bc.downloadToFileWithResponse(outFile.toPath().toString(), null,
            new ParallelTransferOptions().setProgressReceiver(mockReceiver),
            new DownloadRetryOptions().setMaxRetryRequests(3), null, false).block();

        /*
         * Should receive at least one notification indicating completed progress, multiple notifications may be
         * received if there are empty buffers in the stream.
         */
        assertTrue(mockReceiver.progresses.stream().anyMatch(progress -> progress == fileSize));

        // There should be NO notification with a larger than expected size.
        assertFalse(mockReceiver.progresses.stream().anyMatch(progress -> progress > fileSize));

        /*
        We should receive at least one notification reporting an intermediary value per block, but possibly more
        notifications will be received depending on the implementation. We specify numBlocks - 1 because the last block
        will be the total size as above. Finally, we assert that the number reported monotonically increases.
         */
        long prevCount = -1;
        for (long progress : mockReceiver.progresses) {
            assertTrue(progress >= prevCount, "Reported progress should monotonically increase");
            prevCount = progress;
        }
    }

    @SuppressWarnings("deprecation")
    private static final class MockReceiver implements ProgressReceiver {
        List<Long> progresses = new ArrayList<>();

        @Override
        public void reportProgress(long bytesTransferred) {
            progresses.add(bytesTransferred);
        }
    }

    @LiveOnly
    @ParameterizedTest
    @ValueSource(ints = {100, 8 * 1026 * 1024 + 10})
    public void downloadFileProgressListener(int fileSize) throws Exception {
        File file = getRandomFile(fileSize);
        file.deleteOnExit();
        createdFiles.add(file);

        bc.uploadFromFile(file.toPath().toString(), true).block();

        File outFile = new File(prefix);
        outFile.deleteOnExit();
        createdFiles.add(outFile);
        Files.deleteIfExists(outFile.toPath());

        MockProgressListener mockListener = new MockProgressListener();

        bc.downloadToFileWithResponse(outFile.toPath().toString(), null,
            new ParallelTransferOptions().setProgressListener(mockListener),
            new DownloadRetryOptions().setMaxRetryRequests(3), null, false).block();

        /*
         * Should receive at least one notification indicating completed progress, multiple notifications may be
         * received if there are empty buffers in the stream.
         */
        assertTrue(mockListener.progresses.stream().anyMatch(progress -> progress == fileSize));

        // There should be NO notification with a larger than expected size.
        assertFalse(mockListener.progresses.stream().anyMatch(progress -> progress > fileSize));

        // We should receive at least one notification reporting an intermediary value per block, but possibly more
        // notifications will be received depending on the implementation. We specify numBlocks - 1 because the last
        // block will be the total size as above. Finally, we assert that the number reported monotonically increases.
        long prevCount = -1;
        for (long progress : mockListener.progresses) {
            assertTrue(progress >= prevCount, "Reported progress should monotonically increase");
            prevCount = progress;
        }
    }

    private static final class MockProgressListener implements ProgressListener {
        List<Long> progresses = new ArrayList<>();

        @Override
        public void handleProgress(long progress) {
            progresses.add(progress);
        }
    }

    @Disabled("Very large data sizes.")
    @ParameterizedTest
    @MethodSource("downloadToFileBlockSizeSupplier")
    /* Enable once we have ability to run large resource heavy tests in CI. */
    public void downloadToFileBlockSize(int sizeOfData, long downloadBlockSize) throws IOException {
        File file = getRandomFile(sizeOfData);
        file.deleteOnExit();
        createdFiles.add(file);

        bc.uploadFromFile(file.toPath().toString(), true).block();
        File outFile = new File(prefix);
        createdFiles.add(outFile);
        Files.deleteIfExists(outFile.toPath());

        StepVerifier.create(bc.downloadToFileWithResponse(
            new BlobDownloadToFileOptions(outFile.toPath().toString())
                .setParallelTransferOptions(new com.azure.storage.common.ParallelTransferOptions()
                .setBlockSizeLong(downloadBlockSize)).setDownloadRetryOptions(new DownloadRetryOptions()
                .setMaxRetryRequests(3))))
            .expectNextCount(1)
            .verifyComplete();
    }

    private static Stream<Arguments> downloadToFileBlockSizeSupplier() {
        return Stream.of(
            /* This was the default before. */
            Arguments.of(5000L * Constants.MB, 5000L * Constants.MB),
            /* Trying to see if we can set it to a number greater than previous default. */
            Arguments.of(6000L * Constants.MB, 6000L * Constants.MB),
            /* Testing chunking with a large size */
            Arguments.of(6000L * Constants.MB, 5100L * Constants.MB));
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void getPropertiesDefault() {
        bc.setTags(Collections.singletonMap("foo", "bar")).block();
        StepVerifier.create(bc.getPropertiesWithResponse(null))
            .assertNext(r -> {
                HttpHeaders headers = r.getHeaders();
                BlobProperties properties = r.getValue();

                assertTrue(validateBasicHeaders(headers));
                assertTrue(CoreUtils.isNullOrEmpty(properties.getMetadata()));
                assertEquals(BlobType.BLOCK_BLOB, properties.getBlobType());
                assertNull(properties.getCopyCompletionTime()); // tested in "copy"
                assertNull(properties.getCopyStatusDescription()); // only returned when the service has errors; cannot validate
                assertNull(properties.getCopyId()); // tested in "abort copy"
                assertNull(properties.getCopyProgress()); // tested in "copy"
                assertNull(properties.getCopySource()); // tested in "copy"
                assertNull(properties.getCopyStatus()); // tested in "copy"
                assertNull(properties.isIncrementalCopy()); // tested in PageBlob."start incremental copy"
                assertNull(properties.getCopyDestinationSnapshot()); // tested in PageBlob."start incremental copy"
                assertNull(properties.getLeaseDuration()); // tested in "acquire lease"
                assertEquals(LeaseStateType.AVAILABLE, properties.getLeaseState());
                assertEquals(LeaseStatusType.UNLOCKED, properties.getLeaseStatus());
                assertTrue(properties.getBlobSize() >= 0);
                assertNotNull(properties.getContentType());
                assertNotNull(properties.getContentMd5());
                assertNull(properties.getContentEncoding()); // tested in "set HTTP headers"
                assertNull(properties.getContentDisposition()); // tested in "set HTTP headers"
                assertNull(properties.getContentLanguage()); // tested in "set HTTP headers"
                assertNull(properties.getCacheControl()); // tested in "set HTTP headers"
                assertNull(properties.getBlobSequenceNumber()); // tested in PageBlob."create sequence number"
                assertEquals("bytes", headers.getValue(HttpHeaderName.ACCEPT_RANGES));
                assertNull(properties.getCommittedBlockCount()); // tested in AppendBlob."append block"
                assertTrue(properties.isServerEncrypted());
                assertEquals(AccessTier.HOT, properties.getAccessTier());
                assertTrue(properties.isAccessTierInferred());
                assertNull(properties.getArchiveStatus()); // tested in "set tier"
                assertNotNull(properties.getCreationTime());
                assertEquals(1, properties.getTagCount());
                assertNull(properties.getRehydratePriority()); // tested in setTier rehydrate priority
                assertNull(properties.isSealed()); // tested in AppendBlob. "seal blob"
                assertNotNull(properties.getRequestId());
                //properties.getLastAccessedTime() /* TODO: re-enable when last access time enabled. */
            })
            .verifyComplete();
    }

    @Test
    public void getPropertiesMin() {
        assertAsyncResponseStatusCode(bc.getPropertiesWithResponse(null),
            200);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void getPropertiesAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                String leaseID, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t).block();
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(setupBlobLeaseCondition(bc, leaseID))
            .setIfMatch(setupBlobMatchCondition(bc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        assertAsyncResponseStatusCode(bc.getPropertiesWithResponse(bac), 200);
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsFailSupplier")
    public void getPropertiesACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                    String leaseID, String tags) {
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(setupBlobLeaseCondition(bc, leaseID))
            .setIfMatch(match)
            .setIfNoneMatch(setupBlobMatchCondition(bc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        StepVerifier.create(bc.getPropertiesWithResponse(bac))
            .verifyError(BlobStorageException.class);
    }

    /*
    This test requires two accounts that are configured in a very specific way. It is not feasible to setup that
    relationship programmatically, so we have recorded a successful interaction and only test recordings.
     */
    @Test
    @PlaybackOnly
    public void getPropertiesORS() throws MalformedURLException {
        BlobAsyncClient sourceBlob = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient("test1")
            .getBlobAsyncClient("javablobgetpropertiesors2blobapitestgetpropertiesors57d93407b");
        BlobAsyncClient destBlob = alternateBlobServiceAsyncClient.getBlobContainerAsyncClient("test2")
            .getBlobAsyncClient("javablobgetpropertiesors2blobapitestgetpropertiesors57d93407b");

        StepVerifier.create(sourceBlob.getProperties())
            .assertNext(r -> assertTrue(validateOR(r.getObjectReplicationSourcePolicies(),
                "fd2da1b9-56f5-45ff-9eb6-310e6dfc2c80", "105f9aad-f39b-4064-8e47-ccd7937295ca")))
            .verifyComplete();
        StepVerifier.create(sourceBlob.downloadWithResponse(null, null, null,
            false))
            .assertNext(r -> assertTrue(validateOR(r.getDeserializedHeaders().getObjectReplicationSourcePolicies(),
                "fd2da1b9-56f5-45ff-9eb6-310e6dfc2c80", "105f9aad-f39b-4064-8e47-ccd7937295ca")))
            .verifyComplete();

        // There is a sas token attached at the end. Only check that the path is the same.
        StepVerifier.create(destBlob.getProperties())
            .assertNext(r -> {
                // disable recording copy source URL since the URL is redacted in playback mode
                assertNotNull(r.getCopySource());
                assertEquals("fd2da1b9-56f5-45ff-9eb6-310e6dfc2c80", r.getObjectReplicationDestinationPolicyId());
            })
            .verifyComplete();

        StepVerifier.create(destBlob.downloadWithResponse(null, null, null,
            false))
            .assertNext(r -> assertEquals("fd2da1b9-56f5-45ff-9eb6-310e6dfc2c80",
                r.getDeserializedHeaders().getObjectReplicationDestinationPolicyId()))
            .verifyComplete();
    }

    private static boolean validateOR(List<ObjectReplicationPolicy> policies, String policyId, String ruleId) {
        return policies.stream()
            .filter(policy -> policyId.equals(policy.getPolicyId()))
            .findFirst()
            .get()
            .getRules()
            .stream()
            .filter(rule -> ruleId.equals(rule.getRuleId()))
            .findFirst()
            .get()
            .getStatus() == ObjectReplicationStatus.COMPLETE;
    }

    // Test getting the properties from a listing

    @Test
    public void getPropertiesError() {
        bc = ccAsync.getBlobAsyncClient(generateBlobName());
        StepVerifier.create(bc.getProperties())
            .verifyErrorSatisfies(r -> {
                BlobStorageException ex = assertInstanceOf(BlobStorageException.class, r);
                assertTrue(ex.getMessage().contains("BlobNotFound"));
            });
    }

    @Test
    public void setHTTPHeadersNull() {
        StepVerifier.create(bc.setHttpHeadersWithResponse(null, null))
            .assertNext(r -> {
                assertResponseStatusCode(r, 200);
                assertTrue(validateBasicHeaders(r.getHeaders()));
            })
            .verifyComplete();
    }

    @Test
    public void setHTTPHeadersMin() throws NoSuchAlgorithmException {
        BlobProperties properties = bc.getProperties().block();
        BlobHttpHeaders headers = new BlobHttpHeaders()
            .setContentEncoding(properties.getContentEncoding())
            .setContentDisposition(properties.getContentDisposition())
            .setContentType("type")
            .setCacheControl(properties.getCacheControl())
            .setContentLanguage(properties.getContentLanguage())
            .setContentMd5(Base64.getEncoder().encode(MessageDigest.getInstance("MD5")
                .digest(DATA.getDefaultBytes())));

        bc.setHttpHeaders(headers).block();
        StepVerifier.create(bc.getProperties())
            .assertNext(r -> assertEquals("type", r.getContentType()))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("setHTTPHeadersHeadersSupplier")
    public void setHTTPHeadersHeaders(String cacheControl, String contentDisposition, String contentEncoding,
                                      String contentLanguage, byte[] contentMD5, String contentType) {
        BlobHttpHeaders putHeaders = new BlobHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType);

        bc.setHttpHeaders(putHeaders).block();

        StepVerifier.create(bc.getPropertiesWithResponse(null))
            .assertNext(r -> assertTrue(validateBlobProperties(r, cacheControl, contentDisposition, contentEncoding,
                contentLanguage, contentMD5, contentType)))
            .verifyComplete();
    }

    private static Stream<Arguments> setHTTPHeadersHeadersSupplier() throws NoSuchAlgorithmException {
        return Stream.of(Arguments.of(null, null, null, null, null, null),
            Arguments.of("control", "disposition", "encoding", "language",
                Base64.getEncoder().encode(MessageDigest.getInstance("MD5").digest(DATA.getDefaultBytes())),
                "type"));
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void setHTTPHeadersAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                 String leaseID, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t).block();
        match = setupBlobMatchCondition(bc, match);
        leaseID = setupBlobLeaseCondition(bc, leaseID);
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        assertAsyncResponseStatusCode(bc.setHttpHeadersWithResponse(null, bac), 200);
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsFailSupplier")
    public void setHTTPHeadersACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                     String leaseID, String tags) {
        noneMatch = setupBlobMatchCondition(bc, noneMatch);
        setupBlobLeaseCondition(bc, leaseID);
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        StepVerifier.create(bc.setHttpHeadersWithResponse(null, bac))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void setHTTPHeadersError() {
        bc = ccAsync.getBlobAsyncClient(generateBlobName());
        StepVerifier.create(bc.setHttpHeaders(null))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void setMetadataAllNull() {
        StepVerifier.create(bc.setMetadataWithResponse(null, null))
            .assertNext(r -> {
                assertResponseStatusCode(r, 200);
                assertTrue(validateBasicHeaders(r.getHeaders()));
                assertTrue(Boolean.parseBoolean(r.getHeaders().getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
            })
            .verifyComplete();

        StepVerifier.create(bc.getProperties())
            .assertNext(r -> assertEquals(0, r.getMetadata().size()))
            .verifyComplete();
    }

    @Test
    public void setMetadataMin() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("foo", "bar");
        bc.setMetadata(metadata).block();

        StepVerifier.create(bc.getProperties())
            .assertNext(r -> assertEquals(metadata, r.getMetadata()))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("setMetadataMetadataSupplier")
    public void setMetadataMetadata(String key1, String value1, String key2, String value2, int statusCode) {
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2);
        }

        assertAsyncResponseStatusCode(bc.setMetadataWithResponse(metadata, null), statusCode);
        StepVerifier.create(bc.getProperties())
            .assertNext(r -> assertEquals(metadata, r.getMetadata()))
            .verifyComplete();
    }

    private static Stream<Arguments> setMetadataMetadataSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null, 200),
            Arguments.of("foo", "bar", "fizz", "buzz", 200),
            Arguments.of("i0", "a", "i_", "a", 200), /* Test culture sensitive word sort */
            Arguments.of("foo", "bar0, bar1", null, null, 200)); /* Test comma separated values */
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("downloadFileACSupplier")
    public void setMetadataAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                              String leaseID) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t).block();
        match = setupBlobMatchCondition(bc, match);
        leaseID = setupBlobLeaseCondition(bc, leaseID);
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertAsyncResponseStatusCode(bc.setMetadataWithResponse(null, bac), 200);
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsFailSupplier")
    public void setMetadataACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                  String leaseID, String tags) {
        noneMatch = setupBlobMatchCondition(bc, noneMatch);
        setupBlobLeaseCondition(bc, leaseID);

        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        StepVerifier.create(bc.setMetadataWithResponse(null, bac))
            .verifyError(BlobStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("setMetadataWhitespaceErrorSupplier")
    public void setMetadataWhitespaceError(String key, String value) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put(key, value);

        assertThrows(Exception.class, () -> bc.setMetadata(metadata).block());
        // Need this second error type since for the first case, Netty throws IllegalArgumentException, and that is
        // recorded in the playback file. On Playback, the framework will throw Exceptions.ReactiveException.
        //assertTrue(e instanceof IllegalArgumentException || e instanceof Exceptions.ReactiveException);
    }

    private static Stream<Arguments> setMetadataWhitespaceErrorSupplier() {
        return Stream.of(
            Arguments.of(" foo", "bar"), // Leading whitespace key
            Arguments.of("foo ", "bar"), // Trailing whitespace key
            Arguments.of("foo", " bar"), // Leading whitespace value
            Arguments.of("foo", "bar ")); // Trailing whitespace value
    }

    @Test
    public void setMetadataError() {
        bc = ccAsync.getBlobAsyncClient(generateBlobName());

        StepVerifier.create(bc.setMetadata(null))
            .verifyError(BlobStorageException.class);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void setTagsAllNull() {
        assertAsyncResponseStatusCode(bc.setTagsWithResponse(new BlobSetTagsOptions(new HashMap<>())),
            204);
        StepVerifier.create(bc.getTags())
            .assertNext(r -> assertEquals(0, r.size()))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void setTagsMin() {
        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "bar");
        bc.setTags(tags).block();
        StepVerifier.create(bc.getTags())
            .assertNext(r -> assertEquals(tags, r))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("setTagsTagsSupplier")
    public void setTagsTags(String key1, String value1, String key2, String value2, int statusCode) {
        Map<String, String> tags = new HashMap<>();
        if (key1 != null && value1 != null) {
            tags.put(key1, value1);
        }
        if (key2 != null && value2 != null) {
            tags.put(key2, value2);
        }
        assertAsyncResponseStatusCode(bc.setTagsWithResponse(new BlobSetTagsOptions(tags)), statusCode);
        StepVerifier.create(bc.getTags())
            .assertNext(r -> assertEquals(tags, r))
            .verifyComplete();
    }

    private static Stream<Arguments> setTagsTagsSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null, 204),
            Arguments.of("foo", "bar", "fizz", "buzz", 204),
            Arguments.of(" +-./:=_  +-./:=_", " +-./:=_", null, null, 204));
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("setTagsACSupplier")
    public void setTagsAC(String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t).block();
        t = new HashMap<>();
        t.put("fizz", "buzz");

        assertAsyncResponseStatusCode(bc.setTagsWithResponse(new BlobSetTagsOptions(t)
            .setRequestConditions(new BlobRequestConditions().setTagsConditions(tags))), 204);
    }

    private static Stream<Arguments> setTagsACSupplier() {
        return Stream.of(
            Arguments.of((String) null),
            Arguments.of("\"foo\" = 'bar'")
        );
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-07-07")
    @Test
    public void setTagsACFail() {
        Map<String, String> t = new HashMap<>();
        t.put("fizz", "buzz");
        String tags = "\"foo\" = 'bar'";

        StepVerifier.create(bc.setTagsWithResponse(new BlobSetTagsOptions(t)
            .setRequestConditions(new BlobRequestConditions().setTagsConditions(tags))))
            .verifyError(BlobStorageException.class);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void getTagsAC() {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t).block();
        String[] tagsList = new String[]{null, "\"foo\" = 'bar'"};
        for (String tags : tagsList) {
            assertAsyncResponseStatusCode(bc.getTagsWithResponse(new BlobGetTagsOptions().setRequestConditions(
                new BlobRequestConditions().setTagsConditions(tags))), 200);
        }
    }

    @Test
    public void getTagsACFail() {
        Map<String, String> t = new HashMap<>();
        t.put("fizz", "buzz");
        String tags = "\"foo\" = 'bar'";

        StepVerifier.create(bc.getTagsWithResponse(new BlobGetTagsOptions()
            .setRequestConditions(new BlobRequestConditions().setTagsConditions(tags))))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void setTagsError() {
        bc = ccAsync.getBlobAsyncClient(generateBlobName());
        StepVerifier.create(bc.setTags(new HashMap<>()))
            .verifyError(BlobStorageException.class);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void setTagsLease() {
        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "bar");
        String leaseID = setupBlobLeaseCondition(bc, RECEIVED_LEASE_ID);
        BlobRequestConditions bac = new BlobRequestConditions().setLeaseId(leaseID);
        assertAsyncResponseStatusCode(bc.setTagsWithResponse(new BlobSetTagsOptions(tags).setRequestConditions(bac)),
            204);

        StepVerifier.create(bc.getTags())
            .assertNext(r -> assertEquals(tags, r))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void getTagsLease() {
        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "bar");
        String leaseID = setupBlobLeaseCondition(bc, RECEIVED_LEASE_ID);
        BlobRequestConditions bac = new BlobRequestConditions().setLeaseId(leaseID);
        bc.setTagsWithResponse(new BlobSetTagsOptions(tags).setRequestConditions(bac)).block();

        StepVerifier.create(bc.getTagsWithResponse(new BlobGetTagsOptions()
            .setRequestConditions(bac)))
            .assertNext(r -> {
                assertResponseStatusCode(r, 200);
                assertEquals(tags, r.getValue());
            })
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void setTagsLeaseFail() {
        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "bar");
        BlobRequestConditions bac = new BlobRequestConditions().setLeaseId(GARBAGE_LEASE_ID);

        StepVerifier.create(bc.setTagsWithResponse(
            new BlobSetTagsOptions(tags).setRequestConditions(bac)))
            .verifyErrorSatisfies(r -> {
                BlobStorageException e = assertInstanceOf(BlobStorageException.class, r);
                assertEquals(412, e.getStatusCode());
            });
    }

    @Test
    public void snapshot() {
        StepVerifier.create(bc.createSnapshotWithResponse(null, null)
            .flatMap(r -> {
                assertTrue(validateBasicHeaders(r.getHeaders()));
                return r.getValue().exists();
            }))
            .assertNext(r -> assertTrue(r))
            .verifyComplete();
    }

    @Test
    public void snapshotMin() {
        assertAsyncResponseStatusCode(bc.createSnapshotWithResponse(null,
            null), 201);
    }

    @Test
    public void getSnapshot() {
        byte[] data = "test".getBytes();
        String blobName = generateBlobName();
        BlockBlobAsyncClient bu = ccAsync.getBlobAsyncClient(blobName).getBlockBlobAsyncClient();
        bu.upload(Flux.just(ByteBuffer.wrap(data)), data.length).block();
        String snapshotId = bu.createSnapshot().block().getSnapshotId();

        BlockBlobAsyncClient snapshotBlob = ccAsync.getBlobAsyncClient(blobName, snapshotId).getBlockBlobAsyncClient();
        assertEquals(snapshotId, snapshotBlob.getSnapshotId());
        assertNull(bu.getSnapshotId());
    }

    @Test
    public void isSnapshot() {
        byte[] data = "test".getBytes();
        String blobName = generateBlobName();
        BlockBlobAsyncClient bu = ccAsync.getBlobAsyncClient(blobName).getBlockBlobAsyncClient();
        bu.upload(Flux.just(ByteBuffer.wrap(data)), data.length).block();
        String snapshotId = bu.createSnapshot().block().getSnapshotId();

        BlockBlobAsyncClient snapshotBlob = ccAsync.getBlobAsyncClient(blobName, snapshotId).getBlockBlobAsyncClient();
        assertTrue(snapshotBlob.isSnapshot());
        assertFalse(bu.isSnapshot());
    }

    @ParameterizedTest
    @MethodSource("snapshotMetadataSupplier")
    public void snapshotMetadata(String key1, String value1, String key2, String value2) {
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2);
        }

        StepVerifier.create(bc.createSnapshotWithResponse(metadata, null)
            .flatMap(r -> {
                assertEquals(201, r.getStatusCode());
                return r.getValue().getProperties();
            }))
            .assertNext(r -> assertEquals(metadata, r.getMetadata()))
            .verifyComplete();
    }

    private static Stream<Arguments> snapshotMetadataSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null),
            Arguments.of("foo", "bar", "fizz", "buzz"));
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void snapshotAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                           String leaseID, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t).block();
        match = setupBlobMatchCondition(bc, match);
        leaseID = setupBlobLeaseCondition(bc, leaseID);
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        assertAsyncResponseStatusCode(bc.createSnapshotWithResponse(null, bac), 201);
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsFailSupplier")
    public void snapshotACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                               String leaseID, String tags) {
        noneMatch = setupBlobMatchCondition(bc, noneMatch);
        setupBlobLeaseCondition(bc, leaseID);
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        StepVerifier.create(bc.createSnapshotWithResponse(null, bac))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void snapshotError() {
        bc = ccAsync.getBlobAsyncClient(generateBlobName());
        StepVerifier.create(bc.createSnapshot())
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void copy() {
        BlockBlobAsyncClient copyDestBlob = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        PollerFlux<BlobCopyInfo, Void> poller = setPlaybackPollerFluxPollInterval(copyDestBlob.beginCopy(bc.getBlobUrl(),
            null));

        AsyncPollResponse<BlobCopyInfo, Void> response = poller.blockLast();
        BlobProperties properties = copyDestBlob.getProperties().block();

        assertEquals(CopyStatusType.SUCCESS, properties.getCopyStatus());
        assertNotNull(properties.getCopyCompletionTime());
        assertNotNull(properties.getCopyProgress());
        assertNotNull(properties.getCopySource());
        assertNotNull(response);
        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, response.getStatus());
        BlobCopyInfo blobInfo = response.getValue();
        assertNotNull(blobInfo);
        assertEquals(properties.getCopyId(), blobInfo.getCopyId());
    }

    @Test
    public void copyMin() {
        BlockBlobAsyncClient copyDestBlob = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();

        PollerFlux<BlobCopyInfo, Void> poller = setPlaybackPollerFluxPollInterval(copyDestBlob.beginCopy(bc.getBlobUrl(),
            null));
        StepVerifier.create(poller.take(1))
            .assertNext(it -> {
                assertNotNull(it.getValue());
                assertNotNull(it.getValue().getCopyId());
                if (ENVIRONMENT.getTestMode() == TestMode.PLAYBACK) {
                    // disable recording copy source URL since the URL is redacted in playback mode
                    assertNotNull(it.getValue().getCopySourceUrl());
                } else {
                    assertEquals(bc.getBlobUrl(), it.getValue().getCopySourceUrl());
                }
                assertTrue(it.getStatus() == LongRunningOperationStatus.IN_PROGRESS
                    || it.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);
            }).verifyComplete();
    }

    @Test
    public void copyPoller() {
        BlockBlobAsyncClient copyDestBlob = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();

        PollerFlux<BlobCopyInfo, Void> poller = setPlaybackPollerFluxPollInterval(copyDestBlob.beginCopy(bc.getBlobUrl(),
            null, null, null, null, null, null));

        AsyncPollResponse<BlobCopyInfo, Void> lastResponse = poller.doOnNext(it -> {
            assertNotNull(it.getValue());
            assertNotNull(it.getValue().getCopyId());
            if (ENVIRONMENT.getTestMode() == TestMode.PLAYBACK) {
                // disable recording copy source URL since the URL is redacted in playback mode
                assertNotNull(it.getValue().getCopySourceUrl());
            } else {
                assertEquals(bc.getBlobUrl(), it.getValue().getCopySourceUrl());
            }
        }).blockLast();

        assertNotNull(lastResponse);
        assertNotNull(lastResponse.getValue());

        StepVerifier.create(copyDestBlob.getProperties()).assertNext(it -> {
            assertEquals(lastResponse.getValue().getCopyId(), it.getCopyId());
            assertEquals(CopyStatusType.SUCCESS, it.getCopyStatus());
            assertNotNull(it.getCopyCompletionTime());
            assertNotNull(it.getCopyProgress());
            assertNotNull(it.getCopySource());
            assertNotNull(it.getCopyId());
        }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("snapshotMetadataSupplier")
    public void copyMetadata(String key1, String value1, String key2, String value2) {
        BlockBlobAsyncClient bu2 = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2);
        }

        PollerFlux<BlobCopyInfo, Void> poller = setPlaybackPollerFluxPollInterval(bu2.beginCopy(bc.getBlobUrl(),
            metadata, null, null, null, null, null));
        poller.blockLast();

        StepVerifier.create(bu2.getProperties()).assertNext(it -> assertEquals(metadata, it.getMetadata()))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("copyTagsSupplier")
    public void copyTags(String key1, String value1, String key2, String value2) {
        BlockBlobAsyncClient bu2 = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        Map<String, String> tags = new HashMap<>();
        if (key1 != null && value1 != null) {
            tags.put(key1, value1);
        }
        if (key2 != null && value2 != null) {
            tags.put(key2, value2);
        }

        PollerFlux<BlobCopyInfo, Void> poller = setPlaybackPollerFluxPollInterval(
            bu2.beginCopy(new BlobBeginCopyOptions(bc.getBlobUrl()).setTags(tags)));
        poller.blockLast();

        StepVerifier.create(bu2.getTags()).assertNext(it -> assertEquals(tags, it)).verifyComplete();
    }

    private static Stream<Arguments> copyTagsSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null),
            Arguments.of("foo", "bar", "fizz", "buzz"),
            Arguments.of(" +-./:=_  +-./:=_", " +-./:=_", null, null));
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @CsvSource({"true,true", "true,false", "false,true", "false,false"})
    public void copySeal(boolean source, boolean destination) {
        AppendBlobAsyncClient appendBlobClient = ccAsync.getBlobAsyncClient(generateBlobName()).getAppendBlobAsyncClient();
        appendBlobClient.create().block();
        if (source) {
            appendBlobClient.seal();
        }

        AppendBlobAsyncClient bu2 = ccAsync.getBlobAsyncClient(generateBlobName()).getAppendBlobAsyncClient();

        PollerFlux<BlobCopyInfo, Void> poller = setPlaybackPollerFluxPollInterval(
            bu2.beginCopy(new BlobBeginCopyOptions(appendBlobClient.getBlobUrl()).setSealDestination(destination)));
        poller.blockLast();

        StepVerifier.create(bu2.getProperties()).assertNext(it ->
            assertEquals(Boolean.TRUE.equals(it.isSealed()), destination)).verifyComplete();

    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("copySourceACSupplier")
    public void copySourceAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                             String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t).block();
        BlockBlobAsyncClient copyDestBlob = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        match = setupBlobMatchCondition(bc, match);
        BlobBeginCopySourceRequestConditions mac = new BlobBeginCopySourceRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setTagsConditions(tags);

        PollerFlux<BlobCopyInfo, Void> poller = copyDestBlob.beginCopy(new BlobBeginCopyOptions(bc.getBlobUrl())
            .setSourceRequestConditions(mac));
        AsyncPollResponse<BlobCopyInfo, Void> response = poller.blockLast();
        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, response.getStatus());
    }

    private static Stream<Arguments> copySourceACSupplier() {
        return Stream.of(Arguments.of(null, null, null, null, null),
            Arguments.of(OLD_DATE, null, null, null, null),
            Arguments.of(null, NEW_DATE, null, null, null),
            Arguments.of(null, null, RECEIVED_ETAG, null, null),
            Arguments.of(null, null, null, GARBAGE_ETAG, null),
            Arguments.of(null, null, null, null, "\"foo\" = 'bar'"));
    }

    @ParameterizedTest
    @MethodSource("copySourceACFailSupplier")
    public void copySourceACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                 String tags) {
        BlockBlobAsyncClient copyDestBlob = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        noneMatch = setupBlobMatchCondition(bc, noneMatch);
        BlobBeginCopySourceRequestConditions mac = new BlobBeginCopySourceRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setTagsConditions(tags);

        PollerFlux<BlobCopyInfo, Void> poller = copyDestBlob.beginCopy(new BlobBeginCopyOptions(bc.getBlobUrl())
            .setSourceRequestConditions(mac));
        assertThrows(BlobStorageException.class, poller::blockLast);
    }

    private static Stream<Arguments> copySourceACFailSupplier() {
        return Stream.of(
            Arguments.of(NEW_DATE, null, null, null, null),
            Arguments.of(null, OLD_DATE, null, null, null),
            Arguments.of(null, null, GARBAGE_ETAG, null, null),
            Arguments.of(null, null, null, RECEIVED_ETAG, null),
            Arguments.of(null, null, null, null, "\"foo\" = 'bar'"));
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void copyDestAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                           String leaseID, String tags) {
        BlockBlobAsyncClient bu2 = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        bu2.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()).block();
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bu2.setTags(t).block();
        match = setupBlobMatchCondition(bu2, match);
        leaseID = setupBlobLeaseCondition(bu2, leaseID);
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        PollerFlux<BlobCopyInfo, Void> poller = setPlaybackPollerFluxPollInterval(bu2.beginCopy(bc.getBlobUrl(),
            null, null, null, null, bac, null));
        AsyncPollResponse<BlobCopyInfo, Void> response = poller.blockLast();
        assertNotNull(response);
        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, response.getStatus());
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsFailSupplier")
    public void copyDestACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                               String leaseID, String tags) {
        BlockBlobAsyncClient bu2 = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        bu2.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()).block();
        noneMatch = setupBlobMatchCondition(bu2, noneMatch);
        setupBlobLeaseCondition(bu2, leaseID);
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        PollerFlux<BlobCopyInfo, Void> poller = setPlaybackPollerFluxPollInterval(bu2.beginCopy(
            bc.getBlobUrl(), null, null, null, null, bac, null));
        assertThrows(BlobStorageException.class, poller::blockLast);
    }

    @Test
    public void delete() {
        StepVerifier.create(bc.deleteWithResponse(null, null))
            .assertNext(r -> {
                HttpHeaders headers = r.getHeaders();

                assertResponseStatusCode(r, 202);
                assertNotNull(headers.getValue(X_MS_REQUEST_ID));
                assertNotNull(headers.getValue(X_MS_VERSION));
                assertNotNull(headers.getValue(HttpHeaderName.DATE));
            })
            .verifyComplete();
    }

    @Test
    public void deleteMin() {
        assertAsyncResponseStatusCode(bc.deleteWithResponse(null,
            null), 202);
    }

    @ParameterizedTest
    @MethodSource("deleteOptionsSupplier")
    public void deleteOptions(DeleteSnapshotsOptionType option, int blobsRemaining) {
        bc.createSnapshot().block();
        // Create an extra blob so the list isn't empty (null) when we delete base blob, too
        BlockBlobAsyncClient bu2 = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        bu2.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()).block();

        bc.deleteWithResponse(option, null).block();

        StepVerifier.create(ccAsync.listBlobs().count())
            .assertNext(r -> assertEquals(blobsRemaining, r))
            .verifyComplete();
    }

    private static Stream<Arguments> deleteOptionsSupplier() {
        return Stream.of(
            Arguments.of(DeleteSnapshotsOptionType.INCLUDE, 1),
            Arguments.of(DeleteSnapshotsOptionType.ONLY, 2));
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void deleteAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                         String leaseID, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t).block();
        match = setupBlobMatchCondition(bc, match);
        leaseID = setupBlobLeaseCondition(bc, leaseID);
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        assertAsyncResponseStatusCode(bc.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, bac),
            202);
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsFailSupplier")
    public void deleteACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                             String leaseID, String tags) {
        noneMatch = setupBlobMatchCondition(bc, noneMatch);
        setupBlobLeaseCondition(bc, leaseID);
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        StepVerifier.create(bc.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, bac))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void blobDeleteError() {
        bc = ccAsync.getBlobAsyncClient(generateBlobName());
        StepVerifier.create(bc.delete())
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void deleteIfExistsContainer() {
        StepVerifier.create(bc.deleteIfExists())
            .assertNext(r -> assertTrue(r))
            .verifyComplete();
    }

    @Test
    public void deleteIfExists() {

        StepVerifier.create(bc.deleteIfExistsWithResponse(null, null))
            .assertNext(r -> {
                HttpHeaders headers = r.getHeaders();

                assertTrue(r.getValue());
                assertResponseStatusCode(r, 202);
                assertNotNull(headers.getValue(X_MS_REQUEST_ID));
                assertNotNull(headers.getValue(X_MS_VERSION));
                assertNotNull(headers.getValue(HttpHeaderName.DATE));
            })
            .verifyComplete();
    }

    @Test
    public void deleteIfExistsMin() {
        assertAsyncResponseStatusCode(bc.deleteIfExistsWithResponse(null,
            null), 202);
    }

    @Test
    public void deleteIfExistsBlobThatDoesNotExist() {
        bc = ccAsync.getBlobAsyncClient(generateBlobName());
        StepVerifier.create(bc.deleteIfExistsWithResponse(null, null))
            .assertNext(r -> {
                assertFalse(r.getValue());
                assertEquals(404, r.getStatusCode());
            })
            .verifyComplete();
    }

    @Test
    public void deleteIfExistsContainerThatWasAlreadyDeleted() {
        StepVerifier.create(bc.deleteIfExistsWithResponse(null, null))
            .assertNext(r -> {
                assertTrue(r.getValue());
                assertEquals(202, r.getStatusCode());
            })
            .verifyComplete();

        StepVerifier.create(bc.deleteIfExistsWithResponse(null, null))
            .assertNext(r -> {
                assertFalse(r.getValue());
                assertEquals(404, r.getStatusCode());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("deleteOptionsSupplier")
    public void deleteIfExistsOptions(DeleteSnapshotsOptionType option, int blobsRemaining) {
        bc.createSnapshot().block();
        // Create an extra blob so the list isn't empty (null) when we delete base blob, too
        BlockBlobAsyncClient bu2 = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        bu2.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()).block();

        bc.deleteIfExistsWithResponse(option, null).block();

        StepVerifier.create(ccAsync.listBlobs().count())
            .assertNext(r -> assertEquals(blobsRemaining, r))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void deleteIfExistsAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                 String leaseID, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t).block();
        match = setupBlobMatchCondition(bc, match);
        leaseID = setupBlobLeaseCondition(bc, leaseID);
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        assertAsyncResponseStatusCode(bc.deleteIfExistsWithResponse(DeleteSnapshotsOptionType.INCLUDE, bac),
            202);
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsFailSupplier")
    public void deleteIfExistsACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                     String leaseID, String tags) {
        noneMatch = setupBlobMatchCondition(bc, noneMatch);
        setupBlobLeaseCondition(bc, leaseID);
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        StepVerifier.create(bc.deleteIfExistsWithResponse(DeleteSnapshotsOptionType.INCLUDE, bac))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void setTierBlockBlob() {
        List<AccessTier> tiers = Arrays.asList(AccessTier.HOT, AccessTier.COOL, AccessTier.ARCHIVE);
        for (AccessTier tier : tiers) {
            BlobContainerAsyncClient cc = primaryBlobServiceAsyncClient.createBlobContainer(generateContainerName()).block();
            BlockBlobAsyncClient bc = cc.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
            bc.upload(DATA.getDefaultFlux(), DATA.getDefaultData().remaining()).block();

            StepVerifier.create(bc.setAccessTierWithResponse(tier, null, null))
                .assertNext(r -> {
                    HttpHeaders headers = r.getHeaders();

                    assertTrue(r.getStatusCode() == 200 || r.getStatusCode() == 202);
                    assertNotNull(headers.getValue(X_MS_VERSION));
                    assertNotNull(headers.getValue(X_MS_REQUEST_ID));
                })
                .verifyComplete();

            StepVerifier.create(bc.getProperties())
                .assertNext(r -> assertEquals(tier, r.getAccessTier()))
                .verifyComplete();

            StepVerifier.create(cc.listBlobs())
                .assertNext(r -> assertEquals(tier, r.getProperties().getAccessTier()))
                .verifyComplete();

            cc.delete().block();
        }
    }

    @ParameterizedTest
    @MethodSource("setTierPageBlobSupplier")
    public void setTierPageBlob(AccessTier tier) {
        BlobContainerAsyncClient cc = premiumBlobServiceAsyncClient.createBlobContainer(generateContainerName()).block();

        PageBlobAsyncClient bc = cc.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        bc.create(512).block();

        bc.setAccessTier(tier).block();

        StepVerifier.create(bc.getProperties())
            .assertNext(r -> assertEquals(tier, r.getAccessTier()))
            .verifyComplete();

        StepVerifier.create(cc.listBlobs())
            .assertNext(r -> assertEquals(tier, r.getProperties().getAccessTier()))
            .verifyComplete();

        // cleanup:
        cc.delete().block();
    }

    private static Stream<Arguments> setTierPageBlobSupplier() {
        return Stream.of(Arguments.of(AccessTier.P4), Arguments.of(AccessTier.P6), Arguments.of(AccessTier.P10),
            Arguments.of(AccessTier.P20), Arguments.of(AccessTier.P30), Arguments.of(AccessTier.P40),
            Arguments.of(AccessTier.P50));
    }

    @Test
    public void setTierMin() {
        BlobContainerAsyncClient cc = primaryBlobServiceAsyncClient.createBlobContainer(generateContainerName()).block();
        BlockBlobAsyncClient bu = cc.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        bu.upload(DATA.getDefaultFlux(), DATA.getDefaultData().remaining()).block();

        StepVerifier.create(bc.setAccessTierWithResponse(AccessTier.HOT, null, null))
            .assertNext(r -> assertTrue(r.getStatusCode() == 200 || r.getStatusCode() == 202))
            .verifyComplete();

        // cleanup:
        cc.delete().block();
    }

    @Test
    public void setTierInferred() {
        BlobContainerAsyncClient cc = primaryBlobServiceAsyncClient.createBlobContainer(generateContainerName()).block();
        BlockBlobAsyncClient bc = cc.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        bc.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()).block();

        StepVerifier.create(bc.getProperties())
            .assertNext(r -> assertTrue(r.isAccessTierInferred()))
            .verifyComplete();

        StepVerifier.create(cc.listBlobs())
            .assertNext(r -> assertTrue(r.getProperties().isAccessTierInferred()))
            .verifyComplete();

        bc.setAccessTier(AccessTier.HOT).block();

        StepVerifier.create(bc.getProperties())
            .assertNext(r -> assertNull(r.isAccessTierInferred()))
            .verifyComplete();

        StepVerifier.create(cc.listBlobs())
            .assertNext(r -> assertNull(r.getProperties().isAccessTierInferred()))
            .verifyComplete();

        cc.delete().block();
    }

    @ParameterizedTest
    @MethodSource("setTierArchiveStatusSupplier")
    public void setTierArchiveStatus(AccessTier sourceTier, AccessTier destTier, ArchiveStatus status) {
        BlobContainerAsyncClient cc = primaryBlobServiceAsyncClient.createBlobContainer(generateContainerName()).block();
        BlockBlobAsyncClient bc = cc.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        bc.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()).block();

        bc.setAccessTier(sourceTier).block();
        bc.setAccessTier(destTier).block();

        StepVerifier.create(bc.getProperties())
            .assertNext(r -> assertEquals(status, r.getArchiveStatus()))
            .verifyComplete();

        StepVerifier.create(cc.listBlobs())
            .assertNext(r -> assertEquals(status, r.getProperties().getArchiveStatus()))
            .verifyComplete();

        cc.delete().block();
    }

    private static Stream<Arguments> setTierArchiveStatusSupplier() {
        return Stream.of(
            Arguments.of(AccessTier.ARCHIVE, AccessTier.COOL, ArchiveStatus.REHYDRATE_PENDING_TO_COOL),
            Arguments.of(AccessTier.ARCHIVE, AccessTier.HOT, ArchiveStatus.REHYDRATE_PENDING_TO_HOT),
            Arguments.of(AccessTier.ARCHIVE, AccessTier.HOT, ArchiveStatus.REHYDRATE_PENDING_TO_HOT));
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-12-02")
    @Test
    public void setTierCold() {
        BlobContainerAsyncClient cc = primaryBlobServiceAsyncClient.createBlobContainer(generateContainerName()).block();
        BlockBlobAsyncClient bc = cc.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        bc.upload(DATA.getDefaultFlux(), DATA.getDefaultData().remaining()).block();

        StepVerifier.create(bc.setAccessTierWithResponse(AccessTier.COLD, null, null))
            .assertNext(r -> {
                HttpHeaders headers = r.getHeaders();

                assertTrue(r.getStatusCode() == 200 || r.getStatusCode() == 202);
                assertNotNull(headers.getValue(X_MS_VERSION));
                assertNotNull(headers.getValue(X_MS_REQUEST_ID));
            })
            .verifyComplete();

        StepVerifier.create(bc.getProperties())
            .assertNext(r -> assertEquals(AccessTier.COLD, r.getAccessTier()))
            .verifyComplete();

        StepVerifier.create(cc.listBlobs())
            .assertNext(r -> assertEquals(AccessTier.COLD, r.getProperties().getAccessTier()))
            .verifyComplete();

        cc.delete().block();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-12-02")
    @Test
    public void setTierArchiveStatusRehydratePendingToCold() {
        BlobContainerAsyncClient cc = primaryBlobServiceAsyncClient.createBlobContainer(generateContainerName()).block();
        BlockBlobAsyncClient bc = cc.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        bc.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()).block();

        bc.setAccessTier(AccessTier.ARCHIVE).block();
        bc.setAccessTier(AccessTier.COLD).block();

        StepVerifier.create(bc.getProperties())
            .assertNext(r -> assertEquals(ArchiveStatus.REHYDRATE_PENDING_TO_COLD, r.getArchiveStatus()))
            .verifyComplete();

        StepVerifier.create(cc.listBlobs())
            .assertNext(r -> assertEquals(ArchiveStatus.REHYDRATE_PENDING_TO_COLD, r.getProperties().getArchiveStatus()))
            .verifyComplete();

        cc.delete().block();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("setTierRehydratePrioritySupplier")
    public void setTierRehydratePriority(RehydratePriority rehydratePriority) {
        if (rehydratePriority != null) {
            bc.setAccessTier(AccessTier.ARCHIVE).block();
            bc.setAccessTierWithResponse(new BlobSetAccessTierOptions(AccessTier.HOT).setPriority(rehydratePriority)).block();
        }

        StepVerifier.create(bc.getPropertiesWithResponse(null))
            .assertNext(r -> {
                assertEquals(200, r.getStatusCode());
                assertEquals(rehydratePriority, r.getValue().getRehydratePriority());
            })
            .verifyComplete();
    }

    private static Stream<Arguments> setTierRehydratePrioritySupplier() {
        return Stream.of(
            Arguments.of((RehydratePriority) null),
            Arguments.of(RehydratePriority.STANDARD),
            Arguments.of(RehydratePriority.HIGH));
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void setTierSnapshot() {
        BlobAsyncClientBase bc2 = bc.createSnapshot().block();

        bc2.setAccessTier(AccessTier.COOL).block();

        StepVerifier.create(bc2.getProperties())
            .assertNext(r -> assertEquals(AccessTier.COOL, r.getAccessTier()))
            .verifyComplete();

        StepVerifier.create(bc.getProperties())
            .assertNext(r -> assertNotEquals(AccessTier.COOL, r.getAccessTier()))
            .verifyComplete();
    }

    @Test
    public void setTierSnapshotError() {
        bc.createSnapshotWithResponse(null, null).block();
        String fakeVersion = "2020-04-17T20:37:16.5129130Z";
        BlobAsyncClient bc2 = bc.getSnapshotClient(fakeVersion);

        StepVerifier.create(bc2.setAccessTier(AccessTier.COOL))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void setTierError() {
        BlobContainerAsyncClient cc = primaryBlobServiceAsyncClient.createBlobContainer(generateContainerName()).block();
        BlockBlobAsyncClient bc = cc.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        bc.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()).block();

        StepVerifier.create(bc.setAccessTier(AccessTier.fromString("garbage")))
            .verifyErrorSatisfies(r -> {
                BlobStorageException e = assertInstanceOf(BlobStorageException.class, r);
                assertEquals(BlobErrorCode.INVALID_HEADER_VALUE, e.getErrorCode());
            });

        // cleanup:
        cc.delete().block();
    }

    @Test
    public void setTierIllegalArgument() {
        StepVerifier.create(bc.setAccessTier(null))
            .verifyError(NullPointerException.class);
    }

    @Test
    public void setTierLease() {
        BlobContainerAsyncClient cc = primaryBlobServiceAsyncClient.createBlobContainer(generateContainerName()).block();
        BlockBlobAsyncClient bc = cc.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        bc.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()).block();
        String leaseID = setupBlobLeaseCondition(bc, RECEIVED_LEASE_ID);

        StepVerifier.create(bc.setAccessTierWithResponse(AccessTier.HOT, null, leaseID))
            .expectNextCount(1)
            .verifyComplete();

        // cleanup:
        cc.delete().block();
    }

    @Test
    public void setTierLeaseFail() {
        BlobContainerAsyncClient cc = primaryBlobServiceAsyncClient.createBlobContainer(generateContainerName()).block();
        BlockBlobAsyncClient bc = cc.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        bc.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()).block();

        StepVerifier.create(bc.setAccessTierWithResponse(AccessTier.HOT, null, "garbage"))
            .verifyError(BlobStorageException.class);

        cc.delete().block();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void setTierTags() {
        BlobContainerAsyncClient cc = primaryBlobServiceAsyncClient.createBlobContainer(generateContainerName()).block();
        BlockBlobAsyncClient bc = cc.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        bc.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()).block();
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t).block();

        StepVerifier.create(bc.setAccessTierWithResponse(new BlobSetAccessTierOptions(AccessTier.HOT)
            .setTagsConditions("\"foo\" = 'bar'")))
            .expectNextCount(1)
            .verifyComplete();

        // cleanup:
        cc.delete().block();
    }

    @Test
    public void setTierTagsFail() {
        BlobContainerAsyncClient cc = primaryBlobServiceAsyncClient.createBlobContainer(generateContainerName()).block();
        BlockBlobAsyncClient bc = cc.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        bc.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()).block();

        StepVerifier.create(bc.setAccessTierWithResponse(
            new BlobSetAccessTierOptions(AccessTier.HOT).setTagsConditions("\"foo\" = 'bar'")))
            .verifyError(BlobStorageException.class);

        cc.delete().block();
    }

    @ParameterizedTest
    @MethodSource("copyWithTierSupplier")
    public void copyWithTier(AccessTier tier1, AccessTier tier2) {
        String blobName = generateBlobName();
        BlockBlobAsyncClient bc = ccAsync.getBlobAsyncClient(blobName).getBlockBlobAsyncClient();
        bc.uploadWithResponse(DATA.getDefaultFlux(), DATA.getDefaultDataSize(), null, null, tier1,
            null, null).block();
        BlockBlobAsyncClient bcCopy = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();

        String secondSas = ccAsync.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusHours(1),
            new BlobSasPermission().setReadPermission(true)));
        bcCopy.copyFromUrlWithResponse(bc.getBlobUrl() + "?" + secondSas, null, tier2,
            null, null).block();

        StepVerifier.create(bcCopy.getProperties())
            .assertNext(r -> assertEquals(tier2, r.getAccessTier()))
            .verifyComplete();
    }

    private static Stream<Arguments> copyWithTierSupplier() {
        return Stream.of(
            Arguments.of(AccessTier.HOT, AccessTier.COOL),
            Arguments.of(AccessTier.COOL, AccessTier.HOT));
    }

    @Test
    public void undeleteError() {
        bc = ccAsync.getBlobAsyncClient(generateBlobName());
        StepVerifier.create(bc.undelete())
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void getAccountInfo() {
        StepVerifier.create(primaryBlobServiceAsyncClient.getAccountInfoWithResponse())
            .assertNext(r -> {
                assertNotNull(r.getHeaders().getValue(HttpHeaderName.DATE));
                assertNotNull(r.getHeaders().getValue(X_MS_REQUEST_ID));
                assertNotNull(r.getHeaders().getValue(X_MS_VERSION));
                assertNotNull(r.getValue().getAccountKind());
                assertNotNull(r.getValue().getSkuName());
                assertFalse(r.getValue().isHierarchicalNamespaceEnabled());
            })
            .verifyComplete();
    }

    @Test
    public void getAccountInfoMin() {
        assertAsyncResponseStatusCode(bc.getAccountInfoWithResponse(),
            200);
    }

    @Test
    public void getAccountInfoBase() {
        StepVerifier.create(bc.getAccountInfo())
            .assertNext(r -> {
                assertNotNull(r.getAccountKind());
                assertNotNull(r.getSkuName());
                assertFalse(r.isHierarchicalNamespaceEnabled());
            })
            .verifyComplete();
    }

    @Test
    public void getAccountInfoBaseFail() {
        BlobServiceAsyncClient serviceClient = instrument(new BlobServiceClientBuilder()
            .endpoint(ENVIRONMENT.getPrimaryAccount().getBlobEndpoint())
            .credential(new MockTokenCredential()))
            .buildAsyncClient();

        BlobAsyncClient blobClient = serviceClient.getBlobContainerAsyncClient(generateContainerName()).getBlobAsyncClient(generateBlobName());

        StepVerifier.create(blobClient.getAccountInfo())
            .verifyErrorSatisfies(r -> {
                BlobStorageException e = assertInstanceOf(BlobStorageException.class, r);
                assertEquals(BlobErrorCode.INVALID_AUTHENTICATION_INFO, e.getErrorCode());
            });
    }

    @Test
    public void getContainerName() {
        assertEquals(containerName, bc.getContainerName());
    }

    @Test
    public void getContainerClient() {
        String sasToken = ccAsync.generateSas(
            new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(2),
                new BlobSasPermission().setReadPermission(true)));

        // Ensure a sas token is also persisted
        ccAsync = getContainerAsyncClient(sasToken, ccAsync.getBlobContainerUrl());

        // Ensure the correct endpoint
        assertEquals(ccAsync.getBlobContainerUrl(), bc.getContainerAsyncClient().getBlobContainerUrl());
        // Ensure it is a functional client
        StepVerifier.create(bc.getContainerAsyncClient().getProperties())
            .assertNext(r -> assertNotNull(r))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("getBlobNameSupplier")
    public void getBlobName(String inputName, String expectedOutputName) {
        bc = ccAsync.getBlobAsyncClient(inputName);
        assertEquals(expectedOutputName, bc.getBlobName());
    }

    private static Stream<Arguments> getBlobNameSupplier() {
        return Stream.of(
            Arguments.of("blobName", "blobName"),
            Arguments.of("dir1/a%20b.txt", "dir1/a%20b.txt"),
            Arguments.of("path/to]a blob", "path/to]a blob"),
            Arguments.of("path%2Fto%5Da%20blob", "path%2Fto%5Da%20blob"),
            Arguments.of("斑點", "斑點"),
            Arguments.of("%E6%96%91%E9%BB%9E", "%E6%96%91%E9%BB%9E"),
            Arguments.of("斑點", "斑點"));
    }

    @ParameterizedTest
    @MethodSource("getBlobNameSupplier")
    public void getBlobNameAndBuildClient(String originalBlobName, String finalBlobName) {
        BlobAsyncClient client = ccAsync.getBlobAsyncClient(originalBlobName);
        BlobAsyncClientBase baseClient = ccAsync.getBlobAsyncClient(client.getBlobName()).getBlockBlobAsyncClient();

        assertEquals(finalBlobName, baseClient.getBlobName());
    }

    private static Stream<Arguments> getNonEncodedBlobNameSupplier() {
        return Stream.of(
            Arguments.of("test%test"),
            Arguments.of("ab2a7d5f-b973-4222-83ba-d0581817a819 %Россия 한국 中国!?/file"),
            Arguments.of("%E6%96%91%E9%BB%9E"),
            Arguments.of("斑點"));
    }

    @ParameterizedTest
    @MethodSource("getNonEncodedBlobNameSupplier")
    public void getNonEncodedBlobName(String originalBlobName) {
        BlobAsyncClient client = ccAsync.getBlobAsyncClient(originalBlobName);
        BlockBlobAsyncClient blockBlobClient = ccAsync.getBlobAsyncClient(client.getBlobName()).getBlockBlobAsyncClient();
        assertEquals(blockBlobClient.getBlobName(), originalBlobName);

        // see if the blob name will be properly encoded in the url
        String encodedName = Utility.urlEncode(originalBlobName);
        assertTrue(ccAsync.getBlobAsyncClient(originalBlobName).getBlobUrl().contains(encodedName));
    }

    @Test
    public void getNonEncodedSpecializedBlob() {
        String originalBlobName = "test%test";
        SpecializedBlobClientBuilder specializedBlobClientBuilder = getSpecializedBuilder(ccAsync.getBlobContainerUrl());
        specializedBlobClientBuilder.containerName(ccAsync.getBlobContainerName()).blobName(originalBlobName);

        BlockBlobAsyncClient blockBlobClient = specializedBlobClientBuilder.buildBlockBlobAsyncClient();
        assertEquals(blockBlobClient.getBlobName(), originalBlobName);

        // see if the blob name will be properly encoded in the url
        String encodedName = Utility.urlEncode(originalBlobName);
        assertTrue(ccAsync.getBlobAsyncClient(originalBlobName).getBlobUrl().contains(encodedName));
    }

    @Test
    public void getNonEncodedBlobClient() {
        String originalBlobName = "test%test";
        BlobClientBuilder blobClientBuilder = getBlobClientBuilder(ccAsync.getBlobContainerUrl());
        blobClientBuilder.containerName(ccAsync.getBlobContainerName()).blobName(originalBlobName);

        BlobAsyncClient blobClient = blobClientBuilder.buildAsyncClient();
        assertEquals(blobClient.getBlobName(), originalBlobName);

        // see if the blob name will be properly encoded in the url
        String encodedName = Utility.urlEncode(originalBlobName);
        assertTrue(ccAsync.getBlobAsyncClient(originalBlobName).getBlobUrl().contains(encodedName));
    }

    @Test
    public void builderCpkValidation() {
        URL endpoint = BlobUrlParts.parse(bc.getBlobUrl()).setScheme("http").toUrl();
        BlobClientBuilder builder = new BlobClientBuilder()
            .customerProvidedKey(new CustomerProvidedKey(Base64.getEncoder().encodeToString(getRandomByteArray(256))))
            .endpoint(endpoint.toString());

        assertThrows(IllegalArgumentException.class, builder::buildAsyncClient);
    }

    @Test
    public void builderBearerTokenValidation() {
        URL endpoint = BlobUrlParts.parse(bc.getBlobUrl()).setScheme("http").toUrl();
        BlobClientBuilder builder = new BlobClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint.toString());

        assertThrows(IllegalArgumentException.class, builder::buildAsyncClient);
    }

    @Test
    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials
    // and auth would fail because we changed a signed header.
    public void perCallPolicy() {
        bc = getBlobClientBuilder(bc.getBlobUrl()).addPolicy(getPerCallVersionPolicy()).buildAsyncClient();
        StepVerifier.create(bc.getPropertiesWithResponse(null))
            .assertNext(r -> assertEquals("2017-11-09", r.getHeaders().getValue(X_MS_VERSION)))
            .verifyComplete();
    }

    @Test
    public void specializedChildClientGetsCached() {
        assertEquals(bc.getBlockBlobAsyncClient(), bc.getBlockBlobAsyncClient());
        assertEquals(bc.getAppendBlobAsyncClient(), bc.getAppendBlobAsyncClient());
        assertEquals(bc.getPageBlobAsyncClient(), bc.getPageBlobAsyncClient());
    }

    @Test
    public void defaultAudience() {
        BlobAsyncClient aadBlob = getBlobClientBuilderWithTokenCredential(bc.getBlobUrl())
            .audience(null)
            .buildAsyncClient();

        StepVerifier.create(aadBlob.exists())
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    public void storageAccountAudience() {
        BlobAsyncClient aadBlob = getBlobClientBuilderWithTokenCredential(bc.getBlobUrl())
            .audience(BlobAudience.createBlobServiceAccountAudience(ccAsync.getAccountName()))
            .buildAsyncClient();

        StepVerifier.create(aadBlob.exists())
            .expectNext(true)
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2024-08-04")
    @LiveOnly
    @Test
    /* This test tests if the bearer challenge is working properly. A bad audience is passed in, the service returns
    the default audience, and the request gets retried with this default audience, making the call function as expected.
     */
    public void audienceErrorBearerChallengeRetry() {
        BlobAsyncClient aadBlob = getBlobClientBuilderWithTokenCredential(bc.getBlobUrl())
            .audience(BlobAudience.createBlobServiceAccountAudience("badAudience"))
            .buildAsyncClient();

        StepVerifier.create(aadBlob.getProperties())
            .assertNext(r -> assertNotNull(r))
            .verifyComplete();
    }

    @Test
    public void audienceFromString() {
        String url = String.format("https://%s.blob.core.windows.net/", ccAsync.getAccountName());
        BlobAudience audience = BlobAudience.fromString(url);

        BlobAsyncClient aadBlob = getBlobClientBuilderWithTokenCredential(bc.getBlobUrl())
            .audience(audience)
            .buildAsyncClient();

        StepVerifier.create(aadBlob.exists())
            .expectNext(true)
            .verifyComplete();
    }

}
