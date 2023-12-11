package com.azure.storage.blob;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.Response;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.BinaryData;
import com.azure.core.util.FluxUtil;
import com.azure.storage.blob.models.AppendBlobRequestConditions;
import com.azure.storage.blob.models.BlobAccessPolicy;
import com.azure.storage.blob.models.BlobCopySourceTagsMode;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobSignedIdentifier;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.models.BlockListType;
import com.azure.storage.blob.models.CopyStatusType;
import com.azure.storage.blob.models.PageBlobCopyIncrementalRequestConditions;
import com.azure.storage.blob.models.PageBlobRequestConditions;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.options.BlobCopyFromUrlOptions;
import com.azure.storage.blob.options.PageBlobCopyIncrementalOptions;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.blob.specialized.AppendBlobAsyncClient;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.blob.specialized.PageBlobAsyncClient;
import com.azure.storage.blob.specialized.PageBlobClient;
import com.azure.storage.common.implementation.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Isolated
public class AccessPolicyAsyncTests extends BlobTestBase {
    private BlockBlobAsyncClient blockBlobAsyncClient;
    private AppendBlobAsyncClient appendBlobAsyncClient;
    private PageBlobAsyncClient pageBlobAsyncClient;
    private final String scope1 = "testscope1";
    private BlockBlobAsyncClient cpknBlockBlob;
    private BlobContainerAsyncClient blobContainerClient;
    private BlobAsyncClient blobVersionedClient;

    @BeforeEach
    public void setup() {
        blockBlobAsyncClient = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        blockBlobAsyncClient.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize(), true).block();

        appendBlobAsyncClient = ccAsync.getBlobAsyncClient(generateBlobName()).getAppendBlobAsyncClient();
        appendBlobAsyncClient.create().block();

        pageBlobAsyncClient = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        pageBlobAsyncClient.create(PageBlobClient.PAGE_BYTES).block();

        BlobContainerClientBuilder builder = getContainerClientBuilder(ccAsync.getBlobContainerUrl())
            .credential(ENVIRONMENT.getPrimaryAccount().getCredential());
        BlobContainerAsyncClient cpknContainer = builder.encryptionScope(scope1).buildAsyncClient();
        cpknBlockBlob = cpknContainer.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();

        blobContainerClient = versionedBlobServiceAsyncClient.createBlobContainer(generateContainerName()).block();
        blobVersionedClient = blobContainerClient.getBlobAsyncClient(generateBlobName());
    }

    //Container Async Tests

    @Test
    public void setAccessPolicyMinAccess() {
        setAccessPolicySleepAsync(ccAsync, PublicAccessType.CONTAINER, null);
        StepVerifier.create(ccAsync.getProperties())
            .assertNext(r -> assertEquals(r.getBlobPublicAccess(), PublicAccessType.CONTAINER))
            .verifyComplete();
    }

    @Test
    public void setAccessPolicyMinIds() {
        BlobSignedIdentifier identifier = new BlobSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new BlobAccessPolicy()
                .setStartsOn(testResourceNamer.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime())
                .setExpiresOn(testResourceNamer.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime()
                    .plusDays(1))
                .setPermissions("r"));

        List<BlobSignedIdentifier> ids = Collections.singletonList(identifier);

        setAccessPolicySleepAsync(ccAsync, null, ids);

        StepVerifier.create(ccAsync.getAccessPolicy())
            .assertNext(r -> assertEquals(r.getIdentifiers().get(0).getId(), "0000"))
            .verifyComplete();
    }

    @Test
    public void setAccessPolicyError() {
        ccAsync = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(generateContainerName());

        StepVerifier.create(ccAsync.setAccessPolicy(null, null))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void getAccessPolicy() {
        BlobSignedIdentifier identifier = new BlobSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new BlobAccessPolicy()
                .setStartsOn(testResourceNamer.now())
                .setExpiresOn(testResourceNamer.now().plusDays(1))
                .setPermissions("r"));
        List<BlobSignedIdentifier> ids = Collections.singletonList(identifier);
        setAccessPolicySleepAsync(ccAsync, PublicAccessType.BLOB, ids);

        StepVerifier.create(ccAsync.getAccessPolicyWithResponse(null))
            .assertNext(r -> {
                assertResponseStatusCode(r, 200);
                assertEquals(r.getValue().getBlobAccessType(), PublicAccessType.BLOB);
                assertTrue(validateBasicHeaders(r.getHeaders()));
                assertEquals(r.getValue().getIdentifiers().get(0).getAccessPolicy().getExpiresOn(),
                    identifier.getAccessPolicy().getExpiresOn());
                assertEquals(r.getValue().getIdentifiers().get(0).getAccessPolicy().getStartsOn(),
                    identifier.getAccessPolicy().getStartsOn());
                assertEquals(r.getValue().getIdentifiers().get(0).getAccessPolicy().getPermissions(),
                    identifier.getAccessPolicy().getPermissions());
            })
            .verifyComplete();
    }

    //block blob async

    @Test
    public void stageBlockFromUrlMin() {
        setAccessPolicySleepAsync(ccAsync, PublicAccessType.CONTAINER, null);
        BlockBlobAsyncClient bu2 = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        String blockID = getBlockID();

        assertAsyncResponseStatusCode(bu2.stageBlockFromUrlWithResponse(blockID, blockBlobAsyncClient.getBlobUrl(),
            null, null, null, null), 201);
    }

    @Test
    public void stageBlockFromUrl() {
        setAccessPolicySleepAsync(ccAsync, PublicAccessType.CONTAINER, null);
        BlockBlobAsyncClient bu2 = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        String blockID = getBlockID();

        StepVerifier.create(bu2.stageBlockFromUrlWithResponse(blockID, blockBlobAsyncClient.getBlobUrl(),
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
                assertEquals(r.getUncommittedBlocks().size(), 1);
                assertEquals(r.getCommittedBlocks().size(), 0);
                assertEquals(r.getUncommittedBlocks().get(0).getName(), blockID);
            })
            .verifyComplete();

        bu2.commitBlockList(Collections.singletonList(blockID)).block();

        StepVerifier.create(bu2.downloadStream())
            .assertNext(r -> assertEquals(r, DATA.getDefaultData()))
            .verifyComplete();
    }

    @Test
    public void stageBlockFromURLRange() {
        setAccessPolicySleepAsync(ccAsync, PublicAccessType.CONTAINER, null);
        BlockBlobAsyncClient destURL = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();

        destURL.stageBlockFromUrl(getBlockID(), blockBlobAsyncClient.getBlobUrl(), new BlobRange(2L, 3L))
            .block();

        StepVerifier.create(destURL.listBlocks(BlockListType.UNCOMMITTED))
            .assertNext(r -> {
                assertEquals(r.getCommittedBlocks().size(), 0);
                assertEquals(r.getUncommittedBlocks().size(), 1);
            })
            .verifyComplete();
    }

    @Test
    public void stageBlockFromURLMD5() throws NoSuchAlgorithmException {
        setAccessPolicySleepAsync(ccAsync, PublicAccessType.CONTAINER, null);
        BlockBlobAsyncClient destURL = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();

        StepVerifier.create(destURL.stageBlockFromUrlWithResponse(getBlockID(), blockBlobAsyncClient.getBlobUrl(),
                null, MessageDigest.getInstance("MD5").digest(DATA.getDefaultBytes()), null,
                null))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void stageBlockFromURLMD5Fail() {
        setAccessPolicySleepAsync(ccAsync, PublicAccessType.CONTAINER, null);
        BlockBlobAsyncClient destURL = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();

        StepVerifier.create(destURL.stageBlockFromUrlWithResponse(getBlockID(), blockBlobAsyncClient.getBlobUrl(),
                null, "garbage".getBytes(), null, null))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void stageBlockFromURLLease() {
        setAccessPolicySleepAsync(ccAsync, PublicAccessType.CONTAINER, null);
        StepVerifier.create(blockBlobAsyncClient.stageBlockFromUrlWithResponse(getBlockID(),
                blockBlobAsyncClient.getBlobUrl(), null, null,
                setupBlobLeaseCondition(blockBlobAsyncClient, RECEIVED_LEASE_ID), null))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void stageBlockFromURLLeaseFail() {
        setAccessPolicySleepAsync(ccAsync, PublicAccessType.CONTAINER, null);
        StepVerifier.create(blockBlobAsyncClient.stageBlockFromUrlWithResponse(getBlockID(),
                blockBlobAsyncClient.getBlobUrl(), null, null, "garbage",
                null))
            .verifyError(BlobStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("stageBlockFromURLSourceACSupplier")
    public void stageBlockFromURLSourceAC(OffsetDateTime sourceIfModifiedSince, OffsetDateTime sourceIfUnmodifiedSince,
                                          String sourceIfMatch, String sourceIfNoneMatch) {
        setAccessPolicySleepAsync(ccAsync, PublicAccessType.CONTAINER, null);
        String blockID = getBlockID();

        BlockBlobAsyncClient sourceURL = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        sourceURL.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()).block();

        sourceIfMatch = setupBlobMatchCondition(sourceURL, sourceIfMatch);
        BlobRequestConditions smac = new BlobRequestConditions()
            .setIfModifiedSince(sourceIfModifiedSince)
            .setIfUnmodifiedSince(sourceIfUnmodifiedSince)
            .setIfMatch(sourceIfMatch)
            .setIfNoneMatch(sourceIfNoneMatch);

        assertAsyncResponseStatusCode(blockBlobAsyncClient.stageBlockFromUrlWithResponse(blockID, sourceURL.getBlobUrl(),
            null, null, null, smac), 201);
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
        setAccessPolicySleepAsync(ccAsync, PublicAccessType.CONTAINER, null);
        String blockID = getBlockID();

        BlockBlobAsyncClient sourceURL = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        sourceURL.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()).block();

        BlobRequestConditions smac = new BlobRequestConditions()
            .setIfModifiedSince(sourceIfModifiedSince)
            .setIfUnmodifiedSince(sourceIfUnmodifiedSince)
            .setIfMatch(sourceIfMatch)
            .setIfNoneMatch(setupBlobMatchCondition(sourceURL, sourceIfNoneMatch));

        StepVerifier.create(blockBlobAsyncClient.stageBlockFromUrlWithResponse(blockID, sourceURL.getBlobUrl(),
                null, null, null, smac))
            .verifyError(BlobStorageException.class);
    }
    private static Stream<Arguments> stageBlockFromURLSourceACFailSupplier() {
        return Stream.of(
            Arguments.of(NEW_DATE, null, null, null),
            Arguments.of(null, OLD_DATE, null, null),
            Arguments.of(null, null, GARBAGE_ETAG, null),
            Arguments.of(null, null, null, RECEIVED_ETAG));
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20210608ServiceVersion")
    @ParameterizedTest
    @MethodSource("uploadFromUrlCopySourceTagsSupplier")
    public void uploadFromUrlCopySourceTags(BlobCopySourceTagsMode mode) {
        setAccessPolicySleepAsync(ccAsync, PublicAccessType.CONTAINER, null);
        Map<String, String> sourceTags = Collections.singletonMap("foo", "bar");
        Map<String, String> destTags = Collections.singletonMap("fizz", "buzz");
        blockBlobAsyncClient.setTags(sourceTags).block();

        String sas = blockBlobAsyncClient.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobSasPermission().setTagsPermission(true).setReadPermission(true)));

        BlobAsyncClient bc2 = ccAsync.getBlobAsyncClient(generateBlobName());

        BlobCopyFromUrlOptions options = new BlobCopyFromUrlOptions(
            blockBlobAsyncClient.getBlobUrl() + "?" + sas).setCopySourceTagsMode(mode);
        if (BlobCopySourceTagsMode.REPLACE == mode) {
            options.setTags(destTags);
        }

        bc2.copyFromUrlWithResponse(options).block();

        StepVerifier.create(bc2.getTags())
            .assertNext(r -> {
                if (BlobCopySourceTagsMode.REPLACE == mode) {
                    assertEquals(r, destTags);
                } else {
                    assertEquals(r, sourceTags);
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

    //append blob async

    @Test
    public void appendBlockFromURLMin() {
        setAccessPolicySleepAsync(ccAsync, PublicAccessType.CONTAINER, null);
        byte[] data = getRandomByteArray(1024);
        appendBlobAsyncClient.appendBlock(Flux.just(ByteBuffer.wrap(data)), data.length).block();

        AppendBlobAsyncClient destURL = ccAsync.getBlobAsyncClient(generateBlobName()).getAppendBlobAsyncClient();
        destURL.create().block();

        BlobRange blobRange = new BlobRange(0, (long) PageBlobClient.PAGE_BYTES);

        StepVerifier.create(destURL.appendBlockFromUrlWithResponse(appendBlobAsyncClient.getBlobUrl(), blobRange, null,
                null, null))
            .assertNext(r -> {
                assertResponseStatusCode(r, 201);
                validateBasicHeaders(r.getHeaders());
            })
            .verifyComplete();
    }

    @Test
    public void appendBlockFromURLRange() {
        setAccessPolicySleepAsync(ccAsync, PublicAccessType.CONTAINER, null);
        byte[] data = getRandomByteArray(4 * 1024);
        appendBlobAsyncClient.appendBlock(Flux.just(ByteBuffer.wrap(data)), data.length).block();

        AppendBlobAsyncClient destURL = ccAsync.getBlobAsyncClient(generateBlobName()).getAppendBlobAsyncClient();
        destURL.create().block();

        destURL.appendBlockFromUrl(appendBlobAsyncClient.getBlobUrl(), new BlobRange(2 * 1024, 1024L)).block();

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(destURL.downloadStream()))
            .assertNext(r -> TestUtils.assertArraysEqual(data, 2 * 1024, r, 0, 1024))
            .verifyComplete();
    }

    @Test
    public void appendBlockFromURLMD5() throws NoSuchAlgorithmException {
        setAccessPolicySleepAsync(ccAsync, PublicAccessType.CONTAINER, null);
        byte[] data = getRandomByteArray(1024);
        appendBlobAsyncClient.appendBlock(Flux.just(ByteBuffer.wrap(data)), data.length).block();

        AppendBlobAsyncClient destURL = ccAsync.getBlobAsyncClient(generateBlobName()).getAppendBlobAsyncClient();
        destURL.create().block();

        StepVerifier.create(destURL.appendBlockFromUrlWithResponse(appendBlobAsyncClient.getBlobUrl(), null,
                MessageDigest.getInstance("MD5").digest(data), null, null))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void appendBlockFromURLMD5Fail() throws NoSuchAlgorithmException {
        setAccessPolicySleepAsync(ccAsync, PublicAccessType.CONTAINER, null);
        byte[] data = getRandomByteArray(1024);
        appendBlobAsyncClient.appendBlock(Flux.just(ByteBuffer.wrap(data)), data.length).block();

        AppendBlobAsyncClient destURL = ccAsync.getBlobAsyncClient(generateBlobName()).getAppendBlobAsyncClient();
        destURL.create().block();

        StepVerifier.create(destURL.appendBlockFromUrlWithResponse(appendBlobAsyncClient.getBlobUrl(), null,
                MessageDigest.getInstance("MD5").digest("garbage".getBytes()), null,
                null))
            .verifyError(BlobStorageException.class);
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("appendBlockSupplier")
    public void appendBlockFromURLDestinationAC(OffsetDateTime modified, OffsetDateTime unmodified, String match,
                                                String noneMatch, String leaseID, Long appendPosE, Long maxSizeLTE,
                                                String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        appendBlobAsyncClient.setTags(t).block();
        setAccessPolicySleepAsync(ccAsync, PublicAccessType.CONTAINER, null);
        match = setupBlobMatchCondition(appendBlobAsyncClient, match);
        leaseID = setupBlobLeaseCondition(appendBlobAsyncClient, leaseID);
        AppendBlobRequestConditions bac = new AppendBlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setAppendPosition(appendPosE)
            .setMaxSize(maxSizeLTE)
            .setTagsConditions(tags);

        AppendBlobAsyncClient sourceURL = ccAsync.getBlobAsyncClient(generateBlobName()).getAppendBlobAsyncClient();
        sourceURL.create().block();
        sourceURL.appendBlockWithResponse(DATA.getDefaultFlux(), DATA.getDefaultDataSize(), null,
            null).block();

        assertAsyncResponseStatusCode(appendBlobAsyncClient.appendBlockFromUrlWithResponse(sourceURL.getBlobUrl(), null,
            null, bac, null), 201);
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


    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("appendBlockFailSupplier")
    public void appendBlockFromURLDestinationACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
                                                    String noneMatch, String leaseID, Long maxSizeLTE, Long appendPosE,
                                                    String tags) {
        setAccessPolicySleepAsync(ccAsync, PublicAccessType.CONTAINER, null);
        noneMatch = setupBlobMatchCondition(appendBlobAsyncClient, noneMatch);
        setupBlobLeaseCondition(appendBlobAsyncClient, leaseID);

        AppendBlobRequestConditions bac = new AppendBlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setAppendPosition(appendPosE)
            .setMaxSize(maxSizeLTE)
            .setTagsConditions(tags);

        AppendBlobAsyncClient sourceURL = ccAsync.getBlobAsyncClient(generateBlobName()).getAppendBlobAsyncClient();
        sourceURL.create().block();
        sourceURL.appendBlockWithResponse(DATA.getDefaultFlux(), DATA.getDefaultDataSize(), null,
            null).block();

        StepVerifier.create(appendBlobAsyncClient.appendBlockFromUrlWithResponse(sourceURL.getBlobUrl(), null,
                null, bac, null))
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

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("appendBlockFromURLSupplier")
    public void appendBlockFromURLSourceAC(OffsetDateTime sourceIfModifiedSince, OffsetDateTime sourceIfUnmodifiedSince,
                                           String sourceIfMatch, String sourceIfNoneMatch) {
        setAccessPolicySleepAsync(ccAsync, PublicAccessType.CONTAINER, null);
        AppendBlobAsyncClient sourceURL = ccAsync.getBlobAsyncClient(generateBlobName()).getAppendBlobAsyncClient();
        sourceURL.create().block();
        sourceURL.appendBlockWithResponse(DATA.getDefaultFlux(), DATA.getDefaultDataSize(), null,
            null).block();

        BlobRequestConditions smac = new BlobRequestConditions()
            .setIfModifiedSince(sourceIfModifiedSince)
            .setIfUnmodifiedSince(sourceIfUnmodifiedSince)
            .setIfMatch(setupBlobMatchCondition(sourceURL, sourceIfMatch))
            .setIfNoneMatch(sourceIfNoneMatch);

        assertAsyncResponseStatusCode(appendBlobAsyncClient.appendBlockFromUrlWithResponse(sourceURL.getBlobUrl(), null,
            null, null, smac), 201);
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

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("appendBlockFromURLFailSupplier")
    public void appendBlockFromURLSourceACFail(OffsetDateTime sourceIfModifiedSince,
                                               OffsetDateTime sourceIfUnmodifiedSince, String sourceIfMatch,
                                               String sourceIfNoneMatch) {
        setAccessPolicySleepAsync(ccAsync, PublicAccessType.CONTAINER, null);
        AppendBlobAsyncClient sourceURL = ccAsync.getBlobAsyncClient(generateBlobName()).getAppendBlobAsyncClient();
        sourceURL.create().block();
        sourceURL.appendBlockWithResponse(DATA.getDefaultFlux(), DATA.getDefaultDataSize(), null,
            null).block();

        BlobRequestConditions smac = new BlobRequestConditions()
            .setIfModifiedSince(sourceIfModifiedSince)
            .setIfUnmodifiedSince(sourceIfUnmodifiedSince)
            .setIfMatch(sourceIfMatch)
            .setIfNoneMatch(setupBlobMatchCondition(sourceURL, sourceIfNoneMatch));

        StepVerifier.create(appendBlobAsyncClient.appendBlockFromUrlWithResponse(sourceURL.getBlobUrl(), null,
                null, null, smac))
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

    //page blob async

    @Test
    public void uploadPageFromURLMin() {
        setAccessPolicySleepAsync(ccAsync, PublicAccessType.CONTAINER, null);
        PageBlobAsyncClient destURL = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        destURL.create(PageBlobClient.PAGE_BYTES).block();
        PageRange pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1);
        destURL.uploadPages(pageRange, Flux.just(ByteBuffer.wrap(getRandomByteArray(PageBlobClient.PAGE_BYTES)))).block();

        StepVerifier.create(pageBlobAsyncClient.uploadPagesFromUrlWithResponse(pageRange, destURL.getBlobUrl(), null, null,
                null, null))
            .assertNext(r -> {
                assertResponseStatusCode(r, 201);
                assertTrue(validateBasicHeaders(r.getHeaders()));
            })
            .verifyComplete();
    }

    @Test
    public void uploadPageFromURLRange() {
        setAccessPolicySleepAsync(ccAsync, PublicAccessType.CONTAINER, null);

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
    public void uploadPageFromURLMD5() throws NoSuchAlgorithmException {
        setAccessPolicySleepAsync(ccAsync, PublicAccessType.CONTAINER, null);

        PageBlobAsyncClient destURL = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        destURL.create(PageBlobClient.PAGE_BYTES).block();

        byte[] data = getRandomByteArray(PageBlobClient.PAGE_BYTES);
        PageRange pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1);
        pageBlobAsyncClient.uploadPages(pageRange, Flux.just(ByteBuffer.wrap(data))).block();

        StepVerifier.create(destURL.uploadPagesFromUrlWithResponse(pageRange, pageBlobAsyncClient.getBlobUrl(), null,
                MessageDigest.getInstance("MD5").digest(data), null, null))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void uploadPageFromURLMD5Fail() throws NoSuchAlgorithmException {
        setAccessPolicySleepAsync(ccAsync, PublicAccessType.CONTAINER, null);
        PageBlobAsyncClient destURL = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        destURL.create(PageBlobClient.PAGE_BYTES).block();

        PageRange pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1);
        pageBlobAsyncClient.uploadPages(pageRange, Flux.just(ByteBuffer.wrap(getRandomByteArray(PageBlobClient.PAGE_BYTES)))).block();

        StepVerifier.create(destURL.uploadPagesFromUrlWithResponse(pageRange, pageBlobAsyncClient.getBlobUrl(), null,
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
        pageBlobAsyncClient.setTags(t).block();
        setAccessPolicySleepAsync(ccAsync, PublicAccessType.CONTAINER, null);
        PageBlobAsyncClient sourceURL = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        sourceURL.create(PageBlobClient.PAGE_BYTES).block();
        PageRange pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1);
        sourceURL.uploadPages(pageRange, Flux.just(ByteBuffer.wrap(getRandomByteArray(PageBlobClient.PAGE_BYTES)))).block();

        PageBlobRequestConditions pac = new PageBlobRequestConditions()
            .setLeaseId(setupBlobLeaseCondition(pageBlobAsyncClient, leaseID))
            .setIfMatch(setupBlobMatchCondition(pageBlobAsyncClient, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfSequenceNumberLessThan(sequenceNumberLT)
            .setIfSequenceNumberLessThanOrEqualTo(sequenceNumberLTE)
            .setIfSequenceNumberEqualTo(sequenceNumberEqual)
            .setTagsConditions(tags);

        assertAsyncResponseStatusCode(pageBlobAsyncClient.uploadPagesFromUrlWithResponse(pageRange, sourceURL.getBlobUrl(), null, null, pac,
            null), 201);
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
    public void uploadPageFromURLDestinationACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
                                                   String noneMatch, String leaseID, Long sequenceNumberLT, Long sequenceNumberLTE, Long sequenceNumberEqual,
                                                   String tags) {
        setAccessPolicySleepAsync(ccAsync, PublicAccessType.CONTAINER, null);

        PageBlobAsyncClient sourceURL = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        sourceURL.create(PageBlobClient.PAGE_BYTES).block();
        PageRange pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1);
        sourceURL.uploadPages(pageRange, Flux.just(ByteBuffer.wrap(getRandomByteArray(PageBlobClient.PAGE_BYTES)))).block();

        noneMatch = setupBlobMatchCondition(pageBlobAsyncClient, noneMatch);
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

        StepVerifier.create(pageBlobAsyncClient.uploadPagesFromUrlWithResponse(
                pageRange, sourceURL.getBlobUrl(), null, null, pac, null))
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

    @ParameterizedTest
    @MethodSource("uploadPageFromURLSourceACSupplier")
    public void uploadPageFromURLSourceAC(OffsetDateTime sourceIfModifiedSince, OffsetDateTime sourceIfUnmodifiedSince,
                                          String sourceIfMatch, String sourceIfNoneMatch) {
        setAccessPolicySleepAsync(ccAsync, PublicAccessType.CONTAINER, null);
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

        assertAsyncResponseStatusCode(pageBlobAsyncClient.uploadPagesFromUrlWithResponse(pageRange, sourceURL.getBlobUrl(), null, null, null,
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
        setAccessPolicySleepAsync(ccAsync, PublicAccessType.CONTAINER, null);
        PageBlobAsyncClient sourceURL = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        sourceURL.create(PageBlobClient.PAGE_BYTES).block();
        PageRange pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1);
        sourceURL.uploadPages(pageRange, Flux.just(ByteBuffer.wrap(getRandomByteArray(PageBlobClient.PAGE_BYTES)))).block();

        BlobRequestConditions smac = new BlobRequestConditions()
            .setIfModifiedSince(sourceIfModifiedSince)
            .setIfUnmodifiedSince(sourceIfUnmodifiedSince)
            .setIfMatch(sourceIfMatch)
            .setIfNoneMatch(setupBlobMatchCondition(sourceURL, sourceIfNoneMatch));

        StepVerifier.create(pageBlobAsyncClient.uploadPagesFromUrlWithResponse(
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

    @Test
    public void startIncrementalCopy() {
        setAccessPolicySleepAsync(ccAsync, PublicAccessType.BLOB, null);
        PageBlobAsyncClient bc2 = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        String snapId = pageBlobAsyncClient.createSnapshot().block().getSnapshotId();

        Response<CopyStatusType> copyResponse = bc2.copyIncrementalWithResponse(pageBlobAsyncClient.getBlobUrl(), snapId, null).block();

        CopyStatusType status = copyResponse.getValue();
        OffsetDateTime start = testResourceNamer.now();
        while (status != CopyStatusType.SUCCESS) {
            status = bc2.getProperties().block().getCopyStatus();
            OffsetDateTime currentTime = testResourceNamer.now();
            if (status == CopyStatusType.FAILED || currentTime.minusMinutes(1) == start) {
                throw new RuntimeException("Copy failed or took too long");
            }
            sleepIfRunningAgainstService(1000);
        }

        BlobProperties properties = bc2.getProperties().block();
        assertTrue(properties.isIncrementalCopy());
        assertNotNull(properties.getCopyDestinationSnapshot());
        validateBasicHeaders(copyResponse.getHeaders());
        assertNotNull(copyResponse.getHeaders().getValue(X_MS_COPY_ID));
        assertNotNull(copyResponse.getValue());
    }

    @Test
    public void startIncrementalCopyMin() {
        setAccessPolicySleepAsync(ccAsync, PublicAccessType.BLOB, null);
        PageBlobAsyncClient bc2 = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        String snapshot = pageBlobAsyncClient.createSnapshot().block().getSnapshotId();

        assertAsyncResponseStatusCode(bc2.copyIncrementalWithResponse(pageBlobAsyncClient.getBlobUrl(), snapshot, null), 202);
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("startIncrementalCopyACSupplier")
    public void startIncrementalCopyAC(OffsetDateTime modified, OffsetDateTime unmodified, String match,
                                       String noneMatch, String tags) {
        setAccessPolicySleepAsync(ccAsync, PublicAccessType.BLOB, null);
        PageBlobAsyncClient bu2 = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        String snapshot = pageBlobAsyncClient.createSnapshot().block().getSnapshotId();

        Response<CopyStatusType> copyResponse = bu2.copyIncrementalWithResponse(pageBlobAsyncClient.getBlobUrl(), snapshot, null).block();

        CopyStatusType status = copyResponse.getValue();
        OffsetDateTime start = testResourceNamer.now();
        while (status != CopyStatusType.SUCCESS) {
            status = bu2.getProperties().block().getCopyStatus();
            OffsetDateTime currentTime = testResourceNamer.now();
            if (status == CopyStatusType.FAILED || currentTime.minusMinutes(1) == start) {
                throw new RuntimeException("Copy failed or took too long");
            }
            sleepIfRunningAgainstService(1000);
        }
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bu2.setTags(t).block();

        snapshot = pageBlobAsyncClient.createSnapshot().block().getSnapshotId();
        match = setupBlobMatchCondition(bu2, match);
        PageBlobCopyIncrementalRequestConditions mac = new PageBlobCopyIncrementalRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setTagsConditions(tags);

        assertAsyncResponseStatusCode(bu2.copyIncrementalWithResponse(new PageBlobCopyIncrementalOptions(pageBlobAsyncClient.getBlobUrl(),
            snapshot).setRequestConditions(mac)),  202);
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
        setAccessPolicySleepAsync(ccAsync, PublicAccessType.BLOB, null);

        PageBlobAsyncClient bu2 = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        String snapshot = pageBlobAsyncClient.createSnapshot().block().getSnapshotId();
        bu2.copyIncremental(pageBlobAsyncClient.getBlobUrl(), snapshot).block();
        String finalSnapshot = pageBlobAsyncClient.createSnapshot().block().getSnapshotId();
        noneMatch = setupBlobMatchCondition(bu2, noneMatch);
        PageBlobCopyIncrementalRequestConditions mac = new PageBlobCopyIncrementalRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setTagsConditions(tags);

        StepVerifier.create(bu2.copyIncrementalWithResponse(
                new PageBlobCopyIncrementalOptions(pageBlobAsyncClient.getBlobUrl(), finalSnapshot).setRequestConditions(mac)))
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

    // cpkn tests

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20201206ServiceVersion")
    @Test
    public void asyncCopyEncryptionScope() {
        setAccessPolicySleepAsync(ccAsync, PublicAccessType.CONTAINER, null);
        BlobAsyncClient blobSource = ccAsync.getBlobAsyncClient(generateBlobName());
        blobSource.upload(DATA.getDefaultBinaryData()).block();

        cpknBlockBlob.copyFromUrlWithResponse(new BlobCopyFromUrlOptions(blobSource.getBlobUrl())).block();

        StepVerifier.create(cpknBlockBlob.getProperties())
            .assertNext(r -> assertEquals(scope1, r.getEncryptionScope()))
            .verifyComplete();
    }

    //sas

    @Test
    public void containerSasIdentifierAndPermissions() {
        BlobSignedIdentifier identifier = new BlobSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new BlobAccessPolicy().setPermissions("racwdl")
                .setExpiresOn(testResourceNamer.now().plusDays(1)));
        setAccessPolicySleepAsync(ccAsync, null, Arrays.asList(identifier));

        // Check containerSASPermissions
        BlobContainerSasPermission permissions = new BlobContainerSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setListPermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)
            .setListPermission(true);
        if (Constants.SAS_SERVICE_VERSION.compareTo("2019-12-12") >= 0) {
            permissions.setDeleteVersionPermission(true).setFilterPermission(true);
        }
        if (Constants.SAS_SERVICE_VERSION.compareTo("2020-06-12") >= 0) {
            permissions.setImmutabilityPolicyPermission(true);
        }

        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);

        BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(identifier.getId());
        String sasWithId = ccAsync.generateSas(sasValues);
        BlobContainerAsyncClient client1 = getContainerAsyncClient(sasWithId, ccAsync.getBlobContainerUrl());
        StepVerifier.create(client1.listBlobs())
            .thenConsumeWhile(r -> true)
            .verifyComplete();

        sasValues = new BlobServiceSasSignatureValues(expiryTime, permissions);
        String sasWithPermissions = ccAsync.generateSas(sasValues);
        BlobContainerAsyncClient client2 = getContainerAsyncClient(sasWithPermissions, ccAsync.getBlobContainerUrl());
        StepVerifier.create(client2.listBlobs())
            .thenConsumeWhile(r -> true)
            .verifyComplete();;
    }

    //servive async
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void sasSanitization(boolean unsanitize) {
        String identifier = "id with spaces";
        String blobName = generateBlobName();
        setAccessPolicySleepAsync(ccAsync, null,Collections.singletonList(new BlobSignedIdentifier()
            .setId(identifier)
            .setAccessPolicy(new BlobAccessPolicy()
                .setPermissions("racwdl")
                .setExpiresOn(testResourceNamer.now().plusDays(1)))));
        ccAsync.getBlobAsyncClient(blobName).upload(BinaryData.fromBytes("test".getBytes())).block();
        String sas = ccAsync.generateSas(new BlobServiceSasSignatureValues(identifier));
        if (unsanitize) {
            sas = sas.replace("%20", " ");
        }

        // when: "Endpoint with SAS built in, works as expected"
        String finalSas = sas;
        BlobContainerAsyncClient client1 = instrument(new BlobContainerClientBuilder()
            .endpoint(ccAsync.getBlobContainerUrl() + "?" + finalSas))
            .buildAsyncClient();
        StepVerifier.create(client1.getBlobAsyncClient(blobName).downloadContent())
            .expectNextCount(1)
            .verifyComplete();


        String connectionString = "AccountName=" + BlobUrlParts.parse(ccAsync.getAccountUrl()).getAccountName()
            + ";SharedAccessSignature=" + sas;
        BlobContainerAsyncClient client2 = instrument(new BlobContainerClientBuilder()
            .connectionString(connectionString)
            .containerName(ccAsync.getBlobContainerName()))
            .buildAsyncClient();
        StepVerifier.create(client2.getBlobAsyncClient(blobName).downloadContent())
            .expectNextCount(1)
            .verifyComplete();
    }

    //versioned

    @Test
    public void copyFromUrlBlobsWithVersion() {
        setAccessPolicySleepAsync(blobContainerClient, PublicAccessType.CONTAINER, null);
        BlockBlobItem blobItemV1 = blobVersionedClient.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(),
            DATA.getDefaultDataSize()).block();
        BlobAsyncClient sourceBlob = blobContainerClient.getBlobAsyncClient(generateBlobName());
        sourceBlob.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()).block();

        StepVerifier.create(blobVersionedClient.copyFromUrlWithResponse(sourceBlob.getBlobUrl(), null, null,
                null, null))
            .assertNext(r -> {
                assertNotNull(r.getHeaders().getValue(X_MS_VERSION_ID));
                assertNotEquals(blobItemV1.getVersionId(), r.getHeaders().getValue(X_MS_VERSION_ID));
            })
            .verifyComplete();
    }

}
