// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.core.http.rest.Response
import com.azure.core.exception.UnexpectedLengthException
import com.azure.storage.blob.models.BlobAccessConditions
import com.azure.storage.blob.models.BlobHTTPHeaders
import com.azure.storage.blob.models.BlobRange
import com.azure.storage.blob.models.CopyStatusType
import com.azure.storage.blob.models.LeaseAccessConditions
import com.azure.storage.blob.models.Metadata
import com.azure.storage.blob.models.ModifiedAccessConditions
import com.azure.storage.blob.models.PageBlobAccessConditions
import com.azure.storage.blob.models.PageBlobItem
import com.azure.storage.blob.models.PageRange
import com.azure.storage.blob.models.PublicAccessType
import com.azure.storage.blob.models.SequenceNumberAccessConditions
import com.azure.storage.blob.models.SequenceNumberActionType
import com.azure.storage.blob.models.SourceModifiedAccessConditions
import com.azure.storage.blob.models.StorageException
import spock.lang.Unroll

import java.security.MessageDigest
import java.time.OffsetDateTime

class PageBlobAPITest extends APISpec {
    PageBlobClient bc
    PageBlobAsyncClient bcAsync

    def setup() {
        def name = generateBlobName()
        bc = cc.getPageBlobClient(name)
        bcAsync = ccAsync.getPageBlobAsyncClient(name)
        bc.create(PageBlobClient.PAGE_BYTES)
    }

    def "Create all null"() {
        setup:
        bc = cc.getPageBlobClient(generateBlobName())

        when:
        def response = bc.createWithResponse(PageBlobClient.PAGE_BYTES, null, null, null, null, null, null)

        then:
        response.statusCode() == 201
        validateBasicHeaders(response.headers())
        response.value().contentMD5() == null
        response.value().isServerEncrypted()
    }

    def "Create min"() {
        expect:
        bc.createWithResponse(PageBlobClient.PAGE_BYTES, null, null, null, null, null, null).statusCode() == 201
    }

    def "Create sequence number"() {
        when:
        bc.createWithResponse(PageBlobClient.PAGE_BYTES, 2, null, null, null, null, null)

        then:
        Integer.parseInt(bc.getPropertiesWithResponse(null, null, null).headers().value("x-ms-blob-sequence-number")) == 2
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
        bc.createWithResponse(PageBlobClient.PAGE_BYTES, null, headers, null, null, null, null)

        def response = bc.getPropertiesWithResponse(null, null, null)

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType

        then:
        validateBlobProperties(response, cacheControl, contentDisposition, contentEncoding, contentLanguage, contentMD5, contentType)

        where:
        cacheControl | contentDisposition | contentEncoding | contentLanguage | contentMD5                                                                               | contentType
        null         | null               | null            | null            | null                                                                                     | null
        "control"    | "disposition"      | "encoding"      | "language"      | Base64.getEncoder().encode(MessageDigest.getInstance("MD5").digest(defaultData.array())) | "type"
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
        bc.createWithResponse(PageBlobClient.PAGE_BYTES, null, null, metadata, null, null, null)

        def response = bc.getPropertiesWithResponse(null, null, null)

        then:
        response.statusCode() == 200
        response.value().metadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @Unroll
    def "Create AC"() {
        setup:
        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(setupBlobLeaseCondition(bc, leaseID)))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(setupBlobMatchCondition(bc, match))
                .ifNoneMatch(noneMatch))

        expect:

        bc.createWithResponse(PageBlobClient.PAGE_BYTES, null, null, null, bac, null, null).statusCode() == 201

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
        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(setupBlobLeaseCondition(bc, leaseID)))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(setupBlobMatchCondition(bc, noneMatch)))

        when:
        bc.createWithResponse(PageBlobClient.PAGE_BYTES, null, null, null, bac, null, null)

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

    def "Create error"() {
        when:
        bc.createWithResponse(PageBlobClient.PAGE_BYTES, null, null, null,
            new BlobAccessConditions().leaseAccessConditions(new LeaseAccessConditions().leaseId("id")), null, null)

        then:
        thrown(StorageException)
    }

    def "Upload page"() {
        when:
        def response = bc.uploadPagesWithResponse(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)), null, null, null)

        then:
        response.statusCode() == 201
        validateBasicHeaders(response.headers())
        response.headers().value("x-ms-content-crc64") != null
        response.value().blobSequenceNumber() == 0
        response.value().isServerEncrypted()
    }

    def "Upload page min"() {
        expect:
        bc.uploadPagesWithResponse(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)), null, null, null).statusCode() == 201
    }

    @Unroll
    def "Upload page IA"() {
        when:
        def data = (dataSize == null) ? null : new ByteArrayInputStream(getRandomByteArray(dataSize))
        bc.uploadPages(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES * 2 - 1), data)

        then:
        thrown(exceptionType)

        where:
        dataSize                      | exceptionType
        null                          | NullPointerException
        PageBlobClient.PAGE_BYTES     | UnexpectedLengthException
        PageBlobClient.PAGE_BYTES * 3 | UnexpectedLengthException
    }

    @Unroll
    def "Upload page AC"() {
        setup:
        PageBlobAccessConditions pac = new PageBlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(setupBlobLeaseCondition(bc, leaseID)))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(setupBlobMatchCondition(bc, match))
                .ifNoneMatch(noneMatch))
            .sequenceNumberAccessConditions(new SequenceNumberAccessConditions()
                .ifSequenceNumberLessThan(sequenceNumberLT)
                .ifSequenceNumberLessThanOrEqualTo(sequenceNumberLTE)
                .ifSequenceNumberEqualTo(sequenceNumberEqual))

        expect:
        bc.uploadPagesWithResponse(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)), pac, null, null).statusCode() == 201

        where:
        modified | unmodified | match        | noneMatch   | leaseID         | sequenceNumberLT | sequenceNumberLTE | sequenceNumberEqual
        null     | null       | null         | null        | null            | null             | null              | null
        oldDate  | null       | null         | null        | null            | null             | null              | null
        null     | newDate    | null         | null        | null            | null             | null              | null
        null     | null       | receivedEtag | null        | null            | null             | null              | null
        null     | null       | null         | garbageEtag | null            | null             | null              | null
        null     | null       | null         | null        | receivedLeaseID | null             | null              | null
        null     | null       | null         | null        | null            | 5                | null              | null
        null     | null       | null         | null        | null            | null             | 3                 | null
        null     | null       | null         | null        | null            | null             | null              | 0
    }

    @Unroll
    def "Upload page AC fail"() {
        setup:
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        setupBlobLeaseCondition(bc, leaseID)
        PageBlobAccessConditions pac = new PageBlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))
            .sequenceNumberAccessConditions(new SequenceNumberAccessConditions()
                .ifSequenceNumberLessThan(sequenceNumberLT)
                .ifSequenceNumberLessThanOrEqualTo(sequenceNumberLTE)
                .ifSequenceNumberEqualTo(sequenceNumberEqual))

        when:
        bc.uploadPagesWithResponse(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)), pac, null, null)

        then:
        thrown(StorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID        | sequenceNumberLT | sequenceNumberLTE | sequenceNumberEqual
        newDate  | null       | null        | null         | null           | null             | null              | null
        null     | oldDate    | null        | null         | null           | null             | null              | null
        null     | null       | garbageEtag | null         | null           | null             | null              | null
        null     | null       | null        | receivedEtag | null           | null             | null              | null
        null     | null       | null        | null         | garbageLeaseID | null             | null              | null
        null     | null       | null        | null         | null           | -1               | null              | null
        null     | null       | null        | null         | null           | null             | -1                | null
        null     | null       | null        | null         | null           | null             | null              | 100
    }

    def "Upload page error"() {
        setup:
        bc = cc.getPageBlobClient(generateBlobName())

        when:
        bc.uploadPagesWithResponse(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)),
            new PageBlobAccessConditions().leaseAccessConditions(new LeaseAccessConditions().leaseId("id")),
            null, null)

        then:
        thrown(StorageException)
    }

    def "Upload page from URL min"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def destURL = cc.getPageBlobClient(generateBlobName())
        destURL.create(PageBlobClient.PAGE_BYTES)
        destURL.uploadPages(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)))
        def pageRange = new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1)

        when:
        Response<PageBlobItem> response = bc.uploadPagesFromURLWithResponse(pageRange, destURL.getBlobUrl(), null, null, null, null, null, null)

        then:
        response.statusCode() == 201
        validateBasicHeaders(response.headers())
    }

    def "Upload page from URL range"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)

        byte[] data = getRandomByteArray(PageBlobClient.PAGE_BYTES * 4)

        def sourceURL = cc.getPageBlobClient(generateBlobName())
        sourceURL.create(PageBlobClient.PAGE_BYTES * 4)
        sourceURL.uploadPages(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES * 4 - 1),
            new ByteArrayInputStream(data))

        def destURL = cc.getPageBlobClient(generateBlobName())
        destURL.create(PageBlobClient.PAGE_BYTES * 2)

        when:
        destURL.uploadPagesFromURL(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES * 2 - 1),
            sourceURL.getBlobUrl(), PageBlobClient.PAGE_BYTES * 2)

        then:
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        destURL.download(outputStream)
        outputStream.toByteArray() == Arrays.copyOfRange(data, PageBlobClient.PAGE_BYTES * 2, PageBlobClient.PAGE_BYTES * 4)
    }

    def "Upload page from URL IA"() {
        when:
        bc.uploadPagesFromURL(null, bc.getBlobUrl(), (Long) PageBlobClient.PAGE_BYTES)

        then:
        thrown(IllegalArgumentException)
    }

    def "Upload page from URL MD5"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def destURL = cc.getPageBlobClient(generateBlobName())
        destURL.create(PageBlobClient.PAGE_BYTES)
        def data = getRandomByteArray(PageBlobClient.PAGE_BYTES)
        def pageRange = new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1)
        bc.uploadPages(pageRange, new ByteArrayInputStream(data))

        when:
        destURL.uploadPagesFromURLWithResponse(pageRange, bc.getBlobUrl(), null, MessageDigest.getInstance("MD5").digest(data),
            null, null, null, null)

        then:
        notThrown(StorageException)
    }

    def "Upload page from URL MD5 fail"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def destURL = cc.getPageBlobClient(generateBlobName())
        destURL.create(PageBlobClient.PAGE_BYTES)
        def pageRange = new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1)
        bc.uploadPages(pageRange, new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)))

        when:
        destURL.uploadPagesFromURLWithResponse(pageRange, bc.getBlobUrl(), null,
            MessageDigest.getInstance("MD5").digest("garbage".getBytes()), null, null, null, null)

        then:
        thrown(StorageException)
    }

    @Unroll
    def "Upload page from URL destination AC"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def sourceURL = cc.getPageBlobClient(generateBlobName())
        sourceURL.create(PageBlobClient.PAGE_BYTES)
        def pageRange = new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1)
        sourceURL.uploadPages(pageRange, new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)))

        def pac = new PageBlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(setupBlobLeaseCondition(bc, leaseID)))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(setupBlobMatchCondition(bc, match))
                .ifNoneMatch(noneMatch))
            .sequenceNumberAccessConditions(new SequenceNumberAccessConditions()
                .ifSequenceNumberLessThan(sequenceNumberLT)
                .ifSequenceNumberLessThanOrEqualTo(sequenceNumberLTE)
                .ifSequenceNumberEqualTo(sequenceNumberEqual))

        expect:
        bc.uploadPagesFromURLWithResponse(pageRange, sourceURL.getBlobUrl(), null, null, pac, null, null, null).statusCode() == 201

        where:
        modified | unmodified | match        | noneMatch   | leaseID         | sequenceNumberLT | sequenceNumberLTE | sequenceNumberEqual
        null     | null       | null         | null        | null            | null             | null              | null
        oldDate  | null       | null         | null        | null            | null             | null              | null
        null     | newDate    | null         | null        | null            | null             | null              | null
        null     | null       | receivedEtag | null        | null            | null             | null              | null
        null     | null       | null         | garbageEtag | null            | null             | null              | null
        null     | null       | null         | null        | receivedLeaseID | null             | null              | null
        null     | null       | null         | null        | null            | 5                | null              | null
        null     | null       | null         | null        | null            | null             | 3                 | null
        null     | null       | null         | null        | null            | null             | null              | 0
    }

    @Unroll
    def "Upload page from URL destination AC fail"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)

        def sourceURL = cc.getPageBlobClient(generateBlobName())
        sourceURL.create(PageBlobClient.PAGE_BYTES)
        def pageRange = new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1)
        sourceURL.uploadPages(pageRange, new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)))

        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        def pac = new PageBlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))
            .sequenceNumberAccessConditions(new SequenceNumberAccessConditions()
                .ifSequenceNumberLessThan(sequenceNumberLT)
                .ifSequenceNumberLessThanOrEqualTo(sequenceNumberLTE)
                .ifSequenceNumberEqualTo(sequenceNumberEqual))

        when:
        bc.uploadPagesFromURLWithResponse(pageRange, sourceURL.getBlobUrl(), null, null, pac, null, null, null)

        then:
        thrown(StorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID        | sequenceNumberLT | sequenceNumberLTE | sequenceNumberEqual
        newDate  | null       | null        | null         | null           | null             | null              | null
        null     | oldDate    | null        | null         | null           | null             | null              | null
        null     | null       | garbageEtag | null         | null           | null             | null              | null
        null     | null       | null        | receivedEtag | null           | null             | null              | null
        null     | null       | null        | null         | garbageLeaseID | null             | null              | null
        null     | null       | null        | null         | null           | -1               | null              | null
        null     | null       | null        | null         | null           | null             | -1                | null
        null     | null       | null        | null         | null           | null             | null              | 100
    }

    @Unroll
    def "Upload page from URL source AC"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def sourceURL = cc.getPageBlobClient(generateBlobName())
        sourceURL.create(PageBlobClient.PAGE_BYTES)
        def pageRange = new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1)
        sourceURL.uploadPages(pageRange, new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)))

        sourceIfMatch = setupBlobMatchCondition(sourceURL, sourceIfMatch)
        def smac = new SourceModifiedAccessConditions()
            .sourceIfModifiedSince(sourceIfModifiedSince)
            .sourceIfUnmodifiedSince(sourceIfUnmodifiedSince)
            .sourceIfMatch(sourceIfMatch)
            .sourceIfNoneMatch(sourceIfNoneMatch)

        expect:
        bc.uploadPagesFromURLWithResponse(pageRange, sourceURL.getBlobUrl(), null, null, null, smac, null, null).statusCode() == 201

        where:
        sourceIfModifiedSince | sourceIfUnmodifiedSince | sourceIfMatch | sourceIfNoneMatch
        null                  | null                    | null          | null
        oldDate               | null                    | null          | null
        null                  | newDate                 | null          | null
        null                  | null                    | receivedEtag  | null
        null                  | null                    | null          | garbageEtag
    }

    @Unroll
    def "Upload page from URL source AC fail"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def sourceURL = cc.getPageBlobClient(generateBlobName())
        sourceURL.create(PageBlobClient.PAGE_BYTES)
        def pageRange = new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1)
        sourceURL.uploadPages(pageRange, new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)))

        def smac = new SourceModifiedAccessConditions()
            .sourceIfModifiedSince(sourceIfModifiedSince)
            .sourceIfUnmodifiedSince(sourceIfUnmodifiedSince)
            .sourceIfMatch(sourceIfMatch)
            .sourceIfNoneMatch(setupBlobMatchCondition(sourceURL, sourceIfNoneMatch))

        when:
        bc.uploadPagesFromURLWithResponse(pageRange, sourceURL.getBlobUrl(), null, null, null, smac, null, null)
        then:
        thrown(StorageException)

        where:
        sourceIfModifiedSince | sourceIfUnmodifiedSince | sourceIfMatch | sourceIfNoneMatch
        newDate               | null                    | null          | null
        null                  | oldDate                 | null          | null
        null                  | null                    | garbageEtag   | null
        null                  | null                    | null          | receivedEtag
    }

    def "Clear page"() {
        setup:
        bc.uploadPagesWithResponse(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)), null, null, null)

        when:
        Response<PageBlobItem> response = bc.clearPagesWithResponse(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1), null, null, null)

        then:
        bc.getPageRanges(new BlobRange(0)).pageRange().size() == 0
        validateBasicHeaders(response.headers())
        response.value().contentMD5() == null
        response.value().blobSequenceNumber() == 0
    }

    def "Clear page min"() {
        expect:
        bc.clearPages(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1))
    }

    @Unroll
    def "Clear pages AC"() {
        setup:
        bc.uploadPages(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)))
        match = setupBlobMatchCondition(bc, match)
        leaseID = setupBlobLeaseCondition(bc, leaseID)
        def pac = new PageBlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))
            .sequenceNumberAccessConditions(new SequenceNumberAccessConditions()
                .ifSequenceNumberLessThan(sequenceNumberLT)
                .ifSequenceNumberLessThanOrEqualTo(sequenceNumberLTE)
                .ifSequenceNumberEqualTo(sequenceNumberEqual))

        expect:
        bc.clearPagesWithResponse(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1), pac, null, null)
            .statusCode() == 201

        where:
        modified | unmodified | match        | noneMatch   | leaseID         | sequenceNumberLT | sequenceNumberLTE | sequenceNumberEqual
        null     | null       | null         | null        | null            | null             | null              | null
        oldDate  | null       | null         | null        | null            | null             | null              | null
        null     | newDate    | null         | null        | null            | null             | null              | null
        null     | null       | receivedEtag | null        | null            | null             | null              | null
        null     | null       | null         | garbageEtag | null            | null             | null              | null
        null     | null       | null         | null        | receivedLeaseID | null             | null              | null
        null     | null       | null         | null        | null            | 5                | null              | null
        null     | null       | null         | null        | null            | null             | 3                 | null
        null     | null       | null         | null        | null            | null             | null              | 0
    }

    @Unroll
    def "Clear pages AC fail"() {
        setup:
        bc.uploadPages(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)))
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        setupBlobLeaseCondition(bc, leaseID)
        def pac = new PageBlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))
            .sequenceNumberAccessConditions(new SequenceNumberAccessConditions()
                .ifSequenceNumberLessThan(sequenceNumberLT)
                .ifSequenceNumberLessThanOrEqualTo(sequenceNumberLTE)
                .ifSequenceNumberEqualTo(sequenceNumberEqual))


        when:
        bc.clearPagesWithResponse(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1), pac, null, null)

        then:
        thrown(StorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID        | sequenceNumberLT | sequenceNumberLTE | sequenceNumberEqual
        newDate  | null       | null        | null         | null           | null             | null              | null
        null     | oldDate    | null        | null         | null           | null             | null              | null
        null     | null       | garbageEtag | null         | null           | null             | null              | null
        null     | null       | null        | receivedEtag | null           | null             | null              | null
        null     | null       | null        | null         | garbageLeaseID | null             | null              | null
        null     | null       | null        | null         | null           | -1               | null              | null
        null     | null       | null        | null         | null           | null             | -1                | null
        null     | null       | null        | null         | null           | null             | null              | 100
    }

    def "Clear page error"() {
        setup:
        bc = cc.getPageBlobClient(generateBlobName())

        when:
        bc.clearPages(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1))

        then:
        thrown(StorageException)
    }

    def "Get page ranges"() {
        setup:
        bc.uploadPages(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)))

        when:
        def response = bc.getPageRangesWithResponse(new BlobRange(0, PageBlobClient.PAGE_BYTES), null, null, null)

        then:
        response.statusCode() == 200
        response.value().pageRange().size() == 1
        validateBasicHeaders(response.headers())
        Long.parseLong(response.headers().get("x-ms-blob-content-length").value()) == (long) PageBlobClient.PAGE_BYTES
    }

    def "Get page ranges min"() {
        when:
        bc.getPageRanges(null)

        then:
        notThrown(StorageException)
    }

    @Unroll
    def "Get page ranges AC"() {
        setup:
        match = setupBlobMatchCondition(bc, match)
        leaseID = setupBlobLeaseCondition(bc, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))


        when:
        bc.getPageRangesWithResponse(new BlobRange(0, PageBlobClient.PAGE_BYTES), bac, null, null)

        then:
        notThrown(StorageException)

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
    def "Get page ranges AC fail"() {
        setup:
        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(setupBlobLeaseCondition(bc, leaseID)))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(setupBlobMatchCondition(bc, noneMatch)))

        when:
        bc.getPageRangesWithResponse(new BlobRange(0, PageBlobClient.PAGE_BYTES), bac, null, null)

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

    def "Get page ranges error"() {
        setup:
        bc = cc.getPageBlobClient(generateBlobName())

        when:
        bc.getPageRanges(null)

        then:
        thrown(StorageException)
    }

    def "Get page ranges diff"() {
        setup:
        bc.create(PageBlobClient.PAGE_BYTES * 2)

        bc.uploadPages(new PageRange().start(PageBlobClient.PAGE_BYTES).end(PageBlobClient.PAGE_BYTES * 2 - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)))

        def snapId = bc.createSnapshot().getSnapshotId()

        bc.uploadPages(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)))

        bc.clearPages(new PageRange().start(PageBlobClient.PAGE_BYTES).end(PageBlobClient.PAGE_BYTES * 2 - 1))

        when:
        def response = bc.getPageRangesDiffWithResponse(new BlobRange(0, PageBlobClient.PAGE_BYTES * 2), snapId, null, null, null)

        then:
        response.value().pageRange().size() == 1
        response.value().pageRange().get(0).start() == 0
        response.value().pageRange().get(0).end() == PageBlobClient.PAGE_BYTES - 1
        response.value().clearRange().size() == 1
        response.value().clearRange().get(0).start() == PageBlobClient.PAGE_BYTES
        response.value().clearRange().get(0).end() == PageBlobClient.PAGE_BYTES * 2 - 1
        validateBasicHeaders(response.headers())
        Integer.parseInt(response.headers().value("x-ms-blob-content-length")) == PageBlobClient.PAGE_BYTES * 2
    }

    def "Get page ranges diff min"() {
        setup:
        def snapId = bc.createSnapshot().getSnapshotId()

        when:
        bc.getPageRangesDiff(null, snapId).iterator().hasNext()

        then:
        notThrown(StorageException)
    }

    @Unroll
    def "Get page ranges diff AC"() {
        setup:
        def snapId = bc.createSnapshot().getSnapshotId()
        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(setupBlobLeaseCondition(bc, leaseID)))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(setupBlobMatchCondition(bc, match))
                .ifNoneMatch(noneMatch))

        when:
        bc.getPageRangesDiffWithResponse(new BlobRange(0, PageBlobClient.PAGE_BYTES), snapId, bac, null, null)

        then:
        notThrown(StorageException)

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
    def "Get page ranges diff AC fail"() {
        setup:
        def snapId = bc.createSnapshot().getSnapshotId()

        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(setupBlobLeaseCondition(bc, leaseID)))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(setupBlobMatchCondition(bc, noneMatch)))

        when:
        bc.getPageRangesDiffWithResponse(new BlobRange(0, PageBlobClient.PAGE_BYTES), snapId, bac, null, null)

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

    def "Get page ranges diff error"() {
        setup:
        bc = cc.getPageBlobClient(generateBlobName())

        when:
        bc.getPageRangesDiff(null, "snapshot")

        then:
        thrown(StorageException)
    }

    @Unroll
    def "PageRange IA"() {
        setup:
        def range = new PageRange().start(start).end(end)

        when:
        bc.clearPages(range)

        then:
        thrown(IllegalArgumentException)

        where:
        start                         | end
        1                             | 1
        -PageBlobClient.PAGE_BYTES    | PageBlobClient.PAGE_BYTES - 1
        0                             | 0
        1                             | PageBlobClient.PAGE_BYTES - 1
        0                             | PageBlobClient.PAGE_BYTES
        PageBlobClient.PAGE_BYTES * 2 | PageBlobClient.PAGE_BYTES - 1
    }

    def "Resize"() {
        setup:
        def response = bc.resizeWithResponse(PageBlobClient.PAGE_BYTES * 2, null, null, null)

        expect:
        Integer.parseInt(bc.getPropertiesWithResponse(null, null, null).headers().value("Content-Length")) == PageBlobClient.PAGE_BYTES * 2
        validateBasicHeaders(response.headers())
        response.value().blobSequenceNumber() != null
    }

    def "Resize min"() {
        expect:
        bc.resizeWithResponse(PageBlobClient.PAGE_BYTES, null, null, null).statusCode() == 200
    }

    @Unroll
    def "Resize AC"() {
        setup:
        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(setupBlobLeaseCondition(bc, leaseID)))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(setupBlobMatchCondition(bc, match))
                .ifNoneMatch(noneMatch))

        expect:
        bc.resizeWithResponse(PageBlobClient.PAGE_BYTES * 2, bac, null, null).statusCode() == 200

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
    def "Resize AC fail"() {
        setup:
        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(setupBlobLeaseCondition(bc, leaseID)))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(setupBlobMatchCondition(bc, noneMatch)))

        when:
        bc.resizeWithResponse(PageBlobClient.PAGE_BYTES * 2, bac, null, null)

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

    def "Resize error"() {
        setup:
        bc = cc.getPageBlobClient(generateBlobName())

        when:
        bc.resize(0)

        then:
        thrown(StorageException)
    }

    @Unroll
    def "Sequence number"() {
        setup:
        Response<PageBlobItem> response = bc.updateSequenceNumberWithResponse(action, number, null, null, null)

        expect:
        Integer.parseInt(bc.getPropertiesWithResponse(null, null, null).headers().value("x-ms-blob-sequence-number")) == result
        validateBasicHeaders(response.headers())
        response.value().blobSequenceNumber() == result

        where:
        action                             | number || result
        SequenceNumberActionType.UPDATE    | 5      || 5
        SequenceNumberActionType.INCREMENT | null   || 1
        SequenceNumberActionType.MAX       | 2      || 2
    }

    def "Sequence number min"() {
        expect:
        bc.updateSequenceNumberWithResponse(SequenceNumberActionType.INCREMENT, null, null, null, null).statusCode() == 200
    }

    @Unroll
    def "Sequence number AC"() {
        setup:
        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(setupBlobLeaseCondition(bc, leaseID)))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(setupBlobMatchCondition(bc, match))
                .ifNoneMatch(noneMatch))

        expect:
        bc.updateSequenceNumberWithResponse(SequenceNumberActionType.UPDATE, 1, bac, null, null)
            .statusCode() == 200

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
    def "Sequence number AC fail"() {
        setup:
        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(setupBlobLeaseCondition(bc, leaseID)))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(setupBlobMatchCondition(bc, noneMatch)))

        when:
        bc.updateSequenceNumberWithResponse(SequenceNumberActionType.UPDATE, 1, bac, null, null)

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

    def "Sequence number error"() {
        setup:
        bc = cc.getPageBlobClient(generateBlobName())

        when:
        bc.updateSequenceNumber(SequenceNumberActionType.UPDATE, 0)

        then:
        thrown(StorageException)
    }

    def "Start incremental copy"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.BLOB, null)
        def bc2 = cc.getPageBlobClient(generateBlobName())
        def snapId = bc.createSnapshot().getSnapshotId()

        def copyResponse = bc2.copyIncrementalWithResponse(bc.getBlobUrl(), snapId, null, null, null)
        def status = copyResponse.value().toString()

        OffsetDateTime start = OffsetDateTime.now()
        while (status != CopyStatusType.SUCCESS.toString()) {
            status = bc2.getPropertiesWithResponse(null, null, null).headers().value("x-ms-copy-status")
            OffsetDateTime currentTime = OffsetDateTime.now()
            if (status == CopyStatusType.FAILED.toString() || currentTime.minusMinutes(1) == start) {
                throw new Exception("Copy failed or took too long")
            }
            sleepIfRecord(1000)
        }

        expect:
        Response<BlobProperties> propertiesResponse = bc2.getPropertiesWithResponse(null, null, null)
        Boolean.parseBoolean(propertiesResponse.headers().value("x-ms-incremental-copy"))
        propertiesResponse.headers().value("x-ms-copy-destination-snapshot") != null
        validateBasicHeaders(copyResponse.headers())
        copyResponse.headers().value("x-ms-copy-id") != null
        copyResponse.value() != null
    }

    def "Start incremental copy min"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.BLOB, null)
        def bc2 = cc.getPageBlobClient(generateBlobName())
        String snapshot = bc.createSnapshot().getSnapshotId()

        expect:
        bc2.copyIncrementalWithResponse(bc.getBlobUrl(), snapshot, null, null, null).statusCode() == 202
    }

    @Unroll
    def "Start incremental copy AC"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.BLOB, null)
        PageBlobClient bu2 = cc.getPageBlobClient(generateBlobName())
        String snapshot = bc.createSnapshot().getSnapshotId()

        def copyResponse = bu2.copyIncrementalWithResponse(bc.getBlobUrl(), snapshot, null, null, null)
        def status = copyResponse.value().toString()

        OffsetDateTime start = OffsetDateTime.now()
        while (status != CopyStatusType.SUCCESS.toString()) {
            status = bu2.getPropertiesWithResponse(null, null, null).headers().value("x-ms-copy-status")
            OffsetDateTime currentTime = OffsetDateTime.now()
            if (status == CopyStatusType.FAILED.toString() || currentTime.minusMinutes(1) == start) {
                throw new Exception("Copy failed or took too long")
            }
            sleepIfRecord(1000)
        }

        snapshot = bc.createSnapshot().getSnapshotId()
        match = setupBlobMatchCondition(bu2, match)
        def mac = new ModifiedAccessConditions()
            .ifModifiedSince(modified)
            .ifUnmodifiedSince(unmodified)
            .ifMatch(match)
            .ifNoneMatch(noneMatch)

        expect:
        bu2.copyIncrementalWithResponse(bc.getBlobUrl(), snapshot, mac, null, null).statusCode() == 202

        where:
        modified | unmodified | match        | noneMatch
        null     | null       | null         | null
        oldDate  | null       | null         | null
        null     | newDate    | null         | null
        null     | null       | receivedEtag | null
        null     | null       | null         | garbageEtag
    }

    @Unroll
    def "Start incremental copy AC fail"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.BLOB, null)
        PageBlobClient bu2 = cc.getPageBlobClient(generateBlobName())
        String snapshot = bc.createSnapshot().getSnapshotId()
        bu2.copyIncremental(bc.getBlobUrl(), snapshot)
        snapshot = bc.createSnapshot().getSnapshotId()
        noneMatch = setupBlobMatchCondition(bu2, noneMatch)
        def mac = new ModifiedAccessConditions()
            .ifModifiedSince(modified)
            .ifUnmodifiedSince(unmodified)
            .ifMatch(match)
            .ifNoneMatch(noneMatch)

        when:
        bu2.copyIncrementalWithResponse(bc.getBlobUrl(), snapshot, mac, null, null)

        then:
        thrown(StorageException)

        where:
        modified | unmodified | match       | noneMatch
        newDate  | null       | null        | null
        null     | oldDate    | null        | null
        null     | null       | garbageEtag | null
        null     | null       | null        | receivedEtag
    }

    def "Start incremental copy error"() {
        setup:
        bc = cc.getPageBlobClient(generateBlobName())

        when:
        bc.copyIncremental(new URL("https://www.error.com"), "snapshot")

        then:
        thrown(StorageException)
    }
}
