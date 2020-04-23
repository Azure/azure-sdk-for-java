// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.specialized


import com.azure.storage.file.share.APISpec
import com.azure.storage.file.share.FileSmbProperties
import com.azure.storage.file.share.ShareClient
import com.azure.storage.file.share.ShareFileClient
import com.azure.storage.file.share.models.LeaseDurationType
import com.azure.storage.file.share.models.LeaseStateType
import com.azure.storage.file.share.models.NtfsFileAttributes
import com.azure.storage.file.share.models.ShareFileHttpHeaders
import com.azure.storage.file.share.models.ShareStorageException
import spock.lang.Unroll

import java.nio.charset.StandardCharsets

class LeaseAPITest extends APISpec {
    ShareFileClient primaryFileClient
    ShareClient shareClient
    String shareName
    String filePath
    def data = "default".getBytes(StandardCharsets.UTF_8)
    def defaultData = getInputStream(data)
    def dataLength = defaultData.available()
    static Map<String, String> testMetadata
    static ShareFileHttpHeaders httpHeaders
    static FileSmbProperties smbProperties
    static String filePermission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-188441444-3053964)S:NO_ACCESS_CONTROL"

    def setup() {
        shareName = testResourceName.randomName(methodName, 60)
        filePath = testResourceName.randomName(methodName, 60)
        shareClient = shareBuilderHelper(interceptorManager, shareName).buildClient()
        shareClient.create()
        primaryFileClient = fileBuilderHelper(interceptorManager, shareName, filePath).buildFileClient()
        primaryFileClient.create(50)
        testMetadata = Collections.singletonMap("testmetadata", "value")
        httpHeaders = new ShareFileHttpHeaders().setContentLanguage("en")
            .setContentType("application/octet-stream")
        smbProperties = new FileSmbProperties().setNtfsFileAttributes(EnumSet.<NtfsFileAttributes> of(NtfsFileAttributes.NORMAL))
    }

    @Unroll
    def "Acquire file lease"() {
        setup:
        def leaseClient = createLeaseClient(primaryFileClient, getRandomUUID())

        when:
        def leaseId = leaseClient.acquireLease()

        then:
        leaseId != null

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

    def "Release blob lease error"() {
        setup:
        def fc = shareClient.getFileClient("garbage")

        when:
        createLeaseClient(fc, "id").releaseLease()

        then:
        thrown(ShareStorageException)
    }

    @Unroll
    def "Break blob lease"() {
        setup:
        def leaseClient = createLeaseClient(primaryFileClient, getRandomUUID())
        leaseClient.acquireLease()

        when:
        leaseClient.breakLease()
        def leaseState = primaryFileClient.getProperties().getLeaseState()

        then:
        leaseState == LeaseStateType.BROKEN
    }

    def "Break blob lease min"() {
        setup:
        setupFileLeaseCondition(primaryFileClient, receivedLeaseID)

        when:
        createLeaseClient(primaryFileClient).breakLease()

        then:
        notThrown(ShareStorageException)
    }

    def "Break blob lease error"() {
        when:
        createLeaseClient(primaryFileClient).breakLease()

        then:
        thrown(ShareStorageException)
    }

    def "Change blob lease"() {
        setup:
        def leaseClient = createLeaseClient(primaryFileClient, getRandomUUID())
        leaseClient.acquireLease()
        def changeLeaseResponse = leaseClient.changeLeaseWithResponse(getRandomUUID(), null, null)
        def leaseClient2 = createLeaseClient(primaryFileClient, changeLeaseResponse.getValue())

        expect:
        leaseClient2.releaseLeaseWithResponse(null, null).getStatusCode() == 200
    }

    def "Change blob lease min"() {
        setup:
        def leaseID = setupFileLeaseCondition(primaryFileClient, receivedLeaseID)

        when:
        createLeaseClient(primaryFileClient, leaseID).changeLease(getRandomUUID())

        then:
        notThrown(ShareStorageException)
    }

    def "Change blob lease error"() {
        setup:
        def fc = shareClient.getFileClient("garbage")

        when:
        createLeaseClient(fc, "id").changeLease("id")

        then:
        thrown(ShareStorageException)
    }
}
