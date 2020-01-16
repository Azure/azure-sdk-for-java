// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized

import com.azure.core.exception.UnexpectedLengthException
import com.azure.core.http.RequestConditions
import com.azure.storage.blob.APISpec
import com.azure.storage.blob.models.BlobErrorCode
import com.azure.storage.blob.models.BlobHttpHeaders
import com.azure.storage.blob.models.BlobRange
import com.azure.storage.blob.models.BlobRequestConditions
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.blob.models.CopyStatusType
import com.azure.storage.blob.models.PageBlobRequestConditions
import com.azure.storage.blob.models.PageRange
import com.azure.storage.blob.models.PublicAccessType
import com.azure.storage.blob.models.SequenceNumberActionType
import spock.lang.Unroll

import java.security.MessageDigest
import java.time.OffsetDateTime

class PageBlobAPITest extends APISpec {
    PageBlobClient bc
    String blobName

    def setup() {
        blobName = generateBlobName()
        bc = cc.getBlobClient(blobName).getPageBlobClient()
        bc.create(PageBlobClient.PAGE_BYTES)
    }

    def "Create all null"() {
        setup:
        bc = cc.getBlobClient(generateBlobName()).getPageBlobClient()

        when:
        def response = bc.createWithResponse(PageBlobClient.PAGE_BYTES, null, null, null, null, null, null)

        then:
        response.getStatusCode() == 201
        validateBasicHeaders(response.getHeaders())
        response.getValue().getContentMd5() == null
        response.getValue().isServerEncrypted()
    }

    def "Create min"() {
        expect:
        bc.createWithResponse(PageBlobClient.PAGE_BYTES, null, null, null, null, null, null).getStatusCode() == 201
    }

    def "Create sequence number"() {
        when:
        bc.createWithResponse(PageBlobClient.PAGE_BYTES, 2, null, null, null, null, null)

        then:
        bc.getProperties().getBlobSequenceNumber() == 2
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
        def metadata = new HashMap<String, String>()
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
        response.getStatusCode() == 200
        response.getValue().getMetadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @Unroll
    def "Create AC"() {
        setup:
        def bac = new BlobRequestConditions()
            .setLeaseId(setupBlobLeaseCondition(bc, leaseID))
            .setIfMatch(setupBlobMatchCondition(bc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:

        bc.createWithResponse(PageBlobClient.PAGE_BYTES, null, null, null, bac, null, null).getStatusCode() == 201

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
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupBlobMatchCondition(bc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        bc.createWithResponse(PageBlobClient.PAGE_BYTES, null, null, null, bac, null, null)

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

    def "Create error"() {
        when:
        bc.createWithResponse(PageBlobClient.PAGE_BYTES, null, null, null, new BlobRequestConditions().setLeaseId("id"), null, null)

        then:
        thrown(BlobStorageException)
    }

    def "Upload page"() {
        when:
        def response = bc.uploadPagesWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)), null, null, null, null)

        then:
        response.getStatusCode() == 201
        validateBasicHeaders(response.getHeaders())
        response.getHeaders().getValue("x-ms-content-crc64") != null
        response.getValue().getBlobSequenceNumber() == 0
        response.getValue().isServerEncrypted()
    }

    def "Upload page min"() {
        expect:
        bc.uploadPagesWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)), null, null, null, null)
            .getStatusCode() == 201
    }

    @Unroll
    def "Upload page IA"() {
        when:
        def data = (dataSize == null) ? null : new ByteArrayInputStream(getRandomByteArray(dataSize))
        bc.uploadPages(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES * 2 - 1), data)

        then:
        thrown(exceptionType)

        where:
        dataSize                      | exceptionType
        null                          | NullPointerException
        PageBlobClient.PAGE_BYTES     | UnexpectedLengthException
        PageBlobClient.PAGE_BYTES * 3 | UnexpectedLengthException
    }

    def "Upload page transactionalMD5"() {
        setup:
        def data = getRandomByteArray(PageBlobClient.PAGE_BYTES)
        byte[] md5 = MessageDigest.getInstance("MD5").digest(data)

        expect:
        bc.uploadPagesWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(data), md5, null, null, null).getStatusCode() == 201
    }

    def "Upload page transactionalMD5 fail"() {
        when:
        bc.uploadPagesWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)),
            MessageDigest.getInstance("MD5").digest("garbage".getBytes()), null, null, null)

        then:
        def e = thrown(BlobStorageException)
        e.getErrorCode() == BlobErrorCode.MD5MISMATCH
    }

    @Unroll
    def "Upload page AC"() {
        setup:
        def pac = new PageBlobRequestConditions()
            .setLeaseId(setupBlobLeaseCondition(bc, leaseID))
            .setIfMatch(setupBlobMatchCondition(bc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfSequenceNumberLessThan(sequenceNumberLT)
            .setIfSequenceNumberLessThanOrEqualTo(sequenceNumberLTE)
            .setIfSequenceNumberEqualTo(sequenceNumberEqual)

        expect:
        bc.uploadPagesWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)), null, pac, null, null)
            .getStatusCode() == 201

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
        def pac = new PageBlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfSequenceNumberLessThan(sequenceNumberLT)
            .setIfSequenceNumberLessThanOrEqualTo(sequenceNumberLTE)
            .setIfSequenceNumberEqualTo(sequenceNumberEqual)

        when:
        bc.uploadPagesWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)), null, pac, null, null)

        then:
        thrown(BlobStorageException)

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
        bc = cc.getBlobClient(generateBlobName()).getPageBlobClient()

        when:
        bc.uploadPagesWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)), null,
            new PageBlobRequestConditions().setLeaseId("id"), null, null)

        then:
        thrown(BlobStorageException)
    }

    def "Upload page from URL min"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def destURL = cc.getBlobClient(generateBlobName()).getPageBlobClient()
        destURL.create(PageBlobClient.PAGE_BYTES)
        destURL.uploadPages(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)))
        def pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1)

        when:
        def response = bc.uploadPagesFromUrlWithResponse(pageRange, destURL.getBlobUrl(), null, null, null, null, null, null)

        then:
        response.getStatusCode() == 201
        validateBasicHeaders(response.getHeaders())
    }

    def "Upload page from URL range"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)

        def data = getRandomByteArray(PageBlobClient.PAGE_BYTES * 4)

        def sourceURL = cc.getBlobClient(generateBlobName()).getPageBlobClient()
        sourceURL.create(PageBlobClient.PAGE_BYTES * 4)
        sourceURL.uploadPages(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES * 4 - 1),
            new ByteArrayInputStream(data))

        def destURL = cc.getBlobClient(generateBlobName()).getPageBlobClient()
        destURL.create(PageBlobClient.PAGE_BYTES * 2)

        when:
        destURL.uploadPagesFromUrl(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES * 2 - 1),
            sourceURL.getBlobUrl(), PageBlobClient.PAGE_BYTES * 2)

        then:
        def outputStream = new ByteArrayOutputStream()
        destURL.download(outputStream)
        outputStream.toByteArray() == Arrays.copyOfRange(data, PageBlobClient.PAGE_BYTES * 2, PageBlobClient.PAGE_BYTES * 4)
    }

    def "Upload page from URL IA"() {
        when:
        bc.uploadPagesFromUrl(null, bc.getBlobUrl(), (Long) PageBlobClient.PAGE_BYTES)

        then:
        thrown(IllegalArgumentException)
    }

    def "Upload page from URL MD5"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def destURL = cc.getBlobClient(generateBlobName()).getPageBlobClient()
        destURL.create(PageBlobClient.PAGE_BYTES)
        def data = getRandomByteArray(PageBlobClient.PAGE_BYTES)
        def pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1)
        bc.uploadPages(pageRange, new ByteArrayInputStream(data))

        when:
        destURL.uploadPagesFromUrlWithResponse(pageRange, bc.getBlobUrl(), null, MessageDigest.getInstance("MD5").digest(data),
            null, null, null, null)

        then:
        notThrown(BlobStorageException)
    }

    def "Upload page from URL MD5 fail"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def destURL = cc.getBlobClient(generateBlobName()).getPageBlobClient()
        destURL.create(PageBlobClient.PAGE_BYTES)
        def pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1)
        bc.uploadPages(pageRange, new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)))

        when:
        destURL.uploadPagesFromUrlWithResponse(pageRange, bc.getBlobUrl(), null,
            MessageDigest.getInstance("MD5").digest("garbage".getBytes()), null, null, null, null)

        then:
        thrown(BlobStorageException)
    }

    @Unroll
    def "Upload page from URL destination AC"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def sourceURL = cc.getBlobClient(generateBlobName()).getPageBlobClient()
        sourceURL.create(PageBlobClient.PAGE_BYTES)
        def pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1)
        sourceURL.uploadPages(pageRange, new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)))

        def pac = new PageBlobRequestConditions()
            .setLeaseId(setupBlobLeaseCondition(bc, leaseID))
            .setIfMatch(setupBlobMatchCondition(bc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfSequenceNumberLessThan(sequenceNumberLT)
            .setIfSequenceNumberLessThanOrEqualTo(sequenceNumberLTE)
            .setIfSequenceNumberEqualTo(sequenceNumberEqual)

        expect:
        bc.uploadPagesFromUrlWithResponse(pageRange, sourceURL.getBlobUrl(), null, null, pac, null, null, null).getStatusCode() == 201

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

        def sourceURL = cc.getBlobClient(generateBlobName()).getPageBlobClient()
        sourceURL.create(PageBlobClient.PAGE_BYTES)
        def pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1)
        sourceURL.uploadPages(pageRange, new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)))

        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        def pac = new PageBlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfSequenceNumberLessThan(sequenceNumberLT)
            .setIfSequenceNumberLessThanOrEqualTo(sequenceNumberLTE)
            .setIfSequenceNumberEqualTo(sequenceNumberEqual)

        when:
        bc.uploadPagesFromUrlWithResponse(pageRange, sourceURL.getBlobUrl(), null, null, pac, null, null, null)

        then:
        thrown(BlobStorageException)

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
        def sourceURL = cc.getBlobClient(generateBlobName()).getPageBlobClient()
        sourceURL.create(PageBlobClient.PAGE_BYTES)
        def pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1)
        sourceURL.uploadPages(pageRange, new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)))

        sourceIfMatch = setupBlobMatchCondition(sourceURL, sourceIfMatch)
        def smac = new BlobRequestConditions()
            .setIfModifiedSince(sourceIfModifiedSince)
            .setIfUnmodifiedSince(sourceIfUnmodifiedSince)
            .setIfMatch(sourceIfMatch)
            .setIfNoneMatch(sourceIfNoneMatch)

        expect:
        bc.uploadPagesFromUrlWithResponse(pageRange, sourceURL.getBlobUrl(), null, null, null, smac, null, null).getStatusCode() == 201

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
        def sourceURL = cc.getBlobClient(generateBlobName()).getPageBlobClient()
        sourceURL.create(PageBlobClient.PAGE_BYTES)
        def pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1)
        sourceURL.uploadPages(pageRange, new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)))

        def smac = new BlobRequestConditions()
            .setIfModifiedSince(sourceIfModifiedSince)
            .setIfUnmodifiedSince(sourceIfUnmodifiedSince)
            .setIfMatch(sourceIfMatch)
            .setIfNoneMatch(setupBlobMatchCondition(sourceURL, sourceIfNoneMatch))

        when:
        bc.uploadPagesFromUrlWithResponse(pageRange, sourceURL.getBlobUrl(), null, null, null, smac, null, null)
        then:
        thrown(BlobStorageException)

        where:
        sourceIfModifiedSince | sourceIfUnmodifiedSince | sourceIfMatch | sourceIfNoneMatch
        newDate               | null                    | null          | null
        null                  | oldDate                 | null          | null
        null                  | null                    | garbageEtag   | null
        null                  | null                    | null          | receivedEtag
    }

    def "Clear page"() {
        setup:
        bc.uploadPagesWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)), null, null, null, null)

        when:
        def response = bc.clearPagesWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            null, null, null)

        then:
        bc.getPageRanges(new BlobRange(0)).getPageRange().size() == 0
        validateBasicHeaders(response.getHeaders())
        response.getValue().getContentMd5() == null
        response.getValue().getBlobSequenceNumber() == 0
    }

    def "Clear page min"() {
        expect:
        bc.clearPages(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1))
    }

    @Unroll
    def "Clear pages AC"() {
        setup:
        bc.uploadPages(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)))
        match = setupBlobMatchCondition(bc, match)
        leaseID = setupBlobLeaseCondition(bc, leaseID)
        def pac = new PageBlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfSequenceNumberLessThan(sequenceNumberLT)
            .setIfSequenceNumberLessThanOrEqualTo(sequenceNumberLTE)
            .setIfSequenceNumberEqualTo(sequenceNumberEqual)

        expect:
        bc.clearPagesWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1), pac, null, null)
            .getStatusCode() == 201

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
        bc.uploadPages(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)))
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        setupBlobLeaseCondition(bc, leaseID)
        def pac = new PageBlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfSequenceNumberLessThan(sequenceNumberLT)
            .setIfSequenceNumberLessThanOrEqualTo(sequenceNumberLTE)
            .setIfSequenceNumberEqualTo(sequenceNumberEqual)


        when:
        bc.clearPagesWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1), pac, null, null)

        then:
        thrown(BlobStorageException)

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
        bc = cc.getBlobClient(generateBlobName()).getPageBlobClient()

        when:
        bc.clearPages(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1))

        then:
        thrown(BlobStorageException)
    }

    def "Get page ranges"() {
        setup:
        bc.uploadPages(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)))

        when:
        def response = bc.getPageRangesWithResponse(new BlobRange(0, PageBlobClient.PAGE_BYTES), null, null, null)

        then:
        response.getStatusCode() == 200
        response.getValue().getPageRange().size() == 1
        validateBasicHeaders(response.getHeaders())
        Long.parseLong(response.getHeaders().get("x-ms-blob-content-length").getValue()) == (long) PageBlobClient.PAGE_BYTES
    }

    def "Get page ranges min"() {
        when:
        bc.getPageRanges(null)

        then:
        notThrown(BlobStorageException)
    }

    @Unroll
    def "Get page ranges AC"() {
        setup:
        match = setupBlobMatchCondition(bc, match)
        leaseID = setupBlobLeaseCondition(bc, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)


        when:
        bc.getPageRangesWithResponse(new BlobRange(0, PageBlobClient.PAGE_BYTES), bac, null, null)

        then:
        notThrown(BlobStorageException)

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
        def bac = new BlobRequestConditions()
            .setLeaseId(setupBlobLeaseCondition(bc, leaseID))
            .setIfMatch(match)
            .setIfNoneMatch(setupBlobMatchCondition(bc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        bc.getPageRangesWithResponse(new BlobRange(0, PageBlobClient.PAGE_BYTES), bac, null, null)

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

    def "Get page ranges error"() {
        setup:
        bc = cc.getBlobClient(generateBlobName()).getPageBlobClient()

        when:
        bc.getPageRanges(null)

        then:
        thrown(BlobStorageException)
    }

    def "Get page ranges diff"() {
        setup:
        bc.create(PageBlobClient.PAGE_BYTES * 2, true)

        bc.uploadPages(new PageRange().setStart(PageBlobClient.PAGE_BYTES).setEnd(PageBlobClient.PAGE_BYTES * 2 - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)))

        def snapId = bc.createSnapshot().getSnapshotId()

        bc.uploadPages(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)))

        bc.clearPages(new PageRange().setStart(PageBlobClient.PAGE_BYTES).setEnd(PageBlobClient.PAGE_BYTES * 2 - 1))

        when:
        def response = bc.getPageRangesDiffWithResponse(new BlobRange(0, PageBlobClient.PAGE_BYTES * 2), snapId, null, null, null)

        then:
        response.getValue().getPageRange().size() == 1
        response.getValue().getPageRange().get(0).getStart() == 0
        response.getValue().getPageRange().get(0).getEnd() == PageBlobClient.PAGE_BYTES - 1
        response.getValue().getClearRange().size() == 1
        response.getValue().getClearRange().get(0).getStart() == PageBlobClient.PAGE_BYTES
        response.getValue().getClearRange().get(0).getEnd() == PageBlobClient.PAGE_BYTES * 2 - 1
        validateBasicHeaders(response.getHeaders())
        Integer.parseInt(response.getHeaders().getValue("x-ms-blob-content-length")) == PageBlobClient.PAGE_BYTES * 2
    }

    def "Get page ranges diff min"() {
        setup:
        def snapId = bc.createSnapshot().getSnapshotId()

        when:
        bc.getPageRangesDiff(null, snapId).iterator().hasNext()

        then:
        notThrown(BlobStorageException)
    }

    @Unroll
    def "Get page ranges diff AC"() {
        setup:
        def snapId = bc.createSnapshot().getSnapshotId()
        def bac = new BlobRequestConditions()
            .setLeaseId(setupBlobLeaseCondition(bc, leaseID))
            .setIfMatch(setupBlobMatchCondition(bc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        bc.getPageRangesDiffWithResponse(new BlobRange(0, PageBlobClient.PAGE_BYTES), snapId, bac, null, null)

        then:
        notThrown(BlobStorageException)

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

        def bac = new BlobRequestConditions()
            .setLeaseId(setupBlobLeaseCondition(bc, leaseID))
            .setIfMatch(match)
            .setIfNoneMatch(setupBlobMatchCondition(bc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        bc.getPageRangesDiffWithResponse(new BlobRange(0, PageBlobClient.PAGE_BYTES), snapId, bac, null, null)

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

    def "Get page ranges diff error"() {
        setup:
        bc = cc.getBlobClient(generateBlobName()).getPageBlobClient()

        when:
        bc.getPageRangesDiff(null, "snapshot")

        then:
        thrown(BlobStorageException)
    }

    @Unroll
    def "PageRange IA"() {
        setup:
        def range = new PageRange().setStart(start).setEnd(end)

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
        bc.getProperties().getBlobSize() == PageBlobClient.PAGE_BYTES * 2
        validateBasicHeaders(response.getHeaders())
        response.getValue().getBlobSequenceNumber() != null
    }

    def "Resize min"() {
        expect:
        bc.resizeWithResponse(PageBlobClient.PAGE_BYTES, null, null, null).getStatusCode() == 200
    }

    @Unroll
    def "Resize AC"() {
        setup:
        def bac = new BlobRequestConditions()
            .setLeaseId(setupBlobLeaseCondition(bc, leaseID))
            .setIfMatch(setupBlobMatchCondition(bc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        bc.resizeWithResponse(PageBlobClient.PAGE_BYTES * 2, bac, null, null).getStatusCode() == 200

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
        def bac = new BlobRequestConditions()
            .setLeaseId(setupBlobLeaseCondition(bc, leaseID))
            .setIfMatch(match)
            .setIfNoneMatch(setupBlobMatchCondition(bc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        bc.resizeWithResponse(PageBlobClient.PAGE_BYTES * 2, bac, null, null)

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

    def "Resize error"() {
        setup:
        bc = cc.getBlobClient(generateBlobName()).getPageBlobClient()

        when:
        bc.resize(0)

        then:
        thrown(BlobStorageException)
    }

    @Unroll
    def "Sequence number"() {
        setup:
        def response = bc.updateSequenceNumberWithResponse(action, number, null, null, null)

        expect:
        bc.getProperties().getBlobSequenceNumber() == result
        validateBasicHeaders(response.getHeaders())
        response.getValue().getBlobSequenceNumber() == result

        where:
        action                             | number || result
        SequenceNumberActionType.UPDATE    | 5      || 5
        SequenceNumberActionType.INCREMENT | null   || 1
        SequenceNumberActionType.MAX       | 2      || 2
    }

    def "Sequence number min"() {
        expect:
        bc.updateSequenceNumberWithResponse(SequenceNumberActionType.INCREMENT, null, null, null, null).getStatusCode() == 200
    }

    @Unroll
    def "Sequence number AC"() {
        setup:
        def bac = new BlobRequestConditions()
            .setLeaseId(setupBlobLeaseCondition(bc, leaseID))
            .setIfMatch(setupBlobMatchCondition(bc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        bc.updateSequenceNumberWithResponse(SequenceNumberActionType.UPDATE, 1, bac, null, null)
            .getStatusCode() == 200

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
        def bac = new BlobRequestConditions()
            .setLeaseId(setupBlobLeaseCondition(bc, leaseID))
            .setIfMatch(match)
            .setIfNoneMatch(setupBlobMatchCondition(bc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        bc.updateSequenceNumberWithResponse(SequenceNumberActionType.UPDATE, 1, bac, null, null)

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

    def "Sequence number error"() {
        setup:
        bc = cc.getBlobClient(generateBlobName()).getPageBlobClient()

        when:
        bc.updateSequenceNumber(SequenceNumberActionType.UPDATE, 0)

        then:
        thrown(BlobStorageException)
    }

    def "Start incremental copy"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.BLOB, null)
        def bc2 = cc.getBlobClient(generateBlobName()).getPageBlobClient()
        def snapId = bc.createSnapshot().getSnapshotId()

        def copyResponse = bc2.copyIncrementalWithResponse(bc.getBlobUrl(), snapId, null, null, null)

        def status = copyResponse.getValue()
        def start = OffsetDateTime.now()
        while (status != CopyStatusType.SUCCESS) {
            status = bc2.getProperties().getCopyStatus()
            def currentTime = OffsetDateTime.now()
            if (status == CopyStatusType.FAILED || currentTime.minusMinutes(1) == start) {
                throw new Exception("Copy failed or took too long")
            }
            sleepIfRecord(1000)
        }

        expect:
        def properties = bc2.getProperties()
        properties.isIncrementalCopy()
        properties.getCopyDestinationSnapshot() != null
        validateBasicHeaders(copyResponse.getHeaders())
        copyResponse.getHeaders().getValue("x-ms-copy-id") != null
        copyResponse.getValue() != null
    }

    def "Start incremental copy min"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.BLOB, null)
        def bc2 = cc.getBlobClient(generateBlobName()).getPageBlobClient()
        def snapshot = bc.createSnapshot().getSnapshotId()

        expect:
        bc2.copyIncrementalWithResponse(bc.getBlobUrl(), snapshot, null, null, null).getStatusCode() == 202
    }

    @Unroll
    def "Start incremental copy AC"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.BLOB, null)
        def bu2 = cc.getBlobClient(generateBlobName()).getPageBlobClient()
        def snapshot = bc.createSnapshot().getSnapshotId()

        def copyResponse = bu2.copyIncrementalWithResponse(bc.getBlobUrl(), snapshot, null, null, null)

        def status = copyResponse.getValue()
        def start = OffsetDateTime.now()
        while (status != CopyStatusType.SUCCESS) {
            status = bu2.getProperties().getCopyStatus()
            def currentTime = OffsetDateTime.now()
            if (status == CopyStatusType.FAILED || currentTime.minusMinutes(1) == start) {
                throw new Exception("Copy failed or took too long")
            }
            sleepIfRecord(1000)
        }

        snapshot = bc.createSnapshot().getSnapshotId()
        match = setupBlobMatchCondition(bu2, match)
        def mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)

        expect:
        bu2.copyIncrementalWithResponse(bc.getBlobUrl(), snapshot, mac, null, null).getStatusCode() == 202

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
        def bu2 = cc.getBlobClient(generateBlobName()).getPageBlobClient()
        def snapshot = bc.createSnapshot().getSnapshotId()
        bu2.copyIncremental(bc.getBlobUrl(), snapshot)
        snapshot = bc.createSnapshot().getSnapshotId()
        noneMatch = setupBlobMatchCondition(bu2, noneMatch)
        def mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)

        when:
        bu2.copyIncrementalWithResponse(bc.getBlobUrl(), snapshot, mac, null, null)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match       | noneMatch
        newDate  | null       | null        | null
        null     | oldDate    | null        | null
        null     | null       | garbageEtag | null
        null     | null       | null        | receivedEtag
    }

    def "Start incremental copy error"() {
        setup:
        bc = cc.getBlobClient(generateBlobName()).getPageBlobClient()

        when:
        bc.copyIncremental("https://www.error.com", "snapshot")

        then:
        thrown(BlobStorageException)
    }

    def "Get Container Name"() {
        expect:
        containerName == bc.getContainerName()
    }

    def "Get Page Blob Name"() {
        expect:
        blobName == bc.getBlobName()
    }

    def "Get Blob Name and Build Client"() {
        when:
        def client = cc.getBlobClient(originalBlobName)
        def blockClient = cc.getBlobClient(client.getBlobName()).getPageBlobClient()

        then:
        blockClient.getBlobName() == finalBlobName

        where:
        originalBlobName       | finalBlobName
        "blob"                 | "blob"
        "path/to]a blob"       | "path/to]a blob"
        "path%2Fto%5Da%20blob" | "path/to]a blob"
        "斑點"                 | "斑點"
        "%E6%96%91%E9%BB%9E"   | "斑點"
    }

    def "Create overwrite false"() {
        when:
        bc.create(512)

        then:
        thrown(BlobStorageException)
    }

    def "Create overwrite true"() {
        when:
        bc.create(512, true)

        then:
        notThrown(Throwable)
    }
}
