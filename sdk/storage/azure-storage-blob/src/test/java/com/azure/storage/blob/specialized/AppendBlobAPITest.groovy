// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized

import com.azure.core.exception.UnexpectedLengthException
import com.azure.core.util.Context
import com.azure.storage.blob.APISpec
import com.azure.storage.blob.models.BlobErrorCode
import com.azure.storage.blob.models.AppendBlobRequestConditions
import com.azure.storage.blob.models.BlobHttpHeaders
import com.azure.storage.blob.models.BlobRange
import com.azure.storage.blob.models.BlobRequestConditions
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.blob.models.PublicAccessType
import spock.lang.Unroll

import java.security.MessageDigest

class AppendBlobAPITest extends APISpec {
    AppendBlobClient bc
    String blobName

    def setup() {
        blobName = generateBlobName()
        bc = cc.getBlobClient(blobName).getAppendBlobClient()
        bc.create()
    }

    def "Create defaults"() {
        when:
        def createResponse = bc.createWithResponse(null, null, null, null, null)

        then:
        createResponse.getStatusCode() == 201
        validateBasicHeaders(createResponse.getHeaders())
        createResponse.getValue().getContentMd5() == null
        createResponse.getValue().isServerEncrypted()
    }

    def "Create min"() {
        expect:
        bc.createWithResponse(null, null, null, null, null).getStatusCode() == 201
    }

    def "Create error"() {
        when:
        bc.createWithResponse(null, null, new BlobRequestConditions().setIfMatch("garbage"), null, Context.NONE)

        then:
        thrown(BlobStorageException)
    }

    @Unroll
    def "Create headers"() {
        setup:
        def headers = new BlobHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType)

        when:
        bc.createWithResponse(headers, null, null, null, null)
        def response = bc.getPropertiesWithResponse(null, null, null)

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType

        then:
        validateBlobProperties(response, cacheControl, contentDisposition, contentEncoding, contentLanguage, contentMD5, contentType)

        where:
        cacheControl | contentDisposition | contentEncoding | contentLanguage | contentMD5                                                                                  | contentType
        null         | null               | null            | null            | null                                                                                        | null
        "control"    | "disposition"      | "encoding"      | "language"      | Base64.getEncoder().encode(MessageDigest.getInstance("MD5").digest(defaultText.getBytes())) | "type"
    }

    @Unroll
    def "Create metadata"() {
        setup:
        def metadata = new HashMap<String, String>()
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }

        when:
        bc.createWithResponse(null, metadata, null, null, Context.NONE)
        def response = bc.getProperties()

        then:
        response.getMetadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @Unroll
    def "Create AC"() {
        setup:
        match = setupBlobMatchCondition(bc, match)
        leaseID = setupBlobLeaseCondition(bc, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        bc.createWithResponse(null, null, bac, null, null).getStatusCode() == 201

        where:
        modified | unmodified | match        | noneMatch   | leaseID
        null     | null       | null         | null        | null
        oldDate  | null       | null         | null        | null
        null     | newDate    | null         | null        | null
        null     | null       | receivedEtag | null        | null
        null     | null       | null         | garbageEtag | null
        null     | null       | null         | null        | receivedLeaseID
    }

    @Unroll
    def "Create AC fail"() {
        setup:
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        setupBlobLeaseCondition(bc, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        bc.createWithResponse(null, null, bac, null, Context.NONE)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | garbageLeaseID
    }

    def "Append block defaults"() {
        setup:
        def appendResponse = bc.appendBlockWithResponse(defaultInputStream.get(), defaultDataSize, null, null, null,
            null)

        when:
        def downloadStream = new ByteArrayOutputStream()
        bc.download(downloadStream)

        then:
        downloadStream.toByteArray() == defaultData.array()
        validateBasicHeaders(appendResponse.getHeaders())
        appendResponse.getHeaders().getValue("x-ms-content-crc64") != null
        appendResponse.getValue().getBlobAppendOffset() != null
        appendResponse.getValue().getBlobCommittedBlockCount() != null

        expect:
        bc.getProperties().getCommittedBlockCount() == 1
    }

    def "Append block min"() {
        bc.appendBlockWithResponse(defaultInputStream.get(), defaultDataSize, null, null, null,
            null).getStatusCode() == 201
    }

    @Unroll
    def "Append block IA"() {
        when:
        bc.appendBlock(data, dataSize)

        then:
        thrown(exceptionType)

        where:
        data                     | dataSize            | exceptionType
        null                     | defaultDataSize     | NullPointerException
        defaultInputStream.get() | defaultDataSize + 1 | UnexpectedLengthException
        defaultInputStream.get() | defaultDataSize - 1 | UnexpectedLengthException
    }

    def "Append block empty body"() {
        when:
        bc.appendBlock(new ByteArrayInputStream(new byte[0]), 0)

        then:
        thrown(BlobStorageException)
    }

    def "Append block null body"() {
        when:
        bc.appendBlock(new ByteArrayInputStream(null), 0)

        then:
        thrown(NullPointerException)
    }

    def "Append block transactionalMD5"() {
        setup:
        byte[] md5 = MessageDigest.getInstance("MD5").digest(defaultData.array())

        expect:
        bc.appendBlockWithResponse(defaultInputStream.get(), defaultDataSize, md5, null, null, null).statusCode == 201
    }

    def "Append block transactionalMD5 fail"() {
        when:
        bc.appendBlockWithResponse(defaultInputStream.get(), defaultDataSize,
            MessageDigest.getInstance("MD5").digest("garbage".getBytes()), null, null, null)

        then:
        def e = thrown(BlobStorageException)
        e.getErrorCode() == BlobErrorCode.MD5MISMATCH
    }

    @Unroll
    def "Append block AC"() {
        setup:
        match = setupBlobMatchCondition(bc, match)
        leaseID = setupBlobLeaseCondition(bc, leaseID)
        def bac = new AppendBlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setAppendPosition(appendPosE)
            .setMaxSize(maxSizeLTE)


        expect:
        bc.appendBlockWithResponse(defaultInputStream.get(), defaultDataSize, null, bac, null, null)
            .getStatusCode() == 201

        where:
        modified | unmodified | match        | noneMatch   | leaseID         | appendPosE | maxSizeLTE
        null     | null       | null         | null        | null            | null       | null
        oldDate  | null       | null         | null        | null            | null       | null
        null     | newDate    | null         | null        | null            | null       | null
        null     | null       | receivedEtag | null        | null            | null       | null
        null     | null       | null         | garbageEtag | null            | null       | null
        null     | null       | null         | null        | receivedLeaseID | null       | null
        null     | null       | null         | null        | null            | 0          | null
        null     | null       | null         | null        | null            | null       | 100
    }

    @Unroll
    def "Append block AC fail"() {
        setup:
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        setupBlobLeaseCondition(bc, leaseID)

        def bac = new AppendBlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setAppendPosition(appendPosE)
            .setMaxSize(maxSizeLTE)

        when:
        bc.appendBlockWithResponse(defaultInputStream.get(), defaultDataSize, null, bac, null, null)

        then:
        thrown(BlobStorageException)

        cleanup:
        defaultInputStream.get().reset()

        where:
        modified | unmodified | match       | noneMatch    | leaseID        | appendPosE | maxSizeLTE
        newDate  | null       | null        | null         | null           | null       | null
        null     | oldDate    | null        | null         | null           | null       | null
        null     | null       | garbageEtag | null         | null           | null       | null
        null     | null       | null        | receivedEtag | null           | null       | null
        null     | null       | null        | null         | garbageLeaseID | null       | null
        null     | null       | null        | null         | null           | 1          | null
        null     | null       | null        | null         | null           | null       | 1

    }

    def "Append block error"() {
        setup:
        bc = cc.getBlobClient(generateBlobName()).getAppendBlobClient()

        when:
        bc.appendBlock(defaultInputStream.get(), defaultDataSize)

        then:
        thrown(BlobStorageException)
    }

    def "Append block from URL min"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def data = getRandomByteArray(1024)
        bc.appendBlock(new ByteArrayInputStream(data), data.length)

        def destURL = cc.getBlobClient(generateBlobName()).getAppendBlobClient()
        destURL.create()

        def blobRange = new BlobRange(0, (long) PageBlobClient.PAGE_BYTES)

        when:
        def response = destURL.appendBlockFromUrlWithResponse(bc.getBlobUrl(), blobRange, null, null, null, null, null)

        then:
        response.getStatusCode() == 201
        validateBasicHeaders(response.getHeaders())
    }

    def "Append block from URL range"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def data = getRandomByteArray(4 * 1024)
        bc.appendBlock(new ByteArrayInputStream(data), data.length)

        def destURL = cc.getBlobClient(generateBlobName()).getAppendBlobClient()
        destURL.create()

        when:
        destURL.appendBlockFromUrl(bc.getBlobUrl(), new BlobRange(2 * 1024, 1024))

        then:
        def downloadStream = new ByteArrayOutputStream(1024)
        destURL.download(downloadStream)
        downloadStream.toByteArray() == Arrays.copyOfRange(data, 2 * 1024, 3 * 1024)
    }

    def "Append block from URL MD5"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def data = getRandomByteArray(1024)
        bc.appendBlock(new ByteArrayInputStream(data), data.length)

        def destURL = cc.getBlobClient(generateBlobName()).getAppendBlobClient()
        destURL.create()

        when:
        destURL.appendBlockFromUrlWithResponse(bc.getBlobUrl(), null, MessageDigest.getInstance("MD5").digest(data),
            null, null, null, Context.NONE)

        then:
        notThrown(BlobStorageException)
    }

    def "Append block from URL MD5 fail"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def data = getRandomByteArray(1024)
        bc.appendBlock(new ByteArrayInputStream(data), data.length)

        def destURL = cc.getBlobClient(generateBlobName()).getAppendBlobClient()
        destURL.create()

        when:
        destURL.appendBlockFromUrlWithResponse(bc.getBlobUrl(), null, MessageDigest.getInstance("MD5").digest("garbage".getBytes()),
            null, null, null, Context.NONE)

        then:
        thrown(BlobStorageException)
    }

    @Unroll
    def "Append block from URL destination AC"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        match = setupBlobMatchCondition(bc, match)
        leaseID = setupBlobLeaseCondition(bc, leaseID)
        def bac = new AppendBlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setAppendPosition(appendPosE)
            .setMaxSize(maxSizeLTE)

        def sourceURL = cc.getBlobClient(generateBlobName()).getAppendBlobClient()
        sourceURL.create()
        sourceURL.appendBlockWithResponse(defaultInputStream.get(), defaultDataSize, null, null, null, null)
            .getStatusCode()

        expect:
        bc.appendBlockFromUrlWithResponse(sourceURL.getBlobUrl(), null, null, bac, null, null, null).getStatusCode() == 201

        where:
        modified | unmodified | match        | noneMatch   | leaseID         | appendPosE | maxSizeLTE
        null     | null       | null         | null        | null            | null       | null
        oldDate  | null       | null         | null        | null            | null       | null
        null     | newDate    | null         | null        | null            | null       | null
        null     | null       | receivedEtag | null        | null            | null       | null
        null     | null       | null         | garbageEtag | null            | null       | null
        null     | null       | null         | null        | receivedLeaseID | null       | null
        null     | null       | null         | null        | null            | 0          | null
        null     | null       | null         | null        | null            | null       | 100
    }

    @Unroll
    def "Append block from URL AC destination fail"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        setupBlobLeaseCondition(bc, leaseID)

        def bac = new AppendBlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setAppendPosition(appendPosE)
            .setMaxSize(maxSizeLTE)

        def sourceURL = cc.getBlobClient(generateBlobName()).getAppendBlobClient()
        sourceURL.create()
        sourceURL.appendBlockWithResponse(defaultInputStream.get(), defaultDataSize, null, null, null, null)
            .getStatusCode()

        when:
        bc.appendBlockFromUrlWithResponse(sourceURL.getBlobUrl(), null, null, bac, null, null, Context.NONE)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID        | appendPosE | maxSizeLTE
        newDate  | null       | null        | null         | null           | null       | null
        null     | oldDate    | null        | null         | null           | null       | null
        null     | null       | garbageEtag | null         | null           | null       | null
        null     | null       | null        | receivedEtag | null           | null       | null
        null     | null       | null        | null         | garbageLeaseID | null       | null
        null     | null       | null        | null         | null           | 1          | null
        null     | null       | null        | null         | null           | null       | 1
    }

    @Unroll
    def "Append block from URL source AC"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)

        def sourceURL = cc.getBlobClient(generateBlobName()).getAppendBlobClient()
        sourceURL.create()
        sourceURL.appendBlockWithResponse(defaultInputStream.get(), defaultDataSize, null, null, null, null)
            .getStatusCode()

        def smac = new BlobRequestConditions()
            .setIfModifiedSince(sourceIfModifiedSince)
            .setIfUnmodifiedSince(sourceIfUnmodifiedSince)
            .setIfMatch(setupBlobMatchCondition(sourceURL, sourceIfMatch))
            .setIfNoneMatch(sourceIfNoneMatch)

        expect:
        bc.appendBlockFromUrlWithResponse(sourceURL.getBlobUrl(), null, null, null, smac, null, null).getStatusCode() == 201

        where:
        sourceIfModifiedSince | sourceIfUnmodifiedSince | sourceIfMatch | sourceIfNoneMatch
        null                  | null                    | null          | null
        oldDate               | null                    | null          | null
        null                  | newDate                 | null          | null
        null                  | null                    | receivedEtag  | null
        null                  | null                    | null          | garbageEtag
    }

    @Unroll
    def "Append block from URL AC source fail"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)

        def sourceURL = cc.getBlobClient(generateBlobName()).getAppendBlobClient()
        sourceURL.create()
        sourceURL.appendBlockWithResponse(defaultInputStream.get(), defaultDataSize, null, null, null, null)
            .getStatusCode()

        def smac = new BlobRequestConditions()
            .setIfModifiedSince(sourceIfModifiedSince)
            .setIfUnmodifiedSince(sourceIfUnmodifiedSince)
            .setIfMatch(sourceIfMatch)
            .setIfNoneMatch(setupBlobMatchCondition(sourceURL, sourceIfNoneMatch))

        when:
        bc.appendBlockFromUrlWithResponse(sourceURL.getBlobUrl(), null, null, null, smac, null, Context.NONE)

        then:
        thrown(BlobStorageException)

        where:
        sourceIfModifiedSince | sourceIfUnmodifiedSince | sourceIfMatch | sourceIfNoneMatch
        newDate               | null                    | null          | null
        null                  | oldDate                 | null          | null
        null                  | null                    | garbageEtag   | null
        null                  | null                    | null          | receivedEtag
    }

    def "Get Container Name"() {
        expect:
        containerName == bc.getContainerName()
    }

    def "Get Append Blob Name"() {
        expect:
        blobName == bc.getBlobName()
    }

    def "Create overwrite false"() {
        when:
        bc.create()

        then:
        thrown(BlobStorageException)
    }

    def "Create overwrite true"() {
        when:
        bc.create(true)

        then:
        notThrown(Throwable)
    }
}
