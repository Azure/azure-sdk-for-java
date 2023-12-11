package com.azure.storage.blob;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.RequestConditions;
import com.azure.core.http.rest.Response;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.AppendBlobItem;
import com.azure.storage.blob.models.AppendBlobRequestConditions;
import com.azure.storage.blob.models.BlobAccessPolicy;
import com.azure.storage.blob.models.BlobContainerAccessPolicies;
import com.azure.storage.blob.models.BlobCopyInfo;
import com.azure.storage.blob.models.BlobCopySourceTagsMode;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobSignedIdentifier;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.models.BlockList;
import com.azure.storage.blob.models.BlockListType;
import com.azure.storage.blob.models.CopyStatusType;
import com.azure.storage.blob.models.PageBlobCopyIncrementalRequestConditions;
import com.azure.storage.blob.models.PageBlobItem;
import com.azure.storage.blob.models.PageBlobRequestConditions;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.models.SyncCopyStatusType;
import com.azure.storage.blob.options.BlobCopyFromUrlOptions;
import com.azure.storage.blob.options.PageBlobCopyIncrementalOptions;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.blob.specialized.AppendBlobClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.blob.specialized.PageBlobClient;
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder;
import com.azure.storage.common.implementation.Constants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Isolated
public class AccessPolicyTests extends BlobTestBase {
    private BlobClient bc;
    private BlockBlobClient blockBlobClient;
    private AppendBlobClient appendBlobClient;
    private PageBlobClient pageBlobClient;
    private final String scope1 = "testscope1";
    private BlockBlobClient cpknBlockBlob;
    private BlobContainerClient blobContainerClient;
    private BlobClient blobVersionedClient;

    @BeforeEach
    public void setup() {
        bc = cc.getBlobClient(generateBlobName());
        bc.getBlockBlobClient().upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());

        blockBlobClient = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        blockBlobClient.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize(), true);

        appendBlobClient = cc.getBlobClient(generateBlobName()).getAppendBlobClient();
        appendBlobClient.create();

        pageBlobClient = cc.getBlobClient(generateBlobName()).getPageBlobClient();
        pageBlobClient.create(PageBlobClient.PAGE_BYTES);

        BlobContainerClientBuilder builder = getContainerClientBuilder(cc.getBlobContainerUrl())
            .credential(ENVIRONMENT.getPrimaryAccount().getCredential());
        BlobContainerClient cpknContainer = builder.encryptionScope(scope1).buildClient();
        cpknBlockBlob = cpknContainer.getBlobClient(generateBlobName()).getBlockBlobClient();

        blobContainerClient = versionedBlobServiceClient.createBlobContainer(generateContainerName());
        blobVersionedClient = blobContainerClient.getBlobClient(generateBlobName());
    }

    //BlobApiTests

    @Test
    public void abortCopyLeaseFail() {
        // Data has to be large enough and copied between accounts to give us enough time to abort
        new SpecializedBlobClientBuilder()
            .blobClient(bc)
            .buildBlockBlobClient()
            .upload(new ByteArrayInputStream(getRandomByteArray(8 * 1024 * 1024)), 8 * 1024 * 1024, true);
        // So we don't have to create a SAS.
        setAccessPolicySleep(cc, PublicAccessType.BLOB, null);

        BlobContainerClient cu2 = alternateBlobServiceClient.getBlobContainerClient(generateBlobName());
        cu2.create();
        BlockBlobClient bu2 = cu2.getBlobClient(generateBlobName()).getBlockBlobClient();
        bu2.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());

        String leaseId = setupBlobLeaseCondition(bu2, RECEIVED_LEASE_ID);
        BlobRequestConditions blobRequestConditions = new BlobRequestConditions().setLeaseId(leaseId);

        SyncPoller<BlobCopyInfo, Void> poller = bu2.beginCopy(bc.getBlobUrl(), null, null, null,
            null, blobRequestConditions, getPollingDuration(500));
        PollResponse<BlobCopyInfo> response = poller.poll();
        assertNotEquals(LongRunningOperationStatus.FAILED, response.getStatus());
        BlobCopyInfo blobCopyInfo = response.getValue();


        BlobStorageException e = assertThrows(BlobStorageException.class, () ->
            bu2.abortCopyFromUrlWithResponse(blobCopyInfo.getCopyId(), GARBAGE_LEASE_ID, null, null));
        assertEquals(412, e.getStatusCode());

        // cleanup:
        cu2.delete();
    }

    @Test
    public void abortCopy() {
        // Data has to be large enough and copied between accounts to give us enough time to abort
        new SpecializedBlobClientBuilder()
            .blobClient(bc)
            .buildBlockBlobClient()
            .upload(new ByteArrayInputStream(getRandomByteArray(8 * 1024 * 1024)), 8 * 1024 * 1024, true);
        // So we don't have to create a SAS.
        setAccessPolicySleep(cc, PublicAccessType.BLOB, null);

        BlobContainerClient cu2 = alternateBlobServiceClient.getBlobContainerClient(generateBlobName());
        cu2.create();
        BlobClient bu2 = cu2.getBlobClient(generateBlobName());

        SyncPoller<BlobCopyInfo, Void> poller = bu2.beginCopy(bc.getBlobUrl(), null, null, null,
            null, null, getPollingDuration(1000));
        PollResponse<BlobCopyInfo> lastResponse = poller.poll();
        assertNotNull(lastResponse);
        assertNotNull(lastResponse.getValue());
        Response<Void> response = bu2.abortCopyFromUrlWithResponse(lastResponse.getValue().getCopyId(), null,
            null, null);
        HttpHeaders headers = response.getHeaders();
        assertResponseStatusCode(response, 204);
        assertNotNull(headers.getValue(X_MS_REQUEST_ID));
        assertNotNull(headers.getValue(X_MS_VERSION));
        assertNotNull(headers.getValue(HttpHeaderName.DATE));
        // cleanup:
        // Normal test cleanup will not clean up containers in the alternate account.
        assertResponseStatusCode(cu2.deleteWithResponse(null, null, null), 202);
    }

    @Test
    public void abortCopyLease() {
        // Data has to be large enough and copied between accounts to give us enough time to abort
        new SpecializedBlobClientBuilder().blobClient(bc)
            .buildBlockBlobClient()
            .upload(new ByteArrayInputStream(getRandomByteArray(8 * 1024 * 1024)), 8 * 1024 * 1024, true);
        // So we don't have to create a SAS.
        setAccessPolicySleep(cc, PublicAccessType.BLOB, null);

        BlobContainerClient cu2 = alternateBlobServiceClient.getBlobContainerClient(generateContainerName());
        cu2.create();
        BlockBlobClient bu2 = cu2.getBlobClient(generateBlobName()).getBlockBlobClient();
        bu2.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        String leaseId = setupBlobLeaseCondition(bu2, RECEIVED_LEASE_ID);
        BlobRequestConditions blobAccess = new BlobRequestConditions().setLeaseId(leaseId);

        SyncPoller<BlobCopyInfo, Void> poller = bu2.beginCopy(bc.getBlobUrl(), null, null, null,
            null, blobAccess, getPollingDuration(1000));
        PollResponse<BlobCopyInfo> lastResponse = poller.poll();

        assertNotNull(lastResponse);
        assertNotNull(lastResponse.getValue());
        String copyId = lastResponse.getValue().getCopyId();
        assertResponseStatusCode(bu2.abortCopyFromUrlWithResponse(copyId, leaseId, null, null), 204);
        // cleanup:
        // Normal test cleanup will not clean up containers in the alternate account.
        cu2.delete();
    }

    @Test
    public void syncCopy() {
        // Sync copy is a deep copy, which requires either sas or public access.
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);
        BlockBlobClient bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient();

        HttpHeaders headers = bu2.copyFromUrlWithResponse(bc.getBlobUrl(), null, null, null, null, null, null)
            .getHeaders();

        assertEquals(SyncCopyStatusType.SUCCESS.toString(), headers.getValue(X_MS_COPY_STATUS));
        assertNotNull(headers.getValue(X_MS_COPY_ID));
        assertTrue(validateBasicHeaders(headers));
    }

    @Test
    public void syncCopyMin() {
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);
        BlockBlobClient bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        assertResponseStatusCode(bu2.copyFromUrlWithResponse(bc.getBlobUrl(), null, null, null, null, null, null), 202);
    }

    @ParameterizedTest
    @MethodSource("snapshotMetadataSupplier")
    public void syncCopyMetadata(String key1, String value1, String key2, String value2) {
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);
        BlockBlobClient bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2);
        }

        bu2.copyFromUrlWithResponse(bc.getBlobUrl(), metadata, null, null, null, null, null);
        assertEquals(metadata, bu2.getProperties().getMetadata());
    }

    private static Stream<Arguments> snapshotMetadataSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null),
            Arguments.of("foo", "bar", "fizz", "buzz"));
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("copyTagsSupplier")
    public void syncCopyTags(String key1, String value1, String key2, String value2) {
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);
        BlockBlobClient bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        Map<String, String> tags = new HashMap<>();
        if (key1 != null && value1 != null) {
            tags.put(key1, value1);
        }
        if (key2 != null && value2 != null) {
            tags.put(key2, value2);
        }

        bu2.copyFromUrlWithResponse(new BlobCopyFromUrlOptions(bc.getBlobUrl()).setTags(tags), null, null);
        assertEquals(tags, bu2.getTags());
    }

    private static Stream<Arguments> copyTagsSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null),
            Arguments.of("foo", "bar", "fizz", "buzz"),
            Arguments.of(" +-./:=_  +-./:=_", " +-./:=_", null, null));
    }

    @ParameterizedTest
    @MethodSource("syncCopySourceACSupplier")
    public void syncCopySourceAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch) {
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);
        BlockBlobClient bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        match = setupBlobMatchCondition(bc, match);
        RequestConditions mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch);

        assertResponseStatusCode(bu2.copyFromUrlWithResponse(bc.getBlobUrl(), null, null, mac, null, null, null), 202);
    }

    private static Stream<Arguments> syncCopySourceACSupplier() {
        return Stream.of(Arguments.of(null, null, null, null),
            Arguments.of(OLD_DATE, null, null, null),
            Arguments.of(null, NEW_DATE, null, null),
            Arguments.of(null, null, RECEIVED_ETAG, null),
            Arguments.of(null, null, null, GARBAGE_ETAG));
    }

    @ParameterizedTest
    @MethodSource("syncCopySourceACFailSupplier")
    public void syncCopySourceACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
                                     String noneMatch) {
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);
        BlockBlobClient bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        noneMatch = setupBlobMatchCondition(bc, noneMatch);
        RequestConditions mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch);

        assertThrows(BlobStorageException.class, () -> bu2.copyFromUrlWithResponse(bc.getBlobUrl(), null, null, mac,
            null, null, null));
    }

    private static Stream<Arguments> syncCopySourceACFailSupplier() {
        return Stream.of(Arguments.of(NEW_DATE, null, null, null),
            Arguments.of(null, OLD_DATE, null, null),
            Arguments.of(null, null, GARBAGE_ETAG, null),
            Arguments.of(null, null, null, RECEIVED_ETAG));
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void syncCopyDestAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                               String leaseID, String tags) {
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);
        BlockBlobClient bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        bu2.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bu2.setTags(t);
        match = setupBlobMatchCondition(bu2, match);
        leaseID = setupBlobLeaseCondition(bu2, leaseID);
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);
        assertResponseStatusCode(bu2.copyFromUrlWithResponse(bc.getBlobUrl(), null, null, null, bac, null, null), 202);
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsFailSupplier")
    public void syncCopyDestACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                   String leaseID, String tags) {
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);
        BlockBlobClient bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        bu2.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        noneMatch = setupBlobMatchCondition(bu2, noneMatch);
        setupBlobLeaseCondition(bu2, leaseID);
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);
        assertThrows(BlobStorageException.class, () -> bu2.copyFromUrlWithResponse(bc.getBlobUrl(), null, null, null,
            bac, null, null));
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20210608ServiceVersion")
    @ParameterizedTest
    @MethodSource("syncCopySourceTagsSupplier")
    public void syncCopySourceTags(BlobCopySourceTagsMode mode) {
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);
        Map<String, String> sourceTags = new HashMap<>();
        sourceTags.put("foo", "bar");
        Map<String, String> destTags = new HashMap<>();
        destTags.put("fizz", "buzz");
        bc.setTags(sourceTags);

        String sas = bc.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobSasPermission().setTagsPermission(true).setReadPermission(true)));

        BlobClient bc2 = cc.getBlobClient(generateBlobName());

        BlobCopyFromUrlOptions options = new BlobCopyFromUrlOptions(bc.getBlobUrl() + "?" + sas)
            .setCopySourceTagsMode(mode);
        if (BlobCopySourceTagsMode.REPLACE == mode) {
            options.setTags(destTags);
        }

        bc2.copyFromUrlWithResponse(options, null, null);
        Map<String, String> receivedTags = bc2.getTags();

        if (BlobCopySourceTagsMode.REPLACE == mode) {
            assertEquals(receivedTags, destTags);
        } else {
            assertEquals(receivedTags,  sourceTags);
        }
    }

    private static Stream<Arguments> syncCopySourceTagsSupplier() {
        return Stream.of(Arguments.of(BlobCopySourceTagsMode.COPY),
            Arguments.of(BlobCopySourceTagsMode.REPLACE));
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20211202ServiceVersion")
    @Test
    public void syncCopyFromUrlAccessTierCold() {
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);
        BlockBlobClient bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        BlobCopyFromUrlOptions copyOptions = new BlobCopyFromUrlOptions(bc.getBlobUrl()).setTier(AccessTier.COLD);

        assertResponseStatusCode(bu2.copyFromUrlWithResponse(copyOptions, null, null), 202);
        assertEquals(bu2.getProperties().getAccessTier(), AccessTier.COLD);
    }

    //ContainerApiTests

    @Test
    public void setAccessPolicyMinAccess() {
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);
        assertEquals(cc.getProperties().getBlobPublicAccess(), PublicAccessType.CONTAINER);
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

        setAccessPolicySleep(cc, null, ids);

        assertEquals(cc.getAccessPolicy().getIdentifiers().get(0).getId(), "0000");
    }

    @Test
    public void setAccessPolicyError() {
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName());

        assertThrows(BlobStorageException.class, () -> cc.setAccessPolicy(null, null));
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
        setAccessPolicySleep(cc, PublicAccessType.BLOB, ids);
        Response<BlobContainerAccessPolicies> response = cc.getAccessPolicyWithResponse(null, null, null);

        assertResponseStatusCode(response, 200);
        assertEquals(response.getValue().getBlobAccessType(), PublicAccessType.BLOB);
        assertTrue(validateBasicHeaders(response.getHeaders()));
        assertEquals(response.getValue().getIdentifiers().get(0).getAccessPolicy().getExpiresOn(),
            identifier.getAccessPolicy().getExpiresOn());
        assertEquals(response.getValue().getIdentifiers().get(0).getAccessPolicy().getStartsOn(),
            identifier.getAccessPolicy().getStartsOn());
        assertEquals(response.getValue().getIdentifiers().get(0).getAccessPolicy().getPermissions(),
            identifier.getAccessPolicy().getPermissions());
    }

    //BlockBlobApiTests

    @Test
    public void stageBlockFromURLMD5Fail() {
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);
        BlockBlobClient destURL = cc.getBlobClient(generateBlobName()).getBlockBlobClient();

        assertThrows(BlobStorageException.class, () -> destURL.stageBlockFromUrlWithResponse(getBlockID(),
            blockBlobClient.getBlobUrl(), null, "garbage".getBytes(), null, null,
            null, null));
    }

    @Test
    public void stageBlockFromURLLease() {
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);

        assertDoesNotThrow(() -> blockBlobClient.stageBlockFromUrlWithResponse(getBlockID(),
            blockBlobClient.getBlobUrl(), null, null,
            setupBlobLeaseCondition(blockBlobClient, RECEIVED_LEASE_ID), null, null,
            null));
    }

    @Test
    public void stageBlockFromURLLeaseFail() {
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);
        assertThrows(BlobStorageException.class, () -> blockBlobClient.stageBlockFromUrlWithResponse(getBlockID(),
            blockBlobClient.getBlobUrl(), null, null, "garbage", null,
            null, null));
    }

    @ParameterizedTest
    @MethodSource("stageBlockFromURLSourceACSupplier")
    public void stageBlockFromURLSourceAC(OffsetDateTime sourceIfModifiedSince, OffsetDateTime sourceIfUnmodifiedSince,
                                          String sourceIfMatch, String sourceIfNoneMatch) {
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);
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
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);
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

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20210608ServiceVersion")
    @ParameterizedTest
    @MethodSource("uploadFromUrlCopySourceTagsSupplier")
    public void uploadFromUrlCopySourceTags(BlobCopySourceTagsMode mode) {
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);
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

    @Test
    public void stageBlockFromUrl() {
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);
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
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);
        BlockBlobClient bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        String blockID = getBlockID();

        assertResponseStatusCode(bu2.stageBlockFromUrlWithResponse(blockID, blockBlobClient.getBlobUrl(), null, null,
            null, null, null, null), 201);
    }

    @Test
    public void stageBlockFromURLRange() {
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);
        BlockBlobClient destURL = cc.getBlobClient(generateBlobName()).getBlockBlobClient();

        destURL.stageBlockFromUrl(getBlockID(), blockBlobClient.getBlobUrl(), new BlobRange(2L, 3L));
        BlockList blockList = destURL.listBlocks(BlockListType.UNCOMMITTED);

        assertEquals(blockList.getCommittedBlocks().size(), 0);
        assertEquals(blockList.getUncommittedBlocks().size(), 1);
    }

    @Test
    public void stageBlockFromURLMD5() {
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);
        BlockBlobClient destURL = cc.getBlobClient(generateBlobName()).getBlockBlobClient();

        assertDoesNotThrow(() -> destURL.stageBlockFromUrlWithResponse(getBlockID(), blockBlobClient.getBlobUrl(),
            null, MessageDigest.getInstance("MD5").digest(DATA.getDefaultBytes()), null,
            null, null, null));
    }

    //AppendBlobTests

    @Test
    public void appendBlockFromURLMin() {
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);
        byte[] data = getRandomByteArray(1024);
        appendBlobClient.appendBlock(new ByteArrayInputStream(data), data.length);

        AppendBlobClient destURL = cc.getBlobClient(generateBlobName()).getAppendBlobClient();
        destURL.create();

        BlobRange blobRange = new BlobRange(0, (long) PageBlobClient.PAGE_BYTES);

        Response<AppendBlobItem> response = destURL.appendBlockFromUrlWithResponse(appendBlobClient.getBlobUrl(), blobRange, null,
            null, null, null, null);

        assertResponseStatusCode(response, 201);
        validateBasicHeaders(response.getHeaders());
    }

    @Test
    public void appendBlockFromURLRange() {
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);
        byte[] data = getRandomByteArray(4 * 1024);
        appendBlobClient.appendBlock(new ByteArrayInputStream(data), data.length);

        AppendBlobClient destURL = cc.getBlobClient(generateBlobName()).getAppendBlobClient();
        destURL.create();

        destURL.appendBlockFromUrl(appendBlobClient.getBlobUrl(), new BlobRange(2 * 1024, 1024L));

        ByteArrayOutputStream downloadStream = new ByteArrayOutputStream(1024);
        destURL.downloadStream(downloadStream);
        TestUtils.assertArraysEqual(data, 2 * 1024, downloadStream.toByteArray(), 0, 1024);
    }

    @Test
    public void appendBlockFromURLMD5() {
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);
        byte[] data = getRandomByteArray(1024);
        appendBlobClient.appendBlock(new ByteArrayInputStream(data), data.length);

        AppendBlobClient destURL = cc.getBlobClient(generateBlobName()).getAppendBlobClient();
        destURL.create();

        assertDoesNotThrow(() -> destURL.appendBlockFromUrlWithResponse(appendBlobClient.getBlobUrl(), null,
            MessageDigest.getInstance("MD5").digest(data), null, null, null, Context.NONE));

    }

    @Test
    public void appendBlockFromURLMD5Fail() {
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);
        byte[] data = getRandomByteArray(1024);
        appendBlobClient.appendBlock(new ByteArrayInputStream(data), data.length);

        AppendBlobClient destURL = cc.getBlobClient(generateBlobName()).getAppendBlobClient();
        destURL.create();

        assertThrows(BlobStorageException.class, () -> destURL.appendBlockFromUrlWithResponse(appendBlobClient.getBlobUrl(), null,
            MessageDigest.getInstance("MD5").digest("garbage".getBytes()), null, null, null, Context.NONE));
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("appendBlockSupplier")
    public void appendBlockFromURLDestinationAC(OffsetDateTime modified, OffsetDateTime unmodified, String match,
                                                String noneMatch, String leaseID, Long appendPosE, Long maxSizeLTE, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        appendBlobClient.setTags(t);
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);
        match = setupBlobMatchCondition(appendBlobClient, match);
        leaseID = setupBlobLeaseCondition(appendBlobClient, leaseID);
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

        assertResponseStatusCode(appendBlobClient.appendBlockFromUrlWithResponse(sourceURL.getBlobUrl(), null, null, bac, null, null,
            null), 201);
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
                                                    String noneMatch, String leaseID, Long maxSizeLTE, Long appendPosE, String tags) {
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);
        noneMatch = setupBlobMatchCondition(appendBlobClient, noneMatch);
        setupBlobLeaseCondition(appendBlobClient, leaseID);

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

        assertThrows(BlobStorageException.class, () -> appendBlobClient.appendBlockFromUrlWithResponse(sourceURL.getBlobUrl(), null,
            null, bac, null, null, Context.NONE));
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
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);
        AppendBlobClient sourceURL = cc.getBlobClient(generateBlobName()).getAppendBlobClient();
        sourceURL.create();
        sourceURL.appendBlockWithResponse(DATA.getDefaultInputStream(), DATA.getDefaultDataSize(), null, null, null,
            null);

        BlobRequestConditions smac = new BlobRequestConditions()
            .setIfModifiedSince(sourceIfModifiedSince)
            .setIfUnmodifiedSince(sourceIfUnmodifiedSince)
            .setIfMatch(setupBlobMatchCondition(sourceURL, sourceIfMatch))
            .setIfNoneMatch(sourceIfNoneMatch);

        assertResponseStatusCode(appendBlobClient.appendBlockFromUrlWithResponse(sourceURL.getBlobUrl(), null, null, null, smac, null,
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

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("appendBlockFromURLFailSupplier")
    public void appendBlockFromURLSourceACFail(OffsetDateTime sourceIfModifiedSince,
                                               OffsetDateTime sourceIfUnmodifiedSince, String sourceIfMatch, String sourceIfNoneMatch) {
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);
        AppendBlobClient sourceURL = cc.getBlobClient(generateBlobName()).getAppendBlobClient();
        sourceURL.create();
        sourceURL.appendBlockWithResponse(DATA.getDefaultInputStream(), DATA.getDefaultDataSize(), null, null, null,
            null);

        BlobRequestConditions smac = new BlobRequestConditions()
            .setIfModifiedSince(sourceIfModifiedSince)
            .setIfUnmodifiedSince(sourceIfUnmodifiedSince)
            .setIfMatch(sourceIfMatch)
            .setIfNoneMatch(setupBlobMatchCondition(sourceURL, sourceIfNoneMatch));

        assertThrows(BlobStorageException.class, () -> appendBlobClient.appendBlockFromUrlWithResponse(sourceURL.getBlobUrl(), null,
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

    //Page Blob Tests

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("startIncrementalCopyACSupplier")
    public void startIncrementalCopyAC(OffsetDateTime modified, OffsetDateTime unmodified, String match,
                                       String noneMatch, String tags) {
        setAccessPolicySleep(cc, PublicAccessType.BLOB, null);
        PageBlobClient bu2 = cc.getBlobClient(generateBlobName()).getPageBlobClient();
        String snapshot = pageBlobClient.createSnapshot().getSnapshotId();

        sleepIfRunningAgainstService(10 * 1000);

        Response<CopyStatusType> copyResponse = bu2.copyIncrementalWithResponse(pageBlobClient.getBlobUrl(), snapshot, null, null,
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

        snapshot = pageBlobClient.createSnapshot().getSnapshotId();
        match = setupBlobMatchCondition(bu2, match);
        PageBlobCopyIncrementalRequestConditions mac = new PageBlobCopyIncrementalRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setTagsConditions(tags);

        assertResponseStatusCode(bu2.copyIncrementalWithResponse(new PageBlobCopyIncrementalOptions(pageBlobClient.getBlobUrl(),
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
        setAccessPolicySleep(cc, PublicAccessType.BLOB, null);

        PageBlobClient bu2 = cc.getBlobClient(generateBlobName()).getPageBlobClient();
        String snapshot = pageBlobClient.createSnapshot().getSnapshotId();
        bu2.copyIncremental(pageBlobClient.getBlobUrl(), snapshot);
        String finalSnapshot = pageBlobClient.createSnapshot().getSnapshotId();
        noneMatch = setupBlobMatchCondition(bu2, noneMatch);
        PageBlobCopyIncrementalRequestConditions mac = new PageBlobCopyIncrementalRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setTagsConditions(tags);

        assertThrows(BlobStorageException.class, () -> bu2.copyIncrementalWithResponse(
            new PageBlobCopyIncrementalOptions(pageBlobClient.getBlobUrl(), finalSnapshot).setRequestConditions(mac), null, null));
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
    public void uploadPageFromURLMin() {
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);
        PageBlobClient destURL = cc.getBlobClient(generateBlobName()).getPageBlobClient();
        destURL.create(PageBlobClient.PAGE_BYTES);
        destURL.uploadPages(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)));
        PageRange pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1);

        Response<PageBlobItem> response = pageBlobClient.uploadPagesFromUrlWithResponse(pageRange, destURL.getBlobUrl(), null, null,
            null, null, null, null);

        assertResponseStatusCode(response, 201);
        assertTrue(validateBasicHeaders(response.getHeaders()));
    }

    @Test
    public void uploadPageFromURLRange() {
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);

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
    public void uploadPageFromURLMD5() throws NoSuchAlgorithmException {
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);
        PageBlobClient destURL = cc.getBlobClient(generateBlobName()).getPageBlobClient();
        destURL.create(PageBlobClient.PAGE_BYTES);
        byte[] data = getRandomByteArray(PageBlobClient.PAGE_BYTES);
        PageRange pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1);
        pageBlobClient.uploadPages(pageRange, new ByteArrayInputStream(data));

        assertDoesNotThrow(() -> destURL.uploadPagesFromUrlWithResponse(pageRange, pageBlobClient.getBlobUrl(), null,
            MessageDigest.getInstance("MD5").digest(data), null, null, null, null));
    }

    @Test
    public void uploadPageFromURLMD5Fail() {
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);
        PageBlobClient destURL = cc.getBlobClient(generateBlobName()).getPageBlobClient();
        destURL.create(PageBlobClient.PAGE_BYTES);
        PageRange pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1);
        pageBlobClient.uploadPages(pageRange, new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)));

        assertThrows(BlobStorageException.class, () -> destURL.uploadPagesFromUrlWithResponse(pageRange,
            pageBlobClient.getBlobUrl(), null, MessageDigest.getInstance("MD5").digest("garbage".getBytes()), null, null, null,
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
        pageBlobClient.setTags(t);
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);
        PageBlobClient sourceURL = cc.getBlobClient(generateBlobName()).getPageBlobClient();
        sourceURL.create(PageBlobClient.PAGE_BYTES);
        PageRange pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1);
        sourceURL.uploadPages(pageRange, new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)));

        PageBlobRequestConditions pac = new PageBlobRequestConditions()
            .setLeaseId(setupBlobLeaseCondition(pageBlobClient, leaseID))
            .setIfMatch(setupBlobMatchCondition(pageBlobClient, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfSequenceNumberLessThan(sequenceNumberLT)
            .setIfSequenceNumberLessThanOrEqualTo(sequenceNumberLTE)
            .setIfSequenceNumberEqualTo(sequenceNumberEqual)
            .setTagsConditions(tags);

        assertResponseStatusCode(pageBlobClient.uploadPagesFromUrlWithResponse(pageRange, sourceURL.getBlobUrl(), null, null, pac,
            null, null, null), 201);
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
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);

        PageBlobClient sourceURL = cc.getBlobClient(generateBlobName()).getPageBlobClient();
        sourceURL.create(PageBlobClient.PAGE_BYTES);
        PageRange pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1);
        sourceURL.uploadPages(pageRange, new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)));

        noneMatch = setupBlobMatchCondition(pageBlobClient, noneMatch);
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

        assertThrows(BlobStorageException.class, () -> pageBlobClient.uploadPagesFromUrlWithResponse(
            pageRange, sourceURL.getBlobUrl(), null, null, pac, null, null, null));
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
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);
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

        assertResponseStatusCode(pageBlobClient.uploadPagesFromUrlWithResponse(pageRange, sourceURL.getBlobUrl(), null, null, null,
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
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);
        PageBlobClient sourceURL = cc.getBlobClient(generateBlobName()).getPageBlobClient();
        sourceURL.create(PageBlobClient.PAGE_BYTES);
        PageRange pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1);
        sourceURL.uploadPages(pageRange, new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)));

        BlobRequestConditions smac = new BlobRequestConditions()
            .setIfModifiedSince(sourceIfModifiedSince)
            .setIfUnmodifiedSince(sourceIfUnmodifiedSince)
            .setIfMatch(sourceIfMatch)
            .setIfNoneMatch(setupBlobMatchCondition(sourceURL, sourceIfNoneMatch));

        assertThrows(BlobStorageException.class, () -> pageBlobClient.uploadPagesFromUrlWithResponse(
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
    public void startIncrementalCopy() {
        setAccessPolicySleep(cc, PublicAccessType.BLOB, null);
        PageBlobClient bc2 = cc.getBlobClient(generateBlobName()).getPageBlobClient();
        String snapId = pageBlobClient.createSnapshot().getSnapshotId();

        Response<CopyStatusType> copyResponse = bc2.copyIncrementalWithResponse(pageBlobClient.getBlobUrl(), snapId, null, null,
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
        setAccessPolicySleep(cc, PublicAccessType.BLOB, null);
        PageBlobClient bc2 = cc.getBlobClient(generateBlobName()).getPageBlobClient();
        String snapshot = pageBlobClient.createSnapshot().getSnapshotId();

        assertResponseStatusCode(bc2.copyIncrementalWithResponse(pageBlobClient.getBlobUrl(), snapshot, null, null, null), 202);
    }

    //ServiceApiTests

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void sasSanitization(boolean unsanitize) {
        String identifier = "id with spaces";
        String blobName = generateBlobName();
        setAccessPolicySleep(cc, null,Collections.singletonList(new BlobSignedIdentifier()
            .setId(identifier)
            .setAccessPolicy(new BlobAccessPolicy()
                .setPermissions("racwdl")
                .setExpiresOn(testResourceNamer.now().plusDays(1)))));
        cc.getBlobClient(blobName).upload(BinaryData.fromBytes("test".getBytes()));
        String sas = cc.generateSas(new BlobServiceSasSignatureValues(identifier));
        if (unsanitize) {
            sas = sas.replace("%20", " ");
        }

        //

        // when: "Endpoint with SAS built in, works as expected"
        String finalSas = sas;
        assertDoesNotThrow(() -> instrument(new BlobContainerClientBuilder()
            .endpoint(cc.getBlobContainerUrl() + "?" + finalSas))
            .buildClient()
            .getBlobClient(blobName)
            .downloadContent());

        String connectionString = "AccountName=" + BlobUrlParts.parse(cc.getAccountUrl()).getAccountName()
            + ";SharedAccessSignature=" + sas;
        assertDoesNotThrow(() -> instrument(new BlobContainerClientBuilder()
            .connectionString(connectionString)
            .containerName(cc.getBlobContainerName()))
            .buildClient()
            .getBlobClient(blobName)
            .downloadContent());
    }

    //cpkn tests

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20201206ServiceVersion")
    @Test
    public void syncCopyEncryptionScope() {
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);
        BlobClient blobSource = cc.getBlobClient(generateBlobName());
        blobSource.upload(DATA.getDefaultBinaryData());

        cpknBlockBlob.copyFromUrlWithResponse(new BlobCopyFromUrlOptions(blobSource.getBlobUrl()), null, null);

        Assertions.assertEquals(scope1, cpknBlockBlob.getProperties().getEncryptionScope());
    }

    //sasClientTests

    @Test
    public void containerSasIdentifierAndPermissions() {
        BlobSignedIdentifier identifier = new BlobSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new BlobAccessPolicy().setPermissions("racwdl")
                .setExpiresOn(testResourceNamer.now().plusDays(1)));
        setAccessPolicySleep(cc, null, Arrays.asList(identifier));

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
        String sasWithId = cc.generateSas(sasValues);
        BlobContainerClient client1 = getContainerClient(sasWithId, cc.getBlobContainerUrl());
        assertDoesNotThrow(() -> client1.listBlobs().iterator().hasNext());

        sasValues = new BlobServiceSasSignatureValues(expiryTime, permissions);
        String sasWithPermissions = cc.generateSas(sasValues);
        BlobContainerClient client2 = getContainerClient(sasWithPermissions, cc.getBlobContainerUrl());
        assertDoesNotThrow(() -> client2.listBlobs().iterator().hasNext());
    }

    //versioned

    @Test
    public void copyFromUrlBlobsWithVersion() {
        blobContainerClient.setAccessPolicy(PublicAccessType.CONTAINER, null);
        BlockBlobItem blobItemV1 = blobVersionedClient.getBlockBlobClient().upload(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize());
        BlobClient sourceBlob = blobContainerClient.getBlobClient(generateBlobName());
        sourceBlob.getBlockBlobClient().upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());

        Response<String> response = blobVersionedClient.copyFromUrlWithResponse(sourceBlob.getBlobUrl(), null, null,
            null, null, null, Context.NONE);
        String versionIdAfterCopy = response.getHeaders().getValue(X_MS_VERSION_ID);

        assertNotNull(versionIdAfterCopy);
        assertNotEquals(blobItemV1.getVersionId(), versionIdAfterCopy);
    }

}
