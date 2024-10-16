// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.exception.UnexpectedLengthException;
import com.azure.core.http.rest.Response;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.FluxUtil;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.blob.models.AppendBlobItem;
import com.azure.storage.blob.models.AppendBlobRequestConditions;
import com.azure.storage.blob.models.BlobAudience;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.options.AppendBlobCreateOptions;
import com.azure.storage.blob.options.AppendBlobSealOptions;
import com.azure.storage.blob.options.BlobGetTagsOptions;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import com.azure.storage.common.test.shared.policy.TransientFailureInjectingHttpPipelinePolicy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

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

        StepVerifier.create(bc.createWithResponse(headers, null, null)
            .then(bc.getPropertiesWithResponse(null)))
            .assertNext(p -> validateBlobProperties(p, cacheControl, contentDisposition, contentEncoding,
                contentLanguage, contentMD5, finalContentType))
            .verifyComplete();
    }

    private static Stream<Arguments> createHeadersSupplier() throws NoSuchAlgorithmException {
        return Stream.of(
            Arguments.of(null, null, null, null, null, null),
            Arguments.of("control", "disposition", "encoding", "language",
                Base64.getEncoder().encode(MessageDigest.getInstance("MD5")
                    .digest(DATA.getDefaultText().getBytes())),
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

        StepVerifier.create(bc.createWithResponse(null, metadata, null)
            .then(bc.getProperties()))
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

        StepVerifier.create(bc.createWithResponse(new AppendBlobCreateOptions().setTags(tags))
            .then(bc.getTagsWithResponse(new BlobGetTagsOptions())))
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

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("createACSupplier")
    public void createAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                         String leaseID, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");

        Mono<Response<AppendBlobItem>> response = bc.setTags(t)
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
                return bc.createWithResponse(null, null, bac);
            });

        assertAsyncResponseStatusCode(response, 201);
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
        Mono<Response<AppendBlobItem>> response = Mono.zip(setupBlobLeaseCondition(bc, leaseID),
            setupBlobMatchCondition(bc, noneMatch))
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
                return bc.createWithResponse(null, null, bac);
            });

        StepVerifier.create(response)
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

        assertAsyncResponseStatusCode(bc.createIfNotExistsWithResponse(new AppendBlobCreateOptions()),
            201);
        assertAsyncResponseStatusCode(bc.createIfNotExistsWithResponse(new AppendBlobCreateOptions()),
            409);
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

        StepVerifier.create(bc.createIfNotExistsWithResponse(options)
            .then(bc.getPropertiesWithResponse(null)))
            .assertNext(p -> validateBlobProperties(p, cacheControl, contentDisposition, contentEncoding,
                contentLanguage, contentMD5, finalContentType))
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

        StepVerifier.create(bc.createIfNotExistsWithResponse(options).then(bc.getProperties()))
            .assertNext(p -> {
                for (Map.Entry<String, String> entry : metadata.entrySet()) {
                    assertEquals(entry.getValue(), p.getMetadata().get(entry.getKey()));
                }
            })
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("createIfNotExistsTagsSupplier")
    public void createIfNotExistsTags(String key1, String value1, String key2, String value2) {
        Map<String, String> tags = new HashMap<>();
        if (key1 != null) {
            tags.put(key1, value1);
        }
        if (key2 != null) {
            tags.put(key2, value2);
        }

        Mono<Response<Map<String, String>>> response = bc.delete()
            .then(bc.createIfNotExistsWithResponse(new AppendBlobCreateOptions().setTags(tags)))
            .then(bc.getTagsWithResponse(new BlobGetTagsOptions()));

        StepVerifier.create(response)
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

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("appendBlockSupplier")
    public void appendBlockAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                              String leaseID, Long appendPosE, Long maxSizeLTE, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");

        Mono<Response<AppendBlobItem>> response = bc.setTags(t)
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
                AppendBlobRequestConditions bac = new AppendBlobRequestConditions()
                    .setLeaseId(newLease)
                    .setIfMatch(newMatch)
                    .setIfNoneMatch(noneMatch)
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified)
                    .setAppendPosition(appendPosE)
                    .setMaxSize(maxSizeLTE)
                    .setTagsConditions(tags);
                return bc.appendBlockWithResponse(DATA.getDefaultFlux(), DATA.getDefaultDataSize(), null, bac);
            });

        assertAsyncResponseStatusCode(response, 201);
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
                                  String leaseID, Long appendPosE, Long maxSizeLTE, String tags) {
        Mono<Response<AppendBlobItem>> response = Mono.zip(setupBlobLeaseCondition(bc, leaseID),
            setupBlobMatchCondition(bc, noneMatch))
            .flatMap(tuple -> {
                String newNoneMatch = tuple.getT2();
                if ("null".equals(newNoneMatch)) {
                    newNoneMatch = null;
                }
                AppendBlobRequestConditions bac = new AppendBlobRequestConditions()
                    .setLeaseId(leaseID)
                    .setIfMatch(match)
                    .setIfNoneMatch(newNoneMatch)
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified)
                    .setAppendPosition(appendPosE)
                    .setMaxSize(maxSizeLTE)
                    .setTagsConditions(tags);
                return bc.appendBlockWithResponse(DATA.getDefaultFlux(), DATA.getDefaultDataSize(), null, bac);
            });

        StepVerifier.create(response)
            .verifyError(BlobStorageException.class);
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
        bc = ccAsync.getBlobAsyncClient(generateBlobName()).getAppendBlobAsyncClient();
        StepVerifier.create(bc.appendBlock(DATA.getDefaultFlux(), DATA.getDefaultDataSize()))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void appendBlockRetryOnTransientFailure() {
        AppendBlobAsyncClient clientWithFailure = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            bc.getBlobUrl(), new TransientFailureInjectingHttpPipelinePolicy()).getAppendBlobAsyncClient();

        StepVerifier.create(clientWithFailure.appendBlock(DATA.getDefaultFlux(), DATA.getDefaultDataSize())
            .then(FluxUtil.collectBytesInByteBufferStream(bc.downloadStream())))
            .assertNext(r -> TestUtils.assertArraysEqual(DATA.getDefaultBytes(), r))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2022-11-02")
    @Test
    public void appendBlockHighThroughput() {
        int size = 5 * Constants.MB;
        byte[] randomData = getRandomByteArray(size); // testing upload of size greater than 4MB
        Flux<ByteBuffer> uploadStream = Flux.just(ByteBuffer.wrap(randomData));

        assertAsyncResponseStatusCode(bc.appendBlockWithResponse(uploadStream, size, null,
            null), 201);

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(bc.downloadStream()))
            .assertNext(r -> TestUtils.assertArraysEqual(randomData, r))
            .verifyComplete();
    }

    @Test
    public void appendBlockFromURLMin() {
        byte[] data = getRandomByteArray(1024);

        AppendBlobAsyncClient destURL = ccAsync.getBlobAsyncClient(generateBlobName()).getAppendBlobAsyncClient();

        BlobRange blobRange = new BlobRange(0, (long) PageBlobClient.PAGE_BYTES);

        String sas = bc.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobSasPermission().setTagsPermission(true).setReadPermission(true)));

        Mono<Response<AppendBlobItem>> response = bc.appendBlock(Flux.just(ByteBuffer.wrap(data)), data.length)
            .then(destURL.create())
            .then(destURL.appendBlockFromUrlWithResponse(bc.getBlobUrl() + "?" + sas, blobRange,
                null, null, null));

        StepVerifier.create(response)
            .assertNext(r -> {
                assertResponseStatusCode(r, 201);
                validateBasicHeaders(r.getHeaders());
            })
            .verifyComplete();
    }

    /*@RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2024-08-04")
    @Test
    public void appendBlockFromURLSourceErrorAndStatusCodeNewTest() {
        AppendBlobAsyncClient destBlob = ccAsync.getBlobAsyncClient(generateBlobName()).getAppendBlobAsyncClient();

        StepVerifier.create(destBlob.createIfNotExists().then(destBlob.appendBlockFromUrl(bc.getBlobUrl(), new BlobRange(0, (long) PageBlobClient.PAGE_BYTES))))
            .verifyErrorSatisfies(r -> {
                BlobStorageException e = assertInstanceOf(BlobStorageException.class, r);
                assertTrue(e.getStatusCode() == 409);
                assertTrue(e.getServiceMessage().contains("PublicAccessNotPermitted"));
                assertTrue(e.getServiceMessage().contains("Public access is not permitted on this storage account."));
            });
    }*/

    @Test
    public void appendBlockFromURLRange() {
        byte[] data = getRandomByteArray(4 * 1024);

        AppendBlobAsyncClient destURL = ccAsync.getBlobAsyncClient(generateBlobName()).getAppendBlobAsyncClient();

        String sas = bc.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobSasPermission().setTagsPermission(true).setReadPermission(true)));

        Mono<byte[]> response = bc.appendBlock(Flux.just(ByteBuffer.wrap(data)), data.length)
            .then(destURL.create())
            .then(destURL.appendBlockFromUrl(bc.getBlobUrl() + "?" + sas, new BlobRange(2 * 1024, 1024L)))
            .then(FluxUtil.collectBytesInByteBufferStream(destURL.downloadStream()));

        StepVerifier.create(response)
            .assertNext(r -> TestUtils.assertArraysEqual(data, 2 * 1024, r, 0, 1024))
            .verifyComplete();
    }

    @Test
    public void appendBlockFromURLMD5() throws NoSuchAlgorithmException {
        byte[] data = getRandomByteArray(1024);

        AppendBlobAsyncClient destURL = ccAsync.getBlobAsyncClient(generateBlobName()).getAppendBlobAsyncClient();

        String sas = bc.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobSasPermission().setTagsPermission(true).setReadPermission(true)));

        Mono<Response<AppendBlobItem>> response = bc.appendBlock(Flux.just(ByteBuffer.wrap(data)), data.length)
            .then(destURL.create())
            .then(destURL.appendBlockFromUrlWithResponse(bc.getBlobUrl() + "?" + sas, null,
                MessageDigest.getInstance("MD5").digest(data), null, null));

        StepVerifier.create(response)
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void appendBlockFromURLMD5Fail() throws NoSuchAlgorithmException {
        byte[] data = getRandomByteArray(1024);

        AppendBlobAsyncClient destURL = ccAsync.getBlobAsyncClient(generateBlobName()).getAppendBlobAsyncClient();

        String sas = bc.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobSasPermission().setTagsPermission(true).setReadPermission(true)));

        Mono<Response<AppendBlobItem>> response = bc.appendBlock(Flux.just(ByteBuffer.wrap(data)), data.length)
            .then(destURL.create())
            .then(destURL.appendBlockFromUrlWithResponse(bc.getBlobUrl() + "?" + sas, null,
                MessageDigest.getInstance("MD5").digest("garbage".getBytes()), null,
                null));

        StepVerifier.create(response)
            .verifyError(BlobStorageException.class);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("appendBlockSupplier")
    public void appendBlockFromURLDestinationAC(OffsetDateTime modified, OffsetDateTime unmodified, String match,
                                                String noneMatch, String leaseID, Long appendPosE, Long maxSizeLTE,
                                                String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");

        Mono<Response<AppendBlobItem>> response = bc.setTags(t)
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
                AppendBlobRequestConditions bac = new AppendBlobRequestConditions()
                    .setLeaseId(newLease)
                    .setIfMatch(newMatch)
                    .setIfNoneMatch(noneMatch)
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified)
                    .setAppendPosition(appendPosE)
                    .setMaxSize(maxSizeLTE)
                    .setTagsConditions(tags);

                AppendBlobAsyncClient sourceURL = ccAsync.getBlobAsyncClient(generateBlobName()).getAppendBlobAsyncClient();
                String sas = sourceURL.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
                    new BlobSasPermission().setTagsPermission(true).setReadPermission(true)));

                return sourceURL.create()
                    .then(sourceURL.appendBlockWithResponse(DATA.getDefaultFlux(), DATA.getDefaultDataSize(), null,
                        null))
                    .then(bc.appendBlockFromUrlWithResponse(sourceURL.getBlobUrl() + "?" + sas, null,
                        null, bac, null));
            });

        assertAsyncResponseStatusCode(response, 201);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("appendBlockFailSupplier")
    public void appendBlockFromURLDestinationACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
                                                    String noneMatch, String leaseID, Long maxSizeLTE, Long appendPosE,
                                                    String tags) {
        Mono<Response<AppendBlobItem>> response = Mono.zip(setupBlobLeaseCondition(bc, leaseID),
            setupBlobMatchCondition(bc, noneMatch))
            .flatMap(tuple -> {
                String newNoneMatch = tuple.getT2();
                if ("null".equals(newNoneMatch)) {
                    newNoneMatch = null;
                }
                AppendBlobRequestConditions bac = new AppendBlobRequestConditions()
                    .setLeaseId(leaseID)
                    .setIfMatch(match)
                    .setIfNoneMatch(newNoneMatch)
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified)
                    .setAppendPosition(appendPosE)
                    .setMaxSize(maxSizeLTE)
                    .setTagsConditions(tags);

                AppendBlobAsyncClient sourceURL = ccAsync.getBlobAsyncClient(generateBlobName()).getAppendBlobAsyncClient();
                String sas = sourceURL.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
                    new BlobSasPermission().setTagsPermission(true).setReadPermission(true)));

                return sourceURL.create()
                    .then(sourceURL.appendBlockWithResponse(DATA.getDefaultFlux(), DATA.getDefaultDataSize(), null,
                        null))
                    .then(bc.appendBlockFromUrlWithResponse(sourceURL.getBlobUrl() + "?" + sas, null,
                        null, bac, null));
            });

        StepVerifier.create(response)
            .verifyError(BlobStorageException.class);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("appendBlockFromURLSupplier")
    public void appendBlockFromURLSourceAC(OffsetDateTime sourceIfModifiedSince, OffsetDateTime sourceIfUnmodifiedSince,
                                           String sourceIfMatch, String sourceIfNoneMatch) {
        AppendBlobAsyncClient sourceURL = ccAsync.getBlobAsyncClient(generateBlobName()).getAppendBlobAsyncClient();

        Mono<Response<AppendBlobItem>> response = sourceURL.create()
            .then(sourceURL.appendBlockWithResponse(DATA.getDefaultFlux(), DATA.getDefaultDataSize(), null,
            null))
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

                String sas = sourceURL.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
                    new BlobSasPermission().setTagsPermission(true).setReadPermission(true)));

                return bc.appendBlockFromUrlWithResponse(sourceURL.getBlobUrl() + "?" + sas, null,
                    null, null, smac);
            });

        assertAsyncResponseStatusCode(response, 201);
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
                                               OffsetDateTime sourceIfUnmodifiedSince, String sourceIfMatch,
                                               String sourceIfNoneMatch) {
        AppendBlobAsyncClient sourceURL = ccAsync.getBlobAsyncClient(generateBlobName()).getAppendBlobAsyncClient();

        Mono<Response<AppendBlobItem>> response = sourceURL.create()
            .then(sourceURL.appendBlockWithResponse(DATA.getDefaultFlux(), DATA.getDefaultDataSize(), null,
                null))
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

                String sas = sourceURL.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
                    new BlobSasPermission().setTagsPermission(true).setReadPermission(true)));

                return bc.appendBlockFromUrlWithResponse(sourceURL.getBlobUrl() + "?" + sas, null,
                    null, null, smac);
            });

        StepVerifier.create(response)
            .verifyError(BlobStorageException.class);
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
        StepVerifier.create(bc.create())
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void createOverwriteTrue() {
        StepVerifier.create(bc.create(true))
            .expectNextCount(1)
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void sealDefaults() {
        StepVerifier.create(bc.sealWithResponse(null))
            .assertNext(r -> {
                assertResponseStatusCode(r, 200);
                assertEquals("true", r.getHeaders().getValue(X_MS_BLOB_SEALED));
            })
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void sealMin() {
        StepVerifier.create(bc.seal().then(bc.getProperties()))
            .assertNext(p -> assertTrue(p.isSealed()))
            .verifyComplete();

        StepVerifier.create(bc.downloadStreamWithResponse(null, null, null,
                false))
            .assertNext(r -> assertTrue(r.getDeserializedHeaders().isSealed()))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void sealError() {
        bc = ccAsync.getBlobAsyncClient(generateBlobName()).getAppendBlobAsyncClient();
        StepVerifier.create(bc.seal())
            .verifyError(BlobStorageException.class);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("sealACSupplier")
    public void sealAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                       String leaseID, Long appendPosE) {

        Mono<Response<Void>> response = Mono.zip(setupBlobLeaseCondition(bc, leaseID),
            setupBlobMatchCondition(bc, match))
            .flatMap(tuple -> {
                String newLease = tuple.getT1();
                String newMatch = tuple.getT2();
                if ("null".equals(newLease)) {
                    newLease = null;
                }
                if ("null".equals(newMatch)) {
                    newMatch = null;
                }
                AppendBlobRequestConditions bac = new AppendBlobRequestConditions()
                    .setLeaseId(newLease)
                    .setIfMatch(newMatch)
                    .setIfNoneMatch(noneMatch)
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified)
                    .setAppendPosition(appendPosE);
                return bc.sealWithResponse(new AppendBlobSealOptions().setRequestConditions(bac));
            });

        assertAsyncResponseStatusCode(response, 200);

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

        Mono<Response<Void>> response = Mono.zip(setupBlobLeaseCondition(bc, leaseID),
            setupBlobMatchCondition(bc, noneMatch))
            .flatMap(tuple -> {
                String newNoneMatch = tuple.getT2();
                if ("null".equals(newNoneMatch)) {
                    newNoneMatch = null;
                }
                AppendBlobRequestConditions bac = new AppendBlobRequestConditions()
                    .setLeaseId(leaseID)
                    .setIfMatch(match)
                    .setIfNoneMatch(newNoneMatch)
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified)
                    .setAppendPosition(appendPosE);
                return bc.sealWithResponse(new AppendBlobSealOptions().setRequestConditions(bac));
            });

        StepVerifier.create(response)
            .verifyError(BlobStorageException.class);
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
        AppendBlobAsyncClient specialBlob = getSpecializedBuilder(bc.getBlobUrl())
            .addPolicy(getPerCallVersionPolicy())
            .buildAppendBlobAsyncClient();

        StepVerifier.create(specialBlob.getPropertiesWithResponse(null))
            .assertNext(r -> assertEquals("2017-11-09", r.getHeaders().getValue(X_MS_VERSION)))
            .verifyComplete();
    }

    @Test
    public void defaultAudience() {
        AppendBlobAsyncClient aadBlob = getSpecializedBuilderWithTokenCredential(bc.getBlobUrl())
            .audience(null)
            .buildAppendBlobAsyncClient();

        StepVerifier.create(aadBlob.exists())
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    public void storageAccountAudience() {
        AppendBlobAsyncClient aadBlob = getSpecializedBuilderWithTokenCredential(bc.getBlobUrl())
            .audience(BlobAudience.createBlobServiceAccountAudience(ccAsync.getAccountName()))
            .buildAppendBlobAsyncClient();

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
        AppendBlobAsyncClient aadBlob = getSpecializedBuilderWithTokenCredential(bc.getBlobUrl())
            .audience(BlobAudience.createBlobServiceAccountAudience("badAudience"))
            .buildAppendBlobAsyncClient();

        StepVerifier.create(aadBlob.getProperties())
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();
    }

    @Test
    public void audienceFromString() {
        String url = String.format("https://%s.blob.core.windows.net/", ccAsync.getAccountName());
        BlobAudience audience = BlobAudience.fromString(url);

        AppendBlobAsyncClient aadBlob = getSpecializedBuilderWithTokenCredential(bc.getBlobUrl())
            .audience(audience)
            .buildAppendBlobAsyncClient();

        StepVerifier.create(aadBlob.exists())
            .expectNext(true)
            .verifyComplete();
    }
}
