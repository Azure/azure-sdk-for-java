// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.datalake.specialized;

import com.azure.core.http.RequestConditions;
import com.azure.core.util.CoreUtils;
import com.azure.storage.file.datalake.DataLakeFileAsyncClient;
import com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient;
import com.azure.storage.file.datalake.DataLakeTestBase;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import com.azure.storage.file.datalake.models.LeaseDurationType;
import com.azure.storage.file.datalake.models.LeaseStateType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LeaseAsyncApiTests  extends DataLakeTestBase {
    private DataLakeFileAsyncClient createPathClient() {
        DataLakeFileAsyncClient fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());
        fc.create().block();
        return fc;
    }

    @ParameterizedTest
    @MethodSource("acquireLeaseSupplier")
    public void acquireFileLease(String proposedId, int leaseTime, LeaseStateType leaseStateType,
                                 LeaseDurationType leaseDurationType) {
        DataLakeFileAsyncClient fc = createPathClient();
        DataLakeLeaseAsyncClient leaseClient = createLeaseAsyncClient(fc, proposedId);

        StepVerifier.create(leaseClient.acquireLease(leaseTime))
            .assertNext(r -> assertEquals(r, leaseClient.getLeaseId()))
            .verifyComplete();

        StepVerifier.create(fc.getPropertiesWithResponse(null))
            .assertNext(r -> {
                assertEquals(leaseStateType, r.getValue().getLeaseState());
                assertEquals(leaseDurationType, r.getValue().getLeaseDuration());
                validateBasicHeaders(r.getHeaders());
            })
            .verifyComplete();
    }

    private static Stream<Arguments> acquireLeaseSupplier() {
        return Stream.of(
            // proposedId | leaseTime | leaseStateType | leaseDurationType
            Arguments.of(null, -1, LeaseStateType.LEASED, LeaseDurationType.INFINITE),
            Arguments.of(null, 25, LeaseStateType.LEASED, LeaseDurationType.FIXED),
            Arguments.of(CoreUtils.randomUuid().toString(), -1, LeaseStateType.LEASED, LeaseDurationType.INFINITE)
        );
    }

    @Test
    public void acquireFileLeaseMin() {
        assertAsyncResponseStatusCode(createLeaseAsyncClient(createPathClient())
            .acquireLeaseWithResponse(-1, null), 201);
    }

    @ParameterizedTest
    @ValueSource(ints = { -10, 10, 70 })
    public void acquireFileLeaseDurationFail(int duration) {
        StepVerifier.create(createLeaseAsyncClient(createPathClient()).acquireLease(duration))
            .verifyError(DataLakeStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("validLeaseConditions")
    public void acquireFileLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch) {
        DataLakeFileAsyncClient fc = createPathClient();
        match = setupPathMatchCondition(fc, match);
        RequestConditions mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch);

        assertAsyncResponseStatusCode(createLeaseAsyncClient(fc)
            .acquireLeaseWithResponse(-1, mac), 201);
    }

    private static Stream<Arguments> validLeaseConditions() {
        return Stream.of(
            // modified | unmodified | match | noneMatch
            Arguments.of(null, null, null, null),
            Arguments.of(OLD_DATE, null, null, null),
            Arguments.of(null, NEW_DATE, null, null),
            Arguments.of(null, null, RECEIVED_ETAG, null),
            Arguments.of(null, null, null, GARBAGE_ETAG)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidLeaseConditions")
    public void acquireFileLeaseACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
                                       String noneMatch) {
        DataLakeFileAsyncClient fc = createPathClient();
        noneMatch = setupPathMatchCondition(fc, noneMatch);
        RequestConditions mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch);

        StepVerifier.create(createLeaseAsyncClient(fc).acquireLeaseWithResponse(-1, mac))
            .verifyError(DataLakeStorageException.class);
    }

    private static Stream<Arguments> invalidLeaseConditions() {
        return Stream.of(
            // modified | unmodified | match | noneMatch
            Arguments.of(NEW_DATE, null, null, null),
            Arguments.of(null, OLD_DATE, null, null),
            Arguments.of(null, null, GARBAGE_ETAG, null),
            Arguments.of(null, null, null, RECEIVED_ETAG));
    }

    @Test
    public void acquireFileLeaseError() {
        DataLakeFileAsyncClient fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        StepVerifier.create(createLeaseAsyncClient(fc).acquireLease(20))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void renewFileLease() {
        DataLakeFileAsyncClient fc = createPathClient();
        String leaseId = setupPathLeaseCondition(fc, RECEIVED_LEASE_ID);
        DataLakeLeaseAsyncClient leaseClient = createLeaseAsyncClient(fc, leaseId);

        StepVerifier.create(leaseClient.renewLeaseWithResponse(null))
            .assertNext(r -> {
                validateBasicHeaders(r.getHeaders());
                assertEquals(leaseClient.getLeaseId(), r.getValue());
            })
            .verifyComplete();

        StepVerifier.create(fc.getProperties())
            .assertNext(p -> assertEquals(LeaseStateType.LEASED, p.getLeaseState()))
            .verifyComplete();
    }

    @Test
    public void renewFileLeaseMin() {
        DataLakeFileAsyncClient fc = createPathClient();
        String leaseId = setupPathLeaseCondition(fc, RECEIVED_LEASE_ID);

        assertAsyncResponseStatusCode(createLeaseAsyncClient(fc, leaseId)
            .renewLeaseWithResponse(null), 200);
    }

    @ParameterizedTest
    @MethodSource("validLeaseConditions")
    public void renewFileLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch) {
        DataLakeFileAsyncClient fc = createPathClient();
        match = setupPathMatchCondition(fc, match);
        String leaseId = setupPathLeaseCondition(fc, RECEIVED_LEASE_ID);
        RequestConditions mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch);

        assertAsyncResponseStatusCode(createLeaseAsyncClient(fc, leaseId).renewLeaseWithResponse(mac), 200);
    }

    @ParameterizedTest
    @MethodSource("invalidLeaseConditions")
    public void renewFileLeaseACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
                                     String noneMatch) {
        DataLakeFileAsyncClient fc = createPathClient();
        String leaseId = setupPathLeaseCondition(fc, RECEIVED_LEASE_ID);
        noneMatch = setupPathMatchCondition(fc, noneMatch);
        RequestConditions mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch);

        StepVerifier.create(createLeaseAsyncClient(fc, leaseId).renewLeaseWithResponse(mac))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void renewFileLeaseError() {
        DataLakeFileAsyncClient fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        StepVerifier.create(createLeaseAsyncClient(fc, "id").renewLease())
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void releaseFileLease() {
        DataLakeFileAsyncClient fc = createPathClient();
        String leaseId = setupPathLeaseCondition(fc, RECEIVED_LEASE_ID);

        StepVerifier.create(createLeaseAsyncClient(fc, leaseId).releaseLeaseWithResponse(null))
            .assertNext(r -> validateBasicHeaders(r.getHeaders()))
            .verifyComplete();

        StepVerifier.create(fc.getProperties())
            .assertNext(p -> assertEquals(LeaseStateType.AVAILABLE, p.getLeaseState()))
            .verifyComplete();
    }

    @Test
    public void releaseFileLeaseMin() {
        DataLakeFileAsyncClient fc = createPathClient();
        String leaseId = setupPathLeaseCondition(fc, RECEIVED_LEASE_ID);

        assertAsyncResponseStatusCode(createLeaseAsyncClient(fc, leaseId)
            .releaseLeaseWithResponse(null), 200);
    }

    @ParameterizedTest
    @MethodSource("validLeaseConditions")
    public void releaseFileLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch) {
        DataLakeFileAsyncClient fc = createPathClient();
        match = setupPathMatchCondition(fc, match);
        String leaseId = setupPathLeaseCondition(fc, RECEIVED_LEASE_ID);
        RequestConditions mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch);

        assertAsyncResponseStatusCode(createLeaseAsyncClient(fc, leaseId).releaseLeaseWithResponse(mac), 200);
    }

    @ParameterizedTest
    @MethodSource("invalidLeaseConditions")
    public void releaseFileLeaseACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
                                       String noneMatch) {
        DataLakeFileAsyncClient fc = createPathClient();
        String leaseId = setupPathLeaseCondition(fc, RECEIVED_LEASE_ID);
        noneMatch = setupPathMatchCondition(fc, noneMatch);
        RequestConditions mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch);

        StepVerifier.create(createLeaseAsyncClient(fc, leaseId).releaseLeaseWithResponse(mac))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void releaseFileLeaseError() {
        DataLakeFileAsyncClient fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        StepVerifier.create(createLeaseAsyncClient(fc, "id").releaseLease())
            .verifyError(DataLakeStorageException.class);
    }

    @ParameterizedTest
    @CsvSource(value = {"-1,null,0", "-1,20,25", "20,15,16"}, nullValues = "null")
    public void breakFileLease(int leaseTime, Integer breakPeriod, int remainingTime) {
        DataLakeFileAsyncClient fc = createPathClient();
        DataLakeLeaseAsyncClient leaseClient = createLeaseAsyncClient(fc, testResourceNamer.randomUuid());

        leaseClient.acquireLease(leaseTime).block();

        StepVerifier.create(leaseClient.breakLeaseWithResponse(breakPeriod, null))
            .assertNext(r -> {
                assertTrue(r.getValue() <= remainingTime);
                validateBasicHeaders(r.getHeaders());
            })
            .verifyComplete();

        StepVerifier.create(fc.getProperties())
            .assertNext(p -> assertTrue(p.getLeaseState() == LeaseStateType.BROKEN
                || p.getLeaseState() == LeaseStateType.BREAKING))
            .verifyComplete();
    }

    @Test
    public void breakFileLeaseMin() {
        DataLakeFileAsyncClient fc = createPathClient();
        setupPathLeaseCondition(fc, RECEIVED_LEASE_ID);

        assertAsyncResponseStatusCode(createLeaseAsyncClient(fc)
            .breakLeaseWithResponse(null, null), 202);
    }

    @ParameterizedTest
    @MethodSource("validLeaseConditions")
    public void breakFileLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch) {
        DataLakeFileAsyncClient fc = createPathClient();
        match = setupPathMatchCondition(fc, match);
        setupPathLeaseCondition(fc, RECEIVED_LEASE_ID);
        RequestConditions mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch);

        assertAsyncResponseStatusCode(createLeaseAsyncClient(fc)
            .breakLeaseWithResponse(null, mac), 202);
    }

    @ParameterizedTest
    @MethodSource("invalidLeaseConditions")
    public void breakFileLeaseACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
                                     String noneMatch) {
        DataLakeFileAsyncClient fc = createPathClient();
        setupPathLeaseCondition(fc, RECEIVED_LEASE_ID);
        noneMatch = setupPathMatchCondition(fc, noneMatch);
        RequestConditions mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch);

        StepVerifier.create(createLeaseAsyncClient(fc).breakLeaseWithResponse(null, mac))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void breakFileLeaseError() {
        DataLakeFileAsyncClient fc = createPathClient();

        StepVerifier.create(createLeaseAsyncClient(fc).breakLease())
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void changeFileLease() {
        DataLakeFileAsyncClient fc = createPathClient();
        DataLakeLeaseAsyncClient leaseClient = createLeaseAsyncClient(fc, testResourceNamer.randomUuid());
        leaseClient.acquireLease(15).block();

        String newLeaseId = testResourceNamer.randomUuid();
        StepVerifier.create(leaseClient.changeLeaseWithResponse(newLeaseId, null)
            .flatMap(r -> {
                validateBasicHeaders(r.getHeaders());
                assertEquals(leaseClient.getLeaseId(), r.getValue());
                return createLeaseAsyncClient(fc, r.getValue()).releaseLeaseWithResponse(null);
            }))
            .assertNext(r -> {
                assertEquals(200, r.getStatusCode());
            })
            .verifyComplete();
    }

    @Test
    public void changeFileLeaseMin() {
        DataLakeFileAsyncClient fc = createPathClient();
        String leaseID = setupPathLeaseCondition(fc, RECEIVED_LEASE_ID);

        assertAsyncResponseStatusCode(createLeaseAsyncClient(fc, leaseID)
            .changeLeaseWithResponse(testResourceNamer.randomUuid(), null), 200);
    }

    @ParameterizedTest
    @MethodSource("validLeaseConditions")
    public void changeFileLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch) {
        DataLakeFileAsyncClient fc = createPathClient();
        match = setupPathMatchCondition(fc, match);
        String leaseID = setupPathLeaseCondition(fc, RECEIVED_LEASE_ID);
        RequestConditions mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch);

        assertAsyncResponseStatusCode(createLeaseAsyncClient(fc, leaseID)
            .changeLeaseWithResponse(testResourceNamer.randomUuid(), mac), 200);
    }

    @ParameterizedTest
    @MethodSource("invalidLeaseConditions")
    public void changeFileLeaseACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
                                      String noneMatch) {
        DataLakeFileAsyncClient fc = createPathClient();
        noneMatch = setupPathMatchCondition(fc, noneMatch);
        setupPathLeaseCondition(fc, RECEIVED_LEASE_ID);
        RequestConditions mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch);

        StepVerifier.create(createLeaseAsyncClient(fc, RECEIVED_LEASE_ID)
            .changeLeaseWithResponse(testResourceNamer.randomUuid(), mac))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void changeFileLeaseError() {
        DataLakeFileAsyncClient fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        StepVerifier.create(createLeaseAsyncClient(fc, "id").changeLease("id"))
            .verifyError(DataLakeStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("acquireLeaseSupplier")
    public void acquireFileSystemLease(String proposedID, int leaseTime, LeaseStateType leaseState,
                                       LeaseDurationType leaseDuration) {
        DataLakeLeaseAsyncClient leaseClient = createLeaseAsyncClient(dataLakeFileSystemAsyncClient, proposedID);
        StepVerifier.create(leaseClient.acquireLeaseWithResponse(leaseTime, null))
            .assertNext(r -> {
                validateBasicHeaders(r.getHeaders());
                assertEquals(r.getValue(), leaseClient.getLeaseId());
            })
            .verifyComplete();

        StepVerifier.create(dataLakeFileSystemAsyncClient.getProperties())
            .assertNext(p -> {
                assertEquals(p.getLeaseState(), leaseState);
                assertEquals(p.getLeaseDuration(), leaseDuration);
            })
            .verifyComplete();
    }

    @Test
    public void acquireFileSystemLeaseMin() {
        assertAsyncResponseStatusCode(createLeaseAsyncClient(dataLakeFileSystemAsyncClient)
            .acquireLeaseWithResponse(-1, null), 201);
    }

    @ParameterizedTest
    @ValueSource(ints = { -10, 10, 70 })
    public void acquireFileSystemLeaseDurationFail(int duration) {
        DataLakeLeaseAsyncClient leaseClient = createLeaseAsyncClient(dataLakeFileSystemAsyncClient);

        StepVerifier.create(leaseClient.acquireLease(duration))
            .verifyError(DataLakeStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("validLeaseConditions")
    public void acquireFileSystemLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified, String match,
                                         String noneMatch) {
        RequestConditions mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch);

        assertAsyncResponseStatusCode(createLeaseAsyncClient(dataLakeFileSystemAsyncClient)
            .acquireLeaseWithResponse(-1, mac), 201);
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedConditions")
    public void acquireFileSystemLeaseACFail(OffsetDateTime modified, OffsetDateTime unmodified) {
        RequestConditions mac = new RequestConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified);

        StepVerifier.create(createLeaseAsyncClient(dataLakeFileSystemAsyncClient)
            .acquireLeaseWithResponse(-1, mac))
            .verifyError(DataLakeStorageException.class);
    }

    private static Stream<Arguments> invalidModifiedConditions() {
        return Stream.of(
            // modified | unmodified
            Arguments.of(NEW_DATE, null),
            Arguments.of(null, OLD_DATE)
        );
    }

    @Test
    public void acquireFileSystemLeaseError() {
        DataLakeFileSystemAsyncClient fsc = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName());

        StepVerifier.create(createLeaseAsyncClient(fsc).acquireLease(50))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void renewFileSystemLease() {
        String leaseID = setupFileSystemLeaseAsyncCondition(dataLakeFileSystemAsyncClient, RECEIVED_LEASE_ID);
        DataLakeLeaseAsyncClient leaseClient = createLeaseAsyncClient(dataLakeFileSystemAsyncClient, leaseID);

        StepVerifier.create(leaseClient.renewLeaseWithResponse(null))
            .assertNext(r -> {
                validateBasicHeaders(r.getHeaders());
                assertEquals(leaseClient.getLeaseId(), r.getValue());
            })
            .verifyComplete();

        StepVerifier.create(dataLakeFileSystemAsyncClient.getProperties())
            .assertNext(p -> assertEquals(LeaseStateType.LEASED, p.getLeaseState()))
            .verifyComplete();
    }

    @Test
    public void renewFileSystemLeaseMin() {
        String leaseID = setupFileSystemLeaseAsyncCondition(dataLakeFileSystemAsyncClient, RECEIVED_LEASE_ID);

        assertAsyncResponseStatusCode(createLeaseAsyncClient(dataLakeFileSystemAsyncClient, leaseID)
            .renewLeaseWithResponse(null), 200);
    }

    @ParameterizedTest
    @MethodSource("validModifiedConditions")
    public void renewFileSystemLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified) {
        String leaseID = setupFileSystemLeaseAsyncCondition(dataLakeFileSystemAsyncClient, RECEIVED_LEASE_ID);
        RequestConditions mac = new RequestConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified);

        assertAsyncResponseStatusCode(createLeaseAsyncClient(dataLakeFileSystemAsyncClient, leaseID)
            .renewLeaseWithResponse(mac), 200);
    }

    private static Stream<Arguments> validModifiedConditions() {
        return Stream.of(
            // modified | unmodified
            Arguments.of(null, null),
            Arguments.of(OLD_DATE, null),
            Arguments.of(null, NEW_DATE)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedConditions")
    public void renewFileSystemLeaseACFail(OffsetDateTime modified, OffsetDateTime unmodified) {
        String leaseID = setupFileSystemLeaseAsyncCondition(dataLakeFileSystemAsyncClient, RECEIVED_LEASE_ID);
        RequestConditions mac = new RequestConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified);

        StepVerifier.create(createLeaseAsyncClient(dataLakeFileSystemAsyncClient, leaseID).renewLeaseWithResponse(mac))
            .verifyError(DataLakeStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("invalidMatchConditions")
    public void renewFileSystemLeaseACIllegal(String match, String noneMatch) {
        RequestConditions mac = new RequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch);

        StepVerifier.create(createLeaseAsyncClient(dataLakeFileSystemAsyncClient, RECEIVED_ETAG).renewLeaseWithResponse(mac))
            .verifyError(DataLakeStorageException.class);
    }

    private static Stream<Arguments> invalidMatchConditions() {
        return Stream.of(
            // match | noneMatch
            Arguments.of(RECEIVED_ETAG, null),
            Arguments.of(null, GARBAGE_ETAG)
        );
    }

    @Test
    public void renewFileSystemLeaseError() {
        DataLakeFileSystemAsyncClient fsc = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName());

        StepVerifier.create(createLeaseAsyncClient(fsc, "id").renewLease())
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void releaseFileSystemLease() {
        String leaseID = setupFileSystemLeaseAsyncCondition(dataLakeFileSystemAsyncClient, RECEIVED_LEASE_ID);

        StepVerifier.create(createLeaseAsyncClient(dataLakeFileSystemAsyncClient, leaseID)
            .releaseLeaseWithResponse(null))
            .assertNext(r -> validateBasicHeaders(r.getHeaders()))
            .verifyComplete();

        StepVerifier.create(dataLakeFileSystemAsyncClient.getProperties())
            .assertNext(p -> assertEquals(LeaseStateType.AVAILABLE, p.getLeaseState()))
            .verifyComplete();
    }

    @Test
    public void releaseFileSystemLeaseMin() {
        String leaseID = setupFileSystemLeaseAsyncCondition(dataLakeFileSystemAsyncClient, RECEIVED_LEASE_ID);

        assertAsyncResponseStatusCode(createLeaseAsyncClient(dataLakeFileSystemAsyncClient, leaseID)
            .releaseLeaseWithResponse(null), 200);
    }

    @ParameterizedTest
    @MethodSource("validModifiedConditions")
    public void releaseFileSystemLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified) {
        String leaseID = setupFileSystemLeaseAsyncCondition(dataLakeFileSystemAsyncClient, RECEIVED_LEASE_ID);
        RequestConditions mac = new RequestConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified);

        assertAsyncResponseStatusCode(createLeaseAsyncClient(dataLakeFileSystemAsyncClient, leaseID)
            .releaseLeaseWithResponse(mac), 200);
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedConditions")
    public void releaseFileSystemLeaseACFail(OffsetDateTime modified, OffsetDateTime unmodified) {
        String leaseID = setupFileSystemLeaseAsyncCondition(dataLakeFileSystemAsyncClient, RECEIVED_LEASE_ID);
        RequestConditions mac = new RequestConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified);

        StepVerifier.create(createLeaseAsyncClient(dataLakeFileSystemAsyncClient, leaseID).releaseLeaseWithResponse(mac))
            .verifyError(DataLakeStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("invalidMatchConditions")
    public void releaseFileSystemLeaseACIllegal(String match, String noneMatch) {
        RequestConditions mac = new RequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch);

        StepVerifier.create(createLeaseAsyncClient(dataLakeFileSystemAsyncClient, RECEIVED_ETAG)
            .releaseLeaseWithResponse(mac))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void releaseFileSystemLeaseError() {
        DataLakeFileSystemAsyncClient fsc = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName());

        StepVerifier.create(createLeaseAsyncClient(fsc, "id").releaseLease())
            .verifyError(DataLakeStorageException.class);
    }

    @ParameterizedTest
    @CsvSource(value = {"-1,null,0", "-1,20,25", "20,15,16"}, nullValues = "null")
    public void breakFileSystemLease(int leaseTime, Integer breakPeriod, int remainingTime) {
        DataLakeLeaseAsyncClient leaseClient = createLeaseAsyncClient(dataLakeFileSystemAsyncClient, testResourceNamer.randomUuid());
        leaseClient.acquireLease(leaseTime).block();

        StepVerifier.create(leaseClient.breakLeaseWithResponse(breakPeriod, null))
            .assertNext(r -> {
                assertTrue(r.getValue() <= remainingTime);
                validateBasicHeaders(r.getHeaders());
            })
            .verifyComplete();

        StepVerifier.create(dataLakeFileSystemAsyncClient.getProperties())
            .assertNext(p -> assertTrue(p.getLeaseState() == LeaseStateType.BROKEN
                || p.getLeaseState() == LeaseStateType.BREAKING))
            .verifyComplete();

        // Break the lease for cleanup.
        leaseClient.breakLeaseWithResponse(0, null).block();
    }

    @Test
    public void breakFileSystemLeaseMin() {
        setupFileSystemLeaseAsyncCondition(dataLakeFileSystemAsyncClient, RECEIVED_LEASE_ID);

        assertAsyncResponseStatusCode(createLeaseAsyncClient(dataLakeFileSystemAsyncClient)
            .breakLeaseWithResponse(null, null), 202);
    }

    @ParameterizedTest
    @MethodSource("validModifiedConditions")
    public void breakFileSystemLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified) {
        setupFileSystemLeaseAsyncCondition(dataLakeFileSystemAsyncClient, RECEIVED_LEASE_ID);
        RequestConditions mac = new RequestConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified);

        assertAsyncResponseStatusCode(createLeaseAsyncClient(dataLakeFileSystemAsyncClient)
            .breakLeaseWithResponse(null, mac), 202);
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedConditions")
    public void breakFileSystemLeaseACFail(OffsetDateTime modified, OffsetDateTime unmodified) {
        setupFileSystemLeaseAsyncCondition(dataLakeFileSystemAsyncClient, RECEIVED_LEASE_ID);
        RequestConditions mac = new RequestConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified);

        StepVerifier.create(createLeaseAsyncClient(dataLakeFileSystemAsyncClient)
            .breakLeaseWithResponse(null, mac))
            .verifyError(DataLakeStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("invalidMatchConditions")
    public void breakFileSystemLeaseACIllegal(String match, String noneMatch) {
        RequestConditions mac = new RequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch);

        StepVerifier.create(createLeaseAsyncClient(dataLakeFileSystemAsyncClient)
            .breakLeaseWithResponse(null, mac))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void breakFileSystemLeaseError() {
        DataLakeFileSystemAsyncClient fsc = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName());

        StepVerifier.create(createLeaseAsyncClient(fsc).breakLease())
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void changeFileSystemLease() {
        String leaseID = setupFileSystemLeaseAsyncCondition(dataLakeFileSystemAsyncClient, RECEIVED_LEASE_ID);
        DataLakeLeaseAsyncClient leaseClient = createLeaseAsyncClient(dataLakeFileSystemAsyncClient, leaseID);

        StepVerifier.create(leaseClient.changeLeaseWithResponse(testResourceNamer.randomUuid(), null)
            .flatMap(r -> {
                validateBasicHeaders(r.getHeaders());
                assertEquals(leaseClient.getLeaseId(), r.getValue());
                return createLeaseAsyncClient(dataLakeFileSystemAsyncClient, r.getValue())
                    .releaseLeaseWithResponse(null);
            }))
            .assertNext(r -> {
                assertEquals(200, r.getStatusCode());
            })
            .verifyComplete();
    }

    @Test
    public void changeFileSystemLeaseMin() {
        String leaseID = setupFileSystemLeaseAsyncCondition(dataLakeFileSystemAsyncClient, RECEIVED_LEASE_ID);

        assertAsyncResponseStatusCode(createLeaseAsyncClient(dataLakeFileSystemAsyncClient, leaseID)
            .changeLeaseWithResponse(testResourceNamer.randomUuid(), null), 200);
    }

    @ParameterizedTest
    @MethodSource("validModifiedConditions")
    public void changeFileSystemLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified) {
        String leaseID = setupFileSystemLeaseAsyncCondition(dataLakeFileSystemAsyncClient, RECEIVED_LEASE_ID);
        RequestConditions mac = new RequestConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified);

        assertAsyncResponseStatusCode(createLeaseAsyncClient(dataLakeFileSystemAsyncClient, leaseID)
            .changeLeaseWithResponse(testResourceNamer.randomUuid(), mac), 200);
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedConditions")
    public void changeFileSystemLeaseACFail(OffsetDateTime modified, OffsetDateTime unmodified) {
        String leaseID = setupFileSystemLeaseAsyncCondition(dataLakeFileSystemAsyncClient, RECEIVED_LEASE_ID);
        RequestConditions mac = new RequestConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified);

        StepVerifier.create(createLeaseAsyncClient(dataLakeFileSystemAsyncClient, leaseID)
            .changeLeaseWithResponse(testResourceNamer.randomUuid(), mac))
            .verifyError(DataLakeStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("invalidMatchConditions")
    public void changeFileSystemLeaseACIllegal(String match, String noneMatch) {
        RequestConditions mac = new RequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch);

        StepVerifier.create(createLeaseAsyncClient(dataLakeFileSystemAsyncClient, RECEIVED_LEASE_ID)
            .changeLeaseWithResponse(GARBAGE_LEASE_ID, mac))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void changeFileSystemLeaseError() {
        DataLakeFileSystemAsyncClient fsc = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName());

        StepVerifier.create(createLeaseAsyncClient(fsc, "id").changeLease("id"))
            .verifyError(DataLakeStorageException.class);
    }







}
