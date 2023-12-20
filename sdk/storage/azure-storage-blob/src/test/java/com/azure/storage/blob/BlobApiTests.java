// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.RequestConditions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.HttpClientOptions;
import com.azure.core.util.ProgressListener;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.ArchiveStatus;
import com.azure.storage.blob.models.BlobAudience;
import com.azure.storage.blob.models.BlobBeginCopySourceRequestConditions;
import com.azure.storage.blob.models.BlobCopyInfo;
import com.azure.storage.blob.models.BlobCopySourceTagsMode;
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
import com.azure.storage.blob.models.CopyStatusType;
import com.azure.storage.blob.models.CustomerProvidedKey;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.models.DownloadRetryOptions;
import com.azure.storage.blob.models.LeaseStateType;
import com.azure.storage.blob.models.LeaseStatusType;
import com.azure.storage.blob.models.ObjectReplicationPolicy;
import com.azure.storage.blob.models.ObjectReplicationStatus;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.models.RehydratePriority;
import com.azure.storage.blob.models.StorageAccountInfo;
import com.azure.storage.blob.models.SyncCopyStatusType;
import com.azure.storage.blob.options.BlobBeginCopyOptions;
import com.azure.storage.blob.options.BlobCopyFromUrlOptions;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import com.azure.storage.blob.options.BlobGetTagsOptions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.blob.options.BlobSetAccessTierOptions;
import com.azure.storage.blob.options.BlobSetTagsOptions;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.blob.specialized.AppendBlobAsyncClient;
import com.azure.storage.blob.specialized.AppendBlobClient;
import com.azure.storage.blob.specialized.BlobClientBase;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.blob.specialized.PageBlobClient;
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder;
import com.azure.storage.common.Utility;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.policy.RequestRetryOptions;
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
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.Exceptions;
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
import java.time.Duration;
import java.time.LocalDate;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlobApiTests extends BlobTestBase {
    private BlobClient bc;
    private BlobAsyncClient bcAsync;
    private final List<File> createdFiles = new ArrayList<>();

    @BeforeEach
    public void setup() {
        String blobName = generateBlobName();
        bc = cc.getBlobClient(blobName);
        bcAsync = ccAsync.getBlobAsyncClient(blobName);
        bc.getBlockBlobClient().upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
    }

    @AfterEach
    public void cleanup() {
        createdFiles.forEach(File::delete);
    }

    @Test
    public void uploadInputStreamOverwriteFails() {
        assertThrows(BlobStorageException.class, () -> bc.upload(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize()));
    }

    @Test
    public void uploadBinaryDataOverwriteFails() {
        assertThrows(BlobStorageException.class, () -> bc.upload(DATA.getDefaultBinaryData()));
    }

    @Test
    public void uploadInputStreamOverwrite() {
        byte[] randomData = getRandomByteArray(Constants.KB);
        ByteArrayInputStream input = new ByteArrayInputStream(randomData);

        bc.upload(input, Constants.KB, true);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bc.downloadStreamWithResponse(stream, null, null, null, false,
            null, null);
        TestUtils.assertArraysEqual(stream.toByteArray(), randomData);
    }

    @Test
    public void uploadBinaryDataOverwrite() {
        byte[] randomData = getRandomByteArray(Constants.KB);

        bc.upload(BinaryData.fromBytes(randomData), true);

        BinaryData blobContent = bc.downloadContent();
        TestUtils.assertArraysEqual(blobContent.toBytes(), randomData);
    }

    /* Tests an issue found where buffered upload would not deep copy buffers while determining what upload path to
    take. */
    @ParameterizedTest
    @ValueSource(ints = {
        Constants.KB, /* Less than copyToOutputStream buffer size, Less than maxSingleUploadSize */
        8 * Constants.KB, /* Equal to copyToOutputStream buffer size, Less than maxSingleUploadSize */
        20 * Constants.KB }) /* Greater than copyToOutputStream buffer size, Less than maxSingleUploadSize */
    public void uploadInputStreamSingleUpload() {
        byte[] randomData = getRandomByteArray(20 * Constants.KB);
        ByteArrayInputStream input = new ByteArrayInputStream(randomData);

        bc.upload(input, 20 * Constants.KB, true);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bc.downloadStreamWithResponse(stream, null, null, null, false, null, null);
        TestUtils.assertArraysEqual(stream.toByteArray(), randomData);
    }

    /* TODO (gapra): Add more tests to test large data sizes. */

    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    @Test
    public void uploadInputStreamLargeData() {
        byte[] randomData = getRandomByteArray(20 * Constants.MB);
        ByteArrayInputStream input = new ByteArrayInputStream(randomData);

        ParallelTransferOptions pto = new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) Constants.MB);

        // Uses blob output stream under the hood.
        assertDoesNotThrow(() -> bc.uploadWithResponse(input, 20 * Constants.MB, pto, null, null, null, null, null,
            null));
    }

    @Test
    public void uploadIncorrectSize() {
        int[] dataSizes = new int[]{DATA.getDefaultDataSize() + 1, DATA.getDefaultDataSize() - 1};
        for (int dataSize : dataSizes) {
            assertThrows(IllegalStateException.class, () -> bc.upload(DATA.getDefaultInputStream(), dataSize, true));
        }
    }

    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    @ParameterizedTest
    @MethodSource("uploadNumBlocksSupplier")
    public void uploadNumBlocks(int size, Long maxUploadSize, long numBlocks) {
        byte[] randomData = getRandomByteArray(size);
        ByteArrayInputStream input = new ByteArrayInputStream(randomData);

        ParallelTransferOptions pto = new ParallelTransferOptions().setBlockSizeLong(maxUploadSize)
            .setMaxSingleUploadSizeLong(maxUploadSize);

        bc.uploadWithResponse(input, size, pto, null, null, null, null, null, null);

        List<Block> blocksUploaded = bc.getBlockBlobClient().listBlocks(BlockListType.ALL).getCommittedBlocks();
        assertEquals(blocksUploaded.size(), numBlocks);
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
        assertNotNull(bc.uploadWithResponse(new BlobParallelUploadOptions(DATA.getDefaultInputStream()), null, null)
            .getValue().getETag());
    }

    @Test
    public void uploadReturnValueBinaryData() {
        assertNotNull(bc.uploadWithResponse(new BlobParallelUploadOptions(DATA.getDefaultBinaryData()), null, null)
            .getValue().getETag());
    }

    @Test
    public void uploadInputStreamMin() {
        assertDoesNotThrow(() -> bc.upload(DATA.getDefaultInputStream()));
        TestUtils.assertArraysEqual(bc.downloadContent().toBytes(), DATA.getDefaultBytes());
    }

    @Test
    public void uploadInputStreamNoLengthOverwrite() {
        byte[] randomData = getRandomByteArray(Constants.KB);
        ByteArrayInputStream input = new ByteArrayInputStream(randomData);

        bc.upload(input, true);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bc.downloadWithResponse(stream, null, null, null, false, null, null);
        TestUtils.assertArraysEqual(stream.toByteArray(), randomData);
    }

    @Test
    public void uploadInputStreamNoLength() {
        assertDoesNotThrow(() -> bc.uploadWithResponse(new BlobParallelUploadOptions(DATA.getDefaultInputStream()),
            null, null));
        TestUtils.assertArraysEqual(bc.downloadContent().toBytes(), DATA.getDefaultBytes());
    }

    @Test
    public void uploadInputStreamBadLength() {
        long[] badLengths = {0, -100, DATA.getDefaultDataSize() - 1, DATA.getDefaultDataSize() + 1};
        for (long length : badLengths) {
            assertThrows(Exception.class, () ->
                bc.uploadWithResponse(new BlobParallelUploadOptions(DATA.getDefaultInputStream(), length), null, null));
        }
    }

    @Test
    public void uploadSuccessfulRetry() {
        BlobClient clientWithFailure = getBlobClient(ENVIRONMENT.getPrimaryAccount().getCredential(), bc.getBlobUrl(),
            new TransientFailureInjectingHttpPipelinePolicy());

        assertNotNull(clientWithFailure);
        clientWithFailure.uploadWithResponse(new BlobParallelUploadOptions(DATA.getDefaultInputStream()), null, null);
        TestUtils.assertArraysEqual(bc.downloadContent().toBytes(), DATA.getDefaultBytes());
    }

    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    @Test
    // Reading from recordings will not allow for the timing of the test to work correctly.
    public void uploadTimeout() {
        int size = 1024;
        byte[] randomData = getRandomByteArray(size);
        ByteArrayInputStream input = new ByteArrayInputStream(randomData);

        assertThrows(IllegalStateException.class, () -> bc.uploadWithResponse(input, size, null,
            null, null, null, null, Duration.ofNanos(5L), null));
    }

    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    @Test
    public void uploadFailWithSmallTimeoutsForServiceClient() {
        // setting very small timeout values for the service client
        liveTestScenarioWithRetry(() -> {
            HttpClientOptions clientOptions = new HttpClientOptions()
                .setApplicationId("client-options-id")
                .setResponseTimeout(Duration.ofNanos(1))
                .setReadTimeout(Duration.ofNanos(1))
                .setWriteTimeout(Duration.ofNanos(1))
                .setConnectTimeout(Duration.ofNanos(1));

            BlobServiceClientBuilder clientBuilder = new BlobServiceClientBuilder()
                .endpoint(ENVIRONMENT.getPrimaryAccount().getBlobEndpoint())
                .credential(ENVIRONMENT.getPrimaryAccount().getCredential())
                .retryOptions(new RequestRetryOptions(null, 1, (Integer) null, null, null, null))
                .clientOptions(clientOptions);

            BlobServiceClient serviceClient = clientBuilder.buildClient();

            int size = 1024;
            byte[] randomData = getRandomByteArray(size);
            ByteArrayInputStream input = new ByteArrayInputStream(randomData);

            BlobContainerClient blobContainer = serviceClient.createBlobContainer(generateContainerName());
            BlobClient blobClient = blobContainer.getBlobClient(generateBlobName());
            // test whether failure occurs due to small timeout intervals set on the service client
            assertThrows(RuntimeException.class, () -> blobClient.uploadWithResponse(input, size, null, null, null,
                null, null, Duration.ofSeconds(10), null));
        });

    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20211202ServiceVersion")
    @Test
    public void uploadStreamAccessTierCold() {
        byte[] randomData = getRandomByteArray(Constants.KB);
        ByteArrayInputStream input = new ByteArrayInputStream(randomData);
        BlobParallelUploadOptions blobUploadOptions = new BlobParallelUploadOptions(input).setTier(AccessTier.COLD);

        bc.uploadWithResponse(blobUploadOptions, null, null);
        BlobProperties properties = bc.getProperties();
        assertEquals(AccessTier.COLD, properties.getAccessTier());
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @Test
    public void downloadAllNull() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bc.setTags(Collections.singletonMap("foo", "bar"));
        BlobDownloadResponse response = bc.downloadStreamWithResponse(stream, null, null,
            null, false, null, null);
        ByteBuffer body = ByteBuffer.wrap(stream.toByteArray());
        BlobDownloadHeaders headers = response.getDeserializedHeaders();

        assertEquals(DATA.getDefaultData(), body);
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
//        headers.getLastAccessedTime() /* TODO (gapra): re-enable when last access time enabled. */
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @Test
    public void downloadAllNullStreaming() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bc.setTags(Collections.singletonMap("foo", "bar"));
        BlobDownloadResponse response = bc.downloadStreamWithResponse(stream, null, null,
            null, false, null, null);
        ByteBuffer body = ByteBuffer.wrap(stream.toByteArray());
        BlobDownloadHeaders headers = response.getDeserializedHeaders();

        assertEquals(DATA.getDefaultData(), body);
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
//        headers.getLastAccessedTime() /* TODO (gapra): re-enable when last access time enabled. */
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @Test
    public void downloadAllNullBinaryData() {
        bc.setTags(Collections.singletonMap("foo", "bar"));
        BlobDownloadContentResponse response = bc.downloadContentWithResponse(null, null, null, null);
        BinaryData body = response.getValue();
        BlobDownloadHeaders headers = response.getDeserializedHeaders();

        TestUtils.assertArraysEqual(DATA.getDefaultBytes(), body.toBytes());
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
//        headers.getLastAccessedTime() /* TODO (gapra): re-enable when last access time enabled. */
    }

    @Test
    public void downloadEmptyFile() {
        AppendBlobClient bc = cc.getBlobClient("emptyAppendBlob").getAppendBlobClient();
        bc.create();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        bc.downloadStream(outStream);
        byte[] result = outStream.toByteArray();
        assertEquals(0, result.length);
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
        BlobClient bu2 = getBlobClient(ENVIRONMENT.getPrimaryAccount().getCredential(), bc.getBlobUrl(),
            new MockRetryRangeResponsePolicy("bytes=2-6"));

        BlobRange range = new BlobRange(2, 5L);
        DownloadRetryOptions options = new DownloadRetryOptions().setMaxRetryRequests(3);
        RuntimeException e = assertThrows(RuntimeException.class, () ->
            bu2.downloadStreamWithResponse(new ByteArrayOutputStream(), range, options, null,
                false, null, null));

        /*
        Because the dummy Flux always throws an error. This will also validate that an IllegalArgumentException is
        NOT thrown because the types would not match.
         */
        assertInstanceOf(IOException.class, e.getCause());
    }

    @Test
    public void downloadMin() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        bc.downloadStream(outStream);
        byte[] result = outStream.toByteArray();
        TestUtils.assertArraysEqual(DATA.getDefaultBytes(), result);
    }

    @Test
    public void downloadStreamingMin() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        bc.downloadStream(outStream);
        byte[] result = outStream.toByteArray();
        TestUtils.assertArraysEqual(DATA.getDefaultBytes(), result);
    }

    @Test
    public void downloadBinaryDataMin() {
        BinaryData result = bc.downloadContent();
        TestUtils.assertArraysEqual(DATA.getDefaultBytes(), result.toBytes());
    }

    @ParameterizedTest
    @MethodSource("downloadRangeSupplier")
    public void downloadRange(long offset, Long count, String expectedData) {
        BlobRange range = (count == null) ? new BlobRange(offset) : new BlobRange(offset, count);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        bc.downloadStreamWithResponse(outStream, range, null, null, false, null, null);
        String bodyStr = outStream.toString();
        assertEquals(expectedData, bodyStr);
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
        bc.setTags(t);
        match = setupBlobMatchCondition(bc, match);
        leaseID = setupBlobLeaseCondition(bc, leaseID);
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        BlobDownloadResponse response = bc.downloadStreamWithResponse(new ByteArrayOutputStream(), null, null, bac,
            false, null, null);
        assertResponseStatusCode(response, 200);
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void downloadACStreaming(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t);
        match = setupBlobMatchCondition(bc, match);
        leaseID = setupBlobLeaseCondition(bc, leaseID);
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        BlobDownloadResponse response = bc.downloadStreamWithResponse(new ByteArrayOutputStream(), null,
            null, bac, false, null, null);
        assertResponseStatusCode(response, 200);
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void downloadACBinaryData(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t);
        match = setupBlobMatchCondition(bc, match);
        leaseID = setupBlobLeaseCondition(bc, leaseID);
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        BlobDownloadContentResponse response = bc.downloadContentWithResponse(null, bac, null, null);
        assertResponseStatusCode(response, 200);
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

        assertThrows(BlobStorageException.class, () -> bc.downloadWithResponse(new ByteArrayOutputStream(), null,
            null, bac, false, null, null));
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

        assertThrows(BlobStorageException.class, () -> bc.downloadStreamWithResponse(new ByteArrayOutputStream(),
            null, null, bac, false, null, null));
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
        assertThrows(BlobStorageException.class, () -> bc.downloadContentWithResponse(null, bac, null, null));
    }

    @Test
    public void downloadMd5() throws NoSuchAlgorithmException {
        BlobDownloadResponse response = bc.downloadStreamWithResponse(new ByteArrayOutputStream(), new BlobRange(0, 3L),
            null, null, true, null, null);
        byte[] contentMD5 = response.getDeserializedHeaders().getContentMd5();
        TestUtils.assertArraysEqual(MessageDigest.getInstance("MD5").digest(DATA.getDefaultText().substring(0, 3).getBytes()),
            contentMD5);
    }

    @Test
    public void downloadRetryDefault() {
        BlobClient failureBlobClient = getBlobClient(ENVIRONMENT.getPrimaryAccount().getCredential(), bc.getBlobUrl(),
            new MockFailureResponsePolicy(5));

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        failureBlobClient.downloadStream(outStream);
        String bodyStr = outStream.toString();
        assertEquals(DATA.getDefaultText(), bodyStr);
    }

    @Test
    public void downloadError() {
        bc = cc.getBlobClient(generateBlobName());
        assertThrows(NullPointerException.class, () -> bc.downloadStream(null));
    }

    @Test
    public void downloadSnapshot() {
        ByteArrayOutputStream originalStream = new ByteArrayOutputStream();
        bc.downloadStream(originalStream);

        BlobClientBase bc2 = bc.createSnapshot();
        new SpecializedBlobClientBuilder()
            .blobClient(bc)
            .buildBlockBlobClient()
            .upload(new ByteArrayInputStream("ABC".getBytes()), 3, true);

        ByteArrayOutputStream snapshotStream = new ByteArrayOutputStream();
        bc2.downloadStream(snapshotStream);
        TestUtils.assertArraysEqual(originalStream.toByteArray(), snapshotStream.toByteArray());
    }

    @Test
    public void downloadSnapshotBinaryData() {
        BinaryData originalContent = bc.downloadContent();

        BlobClientBase bc2 = bc.createSnapshot();
        new SpecializedBlobClientBuilder()
            .blobClient(bc)
            .buildBlockBlobClient()
            .upload(new ByteArrayInputStream("ABC".getBytes()), 3, true);

        BinaryData snapshotContent = bc2.downloadContent();
        TestUtils.assertArraysEqual(originalContent.toBytes(), snapshotContent.toBytes());
    }

    @Test
    public void downloadToFileExists() throws IOException {
        File testFile = new File(generateResourceName(entityNo) + ".txt");
        if (!testFile.exists()) {
            assertTrue(testFile.createNewFile());
        }

        // Default Overwrite is false so this should fail
        UncheckedIOException e = assertThrows(UncheckedIOException.class, () -> bc.downloadToFile(testFile.getPath()));
        assertInstanceOf(FileAlreadyExistsException.class, e.getCause());
        // cleanup:
        testFile.delete();
    }

    @Test
    public void downloadToFileExistsSucceeds() throws IOException {
        File testFile = new File(generateResourceName(entityNo) + ".txt");
        if (!testFile.exists()) {
            assertTrue(testFile.createNewFile());
        }

        bc.downloadToFile(testFile.getPath(), true);
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

        bc.downloadToFile(testFile.getPath());
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
            null, false, openOptions, null, null);

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
            null, false, openOptions, null, null);

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

        bc.uploadFromFile(file.toPath().toString(), true);

        File outFile = new File(prefix + ".txt");
        createdFiles.add(outFile);
        outFile.deleteOnExit();
        Files.deleteIfExists(outFile.toPath());

        BlobProperties properties = bc.downloadToFileWithResponse(outFile.toPath().toString(), null,
            new ParallelTransferOptions().setBlockSizeLong(4L * 1024 * 1024), null, null, false, null, null).getValue();

        assertTrue(compareFiles(file, outFile, 0, fileSize));
        assertEquals(properties.getBlobType(), BlobType.BLOCK_BLOB);
        assertNotNull(properties.getCreationTime());
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
    public void downloadFileSyncBufferCopy(int fileSize) throws IOException {
        String containerName = generateContainerName();
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
            .endpoint(ENVIRONMENT.getPrimaryAccount().getBlobEndpoint())
            .credential(ENVIRONMENT.getPrimaryAccount().getCredential())
            .buildClient();

        BlobClient blobClient = blobServiceClient.createBlobContainer(containerName).getBlobClient(generateBlobName());


        File file = getRandomFile(fileSize);
        file.deleteOnExit();
        createdFiles.add(file);

        blobClient.uploadFromFile(file.toPath().toString(), true);
        File outFile = new File(prefix + ".txt");
        createdFiles.add(outFile);
        Files.deleteIfExists(outFile.toPath());

        BlobProperties properties = blobClient.downloadToFileWithResponse(outFile.toPath().toString(), null,
            new ParallelTransferOptions().setBlockSizeLong(4L * 1024 * 1024), null, null, false, null, null).getValue();

        assertTrue(compareFiles(file, outFile, 0, fileSize));
        assertEquals(properties.getBlobType(), BlobType.BLOCK_BLOB);

        // cleanup:
        blobServiceClient.deleteBlobContainer(containerName);
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

        bc.uploadFromFile(file.toPath().toString(), true);
        File outFile = new File(generateBlobName());
        createdFiles.add(outFile);
        Files.deleteIfExists(outFile.toPath());

        bc.downloadToFileWithResponse(outFile.toPath().toString(), range, null, null, null, false, null, null);

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

        bc.uploadFromFile(file.toPath().toString(), true);
        File outFile = new File(prefix);
        createdFiles.add(outFile);
        Files.deleteIfExists(outFile.toPath());

        assertThrows(BlobStorageException.class, () -> bc.downloadToFileWithResponse(outFile.toPath().toString(),
            new BlobRange(DATA.getDefaultDataSize() + 1), null, null, null, false, null, null));

    }

    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    @Test
    public void downloadFileCountNull() throws IOException {
        File file = getRandomFile(DATA.getDefaultDataSize());
        file.deleteOnExit();
        createdFiles.add(file);

        bc.uploadFromFile(file.toPath().toString(), true);
        File outFile = new File(generateBlobName());
        createdFiles.add(outFile);
        Files.deleteIfExists(outFile.toPath());

        bc.downloadToFileWithResponse(outFile.toPath().toString(), new BlobRange(0), null, null, null, false, null,
            null);

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

        bc.uploadFromFile(file.toPath().toString(), true);
        File outFile = new File(prefix);
        createdFiles.add(outFile);
        Files.deleteIfExists(outFile.toPath());

        match = setupBlobMatchCondition(bc, match);
        leaseID = setupBlobLeaseCondition(bc, leaseID);
        BlobRequestConditions bro = new BlobRequestConditions().setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified).setIfMatch(match).setIfNoneMatch(noneMatch)
            .setLeaseId(leaseID);

        assertDoesNotThrow(() -> bc.downloadToFileWithResponse(outFile.toPath().toString(), null,
            null, null, bro, false, null, null));
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

        bc.uploadFromFile(file.toPath().toString(), true);
        File outFile = new File(prefix);
        createdFiles.add(outFile);
        Files.deleteIfExists(outFile.toPath());

        noneMatch = setupBlobMatchCondition(bc, noneMatch);
        setupBlobLeaseCondition(bc, leaseID);
        BlobRequestConditions bro = new BlobRequestConditions().setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified).setIfMatch(match).setIfNoneMatch(noneMatch)
            .setLeaseId(leaseID);

        BlobStorageException e = assertThrows(BlobStorageException.class, () ->
                bc.downloadToFileWithResponse(outFile.toPath().toString(), null, null,
                    null, bro, false, null, null));
        assertTrue(e.getErrorCode() == BlobErrorCode.CONDITION_NOT_MET
            || e.getErrorCode() == BlobErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION);
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

        bc.uploadFromFile(file.toPath().toString(), true);
        File outFile = new File(prefix);
        outFile.deleteOnExit();
        createdFiles.add(outFile);
        Files.deleteIfExists(outFile.toPath());

        MockReceiver mockReceiver = new MockReceiver();

        bc.downloadToFileWithResponse(outFile.toPath().toString(), null,
            new ParallelTransferOptions().setProgressReceiver(mockReceiver),
            new DownloadRetryOptions().setMaxRetryRequests(3), null, false, null, null);

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

        bc.uploadFromFile(file.toPath().toString(), true);

        File outFile = new File(prefix);
        outFile.deleteOnExit();
        createdFiles.add(outFile);
        Files.deleteIfExists(outFile.toPath());

        MockProgressListener mockListener = new MockProgressListener();

        bc.downloadToFileWithResponse(outFile.toPath().toString(), null,
            new ParallelTransferOptions().setProgressListener(mockListener),
            new DownloadRetryOptions().setMaxRetryRequests(3), null, false, null, null);

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

        bc.uploadFromFile(file.toPath().toString(), true);
        File outFile = new File(prefix);
        createdFiles.add(outFile);
        Files.deleteIfExists(outFile.toPath());

        assertDoesNotThrow(() -> bc.downloadToFileWithResponse(
            new BlobDownloadToFileOptions(outFile.toPath().toString())
                .setParallelTransferOptions(new com.azure.storage.common.ParallelTransferOptions()
                    .setBlockSizeLong(downloadBlockSize)).setDownloadRetryOptions(new DownloadRetryOptions()
                    .setMaxRetryRequests(3)), null, null));
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
        bc.setTags(Collections.singletonMap("foo", "bar"));
        Response<BlobProperties> response = bc.getPropertiesWithResponse(null, null, null);
        HttpHeaders headers = response.getHeaders();
        BlobProperties properties = response.getValue();

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
//        properties.getLastAccessedTime() /* TODO: re-enable when last access time enabled. */
    }

    @Test
    public void getPropertiesMin() {
        assertResponseStatusCode(bc.getPropertiesWithResponse(null, null, null), 200);
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void getPropertiesAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t);
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(setupBlobLeaseCondition(bc, leaseID))
            .setIfMatch(setupBlobMatchCondition(bc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        assertResponseStatusCode(bc.getPropertiesWithResponse(bac, null, null), 200);
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

        assertThrows(BlobStorageException.class, () -> bc.getPropertiesWithResponse(bac, null, null));
    }

    /*
    This test requires two accounts that are configured in a very specific way. It is not feasible to setup that
    relationship programmatically, so we have recorded a successful interaction and only test recordings.
     */

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
        bc = cc.getBlobClient(generateBlobName());
        BlobStorageException ex = assertThrows(BlobStorageException.class, () -> bc.getProperties());
        assertTrue(ex.getMessage().contains("BlobNotFound"));
    }

    @Test
    public void setHTTPHeadersNull() {
        Response<Void> response = bc.setHttpHeadersWithResponse(null, null, null, null);

        assertResponseStatusCode(response, 200);
        assertTrue(validateBasicHeaders(response.getHeaders()));
    }

    @Test
    public void setHTTPHeadersMin() throws NoSuchAlgorithmException {
        BlobProperties properties = bc.getProperties();
        BlobHttpHeaders headers = new BlobHttpHeaders()
            .setContentEncoding(properties.getContentEncoding())
            .setContentDisposition(properties.getContentDisposition())
            .setContentType("type")
            .setCacheControl(properties.getCacheControl())
            .setContentLanguage(properties.getContentLanguage())
            .setContentMd5(Base64.getEncoder().encode(MessageDigest.getInstance("MD5")
                .digest(DATA.getDefaultBytes())));

        bc.setHttpHeaders(headers);
        assertEquals("type", bc.getProperties().getContentType());
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

        bc.setHttpHeaders(putHeaders);

        assertTrue(validateBlobProperties(
            bc.getPropertiesWithResponse(null, null, null),
            cacheControl, contentDisposition, contentEncoding, contentLanguage, contentMD5, contentType));
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
        bc.setTags(t);
        match = setupBlobMatchCondition(bc, match);
        leaseID = setupBlobLeaseCondition(bc, leaseID);
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        assertResponseStatusCode(bc.setHttpHeadersWithResponse(null, bac, null, null), 200);
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

        assertThrows(BlobStorageException.class, () -> bc.setHttpHeadersWithResponse(null, bac, null, null));
    }

    @Test
    public void setHTTPHeadersError() {
        bc = cc.getBlobClient(generateBlobName());
        assertThrows(BlobStorageException.class, () -> bc.setHttpHeaders(null));
    }

    @Test
    public void setMetadataAllNull() {
        Response<Void> response = bc.setMetadataWithResponse(null, null, null, null);
        assertEquals(0, bc.getProperties().getMetadata().size());
        assertResponseStatusCode(response, 200);
        assertTrue(validateBasicHeaders(response.getHeaders()));
        assertTrue(Boolean.parseBoolean(response.getHeaders().getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
    }

    @Test
    public void setMetadataMin() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("foo", "bar");
        bc.setMetadata(metadata);
        assertEquals(metadata, bc.getProperties().getMetadata());
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

        assertResponseStatusCode(bc.setMetadataWithResponse(metadata, null, null, null), statusCode);
        assertEquals(metadata, bc.getProperties().getMetadata());
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
        bc.setTags(t);
        match = setupBlobMatchCondition(bc, match);
        leaseID = setupBlobLeaseCondition(bc, leaseID);
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertResponseStatusCode(bc.setMetadataWithResponse(null, bac, null, null), 200);
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

        assertThrows(BlobStorageException.class, () -> bc.setMetadataWithResponse(null, bac, null, null));
    }

    @ParameterizedTest
    @MethodSource("setMetadataWhitespaceErrorSupplier")
    public void setMetadataWhitespaceError(String key, String value) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put(key, value);

        assertThrows(Exception.class, () -> bc.setMetadata(metadata));
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
        bc = cc.getBlobClient(generateBlobName());
        assertThrows(BlobStorageException.class, () -> bc.setMetadata(null));
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @Test
    public void setTagsAllNull() {
        Response<Void> response = bc.setTagsWithResponse(new BlobSetTagsOptions(new HashMap<>()),
            null, null);
        assertEquals(0, bc.getTags().size());
        assertResponseStatusCode(response, 204);
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @Test
    public void setTagsMin() {
        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "bar");
        bc.setTags(tags);
        assertEquals(tags, bc.getTags());
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

        assertResponseStatusCode(bc.setTagsWithResponse(new BlobSetTagsOptions(tags), null, null), statusCode);
        assertEquals(tags, bc.getTags());
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
        bc.setTags(t);
        t = new HashMap<>();
        t.put("fizz", "buzz");

        assertResponseStatusCode(bc.setTagsWithResponse(new BlobSetTagsOptions(t).setRequestConditions(
            new BlobRequestConditions().setTagsConditions(tags)), null, null), 204);
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

        assertThrows(BlobStorageException.class, () -> bc.setTagsWithResponse(new BlobSetTagsOptions(t)
                .setRequestConditions(new BlobRequestConditions().setTagsConditions(tags)), null, null));
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @Test
    public void getTagsAC() {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t);
        String[] tagsList = new String[]{null, "\"foo\" = 'bar'"};
        for (String tags : tagsList) {
            assertResponseStatusCode(bc.getTagsWithResponse(new BlobGetTagsOptions().setRequestConditions(
                new BlobRequestConditions().setTagsConditions(tags)), null, null), 200);
        }
    }

    @Test
    public void getTagsACFail() {
        Map<String, String> t = new HashMap<>();
        t.put("fizz", "buzz");
        String tags = "\"foo\" = 'bar'";

        assertThrows(BlobStorageException.class, () -> bc.getTagsWithResponse(new BlobGetTagsOptions()
            .setRequestConditions(new BlobRequestConditions().setTagsConditions(tags)), null, null));
    }

    @Test
    public void setTagsError() {
        bc = cc.getBlobClient(generateBlobName());
        assertThrows(BlobStorageException.class, () -> bc.setTags(new HashMap<>()));
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @Test
    public void setTagsLease() {
        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "bar");
        String leaseID = setupBlobLeaseCondition(bc, RECEIVED_LEASE_ID);
        BlobRequestConditions bac = new BlobRequestConditions().setLeaseId(leaseID);

        Response<Void> response = bc.setTagsWithResponse(new BlobSetTagsOptions(tags).setRequestConditions(bac),
            null, null);

        assertResponseStatusCode(response, 204);
        assertEquals(tags, bc.getTags());
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @Test
    public void getTagsLease() {
        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "bar");
        String leaseID = setupBlobLeaseCondition(bc, RECEIVED_LEASE_ID);
        BlobRequestConditions bac = new BlobRequestConditions().setLeaseId(leaseID);
        bc.setTagsWithResponse(new BlobSetTagsOptions(tags).setRequestConditions(bac), null, null);

        Response<Map<String, String>> response = bc.getTagsWithResponse(new BlobGetTagsOptions()
            .setRequestConditions(bac), null, null);

        assertResponseStatusCode(response, 200);
        assertEquals(tags, response.getValue());
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    public void setTagsLeaseFail() {
        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "bar");
        BlobRequestConditions bac = new BlobRequestConditions().setLeaseId(GARBAGE_LEASE_ID);
        BlobStorageException e = assertThrows(BlobStorageException.class, () -> bc.setTagsWithResponse(
            new BlobSetTagsOptions(tags).setRequestConditions(bac), null, null));
        assertEquals(412, e.getStatusCode());
    }

    @Test
    public void snapshot() {
        Response<BlobClientBase> response = bc.createSnapshotWithResponse(null, null, null, null);
        assertTrue(response.getValue().exists());
        assertTrue(validateBasicHeaders(response.getHeaders()));
    }

    @Test
    public void snapshotMin() {
        assertResponseStatusCode(bc.createSnapshotWithResponse(null, null, null, null), 201);
    }

    @Test
    public void getSnapshot() {
        byte[] data = "test".getBytes();
        String blobName = generateBlobName();
        BlockBlobClient bu = cc.getBlobClient(blobName).getBlockBlobClient();
        bu.upload(new ByteArrayInputStream(data), data.length);
        String snapshotId = bu.createSnapshot().getSnapshotId();

        BlockBlobClient snapshotBlob = cc.getBlobClient(blobName, snapshotId).getBlockBlobClient();
        assertEquals(snapshotId, snapshotBlob.getSnapshotId());
        assertNull(bu.getSnapshotId());
    }

    @Test
    public void isSnapshot() {
        byte[] data = "test".getBytes();
        String blobName = generateBlobName();
        BlockBlobClient bu = cc.getBlobClient(blobName).getBlockBlobClient();
        bu.upload(new ByteArrayInputStream(data), data.length);
        String snapshotId = bu.createSnapshot().getSnapshotId();

        BlockBlobClient snapshotBlob = cc.getBlobClient(blobName, snapshotId).getBlockBlobClient();
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

        Response<BlobClientBase> response = bc.createSnapshotWithResponse(metadata, null, null, null);
        BlobClientBase bcSnap = response.getValue();
        assertResponseStatusCode(response, 201);
        assertEquals(metadata, bcSnap.getProperties().getMetadata());
    }

    private static Stream<Arguments> snapshotMetadataSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null),
            Arguments.of("foo", "bar", "fizz", "buzz"));
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void snapshotAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t);
        match = setupBlobMatchCondition(bc, match);
        leaseID = setupBlobLeaseCondition(bc, leaseID);
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        assertResponseStatusCode(bc.createSnapshotWithResponse(null, bac, null, null), 201);
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

        assertThrows(BlobStorageException.class, () -> bc.createSnapshotWithResponse(null, bac, null, null));
    }

    @Test
    public void snapshotError() {
        bc = cc.getBlobClient(generateBlobName());
        assertThrows(BlobStorageException.class, () -> bc.createSnapshot());
    }

    @Test
    public void copy() {
        BlockBlobAsyncClient copyDestBlob = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        PollerFlux<BlobCopyInfo, Void> poller = copyDestBlob.beginCopy(bc.getBlobUrl(), getPollingDuration(1000));

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

        PollerFlux<BlobCopyInfo, Void> poller = copyDestBlob.beginCopy(bc.getBlobUrl(), getPollingDuration(1000));
        StepVerifier.create(poller.take(1)).assertNext(it -> {
            assertNotNull(it.getValue());
            assertNotNull(it.getValue().getCopyId());
            if (ENVIRONMENT.getTestMode() == TestMode.PLAYBACK) {
                assertEquals(redactUrl(bc.getBlobUrl()), it.getValue().getCopySourceUrl());
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

        PollerFlux<BlobCopyInfo, Void> poller = copyDestBlob.beginCopy(bc.getBlobUrl(), null, null,
            null, null, null, getPollingDuration(1000));

        AsyncPollResponse<BlobCopyInfo, Void> lastResponse = poller.doOnNext(it -> {
            assertNotNull(it.getValue());
            assertNotNull(it.getValue().getCopyId());
            if (ENVIRONMENT.getTestMode() == TestMode.PLAYBACK) {
                assertEquals(redactUrl(bc.getBlobUrl()), it.getValue().getCopySourceUrl());
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

        PollerFlux<BlobCopyInfo, Void> poller = bu2.beginCopy(bc.getBlobUrl(), metadata, null, null,
            null, null, getPollingDuration(1000));
        poller.blockLast();

        StepVerifier.create(bu2.getProperties()).assertNext(it -> assertEquals(metadata, it.getMetadata()))
            .verifyComplete();
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
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

        PollerFlux<BlobCopyInfo, Void> poller = bu2.beginCopy(new BlobBeginCopyOptions(bc.getBlobUrl()).setTags(tags)
            .setPollInterval(getPollingDuration(1000)));
        poller.blockLast();

        StepVerifier.create(bu2.getTags()).assertNext(it -> assertEquals(it, tags)).verifyComplete();
    }

    private static Stream<Arguments> copyTagsSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null),
            Arguments.of("foo", "bar", "fizz", "buzz"),
            Arguments.of(" +-./:=_  +-./:=_", " +-./:=_", null, null));
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @CsvSource({"true,true", "true,false", "false,true", "false,false"})
    public void copySeal(boolean source, boolean destination) {
        AppendBlobClient appendBlobClient = cc.getBlobClient(generateBlobName()).getAppendBlobClient();
        appendBlobClient.create();
        if (source) {
            appendBlobClient.seal();
        }

        AppendBlobAsyncClient bu2 = ccAsync.getBlobAsyncClient(generateBlobName()).getAppendBlobAsyncClient();

        PollerFlux<BlobCopyInfo, Void> poller = bu2.beginCopy(new BlobBeginCopyOptions(appendBlobClient.getBlobUrl())
            .setSealDestination(destination)
            .setPollInterval(getPollingDuration(1000)));
        poller.blockLast();

        StepVerifier.create(bu2.getProperties()).assertNext(it ->
            assertEquals(Boolean.TRUE.equals(it.isSealed()), destination)).verifyComplete();

    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("copySourceACSupplier")
    public void copySourceAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t);
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

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
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

        PollerFlux<BlobCopyInfo, Void> poller = bu2.beginCopy(bc.getBlobUrl(), null, null, null, null, bac,
            getPollingDuration(1000));
        AsyncPollResponse<BlobCopyInfo, Void> response = poller.blockLast();
        assertNotNull(response);
        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, response.getStatus());
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsFailSupplier")
    public void copyDestACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID, String tags) {
        BlockBlobClient bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        bu2.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        noneMatch = setupBlobMatchCondition(bu2, noneMatch);
        setupBlobLeaseCondition(bu2, leaseID);
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        assertThrows(BlobStorageException.class, () -> bu2.copyFromUrlWithResponse(bc.getBlobUrl(), null, null, null,
            bac, null, null));
    }

    @Test
    public void abortCopyLeaseFail() {
        // Data has to be large enough and copied between accounts to give us enough time to abort
        new SpecializedBlobClientBuilder()
            .blobClient(bc)
            .buildBlockBlobClient()
            .upload(new ByteArrayInputStream(getRandomByteArray(8 * 1024 * 1024)), 8 * 1024 * 1024, true);
        // So we don't have to create a SAS.
        cc.setAccessPolicy(PublicAccessType.BLOB, null);

        BlobContainerClient cu2 = alternateBlobServiceClient.getBlobContainerClient(generateBlobName());
        cu2.create();
        BlockBlobClient bu2 = cu2.getBlobClient(generateBlobName()).getBlockBlobClient();
        bu2.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());

        String leaseId = setupBlobLeaseCondition(bu2, RECEIVED_LEASE_ID);
        BlobRequestConditions blobRequestConditions = new BlobRequestConditions().setLeaseId(leaseId);

        SyncPoller<BlobCopyInfo, Void> poller = bu2.beginCopy(bc.getBlobUrl(), null, null, null,
            null, blobRequestConditions, getPollingDuration(500));
        PollResponse<BlobCopyInfo> response = poller.poll();
        assertNotEquals(LongRunningOperationStatus.FAILED, response.getStatus());
        BlobCopyInfo blobCopyInfo = response.getValue();


        BlobStorageException e = assertThrows(BlobStorageException.class, () ->
            bu2.abortCopyFromUrlWithResponse(blobCopyInfo.getCopyId(), GARBAGE_LEASE_ID, null, null));
        assertEquals(412, e.getStatusCode());

        // cleanup:
        cu2.delete();
    }

    @Test
    public void abortCopy() {
        // Data has to be large enough and copied between accounts to give us enough time to abort
        new SpecializedBlobClientBuilder()
            .blobClient(bc)
            .buildBlockBlobClient()
            .upload(new ByteArrayInputStream(getRandomByteArray(8 * 1024 * 1024)), 8 * 1024 * 1024, true);
        // So we don't have to create a SAS.
        cc.setAccessPolicy(PublicAccessType.BLOB, null);

        BlobContainerClient cu2 = alternateBlobServiceClient.getBlobContainerClient(generateBlobName());
        cu2.create();
        BlobClient bu2 = cu2.getBlobClient(generateBlobName());

        SyncPoller<BlobCopyInfo, Void> poller = bu2.beginCopy(bc.getBlobUrl(), null, null, null,
            null, null, getPollingDuration(1000));
        PollResponse<BlobCopyInfo> lastResponse = poller.poll();
        assertNotNull(lastResponse);
        assertNotNull(lastResponse.getValue());
        Response<Void> response = bu2.abortCopyFromUrlWithResponse(lastResponse.getValue().getCopyId(), null,
            null, null);
        HttpHeaders headers = response.getHeaders();
        assertResponseStatusCode(response, 204);
        assertNotNull(headers.getValue(X_MS_REQUEST_ID));
        assertNotNull(headers.getValue(X_MS_VERSION));
        assertNotNull(headers.getValue(HttpHeaderName.DATE));
        // cleanup:
        // Normal test cleanup will not clean up containers in the alternate account.
        assertResponseStatusCode(cu2.deleteWithResponse(null, null, null), 202);
    }

    @Test
    public void abortCopyLease() {
        // Data has to be large enough and copied between accounts to give us enough time to abort
        new SpecializedBlobClientBuilder().blobClient(bc)
            .buildBlockBlobClient()
            .upload(new ByteArrayInputStream(getRandomByteArray(8 * 1024 * 1024)), 8 * 1024 * 1024, true);
        // So we don't have to create a SAS.
        cc.setAccessPolicy(PublicAccessType.BLOB, null);

        BlobContainerClient cu2 = alternateBlobServiceClient.getBlobContainerClient(generateContainerName());
        cu2.create();
        BlockBlobClient bu2 = cu2.getBlobClient(generateBlobName()).getBlockBlobClient();
        bu2.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        String leaseId = setupBlobLeaseCondition(bu2, RECEIVED_LEASE_ID);
        BlobRequestConditions blobAccess = new BlobRequestConditions().setLeaseId(leaseId);

        SyncPoller<BlobCopyInfo, Void> poller = bu2.beginCopy(bc.getBlobUrl(), null, null, null,
            null, blobAccess, getPollingDuration(1000));
        PollResponse<BlobCopyInfo> lastResponse = poller.poll();

        assertNotNull(lastResponse);
        assertNotNull(lastResponse.getValue());
        String copyId = lastResponse.getValue().getCopyId();
        assertResponseStatusCode(bu2.abortCopyFromUrlWithResponse(copyId, leaseId, null, null), 204);
        // cleanup:
        // Normal test cleanup will not clean up containers in the alternate account.
        cu2.delete();
    }

    @Test
    public void copyError() {
        bc = cc.getBlobClient(generateBlobName());
        assertThrows(BlobStorageException.class, () -> bc.copyFromUrl("http://www.error.com"));
    }

    @Test
    public void abortCopyError() {
        bc = cc.getBlobClient(generateBlobName());
        assertThrows(BlobStorageException.class, () -> bc.abortCopyFromUrl("id"));
    }

    @Test
    public void syncCopy() {
        // Sync copy is a deep copy, which requires either sas or public access.
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null);
        BlockBlobClient bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient();

        HttpHeaders headers = bu2.copyFromUrlWithResponse(bc.getBlobUrl(), null, null, null, null, null, null)
            .getHeaders();

        assertEquals(SyncCopyStatusType.SUCCESS.toString(), headers.getValue(X_MS_COPY_STATUS));
        assertNotNull(headers.getValue(X_MS_COPY_ID));
        assertTrue(validateBasicHeaders(headers));
    }

    @Test
    public void syncCopyMin() {
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null);
        BlockBlobClient bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        assertResponseStatusCode(bu2.copyFromUrlWithResponse(bc.getBlobUrl(), null, null, null, null, null, null), 202);
    }

    @ParameterizedTest
    @MethodSource("snapshotMetadataSupplier")
    public void syncCopyMetadata(String key1, String value1, String key2, String value2) {
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null);
        BlockBlobClient bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2);
        }

        bu2.copyFromUrlWithResponse(bc.getBlobUrl(), metadata, null, null, null, null, null);
        assertEquals(metadata, bu2.getProperties().getMetadata());
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("copyTagsSupplier")
    public void syncCopyTags(String key1, String value1, String key2, String value2) {
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null);
        BlockBlobClient bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        Map<String, String> tags = new HashMap<>();
        if (key1 != null && value1 != null) {
            tags.put(key1, value1);
        }
        if (key2 != null && value2 != null) {
            tags.put(key2, value2);
        }

        bu2.copyFromUrlWithResponse(new BlobCopyFromUrlOptions(bc.getBlobUrl()).setTags(tags), null, null);
        assertEquals(tags, bu2.getTags());
    }

    @ParameterizedTest
    @MethodSource("syncCopySourceACSupplier")
    public void syncCopySourceAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch) {
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null);
        BlockBlobClient bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        match = setupBlobMatchCondition(bc, match);
        RequestConditions mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch);

        assertResponseStatusCode(bu2.copyFromUrlWithResponse(bc.getBlobUrl(), null, null, mac, null, null, null), 202);
    }

    private static Stream<Arguments> syncCopySourceACSupplier() {
        return Stream.of(Arguments.of(null, null, null, null),
            Arguments.of(OLD_DATE, null, null, null),
            Arguments.of(null, NEW_DATE, null, null),
            Arguments.of(null, null, RECEIVED_ETAG, null),
            Arguments.of(null, null, null, GARBAGE_ETAG));
    }

    @ParameterizedTest
    @MethodSource("syncCopySourceACFailSupplier")
    public void syncCopySourceACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
        String noneMatch) {
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null);
        BlockBlobClient bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        noneMatch = setupBlobMatchCondition(bc, noneMatch);
        RequestConditions mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch);

        assertThrows(BlobStorageException.class, () -> bu2.copyFromUrlWithResponse(bc.getBlobUrl(), null, null, mac,
            null, null, null));
    }

    private static Stream<Arguments> syncCopySourceACFailSupplier() {
        return Stream.of(Arguments.of(NEW_DATE, null, null, null),
            Arguments.of(null, OLD_DATE, null, null),
            Arguments.of(null, null, GARBAGE_ETAG, null),
            Arguments.of(null, null, null, RECEIVED_ETAG));
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void syncCopyDestAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID, String tags) {
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null);
        BlockBlobClient bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        bu2.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bu2.setTags(t);
        match = setupBlobMatchCondition(bu2, match);
        leaseID = setupBlobLeaseCondition(bu2, leaseID);
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);
        assertResponseStatusCode(bu2.copyFromUrlWithResponse(bc.getBlobUrl(), null, null, null, bac, null, null), 202);
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsFailSupplier")
    public void syncCopyDestACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID, String tags) {
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null);
        BlockBlobClient bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        bu2.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        noneMatch = setupBlobMatchCondition(bu2, noneMatch);
        setupBlobLeaseCondition(bu2, leaseID);
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);
        assertThrows(BlobStorageException.class, () -> bu2.copyFromUrlWithResponse(bc.getBlobUrl(), null, null, null,
            bac, null, null));
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20210608ServiceVersion")
    @ParameterizedTest
    @MethodSource("syncCopySourceTagsSupplier")
    public void syncCopySourceTags(BlobCopySourceTagsMode mode) {
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null);
        Map<String, String> sourceTags = new HashMap<>();
        sourceTags.put("foo", "bar");
        Map<String, String> destTags = new HashMap<>();
        destTags.put("fizz", "buzz");
        bc.setTags(sourceTags);

        String sas = bc.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobSasPermission().setTagsPermission(true).setReadPermission(true)));

        BlobClient bc2 = cc.getBlobClient(generateBlobName());

        BlobCopyFromUrlOptions options = new BlobCopyFromUrlOptions(bc.getBlobUrl() + "?" + sas)
            .setCopySourceTagsMode(mode);
        if (BlobCopySourceTagsMode.REPLACE == mode) {
            options.setTags(destTags);
        }

        bc2.copyFromUrlWithResponse(options, null, null);
        Map<String, String> receivedTags = bc2.getTags();

        if (BlobCopySourceTagsMode.REPLACE == mode) {
            assertEquals(receivedTags, destTags);
        } else {
            assertEquals(receivedTags,  sourceTags);
        }
    }

    private static Stream<Arguments> syncCopySourceTagsSupplier() {
        return Stream.of(Arguments.of(BlobCopySourceTagsMode.COPY),
            Arguments.of(BlobCopySourceTagsMode.REPLACE));
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20211202ServiceVersion")
    @Test
    public void syncCopyFromUrlAccessTierCold() {
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null);
        BlockBlobClient bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        BlobCopyFromUrlOptions copyOptions = new BlobCopyFromUrlOptions(bc.getBlobUrl()).setTier(AccessTier.COLD);

        assertResponseStatusCode(bu2.copyFromUrlWithResponse(copyOptions, null, null), 202);
        assertEquals(bu2.getProperties().getAccessTier(), AccessTier.COLD);
    }

    @Test
    public void syncCopyError() {
        BlockBlobClient bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        assertThrows(BlobStorageException.class, () -> bu2.copyFromUrl(bc.getBlobUrl()));
    }

    @Test
    public void delete() {
        Response<Void> response = bc.deleteWithResponse(null, null, null, null);
        HttpHeaders headers = response.getHeaders();

        assertResponseStatusCode(response, 202);
        assertNotNull(headers.getValue(X_MS_REQUEST_ID));
        assertNotNull(headers.getValue(X_MS_VERSION));
        assertNotNull(headers.getValue(HttpHeaderName.DATE));
    }

    @Test
    public void deleteMin() {
        assertResponseStatusCode(bc.deleteWithResponse(null, null, null, null), 202);
    }

    @ParameterizedTest
    @MethodSource("deleteOptionsSupplier")
    public void deleteOptions(DeleteSnapshotsOptionType option, int blobsRemaining) {
        bc.createSnapshot();
        // Create an extra blob so the list isn't empty (null) when we delete base blob, too
        BlockBlobClient bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        bu2.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());

        bc.deleteWithResponse(option, null, null, null);
        assertEquals(cc.listBlobs().stream().count(), blobsRemaining);
    }

    private static Stream<Arguments> deleteOptionsSupplier() {
        return Stream.of(
            Arguments.of(DeleteSnapshotsOptionType.INCLUDE, 1),
            Arguments.of(DeleteSnapshotsOptionType.ONLY, 2));
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void deleteAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t);
        match = setupBlobMatchCondition(bc, match);
        leaseID = setupBlobLeaseCondition(bc, leaseID);
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        assertResponseStatusCode(bc.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, bac, null, null), 202);
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

        assertThrows(BlobStorageException.class, () -> bc.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, bac,
            null, null));
    }

    @Test
    public void blobDeleteError() {
        bc = cc.getBlobClient(generateBlobName());
        assertThrows(BlobStorageException.class, () -> bc.delete());
    }

    @Test
    public void deleteIfExistsContainer() {
        assertTrue(bc.deleteIfExists());
    }

    @Test
    public void deleteIfExists() {
        Response<Boolean> response = bc.deleteIfExistsWithResponse(null, null, null, null);
        HttpHeaders headers = response.getHeaders();

        assertTrue(response.getValue());
        assertResponseStatusCode(response, 202);
        assertNotNull(headers.getValue(X_MS_REQUEST_ID));
        assertNotNull(headers.getValue(X_MS_VERSION));
        assertNotNull(headers.getValue(HttpHeaderName.DATE));
    }

    @Test
    public void deleteIfExistsMin() {
        assertResponseStatusCode(bc.deleteIfExistsWithResponse(null, null,
            null, null), 202);
    }

    @Test
    public void deleteIfExistsBlobThatDoesNotExist() {
        bc = cc.getBlobClient(generateBlobName());
        Response<Boolean> response = bc.deleteIfExistsWithResponse(null, null, null, null);

        assertFalse(response.getValue());
        assertResponseStatusCode(response, 404);
    }

    @Test
    public void deleteIfExistsContainerThatWasAlreadyDeleted() {
        Response<Boolean> initialResponse = bc.deleteIfExistsWithResponse(null, null, null, null);
        Response<Boolean> secondResponse = bc.deleteIfExistsWithResponse(null, null, null, null);

        assertTrue(initialResponse.getValue());
        assertResponseStatusCode(initialResponse, 202);
        assertFalse(secondResponse.getValue());
        assertResponseStatusCode(secondResponse, 404);

    }

    @ParameterizedTest
    @MethodSource("deleteOptionsSupplier")
    public void deleteIfExistsOptions(DeleteSnapshotsOptionType option, int blobsRemaining) {
        bc.createSnapshot();
        // Create an extra blob so the list isn't empty (null) when we delete base blob, too
        BlockBlobClient bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        bu2.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());

        bc.deleteIfExistsWithResponse(option, null, null, null);
        assertEquals(cc.listBlobs().stream().count(), blobsRemaining);
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void deleteIfExistsAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t);
        match = setupBlobMatchCondition(bc, match);
        leaseID = setupBlobLeaseCondition(bc, leaseID);
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        assertResponseStatusCode(bc.deleteIfExistsWithResponse(DeleteSnapshotsOptionType.INCLUDE, bac, null, null),
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

        assertThrows(BlobStorageException.class, () -> bc.deleteIfExistsWithResponse(DeleteSnapshotsOptionType.INCLUDE,
            bac, null, null));
    }

    @Test
    public void setTierBlockBlob() {
        List<AccessTier> tiers = Arrays.asList(AccessTier.HOT, AccessTier.COOL, AccessTier.ARCHIVE);
        for (AccessTier tier : tiers) {
            BlobContainerClient cc = primaryBlobServiceClient.createBlobContainer(generateContainerName());
            BlockBlobClient bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
            bc.upload(DATA.getDefaultInputStream(), DATA.getDefaultData().remaining());

            Response<Void> initialResponse = bc.setAccessTierWithResponse(tier, null, null, null, null);
            HttpHeaders headers = initialResponse.getHeaders();

            assertTrue(initialResponse.getStatusCode() == 200 || initialResponse.getStatusCode() == 202);
            assertNotNull(headers.getValue(X_MS_VERSION));
            assertNotNull(headers.getValue(X_MS_REQUEST_ID));
            assertEquals(tier, bc.getProperties().getAccessTier());
            assertEquals(tier, cc.listBlobs().iterator().next().getProperties().getAccessTier());
        }
        // cleanup:
        cc.delete();
    }

    @ParameterizedTest
    @MethodSource("setTierPageBlobSupplier")
    public void setTierPageBlob(AccessTier tier) {
        BlobContainerClient cc = premiumBlobServiceClient.createBlobContainer(generateContainerName());

        PageBlobClient bc = cc.getBlobClient(generateBlobName()).getPageBlobClient();
        bc.create(512);

        bc.setAccessTier(tier);
        assertEquals(tier, bc.getProperties().getAccessTier());
        assertEquals(tier, cc.listBlobs().iterator().next().getProperties().getAccessTier());

        // cleanup:
        cc.delete();
    }

    private static Stream<Arguments> setTierPageBlobSupplier() {
        return Stream.of(Arguments.of(AccessTier.P4), Arguments.of(AccessTier.P6), Arguments.of(AccessTier.P10),
            Arguments.of(AccessTier.P20), Arguments.of(AccessTier.P30), Arguments.of(AccessTier.P40),
            Arguments.of(AccessTier.P50));
    }

    @Test
    public void setTierMin() {
        BlobContainerClient cc = primaryBlobServiceClient.createBlobContainer(generateContainerName());
        BlockBlobClient bu = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        bu.upload(DATA.getDefaultInputStream(), DATA.getDefaultData().remaining());

        int statusCode = bc.setAccessTierWithResponse(AccessTier.HOT, null, null, null, null).getStatusCode();
        assertTrue(statusCode == 200 || statusCode == 202);

        // cleanup:
        cc.delete();
    }

    @Test
    public void setTierInferred() {
        BlobContainerClient cc = primaryBlobServiceClient.createBlobContainer(generateBlobName());
        BlockBlobClient bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        bc.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());

        Boolean inferred1 = bc.getProperties().isAccessTierInferred();
        Boolean inferredList1 = cc.listBlobs().iterator().next().getProperties().isAccessTierInferred();

        bc.setAccessTier(AccessTier.HOT);

        Boolean inferred2 = bc.getProperties().isAccessTierInferred();
        Boolean inferredList2 = cc.listBlobs().iterator().next().getProperties().isAccessTierInferred();

        assertTrue(inferred1);
        assertTrue(inferredList1);
        assertNull(inferred2);
        assertNull(inferredList2);
    }

    @ParameterizedTest
    @MethodSource("setTierArchiveStatusSupplier")
    public void setTierArchiveStatus(AccessTier sourceTier, AccessTier destTier, ArchiveStatus status) {
        BlobContainerClient cc = primaryBlobServiceClient.createBlobContainer(generateBlobName());
        BlockBlobClient bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        bc.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());

        bc.setAccessTier(sourceTier);
        bc.setAccessTier(destTier);

        assertEquals(status, bc.getProperties().getArchiveStatus());
        assertEquals(status, cc.listBlobs().iterator().next().getProperties().getArchiveStatus());
    }

    private static Stream<Arguments> setTierArchiveStatusSupplier() {
        return Stream.of(
            Arguments.of(AccessTier.ARCHIVE, AccessTier.COOL, ArchiveStatus.REHYDRATE_PENDING_TO_COOL),
            Arguments.of(AccessTier.ARCHIVE, AccessTier.HOT, ArchiveStatus.REHYDRATE_PENDING_TO_HOT),
            Arguments.of(AccessTier.ARCHIVE, AccessTier.HOT, ArchiveStatus.REHYDRATE_PENDING_TO_HOT));
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20211202ServiceVersion")
    @Test
    public void setTierCold() {
        BlobContainerClient cc = primaryBlobServiceClient.createBlobContainer(generateContainerName());
        BlockBlobClient bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        bc.upload(DATA.getDefaultInputStream(), DATA.getDefaultData().remaining());

        Response<Void> initialResponse = bc.setAccessTierWithResponse(AccessTier.COLD, null, null, null, null);
        HttpHeaders headers = initialResponse.getHeaders();

        assertTrue(initialResponse.getStatusCode() == 200 || initialResponse.getStatusCode() == 202);
        assertNotNull(headers.getValue(X_MS_VERSION));
        assertNotNull(headers.getValue(X_MS_REQUEST_ID));
        assertEquals(AccessTier.COLD, bc.getProperties().getAccessTier());
        assertEquals(AccessTier.COLD, cc.listBlobs().iterator().next().getProperties().getAccessTier());
        // cleanup:
        cc.delete();
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20211202ServiceVersion")
    @Test
    public void setTierArchiveStatusRehydratePendingToCold() {
        BlobContainerClient cc = primaryBlobServiceClient.createBlobContainer(generateBlobName());
        BlockBlobClient bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        bc.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());

        bc.setAccessTier(AccessTier.ARCHIVE);
        bc.setAccessTier(AccessTier.COLD);

        assertEquals(bc.getProperties().getArchiveStatus(), ArchiveStatus.REHYDRATE_PENDING_TO_COLD);
        assertEquals(cc.listBlobs().iterator().next().getProperties().getArchiveStatus(),
            ArchiveStatus.REHYDRATE_PENDING_TO_COLD);
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("setTierRehydratePrioritySupplier")
    public void setTierRehydratePriority(RehydratePriority rehydratePriority) {
        if (rehydratePriority != null) {
            bc.setAccessTier(AccessTier.ARCHIVE);
            bc.setAccessTierWithResponse(new BlobSetAccessTierOptions(AccessTier.HOT).setPriority(rehydratePriority),
                null, null);
        }

        Response<BlobProperties> resp = bc.getPropertiesWithResponse(null, null, null);
        assertResponseStatusCode(resp, 200);
        assertEquals(resp.getValue().getRehydratePriority(), rehydratePriority);
    }

    private static Stream<Arguments> setTierRehydratePrioritySupplier() {
        return Stream.of(
            Arguments.of((RehydratePriority) null),
            Arguments.of(RehydratePriority.STANDARD),
            Arguments.of(RehydratePriority.HIGH));
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @Test
    public void setTierSnapshot() {
        BlobClientBase bc2 = bc.createSnapshot();

        bc2.setAccessTier(AccessTier.COOL);
        assertEquals(bc2.getProperties().getAccessTier(), AccessTier.COOL);
        assertNotEquals(bc.getProperties().getAccessTier(), AccessTier.COOL);
    }

    @Test
    public void setTierSnapshotError() {
        bc.createSnapshotWithResponse(null, null, null, null);
        String fakeVersion = "2020-04-17T20:37:16.5129130Z";
        BlobClient bc2 = bc.getSnapshotClient(fakeVersion);

        assertThrows(BlobStorageException.class, () -> bc2.setAccessTier(AccessTier.COOL));
    }

    @Test
    public void setTierError() {
        BlobContainerClient cc = primaryBlobServiceClient.createBlobContainer(generateContainerName());
        BlockBlobClient bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        bc.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());

        BlobStorageException e = assertThrows(BlobStorageException.class, () ->
            bc.setAccessTier(AccessTier.fromString("garbage")));
        assertEquals(e.getErrorCode(), BlobErrorCode.INVALID_HEADER_VALUE);

        // cleanup:
        cc.delete();
    }

    @Test
    public void setTierIllegalArgument() {
        assertThrows(NullPointerException.class, () -> bc.setAccessTier(null));
    }

    @Test
    public void setTierLease() {
        BlobContainerClient cc = primaryBlobServiceClient.createBlobContainer(generateContainerName());
        BlockBlobClient bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        bc.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        String leaseID = setupBlobLeaseCondition(bc, RECEIVED_LEASE_ID);

        assertDoesNotThrow(() -> bc.setAccessTierWithResponse(AccessTier.HOT, null, leaseID, null, null));

        // cleanup:
        cc.delete();
    }

    @Test
    public void setTierLeaseFail() {
        BlobContainerClient cc = primaryBlobServiceClient.createBlobContainer(generateContainerName());
        BlockBlobClient bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        bc.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());

        assertThrows(BlobStorageException.class, () -> bc.setAccessTierWithResponse(AccessTier.HOT, null,
            "garbage", null, null));
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @Test
    public void setTierTags() {
        BlobContainerClient cc = primaryBlobServiceClient.createBlobContainer(generateContainerName());
        BlockBlobClient bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        bc.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t);

        assertDoesNotThrow(() -> bc.setAccessTierWithResponse(new BlobSetAccessTierOptions(AccessTier.HOT)
            .setTagsConditions("\"foo\" = 'bar'"), null, null));
        // cleanup:
        cc.delete();
    }

    @Test
    public void setTierTagsFail() {
        BlobContainerClient cc = primaryBlobServiceClient.createBlobContainer(generateContainerName());
        BlockBlobClient bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        bc.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());

        assertThrows(BlobStorageException.class, () -> bc.setAccessTierWithResponse(
            new BlobSetAccessTierOptions(AccessTier.HOT).setTagsConditions("\"foo\" = 'bar'"), null, null));
    }

    @ParameterizedTest
    @MethodSource("copyWithTierSupplier")
    public void copyWithTier(AccessTier tier1, AccessTier tier2) {
        String blobName = generateBlobName();
        BlockBlobClient bc = cc.getBlobClient(blobName).getBlockBlobClient();
        bc.uploadWithResponse(DATA.getDefaultInputStream(), DATA.getDefaultDataSize(), null, null, tier1,
            null, null, null, null);
        BlockBlobClient bcCopy = cc.getBlobClient(generateBlobName()).getBlockBlobClient();

        String secondSas = cc.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusHours(1),
            new BlobSasPermission().setReadPermission(true)));
        bcCopy.copyFromUrlWithResponse(bc.getBlobUrl() + "?" + secondSas, null, tier2, null, null, null, null);

        assertEquals(bcCopy.getProperties().getAccessTier(), tier2);
    }

    private static Stream<Arguments> copyWithTierSupplier() {
        return Stream.of(
            Arguments.of(AccessTier.HOT, AccessTier.COOL),
            Arguments.of(AccessTier.COOL, AccessTier.HOT));
    }

    @Test
    public void undeleteError() {
        bc = cc.getBlobClient(generateBlobName());
        assertThrows(BlobStorageException.class, () -> bc.undelete());
    }

    @Test
    public void getAccountInfo() {
        Response<StorageAccountInfo> response = primaryBlobServiceClient.getAccountInfoWithResponse(null, null);

        assertNotNull(response.getHeaders().getValue(HttpHeaderName.DATE));
        assertNotNull(response.getHeaders().getValue(X_MS_REQUEST_ID));
        assertNotNull(response.getHeaders().getValue(X_MS_VERSION));
        assertNotNull(response.getValue().getAccountKind());
        assertNotNull(response.getValue().getSkuName());
        assertFalse(response.getValue().isHierarchicalNamespaceEnabled());
    }

    @Test
    public void getAccountInfoMin() {
        assertResponseStatusCode(bc.getAccountInfoWithResponse(null, null), 200);
    }

    @Test
    public void getContainerName() {
        assertEquals(containerName, bc.getContainerName());
    }

    @Test
    public void getContainerClient() {
        String sasToken = cc.generateSas(
            new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(2),
                new BlobSasPermission().setReadPermission(true)));

        // Ensure a sas token is also persisted
        cc = getContainerClient(sasToken, cc.getBlobContainerUrl());

        // Ensure the correct endpoint
        assertEquals(cc.getBlobContainerUrl(), bc.getContainerClient().getBlobContainerUrl());
        // Ensure it is a functional client
        assertNotNull(bc.getContainerClient().getProperties());
    }

    @ParameterizedTest
    @MethodSource("getBlobNameSupplier")
    public void getBlobName(String inputName, String expectedOutputName) {
        bc = cc.getBlobClient(inputName);
        assertEquals(expectedOutputName, bc.getBlobName());
    }

    private static Stream<Arguments> getBlobNameSupplier() {
        return Stream.of(
            Arguments.of("blobName", "blobName"), // standard names should be preserved
            // encoded names should be decoded (not double decoded))
            Arguments.of(Utility.urlEncode("dir1/a%20b.txt"), "dir1/a%20b.txt"));
    }

    @ParameterizedTest
    @MethodSource("getBlobNameAndBuildClientSupplier")
    public void getBlobNameAndBuildClient(String originalBlobName, String finalBlobName) {
        BlobClient client = cc.getBlobClient(originalBlobName);
        BlobClientBase baseClient = cc.getBlobClient(client.getBlobName()).getBlockBlobClient();

        assertEquals(baseClient.getBlobName(), finalBlobName);
    }

    private static Stream<Arguments> getBlobNameAndBuildClientSupplier() {
        return Stream.of(
            Arguments.of("blob", "blob"),
            Arguments.of("path/to]a blob", "path/to]a blob"),
            Arguments.of("path%2Fto%5Da%20blob", "path/to]a blob"),
            Arguments.of("斑點", "斑點"),
            Arguments.of("%E6%96%91%E9%BB%9E", "斑點"));
    }

    @Test
    public void builderCpkValidation() {
        URL endpoint = BlobUrlParts.parse(bc.getBlobUrl()).setScheme("http").toUrl();
        BlobClientBuilder builder = new BlobClientBuilder()
            .customerProvidedKey(new CustomerProvidedKey(Base64.getEncoder().encodeToString(getRandomByteArray(256))))
            .endpoint(endpoint.toString());

        assertThrows(IllegalArgumentException.class, builder::buildClient);
    }

    @Test
    public void builderBearerTokenValidation() {
        URL endpoint = BlobUrlParts.parse(bc.getBlobUrl()).setScheme("http").toUrl();
        BlobClientBuilder builder = new BlobClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint.toString());

        assertThrows(IllegalArgumentException.class, builder::buildClient);
    }

    @Test
    public void parseSasTokenWithStartAndEndTimeDateOnly() {
        // sas token's st and se are usually in the following format: st=2021-06-21T00:00:00Z&se=2021-06-22T00:00:00Z
        // using a hardcoded url to test the start time and end time parsing without time added
        String testUrl = "https://accountName/containerName?sp=racwdl&st=2023-06-21&se=2023-06-22&spr=https&sv=2022-11-02&sr=c&sig=<signatureToken>";
        BlobUrlParts parts = BlobUrlParts.parse(testUrl);

        assertEquals(parts.getCommonSasQueryParameters().getStartTime().toLocalDate(),
            LocalDate.of(2023, 6, 21));
        assertEquals(parts.getCommonSasQueryParameters().getExpiryTime().toLocalDate(),
            LocalDate.of(2023, 6, 22));
    }

    @Test
    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials
    // and auth would fail because we changed a signed header.
    public void perCallPolicy() {
        bc = getBlobClientBuilder(bc.getBlobUrl()).addPolicy(getPerCallVersionPolicy()).buildClient();
        Response<BlobProperties> response = bc.getPropertiesWithResponse(null, null, null);
        assertEquals("2017-11-09", response.getHeaders().getValue(X_MS_VERSION));

    }

    @Test
    public void specializedChildClientGetsCached() {
        assertEquals(bc.getBlockBlobClient(), bc.getBlockBlobClient());
        assertEquals(bc.getAppendBlobClient(), bc.getAppendBlobClient());
        assertEquals(bc.getPageBlobClient(), bc.getPageBlobClient());
        assertEquals(bcAsync.getBlockBlobAsyncClient(), bcAsync.getBlockBlobAsyncClient());
        assertEquals(bcAsync.getAppendBlobAsyncClient(), bcAsync.getAppendBlobAsyncClient());
        assertEquals(bcAsync.getPageBlobAsyncClient(), bcAsync.getPageBlobAsyncClient());
    }

    @Test
    public void defaultAudience() {
        BlobClient aadBlob = getBlobClientBuilderWithTokenCredential(bc.getBlobUrl())
            .audience(null)
            .buildClient();

        assertTrue(aadBlob.exists());
    }

    @Test
    public void storageAccountAudience() {
        BlobClient aadBlob = getBlobClientBuilderWithTokenCredential(bc.getBlobUrl())
            .audience(BlobAudience.createBlobServiceAccountAudience(cc.getAccountName()))
            .buildClient();

        assertTrue(aadBlob.exists());
    }

    @Test
    public void audienceError() {
        BlobClient aadBlob = instrument(new BlobClientBuilder().endpoint(bc.getBlobUrl())
            .credential(new MockTokenCredential())
            .audience(BlobAudience.createBlobServiceAccountAudience("badAudience")))
            .buildClient();

        BlobStorageException e = assertThrows(BlobStorageException.class, aadBlob::exists);
        assertTrue(e.getErrorCode() == BlobErrorCode.INVALID_AUTHENTICATION_INFO);
    }

    @Test
    public void audienceFromString() {
        String url = String.format("https://%s.blob.core.windows.net/", cc.getAccountName());
        BlobAudience audience = BlobAudience.fromString(url);

        BlobClient aadBlob = getBlobClientBuilderWithTokenCredential(bc.getBlobUrl())
            .audience(audience)
            .buildClient();

        assertTrue(aadBlob.exists());
    }

}
