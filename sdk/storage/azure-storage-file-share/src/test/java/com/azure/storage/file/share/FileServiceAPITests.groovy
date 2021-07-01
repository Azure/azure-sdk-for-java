// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share

import com.azure.core.util.Context
import com.azure.storage.common.StorageSharedKeyCredential
import com.azure.storage.common.test.shared.extensions.PlaybackOnly
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion
import com.azure.storage.file.share.models.ListSharesOptions
import com.azure.storage.file.share.models.ShareAccessTier
import com.azure.storage.file.share.models.ShareCorsRule
import com.azure.storage.file.share.models.ShareErrorCode
import com.azure.storage.file.share.models.ShareItem
import com.azure.storage.file.share.models.ShareMetrics
import com.azure.storage.file.share.models.ShareProperties
import com.azure.storage.file.share.models.ShareProtocolSettings
import com.azure.storage.file.share.models.ShareRetentionPolicy
import com.azure.storage.file.share.models.ShareServiceProperties
import com.azure.storage.file.share.models.ShareSmbSettings
import com.azure.storage.file.share.models.ShareStorageException
import com.azure.storage.file.share.models.SmbMultichannel
import com.azure.storage.file.share.options.ShareCreateOptions
import com.azure.storage.file.share.options.ShareSetPropertiesOptions
import spock.lang.IgnoreIf
import spock.lang.Requires
import spock.lang.ResourceLock
import spock.lang.Unroll

import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

class FileServiceAPITests extends APISpec {
    String shareName

    static def testMetadata = Collections.singletonMap("testmetadata", "value")
    static def reallyLongString = "thisisareallylongstringthatexceedsthe64characterlimitallowedoncertainproperties"
    static def TOO_MANY_RULES = new ArrayList<ShareCorsRule>()
    static def INVALID_ALLOWED_HEADER = Collections.singletonList(new ShareCorsRule().setAllowedHeaders(reallyLongString))
    static def INVALID_EXPOSED_HEADER = Collections.singletonList(new ShareCorsRule().setExposedHeaders(reallyLongString))
    static def INVALID_ALLOWED_ORIGIN = Collections.singletonList(new ShareCorsRule().setAllowedOrigins(reallyLongString))
    static def INVALID_ALLOWED_METHOD = Collections.singletonList(new ShareCorsRule().setAllowedMethods("NOTAREALHTTPMETHOD"))

    def setupSpec() {
        for (int i = 0; i < 6; i++) {
            TOO_MANY_RULES.add(new ShareCorsRule())
        }
        TOO_MANY_RULES = Collections.unmodifiableList(TOO_MANY_RULES)
    }

    def setup() {
        shareName = namer.getRandomName(60)
        primaryFileServiceClient = fileServiceBuilderHelper().buildClient()
    }

    def "Get file service URL"() {
        given:
        def accountName = StorageSharedKeyCredential.fromConnectionString(env.primaryAccount.connectionString).getAccountName()
        def expectURL = String.format("https://%s.file.core.windows.net", accountName)
        when:
        def fileServiceURL = primaryFileServiceClient.getFileServiceUrl()

        then:
        expectURL == fileServiceURL
    }

    def "Get share does not create a share"() {
        given:
        def shareClient = primaryFileServiceClient.getShareClient(shareName)

        expect:
        shareClient instanceof ShareClient
    }

    def "Create share"() {
        when:
        def createShareResponse = primaryFileServiceClient.createShareWithResponse(shareName, null, null, null, null)

        then:
        FileTestHelper.assertResponseStatusCode(createShareResponse, 201)
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2019_12_12")
    def "Create share max overloads"() {
        when:
        def createShareResponse = primaryFileServiceClient.createShareWithResponse(shareName, new ShareCreateOptions()
            .setQuotaInGb(1).setMetadata(testMetadata).setAccessTier(ShareAccessTier.HOT), null, null)

        then:
        FileTestHelper.assertResponseStatusCode(createShareResponse, 201)
    }

    @Unroll
    def "Create share with invalid args"() {
        when:
        primaryFileServiceClient.createShareWithResponse(shareName, metadata, quota, null, null)

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, statusCode, errMsg)

        where:
        metadata                                      | quota | statusCode | errMsg
        Collections.singletonMap("invalid#", "value") | 1     | 400        | ShareErrorCode.INVALID_METADATA
        testMetadata                                  | -1    | 400        | ShareErrorCode.INVALID_HEADER_VALUE
    }

    def "Delete share"() {
        given:
        primaryFileServiceClient.createShare(shareName)

        when:
        def deleteShareResponse = primaryFileServiceClient.deleteShareWithResponse(shareName, null, null, null)

        then:
        FileTestHelper.assertResponseStatusCode(deleteShareResponse, 202)
    }

    def "Delete share does not exist"() {
        when:
        primaryFileServiceClient.deleteShare(namer.getRandomName(60))

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.SHARE_NOT_FOUND)
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "List shares with filter"() {
        given:
        LinkedList<ShareItem> testShares = new LinkedList<>()
        options.setPrefix(shareName)
        for (int i = 0; i < 4; i++) {
            ShareItem share = new ShareItem().setProperties(new ShareProperties().setQuota(i + 1)).setName(shareName + i)
            if (i == 2) {
                share.setMetadata(testMetadata)
            }

            testShares.add(share)
            primaryFileServiceClient.createShareWithResponse(share.getName(), share.getMetadata(), share.getProperties().getQuota(), null, null)

            if (i == 3) {
                share.getProperties().setDeletedTime(OffsetDateTime.now())
                primaryFileServiceClient.deleteShare(share.getName())
            }
        }

        when:
        def shares = primaryFileServiceClient.listShares(options, null, null).iterator()

        then:
        for (int i = 0; i < limits; i++) {
            FileTestHelper.assertSharesAreEqual(testShares.pop(), shares.next(), includeMetadata, includeSnapshot, includeDeleted)
        }
        includeDeleted || !shares.hasNext()

        where:
        options                                           | limits | includeMetadata | includeSnapshot | includeDeleted
        new ListSharesOptions()                           | 3      | false           | true            | false
        new ListSharesOptions().setIncludeMetadata(true)  | 3      | true            | true            | false
        new ListSharesOptions().setIncludeMetadata(false) | 3      | false           | true            | false
        new ListSharesOptions().setMaxResultsPerPage(2)   | 3      | false           | true            | false
        new ListSharesOptions().setIncludeDeleted(true)   | 4      | false           | true            | true
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "List shares with args"() {
        given:
        LinkedList<ShareItem> testShares = new LinkedList<>()
        options.setPrefix(shareName)
        for (int i = 0; i < 4; i++) {
            ShareItem share = new ShareItem().setName(shareName + i).setProperties(new ShareProperties().setQuota(2))
                .setMetadata(testMetadata)
            def shareClient = primaryFileServiceClient.getShareClient(share.getName())
            shareClient.createWithResponse(share.getMetadata(), share.getProperties().getQuota(), null, null)
            if (i == 2) {
                def snapshot = shareClient.createSnapshot().getSnapshot()
                testShares.add(new ShareItem().setName(share.getName()).setMetadata(share.getMetadata()).setProperties(share.getProperties()).setSnapshot(snapshot))
            }
            if (i == 3) {
                share.getProperties().setDeletedTime(OffsetDateTime.now())
                primaryFileServiceClient.deleteShare(share.getName())
            }
            testShares.add(share)
        }

        when:
        def shares = primaryFileServiceClient.listShares(options, null, null).iterator()

        then:
        for (int i = 0; i < limits; i++) {
            assert FileTestHelper.assertSharesAreEqual(testShares.pop(), shares.next(), includeMetadata, includeSnapshot, includeDeleted)
        }
        !shares.hasNext()

        where:
        options                                                                                            | limits | includeMetadata | includeSnapshot | includeDeleted
        new ListSharesOptions()                                                                            | 3      | false           | false           | false
        new ListSharesOptions().setIncludeMetadata(true)                                                   | 3      | true            | false           | false
        new ListSharesOptions().setIncludeMetadata(true).setIncludeSnapshots(true)                         | 4      | true            | true            | false
        new ListSharesOptions().setIncludeMetadata(true).setIncludeSnapshots(true).setIncludeDeleted(true) | 5      | true            | true            | true
    }

    def "List shares max results by page"() {
        given:
        LinkedList<ShareItem> testShares = new LinkedList<>()
        def options = new ListSharesOptions().setPrefix(shareName)
        for (int i = 0; i < 4; i++) {
            ShareItem share = new ShareItem().setName(shareName + i)
            def shareClient = primaryFileServiceClient.getShareClient(share.getName())
            shareClient.create()
            testShares.add(share)
        }

        when:
        def shares = primaryFileServiceClient.listShares(options, null, null).iterableByPage(2).iterator()

        then:
        for (def page : shares) {
            assert page.value.size() <= 2
        }
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2019_12_12")
    def "List shares get access tier"() {
        setup:
        def shareName = generateShareName()
        def share = primaryFileServiceClient.createShareWithResponse(shareName, new ShareCreateOptions().setAccessTier(ShareAccessTier.HOT), null, null).getValue()

        def time = namer.getUtcNow().truncatedTo(ChronoUnit.SECONDS)
        time = time.minusSeconds(1) // account for time skew on the other side.
        share.setProperties(new ShareSetPropertiesOptions().setAccessTier(ShareAccessTier.TRANSACTION_OPTIMIZED))

        when:
        def shares = primaryFileServiceClient.listShares(new ListSharesOptions().setPrefix(namer.getResourcePrefix()), null, null).iterator()

        then:
        def item = shares.next()
        item.getName() == shareName
        item.getProperties().getAccessTier() == ShareAccessTier.TRANSACTION_OPTIMIZED.toString()
        item.getProperties().getAccessTierChangeTime().isEqual(time) || item.getProperties().getAccessTierChangeTime().isAfter(time)
        item.getProperties().getAccessTierChangeTime().isBefore(time.plusMinutes(1))
        item.getProperties().getAccessTierTransitionState() == "pending-from-hot"
    }

    def "List shares with premium share"() {
        setup:
        def premiumShareName = generateShareName()
        premiumFileServiceClient.createShare(premiumShareName)

        when:
        def shares = premiumFileServiceClient.listShares().iterator()

        then:
        for (def shareItem : shares) {
            if (shareItem.getName() == premiumShareName) {
                shareItem.getProperties().getETag()
                shareItem.getProperties().getMetadata()
                shareItem.getProperties().getLastModified()
                shareItem.getProperties().getNextAllowedQuotaDowngradeTime()
                shareItem.getProperties().getProvisionedEgressMBps()
                shareItem.getProperties().getProvisionedIngressMBps()
                shareItem.getProperties().getProvisionedIops()
            }
        }
    }

    @ResourceLock("ServiceProperties")
    def "Set and get properties"() {
        given:
        def originalProperties = primaryFileServiceClient.getProperties()
        def retentionPolicy = new ShareRetentionPolicy().setEnabled(true).setDays(3)
        def metrics = new ShareMetrics().setEnabled(true).setIncludeApis(false)
            .setRetentionPolicy(retentionPolicy).setVersion("1.0")
        def updatedProperties = new ShareServiceProperties().setHourMetrics(metrics)
            .setMinuteMetrics(metrics).setCors(new ArrayList<>())

        when:
        def getPropertiesBeforeResponse = primaryFileServiceClient.getPropertiesWithResponse(null, null)
        def setPropertiesResponse = primaryFileServiceClient.setPropertiesWithResponse(updatedProperties, null, null)
        def getPropertiesAfterResponse = primaryFileServiceClient.getPropertiesWithResponse(null, null)

        then:
        FileTestHelper.assertResponseStatusCode(getPropertiesBeforeResponse, 200)
        FileTestHelper.assertFileServicePropertiesAreEqual(originalProperties, getPropertiesBeforeResponse.getValue())
        FileTestHelper.assertResponseStatusCode(setPropertiesResponse, 202)
        FileTestHelper.assertResponseStatusCode(getPropertiesAfterResponse, 200)
        FileTestHelper.assertFileServicePropertiesAreEqual(updatedProperties, getPropertiesAfterResponse.getValue())
    }

    @PlaybackOnly
    @ResourceLock("ServiceProperties")
    def "Set and get properties premium"() {
        given:
        def originalProperties = premiumFileServiceClient.getProperties()
        def retentionPolicy = new ShareRetentionPolicy().setEnabled(true).setDays(3)
        def metrics = new ShareMetrics().setEnabled(true).setIncludeApis(false)
            .setRetentionPolicy(retentionPolicy).setVersion("1.0")
        def protocolSettings = new ShareProtocolSettings().setSmb(new ShareSmbSettings().setMultichannel(new SmbMultichannel().setEnabled(true)))
        def updatedProperties = new ShareServiceProperties().setHourMetrics(metrics)
            .setMinuteMetrics(metrics).setCors(new ArrayList<>())
            .setProtocol(protocolSettings)

        when:
        def getPropertiesBeforeResponse = premiumFileServiceClient.getPropertiesWithResponse(null, null)
        def setPropertiesResponse = premiumFileServiceClient.setPropertiesWithResponse(updatedProperties, null, null)
        def getPropertiesAfterResponse = premiumFileServiceClient.getPropertiesWithResponse(null, null)

        then:
        FileTestHelper.assertResponseStatusCode(getPropertiesBeforeResponse, 200)
        FileTestHelper.assertFileServicePropertiesAreEqual(originalProperties, getPropertiesBeforeResponse.getValue())
        FileTestHelper.assertResponseStatusCode(setPropertiesResponse, 202)
        FileTestHelper.assertResponseStatusCode(getPropertiesAfterResponse, 200)
        FileTestHelper.assertFileServicePropertiesAreEqual(updatedProperties, getPropertiesAfterResponse.getValue())
    }

    @Unroll
    def "Set and get properties with invalid args"() {
        given:
        def retentionPolicy = new ShareRetentionPolicy().setEnabled(true).setDays(3)
        def metrics = new ShareMetrics().setEnabled(true).setIncludeApis(false)
            .setRetentionPolicy(retentionPolicy).setVersion("1.0")

        when:
        def updatedProperties = new ShareServiceProperties().setHourMetrics(metrics)
            .setMinuteMetrics(metrics).setCors(coreList)
        primaryFileServiceClient.setProperties(updatedProperties)

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, statusCode, errMsg)

        where:
        coreList               | statusCode | errMsg
        TOO_MANY_RULES         | 400        | ShareErrorCode.INVALID_XML_DOCUMENT
        INVALID_ALLOWED_HEADER | 400        | ShareErrorCode.INVALID_XML_DOCUMENT
        INVALID_EXPOSED_HEADER | 400        | ShareErrorCode.INVALID_XML_DOCUMENT
        INVALID_ALLOWED_ORIGIN | 400        | ShareErrorCode.INVALID_XML_DOCUMENT
        INVALID_ALLOWED_METHOD | 400        | ShareErrorCode.INVALID_XML_NODE_VALUE
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2019_12_12")
    def "Restore share min"() {
        given:
        def shareClient = primaryFileServiceClient.getShareClient(generateShareName())
        shareClient.create()
        def fileName = generatePathName()
        shareClient.getFileClient(fileName).create(2)
        shareClient.delete()
        sleepIfLive(30000)
        def shareItem = primaryFileServiceClient.listShares(
            new ListSharesOptions()
                .setPrefix(shareClient.getShareName())
                .setIncludeDeleted(true),
            null, Context.NONE).first()

        when:
        def restoredShareClient = primaryFileServiceClient.undeleteShare(shareItem.getName(), shareItem.getVersion())

        then:
        restoredShareClient.getFileClient(fileName).exists()
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2019_12_12")
    def "Restore share max"() {
        given:
        def shareClient = primaryFileServiceClient.getShareClient(generateShareName())
        shareClient.create()
        def fileName = generatePathName()
        shareClient.getFileClient(fileName).create(2)
        shareClient.delete()
        sleepIfLive(30000)
        def shareItem = primaryFileServiceClient.listShares(
            new ListSharesOptions()
                .setPrefix(shareClient.getShareName())
                .setIncludeDeleted(true),
            null, Context.NONE).first()

        when:
        def restoredShareClient = primaryFileServiceClient.undeleteShareWithResponse(
            shareItem.getName(), shareItem.getVersion(), null, Context.NONE)
            .getValue()

        then:
        restoredShareClient.getFileClient(fileName).exists()
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2019_12_12")
    def "Restore share error"() {
        when:
        primaryFileServiceClient.undeleteShare(generateShareName(), "01D60F8BB59A4652")

        then:
        thrown(ShareStorageException.class)
    }

    @IgnoreIf( { getEnv().serviceVersion != null } )
    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials and auth would fail because we changed a signed header.
    def "Per call policy"() {
        def serviceClient = getServiceClient(env.primaryAccount.credential, primaryFileServiceClient.getFileServiceUrl(), getPerCallVersionPolicy())

        when:
        def response = serviceClient.getPropertiesWithResponse(null, null)

        then:
        notThrown(ShareStorageException)
        response.getHeaders().getValue("x-ms-version") == "2017-11-09"
    }
}
