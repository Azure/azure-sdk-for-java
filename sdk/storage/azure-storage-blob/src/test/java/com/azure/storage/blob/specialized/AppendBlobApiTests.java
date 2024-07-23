// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.exception.UnexpectedLengthException;
import com.azure.core.http.rest.Response;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.Context;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.blob.models.AppendBlobItem;
import com.azure.storage.blob.models.AppendBlobRequestConditions;
import com.azure.storage.blob.models.BlobAudience;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.options.AppendBlobCreateOptions;
import com.azure.storage.blob.options.AppendBlobSealOptions;
import com.azure.storage.blob.options.BlobGetTagsOptions;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import com.azure.storage.common.test.shared.policy.TransientFailureInjectingHttpPipelinePolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AppendBlobApiTests extends BlobTestBase {

    private AppendBlobClient bc;
    private String blobName;

    @BeforeEach
    public void setup() {
        blobName = generateBlobName();
        bc = cc.getBlobClient(blobName).getAppendBlobClient();
        bc.create();
    }

    @Test
    public void createDefaults() {
        Response<AppendBlobItem> response = bc.createWithResponse(null, null, null, null, null);
        assertEquals(201, response.getStatusCode());
        validateBasicHeaders(response.getHeaders());
        assertNull(response.getValue().getContentMd5());
        assertTrue(response.getValue().isServerEncrypted());
    }

    @Test
    public void createMin() {
        assertResponseStatusCode(bc.createWithResponse(null, null, null, null, null), 201);
    }

    @Test
    public void createError() {
        assertThrows(BlobStorageException.class, () -> bc.createWithResponse(null, null, new BlobRequestConditions()
            .setIfMatch("garbage"), null, Context.NONE));
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

        bc.createWithResponse(headers, null, null, null, null);
        Response<BlobProperties> response = bc.getPropertiesWithResponse(null, null, null);

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType;

        validateBlobProperties(response, cacheControl, contentDisposition, contentEncoding, contentLanguage, contentMD5,
            contentType);
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

        bc.createWithResponse(null, metadata, null, null, Context.NONE);

        BlobProperties response = bc.getProperties();
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            assertEquals(entry.getValue(), response.getMetadata().get(entry.getKey()));
        }
        assertEquals(metadata, response.getMetadata());
    }

    private static Stream<Arguments> createMetadataSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null),
            Arguments.of("foo", "bar", "fizz", "buzz"));
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
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

        bc.createWithResponse(new AppendBlobCreateOptions().setTags(tags), null, Context.NONE);

        Response<Map<String, String>> response = bc.getTagsWithResponse(new BlobGetTagsOptions(), null, null);
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            assertEquals(entry.getValue(), response.getValue().get(entry.getKey()));
        }
    }

    private static Stream<Arguments> createTagsSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null),
            Arguments.of("foo", "bar", "fizz", "buzz"));
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("createACSupplier")
    public void createAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
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

        assertResponseStatusCode(bc.createWithResponse(null, null, bac, null, null), 201);
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

        assertThrows(BlobStorageException.class, () -> bc.createWithResponse(null, null, bac, null, Context.NONE));
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
        bc = cc.getBlobClient(blobName).getAppendBlobClient();

        Response<AppendBlobItem> createResponse = bc.createIfNotExistsWithResponse(new AppendBlobCreateOptions(), null,
            null);

        assertResponseStatusCode(createResponse, 201);
        validateBasicHeaders(createResponse.getHeaders());
        assertNull(createResponse.getValue().getContentMd5());
        assertTrue(createResponse.getValue().isServerEncrypted());
    }

    @Test
    public void createIfNotExistsMin() {
        String blobName = cc.getBlobClient(generateBlobName()).getBlobName();
        bc = cc.getBlobClient(blobName).getAppendBlobClient();

        assertResponseStatusCode(bc.createIfNotExistsWithResponse(null, null, null), 201);
    }

    @Test
    public void createIfNotExistsOnABlobThatAlreadyExists() {
        String blobName = cc.getBlobClient(generateBlobName()).getBlobName();
        bc = cc.getBlobClient(blobName).getAppendBlobClient();
        Response<AppendBlobItem> initialResponse =
            bc.createIfNotExistsWithResponse(new AppendBlobCreateOptions(), null, null);
        Response<AppendBlobItem> secondResponse =
            bc.createIfNotExistsWithResponse(new AppendBlobCreateOptions(), null, null);

        assertResponseStatusCode(initialResponse, 201);
        assertResponseStatusCode(secondResponse, 409);
    }

    @ParameterizedTest
    @MethodSource("createHeadersSupplier")
    public void createIfNotExistsHeaders(String cacheControl, String contentDisposition, String contentEncoding,
        String contentLanguage, byte[] contentMD5, String contentType) {
        String blobName = cc.getBlobClient(generateBlobName()).getBlobName();
        bc = cc.getBlobClient(blobName).getAppendBlobClient();

        BlobHttpHeaders headers = new BlobHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType);
        AppendBlobCreateOptions options = new AppendBlobCreateOptions().setHeaders(headers);

        bc.createIfNotExistsWithResponse(options, null, null);
        Response<BlobProperties> response = bc.getPropertiesWithResponse(null, null, null);

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType;

        validateBlobProperties(response, cacheControl, contentDisposition, contentEncoding, contentLanguage, contentMD5,
            contentType);
    }

    @ParameterizedTest
    @MethodSource("createMetadataSupplier")
    public void createIfNotExistsMetadata(String key1, String value1, String key2, String value2) {
        String blobName = cc.getBlobClient(generateBlobName()).getBlobName();
        bc = cc.getBlobClient(blobName).getAppendBlobClient();

        Map<String, String> metadata = new HashMap<>();
        if (key1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null) {
            metadata.put(key2, value2);
        }

        AppendBlobCreateOptions options = new AppendBlobCreateOptions().setMetadata(metadata);

        bc.createIfNotExistsWithResponse(options, null, Context.NONE);
        BlobProperties response = bc.getProperties();

        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            assertEquals(entry.getValue(), response.getMetadata().get(entry.getKey()));
        }
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("createIfNotExistsTagsSupplier")
    public void createIfNotExistsTags(String key1, String value1, String key2, String value2) {
        bc.delete();
        Map<String, String> tags = new HashMap<>();
        if (key1 != null) {
            tags.put(key1, value1);
        }
        if (key2 != null) {
            tags.put(key2, value2);
        }

        bc.createIfNotExistsWithResponse(new AppendBlobCreateOptions().setTags(tags), null, Context.NONE);
        Response<Map<String, String>> response = bc.getTagsWithResponse(new BlobGetTagsOptions(), null, null);
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            assertEquals(entry.getValue(), response.getValue().get(entry.getKey()));
        }
    }

    private Stream<Arguments> createIfNotExistsTagsSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null),
            Arguments.of("foo", "bar", "fizz", "buzz"),
            Arguments.of(" +-./:=_  +-./:=_", " +-./:=_", null, null));
    }

    @Test
    public void appendBlockDefaults() {
        Response<AppendBlobItem> appendResponse = bc.appendBlockWithResponse(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize(), null, null, null, null);

        ByteArrayOutputStream downloadStream = new ByteArrayOutputStream();
        bc.downloadStream(downloadStream);
        TestUtils.assertArraysEqual(DATA.getDefaultBytes(), downloadStream.toByteArray());

        validateBasicHeaders(appendResponse.getHeaders());
        assertNotNull(appendResponse.getHeaders().getValue(X_MS_CONTENT_CRC64));
        assertNotNull(appendResponse.getValue().getBlobAppendOffset());
        assertNotNull(appendResponse.getValue().getBlobCommittedBlockCount());
        assertEquals(1, bc.getProperties().getCommittedBlockCount());
    }

    @Test
    public void appendBlockMin() {
        assertResponseStatusCode(bc.appendBlockWithResponse(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize(), null, null, null, null), 201);
    }

    @ParameterizedTest
    @MethodSource("appendBlockIASupplier")
    public void appendBlockIA(InputStream stream, long dataSize, Class<? extends Throwable> exceptionType) {
        assertThrows(exceptionType, () -> bc.appendBlock(stream, dataSize));
    }

    private static Stream<Arguments> appendBlockIASupplier() {
        return Stream.of(
            Arguments.of(null, DATA.getDefaultDataSize(), NullPointerException.class),
            Arguments.of(DATA.getDefaultInputStream(), DATA.getDefaultDataSize() + 1, UnexpectedLengthException.class),
            Arguments.of(DATA.getDefaultInputStream(), DATA.getDefaultDataSize() - 1, UnexpectedLengthException.class)
        );
    }

    @Test
    public void appendBlockEmptyBody() {
        assertThrows(BlobStorageException.class, () -> bc.appendBlock(new ByteArrayInputStream(new byte[0]), 0));
    }

    @Test
    public void appendBlockNullBody() {
        assertThrows(NullPointerException.class, () -> bc.appendBlock(new ByteArrayInputStream(null), 0));
    }

    @Test
    public void appendBlockTransactionalMD5() throws NoSuchAlgorithmException {
        byte[] md5 = MessageDigest.getInstance("MD5").digest(DATA.getDefaultBytes());

        assertResponseStatusCode(bc.appendBlockWithResponse(DATA.getDefaultInputStream(), DATA.getDefaultDataSize(),
            md5, null, null, null), 201);
    }

    @Test
    public void appendBlockTransactionalMD5Fail() {
        BlobStorageException e = assertThrows(BlobStorageException.class,
            () -> bc.appendBlockWithResponse(DATA.getDefaultInputStream(), DATA.getDefaultDataSize(),
            MessageDigest.getInstance("MD5").digest("garbage".getBytes()), null, null, null));

        assertExceptionStatusCodeAndMessage(e, 400, BlobErrorCode.MD5MISMATCH);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("appendBlockSupplier")
    public void appendBlockAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID, Long appendPosE, Long maxSizeLTE, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t);
        match = setupBlobMatchCondition(bc, match);
        leaseID = setupBlobLeaseCondition(bc, leaseID);
        AppendBlobRequestConditions bac = new AppendBlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setAppendPosition(appendPosE)
            .setMaxSize(maxSizeLTE)
            .setTagsConditions(tags);

        assertResponseStatusCode(bc.appendBlockWithResponse(DATA.getDefaultInputStream(), DATA.getDefaultDataSize(),
            null, bac, null, null), 201);
    }

    private static Stream<Arguments> appendBlockSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null, null, null, null, null),
            Arguments.of(OLD_DATE, null, null, null, null, null, null, null),
            Arguments.of(null, NEW_DATE, null, null, null, null, null, null),
            Arguments.of(null, null, RECEIVED_ETAG, null, null, null, null, null),
            Arguments.of(null, null, null, GARBAGE_ETAG, null, null, null, null),
            Arguments.of(null, null, null, null, RECEIVED_LEASE_ID, null, null, null),
            Arguments.of(null, null, null, null, null, 0L, null, null),
            Arguments.of(null, null, null, null, null, null, 100L, null),
            Arguments.of(null, null, null, null, null, null, null, "\"foo\" = 'bar'")
        );
    }

    @ParameterizedTest
    @MethodSource("appendBlockFailSupplier")
    public void appendBlockACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID, Long appendPosE, Long maxSizeLTE, String tags) throws IOException {
        noneMatch = setupBlobMatchCondition(bc, noneMatch);
        setupBlobLeaseCondition(bc, leaseID);

        AppendBlobRequestConditions bac = new AppendBlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setAppendPosition(appendPosE)
            .setMaxSize(maxSizeLTE)
            .setTagsConditions(tags);

        assertThrows(BlobStorageException.class, () ->
            bc.appendBlockWithResponse(DATA.getDefaultInputStream(), DATA.getDefaultDataSize(), null, bac, null, null));

        DATA.getDefaultInputStream().reset();
    }

    private static Stream<Arguments> appendBlockFailSupplier() {
        return Stream.of(
            Arguments.of(NEW_DATE, null, null, null, null, null, null, null),
            Arguments.of(null, OLD_DATE, null, null, null, null, null, null),
            Arguments.of(null, null, GARBAGE_ETAG, null, null, null, null, null),
            Arguments.of(null, null, null, RECEIVED_ETAG, null, null, null, null),
            Arguments.of(null, null, null, null, GARBAGE_LEASE_ID, null, null, null),
            Arguments.of(null, null, null, null, null, 1L, null, null),
            Arguments.of(null, null, null, null, null, null, 1L, null),
            Arguments.of(null, null, null, null, null, null, null, "\"notfoo\" = 'notbar'")
        );
    }

    @Test
    public void appendBlockError() {
        bc = cc.getBlobClient(generateBlobName()).getAppendBlobClient();
        assertThrows(BlobStorageException.class, () ->
            bc.appendBlock(DATA.getDefaultInputStream(), DATA.getDefaultDataSize()));
    }

    @Test
    public void appendBlockRetryOnTransientFailure() {
        AppendBlobClient clientWithFailure = getBlobClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            bc.getBlobUrl(), new TransientFailureInjectingHttpPipelinePolicy()).getAppendBlobClient();

        clientWithFailure.appendBlock(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bc.downloadStream(os);
        TestUtils.assertArraysEqual(DATA.getDefaultBytes(), os.toByteArray());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2022-11-02")
    @Test
    public void appendBlockHighThroughput() {
        int size = 5 * Constants.MB;
        byte[] randomData = getRandomByteArray(size); // testing upload of size greater than 4MB
        ByteArrayInputStream uploadStream = new ByteArrayInputStream(randomData);
        ByteArrayOutputStream downloadStream = new ByteArrayOutputStream(size);

        assertResponseStatusCode(bc.appendBlockWithResponse(uploadStream, size, null, null, null, null), 201);

        bc.downloadStream(downloadStream); // Check if block was appended correctly by downloading the block
        TestUtils.assertArraysEqual(randomData, downloadStream.toByteArray());
    }

    @Test
    public void appendBlockFromURLMin() {
        byte[] data = getRandomByteArray(1024);
        bc.appendBlock(new ByteArrayInputStream(data), data.length);

        AppendBlobClient destURL = cc.getBlobClient(generateBlobName()).getAppendBlobClient();
        destURL.create();

        BlobRange blobRange = new BlobRange(0, (long) PageBlobClient.PAGE_BYTES);

        String sas = bc.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));
        Response<AppendBlobItem> response = destURL.appendBlockFromUrlWithResponse(bc.getBlobUrl() + "?" + sas, blobRange, null,
            null, null, null, null);

        assertResponseStatusCode(response, 201);
        validateBasicHeaders(response.getHeaders());
    }

    /*@RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2024-08-04")
    @Test
    public void appendBlockFromURLSourceErrorAndStatusCodeNewTest() {
        AppendBlobClient destBlob = cc.getBlobClient(generateBlobName()).getAppendBlobClient();
        destBlob.createIfNotExists();

        BlobStorageException e = assertThrows(BlobStorageException.class, () -> destBlob.appendBlockFromUrl(bc.getBlobUrl(), new BlobRange(0, (long) PageBlobClient.PAGE_BYTES)));

        assertTrue(e.getStatusCode() == 409);
        assertTrue(e.getServiceMessage().contains("PublicAccessNotPermitted"));
        assertTrue(e.getServiceMessage().contains("Public access is not permitted on this storage account."));
    }*/

    @Test
    public void appendBlockFromURLRange() {
        byte[] data = getRandomByteArray(4 * 1024);
        bc.appendBlock(new ByteArrayInputStream(data), data.length);

        AppendBlobClient destURL = cc.getBlobClient(generateBlobName()).getAppendBlobClient();
        destURL.create();

        String sas = bc.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));
        destURL.appendBlockFromUrl(bc.getBlobUrl() + "?" + sas, new BlobRange(2 * 1024, 1024L));

        ByteArrayOutputStream downloadStream = new ByteArrayOutputStream(1024);
        destURL.downloadStream(downloadStream);
        TestUtils.assertArraysEqual(data, 2 * 1024, downloadStream.toByteArray(), 0, 1024);
    }

    @Test
    public void appendBlockFromURLMD5() {
        byte[] data = getRandomByteArray(1024);
        bc.appendBlock(new ByteArrayInputStream(data), data.length);

        AppendBlobClient destURL = cc.getBlobClient(generateBlobName()).getAppendBlobClient();
        destURL.create();

        String sas = bc.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));
        assertDoesNotThrow(() -> destURL.appendBlockFromUrlWithResponse(bc.getBlobUrl() + "?" + sas, null,
            MessageDigest.getInstance("MD5").digest(data), null, null, null, Context.NONE));

    }

    @Test
    public void appendBlockFromURLMD5Fail() {
        byte[] data = getRandomByteArray(1024);
        bc.appendBlock(new ByteArrayInputStream(data), data.length);

        AppendBlobClient destURL = cc.getBlobClient(generateBlobName()).getAppendBlobClient();
        destURL.create();

        String sas = bc.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));
        assertThrows(BlobStorageException.class, () -> destURL.appendBlockFromUrlWithResponse(bc.getBlobUrl() + "?" + sas, null,
            MessageDigest.getInstance("MD5").digest("garbage".getBytes()), null, null, null, Context.NONE));
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("appendBlockSupplier")
    public void appendBlockFromURLDestinationAC(OffsetDateTime modified, OffsetDateTime unmodified, String match,
                                                String noneMatch, String leaseID, Long appendPosE, Long maxSizeLTE, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t);
        match = setupBlobMatchCondition(bc, match);
        leaseID = setupBlobLeaseCondition(bc, leaseID);
        AppendBlobRequestConditions bac = new AppendBlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setAppendPosition(appendPosE)
            .setMaxSize(maxSizeLTE)
            .setTagsConditions(tags);

        AppendBlobClient sourceURL = cc.getBlobClient(generateBlobName()).getAppendBlobClient();
        sourceURL.create();
        sourceURL.appendBlockWithResponse(DATA.getDefaultInputStream(), DATA.getDefaultDataSize(), null, null, null,
            null).getStatusCode();

        String sas = sourceURL.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));
        assertResponseStatusCode(bc.appendBlockFromUrlWithResponse(sourceURL.getBlobUrl() + "?" + sas, null, null, bac, null, null,
            null), 201);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("appendBlockFailSupplier")
    public void appendBlockFromURLDestinationACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
                                                    String noneMatch, String leaseID, Long maxSizeLTE, Long appendPosE, String tags) {
        noneMatch = setupBlobMatchCondition(bc, noneMatch);
        setupBlobLeaseCondition(bc, leaseID);

        AppendBlobRequestConditions bac = new AppendBlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setAppendPosition(appendPosE)
            .setMaxSize(maxSizeLTE)
            .setTagsConditions(tags);

        AppendBlobClient sourceURL = cc.getBlobClient(generateBlobName()).getAppendBlobClient();
        sourceURL.create();
        sourceURL.appendBlockWithResponse(DATA.getDefaultInputStream(), DATA.getDefaultDataSize(), null, null, null,
            null);

        String sas = sourceURL.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));
        assertThrows(BlobStorageException.class, () -> bc.appendBlockFromUrlWithResponse(sourceURL.getBlobUrl() + "?" + sas, null,
            null, bac, null, null, Context.NONE));
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("appendBlockFromURLSupplier")
    public void appendBlockFromURLSourceAC(OffsetDateTime sourceIfModifiedSince, OffsetDateTime sourceIfUnmodifiedSince,
                                           String sourceIfMatch, String sourceIfNoneMatch) {
        AppendBlobClient sourceURL = cc.getBlobClient(generateBlobName()).getAppendBlobClient();
        sourceURL.create();
        sourceURL.appendBlockWithResponse(DATA.getDefaultInputStream(), DATA.getDefaultDataSize(), null, null, null,
            null);

        BlobRequestConditions smac = new BlobRequestConditions()
            .setIfModifiedSince(sourceIfModifiedSince)
            .setIfUnmodifiedSince(sourceIfUnmodifiedSince)
            .setIfMatch(setupBlobMatchCondition(sourceURL, sourceIfMatch))
            .setIfNoneMatch(sourceIfNoneMatch);

        String sas = sourceURL.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));
        assertResponseStatusCode(bc.appendBlockFromUrlWithResponse(sourceURL.getBlobUrl() + "?" + sas, null, null, null, smac, null,
            null), 201);
    }

    private static Stream<Arguments> appendBlockFromURLSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null),
            Arguments.of(OLD_DATE, null, null, null),
            Arguments.of(null, NEW_DATE, null, null),
            Arguments.of(null, null, RECEIVED_ETAG, null),
            Arguments.of(null, null, null, GARBAGE_ETAG)
        );
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("appendBlockFromURLFailSupplier")
    public void appendBlockFromURLSourceACFail(OffsetDateTime sourceIfModifiedSince,
                                               OffsetDateTime sourceIfUnmodifiedSince, String sourceIfMatch, String sourceIfNoneMatch) {
        AppendBlobClient sourceURL = cc.getBlobClient(generateBlobName()).getAppendBlobClient();
        sourceURL.create();
        sourceURL.appendBlockWithResponse(DATA.getDefaultInputStream(), DATA.getDefaultDataSize(), null, null, null,
            null);

        BlobRequestConditions smac = new BlobRequestConditions()
            .setIfModifiedSince(sourceIfModifiedSince)
            .setIfUnmodifiedSince(sourceIfUnmodifiedSince)
            .setIfMatch(sourceIfMatch)
            .setIfNoneMatch(setupBlobMatchCondition(sourceURL, sourceIfNoneMatch));

        String sas = sourceURL.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));
        assertThrows(BlobStorageException.class, () -> bc.appendBlockFromUrlWithResponse(sourceURL.getBlobUrl() + "?" + sas, null,
            null, null, smac, null, Context.NONE));
    }

    private static Stream<Arguments> appendBlockFromURLFailSupplier() {
        return Stream.of(
            Arguments.of(NEW_DATE, null, null, null),
            Arguments.of(null, OLD_DATE, null, null),
            Arguments.of(null, null, GARBAGE_ETAG, null),
            Arguments.of(null, null, null, RECEIVED_ETAG)
        );
    }

    @Test
    public void getContainerName() {
        assertEquals(containerName, bc.getContainerName());
    }

    @Test
    public void getAppendBlobName() {
        assertEquals(blobName, bc.getBlobName());
    }

    @Test
    public void createOverwriteFalse() {
        assertThrows(BlobStorageException.class, () -> bc.create());
    }

    @Test
    public void createOverwriteTrue() {
        assertDoesNotThrow(() -> bc.create(true));

    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void sealDefaults() {
        Response<Void> sealResponse = bc.sealWithResponse(null, null, null);
        assertResponseStatusCode(sealResponse, 200);
        assertEquals("true", sealResponse.getHeaders().getValue("x-ms-blob-sealed"));
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void sealMin() {
        bc.seal();

        assertTrue(bc.getProperties().isSealed());
        assertTrue(bc.downloadStreamWithResponse(new ByteArrayOutputStream(), null, null, null, false, null, null)
            .getDeserializedHeaders().isSealed());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void sealError() {
        bc = cc.getBlobClient(generateBlobName()).getAppendBlobClient();
        assertThrows(BlobStorageException.class, () -> bc.seal());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("sealACSupplier")
    public void sealAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID, Long appendPosE) {
        match = setupBlobMatchCondition(bc, match);
        leaseID = setupBlobLeaseCondition(bc, leaseID);
        AppendBlobRequestConditions bac = new AppendBlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setAppendPosition(appendPosE);

        assertResponseStatusCode(bc.sealWithResponse(new AppendBlobSealOptions().setRequestConditions(bac), null, null),
            200);

    }

    private static Stream<Arguments> sealACSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null, null, null),
            Arguments.of(OLD_DATE, null, null, null, null, null),
            Arguments.of(null, NEW_DATE, null, null, null, null),
            Arguments.of(null, null, RECEIVED_ETAG, null, null, null),
            Arguments.of(null, null, null, GARBAGE_ETAG, null, null),
            Arguments.of(null, null, null, null, RECEIVED_LEASE_ID, null),
            Arguments.of(null, null, null, null, null, 0L)
        );
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("sealACFailSupplier")
    public void sealACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID, Long appendPosE) {
        noneMatch = setupBlobMatchCondition(bc, noneMatch);
        setupBlobLeaseCondition(bc, leaseID);

        AppendBlobRequestConditions bac = new AppendBlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setAppendPosition(appendPosE);

        assertThrows(BlobStorageException.class, () ->
            bc.sealWithResponse(new AppendBlobSealOptions().setRequestConditions(bac), null, null));
    }

    private static Stream<Arguments> sealACFailSupplier() {
        return Stream.of(
            Arguments.of(NEW_DATE, null, null, null, null, null),
            Arguments.of(null, OLD_DATE, null, null, null, null),
            Arguments.of(null, null, GARBAGE_ETAG, null, null, null),
            Arguments.of(null, null, null, RECEIVED_ETAG, null, null),
            Arguments.of(null, null, null, null, GARBAGE_LEASE_ID, null),
            Arguments.of(null, null, null, null, null, 1L)
        );
    }

    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials
    // and auth would fail because we changed a signed header.
    @Test
    public void perCallPolicy() {
        AppendBlobClient specialBlob = getSpecializedBuilder(bc.getBlobUrl())
            .addPolicy(getPerCallVersionPolicy())
            .buildAppendBlobClient();

        Response<BlobProperties> response = specialBlob.getPropertiesWithResponse(null, null, null);
        assertEquals("2017-11-09", response.getHeaders().getValue(X_MS_VERSION));
    }

    @Test
    public void defaultAudience() {
        AppendBlobClient aadBlob = getSpecializedBuilderWithTokenCredential(bc.getBlobUrl())
            .audience(null)
            .buildAppendBlobClient();

        assertTrue(aadBlob.exists());
    }

    @Test
    public void storageAccountAudience() {
        AppendBlobClient aadBlob = getSpecializedBuilderWithTokenCredential(bc.getBlobUrl())
            .audience(BlobAudience.createBlobServiceAccountAudience(cc.getAccountName()))
            .buildAppendBlobClient();

        assertTrue(aadBlob.exists());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2024-08-04")
    @LiveOnly
    @Test
    /* This test tests if the bearer challenge is working properly. A bad audience is passed in, the service returns
    the default audience, and the request gets retried with this default audience, making the call function as expected.
     */
    public void audienceErrorBearerChallengeRetry() {
        AppendBlobClient aadBlob = getSpecializedBuilderWithTokenCredential(bc.getBlobUrl())
            .audience(BlobAudience.createBlobServiceAccountAudience("badAudience"))
            .buildAppendBlobClient();

        assertNotNull(aadBlob.getProperties());
    }

    @Test
    public void audienceFromString() {
        String url = String.format("https://%s.blob.core.windows.net/", cc.getAccountName());
        BlobAudience audience = BlobAudience.fromString(url);

        AppendBlobClient aadBlob = getSpecializedBuilderWithTokenCredential(bc.getBlobUrl())
            .audience(audience)
            .buildAppendBlobClient();

        assertTrue(aadBlob.exists());
    }
}
