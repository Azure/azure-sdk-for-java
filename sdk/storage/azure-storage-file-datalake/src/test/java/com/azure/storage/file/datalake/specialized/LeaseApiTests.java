// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.datalake.specialized;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.RequestConditions;
import com.azure.core.http.rest.Response;
import com.azure.storage.file.datalake.APISpec;
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
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
            Arguments.of(null, -1, LeaseStateType.LEASED, LeaseDurationType.INFINITE),
            Arguments.of(null, 25, LeaseStateType.LEASED, LeaseDurationType.FIXED),
            Arguments.of(UUID.randomUUID().toString(), -1, LeaseStateType.LEASED, LeaseDurationType.INFINITE)
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

        // If running in live mode wait for the lease to expire to ensure we are actually renewing it
        sleepIfRunningAgainstService(16000);
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

    @Unroll
    def "Acquire file system lease AC fail"() {
        setup:
        def mac = new RequestConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified)

        when:
        createLeaseClient(fsc).acquireLeaseWithResponse(-1, mac, null, null)

        then:
        thrown(DataLakeStorageException)

        where:
        modified        | unmodified
        APISpec.newDate | null
        null            | APISpec.oldDate
    }

    def "Acquire file system lease error"() {
        setup:
        fsc = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName())

        when:
        createLeaseClient(fsc).acquireLease(50)

        then:
        thrown(DataLakeStorageException)
    }

    def "Renew file system lease"() {
        setup:
        def leaseID = setupFileSystemLeaseCondition(fsc, APISpec.receivedLeaseID)
        def leaseClient = createLeaseClient(fsc, leaseID)

        when:
        // If running in live mode wait for the lease to expire to ensure we are actually renewing it
        sleepIfRecord(16000)
        def renewLeaseResponse = leaseClient.renewLeaseWithResponse(null, null, null)

        then:
        fsc.getProperties().getLeaseState() == LeaseStateType.LEASED
        validateBasicHeaders(renewLeaseResponse.getHeaders())
        renewLeaseResponse.getValue() == leaseClient.getLeaseId()
    }

    def "Renew file system lease min"() {
        setup:
        def leaseID = setupFileSystemLeaseCondition(fsc, APISpec.receivedLeaseID)

        expect:
        createLeaseClient(fsc, leaseID).renewLeaseWithResponse(null, null, null).getStatusCode() == 200
    }

    @Unroll
    def "Renew file system lease AC"() {
        setup:
        def leaseID = setupFileSystemLeaseCondition(fsc, APISpec.receivedLeaseID)
        def mac = new RequestConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified)

        expect:
        createLeaseClient(fsc, leaseID).renewLeaseWithResponse(mac, null, null).getStatusCode() == 200

        where:
        modified        | unmodified
        null            | null
        APISpec.oldDate | null
        null            | APISpec.newDate
    }

    @Unroll
    def "Renew file system lease AC fail"() {
        setup:
        def leaseID = setupFileSystemLeaseCondition(fsc, APISpec.receivedLeaseID)
        def mac = new RequestConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified)

        when:
        createLeaseClient(fsc, leaseID).renewLeaseWithResponse(mac, null, null)

        then:
        thrown(DataLakeStorageException)

        where:
        modified        | unmodified
        APISpec.newDate | null
        null            | APISpec.oldDate
    }

    @Unroll
    def "Renew file system lease AC illegal"() {
        setup:
        def mac = new RequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch)

        when:
        createLeaseClient(fsc, APISpec.receivedEtag).renewLeaseWithResponse(mac, null, null)

        then:
        thrown(DataLakeStorageException)

        where:
        match                | noneMatch
        APISpec.receivedEtag | null
        null                 | APISpec.garbageEtag
    }

    def "Renew file system lease error"() {
        setup:
        fsc = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName())

        when:
        createLeaseClient(fsc, "id").renewLease()

        then:
        thrown(DataLakeStorageException)
    }

    def "Release file system lease"() {
        setup:
        def leaseID = setupFileSystemLeaseCondition(fsc, APISpec.receivedLeaseID)

        def releaseLeaseResponse = createLeaseClient(fsc, leaseID).releaseLeaseWithResponse(null, null, null)

        expect:
        fsc.getProperties().getLeaseState() == LeaseStateType.AVAILABLE
        validateBasicHeaders(releaseLeaseResponse.getHeaders())
    }

    def "Release file system lease min"() {
        setup:
        def leaseID = setupFileSystemLeaseCondition(fsc, APISpec.receivedLeaseID)

        expect:
        createLeaseClient(fsc, leaseID).releaseLeaseWithResponse(null, null, null).getStatusCode() == 200
    }

    @Unroll
    def "Release file system lease AC"() {
        setup:
        def leaseID = setupFileSystemLeaseCondition(fsc, APISpec.receivedLeaseID)
        def mac = new RequestConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified)

        expect:
        createLeaseClient(fsc, leaseID).releaseLeaseWithResponse(mac, null, null).getStatusCode() == 200

        where:
        modified        | unmodified
        null            | null
        APISpec.oldDate | null
        null            | APISpec.newDate
    }

    @Unroll
    def "Release file system lease AC fail"() {
        setup:
        def leaseID = setupFileSystemLeaseCondition(fsc, APISpec.receivedLeaseID)
        def mac = new RequestConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified)

        when:
        createLeaseClient(fsc, leaseID).releaseLeaseWithResponse(mac, null, null)

        then:
        thrown(DataLakeStorageException)

        where:
        modified        | unmodified
        APISpec.newDate | null
        null            | APISpec.oldDate
    }

    @Unroll
    def "Release file system lease AC illegal"() {
        setup:
        def mac = new RequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch)

        when:
        createLeaseClient(fsc, APISpec.receivedLeaseID).releaseLeaseWithResponse(mac, null, null)

        then:
        thrown(DataLakeStorageException)

        where:
        match                | noneMatch
        APISpec.receivedEtag | null
        null                 | APISpec.garbageEtag
    }

    def "Release file system lease error"() {
        setup:
        fsc = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName())

        when:
        createLeaseClient(fsc, "id").releaseLease()

        then:
        thrown(DataLakeStorageException)
    }

    @Unroll
    def "Break file system lease"() {
        setup:
        def leaseClient = createLeaseClient(fsc, namer.getRandomUuid())
        leaseClient.acquireLease(leaseTime)

        def breakLeaseResponse = leaseClient.breakLeaseWithResponse(breakPeriod, null, null, null)
        def state = fsc.getProperties().getLeaseState()

        expect:
        state == LeaseStateType.BROKEN || state == LeaseStateType.BREAKING
        breakLeaseResponse.getValue() <= remainingTime
        validateBasicHeaders(breakLeaseResponse.getHeaders())
        if (breakPeriod != null) {
            // If running in live mode wait for the lease to break so we can delete the file system after the test completes
            sleepIfRecord(breakPeriod * 1000)
        }

        where:
        leaseTime | breakPeriod | remainingTime
                                  -1        | null        | 0
                                                            -1        | 20          | 25
        20        | 15          | 16

    }

    def "Break file system lease min"() {
        setup:
        setupFileSystemLeaseCondition(fsc, APISpec.receivedLeaseID)

        expect:
        createLeaseClient(fsc).breakLeaseWithResponse(null, null, null, null).getStatusCode() == 202
    }

    @Unroll
    def "Break file system lease AC"() {
        setup:
        setupFileSystemLeaseCondition(fsc, APISpec.receivedLeaseID)
        def mac = new RequestConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified)

        expect:
        createLeaseClient(fsc).breakLeaseWithResponse(null, mac, null, null).getStatusCode() == 202

        where:
        modified        | unmodified
        null            | null
        APISpec.oldDate | null
        null            | APISpec.newDate
    }

    @Unroll
    def "Break file system lease AC fail"() {
        setup:
        setupFileSystemLeaseCondition(fsc, APISpec.receivedLeaseID)
        def mac = new RequestConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified)

        when:
        createLeaseClient(fsc).breakLeaseWithResponse(null, mac, null, null)

        then:
        thrown(DataLakeStorageException)

        where:
        modified        | unmodified
        APISpec.newDate | null
        null            | APISpec.oldDate
    }

    @Unroll
    def "Break file system lease AC illegal"() {
        setup:
        def mac = new RequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch)

        when:
        createLeaseClient(fsc).breakLeaseWithResponse(null, mac, null, null)

        then:
        thrown(DataLakeStorageException)

        where:
        match                | noneMatch
        APISpec.receivedEtag | null
        null                 | APISpec.garbageEtag
    }

    def "Break file system lease error"() {
        setup:
        fsc = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName())

        when:
        createLeaseClient(fsc).breakLease()

        then:
        thrown(DataLakeStorageException)
    }

    def "Change file system lease"() {
        setup:
        def leaseID = setupFileSystemLeaseCondition(fsc, APISpec.receivedLeaseID)
        def leaseClient = createLeaseClient(fsc, leaseID)

        when:
        def changeLeaseResponse = leaseClient.changeLeaseWithResponse(namer.getRandomUuid(), null, null, null)
        def newLeaseId = changeLeaseResponse.getValue()

        then:
        validateBasicHeaders(changeLeaseResponse.getHeaders())
        newLeaseId == leaseClient.getLeaseId()

        expect:
        createLeaseClient(fsc, newLeaseId).releaseLeaseWithResponse(null, null, null).getStatusCode() == 200
    }

    def "Change file system lease min"() {
        setup:
        def leaseID = setupFileSystemLeaseCondition(fsc, APISpec.receivedLeaseID)

        expect:
        createLeaseClient(fsc, leaseID).changeLeaseWithResponse(namer.getRandomUuid(), null, null, null).getStatusCode() == 200
    }

    @Unroll
    def "Change file system lease AC"() {
        setup:
        def leaseID = setupFileSystemLeaseCondition(fsc, APISpec.receivedLeaseID)
        def mac = new RequestConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified)

        expect:
        createLeaseClient(fsc, leaseID).changeLeaseWithResponse(namer.getRandomUuid(), mac, null, null).getStatusCode() == 200

        where:
        modified        | unmodified
        null            | null
        APISpec.oldDate | null
        null            | APISpec.newDate
    }

    @Unroll
    def "Change file system lease AC fail"() {
        setup:
        def leaseID = setupFileSystemLeaseCondition(fsc, APISpec.receivedLeaseID)
        def mac = new RequestConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified)

        when:
        createLeaseClient(fsc, leaseID).changeLeaseWithResponse(namer.getRandomUuid(), mac, null, null)

        then:
        thrown(DataLakeStorageException)

        where:
        modified        | unmodified
        APISpec.newDate | null
        null            | APISpec.oldDate
    }

    @Unroll
    def "Change file system lease AC illegal"() {
        setup:
        def mac = new RequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch)

        when:
        createLeaseClient(fsc, APISpec.receivedLeaseID).changeLeaseWithResponse(APISpec.garbageLeaseID, mac, null, null)

        then:
        thrown(DataLakeStorageException)

        where:
        match                | noneMatch
        APISpec.receivedEtag | null
        null                 | APISpec.garbageEtag
    }

    @Test
    public void changeFileSystemLeaseError() {
        DataLakeFileSystemClient fsc = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());

        assertThrows(DataLakeStorageException.class, () -> createLeaseClient(fsc, "id").changeLease("id"));
    }
}
