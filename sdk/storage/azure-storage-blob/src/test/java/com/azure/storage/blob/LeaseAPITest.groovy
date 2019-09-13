// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob


import com.azure.storage.blob.models.LeaseDurationType
import com.azure.storage.blob.models.LeaseStateType
import com.azure.storage.blob.models.ModifiedAccessConditions
import com.azure.storage.blob.models.StorageException
import spock.lang.Unroll

class LeaseAPITest extends APISpec {
    @Unroll
    def "Acquire blob lease"() {
        setup:
        def bc = cc.getBlobClient(generateBlobName())
        def leaseClient = new LeaseClient(bc, proposedID)

        when:
        def leaseId = leaseClient.acquireLease(leaseTime)

        then:
        leaseId != null

        when:
        def response = bc.getPropertiesWithResponse(null, null, null)
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

    def "Acquire blob lease min"() {
        expect:
        new LeaseClient(cc.getBlobClient(generateBlobName()))
            .acquireLeaseWithResponse(-1, null, null, null)
            .getStatusCode() == 201
    }

    @Unroll
    def "Acquire blob lease AC"() {
        setup:
        def bc = cc.getBlobClient(generateBlobName())
        match = setupBlobMatchCondition(bc, match)
        def mac = new ModifiedAccessConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)

        expect:
        new LeaseClient(bc)
            .acquireLeaseWithResponse(-1, mac, null, null)
            .getStatusCode() == 201

        where:
        modified | unmodified | match        | noneMatch
        null     | null       | null         | null
        oldDate  | null       | null         | null
        null     | newDate    | null         | null
        null     | null       | receivedEtag | null
        null     | null       | null         | garbageEtag
    }

    @Unroll
    def "Acquire blob lease AC fail"() {
        setup:
        def bc = cc.getBlobClient(generateBlobName())
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        def mac = new ModifiedAccessConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)

        when:
        new LeaseClient(bc).acquireLeaseWithResponse(-1, mac, null, null)

        then:
        thrown(StorageException)

        where:
        modified | unmodified | match       | noneMatch
        newDate  | null       | null        | null
        null     | oldDate    | null        | null
        null     | null       | garbageEtag | null
        null     | null       | null        | receivedEtag
    }

    def "Acquire blob lease error"() {
        setup:
        def bc = cc.getBlockBlobClient(generateBlobName())

        when:
        new LeaseClient(bc).acquireLease(20)

        then:
        thrown(StorageException)
    }

    def "Renew blob lease"() {
        setup:
        def bc = cc.getBlobClient(generateBlobName())
        def leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)

        // If running in live mode wait for the lease to expire to ensure we are actually renewing it
        sleepIfRecord(16000)
        def renewLeaseResponse = new LeaseClient(bc, leaseID).renewLeaseWithResponse(null, null, null)

        expect:
        bc.getProperties().getLeaseState() == LeaseStateType.LEASED
        validateBasicHeaders(renewLeaseResponse.getHeaders())
        renewLeaseResponse.getValue() != null
    }

    def "Renew blob lease min"() {
        setup:
        def bc = cc.getBlobClient(generateBlobName())
        def leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)

        expect:
        new LeaseClient(bc, leaseID)
            .renewLeaseWithResponse(null, null, null)
            .getStatusCode() == 200
    }

    @Unroll
    def "Renew blob lease AC"() {
        setup:
        def bc = cc.getBlobClient(generateBlobName())
        match = setupBlobMatchCondition(bc, match)
        def leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)
        def mac = new ModifiedAccessConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)

        expect:
        new LeaseClient(bc, leaseID)
            .renewLeaseWithResponse(mac, null, null)
            .getStatusCode() == 200

        where:
        modified | unmodified | match        | noneMatch
        null     | null       | null         | null
        oldDate  | null       | null         | null
        null     | newDate    | null         | null
        null     | null       | receivedEtag | null
        null     | null       | null         | garbageEtag
    }

    @Unroll
    def "Renew blob lease AC fail"() {
        setup:
        def bc = cc.getBlobClient(generateBlobName())
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        def leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)
        def mac = new ModifiedAccessConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)

        when:
        new LeaseClient(bc, leaseID).renewLeaseWithResponse(mac, null, null)

        then:
        thrown(StorageException)

        where:
        modified | unmodified | match       | noneMatch
        newDate  | null       | null        | null
        null     | oldDate    | null        | null
        null     | null       | garbageEtag | null
        null     | null       | null        | receivedEtag
    }

    def "Renew blob lease error"() {
        setup:
        def bc = cc.getBlockBlobClient(generateBlobName())

        when:
        new LeaseClient(bc, "id").renewLease()

        then:
        thrown(StorageException)
    }

    def "Release blob lease"() {
        setup:
        def bc = cc.getBlobClient(generateBlobName())
        def leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)
        def headers = new LeaseClient(bc, leaseID).releaseLeaseWithResponse(null, null, null).getHeaders()

        expect:
        bc.getProperties().getLeaseState() == LeaseStateType.AVAILABLE
        validateBasicHeaders(headers)
    }

    def "Release blob lease min"() {
        setup:
        def bc = cc.getBlobClient(generateBlobName())
        def leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)

        expect:
        new LeaseClient(bc, leaseID).releaseLeaseWithResponse(null, null, null).getStatusCode() == 200
    }

    @Unroll
    def "Release blob lease AC"() {
        setup:
        def bc = cc.getBlobClient(generateBlobName())
        match = setupBlobMatchCondition(bc, match)
        def leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)
        def mac = new ModifiedAccessConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)

        expect:
        new LeaseClient(bc, leaseID).releaseLeaseWithResponse(mac, null, null).getStatusCode() == 200

        where:
        modified | unmodified | match        | noneMatch
        null     | null       | null         | null
        oldDate  | null       | null         | null
        null     | newDate    | null         | null
        null     | null       | receivedEtag | null
        null     | null       | null         | garbageEtag
    }

    @Unroll
    def "Release blob lease AC fail"() {
        setup:
        def bc = cc.getBlobClient(generateBlobName())
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        def leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)
        def mac = new ModifiedAccessConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)

        when:
        new LeaseClient(bc, leaseID).releaseLeaseWithResponse(mac, null, null)

        then:
        thrown(StorageException)

        where:
        modified | unmodified | match       | noneMatch
        newDate  | null       | null        | null
        null     | oldDate    | null        | null
        null     | null       | garbageEtag | null
        null     | null       | null        | receivedEtag
    }

    def "Release blob lease error"() {
        setup:
        def bc = cc.getBlockBlobClient(generateBlobName())

        when:
        new LeaseClient(bc, "id").releaseLease()

        then:
        thrown(StorageException)
    }

    @Unroll
    def "Break blob lease"() {
        setup:
        def bc = cc.getBlobClient(generateBlobName())
        def leaseClient = new LeaseClient(bc, getRandomUUID())

        when:
        leaseClient.acquireLease(leaseTime)
        def breakLeaseResponse = leaseClient.breakLeaseWithResponse(breakPeriod, null, null, null)
        def leaseState = bc.getProperties().getLeaseState()

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

    def "Break blob lease min"() {
        setup:
        def bc = cc.getBlobClient(generateBlobName())
        setupBlobLeaseCondition(bc, receivedLeaseID)

        expect:
        new LeaseClient(bc).breakLeaseWithResponse(null, null, null, null).getStatusCode() == 202
    }

    @Unroll
    def "Break blob lease AC"() {
        setup:
        def bc = cc.getBlobClient(generateBlobName())
        match = setupBlobMatchCondition(bc, match)
        setupBlobLeaseCondition(bc, receivedLeaseID)
        def mac = new ModifiedAccessConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)

        expect:
        new LeaseClient(bc).breakLeaseWithResponse(null, mac, null, null).getStatusCode() == 202

        where:
        modified | unmodified | match        | noneMatch
        null     | null       | null         | null
        oldDate  | null       | null         | null
        null     | newDate    | null         | null
        null     | null       | receivedEtag | null
        null     | null       | null         | garbageEtag
    }

    @Unroll
    def "Break blob lease AC fail"() {
        setup:
        def bc = cc.getBlobClient(generateBlobName())
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        setupBlobLeaseCondition(bc, receivedLeaseID)
        def mac = new ModifiedAccessConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)

        when:
        new LeaseClient(bc).breakLeaseWithResponse(null, mac, null, null)

        then:
        thrown(StorageException)

        where:
        modified | unmodified | match       | noneMatch
        newDate  | null       | null        | null
        null     | oldDate    | null        | null
        null     | null       | garbageEtag | null
        null     | null       | null        | receivedEtag
    }

    def "Break blob lease error"() {
        setup:
        defvbc = cc.getBlockBlobClient(generateBlobName())

        when:
        new LeaseClient(bc).breakLease()

        then:
        thrown(StorageException)
    }

    def "Change blob lease"() {
        setup:
        def bc = cc.getBlobClient(generateBlobName())
        def leaseClient = new LeaseClient(bc, getRandomUUID())
        leaseClient.acquireLease(15)
        def changeLeaseResponse = leaseClient.changeLeaseWithResponse(getRandomUUID(), null, null, null)
        def leaseClient2 = new LeaseClient(bc, changeLeaseResponse.getValue())

        expect:
        leaseClient2.releaseLeaseWithResponse(null, null, null).getStatusCode() == 200
        validateBasicHeaders(changeLeaseResponse.getHeaders())
    }

    def "Change blob lease min"() {
        setup:
        def bc = cc.getBlobClient(generateBlobName())
        def leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)

        expect:
        new LeaseClient(bc, leaseID).changeLeaseWithResponse(getRandomUUID(), null, null, null).getStatusCode() == 200
    }

    @Unroll
    def "Change blob lease AC"() {
        setup:
        def bc = cc.getBlobClient(generateBlobName())
        match = setupBlobMatchCondition(bc, match)
        String leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)
        def mac = new ModifiedAccessConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)

        expect:
        new LeaseClient(bc, leaseID).changeLeaseWithResponse(getRandomUUID(), mac, null, null).getStatusCode() == 200

        where:
        modified | unmodified | match        | noneMatch
        null     | null       | null         | null
        oldDate  | null       | null         | null
        null     | newDate    | null         | null
        null     | null       | receivedEtag | null
        null     | null       | null         | garbageEtag
    }

    @Unroll
    def "Change blob lease AC fail"() {
        setup:
        def bc = cc.getBlobClient(generateBlobName())
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        String leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)
        def mac = new ModifiedAccessConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)

        when:
        new LeaseClient(bc, leaseID).changeLeaseWithResponse(getRandomUUID(), mac, null, null)

        then:
        thrown(StorageException)

        where:
        modified | unmodified | match       | noneMatch
        newDate  | null       | null        | null
        null     | oldDate    | null        | null
        null     | null       | garbageEtag | null
        null     | null       | null        | receivedEtag
    }

    def "Change blob lease error"() {
        setup:
        def bc = cc.getBlockBlobClient(generateBlobName())

        when:
        new LeaseClient(bc, "id").changeLease("id")

        then:
        thrown(StorageException)
    }













    @Unroll
    def "Acquire container lease"() {
        setup:
        def leaseResponse = new LeaseClient(cc, proposedID).acquireLeaseWithResponse(leaseTime, null, null, null)

        when:
        def properties = cc.getProperties()

        then:
        leaseResponse.getValue() != null
        validateBasicHeaders(leaseResponse.getHeaders())
        properties.getLeaseState() == leaseState
        properties.getLeaseDuration() == leaseDuration

        where:
        proposedID                   | leaseTime || leaseState            | leaseDuration
        null                         | -1        || LeaseStateType.LEASED | LeaseDurationType.INFINITE
        null                         | 25        || LeaseStateType.LEASED | LeaseDurationType.FIXED
        UUID.randomUUID().toString() | -1        || LeaseStateType.LEASED | LeaseDurationType.INFINITE
    }

    def "Acquire container lease min"() {
        expect:
        new LeaseClient(cc).acquireLeaseWithResponse(-1, null, null, null).getStatusCode() == 201
    }

    @Unroll
    def "Acquire container lease AC"() {
        setup:
        def mac = new ModifiedAccessConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified)

        expect:
        new LeaseClient(cc).acquireLeaseWithResponse( -1, mac, null, null).getStatusCode() == 201

        where:
        modified | unmodified
        null     | null
        oldDate  | null
        null     | newDate
    }

    @Unroll
    def "Acquire container lease AC fail"() {
        setup:
        def mac = new ModifiedAccessConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified)

        when:
        new LeaseClient(cc).acquireLeaseWithResponse(-1, mac, null, null)

        then:
        thrown(StorageException)

        where:
        modified | unmodified
        newDate  | null
        null     | oldDate
    }

    @Unroll
    def "Acquire container lease AC illegal"() {
        setup:
        def mac = new ModifiedAccessConditions().setIfMatch(match).setIfNoneMatch(noneMatch)

        when:
        new LeaseClient(cc).acquireLeaseWithResponse(-1, mac, null, null)

        then:
        thrown(UnsupportedOperationException)

        where:
        match        | noneMatch
        receivedEtag | null
        null         | garbageEtag
    }

    def "Acquire container lease error"() {
        setup:
        cc = primaryBlobServiceClient.getContainerClient(generateContainerName())

        when:
        new LeaseClient(cc).acquireLease(50)

        then:
        thrown(StorageException)
    }

    def "Renew container lease"() {
        setup:
        def leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)

        // If running in live mode wait for the lease to expire to ensure we are actually renewing it
        sleepIfRecord(16000)
        def renewLeaseResponse = new LeaseClient(cc, leaseID).renewLeaseWithResponse(null, null, null)

        expect:
        cc.getProperties().getLeaseState() == LeaseStateType.LEASED
        validateBasicHeaders(renewLeaseResponse.getHeaders())
    }

    def "Renew container lease min"() {
        setup:
        def leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)

        expect:
        new LeaseClient(cc, leaseID).renewLeaseWithResponse(null, null, null).getStatusCode() == 200
    }

    @Unroll
    def "Renew container lease AC"() {
        setup:
        def leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)
        def mac = new ModifiedAccessConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified)

        expect:
        new LeaseClient(cc, leaseID).renewLeaseWithResponse(mac, null, null).getStatusCode() == 200

        where:
        modified | unmodified
        null     | null
        oldDate  | null
        null     | newDate
    }

    @Unroll
    def "Renew container lease AC fail"() {
        setup:
        def leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)
        def mac = new ModifiedAccessConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified)

        when:
        new LeaseClient(cc, leaseID).renewLeaseWithResponse(mac, null, null)

        then:
        thrown(StorageException)

        where:
        modified | unmodified
        newDate  | null
        null     | oldDate
    }

    @Unroll
    def "Renew container lease AC illegal"() {
        setup:
        def mac = new ModifiedAccessConditions().setIfMatch(match).setIfNoneMatch(noneMatch)

        when:
        new LeaseClient(cc, receivedEtag).renewLeaseWithResponse(mac, null, null)

        then:
        thrown(UnsupportedOperationException)

        where:
        match        | noneMatch
        receivedEtag | null
        null         | garbageEtag
    }

    def "Renew container lease error"() {
        setup:
        cc = primaryBlobServiceClient.getContainerClient(generateContainerName())

        when:
        new LeaseClient(cc, "id").renewLease()

        then:
        thrown(StorageException)
    }

    def "Release container lease"() {
        setup:
        def leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)

        def releaseLeaseResponse = new LeaseClient(cc, leaseID).releaseLeaseWithResponse(null, null, null)

        expect:
        cc.getProperties().getLeaseState() == LeaseStateType.AVAILABLE
        validateBasicHeaders(releaseLeaseResponse.getHeaders())
    }

    def "Release container lease min"() {
        setup:
        def leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)

        expect:
        new LeaseClient(cc, leaseID).releaseLeaseWithResponse(null, null, null).getStatusCode() == 200
    }

    @Unroll
    def "Release container lease AC"() {
        setup:
        def leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)
        def mac = new ModifiedAccessConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified)

        expect:
        new LeaseClient(cc, leaseID).releaseLeaseWithResponse(mac, null, null).getStatusCode() == 200

        where:
        modified | unmodified
        null     | null
        oldDate  | null
        null     | newDate
    }

    @Unroll
    def "Release container lease AC fail"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)
        def mac = new ModifiedAccessConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified)

        when:
        cc.releaseLeaseWithResponse(leaseID, mac, null, null)

        then:
        thrown(StorageException)

        where:
        modified | unmodified
        newDate  | null
        null     | oldDate
    }

    @Unroll
    def "Release container lease AC illegal"() {
        setup:
        def mac = new ModifiedAccessConditions().setIfMatch(match).setIfNoneMatch(noneMatch)

        when:
        new LeaseClient(cc, receivedLeaseID).releaseLeaseWithResponse(mac, null, null)

        then:
        thrown(UnsupportedOperationException)

        where:
        match        | noneMatch
        receivedEtag | null
        null         | garbageEtag
    }

    def "Release container lease error"() {
        setup:
        cc = primaryBlobServiceClient.getContainerClient(generateContainerName())

        when:
        new LeaseClient(cc, "id").releaseLease()

        then:
        thrown(StorageException)
    }

    @Unroll
    def "Break container lease"() {
        setup:
        def leaseClient = new LeaseClient(cc, getRandomUUID())
        leaseClient.acquireLease(leaseTime)

        def breakLeaseResponse = leaseClient.breakLeaseWithResponse(breakPeriod, null, null, null)
        def state = cc.getProperties().getLeaseState()

        expect:
        state == LeaseStateType.BROKEN || state == LeaseStateType.BREAKING
        breakLeaseResponse.getValue() <= remainingTime
        validateBasicHeaders(breakLeaseResponse.getHeaders())
        if (breakPeriod != null) {
            // If running in live mode wait for the lease to break so we can delete the container after the test completes
            sleepIfRecord(breakPeriod * 1000)
        }

        where:
        leaseTime | breakPeriod | remainingTime
        -1        | null        | 0
        -1        | 20          | 25
        20        | 15          | 16

    }

    def "Break container lease min"() {
        setup:
        setupContainerLeaseCondition(cc, receivedLeaseID)

        expect:
        new LeaseClient(cc).breakLeaseWithResponse(null, null, null, null).getStatusCode() == 202
    }

    @Unroll
    def "Break container lease AC"() {
        setup:
        setupContainerLeaseCondition(cc, receivedLeaseID)
        def mac = new ModifiedAccessConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified)

        expect:
        new LeaseClient(cc).breakLeaseWithResponse(null, mac, null, null).getStatusCode() == 202

        where:
        modified | unmodified
        null     | null
        oldDate  | null
        null     | newDate
    }

    @Unroll
    def "Break container lease AC fail"() {
        setup:
        setupContainerLeaseCondition(cc, receivedLeaseID)
        def mac = new ModifiedAccessConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified)

        when:
        new LeaseClient(cc).breakLeaseWithResponse(null, mac, null, null)

        then:
        thrown(StorageException)

        where:
        modified | unmodified
        newDate  | null
        null     | oldDate
    }

    @Unroll
    def "Break container lease AC illegal"() {
        setup:
        def mac = new ModifiedAccessConditions().setIfMatch(match).setIfNoneMatch(noneMatch)

        when:
        new LeaseClient(cc).breakLeaseWithResponse(null, mac, null, null)

        then:
        thrown(UnsupportedOperationException)

        where:
        match        | noneMatch
        receivedEtag | null
        null         | garbageEtag
    }

    def "Break container lease error"() {
        setup:
        cc = primaryBlobServiceClient.getContainerClient(generateContainerName())

        when:
        new LeaseClient(cc).breakLease()

        then:
        thrown(StorageException)
    }

    def "Change container lease"() {
        setup:
        def leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)
        def leaseClient = new LeaseClient(cc, leaseID)
        def changeLeaseResponse = leaseClient.changeLeaseWithResponse(getRandomUUID(), null, null, null)
        leaseID = changeLeaseResponse.getValue()

        expect:
        new LeaseClient(cc, leaseID).releaseLeaseWithResponse(null, null, null).getStatusCode() == 200
        validateBasicHeaders(changeLeaseResponse.getHeaders())
    }

    def "Change container lease min"() {
        setup:
        def leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)

        expect:
        new LeaseClient(cc, leaseID).changeLeaseWithResponse(getRandomUUID(), null, null, null).getStatusCode() == 200
    }

    @Unroll
    def "Change container lease AC"() {
        setup:
        def leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)
        def mac = new ModifiedAccessConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified)

        expect:
        new LeaseClient(cc, leaseID).changeLeaseWithResponse(getRandomUUID(), mac, null, null).getStatusCode() == 200

        where:
        modified | unmodified
        null     | null
        oldDate  | null
        null     | newDate
    }

    @Unroll
    def "Change container lease AC fail"() {
        setup:
        def leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)
        def mac = new ModifiedAccessConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified)

        when:
        new LeaseClient(cc, leaseID).changeLeaseWithResponse(getRandomUUID(), mac, null, null)

        then:
        thrown(StorageException)

        where:
        modified | unmodified
        newDate  | null
        null     | oldDate
    }

    @Unroll
    def "Change container lease AC illegal"() {
        setup:
        def mac = new ModifiedAccessConditions().setIfMatch(match).setIfNoneMatch(noneMatch)

        when:
        new LeaseClient(cc, receivedLeaseID).changeLeaseWithResponse(garbageLeaseID, mac, null, null)

        then:
        thrown(UnsupportedOperationException)

        where:
        match        | noneMatch
        receivedEtag | null
        null         | garbageEtag
    }

    def "Change container lease error"() {
        setup:
        cc = primaryBlobServiceClient.getContainerClient(generateContainerName())

        when:
        new LeaseClient(cc, "id").changeLease("id")

        then:
        thrown(StorageException)
    }
}
