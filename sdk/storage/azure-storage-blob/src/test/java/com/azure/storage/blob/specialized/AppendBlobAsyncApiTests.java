// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.exception.UnexpectedLengthException;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.FluxUtil;
import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.options.AppendBlobCreateOptions;
import com.azure.storage.blob.options.BlobGetTagsOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AppendBlobAsyncApiTests extends BlobTestBase {

    private AppendBlobAsyncClient bc;
    private String blobName;

    @BeforeEach
    public void setup() {
        blobName = generateBlobName();
        bc = ccAsync.getBlobAsyncClient(blobName).getAppendBlobAsyncClient();
        bc.create().block();
    }

    @Test
    public void createDefaults() {
        StepVerifier.create(bc.createWithResponse(null, null, null))
            .assertNext(r -> {
                assertEquals(201, r.getStatusCode());
                validateBasicHeaders(r.getHeaders());
                assertNull(r.getValue().getContentMd5());
                assertTrue(r.getValue().isServerEncrypted());
            })
            .verifyComplete();
    }

    @Test
    public void createMin() {
        assertAsyncResponseStatusCode(
            bc.createWithResponse(null, null, null), 201);
    }

    @Test
    public void createError() {
        StepVerifier.create(bc.createWithResponse(null, null, new BlobRequestConditions()
            .setIfMatch("garbage")))
            .verifyError(BlobStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("createHeadersSupplier")
    public void createHeaders(String cacheControl, String contentDisposition, String contentEncoding,
                              String contentLanguage, byte[] contentMD5, String contentType) throws Exception {

        BlobHttpHeaders headers = new BlobHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType);

        contentType = (contentType == null) ? "application/octet-stream" : contentType;
        String finalContentType = contentType;

        bc.createWithResponse(headers, null, null).block();

        StepVerifier.create(bc.getPropertiesWithResponse(null))
            .assertNext(p -> validateBlobProperties(p, cacheControl, contentDisposition, contentEncoding, contentLanguage, contentMD5,
                finalContentType))
            .verifyComplete();
    }

    private static Stream<Arguments> createHeadersSupplier() throws NoSuchAlgorithmException {
        return Stream.of(
            Arguments.of(null, null, null, null, null, null),
            Arguments.of("control", "disposition", "encoding", "language",
                Base64.getEncoder().encode(MessageDigest.getInstance("MD5").digest(DATA.getDefaultText().getBytes())),
                "type")
        );
    }

    @ParameterizedTest
    @MethodSource("createMetadataSupplier")
    public void createMetadata(String key1, String value1, String key2, String value2) {
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null) {
            metadata.put(key2, value2);
        }

        bc.createWithResponse(null, metadata, null).block();

        StepVerifier.create(bc.getProperties())
            .assertNext(p -> {
                for (Map.Entry<String, String> entry : metadata.entrySet()) {
                    assertEquals(entry.getValue(), p.getMetadata().get(entry.getKey()));
                }
                assertEquals(metadata, p.getMetadata());
            })
            .verifyComplete();
    }

    private static Stream<Arguments> createMetadataSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null),
            Arguments.of("foo", "bar", "fizz", "buzz"));
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("createTagsSupplier")
    public void createTags(String key1, String value1, String key2, String value2) {
        HashMap<String, String> tags = new HashMap<>();
        if (key1 != null) {
            tags.put(key1, value1);
        }
        if (key2 != null) {
            tags.put(key2, value2);
        }

        bc.createWithResponse(new AppendBlobCreateOptions().setTags(tags)).block();

        StepVerifier.create(bc.getTagsWithResponse(new BlobGetTagsOptions()))
            .assertNext(r -> {
                for (Map.Entry<String, String> entry : tags.entrySet()) {
                    assertEquals(entry.getValue(), r.getValue().get(entry.getKey()));
                }
            })
            .verifyComplete();
    }

    private static Stream<Arguments> createTagsSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null),
            Arguments.of("foo", "bar", "fizz", "buzz"));
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("createACSupplier")
    public void createAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
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


        assertAsyncResponseStatusCode(bc.createWithResponse(null, null, bac), 201);
    }

    private Stream<Arguments> createACSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null, null, null),
            Arguments.of(OLD_DATE, null, null, null, null, null),
            Arguments.of(null, NEW_DATE, null, null, null, null),
            Arguments.of(null, null, RECEIVED_ETAG, null, null, null),
            Arguments.of(null, null, null, GARBAGE_ETAG, null, null),
            Arguments.of(null, null, null, null, RECEIVED_LEASE_ID, null),
            Arguments.of(null, null, null, null, null, "\"foo\" = 'bar'")
        );
    }

    @ParameterizedTest
    @MethodSource("createACFailSupplier")
    public void createACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
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

        StepVerifier.create(bc.createWithResponse(null, null, bac))
            .verifyError(BlobStorageException.class);
    }

    private Stream<Arguments> createACFailSupplier() {
        return Stream.of(
            Arguments.of(NEW_DATE, null, null, null, null, null),
            Arguments.of(null, OLD_DATE, null, null, null, null),
            Arguments.of(null, null, GARBAGE_ETAG, null, null, null),
            Arguments.of(null, null, null, RECEIVED_ETAG, null, null),
            Arguments.of(null, null, null, null, GARBAGE_LEASE_ID, null),
            Arguments.of(null, null, null, null, null, "\"notfoo\" = 'notbar'")
        );
    }

    @Test
    public void createIfNotExistsDefaults() {
        String blobName = cc.getBlobClient(generateBlobName()).getBlobName();
        bc = ccAsync.getBlobAsyncClient(blobName).getAppendBlobAsyncClient();

        StepVerifier.create(bc.createIfNotExistsWithResponse(new AppendBlobCreateOptions()))
            .assertNext(r -> {
                assertResponseStatusCode(r, 201);
                validateBasicHeaders(r.getHeaders());
                assertNull(r.getValue().getContentMd5());
                assertTrue(r.getValue().isServerEncrypted());
            })
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsMin() {
        String blobName = ccAsync.getBlobAsyncClient(generateBlobName()).getBlobName();
        bc = ccAsync.getBlobAsyncClient(blobName).getAppendBlobAsyncClient();

        assertAsyncResponseStatusCode(bc.createIfNotExistsWithResponse(null), 201);
    }

    @Test
    public void createIfNotExistsOnABlobThatAlreadyExists() {
        String blobName = ccAsync.getBlobAsyncClient(generateBlobName()).getBlobName();
        bc = ccAsync.getBlobAsyncClient(blobName).getAppendBlobAsyncClient();

        assertAsyncResponseStatusCode(bc.createIfNotExistsWithResponse(new AppendBlobCreateOptions()), 201);
        assertAsyncResponseStatusCode(bc.createIfNotExistsWithResponse(new AppendBlobCreateOptions()), 409);
    }

    @ParameterizedTest
    @MethodSource("createHeadersSupplier")
    public void createIfNotExistsHeaders(String cacheControl, String contentDisposition, String contentEncoding,
                                         String contentLanguage, byte[] contentMD5, String contentType) {
        String blobName = ccAsync.getBlobAsyncClient(generateBlobName()).getBlobName();
        bc = ccAsync.getBlobAsyncClient(blobName).getAppendBlobAsyncClient();

        BlobHttpHeaders headers = new BlobHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType);
        AppendBlobCreateOptions options = new AppendBlobCreateOptions().setHeaders(headers);

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType;
        String finalContentType = contentType;

        bc.createIfNotExistsWithResponse(options).block();

        StepVerifier.create(bc.getPropertiesWithResponse(null))
            .assertNext(p -> {
                validateBlobProperties(p, cacheControl, contentDisposition, contentEncoding, contentLanguage, contentMD5,
                    finalContentType);
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createMetadataSupplier")
    public void createIfNotExistsMetadata(String key1, String value1, String key2, String value2) {
        String blobName = ccAsync.getBlobAsyncClient(generateBlobName()).getBlobName();
        bc = ccAsync.getBlobAsyncClient(blobName).getAppendBlobAsyncClient();

        Map<String, String> metadata = new HashMap<>();
        if (key1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null) {
            metadata.put(key2, value2);
        }

        AppendBlobCreateOptions options = new AppendBlobCreateOptions().setMetadata(metadata);

        bc.createIfNotExistsWithResponse(options).block();

        StepVerifier.create(bc.getProperties())
            .assertNext(p -> {
                for (Map.Entry<String, String> entry : metadata.entrySet()) {
                    assertEquals(entry.getValue(), p.getMetadata().get(entry.getKey()));
                }
            })
            .verifyComplete();
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("createIfNotExistsTagsSupplier")
    public void createIfNotExistsTags(String key1, String value1, String key2, String value2) {
        bc.delete().block();
        Map<String, String> tags = new HashMap<>();
        if (key1 != null) {
            tags.put(key1, value1);
        }
        if (key2 != null) {
            tags.put(key2, value2);
        }

        bc.createIfNotExistsWithResponse(new AppendBlobCreateOptions().setTags(tags)).block();

        StepVerifier.create(bc.getTagsWithResponse(new BlobGetTagsOptions()))
            .assertNext(r -> {
                for (Map.Entry<String, String> entry : tags.entrySet()) {
                    assertEquals(entry.getValue(), r.getValue().get(entry.getKey()));
                }
            })
            .verifyComplete();
    }

    private Stream<Arguments> createIfNotExistsTagsSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null),
            Arguments.of("foo", "bar", "fizz", "buzz"),
            Arguments.of(" +-./:=_  +-./:=_", " +-./:=_", null, null));
    }

    @Test
    public void appendBlockDefaults() {
        StepVerifier.create(bc.appendBlockWithResponse(DATA.getDefaultFlux(),
            DATA.getDefaultDataSize(), null, null))
            .assertNext(r -> {
                validateBasicHeaders(r.getHeaders());
                assertNotNull(r.getHeaders().getValue(X_MS_CONTENT_CRC64));
                assertNotNull(r.getValue().getBlobAppendOffset());
                assertNotNull(r.getValue().getBlobCommittedBlockCount());
            })
            .verifyComplete();

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(bc.downloadStream()))
            .assertNext(r -> TestUtils.assertArraysEqual(DATA.getDefaultBytes(), r))
            .verifyComplete();
    }

    @Test
    public void appendBlockMin() {
        assertAsyncResponseStatusCode(bc.appendBlockWithResponse(DATA.getDefaultFlux(),
            DATA.getDefaultDataSize(), null, null), 201);
    }

    @ParameterizedTest
    @MethodSource("appendBlockIASupplier")
    public void appendBlockIA(Flux<ByteBuffer> stream, long dataSize, Class<? extends Throwable> exceptionType) {
        StepVerifier.create(bc.appendBlock(stream, dataSize))
            .verifyError(exceptionType);
    }

    private static Stream<Arguments> appendBlockIASupplier() {
        return Stream.of(
            Arguments.of(null, DATA.getDefaultDataSize(), NullPointerException.class),
            Arguments.of(DATA.getDefaultFlux(), DATA.getDefaultDataSize() + 1, UnexpectedLengthException.class),
            Arguments.of(DATA.getDefaultFlux(), DATA.getDefaultDataSize() - 1, UnexpectedLengthException.class)
        );
    }

    @Test
    public void appendBlockEmptyBody() {
        StepVerifier.create(bc.appendBlock(Flux.just(ByteBuffer.wrap(new byte[0])), 0))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void appendBlockNullBody() {
        assertThrows(NullPointerException.class, () -> bc.appendBlock(Flux.just((ByteBuffer) null), 0));
    }

    @Test
    public void appendBlockTransactionalMD5() throws NoSuchAlgorithmException {
        byte[] md5 = MessageDigest.getInstance("MD5").digest(DATA.getDefaultBytes());

        assertAsyncResponseStatusCode(bc.appendBlockWithResponse(DATA.getDefaultFlux(), DATA.getDefaultDataSize(),
            md5, null), 201);
    }

    @Test
    public void appendBlockTransactionalMD5Fail() throws NoSuchAlgorithmException {
        StepVerifier.create(bc.appendBlockWithResponse(DATA.getDefaultFlux(), DATA.getDefaultDataSize(),
                MessageDigest.getInstance("MD5").digest("garbage".getBytes()), null))
            .verifyErrorSatisfies(r -> {
                BlobStorageException e = assertInstanceOf(BlobStorageException.class, r);
                assertExceptionStatusCodeAndMessage(e, 400, BlobErrorCode.MD5MISMATCH);
            });
    }



}
