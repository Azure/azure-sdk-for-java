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
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.BinaryData;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.ProgressListener;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.blob.ProgressReceiver;
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
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import com.azure.storage.common.test.shared.http.WireTapHttpClient;
import com.azure.storage.common.test.shared.policy.RequestAssertionPolicy;
import com.azure.storage.common.test.shared.policy.TransientFailureInjectingHttpPipelinePolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlockBlobAsyncApiTests  extends BlobTestBase {

    private BlockBlobAsyncClient blockBlobAsyncClient;
    private BlobAsyncClient blobAsyncClient;
    private String blobName;
    private final List<File> createdFiles = new ArrayList<>();

    @BeforeEach
    public void setup() {
        blobName = generateBlobName();
        blobAsyncClient = ccAsync.getBlobAsyncClient(blobName);
        blockBlobAsyncClient = blobAsyncClient.getBlockBlobAsyncClient();
        blockBlobAsyncClient.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize(), true).block();
    }

    @AfterEach
    public void cleanup() {
        createdFiles.forEach(File::delete);
    }

    @Test
    public void stageBlock() {
        StepVerifier.create(blockBlobAsyncClient.stageBlockWithResponse(getBlockID(), DATA.getDefaultFlux(),
            DATA.getDefaultDataSize(), null, null))
            .assertNext(r -> {
                HttpHeaders headers = r.getHeaders();

                assertResponseStatusCode(r, 201);
                assertNotNull(headers.getValue(X_MS_CONTENT_CRC64));
                assertNotNull(headers.getValue(X_MS_REQUEST_ID));
                assertNotNull(headers.getValue(X_MS_VERSION));
                assertNotNull(headers.getValue(HttpHeaderName.DATE));
                assertTrue(Boolean.parseBoolean(headers.getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
            })
            .verifyComplete();
    }

    @Test
    public void stageBlockWithBinaryDataAsync() {
        BinaryData.fromFlux(DATA.getDefaultFlux(), (long) DATA.getDefaultDataSize(), false)
            .flatMap(flux -> {
                List<BinaryData> binaryDataList = Arrays.asList(
                    BinaryData.fromBytes(DATA.getDefaultBytes()),
                    BinaryData.fromString(DATA.getDefaultText()),
                    BinaryData.fromFile(DATA.getDefaultFile()),
                    flux,
                    BinaryData.fromStream(DATA.getDefaultInputStream(), (long) DATA.getDefaultDataSize()));

                for (BinaryData binaryData : binaryDataList) {
                    StepVerifier.create(blockBlobAsyncClient.stageBlockWithResponse(new BlockBlobStageBlockOptions(getBlockID(),
                            binaryData)))
                        .assertNext(it -> {
                            HttpHeaders headers = it.getHeaders();
                            assertResponseStatusCode(it, 201);
                            assertNotNull(headers.getValue(X_MS_CONTENT_CRC64));
                            assertNotNull(headers.getValue(X_MS_REQUEST_ID));
                            assertNotNull(headers.getValue(X_MS_VERSION));
                            assertNotNull(headers.getValue(HttpHeaderName.DATE));
                            assertTrue(Boolean.parseBoolean(headers.getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
                        }).verifyComplete();
                }

                return Mono.empty();
            });
    }

    @Test
    public void stageBlockMin() {
        assertAsyncResponseStatusCode(blockBlobAsyncClient.stageBlockWithResponse(getBlockID(), DATA.getDefaultFlux(),
            DATA.getDefaultDataSize(), null, null), 201);

        StepVerifier.create(blockBlobAsyncClient.listBlocks(BlockListType.ALL))
            .assertNext(r -> assertEquals(1, r.getUncommittedBlocks().size()))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("stageBlockMinwithBinaryDataSupplier")
    public void stageBlockMinWithBinaryData(BinaryData binaryData) {
        assertAsyncResponseStatusCode(blockBlobAsyncClient.stageBlockWithResponse(new BlockBlobStageBlockOptions(
            getBlockID(), binaryData)), 201);

        StepVerifier.create(blockBlobAsyncClient.listBlocks(BlockListType.ALL))
            .assertNext(r -> assertEquals(1, r.getUncommittedBlocks().size()))
            .verifyComplete();
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
        Mono<Response<Void>> response = BinaryData.fromFlux(DATA.getDefaultFlux(), DATA.getDefaultDataSizeLong(),
            false).flatMap(r -> blockBlobAsyncClient.stageBlockWithResponse(new BlockBlobStageBlockOptions(
                getBlockID(), r)));

        assertAsyncResponseStatusCode(response, 201);

        StepVerifier.create(blockBlobAsyncClient.listBlocks(BlockListType.ALL))
            .assertNext(r -> assertEquals(1, r.getUncommittedBlocks().size()))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("stageBlockDoesNotTransformReplayableBinaryDataSupplier")
    public void stageBlockDoesNotTransformReplayableBinaryData(BinaryData binaryData) {
        WireTapHttpClient wireTap = new WireTapHttpClient(getHttpClient());
        BlockBlobAsyncClient wireTapClient = getSpecializedBuilder(ENVIRONMENT.getPrimaryAccount().getCredential(),
            blockBlobAsyncClient.getBlobUrl())
            .httpClient(wireTap)
            .buildBlockBlobAsyncClient();

        assertAsyncResponseStatusCode(wireTapClient.stageBlockWithResponse(new BlockBlobStageBlockOptions(getBlockID(),
            binaryData)), 201);

        StepVerifier.create(blockBlobAsyncClient.listBlocks(BlockListType.ALL))
            .assertNext(r -> assertEquals(1, r.getUncommittedBlocks().size()))
            .verifyComplete();

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(wireTap.getLastRequest().getBody()))
            .assertNext(r -> assertArrayEquals(binaryData.toBytes(), r))
            .verifyComplete();
    }

    private static Stream<Arguments> stageBlockDoesNotTransformReplayableBinaryDataSupplier() {
        return Stream.of(
            Arguments.of(BinaryData.fromBytes(DATA.getDefaultBytes())),
            Arguments.of(BinaryData.fromString(DATA.getDefaultText())),
            Arguments.of(BinaryData.fromFile(DATA.getDefaultFile())));
    }

    @ParameterizedTest
    @MethodSource("stageBlockIllegalArgumentsSupplier")
    public void stageBlockIllegalArguments(boolean getBlockId, Flux<ByteBuffer> stream, int dataSize,
                                           Class<? extends Throwable> exceptionType) {
        String blockID = (getBlockId) ? getBlockID() : null;
        StepVerifier.create(blockBlobAsyncClient.stageBlock(blockID, stream, dataSize))
            .verifyError(exceptionType);
    }

    private static Stream<Arguments> stageBlockIllegalArgumentsSupplier() {
        return Stream.of(
            Arguments.of(false, DATA.getDefaultFlux(), DATA.getDefaultDataSize(), BlobStorageException.class),
            Arguments.of(true, null, DATA.getDefaultDataSize(), NullPointerException.class),
            Arguments.of(true, DATA.getDefaultFlux(), DATA.getDefaultDataSize() + 1,
                UnexpectedLengthException.class),
            Arguments.of(true, DATA.getDefaultFlux(), DATA.getDefaultDataSize() - 1,
                UnexpectedLengthException.class)
        );
    }

    @Test
    public void stageBlockIllegalArgumentsWithBinaryData() {
        //This is done without a parameterized test as the toString call updates the internal length being stored,
        //resulting in incorrect test behavior.
        try {
            StepVerifier.create(blockBlobAsyncClient.stageBlock(getBlockID(), null))
                .verifyComplete();
        } catch (NullPointerException e) {
            //StepVerifier cant handle the error in the creation of BlockBlobStageBlockOptions
            assertEquals("The argument must not be null or an empty string. Argument name:"
                + " data must not be null.", e.getMessage());
        }

        try {
            BinaryData data = BinaryData.fromStream(DATA.getDefaultInputStream(), null);
            StepVerifier.create(blockBlobAsyncClient.stageBlock(getBlockID(), data))
                .verifyComplete();
        } catch (NullPointerException e) {
            //StepVerifier cant handle the error in the creation of BlockBlobStageBlockOptions
            assertEquals("The argument must not be null or an empty string. Argument name: data must have"
                + " defined length.", e.getMessage());
        }

        BinaryData binaryData = BinaryData.fromStream(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSizeLong() + 1);
        StepVerifier.create(blockBlobAsyncClient.stageBlock(getBlockID(), binaryData))
            .verifyError(UnexpectedLengthException.class);

        BinaryData binaryData1 = BinaryData.fromStream(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSizeLong() - 1);
        StepVerifier.create(blockBlobAsyncClient.stageBlock(getBlockID(), binaryData1))
            .verifyError(UnexpectedLengthException.class);
    }

    @Test
    public void stageBlockEmptyBody() {
        StepVerifier.create(blockBlobAsyncClient.stageBlock(getBlockID(),
            Flux.just(ByteBuffer.wrap(new byte[0])), 0))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void stageBlockTransactionalMD5() throws NoSuchAlgorithmException {
        byte[] md5 = MessageDigest.getInstance("MD5").digest(DATA.getDefaultBytes());

        assertAsyncResponseStatusCode(blockBlobAsyncClient.stageBlockWithResponse(getBlockID(), DATA.getDefaultFlux(),
            DATA.getDefaultDataSize(), md5, null), 201);
    }

    @Test
    public void stageBlockTransactionalMD5fail() throws NoSuchAlgorithmException {
        StepVerifier.create(blockBlobAsyncClient.stageBlockWithResponse(getBlockID(), DATA.getDefaultFlux(),
            DATA.getDefaultDataSize(), MessageDigest.getInstance("MD5").digest("garbage".getBytes()),
            null))
            .verifyErrorSatisfies(r -> {
                BlobStorageException e = assertInstanceOf(BlobStorageException.class, r);
                assertEquals(BlobErrorCode.MD5MISMATCH, e.getErrorCode());
            });
    }

    @Test
    public void stageBlockTransactionalMD5FailBinaryData() throws NoSuchAlgorithmException {
        StepVerifier.create(blockBlobAsyncClient.stageBlockWithResponse(new BlockBlobStageBlockOptions(getBlockID(),
            BinaryData.fromBytes(DATA.getDefaultBytes())).setContentMd5(MessageDigest.getInstance("MD5")
            .digest("garbage".getBytes()))))
            .verifyErrorSatisfies(r -> {
                BlobStorageException e = assertInstanceOf(BlobStorageException.class, r);
                assertEquals(BlobErrorCode.MD5MISMATCH, e.getErrorCode());
            });
    }

    @Test
    public void stageBlockNullBody() {
        StepVerifier.create(blockBlobAsyncClient.stageBlock(getBlockID(), null, 0))
            .verifyError(NullPointerException.class);
    }

    @Test
    public void stageBlockLease() {
        Mono<Response<Void>> response = setupBlobLeaseCondition(blockBlobAsyncClient, RECEIVED_LEASE_ID)
            .flatMap(r -> blockBlobAsyncClient.stageBlockWithResponse(getBlockID(), DATA.getDefaultFlux(),
                DATA.getDefaultDataSize(), null, r));

        assertAsyncResponseStatusCode(response, 201);
    }

    @Test
    public void stageBlockLeaseBinaryData() {
        Mono<Response<Void>> response = setupBlobLeaseCondition(blockBlobAsyncClient, RECEIVED_LEASE_ID)
            .flatMap(r -> blockBlobAsyncClient.stageBlockWithResponse(
                new BlockBlobStageBlockOptions(getBlockID(), BinaryData.fromBytes(DATA.getDefaultBytes()))
                    .setLeaseId(r)));

        assertAsyncResponseStatusCode(response, 201);
    }

    @Test
    public void stageBlockLeaseFail() {
        StepVerifier.create(setupBlobLeaseCondition(blockBlobAsyncClient, RECEIVED_LEASE_ID)
            .then(blockBlobAsyncClient.stageBlockWithResponse(getBlockID(), DATA.getDefaultFlux(),
                DATA.getDefaultDataSize(), null, GARBAGE_LEASE_ID)))
            .verifyErrorSatisfies(r -> {
                BlobStorageException e = assertInstanceOf(BlobStorageException.class, r);
                assertEquals(BlobErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION, e.getErrorCode());
            });
    }

    @Test
    public void stageBlockError() {
        blockBlobAsyncClient = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();

        StepVerifier.create(blockBlobAsyncClient.stageBlock("id", DATA.getDefaultFlux(),
            DATA.getDefaultDataSize()))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void stageBlockRetryOnTransientFailure() {
        BlockBlobAsyncClient clientWithFailure = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            blobAsyncClient.getBlobUrl(), new TransientFailureInjectingHttpPipelinePolicy()).getBlockBlobAsyncClient();

        byte[] data = getRandomByteArray(10);
        String blockId = getBlockID();

        Mono<byte[]> response = clientWithFailure.stageBlock(blockId, Flux.just(ByteBuffer.wrap(data)), data.length)
            .then(blobAsyncClient.getBlockBlobAsyncClient().commitBlockList(Collections.singletonList(blockId), true))
            .then(FluxUtil.collectBytesInByteBufferStream(blobAsyncClient.downloadStream()));

        StepVerifier.create(response)
            .assertNext(r -> TestUtils.assertArraysEqual(data, r))
            .verifyComplete();
    }

    @Test
    public void stageBlockRetryOnTransientFailureWithRetriableBinaryData() {
        List<BinaryData> binaryDataList = Arrays.asList(
            BinaryData.fromBytes(DATA.getDefaultBytes()),
            BinaryData.fromString(DATA.getDefaultText()),
            BinaryData.fromFile(DATA.getDefaultFile()));
        BlockBlobAsyncClient clientWithFailure = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            blobAsyncClient.getBlobUrl(), new TransientFailureInjectingHttpPipelinePolicy()).getBlockBlobAsyncClient();

        for (BinaryData binaryData : binaryDataList) {
            String blockId = getBlockID();

            Mono<byte[]> response = clientWithFailure.stageBlock(blockId, binaryData)
                .then(blobAsyncClient.getBlockBlobAsyncClient().commitBlockList(Collections.singletonList(blockId), true))
                .then(FluxUtil.collectBytesInByteBufferStream(blobAsyncClient.downloadStream()));

            StepVerifier.create(response)
                .assertNext(r -> TestUtils.assertArraysEqual(binaryData.toBytes(), r))
                .verifyComplete();
        }
    }

    @Test
    public void stageBlockFromUrl() {
        BlockBlobAsyncClient bu2 = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        String blockID = getBlockID();

        String sas = blockBlobAsyncClient.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));
        StepVerifier.create(bu2.stageBlockFromUrlWithResponse(blockID, blockBlobAsyncClient.getBlobUrl() + "?" + sas,
                null, null, null, null))
            .assertNext(r -> {
                HttpHeaders headers = r.getHeaders();
                assertNotNull(headers.getValue(X_MS_CONTENT_CRC64));
                assertNotNull(headers.getValue(X_MS_REQUEST_ID));
                assertNotNull(headers.getValue(X_MS_VERSION));
                assertTrue(Boolean.parseBoolean(headers.getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
            })
            .verifyComplete();

        StepVerifier.create(bu2.listBlocks(BlockListType.ALL))
            .assertNext(r -> {
                assertEquals(1, r.getUncommittedBlocks().size());
                assertEquals(0, r.getCommittedBlocks().size());
                assertEquals(blockID, r.getUncommittedBlocks().get(0).getName());
            })
            .verifyComplete();

        StepVerifier.create(bu2.commitBlockList(Collections.singletonList(blockID)).thenMany(bu2.downloadStream()))
            .assertNext(r -> assertEquals(DATA.getDefaultData(), r))
            .verifyComplete();
    }

    /*@RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2024-08-04")
    @Test
    public void stageBlockFromUrlSourceErrorAndStatusCode() {
        BlockBlobAsyncClient destBlob = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();

        String blockID = getBlockID();

        StepVerifier.create(destBlob.stageBlockFromUrl(blockID, blockBlobAsyncClient.getBlobUrl(), new BlobRange(0, (long) PageBlobClient.PAGE_BYTES)))
            .verifyErrorSatisfies(r -> {
                BlobStorageException e = assertInstanceOf(BlobStorageException.class, r);
                assertTrue(e.getStatusCode() == 409);
                assertTrue(e.getServiceMessage().contains("PublicAccessNotPermitted"));
                assertTrue(e.getServiceMessage().contains("Public access is not permitted on this storage account."));
            });
    }*/

    @Test
    public void stageBlockFromUrlMin() {
        BlockBlobAsyncClient bu2 = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        String blockID = getBlockID();

        String sas = blockBlobAsyncClient.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));
        assertAsyncResponseStatusCode(bu2.stageBlockFromUrlWithResponse(blockID,
            blockBlobAsyncClient.getBlobUrl() + "?" + sas, null, null, null,
            null), 201);
    }

    @ParameterizedTest
    @MethodSource("stageBlockFromURLIASupplier")
    public void stageBlockFromURLIA(boolean getBlockId, String sourceURL, Class<? extends Throwable> exceptionType) {
        String blockID = (getBlockId) ? getBlockID() : null;
        StepVerifier.create(blockBlobAsyncClient.stageBlockFromUrl(blockID, sourceURL, null))
            .verifyError(exceptionType);
    }

    private static Stream<Arguments> stageBlockFromURLIASupplier() {
        return Stream.of(
            Arguments.of(false, "http://www.example.com", BlobStorageException.class),
            Arguments.of(true, null, IllegalArgumentException.class));
    }

    @Test
    public void stageBlockFromURLRange() {
        BlockBlobAsyncClient destURL = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();

        String sas = blockBlobAsyncClient.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));

        StepVerifier.create(destURL.stageBlockFromUrl(getBlockID(), blockBlobAsyncClient.getBlobUrl() + "?" + sas,
            new BlobRange(2L, 3L))
            .then(destURL.listBlocks(BlockListType.UNCOMMITTED)))
            .assertNext(r -> {
                assertEquals(0, r.getCommittedBlocks().size());
                assertEquals(1, r.getUncommittedBlocks().size());
            })
            .verifyComplete();
    }

    @Test
    public void stageBlockFromURLMD5() throws NoSuchAlgorithmException {
        BlockBlobAsyncClient destURL = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();

        String sas = blockBlobAsyncClient.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));
        StepVerifier.create(destURL.stageBlockFromUrlWithResponse(getBlockID(),
            blockBlobAsyncClient.getBlobUrl() + "?" + sas, null,
            MessageDigest.getInstance("MD5").digest(DATA.getDefaultBytes()), null, null))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void stageBlockFromURLMD5Fail() {
        BlockBlobAsyncClient destURL = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();

        String sas = blockBlobAsyncClient.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));
        StepVerifier.create(destURL.stageBlockFromUrlWithResponse(getBlockID(),
            blockBlobAsyncClient.getBlobUrl() + "?" + sas, null, "garbage".getBytes(), null,
            null))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void stageBlockFromURLLease() {
        String sas = blockBlobAsyncClient.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));

        Mono<Response<Void>> response = setupBlobLeaseCondition(blockBlobAsyncClient, RECEIVED_LEASE_ID)
            .flatMap(r -> blockBlobAsyncClient.stageBlockFromUrlWithResponse(getBlockID(),
                blockBlobAsyncClient.getBlobUrl() + "?" + sas, null, null,
                r, null));

        StepVerifier.create(response)
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void stageBlockFromURLLeaseFail() {
        String sas = blockBlobAsyncClient.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));
        StepVerifier.create(blockBlobAsyncClient.stageBlockFromUrlWithResponse(getBlockID(),
            blockBlobAsyncClient.getBlobUrl() + "?" + sas, null, null,
            "garbage", null))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void stageBlockFromURLError() {
        blockBlobAsyncClient = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(generateContainerName())
            .getBlobAsyncClient(generateBlobName())
            .getBlockBlobAsyncClient();

        StepVerifier.create(blockBlobAsyncClient.stageBlockFromUrl(getBlockID(), blockBlobAsyncClient.getBlobUrl(),
            null))
            .verifyError(BlobStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("stageBlockFromURLSourceACSupplier")
    public void stageBlockFromURLSourceAC(OffsetDateTime sourceIfModifiedSince, OffsetDateTime sourceIfUnmodifiedSince,
                                          String sourceIfMatch, String sourceIfNoneMatch) {
        String blockID = getBlockID();

        BlockBlobAsyncClient sourceURL = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        String sas = sourceURL.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));

        Mono<Response<Void>> response = sourceURL.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize())
            .then(setupBlobMatchCondition(sourceURL, sourceIfMatch))
            .flatMap(r -> {
                String newMatch = r;
                if ("null".equals(newMatch)) {
                    newMatch = null;
                }
                BlobRequestConditions smac = new BlobRequestConditions()
                    .setIfModifiedSince(sourceIfModifiedSince)
                    .setIfUnmodifiedSince(sourceIfUnmodifiedSince)
                    .setIfMatch(newMatch)
                    .setIfNoneMatch(sourceIfNoneMatch);
                return blockBlobAsyncClient.stageBlockFromUrlWithResponse(blockID,
                    sourceURL.getBlobUrl() + "?" + sas, null, null, null, smac);
            });


        assertAsyncResponseStatusCode(response, 201);
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
                                              OffsetDateTime sourceIfUnmodifiedSince, String sourceIfMatch,
                                              String sourceIfNoneMatch) {
        String blockID = getBlockID();
        BlockBlobAsyncClient sourceURL = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        String sas = sourceURL.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));

        Mono<Response<Void>> response = sourceURL.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize())
            .then(setupBlobMatchCondition(sourceURL, sourceIfNoneMatch))
            .flatMap(r -> {
                String newNoneMatch = r;
                if ("null".equals(newNoneMatch)) {
                    newNoneMatch = null;
                }
                BlobRequestConditions smac = new BlobRequestConditions()
                    .setIfModifiedSince(sourceIfModifiedSince)
                    .setIfUnmodifiedSince(sourceIfUnmodifiedSince)
                    .setIfMatch(sourceIfMatch)
                    .setIfNoneMatch(newNoneMatch);

                return blockBlobAsyncClient.stageBlockFromUrlWithResponse(blockID,
                    sourceURL.getBlobUrl() + "?" + sas, null, null, null, smac);
            });


        StepVerifier.create(response)
            .verifyError(BlobStorageException.class);
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
        List<String> ids = Collections.singletonList(blockID);

        StepVerifier.create(blockBlobAsyncClient.stageBlock(blockID, DATA.getDefaultFlux(), DATA.getDefaultDataSize())
            .then(blockBlobAsyncClient.commitBlockListWithResponse(ids, null, null, null,
            null)))
            .assertNext(r -> {
                assertResponseStatusCode(r, 201);
                HttpHeaders headers = r.getHeaders();
                validateBasicHeaders(headers);
                assertNotNull(headers.getValue(X_MS_CONTENT_CRC64));
                assertTrue(Boolean.parseBoolean(headers.getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
            })
            .verifyComplete();
    }

    @Test
    public void commitBlockListmin() {
        blockBlobAsyncClient = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        String blockID = getBlockID();
        List<String> ids = Collections.singletonList(blockID);

        StepVerifier.create(blockBlobAsyncClient.stageBlock(blockID, DATA.getDefaultFlux(), DATA.getDefaultDataSize())
            .then(blockBlobAsyncClient.commitBlockList(ids)))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void commitBlockListMinNoOverwrite() {
        StepVerifier.create(blockBlobAsyncClient.commitBlockList(new ArrayList<>()))
            .verifyErrorSatisfies(r -> {
                BlobStorageException e = assertInstanceOf(BlobStorageException.class, r);
                assertEquals(BlobErrorCode.BLOB_ALREADY_EXISTS, e.getErrorCode());
            });
    }

    @Test
    public void commitBlockListOverwrite() {
        StepVerifier.create(blockBlobAsyncClient.commitBlockList(new ArrayList<>(), true))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void commitBlockListNull() {
        assertAsyncResponseStatusCode(blockBlobAsyncClient.commitBlockListWithResponse(null, null,
            null, null, null), 201);
    }

    @ParameterizedTest
    @MethodSource("commitBlockListHeadersSupplier")
    public void commitBlockListHeaders(String cacheControl, String contentDisposition, String contentEncoding,
                                       String contentLanguage, byte[] contentMD5, String contentType) {
        String blockID = getBlockID();
        List<String> ids = Collections.singletonList(blockID);
        BlobHttpHeaders headers = new BlobHttpHeaders()
            .setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType);


        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType;
        String finalContentType = contentType;

        Mono<Response<BlobProperties>> response = blockBlobAsyncClient.stageBlock(blockID, DATA.getDefaultFlux(), DATA.getDefaultDataSize())
            .then(blockBlobAsyncClient.commitBlockListWithResponse(ids, headers, null, null, null))
            .then(blockBlobAsyncClient.getPropertiesWithResponse(null));

        StepVerifier.create(response)
            .assertNext(r ->
                validateBlobProperties(r, cacheControl, contentDisposition, contentEncoding, contentLanguage,
                    contentMD5, finalContentType))
            .verifyComplete();
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

        StepVerifier.create(blockBlobAsyncClient.commitBlockListWithResponse(null, null, metadata, null,
            null)
            .then(blockBlobAsyncClient.getPropertiesWithResponse(null)))
            .assertNext(r -> {
                assertResponseStatusCode(r, 200);
                assertEquals(metadata, r.getValue().getMetadata());
            })
            .verifyComplete();
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

        StepVerifier.create(blockBlobAsyncClient.commitBlockListWithResponse(new BlockBlobCommitBlockListOptions(null)
            .setTags(tags))
            .then(blockBlobAsyncClient.getTagsWithResponse(new BlobGetTagsOptions())))
            .assertNext(r -> {
                assertResponseStatusCode(r, 200);
                assertEquals(tags, r.getValue());
            })
            .verifyComplete();
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


        Mono<Response<BlockBlobItem>> response = blockBlobAsyncClient.setTags(t)
            .then(Mono.zip(setupBlobLeaseCondition(blockBlobAsyncClient, leaseID), setupBlobMatchCondition(blockBlobAsyncClient, match)))
            .flatMap(tuple -> {
                String newLease = tuple.getT1();
                String newMatch = tuple.getT2();
                if ("null".equals(newLease)) {
                    newLease = null;
                }
                if ("null".equals(newMatch)) {
                    newMatch = null;
                }
                BlobRequestConditions bac = new BlobRequestConditions()
                    .setLeaseId(newLease)
                    .setIfMatch(newMatch)
                    .setIfNoneMatch(noneMatch)
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified)
                    .setTagsConditions(tags);

                return blockBlobAsyncClient.commitBlockListWithResponse(null, null,
                    null, null, bac);
            });

        assertAsyncResponseStatusCode(response, 201);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsFailSupplier")
    public void commitBlockListACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
                                      String noneMatch, String leaseID, String tags) {

        Mono<Response<BlockBlobItem>> response = Mono.zip(setupBlobLeaseCondition(blockBlobAsyncClient, leaseID), setupBlobMatchCondition(blockBlobAsyncClient, noneMatch))
            .flatMap(tuple -> {
                String newNoneMatch = tuple.getT2();
                if ("null".equals(newNoneMatch)) {
                    newNoneMatch = null;
                }
                BlobRequestConditions bac = new BlobRequestConditions()
                    .setLeaseId(leaseID)
                    .setIfMatch(match)
                    .setIfNoneMatch(newNoneMatch)
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified)
                    .setTagsConditions(tags);

                return blockBlobAsyncClient.commitBlockListWithResponse(null, null,
                    null, null, bac);
            });

        StepVerifier.create(response)
            .verifyErrorSatisfies(r -> {
                BlobStorageException e = assertInstanceOf(BlobStorageException.class, r);
                assertTrue(e.getErrorCode() == BlobErrorCode.CONDITION_NOT_MET
                    || e.getErrorCode() == BlobErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION);
            });
    }

    @Test
    public void commitBlockListError() {
        blockBlobAsyncClient = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        StepVerifier.create(blockBlobAsyncClient.commitBlockListWithResponse(new ArrayList<>(),
                null, null, null, new BlobRequestConditions().setLeaseId("garbage")))
                .verifyError(BlobStorageException.class);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-12-02")
    @Test
    public void commitBlockListColdTier() {
        blockBlobAsyncClient = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        String blockID = getBlockID();
        List<String> ids = Collections.singletonList(blockID);
        BlockBlobCommitBlockListOptions commitOptions = new BlockBlobCommitBlockListOptions(ids)
            .setTier(AccessTier.COLD);

        Mono<BlobProperties> response = blockBlobAsyncClient.stageBlock(blockID, DATA.getDefaultFlux(), DATA.getDefaultDataSize())
            .then(blockBlobAsyncClient.commitBlockListWithResponse(commitOptions))
            .then(blockBlobAsyncClient.getProperties());

        StepVerifier.create(response)
            .assertNext(r -> assertEquals(AccessTier.COLD, r.getAccessTier()))
            .verifyComplete();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void getBlockList() {
        List<String> committedBlocks = Arrays.asList(getBlockID(), getBlockID());

        List<String> uncommittedBlocks = Arrays.asList(getBlockID(), getBlockID());

        uncommittedBlocks.sort(String::compareTo);

        Set<String> actualCommittedBlocks = new HashSet<>();
        Set<String> actualUncommittedBlocks = new HashSet<>();

        Mono<BlockList> step = blockBlobAsyncClient.stageBlock(committedBlocks.get(0), DATA.getDefaultFlux(), DATA.getDefaultDataSize())
            .then(blockBlobAsyncClient.stageBlock(committedBlocks.get(1), DATA.getDefaultFlux(), DATA.getDefaultDataSize()))
            .then(blockBlobAsyncClient.commitBlockList(committedBlocks, true))
            .then(blockBlobAsyncClient.stageBlock(uncommittedBlocks.get(0), DATA.getDefaultFlux(), DATA.getDefaultDataSize()))
            .then(blockBlobAsyncClient.stageBlock(uncommittedBlocks.get(1), DATA.getDefaultFlux(), DATA.getDefaultDataSize()))
            .then(blockBlobAsyncClient.listBlocks(BlockListType.ALL));

        StepVerifier.create(step)
            .assertNext(r -> {
                for (Block block : r.getCommittedBlocks()) {
                    actualCommittedBlocks.add(block.getName());
                    assertEquals(DATA.getDefaultDataSize(), block.getSize());
                }

                for (Block block : r.getUncommittedBlocks()) {
                    actualUncommittedBlocks.add(block.getName());
                    assertEquals(DATA.getDefaultDataSize(), block.getSize());
                }

            })
            .verifyComplete();

        assertEquals(new HashSet<>(committedBlocks), actualCommittedBlocks);
        assertEquals(new HashSet<>(uncommittedBlocks), actualUncommittedBlocks);
    }

    @Test
    public void getBlockListMin() {
        StepVerifier.create(blockBlobAsyncClient.listBlocks(BlockListType.ALL))
            .expectNextCount(1)
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("getBlockListTypeSupplier")
    public void getBlockListType(BlockListType type, int committedCount, int uncommittedCount) {
        String blockID = getBlockID();

        Mono<BlockList> response = blockBlobAsyncClient.stageBlock(blockID, DATA.getDefaultFlux(), DATA.getDefaultDataSize())
            .then(blockBlobAsyncClient.commitBlockList(Collections.singletonList(blockID), true))
            .then(blockBlobAsyncClient.stageBlock(getBlockID(), DATA.getDefaultFlux(), DATA.getDefaultDataSize()))
            .then(blockBlobAsyncClient.listBlocks(type));

        StepVerifier.create(response)
            .assertNext(r -> {
                assertEquals(committedCount, r.getCommittedBlocks().size());
                assertEquals(uncommittedCount, r.getUncommittedBlocks().size());
            })
            .verifyComplete();
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
        StepVerifier.create(blockBlobAsyncClient.listBlocks(null))
            .assertNext(r -> assertDoesNotThrow(() -> r.getCommittedBlocks().iterator().hasNext()))
            .verifyComplete();
    }

    @Test
    public void getBlockListLease() {
        Mono<Response<BlockList>> response = setupBlobLeaseCondition(blockBlobAsyncClient, RECEIVED_LEASE_ID)
            .flatMap(r -> blockBlobAsyncClient.listBlocksWithResponse(BlockListType.ALL, r));

        StepVerifier.create(response)
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void getBlockListLeaseFail() {
        StepVerifier.create(setupBlobLeaseCondition(blockBlobAsyncClient, GARBAGE_LEASE_ID)
            .then(blockBlobAsyncClient.listBlocksWithResponse(BlockListType.ALL, GARBAGE_LEASE_ID)))
            .verifyErrorSatisfies(r -> {
                BlobStorageException e = assertInstanceOf(BlobStorageException.class, r);
                assertEquals(BlobErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION, e.getErrorCode());
            });
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void getBlockListTags() {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");

        StepVerifier.create(blockBlobAsyncClient.setTags(t)
            .then(blockBlobAsyncClient.listBlocksWithResponse(
                new BlockBlobListBlocksOptions(BlockListType.ALL).setIfTagsMatch("\"foo\" = 'bar'"))))
            .expectNextCount(1)
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void getBlockListTagsFail() {
        StepVerifier.create(blockBlobAsyncClient.listBlocksWithResponse(new BlockBlobListBlocksOptions(BlockListType.ALL)
            .setIfTagsMatch("\"notfoo\" = 'notbar'")))
            .verifyErrorSatisfies(r -> {
                BlobStorageException e = assertInstanceOf(BlobStorageException.class, r);
                assertEquals(BlobErrorCode.CONDITION_NOT_MET, e.getErrorCode());
            });
    }

    @Test
    public void getBlockListError() {
        blockBlobAsyncClient = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();

        StepVerifier.create(blockBlobAsyncClient.listBlocks(BlockListType.ALL))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void upload() {
        StepVerifier.create(blockBlobAsyncClient.uploadWithResponse(DATA.getDefaultFlux(),
            DATA.getDefaultDataSize(), null, null, null, null, null))
            .assertNext(r -> {
                assertResponseStatusCode(r, 201);
                validateBasicHeaders(r.getHeaders());
                assertNotNull(r.getHeaders().getValue(HttpHeaderName.CONTENT_MD5));
                assertTrue(Boolean.parseBoolean(r.getHeaders().getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
            })
            .verifyComplete();

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(blockBlobAsyncClient.downloadStream()))
            .assertNext(r -> TestUtils.assertArraysEqual(r, DATA.getDefaultBytes()))
            .verifyComplete();
    }

    // Override name to prevent BinaryData.toString() invocation by test framework.
    @Test
    public void uploadBinaryData() {
        BinaryData.fromFlux(DATA.getDefaultFlux(), (long) DATA.getDefaultDataSize(), false)
            .flatMap(flux -> {
                List<BinaryData> binaryDataList = Arrays.asList(
                    BinaryData.fromBytes(DATA.getDefaultBytes()),
                    BinaryData.fromString(DATA.getDefaultText()),
                    BinaryData.fromFile(DATA.getDefaultFile()),
                    flux,
                    BinaryData.fromStream(DATA.getDefaultInputStream(), (long) DATA.getDefaultDataSize()));

                for (BinaryData binaryData : binaryDataList) {
                    BlockBlobSimpleUploadOptions uploadOptions = new BlockBlobSimpleUploadOptions(binaryData);
                    StepVerifier.create(blockBlobAsyncClient.uploadWithResponse(uploadOptions))
                        .assertNext(r -> {
                            assertResponseStatusCode(r, 201);
                            validateBasicHeaders(r.getHeaders());
                            assertNotNull(r.getHeaders().getValue(HttpHeaderName.CONTENT_MD5));
                            assertTrue(Boolean.parseBoolean(r.getHeaders().getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
                        })
                        .verifyComplete();

                    StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(blockBlobAsyncClient.downloadStream()))
                        .assertNext(r -> TestUtils.assertArraysEqual(r, DATA.getDefaultBytes()))
                        .verifyComplete();
                }
                return Mono.empty();
            });
    }

    // Override name to prevent BinaryData.toString() invocation by test framework.
    @ParameterizedTest
    @MethodSource("stageBlockDoesNotTransformReplayableBinaryDataSupplier")
    public void uploadDoesNotTransformReplayableBinaryData(BinaryData binaryData) {
        BlockBlobSimpleUploadOptions uploadOptions = new BlockBlobSimpleUploadOptions(binaryData);
        WireTapHttpClient wireTap = new WireTapHttpClient(getHttpClient());
        BlockBlobAsyncClient wireTapClient = getSpecializedBuilder(ENVIRONMENT.getPrimaryAccount().getCredential(),
            blockBlobAsyncClient.getBlobUrl())
            .httpClient(wireTap)
            .buildBlockBlobAsyncClient();

        assertAsyncResponseStatusCode(wireTapClient.uploadWithResponse(uploadOptions), 201);

        // Check that replayable BinaryData contents are passed to http client unchanged.
        assertEquals(binaryData, wireTap.getLastRequest().getBodyAsBinaryData());
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


        Mono<Tuple2<BlockList, byte[]>> tuple = Mono.zip(blobAsyncClient.getBlockBlobAsyncClient().listBlocks(BlockListType.COMMITTED),
            FluxUtil.collectBytesInByteBufferStream(blobAsyncClient.downloadStream()));

        StepVerifier.create(tuple)
            .assertNext(r -> {
                try {
                    FileOutputStream outStream = new FileOutputStream(outFile);
                    outStream.write(Objects.requireNonNull(r.getT2()));
                    outStream.close();
                    compareFiles(file, outFile, 0, fileSize);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                assertEquals(committedBlockCount, r.getT1().getCommittedBlocks().size());
            })
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

        StepVerifier.create(blobAsyncClient.uploadFromFile(file.getAbsolutePath(), null, null,
            metadata, null, null)
            .then(blockBlobAsyncClient.getProperties()))
            .assertNext(r -> assertEquals(metadata, r.getMetadata()))
            .verifyComplete();

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(blockBlobAsyncClient.downloadStream()))
            .assertNext(r -> {
                try {
                    TestUtils.assertArraysEqual(r, Files.readAllBytes(file.toPath()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })
            .verifyComplete();
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

        StepVerifier.create(blobAsyncClient.uploadFromFileWithResponse(new BlobUploadFromFileOptions(file.getAbsolutePath()).setTags(tags))
            .then(blockBlobAsyncClient.getTags()))
            .assertNext(r -> assertEquals(tags, r))
            .verifyComplete();

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(blockBlobAsyncClient.downloadStream()))
            .assertNext(r -> {
                try {
                    TestUtils.assertArraysEqual(r, Files.readAllBytes(file.toPath()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })
            .verifyComplete();
    }

    @LiveOnly
    @Test
    public void uploadFromFileDefaultNoOverwrite() throws IOException {
        File file = getRandomFile(50);
        file.deleteOnExit();
        createdFiles.add(file);

        StepVerifier.create(blobAsyncClient.uploadFromFile(file.toPath().toString()))
            .verifyErrorSatisfies(r -> {
                BlobStorageException e = assertInstanceOf(BlobStorageException.class, r);
                assertEquals(BlobErrorCode.BLOB_ALREADY_EXISTS, e.getErrorCode());
            });
    }

    @LiveOnly
    @Test
    public void uploadFromFileOverwrite() throws IOException {
        File file = getRandomFile(50);
        file.deleteOnExit();
        createdFiles.add(file);

        StepVerifier.create(blobAsyncClient.uploadFromFile(file.toPath().toString(), true))
            .verifyComplete();
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
    @LiveOnly
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

        StepVerifier.create(blobAsyncClient.uploadFromFile(file.toPath().toString(), parallelTransferOptions,
            null, null, null, null))
            .verifyComplete();

        assertEquals(size, uploadReporter.getReportedByteCount());
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
    @LiveOnly
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

        StepVerifier.create(blobAsyncClient.uploadFromFile(file.toPath().toString(), parallelTransferOptions,
            null, null, null, null))
            .verifyComplete();

        assertEquals(size, uploadListener.getReportedByteCount());
    }

    @ParameterizedTest
    @MethodSource("uploadFromFileOptionsSupplier")
    @LiveOnly
    public void uploadFromFileOptions(int dataSize, Long singleUploadSize, Long blockSize, double expectedBlockCount)
        throws IOException {
        File file = getRandomFile(dataSize);
        file.deleteOnExit();
        createdFiles.add(file);

        StepVerifier.create(blobAsyncClient.uploadFromFile(file.toPath().toString(),
            new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(singleUploadSize),
            null, null, null, null)
            .then(blobAsyncClient.getBlockBlobAsyncClient().listBlocks(BlockListType.COMMITTED)))
            .assertNext(r -> assertEquals(expectedBlockCount, r.getCommittedBlocks().size()))
            .verifyComplete();
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

    @Test
    public void uploadMin() {
        StepVerifier.create(blockBlobAsyncClient.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize(), true)
            .then(FluxUtil.collectBytesInByteBufferStream(blockBlobAsyncClient.downloadStream())))
            .assertNext(r -> TestUtils.assertArraysEqual(r, DATA.getDefaultBytes()))
            .verifyComplete();
    }

    // Override name to prevent BinaryData.toString() invocation by test framework.
    @Test
    public void uploadMinBinaryData() {
        BinaryData.fromFlux(DATA.getDefaultFlux(), (long) DATA.getDefaultDataSize(), false)
            .flatMap(flux -> {
                List<BinaryData> binaryDataList = Arrays.asList(
                    BinaryData.fromBytes(DATA.getDefaultBytes()),
                    BinaryData.fromString(DATA.getDefaultText()),
                    BinaryData.fromFile(DATA.getDefaultFile()),
                    flux,
                    BinaryData.fromStream(DATA.getDefaultInputStream(), (long) DATA.getDefaultDataSize()));

                for (BinaryData binaryData : binaryDataList) {
                    StepVerifier.create(blockBlobAsyncClient.upload(binaryData, true)
                        .then(FluxUtil.collectBytesInByteBufferStream(blockBlobAsyncClient.downloadStream())))
                        .assertNext(r -> TestUtils.assertArraysEqual(r, DATA.getDefaultBytes()))
                        .verifyComplete();
                }
                return Mono.empty();
            });
    }

    @ParameterizedTest
    @MethodSource("uploadIllegalArgumentSupplier")
    public void uploadIllegalArgument(Flux<ByteBuffer> stream, long dataSize, Class<? extends Throwable> exceptionType) {
        StepVerifier.create(blockBlobAsyncClient.upload(stream, dataSize))
            .verifyError(exceptionType);
    }

    private static Stream<Arguments> uploadIllegalArgumentSupplier() {
        return Stream.of(
            Arguments.of(null, DATA.getDefaultDataSize(), NullPointerException.class),
            Arguments.of(DATA.getDefaultFlux(), DATA.getDefaultDataSize() + 1, UnexpectedLengthException.class),
            Arguments.of(DATA.getDefaultFlux(), DATA.getDefaultDataSize() - 1, UnexpectedLengthException.class));
    }

    @Test
    public void uploadIllegalArgumentBinaryData() {
        assertThrows(NullPointerException.class, () -> blockBlobAsyncClient.upload(null));

        assertThrows(NullPointerException.class, () -> blockBlobAsyncClient.upload(
            BinaryData.fromStream(DATA.getDefaultInputStream(), null)));

        BinaryData badLength1 = BinaryData.fromStream(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSizeLong() + 1);
        StepVerifier.create(blockBlobAsyncClient.upload(badLength1))
            .verifyError(UnexpectedLengthException.class);


        BinaryData badLength2 = BinaryData.fromStream(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSizeLong() - 1);
        StepVerifier.create(blockBlobAsyncClient.upload(badLength2))
            .verifyError(UnexpectedLengthException.class);
    }

    @Test
    public void uploadEmptyBody() {
        assertAsyncResponseStatusCode(blockBlobAsyncClient.uploadWithResponse(Flux.just(ByteBuffer.wrap(new byte[0])),
            0, null, null, null, null, null), 201);
    }

    @Test
    public void uploadNullBody() {
        StepVerifier.create(blockBlobAsyncClient.uploadWithResponse(null, 0, null, null,
            null, null, null))
            .verifyError(NullPointerException.class);
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

        // If the value isn't set the service will automatically set it
        contentMD5 = (contentMD5 == null) ? MessageDigest.getInstance("MD5").digest(DATA.getDefaultBytes()) : contentMD5;
        contentType = (contentType == null) ? "application/octet-stream" : contentType;
        String finalContentType = contentType;
        byte[] finalContentMD = contentMD5;

        StepVerifier.create(blockBlobAsyncClient.uploadWithResponse(DATA.getDefaultFlux(), DATA.getDefaultDataSize(),
            headers, null, null, null, null)
            .then(blockBlobAsyncClient.getPropertiesWithResponse(null)))
            .assertNext(r ->
                validateBlobProperties(r, cacheControl, contentDisposition, contentEncoding, contentLanguage, finalContentMD,
                    finalContentType))
            .verifyComplete();
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

        assertAsyncResponseStatusCode(blockBlobAsyncClient.uploadWithResponse(DATA.getDefaultFlux(),
            DATA.getDefaultDataSize(), null, null, null, md5, null),
            201);
    }

    @Test
    public void uploadTransactionalMD5Fail() throws NoSuchAlgorithmException {
        StepVerifier.create(blockBlobAsyncClient.stageBlockWithResponse(getBlockID(), DATA.getDefaultFlux(),
            DATA.getDefaultDataSize(), MessageDigest.getInstance("MD5").digest("garbage".getBytes()), null))
            .verifyErrorSatisfies(r -> {
                BlobStorageException e = assertInstanceOf(BlobStorageException.class, r);
                assertEquals(BlobErrorCode.MD5MISMATCH, e.getErrorCode());
            });
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

        StepVerifier.create(blockBlobAsyncClient.uploadWithResponse(DATA.getDefaultFlux(), DATA.getDefaultDataSize(),
            null, metadata, null, null, null)
            .then(blockBlobAsyncClient.getPropertiesWithResponse(null)))
            .assertNext(r -> {
                assertResponseStatusCode(r, 200);
                assertEquals(metadata, r.getValue().getMetadata());
            })
            .verifyComplete();
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

        StepVerifier.create(blockBlobAsyncClient.uploadWithResponse(
            new BlockBlobSimpleUploadOptions(DATA.getDefaultInputStream(), DATA.getDefaultDataSize()).setTags(tags))
            .then(blockBlobAsyncClient.getTagsWithResponse(new BlobGetTagsOptions())))
            .assertNext(r -> {
                assertResponseStatusCode(r, 200);
                assertEquals(tags, r.getValue());
            })
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void uploadAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                         String leaseID, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");

        Mono<Response<BlockBlobItem>> response = blockBlobAsyncClient.setTags(t)
            .then(Mono.zip(setupBlobLeaseCondition(blockBlobAsyncClient, leaseID), setupBlobMatchCondition(blockBlobAsyncClient, match)))
            .flatMap(tuple -> {
                String newLease = tuple.getT1();
                String newMatch = tuple.getT2();
                if ("null".equals(newLease)) {
                    newLease = null;
                }
                if ("null".equals(newMatch)) {
                    newMatch = null;
                }
                BlobRequestConditions bac = new BlobRequestConditions()
                    .setLeaseId(newLease)
                    .setIfMatch(newMatch)
                    .setIfNoneMatch(noneMatch)
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified)
                    .setTagsConditions(tags);

                return blockBlobAsyncClient.uploadWithResponse(DATA.getDefaultFlux(),
                    DATA.getDefaultDataSize(), null, null, null, null, bac);
            });

        assertAsyncResponseStatusCode(response, 201);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsFailSupplier")
    public void uploadACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                             String leaseID, String tags) {

        Mono<Response<BlockBlobItem>> response = Mono.zip(setupBlobLeaseCondition(blockBlobAsyncClient, leaseID), setupBlobMatchCondition(blockBlobAsyncClient, noneMatch))
            .flatMap(tuple -> {
                String newNoneMatch = tuple.getT2();
                if ("null".equals(newNoneMatch)) {
                    newNoneMatch = null;
                }
                BlobRequestConditions bac = new BlobRequestConditions()
                    .setLeaseId(leaseID)
                    .setIfMatch(match)
                    .setIfNoneMatch(newNoneMatch)
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified)
                    .setTagsConditions(tags);

                return blockBlobAsyncClient.uploadWithResponse(DATA.getDefaultFlux(), DATA.getDefaultDataSize(),
                    null, null, null, null, bac);
            });

        StepVerifier.create(response)
            .verifyErrorSatisfies(r -> {
                BlobStorageException e = assertInstanceOf(BlobStorageException.class, r);
                assertTrue(e.getErrorCode() == BlobErrorCode.CONDITION_NOT_MET
                    || e.getErrorCode() == BlobErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION);
            });
    }

    @Test
    public void uploadError() {
        blockBlobAsyncClient = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();

        StepVerifier.create(blockBlobAsyncClient.uploadWithResponse(DATA.getDefaultFlux(),
            DATA.getDefaultDataSize(), null, null, null, null,
            new BlobRequestConditions().setLeaseId("id")))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void uploadWithTier() {
        BlockBlobAsyncClient bc = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();

        StepVerifier.create(bc.uploadWithResponse(DATA.getDefaultFlux(), DATA.getDefaultDataSize(), null,
            null, AccessTier.COOL, null, null)
            .then(bc.getProperties()))
            .assertNext(r -> assertEquals(AccessTier.COOL, r.getAccessTier()))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-12-02")
    @Test
    public void uploadWithAccessTierCold() {
        BlockBlobAsyncClient bc = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();

        StepVerifier.create(bc.uploadWithResponse(DATA.getDefaultFlux(), DATA.getDefaultDataSize(), null,
            null, AccessTier.COLD, null, null)
            .then(bc.getProperties()))
            .assertNext(r -> assertEquals(AccessTier.COLD, r.getAccessTier()))
            .verifyComplete();
    }

    @Test
    public void uploadOverwriteFalse() {
        StepVerifier.create(blockBlobAsyncClient.upload(DATA.getDefaultFlux(),
            DATA.getDefaultDataSize()))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void uploadOverwriteTrue() {
        StepVerifier.create(blockBlobAsyncClient.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize(), true))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void uploadRetryOnTransientFailure() {
        BlockBlobAsyncClient clientWithFailure = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            blobAsyncClient.getBlobUrl(), new TransientFailureInjectingHttpPipelinePolicy()).getBlockBlobAsyncClient();

        clientWithFailure.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize(), true);

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(blobAsyncClient.downloadStream()))
            .assertNext(r -> TestUtils.assertArraysEqual(r, DATA.getDefaultBytes()))
            .verifyComplete();
    }

    @LiveOnly
    @Test
    public void asyncBufferedUploadEmpty() {
        StepVerifier.create(blobAsyncClient.upload(Flux.just(ByteBuffer.wrap(new byte[0])), null,
            true))
            .assertNext(it -> assertNotNull(it.getETag()))
            .verifyComplete();

        StepVerifier.create(blobAsyncClient.downloadStream())
            .assertNext(it -> assertEquals(0, it.remaining()))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("asyncBufferedUploadEmptyBuffersSupplier")
    @LiveOnly
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
    @LiveOnly
    public void asyncBufferedUpload(int dataSize, long bufferSize, int numBuffs, int blockCount) {
        //todo isbr
        BlobAsyncClient asyncClient = getPrimaryServiceClientForWrites(bufferSize)
            .getBlobContainerAsyncClient(blobAsyncClient.getContainerName())
            .getBlobAsyncClient(blobAsyncClient.getBlobName());

        ByteBuffer data = getRandomData(dataSize);
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSizeLong(bufferSize).setMaxConcurrency(numBuffs).setMaxSingleUploadSizeLong(4L * Constants.MB);
        data.position(0);

        // Due to memory issues, this check only runs on small to medium sized data sets.
        if (dataSize < 100 * 1024 * 1024) {
            StepVerifier.create(asyncClient.upload(Flux.just(data), parallelTransferOptions, true)
                .then(collectBytesInBuffer(blockBlobAsyncClient.downloadStream())))
                .assertNext(it -> assertEquals(data, it))
                .verifyComplete();

            StepVerifier.create(blockBlobAsyncClient.listBlocks(BlockListType.ALL))
                .assertNext(it -> assertEquals(blockCount, it.getCommittedBlocks().size()))
                .verifyComplete();
        } else {
            StepVerifier.create(asyncClient.upload(Flux.just(data), parallelTransferOptions, true)
                .then(blockBlobAsyncClient.listBlocks(BlockListType.ALL)))
                .assertNext(it -> assertEquals(blockCount, it.getCommittedBlocks().size()))
                .verifyComplete();
        }
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
        StepVerifier.create(blobAsyncClient.upload(DATA.getDefaultBinaryData(), true)
            .then(blockBlobAsyncClient.downloadContent()))
            .assertNext(it -> TestUtils.assertArraysEqual(it.toBytes(), DATA.getDefaultBinaryData().toBytes()))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("asyncBufferedUploadComputeMd5Supplier")
    @LiveOnly
    public void asyncBufferedUploadComputeMd5(int size, Long maxSingleUploadSize, Long blockSize, int byteBufferCount) {
        List<ByteBuffer> byteBufferList = new ArrayList<>();
        for (int i = 0; i < byteBufferCount; i++) {
            byteBufferList.add(getRandomData(size));
        }
        Flux<ByteBuffer> flux = Flux.fromIterable(byteBufferList);
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setMaxSingleUploadSizeLong(maxSingleUploadSize)
            .setBlockSizeLong(blockSize);

        assertAsyncResponseStatusCode(Objects.requireNonNull(blobAsyncClient.uploadWithResponse(
            new BlobParallelUploadOptions(flux)
                .setParallelTransferOptions(parallelTransferOptions)
                .setComputeMd5(true))), 201);

    }

    private static Stream<Arguments> asyncBufferedUploadComputeMd5Supplier() {
        return Stream.of(
            Arguments.of(Constants.KB, null, null, 1), // Simple case where uploadFull is called.
            Arguments.of(Constants.KB, (long) Constants.KB, 500L * Constants.KB, 1000), // uploadChunked 2 blocks staged
            Arguments.of(Constants.KB, (long) Constants.KB, 5L * Constants.KB, 1000)); // uploadChunked 100 blocks staged
    }

    @Test
    public void asyncUploadBinaryDataWithResponse() {
        assertAsyncResponseStatusCode(Objects.requireNonNull(blobAsyncClient.uploadWithResponse(
            new BlobParallelUploadOptions(DATA.getDefaultBinaryData()))), 201);
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
    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @MethodSource("bufferedUploadWithReporterSupplier")
    @LiveOnly
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
            null, null, null, null))
            .assertNext(it -> {
                assertEquals(201, it.getStatusCode());
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
    @LiveOnly
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
            null, null, null, null))
            .assertNext(it -> {
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
    @LiveOnly
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
            .assertNext(it -> assertEquals(blockCount, it.getCommittedBlocks().size()))
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
    @LiveOnly
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
            .assertNext(it -> assertEquals(blockCount, it.getCommittedBlocks().size()))
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
    @LiveOnly
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
            .assertNext(it -> assertEquals(blockCount, it.getCommittedBlocks().size()))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("bufferedUploadHandlePathingHotFluxWithTransientFailureSupplier")
    @LiveOnly
    public void bufferedUploadHandlePathingHotFluxWithTransientFailure(int[] dataSizeList, int blockCount) {
        BlobAsyncClient clientWithFailure = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            blobAsyncClient.getBlobUrl(), new TransientFailureInjectingHttpPipelinePolicy());

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
            .assertNext(it -> assertEquals(blockCount, it.getCommittedBlocks().size()))
            .verifyComplete();
    }

    private static Stream<Arguments> bufferedUploadHandlePathingHotFluxWithTransientFailureSupplier() {
        return Stream.of(
            Arguments.of(new int[]{10, 100, 1000, 10000}, 0),
            Arguments.of(new int[]{4 * Constants.MB + 1, 10}, 2),
            Arguments.of(new int[]{4 * Constants.MB, 4 * Constants.MB}, 2));
    }

    @Test
    public void bufferedUploadIllegalArgumentsNull() {
        StepVerifier.create(blobAsyncClient.upload(null, new ParallelTransferOptions()
                .setBlockSizeLong(4L)
                .setMaxConcurrency(4), true))
            .verifyErrorSatisfies(it -> assertInstanceOf(NullPointerException.class, it));
    }

    // todo isbr: Should think about moving this test out of here as this should be in a test class specific to ParallelTransferOptions that isn't recorded.
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
    @LiveOnly
    public void bufferedUploadHeaders(int dataSize, String cacheControl, String contentDisposition,
                                      String contentEncoding, String contentLanguage, boolean validateContentMD5,
                                      String contentType)
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
    @LiveOnly
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
            Flux.just(getRandomData(10)), parallelTransferOptions, null, metadata, null,
            null);

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
    @SuppressWarnings("deprecation")
    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("bufferedUploadTagsSupplier")
    @LiveOnly
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
    @LiveOnly
    public void bufferedUploadOptions(int dataSize, Long singleUploadSize, Long blockSize, int expectedBlockCount) {
        ByteBuffer data = getRandomData(dataSize);

        StepVerifier.create(blobAsyncClient.uploadWithResponse(Flux.just(data),
            new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(singleUploadSize),
            null, null, null, null)
            .then(Objects.requireNonNull(blobAsyncClient.getBlockBlobAsyncClient()).listBlocks(BlockListType.COMMITTED)))
            .assertNext(r -> assertEquals(expectedBlockCount, r.getCommittedBlocks().size()))
            .verifyComplete();
    }

    @SuppressWarnings("deprecation")
    private static Stream<Arguments> bufferedUploadOptionsSupplier() {
        return Stream.of(
            // Test that the default for singleUploadSize is the maximum
            Arguments.of(BlockBlobAsyncClient.MAX_UPLOAD_BLOB_BYTES - 1, null, null, 0),
            // This also validates the default for blockSize
            Arguments.of(BlockBlobAsyncClient.MAX_UPLOAD_BLOB_BYTES + 1, null, null,
                (int) Math.ceil(((double) BlockBlobAsyncClient.MAX_UPLOAD_BLOB_BYTES + 1) / (double) BlobClient.BLOB_DEFAULT_UPLOAD_BLOCK_SIZE)/* "". This also validates the default for blockSize*/),
            // Test that singleUploadSize is respected
            Arguments.of(100, 50L, null, 1),
            // Test that blockSize is respected
            Arguments.of(100, 50L, 20L, 5));
    }

    @ParameterizedTest
    @MethodSource("bufferedUploadWithLengthSupplier")
    @LiveOnly
    public void bufferedUploadWithLength(int dataSize, Long singleUploadSize, Long blockSize, int expectedBlockCount) {
        Flux<ByteBuffer> data = Flux.just(getRandomData(dataSize));
        Mono<BlobParallelUploadOptions> parallelUploadOptions = BinaryData.fromFlux(data, (long) dataSize)
            .flatMap(r -> Mono.just(new BlobParallelUploadOptions(r)
                .setParallelTransferOptions(new ParallelTransferOptions()
                    .setBlockSizeLong(blockSize)
                    .setMaxSingleUploadSizeLong(singleUploadSize))));

        StepVerifier.create(parallelUploadOptions.flatMap(r -> blobAsyncClient.uploadWithResponse(r))
            .then(Objects.requireNonNull(blobAsyncClient.getBlockBlobAsyncClient()
            .listBlocks(BlockListType.COMMITTED))))
            .assertNext(r -> assertEquals(expectedBlockCount, r.getCommittedBlocks().size()))
            .verifyComplete();
    }

    private static Stream<Arguments> bufferedUploadWithLengthSupplier() {
        return Stream.of(Arguments.of(100, 100L, null, 0), // Test that singleUploadSize is respected
            Arguments.of(100, 50L, 20L, 5)); // Test that blockSize is respected
    }
    // Only run these tests in live mode as they use variables that can't be captured.
    @ParameterizedTest
    @MethodSource("bufferedUploadACSupplier")
    @LiveOnly
    public void bufferedUploadAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                 String leaseID) {
        Mono<Response<BlockBlobItem>> response = blockBlobAsyncClient.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize(), true)
            .then(Mono.zip(setupBlobLeaseCondition(blockBlobAsyncClient, leaseID), setupBlobMatchCondition(blockBlobAsyncClient, match)))
            .flatMap(tuple -> {
                String newLease = tuple.getT1();
                String newMatch = tuple.getT2();
                if ("null".equals(newLease)) {
                    newLease = null;
                }
                if ("null".equals(newMatch)) {
                    newMatch = null;
                }
                BlobRequestConditions requestConditions = new BlobRequestConditions()
                    .setLeaseId(newLease)
                    .setIfMatch(newMatch)
                    .setIfNoneMatch(noneMatch)
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified);
                ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(10L);

                return blobAsyncClient.uploadWithResponse(Flux.just(getRandomData(10)),
                    parallelTransferOptions, null, null, null, requestConditions);
            });

        assertAsyncResponseStatusCode(response, 201);
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
    @LiveOnly
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#fileACFailSupplier")
    public void bufferedUploadACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                     String leaseID) {
        Mono<Response<BlockBlobItem>> response = blockBlobAsyncClient.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize(), true)
            .then(Mono.zip(setupBlobLeaseCondition(blockBlobAsyncClient, leaseID), setupBlobMatchCondition(blockBlobAsyncClient, noneMatch)))
            .flatMap(tuple -> {
                String newLease = tuple.getT1();
                String newNoneMatch = tuple.getT2();
                if ("null".equals(newLease)) {
                    newLease = null;
                }
                if ("null".equals(newNoneMatch)) {
                    newNoneMatch = null;
                }
                BlobRequestConditions requestConditions = new BlobRequestConditions()
                    .setLeaseId(newLease)
                    .setIfMatch(match)
                    .setIfNoneMatch(newNoneMatch)
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified);
                ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(10L);

                return blobAsyncClient.uploadWithResponse(Flux.just(getRandomData(10)), parallelTransferOptions,
                    null, null, null, requestConditions);
            });

        StepVerifier.create(response)
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
    @LiveOnly
    public void uploadBufferPoolLockThreeOrMoreBuffers(int dataLength, int blockSize, int numBuffers) {
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSizeLong((long) blockSize)
            .setMaxConcurrency(numBuffers);

        Mono<Response<BlockBlobItem>> response = blockBlobAsyncClient.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize(), true)
            .then(setupBlobLeaseCondition(blockBlobAsyncClient, GARBAGE_LEASE_ID))
            .flatMap(r -> {
                BlobRequestConditions requestConditions = new BlobRequestConditions().setLeaseId(r);
                return blobAsyncClient.uploadWithResponse(Flux.just(getRandomData(dataLength)),
                    parallelTransferOptions, null, null, null, requestConditions);
            });

        StepVerifier.create(response)
            .verifyErrorSatisfies(it -> assertInstanceOf(BlobStorageException.class, it));
    }

    @LiveOnly
    @Test
    public void bufferedUploadNetworkError() {
        /*
         This test uses a Flowable that does not allow multiple subscriptions and therefore ensures that we are
         buffering properly to allow for retries even given this source behavior.
         */

        // Mock a response that will always be retried.
        HttpResponse mockHttpResponse = new MockHttpResponse(new HttpRequest(HttpMethod.PUT, "https://www.fake.com"),
            500);

        // Mock a policy that will always then check that the data is still the same and return a retryable error.
        ByteBuffer localData = DATA.getDefaultData();
        HttpPipelinePolicy mockPolicy = (context, next) -> collectBytesInBuffer(context.getHttpRequest().getBody())
            .map(localData::equals)
            .flatMap(it -> it ? Mono.just(mockHttpResponse) : Mono.error(new IllegalArgumentException()));

        // Build the pipeline
        BlobAsyncClient blobAsyncClient = new BlobServiceClientBuilder()
            .credential(ENVIRONMENT.getPrimaryAccount().getCredential())
            .endpoint(ENVIRONMENT.getPrimaryAccount().getBlobEndpoint())
            .retryOptions(new RequestRetryOptions(null, 3, null, 500L,
                1500L, null))
            .addPolicy(mockPolicy).buildAsyncClient()
            .getBlobContainerAsyncClient(generateContainerName()).getBlobAsyncClient(generateBlobName());

        // Try to upload the flowable, which will hit a retry. A normal upload would throw, but buffering prevents that.
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(1024L)
            .setMaxConcurrency(4);
        // TODO: It could be that duplicates aren't getting made in the retry policy? Or before the retry policy?

        // A second subscription to a download stream will
        StepVerifier.create(blockBlobAsyncClient.upload(Flux.just(DATA.getDefaultData()), DATA.getDefaultDataSize(), true)
            .then(blobAsyncClient.upload(blockBlobAsyncClient.downloadStream(), parallelTransferOptions, true)))
            .verifyErrorSatisfies(it -> {
                assertInstanceOf(BlobStorageException.class, it);
                assertEquals(500, ((BlobStorageException) it).getStatusCode());
            });
    }

    @LiveOnly
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

    @LiveOnly
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

    @LiveOnly
    @Test
    public void bufferedUploadWithSpecifiedLength() {
        Flux<ByteBuffer> fluxData = Flux.just(getRandomData(DATA.getDefaultDataSize()));
        Mono<Response<BlockBlobItem>> response = BinaryData.fromFlux(fluxData, DATA.getDefaultDataSizeLong())
            .flatMap(r -> blobAsyncClient.uploadWithResponse(new BlobParallelUploadOptions(r)));

        StepVerifier.create(response)
            .assertNext(it -> assertNotNull(it.getValue().getETag())).verifyComplete();
    }

    @LiveOnly
    @Test
    public void bufferedUploadOverwrite() throws IOException {
        File file = getRandomFile(50);
        file.deleteOnExit();
        createdFiles.add(file);

        StepVerifier.create(blobAsyncClient.uploadFromFile(file.toPath().toString(), true)
            .then(blobAsyncClient.uploadFromFile(getRandomFile(50).toPath().toString(), true)))
            .verifyComplete();
    }

    @Test
    public void bufferedUploadNonMarkableStream() throws IOException {
        File file = getRandomFile(10);
        file.deleteOnExit();
        createdFiles.add(file);

        File outFile = getRandomFile(10);
        createdFiles.add(outFile);
        outFile.deleteOnExit();
        Files.deleteIfExists(outFile.toPath());

        StepVerifier.create(blobAsyncClient.uploadFromFile(file.getPath(), true)
            .then(blobAsyncClient.downloadToFile(outFile.toPath().toString(), true)))
            .expectNextCount(1)
            .verifyComplete();

        compareFiles(file, outFile, 0, file.length());
    }

    @Test
    public void getContainerName() {
        assertEquals(containerName, blockBlobAsyncClient.getContainerName());
    }

    @Test
    public void getBlockBlobName() {
        assertEquals(blobName, blockBlobAsyncClient.getBlobName());
    }

    @ParameterizedTest
    @MethodSource("getBlobNameAndBuildClientSupplier")
    public void getBlobNameAndBuildClient(String originalBlobName, String finalBlobName) {
        BlobAsyncClient client = ccAsync.getBlobAsyncClient(originalBlobName);
        BlockBlobAsyncClient blockClient = ccAsync.getBlobAsyncClient(client.getBlobName()).getBlockBlobAsyncClient();

        assertEquals(finalBlobName, blockClient.getBlobName());
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
        URL endpoint = BlobUrlParts.parse(blockBlobAsyncClient.getBlobUrl()).setScheme("http").toUrl();
        SpecializedBlobClientBuilder builder = new SpecializedBlobClientBuilder()
            .customerProvidedKey(new CustomerProvidedKey(Base64.getEncoder()
                .encodeToString(getRandomByteArray(256))))
            .endpoint(endpoint.toString());

        assertThrows(IllegalArgumentException.class, builder::buildBlockBlobAsyncClient);
    }

    @Test
    public void builderBearerTokenValidation() {
        URL endpoint = BlobUrlParts.parse(blockBlobAsyncClient.getBlobUrl()).setScheme("http").toUrl();
        SpecializedBlobClientBuilder builder = new SpecializedBlobClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint.toString());

        assertThrows(IllegalArgumentException.class, builder::buildBlockBlobAsyncClient);
    }

    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials
    // and auth would fail because we changed a signed header.
    @Test
    public void perCallPolicy() {
        BlockBlobAsyncClient specialBlob = getSpecializedBuilder(blockBlobAsyncClient.getBlobUrl())
            .addPolicy(getPerCallVersionPolicy())
            .buildBlockBlobAsyncClient();

        StepVerifier.create(specialBlob.getPropertiesWithResponse(null))
            .assertNext(r -> assertEquals("2017-11-09", r.getHeaders().getValue(X_MS_VERSION)))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-04-08")
    @Test
    public void uploadFromUrlMin() {
        BlobAsyncClient sourceBlob = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(containerName)
            .getBlobAsyncClient(generateBlobName());
        String sas = sourceBlob.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));

        Mono<BlockBlobItem> response = sourceBlob.upload(DATA.getDefaultFlux(), null).then(blockBlobAsyncClient.exists())
            .flatMap(r -> {
                if (r) {
                    return blockBlobAsyncClient.delete();
                }
                return Mono.empty();
            })
            .then(blockBlobAsyncClient.uploadFromUrl(sourceBlob.getBlobUrl() + "?" + sas));

        StepVerifier.create(response)
            .assertNext(r -> {
                assertNotNull(r);
                assertNotNull(r.getETag());
                assertNotNull(r.getLastModified());
            })
            .verifyComplete();

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(blobAsyncClient.downloadStream()))
            .assertNext(r -> TestUtils.assertArraysEqual(DATA.getDefaultBytes(), r))
            .verifyComplete();
    }

    /*@RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2024-08-04")
    @Test
    public void uploadFromUrlSourceErrorAndStatusCode() {
        BlockBlobAsyncClient destBlob = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();

        StepVerifier.create(destBlob.uploadFromUrl(blockBlobAsyncClient.getBlobUrl()))
            .verifyErrorSatisfies(r -> {
                BlobStorageException e = assertInstanceOf(BlobStorageException.class, r);
                assertTrue(e.getStatusCode() == 409);
                assertTrue(e.getServiceMessage().contains("PublicAccessNotPermitted"));
                assertTrue(e.getServiceMessage().contains("Public access is not permitted on this storage account."));
            });
    }*/

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-04-08")
    @Test
    public void uploadFromUrlOverwrite() {
        BlobAsyncClient sourceBlob = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(containerName)
            .getBlobAsyncClient(generateBlobName());
        String sas = sourceBlob.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));

        Mono<BlockBlobItem> response = sourceBlob.upload(DATA.getDefaultFlux(), null)
            .then(blockBlobAsyncClient.upload(Flux.just(ByteBuffer.wrap(new byte[0])), 0, true))
            .then(blockBlobAsyncClient.uploadFromUrl(sourceBlob.getBlobUrl() + "?" + sas, true));

        StepVerifier.create(response)
            .assertNext(r -> {
                assertNotNull(r);
                assertNotNull(r.getETag());
                assertNotNull(r.getLastModified());
            })
            .verifyComplete();

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(blobAsyncClient.downloadStream()))
            .assertNext(r -> TestUtils.assertArraysEqual(DATA.getDefaultBytes(), r))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-04-08")
    @Test
    public void uploadFromUrlOverwriteFailsOnExistingBlob() {
        BlobAsyncClient sourceBlob = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(containerName)
            .getBlobAsyncClient(generateBlobName());
        String sas = sourceBlob.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));

        Mono<BlockBlobItem> response = sourceBlob.upload(DATA.getDefaultFlux(), null)
            .then(blockBlobAsyncClient.upload(Flux.just(ByteBuffer.wrap(new byte[0])), 0, true))
            .then(blockBlobAsyncClient.uploadFromUrl(sourceBlob.getBlobUrl() + "?" + sas, false));

        StepVerifier.create(response)
            .verifyErrorSatisfies(r -> {
                BlobStorageException e = assertInstanceOf(BlobStorageException.class, r);
                assertEquals(BlobErrorCode.BLOB_ALREADY_EXISTS, e.getErrorCode());
            });

        StepVerifier.create(blockBlobAsyncClient.uploadFromUrl(sourceBlob.getBlobUrl() + "?" + sas))
            .verifyErrorSatisfies(r -> {
                BlobStorageException e = assertInstanceOf(BlobStorageException.class, r);
                assertEquals(BlobErrorCode.BLOB_ALREADY_EXISTS, e.getErrorCode());
            });
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-04-08")
    @Test
    public void uploadFromUrlMax() throws NoSuchAlgorithmException {
        BlobAsyncClient sourceBlob = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(containerName)
            .getBlobAsyncClient(generateBlobName());
        byte[] sourceBlobMD5 = MessageDigest.getInstance("MD5").digest(DATA.getDefaultBytes());
        String sas = sourceBlob.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));

        Mono<Response<BlockBlobItem>> response = sourceBlob.upload(DATA.getDefaultFlux(), null)
            .then(sourceBlob.setHttpHeaders(new BlobHttpHeaders().setContentLanguage("en-GB")))
            .then(blockBlobAsyncClient.upload(Flux.just(ByteBuffer.wrap(new byte[0])), 0, true))
            .then(Mono.zip(sourceBlob.getProperties(), blockBlobAsyncClient.getProperties()))
            .flatMap(tuple -> {
                BlobUploadFromUrlOptions options = new BlobUploadFromUrlOptions(sourceBlob.getBlobUrl() + "?" + sas)
                    .setContentMd5(sourceBlobMD5)
                    .setCopySourceBlobProperties(true)
                    .setDestinationRequestConditions(new BlobRequestConditions()
                        .setIfMatch(tuple.getT2().getETag()))
                    .setSourceRequestConditions(new BlobRequestConditions().setIfMatch(tuple.getT1().getETag()))
                    .setHeaders(new BlobHttpHeaders().setContentType("text"))
                    .setTier(AccessTier.COOL);
                return blockBlobAsyncClient.uploadFromUrlWithResponse(options);
            });

        StepVerifier.create(response)
            .assertNext(r -> {
                assertNotNull(r);
                assertNotNull(r.getRequest());
                assertNotNull(r.getHeaders());
                BlockBlobItem blockBlobItem = r.getValue();
                assertNotNull(blockBlobItem);
                assertNotNull(blockBlobItem.getETag());
                assertNotNull(blockBlobItem.getLastModified());
            })
            .verifyComplete();

        StepVerifier.create(blobAsyncClient.getProperties())
            .assertNext(r -> {
                assertEquals("en-GB", r.getContentLanguage());
                assertEquals("text", r.getContentType());
                assertEquals(AccessTier.COOL, r.getAccessTier());
            })
            .verifyComplete();

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(blockBlobAsyncClient.downloadStream()))
            .assertNext(r -> TestUtils.assertArraysEqual(DATA.getDefaultBytes(), r))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-04-08")
    @Test
    public void uploadFromWithInvalidSourceMD5() throws NoSuchAlgorithmException {
        BlobAsyncClient sourceBlob = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(containerName)
            .getBlobAsyncClient(generateBlobName());
        byte[] sourceBlobMD5 = MessageDigest.getInstance("MD5")
            .digest("garbage".getBytes(StandardCharsets.UTF_8));
        String sas = sourceBlob.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));
        BlobUploadFromUrlOptions options = new BlobUploadFromUrlOptions(sourceBlob.getBlobUrl() + "?" + sas)
            .setContentMd5(sourceBlobMD5);

        Mono<Response<BlockBlobItem>> response = sourceBlob.upload(DATA.getDefaultFlux(), null)
            .then(blockBlobAsyncClient.upload(Flux.just(ByteBuffer.wrap(new byte[0])), 0, true))
            .then(blockBlobAsyncClient.uploadFromUrlWithResponse(options));

        StepVerifier.create(response)
            .verifyErrorSatisfies(r -> {
                BlobStorageException e = assertInstanceOf(BlobStorageException.class, r);
                assertEquals(BlobErrorCode.MD5MISMATCH, e.getErrorCode());
            });
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-04-08")
    @ParameterizedTest
    @MethodSource("uploadFromUrlSourceRequestConditionsSupplier")
    public void uploadFromUrlSourceRequestConditions(BlobRequestConditions requestConditions, BlobErrorCode errorCode) {
        BlobAsyncClient sourceBlob = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(containerName)
            .getBlobAsyncClient(generateBlobName());

        Mono<Response<BlockBlobItem>> response = sourceBlob.upload(DATA.getDefaultFlux(), null)
            .flatMap(r -> {
                String sas = sourceBlob.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
                    new BlobContainerSasPermission().setReadPermission(true)));
                BlobUploadFromUrlOptions options = new BlobUploadFromUrlOptions(sourceBlob.getBlobUrl() + "?" + sas)
                    .setSourceRequestConditions(requestConditions);
                return blockBlobAsyncClient.upload(Flux.just(ByteBuffer.wrap(new byte[0])), 0, true)
                    .then(blockBlobAsyncClient.uploadFromUrlWithResponse(options));
            });

        StepVerifier.create(response)
            .verifyErrorSatisfies(r -> {
                BlobStorageException e = assertInstanceOf(BlobStorageException.class, r);
                assertEquals(errorCode, e.getErrorCode());
            });
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
        BlobAsyncClient sourceBlob = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(containerName)
            .getBlobAsyncClient(generateBlobName());
        String sas = sourceBlob.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));
        BlobUploadFromUrlOptions options = new BlobUploadFromUrlOptions(sourceBlob.getBlobUrl() + "?" + sas)
            .setDestinationRequestConditions(requestConditions);

        Mono<Response<BlockBlobItem>> response = sourceBlob.upload(DATA.getDefaultFlux(), null)
            .then(blockBlobAsyncClient.upload(Flux.just(ByteBuffer.wrap(new byte[0])), 0, true))
            .flatMap(r -> {
                if (requestConditions.getLeaseId() != null) {
                    return createLeaseAsyncClient(blobAsyncClient).acquireLease(60);
                }
                return Mono.empty();
            })
            .then(blockBlobAsyncClient.uploadFromUrlWithResponse(options));

        StepVerifier.create(response)
            .verifyErrorSatisfies(r -> {
                BlobStorageException e = assertInstanceOf(BlobStorageException.class, r);
                assertEquals(errorCode, e.getErrorCode());
            });
    }

    private static Stream<Arguments> uploadFromUrlDestinationRequestConditionsSupplier() {
        return Stream.of(
            Arguments.of(new BlobRequestConditions().setIfMatch("dummy"), BlobErrorCode.TARGET_CONDITION_NOT_MET),
            Arguments.of(new BlobRequestConditions().setIfNoneMatch("*"), BlobErrorCode.BLOB_ALREADY_EXISTS),
            Arguments.of(new BlobRequestConditions().setIfModifiedSince(OffsetDateTime.now().plusDays(10)),
                BlobErrorCode.CONDITION_NOT_MET),
            Arguments.of(new BlobRequestConditions().setIfUnmodifiedSince(OffsetDateTime.now().minusDays(1)),
                BlobErrorCode.CONDITION_NOT_MET),
            Arguments.of(new BlobRequestConditions().setLeaseId("9260fd2d-34c1-42b5-9217-8fb7c6484bfb"),
                BlobErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION));
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-06-08")
    @ParameterizedTest
    @MethodSource("uploadFromUrlCopySourceTagsSupplier")
    public void uploadFromUrlCopySourceTags(BlobCopySourceTagsMode mode) {
        Map<String, String> sourceTags = Collections.singletonMap("foo", "bar");
        Map<String, String> destTags = Collections.singletonMap("fizz", "buzz");

        String sas = blockBlobAsyncClient.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobSasPermission().setTagsPermission(true).setReadPermission(true)));

        BlobAsyncClient bc2 = ccAsync.getBlobAsyncClient(generateBlobName());

        BlobCopyFromUrlOptions options = new BlobCopyFromUrlOptions(
            blockBlobAsyncClient.getBlobUrl() + "?" + sas).setCopySourceTagsMode(mode);
        if (BlobCopySourceTagsMode.REPLACE == mode) {
            options.setTags(destTags);
        }

        StepVerifier.create(blockBlobAsyncClient.setTags(sourceTags)
            .then(bc2.copyFromUrlWithResponse(options))
            .then(bc2.getTags()))
            .assertNext(r -> {
                if (BlobCopySourceTagsMode.REPLACE == mode) {
                    assertEquals(destTags, r);
                } else {
                    assertEquals(sourceTags, r);
                }
            })
            .verifyComplete();
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
        BlobAsyncClient sourceBlob = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(containerName)
            .getBlobAsyncClient(generateBlobName());
        String sas = sourceBlob.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));
        BlobUploadFromUrlOptions uploadOptions = new BlobUploadFromUrlOptions(
            sourceBlob.getBlobUrl() + "?" + sas).setTier(AccessTier.COLD);

        Mono<BlobProperties> response = sourceBlob.upload(DATA.getDefaultFlux(), null)
            .then(blockBlobAsyncClient.exists())
            .flatMap(r -> {
                if (r) {
                    return blockBlobAsyncClient.delete();
                }
                return Mono.empty();
            })
            .then(blockBlobAsyncClient.uploadFromUrlWithResponse(uploadOptions))
            .then(blockBlobAsyncClient.getProperties());

        StepVerifier.create(response)
            .assertNext(r -> assertEquals(AccessTier.COLD, r.getAccessTier()))
            .verifyComplete();
    }

    @Test
    public void defaultAudience() {
        BlockBlobAsyncClient aadBlob = getSpecializedBuilderWithTokenCredential(blockBlobAsyncClient.getBlobUrl())
            .audience(null)
            .buildBlockBlobAsyncClient();

        StepVerifier.create(aadBlob.exists())
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    public void storageAccountAudience() {
        BlockBlobAsyncClient aadBlob = getSpecializedBuilderWithTokenCredential(blockBlobAsyncClient.getBlobUrl())
            .audience(BlobAudience.createBlobServiceAccountAudience(ccAsync.getAccountName()))
            .buildBlockBlobAsyncClient();

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
        BlockBlobAsyncClient aadBlob = getSpecializedBuilderWithTokenCredential(blockBlobAsyncClient.getBlobUrl())
            .audience(BlobAudience.createBlobServiceAccountAudience("badAudience"))
            .buildBlockBlobAsyncClient();

        StepVerifier.create(aadBlob.getProperties())
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();
    }

    @Test
    public void audienceFromString() {
        String url = String.format("https://%s.blob.core.windows.net/", ccAsync.getAccountName());
        BlobAudience audience = BlobAudience.fromString(url);

        BlockBlobAsyncClient aadBlob = getSpecializedBuilderWithTokenCredential(blockBlobAsyncClient.getBlobUrl())
            .audience(audience)
            .buildBlockBlobAsyncClient();

        StepVerifier.create(aadBlob.exists())
            .expectNext(true)
            .verifyComplete();
    }
}
