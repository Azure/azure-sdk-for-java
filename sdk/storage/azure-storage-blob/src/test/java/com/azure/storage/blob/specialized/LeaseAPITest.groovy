// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized

import com.azure.storage.blob.APISpec
import com.azure.storage.blob.BlobServiceVersion
import com.azure.storage.blob.models.BlobLeaseRequestConditions
import com.azure.storage.blob.models.LeaseDurationType
import com.azure.storage.blob.models.LeaseStateType

import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.blob.options.BlobAcquireLeaseOptions
import com.azure.storage.blob.options.BlobBreakLeaseOptions
import com.azure.storage.blob.options.BlobChangeLeaseOptions
import com.azure.storage.blob.options.BlobReleaseLeaseOptions
import com.azure.storage.blob.options.BlobRenewLeaseOptions
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion
import spock.lang.Unroll

import java.time.Duration

class LeaseAPITest extends APISpec {
    private BlobClientBase createBlobClient() {
        def bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        bc.upload(data.defaultInputStream, data.defaultDataSize)

        return bc
    }

    @Unroll
    def "Acquire blob lease"() {
        setup:
        def bc = createBlobClient()
        def leaseClient = createLeaseClient(bc, proposedID)

        expect:
        if (proposedID != null) {
            assert leaseClient.getLeaseId() == proposedID
        }

        when:
        def leaseId = leaseClient.acquireLease(leaseTime)

        then:
        leaseId != null
        leaseClient.getLeaseId() == leaseId

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
        createLeaseClient(createBlobClient())
            .acquireLeaseWithResponse(new BlobAcquireLeaseOptions(-1), null, null)
            .getStatusCode() == 201
    }

    @Unroll
    def "Acquire blob lease duration fail"() {
        setup:
        def leaseClient = createLeaseClient(createBlobClient())

        when:
        leaseClient.acquireLease(duration)

        then:
        thrown(BlobStorageException)

        where:
        duration | _
        -10      | _
        10       | _
        70       | _
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "Acquire blob lease AC"() {
        setup:
        def bc = createBlobClient()
        def t = new HashMap<String, String>()
        t.put("foo", "bar")
        bc.setTags(t)
        match = setupBlobMatchCondition(bc, match)
        def mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setTagsConditions(tags)

        expect:
        createLeaseClient(bc)
            .acquireLeaseWithResponse(new BlobAcquireLeaseOptions(-1).setRequestConditions(mac), null, null)
            .getStatusCode() == 201

        where:
        modified | unmodified | match        | noneMatch    | tags
        null     | null       | null         | null         | null
        oldDate  | null       | null         | null         | null
        null     | newDate    | null         | null         | null
        null     | null       | receivedEtag | null         | null
        null     | null       | null         | garbageEtag  | null
        null     | null       | null         | null         | "\"foo\" = 'bar'"
    }

    @Unroll
    def "Acquire blob lease AC fail"() {
        setup:
        def bc = createBlobClient()
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        def mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setTagsConditions(tags)

        when:
        createLeaseClient(bc).acquireLeaseWithResponse(new BlobAcquireLeaseOptions(-1).setRequestConditions(mac), null, null)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match        | noneMatch    | tags
        newDate  | null       | null         | null         | null
        null     | oldDate    | null         | null         | null
        null     | null       | garbageEtag  | null         | null
        null     | null       | null         | receivedEtag | null
        null     | null       | null         | null         | "\"notfoo\" = 'notbar'"
    }

    def "Acquire blob lease error"() {
        setup:
        def bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        createLeaseClient(bc).acquireLease(20)

        then:
        thrown(BlobStorageException)
    }

    def "Renew blob lease"() {
        setup:
        def bc = createBlobClient()
        def leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)
        def leaseClient = createLeaseClient(bc, leaseID)

        when:
        // If running in live mode wait for the lease to expire to ensure we are actually renewing it
        sleepIfRecord(16000)
        def renewLeaseResponse = leaseClient.renewLeaseWithResponse(new BlobRenewLeaseOptions(), null, null)

        then:
        bc.getProperties().getLeaseState() == LeaseStateType.LEASED
        validateBasicHeaders(renewLeaseResponse.getHeaders())
        renewLeaseResponse.getValue() != null
        renewLeaseResponse.getValue() == leaseClient.getLeaseId()
    }

    def "Renew blob lease min"() {
        setup:
        def bc = createBlobClient()
        def leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)

        expect:
        createLeaseClient(bc, leaseID)
            .renewLeaseWithResponse(new BlobRenewLeaseOptions(), null, null)
            .getStatusCode() == 200
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "Renew blob lease AC"() {
        setup:
        def bc = createBlobClient()
        def t = new HashMap<String, String>()
        t.put("foo", "bar")
        bc.setTags(t)
        match = setupBlobMatchCondition(bc, match)
        def leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)
        def mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setTagsConditions(tags)

        expect:
        createLeaseClient(bc, leaseID)
            .renewLeaseWithResponse(new BlobRenewLeaseOptions().setRequestConditions(mac), null, null)
            .getStatusCode() == 200

        where:
        modified | unmodified | match        | noneMatch    | tags
        null     | null       | null         | null         | null
        oldDate  | null       | null         | null         | null
        null     | newDate    | null         | null         | null
        null     | null       | receivedEtag | null         | null
        null     | null       | null         | garbageEtag  | null
        null     | null       | null         | null         | "\"foo\" = 'bar'"
    }

    @Unroll
    def "Renew blob lease AC fail"() {
        setup:
        def bc = createBlobClient()
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        def leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)
        def mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setTagsConditions(tags)

        when:
        createLeaseClient(bc, leaseID).renewLeaseWithResponse(new BlobRenewLeaseOptions().setRequestConditions(mac), null, null)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match        | noneMatch    | tags
        newDate  | null       | null         | null         | null
        null     | oldDate    | null         | null         | null
        null     | null       | garbageEtag  | null         | null
        null     | null       | null         | receivedEtag | null
        null     | null       | null         | null         | "\"notfoo\" = 'notbar'"
    }

    def "Renew blob lease error"() {
        setup:
        def bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        createLeaseClient(bc, "id").renewLease()

        then:
        thrown(BlobStorageException)
    }

    def "Release blob lease"() {
        setup:
        def bc = createBlobClient()
        def leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)
        def headers = createLeaseClient(bc, leaseID).releaseLeaseWithResponse(new BlobReleaseLeaseOptions(), null, null).getHeaders()

        expect:
        bc.getProperties().getLeaseState() == LeaseStateType.AVAILABLE
        validateBasicHeaders(headers)
    }

    def "Release blob lease min"() {
        setup:
        def bc = createBlobClient()
        def leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)

        expect:
        createLeaseClient(bc, leaseID).releaseLeaseWithResponse(new BlobReleaseLeaseOptions(), null, null).getStatusCode() == 200
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "Release blob lease AC"() {
        setup:
        def bc = createBlobClient()
        def t = new HashMap<String, String>()
        t.put("foo", "bar")
        bc.setTags(t)
        match = setupBlobMatchCondition(bc, match)
        def leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)
        def mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setTagsConditions(tags)

        expect:
        createLeaseClient(bc, leaseID).releaseLeaseWithResponse(new BlobReleaseLeaseOptions().setRequestConditions(mac), null, null).getStatusCode() == 200

        where:
        modified | unmodified | match        | noneMatch    | tags
        null     | null       | null         | null         | null
        oldDate  | null       | null         | null         | null
        null     | newDate    | null         | null         | null
        null     | null       | receivedEtag | null         | null
        null     | null       | null         | garbageEtag  | null
        null     | null       | null         | null         | "\"foo\" = 'bar'"
    }

    @Unroll
    def "Release blob lease AC fail"() {
        setup:
        def bc = createBlobClient()
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        def leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)
        def mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setTagsConditions(tags)

        when:
        createLeaseClient(bc, leaseID).releaseLeaseWithResponse(new BlobReleaseLeaseOptions().setRequestConditions(mac), null, null)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match        | noneMatch    | tags
        newDate  | null       | null         | null         | null
        null     | oldDate    | null         | null         | null
        null     | null       | garbageEtag  | null         | null
        null     | null       | null         | receivedEtag | null
        null     | null       | null         | null         | "\"notfoo\" = 'notbar'"
    }

    def "Release blob lease error"() {
        setup:
        def bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        createLeaseClient(bc, "id").releaseLease()

        then:
        thrown(BlobStorageException)
    }

    @Unroll
    def "Break blob lease"() {
        setup:
        def bc = createBlobClient()
        def leaseClient = createLeaseClient(bc, namer.getRandomUuid())

        when:
        leaseClient.acquireLease(leaseTime)
        def breakLeaseResponse = leaseClient.breakLeaseWithResponse(new BlobBreakLeaseOptions().setBreakPeriod(breakPeriod == null ? null : Duration.ofSeconds(breakPeriod)), null, null)
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
        def bc = createBlobClient()
        setupBlobLeaseCondition(bc, receivedLeaseID)

        expect:
        createLeaseClient(bc).breakLeaseWithResponse(new BlobBreakLeaseOptions(), null, null).getStatusCode() == 202
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "Break blob lease AC"() {
        setup:
        def bc = createBlobClient()
        def t = new HashMap<String, String>()
        t.put("foo", "bar")
        bc.setTags(t)
        match = setupBlobMatchCondition(bc, match)
        setupBlobLeaseCondition(bc, receivedLeaseID)
        def mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setTagsConditions(tags)

        expect:
        createLeaseClient(bc).breakLeaseWithResponse(new BlobBreakLeaseOptions().setRequestConditions(mac), null, null).getStatusCode() == 202

        where:
        modified | unmodified | match        | noneMatch    | tags
        null     | null       | null         | null         | null
        oldDate  | null       | null         | null         | null
        null     | newDate    | null         | null         | null
        null     | null       | receivedEtag | null         | null
        null     | null       | null         | garbageEtag  | null
        null     | null       | null         | null         | "\"foo\" = 'bar'"
    }

    @Unroll
    def "Break blob lease AC fail"() {
        setup:
        def bc = createBlobClient()
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        setupBlobLeaseCondition(bc, receivedLeaseID)
        def mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setTagsConditions(tags)

        when:
        createLeaseClient(bc).breakLeaseWithResponse(new BlobBreakLeaseOptions().setRequestConditions(mac), null, null)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match        | noneMatch    | tags
        newDate  | null       | null         | null         | null
        null     | oldDate    | null         | null         | null
        null     | null       | garbageEtag  | null         | null
        null     | null       | null         | receivedEtag | null
        null     | null       | null         | null         | "\"notfoo\" = 'notbar'"
    }

    def "Break blob lease error"() {
        setup:
        def bc = createBlobClient()

        when:
        createLeaseClient(bc).breakLease()

        then:
        thrown(BlobStorageException)
    }

    def "Change blob lease"() {
        setup:
        def bc = createBlobClient()
        def leaseClient = createLeaseClient(bc, namer.getRandomUuid())
        leaseClient.acquireLease(15)

        when:
        def newLeaseId = namer.getRandomUuid()
        def changeLeaseResponse = leaseClient.changeLeaseWithResponse(new BlobChangeLeaseOptions(newLeaseId), null, null)

        then:
        changeLeaseResponse.getValue() == newLeaseId
        changeLeaseResponse.getValue() == leaseClient.getLeaseId()

        when:
        def leaseClient2 = createLeaseClient(bc, changeLeaseResponse.getValue())

        then:
        leaseClient2.releaseLeaseWithResponse(new BlobReleaseLeaseOptions(), null, null).getStatusCode() == 200
        validateBasicHeaders(changeLeaseResponse.getHeaders())
    }

    def "Change blob lease min"() {
        setup:
        def bc = createBlobClient()
        def leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)

        expect:
        createLeaseClient(bc, leaseID).changeLeaseWithResponse(new BlobChangeLeaseOptions(namer.getRandomUuid()), null, null).getStatusCode() == 200
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "Change blob lease AC"() {
        setup:
        def bc = createBlobClient()
        def t = new HashMap<String, String>()
        t.put("foo", "bar")
        bc.setTags(t)
        match = setupBlobMatchCondition(bc, match)
        String leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)
        def mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setTagsConditions(tags)

        expect:
        createLeaseClient(bc, leaseID).changeLeaseWithResponse(new BlobChangeLeaseOptions(namer.getRandomUuid()).setRequestConditions(mac), null, null).getStatusCode() == 200

        where:
        modified | unmodified | match        | noneMatch    | tags
        null     | null       | null         | null         | null
        oldDate  | null       | null         | null         | null
        null     | newDate    | null         | null         | null
        null     | null       | receivedEtag | null         | null
        null     | null       | null         | garbageEtag  | null
        null     | null       | null         | null         | "\"foo\" = 'bar'"
    }

    @Unroll
    def "Change blob lease AC fail"() {
        setup:
        def bc = createBlobClient()
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        String leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)
        def mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setTagsConditions(tags)

        when:
        createLeaseClient(bc, leaseID).changeLeaseWithResponse(new BlobChangeLeaseOptions(namer.getRandomUuid()).setRequestConditions(mac), null, null)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match        | noneMatch    | tags
        newDate  | null       | null         | null         | null
        null     | oldDate    | null         | null         | null
        null     | null       | garbageEtag  | null         | null
        null     | null       | null         | receivedEtag | null
        null     | null       | null         | null         | "\"notfoo\" = 'notbar'"
    }

    def "Change blob lease error"() {
        setup:
        def bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        createLeaseClient(bc, "id").changeLease("id")

        then:
        thrown(BlobStorageException)
    }


    @Unroll
    def "Acquire container lease"() {
        setup:
        def leaseClient = createLeaseClient(cc, proposedID)

        when:
        def leaseResponse = leaseClient.acquireLeaseWithResponse(new BlobAcquireLeaseOptions(leaseTime), null, null)

        then:
        leaseResponse.getValue() == leaseClient.getLeaseId()

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
        createLeaseClient(cc).acquireLeaseWithResponse(new BlobAcquireLeaseOptions(-1), null, null).getStatusCode() == 201
    }

    @Unroll
    def "Acquire container lease duration fail"() {
        setup:
        def leaseClient = createLeaseClient(cc)

        when:
        leaseClient.acquireLease(duration)

        then:
        thrown(BlobStorageException)

        where:
        duration | _
        -10      | _
        10       | _
        70       | _
    }

    @Unroll
    def "Acquire container lease AC"() {
        setup:
        def mac = new BlobLeaseRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)

        expect:
        createLeaseClient(cc).acquireLeaseWithResponse(new BlobAcquireLeaseOptions(-1).setRequestConditions(mac), null, null).getStatusCode() == 201

        where:
        modified | unmodified | match        | noneMatch
        null     | null       | null         | null
        oldDate  | null       | null         | null
        null     | newDate    | null         | null
        null     | null       | receivedEtag | null
        null     | null       | null         | garbageEtag
    }

    @Unroll
    def "Acquire container lease AC fail"() {
        setup:
        def mac = new BlobLeaseRequestConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified)

        when:
        createLeaseClient(cc).acquireLeaseWithResponse(new BlobAcquireLeaseOptions(-1).setRequestConditions(mac), null, null)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified
        newDate  | null
        null     | oldDate
    }

    def "Acquire container lease error"() {
        setup:
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName())

        when:
        createLeaseClient(cc).acquireLease(50)

        then:
        thrown(BlobStorageException)
    }

    def "Renew container lease"() {
        setup:
        def leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)
        def leaseClient = createLeaseClient(cc, leaseID)

        when:
        // If running in live mode wait for the lease to expire to ensure we are actually renewing it
        sleepIfRecord(16000)
        def renewLeaseResponse = leaseClient.renewLeaseWithResponse(new BlobRenewLeaseOptions(), null, null)

        then:
        renewLeaseResponse.getValue() == leaseClient.getLeaseId()
        cc.getProperties().getLeaseState() == LeaseStateType.LEASED
        validateBasicHeaders(renewLeaseResponse.getHeaders())
    }

    def "Renew container lease min"() {
        setup:
        def leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)

        expect:
        createLeaseClient(cc, leaseID).renewLeaseWithResponse(new BlobRenewLeaseOptions(), null, null).getStatusCode() == 200
    }

    @Unroll
    def "Renew container lease AC"() {
        setup:
        def leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)
        def mac = new BlobLeaseRequestConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified)

        expect:
        createLeaseClient(cc, leaseID).renewLeaseWithResponse(new BlobRenewLeaseOptions().setRequestConditions(mac), null, null).getStatusCode() == 200

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
        def mac = new BlobLeaseRequestConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified)

        when:
        createLeaseClient(cc, leaseID).renewLeaseWithResponse(new BlobRenewLeaseOptions().setRequestConditions(mac), null, null)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified
        newDate  | null
        null     | oldDate
    }

    @Unroll
    def "Renew container lease AC illegal"() {
        setup:
        def mac = new BlobLeaseRequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch)

        when:
        createLeaseClient(cc, receivedEtag).renewLeaseWithResponse(new BlobRenewLeaseOptions().setRequestConditions(mac), null, null)

        then:
        thrown(BlobStorageException)

        where:
        match        | noneMatch
        receivedEtag | null
        null         | garbageEtag
    }

    def "Renew container lease error"() {
        setup:
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName())

        when:
        createLeaseClient(cc, "id").renewLease()

        then:
        thrown(BlobStorageException)
    }

    def "Release container lease"() {
        setup:
        def leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)

        def releaseLeaseResponse = createLeaseClient(cc, leaseID).releaseLeaseWithResponse(new BlobReleaseLeaseOptions(), null, null)

        expect:
        cc.getProperties().getLeaseState() == LeaseStateType.AVAILABLE
        validateBasicHeaders(releaseLeaseResponse.getHeaders())
    }

    def "Release container lease min"() {
        setup:
        def leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)

        expect:
        createLeaseClient(cc, leaseID).releaseLeaseWithResponse(new BlobReleaseLeaseOptions(), null, null).getStatusCode() == 200
    }

    @Unroll
    def "Release container lease AC"() {
        setup:
        def leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)
        def mac = new BlobLeaseRequestConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified)

        expect:
        createLeaseClient(cc, leaseID).releaseLeaseWithResponse(new BlobReleaseLeaseOptions().setRequestConditions(mac), null, null).getStatusCode() == 200

        where:
        modified | unmodified
        null     | null
        oldDate  | null
        null     | newDate
    }

    @Unroll
    def "Release container lease AC fail"() {
        setup:
        def leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)
        def mac = new BlobLeaseRequestConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified)

        when:
        createLeaseClient(cc, leaseID).releaseLeaseWithResponse(new BlobReleaseLeaseOptions().setRequestConditions(mac), null, null)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified
        newDate  | null
        null     | oldDate
    }

    @Unroll
    def "Release container lease AC illegal"() {
        setup:
        def mac = new BlobLeaseRequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch)

        when:
        createLeaseClient(cc, receivedLeaseID).releaseLeaseWithResponse(new BlobReleaseLeaseOptions().setRequestConditions(mac), null, null)

        then:
        thrown(BlobStorageException)

        where:
        match        | noneMatch
        receivedEtag | null
        null         | garbageEtag
    }

    def "Release container lease error"() {
        setup:
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName())

        when:
        createLeaseClient(cc, "id").releaseLease()

        then:
        thrown(BlobStorageException)
    }

    @Unroll
    def "Break container lease"() {
        setup:
        def leaseClient = createLeaseClient(cc, namer.getRandomUuid())
        leaseClient.acquireLease(leaseTime)

        def breakLeaseResponse = leaseClient.breakLeaseWithResponse(new BlobBreakLeaseOptions().setBreakPeriod(breakPeriod == null ? null : Duration.ofSeconds(breakPeriod)), null, null)
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
        createLeaseClient(cc).breakLeaseWithResponse(new BlobBreakLeaseOptions(), null, null).getStatusCode() == 202
    }

    @Unroll
    def "Break container lease AC"() {
        setup:
        setupContainerLeaseCondition(cc, receivedLeaseID)
        def mac = new BlobLeaseRequestConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified)

        expect:
        createLeaseClient(cc).breakLeaseWithResponse(new BlobBreakLeaseOptions().setRequestConditions(mac), null, null).getStatusCode() == 202

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
        def mac = new BlobLeaseRequestConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified)

        when:
        createLeaseClient(cc).breakLeaseWithResponse(new BlobBreakLeaseOptions().setRequestConditions(mac), null, null)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified
        newDate  | null
        null     | oldDate
    }

    @Unroll
    def "Break container lease AC illegal"() {
        setup:
        def mac = new BlobLeaseRequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch)

        when:
        createLeaseClient(cc).breakLeaseWithResponse(new BlobBreakLeaseOptions().setRequestConditions(mac), null, null)

        then:
        thrown(BlobStorageException)

        where:
        match        | noneMatch
        receivedEtag | null
        null         | garbageEtag
    }

    def "Break container lease error"() {
        setup:
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName())

        when:
        createLeaseClient(cc).breakLease()

        then:
        thrown(BlobStorageException)
    }

    def "Change container lease"() {
        setup:
        def leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)
        def leaseClient = createLeaseClient(cc, leaseID)

        expect:
        leaseClient.getLeaseId() == leaseID

        when:
        def changeLeaseResponse = leaseClient.changeLeaseWithResponse(new BlobChangeLeaseOptions(namer.getRandomUuid()), null, null)

        then:
        validateBasicHeaders(changeLeaseResponse.getHeaders())
        def newLeaseId = changeLeaseResponse.getValue()
        newLeaseId == leaseClient.getLeaseId()
        newLeaseId != leaseID

        expect:
        createLeaseClient(cc, newLeaseId).releaseLeaseWithResponse(new BlobReleaseLeaseOptions(), null, null).getStatusCode() == 200
    }

    def "Change container lease min"() {
        setup:
        def leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)

        expect:
        createLeaseClient(cc, leaseID).changeLeaseWithResponse(new BlobChangeLeaseOptions(namer.getRandomUuid()), null, null).getStatusCode() == 200
    }

    @Unroll
    def "Change container lease AC"() {
        setup:
        def leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)
        def mac = new BlobLeaseRequestConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified)

        expect:
        createLeaseClient(cc, leaseID).changeLeaseWithResponse(new BlobChangeLeaseOptions(namer.getRandomUuid()).setRequestConditions(mac), null, null).getStatusCode() == 200

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
        def mac = new BlobLeaseRequestConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified)

        when:
        createLeaseClient(cc, leaseID).changeLeaseWithResponse(new BlobChangeLeaseOptions(namer.getRandomUuid()).setRequestConditions(mac), null, null)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified
        newDate  | null
        null     | oldDate
    }

    @Unroll
    def "Change container lease AC illegal"() {
        setup:
        def mac = new BlobLeaseRequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch)

        when:
        createLeaseClient(cc, receivedLeaseID).changeLeaseWithResponse(new BlobChangeLeaseOptions(garbageLeaseID).setRequestConditions(mac), null, null)

        then:
        thrown(BlobStorageException)

        where:
        match        | noneMatch
        receivedEtag | null
        null         | garbageEtag
    }

    def "Change container lease error"() {
        setup:
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName())

        when:
        createLeaseClient(cc, "id").changeLease("id")

        then:
        thrown(BlobStorageException)
    }
}
