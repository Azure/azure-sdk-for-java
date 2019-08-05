// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.core.http.rest.Response
import com.azure.storage.blob.models.*
import spock.lang.Unroll

import java.security.MessageDigest
import java.time.OffsetDateTime

class PageBlobAPITest extends APISpec {
    PageBlobClient bu

    def setup() {
        bu = cu.getPageBlobClient(generateBlobName())
        bu.create(PageBlobClient.PAGE_BYTES)
    }

    def "Create all null"() {
        setup:
        bu = cu.getPageBlobClient(generateBlobName())

        when:
        Response<PageBlobItem> response = bu.create(PageBlobClient.PAGE_BYTES)

        then:
        response.statusCode() == 201
        validateBasicHeaders(response.headers())
        response.value().contentMD5() == null
        response.value().isServerEncrypted()
    }

    def "Create min"() {
        expect:
        bu.create(PageBlobClient.PAGE_BYTES).statusCode() == 201
    }

    def "Create sequence number"() {
        when:
        bu.create(PageBlobClient.PAGE_BYTES, 2, null, null,
            null, null)

        then:
        Integer.parseInt(bu.getProperties().headers().value("x-ms-blob-sequence-number")) == 2
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
        bu.create(PageBlobClient.PAGE_BYTES, null, headers, null, null, null)

        Response<BlobProperties> response = bu.getProperties(null, null)

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
        bu.create(PageBlobClient.PAGE_BYTES, null, null, metadata, null, null)

        Response<BlobProperties> response = bu.getProperties(null, null)

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
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(setupBlobLeaseCondition(bu, leaseID)))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(setupBlobMatchCondition(bu, match))
                .ifNoneMatch(noneMatch))

        expect:
        bu.create(PageBlobClient.PAGE_BYTES, null, null, null, bac, null)
            .statusCode() == 201

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
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(setupBlobLeaseCondition(bu, leaseID)))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(setupBlobMatchCondition(bu, noneMatch)))

        when:
        bu.create(PageBlobClient.PAGE_BYTES, null, null, null, bac, null)

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
        bu.create(PageBlobClient.PAGE_BYTES, null, null, null,
            new BlobAccessConditions().leaseAccessConditions(new LeaseAccessConditions().leaseId("id")), null)

        then:
        thrown(StorageException)
    }

    def "Upload page"() {
        when:
        Response<PageBlobItem> response = bu.uploadPages(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)))

        then:
        response.statusCode() == 201
        validateBasicHeaders(response.headers())
        response.value().contentMD5() != null
        response.value().blobSequenceNumber() == 0
        response.value().isServerEncrypted()
    }

    def "Upload page min"() {
        expect:
        bu.uploadPages(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES))).statusCode() == 201
    }

    @Unroll
    def "Upload page IA"() {
        when:
        bu.uploadPages(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES * 2 - 1), data)

        then:
        def e = thrown(Exception)
        exceptionType.isInstance(e)

        where:
        data                                                                        | exceptionType
        null                                                                        | NullPointerException
        new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES))     | IndexOutOfBoundsException
        new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES * 3)) | StorageException
    }

    @Unroll
    def "Upload page AC"() {
        setup:
        PageBlobAccessConditions pac = new PageBlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(setupBlobLeaseCondition(bu, leaseID)))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(setupBlobMatchCondition(bu, match))
                .ifNoneMatch(noneMatch))
            .sequenceNumberAccessConditions(new SequenceNumberAccessConditions()
                .ifSequenceNumberLessThan(sequenceNumberLT)
                .ifSequenceNumberLessThanOrEqualTo(sequenceNumberLTE)
                .ifSequenceNumberEqualTo(sequenceNumberEqual))

        expect:
        bu.uploadPages(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)), pac, null).statusCode() == 201

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
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        setupBlobLeaseCondition(bu, leaseID)
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
        bu.uploadPages(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)), pac, null)

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
        bu = cu.getPageBlobClient(generateBlobName())

        when:
        bu.uploadPages(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)),
            new PageBlobAccessConditions().leaseAccessConditions(new LeaseAccessConditions().leaseId("id")),
            null)

        then:
        thrown(StorageException)
    }

    def "Upload page from URL min"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def destURL = cu.getPageBlobClient(generateBlobName())
        destURL.create(PageBlobClient.PAGE_BYTES)
        destURL.uploadPages(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)))
        def pageRange = new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1)

        when:
        Response<PageBlobItem> response = bu.uploadPagesFromURL(pageRange, destURL.getBlobUrl(), null)

        then:
        response.statusCode() == 201
        validateBasicHeaders(response.headers())
    }

    def "Upload page from URL range"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null, null, null)

        byte[] data = getRandomByteArray(PageBlobClient.PAGE_BYTES * 4)

        def sourceURL = cu.getPageBlobClient(generateBlobName())
        sourceURL.create(PageBlobClient.PAGE_BYTES * 4)
        sourceURL.uploadPages(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES * 4 - 1),
            new ByteArrayInputStream(data))

        def destURL = cu.getPageBlobClient(generateBlobName())
        destURL.create(PageBlobClient.PAGE_BYTES * 2)

        when:
        destURL.uploadPagesFromURL(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES * 2 - 1),
            sourceURL.getBlobUrl(), PageBlobClient.PAGE_BYTES * 2)

        then:
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        destURL.download(outputStream)
        outputStream.toByteArray() == Arrays.copyOfRange(data, PageBlobClient.PAGE_BYTES * 2, PageBlobClient.PAGE_BYTES * 4)
    }

    @Unroll
    def "Upload page from URL IA"() {
        when:
        bu.uploadPagesFromURL(range, bu.getBlobUrl(), sourceOffset)

        then:
        thrown(IllegalArgumentException)

        where:
        sourceOffset                     | range
        (Long) PageBlobClient.PAGE_BYTES | null
    }

    def "Upload page from URL MD5"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null, null, null)
        def destURL = cu.getPageBlobClient(generateBlobName())
        destURL.create(PageBlobClient.PAGE_BYTES)
        def data = getRandomByteArray(PageBlobClient.PAGE_BYTES)
        def pageRange = new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1)
        bu.uploadPages(pageRange, new ByteArrayInputStream(data))

        when:
        destURL.uploadPagesFromURL(pageRange, bu.getBlobUrl(), null, MessageDigest.getInstance("MD5").digest(data),
            null, null, null)

        then:
        notThrown(StorageException)
    }

    def "Upload page from URL MD5 fail"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null, null, null)
        def destURL = cu.getPageBlobClient(generateBlobName())
        destURL.create(PageBlobClient.PAGE_BYTES)
        def pageRange = new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1)
        bu.uploadPages(pageRange, new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)))

        when:
        destURL.uploadPagesFromURL(pageRange, bu.getBlobUrl(), null,
            MessageDigest.getInstance("MD5").digest("garbage".getBytes()), null, null, null)

        then:
        thrown(StorageException)
    }

    @Unroll
    def "Upload page from URL destination AC"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null, null, null)
        def sourceURL = cu.getPageBlobClient(generateBlobName())
        sourceURL.create(PageBlobClient.PAGE_BYTES)
        def pageRange = new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1)
        sourceURL.uploadPages(pageRange, new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)))

        def pac = new PageBlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(setupBlobLeaseCondition(bu, leaseID)))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(setupBlobMatchCondition(bu, match))
                .ifNoneMatch(noneMatch))
            .sequenceNumberAccessConditions(new SequenceNumberAccessConditions()
                .ifSequenceNumberLessThan(sequenceNumberLT)
                .ifSequenceNumberLessThanOrEqualTo(sequenceNumberLTE)
                .ifSequenceNumberEqualTo(sequenceNumberEqual))

        expect:
        bu.uploadPagesFromURL(pageRange, sourceURL.getBlobUrl(), null, null, pac, null, null).statusCode() == 201

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
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null, null, null)

        def sourceURL = cu.getPageBlobClient(generateBlobName())
        sourceURL.create(PageBlobClient.PAGE_BYTES)
        def pageRange = new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1)
        sourceURL.uploadPages(pageRange, new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)))

        noneMatch = setupBlobMatchCondition(bu, noneMatch)
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
        bu.uploadPagesFromURL(pageRange, sourceURL.getBlobUrl(), null, null, pac, null, null)

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
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null, null, null)
        def sourceURL = cu.getPageBlobClient(generateBlobName())
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
        bu.uploadPagesFromURL(pageRange, sourceURL.getBlobUrl(), null, null, null, smac, null).statusCode() == 201

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
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null, null, null)
        def sourceURL = cu.getPageBlobClient(generateBlobName())
        sourceURL.create(PageBlobClient.PAGE_BYTES)
        def pageRange = new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1)
        sourceURL.uploadPages(pageRange, new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)))

        def smac = new SourceModifiedAccessConditions()
            .sourceIfModifiedSince(sourceIfModifiedSince)
            .sourceIfUnmodifiedSince(sourceIfUnmodifiedSince)
            .sourceIfMatch(sourceIfMatch)
            .sourceIfNoneMatch(setupBlobMatchCondition(sourceURL, sourceIfNoneMatch))

        when:
        bu.uploadPagesFromURL(pageRange, sourceURL.getBlobUrl(), null, null, null, smac, null)

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
        bu.uploadPages(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)), null, null)

        when:
        Response<PageBlobItem> response = bu.clearPages(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1))

        then:
        bu.getPageRanges(new BlobRange(0)).value().pageRange().size() == 0
        validateBasicHeaders(response.headers())
        response.value().contentMD5() == null
        response.value().blobSequenceNumber() == 0
    }

    def "Clear page min"() {
        expect:
        bu.clearPages(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1))
    }

    @Unroll
    def "Clear pages AC"() {
        setup:
        bu.uploadPages(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)))
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
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
        bu.clearPages(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1), pac, null)
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
        bu.uploadPages(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)))
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        setupBlobLeaseCondition(bu, leaseID)
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
        bu.clearPages(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1), pac, null)

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
        bu = cu.getPageBlobClient(generateBlobName())

        when:
        bu.clearPages(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1))

        then:
        thrown(StorageException)
    }

    def "Get page ranges"() {
        setup:
        bu.uploadPages(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)))

        when:
        Response<PageList> response = bu.getPageRanges(new BlobRange(0, PageBlobClient.PAGE_BYTES))

        then:
        response.statusCode() == 200
        response.value().pageRange().size() == 1
        validateBasicHeaders(response.headers())
        Integer.parseInt(response.headers().value("x-ms-blob-content-length")) == PageBlobClient.PAGE_BYTES
    }

    def "Get page ranges min"() {
        when:
        bu.getPageRanges(null)

        then:
        notThrown(StorageException)
    }

    @Unroll
    def "Get page ranges AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))


        when:
        bu.getPageRanges(new BlobRange(0, PageBlobClient.PAGE_BYTES), bac, null)

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
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(setupBlobLeaseCondition(bu, leaseID)))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(setupBlobMatchCondition(bu, noneMatch)))

        when:
        bu.getPageRanges(new BlobRange(0, PageBlobClient.PAGE_BYTES), bac, null)

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
        bu = cu.getPageBlobClient(generateBlobName())

        when:
        bu.getPageRanges(null)

        then:
        thrown(StorageException)
    }

    def "Get page ranges diff"() {
        setup:
        bu.create(PageBlobClient.PAGE_BYTES * 2)

        bu.uploadPages(new PageRange().start(PageBlobClient.PAGE_BYTES).end(PageBlobClient.PAGE_BYTES * 2 - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)))

        String snapshot = bu.createSnapshot(null, null, null).value()

        bu.uploadPages(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)))

        bu.clearPages(new PageRange().start(PageBlobClient.PAGE_BYTES).end(PageBlobClient.PAGE_BYTES * 2 - 1))

        when:
        Response<PageList> response = bu.getPageRangesDiff(new BlobRange(0, PageBlobClient.PAGE_BYTES * 2), snapshot)

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
        String snapshot = bu.createSnapshot().value().getSnapshotId()

        when:
        bu.getPageRangesDiff(null, snapshot).iterator().hasNext()

        then:
        notThrown(StorageException)
    }

    @Unroll
    def "Get page ranges diff AC"() {
        setup:
        String snapshot = bu.createSnapshot().value().getSnapshotId()
        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(setupBlobLeaseCondition(bu, leaseID)))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(setupBlobMatchCondition(bu, match))
                .ifNoneMatch(noneMatch))

        when:
        bu.getPageRangesDiff(new BlobRange(0, PageBlobClient.PAGE_BYTES), snapshot, bac, null)

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
        String snapshot = bu.createSnapshot().value()

        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(setupBlobLeaseCondition(bu, leaseID)))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(setupBlobMatchCondition(bu, noneMatch)))

        when:
        bu.getPageRangesDiff(new BlobRange(0, PageBlobClient.PAGE_BYTES), snapshot, bac, null)

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
        bu = cu.getPageBlobClient(generateBlobName())

        when:
        bu.getPageRangesDiff(null, "snapshot")

        then:
        thrown(StorageException)
    }

    @Unroll
    def "PageRange IA"() {
        setup:
        def range = new PageRange().start(start).end(end)

        when:
        bu.clearPages(range)

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
        Response<PageBlobItem> response = bu.resize(PageBlobClient.PAGE_BYTES * 2)

        expect:
        Integer.parseInt(bu.getProperties().headers().value("Content-Length")) == PageBlobClient.PAGE_BYTES * 2
        validateBasicHeaders(response.headers())
        response.value().blobSequenceNumber() != null
    }

    def "Resize min"() {
        expect:
        bu.resize(PageBlobClient.PAGE_BYTES).statusCode() == 200
    }

    @Unroll
    def "Resize AC"() {
        setup:
        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(setupBlobLeaseCondition(bu, leaseID)))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(setupBlobMatchCondition(bu, match))
                .ifNoneMatch(noneMatch))

        expect:
        bu.resize(PageBlobClient.PAGE_BYTES * 2, bac, null).statusCode() == 200

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
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(setupBlobLeaseCondition(bu, leaseID)))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(setupBlobMatchCondition(bu, noneMatch)))

        when:
        bu.resize(PageBlobClient.PAGE_BYTES * 2, bac, null)

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
        bu = cu.getPageBlobClient(generateBlobName())

        when:
        bu.resize(0)

        then:
        thrown(StorageException)
    }

    @Unroll
    def "Sequence number"() {
        setup:
        Response<PageBlobItem> response = bu.updateSequenceNumber(action, number)

        expect:
        Integer.parseInt(bu.getProperties().headers().value("x-ms-blob-sequence-number")) == result
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
        bu.updateSequenceNumber(SequenceNumberActionType.INCREMENT, null).statusCode() == 200
    }

    @Unroll
    def "Sequence number AC"() {
        setup:
        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(setupBlobLeaseCondition(bu, leaseID)))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(setupBlobMatchCondition(bu, match))
                .ifNoneMatch(noneMatch))

        expect:
        bu.updateSequenceNumber(SequenceNumberActionType.UPDATE, 1, bac, null)
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
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(setupBlobLeaseCondition(bu, leaseID)))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(setupBlobMatchCondition(bu, noneMatch)))

        when:
        bu.updateSequenceNumber(SequenceNumberActionType.UPDATE, 1, bac, null)

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
        bu = cu.getPageBlobClient(generateBlobName())

        when:
        bu.updateSequenceNumber(SequenceNumberActionType.UPDATE, 0)

        then:
        thrown(StorageException)
    }

    def "Start incremental copy"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.BLOB, null)
        PageBlobClient bu2 = cu.getPageBlobClient(generateBlobName())
        String snapshot = bu.createSnapshot().value().getSnapshotId()

        Response<CopyStatusType> copyResponse = bu2.copyIncremental(bu.getBlobUrl(), snapshot)
        String status = copyResponse.value().toString()

        OffsetDateTime start = OffsetDateTime.now()
        while (status != CopyStatusType.SUCCESS.toString()) {
            status = bu2.getProperties().headers().value("x-ms-copy-status")
            OffsetDateTime currentTime = OffsetDateTime.now()
            if (status == CopyStatusType.FAILED.toString() || currentTime.minusMinutes(1) == start) {
                throw new Exception("Copy failed or took too long")
            }
            sleep(1000)
        }

        expect:
        Response<BlobProperties> propertiesResponse = bu2.getProperties()
        Boolean.parseBoolean(propertiesResponse.headers().value("x-ms-incremental-copy"))
        propertiesResponse.headers().value("x-ms-copy-destination-snapshot") != null
        validateBasicHeaders(copyResponse.headers())
        copyResponse.headers().value("x-ms-copy-id") != null
        copyResponse.value() != null
    }

    def "Start incremental copy min"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.BLOB, null)
        PageBlobClient bu2 = cu.getPageBlobClient(generateBlobName())
        String snapshot = bu.createSnapshot().value().getSnapshotId()

        expect:
        bu2.copyIncremental(bu.getBlobUrl(), snapshot).statusCode() == 202
    }

    @Unroll
    def "Start incremental copy AC"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.BLOB, null)
        PageBlobClient bu2 = cu.getPageBlobClient(generateBlobName())
        String snapshot = bu.createSnapshot().value().getSnapshotId()

        Response<CopyStatusType> copyResponse = bu2.copyIncremental(bu.getBlobUrl(), snapshot)
        String status = copyResponse.value().toString()

        OffsetDateTime start = OffsetDateTime.now()
        while (status != CopyStatusType.SUCCESS.toString()) {
            status = bu2.getProperties().headers().value("x-ms-copy-status")
            OffsetDateTime currentTime = OffsetDateTime.now()
            if (status == CopyStatusType.FAILED.toString() || currentTime.minusMinutes(1) == start) {
                throw new Exception("Copy failed or took too long")
            }
            sleep(1000)
        }

        snapshot = bu.createSnapshot().value().getSnapshotId()
        match = setupBlobMatchCondition(bu2, match)
        def mac = new ModifiedAccessConditions()
            .ifModifiedSince(modified)
            .ifUnmodifiedSince(unmodified)
            .ifMatch(match)
            .ifNoneMatch(noneMatch)

        expect:
        bu2.copyIncremental(bu.getBlobUrl(), snapshot, mac, null).statusCode() == 202

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
        cu.setAccessPolicy(PublicAccessType.BLOB, null)
        PageBlobClient bu2 = cu.getPageBlobClient(generateBlobName())
        String snapshot = bu.createSnapshot().value().getSnapshotId()
        bu2.copyIncremental(bu.getBlobUrl(), snapshot)
        snapshot = bu.createSnapshot().value().getSnapshotId()
        noneMatch = setupBlobMatchCondition(bu2, noneMatch)
        def mac = new ModifiedAccessConditions()
            .ifModifiedSince(modified)
            .ifUnmodifiedSince(unmodified)
            .ifMatch(match)
            .ifNoneMatch(noneMatch)

        when:
        bu2.copyIncremental(bu.getBlobUrl(), snapshot, mac, null).statusCode()

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
        bu = cu.getPageBlobClient(generateBlobName())

        when:
        bu.copyIncremental(new URL("https://www.error.com"), "snapshot")

        then:
        thrown(StorageException)
    }
}
