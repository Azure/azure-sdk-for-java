// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.Response;
import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.blob.models.BlobContainerProperties;
import com.azure.storage.blob.models.BlobLeaseRequestConditions;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.LeaseDurationType;
import com.azure.storage.blob.models.LeaseStateType;
import com.azure.storage.blob.options.BlobAcquireLeaseOptions;
import com.azure.storage.blob.options.BlobBreakLeaseOptions;
import com.azure.storage.blob.options.BlobChangeLeaseOptions;
import com.azure.storage.blob.options.BlobReleaseLeaseOptions;
import com.azure.storage.blob.options.BlobRenewLeaseOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LeaseApiTests extends BlobTestBase {
    private BlobClientBase createBlobClient() {
        BlockBlobClient bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        bc.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        return bc;
    }

    @ParameterizedTest
    @MethodSource("acquireBlobLeaseSupplier")
    public void acquireBlobLease(String proposedID, int leaseTime, LeaseStateType leaseState,
        LeaseDurationType leaseDuration) {
        BlobClientBase bc = createBlobClient();
        BlobLeaseClient leaseClient = createLeaseClient(bc, proposedID);

        if (proposedID != null) {
            assertEquals(leaseClient.getLeaseId(), proposedID);
        }

        String leaseId = leaseClient.acquireLease(leaseTime);

        assertNotNull(leaseId);
        assertEquals(leaseClient.getLeaseId(), leaseId);

        Response<BlobProperties> response = bc.getPropertiesWithResponse(null, null, null);
        BlobProperties properties = response.getValue();
        HttpHeaders headers = response.getHeaders();

        assertEquals(leaseState, properties.getLeaseState());
        assertEquals(leaseDuration, properties.getLeaseDuration());
        assertTrue(validateBasicHeaders(headers));
    }

    private static Stream<Arguments> acquireBlobLeaseSupplier() {
        return Stream.of(
            Arguments.of(null, -1, LeaseStateType.LEASED, LeaseDurationType.INFINITE),
            Arguments.of(null, 25, LeaseStateType.LEASED, LeaseDurationType.FIXED),
            Arguments.of(UUID.randomUUID().toString(), -1, LeaseStateType.LEASED, LeaseDurationType.INFINITE)
        );
    }

    @Test
    public void acquireBlobLeaseMin() {
        assertResponseStatusCode(createLeaseClient(createBlobClient()).acquireLeaseWithResponse(
            new BlobAcquireLeaseOptions(-1), null, null), 201);
    }

    @ParameterizedTest
    @ValueSource(ints = { -10, 10, 70 })
    public void acquireBlobLeaseDurationFail(int duration) {
        BlobLeaseClient leaseClient = createLeaseClient(createBlobClient());
        assertThrows(BlobStorageException.class, () -> leaseClient.acquireLease(duration));
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("acquireBlobLeaseDurationFailSupplier")
    public void acquireBlobLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String tags) {
        BlobClientBase bc = createBlobClient();
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t);
        match = setupBlobMatchCondition(bc, match);
        BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setTagsConditions(tags);

        assertResponseStatusCode(createLeaseClient(bc).acquireLeaseWithResponse(
            new BlobAcquireLeaseOptions(-1).setRequestConditions(mac), null, null), 201);
    }

    private static Stream<Arguments> acquireBlobLeaseDurationFailSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null, null),
            Arguments.of(OLD_DATE, null, null, null, null),
            Arguments.of(null, NEW_DATE, null, null, null),
            Arguments.of(null, null, RECEIVED_ETAG, null, null),
            Arguments.of(null, null, null, GARBAGE_ETAG, null),
            Arguments.of(null, null, null, null, "\"foo\" = 'bar'"));
    }

    @ParameterizedTest
    @MethodSource("acquireBlobLeaseACFailSupplier")
    public void acquireBlobLeaseACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
        String noneMatch, String tags) {
        BlobClientBase bc = createBlobClient();
        noneMatch = setupBlobMatchCondition(bc, noneMatch);
        BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setTagsConditions(tags);

        assertThrows(BlobStorageException.class, () -> createLeaseClient(bc).acquireLeaseWithResponse(
            new BlobAcquireLeaseOptions(-1).setRequestConditions(mac), null, null));
    }

    private static Stream<Arguments> acquireBlobLeaseACFailSupplier() {
        return Stream.of(
            Arguments.of(NEW_DATE, null, null, null, null),
            Arguments.of(null, OLD_DATE, null, null, null),
            Arguments.of(null, null, GARBAGE_ETAG, null, null),
            Arguments.of(null, null, null, RECEIVED_ETAG, null),
            Arguments.of(null, null, null, null, "\"notfoo\" = 'notbar'"));
    }

    @Test
    public void acquireBlobLeaseError() {
        BlockBlobClient bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        assertThrows(BlobStorageException.class, () -> createLeaseClient(bc).acquireLease(20));
    }

    @Test
    public void renewBlobLease() {
        BlobClientBase bc = createBlobClient();
        String leaseID = setupBlobLeaseCondition(bc, RECEIVED_LEASE_ID);
        BlobLeaseClient leaseClient = createLeaseClient(bc, leaseID);

        // If running in live mode wait for the lease to expire to ensure we are actually renewing it
        sleepIfRunningAgainstService(16000);
        Response<String> renewLeaseResponse = leaseClient.renewLeaseWithResponse(
            new BlobRenewLeaseOptions(), null, null);

        assertEquals(bc.getProperties().getLeaseState(), LeaseStateType.LEASED);
        validateBasicHeaders(renewLeaseResponse.getHeaders());
        assertNotNull(renewLeaseResponse.getValue());
        assertEquals(renewLeaseResponse.getValue(), leaseClient.getLeaseId());
    }

    @Test
    public void renewBlobLeaseMin() {
        BlobClientBase bc = createBlobClient();
        String leaseID = setupBlobLeaseCondition(bc, RECEIVED_LEASE_ID);

        assertResponseStatusCode(createLeaseClient(bc, leaseID)
            .renewLeaseWithResponse(new BlobRenewLeaseOptions(), null, null), 200);
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("acquireBlobLeaseDurationFailSupplier")
    public void renewBlobLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String tags) {
        BlobClientBase bc = createBlobClient();
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t);
        match = setupBlobMatchCondition(bc, match);
        String leaseID = setupBlobLeaseCondition(bc, RECEIVED_LEASE_ID);
        BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setTagsConditions(tags);

        assertResponseStatusCode(createLeaseClient(bc, leaseID)
            .renewLeaseWithResponse(new BlobRenewLeaseOptions().setRequestConditions(mac), null, null), 200);
    }

    @ParameterizedTest
    @MethodSource("acquireBlobLeaseACFailSupplier")
    public void renewBlobLeaseACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String tags) {
        BlobClientBase bc = createBlobClient();
        noneMatch = setupBlobMatchCondition(bc, noneMatch);
        String leaseID = setupBlobLeaseCondition(bc, RECEIVED_LEASE_ID);
        BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setTagsConditions(tags);

        assertThrows(BlobStorageException.class, () -> createLeaseClient(bc, leaseID).renewLeaseWithResponse(
            new BlobRenewLeaseOptions().setRequestConditions(mac), null, null));
    }

    @Test
    public void renewBlobLeaseError() {
        BlockBlobClient bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        assertThrows(BlobStorageException.class, () -> createLeaseClient(bc, "id").renewLease());
    }

    @Test
    public void releaseBlobLease() {
        BlobClientBase bc = createBlobClient();
        String leaseID = setupBlobLeaseCondition(bc, RECEIVED_LEASE_ID);
        HttpHeaders headers = createLeaseClient(bc, leaseID)
            .releaseLeaseWithResponse(new BlobReleaseLeaseOptions(), null, null).getHeaders();

        assertEquals(bc.getProperties().getLeaseState(), LeaseStateType.AVAILABLE);
        assertTrue(validateBasicHeaders(headers));
    }

    @Test
    public void releaseBlobLeaseMin() {
        BlobClientBase bc = createBlobClient();
        String leaseID = setupBlobLeaseCondition(bc, RECEIVED_LEASE_ID);
        assertResponseStatusCode(createLeaseClient(bc, leaseID).releaseLeaseWithResponse(
            new BlobReleaseLeaseOptions(), null, null), 200);
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("acquireBlobLeaseDurationFailSupplier")
    public void releaseBlobLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String tags) {
        BlobClientBase bc = createBlobClient();
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t);
        match = setupBlobMatchCondition(bc, match);
        String leaseID = setupBlobLeaseCondition(bc, RECEIVED_LEASE_ID);
        BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setTagsConditions(tags);

        assertResponseStatusCode(createLeaseClient(bc, leaseID).releaseLeaseWithResponse(
            new BlobReleaseLeaseOptions().setRequestConditions(mac), null, null), 200);
    }

    @ParameterizedTest
    @MethodSource("acquireBlobLeaseACFailSupplier")
    public void releaseBlobLeaseACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String tags) {
        BlobClientBase bc = createBlobClient();
        noneMatch = setupBlobMatchCondition(bc, noneMatch);
        String leaseID = setupBlobLeaseCondition(bc, RECEIVED_LEASE_ID);
        BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setTagsConditions(tags);

        assertThrows(BlobStorageException.class, () -> createLeaseClient(bc, leaseID)
            .releaseLeaseWithResponse(new BlobReleaseLeaseOptions().setRequestConditions(mac), null, null));
    }

    @Test
    public void releaseBlobLeaseError() {
        BlockBlobClient bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        assertThrows(BlobStorageException.class, () -> createLeaseClient(bc, "id").releaseLease());
    }

    @ParameterizedTest
    @MethodSource("breakBlobLeaseSupplier")
    public void breakBlobLease(int leaseTime, Integer breakPeriod, int remainingTime) {
        BlobClientBase bc = createBlobClient();
        BlobLeaseClient leaseClient = createLeaseClient(bc, testResourceNamer.randomUuid());
        leaseClient.acquireLease(leaseTime);
        Response<Integer> breakLeaseResponse = leaseClient.breakLeaseWithResponse(new BlobBreakLeaseOptions()
            .setBreakPeriod(breakPeriod == null ? null : Duration.ofSeconds(breakPeriod)), null, null);
        LeaseStateType leaseState = bc.getProperties().getLeaseState();

        assertTrue(leaseState == LeaseStateType.BROKEN || leaseState == LeaseStateType.BREAKING);
        assertTrue(breakLeaseResponse.getValue() <= remainingTime);
        assertTrue(validateBasicHeaders(breakLeaseResponse.getHeaders()));
    }

    private static Stream<Arguments> breakBlobLeaseSupplier() {
        return Stream.of(
            Arguments.of(-1, null, 0),
            Arguments.of(-1, 20, 25),
            Arguments.of(20, 15, 16));
    }

    @Test
    public void breakBlobLeaseMin() {
        BlobClientBase bc = createBlobClient();
        setupBlobLeaseCondition(bc, RECEIVED_LEASE_ID);

        assertResponseStatusCode(createLeaseClient(bc).breakLeaseWithResponse(
            new BlobBreakLeaseOptions(), null, null), 202);
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("acquireBlobLeaseDurationFailSupplier")
    public void breakBlobLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String tags) {
        BlobClientBase bc = createBlobClient();
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t);
        match = setupBlobMatchCondition(bc, match);
        setupBlobLeaseCondition(bc, RECEIVED_ETAG);
        BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setTagsConditions(tags);

        assertResponseStatusCode(createLeaseClient(bc).breakLeaseWithResponse(
            new BlobBreakLeaseOptions().setRequestConditions(mac), null, null), 202);
    }

    @ParameterizedTest
    @MethodSource("acquireBlobLeaseACFailSupplier")
    public void breakBlobLeaseACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String tags) {
        BlobClientBase bc = createBlobClient();
        noneMatch = setupBlobMatchCondition(bc, noneMatch);
        setupBlobLeaseCondition(bc, RECEIVED_LEASE_ID);
        BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setTagsConditions(tags);

        assertThrows(BlobStorageException.class, () -> createLeaseClient(bc)
            .breakLeaseWithResponse(new BlobBreakLeaseOptions().setRequestConditions(mac), null, null));
    }

    @Test
    public void breakBlobLeaseError() {
        BlobClientBase bc = createBlobClient();
        assertThrows(BlobStorageException.class, () -> createLeaseClient(bc).breakLease());
    }

    @Test
    public void changeBlobLease() {
        BlobClientBase bc = createBlobClient();
        BlobLeaseClient leaseClient = createLeaseClient(bc, testResourceNamer.randomUuid());
        leaseClient.acquireLease(15);

        String newLeaseId = testResourceNamer.randomUuid();
        Response<String> changeLeaseResponse = leaseClient.changeLeaseWithResponse(
            new BlobChangeLeaseOptions(newLeaseId), null, null);

        assertEquals(changeLeaseResponse.getValue(), newLeaseId);
        assertEquals(changeLeaseResponse.getValue(), leaseClient.getLeaseId());

        BlobLeaseClient leaseClient2 = createLeaseClient(bc, changeLeaseResponse.getValue());

        assertResponseStatusCode(leaseClient2.releaseLeaseWithResponse(new BlobReleaseLeaseOptions(), null, null), 200);
        assertTrue(validateBasicHeaders(changeLeaseResponse.getHeaders()));
    }

    @Test
    public void changeBlobLeaseMin() {
        BlobClientBase bc = createBlobClient();
        String leaseID = setupBlobLeaseCondition(bc, RECEIVED_LEASE_ID);
        assertResponseStatusCode(createLeaseClient(bc, leaseID).changeLeaseWithResponse(
            new BlobChangeLeaseOptions(testResourceNamer.randomUuid()), null, null), 200);
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("acquireBlobLeaseDurationFailSupplier")
    public void changeBlobLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String tags) {
        BlobClientBase bc = createBlobClient();
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t);
        match = setupBlobMatchCondition(bc, match);
        String leaseID = setupBlobLeaseCondition(bc, RECEIVED_LEASE_ID);
        BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setTagsConditions(tags);

        assertResponseStatusCode(createLeaseClient(bc, leaseID).changeLeaseWithResponse(
            new BlobChangeLeaseOptions(testResourceNamer.randomUuid()).setRequestConditions(mac), null, null), 200);
    }

    @ParameterizedTest
    @MethodSource("acquireBlobLeaseACFailSupplier")
    public void changeBlobLeaseACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
        String noneMatch, String tags) {
        BlobClientBase bc = createBlobClient();
        noneMatch = setupBlobMatchCondition(bc, noneMatch);
        String leaseID = setupBlobLeaseCondition(bc, RECEIVED_LEASE_ID);
        BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setTagsConditions(tags);

        assertThrows(BlobStorageException.class, () -> createLeaseClient(bc, leaseID).changeLeaseWithResponse(
            new BlobChangeLeaseOptions(testResourceNamer.randomUuid()).setRequestConditions(mac), null, null));
    }

    @Test
    public void changeBlobLeaseError() {
        BlockBlobClient bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        assertThrows(BlobStorageException.class, () -> createLeaseClient(bc, "id").changeLease("id"));
    }


    @ParameterizedTest
    @MethodSource("acquireContainerLeaseSupplier")
    public void acquireContainerLease(String proposedID, int leaseTime, LeaseStateType leaseState,
        LeaseDurationType leaseDuration) {
        BlobLeaseClient leaseClient = createLeaseClient(cc, proposedID);

        Response<String> leaseResponse = leaseClient.acquireLeaseWithResponse(new BlobAcquireLeaseOptions(leaseTime),
            null, null);

        assertEquals(leaseResponse.getValue(), leaseClient.getLeaseId());

        BlobContainerProperties properties = cc.getProperties();

        assertNotNull(leaseResponse.getValue());
        assertTrue(validateBasicHeaders(leaseResponse.getHeaders()));
        assertEquals(properties.getLeaseState(), leaseState);
        assertEquals(properties.getLeaseDuration(), leaseDuration);
    }

    private static Stream<Arguments> acquireContainerLeaseSupplier() {
        return Stream.of(
            Arguments.of(null, -1, LeaseStateType.LEASED, LeaseDurationType.INFINITE),
            Arguments.of(null, 25, LeaseStateType.LEASED, LeaseDurationType.FIXED),
            Arguments.of(UUID.randomUUID().toString(), -1, LeaseStateType.LEASED, LeaseDurationType.INFINITE)
        );
    }

    @Test
    public void acquireContainerLeaseMin() {
        assertResponseStatusCode(createLeaseClient(cc).acquireLeaseWithResponse(
            new BlobAcquireLeaseOptions(-1), null, null), 201);
    }

    @ParameterizedTest
    @ValueSource(ints = { -10, 10, 70 })
    public void acquireContainerLeaseDurationFail(int duration) {
        BlobLeaseClient leaseClient = createLeaseClient(cc);
        assertThrows(BlobStorageException.class, () -> leaseClient.acquireLease(duration));
    }

    @ParameterizedTest
    @MethodSource("acquireContainerLeaseACSupplier")
    public void acquireContainerLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified, String match,
        String noneMatch) {
        BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch);

        assertResponseStatusCode(createLeaseClient(cc).acquireLeaseWithResponse(
            new BlobAcquireLeaseOptions(-1).setRequestConditions(mac), null, null), 201);
    }

    private static Stream<Arguments> acquireContainerLeaseACSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null),
            Arguments.of(OLD_DATE, null, null, null),
            Arguments.of(null, NEW_DATE, null, null),
            Arguments.of(null, null, RECEIVED_ETAG, null),
            Arguments.of(null, null, null, GARBAGE_ETAG));
    }

    @ParameterizedTest
    @MethodSource("acquireContainerLeaseACFailSupplier")
    public void acquireContainerLeaseACFail(OffsetDateTime modified, OffsetDateTime unmodified) {
        BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertThrows(BlobStorageException.class, () -> createLeaseClient(cc).acquireLeaseWithResponse(
            new BlobAcquireLeaseOptions(-1).setRequestConditions(mac), null, null));
    }

    private static Stream<Arguments> acquireContainerLeaseACFailSupplier() {
        return Stream.of(
            Arguments.of(NEW_DATE, null),
            Arguments.of(null, OLD_DATE));
    }

    @Test
    public void acquireContainerLeaseError() {
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName());
        assertThrows(BlobStorageException.class, () -> createLeaseClient(cc).acquireLease(50));
    }

    @Test
    public void renewContainerLease() {
        String leaseID = setupContainerLeaseCondition(cc, RECEIVED_LEASE_ID);
        BlobLeaseClient leaseClient = createLeaseClient(cc, leaseID);

        // If running in live mode wait for the lease to expire to ensure we are actually renewing it
        sleepIfRunningAgainstService(16000);
        Response<String> renewLeaseResponse = leaseClient.renewLeaseWithResponse(new BlobRenewLeaseOptions(), null, null);

        assertEquals(renewLeaseResponse.getValue(), leaseClient.getLeaseId());
        assertEquals(cc.getProperties().getLeaseState(), LeaseStateType.LEASED);
        assertTrue(validateBasicHeaders(renewLeaseResponse.getHeaders()));
    }

    @Test
    public void renewContainerLeaseMin() {
        String leaseID = setupContainerLeaseCondition(cc, RECEIVED_LEASE_ID);
        assertResponseStatusCode(createLeaseClient(cc, leaseID).renewLeaseWithResponse(
            new BlobRenewLeaseOptions(), null, null), 200);
    }

    @ParameterizedTest
    @MethodSource("renewContainerLeaseACSupplier")
    public void renewContainerLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified) {
        String leaseID = setupContainerLeaseCondition(cc, RECEIVED_LEASE_ID);
        BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertResponseStatusCode(createLeaseClient(cc, leaseID).renewLeaseWithResponse(
            new BlobRenewLeaseOptions().setRequestConditions(mac), null, null), 200);
    }

    private static Stream<Arguments> renewContainerLeaseACSupplier() {
        return Stream.of(
            Arguments.of(OLD_DATE, null),
            Arguments.of(null, NEW_DATE));
    }

    @ParameterizedTest
    @MethodSource("acquireContainerLeaseACFailSupplier")
    public void renewContainerLeaseACFail(OffsetDateTime modified, OffsetDateTime unmodified) {
        String leaseID = setupContainerLeaseCondition(cc, RECEIVED_LEASE_ID);
        BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertThrows(BlobStorageException.class, () -> createLeaseClient(cc, leaseID).renewLeaseWithResponse(
            new BlobRenewLeaseOptions().setRequestConditions(mac), null, null));
    }

    @ParameterizedTest
    @MethodSource("renewContainerLeaseACIllegalSupplier")
    public void renewContainerLeaseACIllegal(String match, String noneMatch) {
        BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch);
        assertThrows(BlobStorageException.class, () -> createLeaseClient(cc, RECEIVED_ETAG)
            .renewLeaseWithResponse(new BlobRenewLeaseOptions().setRequestConditions(mac), null, null));
    }

    private static Stream<Arguments> renewContainerLeaseACIllegalSupplier() {
        return Stream.of(Arguments.of(RECEIVED_ETAG, null), Arguments.of(null, GARBAGE_ETAG));
    }

    @Test
    public void renewContainerLeaseError() {
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName());
        assertThrows(BlobStorageException.class, () -> createLeaseClient(cc, "id").renewLease());
    }

    @Test
    public void releaseContainerLease() {
        String leaseID = setupContainerLeaseCondition(cc, RECEIVED_LEASE_ID);
        Response<Void> releaseLeaseResponse = createLeaseClient(cc, leaseID).releaseLeaseWithResponse(
            new BlobReleaseLeaseOptions(), null, null);

        assertEquals(cc.getProperties().getLeaseState(), LeaseStateType.AVAILABLE);
        assertTrue(validateBasicHeaders(releaseLeaseResponse.getHeaders()));
    }

    @Test
    public void releaseContainerLeaseMin() {
        String leaseID = setupContainerLeaseCondition(cc, RECEIVED_LEASE_ID);

        assertResponseStatusCode(createLeaseClient(cc, leaseID)
            .releaseLeaseWithResponse(new BlobReleaseLeaseOptions(), null, null), 200);
    }

    @ParameterizedTest
    @MethodSource("renewContainerLeaseACSupplier")
    public void releaseContainerLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified) {
        String leaseID = setupContainerLeaseCondition(cc, RECEIVED_LEASE_ID);
        BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertResponseStatusCode(createLeaseClient(cc, leaseID).releaseLeaseWithResponse(
            new BlobReleaseLeaseOptions().setRequestConditions(mac), null, null), 200);
    }

    @ParameterizedTest
    @MethodSource("acquireContainerLeaseACFailSupplier")
    public void releaseContainerLeaseACFail(OffsetDateTime modified, OffsetDateTime unmodified) {
        String leaseID = setupContainerLeaseCondition(cc, RECEIVED_LEASE_ID);
        BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertThrows(BlobStorageException.class, () -> createLeaseClient(cc, leaseID).releaseLeaseWithResponse(
            new BlobReleaseLeaseOptions().setRequestConditions(mac), null, null));
    }

    @ParameterizedTest
    @MethodSource("renewContainerLeaseACIllegalSupplier")
    public void releaseContainerLeaseACIllegal(String match, String noneMatch) {
        BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch);

        assertThrows(BlobStorageException.class, () -> createLeaseClient(cc, RECEIVED_LEASE_ID)
            .releaseLeaseWithResponse(new BlobReleaseLeaseOptions().setRequestConditions(mac), null, null));
    }

    @Test
    public void releaseContainerLeaseError() {
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName());
        assertThrows(BlobStorageException.class, () -> createLeaseClient(cc, "id").releaseLease());
    }

    @ParameterizedTest
    @MethodSource("breakBlobLeaseSupplier")
    public void breakContainerLease(int leaseTime, Integer breakPeriod, int remainingTime) {
        BlobLeaseClient leaseClient = createLeaseClient(cc, testResourceNamer.randomUuid());
        leaseClient.acquireLease(leaseTime);

        Response<Integer> breakLeaseResponse = leaseClient.breakLeaseWithResponse(
            new BlobBreakLeaseOptions().setBreakPeriod(breakPeriod == null ? null : Duration.ofSeconds(breakPeriod)),
            null, null);
        LeaseStateType state = cc.getProperties().getLeaseState();

        assertTrue(state == LeaseStateType.BROKEN || state == LeaseStateType.BREAKING);
        assertTrue(breakLeaseResponse.getValue() <= remainingTime);
        assertTrue(validateBasicHeaders(breakLeaseResponse.getHeaders()));
        if (breakPeriod != null) {
            // If running in live mode wait for the lease to break so we can delete the container after the test completes
            sleepIfRunningAgainstService(breakPeriod * 1000);
        }
    }

    @Test
    public void breakContainerLeaseMin() {
        setupContainerLeaseCondition(cc, RECEIVED_LEASE_ID);
        assertResponseStatusCode(createLeaseClient(cc).breakLeaseWithResponse(new BlobBreakLeaseOptions(),
            null, null), 202);
    }

    @ParameterizedTest
    @MethodSource("renewContainerLeaseACSupplier")
    public void breakContainerLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified) {
        setupContainerLeaseCondition(cc, RECEIVED_LEASE_ID);
        BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertResponseStatusCode(createLeaseClient(cc).breakLeaseWithResponse(new BlobBreakLeaseOptions()
            .setRequestConditions(mac), null, null), 202);
    }

    @ParameterizedTest
    @MethodSource("acquireContainerLeaseACFailSupplier")
    public void breakContainerLeaseACFail(OffsetDateTime modified, OffsetDateTime unmodified) {
        setupContainerLeaseCondition(cc, RECEIVED_LEASE_ID);
        BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertThrows(BlobStorageException.class, () -> createLeaseClient(cc).breakLeaseWithResponse(
            new BlobBreakLeaseOptions().setRequestConditions(mac), null, null));
    }

    @ParameterizedTest
    @MethodSource("renewContainerLeaseACIllegalSupplier")
    public void breakContainerLeaseACIllegal(String match, String noneMatch) {
        BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch);

        assertThrows(BlobStorageException.class, () -> createLeaseClient(cc).breakLeaseWithResponse(
            new BlobBreakLeaseOptions().setRequestConditions(mac), null, null));
    }

    @Test
    public void breakContainerLeaseError() {
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName());
        assertThrows(BlobStorageException.class, () -> createLeaseClient(cc).breakLease());
    }

    @Test
    public void changeContainerLease() {
        String leaseID = setupContainerLeaseCondition(cc, RECEIVED_LEASE_ID);
        BlobLeaseClient leaseClient = createLeaseClient(cc, leaseID);

        assertEquals(leaseClient.getLeaseId(), leaseID);
        Response<String> changeLeaseResponse = leaseClient.changeLeaseWithResponse(
            new BlobChangeLeaseOptions(testResourceNamer.randomUuid()), null, null);

        assertTrue(validateBasicHeaders(changeLeaseResponse.getHeaders()));
        String newLeaseId = changeLeaseResponse.getValue();
        assertEquals(newLeaseId, leaseClient.getLeaseId());
        assertNotEquals(newLeaseId, leaseID);

        assertResponseStatusCode(createLeaseClient(cc, newLeaseId).releaseLeaseWithResponse(
            new BlobReleaseLeaseOptions(), null, null), 200);
    }

    @Test
    public void changeContainerLeaseMin() {
        String leaseID = setupContainerLeaseCondition(cc, RECEIVED_LEASE_ID);

        assertResponseStatusCode(createLeaseClient(cc, leaseID).changeLeaseWithResponse(
            new BlobChangeLeaseOptions(testResourceNamer.randomUuid()), null, null), 200);
    }

    @ParameterizedTest
    @MethodSource("renewContainerLeaseACSupplier")
    public void changeContainerLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified) {
        String leaseID = setupContainerLeaseCondition(cc, RECEIVED_LEASE_ID);
        BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertResponseStatusCode(createLeaseClient(cc, leaseID).changeLeaseWithResponse(new BlobChangeLeaseOptions(
            testResourceNamer.randomUuid()).setRequestConditions(mac), null, null), 200);
    }

    @ParameterizedTest
    @MethodSource("acquireContainerLeaseACFailSupplier")
    public void changeContainerLeaseACFail(OffsetDateTime modified, OffsetDateTime unmodified) {
        String leaseID = setupContainerLeaseCondition(cc, RECEIVED_LEASE_ID);
        BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertThrows(BlobStorageException.class, () -> createLeaseClient(cc, leaseID).changeLeaseWithResponse(
            new BlobChangeLeaseOptions(testResourceNamer.randomUuid()).setRequestConditions(mac), null, null));

    }

    @ParameterizedTest
    @MethodSource("renewContainerLeaseACIllegalSupplier")
    public void changeContainerLeaseACIllegal(String match, String noneMatch) {
        BlobLeaseRequestConditions mac = new BlobLeaseRequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch);

        assertThrows(BlobStorageException.class, () -> createLeaseClient(cc, RECEIVED_LEASE_ID)
                .changeLeaseWithResponse(new BlobChangeLeaseOptions(GARBAGE_LEASE_ID).setRequestConditions(mac),
                    null, null));
    }

    @Test
    public void changeContainerLeaseError() {
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName());
        assertThrows(BlobStorageException.class, () -> createLeaseClient(cc, "id").changeLease("id"));
    }
}
