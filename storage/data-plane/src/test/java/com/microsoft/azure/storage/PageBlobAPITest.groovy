// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage

import com.microsoft.azure.storage.blob.*
import com.microsoft.azure.storage.blob.models.*
import com.microsoft.rest.v2.http.HttpPipeline
import com.microsoft.rest.v2.http.UnexpectedLengthException
import com.microsoft.rest.v2.util.FlowableUtil
import io.reactivex.Flowable
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.security.MessageDigest
import java.sql.Blob

class PageBlobAPITest extends APISpec {
    PageBlobURL bu

    def setup() {
        bu = cu.createPageBlobURL(generateBlobName())
        bu.create(PageBlobURL.PAGE_BYTES, null, null, null, null, null).blockingGet()
    }

    def "Create all null"() {
        setup:
        bu = cu.createPageBlobURL(generateBlobName())

        when:
        PageBlobCreateResponse response =
                bu.create(PageBlobURL.PAGE_BYTES, null, null, null,
                        null, null).blockingGet()

        then:
        response.statusCode() == 201
        validateBasicHeaders(response.headers())
        response.headers().contentMD5() == null
        response.headers().isServerEncrypted()
    }

    def "Create min"() {
        expect:
        bu.create(PageBlobURL.PAGE_BYTES).blockingGet().statusCode() == 201
    }

    def "Create sequence number"() {
        when:
        bu.create(PageBlobURL.PAGE_BYTES, 2, null, null,
                null, null).blockingGet()

        then:
        bu.getProperties(null, null).blockingGet().headers().blobSequenceNumber() == 2
    }

    @Unroll
    def "Create headers"() {
        setup:
        BlobHTTPHeaders headers = new BlobHTTPHeaders().withBlobCacheControl(cacheControl)
                .withBlobContentDisposition(contentDisposition)
                .withBlobContentEncoding(contentEncoding)
                .withBlobContentLanguage(contentLanguage)
                .withBlobContentMD5(contentMD5)
                .withBlobContentType(contentType)

        when:
        bu.create(PageBlobURL.PAGE_BYTES, null, headers, null, null, null)
                .blockingGet()
        BlobGetPropertiesResponse response = bu.getProperties(null, null).blockingGet()

        then:
        validateBlobHeaders(response.headers(), cacheControl, contentDisposition, contentEncoding, contentLanguage,
                contentMD5, contentType == null ? "application/octet-stream" : contentType)
        // HTTP default content type is application/octet-stream

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
        bu.create(PageBlobURL.PAGE_BYTES, null, null, metadata, null, null)
                .blockingGet()
        BlobGetPropertiesResponse response = bu.getProperties(null, null).blockingGet()

        then:
        response.statusCode() == 200
        response.headers().metadata() == metadata

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
        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))


        expect:
        bu.create(PageBlobURL.PAGE_BYTES, null, null, null, bac, null).blockingGet()
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
        bu.create(PageBlobURL.PAGE_BYTES, null, null, null, new BlobAccessConditions().withLeaseAccessConditions(
                new LeaseAccessConditions().withLeaseId("id")), null).blockingGet()


        then:
        thrown(StorageException)
    }

    def "Create context"() {
        setup:
        def pipeline =
                HttpPipeline.build(getStubFactory(getContextStubPolicy(201, PageBlobCreateHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        bu.create(512, null, null, null, null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    def "Upload page"() {
        when:
        PageBlobUploadPagesResponse response = bu.uploadPages(
                new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1),
                Flowable.just(getRandomData(PageBlobURL.PAGE_BYTES)), null, null).blockingGet()
        PageBlobUploadPagesHeaders headers = response.headers()

        then:
        response.statusCode() == 201
        validateBasicHeaders(headers)
        headers.contentMD5() != null
        headers.blobSequenceNumber() == 0
        headers.isServerEncrypted()
    }

    def "Upload page min"() {
        expect:
        bu.uploadPages(new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1),
                Flowable.just(getRandomData(PageBlobURL.PAGE_BYTES)))
    }

    @Unroll
    def "Upload page IA"() {
        when:
        bu.uploadPages(new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES * 2 - 1), data,
                null, null).blockingGet()

        then:
        def e = thrown(Exception)
        exceptionType.isInstance(e)

        where:
        data                                                     | exceptionType
        null                                                     | IllegalArgumentException
        Flowable.just(getRandomData(PageBlobURL.PAGE_BYTES))     | UnexpectedLengthException
        Flowable.just(getRandomData(PageBlobURL.PAGE_BYTES * 3)) | UnexpectedLengthException
    }

    @Unroll
    def "Upload page AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        PageBlobAccessConditions pac = new PageBlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))
                .withSequenceNumberAccessConditions(new SequenceNumberAccessConditions()
                .withIfSequenceNumberLessThan(sequenceNumberLT).withIfSequenceNumberLessThanOrEqualTo(sequenceNumberLTE)
                .withIfSequenceNumberEqualTo(sequenceNumberEqual))

        expect:
        bu.uploadPages(new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1),
                Flowable.just(getRandomData(PageBlobURL.PAGE_BYTES)), pac, null).blockingGet().statusCode() == 201

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
        PageBlobAccessConditions pac = new PageBlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))
                .withSequenceNumberAccessConditions(new SequenceNumberAccessConditions()
                .withIfSequenceNumberLessThan(sequenceNumberLT).withIfSequenceNumberLessThanOrEqualTo(sequenceNumberLTE)
                .withIfSequenceNumberEqualTo(sequenceNumberEqual))

        when:
        bu.uploadPages(new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1),
                Flowable.just(getRandomData(PageBlobURL.PAGE_BYTES)), pac, null).blockingGet()

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
        bu = cu.createPageBlobURL(generateBlobName())

        when:
        bu.uploadPages(new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1),
                Flowable.just(getRandomData(PageBlobURL.PAGE_BYTES)),
                new PageBlobAccessConditions().withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId("id")),
                null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Upload page context"() {
        setup:
        def pipeline =
                HttpPipeline.build(getStubFactory(getContextStubPolicy(201, PageBlobUploadPagesHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        bu.uploadPages(new PageRange().withStart(0).withEnd(511), defaultFlowable, null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    def "Upload page from URL min"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null, null, null).blockingGet()
        def destURL = cu.createPageBlobURL(generateBlobName())
        destURL.create(PageBlobURL.PAGE_BYTES).blockingGet()
        destURL.uploadPages(new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1),
                Flowable.just(getRandomData(PageBlobURL.PAGE_BYTES))).blockingGet()
        def pageRange = new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1)

        when:
        PageBlobUploadPagesFromURLResponse response = bu.uploadPagesFromURL(pageRange, destURL.toURL(), null).blockingGet()

        then:
        response.statusCode() == 201
        validateBasicHeaders(response.headers())
    }

    def "Upload page from URL range"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null, null, null).blockingGet()

        def data = getRandomData(PageBlobURL.PAGE_BYTES * 4).array()

        def sourceURL = cu.createPageBlobURL(generateBlobName())
        sourceURL.create(PageBlobURL.PAGE_BYTES * 4).blockingGet()
        sourceURL.uploadPages(new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES * 4 - 1),
            Flowable.just(ByteBuffer.wrap(data))).blockingGet()

        def destURL = cu.createPageBlobURL(generateBlobName())
        destURL.create(PageBlobURL.PAGE_BYTES * 2).blockingGet()

        when:
        destURL.uploadPagesFromURL(new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES * 2 - 1),
                sourceURL.toURL(), PageBlobURL.PAGE_BYTES * 2).blockingGet()

        then:
        ByteBuffer body = FlowableUtil.collectBytesInBuffer(destURL.download().blockingGet().body(null)).blockingGet()
        body.array() == Arrays.copyOfRange(data, PageBlobURL.PAGE_BYTES * 2, PageBlobURL.PAGE_BYTES * 4)
    }

    @Unroll
    def "Upload page from URL IA"() {
        when:
        bu.uploadPagesFromURL(range, bu.toURL(), sourceOffset).blockingGet()

        then:
        thrown(IllegalArgumentException)

        where:
        sourceOffset                                                     | range
        (Long)PageBlobURL.PAGE_BYTES                                     | null
    }

    def "Upload page from URL MD5"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null, null, null).blockingGet()
        def destURL = cu.createPageBlobURL(generateBlobName())
        destURL.create(PageBlobURL.PAGE_BYTES).blockingGet()
        def data = getRandomData(PageBlobURL.PAGE_BYTES).array()
        def pageRange = new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1)
        bu.uploadPages(pageRange, Flowable.just(ByteBuffer.wrap(data))).blockingGet()

        when:
        destURL.uploadPagesFromURL(pageRange, bu.toURL(), null, MessageDigest.getInstance("MD5").digest(data),
            null, null, null).blockingGet()

        then:
        notThrown(StorageException)
    }

    def "Upload page from URL MD5 fail"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null, null, null).blockingGet()
        def destURL = cu.createPageBlobURL(generateBlobName())
        destURL.create(PageBlobURL.PAGE_BYTES).blockingGet()
        def pageRange = new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1)
        bu.uploadPages(pageRange, Flowable.just(getRandomData(PageBlobURL.PAGE_BYTES))).blockingGet()

        when:
        destURL.uploadPagesFromURL(pageRange, bu.toURL(), null,
                MessageDigest.getInstance("MD5").digest("garbage".getBytes()), null, null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Upload page from URL context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(201, PageBlobUploadPagesFromURLHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        def pageRange = new PageRange().withStart(0).withEnd(511)
        bu.uploadPagesFromURL(pageRange, bu.toURL(), null, null, null, null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    @Unroll
    def  "Upload page from URL destination AC"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null, null, null).blockingGet()
        def sourceURL = cu.createPageBlobURL(generateBlobName())
        sourceURL.create(PageBlobURL.PAGE_BYTES).blockingGet()
        def pageRange = new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1)
        sourceURL.uploadPages(pageRange, Flowable.just(getRandomData(PageBlobURL.PAGE_BYTES))).blockingGet()

        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        def pac = new PageBlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))
                .withSequenceNumberAccessConditions(new SequenceNumberAccessConditions()
                .withIfSequenceNumberLessThan(sequenceNumberLT).withIfSequenceNumberLessThanOrEqualTo(sequenceNumberLTE)
                .withIfSequenceNumberEqualTo(sequenceNumberEqual))

        expect:
        bu.uploadPagesFromURL(pageRange, sourceURL.toURL(), null, null, pac, null, null).blockingGet().statusCode() == 201

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
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null, null, null).blockingGet()

        def sourceURL = cu.createPageBlobURL(generateBlobName())
        sourceURL.create(PageBlobURL.PAGE_BYTES).blockingGet()
        def pageRange = new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1)
        sourceURL.uploadPages(pageRange, Flowable.just(getRandomData(PageBlobURL.PAGE_BYTES))).blockingGet()

        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        def pac = new PageBlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))
                .withSequenceNumberAccessConditions(new SequenceNumberAccessConditions()
                .withIfSequenceNumberLessThan(sequenceNumberLT).withIfSequenceNumberLessThanOrEqualTo(sequenceNumberLTE)
                .withIfSequenceNumberEqualTo(sequenceNumberEqual))

        when:
        bu.uploadPagesFromURL(pageRange, sourceURL.toURL(), null, null, pac, null, null).blockingGet()

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
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null, null, null).blockingGet()
        def sourceURL = cu.createPageBlobURL(generateBlobName())
        sourceURL.create(PageBlobURL.PAGE_BYTES).blockingGet()
        def pageRange = new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1)
        sourceURL.uploadPages(pageRange, Flowable.just(getRandomData(PageBlobURL.PAGE_BYTES))).blockingGet()

        sourceIfMatch = setupBlobMatchCondition(sourceURL, sourceIfMatch)
        def smac = new SourceModifiedAccessConditions()
            .withSourceIfModifiedSince(sourceIfModifiedSince)
            .withSourceIfUnmodifiedSince(sourceIfUnmodifiedSince)
            .withSourceIfMatch(sourceIfMatch)
            .withSourceIfNoneMatch(sourceIfNoneMatch)

        expect:
        bu.uploadPagesFromURL(pageRange, sourceURL.toURL(), null, null, null, smac, null).blockingGet().statusCode() == 201

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
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null, null, null).blockingGet()
        def sourceURL = cu.createPageBlobURL(generateBlobName())
        sourceURL.create(PageBlobURL.PAGE_BYTES).blockingGet()
        def pageRange = new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1)
        sourceURL.uploadPages(pageRange, Flowable.just(getRandomData(PageBlobURL.PAGE_BYTES))).blockingGet()

        sourceIfNoneMatch = setupBlobMatchCondition(sourceURL, sourceIfNoneMatch)
        def smac = new SourceModifiedAccessConditions()
                .withSourceIfModifiedSince(sourceIfModifiedSince)
                .withSourceIfUnmodifiedSince(sourceIfUnmodifiedSince)
                .withSourceIfMatch(sourceIfMatch)
                .withSourceIfNoneMatch(sourceIfNoneMatch)

        when:
        bu.uploadPagesFromURL(pageRange, sourceURL.toURL(), null, null, null, smac, null).blockingGet()

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
        bu.uploadPages(new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1),
                Flowable.just(getRandomData(PageBlobURL.PAGE_BYTES)), null, null).blockingGet()

        when:
        PageBlobClearPagesHeaders headers =
                bu.clearPages(new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1),
                        null, null)
                        .blockingGet().headers()

        then:
        bu.getPageRanges(null, null, null).blockingGet().body().pageRange().size() == 0
        validateBasicHeaders(headers)
        headers.contentMD5() == null
        headers.blobSequenceNumber() == 0
    }

    def "Clear page min"() {
        expect:
        bu.clearPages(new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1))
    }

    @Unroll
    def "Clear pages AC"() {
        setup:
        bu.uploadPages(new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1),
                Flowable.just(getRandomData(PageBlobURL.PAGE_BYTES)), null, null).blockingGet()
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        PageBlobAccessConditions pac = new PageBlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))
                .withSequenceNumberAccessConditions(new SequenceNumberAccessConditions()
                .withIfSequenceNumberLessThan(sequenceNumberLT).withIfSequenceNumberLessThanOrEqualTo(sequenceNumberLTE)
                .withIfSequenceNumberEqualTo(sequenceNumberEqual))

        expect:
        bu.clearPages(new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1), pac, null).blockingGet()
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
        bu.uploadPages(new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1),
                Flowable.just(getRandomData(PageBlobURL.PAGE_BYTES)), null, null).blockingGet()
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        setupBlobLeaseCondition(bu, leaseID)
        PageBlobAccessConditions pac = new PageBlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))
                .withSequenceNumberAccessConditions(new SequenceNumberAccessConditions()
                .withIfSequenceNumberLessThan(sequenceNumberLT).withIfSequenceNumberLessThanOrEqualTo(sequenceNumberLTE)
                .withIfSequenceNumberEqualTo(sequenceNumberEqual))


        when:
        bu.clearPages(new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1), pac, null).blockingGet()

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
        bu = cu.createPageBlobURL(generateBlobName())

        when:
        bu.clearPages(new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1), null, null)
                .blockingGet()

        then:
        thrown(StorageException)
    }

    def "Clear page context"() {
        setup:
        def pipeline =
                HttpPipeline.build(getStubFactory(getContextStubPolicy(201, PageBlobClearPagesHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        bu.clearPages(new PageRange().withStart(0).withEnd(511), null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    def "Get page ranges"() {
        setup:
        bu.uploadPages(new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1),
                Flowable.just(getRandomData(PageBlobURL.PAGE_BYTES)), null, null).blockingGet()

        when:
        PageBlobGetPageRangesResponse response =
                bu.getPageRanges(new BlobRange().withCount(PageBlobURL.PAGE_BYTES), null, null).blockingGet()
        PageBlobGetPageRangesHeaders headers = response.headers()

        then:
        response.statusCode() == 200
        response.body().pageRange().size() == 1
        validateBasicHeaders(headers)
        headers.blobContentLength() == (long) PageBlobURL.PAGE_BYTES
    }

    def "Get page ranges min"() {
        expect:
        bu.getPageRanges(null).blockingGet().statusCode() == 200
    }

    @Unroll
    def "Get page ranges AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))

        expect:
        bu.getPageRanges(new BlobRange().withCount(PageBlobURL.PAGE_BYTES), bac, null).blockingGet().statusCode() == 200

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
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))

        when:
        bu.getPageRanges(new BlobRange().withCount(PageBlobURL.PAGE_BYTES), bac, null).blockingGet().statusCode()

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
        bu = cu.createPageBlobURL(generateBlobName())

        when:
        bu.getPageRanges(null, null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Get page ranges context"() {
        setup:
        def pipeline =
                HttpPipeline.build(getStubFactory(getContextStubPolicy(200, PageBlobGetPageRangesHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        bu.getPageRanges(null, null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    def "Get page ranges diff"() {
        setup:
        bu.create(PageBlobURL.PAGE_BYTES * 2, null, null, null, null, null)
                .blockingGet()
        bu.uploadPages(new PageRange().withStart(PageBlobURL.PAGE_BYTES).withEnd(PageBlobURL.PAGE_BYTES * 2 - 1),
                Flowable.just(getRandomData(PageBlobURL.PAGE_BYTES)),
                null, null).blockingGet()
        String snapshot = bu.createSnapshot(null, null, null).blockingGet().headers().snapshot()
        bu.uploadPages(new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1),
                Flowable.just(getRandomData(PageBlobURL.PAGE_BYTES)), null, null).blockingGet()
        bu.clearPages(new PageRange().withStart(PageBlobURL.PAGE_BYTES).withEnd(PageBlobURL.PAGE_BYTES * 2 - 1),
                null, null).blockingGet()

        when:
        PageBlobGetPageRangesDiffResponse response =
                bu.getPageRangesDiff(new BlobRange().withCount(PageBlobURL.PAGE_BYTES * 2), snapshot,
                        null, null).blockingGet()
        PageBlobGetPageRangesDiffHeaders headers = response.headers()

        then:
        response.body().pageRange().size() == 1
        response.body().pageRange().get(0).start() == 0
        response.body().pageRange().get(0).end() == PageBlobURL.PAGE_BYTES - 1
        response.body().clearRange().size() == 1
        response.body().clearRange().get(0).start() == PageBlobURL.PAGE_BYTES
        response.body().clearRange().get(0).end() == PageBlobURL.PAGE_BYTES * 2 - 1
        validateBasicHeaders(headers)
        headers.blobContentLength() == PageBlobURL.PAGE_BYTES * 2
    }

    def "Get page ranges diff min"() {
        setup:
        def snapshot = bu.createSnapshot().blockingGet().headers().snapshot()

        expect:
        bu.getPageRangesDiff(null, snapshot).blockingGet().statusCode() == 200
    }

    @Unroll
    def "Get page ranges diff AC"() {
        setup:
        String snapshot = bu.createSnapshot(null, null, null).blockingGet().headers().snapshot()
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))

        expect:
        bu.getPageRangesDiff(new BlobRange().withCount(PageBlobURL.PAGE_BYTES), snapshot, bac, null)
                .blockingGet().statusCode() == 200

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
        String snapshot = bu.createSnapshot(null, null, null).blockingGet().headers().snapshot()
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))

        when:
        bu.getPageRangesDiff(new BlobRange().withCount(PageBlobURL.PAGE_BYTES), snapshot, bac, null).blockingGet()

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
        bu = cu.createPageBlobURL(generateBlobName())

        when:
        bu.getPageRangesDiff(null, "snapshot", null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Get page ranges diff context"() {
        setup:
        def pipeline =
                HttpPipeline.build(getStubFactory(getContextStubPolicy(200, PageBlobGetPageRangesDiffHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        bu.getPageRangesDiff(null, "snapshot", null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    // Test the serialization of PageRange with illegal bounds
    @Unroll
    def "PageRange IA"() {
        setup:
        def range = new PageRange().withStart(start).withEnd(end)

        when:
        bu.clearPages(range, null, null)

        then:
        thrown(IllegalArgumentException)

        where:
        start                      | end
        1                          | 1
        -PageBlobURL.PAGE_BYTES    | PageBlobURL.PAGE_BYTES - 1
        0                          | 0
        1                          | PageBlobURL.PAGE_BYTES - 1
        0                          | PageBlobURL.PAGE_BYTES
        PageBlobURL.PAGE_BYTES * 2 | PageBlobURL.PAGE_BYTES - 1
    }

    def "Resize"() {
        setup:
        PageBlobResizeHeaders headers = bu.resize(PageBlobURL.PAGE_BYTES * 2, null, null).blockingGet()
                .headers()

        expect:
        bu.getProperties(null, null).blockingGet().headers().contentLength() == PageBlobURL.PAGE_BYTES * 2
        validateBasicHeaders(headers)
        headers.blobSequenceNumber() != null
    }

    def "Resize min"() {
        expect:
        bu.resize(PageBlobURL.PAGE_BYTES).blockingGet().statusCode() == 200
    }

    @Unroll
    def "Resize AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))

        expect:
        bu.resize(PageBlobURL.PAGE_BYTES * 2, bac, null).blockingGet().statusCode() == 200

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
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))

        when:
        bu.resize(PageBlobURL.PAGE_BYTES * 2, bac, null).blockingGet()

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
        bu = cu.createPageBlobURL(generateBlobName())

        when:
        bu.resize(0, null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Resize context"() {
        setup:
        def pipeline =
                HttpPipeline.build(getStubFactory(getContextStubPolicy(200, PageBlobResizeHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        bu.resize(512, null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    @Unroll
    def "Sequence number"() {
        setup:
        PageBlobUpdateSequenceNumberHeaders headers =
                bu.updateSequenceNumber(action, number, null, null)
                        .blockingGet().headers()

        expect:
        bu.getProperties(null, null).blockingGet().headers().blobSequenceNumber() == result
        validateBasicHeaders(headers)
        headers.blobSequenceNumber() == result

        where:
        action                             | number || result
        SequenceNumberActionType.UPDATE    | 5      || 5
        SequenceNumberActionType.INCREMENT | null   || 1
        SequenceNumberActionType.MAX       | 2      || 2
    }

    def "Sequence number min"() {
        expect:
        bu.updateSequenceNumber(SequenceNumberActionType.INCREMENT, null).blockingGet().statusCode() == 200
    }

    @Unroll
    def "Sequence number AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))

        expect:
        bu.updateSequenceNumber(SequenceNumberActionType.UPDATE, 1, bac, null).blockingGet()
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
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))

        when:
        bu.updateSequenceNumber(SequenceNumberActionType.UPDATE, 1, bac, null).blockingGet()

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
        bu = cu.createPageBlobURL(generateBlobName())

        when:
        bu.updateSequenceNumber(SequenceNumberActionType.UPDATE, 0, null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Sequence number context"() {
        setup:
        def pipeline =
                HttpPipeline.build(getStubFactory(getContextStubPolicy(200, PageBlobUpdateSequenceNumberHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        bu.updateSequenceNumber(SequenceNumberActionType.UPDATE, 3, null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    def "Start incremental copy"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.BLOB, null, null, null).blockingGet()
        PageBlobURL bu2 = cu.createPageBlobURL(generateBlobName())
        String snapshot = bu.createSnapshot(null, null, null).blockingGet().headers().snapshot()
        PageBlobCopyIncrementalHeaders headers = bu2.copyIncremental(bu.toURL(), snapshot, null, null)
                .blockingGet().headers()
        waitForCopy(bu2, headers.copyStatus())

        expect:
        bu2.getProperties(null, null).blockingGet().headers().isIncrementalCopy()
        bu2.getProperties(null, null).blockingGet().headers().destinationSnapshot() != null
        validateBasicHeaders(headers)
        headers.copyId() != null
        headers.copyStatus() != null
    }

    def "Start incremental copy min"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.BLOB, null, null, null).blockingGet()
        PageBlobURL bu2 = cu.createPageBlobURL(generateBlobName())
        String snapshot = bu.createSnapshot(null, null, null).blockingGet().headers().snapshot()

        expect:
        bu2.copyIncremental(bu.toURL(), snapshot, null, null).blockingGet().statusCode() == 202
    }

    @Unroll
    def "Start incremental copy AC"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.BLOB, null, null, null).blockingGet()
        PageBlobURL bu2 = cu.createPageBlobURL(generateBlobName())
        String snapshot = bu.createSnapshot(null, null, null).blockingGet().headers().snapshot()
        def response = bu2.copyIncremental(bu.toURL(), snapshot, null, null).blockingGet()
        waitForCopy(bu2, response.headers().copyStatus())
        snapshot = bu.createSnapshot(null, null, null).blockingGet().headers().snapshot()
        match = setupBlobMatchCondition(bu2, match)
        def mac = new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                .withIfMatch(match).withIfNoneMatch(noneMatch)

        expect:
        bu2.copyIncremental(bu.toURL(), snapshot, mac, null).blockingGet().statusCode() == 202

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
        cu.setAccessPolicy(PublicAccessType.BLOB, null, null, null).blockingGet()
        PageBlobURL bu2 = cu.createPageBlobURL(generateBlobName())
        String snapshot = bu.createSnapshot(null, null, null).blockingGet().headers().snapshot()
        bu2.copyIncremental(bu.toURL(), snapshot, null, null).blockingGet()
        snapshot = bu.createSnapshot(null, null, null).blockingGet().headers().snapshot()
        noneMatch = setupBlobMatchCondition(bu2, noneMatch)
        def mac = new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                .withIfMatch(match).withIfNoneMatch(noneMatch)

        when:
        bu2.copyIncremental(bu.toURL(), snapshot, mac, null).blockingGet().statusCode()

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
        bu = cu.createPageBlobURL(generateBlobName())

        when:
        bu.copyIncremental(new URL("https://www.error.com"), "snapshot", null, null)
                .blockingGet()

        then:
        thrown(StorageException)
    }

    def "Start incremental copy context"() {
        setup:
        def pipeline =
                HttpPipeline.build(getStubFactory(getContextStubPolicy(202, PageBlobCopyIncrementalHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        bu.copyIncremental(bu.toURL(), "snapshot", null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }
}
