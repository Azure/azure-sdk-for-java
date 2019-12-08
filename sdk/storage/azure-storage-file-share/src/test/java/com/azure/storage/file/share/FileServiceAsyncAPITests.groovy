// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share

import com.azure.storage.common.StorageSharedKeyCredential
import com.azure.storage.file.share.models.ListSharesOptions
import com.azure.storage.file.share.models.ShareCorsRule
import com.azure.storage.file.share.models.ShareErrorCode
import com.azure.storage.file.share.models.ShareItem
import com.azure.storage.file.share.models.ShareMetrics
import com.azure.storage.file.share.models.ShareProperties
import com.azure.storage.file.share.models.ShareRetentionPolicy
import com.azure.storage.file.share.models.ShareServiceProperties
import reactor.test.StepVerifier
import spock.lang.Unroll

class FileServiceAsyncAPITests extends APISpec {
    String shareName

    static def testMetadata = Collections.singletonMap("testmetadata", "value")
    static def reallyLongString = "thisisareallylongstringthatexceedsthe64characterlimitallowedoncertainproperties"
    static def TOO_MANY_RULES = new ArrayList<ShareCorsRule>()
    static def INVALID_ALLOWED_HEADER = Collections.singletonList(new ShareCorsRule().setAllowedHeaders(reallyLongString))
    static def INVALID_EXPOSED_HEADER = Collections.singletonList(new ShareCorsRule().setExposedHeaders(reallyLongString))
    static def INVALID_ALLOWED_ORIGIN = Collections.singletonList(new ShareCorsRule().setAllowedOrigins(reallyLongString))
    static def INVALID_ALLOWED_METHOD = Collections.singletonList(new ShareCorsRule().setAllowedMethods("NOTAREALHTTPMETHOD"))

    def setup() {
        shareName = testResourceName.randomName(methodName, 60)
        primaryFileServiceAsyncClient = fileServiceBuilderHelper(interceptorManager).buildAsyncClient()
        for (int i = 0; i < 6; i++) {
            TOO_MANY_RULES.add(new ShareCorsRule())
        }
    }

    def "Get file service URL"() {
        given:
        def accountName = StorageSharedKeyCredential.fromConnectionString(connectionString).getAccountName()
        def expectURL = String.format("https://%s.file.core.windows.net", accountName)

        when:
        def fileServiceURL = primaryFileServiceAsyncClient.getFileServiceUrl()

        then:
        expectURL == fileServiceURL
    }

    def "Get share does not create a share"() {
        when:
        def shareAsyncClient = primaryFileServiceAsyncClient.getShareAsyncClient(shareName)

        then:
        shareAsyncClient instanceof ShareAsyncClient
    }

    def "Create share"() {
        when:
        def createShareVerifier = StepVerifier.create(primaryFileServiceAsyncClient.createShareWithResponse(shareName, null, null))

        then:
        createShareVerifier.assertNext {
            assert FileTestHelper.assertResponseStatusCode(it, 201)
        }.verifyComplete()
    }

    @Unroll
    def "Create share with metadata"() {
        when:
        def createShareVerifier = StepVerifier.create(primaryFileServiceAsyncClient.createShareWithResponse(shareName, metadata, quota))

        then:
        createShareVerifier.assertNext {
            assert FileTestHelper.assertResponseStatusCode(it, 201)
        }.verifyComplete()

        where:
        metadata     | quota
        null         | null
        testMetadata | null
        null         | 1
        testMetadata | 1
    }

    @Unroll
    def "Create share with invalid args"() {
        when:
        def createShareVerifier = StepVerifier.create(primaryFileServiceAsyncClient.createShareWithResponse(shareName, metadata, quota))

        then:
        createShareVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, statusCode, errMsg)
        }

        where:
        metadata                                      | quota | statusCode | errMsg
        Collections.singletonMap("invalid#", "value") | 1     | 400        | ShareErrorCode.INVALID_METADATA
        testMetadata                                  | -1    | 400        | ShareErrorCode.INVALID_HEADER_VALUE
        testMetadata                                  | 0     | 400        | ShareErrorCode.INVALID_HEADER_VALUE
        testMetadata                                  | 5200  | 400        | ShareErrorCode.INVALID_HEADER_VALUE
    }

    def "Delete share"() {
        given:
        primaryFileServiceAsyncClient.createShare(shareName).block()

        when:
        def deleteShareVerifier = StepVerifier.create(primaryFileServiceAsyncClient.deleteShareWithResponse(shareName, null))

        then:
        deleteShareVerifier.assertNext {
            assert FileTestHelper.assertResponseStatusCode(it, 202)
        }.verifyComplete()
    }

    def "Delete share does not exist"() {
        when:
        def deleteShareVerifier = StepVerifier.create(primaryFileServiceAsyncClient.deleteShare(testResourceName.randomName(methodName, 60)))

        then:
        deleteShareVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.SHARE_NOT_FOUND)
        }
    }

    @Unroll
    def "List shares with filter"() {
        given:
        LinkedList<ShareItem> testShares = new LinkedList<>()
        for (int i = 0; i < 3; i++) {
            ShareItem share = new ShareItem().setName(shareName + i).setProperties(new ShareProperties().setQuota(i + 1))
            if (i == 2) {
                share.setMetadata(testMetadata)
            }
            testShares.add(share)
            primaryFileServiceAsyncClient.createShareWithResponse(share.getName(), share.getMetadata(), share.getProperties().getQuota()).block()
        }

        when:
        def sharesVerifier = StepVerifier.create(primaryFileServiceAsyncClient.listShares(options))

        then:
        sharesVerifier.thenConsumeWhile {
            FileTestHelper.assertSharesAreEqual(testShares.pop(), it, includeMetadata, includeSnapshot)
        }.verifyComplete()

        for (int i = 0; i < 3 - limits; i++) {
            testShares.pop()
        }

        testShares.isEmpty()

        where:
        options                                                                                                     | limits | includeMetadata | includeSnapshot
        new ListSharesOptions().setPrefix("fileserviceasyncapitestslistshareswithfilter")                           | 3      | false           | true
        new ListSharesOptions().setPrefix("fileserviceasyncapitestslistshareswithfilter").setIncludeMetadata(true)  | 3      | true            | true
        new ListSharesOptions().setPrefix("fileserviceasyncapitestslistshareswithfilter").setIncludeMetadata(false) | 3      | false           | true
        new ListSharesOptions().setPrefix("fileserviceasyncapitestslistshareswithfilter").setMaxResultsPerPage(2)   | 3      | false           | true
    }

    @Unroll
    def "List shares with args"() {
        given:
        LinkedList<ShareItem> testShares = new LinkedList<>()
        for (int i = 0; i < 3; i++) {
            ShareItem share = new ShareItem().setName(shareName + i).setProperties(new ShareProperties().setQuota(2))
                .setMetadata(testMetadata)
            def shareAsyncClient = primaryFileServiceAsyncClient.getShareAsyncClient(share.getName())
            shareAsyncClient.createWithResponse(share.getMetadata(), share.getProperties().getQuota()).block()
            if (i == 2) {
                StepVerifier.create(shareAsyncClient.createSnapshotWithResponse(null))
                    .assertNext {
                        testShares.add(new ShareItem().setName(share.getName()).setMetadata(share.getMetadata()).setProperties(share.getProperties()).setSnapshot(it.getValue().getSnapshot()))
                        FileTestHelper.assertResponseStatusCode(it, 201)
                    }.verifyComplete()
            }
            testShares.add(share)
        }

        when:
        def sharesVerifier = StepVerifier.create(primaryFileServiceAsyncClient.listShares(options))

        then:
        sharesVerifier.assertNext {
            assert FileTestHelper.assertSharesAreEqual(testShares.pop(), it, includeMetadata, includeSnapshot)
        }.expectNextCount(limits - 1).verifyComplete()

        where:
        options                                                                                                                            | limits | includeMetadata | includeSnapshot
        new ListSharesOptions().setPrefix("fileserviceasyncapitestslistshareswithargs")                                                    | 3      | false           | false
        new ListSharesOptions().setPrefix("fileserviceasyncapitestslistshareswithargs").setIncludeMetadata(true)                           | 3      | true            | false
        new ListSharesOptions().setPrefix("fileserviceasyncapitestslistshareswithargs").setIncludeMetadata(true).setIncludeSnapshots(true) | 4      | true            | true
    }

    def "Set and get properties"() {
        given:
        def originalProperties = primaryFileServiceAsyncClient.getProperties().block()
        def retentionPolicy = new ShareRetentionPolicy().setEnabled(true).setDays(3)
        def metrics = new ShareMetrics().setEnabled(true).setIncludeApis(false)
            .setRetentionPolicy(retentionPolicy).setVersion("1.0")
        def updatedProperties = new ShareServiceProperties().setHourMetrics(metrics)
            .setMinuteMetrics(metrics).setCors(new ArrayList<>())

        when:
        def getPropertiesBeforeVerifier = StepVerifier.create(primaryFileServiceAsyncClient.getPropertiesWithResponse())
        def setPropertiesVerifier = StepVerifier.create(primaryFileServiceAsyncClient.setPropertiesWithResponse(updatedProperties))
        def getPropertiesAfterVerifier = StepVerifier.create(primaryFileServiceAsyncClient.getPropertiesWithResponse())

        then:
        getPropertiesBeforeVerifier.assertNext {
            assert FileTestHelper.assertResponseStatusCode(it, 200)
            assert FileTestHelper.assertFileServicePropertiesAreEqual(originalProperties, it.getValue())
        }.verifyComplete()
        setPropertiesVerifier.assertNext {
            assert FileTestHelper.assertResponseStatusCode(it, 202)
        }.verifyComplete()

        getPropertiesAfterVerifier.assertNext {
            assert FileTestHelper.assertResponseStatusCode(it, 200)
            assert FileTestHelper.assertFileServicePropertiesAreEqual(originalProperties, it.getValue())
        }.verifyComplete()
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
        def setPropertyVerifier = StepVerifier.create(primaryFileServiceAsyncClient.setProperties(updatedProperties))

        then:
        setPropertyVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, statusCode, errMsg)
        }

        where:
        coreList               | statusCode | errMsg
        TOO_MANY_RULES         | 400        | ShareErrorCode.INVALID_XML_DOCUMENT
        INVALID_ALLOWED_HEADER | 400        | ShareErrorCode.INVALID_XML_DOCUMENT
        INVALID_EXPOSED_HEADER | 400        | ShareErrorCode.INVALID_XML_DOCUMENT
        INVALID_ALLOWED_ORIGIN | 400        | ShareErrorCode.INVALID_XML_DOCUMENT
        INVALID_ALLOWED_METHOD | 400        | ShareErrorCode.INVALID_XML_NODE_VALUE

    }
}
