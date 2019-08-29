// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.spock

import com.azure.storage.common.credentials.SharedKeyCredential
import com.azure.storage.file.ShareClient
import com.azure.storage.file.models.CorsRule
import com.azure.storage.file.models.FileServiceProperties
import com.azure.storage.file.models.ListSharesOptions
import com.azure.storage.file.models.Metrics
import com.azure.storage.file.models.RetentionPolicy
import com.azure.storage.file.models.ShareItem
import com.azure.storage.file.models.ShareProperties
import com.azure.storage.file.models.StorageErrorCode
import com.azure.storage.file.models.StorageErrorException
import spock.lang.Unroll

class FileServiceAPITests extends APISpec {
    def shareName

    static def testMetadata = Collections.singletonMap("testmetadata", "value")
    static def reallyLongString = "thisisareallylongstringthatexceedsthe64characterlimitallowedoncertainproperties"
    static def TOO_MANY_RULES = new ArrayList<>()
    static def INVALID_ALLOWED_HEADER = Collections.singletonList(new CorsRule().allowedHeaders(reallyLongString))
    static def INVALID_EXPOSED_HEADER = Collections.singletonList(new CorsRule().exposedHeaders(reallyLongString))
    static def INVALID_ALLOWED_ORIGIN = Collections.singletonList(new CorsRule().allowedOrigins(reallyLongString))
    static def INVALID_ALLOWED_METHOD = Collections.singletonList(new CorsRule().allowedMethods("NOTAREALHTTPMETHOD"))

    def setup() {
        shareName = testResourceName.randomName(methodName, 60)
        primaryFileServiceClient = fileServiceBuilderHelper(interceptorManager).buildClient()
        for (int i = 0; i < 6; i++) {
            TOO_MANY_RULES.add(new CorsRule())
        }
    }

    def "Get file service URL"() {
        given:
        def accoutName = SharedKeyCredential.fromConnectionString(connectionString).accountName()
        def expectURL = String.format("https://%s.file.core.windows.net", accoutName)
        when:
        def fileServiceURL = primaryFileServiceClient.getFileServiceUrl().toString()
        then:
        expectURL.equals(fileServiceURL)
    }

    def "Get share does not create a share"() {
        given:
        def shareClient = primaryFileServiceClient.getShareClient(shareName)
        expect:
        shareClient instanceof ShareClient
    }

    def "Create share"() {
        when:
        def createShareResponse = primaryFileServiceClient.createShareWithResponse(shareName, null, null, null)
        then:
        FileTestHelper.assertResponseStatusCode(createShareResponse, 201)
    }

    def "Create share max overloads"() {
        when:
        def createShareResponse = primaryFileServiceClient.createShareWithResponse(shareName, testMetadata, 1, null)
        then:
        FileTestHelper.assertResponseStatusCode(createShareResponse, 201)
    }

    @Unroll
    def "Create share with invalid args"() {
        when:
        primaryFileServiceClient.createShareWithResponse(shareName, metadata, quota, null)
        then:
        def e = thrown(StorageErrorException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, statusCode, errMsg)
        where:
        metadata                                      | quota | statusCode | errMsg
        Collections.singletonMap("invalid#", "value") | 1     | 400        | StorageErrorCode.INVALID_METADATA
        testMetadata                                  | -1    | 400        | StorageErrorCode.INVALID_HEADER_VALUE
    }

    def "Delete share"() {
        given:
        primaryFileServiceClient.createShare(shareName)
        when:
        def deleteShareResponse = primaryFileServiceClient.deleteShareWithResponse(shareName, null, null)
        then:
        FileTestHelper.assertResponseStatusCode(deleteShareResponse, 202)
    }

    def "Delete share does not exist"() {
        when:
        primaryFileServiceClient.deleteShare(testResourceName.randomName(methodName, 60))
        then:
        def e = thrown(StorageErrorException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.SHARE_NOT_FOUND)
    }

    @Unroll
    def "List shares with filter"() {
        given:
        LinkedList<ShareItem> testShares = new LinkedList<>()
        for (int i = 0; i < 3; i++) {
            ShareItem share = new ShareItem().properties(new ShareProperties().quota(i + 1)).name(shareName + i)
            if (i == 2) {
                share.metadata(testMetadata)
            }

            testShares.add(share)
            primaryFileServiceClient.createShareWithResponse(share.name(), share.metadata(), share.properties().quota(), null)
        }
        when:
        def shares = primaryFileServiceClient.listShares(options).iterator()
        then:
        for (int i = 0; i < limits; i++) {
            FileTestHelper.assertSharesAreEqual(testShares.pop(), shares.next(), includeMetadata, includeSnapshot)
        }
        !shares.hasNext()
        where:
        options                                                                                          | limits | includeMetadata | includeSnapshot
        new ListSharesOptions().prefix("fileserviceapitestslistshareswithfilter")                        | 3      | false           | true
        new ListSharesOptions().prefix("fileserviceapitestslistshareswithfilter").includeMetadata(true)  | 3      | true            | true
        new ListSharesOptions().prefix("fileserviceapitestslistshareswithfilter").includeMetadata(false) | 3      | false           | true
        new ListSharesOptions().prefix("fileserviceapitestslistshareswithfilter").maxResults(2)          | 2      | true            | true
    }

    @Unroll
    def "List shares with args"() {
        given:
        LinkedList<ShareItem> testShares = new LinkedList<>()
        for (int i = 0; i < 3; i++) {
            ShareItem share = new ShareItem().name(shareName + i).properties(new ShareProperties().quota(2))
                .metadata(testMetadata)
            def shareClient = primaryFileServiceClient.getShareClient(share.name())
            shareClient.createWithResponse(share.metadata(), share.properties().quota(), null)
            if (i == 2) {
                def snapshot = shareClient.createSnapshot().snapshot()
                testShares.add(new ShareItem().name(share.name()).metadata(share.metadata()).properties(share.properties()).snapshot(snapshot))
            }
            testShares.add(share)
        }
        when:
        def shares = primaryFileServiceClient.listShares(options).iterator()
        then:
        for (int i = 0; i < limits; i++) {
            FileTestHelper.assertSharesAreEqual(testShares.pop(), shares.next(), includeMetadata, includeSnapshot)
        }
        !shares.hasNext()

        where:
        options                                                                                                               | limits | includeMetadata | includeSnapshot
        new ListSharesOptions().prefix("fileserviceapitestslistshareswithargs")                                               | 3      | false           | false
        new ListSharesOptions().prefix("fileserviceapitestslistshareswithargs") .includeMetadata(true)                        | 3      | true            | false
        new ListSharesOptions().prefix("fileserviceapitestslistshareswithargs") .includeMetadata(true).includeSnapshots(true) | 4      | true            | true
    }

    def "Set and get properties"() {
        given:
        def originalProperties = primaryFileServiceClient.getProperties()
        def retentionPolicy = new RetentionPolicy().enabled(true).days(3)
        def metrics = new Metrics().enabled(true).includeAPIs(false)
            .retentionPolicy(retentionPolicy).version("1.0")
        def updatedProperties = new FileServiceProperties().hourMetrics(metrics)
            .minuteMetrics(metrics).cors(new ArrayList<>())
        when:
        def getPropertiesBeforeResponse = primaryFileServiceClient.getPropertiesWithResponse(null)
        def setPropertiesResponse = primaryFileServiceClient.setPropertiesWithResponse(updatedProperties, null)
        def getPropertiesAfterResponse = primaryFileServiceClient.getPropertiesWithResponse(null)
        then:
        FileTestHelper.assertResponseStatusCode(getPropertiesBeforeResponse, 200)
        FileTestHelper.assertFileServicePropertiesAreEqual(originalProperties, getPropertiesBeforeResponse.value())
        FileTestHelper.assertResponseStatusCode(setPropertiesResponse, 202)
        FileTestHelper.assertResponseStatusCode(getPropertiesAfterResponse, 200)
        FileTestHelper.assertFileServicePropertiesAreEqual(updatedProperties, getPropertiesAfterResponse.value())
    }

    @Unroll
    def "Set and get properties with invalid args"() {
        given:
        def retentionPolicy = new RetentionPolicy().enabled(true).days(3)
        def metrics = new Metrics().enabled(true).includeAPIs(false)
            .retentionPolicy(retentionPolicy).version("1.0")

        when:
        def updatedProperties = new FileServiceProperties().hourMetrics(metrics)
            .minuteMetrics(metrics).cors(coreList)
        primaryFileServiceClient.setProperties(updatedProperties)
        then:
        def e = thrown(StorageErrorException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, statusCode, errMsg)
        where:
        coreList               | statusCode | errMsg
        TOO_MANY_RULES         | 400        | StorageErrorCode.INVALID_XML_DOCUMENT
        INVALID_ALLOWED_HEADER | 400        | StorageErrorCode.INVALID_XML_DOCUMENT
        INVALID_EXPOSED_HEADER | 400        | StorageErrorCode.INVALID_XML_DOCUMENT
        INVALID_ALLOWED_ORIGIN | 400        | StorageErrorCode.INVALID_XML_DOCUMENT
        INVALID_ALLOWED_METHOD | 400        | StorageErrorCode.INVALID_XML_NODE_VALUE
    }
}
