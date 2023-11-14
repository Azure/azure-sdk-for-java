// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.ProgressListener;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobDownloadContentResponse;
import com.azure.storage.blob.models.BlobDownloadHeaders;
import com.azure.storage.blob.models.BlobDownloadResponse;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.BlobType;
import com.azure.storage.blob.models.Block;
import com.azure.storage.blob.models.BlockListType;
import com.azure.storage.blob.models.DownloadRetryOptions;
import com.azure.storage.blob.models.LeaseStateType;
import com.azure.storage.blob.models.LeaseStatusType;
import com.azure.storage.blob.models.ObjectReplicationPolicy;
import com.azure.storage.blob.models.ObjectReplicationStatus;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.blob.options.BlobSetTagsOptions;
import com.azure.storage.blob.specialized.AppendBlobAsyncClient;
import com.azure.storage.blob.specialized.AppendBlobClient;
import com.azure.storage.blob.specialized.BlobAsyncClientBase;
import com.azure.storage.blob.specialized.BlobClientBase;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.TestDataFactory;
import com.azure.storage.common.test.shared.policy.MockFailureResponsePolicy;
import com.azure.storage.common.test.shared.policy.MockRetryRangeResponsePolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import java.sql.Blob;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
    public void uploadInputStreamOverwriteFails() {
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

        StepVerifier.create(bc.downloadStreamWithResponse(null, null, null, false)
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

        StepVerifier.create(bc.downloadStreamWithResponse(null, null, null, false)
                .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue())))
            .assertNext(r -> TestUtils.assertArraysEqual(r, randomData))
            .verifyComplete();
    }

    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    @Test
    public void uploadFluxLargeData() {
        byte[] randomData = getRandomByteArray(20 * Constants.MB);
        Flux<ByteBuffer> input = Flux.just(ByteBuffer.wrap(randomData));

        ParallelTransferOptions pto = new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) Constants.MB);

        StepVerifier.create(bc.uploadWithResponse(input, pto, null, null, null, null))
            .expectNextCount(1)
            .verifyComplete();
    }

    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    @ParameterizedTest
    @MethodSource("uploadNumBlocksSupplier")
    public void uploadNumBlocks(int size, Long maxUploadSize, long numBlocks) {
        byte[] randomData = getRandomByteArray(size);
        Flux<ByteBuffer> input = Flux.just(ByteBuffer.wrap(randomData));

        ParallelTransferOptions pto = new ParallelTransferOptions().setBlockSizeLong(maxUploadSize)
            .setMaxSingleUploadSizeLong(maxUploadSize);

        bc.uploadWithResponse(input, pto, null, null, null, null).block();

        StepVerifier.create(bc.getBlockBlobAsyncClient().listBlocks(BlockListType.ALL))
            .assertNext(r -> assertEquals(r.getCommittedBlocks().size(), numBlocks))
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

    //todo isbr: fix
    public void uploadFluxMin() {
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
            assertThrows(Exception.class, () -> bc.uploadWithResponse(new BlobParallelUploadOptions(DATA.getDefaultInputStream(), length)).block());
        }
    }

    @Test
    public void uploadSuccessfulRetry() {
        BlobAsyncClient clientWithFailure = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(), bc.getBlobUrl(),
            new TransientFailureInjectingHttpPipelinePolicy());

        assertNotNull(clientWithFailure);
        clientWithFailure.uploadWithResponse(new BlobParallelUploadOptions(DATA.getDefaultInputStream())).block();
        StepVerifier.create(bc.downloadContent())
            .assertNext(r -> TestUtils.assertArraysEqual(r.toBytes(), DATA.getDefaultBytes()))
            .verifyComplete();
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20211202ServiceVersion")
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

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
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

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
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

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
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

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
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

        //todo isbr:
        //assertAsyncResponseStatusCode(bc.downloadWithResponse(null, null, bac,
          //  false), 200);
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
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

        //todo isbr:
        //assertAsyncResponseStatusCode(bc.downloadStreamWithResponse(null, null, bac,
        //  false), 200);
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
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

        //todo isbr:
        //assertAsyncResponseStatusCode(bc.downloadContentWithResponse(null, bac, 200);
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
        StepVerifier.create(bc.downloadStreamWithResponse(new BlobRange(0, 3L), null, null, true))
            .assertNext(r -> {
                byte[] contentMD5 = r.getDeserializedHeaders().getContentMd5();
                try {
                    TestUtils.assertArraysEqual(MessageDigest.getInstance("MD5").digest(DATA.getDefaultText().substring(0, 3).getBytes()),
                        contentMD5);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            })
            .verifyComplete();
    }

    @Test
    public void downloadRetryDefault() {
        BlobAsyncClient failureBlobClient = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(), bc.getBlobUrl(),
            new MockFailureResponsePolicy(5));

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

    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
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
                new ParallelTransferOptions().setBlockSizeLong(4L * 1024 * 1024), null, null, false))
            .assertNext(r -> {
                assertEquals(r.getValue().getBlobType(), BlobType.BLOCK_BLOB);
                assertNotNull(r.getValue().getCreationTime());
            })
            .verifyComplete();
    }

    /*
     * Tests downloading a file using a default client that doesn't have a HttpClient passed to it.
     */
    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
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
            .assertNext(it -> assertEquals(it.getValue().getBlobType(), BlobType.BLOCK_BLOB))
            .verifyComplete();

        assertTrue(compareFiles(file, outFile, 0, fileSize));

        // cleanup:
        blobServiceAsyncClient.deleteBlobContainer(containerName);
    }

    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
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

        bc.downloadToFileWithResponse(outFile.toPath().toString(), range, null, null, null, false).block();

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

    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
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
            new BlobRange(DATA.getDefaultDataSize() + 1), null, null, null, false))
            .verifyError(BlobStorageException.class);
    }

    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    @Test
    public void downloadFileCountNull() throws IOException {
        File file = getRandomFile(DATA.getDefaultDataSize());
        file.deleteOnExit();
        createdFiles.add(file);

        bc.uploadFromFile(file.toPath().toString(), true).block();
        File outFile = new File(generateBlobName());
        createdFiles.add(outFile);
        Files.deleteIfExists(outFile.toPath());

        bc.downloadToFileWithResponse(outFile.toPath().toString(), new BlobRange(0), null, null, null, false).block();

        assertTrue(compareFiles(file, outFile, 0, DATA.getDefaultDataSize()));
    }

    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
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

    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
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

        StepVerifier.create(bc.downloadToFileWithResponse(outFile.toPath().toString(), null, null,
            null, bro, false))
            .verifyErrorSatisfies(r -> {
                BlobStorageException e = assertInstanceOf(BlobStorageException.class, r);
                assertTrue(e.getErrorCode() == BlobErrorCode.CONDITION_NOT_MET
                    || e.getErrorCode() == BlobErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION);
            });
    }

    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    @Test
    public void downloadFileETagLock() throws IOException {
        File file = getRandomFile(Constants.MB);
        file.deleteOnExit();
        createdFiles.add(file);

        bc.uploadFromFile(file.toPath().toString(), true);

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
        StepVerifier.create(bacDownloading.downloadToFileWithResponse(outFile.toPath().toString(), null, options, null,
            null, false)).verifyErrorSatisfies(it -> {
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
    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
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

    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
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

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @Test
    public void getPropertiesDefault() {
        bc.setTags(Collections.singletonMap("foo", "bar")).block();
        StepVerifier.create(bc.getPropertiesWithResponse(null))
            .assertNext(r -> {
                HttpHeaders headers = r.getHeaders();
                BlobProperties properties = r.getValue();

                assertTrue(validateBasicHeaders(headers));
                assertTrue(CoreUtils.isNullOrEmpty(properties.getMetadata()));
                assertEquals(properties.getBlobType(), BlobType.BLOCK_BLOB);
                assertNull(properties.getCopyCompletionTime()); // tested in "copy"
                assertNull(properties.getCopyStatusDescription()); // only returned when the service has errors; cannot validate
                assertNull(properties.getCopyId()); // tested in "abort copy"
                assertNull(properties.getCopyProgress()); // tested in "copy"
                assertNull(properties.getCopySource()); // tested in "copy"
                assertNull(properties.getCopyStatus()); // tested in "copy"
                assertNull(properties.isIncrementalCopy()); // tested in PageBlob."start incremental copy"
                assertNull(properties.getCopyDestinationSnapshot()); // tested in PageBlob."start incremental copy"
                assertNull(properties.getLeaseDuration()); // tested in "acquire lease"
                assertEquals(properties.getLeaseState(), LeaseStateType.AVAILABLE);
                assertEquals(properties.getLeaseStatus(), LeaseStatusType.UNLOCKED);
                assertTrue(properties.getBlobSize() >= 0);
                assertNotNull(properties.getContentType());
                assertNotNull(properties.getContentMd5());
                assertNull(properties.getContentEncoding()); // tested in "set HTTP headers"
                assertNull(properties.getContentDisposition()); // tested in "set HTTP headers"
                assertNull(properties.getContentLanguage()); // tested in "set HTTP headers"
                assertNull(properties.getCacheControl()); // tested in "set HTTP headers"
                assertNull(properties.getBlobSequenceNumber()); // tested in PageBlob."create sequence number"
                assertEquals(headers.getValue(HttpHeaderName.ACCEPT_RANGES), "bytes");
                assertNull(properties.getCommittedBlockCount()); // tested in AppendBlob."append block"
                assertTrue(properties.isServerEncrypted());
                assertEquals(properties.getAccessTier(), AccessTier.HOT);
                assertTrue(properties.isAccessTierInferred());
                assertNull(properties.getArchiveStatus()); // tested in "set tier"
                assertNotNull(properties.getCreationTime());
                assertEquals(properties.getTagCount(), 1);
                assertNull(properties.getRehydratePriority()); // tested in setTier rehydrate priority
                assertNull(properties.isSealed()); // tested in AppendBlob. "seal blob"
                assertNotNull(properties.getRequestId());
                //properties.getLastAccessedTime() /* TODO: re-enable when last access time enabled. */
            })
            .verifyComplete();
    }

    @Test
    public void getPropertiesMin() {
        //todo isbr:
        //assertAsyncResponseStatusCode(bc.getPropertiesWithResponse(null), 200);
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
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

        //todo isbr:
        //assertAsyncResponseStatusCode(bc.getPropertiesWithResponse(bac), 200);
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

    //todo isbr:
    @EnabledIf("com.azure.storage.blob.BlobTestBase#isPlaybackMode")
    @Test
    public void getPropertiesORS() throws MalformedURLException {
        BlobClient sourceBlob = primaryBlobServiceClient.getBlobContainerClient("test1")
            .getBlobClient("javablobgetpropertiesors2blobapitestgetpropertiesors57d93407b");
        BlobClient destBlob = alternateBlobServiceClient.getBlobContainerClient("test2")
            .getBlobClient("javablobgetpropertiesors2blobapitestgetpropertiesors57d93407b");

        BlobProperties sourceProperties = sourceBlob.getProperties();
        BlobDownloadResponse sourceDownloadHeaders = sourceBlob.downloadWithResponse(new ByteArrayOutputStream(), null,
            null, null, false, null, null);
        BlobProperties destProperties = destBlob.getProperties();
        BlobDownloadResponse destDownloadHeaders = destBlob.downloadWithResponse(new ByteArrayOutputStream(), null,
            null, null, false, null, null);

        assertTrue(validateOR(sourceProperties.getObjectReplicationSourcePolicies(),
            "fd2da1b9-56f5-45ff-9eb6-310e6dfc2c80", "105f9aad-f39b-4064-8e47-ccd7937295ca"));
        validateOR(sourceDownloadHeaders.getDeserializedHeaders().getObjectReplicationSourcePolicies(),
            "fd2da1b9-56f5-45ff-9eb6-310e6dfc2c80", "105f9aad-f39b-4064-8e47-ccd7937295ca");

        // There is a sas token attached at the end. Only check that the path is the same.
        assertTrue(destProperties.getCopySource().contains(new URL(sourceBlob.getBlobUrl()).getPath()));
        assertEquals(destProperties.getObjectReplicationDestinationPolicyId(), "fd2da1b9-56f5-45ff-9eb6-310e6dfc2c80");
        assertEquals(destDownloadHeaders.getDeserializedHeaders().getObjectReplicationDestinationPolicyId(),
            "fd2da1b9-56f5-45ff-9eb6-310e6dfc2c80");
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
            .assertNext(r -> assertTrue(validateBlobProperties(r, cacheControl, contentDisposition, contentEncoding, contentLanguage, contentMD5, contentType)))
            .verifyComplete();
    }

    private static Stream<Arguments> setHTTPHeadersHeadersSupplier() throws NoSuchAlgorithmException {
        return Stream.of(Arguments.of(null, null, null, null, null, null),
            Arguments.of("control", "disposition", "encoding", "language",
                Base64.getEncoder().encode(MessageDigest.getInstance("MD5").digest(DATA.getDefaultBytes())),
                "type"));
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
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

        //todo isbr
        //assertAsyncResponseStatusCode(bc.setHttpHeadersWithResponse(null, bac), 200);
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

        StepVerifier.create( bc.setHttpHeadersWithResponse(null, bac))
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

        //todo isbr:
        //assertAsyncResponseStatusCode(bc.setMetadataWithResponse(metadata, null), statusCode);
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

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
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

        //todo isbr:
        //assertAsyncResponseStatusCode(bc.setMetadataWithResponse(null, bac), 200);
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

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @Test
    public void setTagsAllNull() {
        //todo isbr:
        //assertAsyncResponseStatusCode(bc.setTagsWithResponse(new BlobSetTagsOptions(new HashMap<>())), 204
        //StepVerifier.create(bc.getTags())
        //.assert(r -> assertEquals(0, r.size())
        //.verifyComplete();
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @Test
    public void setTagsMin() {
        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "bar");
        bc.setTags(tags).block();
        StepVerifier.create(bc.getTags())
            .assertNext(r -> assertEquals(tags, r))
            .verifyComplete();
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
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
        //todo isbr:
        //assertAsyncResponseStatusCode(bc.setTagsWithResponse(new BlobSetTagsOptions(tags)), statusCode);
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

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("setTagsACSupplier")
    public void setTagsAC(String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t).block();
        t = new HashMap<>();
        t.put("fizz", "buzz");

        //todo isbr:
        //assertAsyncResponseStatusCode(bc.setTagsWithResponse(new BlobSetTagsOptions(t).setRequestConditions(
          //  new BlobRequestConditions().setTagsConditions(tags))), 204);
    }

    private static Stream<Arguments> setTagsACSupplier() {
        return Stream.of(
            Arguments.of((String) null),
            Arguments.of("\"foo\" = 'bar'")
        );
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20190707ServiceVersion")
    @Test
    public void setTagsACFail() {
        Map<String, String> t = new HashMap<>();
        t.put("fizz", "buzz");
        String tags = "\"foo\" = 'bar'";

        StepVerifier.create(bc.setTagsWithResponse(new BlobSetTagsOptions(t)
            .setRequestConditions(new BlobRequestConditions().setTagsConditions(tags))))
            .verifyError(BlobStorageException.class);
    }



}
