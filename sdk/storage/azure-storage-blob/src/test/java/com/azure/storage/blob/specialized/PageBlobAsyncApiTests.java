// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.exception.UnexpectedLengthException;
import com.azure.core.http.rest.Response;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.FluxUtil;
import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.PageBlobItem;
import com.azure.storage.blob.models.PageBlobRequestConditions;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.options.BlobGetTagsOptions;
import com.azure.storage.blob.options.PageBlobCreateOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PageBlobAsyncApiTests extends BlobTestBase {

    private PageBlobAsyncClient bc;
    private String blobName;

    @BeforeEach
    public void setup() {
        blobName = generateBlobName();
        bc = ccAsync.getBlobAsyncClient(blobName).getPageBlobAsyncClient();
        bc.create(PageBlobClient.PAGE_BYTES).block();
    }

    @Test
    public void createAllNull() {
        bc = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();

        StepVerifier.create(bc.createWithResponse(PageBlobClient.PAGE_BYTES, null, null, null, null))
            .assertNext(r -> {
                assertResponseStatusCode(r, 201);
                assertTrue(validateBasicHeaders(r.getHeaders()));
                assertNull(r.getValue().getContentMd5());
                assertTrue(r.getValue().isServerEncrypted());
            })
            .verifyComplete();
    }

    @Test
    public void createMin() {
        assertAsyncResponseStatusCode(bc.createWithResponse(PageBlobClient.PAGE_BYTES, null, null, null, null),
            201);
    }

    @Test
    public void createSequenceNumber() {
        bc.createWithResponse(PageBlobClient.PAGE_BYTES, 2L, null, null, null).block();
        StepVerifier.create(bc.getProperties())
            .assertNext(r -> assertEquals(r.getBlobSequenceNumber(), 2))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHeadersSupplier")
    public void createHeaders(String cacheControl, String contentDisposition, String contentEncoding,
                              String contentLanguage, byte[] contentMD5, String contentType) {
        BlobHttpHeaders headers = new BlobHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType);

        bc.createWithResponse(PageBlobClient.PAGE_BYTES, null, headers, null, null).block();

        contentType = (contentType == null) ? "application/octet-stream" : contentType;
        String finalContentType = contentType;

        StepVerifier.create(bc.getPropertiesWithResponse(null))
            .assertNext(r -> {
                assertTrue(validateBlobProperties(r, cacheControl, contentDisposition, contentEncoding, contentLanguage,
                    contentMD5, finalContentType));
            })
            .verifyComplete();
    }

    private static Stream<Arguments> createHeadersSupplier() throws NoSuchAlgorithmException {
        return Stream.of(Arguments.of(null, null, null, null, null, null),
            Arguments.of("control", "disposition", "encoding", "language",
                Base64.getEncoder().encode(MessageDigest.getInstance("MD5").digest(DATA.getDefaultBytes())),
                "type"));
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
        bc.createWithResponse(PageBlobClient.PAGE_BYTES, null, null, metadata, null).block();
        StepVerifier.create(bc.getPropertiesWithResponse(null))
            .assertNext(r -> {
                assertResponseStatusCode(r, 200);
                assertEquals(r.getValue().getMetadata(), metadata);
            })
            .verifyComplete();
    }

    private static Stream<Arguments> createMetadataSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null),
            Arguments.of("foo", "bar", "fizz", "buzz")
        );
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("createTagsSupplier")
    public void createTags(String key1, String value1, String key2, String value2) {
        Map<String, String> tags = new HashMap<>();
        if (key1 != null) {
            tags.put(key1, value1);
        }
        if (key2 != null) {
            tags.put(key2, value2);
        }

        bc.createWithResponse(new PageBlobCreateOptions(PageBlobClient.PAGE_BYTES).setTags(tags)).block();

        StepVerifier.create(bc.getTagsWithResponse(new BlobGetTagsOptions()))
            .assertNext(r -> {
                assertResponseStatusCode(r, 200);
                assertEquals(r.getValue(), tags);
            })
            .verifyComplete();
    }

    private static Stream<Arguments> createTagsSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null),
            Arguments.of("foo", "bar", "fizz", "buzz"),
            Arguments.of(" +-./:=_  +-./:=_", " +-./:=_", null, null)
        );
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void createAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
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

        assertAsyncResponseStatusCode(bc.createWithResponse(PageBlobClient.PAGE_BYTES, null, null, null, bac),
            201);
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsFailSupplier")
    public void createACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                             String leaseID, String tags) {
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupBlobMatchCondition(bc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        StepVerifier.create(bc.createWithResponse(PageBlobClient.PAGE_BYTES, null, null,
            null, bac))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void createError() {
        StepVerifier.create(bc.createWithResponse(PageBlobClient.PAGE_BYTES,
            null, null, null, new BlobRequestConditions().setLeaseId("id")))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void createIfNotExistsAllNull() {
        bc = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();

        StepVerifier.create(bc.createIfNotExistsWithResponse(
            new PageBlobCreateOptions(PageBlobClient.PAGE_BYTES)))
            .assertNext(r -> {
                assertResponseStatusCode(r, 201);
                assertTrue(validateBasicHeaders(r.getHeaders()));
                assertNull(r.getValue().getContentMd5());
                assertTrue(r.getValue().isServerEncrypted());
            })
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsBlobThatAlreadyExists() {
        String blobName = ccAsync.getBlobAsyncClient(generateBlobName()).getBlobName();
        bc = ccAsync.getBlobAsyncClient(blobName).getPageBlobAsyncClient();
        PageBlobCreateOptions options = new PageBlobCreateOptions(PageBlobClient.PAGE_BYTES);

        assertAsyncResponseStatusCode(bc.createIfNotExistsWithResponse(options), 201);
        assertAsyncResponseStatusCode(bc.createIfNotExistsWithResponse(options), 409);
    }

    @Test
    public void createIfNotExistsMin() {
        String blobName = ccAsync.getBlobAsyncClient(generateBlobName()).getBlobName();
        bc = ccAsync.getBlobAsyncClient(blobName).getPageBlobAsyncClient();

        assertAsyncResponseStatusCode(bc.createIfNotExistsWithResponse(new PageBlobCreateOptions(PageBlobClient.PAGE_BYTES)), 201);
    }

    @Test
    public void createIfNotExistsSequenceNumber() {
        String blobName = ccAsync.getBlobAsyncClient(generateBlobName()).getBlobName();
        bc = ccAsync.getBlobAsyncClient(blobName).getPageBlobAsyncClient();

        bc.createIfNotExistsWithResponse(new PageBlobCreateOptions(PageBlobClient.PAGE_BYTES).setSequenceNumber(2L)).block();
    }

    @ParameterizedTest
    @MethodSource("createIfNotExistsHeadersSupplier")
    public void createIfNotExistsHeaders(String cacheControl, String contentDisposition, String contentEncoding,
                                         String contentLanguage, byte[] contentMD5, String contentType) {
        String blobName = ccAsync.getBlobAsyncClient(generateBlobName()).getBlobName();
        bc = ccAsync.getBlobAsyncClient(blobName).getPageBlobAsyncClient();

        BlobHttpHeaders headers = new BlobHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType);

        bc.createIfNotExistsWithResponse(new PageBlobCreateOptions(PageBlobClient.PAGE_BYTES).setHeaders(headers)).block();

        contentType = (contentType == null) ? "application/octet-stream" : contentType;
        String finalContentType = contentType;

        StepVerifier.create(bc.getPropertiesWithResponse(null))
            .assertNext(r -> {
                assertTrue(validateBlobProperties(r, cacheControl, contentDisposition, contentEncoding, contentLanguage,
                    contentMD5, finalContentType));
            })
            .verifyComplete();
    }

    private static Stream<Arguments> createIfNotExistsHeadersSupplier() throws NoSuchAlgorithmException {
        return Stream.of(
            Arguments.of(null, null, null, null, null, null),
            Arguments.of("control", "disposition", "encoding", "language",
                MessageDigest.getInstance("MD5").digest(DATA.getDefaultBytes()), "type"));
    }

    @ParameterizedTest
    @MethodSource("createIfNotExistsMetadataSupplier")
    public void createIfNotExistsMetadata(String key1, String value1, String key2, String value2) {
        String blobName = ccAsync.getBlobAsyncClient(generateBlobName()).getBlobName();
        bc = ccAsync.getBlobAsyncClient(blobName).getPageBlobAsyncClient();

        Map<String, String> metadata = new HashMap<>();
        if (key1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null) {
            metadata.put(key2, value2);
        }

        bc.createIfNotExistsWithResponse(new PageBlobCreateOptions(PageBlobClient.PAGE_BYTES).setMetadata(metadata)).block();

        StepVerifier.create(bc.getPropertiesWithResponse(null))
            .assertNext(r -> {
                assertResponseStatusCode(r, 200);
                assertEquals(r.getValue().getMetadata(), metadata);
            })
            .verifyComplete();
    }

    private static Stream<Arguments> createIfNotExistsMetadataSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null),
            Arguments.of("foo", "bar", "fizz", "buzz"));
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("createIfNotExistsTagsSupplier")
    public void createIfNotExistsTags(String key1, String value1, String key2, String value2) {
        String blobName = ccAsync.getBlobAsyncClient(generateBlobName()).getBlobName();
        bc = ccAsync.getBlobAsyncClient(blobName).getPageBlobAsyncClient();

        Map<String, String> tags = new HashMap<>();
        if (key1 != null) {
            tags.put(key1, value1);
        }
        if (key2 != null) {
            tags.put(key2, value2);
        }

        bc.createIfNotExistsWithResponse(new PageBlobCreateOptions(PageBlobClient.PAGE_BYTES).setTags(tags)).block();

        StepVerifier.create(bc.getTagsWithResponse(new BlobGetTagsOptions()))
            .assertNext(r -> {
                assertResponseStatusCode(r, 200);
                assertEquals(r.getValue(), tags);
            })
            .verifyComplete();
    }

    private static Stream<Arguments> createIfNotExistsTagsSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null),
            Arguments.of("foo", "bar", "fizz", "buzz"),
            Arguments.of(" +-./:=_  +-./:=_", " +-./:=_", null, null));
    }

    @Test
    public void uploadPage() {
        StepVerifier.create(bc.uploadPagesWithResponse(
            new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1), Flux.just(ByteBuffer.wrap(getRandomByteArray(PageBlobClient.PAGE_BYTES))), null, null))
            .assertNext(r -> {
                assertResponseStatusCode(r, 201);
                assertTrue(validateBasicHeaders(r.getHeaders()));
                assertNotNull(r.getHeaders().getValue("x-ms-content-crc64"));
                assertEquals(r.getValue().getBlobSequenceNumber(), 0);
                assertTrue(r.getValue().isServerEncrypted());
            })
            .verifyComplete();
    }

    @Test
    public void uploadPageMin() {
        assertAsyncResponseStatusCode(bc.uploadPagesWithResponse(
            new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1), Flux.just(ByteBuffer.wrap(getRandomByteArray(PageBlobClient.PAGE_BYTES))), null, null), 201);
    }

    @ParameterizedTest
    @MethodSource("uploadPageIASupplier")
    public void uploadPageIA(Integer dataSize, Throwable exceptionType) {
        Flux<ByteBuffer> data = (dataSize == null) ? null : Flux.just(ByteBuffer.wrap(getRandomByteArray(PageBlobClient.PAGE_BYTES)));

        StepVerifier.create(bc.uploadPages(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES * 2 - 1), data))
            .verifyError(exceptionType.getClass());
    }

    private static Stream<Arguments> uploadPageIASupplier() {
        return Stream.of(
            Arguments.of(null, new NullPointerException()),
            Arguments.of(PageBlobClient.PAGE_BYTES, new UnexpectedLengthException(null, 0L, 0L /* dummy values */)),
            Arguments.of(PageBlobClient.PAGE_BYTES * 3, new UnexpectedLengthException(null, 0L, 0L /* dummy values */))
        );
    }

    @Test
    public void uploadPageTransactionalMD5() throws NoSuchAlgorithmException {
        byte[] data = getRandomByteArray(PageBlobClient.PAGE_BYTES);
        byte[] md5 = MessageDigest.getInstance("MD5").digest(data);

        assertAsyncResponseStatusCode(bc.uploadPagesWithResponse(
            new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            Flux.just(ByteBuffer.wrap(data)), md5, null), 201);
    }

    @Test
    public void uploadPageTransactionalMD5Fail() throws NoSuchAlgorithmException {

        StepVerifier.create(bc.uploadPagesWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            Flux.just(ByteBuffer.wrap(getRandomByteArray(PageBlobClient.PAGE_BYTES))), MessageDigest.getInstance("MD5").digest("garbage".getBytes()), null))
            .verifyErrorSatisfies(r -> {
                BlobStorageException e = assertInstanceOf(BlobStorageException.class, r);
                assertEquals(BlobErrorCode.MD5MISMATCH, e.getErrorCode());
            });
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("uploadPageACSupplier")
    public void uploadPageAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                             String leaseID, Long sequenceNumberLT, Long sequenceNumberLTE, Long sequenceNumberEqual, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t).block();
        PageBlobRequestConditions pac = new PageBlobRequestConditions()
            .setLeaseId(setupBlobLeaseCondition(bc, leaseID))
            .setIfMatch(setupBlobMatchCondition(bc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfSequenceNumberLessThan(sequenceNumberLT)
            .setIfSequenceNumberLessThanOrEqualTo(sequenceNumberLTE)
            .setIfSequenceNumberEqualTo(sequenceNumberEqual)
            .setTagsConditions(tags);

        assertAsyncResponseStatusCode(bc.uploadPagesWithResponse(
            new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1), Flux.just(ByteBuffer.wrap(getRandomByteArray(PageBlobClient.PAGE_BYTES))), null, pac), 201);
    }

    private static Stream<Arguments> uploadPageACSupplier() {
        return Stream.of(Arguments.of(null, null, null, null, null, null, null, null, null),
            Arguments.of(OLD_DATE, null, null, null, null, null, null, null, null),
            Arguments.of(null, NEW_DATE, null, null, null, null, null, null, null),
            Arguments.of(null, null, RECEIVED_ETAG, null, null, null, null, null, null),
            Arguments.of(null, null, null, GARBAGE_ETAG, null, null, null, null, null),
            Arguments.of(null, null, null, null, RECEIVED_LEASE_ID, null, null, null, null),
            Arguments.of(null, null, null, null, null, 5L, null, null, null),
            Arguments.of(null, null, null, null, null, null, 3L, null, null),
            Arguments.of(null, null, null, null, null, null, null, 0L, null),
            Arguments.of(null, null, null, null, null, null, null, null, "\"foo\" = 'bar'"));
    }

    @ParameterizedTest
    @MethodSource("uploadPageACFailSupplier")
    public void uploadPageACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                 String leaseID, Long sequenceNumberLT, Long sequenceNumberLTE, Long sequenceNumberEqual, String tags) {
        noneMatch = setupBlobMatchCondition(bc, noneMatch);
        setupBlobLeaseCondition(bc, leaseID);
        PageBlobRequestConditions pac = new PageBlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfSequenceNumberLessThan(sequenceNumberLT)
            .setIfSequenceNumberLessThanOrEqualTo(sequenceNumberLTE)
            .setIfSequenceNumberEqualTo(sequenceNumberEqual)
            .setTagsConditions(tags);

        StepVerifier.create(bc.uploadPagesWithResponse(
            new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1), Flux.just(ByteBuffer.wrap(getRandomByteArray(PageBlobClient.PAGE_BYTES))), null, pac))
            .verifyError(BlobStorageException.class);
    }

    private static Stream<Arguments> uploadPageACFailSupplier() {
        return Stream.of(
            Arguments.of(NEW_DATE, null, null, null, null, null, null, null, null),
            Arguments.of(null, OLD_DATE, null, null, null, null, null, null, null),
            Arguments.of(null, null, GARBAGE_ETAG, null, null, null, null, null, null),
            Arguments.of(null, null, null, RECEIVED_ETAG, null, null, null, null, null),
            Arguments.of(null, null, null, null, GARBAGE_LEASE_ID, null, null, null, null),
            Arguments.of(null, null, null, null, null, -1L, null, null, null),
            Arguments.of(null, null, null, null, null, null, -1L, null, null),
            Arguments.of(null, null, null, null, null, null, null, 100L, null),
            Arguments.of(null, null, null, null, null, null, null, null, "\"notfoo\" = 'notbar'"));
    }

    @Test
    public void uploadPageError() {
        bc = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();

        StepVerifier.create(bc.uploadPagesWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            Flux.just(ByteBuffer.wrap(getRandomByteArray(PageBlobClient.PAGE_BYTES))), null, new PageBlobRequestConditions().setLeaseId("id")))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void uploadPageRetryOnTransientFailure() {
        PageBlobAsyncClient clientWithFailure = getBlobAsyncClient(
            ENVIRONMENT.getPrimaryAccount().getCredential(), bc.getBlobUrl(),
            new TransientFailureInjectingHttpPipelinePolicy()).getPageBlobAsyncClient();

        byte[] data = getRandomByteArray(PageBlobClient.PAGE_BYTES);

        clientWithFailure.uploadPages(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            Flux.just(ByteBuffer.wrap(data))).block();

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(bc.downloadStream()))
            .assertNext(r ->  TestUtils.assertArraysEqual(r, data))
            .verifyComplete();
    }

    @Test
    public void uploadPageFromURLMin() {
        ccAsync.setAccessPolicy(PublicAccessType.CONTAINER, null).block();
        PageBlobAsyncClient destURL = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        destURL.create(PageBlobClient.PAGE_BYTES).block();
        PageRange pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1);
        destURL.uploadPages(pageRange, Flux.just(ByteBuffer.wrap(getRandomByteArray(PageBlobClient.PAGE_BYTES)))).block();

        StepVerifier.create(bc.uploadPagesFromUrlWithResponse(pageRange, destURL.getBlobUrl(), null, null,
            null, null))
            .assertNext(r -> {
                assertResponseStatusCode(r, 201);
                assertTrue(validateBasicHeaders(r.getHeaders()));
            })
            .verifyComplete();
    }

    @Test
    public void uploadPageFromURLRange() {
        ccAsync.setAccessPolicy(PublicAccessType.CONTAINER, null).block();

        byte[] data = getRandomByteArray(PageBlobClient.PAGE_BYTES * 4);

        PageBlobAsyncClient sourceURL = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        sourceURL.create(PageBlobClient.PAGE_BYTES * 4).block();
        sourceURL.uploadPages(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES * 4 - 1),
            Flux.just(ByteBuffer.wrap(data))).block();

        PageBlobAsyncClient destURL = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        destURL.create(PageBlobClient.PAGE_BYTES * 2).block();

        destURL.uploadPagesFromUrl(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES * 2 - 1),
            sourceURL.getBlobUrl(), PageBlobClient.PAGE_BYTES * 2L).block();

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(destURL.downloadStream()))
            .assertNext(r -> TestUtils.assertArraysEqual(data, PageBlobClient.PAGE_BYTES * 2, r, 0,
                PageBlobClient.PAGE_BYTES * 2))
            .verifyComplete();
    }

    @Test
    public void uploadPageFromURLIA() {
        StepVerifier.create(bc.uploadPagesFromUrl(null, bc.getBlobUrl(), (long) PageBlobClient.PAGE_BYTES))
            .verifyError(IllegalArgumentException.class);
    }

    @Test
    public void uploadPageFromURLMD5() throws NoSuchAlgorithmException {
        ccAsync.setAccessPolicy(PublicAccessType.CONTAINER, null).block();

        PageBlobAsyncClient destURL = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        destURL.create(PageBlobClient.PAGE_BYTES).block();

        byte[] data = getRandomByteArray(PageBlobClient.PAGE_BYTES);
        PageRange pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1);
        bc.uploadPages(pageRange, Flux.just(ByteBuffer.wrap(data))).block();

        StepVerifier.create(destURL.uploadPagesFromUrlWithResponse(pageRange, bc.getBlobUrl(), null,
            MessageDigest.getInstance("MD5").digest(data), null, null))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void uploadPageFromURLMD5Fail() throws NoSuchAlgorithmException {
        ccAsync.setAccessPolicy(PublicAccessType.CONTAINER, null).block();
        PageBlobAsyncClient destURL = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        destURL.create(PageBlobClient.PAGE_BYTES).block();

        PageRange pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1);
        bc.uploadPages(pageRange, Flux.just(ByteBuffer.wrap(getRandomByteArray(PageBlobClient.PAGE_BYTES)))).block();

        StepVerifier.create(destURL.uploadPagesFromUrlWithResponse(pageRange, bc.getBlobUrl(), null,
            MessageDigest.getInstance("MD5").digest("garbage".getBytes()), null, null))
            .verifyError(BlobStorageException.class);
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("uploadPageACSupplier")
    public void uploadPageFromURLDestinationAC(OffsetDateTime modified, OffsetDateTime unmodified, String match,
                                               String noneMatch, String leaseID, Long sequenceNumberLT, Long sequenceNumberLTE, Long sequenceNumberEqual,
                                               String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t).block();
        ccAsync.setAccessPolicy(PublicAccessType.CONTAINER, null).block();
        PageBlobAsyncClient sourceURL = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        sourceURL.create(PageBlobClient.PAGE_BYTES).block();
        PageRange pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1);
        sourceURL.uploadPages(pageRange, Flux.just(ByteBuffer.wrap(getRandomByteArray(PageBlobClient.PAGE_BYTES)))).block();

        PageBlobRequestConditions pac = new PageBlobRequestConditions()
            .setLeaseId(setupBlobLeaseCondition(bc, leaseID))
            .setIfMatch(setupBlobMatchCondition(bc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfSequenceNumberLessThan(sequenceNumberLT)
            .setIfSequenceNumberLessThanOrEqualTo(sequenceNumberLTE)
            .setIfSequenceNumberEqualTo(sequenceNumberEqual)
            .setTagsConditions(tags);

        assertAsyncResponseStatusCode(bc.uploadPagesFromUrlWithResponse(pageRange, sourceURL.getBlobUrl(), null, null, pac,
            null), 201);
    }

    @ParameterizedTest
    @MethodSource("uploadPageACFailSupplier")
    public void uploadPageFromURLDestinationACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
                                                   String noneMatch, String leaseID, Long sequenceNumberLT, Long sequenceNumberLTE, Long sequenceNumberEqual,
                                                   String tags) {
        ccAsync.setAccessPolicy(PublicAccessType.CONTAINER, null).block();

        PageBlobAsyncClient sourceURL = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        sourceURL.create(PageBlobClient.PAGE_BYTES).block();
        PageRange pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1);
        sourceURL.uploadPages(pageRange, Flux.just(ByteBuffer.wrap(getRandomByteArray(PageBlobClient.PAGE_BYTES)))).block();

        noneMatch = setupBlobMatchCondition(bc, noneMatch);
        PageBlobRequestConditions pac = new PageBlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfSequenceNumberLessThan(sequenceNumberLT)
            .setIfSequenceNumberLessThanOrEqualTo(sequenceNumberLTE)
            .setIfSequenceNumberEqualTo(sequenceNumberEqual)
            .setTagsConditions(tags);

        StepVerifier.create(bc.uploadPagesFromUrlWithResponse(
            pageRange, sourceURL.getBlobUrl(), null, null, pac, null))
            .verifyError(BlobStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("uploadPageFromURLSourceACSupplier")
    public void uploadPageFromURLSourceAC(OffsetDateTime sourceIfModifiedSince, OffsetDateTime sourceIfUnmodifiedSince,
                                          String sourceIfMatch, String sourceIfNoneMatch) {
        ccAsync.setAccessPolicy(PublicAccessType.CONTAINER, null).block();
        PageBlobAsyncClient sourceURL = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        sourceURL.create(PageBlobClient.PAGE_BYTES).block();
        PageRange pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1);
        sourceURL.uploadPages(pageRange, Flux.just(ByteBuffer.wrap(getRandomByteArray(PageBlobClient.PAGE_BYTES)))).block();

        sourceIfMatch = setupBlobMatchCondition(sourceURL, sourceIfMatch);
        BlobRequestConditions smac = new BlobRequestConditions()
            .setIfModifiedSince(sourceIfModifiedSince)
            .setIfUnmodifiedSince(sourceIfUnmodifiedSince)
            .setIfMatch(sourceIfMatch)
            .setIfNoneMatch(sourceIfNoneMatch);

        assertAsyncResponseStatusCode(bc.uploadPagesFromUrlWithResponse(pageRange, sourceURL.getBlobUrl(), null, null, null,
            smac), 201);
    }

    private static Stream<Arguments> uploadPageFromURLSourceACSupplier() {
        return Stream.of(Arguments.of(null, null, null, null),
            Arguments.of(OLD_DATE, null, null, null),
            Arguments.of(null, NEW_DATE, null, null),
            Arguments.of(null, null, RECEIVED_ETAG, null),
            Arguments.of(null, null, null, GARBAGE_ETAG));
    }

    @ParameterizedTest
    @MethodSource("uploadPageFromURLSourceACFailSupplier")
    public void uploadPageFromURLSourceACFail(OffsetDateTime sourceIfModifiedSince,
                                              OffsetDateTime sourceIfUnmodifiedSince, String sourceIfMatch, String sourceIfNoneMatch) {
        ccAsync.setAccessPolicy(PublicAccessType.CONTAINER, null).block();
        PageBlobAsyncClient sourceURL = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        sourceURL.create(PageBlobClient.PAGE_BYTES).block();
        PageRange pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1);
        sourceURL.uploadPages(pageRange, Flux.just(ByteBuffer.wrap(getRandomByteArray(PageBlobClient.PAGE_BYTES)))).block();

        BlobRequestConditions smac = new BlobRequestConditions()
            .setIfModifiedSince(sourceIfModifiedSince)
            .setIfUnmodifiedSince(sourceIfUnmodifiedSince)
            .setIfMatch(sourceIfMatch)
            .setIfNoneMatch(setupBlobMatchCondition(sourceURL, sourceIfNoneMatch));

        StepVerifier.create(bc.uploadPagesFromUrlWithResponse(
            pageRange, sourceURL.getBlobUrl(), null, null, null, smac))
            .verifyError(BlobStorageException.class);
    }

    private static Stream<Arguments> uploadPageFromURLSourceACFailSupplier() {
        return Stream.of(
            Arguments.of(NEW_DATE, null, null, null),
            Arguments.of(null, OLD_DATE, null, null),
            Arguments.of(null, null, GARBAGE_ETAG, null),
            Arguments.of(null, null, null, RECEIVED_ETAG));
    }




}
