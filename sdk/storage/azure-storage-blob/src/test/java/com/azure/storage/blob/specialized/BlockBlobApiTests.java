// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.exception.UnexpectedLengthException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.Response;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.blob.BlobUrlParts;
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
import com.azure.storage.blob.options.BlobCopyFromUrlOptions;
import com.azure.storage.blob.options.BlobGetTagsOptions;
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
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import com.azure.storage.common.test.shared.http.WireTapHttpClient;
import com.azure.storage.common.test.shared.policy.RequestAssertionPolicy;
import com.azure.storage.common.test.shared.policy.TransientFailureInjectingHttpPipelinePolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("deprecation") // Using old APIs for testing purposes
public class BlockBlobApiTests extends BlobTestBase {
    private BlockBlobClient blockBlobClient;
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
        BlockBlobClient clientWithFailure = getBlobClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            blobClient.getBlobUrl(), new TransientFailureInjectingHttpPipelinePolicy()).getBlockBlobClient();

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
        BlockBlobClient clientWithFailure = getBlobClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            blobClient.getBlobUrl(), new TransientFailureInjectingHttpPipelinePolicy()).getBlockBlobClient();

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
        BlockBlobClient bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        String blockID = getBlockID();

        String sas = blockBlobClient.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));

        HttpHeaders headers = bu2.stageBlockFromUrlWithResponse(blockID, blockBlobClient.getBlobUrl() + "?" + sas, null, null, null,
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

    /*@RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2024-08-04")
    @Test
    public void stageBlockFromUrlSourceErrorAndStatusCode() {
        BlockBlobClient destBlob = cc.getBlobClient(generateBlobName()).getBlockBlobClient();

        String blockID = getBlockID();

        BlobStorageException e = assertThrows(BlobStorageException.class, () -> destBlob.stageBlockFromUrl(blockID, blockBlobClient.getBlobUrl(), new BlobRange(0, (long) PageBlobClient.PAGE_BYTES)));

        assertTrue(e.getStatusCode() == 409);
        assertTrue(e.getServiceMessage().contains("PublicAccessNotPermitted"));
        assertTrue(e.getServiceMessage().contains("Public access is not permitted on this storage account."));
    }*/

    @Test
    public void stageBlockFromUrlMin() {
        BlockBlobClient bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        String blockID = getBlockID();

        String sas = blockBlobClient.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));

        assertResponseStatusCode(bu2.stageBlockFromUrlWithResponse(blockID, blockBlobClient.getBlobUrl() + "?" + sas, null, null,
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
        BlockBlobClient destURL = cc.getBlobClient(generateBlobName()).getBlockBlobClient();

        String sas = blockBlobClient.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));

        destURL.stageBlockFromUrl(getBlockID(), blockBlobClient.getBlobUrl() + "?" + sas, new BlobRange(2L, 3L));
        BlockList blockList = destURL.listBlocks(BlockListType.UNCOMMITTED);

        assertEquals(blockList.getCommittedBlocks().size(), 0);
        assertEquals(blockList.getUncommittedBlocks().size(), 1);
    }

    @Test
    public void stageBlockFromURLMD5() {
        BlockBlobClient destURL = cc.getBlobClient(generateBlobName()).getBlockBlobClient();

        String sas = blockBlobClient.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));

        assertDoesNotThrow(() -> destURL.stageBlockFromUrlWithResponse(getBlockID(), blockBlobClient.getBlobUrl() + "?" + sas,
            null, MessageDigest.getInstance("MD5").digest(DATA.getDefaultBytes()), null,
            null, null, null));
    }

    @Test
    public void stageBlockFromURLMD5Fail() {
        BlockBlobClient destURL = cc.getBlobClient(generateBlobName()).getBlockBlobClient();

        String sas = blockBlobClient.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));

        assertThrows(BlobStorageException.class, () -> destURL.stageBlockFromUrlWithResponse(getBlockID(),
            blockBlobClient.getBlobUrl() + "?" + sas, null, "garbage".getBytes(), null, null,
            null, null));
    }

    @Test
    public void stageBlockFromURLLease() {
        String sas = blockBlobClient.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));

        assertDoesNotThrow(() -> blockBlobClient.stageBlockFromUrlWithResponse(getBlockID(),
            blockBlobClient.getBlobUrl() + "?" + sas, null, null,
            setupBlobLeaseCondition(blockBlobClient, RECEIVED_LEASE_ID), null, null,
            null));
    }

    @Test
    public void stageBlockFromURLLeaseFail() {
        String sas = blockBlobClient.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));

        assertThrows(BlobStorageException.class, () -> blockBlobClient.stageBlockFromUrlWithResponse(getBlockID(),
            blockBlobClient.getBlobUrl() + "?" + sas, null, null, "garbage", null,
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
        String blockID = getBlockID();

        BlockBlobClient sourceURL = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        String sas = sourceURL.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));
        sourceURL.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());

        sourceIfMatch = setupBlobMatchCondition(sourceURL, sourceIfMatch);
        BlobRequestConditions smac = new BlobRequestConditions()
            .setIfModifiedSince(sourceIfModifiedSince)
            .setIfUnmodifiedSince(sourceIfUnmodifiedSince)
            .setIfMatch(sourceIfMatch)
            .setIfNoneMatch(sourceIfNoneMatch);

        assertResponseStatusCode(blockBlobClient.stageBlockFromUrlWithResponse(blockID, sourceURL.getBlobUrl() + "?" + sas,
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
        String blockID = getBlockID();

        BlockBlobClient sourceURL = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        String sas = sourceURL.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));
        sourceURL.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());

        BlobRequestConditions smac = new BlobRequestConditions()
            .setIfModifiedSince(sourceIfModifiedSince)
            .setIfUnmodifiedSince(sourceIfUnmodifiedSince)
            .setIfMatch(sourceIfMatch)
            .setIfNoneMatch(setupBlobMatchCondition(sourceURL, sourceIfNoneMatch));

        assertThrows(BlobStorageException.class, () ->
            blockBlobClient.stageBlockFromUrlWithResponse(blockID, sourceURL.getBlobUrl() + "?" + sas, null,
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

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
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

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
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

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
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

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-12-02")
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

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void getBlockListTags() {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        blockBlobClient.setTags(t);

        assertDoesNotThrow(() -> blockBlobClient.listBlocksWithResponse(
            new BlockBlobListBlocksOptions(BlockListType.ALL).setIfTagsMatch("\"foo\" = 'bar'"), null,
            Context.NONE));
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
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
    @ParameterizedTest
    @MethodSource("stageBlockDoesNotTransformReplayableBinaryDataSupplier")
    public void uploadDoesNotTransformReplayableBinaryData(BinaryData binaryData) {
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

    /* Upload From File Tests: Need to run on liveMode only since blockBlob wil generate a `UUID.randomUUID()`
       for getBlockID that will change every time test is run
     */
    @LiveOnly
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

    @LiveOnly
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

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @LiveOnly
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

    @LiveOnly
    @Test
    public void uploadFromFileDefaultNoOverwrite() throws IOException {
        File file = getRandomFile(50);
        file.deleteOnExit();
        createdFiles.add(file);

        BlobStorageException e = assertThrows(BlobStorageException.class, () ->
            blobClient.uploadFromFile(file.toPath().toString()));
        assertEquals(e.getErrorCode(), BlobErrorCode.BLOB_ALREADY_EXISTS);
    }

    @LiveOnly
    @Test
    public void uploadFromFileOverwrite() throws IOException {
        File file = getRandomFile(50);
        file.deleteOnExit();
        createdFiles.add(file);

        blobClient.uploadFromFile(file.toPath().toString(), true);
    }

    @ParameterizedTest
    @MethodSource("uploadFromFileOptionsSupplier")
    @LiveOnly
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

    @LiveOnly
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

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
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

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
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

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
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

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-12-02")
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
        BlockBlobClient clientWithFailure = getBlobClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            blobClient.getBlobUrl(), new TransientFailureInjectingHttpPipelinePolicy()).getBlockBlobClient();

        byte[] data = getRandomByteArray(10);
        clientWithFailure.upload(new ByteArrayInputStream(data), data.length, true);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        blobClient.downloadStream(os);
        TestUtils.assertArraysEqual(os.toByteArray(), data);
    }

    @ParameterizedTest
    @MethodSource("bufferedUploadSyncHandlePathingWithTransientFailureSupplier")
    @LiveOnly
    public void bufferedUploadSyncHandlePathingWithTransientFailure(int dataSize, int blockCount) {
        /*
        This test ensures that although we no longer mark and reset the source stream for buffered upload, it still
        supports retries in all cases for the sync client.
         */
        BlobClient clientWithFailure = getBlobClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            blobClient.getBlobUrl(), new TransientFailureInjectingHttpPipelinePolicy());

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



    @LiveOnly
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
            Arguments.of("blobName", "blobName"),
            Arguments.of("dir1/a%20b.txt", "dir1/a%20b.txt"),
            Arguments.of("path/to]a blob", "path/to]a blob"),
            Arguments.of("path%2Fto%5Da%20blob", "path%2Fto%5Da%20blob"),
            Arguments.of("斑點", "斑點"),
            Arguments.of("%E6%96%91%E9%BB%9E", "%E6%96%91%E9%BB%9E"),
            Arguments.of("斑點", "斑點"));
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

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-04-08")
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

    /*@RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2024-08-04")
    @Test
    public void uploadFromUrlSourceErrorAndStatusCode() {
        BlockBlobClient destBlob = cc.getBlobClient(generateBlobName()).getBlockBlobClient();

        BlobStorageException e = assertThrows(BlobStorageException.class, () -> destBlob.uploadFromUrl(blockBlobClient.getBlobUrl()));

        assertTrue(e.getStatusCode() == 409);
        assertTrue(e.getServiceMessage().contains("PublicAccessNotPermitted"));
        assertTrue(e.getServiceMessage().contains("Public access is not permitted on this storage account."));
    }*/

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-04-08")
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

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-04-08")
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
    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-04-08")
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

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-04-08")
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

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-04-08")
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
            Arguments.of(new BlobRequestConditions().setIfModifiedSince(OffsetDateTime.now().plusSeconds(20)),
                BlobErrorCode.CANNOT_VERIFY_COPY_SOURCE),
            Arguments.of(new BlobRequestConditions().setIfUnmodifiedSince(OffsetDateTime.now().minusDays(1)),
                BlobErrorCode.CANNOT_VERIFY_COPY_SOURCE)
        );
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-04-08")
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

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-06-08")
    @ParameterizedTest
    @MethodSource("uploadFromUrlCopySourceTagsSupplier")
    public void uploadFromUrlCopySourceTags(BlobCopySourceTagsMode mode) {
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

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-12-02")
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

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2024-08-04")
    @LiveOnly
    @Test
    /* This test tests if the bearer challenge is working properly. A bad audience is passed in, the service returns
    the default audience, and the request gets retried with this default audience, making the call function as expected.
     */
    public void audienceErrorBearerChallengeRetry() {
        BlockBlobClient aadBlob = getSpecializedBuilderWithTokenCredential(blockBlobClient.getBlobUrl())
            .audience(BlobAudience.createBlobServiceAccountAudience("badAudience"))
            .buildBlockBlobClient();

        assertNotNull(aadBlob.getProperties());
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
