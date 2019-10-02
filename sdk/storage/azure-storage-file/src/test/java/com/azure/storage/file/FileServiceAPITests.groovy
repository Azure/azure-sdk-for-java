// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file

import com.azure.storage.common.credentials.SharedKeyCredential
import com.azure.storage.file.models.CorsRule
import com.azure.storage.file.models.FileServiceProperties
import com.azure.storage.file.models.ListSharesOptions
import com.azure.storage.file.models.Metrics
import com.azure.storage.file.models.RetentionPolicy
import com.azure.storage.file.models.ShareItem
import com.azure.storage.file.models.ShareProperties
import com.azure.storage.file.models.StorageErrorCode
import com.azure.storage.file.models.StorageException
import spock.lang.Unroll

class FileServiceAPITests extends APISpec {
    def shareName

    static def testMetadata = Collections.singletonMap("testmetadata", "value")
    static def reallyLongString = "thisisareallylongstringthatexceedsthe64characterlimitallowedoncertainproperties"
    static def TOO_MANY_RULES = new ArrayList<>()
    static def INVALID_ALLOWED_HEADER = Collections.singletonList(new CorsRule().setAllowedHeaders(reallyLongString))
    static def INVALID_EXPOSED_HEADER = Collections.singletonList(new CorsRule().setExposedHeaders(reallyLongString))
    static def INVALID_ALLOWED_ORIGIN = Collections.singletonList(new CorsRule().setAllowedOrigins(reallyLongString))
    static def INVALID_ALLOWED_METHOD = Collections.singletonList(new CorsRule().setAllowedMethods("NOTAREALHTTPMETHOD"))

    def setup() {
        shareName = testResourceName.randomName(methodName, 60)
        primaryFileServiceClient = fileServiceBuilderHelper(interceptorManager).buildClient()
        for (int i = 0; i < 6; i++) {
            TOO_MANY_RULES.add(new CorsRule())
        }
    }

    def "Get file service URL"() {
        given:
        def accountName = SharedKeyCredential.fromConnectionString(connectionString).getAccountName()
        def expectURL = String.format("https://%s.file.core.windows.net", accountName)
        when:
        def fileServiceURL = primaryFileServiceClient.getFileServiceUrl()

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
        def createShareResponse = primaryFileServiceClient.createShareWithResponse(shareName, null, null, null, null)

        then:
        FileTestHelper.assertResponseStatusCode(createShareResponse, 201)
    }

    def "Create share max overloads"() {
        when:
        def createShareResponse = primaryFileServiceClient.createShareWithResponse(shareName, testMetadata, 1, null, null)

        then:
        FileTestHelper.assertResponseStatusCode(createShareResponse, 201)
    }

    @Unroll
    def "Create share with invalid args"() {
        when:
        primaryFileServiceClient.createShareWithResponse(shareName, metadata, quota, null, null)

        then:
        def e = thrown(StorageException)
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
        def deleteShareResponse = primaryFileServiceClient.deleteShareWithResponse(shareName, null, null, null)

        then:
        FileTestHelper.assertResponseStatusCode(deleteShareResponse, 202)
    }

    def "Delete share does not exist"() {
        when:
        primaryFileServiceClient.deleteShare(testResourceName.randomName(methodName, 60))

        then:
        def e = thrown(StorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.SHARE_NOT_FOUND)
    }

    @Unroll
    def "List shares with filter"() {
        given:
        LinkedList<ShareItem> testShares = new LinkedList<>()
        for (int i = 0; i < 3; i++) {
            ShareItem share = new ShareItem().setProperties(new ShareProperties().setQuota(i + 1)).setName(shareName + i)
            if (i == 2) {
                share.setMetadata(testMetadata)
            }

            testShares.add(share)
            primaryFileServiceClient.createShareWithResponse(share.getName(), share.getMetadata(), share.getProperties().getQuota(), null, null)
        }

        when:
        def shares = primaryFileServiceClient.listShares(options, null, null).iterator()

        then:
        for (int i = 0; i < limits; i++) {
            FileTestHelper.assertSharesAreEqual(testShares.pop(), shares.next(), includeMetadata, includeSnapshot)
        }
        !shares.hasNext()

        where:
        options                                                                                                | limits | includeMetadata | includeSnapshot
        new ListSharesOptions().setPrefix("fileserviceapitestslistshareswithfilter")                           | 3      | false           | true
        new ListSharesOptions().setPrefix("fileserviceapitestslistshareswithfilter").setIncludeMetadata(true)  | 3      | true            | true
        new ListSharesOptions().setPrefix("fileserviceapitestslistshareswithfilter").setIncludeMetadata(false) | 3      | false           | true
        new ListSharesOptions().setPrefix("fileserviceapitestslistshareswithfilter").setMaxResults(2)          | 3      | false           | true
    }

    @Unroll
    def "List shares with args"() {
        given:
        LinkedList<ShareItem> testShares = new LinkedList<>()
        for (int i = 0; i < 3; i++) {
            ShareItem share = new ShareItem().setName(shareName + i).setProperties(new ShareProperties().setQuota(2))
                .setMetadata(testMetadata)
            def shareClient = primaryFileServiceClient.getShareClient(share.getName())
            shareClient.createWithResponse(share.getMetadata(), share.getProperties().getQuota(), null, null)
            if (i == 2) {
                def snapshot = shareClient.createSnapshot().getSnapshot()
                testShares.add(new ShareItem().setName(share.getName()).setMetadata(share.getMetadata()).setProperties(share.getProperties()).setSnapshot(snapshot))
            }
            testShares.add(share)
        }

        when:
        def shares = primaryFileServiceClient.listShares(options, null, null).iterator()

        then:
        for (int i = 0; i < limits; i++) {
            assert FileTestHelper.assertSharesAreEqual(testShares.pop(), shares.next(), includeMetadata, includeSnapshot)
        }
        !shares.hasNext()

        where:
        options                                                                                                                        | limits | includeMetadata | includeSnapshot
        new ListSharesOptions().setPrefix("fileserviceapitestslistshareswithargs")                                                     | 3      | false           | false
        new ListSharesOptions().setPrefix("fileserviceapitestslistshareswithargs") .setIncludeMetadata(true)                           | 3      | true            | false
        new ListSharesOptions().setPrefix("fileserviceapitestslistshareswithargs") .setIncludeMetadata(true).setIncludeSnapshots(true) | 4      | true            | true
    }

    def "Set and get properties"() {
        given:
        def originalProperties = primaryFileServiceClient.getProperties()
        def retentionPolicy = new RetentionPolicy().setEnabled(true).setDays(3)
        def metrics = new Metrics().setEnabled(true).setIncludeAPIs(false)
            .setRetentionPolicy(retentionPolicy).setVersion("1.0")
        def updatedProperties = new FileServiceProperties().setHourMetrics(metrics)
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

    @Unroll
    def "Set and get properties with invalid args"() {
        given:
        def retentionPolicy = new RetentionPolicy().setEnabled(true).setDays(3)
        def metrics = new Metrics().setEnabled(true).setIncludeAPIs(false)
            .setRetentionPolicy(retentionPolicy).setVersion("1.0")

        when:
        def updatedProperties = new FileServiceProperties().setHourMetrics(metrics)
            .setMinuteMetrics(metrics).setCors(coreList)
        primaryFileServiceClient.setProperties(updatedProperties)

        then:
        def e = thrown(StorageException)
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
