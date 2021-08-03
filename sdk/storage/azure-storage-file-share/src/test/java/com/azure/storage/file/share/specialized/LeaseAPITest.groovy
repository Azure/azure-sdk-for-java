// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.specialized

import com.azure.core.http.rest.Response
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion
import com.azure.storage.file.share.APISpec
import com.azure.storage.file.share.ShareClient
import com.azure.storage.file.share.ShareFileClient
import com.azure.storage.file.share.ShareServiceVersion
import com.azure.storage.file.share.models.LeaseDurationType
import com.azure.storage.file.share.models.LeaseStateType
import com.azure.storage.file.share.models.ShareErrorCode
import com.azure.storage.file.share.models.ShareStorageException
import com.azure.storage.file.share.options.ShareAcquireLeaseOptions
import com.azure.storage.file.share.options.ShareBreakLeaseOptions
import spock.lang.Unroll

import java.time.Duration

class LeaseAPITest extends APISpec {
    ShareFileClient primaryFileClient
    ShareClient shareClient
    String shareName
    String filePath

    def setup() {
        shareName = namer.getRandomName(60)
        filePath = namer.getRandomName(60)
        shareClient = shareBuilderHelper(shareName).buildClient()
        shareClient.create()
        primaryFileClient = fileBuilderHelper(shareName, filePath).buildFileClient()
        primaryFileClient.create(50)
    }

    @Unroll
    def "Acquire file lease"() {
        setup:
        def leaseClient = createLeaseClient(primaryFileClient, namer.getRandomUuid())

        when:
        def leaseId = leaseClient.acquireLease()

        then:
        leaseId != null
        leaseClient.getLeaseId() == leaseId

        when:
        def response = primaryFileClient.getPropertiesWithResponse(null, null)
        def properties = response.getValue()

        then:
        properties.getLeaseState() == LeaseStateType.LEASED
        properties.getLeaseDuration() == LeaseDurationType.INFINITE
    }

    def "Acquire file lease error"() {
        setup:
        def fc = shareClient.getFileClient("garbage")

        when:
        createLeaseClient(fc).acquireLease()

        then:
        thrown(ShareStorageException)
    }

    def "Release lease"() {
        setup:
        def leaseID = setupFileLeaseCondition(primaryFileClient, receivedLeaseID)
        def headers = createLeaseClient(primaryFileClient, leaseID).releaseLeaseWithResponse(null, null).getHeaders()

        expect:
        primaryFileClient.getProperties().getLeaseState() == LeaseStateType.AVAILABLE
    }

    def "Release lease min"() {
        setup:
        def leaseID = setupFileLeaseCondition(primaryFileClient, receivedLeaseID)

        when:
        createLeaseClient(primaryFileClient, leaseID).releaseLease()

        then:
        notThrown(ShareStorageException)
    }

    def "Release file lease error"() {
        setup:
        def fc = shareClient.getFileClient("garbage")

        when:
        createLeaseClient(fc, "id").releaseLease()

        then:
        thrown(ShareStorageException)
    }

    @Unroll
    def "Break file lease"() {
        setup:
        def leaseClient = createLeaseClient(primaryFileClient, namer.getRandomUuid())
        leaseClient.acquireLease()

        when:
        leaseClient.breakLease()
        def leaseState = primaryFileClient.getProperties().getLeaseState()

        then:
        leaseState == LeaseStateType.BROKEN
    }

    def "Break file lease min"() {
        setup:
        setupFileLeaseCondition(primaryFileClient, receivedLeaseID)

        when:
        createLeaseClient(primaryFileClient).breakLease()

        then:
        notThrown(ShareStorageException)
    }

    def "Break file lease error"() {
        when:
        createLeaseClient(primaryFileClient).breakLease()

        then:
        thrown(ShareStorageException)
    }

    def "Change file lease"() {
        setup:
        def leaseClient = createLeaseClient(primaryFileClient, namer.getRandomUuid())
        leaseClient.acquireLease()

        when:
        def newLeaseId = namer.getRandomUuid()
        def changeLeaseResponse = leaseClient.changeLeaseWithResponse(newLeaseId, null, null)

        then:
        changeLeaseResponse.getValue() == leaseClient.getLeaseId()
        leaseClient.getLeaseId() == newLeaseId

        def leaseClient2 = createLeaseClient(primaryFileClient, changeLeaseResponse.getValue())
        leaseClient2.releaseLeaseWithResponse(null, null).getStatusCode() == 200
    }

    def "Change file lease min"() {
        setup:
        def leaseID = setupFileLeaseCondition(primaryFileClient, receivedLeaseID)

        when:
        createLeaseClient(primaryFileClient, leaseID).changeLease(namer.getRandomUuid())

        then:
        notThrown(ShareStorageException)
    }

    def "Change file lease error"() {
        setup:
        def fc = shareClient.getFileClient("garbage")

        when:
        createLeaseClient(fc, "id").changeLease("id")

        then:
        thrown(ShareStorageException)
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2020_02_10")
    @Unroll
    def "Acquire share lease"() {
        setup:
        def leaseClient = createLeaseClient(shareClient, proposedID)

        when:
        def leaseResponse = leaseClient.acquireLeaseWithResponse(new ShareAcquireLeaseOptions().setDuration(leaseTime), null, null)

        then:
        leaseClient.getLeaseId() == leaseResponse.getValue()

        def properties = shareClient.getProperties()
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

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2020_02_10")
    def "Acquire share lease min"() {
        expect:
        createLeaseClient(shareClient).acquireLeaseWithResponse(null, null, null).getStatusCode() == 201
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2020_02_10")
    def "Acquire share lease snapshot"() {
        setup:
        def shareSnapshot = shareClient.createSnapshot().getSnapshot()
        def shareClient = shareBuilderHelper(shareClient.getShareName(), shareSnapshot).buildClient()
        def leaseClient = createLeaseClient(shareClient)

        when:
        def resp = retry({
            leaseClient
                .acquireLeaseWithResponse(new ShareAcquireLeaseOptions().setDuration(-1), null, null)
        },{ShareStorageException it -> it.errorCode == ShareErrorCode.SHARE_SNAPSHOT_IN_PROGRESS})

        then:
        resp.getStatusCode() == 201
        createLeaseClient(shareClient, resp.getValue()).releaseLeaseWithResponse(null, null)
    }

    def "Acquire share lease snapshot fail"() {
        setup:
        def shareSnapshot = "2020-08-19T19:26:08.0000000Z"
        def shareClient = shareBuilderHelper(shareClient.getShareName(), shareSnapshot).buildClient()

        when:
        createLeaseClient(shareClient).acquireLeaseWithResponse(new ShareAcquireLeaseOptions().setDuration(-1), null, null)

        then:
        thrown(ShareStorageException)
    }

    @Unroll
    def "Acquire share lease duration fail"() {
        setup:
        def leaseClient = createLeaseClient(shareClient)

        when:
        leaseClient.acquireLeaseWithResponse(new ShareAcquireLeaseOptions().setDuration(duration), null, null)

        then:
        thrown(ShareStorageException)

        where:
        duration | _
        -10      | _
        10       | _
        70       | _
    }

    def "Acquire share lease error"() {
        setup:
        shareClient = shareBuilderHelper(generateShareName()).buildClient()

        when:
        createLeaseClient(shareClient).acquireLeaseWithResponse(new ShareAcquireLeaseOptions().setDuration(20), null, null)

        then:
        thrown(ShareStorageException)
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2020_02_10")
    def "Renew share lease"() {
        setup:
        def leaseID = setupShareLeaseCondition(shareClient, receivedLeaseID)
        def leaseClient = createLeaseClient(shareClient, leaseID)

        when:
        // If running in live mode wait for the lease to expire to ensure we are actually renewing it
        sleepIfRecord(16000)
        def renewLeaseResponse = leaseClient.renewLeaseWithResponse(null, null)

        then:
        leaseClient.getLeaseId() == renewLeaseResponse.getValue()
        shareClient.getProperties().getLeaseState() == LeaseStateType.LEASED
        validateBasicHeaders(renewLeaseResponse.getHeaders())
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2020_02_10")
    def "Renew share lease min"() {
        setup:
        def leaseID = setupShareLeaseCondition(shareClient, receivedLeaseID)

        expect:
        createLeaseClient(shareClient, leaseID).renewLeaseWithResponse(null, null).getStatusCode() == 200
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2020_02_10")
    def "Renew share lease snapshot"() {
        setup:
        def shareSnapshot = shareClient.createSnapshot().getSnapshot()
        def shareClient = shareBuilderHelper(shareClient.getShareName(), shareSnapshot).buildClient()
        def leaseID = setupShareLeaseCondition(shareClient, receivedLeaseID)

        when:
        def resp = createLeaseClient(shareClient, leaseID).renewLeaseWithResponse(null, null)

        then:
        resp.getStatusCode() == 200
        createLeaseClient(shareClient, resp.getValue()).releaseLeaseWithResponse(null, null)
    }

    def "Renew share lease snapshot fail"() {
        setup:
        def shareSnapshot = "2020-08-19T19:26:08.0000000Z"
        def shareClient = shareBuilderHelper(shareClient.getShareName(), shareSnapshot).buildClient()

        when:
        createLeaseClient(shareClient).renewLease()

        then:
        thrown(ShareStorageException)
    }

    def "Renew share lease error"() {
        setup:
        shareClient = shareBuilderHelper(generateShareName()).buildClient()

        when:
        createLeaseClient(shareClient, "id").renewLease()

        then:
        thrown(ShareStorageException)
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2020_02_10")
    def "Release share lease"() {
        setup:
        def leaseID = setupShareLeaseCondition(shareClient, receivedLeaseID)

        def releaseLeaseResponse = createLeaseClient(shareClient, leaseID).releaseLeaseWithResponse(null, null)

        expect:
        shareClient.getProperties().getLeaseState() == LeaseStateType.AVAILABLE
        validateBasicHeaders(releaseLeaseResponse.getHeaders())
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2020_02_10")
    def "Release share lease min"() {
        setup:
        def leaseID = setupShareLeaseCondition(shareClient, receivedLeaseID)

        expect:
        createLeaseClient(shareClient, leaseID).releaseLeaseWithResponse(null, null).getStatusCode() == 200
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2020_02_10")
    def "Release share lease snapshot"() {
        setup:
        def shareSnapshot = shareClient.createSnapshot().getSnapshot()
        def shareClient = shareBuilderHelper(shareClient.getShareName(), shareSnapshot).buildClient()
        def leaseID = setupShareLeaseCondition(shareClient, receivedLeaseID)

        expect:
        createLeaseClient(shareClient, leaseID).releaseLeaseWithResponse(null, null).getStatusCode() == 200
    }

    def "Release share lease snapshot fail"() {
        setup:
        def shareSnapshot = "2020-08-19T19:26:08.0000000Z"
        def shareClient = shareBuilderHelper(shareClient.getShareName(), shareSnapshot).buildClient()

        when:
        createLeaseClient(shareClient).releaseLeaseWithResponse(null, null)

        then:
        thrown(ShareStorageException)
    }

    def "Release share lease error"() {
        setup:
        shareClient = shareBuilderHelper(generateShareName()).buildClient()

        when:
        createLeaseClient(shareClient, "id").releaseLease()

        then:
        thrown(ShareStorageException)
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2020_02_10")
    @Unroll
    def "Break share lease"() {
        setup:
        def leaseClient = createLeaseClient(shareClient, namer.getRandomUuid())
        leaseClient.acquireLeaseWithResponse(new ShareAcquireLeaseOptions().setDuration(leaseTime), null, null)

        def breakLeaseResponse = leaseClient.breakLeaseWithResponse(new ShareBreakLeaseOptions().setBreakPeriod(breakPeriod == null ? null : Duration.ofSeconds(breakPeriod)), null, null)
        def state = shareClient.getProperties().getLeaseState()

        expect:
        state == LeaseStateType.BROKEN || state == LeaseStateType.BREAKING
        breakLeaseResponse.getValue() <= remainingTime
        validateBasicHeaders(breakLeaseResponse.getHeaders())
        if (breakPeriod != null) {
            // If running in live mode wait for the lease to break so we can delete the share after the test completes
            sleepIfRecord(breakPeriod * 1000)
        }

        where:
        leaseTime | breakPeriod | remainingTime
        -1        | null        | 0
        -1        | 20          | 25
        20        | 15          | 16

    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2020_02_10")
    def "Break share lease min"() {
        setup:
        setupShareLeaseCondition(shareClient, receivedLeaseID)

        expect:
        createLeaseClient(shareClient).breakLeaseWithResponse(new ShareBreakLeaseOptions(), null, null).getStatusCode() == 202
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2020_02_10")
    def "Break share lease snapshot"() {
        setup:
        def shareSnapshot = shareClient.createSnapshot().getSnapshot()
        def shareClient = shareBuilderHelper(shareClient.getShareName(), shareSnapshot).buildClient()
        def leaseID = setupShareLeaseCondition(shareClient, receivedLeaseID)

        when:
        def resp = createLeaseClient(shareClient, leaseID).breakLeaseWithResponse(new ShareBreakLeaseOptions(), null, null)

        then:
        resp.getStatusCode() == 202

    }

    def "Break share lease snapshot fail"() {
        setup:
        def shareSnapshot = "2020-08-19T19:26:08.0000000Z"
        def shareClient = shareBuilderHelper(shareClient.getShareName(), shareSnapshot).buildClient()

        when:
        createLeaseClient(shareClient).breakLease()

        then:
        thrown(ShareStorageException)
    }

    def "Break share lease error"() {
        setup:
        shareClient = shareBuilderHelper(generateShareName()).buildClient()

        when:
        createLeaseClient(shareClient).breakLease()

        then:
        thrown(ShareStorageException)
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2020_02_10")
    def "Change share lease"() {
        setup:
        def leaseID = setupShareLeaseCondition(shareClient, receivedLeaseID)
        def leaseClient = createLeaseClient(shareClient, leaseID)

        when:
        def newLeaseId = namer.getRandomUuid()
        def changeLeaseResponse = leaseClient.changeLeaseWithResponse(newLeaseId, null, null)

        then:
        newLeaseId == changeLeaseResponse.getValue()
        changeLeaseResponse.getValue() == leaseClient.getLeaseId()

        createLeaseClient(shareClient, newLeaseId).releaseLeaseWithResponse(null, null).getStatusCode() == 200
        validateBasicHeaders(changeLeaseResponse.getHeaders())
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2020_02_10")
    def "Change share lease min"() {
        setup:
        def leaseID = setupShareLeaseCondition(shareClient, receivedLeaseID)

        expect:
        createLeaseClient(shareClient, leaseID).changeLeaseWithResponse(namer.getRandomUuid(), null, null).getStatusCode() == 200
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2020_02_10")
    def "Change share lease snapshot"() {
        setup:
        def shareSnapshot = shareClient.createSnapshot().getSnapshot()
        def shareClient = shareBuilderHelper(shareClient.getShareName(), shareSnapshot).buildClient()
        def leaseID = setupShareLeaseCondition(shareClient, receivedLeaseID)

        when:
        def resp = createLeaseClient(shareClient, leaseID).changeLeaseWithResponse(namer.getRandomUuid(), null,  null)

        then:
        resp.getStatusCode() == 200
        createLeaseClient(shareClient, resp.getValue()).releaseLeaseWithResponse(null, null)
    }

    def "Change share lease snapshot fail"() {
        setup:
        def shareSnapshot = "2020-08-19T19:26:08.0000000Z"
        def shareClient = shareBuilderHelper(shareClient.getShareName(), shareSnapshot).buildClient()

        when:
        createLeaseClient(shareClient).changeLease(namer.getRandomUuid())

        then:
        thrown(ShareStorageException)
    }

    def "Change share lease error"() {
        setup:
        shareClient = shareBuilderHelper(generateShareName()).buildClient()

        when:
        createLeaseClient(shareClient, "id").changeLease("id")

        then:
        thrown(ShareStorageException)
    }
}
