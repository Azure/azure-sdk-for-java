// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.core.http.rest.Response
import com.azure.storage.blob.models.BlobAccessConditions
import com.azure.storage.blob.models.BlobHTTPHeaders
import com.azure.storage.blob.models.BlobRange
import com.azure.storage.blob.models.CopyStatusType
import com.azure.storage.blob.models.LeaseAccessConditions
import com.azure.storage.blob.models.Metadata
import com.azure.storage.blob.models.ModifiedAccessConditions
import com.azure.storage.blob.models.PageBlobAccessConditions
import com.azure.storage.blob.models.PageBlobItem
import com.azure.storage.blob.models.PageList
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
    PageBlobClient bu

    def setup() {
        bu = cu.getPageBlobClient(generateBlobName())
        bu.create(PageBlobClient.PAGE_BYTES)
    }

    def "Create all null"() {
        setup:
        bu = cu.getPageBlobClient(generateBlobName())

        when:
        Response<PageBlobItem> response = bu.createWithResponse(PageBlobClient.PAGE_BYTES, null, null, null, null, null, null)

        then:
        response.statusCode() == 201
        validateBasicHeaders(response.headers())
        response.value().contentMD5() == null
        response.value().isServerEncrypted()
    }

    def "Create min"() {
        expect:
        bu.createWithResponse(PageBlobClient.PAGE_BYTES, null, null, null, null, null, null).statusCode() == 201
    }

    def "Create sequence number"() {
        when:
        bu.createWithResponse(PageBlobClient.PAGE_BYTES, 2, null, null, null, null, null)

        then:
        Integer.parseInt(bu.getPropertiesWithResponse(null, null, null).headers().value("x-ms-blob-sequence-number")) == 2
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
        bu.createWithResponse(PageBlobClient.PAGE_BYTES, null, headers, null, null, null, null)

        Response<BlobProperties> response = bu.getPropertiesWithResponse(null, null, null)

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
        bu.createWithResponse(PageBlobClient.PAGE_BYTES, null, null, metadata, null, null, null)

        Response<BlobProperties> response = bu.getPropertiesWithResponse(null, null, null)

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
        bu.createWithResponse(PageBlobClient.PAGE_BYTES, null, null, null, bac, null, null)
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
        bu.createWithResponse(PageBlobClient.PAGE_BYTES, null, null, null, bac, null, null)

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
        bu.createWithResponse(PageBlobClient.PAGE_BYTES, null, null, null,
            new BlobAccessConditions().leaseAccessConditions(new LeaseAccessConditions().leaseId("id")), null, null)

        then:
        thrown(StorageException)
    }

    def "Upload page"() {
        when:
        Response<PageBlobItem> response = bu.uploadPagesWithResponse(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1),
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
        bu.uploadPagesWithResponse(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)), null, null, null).statusCode() == 201
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
        bu.uploadPagesWithResponse(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1),
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
        bu.uploadPagesWithResponse(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1),
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
        bu = cu.getPageBlobClient(generateBlobName())

        when:
        bu.uploadPagesWithResponse(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)),
            new PageBlobAccessConditions().leaseAccessConditions(new LeaseAccessConditions().leaseId("id")),
            null, null)

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
        Response<PageBlobItem> response = bu.uploadPagesFromURLWithResponse(pageRange, destURL.getBlobUrl(), null, null, null, null, null, null)

        then:
        response.statusCode() == 201
        validateBasicHeaders(response.headers())
    }

    def "Upload page from URL range"() {
        setup:
        cu.setAccessPolicyWithResponse(PublicAccessType.CONTAINER, null, null, null, null)

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
        cu.setAccessPolicyWithResponse(PublicAccessType.CONTAINER, null, null, null, null)
        def destURL = cu.getPageBlobClient(generateBlobName())
        destURL.create(PageBlobClient.PAGE_BYTES)
        def data = getRandomByteArray(PageBlobClient.PAGE_BYTES)
        def pageRange = new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1)
        bu.uploadPages(pageRange, new ByteArrayInputStream(data))

        when:
        destURL.uploadPagesFromURLWithResponse(pageRange, bu.getBlobUrl(), null, MessageDigest.getInstance("MD5").digest(data),
            null, null, null, null)

        then:
        notThrown(StorageException)
    }

    def "Upload page from URL MD5 fail"() {
        setup:
        cu.setAccessPolicyWithResponse(PublicAccessType.CONTAINER, null, null, null, null)
        def destURL = cu.getPageBlobClient(generateBlobName())
        destURL.create(PageBlobClient.PAGE_BYTES)
        def pageRange = new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1)
        bu.uploadPages(pageRange, new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)))

        when:
        destURL.uploadPagesFromURLWithResponse(pageRange, bu.getBlobUrl(), null,
            MessageDigest.getInstance("MD5").digest("garbage".getBytes()), null, null, null, null)

        then:
        thrown(StorageException)
    }

    @Unroll
    def "Upload page from URL destination AC"() {
        setup:
        cu.setAccessPolicyWithResponse(PublicAccessType.CONTAINER, null, null, null, null)
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
        bu.uploadPagesFromURLWithResponse(pageRange, sourceURL.getBlobUrl(), null, null, pac, null, null, null).statusCode() == 201

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
        cu.setAccessPolicyWithResponse(PublicAccessType.CONTAINER, null, null, null, null)

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
        bu.uploadPagesFromURLWithResponse(pageRange, sourceURL.getBlobUrl(), null, null, pac, null, null, null)

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
        cu.setAccessPolicyWithResponse(PublicAccessType.CONTAINER, null, null, null, null)
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
        bu.uploadPagesFromURLWithResponse(pageRange, sourceURL.getBlobUrl(), null, null, null, smac, null, null).statusCode() == 201

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
        cu.setAccessPolicyWithResponse(PublicAccessType.CONTAINER, null, null, null, null)
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
        bu.uploadPagesFromURLWithResponse(pageRange, sourceURL.getBlobUrl(), null, null, null, smac, null, null)

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
        bu.uploadPagesWithResponse(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)), null, null, null)

        when:
        Response<PageBlobItem> response = bu.clearPagesWithResponse(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1), null, null, null)

        then:
        bu.getPageRanges(new BlobRange(0)).pageRange().size() == 0
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
        bu.clearPagesWithResponse(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1), pac, null, null)
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
        bu.clearPagesWithResponse(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1), pac, null, null)

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
        Response<PageList> response = bu.getPageRangesWithResponse(new BlobRange(0, PageBlobClient.PAGE_BYTES), null, null, null)

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
        bu.getPageRangesWithResponse(new BlobRange(0, PageBlobClient.PAGE_BYTES), bac, null, null)

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
        bu.getPageRangesWithResponse(new BlobRange(0, PageBlobClient.PAGE_BYTES), bac, null, null)

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

        String snapshot = bu.createSnapshot()

        bu.uploadPages(new PageRange().start(0).end(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)))

        bu.clearPages(new PageRange().start(PageBlobClient.PAGE_BYTES).end(PageBlobClient.PAGE_BYTES * 2 - 1))

        when:
        Response<PageList> response = bu.getPageRangesDiffWithResponse(new BlobRange(0, PageBlobClient.PAGE_BYTES * 2), snapshot, null, null, null)

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
        String snapshot = bu.createSnapshot().getSnapshotId()

        when:
        bu.getPageRangesDiff(null, snapshot).iterator().hasNext()

        then:
        notThrown(StorageException)
    }

    @Unroll
    def "Get page ranges diff AC"() {
        setup:
        String snapshot = bu.createSnapshot().getSnapshotId()
        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(setupBlobLeaseCondition(bu, leaseID)))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(setupBlobMatchCondition(bu, match))
                .ifNoneMatch(noneMatch))

        when:
        bu.getPageRangesDiffWithResponse(new BlobRange(0, PageBlobClient.PAGE_BYTES), snapshot, bac, null, null)

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
        String snapshot = bu.createSnapshot()

        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(setupBlobLeaseCondition(bu, leaseID)))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(setupBlobMatchCondition(bu, noneMatch)))

        when:
        bu.getPageRangesDiffWithResponse(new BlobRange(0, PageBlobClient.PAGE_BYTES), snapshot, bac, null, null)

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
        Response<PageBlobItem> response = bu.resizeWithResponse(PageBlobClient.PAGE_BYTES * 2, null, null, null)

        expect:
        Integer.parseInt(bu.getPropertiesWithResponse(null, null, null).headers().value("Content-Length")) == PageBlobClient.PAGE_BYTES * 2
        validateBasicHeaders(response.headers())
        response.value().blobSequenceNumber() != null
    }

    def "Resize min"() {
        expect:
        bu.resizeWithResponse(PageBlobClient.PAGE_BYTES, null, null, null).statusCode() == 200
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
        bu.resizeWithResponse(PageBlobClient.PAGE_BYTES * 2, bac, null, null).statusCode() == 200

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
        bu.resizeWithResponse(PageBlobClient.PAGE_BYTES * 2, bac, null, null)

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
        Response<PageBlobItem> response = bu.updateSequenceNumberWithResponse(action, number, null, null, null)

        expect:
        Integer.parseInt(bu.getPropertiesWithResponse(null, null, null).headers().value("x-ms-blob-sequence-number")) == result
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
        bu.updateSequenceNumberWithResponse(SequenceNumberActionType.INCREMENT, null, null, null, null).statusCode() == 200
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
        bu.updateSequenceNumberWithResponse(SequenceNumberActionType.UPDATE, 1, bac, null, null)
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
        bu.updateSequenceNumberWithResponse(SequenceNumberActionType.UPDATE, 1, bac, null, null)

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
        String snapshot = bu.createSnapshot().getSnapshotId()

        Response<CopyStatusType> copyResponse = bu2.copyIncrementalWithResponse(bu.getBlobUrl(), snapshot, null, null, null)
        String status = copyResponse.value().toString()

        OffsetDateTime start = OffsetDateTime.now()
        while (status != CopyStatusType.SUCCESS.toString()) {
            status = bu2.getPropertiesWithResponse(null, null, null).headers().value("x-ms-copy-status")
            OffsetDateTime currentTime = OffsetDateTime.now()
            if (status == CopyStatusType.FAILED.toString() || currentTime.minusMinutes(1) == start) {
                throw new Exception("Copy failed or took too long")
            }
            sleep(1000)
        }

        expect:
        Response<BlobProperties> propertiesResponse = bu2.getPropertiesWithResponse(null, null, null)
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
        String snapshot = bu.createSnapshot().getSnapshotId()

        expect:
        bu2.copyIncrementalWithResponse(bu.getBlobUrl(), snapshot, null, null, null).statusCode() == 202
    }

    @Unroll
    def "Start incremental copy AC"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.BLOB, null)
        PageBlobClient bu2 = cu.getPageBlobClient(generateBlobName())
        String snapshot = bu.createSnapshot().getSnapshotId()

        Response<CopyStatusType> copyResponse = bu2.copyIncrementalWithResponse(bu.getBlobUrl(), snapshot, null, null, null)
        String status = copyResponse.value().toString()

        OffsetDateTime start = OffsetDateTime.now()
        while (status != CopyStatusType.SUCCESS.toString()) {
            status = bu2.getPropertiesWithResponse(null, null, null).headers().value("x-ms-copy-status")
            OffsetDateTime currentTime = OffsetDateTime.now()
            if (status == CopyStatusType.FAILED.toString() || currentTime.minusMinutes(1) == start) {
                throw new Exception("Copy failed or took too long")
            }
            sleep(1000)
        }

        snapshot = bu.createSnapshot().getSnapshotId()
        match = setupBlobMatchCondition(bu2, match)
        def mac = new ModifiedAccessConditions()
            .ifModifiedSince(modified)
            .ifUnmodifiedSince(unmodified)
            .ifMatch(match)
            .ifNoneMatch(noneMatch)

        expect:
        bu2.copyIncrementalWithResponse(bu.getBlobUrl(), snapshot, mac, null, null).statusCode() == 202

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
        String snapshot = bu.createSnapshot().getSnapshotId()
        bu2.copyIncremental(bu.getBlobUrl(), snapshot)
        snapshot = bu.createSnapshot().getSnapshotId()
        noneMatch = setupBlobMatchCondition(bu2, noneMatch)
        def mac = new ModifiedAccessConditions()
            .ifModifiedSince(modified)
            .ifUnmodifiedSince(unmodified)
            .ifMatch(match)
            .ifNoneMatch(noneMatch)

        when:
        bu2.copyIncrementalWithResponse(bu.getBlobUrl(), snapshot, mac, null, null).statusCode()

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
