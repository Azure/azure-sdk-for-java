// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.exception.UnexpectedLengthException;
import com.azure.core.http.HttpRange;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.blob.models.BlobAudience;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.ClearRange;
import com.azure.storage.blob.models.CopyStatusType;
import com.azure.storage.blob.models.PageBlobCopyIncrementalRequestConditions;
import com.azure.storage.blob.models.PageBlobItem;
import com.azure.storage.blob.models.PageBlobRequestConditions;
import com.azure.storage.blob.models.PageList;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.models.PageRangeItem;
import com.azure.storage.blob.models.SequenceNumberActionType;
import com.azure.storage.blob.options.BlobGetTagsOptions;
import com.azure.storage.blob.options.ListPageRangesDiffOptions;
import com.azure.storage.blob.options.ListPageRangesOptions;
import com.azure.storage.blob.options.PageBlobCopyIncrementalOptions;
import com.azure.storage.blob.options.PageBlobCreateOptions;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import com.azure.storage.common.test.shared.policy.TransientFailureInjectingHttpPipelinePolicy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
        StepVerifier.create(bc.createWithResponse(PageBlobClient.PAGE_BYTES, 2L, null,
            null, null).then(bc.getProperties()))
            .assertNext(r -> assertEquals(2, r.getBlobSequenceNumber()))
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

        contentType = (contentType == null) ? "application/octet-stream" : contentType;
        String finalContentType = contentType;

        StepVerifier.create(bc.createWithResponse(PageBlobClient.PAGE_BYTES, null, headers,
            null, null).then(bc.getPropertiesWithResponse(null)))
            .assertNext(r ->
                assertTrue(validateBlobProperties(r, cacheControl, contentDisposition, contentEncoding, contentLanguage,
                    contentMD5, finalContentType)))
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
        StepVerifier.create(bc.createWithResponse(PageBlobClient.PAGE_BYTES, null, null, metadata, null)
            .then(bc.getPropertiesWithResponse(null)))
            .assertNext(r -> {
                assertResponseStatusCode(r, 200);
                assertEquals(metadata, r.getValue().getMetadata());
            })
            .verifyComplete();
    }

    private static Stream<Arguments> createMetadataSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null),
            Arguments.of("foo", "bar", "fizz", "buzz")
        );
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
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

        StepVerifier.create(bc.createWithResponse(new PageBlobCreateOptions(PageBlobClient.PAGE_BYTES).setTags(tags))
            .then(bc.getTagsWithResponse(new BlobGetTagsOptions())))
            .assertNext(r -> {
                assertResponseStatusCode(r, 200);
                assertEquals(tags, r.getValue());
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

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void createAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                         String leaseID, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");

        Mono<Response<PageBlobItem>> response = bc.setTags(t)
            .then(Mono.zip(setupBlobLeaseCondition(bc, leaseID), setupBlobMatchCondition(bc, match)))
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
                return bc.createWithResponse(PageBlobClient.PAGE_BYTES, null, null, null, bac);
            });

        assertAsyncResponseStatusCode(response, 201);
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsFailSupplier")
    public void createACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                             String leaseID, String tags) {

        Mono<Response<PageBlobItem>> response = setupBlobMatchCondition(bc, noneMatch).flatMap(r -> {
            if ("null".equals(r)) {
                r = null;
            }
            BlobRequestConditions bac = new BlobRequestConditions()
                .setLeaseId(leaseID)
                .setIfMatch(match)
                .setIfNoneMatch(r)
                .setIfModifiedSince(modified)
                .setIfUnmodifiedSince(unmodified)
                .setTagsConditions(tags);

            return bc.createWithResponse(PageBlobClient.PAGE_BYTES, null, null, null, bac);
        });

        StepVerifier.create(response)
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

        StepVerifier.create(bc.createIfNotExistsWithResponse(new PageBlobCreateOptions(PageBlobClient.PAGE_BYTES).setSequenceNumber(2L))
            .then(bc.getProperties()))
            .assertNext(r -> assertEquals(2, r.getBlobSequenceNumber()))
            .verifyComplete();
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

        contentType = (contentType == null) ? "application/octet-stream" : contentType;
        String finalContentType = contentType;

        StepVerifier.create(bc.createIfNotExistsWithResponse(new PageBlobCreateOptions(PageBlobClient.PAGE_BYTES).setHeaders(headers))
            .then(bc.getPropertiesWithResponse(null)))
            .assertNext(r ->
                assertTrue(validateBlobProperties(r, cacheControl, contentDisposition, contentEncoding, contentLanguage,
                    contentMD5, finalContentType)))
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

        StepVerifier.create(bc.createIfNotExistsWithResponse(new PageBlobCreateOptions(PageBlobClient.PAGE_BYTES).setMetadata(metadata))
            .then(bc.getPropertiesWithResponse(null)))
            .assertNext(r -> {
                assertResponseStatusCode(r, 200);
                assertEquals(metadata, r.getValue().getMetadata());
            })
            .verifyComplete();
    }

    private static Stream<Arguments> createIfNotExistsMetadataSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null),
            Arguments.of("foo", "bar", "fizz", "buzz"));
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
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

        StepVerifier.create(bc.createIfNotExistsWithResponse(new PageBlobCreateOptions(PageBlobClient.PAGE_BYTES).setTags(tags))
            .then(bc.getTagsWithResponse(new BlobGetTagsOptions())))
            .assertNext(r -> {
                assertResponseStatusCode(r, 200);
                assertEquals(tags, r.getValue());
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
                assertNotNull(r.getHeaders().getValue(X_MS_CONTENT_CRC64));
                assertEquals(0, r.getValue().getBlobSequenceNumber());
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

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("uploadPageACSupplier")
    public void uploadPageAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                             String leaseID, Long sequenceNumberLT, Long sequenceNumberLTE, Long sequenceNumberEqual, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        Mono<Response<PageBlobItem>> response = bc.setTags(t)
            .then(Mono.zip(setupBlobLeaseCondition(bc, leaseID), setupBlobMatchCondition(bc, match)))
            .flatMap(tuple -> {
                String newLease = tuple.getT1();
                String newMatch = tuple.getT2();
                if ("null".equals(newLease)) {
                    newLease = null;
                }
                if ("null".equals(newMatch)) {
                    newMatch = null;
                }
                PageBlobRequestConditions pac = new PageBlobRequestConditions()
                    .setLeaseId(newLease)
                    .setIfMatch(newMatch)
                    .setIfNoneMatch(noneMatch)
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified)
                    .setIfSequenceNumberLessThan(sequenceNumberLT)
                    .setIfSequenceNumberLessThanOrEqualTo(sequenceNumberLTE)
                    .setIfSequenceNumberEqualTo(sequenceNumberEqual)
                    .setTagsConditions(tags);

                return bc.uploadPagesWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
                    Flux.just(ByteBuffer.wrap(getRandomByteArray(PageBlobClient.PAGE_BYTES))), null, pac);
            });

        assertAsyncResponseStatusCode(response, 201);
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
        Mono<Response<PageBlobItem>> response = Mono.zip(setupBlobLeaseCondition(bc, leaseID),
            setupBlobMatchCondition(bc, noneMatch))
            .flatMap(tuple -> {
                String newLease = tuple.getT1();
                String newNoneMatch = tuple.getT2();
                if ("null".equals(newLease)) {
                    newLease = null;
                }
                if ("null".equals(newNoneMatch)) {
                    newNoneMatch = null;
                }
                PageBlobRequestConditions pac = new PageBlobRequestConditions()
                    .setLeaseId(newLease)
                    .setIfMatch(match)
                    .setIfNoneMatch(newNoneMatch)
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified)
                    .setIfSequenceNumberLessThan(sequenceNumberLT)
                    .setIfSequenceNumberLessThanOrEqualTo(sequenceNumberLTE)
                    .setIfSequenceNumberEqualTo(sequenceNumberEqual)
                    .setTagsConditions(tags);

                return bc.uploadPagesWithResponse(
                    new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
                    Flux.just(ByteBuffer.wrap(getRandomByteArray(PageBlobClient.PAGE_BYTES))), null, pac);
            });

        StepVerifier.create(response)
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
        PageBlobAsyncClient clientWithFailure = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            bc.getBlobUrl(), new TransientFailureInjectingHttpPipelinePolicy()).getPageBlobAsyncClient();

        byte[] data = getRandomByteArray(PageBlobClient.PAGE_BYTES);

        StepVerifier.create(clientWithFailure.uploadPages(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            Flux.just(ByteBuffer.wrap(data))).then(FluxUtil.collectBytesInByteBufferStream(bc.downloadStream())))
            .assertNext(r ->  TestUtils.assertArraysEqual(r, data))
            .verifyComplete();
    }

    @Test
    public void uploadPageFromURLMin() {
        PageBlobAsyncClient destURL = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        String sas = destURL.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobSasPermission().setTagsPermission(true).setReadPermission(true)));
        PageRange pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1);

        Mono<Response<PageBlobItem>> response = destURL.create(PageBlobClient.PAGE_BYTES)
            .then(destURL.uploadPages(pageRange, Flux.just(ByteBuffer.wrap(getRandomByteArray(PageBlobClient.PAGE_BYTES)))))
            .then(bc.uploadPagesFromUrlWithResponse(pageRange, destURL.getBlobUrl() + "?" + sas,
                null, null, null, null));

        StepVerifier.create(response)
            .assertNext(r -> {
                assertResponseStatusCode(r, 201);
                assertTrue(validateBasicHeaders(r.getHeaders()));
            })
            .verifyComplete();
    }

    /*@RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2024-08-04")
    @Test
    public void uploadPageFromURLSourceErrorAndStatusCode() {
        PageBlobAsyncClient destBlob = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();

        PageRange pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1);

        StepVerifier.create(destBlob.createIfNotExists(Constants.KB).then(destBlob.uploadPagesFromUrl(pageRange, bc.getBlobUrl(), null)))
            .verifyErrorSatisfies(r -> {
                BlobStorageException e = assertInstanceOf(BlobStorageException.class, r);
                assertTrue(e.getStatusCode() == 409);
                assertTrue(e.getServiceMessage().contains("PublicAccessNotPermitted"));
                assertTrue(e.getServiceMessage().contains("Public access is not permitted on this storage account."));
            });
    }*/

    @Test
    public void uploadPageFromURLRange() {
        byte[] data = getRandomByteArray(PageBlobClient.PAGE_BYTES * 4);

        PageBlobAsyncClient sourceURL = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        PageBlobAsyncClient destURL = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();

        String sas = sourceURL.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobSasPermission().setTagsPermission(true).setReadPermission(true)));

        Mono<byte[]> response = sourceURL.create(PageBlobClient.PAGE_BYTES * 4)
            .then(sourceURL.uploadPages(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES * 4 - 1),
                Flux.just(ByteBuffer.wrap(data))))
            .then(destURL.create(PageBlobClient.PAGE_BYTES * 2))
            .then(destURL.uploadPagesFromUrl(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES * 2 - 1),
                sourceURL.getBlobUrl() + "?" + sas, PageBlobClient.PAGE_BYTES * 2L))
            .then(FluxUtil.collectBytesInByteBufferStream(destURL.downloadStream()));

        StepVerifier.create(response)
            .assertNext(r -> TestUtils.assertArraysEqual(data, PageBlobClient.PAGE_BYTES * 2, r,
                0, PageBlobClient.PAGE_BYTES * 2))
            .verifyComplete();
    }

    @Test
    public void uploadPageFromURLIA() {
        StepVerifier.create(bc.uploadPagesFromUrl(null, bc.getBlobUrl(), (long) PageBlobClient.PAGE_BYTES))
            .verifyError(IllegalArgumentException.class);
    }

    @Test
    public void uploadPageFromURLMD5() throws NoSuchAlgorithmException {
        PageBlobAsyncClient destURL = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        byte[] data = getRandomByteArray(PageBlobClient.PAGE_BYTES);
        PageRange pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1);
        String sas = bc.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobSasPermission().setTagsPermission(true).setReadPermission(true)));

        Mono<Response<PageBlobItem>> response = destURL.create(PageBlobClient.PAGE_BYTES)
            .then(bc.uploadPages(pageRange, Flux.just(ByteBuffer.wrap(data))))
            .then(destURL.uploadPagesFromUrlWithResponse(pageRange, bc.getBlobUrl() + "?" + sas,
                null, MessageDigest.getInstance("MD5").digest(data), null,
                null));

        StepVerifier.create(response)
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void uploadPageFromURLMD5Fail() throws NoSuchAlgorithmException {
        PageBlobAsyncClient destURL = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        PageRange pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1);
        String sas = bc.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobSasPermission().setTagsPermission(true).setReadPermission(true)));

        Mono<Response<PageBlobItem>> response = destURL.create(PageBlobClient.PAGE_BYTES)
            .then(bc.uploadPages(pageRange, Flux.just(ByteBuffer.wrap(getRandomByteArray(PageBlobClient.PAGE_BYTES)))))
            .then(destURL.uploadPagesFromUrlWithResponse(pageRange, bc.getBlobUrl() + "?" + sas,
                null, MessageDigest.getInstance("MD5").digest("garbage".getBytes()),
                null, null));

        StepVerifier.create(response)
            .verifyError(BlobStorageException.class);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("uploadPageACSupplier")
    public void uploadPageFromURLDestinationAC(OffsetDateTime modified, OffsetDateTime unmodified, String match,
                                               String noneMatch, String leaseID, Long sequenceNumberLT, Long sequenceNumberLTE, Long sequenceNumberEqual,
                                               String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        PageBlobAsyncClient sourceURL = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        PageRange pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1);

        Mono<Response<PageBlobItem>> response = bc.setTags(t)
            .then(sourceURL.create(PageBlobClient.PAGE_BYTES))
            .then(sourceURL.uploadPages(pageRange, Flux.just(ByteBuffer.wrap(getRandomByteArray(PageBlobClient.PAGE_BYTES)))))
            .then(Mono.zip(setupBlobLeaseCondition(bc, leaseID), setupBlobMatchCondition(bc, match)))
            .flatMap(tuple -> {
                String newLease = tuple.getT1();
                String newMatch = tuple.getT2();
                if ("null".equals(newLease)) {
                    newLease = null;
                }
                if ("null".equals(newMatch)) {
                    newMatch = null;
                }
                PageBlobRequestConditions pac = new PageBlobRequestConditions()
                    .setLeaseId(newLease)
                    .setIfMatch(newMatch)
                    .setIfNoneMatch(noneMatch)
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified)
                    .setIfSequenceNumberLessThan(sequenceNumberLT)
                    .setIfSequenceNumberLessThanOrEqualTo(sequenceNumberLTE)
                    .setIfSequenceNumberEqualTo(sequenceNumberEqual)
                    .setTagsConditions(tags);
                String sas = sourceURL.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
                    new BlobSasPermission().setTagsPermission(true).setReadPermission(true)));

                return bc.uploadPagesFromUrlWithResponse(pageRange,
                    sourceURL.getBlobUrl() + "?" + sas, null, null, pac,
                    null);
            });

        assertAsyncResponseStatusCode(response, 201);
    }

    @ParameterizedTest
    @MethodSource("uploadPageACFailSupplier")
    public void uploadPageFromURLDestinationACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
                                                   String noneMatch, String leaseID, Long sequenceNumberLT, Long sequenceNumberLTE, Long sequenceNumberEqual,
                                                   String tags) {
        PageBlobAsyncClient sourceURL = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        PageRange pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1);

        Mono<Response<PageBlobItem>> response = sourceURL.create(PageBlobClient.PAGE_BYTES)
            .then(sourceURL.uploadPages(pageRange, Flux.just(ByteBuffer.wrap(getRandomByteArray(PageBlobClient.PAGE_BYTES)))))
            .then(setupBlobMatchCondition(bc, noneMatch))
            .flatMap(r -> {
                if ("null".equals(r)) {
                    r = null;
                }
                PageBlobRequestConditions pac = new PageBlobRequestConditions()
                    .setLeaseId(leaseID)
                    .setIfMatch(match)
                    .setIfNoneMatch(r)
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified)
                    .setIfSequenceNumberLessThan(sequenceNumberLT)
                    .setIfSequenceNumberLessThanOrEqualTo(sequenceNumberLTE)
                    .setIfSequenceNumberEqualTo(sequenceNumberEqual)
                    .setTagsConditions(tags);
                String sas = sourceURL.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
                    new BlobSasPermission().setTagsPermission(true).setReadPermission(true)));

                return bc.uploadPagesFromUrlWithResponse(
                    pageRange, sourceURL.getBlobUrl() + "?" + sas, null, null, pac,
                    null);
            });

        StepVerifier.create(response)
            .verifyError(BlobStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("uploadPageFromURLSourceACSupplier")
    public void uploadPageFromURLSourceAC(OffsetDateTime sourceIfModifiedSince, OffsetDateTime sourceIfUnmodifiedSince,
                                          String sourceIfMatch, String sourceIfNoneMatch) {
        PageBlobAsyncClient sourceURL = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        PageRange pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1);

        Mono<Response<PageBlobItem>> response = sourceURL.create(PageBlobClient.PAGE_BYTES)
            .then(sourceURL.uploadPages(pageRange, Flux.just(ByteBuffer.wrap(getRandomByteArray(PageBlobClient.PAGE_BYTES)))))
            .then(setupBlobMatchCondition(sourceURL, sourceIfMatch))
            .flatMap(r -> {
                if ("null".equals(r)) {
                    r = null;
                }
                BlobRequestConditions smac = new BlobRequestConditions()
                    .setIfModifiedSince(sourceIfModifiedSince)
                    .setIfUnmodifiedSince(sourceIfUnmodifiedSince)
                    .setIfMatch(r)
                    .setIfNoneMatch(sourceIfNoneMatch);
                String sas = sourceURL.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
                    new BlobSasPermission().setTagsPermission(true).setReadPermission(true)));

                return bc.uploadPagesFromUrlWithResponse(pageRange,
                    sourceURL.getBlobUrl() + "?" + sas, null, null, null,
                    smac);
            });

        assertAsyncResponseStatusCode(response, 201);
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
        PageBlobAsyncClient sourceURL = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        PageRange pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1);

        Mono<Response<PageBlobItem>> response = sourceURL.create(PageBlobClient.PAGE_BYTES)
            .then(sourceURL.uploadPages(pageRange, Flux.just(ByteBuffer.wrap(getRandomByteArray(PageBlobClient.PAGE_BYTES)))))
            .then(setupBlobMatchCondition(sourceURL, sourceIfNoneMatch))
            .flatMap(r -> {
                if ("null".equals(r)) {
                    r = null;
                }
                BlobRequestConditions smac = new BlobRequestConditions()
                    .setIfModifiedSince(sourceIfModifiedSince)
                    .setIfUnmodifiedSince(sourceIfUnmodifiedSince)
                    .setIfMatch(sourceIfMatch)
                    .setIfNoneMatch(r);
                String sas = sourceURL.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
                    new BlobSasPermission().setTagsPermission(true).setReadPermission(true)));

                return bc.uploadPagesFromUrlWithResponse(
                    pageRange, sourceURL.getBlobUrl() + "?" + sas, null, null,
                    null, smac);
            });

        StepVerifier.create(response)
            .verifyError(BlobStorageException.class);
    }

    private static Stream<Arguments> uploadPageFromURLSourceACFailSupplier() {
        return Stream.of(
            Arguments.of(NEW_DATE, null, null, null),
            Arguments.of(null, OLD_DATE, null, null),
            Arguments.of(null, null, GARBAGE_ETAG, null),
            Arguments.of(null, null, null, RECEIVED_ETAG));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void clearPage() {
        StepVerifier.create(bc.uploadPagesWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            Flux.just(ByteBuffer.wrap(getRandomByteArray(PageBlobClient.PAGE_BYTES))), null, null)
            .then(bc.clearPagesWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1), null)))
            .assertNext(r -> {
                assertTrue(validateBasicHeaders(r.getHeaders()));
                assertNull(r.getValue().getContentMd5());
                assertEquals(0, r.getValue().getBlobSequenceNumber());
            })
            .verifyComplete();

        StepVerifier.create(bc.getPageRanges(new BlobRange(0)))
            .assertNext(r -> assertEquals(0, r.getPageRange().size()))
            .verifyComplete();
    }

    @Test
    public void clearPageMin() {
        StepVerifier.create(bc.clearPages(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1)))
            .expectNextCount(1)
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("uploadPageACSupplier")
    public void clearPagesAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                             String leaseID, Long sequenceNumberLT, Long sequenceNumberLTE, Long sequenceNumberEqual, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");

        Mono<Response<PageBlobItem>> response = bc.uploadPages(new PageRange().setStart(0)
            .setEnd(PageBlobClient.PAGE_BYTES - 1), Flux.just(ByteBuffer.wrap(getRandomByteArray(PageBlobClient.PAGE_BYTES))))
            .then(bc.setTags(t))
            .then(Mono.zip(setupBlobLeaseCondition(bc, leaseID), setupBlobMatchCondition(bc, match)))
            .flatMap(tuple -> {
                String newLease = tuple.getT1();
                String newMatch = tuple.getT2();
                if ("null".equals(newLease)) {
                    newLease = null;
                }
                if ("null".equals(newMatch)) {
                    newMatch = null;
                }
                PageBlobRequestConditions pac = new PageBlobRequestConditions()
                    .setLeaseId(newLease)
                    .setIfMatch(newMatch)
                    .setIfNoneMatch(noneMatch)
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified)
                    .setIfSequenceNumberLessThan(sequenceNumberLT)
                    .setIfSequenceNumberLessThanOrEqualTo(sequenceNumberLTE)
                    .setIfSequenceNumberEqualTo(sequenceNumberEqual)
                    .setTagsConditions(tags);

                return bc.clearPagesWithResponse(
                    new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1), pac);
            });

        assertAsyncResponseStatusCode(response, 201);
    }

    @ParameterizedTest
    @MethodSource("uploadPageACFailSupplier")
    public void clearPagesACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                 String leaseID, Long sequenceNumberLT, Long sequenceNumberLTE, Long sequenceNumberEqual, String tags) {
        Mono<Response<PageBlobItem>> response = bc.uploadPages(new PageRange().setStart(0)
            .setEnd(PageBlobClient.PAGE_BYTES - 1), Flux.just(ByteBuffer.wrap(getRandomByteArray(PageBlobClient.PAGE_BYTES))))
            .then(Mono.zip(setupBlobLeaseCondition(bc, leaseID), setupBlobMatchCondition(bc, noneMatch)))
            .flatMap(tuple -> {
                String newLease = tuple.getT1();
                String newNoneMatch = tuple.getT2();
                if ("null".equals(newLease)) {
                    newLease = null;
                }
                if ("null".equals(newNoneMatch)) {
                    newNoneMatch = null;
                }
                PageBlobRequestConditions pac = new PageBlobRequestConditions()
                    .setLeaseId(newLease)
                    .setIfMatch(match)
                    .setIfNoneMatch(newNoneMatch)
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified)
                    .setIfSequenceNumberLessThan(sequenceNumberLT)
                    .setIfSequenceNumberLessThanOrEqualTo(sequenceNumberLTE)
                    .setIfSequenceNumberEqualTo(sequenceNumberEqual)
                    .setTagsConditions(tags);

                return bc.clearPagesWithResponse(
                    new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1), pac);
            });

        StepVerifier.create(response)
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void clearPageError() {
        bc = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();

        StepVerifier.create(bc.clearPages(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1)))
            .verifyError(BlobStorageException.class);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void getPageRanges() {
        StepVerifier.create(bc.uploadPages(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            Flux.just(ByteBuffer.wrap(getRandomByteArray(PageBlobClient.PAGE_BYTES))))
            .then(bc.getPageRangesWithResponse(new BlobRange(0, (long) PageBlobClient.PAGE_BYTES), null)))
            .assertNext(r -> {
                assertResponseStatusCode(r, 200);
                assertEquals(1, r.getValue().getPageRange().size());
                assertTrue(validateBasicHeaders(r.getHeaders()));
                assertEquals("512", r.getHeaders().getValue(X_MS_BLOB_CONTENT_LENGTH));
            })
            .verifyComplete();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void getPageRangesMin() {
        StepVerifier.create(bc.getPageRanges(null))
            .expectNextCount(1)
            .verifyComplete();
    }

    @SuppressWarnings("deprecation")
    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void getPageRangesAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                String leaseID, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");

        Mono<Response<PageList>> response = bc.setTags(t)
            .then(Mono.zip(setupBlobLeaseCondition(bc, leaseID), setupBlobMatchCondition(bc, match)))
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

                return bc.getPageRangesWithResponse(new BlobRange(0, (long) PageBlobClient.PAGE_BYTES),
                    bac);
            });

        StepVerifier.create(response)
            .expectNextCount(1)
            .verifyComplete();
    }

    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsFailSupplier")
    public void getPageRangesACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                    String leaseID, String tags) {

        Mono<Response<PageList>> response = Mono.zip(setupBlobLeaseCondition(bc, leaseID),
            setupBlobMatchCondition(bc, noneMatch))
            .flatMap(tuple -> {
                String newLease = tuple.getT1();
                String newNoneMatch = tuple.getT2();
                if ("null".equals(newLease)) {
                    newLease = null;
                }
                if ("null".equals(newNoneMatch)) {
                    newNoneMatch = null;
                }
                BlobRequestConditions bac = new BlobRequestConditions()
                    .setLeaseId(newLease)
                    .setIfMatch(match)
                    .setIfNoneMatch(newNoneMatch)
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified)
                    .setTagsConditions(tags);

                return bc.getPageRangesWithResponse(new BlobRange(0, (long) PageBlobClient.PAGE_BYTES), bac);
            });


        StepVerifier.create(response)
            .verifyError(BlobStorageException.class);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void getPageRangesError() {
        bc = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        StepVerifier.create(bc.getPageRanges(null))
            .verifyError(BlobStorageException.class);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-06-08")
    @Test
    public void listPageRanges() {
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(getRandomByteArray(4 * Constants.KB)));

        Flux<PageRangeItem> response = bc.create(4 * Constants.KB, true)
            .then(bc.uploadPages(new PageRange().setStart(0).setEnd(4 * Constants.KB - 1), data))
            .then(bc.clearPages(new PageRange().setStart(Constants.KB).setEnd(2 * Constants.KB - 1)))
            .then(bc.clearPages(new PageRange().setStart(3 * Constants.KB).setEnd(4 * Constants.KB - 1)))
            .thenMany(bc.listPageRanges(new BlobRange(0, (long) 4 * Constants.KB)));

        StepVerifier.create(response)
            .assertNext(r -> {
                assertEquals(r.getRange(), new HttpRange(0, (long) Constants.KB));
                assertFalse(r.isClear());
            })
            .assertNext(r -> {
                assertEquals(r.getRange(), new HttpRange(2 * Constants.KB, (long) Constants.KB));
                assertFalse(r.isClear());
            })
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-06-08")
    @Test
    public void listPagesRangesPageSize() {
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(getRandomByteArray(4 * Constants.KB)));

        Flux<PagedResponse<PageRangeItem>> response = bc.create(4 * Constants.KB, true)
            .then(bc.uploadPages(new PageRange().setStart(0).setEnd(4 * Constants.KB - 1), data))
            .then(bc.clearPages(new PageRange().setStart(Constants.KB).setEnd(2 * Constants.KB - 1)))
            .then(bc.clearPages(new PageRange().setStart(3 * Constants.KB).setEnd(4 * Constants.KB - 1)))
            .thenMany(bc.listPageRanges(new ListPageRangesOptions(
                new BlobRange(0, 4L * Constants.KB)).setMaxResultsPerPage(1)).byPage());

        // when: "max results on options"
        StepVerifier.create(response)
            .assertNext(r -> assertEquals(1, r.getValue().size()))
            .assertNext(r -> assertEquals(1, r.getValue().size()))
            .verifyComplete();


        // when: "max results on iterableByPage"
        StepVerifier.create(bc.listPageRanges(new ListPageRangesOptions(
            new BlobRange(0, 4L * Constants.KB))).byPage(1))
            .assertNext(r -> assertEquals(1, r.getValue().size()))
            .assertNext(r -> assertEquals(1, r.getValue().size()))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-06-08")
    @Test
    public void listPagesContinuationToken() {
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(getRandomByteArray(4 * Constants.KB)));

        Flux<PagedResponse<PageRangeItem>> response = bc.create(4 * Constants.KB, true)
            .then(bc.uploadPages(new PageRange().setStart(0).setEnd(4 * Constants.KB - 1), data))
            .then(bc.clearPages(new PageRange().setStart(Constants.KB).setEnd(2 * Constants.KB - 1)))
            .then(bc.clearPages(new PageRange().setStart(3 * Constants.KB).setEnd(4 * Constants.KB - 1)))
            .thenMany(bc.listPageRanges(new ListPageRangesOptions(new BlobRange(0, 4L * Constants.KB))
                .setMaxResultsPerPage(1)).byPage())
            .flatMap(r -> bc.listPageRanges(new ListPageRangesOptions(new BlobRange(0, 4L * Constants.KB)))
                .byPage(r.getContinuationToken()));

        StepVerifier.create(response)
            .assertNext(r -> assertEquals(1, r.getValue().size()))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-06-08")
    @Test
    public void listPagesRange() {
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(getRandomByteArray(4 * Constants.KB)));

        Flux<PageRangeItem> response = bc.create(4 * Constants.KB, true)
            .then(bc.uploadPages(new PageRange().setStart(0).setEnd(4 * Constants.KB - 1), data))
            .then(bc.clearPages(new PageRange().setStart(Constants.KB).setEnd(2 * Constants.KB - 1)))
            .then(bc.clearPages(new PageRange().setStart(3 * Constants.KB).setEnd(4 * Constants.KB - 1)))
            .thenMany(bc.listPageRanges(new ListPageRangesOptions(new BlobRange(2 * Constants.KB + 1, 2L * Constants.KB))));

        StepVerifier.create(response)
            .expectNextCount(1)
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-06-08")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void listPagesRangesAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                  String leaseID, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");

        Flux<PageRangeItem> response = bc.setTags(t)
            .then(Mono.zip(setupBlobLeaseCondition(bc, leaseID), setupBlobMatchCondition(bc, match)))
            .flatMapMany(tuple -> {
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

                return bc.listPageRanges(new ListPageRangesOptions(new BlobRange(0,
                    (long) PageBlobClient.PAGE_BYTES)).setRequestConditions(bac));
            });

        StepVerifier.create(response)
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-06-08")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsFailSupplier")
    public void listPageRangesACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                     String leaseID, String tags) {
        Mono<Long> response = Mono.zip(setupBlobLeaseCondition(bc, leaseID),
            setupBlobMatchCondition(bc, noneMatch))
            .flatMap(tuple -> {
                String newLease = tuple.getT1();
                String newNoneMatch = tuple.getT2();
                if ("null".equals(newLease)) {
                    newLease = null;
                }
                if ("null".equals(newNoneMatch)) {
                    newNoneMatch = null;
                }
                BlobRequestConditions bac = new BlobRequestConditions()
                    .setLeaseId(newLease)
                    .setIfMatch(match)
                    .setIfNoneMatch(newNoneMatch)
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified)
                    .setTagsConditions(tags);

                return bc.listPageRanges(new ListPageRangesOptions(
                    new BlobRange(0, (long) PageBlobClient.PAGE_BYTES)).setRequestConditions(bac)).count();
            });

        StepVerifier.create(response)
            .verifyError(BlobStorageException.class);
    }

    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @MethodSource("getPageRangesDiffSupplier")
    public void getPageRangesDiff(List<PageRange> rangesToUpdate, List<PageRange> rangesToClear,
                                  List<PageRange> expectedPageRanges, List<ClearRange> expectedClearRanges) {
        Mono<Response<PageList>> response = bc.create(4 * Constants.MB, true)
            .then(bc.uploadPages(new PageRange().setStart(0).setEnd(4 * Constants.MB - 1),
                Flux.just(ByteBuffer.wrap(getRandomByteArray(4 * Constants.MB)))))
            .then(bc.createSnapshot()).flatMap(snapId -> {
                Flux<PageBlobItem> upload = Flux.fromIterable(rangesToUpdate)
                    .flatMap(it -> bc.uploadPages(it, Flux.just(ByteBuffer.wrap(getRandomByteArray(
                        (int) (it.getEnd() - it.getStart()) + 1)))));
                Flux<PageBlobItem> clear = Flux.fromIterable(rangesToClear)
                    .flatMap(it -> bc.clearPages(it));
                return upload.thenMany(clear).then(bc.getPageRangesDiffWithResponse(new BlobRange(0, 4L * Constants.MB),
                    snapId.getSnapshotId(), null));
            });

        StepVerifier.create(response)
            .assertNext(r -> {
                assertTrue(validateBasicHeaders(r.getHeaders()));
                assertEquals(expectedPageRanges.size(), r.getValue().getPageRange().size());
                assertEquals(expectedClearRanges.size(), r.getValue().getClearRange().size());

                for (int i = 0; i < expectedPageRanges.size(); i++) {
                    PageRange actualRange = r.getValue().getPageRange().get(i);
                    PageRange expectedRange = expectedPageRanges.get(i);
                    assertEquals(expectedRange.getStart(), actualRange.getStart());
                    assertEquals(expectedRange.getEnd(), actualRange.getEnd());
                }

                for (int i = 0; i < expectedClearRanges.size(); i++) {
                    ClearRange actualRange = r.getValue().getClearRange().get(i);
                    ClearRange expectedRange = expectedClearRanges.get(i);
                    assertEquals(expectedRange.getStart(), actualRange.getStart());
                    assertEquals(expectedRange.getEnd(), actualRange.getEnd());
                }

                assertEquals(4 * Constants.MB, Integer.parseInt(r.getHeaders().getValue(X_MS_BLOB_CONTENT_LENGTH)));
            })
            .verifyComplete();
    }

    private static Stream<Arguments> getPageRangesDiffSupplier() {
        return Stream.of(
            Arguments.of(createPageRanges(), createPageRanges(), createPageRanges(), createClearRanges()),
            Arguments.of(createPageRanges(0, 511), createPageRanges(), createPageRanges(0, 511),
                createClearRanges()),
            Arguments.of(createPageRanges(), createPageRanges(0, 511), createPageRanges(),
                createClearRanges(0, 511)),
            Arguments.of(createPageRanges(0, 511), createPageRanges(512, 1023),
                createPageRanges(0, 511), createClearRanges(512, 1023)),
            Arguments.of(createPageRanges(0, 511, 1024, 1535), createPageRanges(512, 1023, 1536, 2047),
                createPageRanges(0, 511, 1024, 1535), createClearRanges(512, 1023, 1536, 2047))
        );
    }

    private static List<PageRange> createPageRanges(long... offsets) {
        List<PageRange> pageRanges = new ArrayList<>();

        if (CoreUtils.isNullOrEmpty(Collections.singleton(offsets))) {
            return pageRanges;
        }

        for (int i = 0; i < offsets.length / 2; i++) {
            pageRanges.add(new PageRange().setStart(offsets[i * 2]).setEnd(offsets[i * 2 + 1]));
        }
        return pageRanges;
    }

    static List<ClearRange> createClearRanges(long... offsets) {
        List<ClearRange> clearRanges = new ArrayList<>();

        if (CoreUtils.isNullOrEmpty(Collections.singleton(offsets))) {
            return clearRanges;
        }

        for (int i = 0; i < offsets.length / 2; i++) {
            clearRanges.add(new ClearRange().setStart(offsets[i * 2]).setEnd(offsets[i * 2 + 1]));
        }

        return clearRanges;
    }

    @SuppressWarnings("deprecation")
    @Test
    public void getPageRangesDiffMin() {
        Mono<PageList> response = bc.createSnapshot()
            .flatMap(r -> bc.getPageRangesDiff(null, r.getSnapshotId()));

        StepVerifier.create(response)
            .assertNext(r -> assertDoesNotThrow(r::getPageRange))
            .verifyComplete();
    }

    @SuppressWarnings("deprecation")
    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void getPageRangesDiffAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                    String leaseID, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");

        Mono<Response<PageList>> response = bc.setTags(t).then(bc.createSnapshot())
            .flatMap(snapId ->
                Mono.zip(setupBlobLeaseCondition(bc, leaseID), setupBlobMatchCondition(bc, match))
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

                        return bc.getPageRangesDiffWithResponse(new BlobRange(0,
                            (long) PageBlobClient.PAGE_BYTES), snapId.getSnapshotId(), bac);
                    })
            );

        StepVerifier.create(response)
            .expectNextCount(1)
            .verifyComplete();
    }

    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsFailSupplier")
    public void getPageRangesDiffACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
                                        String noneMatch, String leaseID, String tags) {

        Mono<Response<PageList>> response = bc.createSnapshot().flatMap(snapId ->
            Mono.zip(setupBlobLeaseCondition(bc, leaseID), setupBlobMatchCondition(bc, noneMatch))
                .flatMap(tuple -> {
                    String newLease = tuple.getT1();
                    String newNoneMatch = tuple.getT2();
                    if ("null".equals(newLease)) {
                        newLease = null;
                    }
                    if ("null".equals(newNoneMatch)) {
                        newNoneMatch = null;
                    }
                    BlobRequestConditions bac = new BlobRequestConditions()
                        .setLeaseId(newLease)
                        .setIfMatch(match)
                        .setIfNoneMatch(newNoneMatch)
                        .setIfModifiedSince(modified)
                        .setIfUnmodifiedSince(unmodified)
                        .setTagsConditions(tags);

                    return bc.getPageRangesDiffWithResponse(
                        new BlobRange(0, (long) PageBlobClient.PAGE_BYTES), snapId.getSnapshotId(), bac);
                })
        );

        StepVerifier.create(response)
            .verifyError(BlobStorageException.class);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void getPageRangesDiffError() {
        bc = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        StepVerifier.create(bc.getPageRangesDiff(null, "snapshot"))
            .verifyError(BlobStorageException.class);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-06-08")
    @Test
    public void listPagesRangesDiff() {
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(getRandomByteArray(4 * Constants.KB)));
        Flux<ByteBuffer> data2 = Flux.just(ByteBuffer.wrap(getRandomByteArray(Constants.KB)));

        Flux<PageRangeItem> response = bc.create(4 * Constants.KB, true)
            .then(bc.uploadPages(new PageRange().setStart(0).setEnd(4 * Constants.KB - 1), data))
            .then(bc.createSnapshot())
            .flatMapMany(r -> bc.uploadPages(new PageRange().setStart(0).setEnd(Constants.KB - 1), data2)
                .then(bc.clearPages(new PageRange().setStart(Constants.KB).setEnd(2 * Constants.KB - 1)))
                .then(bc.uploadPages(new PageRange().setStart(2 * Constants.KB).setEnd(3 * Constants.KB - 1), data2))
                .then(bc.clearPages(new PageRange().setStart(3 * Constants.KB).setEnd(4 * Constants.KB - 1)))
                .thenMany(bc.listPageRangesDiff(new BlobRange(0, 4L * Constants.KB), r.getSnapshotId())));

        StepVerifier.create(response)
            .assertNext(r -> {
                assertEquals(r.getRange(), new HttpRange(0L, (long) Constants.KB));
                assertFalse(r.isClear());
            })
            .assertNext(r -> {
                assertEquals(r.getRange(), new HttpRange(2 * Constants.KB, (long) Constants.KB));
                assertFalse(r.isClear());
            })
            .assertNext(r -> {
                assertEquals(r.getRange(), new HttpRange(Constants.KB, (long) Constants.KB));
                assertTrue(r.isClear());
            })
            .assertNext(r -> {
                assertEquals(r.getRange(), new HttpRange(3 * Constants.KB, (long) Constants.KB));
                assertTrue(r.isClear());
            })
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-06-08")
    @Test
    public void listPagesRangesDiffPageSize() {
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(getRandomByteArray(4 * Constants.KB)));
        Flux<ByteBuffer> data2 = Flux.just(ByteBuffer.wrap(getRandomByteArray(Constants.KB)));

        Flux<Tuple2<PagedResponse<PageRangeItem>, PagedResponse<PageRangeItem>>> response = bc.create(4 * Constants.KB, true)
            .then(bc.uploadPages(new PageRange().setStart(0).setEnd(4 * Constants.KB - 1), data))
            .then(bc.createSnapshot())
            .flatMapMany(r -> bc.uploadPages(new PageRange().setStart(0).setEnd(Constants.KB - 1), data2)
                .then(bc.clearPages(new PageRange().setStart(Constants.KB).setEnd(2 * Constants.KB - 1)))
                .then(bc.uploadPages(new PageRange().setStart(2 * Constants.KB).setEnd(3 * Constants.KB - 1), data2))
                .then(bc.clearPages(new PageRange().setStart(3 * Constants.KB).setEnd(4 * Constants.KB - 1)))
                .thenMany(Flux.zip(bc.listPageRangesDiff(new ListPageRangesDiffOptions(new BlobRange(0, 4L * Constants.KB),
                    r.getSnapshotId()).setMaxResultsPerPage(2)).byPage(),
                    bc.listPageRangesDiff(new ListPageRangesDiffOptions(new BlobRange(0, 4L * Constants.KB),
                    r.getSnapshotId())).byPage(2))));

        // when: "max results on options and on iterableByPage"
        StepVerifier.create(response)
            .assertNext(r -> {
                assertEquals(2, r.getT1().getValue().size());
                assertEquals(2, r.getT2().getValue().size());
            })
            .assertNext(r -> {
                assertEquals(2, r.getT1().getValue().size());
                assertEquals(2, r.getT2().getValue().size());
            })
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-06-08")
    @Test
    public void listPagesDiffContinuationToken() {
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(getRandomByteArray(4 * Constants.KB)));
        Flux<ByteBuffer> data2 = Flux.just(ByteBuffer.wrap(getRandomByteArray(Constants.KB)));

        Flux<PagedResponse<PageRangeItem>> response = bc.create(4 * Constants.KB, true)
            .then(bc.uploadPages(new PageRange().setStart(0).setEnd(4 * Constants.KB - 1), data))
            .then(bc.createSnapshot())
            .flatMapMany(r -> bc.uploadPages(new PageRange().setStart(0).setEnd(Constants.KB - 1), data2)
                .then(bc.clearPages(new PageRange().setStart(Constants.KB).setEnd(2 * Constants.KB - 1)))
                .then(bc.uploadPages(new PageRange().setStart(2 * Constants.KB).setEnd(3 * Constants.KB - 1), data2))
                .then(bc.clearPages(new PageRange().setStart(3 * Constants.KB).setEnd(4 * Constants.KB - 1)))
                .thenMany(Flux.zip(bc.listPageRangesDiff(new ListPageRangesDiffOptions(
                    new BlobRange(0, 4L * Constants.KB), r.getSnapshotId()).setMaxResultsPerPage(2)).byPage(),
                    Flux.just(r.getSnapshotId()))))
            .flatMap(tuple -> bc.listPageRangesDiff(new ListPageRangesDiffOptions(new BlobRange(0, 4L * Constants.KB), tuple.getT2())).byPage(tuple.getT1().getContinuationToken()));

        StepVerifier.create(response)
            .assertNext(r -> assertEquals(2, r.getValue().size()))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-06-08")
    @Test
    public void listPagesDiffRange() {
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(getRandomByteArray(4 * Constants.KB)));
        Flux<ByteBuffer> data2 = Flux.just(ByteBuffer.wrap(getRandomByteArray(Constants.KB)));


        Flux<PageRangeItem> response = bc.create(4 * Constants.KB, true)
            .then(bc.uploadPages(new PageRange().setStart(0).setEnd(4 * Constants.KB - 1), data))
            .then(bc.createSnapshot())
            .flatMapMany(r -> bc.uploadPages(new PageRange().setStart(0).setEnd(Constants.KB - 1), data2)
                .then(bc.clearPages(new PageRange().setStart(Constants.KB).setEnd(2 * Constants.KB - 1)))
                .then(bc.uploadPages(new PageRange().setStart(2 * Constants.KB).setEnd(3 * Constants.KB - 1), data2))
                .then(bc.clearPages(new PageRange().setStart(3 * Constants.KB).setEnd(4 * Constants.KB - 1)))
                .thenMany(bc.listPageRangesDiff(new ListPageRangesDiffOptions(
                    new BlobRange(2 * Constants.KB + 1, 2L * Constants.KB), r.getSnapshotId()))));

        StepVerifier.create(response)
            .expectNextCount(2)
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-06-08")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void listPageRangesDiffAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                     String leaseID, String tags) {
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(getRandomByteArray(4 * Constants.KB)));
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");

        Flux<Long> response = bc.create(4 * Constants.KB, true)
            .then(bc.uploadPages(new PageRange().setStart(0).setEnd(4 * Constants.KB - 1), data))
            .then(bc.createSnapshot())
            .flatMapMany(snapId ->
                bc.setTags(t).then(Mono.zip(setupBlobLeaseCondition(bc, leaseID),
                    setupBlobMatchCondition(bc, match)))
                    .flatMapMany(tuple -> {
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
                        return bc.listPageRangesDiff(new ListPageRangesDiffOptions(
                            new BlobRange(0, (long) PageBlobClient.PAGE_BYTES), snapId.getSnapshotId())
                            .setRequestConditions(bac)).count();
                    })
            );

        StepVerifier.create(response)
                .expectNextCount(1)
                .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-06-08")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsFailSupplier")
    public void listPageRangesDiffACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
                                         String noneMatch, String leaseID, String tags) {
        Mono<Long> response = bc.createSnapshot().flatMap(snapId ->
            Mono.zip(setupBlobLeaseCondition(bc, leaseID), setupBlobMatchCondition(bc, noneMatch))
                .flatMap(tuple -> {
                    String newLease = tuple.getT1();
                    String newNoneMatch = tuple.getT2();
                    if ("null".equals(newLease)) {
                        newLease = null;
                    }
                    if ("null".equals(newNoneMatch)) {
                        newNoneMatch = null;
                    }
                    BlobRequestConditions bac = new BlobRequestConditions()
                        .setLeaseId(newLease)
                        .setIfMatch(match)
                        .setIfNoneMatch(newNoneMatch)
                        .setIfModifiedSince(modified)
                        .setIfUnmodifiedSince(unmodified)
                        .setTagsConditions(tags);

                    return bc.listPageRangesDiff(new ListPageRangesDiffOptions(
                        new BlobRange(0, (long) PageBlobClient.PAGE_BYTES), snapId.getSnapshotId())
                        .setRequestConditions(bac)).count();
                }));

        StepVerifier.create(response)
            .verifyError(BlobStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("pageRangeIASupplier")
    public void pageRangeIA(int start, int end) {
        PageRange range = new PageRange().setStart(start).setEnd(end);
        StepVerifier.create(bc.clearPages(range))
            .verifyError(IllegalArgumentException.class);
    }

    private static Stream<Arguments> pageRangeIASupplier() {
        return Stream.of(
            Arguments.of(1, 1),
            Arguments.of(-PageBlobClient.PAGE_BYTES, PageBlobClient.PAGE_BYTES - 1),
            Arguments.of(0, 0),
            Arguments.of(1, PageBlobClient.PAGE_BYTES - 1),
            Arguments.of(0, PageBlobClient.PAGE_BYTES),
            Arguments.of(PageBlobClient.PAGE_BYTES * 2, PageBlobClient.PAGE_BYTES - 1));
    }

    @Test
    public void resize() {
        StepVerifier.create(bc.resizeWithResponse(PageBlobClient.PAGE_BYTES * 2, null))
            .assertNext(r -> {
                assertTrue(validateBasicHeaders(r.getHeaders()));
                assertNotNull(r.getValue().getBlobSequenceNumber());
            })
            .verifyComplete();

        StepVerifier.create(bc.getProperties())
            .assertNext(r -> assertEquals(PageBlobClient.PAGE_BYTES * 2, r.getBlobSize()))
            .verifyComplete();
    }

    @Test
    public void resizeMin() {
        assertAsyncResponseStatusCode(bc.resizeWithResponse(PageBlobClient.PAGE_BYTES, null),  200);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void resizeAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                         String leaseID, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");

        Mono<Response<PageBlobItem>> response = bc.setTags(t)
            .then(Mono.zip(setupBlobLeaseCondition(bc, leaseID), setupBlobMatchCondition(bc, match)))
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

                return bc.resizeWithResponse(PageBlobClient.PAGE_BYTES * 2, bac);
            });

        assertAsyncResponseStatusCode(response, 200);
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsFailSupplier")
    public void resizeACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                             String leaseID, String tags) {
        Mono<Response<PageBlobItem>> response = Mono.zip(setupBlobLeaseCondition(bc, leaseID),
            setupBlobMatchCondition(bc, noneMatch))
            .flatMap(tuple -> {
                String newLease = tuple.getT1();
                String newNoneMatch = tuple.getT2();
                if ("null".equals(newLease)) {
                    newLease = null;
                }
                if ("null".equals(newNoneMatch)) {
                    newNoneMatch = null;
                }
                BlobRequestConditions bac = new BlobRequestConditions()
                    .setLeaseId(newLease)
                    .setIfMatch(match)
                    .setIfNoneMatch(newNoneMatch)
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified)
                    .setTagsConditions(tags);

                return bc.resizeWithResponse(PageBlobClient.PAGE_BYTES * 2, bac);
            });

        StepVerifier.create(response)
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void resizeError() {
        bc = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        StepVerifier.create(bc.resize(0))
            .verifyError(BlobStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("sequenceNumberSupplier")
    public void sequenceNumber(SequenceNumberActionType action, Long number, Long result) {
        StepVerifier.create(bc.updateSequenceNumberWithResponse(action, number, null))
            .assertNext(r -> {
                assertTrue(validateBasicHeaders(r.getHeaders()));
                assertEquals(result, r.getValue().getBlobSequenceNumber());
            })
            .verifyComplete();

        StepVerifier.create(bc.getProperties())
            .assertNext(r -> assertEquals(result, r.getBlobSequenceNumber()))
            .verifyComplete();
    }

    private static Stream<Arguments> sequenceNumberSupplier() {
        return Stream.of(
            Arguments.of(SequenceNumberActionType.UPDATE, 5L, 5L),
            Arguments.of(SequenceNumberActionType.INCREMENT, null, 1L),
            Arguments.of(SequenceNumberActionType.MAX, 2L, 2L));
    }

    @Test
    public void sequenceNumberMin() {
        assertAsyncResponseStatusCode(bc.updateSequenceNumberWithResponse(SequenceNumberActionType.INCREMENT,
            null, null), 200);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void sequenceNumberAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                 String leaseID, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");

        Mono<Response<PageBlobItem>> response = bc.setTags(t)
            .then(Mono.zip(setupBlobLeaseCondition(bc, leaseID), setupBlobMatchCondition(bc, match)))
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

                return bc.updateSequenceNumberWithResponse(SequenceNumberActionType.UPDATE, 1L, bac);
            });

        assertAsyncResponseStatusCode(response, 200);
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsFailSupplier")
    public void sequenceNumberACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                     String leaseID, String tags) {
        Mono<Response<PageBlobItem>> response = Mono.zip(setupBlobLeaseCondition(bc, leaseID),
            setupBlobMatchCondition(bc, noneMatch))
            .flatMap(tuple -> {
                String newLease = tuple.getT1();
                String newNoneMatch = tuple.getT2();
                if ("null".equals(newLease)) {
                    newLease = null;
                }
                if ("null".equals(newNoneMatch)) {
                    newNoneMatch = null;
                }
                BlobRequestConditions bac = new BlobRequestConditions()
                    .setLeaseId(newLease)
                    .setIfMatch(match)
                    .setIfNoneMatch(newNoneMatch)
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified)
                    .setTagsConditions(tags);

                return bc.updateSequenceNumberWithResponse(SequenceNumberActionType.UPDATE, 1L, bac);
            });

        StepVerifier.create(response)
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void sequenceNumberError() {
        bc = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        StepVerifier.create(bc.updateSequenceNumber(SequenceNumberActionType.UPDATE, 0L))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void startIncrementalCopy() {
        PageBlobAsyncClient bc2 = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        String sas = bc.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobSasPermission().setTagsPermission(true).setReadPermission(true)));
        long ms = ENVIRONMENT.getTestMode() == TestMode.PLAYBACK ? 0L : 1L;

        Mono<BlobProperties> response = bc.createSnapshot().flatMap(snapId ->
            bc2.copyIncrementalWithResponse(bc.getBlobUrl() + "?" + sas, snapId.getSnapshotId(),
            null))
            .flatMap(copyResponse -> {
                validateBasicHeaders(copyResponse.getHeaders());
                assertNotNull(copyResponse.getHeaders().getValue(X_MS_COPY_ID));
                assertNotNull(copyResponse.getValue());

                Mono<CopyStatusType> statusMono = Mono.just(copyResponse.getValue());
                OffsetDateTime start = testResourceNamer.now();

                return statusMono.expand(status -> {
                    if (status == CopyStatusType.SUCCESS) {
                        return Mono.empty();
                    } else {
                        return bc2.getProperties().map(BlobProperties::getCopyStatus)
                            .delaySubscription(Duration.ofSeconds(ms))
                            .doOnNext(currentStatus -> {
                                OffsetDateTime currentTime = testResourceNamer.now();
                                if (currentStatus == CopyStatusType.FAILED || currentTime.minusMinutes(1).isAfter(start)) {
                                    throw new RuntimeException("Copy failed or took too long");
                                }
                            });
                    }
                }).last();
            })
            .flatMap(status -> bc2.getProperties());

        StepVerifier.create(response)
            .assertNext(r -> {
                assertTrue(r.isIncrementalCopy());
                assertNotNull(r.getCopyDestinationSnapshot());
            })
            .verifyComplete();
    }

    @Test
    public void startIncrementalCopyMin() {
        PageBlobAsyncClient bc2 = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        String sas = bc.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobSasPermission().setTagsPermission(true).setReadPermission(true)));

        Mono<Response<CopyStatusType>> response = bc.createSnapshot()
            .flatMap(r -> bc2.copyIncrementalWithResponse(bc.getBlobUrl() + "?" + sas, r.getSnapshotId(),
                null));

        assertAsyncResponseStatusCode(response, 202);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("startIncrementalCopyACSupplier")
    public void startIncrementalCopyAC(OffsetDateTime modified, OffsetDateTime unmodified, String match,
                                       String noneMatch, String tags) {
        PageBlobAsyncClient bc2 = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        String sas = bc.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobSasPermission().setTagsPermission(true).setReadPermission(true)));

        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");

        long ms = ENVIRONMENT.getTestMode() == TestMode.PLAYBACK ? 0L : 1L;

        Mono<Response<CopyStatusType>> response = bc.createSnapshot().flatMap(snapId ->
            bc2.copyIncrementalWithResponse(bc.getBlobUrl() + "?" + sas, snapId.getSnapshotId(),
                null))
            .flatMap(copyResponse -> {
                Mono<CopyStatusType> statusMono = Mono.just(copyResponse.getValue());
                OffsetDateTime start = testResourceNamer.now();

                return statusMono.expand(status -> {
                    if (status == CopyStatusType.SUCCESS) {
                        return Mono.empty();
                    } else {
                        return bc2.getProperties().map(BlobProperties::getCopyStatus)
                            .delaySubscription(Duration.ofSeconds(ms))
                            .doOnNext(currentStatus -> {
                                OffsetDateTime currentTime = testResourceNamer.now();
                                if (currentStatus == CopyStatusType.FAILED || currentTime.minusMinutes(1).isAfter(start)) {
                                    throw new RuntimeException("Copy failed or took too long");
                                }
                            });
                    }
                }).last();
            })
            .flatMap(status -> bc2.setTags(t))
            .then(bc.createSnapshot())
            .flatMap(snapId ->
                setupBlobMatchCondition(bc2, match).flatMap(r -> {
                    if ("null".equals(r)) {
                        r = null;
                    }
                    PageBlobCopyIncrementalRequestConditions mac = new PageBlobCopyIncrementalRequestConditions()
                        .setIfModifiedSince(modified)
                        .setIfUnmodifiedSince(unmodified)
                        .setIfMatch(r)
                        .setIfNoneMatch(noneMatch)
                        .setTagsConditions(tags);
                    return bc2.copyIncrementalWithResponse(new PageBlobCopyIncrementalOptions(
                        bc.getBlobUrl() + "?" + sas, snapId.getSnapshotId()).setRequestConditions(mac));
                })
            );
        assertAsyncResponseStatusCode(response, 202);
    }

    private static Stream<Arguments> startIncrementalCopyACSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null, null),
            Arguments.of(OLD_DATE, null, null, null, null),
            Arguments.of(null, NEW_DATE, null, null, null),
            Arguments.of(null, null, RECEIVED_ETAG, null, null),
            Arguments.of(null, null, null, GARBAGE_ETAG, null),
            Arguments.of(null, null, null, null, "\"foo\" = 'bar'"));
    }

    @ParameterizedTest
    @MethodSource("startIncrementalCopyACFailSupplier")
    public void startIncrementalCopyACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
                                           String noneMatch, String tags) {
        PageBlobAsyncClient bc2 = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        String sas = bc.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobSasPermission().setTagsPermission(true).setReadPermission(true)));

        Mono<Response<CopyStatusType>> response = bc.createSnapshot()
            .flatMap(snapId ->
            bc2.copyIncremental(bc.getBlobUrl() + "?" + sas, snapId.getSnapshotId())
                .then(bc.createSnapshot())
                .flatMap(finalSnapshot ->
                    setupBlobMatchCondition(bc2, noneMatch).flatMap(r -> {
                        if ("null".equals(r)) {
                            r = null;
                        }
                        PageBlobCopyIncrementalRequestConditions mac = new PageBlobCopyIncrementalRequestConditions()
                            .setIfModifiedSince(modified)
                            .setIfUnmodifiedSince(unmodified)
                            .setIfMatch(match)
                            .setIfNoneMatch(r)
                            .setTagsConditions(tags);

                        return bc2.copyIncrementalWithResponse(new PageBlobCopyIncrementalOptions(
                            bc.getBlobUrl() + "?" + sas, finalSnapshot.getSnapshotId()).setRequestConditions(mac));
                    })
                )
            );

        StepVerifier.create(response)
            .verifyError(BlobStorageException.class);
    }

    private static Stream<Arguments> startIncrementalCopyACFailSupplier() {
        return Stream.of(
            Arguments.of(NEW_DATE, null, null, null, null),
            Arguments.of(null, OLD_DATE, null, null, null),
            Arguments.of(null, null, GARBAGE_ETAG, null, null),
            Arguments.of(null, null, null, RECEIVED_ETAG, null),
            Arguments.of(null, null, null, null, "\"notfoo\" = 'notbar'"));
    }

    @Test
    public void startIncrementalCopyError() {
        bc = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        StepVerifier.create(bc.copyIncremental("https://www.error.com", "snapshot"))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void getContainerName() {
        assertEquals(containerName, bc.getContainerName());
    }

    @Test
    public void getPageBlobName() {
        assertEquals(blobName, bc.getBlobName());
    }

    @ParameterizedTest
    @MethodSource("getBlobNameAndBuildClientSupplier")
    public void getBlobNameAndBuildClient(String originalBlobName, String finalBlobName) {
        BlobAsyncClient client = ccAsync.getBlobAsyncClient(originalBlobName);
        PageBlobAsyncClient blockClient = ccAsync.getBlobAsyncClient(client.getBlobName()).getPageBlobAsyncClient();
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
    public void createOverwriteFalse() {
        StepVerifier.create(bc.create(512))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void createOverwriteTrue() {
        StepVerifier.create(bc.create(512, true))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials
    // and auth would fail because we changed a signed header.
    public void perCallPolicy() {
        PageBlobAsyncClient specialBlob = getSpecializedBuilder(bc.getBlobUrl())
            .addPolicy(getPerCallVersionPolicy())
            .buildPageBlobAsyncClient();

        StepVerifier.create(specialBlob.getPropertiesWithResponse(null))
            .assertNext(r ->  assertEquals("2017-11-09", r.getHeaders().getValue(X_MS_VERSION)))
            .verifyComplete();
    }

    @Test
    public void defaultAudience() {
        PageBlobAsyncClient aadBlob = getSpecializedBuilderWithTokenCredential(bc.getBlobUrl())
            .audience(null)
            .buildPageBlobAsyncClient();

        StepVerifier.create(aadBlob.exists())
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    public void storageAccountAudience() {
        PageBlobAsyncClient aadBlob = getSpecializedBuilderWithTokenCredential(bc.getBlobUrl())
            .audience(BlobAudience.createBlobServiceAccountAudience(ccAsync.getAccountName()))
            .buildPageBlobAsyncClient();

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
        PageBlobAsyncClient aadBlob = getSpecializedBuilderWithTokenCredential(bc.getBlobUrl())
            .audience(BlobAudience.createBlobServiceAccountAudience("badAudience"))
            .buildPageBlobAsyncClient();

        StepVerifier.create(aadBlob.getProperties())
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();
    }

    @Test
    public void audienceFromString() {
        String url = String.format("https://%s.blob.core.windows.net/", ccAsync.getAccountName());
        BlobAudience audience = BlobAudience.fromString(url);

        PageBlobAsyncClient aadBlob = getSpecializedBuilderWithTokenCredential(bc.getBlobUrl())
            .audience(audience)
            .buildPageBlobAsyncClient();

        StepVerifier.create(aadBlob.exists())
            .expectNext(true)
            .verifyComplete();
    }



}
