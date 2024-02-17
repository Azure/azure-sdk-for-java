// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.exception.UnexpectedLengthException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.ProgressListener;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.blob.ProgressReceiver;
import com.azure.storage.blob.implementation.models.BlockBlobsPutBlobFromUrlHeaders;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobAudience;
import com.azure.storage.blob.models.BlobCopySourceTagsMode;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.Block;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.models.BlockList;
import com.azure.storage.blob.models.BlockListType;
import com.azure.storage.blob.models.CustomerProvidedKey;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.options.BlobCopyFromUrlOptions;
import com.azure.storage.blob.options.BlobGetTagsOptions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.blob.options.BlobUploadFromFileOptions;
import com.azure.storage.blob.options.BlobUploadFromUrlOptions;
import com.azure.storage.blob.options.BlockBlobCommitBlockListOptions;
import com.azure.storage.blob.options.BlockBlobListBlocksOptions;
import com.azure.storage.blob.options.BlockBlobSimpleUploadOptions;
import com.azure.storage.blob.options.BlockBlobStageBlockOptions;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RetryPolicyType;
import com.azure.storage.common.test.shared.http.WireTapHttpClient;
import com.azure.storage.common.test.shared.policy.RequestAssertionPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("deprecation") // Using old APIs for testing purposes
public class BlockBlobApiTests extends BlobTestBase {
    private BlockBlobClient blockBlobClient;
    private BlockBlobAsyncClient blockBlobAsyncClient;
    private BlobAsyncClient blobAsyncClient;
    private BlobClient blobClient;
    private String blobName;
    private final List<File> createdFiles = new ArrayList<>();

    @BeforeEach
    public void setup() {
        blobName = generateBlobName();
        blobClient = cc.getBlobClient(blobName);
        blockBlobClient = blobClient.getBlockBlobClient();
        blockBlobClient.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize(), true);
        blobAsyncClient = ccAsync.getBlobAsyncClient(generateBlobName());
        blockBlobAsyncClient = blobAsyncClient.getBlockBlobAsyncClient();
        blockBlobAsyncClient.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize(), true).block();
    }

    @AfterEach
    public void cleanup() {
        createdFiles.forEach(File::delete);
    }

    @Test
    public void stageBlock() {
        Response<Void> response = blockBlobClient.stageBlockWithResponse(getBlockID(), DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize(), null, null, null, null);
        HttpHeaders headers = response.getHeaders();

        assertResponseStatusCode(response, 201);
        assertNotNull(headers.getValue(X_MS_CONTENT_CRC64));
        assertNotNull(headers.getValue(X_MS_REQUEST_ID));
        assertNotNull(headers.getValue(X_MS_VERSION));
        assertNotNull(headers.getValue(HttpHeaderName.DATE));
        assertTrue(Boolean.parseBoolean(headers.getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
    }

    @Test
    public void stageBlockWithBinaryData() {
        List<BinaryData> binaryDataList = Arrays.asList(
            BinaryData.fromBytes(DATA.getDefaultBytes()),
            BinaryData.fromString(DATA.getDefaultText()),
            BinaryData.fromFile(DATA.getDefaultFile()),
            BinaryData.fromFlux(DATA.getDefaultFlux(), (long) DATA.getDefaultDataSize(), false).block(),
            BinaryData.fromStream(DATA.getDefaultInputStream(), (long) DATA.getDefaultDataSize()));

        for (BinaryData binaryData : binaryDataList) {
            Response<Void> response = blockBlobClient.stageBlockWithResponse(
                new BlockBlobStageBlockOptions(getBlockID(), binaryData), null, null);
            HttpHeaders headers = response.getHeaders();

            assertResponseStatusCode(response, 201);
            assertNotNull(headers.getValue(X_MS_CONTENT_CRC64));
            assertNotNull(headers.getValue(X_MS_REQUEST_ID));
            assertNotNull(headers.getValue(X_MS_VERSION));
            assertNotNull(headers.getValue(HttpHeaderName.DATE));
            assertTrue(Boolean.parseBoolean(headers.getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
        }
    }

    @Test
    public void stageBlockWithBinaryDataAsync() {
        List<BinaryData> binaryDataList = Arrays.asList(
            BinaryData.fromBytes(DATA.getDefaultBytes()),
            BinaryData.fromString(DATA.getDefaultText()),
            BinaryData.fromFile(DATA.getDefaultFile()),
            BinaryData.fromFlux(DATA.getDefaultFlux(), (long) DATA.getDefaultDataSize(), false).block(),
            BinaryData.fromStream(DATA.getDefaultInputStream(), (long) DATA.getDefaultDataSize()));

        for (BinaryData binaryData : binaryDataList) {
            StepVerifier.create(blockBlobAsyncClient.stageBlockWithResponse(new BlockBlobStageBlockOptions(getBlockID(),
                binaryData))).assertNext(it -> {
                    HttpHeaders headers = it.getHeaders();
                    assertResponseStatusCode(it, 201);
                    assertNotNull(headers.getValue(X_MS_CONTENT_CRC64));
                    assertNotNull(headers.getValue(X_MS_REQUEST_ID));
                    assertNotNull(headers.getValue(X_MS_VERSION));
                    assertNotNull(headers.getValue(HttpHeaderName.DATE));
                    assertTrue(Boolean.parseBoolean(headers.getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
                }).verifyComplete();
        }
    }

    @Test
    public void stageBlockMin() {
        assertResponseStatusCode(blockBlobClient.stageBlockWithResponse(getBlockID(), DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize(), null, null, null, null), 201);
        assertEquals(blockBlobClient.listBlocks(BlockListType.ALL).getUncommittedBlocks().size(), 1);
    }

    @ParameterizedTest
    @MethodSource("stageBlockMinwithBinaryDataSupplier")
    public void stageBlockMinWithBinaryData(BinaryData binaryData) {
        assertResponseStatusCode(blockBlobClient.stageBlockWithResponse(new BlockBlobStageBlockOptions(getBlockID(),
            binaryData), null, null), 201);
        assertEquals(blockBlobClient.listBlocks(BlockListType.ALL).getUncommittedBlocks().size(), 1);
    }

    private static Stream<Arguments> stageBlockMinwithBinaryDataSupplier() {
        return Stream.of(
            Arguments.of(BinaryData.fromBytes(DATA.getDefaultBytes())),
            Arguments.of(BinaryData.fromString(DATA.getDefaultText())),
            Arguments.of(BinaryData.fromFile(DATA.getDefaultFile())),
            Arguments.of(BinaryData.fromStream(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong()))
        );
    }

    @Test
    public void stageBlockMinwithBinaryDataFromFlux() {
        BinaryData binaryData = BinaryData.fromFlux(DATA.getDefaultFlux(), DATA.getDefaultDataSizeLong(), false).block();
        assertResponseStatusCode(blockBlobClient.stageBlockWithResponse(new BlockBlobStageBlockOptions(getBlockID(),
            binaryData), null, null), 201);
        assertEquals(blockBlobClient.listBlocks(BlockListType.ALL).getUncommittedBlocks().size(), 1);
    }

    @ParameterizedTest
    @MethodSource("stageBlockDoesNotTransformReplayableBinaryDataSupplier")
    public void stageBlockDoesNotTransformReplayableBinaryData(BinaryData binaryData) {
        WireTapHttpClient wireTap = new WireTapHttpClient(getHttpClient());
        BlockBlobClient wireTapClient = getSpecializedBuilder(ENVIRONMENT.getPrimaryAccount().getCredential(),
            blockBlobClient.getBlobUrl())
            .httpClient(wireTap)
            .buildBlockBlobClient();

        assertResponseStatusCode(wireTapClient.stageBlockWithResponse(new BlockBlobStageBlockOptions(getBlockID(),
            binaryData), null, null), 201);
        assertEquals(blockBlobClient.listBlocks(BlockListType.ALL).getUncommittedBlocks().size(), 1);
        assertEquals(binaryData, wireTap.getLastRequest().getBodyAsBinaryData());
    }

    private static Stream<Arguments> stageBlockDoesNotTransformReplayableBinaryDataSupplier() {
        return Stream.of(
            Arguments.of(BinaryData.fromBytes(DATA.getDefaultBytes())),
            Arguments.of(BinaryData.fromString(DATA.getDefaultText())),
            Arguments.of(BinaryData.fromFile(DATA.getDefaultFile())));
    }

    @ParameterizedTest
    @MethodSource("stageBlockIllegalArgumentsSupplier")
    public void stageBlockIllegalArguments(boolean getBlockId, InputStream stream, int dataSize,
        Class<? extends Throwable> exceptionType) {
        String blockID = (getBlockId) ? getBlockID() : null;
        assertThrows(exceptionType, () -> blockBlobClient.stageBlock(blockID, stream, dataSize));
    }

    private static Stream<Arguments> stageBlockIllegalArgumentsSupplier() {
        return Stream.of(
            Arguments.of(false, DATA.getDefaultInputStream(), DATA.getDefaultDataSize(), BlobStorageException.class),
            Arguments.of(true, null, DATA.getDefaultDataSize(), NullPointerException.class),
            Arguments.of(true, DATA.getDefaultInputStream(), DATA.getDefaultDataSize() + 1, UnexpectedLengthException.class),
            Arguments.of(true, DATA.getDefaultInputStream(), DATA.getDefaultDataSize() - 1, UnexpectedLengthException.class)
        );
    }

    @Test
    public void stageBlockIllegalArgumentsWithBinaryData() {
        assertThrows(NullPointerException.class, () -> blockBlobClient.stageBlock(getBlockID(), null));

        assertThrows(NullPointerException.class, () -> blockBlobClient.stageBlock(getBlockID(),
            BinaryData.fromStream(DATA.getDefaultInputStream(), null)));

        BinaryData binaryData = BinaryData.fromStream(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong() + 1);
        assertThrows(UnexpectedLengthException.class, () -> blockBlobClient.stageBlock(getBlockID(), binaryData));

        BinaryData binaryData1 = BinaryData.fromStream(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong() - 1);
        assertThrows(UnexpectedLengthException.class, () -> blockBlobClient.stageBlock(getBlockID(), binaryData1));
    }

    @Test
    public void stageBlockEmptyBody() {
        assertThrows(BlobStorageException.class, () -> blockBlobClient.stageBlock(getBlockID(),
            new ByteArrayInputStream(new byte[0]), 0));
    }

    @Test
    public void stageBlockTransactionalMD5() throws NoSuchAlgorithmException {
        byte[] md5 = MessageDigest.getInstance("MD5").digest(DATA.getDefaultBytes());

        assertResponseStatusCode(blockBlobClient.stageBlockWithResponse(getBlockID(), DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize(), md5, null, null, null), 201);
    }

    @Test
    public void stageBlockTransactionalMD5fail() {
        BlobStorageException e = assertThrows(BlobStorageException.class, () ->
            blockBlobClient.stageBlockWithResponse(getBlockID(), DATA.getDefaultInputStream(),
                DATA.getDefaultDataSize(), MessageDigest.getInstance("MD5").digest("garbage".getBytes()), null, null,
                null));

        assertEquals(e.getErrorCode(), BlobErrorCode.MD5MISMATCH);
    }

    @Test
    public void stageBlockTransactionalMD5FailBinaryData() {
        BlobStorageException e = assertThrows(BlobStorageException.class, () -> blockBlobClient.stageBlockWithResponse(
            new BlockBlobStageBlockOptions(getBlockID(), BinaryData.fromBytes(DATA.getDefaultBytes()))
                .setContentMd5(MessageDigest.getInstance("MD5").digest("garbage".getBytes())), null, null));
        assertEquals(e.getErrorCode(), BlobErrorCode.MD5MISMATCH);
    }

    @Test
    public void stageBlockNullBody() {
        assertThrows(NullPointerException.class, () -> blockBlobClient.stageBlock(getBlockID(), null, 0));
    }

    @Test
    public void stageBlockLease() {
        String leaseID = setupBlobLeaseCondition(blockBlobClient, RECEIVED_LEASE_ID);

        assertResponseStatusCode(blockBlobClient.stageBlockWithResponse(getBlockID(), DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize(), null, leaseID, null, null), 201);
    }

    @Test
    public void stageBlockLeaseBinaryData() {
        String leaseID = setupBlobLeaseCondition(blockBlobClient, RECEIVED_LEASE_ID);

        assertResponseStatusCode(blockBlobClient.stageBlockWithResponse(
            new BlockBlobStageBlockOptions(getBlockID(), BinaryData.fromBytes(DATA.getDefaultBytes()))
                .setLeaseId(leaseID),
            null, null), 201);
    }

    @Test
    public void stageBlockLeaseFail() {
        setupBlobLeaseCondition(blockBlobClient, RECEIVED_LEASE_ID);

        BlobStorageException e = assertThrows(BlobStorageException.class,
            () -> blockBlobClient.stageBlockWithResponse(getBlockID(), DATA.getDefaultInputStream(),
                DATA.getDefaultDataSize(), null, GARBAGE_LEASE_ID, null, null));

        assertEquals(e.getErrorCode(), BlobErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION);
    }

    @Test
    public void stageBlockError() {
        blockBlobClient = cc.getBlobClient(generateBlobName()).getBlockBlobClient();

        assertThrows(BlobStorageException.class,
            () -> blockBlobClient.stageBlock("id", DATA.getDefaultInputStream(), DATA.getDefaultDataSize()));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void stageBlockRetryOnTransientFailure() {
        BlockBlobClient clientWithFailure = getBlobClient(
            ENVIRONMENT.getPrimaryAccount().getCredential(),
            blobClient.getBlobUrl(),
            new TransientFailureInjectingHttpPipelinePolicy()).getBlockBlobClient();

        byte[] data = getRandomByteArray(10);
        String blockId = getBlockID();
        clientWithFailure.stageBlock(blockId, new ByteArrayInputStream(data), data.length);
        blobClient.getBlockBlobClient().commitBlockList(Collections.singletonList(blockId), true);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        blobClient.download(os);
        TestUtils.assertArraysEqual(data, os.toByteArray());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void stageBlockRetryOnTransientFailureWithRetriableBinaryData() {
        List<BinaryData> binaryDataList = Arrays.asList(
            BinaryData.fromBytes(DATA.getDefaultBytes()),
            BinaryData.fromString(DATA.getDefaultText()),
            BinaryData.fromFile(DATA.getDefaultFile()));
        BlockBlobClient clientWithFailure = getBlobClient(
            ENVIRONMENT.getPrimaryAccount().getCredential(),
            blobClient.getBlobUrl(),
            new TransientFailureInjectingHttpPipelinePolicy()).getBlockBlobClient();

        for (BinaryData binaryData : binaryDataList) {
            String blockId = getBlockID();
            clientWithFailure.stageBlock(blockId, binaryData);
            blobClient.getBlockBlobClient().commitBlockList(Collections.singletonList(blockId), true);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            blobClient.download(os);
            TestUtils.assertArraysEqual(os.toByteArray(), binaryData.toBytes());
        }
    }

    @Test
    public void stageBlockFromUrl() {
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null);
        BlockBlobClient bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        String blockID = getBlockID();

        HttpHeaders headers = bu2.stageBlockFromUrlWithResponse(blockID, blockBlobClient.getBlobUrl(), null, null, null,
            null, null, null).getHeaders();

        assertNotNull(headers.getValue(X_MS_CONTENT_CRC64));
        assertNotNull(headers.getValue(X_MS_REQUEST_ID));
        assertNotNull(headers.getValue(X_MS_VERSION));
        assertTrue(Boolean.parseBoolean(headers.getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));

        BlockList response = bu2.listBlocks(BlockListType.ALL);
        assertEquals(response.getUncommittedBlocks().size(), 1);
        assertEquals(response.getCommittedBlocks().size(), 0);
        assertEquals(response.getUncommittedBlocks().get(0).getName(), blockID);

        bu2.commitBlockList(Collections.singletonList(blockID));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bu2.downloadStream(outputStream);

        assertEquals(ByteBuffer.wrap(outputStream.toByteArray()), DATA.getDefaultData());
    }

    @Test
    public void stageBlockFromUrlMin() {
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null);
        BlockBlobClient bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        String blockID = getBlockID();

        assertResponseStatusCode(bu2.stageBlockFromUrlWithResponse(blockID, blockBlobClient.getBlobUrl(), null, null,
            null, null, null, null), 201);
    }

    @ParameterizedTest
    @MethodSource("stageBlockFromURLIASupplier")
    public void stageBlockFromURLIA(boolean getBlockId, String sourceURL, Class<? extends Throwable> exceptionType) {
        String blockID = (getBlockId) ? getBlockID() : null;
        assertThrows(exceptionType, () -> blockBlobClient.stageBlockFromUrl(blockID, sourceURL, null));
    }

    private static Stream<Arguments> stageBlockFromURLIASupplier() {
        return Stream.of(
            Arguments.of(false, "http://www.example.com", BlobStorageException.class),
            Arguments.of(true, null, IllegalArgumentException.class));
    }

    @Test
    public void stageBlockFromURLRange() {
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null);
        BlockBlobClient destURL = cc.getBlobClient(generateBlobName()).getBlockBlobClient();

        destURL.stageBlockFromUrl(getBlockID(), blockBlobClient.getBlobUrl(), new BlobRange(2L, 3L));
        BlockList blockList = destURL.listBlocks(BlockListType.UNCOMMITTED);

        assertEquals(blockList.getCommittedBlocks().size(), 0);
        assertEquals(blockList.getUncommittedBlocks().size(), 1);
    }

    @Test
    public void stageBlockFromURLMD5() {
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null);
        BlockBlobClient destURL = cc.getBlobClient(generateBlobName()).getBlockBlobClient();

        assertDoesNotThrow(() -> destURL.stageBlockFromUrlWithResponse(getBlockID(), blockBlobClient.getBlobUrl(),
            null, MessageDigest.getInstance("MD5").digest(DATA.getDefaultBytes()), null,
            null, null, null));
    }

    @Test
    public void stageBlockFromURLMD5Fail() {
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null);
        BlockBlobClient destURL = cc.getBlobClient(generateBlobName()).getBlockBlobClient();

        assertThrows(BlobStorageException.class, () -> destURL.stageBlockFromUrlWithResponse(getBlockID(),
            blockBlobClient.getBlobUrl(), null, "garbage".getBytes(), null, null,
            null, null));
    }

    @Test
    public void stageBlockFromURLLease() {
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null);

        assertDoesNotThrow(() -> blockBlobClient.stageBlockFromUrlWithResponse(getBlockID(),
            blockBlobClient.getBlobUrl(), null, null,
            setupBlobLeaseCondition(blockBlobClient, RECEIVED_LEASE_ID), null, null,
            null));
    }

    @Test
    public void stageBlockFromURLLeaseFail() {
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null);
        assertThrows(BlobStorageException.class, () -> blockBlobClient.stageBlockFromUrlWithResponse(getBlockID(),
            blockBlobClient.getBlobUrl(), null, null, "garbage", null,
            null, null));
    }

    @Test
    public void stageBlockFromURLError() {
        blockBlobClient = primaryBlobServiceClient.getBlobContainerClient(generateContainerName())
            .getBlobClient(generateBlobName())
            .getBlockBlobClient();

        assertThrows(BlobStorageException.class, () ->
                blockBlobClient.stageBlockFromUrl(getBlockID(), blockBlobClient.getBlobUrl(), null));
    }

    @ParameterizedTest
    @MethodSource("stageBlockFromURLSourceACSupplier")
    public void stageBlockFromURLSourceAC(OffsetDateTime sourceIfModifiedSince, OffsetDateTime sourceIfUnmodifiedSince,
        String sourceIfMatch, String sourceIfNoneMatch) {
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null);
        String blockID = getBlockID();

        BlockBlobClient sourceURL = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        sourceURL.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());

        sourceIfMatch = setupBlobMatchCondition(sourceURL, sourceIfMatch);
        BlobRequestConditions smac = new BlobRequestConditions()
            .setIfModifiedSince(sourceIfModifiedSince)
            .setIfUnmodifiedSince(sourceIfUnmodifiedSince)
            .setIfMatch(sourceIfMatch)
            .setIfNoneMatch(sourceIfNoneMatch);

        assertResponseStatusCode(blockBlobClient.stageBlockFromUrlWithResponse(blockID, sourceURL.getBlobUrl(),
            null, null, null, smac, null, null), 201);
    }

    private static Stream<Arguments> stageBlockFromURLSourceACSupplier() {
        return Stream.of(Arguments.of(null, null, null, null),
            Arguments.of(OLD_DATE, null, null, null),
            Arguments.of(null, NEW_DATE, null, null),
            Arguments.of(null, null, RECEIVED_ETAG, null),
            Arguments.of(null, null, null, GARBAGE_ETAG));
    }

    @ParameterizedTest
    @MethodSource("stageBlockFromURLSourceACFailSupplier")
    public void stageBlockFromURLSourceACFail(OffsetDateTime sourceIfModifiedSince,
        OffsetDateTime sourceIfUnmodifiedSince, String sourceIfMatch, String sourceIfNoneMatch) {
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null);
        String blockID = getBlockID();

        BlockBlobClient sourceURL = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        sourceURL.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());

        BlobRequestConditions smac = new BlobRequestConditions()
            .setIfModifiedSince(sourceIfModifiedSince)
            .setIfUnmodifiedSince(sourceIfUnmodifiedSince)
            .setIfMatch(sourceIfMatch)
            .setIfNoneMatch(setupBlobMatchCondition(sourceURL, sourceIfNoneMatch));

        assertThrows(BlobStorageException.class, () ->
            blockBlobClient.stageBlockFromUrlWithResponse(blockID, sourceURL.getBlobUrl(), null,
                null, null, smac, null, null));
    }
    private static Stream<Arguments> stageBlockFromURLSourceACFailSupplier() {
        return Stream.of(
            Arguments.of(NEW_DATE, null, null, null),
            Arguments.of(null, OLD_DATE, null, null),
            Arguments.of(null, null, GARBAGE_ETAG, null),
            Arguments.of(null, null, null, RECEIVED_ETAG));
    }

    @Test
    public void commitBlockList() {
        String blockID = getBlockID();
        blockBlobClient.stageBlock(blockID, DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        List<String> ids = Collections.singletonList(blockID);

        Response<BlockBlobItem> response = blockBlobClient.commitBlockListWithResponse(ids, null, null,
            null, null, null, null);
        HttpHeaders headers = response.getHeaders();

        assertResponseStatusCode(response, 201);
        validateBasicHeaders(headers);
        assertNotNull(headers.getValue(X_MS_CONTENT_CRC64));
        assertTrue(Boolean.parseBoolean(headers.getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
    }

    @Test
    public void commitBlockListmin() {
        blockBlobClient = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        String blockID = getBlockID();
        blockBlobClient.stageBlock(blockID, DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        List<String> ids = Collections.singletonList(blockID);

        assertNotNull(blockBlobClient.commitBlockList(ids));
    }

    @Test
    public void commitBlockListMinNoOverwrite() {
        BlobStorageException e = assertThrows(BlobStorageException.class,
            () -> blockBlobClient.commitBlockList(new ArrayList<>()));
        assertEquals(BlobErrorCode.BLOB_ALREADY_EXISTS, e.getErrorCode());
    }

    @Test
    public void commitBlockListOverwrite() {
        assertDoesNotThrow(() -> blockBlobClient.commitBlockList(new ArrayList<>(), true));
    }

    @Test
    public void commitBlockListNull() {
        assertResponseStatusCode(blockBlobClient.commitBlockListWithResponse(null, null, null, null, null, null, null),
            201);
    }

    @ParameterizedTest
    @MethodSource("commitBlockListHeadersSupplier")
    public void commitBlockListHeaders(String cacheControl, String contentDisposition, String contentEncoding,
        String contentLanguage, byte[] contentMD5, String contentType) {
        String blockID = getBlockID();
        blockBlobClient.stageBlock(blockID, DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        List<String> ids = Collections.singletonList(blockID);
        BlobHttpHeaders headers = new BlobHttpHeaders()
            .setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType);

        blockBlobClient.commitBlockListWithResponse(ids, headers, null, null, null,
            null, null);
        Response<BlobProperties> response = blockBlobClient.getPropertiesWithResponse(null, null,
            null);

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType;

        validateBlobProperties(response, cacheControl, contentDisposition, contentEncoding, contentLanguage,
            contentMD5, contentType);
    }

    private static Stream<Arguments> commitBlockListHeadersSupplier() throws NoSuchAlgorithmException {
        return Stream.of(
            Arguments.of(null, null, null, null, null, null),
            Arguments.of("control", "disposition", "encoding", "language",
                MessageDigest.getInstance("MD5").digest(DATA.getDefaultBytes()), "type"));
    }

    @ParameterizedTest
    @MethodSource("commitBlockListMetadataSupplier")
    public void commitBlockListMetadata(String key1, String value1, String key2, String value2) {
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null) {
            metadata.put(key2, value2);
        }

        blockBlobClient.commitBlockListWithResponse(null, null, metadata, null,
            null, null, null);
        Response<BlobProperties> response = blockBlobClient.getPropertiesWithResponse(null, null, null);

        assertResponseStatusCode(response, 200);
        assertEquals(response.getValue().getMetadata(), metadata);
    }

    private static Stream<Arguments> commitBlockListMetadataSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null),
            Arguments.of("foo", "bar", "fizz", "buzz"));
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("commitBlockListTagsSupplier")
    public void commitBlockListTags(String key1, String value1, String key2, String value2) {
        Map<String, String> tags = new HashMap<>();
        if (key1 != null) {
            tags.put(key1, value1);
        }
        if (key2 != null) {
            tags.put(key2, value2);
        }

        blockBlobClient.commitBlockListWithResponse(
            new BlockBlobCommitBlockListOptions(null).setTags(tags), null, null);
        Response<Map<String, String>> response = blockBlobClient.getTagsWithResponse(new BlobGetTagsOptions(),
            null, null);

        assertResponseStatusCode(response, 200);
        assertEquals(response.getValue(), tags);
    }

    private static Stream<Arguments> commitBlockListTagsSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null),
            Arguments.of("foo", "bar", "fizz", "buzz"),
            Arguments.of(" +-./:=_  +-./:=_", " +-./:=_", null, null));
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void commitBlockListAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        blockBlobClient.setTags(t);
        match = setupBlobMatchCondition(blockBlobClient, match);
        leaseID = setupBlobLeaseCondition(blockBlobClient, leaseID);
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        assertResponseStatusCode(blockBlobClient.commitBlockListWithResponse(null, null,
            null, null, bac, null, null), 201);
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsFailSupplier")
    public void commitBlockListACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
        String noneMatch, String leaseID, String tags) {
        noneMatch = setupBlobMatchCondition(blockBlobClient, noneMatch);
        setupBlobLeaseCondition(blockBlobClient, leaseID);
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        BlobStorageException e = assertThrows(BlobStorageException.class,
            () -> blockBlobClient.commitBlockListWithResponse(null, null, null, null, bac, null, null));
        assertTrue(e.getErrorCode() == BlobErrorCode.CONDITION_NOT_MET
            || e.getErrorCode() == BlobErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION);
    }

    @Test
    public void commitBlockListError() {
        blockBlobClient = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        assertThrows(BlobStorageException.class, () -> blockBlobClient.commitBlockListWithResponse(new ArrayList<>(),
            null, null, null, new BlobRequestConditions().setLeaseId("garbage"), null, null));
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20211202ServiceVersion")
    @Test
    public void commitBlockListColdTier() {
        blockBlobClient = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        String blockID = getBlockID();
        blockBlobClient.stageBlock(blockID, DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        List<String> ids = Collections.singletonList(blockID);
        BlockBlobCommitBlockListOptions commitOptions = new BlockBlobCommitBlockListOptions(ids)
            .setTier(AccessTier.COLD);

        blockBlobClient.commitBlockListWithResponse(commitOptions, null, null);
        BlobProperties properties = blockBlobClient.getProperties();

        assertEquals(properties.getAccessTier(), AccessTier.COLD);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void getBlockList() {
        List<String> committedBlocks = Arrays.asList(getBlockID(), getBlockID());
        blockBlobClient.stageBlock(committedBlocks.get(0), DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        blockBlobClient.stageBlock(committedBlocks.get(1), DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        blockBlobClient.commitBlockList(committedBlocks, true);

        List<String> uncommittedBlocks = Arrays.asList(getBlockID(), getBlockID());
        blockBlobClient.stageBlock(uncommittedBlocks.get(0), DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        blockBlobClient.stageBlock(uncommittedBlocks.get(1), DATA.getDefaultInputStream(), DATA.getDefaultDataSize());

        uncommittedBlocks.sort(String::compareTo);

        // When
        BlockList blockList = blockBlobClient.listBlocks(BlockListType.ALL);

        // Then
        Set<String> actualCommittedBlocks = new HashSet<>();
        Set<String> actualUncommittedBlocks = new HashSet<>();

        for (Block block : blockList.getCommittedBlocks()) {
            actualCommittedBlocks.add(block.getName());
            assertEquals(block.getSize(), DATA.getDefaultDataSize());
        }

        for (Block block : blockList.getUncommittedBlocks()) {
            actualUncommittedBlocks.add(block.getName());
            assertEquals(block.getSize(), DATA.getDefaultDataSize());
        }

        assertEquals(new HashSet<>(committedBlocks), actualCommittedBlocks);
        assertEquals(new HashSet<>(uncommittedBlocks), actualUncommittedBlocks);

    }

    @Test
    public void getBlockListMin() {
        assertDoesNotThrow(() -> blockBlobClient.listBlocks(BlockListType.ALL));
    }

    @ParameterizedTest
    @MethodSource("getBlockListTypeSupplier")
    public void getBlockListType(BlockListType type, int committedCount, int uncommittedCount) {
        String blockID = getBlockID();
        blockBlobClient.stageBlock(blockID, DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        blockBlobClient.commitBlockList(Collections.singletonList(blockID), true);
        blockBlobClient.stageBlock(getBlockID(), DATA.getDefaultInputStream(), DATA.getDefaultDataSize());

        BlockList response = blockBlobClient.listBlocks(type);

        assertEquals(response.getCommittedBlocks().size(), committedCount);
        assertEquals(response.getUncommittedBlocks().size(), uncommittedCount);
    }

    private static Stream<Arguments> getBlockListTypeSupplier() {
        return Stream.of(
            Arguments.of(BlockListType.ALL, 1, 1),
            Arguments.of(BlockListType.COMMITTED, 1, 0),
            Arguments.of(BlockListType.UNCOMMITTED, 0, 1)
        );
    }

    @Test
    public void getBlockListTypeNull() {
        assertDoesNotThrow(() -> blockBlobClient.listBlocks(null).getCommittedBlocks().iterator().hasNext());
    }

    @Test
    public void getBlockListLease() {
        String leaseID = setupBlobLeaseCondition(blockBlobClient, RECEIVED_LEASE_ID);
        assertDoesNotThrow(() -> blockBlobClient.listBlocksWithResponse(BlockListType.ALL, leaseID, null,
            Context.NONE));

    }

    @Test
    public void getBlockListLeaseFail() {
        setupBlobLeaseCondition(blockBlobClient, GARBAGE_LEASE_ID);
        BlobStorageException e = assertThrows(BlobStorageException.class, () ->
            blockBlobClient.listBlocksWithResponse(BlockListType.ALL, GARBAGE_LEASE_ID, null, Context.NONE));

        assertEquals(e.getErrorCode(), BlobErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION);
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @Test
    public void getBlockListTags() {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        blockBlobClient.setTags(t);

        assertDoesNotThrow(() -> blockBlobClient.listBlocksWithResponse(
            new BlockBlobListBlocksOptions(BlockListType.ALL).setIfTagsMatch("\"foo\" = 'bar'"), null,
            Context.NONE));
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @Test
    public void getBlockListTagsFail() {
        BlobStorageException e = assertThrows(BlobStorageException.class, () ->
            blockBlobClient.listBlocksWithResponse(new BlockBlobListBlocksOptions(BlockListType.ALL)
                .setIfTagsMatch("\"notfoo\" = 'notbar'"), null, Context.NONE));

        assertEquals(e.getErrorCode(), BlobErrorCode.CONDITION_NOT_MET);
    }

    @Test
    public void getBlockListError() {
        blockBlobClient = cc.getBlobClient(generateBlobName()).getBlockBlobClient();

        assertThrows(BlobStorageException.class, () -> blockBlobClient.listBlocks(BlockListType.ALL)
            .getCommittedBlocks().iterator().hasNext());
    }

    @Test
    public void upload() {
        Response<BlockBlobItem> response = blockBlobClient.uploadWithResponse(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize(), null, null, null, null, null, null, null);

        assertResponseStatusCode(response, 201);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        blockBlobClient.downloadStream(outStream);
        TestUtils.assertArraysEqual(outStream.toByteArray(), DATA.getDefaultText().getBytes(StandardCharsets.UTF_8));
        validateBasicHeaders(response.getHeaders());
        assertNotNull(response.getHeaders().getValue(HttpHeaderName.CONTENT_MD5));
        assertTrue(Boolean.parseBoolean(response.getHeaders().getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
    }

    // Override name to prevent BinaryData.toString() invocation by test framework.
    @Test
    public void uploadBinaryData() {
        List<BinaryData> binaryDataList = Arrays.asList(
            BinaryData.fromBytes(DATA.getDefaultBytes()),
            BinaryData.fromString(DATA.getDefaultText()),
            BinaryData.fromFile(DATA.getDefaultFile()),
            BinaryData.fromFlux(DATA.getDefaultFlux(), (long) DATA.getDefaultDataSize(), false).block(),
            BinaryData.fromStream(DATA.getDefaultInputStream(), (long) DATA.getDefaultDataSize()));

        for (BinaryData binaryData : binaryDataList) {
            BlockBlobSimpleUploadOptions uploadOptions = new BlockBlobSimpleUploadOptions(binaryData);
            Response<BlockBlobItem> response = blockBlobClient.uploadWithResponse(uploadOptions, null, null);

            assertResponseStatusCode(response, 201);
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            blockBlobClient.download(outStream);
            TestUtils.assertArraysEqual(outStream.toByteArray(), DATA.getDefaultText().getBytes(StandardCharsets.UTF_8));
            validateBasicHeaders(response.getHeaders());
            assertNotNull(response.getHeaders().getValue(HttpHeaderName.CONTENT_MD5));
            assertTrue(Boolean.parseBoolean(response.getHeaders().getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
        }
    }

    // Override name to prevent BinaryData.toString() invocation by test framework.
    @Test
    public void uploadDoesNotTransformReplayableBinaryData() {
        List<BinaryData> binaryDataList = Arrays.asList(
            BinaryData.fromBytes(DATA.getDefaultBytes()),
            BinaryData.fromString(DATA.getDefaultText()),
            BinaryData.fromFile(DATA.getDefaultFile()));

        for (BinaryData binaryData : binaryDataList) {
            BlockBlobSimpleUploadOptions uploadOptions = new BlockBlobSimpleUploadOptions(binaryData);
            WireTapHttpClient wireTap = new WireTapHttpClient(getHttpClient());
            BlockBlobClient wireTapClient = getSpecializedBuilder(ENVIRONMENT.getPrimaryAccount().getCredential(),
                blockBlobClient.getBlobUrl())
                .httpClient(wireTap)
                .buildBlockBlobClient();
            Response<BlockBlobItem> response = wireTapClient.uploadWithResponse(uploadOptions, null, null);

            assertResponseStatusCode(response, 201);
            // Check that replayable BinaryData contents are passed to http client unchanged.
            assertEquals(wireTap.getLastRequest().getBodyAsBinaryData(), binaryData);
        }
    }

    /* Upload From File Tests: Need to run on liveMode only since blockBlob wil generate a `UUID.randomUUID()`
       for getBlockID that will change every time test is run
     */
    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    @ParameterizedTest
    @MethodSource("uploadFromFileSupplier")
    public void uploadFromFile(int fileSize, Long blockSize, int committedBlockCount) throws IOException {
        File file = getRandomFile(fileSize);
        file.deleteOnExit();
        createdFiles.add(file);

        BlobAsyncClient uploadBlobAsyncClient = getBlobAsyncClient(
            ENVIRONMENT.getPrimaryAccount().getCredential(), blobAsyncClient.getBlobUrl(),
            new RequestAssertionPolicy(
                request -> request.getBodyAsBinaryData() == null || request.getBodyAsBinaryData().isReplayable(),
                "File upload should be sending replayable request data"
            )
        );

        // Block length will be ignored for single shot.
        StepVerifier.create(uploadBlobAsyncClient.uploadFromFile(file.getPath(), new ParallelTransferOptions()
            .setBlockSizeLong(blockSize), null, null, null, null)).verifyComplete();

        File outFile = new File(file.getPath() + "result");
        createdFiles.add(outFile);
        outFile.createNewFile();
        outFile.deleteOnExit();
        Files.deleteIfExists(outFile.toPath());

        FileOutputStream outStream = new FileOutputStream(outFile);
        outStream.write(Objects.requireNonNull(FluxUtil.collectBytesInByteBufferStream(blobAsyncClient.downloadStream())
            .block()));
        outStream.close();

        compareFiles(file, outFile, 0, fileSize);
        StepVerifier.create(blobAsyncClient.getBlockBlobAsyncClient().listBlocks(BlockListType.COMMITTED))
            .assertNext(it -> assertEquals(it.getCommittedBlocks().size(), committedBlockCount))
            .verifyComplete();
    }

    @SuppressWarnings("deprecation")
    private static Stream<Arguments> uploadFromFileSupplier() {
        return Stream.of(
            Arguments.of(0, null, 0), // Size is too small to trigger stage block uploading
            Arguments.of(10, null, 0), // Size is too small to trigger stage block uploading
            Arguments.of(10 * Constants.KB, null, 0), // Size is too small to trigger stage block uploading
            Arguments.of(50 * Constants.MB, null, 0), // Size is too small to trigger stage block uploading
            Arguments.of(BlockBlobAsyncClient.MAX_UPLOAD_BLOB_BYTES + 1, null,
                // HTBB optimizations should trigger when file size is >100MB and defaults are used.
                (int) Math.ceil((BlockBlobClient.MAX_UPLOAD_BLOB_BYTES + 1.0) / BlobAsyncClient.BLOB_DEFAULT_HTBB_UPLOAD_BLOCK_SIZE)),
            // Size is too small to trigger stage block uploading
            Arguments.of(101 * Constants.MB, 4L * 1024 * 1024, 0)
    );
    }

    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    @Test
    public void uploadFromFileWithMetadata() throws IOException {
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        File file = getRandomFile(Constants.KB);
        file.deleteOnExit();
        createdFiles.add(file);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        blobClient.uploadFromFile(file.getAbsolutePath(), null, null, metadata, null, null, null);

        assertEquals(metadata, blockBlobClient.getProperties().getMetadata());
        blockBlobClient.downloadStream(outStream);
        TestUtils.assertArraysEqual(outStream.toByteArray(), Files.readAllBytes(file.toPath()));
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    @Test
    public void uploadFromFileWithTags() throws IOException {
        Map<String, String> tags = Collections.singletonMap(testResourceNamer.randomName(prefix, 20),
            testResourceNamer.randomName(prefix, 20));
        File file = getRandomFile(Constants.KB);
        file.deleteOnExit();
        createdFiles.add(file);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        blobClient.uploadFromFileWithResponse(new BlobUploadFromFileOptions(file.getAbsolutePath()).setTags(tags),
            null, null);

        assertEquals(tags, blockBlobClient.getTags());
        blockBlobClient.downloadStream(outStream);

        TestUtils.assertArraysEqual(outStream.toByteArray(), Files.readAllBytes(file.toPath()));
    }

    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    @Test
    public void uploadFromFileDefaultNoOverwrite() throws IOException {
        File file = getRandomFile(50);
        file.deleteOnExit();
        createdFiles.add(file);

        BlobStorageException e = assertThrows(BlobStorageException.class, () ->
            blobClient.uploadFromFile(file.toPath().toString()));

        assertEquals(e.getErrorCode(), BlobErrorCode.BLOB_ALREADY_EXISTS);

        File randomFile = getRandomFile(50);
        randomFile.deleteOnExit();
        createdFiles.add(randomFile);
        Files.deleteIfExists(randomFile.toPath());

        StepVerifier.create(blobAsyncClient.uploadFromFile(getRandomFile(50).toPath().toString()))
            .verifyError(BlobStorageException.class);
    }

    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    @Test
    public void uploadFromFileOverwrite() throws IOException {
        File file = getRandomFile(50);
        file.deleteOnExit();
        createdFiles.add(file);

        blobClient.uploadFromFile(file.toPath().toString(), true);

        File randomFile = getRandomFile(50);
        randomFile.deleteOnExit();
        createdFiles.add(randomFile);

        StepVerifier.create(blobAsyncClient.uploadFromFile(randomFile.toPath().toString(), true)).verifyComplete();
    }

    /*
     * Reports the number of bytes sent when uploading a file. This is different than other reporters which track the
     * number of reportings as upload from file hooks into the loading data from disk data stream which is a hard-coded
     * read size.
     */
    @SuppressWarnings("deprecation")
    static class FileUploadReporter implements ProgressReceiver {
        private long reportedByteCount;

        @Override
        public void reportProgress(long bytesTransferred) {
            this.reportedByteCount = bytesTransferred;
        }

        public long getReportedByteCount() {
            return this.reportedByteCount;
        }
    }

    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @MethodSource("uploadFromFileReporterSupplier")
    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    public void uploadFromFileReporter(int size, Long blockSize, Integer bufferCount) throws IOException {
        FileUploadReporter uploadReporter = new FileUploadReporter();
        File file = getRandomFile(size);
        file.deleteOnExit();
        createdFiles.add(file);

        ParallelTransferOptions  parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSizeLong(blockSize)
            .setMaxConcurrency(bufferCount)
            .setProgressReceiver(uploadReporter)
            .setMaxSingleUploadSizeLong(blockSize - 1);

        StepVerifier.create(blobAsyncClient.uploadFromFile(file.toPath().toString(), parallelTransferOptions, null,
            null, null, null)).verifyComplete();

        assertEquals(uploadReporter.getReportedByteCount(), size);

    }

    private static Stream<Arguments> uploadFromFileReporterSupplier() {
        return Stream.of(
            Arguments.of(10 * Constants.MB, 10L * Constants.MB, 8),
            Arguments.of(20 * Constants.MB, (long) Constants.MB, 5),
            Arguments.of(10 * Constants.MB, 5L * Constants.MB, 2),
            Arguments.of(10 * Constants.MB, 10L * Constants.KB, 100),
            Arguments.of(100, (long) Constants.MB, 2));
    }

    @ParameterizedTest
    @MethodSource("uploadFromFileReporterSupplier")
    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    public void uploadFromFileListener(int size, Long blockSize, Integer bufferCount) throws IOException {
        FileUploadReporter uploadListener = new FileUploadReporter();
        File file = getRandomFile(size);
        file.deleteOnExit();
        createdFiles.add(file);

        ParallelTransferOptions  parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSizeLong(blockSize)
            .setMaxConcurrency(bufferCount)
            .setProgressListener(uploadListener)
            .setMaxSingleUploadSizeLong(blockSize - 1);

        StepVerifier.create(blobAsyncClient.uploadFromFile(file.toPath().toString(), parallelTransferOptions, null,
            null, null, null)).verifyComplete();

        assertEquals(uploadListener.getReportedByteCount(), size);
    }

    @ParameterizedTest
    @MethodSource("uploadFromFileOptionsSupplier")
    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    public void uploadFromFileOptions(int dataSize, Long singleUploadSize, Long blockSize, double expectedBlockCount)
        throws IOException {
        File file = getRandomFile(dataSize);
        file.deleteOnExit();
        createdFiles.add(file);

        blobClient.uploadFromFile(file.toPath().toString(),
            new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(singleUploadSize),
            null, null, null, null, null);

        assertEquals(blobClient.getBlockBlobClient().listBlocks(BlockListType.COMMITTED).getCommittedBlocks().size(),
            expectedBlockCount);
    }

    @SuppressWarnings("deprecation")
    private static Stream<Arguments> uploadFromFileOptionsSupplier() {
        return Stream.of(
            // Test that the default for singleUploadSize is the maximum
            Arguments.of(BlockBlobAsyncClient.MAX_UPLOAD_BLOB_BYTES - 1, null, null, 0),
            Arguments.of(BlockBlobAsyncClient.MAX_UPLOAD_BLOB_BYTES + 1, null, null,
                /* This also validates the default for blockSize*/
                Math.ceil(((double) BlockBlobAsyncClient.MAX_UPLOAD_BLOB_BYTES + 1) / (double) BlobClient.BLOB_DEFAULT_HTBB_UPLOAD_BLOCK_SIZE)),
            // Test that singleUploadSize is respected
            Arguments.of(100, 50L, null, 1),
            // Test that blockSize is respected
            Arguments.of(100, 50L, 20L, 5)
        );
    }

    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    @Test
    // Reading from recordings will not allow for the timing of the test to work correctly.
    public void uploadFromFileTimeout() throws IOException {
        File file = getRandomFile(1024);
        file.deleteOnExit();
        createdFiles.add(file);

        assertThrows(IllegalStateException.class, () -> blobClient.uploadFromFile(file.getPath(), null, null, null,
            null, null, Duration.ofNanos(5L)));
    }

    @Test
    public void uploadMin() {
        blockBlobClient.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize(), true);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        blockBlobClient.download(outStream);
        TestUtils.assertArraysEqual(outStream.toByteArray(), DATA.getDefaultText().getBytes(StandardCharsets.UTF_8));
    }

    // Override name to prevent BinaryData.toString() invocation by test framework.
    @Test
    public void uploadMinBinaryData() {
        List<BinaryData> binaryDataList = Arrays.asList(
            BinaryData.fromBytes(DATA.getDefaultBytes()),
            BinaryData.fromString(DATA.getDefaultText()),
            BinaryData.fromFile(DATA.getDefaultFile()),
            BinaryData.fromFlux(DATA.getDefaultFlux(), (long) DATA.getDefaultDataSize(), false).block(),
            BinaryData.fromStream(DATA.getDefaultInputStream(), (long) DATA.getDefaultDataSize()));

        for (BinaryData binaryData : binaryDataList) {
            blockBlobClient.upload(binaryData, true);
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            blockBlobClient.downloadStream(outStream);
            TestUtils.assertArraysEqual(outStream.toByteArray(), DATA.getDefaultText().getBytes(StandardCharsets.UTF_8));
        }
    }

    @ParameterizedTest
    @MethodSource("uploadIllegalArgumentSupplier")
    public void uploadIllegalArgument(InputStream stream, long dataSize, Class<? extends Throwable> exceptionType) {
        assertThrows(exceptionType, () -> blockBlobClient.upload(stream, dataSize));
    }

    private static Stream<Arguments> uploadIllegalArgumentSupplier() {
        return Stream.of(
            Arguments.of(null, DATA.getDefaultDataSize(), NullPointerException.class),
            Arguments.of(DATA.getDefaultInputStream(), DATA.getDefaultDataSize() + 1, UnexpectedLengthException.class),
            Arguments.of(DATA.getDefaultInputStream(), DATA.getDefaultDataSize() - 1, UnexpectedLengthException.class));
    }

    @Test
    public void uploadIllegalArgumentBinaryData() {
        assertThrows(NullPointerException.class, () -> blockBlobClient.upload(null));

        assertThrows(NullPointerException.class, () ->
            blockBlobClient.upload(BinaryData.fromStream(DATA.getDefaultInputStream(), null)));

        BinaryData badLength1 = BinaryData.fromStream(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong() + 1);
        assertThrows(UnexpectedLengthException.class, () -> blockBlobClient.upload(badLength1));

        BinaryData badLength2 = BinaryData.fromStream(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong() - 1);
        assertThrows(UnexpectedLengthException.class, () -> blockBlobClient.upload(badLength2));
    }

    @Test
    public void uploadEmptyBody() {
        assertResponseStatusCode(blockBlobClient.uploadWithResponse(new ByteArrayInputStream(new byte[0]), 0, null,
            null, null, null, null, null, null), 201);
    }

    @Test
    public void uploadNullBody() {
        assertThrows(NullPointerException.class, () -> blockBlobClient.uploadWithResponse(null, 0, null, null, null,
            null, null, null, null));
    }

    @ParameterizedTest
    @MethodSource("uploadHeadersSupplier")
    public void uploadHeaders(String cacheControl, String contentDisposition, String contentEncoding,
        String contentLanguage, byte[] contentMD5, String contentType) throws NoSuchAlgorithmException {
        BlobHttpHeaders headers = new BlobHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType);

        blockBlobClient.uploadWithResponse(DATA.getDefaultInputStream(), DATA.getDefaultDataSize(), headers, null, null,
            null, null, null, null);
        Response<BlobProperties> response = blockBlobClient.getPropertiesWithResponse(null, null, null);

        // If the value isn't set the service will automatically set it
        contentMD5 =
            (contentMD5 == null) ? MessageDigest.getInstance("MD5").digest(DATA.getDefaultBytes()) : contentMD5;
        contentType = (contentType == null) ? "application/octet-stream" : contentType;
        validateBlobProperties(response, cacheControl, contentDisposition, contentEncoding, contentLanguage, contentMD5,
            contentType);

    }

    private static Stream<Arguments> uploadHeadersSupplier() throws NoSuchAlgorithmException {
        return Stream.of(
            Arguments.of(null, null, null, null, null, null),
            Arguments.of("control", "disposition", "encoding", "language",
                MessageDigest.getInstance("MD5").digest(DATA.getDefaultBytes()), "type"));
    }

    @Test
    public void uploadTransactionalMD5() throws NoSuchAlgorithmException {
        byte[] md5 = MessageDigest.getInstance("MD5").digest(DATA.getDefaultBytes());

        assertResponseStatusCode(blockBlobClient.uploadWithResponse(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize(), null, null, null, md5, null, null, null), 201);
    }

    @Test
    public void uploadTransactionalMD5Fail() {
        BlobStorageException e = assertThrows(BlobStorageException.class, () ->
            blockBlobClient.stageBlockWithResponse(getBlockID(), DATA.getDefaultInputStream(),
                DATA.getDefaultDataSize(), MessageDigest.getInstance("MD5").digest("garbage".getBytes()), null, null,
                null));
        assertEquals(BlobErrorCode.MD5MISMATCH, e.getErrorCode());
    }

    @ParameterizedTest
    @MethodSource("commitBlockListMetadataSupplier")
    public void uploadMetadata(String key1, String value1, String key2, String value2) {
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null) {
            metadata.put(key2, value2);
        }

        blockBlobClient.uploadWithResponse(DATA.getDefaultInputStream(), DATA.getDefaultDataSize(), null, metadata,
            null, null, null, null, null);
        Response<BlobProperties> response = blockBlobClient.getPropertiesWithResponse(null, null, null);
        assertResponseStatusCode(response, 200);
        assertEquals(metadata, response.getValue().getMetadata());
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("commitBlockListTagsSupplier")
    public void uploadTags(String key1, String value1, String key2, String value2) {
        Map<String, String> tags = new HashMap<>();
        if (key1 != null) {
            tags.put(key1, value1);
        }
        if (key2 != null) {
            tags.put(key2, value2);
        }

        blockBlobClient.uploadWithResponse(new BlockBlobSimpleUploadOptions(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize()).setTags(tags), null, null);
        Response<Map<String, String>> response = blockBlobClient.getTagsWithResponse(new BlobGetTagsOptions(), null,
            null);

        assertResponseStatusCode(response, 200);
        assertEquals(tags, response.getValue());
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void uploadAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        blockBlobClient.setTags(t);
        match = setupBlobMatchCondition(blockBlobClient, match);
        leaseID = setupBlobLeaseCondition(blockBlobClient, leaseID);
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        assertResponseStatusCode(blockBlobClient.uploadWithResponse(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize(), null, null, null, null, bac, null, null), 201);
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsFailSupplier")
    public void uploadACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID, String tags) {
        noneMatch = setupBlobMatchCondition(blockBlobClient, noneMatch);
        setupBlobLeaseCondition(blockBlobClient, leaseID);
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        BlobStorageException e = assertThrows(BlobStorageException.class, () ->
            blockBlobClient.uploadWithResponse(DATA.getDefaultInputStream(), DATA.getDefaultDataSize(), null, null,
                null, null, bac, null, null));

        assertTrue(e.getErrorCode() == BlobErrorCode.CONDITION_NOT_MET
            || e.getErrorCode() == BlobErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION);
    }

    @Test
    public void uploadError() {
        blockBlobClient = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        assertThrows(BlobStorageException.class, () -> blockBlobClient.uploadWithResponse(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize(), null, null, null, null, new BlobRequestConditions().setLeaseId("id"), null,
            null));
    }

    @Test
    public void uploadWithTier() {
        BlockBlobClient bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient();

        bc.uploadWithResponse(DATA.getDefaultInputStream(), DATA.getDefaultDataSize(), null, null, AccessTier.COOL,
            null, null, null, null);

        assertEquals(bc.getProperties().getAccessTier(), AccessTier.COOL);
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20211202ServiceVersion")
    @Test
    public void uploadWithAccessTierCold() {
        BlockBlobClient bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        bc.uploadWithResponse(DATA.getDefaultInputStream(), DATA.getDefaultDataSize(), null, null, AccessTier.COLD,
            null, null, null, null);
        assertEquals(bc.getProperties().getAccessTier(), AccessTier.COLD);
    }

    @Test
    public void uploadOverwriteFalse() {
        assertThrows(BlobStorageException.class, () -> blockBlobClient.upload(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize()));
    }

    @Test
    public void uploadOverwriteTrue() {
        assertDoesNotThrow(() -> blockBlobClient.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize(), true));
    }

    @Test
    public void uploadRetryOnTransientFailure() {
        BlockBlobClient clientWithFailure = getBlobClient(
            ENVIRONMENT.getPrimaryAccount().getCredential(),
            blobClient.getBlobUrl(),
            new TransientFailureInjectingHttpPipelinePolicy()
        ).getBlockBlobClient();

        byte[] data = getRandomByteArray(10);
        clientWithFailure.upload(new ByteArrayInputStream(data), data.length, true);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        blobClient.downloadStream(os);
        TestUtils.assertArraysEqual(os.toByteArray(), data);
    }

    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    @Test
    public void asyncBufferedUploadEmpty() {
        StepVerifier.create(blobAsyncClient.upload(Flux.just(ByteBuffer.wrap(new byte[0])), null, true))
            .assertNext(it -> assertNotNull(it.getETag()))
            .verifyComplete();

        StepVerifier.create(blobAsyncClient.downloadStream())
            .assertNext(it -> assertEquals(0, it.remaining()))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("asyncBufferedUploadEmptyBuffersSupplier")
    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    public void asyncBufferedUploadEmptyBuffers(ByteBuffer buffer1, ByteBuffer buffer2, ByteBuffer buffer3,
        byte[] expectedDownload) {
        StepVerifier.create(blobAsyncClient.upload(Flux.fromIterable(Arrays.asList(buffer1, buffer2, buffer3)),
                null, true)).assertNext(it -> assertNotNull(it.getETag())).verifyComplete();

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(blobAsyncClient.downloadStream()))
            .assertNext(it -> TestUtils.assertArraysEqual(it, expectedDownload))
            .verifyComplete();
    }

    private static Stream<Arguments> asyncBufferedUploadEmptyBuffersSupplier() {
        return Stream.of(Arguments.of(ByteBuffer.wrap("Hello".getBytes(StandardCharsets.UTF_8)),
            ByteBuffer.wrap(" ".getBytes(StandardCharsets.UTF_8)),
            ByteBuffer.wrap("world!".getBytes(StandardCharsets.UTF_8)),
            "Hello world!".getBytes(StandardCharsets.UTF_8)),
            Arguments.of(ByteBuffer.wrap("Hello".getBytes(StandardCharsets.UTF_8)),
                ByteBuffer.wrap(" ".getBytes(StandardCharsets.UTF_8)),
                ByteBuffer.wrap(new byte[0]),
                "Hello ".getBytes(StandardCharsets.UTF_8)),
            Arguments.of(ByteBuffer.wrap("Hello".getBytes(StandardCharsets.UTF_8)),
                ByteBuffer.wrap(new byte[0]),
                ByteBuffer.wrap("world!".getBytes(StandardCharsets.UTF_8)),
                "Helloworld!".getBytes(StandardCharsets.UTF_8)),
            Arguments.of(ByteBuffer.wrap(new byte[0]),
                ByteBuffer.wrap(" ".getBytes(StandardCharsets.UTF_8)),
                ByteBuffer.wrap("world!".getBytes(StandardCharsets.UTF_8)),
                " world!".getBytes(StandardCharsets.UTF_8)));
    }

    // Only run these tests in live mode as they use variables that can't be captured.
    @ParameterizedTest
    @MethodSource("asyncBufferedUploadSupplier")
    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    public void asyncBufferedUpload(int dataSize, long bufferSize, int numBuffs, int blockCount) {
        BlobAsyncClient asyncClient = getPrimaryServiceClientForWrites(bufferSize)
            .getBlobContainerAsyncClient(blobAsyncClient.getContainerName())
            .getBlobAsyncClient(blobAsyncClient.getBlobName());

        ByteBuffer data = getRandomData(dataSize);
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSizeLong(bufferSize).setMaxConcurrency(numBuffs).setMaxSingleUploadSizeLong(4L * Constants.MB);
        asyncClient.upload(Flux.just(data), parallelTransferOptions, true).block();
        data.position(0);

        // Due to memory issues, this check only runs on small to medium sized data sets.
        if (dataSize < 100 * 1024 * 1024) {
            StepVerifier.create(collectBytesInBuffer(blockBlobAsyncClient.downloadStream()))
                .assertNext(it -> assertEquals(it, data))
                .verifyComplete();
        }

        StepVerifier.create(blockBlobAsyncClient.listBlocks(BlockListType.ALL))
            .assertNext(it -> assertEquals(it.getCommittedBlocks().size(), blockCount))
            .verifyComplete();
    }

    private static Stream<Arguments> asyncBufferedUploadSupplier() {
        return Stream.of(
            Arguments.of(35 * Constants.MB, 5 * Constants.MB, 2, 7), // Requires cycling through the same buffers multiple times.
            Arguments.of(35 * Constants.MB, 5 * Constants.MB, 5, 7), // Most buffers may only be used once.
            Arguments.of(100 * Constants.MB, 10 * Constants.MB, 2, 10), // Larger data set.
            Arguments.of(100 * Constants.MB, 10 * Constants.MB, 5, 10), // Larger number of Buffs.
            Arguments.of(10 * Constants.MB, Constants.MB, 10, 10), // Exactly enough buffer space to hold all the data.
            Arguments.of(50 * Constants.MB, 10 * Constants.MB, 2, 5), // Larger data.
            Arguments.of(10 * Constants.MB, 2 * Constants.MB, 4, 5),
            Arguments.of(10 * Constants.MB, 3 * Constants.MB, 3, 4)); // Data does not squarely fit in buffers.
    }

    @Test
    public void asyncUploadBinaryData() {
        blobAsyncClient.upload(DATA.getDefaultBinaryData(), true).block();
        StepVerifier.create(blockBlobAsyncClient.downloadContent())
            .assertNext(it -> TestUtils.assertArraysEqual(it.toBytes(), DATA.getDefaultBinaryData().toBytes()))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("asyncBufferedUploadComputeMd5Supplier")
    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    public void asyncBufferedUploadComputeMd5(int size, Long maxSingleUploadSize, Long blockSize, int byteBufferCount) {
        List<ByteBuffer> byteBufferList = new ArrayList<>();
        for (int i = 0; i < byteBufferCount; i++) {
            byteBufferList.add(getRandomData(size));
        }
        Flux<ByteBuffer> flux = Flux.fromIterable(byteBufferList);
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setMaxSingleUploadSizeLong(maxSingleUploadSize)
            .setBlockSizeLong(blockSize);

        assertResponseStatusCode(Objects.requireNonNull(blobAsyncClient.uploadWithResponse(
            new BlobParallelUploadOptions(flux)
                .setParallelTransferOptions(parallelTransferOptions)
                .setComputeMd5(true)).block()), 201);

    }

    private static Stream<Arguments> asyncBufferedUploadComputeMd5Supplier() {
        return Stream.of(
            Arguments.of(Constants.KB, null, null, 1), // Simple case where uploadFull is called.
            Arguments.of(Constants.KB, (long) Constants.KB, 500L * Constants.KB, 1000), // uploadChunked 2 blocks staged
            Arguments.of(Constants.KB, (long) Constants.KB, 5L * Constants.KB, 1000)); // uploadChunked 100 blocks staged
    }

    @Test
    public void asyncUploadBinaryDataWithResponse() {
        assertResponseStatusCode(Objects.requireNonNull(
            blobAsyncClient.uploadWithResponse(new BlobParallelUploadOptions(DATA.getDefaultBinaryData())).block()),
            201);
    }

    private boolean compareListToBuffer(List<ByteBuffer> buffers, ByteBuffer result) {
        result.position(0);
        for (ByteBuffer buffer : buffers) {
            buffer.position(0);
            result.limit(result.position() + buffer.remaining());
            if (!buffer.equals(result)) {
                return false;
            }
            result.position(result.position() + buffer.remaining());
        }
        return result.remaining() == 0;
    }

    /*      Reporter for testing Progress Receiver
     *        Will count the number of reports that are triggered         */

    @SuppressWarnings("deprecation")
    static class Reporter implements ProgressReceiver {
        private final long blockSize;
        private long reportingCount;

        Reporter(long blockSize) {
            this.blockSize = blockSize;
        }

        @Override
        public void reportProgress(long bytesTransferred) {
            assertEquals(0, bytesTransferred % blockSize);
            this.reportingCount += 1;
        }

        long getReportingCount() {
            return this.reportingCount;
        }
    }

    static class Listener implements ProgressListener {
        private final long blockSize;
        private long reportingCount;

        Listener(long blockSize) {
            this.blockSize = blockSize;
        }

        @Override
        public void handleProgress(long bytesTransferred) {
            assertEquals(0, bytesTransferred % blockSize);
            this.reportingCount += 1;
        }

        long getReportingCount() {
            return this.reportingCount;
        }
    }

    // Only run these tests in live mode as they use variables that can't be captured.
    @ParameterizedTest
    @MethodSource("bufferedUploadWithReporterSupplier")
    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    public void bufferedUploadWithReporter(int size, long blockSize, int bufferCount) {
        BlobAsyncClient asyncClient = getPrimaryServiceClientForWrites(blockSize)
            .getBlobContainerAsyncClient(blobAsyncClient.getContainerName())
            .getBlobAsyncClient(blobAsyncClient.getBlobName());

        Reporter uploadReporter = new Reporter(blockSize);

        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSizeLong(blockSize)
            .setMaxConcurrency(bufferCount)
            .setProgressReceiver(uploadReporter)
            .setMaxSingleUploadSizeLong(4L * Constants.MB);

        StepVerifier.create(asyncClient.uploadWithResponse(Flux.just(getRandomData(size)), parallelTransferOptions,
            null, null, null, null)).assertNext(it -> {
                assertEquals(it.getStatusCode(), 201);
                /*
                * Verify that the reporting count is equal or greater than the size divided by block size in the case
                * that operations need to be retried. Retry attempts will increment the reporting count.
                */
                assertTrue(uploadReporter.getReportingCount() >= ((long) size / blockSize));
            }).verifyComplete();
    }

    private static Stream<Arguments> bufferedUploadWithReporterSupplier() {
        return Stream.of(
            Arguments.of(10 * Constants.MB, 10L * Constants.MB, 8),
            Arguments.of(20 * Constants.MB, Constants.MB, 5),
            Arguments.of(10 * Constants.MB, 5 * Constants.MB, 2),
            Arguments.of(10 * Constants.MB, 512 * Constants.KB, 20)
        );
    }

    // Only run these tests in live mode as they use variables that can't be captured.
    @ParameterizedTest
    @MethodSource("bufferedUploadWithReporterSupplier")
    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    public void bufferedUploadWithListener(int size, long blockSize, int bufferCount) {
        BlobAsyncClient asyncClient = getPrimaryServiceClientForWrites(blockSize)
            .getBlobContainerAsyncClient(blobAsyncClient.getContainerName())
            .getBlobAsyncClient(blobAsyncClient.getBlobName());

        Listener uploadListener = new Listener(blockSize);

        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSizeLong(blockSize)
            .setMaxConcurrency(bufferCount)
            .setProgressListener(uploadListener)
            .setMaxSingleUploadSizeLong(4L * Constants.MB);

        StepVerifier.create(asyncClient.uploadWithResponse(Flux.just(getRandomData(size)), parallelTransferOptions,
            null, null, null, null)).assertNext(it -> {
                assertResponseStatusCode(it, 201);
                /*
                * Verify that the reporting count is equal or greater than the size divided by block size in the case
                * that operations need to be retried. Retry attempts will increment the reporting count.
                */
                assertTrue(uploadListener.getReportingCount() >= ((long) size / blockSize));
            }).verifyComplete();
    }

    // Only run these tests in live mode as they use variables that can't be captured.
    @ParameterizedTest
    @MethodSource("bufferedUploadChunkedSourceSupplier")
    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    public void bufferedUploadChunkedSource(int[] dataSizeList, long bufferSize, int numBuffers, int blockCount) {
        /*
        This test should validate that the upload should work regardless of what format the passed data is in because
        it will be chunked appropriately.
         */
        BlobAsyncClient asyncClient = getPrimaryServiceClientForWrites(bufferSize * Constants.MB)
            .getBlobContainerAsyncClient(blobAsyncClient.getContainerName())
            .getBlobAsyncClient(blobAsyncClient.getBlobName());
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSizeLong(bufferSize * Constants.MB)
            .setMaxConcurrency(numBuffers).setMaxSingleUploadSizeLong(4L * Constants.MB);
        List<ByteBuffer> dataList = new ArrayList<>();
        for (int size : dataSizeList) {
            dataList.add(getRandomData(size * Constants.MB));
        }
        Mono<BlockBlobItem> uploadOperation = asyncClient.upload(Flux.fromIterable(dataList), parallelTransferOptions,
            true);

        StepVerifier.create(uploadOperation.then(collectBytesInBuffer(blockBlobAsyncClient.downloadStream())))
            .assertNext(it -> assertTrue(compareListToBuffer(dataList, it)))
            .verifyComplete();

        StepVerifier.create(blockBlobAsyncClient.listBlocks(BlockListType.ALL))
            .assertNext(it -> assertEquals(it.getCommittedBlocks().size(), blockCount))
            .verifyComplete();
    }

    private static Stream<Arguments> bufferedUploadChunkedSourceSupplier() {
        return Stream.of(
            // First item fits entirely in the buffer, next item spans two buffers
            Arguments.of(new int[]{7, 7}, 10, 2, 2),
            // Multiple items fit non-exactly in one buffer.
            Arguments.of(new int[]{3, 3, 3, 3, 3, 3, 3}, 10, 2, 3),
            // Data fits exactly and does not need chunking.
            Arguments.of(new int[]{10, 10}, 10, 2, 2),
            // Data needs chunking and does not fit neatly in buffers. Requires waiting for buffers to be released.
            Arguments.of(new int[]{50, 51, 49}, 10, 2, 15));
    }

    @ParameterizedTest
    @MethodSource("bufferedUploadHandlePathingSupplier")
    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    public void bufferedUploadHandlePathing(int[] dataSizeList, int blockCount) {
        List<ByteBuffer> dataList = new ArrayList<>();
        for (int size : dataSizeList) {
            dataList.add(getRandomData(size));
        }
        Mono<BlockBlobItem> uploadOperation = blobAsyncClient.upload(Flux.fromIterable(dataList),
            new ParallelTransferOptions().setMaxSingleUploadSizeLong(4L * Constants.MB), true);

        StepVerifier.create(uploadOperation.then(collectBytesInBuffer(blockBlobAsyncClient.downloadStream())))
            .assertNext(it -> assertTrue(compareListToBuffer(dataList, it)))
            .verifyComplete();

        StepVerifier.create(blockBlobAsyncClient.listBlocks(BlockListType.ALL))
            .assertNext(it -> assertEquals(it.getCommittedBlocks().size(), blockCount))
            .verifyComplete();
    }

    private static Stream<Arguments> bufferedUploadHandlePathingSupplier() {
        return Stream.of(
            Arguments.of(new int[]{4 * Constants.MB + 1, 10}, 2),
            Arguments.of(new int[]{4 * Constants.MB}, 0),
            Arguments.of(new int[]{10, 100, 1000, 10000}, 0),
            Arguments.of(new int[]{4 * Constants.MB, 4 * Constants.MB}, 2)
        );
    }

    @ParameterizedTest
    @MethodSource("bufferedUploadHandlePathingSupplier")
    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    public void bufferedUploadHandlePathingHotFlux(int[] dataSizeList, int blockCount) {
        List<ByteBuffer> dataList = new ArrayList<>();
        for (int size : dataSizeList) {
            dataList.add(getRandomData(size));
        }
        Mono<BlockBlobItem> uploadOperation = blobAsyncClient.upload(
            Flux.fromIterable(dataList).publish().autoConnect(),
            new ParallelTransferOptions().setMaxSingleUploadSizeLong(4L * Constants.MB), true);

        StepVerifier.create(uploadOperation.then(collectBytesInBuffer(blockBlobAsyncClient.downloadStream())))
            .assertNext(it -> assertTrue(compareListToBuffer(dataList, it)))
            .verifyComplete();

        StepVerifier.create(blockBlobAsyncClient.listBlocks(BlockListType.ALL))
            .assertNext(it -> assertEquals(it.getCommittedBlocks().size(), blockCount))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("bufferedUploadHandlePathingHotFluxWithTransientFailureSupplier")
    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    public void bufferedUploadHandlePathingHotFluxWithTransientFailure(int[] dataSizeList, int blockCount) {
        BlobAsyncClient clientWithFailure = getBlobAsyncClient(
            ENVIRONMENT.getPrimaryAccount().getCredential(),
            blobAsyncClient.getBlobUrl(),
            new TransientFailureInjectingHttpPipelinePolicy()
        );

        List<ByteBuffer> dataList = new ArrayList<>();
        for (int size : dataSizeList) {
            dataList.add(getRandomData(size));
        }
        Mono<BlockBlobItem> uploadOperation = clientWithFailure.upload(
            Flux.fromIterable(dataList).publish().autoConnect(),
            new ParallelTransferOptions().setMaxSingleUploadSizeLong(4L * Constants.MB), true);

        StepVerifier.create(uploadOperation.then(collectBytesInBuffer(blockBlobAsyncClient.downloadStream())))
            .assertNext(it -> assertTrue(compareListToBuffer(dataList, it)))
            .verifyComplete();

        StepVerifier.create(blockBlobAsyncClient.listBlocks(BlockListType.ALL))
            .assertNext(it -> assertEquals(it.getCommittedBlocks().size(), blockCount))
            .verifyComplete();
    }

    private static Stream<Arguments> bufferedUploadHandlePathingHotFluxWithTransientFailureSupplier() {
        return Stream.of(
            Arguments.of(new int[]{10, 100, 1000, 10000}, 0),
            Arguments.of(new int[]{4 * Constants.MB + 1, 10}, 2),
            Arguments.of(new int[]{4 * Constants.MB, 4 * Constants.MB}, 2));
    }

    @ParameterizedTest
    @MethodSource("bufferedUploadSyncHandlePathingWithTransientFailureSupplier")
    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    public void bufferedUploadSyncHandlePathingWithTransientFailure(int dataSize, int blockCount) {
        /*
        This test ensures that although we no longer mark and reset the source stream for buffered upload, it still
        supports retries in all cases for the sync client.
         */
        BlobClient clientWithFailure = getBlobClient(
            ENVIRONMENT.getPrimaryAccount().getCredential(),
            blobClient.getBlobUrl(),
            new TransientFailureInjectingHttpPipelinePolicy()
        );

        byte[] data = getRandomByteArray(dataSize);
        clientWithFailure.uploadWithResponse(new ByteArrayInputStream(data), dataSize,
            new ParallelTransferOptions().setMaxSingleUploadSizeLong(2L * Constants.MB)
                .setBlockSizeLong(2L * Constants.MB), null, null, null, null, null, null);

        ByteArrayOutputStream os = new ByteArrayOutputStream(dataSize);
        blobClient.downloadStream(os);
        TestUtils.assertArraysEqual(data, os.toByteArray());

        assertEquals(blobClient.getBlockBlobClient().listBlocks(BlockListType.ALL).getCommittedBlocks().size(),
            blockCount);
    }

    private static Stream<Arguments> bufferedUploadSyncHandlePathingWithTransientFailureSupplier() {
        return Stream.of(
            Arguments.of(11110, 0),
            Arguments.of(2 * Constants.MB + 11, 2));
    }

    @Test
    public void bufferedUploadIllegalArgumentsNull() {
        StepVerifier.create(blobAsyncClient.upload(null, new ParallelTransferOptions()
                .setBlockSizeLong(4L)
                .setMaxConcurrency(4), true))
            .verifyErrorSatisfies(it -> assertInstanceOf(NullPointerException.class, it));
    }

    @ParameterizedTest
    @MethodSource("bufferedUploadIllegalArgsOutOfBoundsSupplier")
    public void bufferedUploadIllegalArgsOutOfBounds(long bufferSize, int numBuffs) {
        assertThrows(IllegalArgumentException.class, () -> new ParallelTransferOptions()
            .setBlockSizeLong(bufferSize)
            .setMaxConcurrency(numBuffs));
    }

    private static Stream<Arguments> bufferedUploadIllegalArgsOutOfBoundsSupplier() {
        return Stream.of(
            Arguments.of(0, 5),
            Arguments.of(BlockBlobAsyncClient.MAX_STAGE_BLOCK_BYTES_LONG + 1, 5),
            Arguments.of(5, 0));
    }

    // Only run these tests in live mode as they use variables that can't be captured.
    @ParameterizedTest
    @MethodSource("bufferedUploadHeadersSupplier")
    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    public void bufferedUploadHeaders(int dataSize, String cacheControl, String contentDisposition,
        String contentEncoding, String contentLanguage, boolean validateContentMD5, String contentType)
        throws NoSuchAlgorithmException {
        byte[] bytes = getRandomByteArray(dataSize);
        byte[] contentMD5 = validateContentMD5 ? MessageDigest.getInstance("MD5").digest(bytes) : null;
        Mono<Response<BlockBlobItem>> uploadOperation = blobAsyncClient.uploadWithResponse(
            Flux.just(ByteBuffer.wrap(bytes)), new ParallelTransferOptions()
                .setMaxSingleUploadSizeLong(4L * Constants.MB), new BlobHttpHeaders()
                .setCacheControl(cacheControl)
                .setContentDisposition(contentDisposition)
                .setContentEncoding(contentEncoding)
                .setContentLanguage(contentLanguage)
                .setContentMd5(contentMD5)
                .setContentType(contentType),
            null, null, null);

        StepVerifier.create(uploadOperation.then(blockBlobAsyncClient.getPropertiesWithResponse(null)))
            .assertNext(it -> assertTrue(validateBlobProperties(it, cacheControl, contentDisposition, contentEncoding,
                contentLanguage, contentMD5, contentType == null ? "application/octet-stream" : contentType)))
            .verifyComplete();
        // HTTP default content type is application/octet-stream.


    }

    private static Stream<Arguments> bufferedUploadHeadersSupplier() {
        return Stream.of(
            // Depending on the size of the stream either Put Blob or Put Block List will be used.
            // Put Blob will implicitly calculate the MD5 whereas Put Block List won't.
            Arguments.of(DATA.getDefaultDataSize(), null, null, null, null, true, null),
            Arguments.of(DATA.getDefaultDataSize(), "control", "disposition", "encoding", "language", true,
                "type"),
            Arguments.of(6 * Constants.MB, null, null, null, null, false, null),
            Arguments.of(6 * Constants.MB, "control", "disposition", "encoding", "language", true, "type"));
    }

    // Only run these tests in live mode as they use variables that can't be captured.
    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    @ParameterizedTest
    @MethodSource("bufferedUploadMetadataSupplier")
    public void bufferedUploadMetadata(String key1, String value1, String key2, String value2) {
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null) {
            metadata.put(key2, value2);
        }

        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSizeLong(10L)
            .setMaxConcurrency(10);
        Mono<Response<BlockBlobItem>> uploadOperation = blobAsyncClient.uploadWithResponse(
            Flux.just(getRandomData(10)), parallelTransferOptions, null, metadata, null, null);

        StepVerifier.create(uploadOperation.then(blobAsyncClient.getPropertiesWithResponse(null)))
            .assertNext(it -> {
                assertResponseStatusCode(it, 200);
                assertEquals(metadata, it.getValue().getMetadata());
            }).verifyComplete();
    }

    private static Stream<Arguments> bufferedUploadMetadataSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null),
            Arguments.of("foo", "bar", "fizz", "buzz"));
    }

    // Only run these tests in live mode as they use variables that can't be captured.
    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("bufferedUploadTagsSupplier")
    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    public void bufferedUploadTags(String key1, String value1, String key2, String value2) {
        Map<String, String> tags = new HashMap<>();
        if (key1 != null) {
            tags.put(key1, value1);
        }
        if (key2 != null) {
            tags.put(key2, value2);
        }

        ParallelTransferOptions  parallelTransferOptions =
            new ParallelTransferOptions(10, 10, null);
        Mono<Response<BlockBlobItem>> uploadOperation = blobAsyncClient.uploadWithResponse(
            new BlobParallelUploadOptions(Flux.just(getRandomData(10)))
                .setParallelTransferOptions(parallelTransferOptions).setTags(tags));

        StepVerifier.create(uploadOperation.then(blobAsyncClient.getTagsWithResponse(null))).assertNext(it -> {
            assertResponseStatusCode(it, 200);
            assertEquals(tags, it.getValue());
        }).verifyComplete();
    }

    private static Stream<Arguments> bufferedUploadTagsSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null),
            Arguments.of("foo", "bar", "fizz", "buzz"),
            Arguments.of(" +-./:=_  +-./:=_", " +-./:=_", null, null));
    }

    @ParameterizedTest
    @MethodSource("bufferedUploadOptionsSupplier")
    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    public void bufferedUploadOptions(int dataSize, Long singleUploadSize, Long blockSize, int expectedBlockCount) {
        ByteBuffer data = getRandomData(dataSize);

        blobAsyncClient.uploadWithResponse(Flux.just(data),
            new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(singleUploadSize),
            null, null, null, null).block();

        assertEquals(Objects.requireNonNull(blobAsyncClient.getBlockBlobAsyncClient()
            .listBlocks(BlockListType.COMMITTED).block()).getCommittedBlocks().size(), expectedBlockCount);
    }

    @SuppressWarnings("deprecation")
    private static Stream<Arguments> bufferedUploadOptionsSupplier() {
        return Stream.of(
            // Test that the default for singleUploadSize is the maximum
            Arguments.of(BlockBlobAsyncClient.MAX_UPLOAD_BLOB_BYTES - 1, null, null, 0),
            // This also validates the default for blockSize
            Arguments.of(BlockBlobAsyncClient.MAX_UPLOAD_BLOB_BYTES + 1, null, null, (int) Math.ceil(((double) BlockBlobAsyncClient.MAX_UPLOAD_BLOB_BYTES + 1) / (double) BlobClient.BLOB_DEFAULT_UPLOAD_BLOCK_SIZE)/* "". This also validates the default for blockSize*/),
            // Test that singleUploadSize is respected
            Arguments.of(100, 50L, null, 1),
            // Test that blockSize is respected
            Arguments.of(100, 50L, 20L, 5));
    }

    @ParameterizedTest
    @MethodSource("bufferedUploadWithLengthSupplier")
    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    public void bufferedUploadWithLength(int dataSize, Long singleUploadSize, Long blockSize, int expectedBlockCount) {
        Flux<ByteBuffer> data = Flux.just(getRandomData(dataSize));
        BinaryData binaryData = BinaryData.fromFlux(data, (long) dataSize).block();
        BlobParallelUploadOptions parallelUploadOptions = new BlobParallelUploadOptions(binaryData)
            .setParallelTransferOptions(new ParallelTransferOptions()
                .setBlockSizeLong(blockSize)
                .setMaxSingleUploadSizeLong(singleUploadSize));

        blobAsyncClient.uploadWithResponse(parallelUploadOptions).block();

        assertEquals(Objects.requireNonNull(blobAsyncClient.getBlockBlobAsyncClient()
            .listBlocks(BlockListType.COMMITTED).block()).getCommittedBlocks().size(), expectedBlockCount);
    }

    private static Stream<Arguments> bufferedUploadWithLengthSupplier() {
        return Stream.of(Arguments.of(100, 100L, null, 0), // Test that singleUploadSize is respected
            Arguments.of(100, 50L, 20L, 5)); // Test that blockSize is respected
    }
    // Only run these tests in live mode as they use variables that can't be captured.
    @ParameterizedTest
    @MethodSource("bufferedUploadACSupplier")
    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    public void bufferedUploadAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        blockBlobAsyncClient.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize(), true).block();
        match = setupBlobMatchCondition(blockBlobAsyncClient, match);
        leaseID = setupBlobLeaseCondition(blockBlobAsyncClient, leaseID);
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(10L);
        StepVerifier.create(blobAsyncClient.uploadWithResponse(Flux.just(getRandomData(10)),
                parallelTransferOptions, null, null, null, requestConditions))
            .assertNext(it -> assertResponseStatusCode(it, 201))
            .verifyComplete();
    }

    private static Stream<Arguments> bufferedUploadACSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null, null),
            Arguments.of(null, NEW_DATE, null, null, null),
            Arguments.of(OLD_DATE, null, null, null, null),
            Arguments.of(null, null, RECEIVED_ETAG, null, null),
            Arguments.of(null, null, null, GARBAGE_ETAG, null),
            Arguments.of(null, null, null, null, RECEIVED_LEASE_ID));
    }

    // Only run these tests in live mode as they use variables that can't be captured.
    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#fileACFailSupplier")
    public void bufferedUploadACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        blockBlobAsyncClient.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize(), true).block();
        noneMatch = setupBlobMatchCondition(blockBlobAsyncClient, noneMatch);
        leaseID = setupBlobLeaseCondition(blockBlobAsyncClient, leaseID);
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(10L);

        StepVerifier.create(blobAsyncClient.uploadWithResponse(Flux.just(getRandomData(10)), parallelTransferOptions,
                null, null, null, requestConditions))
            .verifyErrorSatisfies(it -> {
                assertInstanceOf(BlobStorageException.class, it);
                BlobStorageException storageException = (BlobStorageException) it;
                assertTrue(storageException.getErrorCode() == BlobErrorCode.CONDITION_NOT_MET
                    || storageException.getErrorCode() == BlobErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION);
            });
    }

    // UploadBufferPool used to lock when the number of failed stage blocks exceeded the maximum number of buffers
    // (discovered when a leaseId was invalid)
    @ParameterizedTest
    @CsvSource(value = {"16,7,2", "16,5,2"})
    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    public void uploadBufferPoolLockThreeOrMoreBuffers(int dataLength, int blockSize, int numBuffers) {
        blockBlobAsyncClient.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize(), true).block();
        String leaseID = setupBlobLeaseCondition(blockBlobAsyncClient, GARBAGE_LEASE_ID);
        BlobRequestConditions requestConditions = new BlobRequestConditions().setLeaseId(leaseID);

        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSizeLong((long) blockSize)
            .setMaxConcurrency(numBuffers);

        StepVerifier.create(blobAsyncClient.uploadWithResponse(Flux.just(getRandomData(dataLength)),
                parallelTransferOptions, null, null, null, requestConditions))
            .verifyErrorSatisfies(it -> assertInstanceOf(BlobStorageException.class, it));
    }

    /*
    def "Upload NRF progress"() {
        setup:
        def data = getRandomData(BlockBlobURL.MAX_UPLOAD_BLOB_BYTES + 1)
        def numBlocks = data.remaining() / BlockBlobURL.MAX_STAGE_BLOCK_BYTES
        long prevCount = 0
        def mockReceiver = Mock(IProgressReceiver)


        when:
        TransferManager.uploadFromNonReplayableFlowable(Flowable.just(data), bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES, 10,
            new TransferManagerUploadToBlockBlobOptions(mockReceiver, null, null, null, 20)).blockingGet()
        data.position(0)

        then:
        // We should receive exactly one notification of the completed progress.
        1 * mockReceiver.reportProgress(data.remaining()) */

    /*
    We should receive at least one notification reporting an intermediary value per block, but possibly more
    notifications will be received depending on the implementation. We specify numBlocks - 1 because the last block
    will be the total size as above. Finally, we assert that the number reported monotonically increases.
     */
    /*(numBlocks - 1.._) * mockReceiver.reportProgress(!data.remaining()) >> { long bytesTransferred ->
        if (!(bytesTransferred > prevCount)) {
            throw new IllegalArgumentException("Reported progress should monotonically increase")
        } else {
            prevCount = bytesTransferred
        }
    }

    // We should receive no notifications that report more progress than the size of the file.
    0 * mockReceiver.reportProgress({ it > data.remaining() })
    notThrown(IllegalArgumentException)
    }*/

    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    @Test
    public void bufferedUploadNetworkError() throws MalformedURLException {
        /*
         This test uses a Flowable that does not allow multiple subscriptions and therefore ensures that we are
         buffering properly to allow for retries even given this source behavior.
         */
        blockBlobAsyncClient.upload(Flux.just(DATA.getDefaultData()), DATA.getDefaultDataSize(), true).block();

        // Mock a response that will always be retried.
        HttpResponse mockHttpResponse = getStubResponse(500, new HttpRequest(HttpMethod.PUT,
            new URL("https://www.fake.com")));

        // Mock a policy that will always then check that the data is still the same and return a retryable error.
        ByteBuffer localData = DATA.getDefaultData();
        HttpPipelinePolicy mockPolicy = (context, next) -> collectBytesInBuffer(context.getHttpRequest().getBody())
            .map(localData::equals)
            .flatMap(it -> it ? Mono.just(mockHttpResponse) : Mono.error(new IllegalArgumentException()));

        // Build the pipeline
        BlobAsyncClient blobAsyncClient = new BlobServiceClientBuilder()
            .credential(ENVIRONMENT.getPrimaryAccount().getCredential())
            .endpoint(ENVIRONMENT.getPrimaryAccount().getBlobEndpoint())
            .retryOptions(new RequestRetryOptions((RetryPolicyType) null, 3, (Integer) null, 500L, 1500L, null))
            .addPolicy(mockPolicy).buildAsyncClient()
            .getBlobContainerAsyncClient(generateContainerName()).getBlobAsyncClient(generateBlobName());

        // Try to upload the flowable, which will hit a retry. A normal upload would throw, but buffering prevents that.
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(1024L)
            .setMaxConcurrency(4);
        // TODO: It could be that duplicates aren't getting made in the retry policy? Or before the retry policy?

        // A second subscription to a download stream will
        StepVerifier.create(blobAsyncClient.upload(blockBlobAsyncClient.downloadStream(), parallelTransferOptions,
            true)).verifyErrorSatisfies(it -> {
                assertInstanceOf(BlobStorageException.class, it);
                assertEquals(500, ((BlobStorageException) it).getStatusCode());
            });
    }

    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    @Test
    public void bufferedUploadDefaultNoOverwrite() {
        StepVerifier.create(blobAsyncClient.upload(DATA.getDefaultFlux(), null))
            .verifyError(IllegalArgumentException.class);
    }

    @Test
    public void uploadBinaryDataNoOverwrite() {
        StepVerifier.create(blobAsyncClient.upload(DATA.getDefaultBinaryData()))
            .verifyError(IllegalArgumentException.class);
    }

    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    @Test
    public void bufferedUploadNoOverwriteInterrupted() throws IOException {
        File smallFile = getRandomFile(50);
        smallFile.deleteOnExit();
        createdFiles.add(smallFile);

        /*
         * Setup the data stream to trigger a small upload upon subscription. This will happen once the upload method
         * has verified whether a blob with the given name already exists, so this will trigger once uploading begins.
         */
        Flux<ByteBuffer> data = Flux.just(getRandomData(Constants.MB)).repeat(257)
            .doOnSubscribe(it -> blobAsyncClient.uploadFromFile(smallFile.toPath().toString()).subscribe());
        blobAsyncClient = ccAsync.getBlobAsyncClient(generateBlobName());

        StepVerifier.create(blobAsyncClient.upload(data, null)).verifyErrorSatisfies(it -> {
            assertInstanceOf(BlobStorageException.class, it);
            assertEquals(BlobErrorCode.BLOB_ALREADY_EXISTS, ((BlobStorageException) it).getErrorCode());
        });
    }

    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    @Test
    public void bufferedUploadWithSpecifiedLength() {
        Flux<ByteBuffer> fluxData = Flux.just(getRandomData(DATA.getDefaultDataSize()));
        BinaryData binaryData = BinaryData.fromFlux(fluxData, DATA.getDefaultDataSizeLong()).block();
        BlobParallelUploadOptions parallelUploadOptions = new BlobParallelUploadOptions(binaryData);
        StepVerifier.create(blobAsyncClient.uploadWithResponse(parallelUploadOptions))
            .assertNext(it -> assertNotNull(it.getValue().getETag())).verifyComplete();
    }

    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    @Test
    public void bufferedUploadOverwrite() throws IOException {
        File file = getRandomFile(50);
        file.deleteOnExit();
        createdFiles.add(file);

        blobClient.uploadFromFile(file.toPath().toString(), true);
        StepVerifier.create(blobAsyncClient.uploadFromFile(getRandomFile(50).toPath().toString(), true))
            .verifyComplete();
    }

    @Test
    public void bufferedUploadNonMarkableStream() throws IOException {
        File file = getRandomFile(10);
        file.deleteOnExit();
        createdFiles.add(file);

        FileInputStream fileStream = new FileInputStream(file);
        File outFile = getRandomFile(10);
        createdFiles.add(outFile);
        outFile.deleteOnExit();
        Files.deleteIfExists(outFile.toPath());

        blobClient.upload(fileStream, file.length(), true);
        blobClient.downloadToFile(outFile.toPath().toString(), true);
        compareFiles(file, outFile, 0, file.length());
    }

    @Test
    public void getContainerName() {
        assertEquals(containerName, blockBlobClient.getContainerName());
    }

    @Test
    public void getBlockBlobName() {
        assertEquals(blobName, blockBlobClient.getBlobName());
    }

    @ParameterizedTest
    @MethodSource("getBlobNameAndBuildClientSupplier")
    public void getBlobNameAndBuildClient(String originalBlobName, String finalBlobName) {
        BlobClient client = cc.getBlobClient(originalBlobName);
        BlockBlobClient blockClient = cc.getBlobClient(client.getBlobName()).getBlockBlobClient();

        assertEquals(blockClient.getBlobName(), finalBlobName);
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
        URL endpoint = BlobUrlParts.parse(blockBlobClient.getBlobUrl()).setScheme("http").toUrl();
        SpecializedBlobClientBuilder builder = new SpecializedBlobClientBuilder()
            .customerProvidedKey(new CustomerProvidedKey(Base64.getEncoder()
                .encodeToString(getRandomByteArray(256))))
            .endpoint(endpoint.toString());

        assertThrows(IllegalArgumentException.class, builder::buildBlockBlobClient);
    }

    @Test
    public void builderBearerTokenValidation() {
        URL endpoint = BlobUrlParts.parse(blockBlobClient.getBlobUrl()).setScheme("http").toUrl();
        SpecializedBlobClientBuilder builder = new SpecializedBlobClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint.toString());

        assertThrows(IllegalArgumentException.class, builder::buildBlockBlobClient);
    }

    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials
    // and auth would fail because we changed a signed header.
    @Test
    public void perCallPolicy() {
        BlockBlobClient specialBlob = getSpecializedBuilder(blockBlobClient.getBlobUrl())
            .addPolicy(getPerCallVersionPolicy())
            .buildBlockBlobClient();

        HttpHeaders headers = specialBlob.getPropertiesWithResponse(null, null, null).getHeaders();
        assertEquals("2017-11-09", headers.getValue(X_MS_VERSION));
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20200408ServiceVersion")
    @Test
    public void uploadFromUrlMin() {
        BlobClient sourceBlob = primaryBlobServiceClient.getBlobContainerClient(containerName)
            .getBlobClient(generateBlobName());
        sourceBlob.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        String sas = sourceBlob.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));
        if (blockBlobClient.exists()) {
            blockBlobClient.delete();
        }

        BlockBlobItem blockBlobItem = blockBlobClient.uploadFromUrl(sourceBlob.getBlobUrl() + "?" + sas);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        blockBlobClient.download(os);
        assertNotNull(blockBlobItem);
        assertNotNull(blockBlobItem.getETag());
        assertNotNull(blockBlobItem.getLastModified());
        TestUtils.assertArraysEqual(DATA.getDefaultBytes(), os.toByteArray());
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20200408ServiceVersion")
    @Test
    public void uploadFromUrlOverwrite() {
        BlobClient sourceBlob = primaryBlobServiceClient.getBlobContainerClient(containerName)
            .getBlobClient(generateBlobName());
        sourceBlob.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        String sas = sourceBlob.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));
        blockBlobClient.upload(new ByteArrayInputStream(new byte[0]), 0, true);

        BlockBlobItem blockBlobItem = blockBlobClient.uploadFromUrl(sourceBlob.getBlobUrl() + "?" + sas, true);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        blockBlobClient.download(os);
        assertNotNull(blockBlobItem);
        assertNotNull(blockBlobItem.getETag());
        assertNotNull(blockBlobItem.getLastModified());
        TestUtils.assertArraysEqual(DATA.getDefaultBytes(), os.toByteArray());
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20200408ServiceVersion")
    @Test
    public void uploadFromUrlOverwriteFailsOnExistingBlob() {
        BlobClient sourceBlob = primaryBlobServiceClient.getBlobContainerClient(containerName)
            .getBlobClient(generateBlobName());
        sourceBlob.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        String sas = sourceBlob.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));
        blockBlobClient.upload(new ByteArrayInputStream(new byte[0]), 0, true);

        BlobStorageException e = assertThrows(BlobStorageException.class,
            () -> blockBlobClient.uploadFromUrl(sourceBlob.getBlobUrl() + "?" + sas, false));
        assertEquals(BlobErrorCode.BLOB_ALREADY_EXISTS, e.getErrorCode());

        BlobStorageException e2 = assertThrows(BlobStorageException.class,
            () -> blockBlobClient.uploadFromUrl(sourceBlob.getBlobUrl() + "?" + sas));
        assertEquals(BlobErrorCode.BLOB_ALREADY_EXISTS, e2.getErrorCode());
    }

    @SuppressWarnings("deprecation")
    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20200408ServiceVersion")
    @Test
    public void uploadFromUrlMax() throws NoSuchAlgorithmException {
        BlobClient sourceBlob = primaryBlobServiceClient.getBlobContainerClient(containerName)
            .getBlobClient(generateBlobName());
        sourceBlob.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        sourceBlob.setHttpHeaders(new BlobHttpHeaders().setContentLanguage("en-GB"));
        byte[] sourceBlobMD5 = MessageDigest.getInstance("MD5").digest(DATA.getDefaultBytes());
        BlobProperties sourceProperties = sourceBlob.getProperties();
        String sas = sourceBlob.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));
        blockBlobClient.upload(new ByteArrayInputStream(new byte[0]), 0, true);
        BlobProperties destinationPropertiesBefore = blockBlobClient.getProperties();

        BlobUploadFromUrlOptions options = new BlobUploadFromUrlOptions(sourceBlob.getBlobUrl() + "?" + sas)
            .setContentMd5(sourceBlobMD5)
            .setCopySourceBlobProperties(true)
            .setDestinationRequestConditions(new BlobRequestConditions()
                .setIfMatch(destinationPropertiesBefore.getETag()))
            .setSourceRequestConditions(new BlobRequestConditions().setIfMatch(sourceProperties.getETag()))
            .setHeaders(new BlobHttpHeaders().setContentType("text"))
            .setTier(AccessTier.COOL);
        Response<BlockBlobItem> response = blockBlobClient.uploadFromUrlWithResponse(options, null, null);
        BlobProperties destinationProperties = blobClient.getProperties();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        blockBlobClient.download(os);

        assertNotNull(response);
        assertNotNull(response.getRequest());
        assertNotNull(response.getHeaders());
        BlockBlobItem blockBlobItem = response.getValue();
        assertNotNull(blockBlobItem);
        assertNotNull(blockBlobItem.getETag());
        assertNotNull(blockBlobItem.getLastModified());
        TestUtils.assertArraysEqual(DATA.getDefaultBytes(), os.toByteArray());
        assertEquals("en-GB", destinationProperties.getContentLanguage());
        assertEquals("text", destinationProperties.getContentType());
        assertEquals(AccessTier.COOL, destinationProperties.getAccessTier());
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20200408ServiceVersion")
    @Test
    public void uploadFromWithInvalidSourceMD5() throws NoSuchAlgorithmException {
        BlobClient sourceBlob = primaryBlobServiceClient.getBlobContainerClient(containerName)
            .getBlobClient(generateBlobName());
        sourceBlob.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        byte[] sourceBlobMD5 = MessageDigest.getInstance("MD5")
            .digest("garbage".getBytes(StandardCharsets.UTF_8));
        String sas = sourceBlob.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));
        blockBlobClient.upload(new ByteArrayInputStream(new byte[0]), 0, true);

        BlobUploadFromUrlOptions options = new BlobUploadFromUrlOptions(sourceBlob.getBlobUrl() + "?" + sas)
            .setContentMd5(sourceBlobMD5);
        BlobStorageException e = assertThrows(BlobStorageException.class,
            () -> blockBlobClient.uploadFromUrlWithResponse(options, null, null));

        assertEquals(e.getErrorCode(), BlobErrorCode.MD5MISMATCH);
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20200408ServiceVersion")
    @ParameterizedTest
    @MethodSource("uploadFromUrlSourceRequestConditionsSupplier")
    public void uploadFromUrlSourceRequestConditions(BlobRequestConditions requestConditions, BlobErrorCode errorCode) {
        BlobClient sourceBlob = primaryBlobServiceClient.getBlobContainerClient(containerName)
            .getBlobClient(generateBlobName());
        sourceBlob.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        String sas = sourceBlob.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));
        blockBlobClient.upload(new ByteArrayInputStream(new byte[0]), 0, true);

        BlobUploadFromUrlOptions options = new BlobUploadFromUrlOptions(sourceBlob.getBlobUrl() + "?" + sas)
            .setSourceRequestConditions(requestConditions);

        BlobStorageException e = assertThrows(BlobStorageException.class,
            () -> blockBlobClient.uploadFromUrlWithResponse(options, null, null));
        assertEquals(e.getErrorCode(), errorCode);
    }

    private static Stream<Arguments> uploadFromUrlSourceRequestConditionsSupplier() {
        return Stream.of(
            Arguments.of(new BlobRequestConditions().setIfMatch("dummy"), BlobErrorCode.SOURCE_CONDITION_NOT_MET),
            Arguments.of(new BlobRequestConditions().setIfModifiedSince(OffsetDateTime.now().plusSeconds(10)),
                BlobErrorCode.CANNOT_VERIFY_COPY_SOURCE),
            Arguments.of(new BlobRequestConditions().setIfUnmodifiedSince(OffsetDateTime.now().minusDays(1)),
                BlobErrorCode.CANNOT_VERIFY_COPY_SOURCE)
        );
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20200408ServiceVersion")
    @ParameterizedTest
    @MethodSource("uploadFromUrlDestinationRequestConditionsSupplier")
    public void uploadFromUrlDestinationRequestConditions(BlobRequestConditions requestConditions,
        BlobErrorCode errorCode) {
        BlobClient sourceBlob = primaryBlobServiceClient.getBlobContainerClient(containerName)
            .getBlobClient(generateBlobName());
        sourceBlob.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        String sas = sourceBlob.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));
        blockBlobClient.upload(new ByteArrayInputStream(new byte[0]), 0, true);
        if (requestConditions.getLeaseId() != null) {
            createLeaseClient(blobClient).acquireLease(60);
        }

        BlobUploadFromUrlOptions options = new BlobUploadFromUrlOptions(sourceBlob.getBlobUrl() + "?" + sas)
            .setDestinationRequestConditions(requestConditions);
        BlobStorageException e = assertThrows(BlobStorageException.class, () ->
            blockBlobClient.uploadFromUrlWithResponse(options, null, null));
        assertEquals(e.getErrorCode(), errorCode);
    }

    private static Stream<Arguments> uploadFromUrlDestinationRequestConditionsSupplier() {
        return Stream.of(
            Arguments.of(new BlobRequestConditions().setIfMatch("dummy"), BlobErrorCode.TARGET_CONDITION_NOT_MET),
            Arguments.of(new BlobRequestConditions().setIfNoneMatch("*"), BlobErrorCode.BLOB_ALREADY_EXISTS),
            Arguments.of(new BlobRequestConditions().setIfModifiedSince(OffsetDateTime.now().plusDays(10)), BlobErrorCode.CONDITION_NOT_MET),
            Arguments.of(new BlobRequestConditions().setIfUnmodifiedSince(OffsetDateTime.now().minusDays(1)), BlobErrorCode.CONDITION_NOT_MET),
            Arguments.of(new BlobRequestConditions().setLeaseId("9260fd2d-34c1-42b5-9217-8fb7c6484bfb"), BlobErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION));
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20210608ServiceVersion")
    @ParameterizedTest
    @MethodSource("uploadFromUrlCopySourceTagsSupplier")
    public void uploadFromUrlCopySourceTags(BlobCopySourceTagsMode mode) {
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null);
        Map<String, String> sourceTags = Collections.singletonMap("foo", "bar");
        Map<String, String> destTags = Collections.singletonMap("fizz", "buzz");
        blockBlobClient.setTags(sourceTags);

        String sas = blockBlobClient.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobSasPermission().setTagsPermission(true).setReadPermission(true)));

        BlobClient bc2 = cc.getBlobClient(generateBlobName());

        BlobCopyFromUrlOptions options = new BlobCopyFromUrlOptions(blockBlobClient.getBlobUrl() + "?" + sas)
            .setCopySourceTagsMode(mode);
        if (BlobCopySourceTagsMode.REPLACE == mode) {
            options.setTags(destTags);
        }

        bc2.copyFromUrlWithResponse(options, null, null);
        Map<String, String> receivedTags = bc2.getTags();

        if (BlobCopySourceTagsMode.REPLACE == mode) {
            assertEquals(receivedTags, destTags);
        } else {
            assertEquals(receivedTags, sourceTags);
        }
    }

    private static Stream<Arguments> uploadFromUrlCopySourceTagsSupplier() {
        return Stream.of(
            Arguments.of(BlobCopySourceTagsMode.COPY),
            Arguments.of(BlobCopySourceTagsMode.REPLACE)
        );
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20211202ServiceVersion")
    @Test
    public void uploadFromUrlAccessTierCold() {
        BlobClient sourceBlob = primaryBlobServiceClient.getBlobContainerClient(containerName)
            .getBlobClient(generateBlobName());
        sourceBlob.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        String sas = sourceBlob.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));
        if (blockBlobClient.exists()) {
            blockBlobClient.delete();
        }

        BlobUploadFromUrlOptions uploadOptions = new BlobUploadFromUrlOptions(sourceBlob.getBlobUrl() + "?" + sas)
            .setTier(AccessTier.COLD);
        blockBlobClient.uploadFromUrlWithResponse(uploadOptions, null, null);
        BlobProperties properties = blockBlobClient.getProperties();

        assertEquals(properties.getAccessTier(), AccessTier.COLD);
    }

    @Test
    public void blockBlobItemNullHeaders() {
        HttpHeaders headers = new HttpHeaders();
        BlockBlobsPutBlobFromUrlHeaders hd = new BlockBlobsPutBlobFromUrlHeaders(headers);

        BlockBlobItem blockBlobItem = new BlockBlobItem(
            hd.getETag(),
            hd.getLastModified(),
            hd.getContentMD5(),
            hd.isXMsRequestServerEncrypted(),
            hd.getXMsEncryptionKeySha256(),
            hd.getXMsEncryptionScope(),
            hd.getXMsVersionId());

        assertNull(blockBlobItem.getETag());
        assertNull(blockBlobItem.getLastModified());
        assertNull(blockBlobItem.getContentMd5());
        assertNull(blockBlobItem.isServerEncrypted());
        assertNull(blockBlobItem.getEncryptionKeySha256());
        assertNull(blockBlobItem.getEncryptionScope());
        assertNull(blockBlobItem.getVersionId());
    }

    @Test
    public void defaultAudience() {
        BlockBlobClient aadBlob = getSpecializedBuilderWithTokenCredential(blockBlobClient.getBlobUrl())
            .audience(null)
            .buildBlockBlobClient();

        assertTrue(aadBlob.exists());
    }

    @Test
    public void storageAccountAudience() {
        BlockBlobClient aadBlob = getSpecializedBuilderWithTokenCredential(blockBlobClient.getBlobUrl())
            .audience(BlobAudience.createBlobServiceAccountAudience(cc.getAccountName()))
            .buildBlockBlobClient();

        assertTrue(aadBlob.exists());
    }

    @Test
    public void audienceError() {
        BlockBlobClient aadBlob = instrument(new SpecializedBlobClientBuilder()
            .endpoint(blockBlobClient.getBlobUrl())
            .credential(new MockTokenCredential())
            .audience(BlobAudience.createBlobServiceAccountAudience("badAudience")))
            .buildBlockBlobClient();

        BlobStorageException e = assertThrows(BlobStorageException.class, () -> aadBlob.exists());
        assertTrue(e.getErrorCode() == BlobErrorCode.INVALID_AUTHENTICATION_INFO);
    }

    @Test
    public void audienceFromString() {
        String url = String.format("https://%s.blob.core.windows.net/", cc.getAccountName());
        BlobAudience audience = BlobAudience.fromString(url);

        BlockBlobClient aadBlob = getSpecializedBuilderWithTokenCredential(blockBlobClient.getBlobUrl())
            .audience(audience)
            .buildBlockBlobClient();

        assertTrue(aadBlob.exists());
    }
}
