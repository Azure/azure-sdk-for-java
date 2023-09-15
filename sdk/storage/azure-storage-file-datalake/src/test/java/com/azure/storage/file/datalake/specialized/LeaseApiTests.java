// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.datalake.specialized;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.RequestConditions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.CoreUtils;
import com.azure.storage.file.datalake.DataLakeFileClient;
import com.azure.storage.file.datalake.DataLakeFileSystemClient;
import com.azure.storage.file.datalake.DataLakeTestBase;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import com.azure.storage.file.datalake.models.FileSystemProperties;
import com.azure.storage.file.datalake.models.LeaseDurationType;
import com.azure.storage.file.datalake.models.LeaseStateType;
import com.azure.storage.file.datalake.models.PathProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.OffsetDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LeaseApiTests extends DataLakeTestBase {
    private DataLakeFileClient createPathClient() {
        DataLakeFileClient fc = dataLakeFileSystemClient.getFileClient(generatePathName());
        fc.create();
        return fc;
    }

    @ParameterizedTest
    @MethodSource("acquireLeaseSupplier")
    public void acquireFileLease(String proposedId, int leaseTime, LeaseStateType leaseStateType,
        LeaseDurationType leaseDurationType) {
        DataLakeFileClient fc = createPathClient();
        DataLakeLeaseClient leaseClient = createLeaseClient(fc, proposedId);

        String leaseId = leaseClient.acquireLease(leaseTime);

        assertEquals(leaseId, leaseClient.getLeaseId());

        Response<PathProperties> response = fc.getPropertiesWithResponse(null, null, null);

        assertEquals(leaseStateType, response.getValue().getLeaseState());
        assertEquals(leaseDurationType, response.getValue().getLeaseDuration());
        validateBasicHeaders(response.getHeaders());
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
        assertEquals(201,
            createLeaseClient(createPathClient()).acquireLeaseWithResponse(-1, null, null, null).getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(ints = { -10, 10, 70 })
    public void acquireFileLeaseDurationFail(int duration) {
        assertThrows(DataLakeStorageException.class, () ->
            createLeaseClient(createPathClient()).acquireLease(duration));
    }

    @ParameterizedTest
    @MethodSource("validLeaseConditions")
    public void acquireFileLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch) {
        DataLakeFileClient fc = createPathClient();
        match = setupPathMatchCondition(fc, match);
        RequestConditions mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch);

        assertEquals(201, createLeaseClient(fc).acquireLeaseWithResponse(-1, mac, null, null).getStatusCode());
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
        DataLakeFileClient fc = createPathClient();
        noneMatch = setupPathMatchCondition(fc, noneMatch);
        RequestConditions mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch);

        assertThrows(DataLakeStorageException.class, () ->
            createLeaseClient(fc).acquireLeaseWithResponse(-1, mac, null, null));
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
        DataLakeFileClient fc = dataLakeFileSystemClient.getFileClient(generatePathName());

        assertThrows(DataLakeStorageException.class, () -> createLeaseClient(fc).acquireLease(20));
    }

    @Test
    public void renewFileLease() {
        DataLakeFileClient fc = createPathClient();
        String leaseId = setupPathLeaseCondition(fc, RECEIVED_LEASE_ID);
        DataLakeLeaseClient leaseClient = createLeaseClient(fc, leaseId);

        Response<String> renewLeaseResponse = leaseClient.renewLeaseWithResponse(null, null, null);

        assertEquals(LeaseStateType.LEASED, fc.getProperties().getLeaseState());
        validateBasicHeaders(renewLeaseResponse.getHeaders());
        assertEquals(leaseClient.getLeaseId(), renewLeaseResponse.getValue());
    }

    @Test
    public void renewFileLeaseMin() {
        DataLakeFileClient fc = createPathClient();
        String leaseId = setupPathLeaseCondition(fc, RECEIVED_LEASE_ID);

        assertEquals(200, createLeaseClient(fc, leaseId).renewLeaseWithResponse(null, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("validLeaseConditions")
    public void renewFileLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch) {
        DataLakeFileClient fc = createPathClient();
        match = setupPathMatchCondition(fc, match);
        String leaseId = setupPathLeaseCondition(fc, RECEIVED_LEASE_ID);
        RequestConditions mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch);

        assertEquals(200, createLeaseClient(fc, leaseId).renewLeaseWithResponse(mac, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidLeaseConditions")
    public void renewFileLeaseACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
        String noneMatch) {
        DataLakeFileClient fc = createPathClient();
        String leaseId = setupPathLeaseCondition(fc, RECEIVED_LEASE_ID);
        noneMatch = setupPathMatchCondition(fc, noneMatch);
        RequestConditions mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch);

        assertThrows(DataLakeStorageException.class, () ->
            createLeaseClient(fc, leaseId).renewLeaseWithResponse(mac, null, null));
    }

    @Test
    public void renewFileLeaseError() {
        DataLakeFileClient fc = dataLakeFileSystemClient.getFileClient(generatePathName());

        assertThrows(DataLakeStorageException.class, () -> createLeaseClient(fc, "id").renewLease());
    }

    @Test
    public void releaseFileLease() {
        DataLakeFileClient fc = createPathClient();
        String leaseId = setupPathLeaseCondition(fc, RECEIVED_LEASE_ID);
        HttpHeaders headers = createLeaseClient(fc, leaseId).releaseLeaseWithResponse(null, null, null).getHeaders();

        assertEquals(LeaseStateType.AVAILABLE, fc.getProperties().getLeaseState());
        validateBasicHeaders(headers);
    }

    @Test
    public void releaseFileLeaseMin() {
        DataLakeFileClient fc = createPathClient();
        String leaseId = setupPathLeaseCondition(fc, RECEIVED_LEASE_ID);

        assertEquals(200, createLeaseClient(fc, leaseId).releaseLeaseWithResponse(null, null, null).getStatusCode());
    }


    @ParameterizedTest
    @MethodSource("validLeaseConditions")
    public void releaseFileLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch) {
        DataLakeFileClient fc = createPathClient();
        match = setupPathMatchCondition(fc, match);
        String leaseId = setupPathLeaseCondition(fc, RECEIVED_LEASE_ID);
        RequestConditions mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch);

        assertEquals(200, createLeaseClient(fc, leaseId).releaseLeaseWithResponse(mac, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidLeaseConditions")
    public void releaseFileLeaseACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
        String noneMatch) {
        DataLakeFileClient fc = createPathClient();
        String leaseId = setupPathLeaseCondition(fc, RECEIVED_LEASE_ID);
        noneMatch = setupPathMatchCondition(fc, noneMatch);
        RequestConditions mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch);

        assertThrows(DataLakeStorageException.class, () ->
            createLeaseClient(fc, leaseId).releaseLeaseWithResponse(mac, null, null));
    }

    @Test
    public void releaseFileLeaseError() {
        DataLakeFileClient fc = dataLakeFileSystemClient.getFileClient(generatePathName());

        assertThrows(DataLakeStorageException.class, () -> createLeaseClient(fc, "id").releaseLease());
    }

    @ParameterizedTest
    @CsvSource(value = {"-1,null,0", "-1,20,25", "20,15,16"}, nullValues = "null")
    public void breakFileLease(int leaseTime, Integer breakPeriod, int remainingTime) {
        DataLakeFileClient fc = createPathClient();
        DataLakeLeaseClient leaseClient = createLeaseClient(fc, testResourceNamer.randomUuid());

        leaseClient.acquireLease(leaseTime);
        Response<Integer> breakLeaseResponse = leaseClient.breakLeaseWithResponse(breakPeriod, null, null, null);
        LeaseStateType leaseState = fc.getProperties().getLeaseState();

        assertTrue(leaseState == LeaseStateType.BROKEN || leaseState == LeaseStateType.BREAKING);
        assertTrue(breakLeaseResponse.getValue() <= remainingTime);
        validateBasicHeaders(breakLeaseResponse.getHeaders());
    }

    @Test
    public void breakFileLeaseMin() {
        DataLakeFileClient fc = createPathClient();
        setupPathLeaseCondition(fc, RECEIVED_LEASE_ID);

        assertEquals(202, createLeaseClient(fc).breakLeaseWithResponse(null, null, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("validLeaseConditions")
    public void breakFileLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch) {
        DataLakeFileClient fc = createPathClient();
        match = setupPathMatchCondition(fc, match);
        setupPathLeaseCondition(fc, RECEIVED_LEASE_ID);
        RequestConditions mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch);

        assertEquals(202, createLeaseClient(fc).breakLeaseWithResponse(null, mac, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidLeaseConditions")
    public void breakFileLeaseACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
        String noneMatch) {
        DataLakeFileClient fc = createPathClient();
        setupPathLeaseCondition(fc, RECEIVED_LEASE_ID);
        noneMatch = setupPathMatchCondition(fc, noneMatch);
        RequestConditions mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch);

        assertThrows(DataLakeStorageException.class, () ->
            createLeaseClient(fc).breakLeaseWithResponse(null, mac, null, null));
    }

    @Test
    public void breakFileLeaseError() {
        DataLakeFileClient fc = createPathClient();

        assertThrows(DataLakeStorageException.class, () -> createLeaseClient(fc).breakLease());
    }

    @Test
    public void changeFileLease() {
        DataLakeFileClient fc = createPathClient();
        DataLakeLeaseClient leaseClient = createLeaseClient(fc, testResourceNamer.randomUuid());
        leaseClient.acquireLease(15);

        String newLeaseId = testResourceNamer.randomUuid();
        Response<String> changeLeaseResponse = leaseClient.changeLeaseWithResponse(newLeaseId, null, null, null);

        validateBasicHeaders(changeLeaseResponse.getHeaders());
        assertEquals(leaseClient.getLeaseId(), changeLeaseResponse.getValue());

        assertEquals(200, createLeaseClient(fc, changeLeaseResponse.getValue())
            .releaseLeaseWithResponse(null, null, null).getStatusCode());
    }

    @Test
    public void changeFileLeaseMin() {
        DataLakeFileClient fc = createPathClient();
        String leaseID = setupPathLeaseCondition(fc, RECEIVED_LEASE_ID);

        assertEquals(200, createLeaseClient(fc, leaseID)
            .changeLeaseWithResponse(testResourceNamer.randomUuid(), null, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("validLeaseConditions")
    public void changeFileLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch) {
        DataLakeFileClient fc = createPathClient();
        match = setupPathMatchCondition(fc, match);
        String leaseID = setupPathLeaseCondition(fc, RECEIVED_LEASE_ID);
        RequestConditions mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch);

        assertEquals(200, createLeaseClient(fc, leaseID)
            .changeLeaseWithResponse(testResourceNamer.randomUuid(), mac, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidLeaseConditions")
    public void changeFileLeaseACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
        String noneMatch) {
        DataLakeFileClient fc = createPathClient();
        noneMatch = setupPathMatchCondition(fc, noneMatch);
        setupPathLeaseCondition(fc, RECEIVED_LEASE_ID);
        RequestConditions mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch);

        assertThrows(DataLakeStorageException.class, () -> createLeaseClient(fc, RECEIVED_LEASE_ID)
            .changeLeaseWithResponse(testResourceNamer.randomUuid(), mac, null, null));
    }

    @Test
    public void changeFileLeaseError() {
        DataLakeFileClient fc = dataLakeFileSystemClient.getFileClient(generatePathName());

        assertThrows(DataLakeStorageException.class, () -> createLeaseClient(fc, "id").changeLease("id"));
    }

    @ParameterizedTest
    @MethodSource("acquireLeaseSupplier")
    public void acquireFileSystemLease(String proposedID, int leaseTime, LeaseStateType leaseState,
        LeaseDurationType leaseDuration) {
        DataLakeLeaseClient leaseClient = createLeaseClient(dataLakeFileSystemClient, proposedID);
        Response<String> leaseResponse = leaseClient.acquireLeaseWithResponse(leaseTime, null, null, null);

        FileSystemProperties properties = dataLakeFileSystemClient.getProperties();

        validateBasicHeaders(leaseResponse.getHeaders());
        assertEquals(leaseResponse.getValue(), leaseClient.getLeaseId());
        assertEquals(properties.getLeaseState(), leaseState);
        assertEquals(properties.getLeaseDuration(), leaseDuration);
    }

    @Test
    public void acquireFileSystemLeaseMin() {
        assertEquals(201, createLeaseClient(dataLakeFileSystemClient)
            .acquireLeaseWithResponse(-1, null, null, null).getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(ints = { -10, 10, 70 })
    public void acquireFileSystemLeaseDurationFail(int duration) {
        DataLakeLeaseClient leaseClient = createLeaseClient(dataLakeFileSystemClient);

        assertThrows(DataLakeStorageException.class, () -> leaseClient.acquireLease(duration));
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

        assertEquals(201, createLeaseClient(dataLakeFileSystemClient).acquireLeaseWithResponse(-1, mac, null, null)
            .getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedConditions")
    public void acquireFileSystemLeaseACFail(OffsetDateTime modified, OffsetDateTime unmodified) {
        RequestConditions mac = new RequestConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified);

        assertThrows(DataLakeStorageException.class, () -> createLeaseClient(dataLakeFileSystemClient)
            .acquireLeaseWithResponse(-1, mac, null, null));
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
        DataLakeFileSystemClient fsc = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());

        assertThrows(DataLakeStorageException.class, () -> createLeaseClient(fsc).acquireLease(50));
    }

    @Test
    public void renewFileSystemLease() {
        String leaseID = setupFileSystemLeaseCondition(dataLakeFileSystemClient, RECEIVED_LEASE_ID);
        DataLakeLeaseClient leaseClient = createLeaseClient(dataLakeFileSystemClient, leaseID);

        Response<String> renewLeaseResponse = leaseClient.renewLeaseWithResponse(null, null, null);

        assertEquals(LeaseStateType.LEASED, dataLakeFileSystemClient.getProperties().getLeaseState());
        validateBasicHeaders(renewLeaseResponse.getHeaders());
        assertEquals(leaseClient.getLeaseId(), renewLeaseResponse.getValue());
    }

    @Test
    public void renewFileSystemLeaseMin() {
        String leaseID = setupFileSystemLeaseCondition(dataLakeFileSystemClient, RECEIVED_LEASE_ID);

        assertEquals(200, createLeaseClient(dataLakeFileSystemClient, leaseID)
            .renewLeaseWithResponse(null, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("validModifiedConditions")
    public void renewFileSystemLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified) {
        String leaseID = setupFileSystemLeaseCondition(dataLakeFileSystemClient, RECEIVED_LEASE_ID);
        RequestConditions mac = new RequestConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified);

        assertEquals(200, createLeaseClient(dataLakeFileSystemClient, leaseID)
            .renewLeaseWithResponse(mac, null, null).getStatusCode());
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
        String leaseID = setupFileSystemLeaseCondition(dataLakeFileSystemClient, RECEIVED_LEASE_ID);
        RequestConditions mac = new RequestConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified);

        assertThrows(DataLakeStorageException.class, () -> createLeaseClient(dataLakeFileSystemClient, leaseID)
            .renewLeaseWithResponse(mac, null, null));
    }

    @ParameterizedTest
    @MethodSource("invalidMatchConditions")
    public void renewFileSystemLeaseACIllegal(String match, String noneMatch) {
        RequestConditions mac = new RequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch);

        assertThrows(DataLakeStorageException.class, () -> createLeaseClient(dataLakeFileSystemClient, RECEIVED_ETAG)
            .renewLeaseWithResponse(mac, null, null));
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
        DataLakeFileSystemClient fsc = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());

        assertThrows(DataLakeStorageException.class, () -> createLeaseClient(fsc, "id").renewLease());
    }

    @Test
    public void releaseFileSystemLease() {
        String leaseID = setupFileSystemLeaseCondition(dataLakeFileSystemClient, RECEIVED_LEASE_ID);

        Response<Void> releaseLeaseResponse = createLeaseClient(dataLakeFileSystemClient, leaseID)
            .releaseLeaseWithResponse(null, null, null);

        assertEquals(LeaseStateType.AVAILABLE, dataLakeFileSystemClient.getProperties().getLeaseState());
        validateBasicHeaders(releaseLeaseResponse.getHeaders());
    }

    @Test
    public void releaseFileSystemLeaseMin() {
        String leaseID = setupFileSystemLeaseCondition(dataLakeFileSystemClient, RECEIVED_LEASE_ID);

        assertEquals(200, createLeaseClient(dataLakeFileSystemClient, leaseID)
            .releaseLeaseWithResponse(null, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("validModifiedConditions")
    public void releaseFileSystemLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified) {
        String leaseID = setupFileSystemLeaseCondition(dataLakeFileSystemClient, RECEIVED_LEASE_ID);
        RequestConditions mac = new RequestConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified);

        assertEquals(200, createLeaseClient(dataLakeFileSystemClient, leaseID)
            .releaseLeaseWithResponse(mac, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedConditions")
    public void releaseFileSystemLeaseACFail(OffsetDateTime modified, OffsetDateTime unmodified) {
        String leaseID = setupFileSystemLeaseCondition(dataLakeFileSystemClient, RECEIVED_LEASE_ID);
        RequestConditions mac = new RequestConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified);

        assertThrows(DataLakeStorageException.class, () -> createLeaseClient(dataLakeFileSystemClient, leaseID)
            .releaseLeaseWithResponse(mac, null, null));
    }

    @ParameterizedTest
    @MethodSource("invalidMatchConditions")
    public void releaseFileSystemLeaseACIllegal(String match, String noneMatch) {
        RequestConditions mac = new RequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch);

        assertThrows(DataLakeStorageException.class, () -> createLeaseClient(dataLakeFileSystemClient, RECEIVED_ETAG)
            .releaseLeaseWithResponse(mac, null, null));
    }

    @Test
    public void releaseFileSystemLeaseError() {
        DataLakeFileSystemClient fsc = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());

        assertThrows(DataLakeStorageException.class, () -> createLeaseClient(fsc, "id").releaseLease());
    }

    @ParameterizedTest
    @CsvSource(value = {"-1,null,0", "-1,20,25", "20,15,16"}, nullValues = "null")
    public void breakFileSystemLease(int leaseTime, Integer breakPeriod, int remainingTime) {
        DataLakeLeaseClient leaseClient = createLeaseClient(dataLakeFileSystemClient, testResourceNamer.randomUuid());
        leaseClient.acquireLease(leaseTime);

        Response<Integer> breakLeaseResponse = leaseClient.breakLeaseWithResponse(breakPeriod, null, null, null);
        LeaseStateType state = dataLakeFileSystemClient.getProperties().getLeaseState();

        assertTrue(state == LeaseStateType.BROKEN || state == LeaseStateType.BREAKING);
        assertTrue(breakLeaseResponse.getValue() <= remainingTime);
        validateBasicHeaders(breakLeaseResponse.getHeaders());

        // Break the lease for cleanup.
        leaseClient.breakLeaseWithResponse(0, null, null, null);
    }

    @Test
    public void breakFileSystemLeaseMin() {
        setupFileSystemLeaseCondition(dataLakeFileSystemClient, RECEIVED_LEASE_ID);

        assertEquals(202, createLeaseClient(dataLakeFileSystemClient)
            .breakLeaseWithResponse(null, null, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("validModifiedConditions")
    public void breakFileSystemLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified) {
        setupFileSystemLeaseCondition(dataLakeFileSystemClient, RECEIVED_LEASE_ID);
        RequestConditions mac = new RequestConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified);

        assertEquals(202, createLeaseClient(dataLakeFileSystemClient)
            .breakLeaseWithResponse(null, mac, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedConditions")
    public void breakFileSystemLeaseACFail(OffsetDateTime modified, OffsetDateTime unmodified) {
        setupFileSystemLeaseCondition(dataLakeFileSystemClient, RECEIVED_LEASE_ID);
        RequestConditions mac = new RequestConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified);

        assertThrows(DataLakeStorageException.class, () -> createLeaseClient(dataLakeFileSystemClient)
            .breakLeaseWithResponse(null, mac, null, null));
    }

    @ParameterizedTest
    @MethodSource("invalidMatchConditions")
    public void breakFileSystemLeaseACIllegal(String match, String noneMatch) {
        RequestConditions mac = new RequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch);

        assertThrows(DataLakeStorageException.class, () -> createLeaseClient(dataLakeFileSystemClient)
            .breakLeaseWithResponse(null, mac, null, null));
    }

    @Test
    public void breakFileSystemLeaseError() {
        DataLakeFileSystemClient fsc = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());

        assertThrows(DataLakeStorageException.class, () -> createLeaseClient(fsc).breakLease());
    }

    @Test
    public void changeFileSystemLease() {
        String leaseID = setupFileSystemLeaseCondition(dataLakeFileSystemClient, RECEIVED_LEASE_ID);
        DataLakeLeaseClient leaseClient = createLeaseClient(dataLakeFileSystemClient, leaseID);

        Response<String> changeLeaseResponse = leaseClient.changeLeaseWithResponse(testResourceNamer.randomUuid(), null,
            null, null);
        String newLeaseId = changeLeaseResponse.getValue();

        validateBasicHeaders(changeLeaseResponse.getHeaders());
        assertEquals(leaseClient.getLeaseId(), newLeaseId);

        assertEquals(200, createLeaseClient(dataLakeFileSystemClient, newLeaseId).releaseLeaseWithResponse(null, null,
            null).getStatusCode());
    }

    @Test
    public void changeFileSystemLeaseMin() {
        String leaseID = setupFileSystemLeaseCondition(dataLakeFileSystemClient, RECEIVED_LEASE_ID);

        assertEquals(200, createLeaseClient(dataLakeFileSystemClient, leaseID)
            .changeLeaseWithResponse(testResourceNamer.randomUuid(), null, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("validModifiedConditions")
    public void changeFileSystemLeaseAC(OffsetDateTime modified, OffsetDateTime unmodified) {
        String leaseID = setupFileSystemLeaseCondition(dataLakeFileSystemClient, RECEIVED_LEASE_ID);
        RequestConditions mac = new RequestConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified);

        assertEquals(200, createLeaseClient(dataLakeFileSystemClient, leaseID)
            .changeLeaseWithResponse(testResourceNamer.randomUuid(), mac, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedConditions")
    public void changeFileSystemLeaseACFail(OffsetDateTime modified, OffsetDateTime unmodified) {
        String leaseID = setupFileSystemLeaseCondition(dataLakeFileSystemClient, RECEIVED_LEASE_ID);
        RequestConditions mac = new RequestConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified);

        assertThrows(DataLakeStorageException.class, () -> createLeaseClient(dataLakeFileSystemClient, leaseID)
            .changeLeaseWithResponse(testResourceNamer.randomUuid(), mac, null, null));
    }

    @ParameterizedTest
    @MethodSource("invalidMatchConditions")
    public void changeFileSystemLeaseACIllegal(String match, String noneMatch) {
        RequestConditions mac = new RequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch);

        assertThrows(DataLakeStorageException.class, () ->
            createLeaseClient(dataLakeFileSystemClient, RECEIVED_LEASE_ID)
                .changeLeaseWithResponse(GARBAGE_LEASE_ID, mac, null, null));
    }

    @Test
    public void changeFileSystemLeaseError() {
        DataLakeFileSystemClient fsc = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());

        assertThrows(DataLakeStorageException.class, () -> createLeaseClient(fsc, "id").changeLease("id"));
    }
}
