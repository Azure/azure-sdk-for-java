// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.specialized

import com.azure.core.http.RequestConditions
import com.azure.storage.file.datalake.APISpec
import com.azure.storage.file.datalake.DataLakeFileClient
import com.azure.storage.file.datalake.models.DataLakeStorageException
import com.azure.storage.file.datalake.models.LeaseDurationType
import com.azure.storage.file.datalake.models.LeaseStateType
import spock.lang.Unroll

class LeaseAPITest extends APISpec {
    private DataLakeFileClient createPathClient() {
        def fc = fsc.getFileClient(generatePathName())
        fc.create()
        return fc
    }

    @Unroll
    def "Acquire file lease"() {
        setup:
        def fc = createPathClient()
        def leaseClient = createLeaseClient(fc, proposedID)

        when:
        def leaseId = leaseClient.acquireLease(leaseTime)

        then:
        leaseId != null
        leaseId == leaseClient.getLeaseId()

        when:
        def response = fc.getPropertiesWithResponse(null, null, null)
        def properties = response.getValue()
        def headers = response.getHeaders()

        then:
        properties.getLeaseState() == leaseState
        properties.getLeaseDuration() == leaseDuration
        validateBasicHeaders(headers)

        where:
        proposedID                   | leaseTime || leaseState            | leaseDuration
        null                         | -1        || LeaseStateType.LEASED | LeaseDurationType.INFINITE
        null                         | 25        || LeaseStateType.LEASED | LeaseDurationType.FIXED
        UUID.randomUUID().toString() | -1        || LeaseStateType.LEASED | LeaseDurationType.INFINITE
    }

    def "Acquire file lease min"() {
        expect:
        createLeaseClient(createPathClient())
            .acquireLeaseWithResponse(-1, null, null, null)
            .getStatusCode() == 201
    }

    @Unroll
    def "Acquire file lease duration fail"() {
        setup:
        def leaseClient = createLeaseClient(createPathClient())

        when:
        leaseClient.acquireLease(duration)

        then:
        thrown(DataLakeStorageException)

        where:
        duration | _
        -10      | _
        10       | _
        70       | _
    }

    @Unroll
    def "Acquire file lease AC"() {
        setup:
        def fc = createPathClient()
        match = setupPathMatchCondition(fc, match)
        def mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)

        expect:
        createLeaseClient(fc)
            .acquireLeaseWithResponse(-1, mac, null, null)
            .getStatusCode() == 201

        where:
        modified        | unmodified      | match                | noneMatch
        null            | null            | null                 | null
        APISpec.oldDate | null            | null                 | null
        null            | APISpec.newDate | null                 | null
        null            | null            | APISpec.receivedEtag | null
        null            | null            | null                 | APISpec.garbageEtag
    }

    @Unroll
    def "Acquire file lease AC fail"() {
        setup:
        def fc = createPathClient()
        noneMatch = setupPathMatchCondition(fc, noneMatch)
        def mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)

        when:
        createLeaseClient(fc).acquireLeaseWithResponse(-1, mac, null, null)

        then:
        thrown(DataLakeStorageException)

        where:
        modified        | unmodified      | match               | noneMatch
        APISpec.newDate | null            | null                | null
        null            | APISpec.oldDate | null                | null
        null            | null            | APISpec.garbageEtag | null
        null            | null            | null                | APISpec.receivedEtag
    }

    def "Acquire file lease error"() {
        setup:
        def fc = fsc.getFileClient(generatePathName())

        when:
        createLeaseClient(fc).acquireLease(20)

        then:
        thrown(DataLakeStorageException)
    }

    def "Renew file lease"() {
        setup:
        def fc = createPathClient()
        def leaseID = setupPathLeaseCondition(fc, APISpec.receivedLeaseID)
        def leaseClient = createLeaseClient(fc, leaseID)

        when:
        // If running in live mode wait for the lease to expire to ensure we are actually renewing it
        sleepIfRecord(16000)
        def renewLeaseResponse = leaseClient.renewLeaseWithResponse(null, null, null)

        then:
        fc.getProperties().getLeaseState() == LeaseStateType.LEASED
        validateBasicHeaders(renewLeaseResponse.getHeaders())
        renewLeaseResponse.getValue() != null
        renewLeaseResponse.getValue() == leaseClient.getLeaseId()
    }

    def "Renew file lease min"() {
        setup:
        def fc = createPathClient()
        def leaseID = setupPathLeaseCondition(fc, APISpec.receivedLeaseID)

        expect:
        createLeaseClient(fc, leaseID)
            .renewLeaseWithResponse(null, null, null)
            .getStatusCode() == 200
    }

    @Unroll
    def "Renew file lease AC"() {
        setup:
        def fc = createPathClient()
        match = setupPathMatchCondition(fc, match)
        def leaseID = setupPathLeaseCondition(fc, APISpec.receivedLeaseID)
        def mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)

        expect:
        createLeaseClient(fc, leaseID)
            .renewLeaseWithResponse(mac, null, null)
            .getStatusCode() == 200

        where:
        modified        | unmodified      | match                | noneMatch
        null            | null            | null                 | null
        APISpec.oldDate | null            | null                 | null
        null            | APISpec.newDate | null                 | null
        null            | null            | APISpec.receivedEtag | null
        null            | null            | null                 | APISpec.garbageEtag
    }

    @Unroll
    def "Renew file lease AC fail"() {
        setup:
        def fc = createPathClient()
        noneMatch = setupPathMatchCondition(fc, noneMatch)
        def leaseID = setupPathLeaseCondition(fc, APISpec.receivedLeaseID)
        def mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)

        when:
        createLeaseClient(fc, leaseID).renewLeaseWithResponse(mac, null, null)

        then:
        thrown(DataLakeStorageException)

        where:
        modified        | unmodified      | match               | noneMatch
        APISpec.newDate | null            | null                | null
        null            | APISpec.oldDate | null                | null
        null            | null            | APISpec.garbageEtag | null
        null            | null            | null                | APISpec.receivedEtag
    }

    def "Renew file lease error"() {
        setup:
        def fc = fsc.getFileClient(generatePathName())

        when:
        createLeaseClient(fc, "id").renewLease()

        then:
        thrown(DataLakeStorageException)
    }

    def "Release file lease"() {
        setup:
        def fc = createPathClient()
        def leaseID = setupPathLeaseCondition(fc, APISpec.receivedLeaseID)
        def headers = createLeaseClient(fc, leaseID).releaseLeaseWithResponse(null, null, null).getHeaders()

        expect:
        fc.getProperties().getLeaseState() == LeaseStateType.AVAILABLE
        validateBasicHeaders(headers)
    }

    def "Release file lease min"() {
        setup:
        def fc = createPathClient()
        def leaseID = setupPathLeaseCondition(fc, APISpec.receivedLeaseID)

        expect:
        createLeaseClient(fc, leaseID).releaseLeaseWithResponse(null, null, null).getStatusCode() == 200
    }

    @Unroll
    def "Release file lease AC"() {
        setup:
        def fc = createPathClient()
        match = setupPathMatchCondition(fc, match)
        def leaseID = setupPathLeaseCondition(fc, APISpec.receivedLeaseID)
        def mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)

        expect:
        createLeaseClient(fc, leaseID).releaseLeaseWithResponse(mac, null, null).getStatusCode() == 200

        where:
        modified        | unmodified      | match                | noneMatch
        null            | null            | null                 | null
        APISpec.oldDate | null            | null                 | null
        null            | APISpec.newDate | null                 | null
        null            | null            | APISpec.receivedEtag | null
        null            | null            | null                 | APISpec.garbageEtag
    }

    @Unroll
    def "Release file lease AC fail"() {
        setup:
        def fc = createPathClient()
        noneMatch = setupPathMatchCondition(fc, noneMatch)
        def leaseID = setupPathLeaseCondition(fc, APISpec.receivedLeaseID)
        def mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)

        when:
        createLeaseClient(fc, leaseID).releaseLeaseWithResponse(mac, null, null)

        then:
        thrown(DataLakeStorageException)

        where:
        modified        | unmodified      | match               | noneMatch
        APISpec.newDate | null            | null                | null
        null            | APISpec.oldDate | null                | null
        null            | null            | APISpec.garbageEtag | null
        null            | null            | null                | APISpec.receivedEtag
    }

    def "Release file lease error"() {
        setup:
        def fc = fsc.getFileClient(generatePathName())

        when:
        createLeaseClient(fc, "id").releaseLease()

        then:
        thrown(DataLakeStorageException)
    }

    @Unroll
    def "Break file lease"() {
        setup:
        def fc = createPathClient()
        def leaseClient = createLeaseClient(fc, namer.getRandomUuid())

        when:
        leaseClient.acquireLease(leaseTime)
        def breakLeaseResponse = leaseClient.breakLeaseWithResponse(breakPeriod, null, null, null)
        def leaseState = fc.getProperties().getLeaseState()

        then:
        leaseState == LeaseStateType.BROKEN || leaseState == LeaseStateType.BREAKING
        breakLeaseResponse.getValue() <= remainingTime
        validateBasicHeaders(breakLeaseResponse.getHeaders())

        where:
        leaseTime | breakPeriod | remainingTime
        -1        | null        | 0
        -1        | 20          | 25
        20        | 15          | 16
    }

    def "Break file lease min"() {
        setup:
        def fc = createPathClient()
        setupPathLeaseCondition(fc, APISpec.receivedLeaseID)

        expect:
        createLeaseClient(fc).breakLeaseWithResponse(null, null, null, null).getStatusCode() == 202
    }

    @Unroll
    def "Break file lease AC"() {
        setup:
        def fc = createPathClient()
        match = setupPathMatchCondition(fc, match)
        setupPathLeaseCondition(fc, APISpec.receivedLeaseID)
        def mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)

        expect:
        createLeaseClient(fc).breakLeaseWithResponse(null, mac, null, null).getStatusCode() == 202

        where:
        modified        | unmodified      | match                | noneMatch
        null            | null            | null                 | null
        APISpec.oldDate | null            | null                 | null
        null            | APISpec.newDate | null                 | null
        null            | null            | APISpec.receivedEtag | null
        null            | null            | null                 | APISpec.garbageEtag
    }

    @Unroll
    def "Break file lease AC fail"() {
        setup:
        def fc = createPathClient()
        noneMatch = setupPathMatchCondition(fc, noneMatch)
        setupPathLeaseCondition(fc, APISpec.receivedLeaseID)
        def mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)

        when:
        createLeaseClient(fc).breakLeaseWithResponse(null, mac, null, null)

        then:
        thrown(DataLakeStorageException)

        where:
        modified        | unmodified      | match               | noneMatch
        APISpec.newDate | null            | null                | null
        null            | APISpec.oldDate | null                | null
        null            | null            | APISpec.garbageEtag | null
        null            | null            | null                | APISpec.receivedEtag
    }

    def "Break file lease error"() {
        setup:
        def fc = createPathClient()

        when:
        createLeaseClient(fc).breakLease()

        then:
        thrown(DataLakeStorageException)
    }

    def "Change file lease"() {
        setup:
        def fc = createPathClient()
        def leaseClient = createLeaseClient(fc, namer.getRandomUuid())
        leaseClient.acquireLease(15)

        when:
        def newLeaseId = namer.getRandomUuid()
        def changeLeaseResponse = leaseClient.changeLeaseWithResponse(newLeaseId, null, null, null)

        then:
        validateBasicHeaders(changeLeaseResponse.getHeaders())
        changeLeaseResponse.getValue() == leaseClient.getLeaseId()

        expect:
        createLeaseClient(fc, changeLeaseResponse.getValue())
            .releaseLeaseWithResponse(null, null, null).getStatusCode() == 200
    }

    def "Change file lease min"() {
        setup:
        def fc = createPathClient()
        def leaseID = setupPathLeaseCondition(fc, APISpec.receivedLeaseID)

        expect:
        createLeaseClient(fc, leaseID).changeLeaseWithResponse(namer.getRandomUuid(), null, null, null).getStatusCode() == 200
    }

    @Unroll
    def "Change file lease AC"() {
        setup:
        def fc = createPathClient()
        match = setupPathMatchCondition(fc, match)
        String leaseID = setupPathLeaseCondition(fc, APISpec.receivedLeaseID)
        def mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)

        expect:
        createLeaseClient(fc, leaseID).changeLeaseWithResponse(namer.getRandomUuid(), mac, null, null).getStatusCode() == 200

        where:
        modified        | unmodified      | match                | noneMatch
        null            | null            | null                 | null
        APISpec.oldDate | null            | null                 | null
        null            | APISpec.newDate | null                 | null
        null            | null            | APISpec.receivedEtag | null
        null            | null            | null                 | APISpec.garbageEtag
    }

    @Unroll
    def "Change file lease AC fail"() {
        setup:
        def fc = createPathClient()
        noneMatch = setupPathMatchCondition(fc, noneMatch)
        String leaseID = setupPathLeaseCondition(fc, APISpec.receivedLeaseID)
        def mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)

        when:
        createLeaseClient(fc, leaseID).changeLeaseWithResponse(namer.getRandomUuid(), mac, null, null)

        then:
        thrown(DataLakeStorageException)

        where:
        modified        | unmodified      | match               | noneMatch
        APISpec.newDate | null            | null                | null
        null            | APISpec.oldDate | null                | null
        null            | null            | APISpec.garbageEtag | null
        null            | null            | null                | APISpec.receivedEtag
    }

    def "Change file lease error"() {
        setup:
        def fc = fsc.getFileClient(generatePathName())

        when:
        createLeaseClient(fc, "id").changeLease("id")

        then:
        thrown(DataLakeStorageException)
    }


    @Unroll
    def "Acquire file system lease"() {
        setup:
        def leaseClient = createLeaseClient(fsc, proposedID)
        def leaseResponse = leaseClient.acquireLeaseWithResponse(leaseTime, null, null, null)

        when:
        def properties = fsc.getProperties()

        then:
        leaseResponse.getValue() != null
        validateBasicHeaders(leaseResponse.getHeaders())
        leaseResponse.getValue() == leaseClient.getLeaseId()
        properties.getLeaseState() == leaseState
        properties.getLeaseDuration() == leaseDuration

        where:
        proposedID                   | leaseTime || leaseState            | leaseDuration
        null                         | -1        || LeaseStateType.LEASED | LeaseDurationType.INFINITE
        null                         | 25        || LeaseStateType.LEASED | LeaseDurationType.FIXED
        UUID.randomUUID().toString() | -1        || LeaseStateType.LEASED | LeaseDurationType.INFINITE
    }

    def "Acquire file system lease min"() {
        expect:
        createLeaseClient(fsc).acquireLeaseWithResponse(-1, null, null, null).getStatusCode() == 201
    }

    @Unroll
    def "Acquire file system lease duration fail"() {
        setup:
        def leaseClient = createLeaseClient(fsc)

        when:
        leaseClient.acquireLease(duration)

        then:
        thrown(DataLakeStorageException)

        where:
        duration | _
        -10      | _
        10       | _
        70       | _
    }

    @Unroll
    def "Acquire file system lease AC"() {
        setup:
        def mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)

        expect:
        createLeaseClient(fsc).acquireLeaseWithResponse(-1, mac, null, null).getStatusCode() == 201

        where:
        modified        | unmodified      | match                | noneMatch
        null            | null            | null                 | null
        APISpec.oldDate | null            | null                 | null
        null            | APISpec.newDate | null                 | null
        null            | null            | APISpec.receivedEtag | null
        null            | null            | null                 | APISpec.garbageEtag
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

    def "Change file system lease error"() {
        setup:
        fsc = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName())

        when:
        createLeaseClient(fsc, "id").changeLease("id")

        then:
        thrown(DataLakeStorageException)
    }
}
