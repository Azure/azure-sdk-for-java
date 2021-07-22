// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share

import com.azure.core.http.netty.NettyAsyncHttpClientBuilder
import com.azure.storage.common.StorageSharedKeyCredential
import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.test.shared.extensions.PlaybackOnly
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion
import com.azure.storage.file.share.implementation.util.ModelHelper
import com.azure.storage.file.share.models.NtfsFileAttributes
import com.azure.storage.file.share.models.ShareAccessPolicy
import com.azure.storage.file.share.models.ShareAccessTier
import com.azure.storage.file.share.models.ShareProtocols
import com.azure.storage.file.share.models.ShareErrorCode
import com.azure.storage.file.share.models.ShareFileHttpHeaders
import com.azure.storage.file.share.models.ShareRequestConditions
import com.azure.storage.file.share.models.ShareRootSquash
import com.azure.storage.file.share.models.ShareSignedIdentifier
import com.azure.storage.file.share.models.ShareSnapshotInfo
import com.azure.storage.file.share.models.ShareSnapshotsDeleteOptionType
import com.azure.storage.file.share.models.ShareStorageException
import com.azure.storage.file.share.options.ShareCreateOptions
import com.azure.storage.file.share.options.ShareDeleteOptions
import com.azure.storage.file.share.options.ShareGetAccessPolicyOptions
import com.azure.storage.file.share.options.ShareGetPropertiesOptions
import com.azure.storage.file.share.options.ShareGetStatisticsOptions
import com.azure.storage.file.share.options.ShareSetAccessPolicyOptions
import com.azure.storage.file.share.options.ShareSetPropertiesOptions
import com.azure.storage.file.share.options.ShareSetMetadataOptions
import spock.lang.Requires
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class ShareAPITests extends APISpec {
    ShareClient primaryShareClient
    String shareName
    static Map<String, String> testMetadata
    FileSmbProperties smbProperties
    static def filePermission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-188441444-3053964)S:NO_ACCESS_CONTROL"

    def setup() {
        shareName = namer.getRandomName(60)
        primaryFileServiceClient = fileServiceBuilderHelper().buildClient()
        primaryShareClient = primaryFileServiceClient.getShareClient(shareName)
        testMetadata = Collections.singletonMap("testmetadata", "value")
        smbProperties = new FileSmbProperties().setNtfsFileAttributes(EnumSet.<NtfsFileAttributes> of(NtfsFileAttributes.NORMAL))
    }

    def "Get share URL"() {
        given:
        def accountName = StorageSharedKeyCredential.fromConnectionString(env.primaryAccount.connectionString).getAccountName()
        def expectURL = String.format("https://%s.file.core.windows.net/%s", accountName, shareName)

        when:
        def shareURL = primaryShareClient.getShareUrl()

        then:
        expectURL == shareURL
    }

    def "Get share snapshot URL"() {
        given:
        def accountName = StorageSharedKeyCredential.fromConnectionString(env.primaryAccount.connectionString).getAccountName()
        def expectURL = String.format("https://%s.file.core.windows.net/%s", accountName, shareName)
        primaryShareClient.create()
        when:
        ShareSnapshotInfo shareSnapshotInfo = primaryShareClient.createSnapshot()
        expectURL = expectURL + "?sharesnapshot=" + shareSnapshotInfo.getSnapshot()
        ShareClient newShareClient = shareBuilderHelper(shareName).snapshot(shareSnapshotInfo.getSnapshot())
            .buildClient()
        def shareURL = newShareClient.getShareUrl()

        then:
        expectURL == shareURL

        when:
        def snapshotEndpoint = String.format("https://%s.file.core.windows.net/%s?sharesnapshot=%s", accountName, shareName, shareSnapshotInfo.getSnapshot())
        ShareClient client = getShareClientBuilder(snapshotEndpoint).credential(StorageSharedKeyCredential.fromConnectionString(env.primaryAccount.connectionString)).buildClient()

        then:
        client.getShareUrl() == snapshotEndpoint
    }

    def "Get root directory client"() {
        given:
        def directoryClient = primaryShareClient.getRootDirectoryClient()

        expect:
        directoryClient instanceof ShareDirectoryClient
    }

    def "Get file client does not create a file"() {
        given:
        def fileClient = primaryShareClient.getFileClient("testFile")
        expect:
        fileClient instanceof ShareFileClient
    }

    def "Exists"() {
        when:
        primaryShareClient.create()

        then:
        primaryShareClient.exists()
    }

    def "Does not exist"() {
        expect:
        !primaryShareClient.exists()
    }

    def "Exists error"() {
        setup:
        primaryShareClient = shareBuilderHelper(shareName)
            .sasToken("sig=dummyToken").buildClient()

        when:
        primaryShareClient.exists()

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 403, ShareErrorCode.AUTHENTICATION_FAILED)
    }

    def "Create share"() {
        expect:
        FileTestHelper.assertResponseStatusCode(primaryShareClient.createWithResponse(null, null, null, null), 201)
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "Create share with args"() {
        expect:
        FileTestHelper.assertResponseStatusCode(primaryShareClient.createWithResponse(new ShareCreateOptions()
            .setMetadata(metadata).setQuotaInGb(quota).setAccessTier(accessTier), null, null), 201)

        where:
        metadata     | quota | accessTier
        null         | null  | null
        null         | 1     | null
        testMetadata | null  | null
        null         | null  | ShareAccessTier.HOT
        testMetadata | 1     | ShareAccessTier.HOT
    }

    @Unroll
    def "Create share with invalid args"() {
        when:
        primaryShareClient.createWithResponse(metadata, quota, null, null)

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, statusCode, errMessage)

        where:
        metadata                                       | quota | statusCode | errMessage
        Collections.singletonMap("", "value")          | 1     | 400        | ShareErrorCode.EMPTY_METADATA_KEY
        Collections.singletonMap("metadata!", "value") | 1     | 400        | ShareErrorCode.INVALID_METADATA
        testMetadata                                   | 6000  | 400        | ShareErrorCode.INVALID_HEADER_VALUE
    }

    def "Create snapshot"() {
        given:
        primaryShareClient.create()
        def shareSnapshotName = namer.getRandomName(60)

        when:
        def createSnapshotResponse = primaryShareClient.createSnapshotWithResponse(null, null, null)
        def shareSnapshotClient = new ShareClientBuilder().shareName(shareSnapshotName).connectionString(env.primaryAccount.connectionString)
            .snapshot(createSnapshotResponse.getValue().getSnapshot()).httpClient(new NettyAsyncHttpClientBuilder().build())
            .buildClient()
        then:
        Objects.equals(createSnapshotResponse.getValue().getSnapshot(),
            shareSnapshotClient.getSnapshotId())
    }

    def "Create snapshot error"() {
        when:
        primaryShareClient.createSnapshot()

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.SHARE_NOT_FOUND)
    }

    def "Create snapshot metadata"() {
        given:
        primaryShareClient.create()
        def shareSnapshotName = namer.getRandomName(60)

        when:
        def createSnapshotResponse = primaryShareClient.createSnapshotWithResponse(testMetadata, null, null)
        def shareSnapshotClient = new ShareClientBuilder().shareName(shareSnapshotName).connectionString(env.primaryAccount.connectionString)
            .snapshot(createSnapshotResponse.getValue().getSnapshot()).httpClient(new NettyAsyncHttpClientBuilder().build())
            .buildClient()
        then:
        Objects.equals(createSnapshotResponse.getValue().getSnapshot(),
            shareSnapshotClient.getSnapshotId())
    }

    def "Create snapshot metadata error"() {
        when:
        primaryShareClient.create()
        primaryShareClient.createSnapshotWithResponse(Collections.singletonMap("", "value"), null, null)

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.EMPTY_METADATA_KEY)
    }

    def "Delete share"() {
        given:
        primaryShareClient.create()

        expect:
        FileTestHelper.assertResponseStatusCode(primaryShareClient.deleteWithResponse(null, null), 202)
    }

    def "Delete share delete snapshot options"() {
        setup:
        primaryShareClient.create()
        def snap1 = primaryShareClient.createSnapshot().getSnapshot()
        def snap2 = primaryShareClient.createSnapshot().getSnapshot()

        when:
        primaryShareClient.deleteWithResponse(new ShareDeleteOptions().setDeleteSnapshotsOptions(ShareSnapshotsDeleteOptionType.INCLUDE), null, null)

        then:
        !primaryShareClient.getSnapshotClient(snap1).exists()
        !primaryShareClient.getSnapshotClient(snap2).exists()
    }

    def "Delete share delete snapshot options error"() {
        setup:
        primaryShareClient.create()
        primaryShareClient.createSnapshot().getSnapshot()
        primaryShareClient.createSnapshot().getSnapshot()

        when:
        primaryShareClient.deleteWithResponse(new ShareDeleteOptions(), null, null)

        then:
        thrown(ShareStorageException)
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2020_02_10")
    def "Delete share lease"() {
        setup:
        primaryShareClient.create()
        def leaseID = setupShareLeaseCondition(primaryShareClient, receivedLeaseID)

        expect:
        FileTestHelper.assertResponseStatusCode(primaryShareClient.deleteWithResponse(new ShareDeleteOptions().setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID)), null, null), 202)
    }

    def "Delete share lease error"() {
        setup:
        primaryShareClient.create()
        def leaseID = setupShareLeaseCondition(primaryShareClient, garbageLeaseID)

        when:
        primaryShareClient.deleteWithResponse(new ShareDeleteOptions().setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID)), null, null)

        then:
        thrown(ShareStorageException)
    }

    def "Delete share error"() {
        when:
        primaryShareClient.delete()

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.SHARE_NOT_FOUND)
    }

    def "Get properties"() {
        given:
        primaryShareClient.createWithResponse(testMetadata, 1, null, null)

        when:
        def getPropertiesResponse = primaryShareClient.getPropertiesWithResponse(null, null)

        then:
        FileTestHelper.assertResponseStatusCode(getPropertiesResponse, 200)
        testMetadata == getPropertiesResponse.getValue().getMetadata()
        getPropertiesResponse.getValue().getQuota() == 1
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2020_02_10")
    def "Get properties lease"() {
        setup:
        primaryShareClient.create()
        def leaseID = setupShareLeaseCondition(primaryShareClient, receivedLeaseID)

        expect:
        FileTestHelper.assertResponseStatusCode(primaryShareClient.getPropertiesWithResponse(new ShareGetPropertiesOptions().setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID)), null, null), 200)
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2020_02_10")
    def "Get properties lease error"() {
        setup:
        primaryShareClient.create()
        def leaseID = setupShareLeaseCondition(primaryShareClient, garbageLeaseID)

        when:
        primaryShareClient.getPropertiesWithResponse(new ShareGetPropertiesOptions().setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID)), null, null)

        then:
        thrown(ShareStorageException)
    }

    def "Get properties error"() {
        when:
        primaryShareClient.getProperties()

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.SHARE_NOT_FOUND)
    }

    @Unroll
    @PlaybackOnly
    def "Get properties premium"() {
        given:
        ShareProtocols enabledProtocol = ModelHelper.parseShareProtocols(protocol)

        def premiumShareClient = premiumFileServiceClient.createShareWithResponse(generateShareName(),
            new ShareCreateOptions().setMetadata(testMetadata).setProtocols(enabledProtocol)
                .setRootSquash(rootSquash), null, null)
            .getValue()

        when:
        def getPropertiesResponse = premiumShareClient.getPropertiesWithResponse(null, null)
        def shareProperties = getPropertiesResponse.getValue()

        then:
        FileTestHelper.assertResponseStatusCode(getPropertiesResponse, 200)
        testMetadata == shareProperties.getMetadata()
        shareProperties.getQuota()
        shareProperties.getNextAllowedQuotaDowngradeTime()
        shareProperties.getProvisionedEgressMBps()
        shareProperties.getProvisionedIngressMBps()
        shareProperties.getProvisionedIops()
        shareProperties.getProtocols().toString() == enabledProtocol.toString()
        shareProperties.getRootSquash() == rootSquash

        where:
        protocol                               | rootSquash
        Constants.HeaderConstants.SMB_PROTOCOL | null
        Constants.HeaderConstants.NFS_PROTOCOL | ShareRootSquash.ALL_SQUASH
        Constants.HeaderConstants.NFS_PROTOCOL | ShareRootSquash.NO_ROOT_SQUASH
        Constants.HeaderConstants.NFS_PROTOCOL | ShareRootSquash.ROOT_SQUASH
    }

    @Unroll
    @PlaybackOnly
    def "Set premium properties"() {
        setup:
        def premiumShareClient = premiumFileServiceClient.createShareWithResponse(generateShareName(),
            new ShareCreateOptions().setProtocols(new ShareProtocols().setNfsEnabled(true)), null, null).getValue()

        when:
        premiumShareClient.setProperties(new ShareSetPropertiesOptions().setRootSquash(rootSquash))

        then:
        premiumShareClient.getProperties().getRootSquash() == rootSquash

        where:
        rootSquash                     | _
        ShareRootSquash.ROOT_SQUASH    | _
        ShareRootSquash.NO_ROOT_SQUASH | _
        ShareRootSquash.ALL_SQUASH     | _
    }

    def "Set access policy"() {
        setup:
        primaryShareClient.create()
        def identifier = new ShareSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new ShareAccessPolicy()
                .setStartsOn(namer.getUtcNow().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime())
                .setExpiresOn(namer.getUtcNow().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime()
                    .plusDays(1))
                .setPermissions("r"))

        def ids = [identifier] as List

        when:
        primaryShareClient.setAccessPolicy(ids)

        then:
        primaryShareClient.getAccessPolicy().iterator().next().getId() == "0000"
    }

    def "Set access policy ids"() {
        setup:
        primaryShareClient.create()
        def identifier = new ShareSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new ShareAccessPolicy()
                .setStartsOn(namer.getUtcNow())
                .setExpiresOn(namer.getUtcNow().plusDays(1))
                .setPermissions("r"))
        def identifier2 = new ShareSignedIdentifier()
            .setId("0001")
            .setAccessPolicy(new ShareAccessPolicy()
                .setStartsOn(namer.getUtcNow())
                .setExpiresOn(namer.getUtcNow().plusDays(2))
                .setPermissions("w"))
        def ids = [identifier, identifier2] as List

        when:
        def response = primaryShareClient.setAccessPolicyWithResponse(ids, null, null)
        def receivedIdentifiers = primaryShareClient.getAccessPolicy().iterator()

        then:
        response.getStatusCode() == 200
        validateBasicHeaders(response.getHeaders())
        def id0 = receivedIdentifiers.next()
        id0.getAccessPolicy().getExpiresOn() == identifier.getAccessPolicy().getExpiresOn()
        id0.getAccessPolicy().getStartsOn() == identifier.getAccessPolicy().getStartsOn()
        id0.getAccessPolicy().getPermissions() == identifier.getAccessPolicy().getPermissions()
        def id1 = receivedIdentifiers.next()
        id1.getAccessPolicy().getExpiresOn() == identifier2.getAccessPolicy().getExpiresOn()
        id1.getAccessPolicy().getStartsOn() == identifier2.getAccessPolicy().getStartsOn()
        id1.getAccessPolicy().getPermissions() == identifier2.getAccessPolicy().getPermissions()
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2020_02_10")
    def "Set access policy lease"() {
        setup:
        primaryShareClient.create()
        def leaseID = setupShareLeaseCondition(primaryShareClient, receivedLeaseID)

        expect:
        primaryShareClient.setAccessPolicyWithResponse(new ShareSetAccessPolicyOptions().setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID)), null, null).getStatusCode() == 200
    }

    def "Set access policy lease error"() {
        setup:
        primaryShareClient.create()
        def leaseID = setupShareLeaseCondition(primaryShareClient, garbageLeaseID)

        when:
        primaryShareClient.setAccessPolicyWithResponse(new ShareSetAccessPolicyOptions().setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID)), null, null)

        then:
        thrown(ShareStorageException)
    }

    def "Set access policy error"() {
        when:
        primaryShareClient.setAccessPolicy(null)

        then:
        thrown(ShareStorageException)
    }

    def "Get access policy"() {
        setup:
        primaryShareClient.create()
        def identifier = new ShareSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new ShareAccessPolicy()
                .setStartsOn(namer.getUtcNow().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime())
                .setExpiresOn(namer.getUtcNow().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime()
                    .plusDays(1))
                .setPermissions("r"))

        def ids = [identifier] as List
        primaryShareClient.setAccessPolicy(ids)

        when:
        def id = primaryShareClient.getAccessPolicy().iterator().next()

        then:
        id.getId() == identifier.getId()
        id.getAccessPolicy().getStartsOn() == identifier.getAccessPolicy().getStartsOn()
        id.getAccessPolicy().getExpiresOn() == identifier.getAccessPolicy().getExpiresOn()
        id.getAccessPolicy().getPermissions() == identifier.getAccessPolicy().getPermissions()
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2020_02_10")
    def "Get access policy lease"() {
        setup:
        primaryShareClient.create()
        def leaseID = setupShareLeaseCondition(primaryShareClient, receivedLeaseID)

        when:
        def response = primaryShareClient.getAccessPolicy(new ShareGetAccessPolicyOptions().setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID)))

        then:
        !response.iterator().hasNext()
    }

    def "Get access policy lease error"() {
        setup:
        primaryShareClient.create()
        def leaseID = setupShareLeaseCondition(primaryShareClient, garbageLeaseID)

        when:
        primaryShareClient.getAccessPolicy(new ShareGetAccessPolicyOptions().setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID))).iterator().hasNext()

        then:
        thrown(ShareStorageException)
    }

    def "Get access policy error"() {
        when:
        primaryShareClient.getAccessPolicy().iterator().hasNext()

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.SHARE_NOT_FOUND)
    }

    def "Set properties quota"() {
        given:
        primaryShareClient.createWithResponse(null, 1, null, null)

        when:
        def getQuotaBeforeResponse = primaryShareClient.getProperties()
        def setQuotaResponse = primaryShareClient.setQuotaWithResponse(2, null, null)
        def getQuotaAfterResponse = primaryShareClient.getProperties()

        then:
        getQuotaBeforeResponse.getQuota() == 1
        FileTestHelper.assertResponseStatusCode(setQuotaResponse, 200)
        getQuotaAfterResponse.getQuota() == 2
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2019_12_12")
    def "Set properties access tier"() {
        given:
        primaryShareClient.createWithResponse(new ShareCreateOptions().setAccessTier(ShareAccessTier.HOT), null, null)
        def time = namer.getUtcNow().truncatedTo(ChronoUnit.SECONDS)

        when:
        def getAccessTierBeforeResponse = primaryShareClient.getProperties()
        def setAccessTierResponse = primaryShareClient.setPropertiesWithResponse(new ShareSetPropertiesOptions().setAccessTier(ShareAccessTier.TRANSACTION_OPTIMIZED), null, null)
        def getAccessTierAfterResponse = primaryShareClient.getProperties()

        then:
        getAccessTierBeforeResponse.getAccessTier() == ShareAccessTier.HOT.toString()
        FileTestHelper.assertResponseStatusCode(setAccessTierResponse, 200)
        getAccessTierAfterResponse.getAccessTier() == ShareAccessTier.TRANSACTION_OPTIMIZED.toString()
        getAccessTierAfterResponse.getAccessTierChangeTime().isEqual(time) || getAccessTierAfterResponse.getAccessTierChangeTime().isAfter(time.minusSeconds(1))
        getAccessTierAfterResponse.getAccessTierChangeTime().isBefore(time.plusMinutes(1))
        getAccessTierAfterResponse.getAccessTierTransitionState() == "pending-from-hot"
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2020_02_10")
    def "Set properties lease"() {
        given:
        primaryShareClient.createWithResponse(new ShareCreateOptions().setAccessTier(ShareAccessTier.HOT), null, null)
        def leaseID = setupShareLeaseCondition(primaryShareClient, receivedLeaseID)

        when:
        def setAccessTierResponse = primaryShareClient.setPropertiesWithResponse(
            new ShareSetPropertiesOptions().setAccessTier(ShareAccessTier.COOL).setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID)), null, null)

        then:
        FileTestHelper.assertResponseStatusCode(setAccessTierResponse, 200)
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2020_02_10")
    def "Set properties lease error"() {
        given:
        primaryShareClient.createWithResponse(new ShareCreateOptions().setAccessTier(ShareAccessTier.HOT), null, null)
        def leaseID = setupShareLeaseCondition(primaryShareClient, garbageLeaseID)

        when:
        def setAccessTierResponse = primaryShareClient.setPropertiesWithResponse(
            new ShareSetPropertiesOptions().setAccessTier(ShareAccessTier.COOL).setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID)), null, null)

        then:
        thrown(ShareStorageException)
    }

    def "Set properties error"() {
        when:
        primaryShareClient.setProperties(new ShareSetPropertiesOptions())
        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.SHARE_NOT_FOUND)
    }

    def "Set metadata"() {
        given:
        primaryShareClient.createWithResponse(testMetadata, null, null, null)
        def metadataAfterSet = Collections.singletonMap("afterset", "value")

        when:
        def getMetadataBeforeResponse = primaryShareClient.getProperties()
        def setMetadataResponse = primaryShareClient.setMetadataWithResponse(metadataAfterSet, null, null)
        def getMetadataAfterResponse = primaryShareClient.getProperties()

        then:
        testMetadata == getMetadataBeforeResponse.getMetadata()
        FileTestHelper.assertResponseStatusCode(setMetadataResponse, 200)
        metadataAfterSet == getMetadataAfterResponse.getMetadata()
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2020_02_10")
    def "Set metadata lease"() {
        given:
        primaryShareClient.createWithResponse(null, 1, null, null)
        def leaseID = setupShareLeaseCondition(primaryShareClient, receivedLeaseID)

        when:
        def resp = primaryShareClient.setMetadataWithResponse(new ShareSetMetadataOptions().setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID)), null, null)

        then:
        FileTestHelper.assertResponseStatusCode(resp, 200)
    }

    def "Set metadata lease error"() {
        given:
        primaryShareClient.createWithResponse(null, 1, null, null)
        def leaseID = setupShareLeaseCondition(primaryShareClient, garbageLeaseID)

        when:
        primaryShareClient.setMetadataWithResponse(
            new ShareSetMetadataOptions().setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID)), null, null)

        then:
        thrown(ShareStorageException)
    }

    def "Set metadata error"() {
        when:
        primaryShareClient.setMetadata(testMetadata)

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.SHARE_NOT_FOUND)
    }

    @Unroll
    def "Get statistics"() {
        setup:
        primaryShareClient.create()
        primaryShareClient.createFile("tempFile", (long) size)

        when:
        def resp = primaryShareClient.getStatisticsWithResponse(null, null)

        then:
        FileTestHelper.assertResponseStatusCode(resp, 200)
        resp.getValue().getShareUsageInBytes() == size
        resp.getValue().getShareUsageInGB() == gigabytes

        where:
        size                    || gigabytes
        0                       || 0
        Constants.KB            || 1
        Constants.GB            || 1
        (long) 3 * Constants.GB || 3
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2020_02_10")
    def "Get statistics lease"() {
        setup:
        primaryShareClient.create()
        def leaseID = setupShareLeaseCondition(primaryShareClient, receivedLeaseID)

        when:
        def resp = primaryShareClient.getStatisticsWithResponse(new ShareGetStatisticsOptions().setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID)), null, null)

        then:
        FileTestHelper.assertResponseStatusCode(resp, 200)
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2020_02_10")
    def "Get statistics lease error"() {
        setup:
        primaryShareClient.create()
        def leaseID = setupShareLeaseCondition(primaryShareClient, garbageLeaseID)

        when:
        def resp = primaryShareClient.getStatisticsWithResponse(new ShareGetStatisticsOptions().setRequestConditions(new ShareRequestConditions().setLeaseId(leaseID)), null, null)

        then:
        thrown(ShareStorageException)
    }

    def "Get statistics error"() {
        when:
        primaryShareClient.getStatistics()

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.SHARE_NOT_FOUND)
    }

    def "Create directory"() {
        given:
        primaryShareClient.create()

        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryShareClient.createDirectoryWithResponse("testCreateDirectory", null, null, null, null, null), 201)
    }

    def "Create directory file permission"() {
        given:
        primaryShareClient.create()
        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryShareClient.createDirectoryWithResponse("testCreateDirectory", null, filePermission, null, null, null), 201)
    }

    def "Create directory file permission key"() {
        given:
        primaryShareClient.create()
        def filePermissionKey = primaryShareClient.createPermission(filePermission)
        smbProperties.setFileCreationTime(namer.getUtcNow())
            .setFileLastWriteTime(namer.getUtcNow())
            .setFilePermissionKey(filePermissionKey)
        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryShareClient.createDirectoryWithResponse("testCreateDirectory", smbProperties, null, null, null, null), 201)
    }

    def "Create directory invalid name"() {
        given:
        primaryShareClient.create()

        when:
        primaryShareClient.createDirectory("test/directory")

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.PARENT_NOT_FOUND)
    }

    def "Create directory metadata"() {
        given:
        primaryShareClient.create()

        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryShareClient.createDirectoryWithResponse("testCreateDirectory", null, null, testMetadata, null, null), 201)
    }

    def "Create directory metadata error"() {
        given:
        primaryShareClient.create()

        when:
        primaryShareClient.createDirectoryWithResponse("testdirectory", null, null, Collections.singletonMap("", "value"), null, null)

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.EMPTY_METADATA_KEY)
    }

    def "Create file"() {
        given:
        primaryShareClient.create()

        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryShareClient.createFileWithResponse("testCreateFile", 1024, null, null, null, null, null, null), 201)
    }

    def "Create file file permission"() {
        given:
        primaryShareClient.create()
        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryShareClient.createFileWithResponse("testCreateFile", 1024, null, null, filePermission, null, null, null), 201)
    }

    def "Create file file permission key"() {
        given:
        primaryShareClient.create()
        def filePermissionKey = primaryShareClient.createPermission(filePermission)
        smbProperties.setFileCreationTime(namer.getUtcNow())
            .setFileLastWriteTime(namer.getUtcNow())
            .setFilePermissionKey(filePermissionKey)

        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryShareClient.createFileWithResponse("testCreateFile", 1024, null, smbProperties, null, null, null, null), 201)
    }

    @Unroll
    def "Create file invalid args"() {
        given:
        primaryShareClient.create()

        when:
        primaryShareClient.createFileWithResponse(fileName, maxSize, null, null, null, null, null, null)
        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, statusCode, errMsg)

        where:
        fileName    | maxSize | statusCode | errMsg
        "testfile:" | 1024    | 400        | ShareErrorCode.INVALID_RESOURCE_NAME
        "fileName"  | -1      | 400        | ShareErrorCode.OUT_OF_RANGE_INPUT

    }

    def "Create file maxOverload"() {
        given:
        primaryShareClient.create()
        smbProperties.setFileCreationTime(namer.getUtcNow())
            .setFileLastWriteTime(namer.getUtcNow())
        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryShareClient.createFileWithResponse("testCreateFile", 1024, null, smbProperties, filePermission, testMetadata, null, null), 201)
    }

    @Unroll
    def "Create file maxOverload invalid args"() {
        given:
        primaryShareClient.create()

        when:
        primaryShareClient.createFileWithResponse(fileName, maxSize, httpHeaders, null, null, metadata, null, null)

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, errMsg)

        where:
        fileName    | maxSize | httpHeaders                                           | metadata                              | errMsg
        "testfile:" | 1024    | null                                                  | testMetadata                          | ShareErrorCode.INVALID_RESOURCE_NAME
        "fileName"  | -1      | null                                                  | testMetadata                          | ShareErrorCode.OUT_OF_RANGE_INPUT
        "fileName"  | 1024    | new ShareFileHttpHeaders().setContentMd5(new byte[0]) | testMetadata                          | ShareErrorCode.INVALID_HEADER_VALUE
        "fileName"  | 1024    | null                                                  | Collections.singletonMap("", "value") | ShareErrorCode.EMPTY_METADATA_KEY
    }

    def "Create file in root directory"() {
        given:
        primaryShareClient.create()
        def directoryClient = primaryShareClient.getRootDirectoryClient()

        expect:
        FileTestHelper.assertResponseStatusCode(
            directoryClient.createFileWithResponse("testCreateFile", 1024, null, null, null, null, null, null), 201)
    }

    def "Delete directory"() {
        given:
        def directoryName = "testCreateDirectory"
        primaryShareClient.create()
        primaryShareClient.createDirectory(directoryName)

        expect:
        FileTestHelper.assertResponseStatusCode(primaryShareClient.deleteDirectoryWithResponse(directoryName, null, null), 202)
    }

    def "Delete directory error"() {
        given:
        primaryShareClient.create()

        when:
        primaryShareClient.deleteDirectory("testdirectory")

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND)
    }

    def "Delete file"() {
        given:
        def fileName = "testCreateFile"
        primaryShareClient.create()
        primaryShareClient.createFile(fileName, 1024)

        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryShareClient.deleteFileWithResponse(fileName, null, null), 202)
    }

    def "Delete file error"() {
        given:
        primaryShareClient.create()

        when:
        primaryShareClient.deleteFile("testdirectory")

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND)
    }

    def "Create permission"() {
        given:
        primaryShareClient.create()

        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryShareClient.createPermissionWithResponse(filePermission, null), 201)
    }

    def "Create and get permission"() {
        given:
        primaryShareClient.create()
        def permissionKey = primaryShareClient.createPermission(filePermission)

        when:
        def permission = primaryShareClient.getPermission(permissionKey)

        then:
        permission == filePermission
    }

    def "Create permission error"() {
        given:
        primaryShareClient.create()

        when:
        primaryShareClient.createPermissionWithResponse("abcde", null)
        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.fromString("FileInvalidPermission"))
    }

    def "Get permission error"() {
        given:
        primaryShareClient.create()

        when:
        primaryShareClient.getPermissionWithResponse("abcde", null)
        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.INVALID_HEADER_VALUE)
    }

    def "getSnapshot"() {
        setup:
        primaryShareClient.create()
        def snapshotId = primaryShareClient.createSnapshot().getSnapshot()

        when:
        def snapClient = primaryShareClient.getSnapshotClient(snapshotId)

        then:
        snapClient.getSnapshotId() == snapshotId
        snapClient.getShareUrl().contains("sharesnapshot=")
        primaryShareClient.getSnapshotId() == null
    }

    def "Get snapshot id"() {
        given:
        def snapshot = OffsetDateTime.of(LocalDateTime.of(2000, 1, 1,
            1, 1), ZoneOffset.UTC).toString()

        when:
        def shareSnapshotClient = shareBuilderHelper(shareName).snapshot(snapshot).buildClient()

        then:
        snapshot == shareSnapshotClient.getSnapshotId()
    }

    def "Get Share Name"() {
        expect:
        shareName == primaryShareClient.getShareName()
    }

    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials and auth would fail because we changed a signed header.
    def "Per call policy"() {
        given:
        primaryShareClient.create()

        def shareClient = shareBuilderHelper(primaryShareClient.getShareName())
            .addPolicy(getPerCallVersionPolicy()).buildClient()

        when:
        def response = shareClient.getPropertiesWithResponse(null, null)

        then:
        notThrown(ShareStorageException)
        response.getHeaders().getValue("x-ms-version") == "2017-11-09"
    }
}
