/*
 * Copyright Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.azure.storage

import com.microsoft.azure.storage.blob.*
import com.microsoft.azure.storage.blob.models.*
import io.reactivex.Flowable
import spock.lang.Unroll

import java.security.MessageDigest

class PageBlobAPITest extends APISpec {
    PageBlobURL bu

    def setup() {
        bu = cu.createPageBlobURL(generateBlobName())
        bu.create(PageBlobURL.PAGE_BYTES, null, null, null, null).blockingGet()
    }

    def "Create all null"() {
        setup:
        bu = cu.createPageBlobURL(generateBlobName())

        when:
        PageBlobCreateResponse response =
                bu.create(PageBlobURL.PAGE_BYTES, null, null, null,
                        null).blockingGet()

        then:
        response.statusCode() == 201
        validateBasicHeaders(response.headers())
        response.headers().contentMD5() == null
        response.headers().isServerEncrypted()
    }

    def "Create sequence number"() {
        when:
        bu.create(PageBlobURL.PAGE_BYTES, 2, null, null,
                null).blockingGet()

        then:
        bu.getProperties(null).blockingGet().headers().blobSequenceNumber() == 2
    }

    @Unroll
    def "Create headers"() {
        setup:
        BlobHTTPHeaders headers = new BlobHTTPHeaders(cacheControl, contentDisposition, contentEncoding,
                contentLanguage, contentMD5, contentType)

        when:
        bu.create(PageBlobURL.PAGE_BYTES, null, headers, null, null)
                .blockingGet()
        BlobGetPropertiesResponse response = bu.getProperties(null).blockingGet()

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
        bu.create(PageBlobURL.PAGE_BYTES, null, null, metadata, null)
                .blockingGet()
        BlobGetPropertiesResponse response = bu.getProperties(null).blockingGet()

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
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null, null)


        expect:
        bu.create(PageBlobURL.PAGE_BYTES, null, null, null, bac).blockingGet()
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
        bu.create(PageBlobURL.PAGE_BYTES, null, null, null,
                new BlobAccessConditions(null, new LeaseAccessConditions("id"),
                        null, null)).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Upload page"() {
        when:
        PageBlobUploadPagesResponse response = bu.uploadPages(
                new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1),
                Flowable.just(getRandomData(PageBlobURL.PAGE_BYTES)), null).blockingGet()
        PageBlobUploadPagesHeaders headers = response.headers()

        then:
        response.statusCode() == 201
        validateBasicHeaders(headers)
        headers.contentMD5() != null
        headers.blobSequenceNumber() == 0
        headers.isServerEncrypted()
    }

    @Unroll
    def "Upload page IA"() {
        when:
        bu.uploadPages(new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES * 2 - 1), data,
                null).blockingGet()

        then:
        thrown(IllegalArgumentException)

        where:
        data                                                     | _
        null                                                     | _
        Flowable.just(getRandomData(PageBlobURL.PAGE_BYTES))     | _
        Flowable.just(getRandomData(PageBlobURL.PAGE_BYTES * 3)) | _
    }

    @Unroll
    def "Upload page AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null,
                new PageBlobAccessConditions(sequenceNumberLT, sequenceNumberLTE, sequenceNumberEqual))

        expect:
        bu.uploadPages(new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1),
                Flowable.just(getRandomData(PageBlobURL.PAGE_BYTES)), bac).blockingGet().statusCode() == 201

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
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null,
                new PageBlobAccessConditions(sequenceNumberLT, sequenceNumberLTE, sequenceNumberEqual))

        when:
        bu.uploadPages(new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1),
                Flowable.just(getRandomData(PageBlobURL.PAGE_BYTES)), bac).blockingGet()

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
                new BlobAccessConditions(null, new LeaseAccessConditions("id"),
                        null, null))
                .blockingGet()

        then:
        thrown(StorageException)
    }

    def "Clear page"() {
        setup:
        bu.uploadPages(new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1),
                Flowable.just(getRandomData(PageBlobURL.PAGE_BYTES)), null).blockingGet()

        when:
        PageBlobClearPagesHeaders headers =
                bu.clearPages(new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1),
                        null)
                        .blockingGet().headers()

        then:
        bu.getPageRanges(null, null).blockingGet().body().pageRange().size() == 0
        validateBasicHeaders(headers)
        headers.contentMD5() == null
        headers.blobSequenceNumber() == 0
    }

    @Unroll
    def "Clear pages AC"() {
        setup:
        bu.uploadPages(new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1),
                Flowable.just(getRandomData(PageBlobURL.PAGE_BYTES)), null).blockingGet()
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null,
                new PageBlobAccessConditions(sequenceNumberLT, sequenceNumberLTE, sequenceNumberEqual))

        expect:
        bu.clearPages(new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1), bac).blockingGet()
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
                Flowable.just(getRandomData(PageBlobURL.PAGE_BYTES)), null).blockingGet()
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null,
                new PageBlobAccessConditions(sequenceNumberLT, sequenceNumberLTE, sequenceNumberEqual))

        when:
        bu.clearPages(new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1), bac).blockingGet()

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
        bu.clearPages(new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1), null)
                .blockingGet()

        then:
        thrown(StorageException)
    }

    def "Get page ranges"() {
        setup:
        bu.uploadPages(new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1),
                Flowable.just(getRandomData(PageBlobURL.PAGE_BYTES)), null).blockingGet()

        when:
        PageBlobGetPageRangesResponse response =
                bu.getPageRanges(new BlobRange(0, PageBlobURL.PAGE_BYTES), null).blockingGet()
        PageBlobGetPageRangesHeaders headers = response.headers()

        then:
        response.statusCode() == 200
        response.body().pageRange().size() == 1
        validateBasicHeaders(headers)
        headers.blobContentLength() == PageBlobURL.PAGE_BYTES
    }

    @Unroll
    def "Get page ranges AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null, null)

        expect:
        bu.getPageRanges(new BlobRange(0, PageBlobURL.PAGE_BYTES), bac).blockingGet().statusCode() == 200

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
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null, null)

        when:
        bu.getPageRanges(new BlobRange(0, PageBlobURL.PAGE_BYTES), bac).blockingGet().statusCode()

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
        bu.getPageRanges(null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Get page ranges diff"() {
        setup:
        bu.create(PageBlobURL.PAGE_BYTES * 2, null, null, null, null)
                .blockingGet()
        bu.uploadPages(new PageRange().withStart(PageBlobURL.PAGE_BYTES).withEnd(PageBlobURL.PAGE_BYTES * 2 - 1),
                Flowable.just(getRandomData(PageBlobURL.PAGE_BYTES)),
                null).blockingGet()
        String snapshot = bu.createSnapshot(null, null).blockingGet().headers().snapshot()
        bu.uploadPages(new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1),
                Flowable.just(getRandomData(PageBlobURL.PAGE_BYTES)), null).blockingGet()
        bu.clearPages(new PageRange().withStart(PageBlobURL.PAGE_BYTES).withEnd(PageBlobURL.PAGE_BYTES * 2 - 1),
                null).blockingGet()

        when:
        PageBlobGetPageRangesDiffResponse response =
                bu.getPageRangesDiff(new BlobRange(0, PageBlobURL.PAGE_BYTES * 2), snapshot,
                        null).blockingGet()
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

    @Unroll
    def "Get page ranges diff AC"() {
        setup:
        String snapshot = bu.createSnapshot(null, null).blockingGet().headers().snapshot()
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null, null)

        expect:
        bu.getPageRangesDiff(new BlobRange(0, PageBlobURL.PAGE_BYTES), snapshot, bac)
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
        String snapshot = bu.createSnapshot(null, null).blockingGet().headers().snapshot()
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null, null)

        when:
        bu.getPageRangesDiff(new BlobRange(0, PageBlobURL.PAGE_BYTES), snapshot, bac).blockingGet()

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
        bu.getPageRangesDiff(null, "snapshot", null).blockingGet()

        then:
        thrown(StorageException)
    }

    // Test the serialization of PageRange with illegal bounds
    @Unroll
    def "PageRange IA"() {
        setup:
        def range = new PageRange().withStart(start).withEnd(end)

        when:
        bu.clearPages(range, null)

        then:
        thrown(IllegalArgumentException)

        where:
        start | end
        1 | 1
        -PageBlobURL.PAGE_BYTES    | PageBlobURL.PAGE_BYTES - 1
        0                          | 0
        1                          | PageBlobURL.PAGE_BYTES - 1
        0                          | PageBlobURL.PAGE_BYTES
        PageBlobURL.PAGE_BYTES * 2 | PageBlobURL.PAGE_BYTES - 1
    }

    def "Resize"() {
        setup:
        PageBlobResizeHeaders headers = bu.resize(PageBlobURL.PAGE_BYTES * 2, null).blockingGet()
                .headers()

        expect:
        bu.getProperties(null).blockingGet().headers().contentLength() == PageBlobURL.PAGE_BYTES * 2
        validateBasicHeaders(headers)
        headers.blobSequenceNumber() != null
    }

    @Unroll
    def "Resize AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null, null)

        expect:
        bu.resize(PageBlobURL.PAGE_BYTES * 2, bac).blockingGet().statusCode() == 200

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
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null, null)

        when:
        bu.resize(PageBlobURL.PAGE_BYTES * 2, bac).blockingGet()

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
        bu.resize(0, null).blockingGet()

        then:
        thrown(StorageException)
    }

    @Unroll
    def "Sequence number"() {
        setup:
        PageBlobUpdateSequenceNumberHeaders headers =
                bu.updateSequenceNumber(action, number, null)
                        .blockingGet().headers()

        expect:
        bu.getProperties(null).blockingGet().headers().blobSequenceNumber() == result
        validateBasicHeaders(headers)
        headers.blobSequenceNumber() == result

        where:
        action                             | number || result
        SequenceNumberActionType.UPDATE    | 5      || 5
        SequenceNumberActionType.INCREMENT | null   || 1
        SequenceNumberActionType.MAX       | 2      || 2
    }

    @Unroll
    def "Sequence number AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null, null)

        expect:
        bu.updateSequenceNumber(SequenceNumberActionType.UPDATE, 1, bac).blockingGet()
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
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null, null)

        when:
        bu.updateSequenceNumber(SequenceNumberActionType.UPDATE, 1, bac).blockingGet()

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
        bu.updateSequenceNumber(SequenceNumberActionType.UPDATE, 0, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Start incremental copy"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.BLOB, null, null).blockingGet()
        PageBlobURL bu2 = cu.createPageBlobURL(generateBlobName())
        String snapshot = bu.createSnapshot(null, null).blockingGet().headers().snapshot()
        PageBlobCopyIncrementalHeaders headers = bu2.copyIncremental(bu.toURL(), snapshot, null)
                .blockingGet().headers()

        expect:
        bu2.getProperties(null).blockingGet().headers().isIncrementalCopy()
        bu2.getProperties(null).blockingGet().headers().destinationSnapshot() != null
        validateBasicHeaders(headers)
        headers.copyId() != null
        headers.copyStatus() != null
    }

    @Unroll
    def "Start incremental copy AC"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.BLOB, null, null).blockingGet()
        PageBlobURL bu2 = cu.createPageBlobURL(generateBlobName())
        String snapshot = bu.createSnapshot(null, null).blockingGet().headers().snapshot()
        bu2.copyIncremental(bu.toURL(), snapshot, null).blockingGet()
        snapshot = bu.createSnapshot(null, null).blockingGet().headers().snapshot()
        match = setupBlobMatchCondition(bu2, match)
        def hac = new HTTPAccessConditions(modified, unmodified, match, noneMatch)

        expect:
        bu2.copyIncremental(bu.toURL(), snapshot, hac).blockingGet().statusCode() == 202

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
        cu.setAccessPolicy(PublicAccessType.BLOB, null, null).blockingGet()
        PageBlobURL bu2 = cu.createPageBlobURL(generateBlobName())
        String snapshot = bu.createSnapshot(null, null).blockingGet().headers().snapshot()
        bu2.copyIncremental(bu.toURL(), snapshot, null).blockingGet()
        snapshot = bu.createSnapshot(null, null).blockingGet().headers().snapshot()
        noneMatch = setupBlobMatchCondition(bu2, noneMatch)
        def hac = new HTTPAccessConditions(modified, unmodified, match, noneMatch)

        when:
        bu2.copyIncremental(bu.toURL(), snapshot, hac).blockingGet().statusCode()

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
        bu.copyIncremental(new URL("https://www.error.com"), "snapshot", null)
                .blockingGet()

        then:
        thrown(StorageException)
    }
}
