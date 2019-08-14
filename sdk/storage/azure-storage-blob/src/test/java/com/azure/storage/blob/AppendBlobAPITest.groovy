// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.core.http.rest.Response
import com.azure.storage.blob.models.*
import spock.lang.Unroll

import java.security.MessageDigest

class AppendBlobAPITest extends APISpec {
    AppendBlobClient bu

    def setup() {
        bu = cu.getAppendBlobClient(generateBlobName())
        bu.create()
    }

    def "Create defaults"() {
        when:
        Response<AppendBlobItem> createResponse = bu.create()

        then:
        createResponse.statusCode() == 201
        validateBasicHeaders(createResponse.headers())
        createResponse.value().contentMD5() == null
        createResponse.value().isServerEncrypted()
    }

    def "Create min"() {
        expect:
        bu.create().statusCode() == 201
    }

    def "Create error"() {
        when:
        bu.create(null, null,
            new BlobAccessConditions().modifiedAccessConditions(new ModifiedAccessConditions().ifMatch("garbage")),
            null)

        then:
        thrown(StorageException)
    }

    @Unroll
    def "Create headers"() {
        setup:
        BlobHTTPHeaders headers = new BlobHTTPHeaders().blobCacheControl(cacheControl)
                .blobContentDisposition(contentDisposition)
                .blobContentEncoding(contentEncoding)
                .blobContentLanguage(contentLanguage)
                .blobContentMD5(contentMD5)
                .blobContentType(contentType)

        when:
        bu.create(headers, null, null, null)
        Response<BlobProperties> response = bu.getProperties()

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType

        then:
        validateBlobProperties(response, cacheControl, contentDisposition, contentEncoding, contentLanguage, contentMD5, contentType)

        where:
        cacheControl | contentDisposition | contentEncoding | contentLanguage | contentMD5                                                                                              | contentType
        null         | null               | null            | null            | null                                                                                                    | null
        "control"    | "disposition"      | "encoding"      | "language"      | Base64.getEncoder().encode(MessageDigest.getInstance("MD5").digest(defaultText.getBytes()))   | "type"
    }

    @Unroll
    def "Create metadata"() {
        setup:
        Metadata metadata = new Metadata()
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }

        when:
        bu.create(null, metadata, null, null)
        Response<BlobProperties> response = bu.getProperties(null, null)

        then:
        response.value().metadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @Unroll
    def "Create AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions().ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))



        expect:
        bu.create(null, null, bac, null).statusCode() == 201

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
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions().ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))

        when:
        bu.create(null, null, bac, null)

        then:
        thrown(StorageException)

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
        Response<AppendBlobItem> appendResponse = bu.appendBlock(defaultInputStream.get(), defaultDataSize)

        expect:
        ByteArrayOutputStream downloadStream = new ByteArrayOutputStream()
        bu.download(downloadStream)

        downloadStream.toByteArray() == defaultData.array()
        validateBasicHeaders(appendResponse.headers())
        appendResponse.value().contentMD5() != null
        appendResponse.value().blobAppendOffset() != null
        appendResponse.value().blobCommittedBlockCount() != null
        Integer.parseInt(bu.getProperties().headers().value("x-ms-blob-committed-block-count")) == 1
    }

    def "Append block min"() {
        expect:
        bu.appendBlock(defaultInputStream.get(), defaultDataSize).statusCode() == 201
    }

    @Unroll
    def "Append block IA"() {
        when:
        bu.appendBlock(data, dataSize)

        then:
        def e = thrown(Exception)
        exceptionType.isInstance(e)

        where:
        data                        | dataSize            | exceptionType
        null                        | defaultDataSize     | NullPointerException
        defaultInputStream.get()    | defaultDataSize + 1 | IndexOutOfBoundsException
        // TODO (alzimmer): This doesn't throw an error as the stream is larger than the stated size
        //defaultInputStream.get()    | defaultDataSize - 1 | StorageException
    }

    def "Append block empty body"() {
        when:
        bu.appendBlock(new ByteArrayInputStream(new byte[0]), 0)

        then:
        thrown(StorageException)
    }

    def "Append block null body"() {
        when:
        bu.appendBlock(new ByteArrayInputStream(null), 0)

        then:
        thrown(NullPointerException)
    }

    @Unroll
    def "Append block AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        AppendBlobAccessConditions bac = new AppendBlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .appendPositionAccessConditions(new AppendPositionAccessConditions()
                .appendPosition(appendPosE)
                .maxSize(maxSizeLTE))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))



        expect:
        bu.appendBlock(defaultInputStream.get(), defaultDataSize, bac, null).statusCode() == 201

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
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        setupBlobLeaseCondition(bu, leaseID)

        AppendBlobAccessConditions bac = new AppendBlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .appendPositionAccessConditions(new AppendPositionAccessConditions()
                .appendPosition(appendPosE)
                .maxSize(maxSizeLTE))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))

        when:
        bu.appendBlock(defaultInputStream.get(), defaultDataSize, bac, null)

        then:
        thrown(StorageException)

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
        bu = cu.getAppendBlobClient(generateBlobName())

        when:
        bu.appendBlock(defaultInputStream.get(), defaultDataSize)

        then:
        thrown(StorageException)
    }

    def "Append block from URL min"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null)
        byte[] data = getRandomByteArray(1024)
        bu.appendBlock(new ByteArrayInputStream(data), data.length)

        AppendBlobClient destURL = cu.getAppendBlobClient(generateBlobName())
        destURL.create()

        BlobRange blobRange = new BlobRange(0, (long) PageBlobClient.PAGE_BYTES)

        when:
        Response<AppendBlobItem> response = destURL.appendBlockFromUrl(bu.getBlobUrl(), blobRange)

        then:
        response.statusCode() == 201
        validateBasicHeaders(response.headers())
    }

    def "Append block from URL range"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null)
        byte[] data = getRandomByteArray(4 * 1024)
        bu.appendBlock(new ByteArrayInputStream(data), data.length)

        AppendBlobClient destURL = cu.getAppendBlobClient(generateBlobName())
        destURL.create()

        when:
        destURL.appendBlockFromUrl(bu.getBlobUrl(), new BlobRange(2 * 1024, 1024))

        then:
        ByteArrayOutputStream downloadStream = new ByteArrayOutputStream(1024)
        destURL.download(downloadStream)
        downloadStream.toByteArray() == Arrays.copyOfRange(data, 2 * 1024, 3 * 1024)
    }

    def "Append block from URL MD5"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null)
        byte[] data = getRandomByteArray(1024)
        bu.appendBlock(new ByteArrayInputStream(data), data.length)

        AppendBlobClient destURL = cu.getAppendBlobClient(generateBlobName())
        destURL.create()

        when:
        destURL.appendBlockFromUrl(bu.getBlobUrl(), null, MessageDigest.getInstance("MD5").digest(data),
                null, null, null)

        then:
        notThrown(StorageException)
    }

    def "Append block from URL MD5 fail"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null)
        byte[] data = getRandomByteArray(1024)
        bu.appendBlock(new ByteArrayInputStream(data), data.length)

        def destURL = cu.getAppendBlobClient(generateBlobName())
        destURL.create()

        when:
        destURL.appendBlockFromUrl(bu.getBlobUrl(), null, MessageDigest.getInstance("MD5").digest("garbage".getBytes()),
                null, null, null)

        then:
        thrown(StorageException)
    }

    @Unroll
    def "Append block from URL destination AC"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null)
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        def bac = new AppendBlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .appendPositionAccessConditions(new AppendPositionAccessConditions()
                .appendPosition(appendPosE)
                .maxSize(maxSizeLTE))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))

        def sourceURL = cu.getAppendBlobClient(generateBlobName())
        sourceURL.create()
        sourceURL.appendBlock(defaultInputStream.get(), defaultDataSize).statusCode()

        expect:
        bu.appendBlockFromUrl(sourceURL.getBlobUrl(), null, null, bac, null, null).statusCode() == 201

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
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null)
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        setupBlobLeaseCondition(bu, leaseID)

        def bac = new AppendBlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .appendPositionAccessConditions(new AppendPositionAccessConditions()
                .appendPosition(appendPosE)
                .maxSize(maxSizeLTE))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))

        def sourceURL = cu.getAppendBlobClient(generateBlobName())
        sourceURL.create()
        sourceURL.appendBlock(defaultInputStream.get(), defaultDataSize).statusCode()

        when:
        bu.appendBlockFromUrl(sourceURL.getBlobUrl(), null, null, bac, null, null)

        then:
        thrown(StorageException)

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
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null)

        def sourceURL = cu.getAppendBlobClient(generateBlobName())
        sourceURL.create()
        sourceURL.appendBlock(defaultInputStream.get(), defaultDataSize).statusCode()

        def smac = new SourceModifiedAccessConditions()
                .sourceIfModifiedSince(sourceIfModifiedSince)
                .sourceIfUnmodifiedSince(sourceIfUnmodifiedSince)
                .sourceIfMatch(setupBlobMatchCondition(sourceURL, sourceIfMatch))
                .sourceIfNoneMatch(sourceIfNoneMatch)

        expect:
        bu.appendBlockFromUrl(sourceURL.getBlobUrl(), null, null, null, smac, null).statusCode() == 201

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
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null, null, null)

        def sourceURL = cu.getAppendBlobClient(generateBlobName())
        sourceURL.create()
        sourceURL.appendBlock(defaultInputStream.get(), defaultDataSize).statusCode()

        def smac = new SourceModifiedAccessConditions()
                .sourceIfModifiedSince(sourceIfModifiedSince)
                .sourceIfUnmodifiedSince(sourceIfUnmodifiedSince)
                .sourceIfMatch(sourceIfMatch)
                .sourceIfNoneMatch(setupBlobMatchCondition(sourceURL, sourceIfNoneMatch))

        when:
        bu.appendBlockFromUrl(sourceURL.getBlobUrl(), null, null, null, smac, null)

        then:
        thrown(StorageException)

        where:
        sourceIfModifiedSince | sourceIfUnmodifiedSince | sourceIfMatch | sourceIfNoneMatch
        newDate               | null                    | null          | null
        null                  | oldDate                 | null          | null
        null                  | null                    | garbageEtag   | null
        null                  | null                    | null          | receivedEtag
    }
}
