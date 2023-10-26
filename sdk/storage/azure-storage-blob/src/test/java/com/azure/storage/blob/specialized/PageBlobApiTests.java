// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.exception.UnexpectedLengthException;
import com.azure.core.http.HttpRange;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.CoreUtils;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
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
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.models.SequenceNumberActionType;
import com.azure.storage.blob.options.BlobGetTagsOptions;
import com.azure.storage.blob.options.ListPageRangesDiffOptions;
import com.azure.storage.blob.options.ListPageRangesOptions;
import com.azure.storage.blob.options.PageBlobCopyIncrementalOptions;
import com.azure.storage.blob.options.PageBlobCreateOptions;
import com.azure.storage.common.implementation.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PageBlobApiTests extends BlobTestBase {
    private PageBlobClient bc;
    private String blobName;

    @BeforeEach
    public void setup() {
        blobName = generateBlobName();
        bc = cc.getBlobClient(blobName).getPageBlobClient();
        bc.create(PageBlobClient.PAGE_BYTES);
    }

    @Test
    public void createAllNull() {
        bc = cc.getBlobClient(generateBlobName()).getPageBlobClient();

        Response<PageBlobItem> response = bc.createWithResponse(PageBlobClient.PAGE_BYTES, null, null, null, null, null,
            null);

        assertResponseStatusCode(response, 201);
        assertTrue(validateBasicHeaders(response.getHeaders()));
        assertNull(response.getValue().getContentMd5());
        assertTrue(response.getValue().isServerEncrypted());
    }

    @Test
    public void createMin() {
        assertResponseStatusCode(bc.createWithResponse(PageBlobClient.PAGE_BYTES, null, null, null, null, null, null),
            201);
    }

    @Test
    public void createSequenceNumber() {
        bc.createWithResponse(PageBlobClient.PAGE_BYTES, 2L, null, null, null,
            null, null);
        assertEquals(bc.getProperties().getBlobSequenceNumber(), 2);
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

        bc.createWithResponse(PageBlobClient.PAGE_BYTES, null, headers, null, null, null, null);

        Response<BlobProperties> response = bc.getPropertiesWithResponse(null, null, null);

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType;
        assertTrue(validateBlobProperties(response, cacheControl, contentDisposition, contentEncoding, contentLanguage,
            contentMD5, contentType));
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
        bc.createWithResponse(PageBlobClient.PAGE_BYTES, null, null, metadata, null, null, null);
        Response<BlobProperties> response = bc.getPropertiesWithResponse(null, null, null);

        assertResponseStatusCode(response, 200);
        assertEquals(response.getValue().getMetadata(), metadata);
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

        bc.createWithResponse(new PageBlobCreateOptions(PageBlobClient.PAGE_BYTES).setTags(tags), null, null);

        Response<Map<String, String>> response = bc.getTagsWithResponse(new BlobGetTagsOptions(), null, null);

        assertResponseStatusCode(response, 200);
        assertEquals(response.getValue(), tags);
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
        bc.setTags(t);
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(setupBlobLeaseCondition(bc, leaseID))
            .setIfMatch(setupBlobMatchCondition(bc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        assertResponseStatusCode(bc.createWithResponse(PageBlobClient.PAGE_BYTES, null, null, null, bac, null, null),
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

        assertThrows(BlobStorageException.class, () -> bc.createWithResponse(PageBlobClient.PAGE_BYTES, null, null,
            null, bac, null, null));
    }

    @Test
    public void createError() {
        assertThrows(BlobStorageException.class, () -> bc.createWithResponse(PageBlobClient.PAGE_BYTES,
            null, null, null, new BlobRequestConditions().setLeaseId("id"), null, null));
    }

    @Test
    public void createIfNotExistsAllNull() {
        bc = cc.getBlobClient(generateBlobName()).getPageBlobClient();

        Response<PageBlobItem> response = bc.createIfNotExistsWithResponse(
            new PageBlobCreateOptions(PageBlobClient.PAGE_BYTES), null, null);

        assertResponseStatusCode(response, 201);
        assertTrue(validateBasicHeaders(response.getHeaders()));
        assertNull(response.getValue().getContentMd5());
        assertTrue(response.getValue().isServerEncrypted());
    }

    @Test
    public void createIfNotExistsBlobThatAlreadyExists() {
        String blobName = cc.getBlobClient(generateBlobName()).getBlobName();
        bc = cc.getBlobClient(blobName).getPageBlobClient();
        PageBlobCreateOptions options = new PageBlobCreateOptions(PageBlobClient.PAGE_BYTES);
        Response<PageBlobItem> initialResponse = bc.createIfNotExistsWithResponse(options, null, null);

        Response<PageBlobItem> secondResponse = bc.createIfNotExistsWithResponse(options, null, null);

        assertResponseStatusCode(initialResponse, 201);
        assertResponseStatusCode(secondResponse, 409);
    }

    @Test
    public void createIfNotExistsMin() {
        String blobName = cc.getBlobClient(generateBlobName()).getBlobName();
        bc = cc.getBlobClient(blobName).getPageBlobClient();

        assertResponseStatusCode(bc.createIfNotExistsWithResponse(new PageBlobCreateOptions(PageBlobClient.PAGE_BYTES),
            null, null), 201);
    }

    @Test
    public void createIfNotExistsSequenceNumber() {
        String blobName = cc.getBlobClient(generateBlobName()).getBlobName();
        bc = cc.getBlobClient(blobName).getPageBlobClient();

        bc.createIfNotExistsWithResponse(new PageBlobCreateOptions(PageBlobClient.PAGE_BYTES).setSequenceNumber(2L),
            null, null);

        assertEquals(bc.getProperties().getBlobSequenceNumber(), 2);
    }

    @ParameterizedTest
    @MethodSource("createIfNotExistsHeadersSupplier")
    public void createIfNotExistsHeaders(String cacheControl, String contentDisposition, String contentEncoding,
        String contentLanguage, byte[] contentMD5, String contentType) {
        String blobName = cc.getBlobClient(generateBlobName()).getBlobName();
        bc = cc.getBlobClient(blobName).getPageBlobClient();

        BlobHttpHeaders headers = new BlobHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType);

        bc.createIfNotExistsWithResponse(new PageBlobCreateOptions(PageBlobClient.PAGE_BYTES).setHeaders(headers),
            null, null);

        Response<BlobProperties> response = bc.getPropertiesWithResponse(null, null, null);

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType;

        assertTrue(validateBlobProperties(response, cacheControl, contentDisposition, contentEncoding, contentLanguage,
            contentMD5, contentType));
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
        String blobName = cc.getBlobClient(generateBlobName()).getBlobName();
        bc = cc.getBlobClient(blobName).getPageBlobClient();

        Map<String, String> metadata = new HashMap<>();
        if (key1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null) {
            metadata.put(key2, value2);
        }

        bc.createIfNotExistsWithResponse(new PageBlobCreateOptions(PageBlobClient.PAGE_BYTES).setMetadata(metadata),
            null, null);
        Response<BlobProperties> response = bc.getPropertiesWithResponse(null, null, null);

        assertResponseStatusCode(response, 200);
        assertEquals(response.getValue().getMetadata(), metadata);
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
        String blobName = cc.getBlobClient(generateBlobName()).getBlobName();
        bc = cc.getBlobClient(blobName).getPageBlobClient();

        Map<String, String> tags = new HashMap<>();
        if (key1 != null) {
            tags.put(key1, value1);
        }
        if (key2 != null) {
            tags.put(key2, value2);
        }

        bc.createIfNotExistsWithResponse(new PageBlobCreateOptions(PageBlobClient.PAGE_BYTES).setTags(tags),
            null, null);

        Response<Map<String, String>> response = bc.getTagsWithResponse(new BlobGetTagsOptions(), null, null);

        assertResponseStatusCode(response, 200);
        assertEquals(response.getValue(), tags);
    }

    private static Stream<Arguments> createIfNotExistsTagsSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null),
            Arguments.of("foo", "bar", "fizz", "buzz"),
            Arguments.of(" +-./:=_  +-./:=_", " +-./:=_", null, null));
    }


    @Test
    public void uploadPage() {
        Response<PageBlobItem> response = bc.uploadPagesWithResponse(
            new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)), null, null, null, null);

        assertResponseStatusCode(response, 201);
        assertTrue(validateBasicHeaders(response.getHeaders()));
        assertNotNull(response.getHeaders().getValue("x-ms-content-crc64"));
        assertEquals(response.getValue().getBlobSequenceNumber(), 0);
        assertTrue(response.getValue().isServerEncrypted());
    }

    @Test
    public void uploadPageMin() {
        assertResponseStatusCode(bc.uploadPagesWithResponse(
            new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
                new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)), null, null, null, null), 201);
    }

    @ParameterizedTest
    @MethodSource("uploadPageIASupplier")
    public void uploadPageIA(Integer dataSize, Throwable exceptionType) {
        ByteArrayInputStream data = (dataSize == null) ? null : new ByteArrayInputStream(getRandomByteArray(dataSize));
        assertThrows(exceptionType.getClass(), () -> bc.uploadPages(
            new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES * 2 - 1), data));
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

        assertResponseStatusCode(bc.uploadPagesWithResponse(
            new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(data), md5, null, null, null), 201);
    }

    @Test
    public void uploadPageTransactionalMD5Fail() {
        BlobStorageException e = assertThrows(BlobStorageException.class, () ->
            bc.uploadPagesWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
                new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)),
                MessageDigest.getInstance("MD5").digest("garbage".getBytes()), null, null, null));
        assertEquals(BlobErrorCode.MD5MISMATCH, e.getErrorCode());
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("uploadPageACSupplier")
    public void uploadPageAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID, Long sequenceNumberLT, Long sequenceNumberLTE, Long sequenceNumberEqual, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t);
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

        assertResponseStatusCode(bc.uploadPagesWithResponse(
            new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
                new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)), null, pac, null, null), 201);
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

        assertThrows(BlobStorageException.class, () -> bc.uploadPagesWithResponse(
            new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)), null, pac, null, null));
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
        bc = cc.getBlobClient(generateBlobName()).getPageBlobClient();

        assertThrows(BlobStorageException.class, () -> bc.uploadPagesWithResponse(
            new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)), null,
            new PageBlobRequestConditions().setLeaseId("id"), null, null));
    }

    @Test
    public void uploadPageRetryOnTransientFailure() {
        PageBlobClient clientWithFailure = getBlobClient(
            ENVIRONMENT.getPrimaryAccount().getCredential(), bc.getBlobUrl(),
            new TransientFailureInjectingHttpPipelinePolicy()).getPageBlobClient();

        byte[] data = getRandomByteArray(PageBlobClient.PAGE_BYTES);
        clientWithFailure.uploadPages(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(data));

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bc.downloadStream(os);
        TestUtils.assertArraysEqual(os.toByteArray(), data);
    }

    @Test
    public void uploadPageFromURLMin() {
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null);
        PageBlobClient destURL = cc.getBlobClient(generateBlobName()).getPageBlobClient();
        destURL.create(PageBlobClient.PAGE_BYTES);
        destURL.uploadPages(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)));
        PageRange pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1);

        Response<PageBlobItem> response = bc.uploadPagesFromUrlWithResponse(pageRange, destURL.getBlobUrl(), null, null,
            null, null, null, null);

        assertResponseStatusCode(response, 201);
        assertTrue(validateBasicHeaders(response.getHeaders()));
    }

    @Test
    public void uploadPageFromURLRange() {
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null);

        byte[] data = getRandomByteArray(PageBlobClient.PAGE_BYTES * 4);

        PageBlobClient sourceURL = cc.getBlobClient(generateBlobName()).getPageBlobClient();
        sourceURL.create(PageBlobClient.PAGE_BYTES * 4);
        sourceURL.uploadPages(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES * 4 - 1),
            new ByteArrayInputStream(data));

        PageBlobClient destURL = cc.getBlobClient(generateBlobName()).getPageBlobClient();
        destURL.create(PageBlobClient.PAGE_BYTES * 2);

        destURL.uploadPagesFromUrl(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES * 2 - 1),
            sourceURL.getBlobUrl(), PageBlobClient.PAGE_BYTES * 2L);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        destURL.downloadStream(outputStream);
        TestUtils.assertArraysEqual(data, PageBlobClient.PAGE_BYTES * 2, outputStream.toByteArray(), 0,
            PageBlobClient.PAGE_BYTES * 2);
    }

    @Test
    public void uploadPageFromURLIA() {
        assertThrows(IllegalArgumentException.class, () -> bc.uploadPagesFromUrl(null, bc.getBlobUrl(),
            (long) PageBlobClient.PAGE_BYTES));
    }

    @Test
    public void uploadPageFromURLMD5() throws NoSuchAlgorithmException {
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null);
        PageBlobClient destURL = cc.getBlobClient(generateBlobName()).getPageBlobClient();
        destURL.create(PageBlobClient.PAGE_BYTES);
        byte[] data = getRandomByteArray(PageBlobClient.PAGE_BYTES);
        PageRange pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1);
        bc.uploadPages(pageRange, new ByteArrayInputStream(data));

        assertDoesNotThrow(() -> destURL.uploadPagesFromUrlWithResponse(pageRange, bc.getBlobUrl(), null,
            MessageDigest.getInstance("MD5").digest(data), null, null, null, null));
    }

    @Test
    public void uploadPageFromURLMD5Fail() {
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null);
        PageBlobClient destURL = cc.getBlobClient(generateBlobName()).getPageBlobClient();
        destURL.create(PageBlobClient.PAGE_BYTES);
        PageRange pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1);
        bc.uploadPages(pageRange, new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)));

        assertThrows(BlobStorageException.class, () -> destURL.uploadPagesFromUrlWithResponse(pageRange,
            bc.getBlobUrl(), null, MessageDigest.getInstance("MD5").digest("garbage".getBytes()), null, null, null,
            null));

    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("uploadPageACSupplier")
    public void uploadPageFromURLDestinationAC(OffsetDateTime modified, OffsetDateTime unmodified, String match,
        String noneMatch, String leaseID, Long sequenceNumberLT, Long sequenceNumberLTE, Long sequenceNumberEqual,
        String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t);
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null);
        PageBlobClient sourceURL = cc.getBlobClient(generateBlobName()).getPageBlobClient();
        sourceURL.create(PageBlobClient.PAGE_BYTES);
        PageRange pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1);
        sourceURL.uploadPages(pageRange, new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)));

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

        assertResponseStatusCode(bc.uploadPagesFromUrlWithResponse(pageRange, sourceURL.getBlobUrl(), null, null, pac,
            null, null, null), 201);
    }

    @ParameterizedTest
    @MethodSource("uploadPageACFailSupplier")
    public void uploadPageFromURLDestinationACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
        String noneMatch, String leaseID, Long sequenceNumberLT, Long sequenceNumberLTE, Long sequenceNumberEqual,
        String tags) {
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null);

        PageBlobClient sourceURL = cc.getBlobClient(generateBlobName()).getPageBlobClient();
        sourceURL.create(PageBlobClient.PAGE_BYTES);
        PageRange pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1);
        sourceURL.uploadPages(pageRange, new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)));

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

        assertThrows(BlobStorageException.class, () -> bc.uploadPagesFromUrlWithResponse(
            pageRange, sourceURL.getBlobUrl(), null, null, pac, null, null, null));
    }

    @ParameterizedTest
    @MethodSource("uploadPageFromURLSourceACSupplier")
    public void uploadPageFromURLSourceAC(OffsetDateTime sourceIfModifiedSince, OffsetDateTime sourceIfUnmodifiedSince,
        String sourceIfMatch, String sourceIfNoneMatch) {
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null);
        PageBlobClient sourceURL = cc.getBlobClient(generateBlobName()).getPageBlobClient();
        sourceURL.create(PageBlobClient.PAGE_BYTES);
        PageRange pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1);
        sourceURL.uploadPages(pageRange, new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)));

        sourceIfMatch = setupBlobMatchCondition(sourceURL, sourceIfMatch);
        BlobRequestConditions smac = new BlobRequestConditions()
            .setIfModifiedSince(sourceIfModifiedSince)
            .setIfUnmodifiedSince(sourceIfUnmodifiedSince)
            .setIfMatch(sourceIfMatch)
            .setIfNoneMatch(sourceIfNoneMatch);

        assertResponseStatusCode(bc.uploadPagesFromUrlWithResponse(pageRange, sourceURL.getBlobUrl(), null, null, null,
            smac, null, null), 201);
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
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null);
        PageBlobClient sourceURL = cc.getBlobClient(generateBlobName()).getPageBlobClient();
        sourceURL.create(PageBlobClient.PAGE_BYTES);
        PageRange pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1);
        sourceURL.uploadPages(pageRange, new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)));

        BlobRequestConditions smac = new BlobRequestConditions()
            .setIfModifiedSince(sourceIfModifiedSince)
            .setIfUnmodifiedSince(sourceIfUnmodifiedSince)
            .setIfMatch(sourceIfMatch)
            .setIfNoneMatch(setupBlobMatchCondition(sourceURL, sourceIfNoneMatch));

        assertThrows(BlobStorageException.class, () -> bc.uploadPagesFromUrlWithResponse(
            pageRange, sourceURL.getBlobUrl(), null, null, null, smac, null, null));
    }

    private static Stream<Arguments> uploadPageFromURLSourceACFailSupplier() {
        return Stream.of(
            Arguments.of(NEW_DATE, null, null, null),
            Arguments.of(null, OLD_DATE, null, null),
            Arguments.of(null, null, GARBAGE_ETAG, null),
            Arguments.of(null, null, null, RECEIVED_ETAG));
    }

    @Test
    public void clearPage() {
        bc.uploadPagesWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)), null, null, null, null);

        Response<PageBlobItem> response = bc.clearPagesWithResponse(
            new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1), null, null, null);

        assertEquals(0, bc.getPageRanges(new BlobRange(0)).getPageRange().size());
        assertTrue(validateBasicHeaders(response.getHeaders()));
        assertNull(response.getValue().getContentMd5());
        assertEquals(0, response.getValue().getBlobSequenceNumber());
    }

    @Test
    public void clearPageMin() {
        assertNotNull(bc.clearPages(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1)));
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("uploadPageACSupplier")
    public void clearPagesAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID, Long sequenceNumberLT, Long sequenceNumberLTE, Long sequenceNumberEqual, String tags) {
        bc.uploadPages(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)));
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t);
        match = setupBlobMatchCondition(bc, match);
        leaseID = setupBlobLeaseCondition(bc, leaseID);
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

        assertResponseStatusCode(bc.clearPagesWithResponse(
            new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1), pac, null, null), 201);
    }

    @ParameterizedTest
    @MethodSource("uploadPageACFailSupplier")
    public void clearPagesACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID, Long sequenceNumberLT, Long sequenceNumberLTE, Long sequenceNumberEqual, String tags) {
        bc.uploadPages(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)));
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

        assertThrows(BlobStorageException.class, () -> bc.clearPagesWithResponse(
            new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1), pac, null, null));
    }

    @Test
    public void clearPageError() {
        bc = cc.getBlobClient(generateBlobName()).getPageBlobClient();

        assertThrows(BlobStorageException.class, () ->
            bc.clearPages(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1)));
    }

    @Test
    public void getPageRanges() {
        bc.uploadPages(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)));
        Response<PageList> response = bc.getPageRangesWithResponse(new BlobRange(0,
            (long) PageBlobClient.PAGE_BYTES), null, null, null);

        assertResponseStatusCode(response, 200);
        assertEquals(1, response.getValue().getPageRange().size());
        assertTrue(validateBasicHeaders(response.getHeaders()));
        assertEquals("512", response.getHeaders().getValue(X_MS_BLOB_CONTENT_LENGTH));
    }

    @Test
    public void getPageRangesMin() {
        assertDoesNotThrow(() -> bc.getPageRanges(null));
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void getPageRangesAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
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

        assertDoesNotThrow(() -> bc.getPageRangesWithResponse(new BlobRange(0, (long) PageBlobClient.PAGE_BYTES),
            bac, null, null));
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsFailSupplier")
    public void getPageRangesACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID, String tags) {
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(setupBlobLeaseCondition(bc, leaseID))
            .setIfMatch(match)
            .setIfNoneMatch(setupBlobMatchCondition(bc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        assertThrows(BlobStorageException.class, () -> bc.getPageRangesWithResponse(
            new BlobRange(0, (long) PageBlobClient.PAGE_BYTES), bac, null, null));
    }

    @Test
    public void getPageRangesError() {
        bc = cc.getBlobClient(generateBlobName()).getPageBlobClient();
        assertThrows(BlobStorageException.class, () -> bc.getPageRanges(null));
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20210608ServiceVersion")
    @Test
    public void listPageRanges() {
        bc.create(4 * Constants.KB, true);
        ByteArrayInputStream data = new ByteArrayInputStream(getRandomByteArray(4 * Constants.KB));
        bc.uploadPages(new PageRange().setStart(0).setEnd(4 * Constants.KB - 1), data);
        bc.clearPages(new PageRange().setStart(Constants.KB).setEnd(2 * Constants.KB - 1));
        bc.clearPages(new PageRange().setStart(3 * Constants.KB).setEnd(4 * Constants.KB - 1));

        Iterator<PageRangeItem> iterable = bc.listPageRanges(new BlobRange(0, (long) 4 * Constants.KB)).iterator();
        PageRangeItem item = iterable.next();

        assertEquals(item.getRange(), new HttpRange(0, (long) Constants.KB));
        assertFalse(item.isClear());

        item = iterable.next();

        assertEquals(item.getRange(), new HttpRange(2 * Constants.KB, (long) Constants.KB));
        assertFalse(item.isClear());

        assertFalse(iterable.hasNext());
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20210608ServiceVersion")
    @Test
    public void listPagesRangesPageSize() {
        bc.create(4 * Constants.KB, true);
        ByteArrayInputStream data = new ByteArrayInputStream(getRandomByteArray(4 * Constants.KB));
        data.mark(Integer.MAX_VALUE);
        bc.uploadPages(new PageRange().setStart(0).setEnd(4 * Constants.KB - 1), data);
        bc.clearPages(new PageRange().setStart(Constants.KB).setEnd(2 * Constants.KB - 1));
        bc.clearPages(new PageRange().setStart(3 * Constants.KB).setEnd(4 * Constants.KB - 1));

        // when: "max results on options"
        Iterator<PagedResponse<PageRangeItem>> iterator = bc.listPageRanges(new ListPageRangesOptions(
            new BlobRange(0, 4L * Constants.KB)).setMaxResultsPerPage(1), null, null)
            .iterableByPage().iterator();
        PagedResponse<PageRangeItem> page = iterator.next();

        assertEquals(page.getValue().size(), 1);

        page = iterator.next();

        assertEquals(page.getValue().size(), 1);
        assertFalse(iterator.hasNext());

        // when: "max results on iterableByPage"
        iterator = bc.listPageRanges(new ListPageRangesOptions(new BlobRange(0, 4L * Constants.KB)),
            null, null).iterableByPage(1).iterator();
        page = iterator.next();

        assertEquals(page.getValue().size(), 1);

        page = iterator.next();

        assertEquals(page.getValue().size(), 1);
        assertFalse(iterator.hasNext());
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20210608ServiceVersion")
    @Test
    public void listPagesContinuationToken() {
        bc.create(4 * Constants.KB, true);
        ByteArrayInputStream data = new ByteArrayInputStream(getRandomByteArray(4 * Constants.KB));
        data.mark(Integer.MAX_VALUE);
        bc.uploadPages(new PageRange().setStart(0).setEnd(4 * Constants.KB - 1), data);
        bc.clearPages(new PageRange().setStart(Constants.KB).setEnd(2 * Constants.KB - 1));
        bc.clearPages(new PageRange().setStart(3 * Constants.KB).setEnd(4 * Constants.KB - 1));

        Iterator<PagedResponse<PageRangeItem>> iterator = bc.listPageRanges(new ListPageRangesOptions(
            new BlobRange(0, 4L * Constants.KB)).setMaxResultsPerPage(1), null, null)
            .iterableByPage().iterator();
        String token = iterator.next().getContinuationToken();

        iterator = bc.listPageRanges(new ListPageRangesOptions(new BlobRange(0, 4L * Constants.KB)),
            null, null).iterableByPage(token).iterator();
        PagedResponse<PageRangeItem> page = iterator.next();

        assertEquals(page.getValue().size(), 1);
        assertFalse(iterator.hasNext());
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20210608ServiceVersion")
    @Test
    public void listPagesRange() {
        bc.create(4 * Constants.KB, true);
        ByteArrayInputStream data = new ByteArrayInputStream(getRandomByteArray(4 * Constants.KB));
        data.mark(Integer.MAX_VALUE);
        bc.uploadPages(new PageRange().setStart(0).setEnd(4 * Constants.KB - 1), data);
        bc.clearPages(new PageRange().setStart(Constants.KB).setEnd(2 * Constants.KB - 1));
        bc.clearPages(new PageRange().setStart(3 * Constants.KB).setEnd(4 * Constants.KB - 1));

        Iterator<PageRangeItem> iterator = bc.listPageRanges(new ListPageRangesOptions(
            new BlobRange(2 * Constants.KB + 1, 2L * Constants.KB)), null, null).iterator();

        int size = 0;
        while (iterator.hasNext()) {
            size++;
            iterator.next();
        }
        assertEquals(size, 1);
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20210608ServiceVersion")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void listPagesRangesAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
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

        assertDoesNotThrow(() -> bc.listPageRanges(new ListPageRangesOptions(new BlobRange(0,
            (long) PageBlobClient.PAGE_BYTES)).setRequestConditions(bac), null, null));
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20210608ServiceVersion")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsFailSupplier")
    public void listPageRangesACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID, String tags) {
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(setupBlobLeaseCondition(bc, leaseID))
            .setIfMatch(match)
            .setIfNoneMatch(setupBlobMatchCondition(bc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        assertThrows(BlobStorageException.class, () -> bc.listPageRanges(new ListPageRangesOptions(
            new BlobRange(0, (long) PageBlobClient.PAGE_BYTES)).setRequestConditions(bac), null, null).stream()
            .count());
    }

    @ParameterizedTest
    @MethodSource("getPageRangesDiffSupplier")
    public void getPageRangesDiff(List<PageRange> rangesToUpdate, List<PageRange> rangesToClear,
        List<PageRange> expectedPageRanges, List<ClearRange> expectedClearRanges) {
        bc.create(4 * Constants.MB, true);

        bc.uploadPages(new PageRange().setStart(0).setEnd(4 * Constants.MB - 1),
            new ByteArrayInputStream(getRandomByteArray(4 * Constants.MB)));

        String snapId = bc.createSnapshot().getSnapshotId();

        rangesToUpdate.forEach(it ->
            bc.uploadPages(it, new ByteArrayInputStream(getRandomByteArray((int) (it.getEnd() - it.getStart()) + 1))));

        rangesToClear.forEach(it -> bc.clearPages(it));

        Response<PageList> response = bc.getPageRangesDiffWithResponse(new BlobRange(0, 4L * Constants.MB),
            snapId, null, null, null);

        assertTrue(validateBasicHeaders(response.getHeaders()));
        assertEquals(expectedPageRanges.size(), response.getValue().getPageRange().size());
        assertEquals(expectedClearRanges.size(), response.getValue().getClearRange().size());

        for (int i = 0; i < expectedPageRanges.size(); i++) {
            PageRange actualRange = response.getValue().getPageRange().get(i);
            PageRange expectedRange = expectedPageRanges.get(i);
            assertEquals(expectedRange.getStart(), actualRange.getStart());
            assertEquals(expectedRange.getEnd(), actualRange.getEnd());
        }

        for (int i = 0; i < expectedClearRanges.size(); i++) {
            ClearRange actualRange = response.getValue().getClearRange().get(i);
            ClearRange expectedRange = expectedClearRanges.get(i);
            assertEquals(expectedRange.getStart(), actualRange.getStart());
            assertEquals(expectedRange.getEnd(), actualRange.getEnd());
        }

        assertEquals(Integer.parseInt(response.getHeaders().getValue(X_MS_BLOB_CONTENT_LENGTH)), 4 * Constants.MB);
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

    @Test
    public void getPageRangesDiffMin() {
        String snapId = bc.createSnapshot().getSnapshotId();
        assertDoesNotThrow(() -> bc.getPageRangesDiff(null, snapId).getPageRange());
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void getPageRangesDiffAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t);
        String snapId = bc.createSnapshot().getSnapshotId();
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(setupBlobLeaseCondition(bc, leaseID))
            .setIfMatch(setupBlobMatchCondition(bc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        assertDoesNotThrow(() -> bc.getPageRangesDiffWithResponse(new BlobRange(0,
            (long) PageBlobClient.PAGE_BYTES), snapId, bac, null, null));
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsFailSupplier")
    public void getPageRangesDiffACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
        String noneMatch, String leaseID, String tags) {
        String snapId = bc.createSnapshot().getSnapshotId();

        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(setupBlobLeaseCondition(bc, leaseID))
            .setIfMatch(match)
            .setIfNoneMatch(setupBlobMatchCondition(bc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        assertThrows(BlobStorageException.class, () -> bc.getPageRangesDiffWithResponse(
            new BlobRange(0, (long) PageBlobClient.PAGE_BYTES), snapId, bac, null, null));
    }

    @Test
    public void getPageRangesDiffError() {
        bc = cc.getBlobClient(generateBlobName()).getPageBlobClient();
        assertThrows(BlobStorageException.class, () -> bc.getPageRangesDiff(null, "snapshot"));
    }

    /* Uncomment any managed disk lines if a managed disk account is available to be tested. They are difficult to
     acquire so we do not run them in the nightly live run tests. */
    @Disabled("Requires a managed disk account")
    @Test
    public void getPageRangesDiffPrevSnapshotUrl() {
        BlobServiceClient managedDiskServiceClient = getServiceClient(ENVIRONMENT.getManagedDiskAccount());
        BlobContainerClient managedDiskContainer = managedDiskServiceClient.getBlobContainerClient(generateContainerName());
        managedDiskContainer.create();
        PageBlobClient managedDiskBlob = managedDiskContainer.getBlobClient(generateBlobName()).getPageBlobClient();
        managedDiskBlob.create(PageBlobClient.PAGE_BYTES * 2);

        managedDiskBlob.uploadPages(new PageRange().setStart(PageBlobClient.PAGE_BYTES).setEnd(PageBlobClient.PAGE_BYTES * 2 - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)));

        String snapUrl = managedDiskBlob.createSnapshot().getBlobUrl();

        managedDiskBlob.uploadPages(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)));

        managedDiskBlob.clearPages(new PageRange().setStart(PageBlobClient.PAGE_BYTES).setEnd(PageBlobClient.PAGE_BYTES * 2 - 1));

        Response<PageList> response = managedDiskBlob.getManagedDiskPageRangesDiffWithResponse(
            new BlobRange(0, PageBlobClient.PAGE_BYTES * 2L), snapUrl, null, null, null);

        assertEquals(1, response.getValue().getPageRange().size());
        assertEquals(0, response.getValue().getPageRange().get(0).getStart());
        assertEquals(PageBlobClient.PAGE_BYTES - 1, response.getValue().getPageRange().get(0).getEnd());
        assertEquals(1, response.getValue().getClearRange().size());
        assertEquals(PageBlobClient.PAGE_BYTES, response.getValue().getClearRange().get(0).getStart());
        assertEquals(PageBlobClient.PAGE_BYTES * 2 - 1, response.getValue().getClearRange().get(0).getEnd());
        assertTrue(validateBasicHeaders(response.getHeaders()));
        assertEquals(PageBlobClient.PAGE_BYTES * 2,
            Integer.parseInt(response.getHeaders().getValue(X_MS_BLOB_CONTENT_LENGTH)));
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20210608ServiceVersion")
    @Test
    public void listPagesRangesDiff() {
        bc.create(4 * Constants.KB, true);
        ByteArrayInputStream data = new ByteArrayInputStream(getRandomByteArray(4 * Constants.KB));
        bc.uploadPages(new PageRange().setStart(0).setEnd(4 * Constants.KB - 1), data);
        String snapshot = bc.createSnapshot().getSnapshotId();
        data = new ByteArrayInputStream(getRandomByteArray(Constants.KB));
        data.mark(Integer.MAX_VALUE);
        bc.uploadPages(new PageRange().setStart(0).setEnd(Constants.KB - 1), data);
        data.reset();
        bc.clearPages(new PageRange().setStart(Constants.KB).setEnd(2 * Constants.KB - 1));
        bc.uploadPages(new PageRange().setStart(2 * Constants.KB).setEnd(3 * Constants.KB - 1), data);
        bc.clearPages(new PageRange().setStart(3 * Constants.KB).setEnd(4 * Constants.KB - 1));

        Iterator<PageRangeItem> iterable = bc.listPageRangesDiff(new BlobRange(0, 4L * Constants.KB), snapshot)
            .iterator();
        PageRangeItem item = iterable.next();

        assertEquals(item.getRange(), new HttpRange(0L, (long) Constants.KB));
        assertFalse(item.isClear());

        item = iterable.next();

        assertEquals(item.getRange(), new HttpRange(2 * Constants.KB, (long) Constants.KB));
        assertFalse(item.isClear());

        item = iterable.next();

        assertEquals(item.getRange(), new HttpRange(Constants.KB, (long) Constants.KB));
        assertTrue(item.isClear());

        item = iterable.next();

        assertEquals(item.getRange(), new HttpRange(3 * Constants.KB, (long) Constants.KB));
        assertTrue(item.isClear());

        assertFalse(iterable.hasNext());
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20210608ServiceVersion")
    @Test
    public void listPagesRangesDiffPageSize() {
        bc.create(4 * Constants.KB, true);
        ByteArrayInputStream data = new ByteArrayInputStream(getRandomByteArray(4 * Constants.KB));
        bc.uploadPages(new PageRange().setStart(0).setEnd(4 * Constants.KB - 1), data);
        String snapshot = bc.createSnapshot().getSnapshotId();
        data = new ByteArrayInputStream(getRandomByteArray(Constants.KB));
        data.mark(Integer.MAX_VALUE);
        bc.uploadPages(new PageRange().setStart(0).setEnd(Constants.KB - 1), data);
        data.reset();
        bc.clearPages(new PageRange().setStart(Constants.KB).setEnd(2 * Constants.KB - 1));
        bc.uploadPages(new PageRange().setStart(2 * Constants.KB).setEnd(3 * Constants.KB - 1), data);
        bc.clearPages(new PageRange().setStart(3 * Constants.KB).setEnd(4 * Constants.KB - 1));

        // when: "max results on options"
        Iterator<PagedResponse<PageRangeItem>> iterator = bc.listPageRangesDiff(
            new ListPageRangesDiffOptions(new BlobRange(0, 4L * Constants.KB), snapshot).setMaxResultsPerPage(2), null,
            null).iterableByPage().iterator();
        PagedResponse<PageRangeItem> page = iterator.next();

        assertEquals(page.getValue().size(), 2);
        page = iterator.next();

        assertEquals(page.getValue().size(), 2);
        assertFalse(iterator.hasNext());

        // when: "max results on iterableByPage"
        iterator = bc.listPageRangesDiff(new ListPageRangesDiffOptions(new BlobRange(0, 4L * Constants.KB),
            snapshot), null, null).iterableByPage(2).iterator();
        page = iterator.next();

        assertEquals(page.getValue().size(), 2);

        page = iterator.next();

        assertEquals(page.getValue().size(), 2);
        assertFalse(iterator.hasNext());
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20210608ServiceVersion")
    @Test
    public void listPagesDiffContinuationToken() {
        bc.create(4 * Constants.KB, true);
        ByteArrayInputStream data = new ByteArrayInputStream(getRandomByteArray(4 * Constants.KB));
        bc.uploadPages(new PageRange().setStart(0).setEnd(4 * Constants.KB - 1), data);
        String snapshot = bc.createSnapshot().getSnapshotId();
        data = new ByteArrayInputStream(getRandomByteArray(Constants.KB));
        data.mark(Integer.MAX_VALUE);
        bc.uploadPages(new PageRange().setStart(0).setEnd(Constants.KB - 1), data);
        data.reset();
        bc.clearPages(new PageRange().setStart(Constants.KB).setEnd(2 * Constants.KB - 1));
        bc.uploadPages(new PageRange().setStart(2 * Constants.KB).setEnd(3 * Constants.KB - 1), data);
        bc.clearPages(new PageRange().setStart(3 * Constants.KB).setEnd(4 * Constants.KB - 1));

        Iterator<PagedResponse<PageRangeItem>> iterator = bc.listPageRangesDiff(new ListPageRangesDiffOptions(
            new BlobRange(0, 4L * Constants.KB), snapshot).setMaxResultsPerPage(2), null, null)
            .iterableByPage().iterator();
        String token = iterator.next().getContinuationToken();

        iterator = bc.listPageRangesDiff(new ListPageRangesDiffOptions(new BlobRange(0, 4L * Constants.KB), snapshot),
            null, null).iterableByPage(token).iterator();
        PagedResponse<PageRangeItem> page = iterator.next();

        assertEquals(page.getValue().size(), 2);
        assertFalse(iterator.hasNext());
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20210608ServiceVersion")
    @Test
    public void listPagesDiffRange() {
        bc.create(4 * Constants.KB, true);
        ByteArrayInputStream data = new ByteArrayInputStream(getRandomByteArray(4 * Constants.KB));
        bc.uploadPages(new PageRange().setStart(0).setEnd(4 * Constants.KB - 1), data);
        String snapshot = bc.createSnapshot().getSnapshotId();
        data = new ByteArrayInputStream(getRandomByteArray(Constants.KB));
        data.mark(Integer.MAX_VALUE);
        bc.uploadPages(new PageRange().setStart(0).setEnd(Constants.KB - 1), data);
        data.reset();
        bc.clearPages(new PageRange().setStart(Constants.KB).setEnd(2 * Constants.KB - 1));
        bc.uploadPages(new PageRange().setStart(2 * Constants.KB).setEnd(3 * Constants.KB - 1), data);
        bc.clearPages(new PageRange().setStart(3 * Constants.KB).setEnd(4 * Constants.KB - 1));

        Iterator<PageRangeItem> iterator = bc.listPageRangesDiff(new ListPageRangesDiffOptions(
            new BlobRange(2 * Constants.KB + 1, 2L * Constants.KB), snapshot), null, null).iterator();

        int size = 0;
        while (iterator.hasNext()) {
            iterator.next();
            size++;
        }
        assertEquals(size, 2);
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20210608ServiceVersion")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void listPageRangesDiffAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID, String tags) {
        bc.create(4 * Constants.KB, true);
        ByteArrayInputStream data = new ByteArrayInputStream(getRandomByteArray(4 * Constants.KB));
        bc.uploadPages(new PageRange().setStart(0).setEnd(4 * Constants.KB - 1), data);
        String snapshot = bc.createSnapshot().getSnapshotId();

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

        assertDoesNotThrow(() -> bc.listPageRangesDiff(new ListPageRangesDiffOptions(
            new BlobRange(0, (long) PageBlobClient.PAGE_BYTES), snapshot).setRequestConditions(bac), null, null)
            .stream().count());
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20210608ServiceVersion")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsFailSupplier")
    public void listPageRangesDiffACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
        String noneMatch, String leaseID, String tags) {
        String snapshot = bc.createSnapshot().getSnapshotId();
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(setupBlobLeaseCondition(bc, leaseID))
            .setIfMatch(match)
            .setIfNoneMatch(setupBlobMatchCondition(bc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        assertThrows(BlobStorageException.class, () -> bc.listPageRangesDiff(new ListPageRangesDiffOptions(
            new BlobRange(0, (long) PageBlobClient.PAGE_BYTES), snapshot).setRequestConditions(bac), null, null)
            .stream().count());
    }

    @ParameterizedTest
    @MethodSource("pageRangeIASupplier")
    public void pageRangeIA(int start, int end) {
        PageRange range = new PageRange().setStart(start).setEnd(end);
        assertThrows(IllegalArgumentException.class, () -> bc.clearPages(range));
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
        Response<PageBlobItem> response = bc.resizeWithResponse(PageBlobClient.PAGE_BYTES * 2, null, null, null);

        assertEquals(PageBlobClient.PAGE_BYTES * 2, bc.getProperties().getBlobSize());
        assertTrue(validateBasicHeaders(response.getHeaders()));
        assertNotNull(response.getValue().getBlobSequenceNumber());
    }

    @Test
    public void resizeMin() {
        assertResponseStatusCode(bc.resizeWithResponse(PageBlobClient.PAGE_BYTES, null, null, null),  200);
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void resizeAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
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

        assertResponseStatusCode(bc.resizeWithResponse(PageBlobClient.PAGE_BYTES * 2, bac, null, null), 200);

    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsFailSupplier")
    public void resizeACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID, String tags) {
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(setupBlobLeaseCondition(bc, leaseID))
            .setIfMatch(match)
            .setIfNoneMatch(setupBlobMatchCondition(bc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        assertThrows(BlobStorageException.class, () ->
            bc.resizeWithResponse(PageBlobClient.PAGE_BYTES * 2, bac, null, null));
    }

    @Test
    public void resizeError() {
        bc = cc.getBlobClient(generateBlobName()).getPageBlobClient();
        assertThrows(BlobStorageException.class, () -> bc.resize(0));
    }

    @ParameterizedTest
    @MethodSource("sequenceNumberSupplier")
    public void sequenceNumber(SequenceNumberActionType action, Long number, Long result) {
        Response<PageBlobItem> response = bc.updateSequenceNumberWithResponse(action, number, null, null, null);

        assertEquals(result, bc.getProperties().getBlobSequenceNumber());
        assertTrue(validateBasicHeaders(response.getHeaders()));
        assertEquals(response.getValue().getBlobSequenceNumber(), result);
    }

    private static Stream<Arguments> sequenceNumberSupplier() {
        return Stream.of(
            Arguments.of(SequenceNumberActionType.UPDATE, 5L, 5L),
            Arguments.of(SequenceNumberActionType.INCREMENT, null, 1L),
            Arguments.of(SequenceNumberActionType.MAX, 2L, 2L));
    }

    @Test
    public void sequenceNumberMin() {
        assertResponseStatusCode(bc.updateSequenceNumberWithResponse(SequenceNumberActionType.INCREMENT,
            null, null, null, null), 200);
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void sequenceNumberAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
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

        assertResponseStatusCode(bc.updateSequenceNumberWithResponse(SequenceNumberActionType.UPDATE, 1L,
            bac, null, null), 200);
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsFailSupplier")
    public void sequenceNumberACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID, String tags) {
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(setupBlobLeaseCondition(bc, leaseID))
            .setIfMatch(match)
            .setIfNoneMatch(setupBlobMatchCondition(bc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        assertThrows(BlobStorageException.class, () ->
            bc.updateSequenceNumberWithResponse(SequenceNumberActionType.UPDATE, 1L, bac, null, null));
    }

    @Test
    public void sequenceNumberError() {
        bc = cc.getBlobClient(generateBlobName()).getPageBlobClient();
        assertThrows(BlobStorageException.class, () -> bc.updateSequenceNumber(SequenceNumberActionType.UPDATE, 0L));
    }

    @Test
    public void startIncrementalCopy() {
        cc.setAccessPolicy(PublicAccessType.BLOB, null);
        PageBlobClient bc2 = cc.getBlobClient(generateBlobName()).getPageBlobClient();
        String snapId = bc.createSnapshot().getSnapshotId();

        Response<CopyStatusType> copyResponse = bc2.copyIncrementalWithResponse(bc.getBlobUrl(), snapId, null, null,
            null);

        CopyStatusType status = copyResponse.getValue();
        OffsetDateTime start = testResourceNamer.now();
        while (status != CopyStatusType.SUCCESS) {
            status = bc2.getProperties().getCopyStatus();
            OffsetDateTime currentTime = testResourceNamer.now();
            if (status == CopyStatusType.FAILED || currentTime.minusMinutes(1) == start) {
                throw new RuntimeException("Copy failed or took too long");
            }
            sleepIfRunningAgainstService(1000);
        }

        BlobProperties properties = bc2.getProperties();
        assertTrue(properties.isIncrementalCopy());
        assertNotNull(properties.getCopyDestinationSnapshot());
        validateBasicHeaders(copyResponse.getHeaders());
        assertNotNull(copyResponse.getHeaders().getValue(X_MS_COPY_ID));
        assertNotNull(copyResponse.getValue());
    }

    @Test
    public void startIncrementalCopyMin() {
        cc.setAccessPolicy(PublicAccessType.BLOB, null);
        PageBlobClient bc2 = cc.getBlobClient(generateBlobName()).getPageBlobClient();
        String snapshot = bc.createSnapshot().getSnapshotId();

        assertResponseStatusCode(bc2.copyIncrementalWithResponse(bc.getBlobUrl(), snapshot, null, null, null), 202);
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("startIncrementalCopyACSupplier")
    public void startIncrementalCopyAC(OffsetDateTime modified, OffsetDateTime unmodified, String match,
        String noneMatch, String tags) {
        cc.setAccessPolicy(PublicAccessType.BLOB, null);
        PageBlobClient bu2 = cc.getBlobClient(generateBlobName()).getPageBlobClient();
        String snapshot = bc.createSnapshot().getSnapshotId();
        Response<CopyStatusType> copyResponse = bu2.copyIncrementalWithResponse(bc.getBlobUrl(), snapshot, null, null,
            null);

        CopyStatusType status = copyResponse.getValue();
        OffsetDateTime start = testResourceNamer.now();
        while (status != CopyStatusType.SUCCESS) {
            status = bu2.getProperties().getCopyStatus();
            OffsetDateTime currentTime = testResourceNamer.now();
            if (status == CopyStatusType.FAILED || currentTime.minusMinutes(1) == start) {
                throw new RuntimeException("Copy failed or took too long");
            }
            sleepIfRunningAgainstService(1000);
        }
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bu2.setTags(t);

        snapshot = bc.createSnapshot().getSnapshotId();
        match = setupBlobMatchCondition(bu2, match);
        PageBlobCopyIncrementalRequestConditions mac = new PageBlobCopyIncrementalRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setTagsConditions(tags);

        assertResponseStatusCode(bu2.copyIncrementalWithResponse(new PageBlobCopyIncrementalOptions(bc.getBlobUrl(),
            snapshot).setRequestConditions(mac), null, null),  202);
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
        cc.setAccessPolicy(PublicAccessType.BLOB, null);
        PageBlobClient bu2 = cc.getBlobClient(generateBlobName()).getPageBlobClient();
        String snapshot = bc.createSnapshot().getSnapshotId();
        bu2.copyIncremental(bc.getBlobUrl(), snapshot);
        String finalSnapshot = bc.createSnapshot().getSnapshotId();
        noneMatch = setupBlobMatchCondition(bu2, noneMatch);
        PageBlobCopyIncrementalRequestConditions mac = new PageBlobCopyIncrementalRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setTagsConditions(tags);

        assertThrows(BlobStorageException.class, () -> bu2.copyIncrementalWithResponse(
            new PageBlobCopyIncrementalOptions(bc.getBlobUrl(), finalSnapshot).setRequestConditions(mac), null, null));
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
        bc = cc.getBlobClient(generateBlobName()).getPageBlobClient();
        assertThrows(BlobStorageException.class, () -> bc.copyIncremental("https://www.error.com", "snapshot"));
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
        BlobClient client = cc.getBlobClient(originalBlobName);
        PageBlobClient blockClient = cc.getBlobClient(client.getBlobName()).getPageBlobClient();
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
    public void createOverwriteFalse() {
        assertThrows(BlobStorageException.class, () -> bc.create(512));
    }

    @Test
    public void createOverwriteTrue() {
        assertDoesNotThrow(() -> bc.create(512, true));
    }

    @Test
    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials
    // and auth would fail because we changed a signed header.
    public void perCallPolicy() {
        PageBlobClient specialBlob = getSpecializedBuilder(bc.getBlobUrl())
            .addPolicy(getPerCallVersionPolicy())
            .buildPageBlobClient();

        Response<BlobProperties> response = specialBlob.getPropertiesWithResponse(null, null, null);

        assertEquals(response.getHeaders().getValue(X_MS_VERSION), "2017-11-09");
    }

    @Test
    public void defaultAudience() {
        PageBlobClient aadBlob = getSpecializedBuilderWithTokenCredential(bc.getBlobUrl())
            .audience(null)
            .buildPageBlobClient();

        assertTrue(aadBlob.exists());
    }

    @Test
    public void storageAccountAudience() {
        PageBlobClient aadBlob = getSpecializedBuilderWithTokenCredential(bc.getBlobUrl())
            .audience(BlobAudience.createBlobServiceAccountAudience(cc.getAccountName()))
            .buildPageBlobClient();

        assertTrue(aadBlob.exists());
    }

    @Test
    public void audienceError() {
        PageBlobClient aadBlob = instrument(new SpecializedBlobClientBuilder()
            .endpoint(bc.getBlobUrl())
            .credential(new MockTokenCredential())
            .audience(BlobAudience.createBlobServiceAccountAudience("badAudience")))
            .buildPageBlobClient();

        BlobStorageException e = assertThrows(BlobStorageException.class, () -> aadBlob.exists());
        assertTrue(e.getErrorCode() == BlobErrorCode.INVALID_AUTHENTICATION_INFO);
    }

    @Test
    public void audienceFromString() {
        String url = String.format("https://%s.blob.core.windows.net/", cc.getAccountName());
        BlobAudience audience = BlobAudience.fromString(url);

        PageBlobClient aadBlob = getSpecializedBuilderWithTokenCredential(bc.getBlobUrl())
            .audience(audience)
            .buildPageBlobClient();

        assertTrue(aadBlob.exists());
    }
}
