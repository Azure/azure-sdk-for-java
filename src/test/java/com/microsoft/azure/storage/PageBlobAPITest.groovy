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

import com.microsoft.azure.storage.blob.BlobAccessConditions
import com.microsoft.azure.storage.blob.BlobHTTPHeaders
import com.microsoft.azure.storage.blob.BlobRange
import com.microsoft.azure.storage.blob.HTTPAccessConditions
import com.microsoft.azure.storage.blob.LeaseAccessConditions
import com.microsoft.azure.storage.blob.Metadata
import com.microsoft.azure.storage.blob.PageBlobAccessConditions
import com.microsoft.azure.storage.blob.PageBlobURL
import com.microsoft.azure.storage.blob.StorageException
import com.microsoft.azure.storage.blob.models.BlobGetPropertiesResponse
import com.microsoft.azure.storage.blob.models.PageBlobClearPagesHeaders
import com.microsoft.azure.storage.blob.models.PageBlobCopyIncrementalHeaders
import com.microsoft.azure.storage.blob.models.PageBlobCreateResponse
import com.microsoft.azure.storage.blob.models.PageBlobGetPageRangesDiffHeaders
import com.microsoft.azure.storage.blob.models.PageBlobGetPageRangesDiffResponse
import com.microsoft.azure.storage.blob.models.PageBlobGetPageRangesHeaders
import com.microsoft.azure.storage.blob.models.PageBlobGetPageRangesResponse
import com.microsoft.azure.storage.blob.models.PageBlobResizeHeaders
import com.microsoft.azure.storage.blob.models.PageBlobUpdateSequenceNumberHeaders
import com.microsoft.azure.storage.blob.models.PageBlobUploadPagesHeaders
import com.microsoft.azure.storage.blob.models.PageBlobUploadPagesResponse
import com.microsoft.azure.storage.blob.models.PageRange
import com.microsoft.azure.storage.blob.models.PublicAccessType
import com.microsoft.azure.storage.blob.models.SequenceNumberActionType
import io.reactivex.Flowable
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.security.MessageDigest

class PageBlobAPITest extends APISpec {
    PageBlobURL bu

    def setup() {
        bu = cu.createPageBlobURL(generateBlobName())
        bu.create(512, null, null, null, null).blockingGet()
    }

    def "Page blob create all null"() {
        setup:
        bu = cu.createPageBlobURL(generateBlobName())

        when:
        PageBlobCreateResponse response =
                bu.create(512, null, null, null, null).blockingGet()

        then:
        response.statusCode() == 201
        validateBasicHeaders(response.headers())
        response.headers().contentMD5() == null
        response.headers().isServerEncrypted()
    }

    def "Page blob create sequence number"() {
        when:
        bu.create(512, 2, null, null, null).blockingGet()

        then:
        bu.getProperties(null).blockingGet().headers().blobSequenceNumber() == 2
    }

    @Unroll
    def "Page blob create headers"() {
        setup:
        BlobHTTPHeaders headers = new BlobHTTPHeaders(cacheControl, contentDisposition, contentEncoding,
                contentLanguage, contentMD5, contentType)

        when:
        bu.create(512, null, headers, null, null).blockingGet()
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
    def "Page blob create metadata"() {
        setup:
        Metadata metadata = new Metadata()
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }

        when:
        bu.create(512, null, null, metadata, null).blockingGet()
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
    def "Page blob create AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null, null)


        expect:
        bu.create(512, null, null, null, bac).blockingGet().statusCode() == 201

        where:
        modified | unmodified | match        | noneMatch   | leaseID
        null     | null       | null         | null        | null
        oldDate  | null       | null         | null        | null
        null     | newDate    | null         | null        | null
        null     | null       | receivedEtag | null        | null
        null     | null       | null         | garbageEtag | null
        null     | null       | null         | null        | receivedLeaseID
    }

    def "Page blob create error"() {
        when:
        bu.create(512, null, null, null,
                new BlobAccessConditions(null, new LeaseAccessConditions("id"),
                        null,null)).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Page blob upload page"() {
        when:
        PageBlobUploadPagesResponse response = bu.uploadPages(new PageRange().withStart(0).withEnd(511),
                Flowable.just(getRandomData(512)), null).blockingGet()
        PageBlobUploadPagesHeaders headers = response.headers()

        then:
        response.statusCode() == 201
        validateBasicHeaders(headers)
        headers.contentMD5() != null
        headers.blobSequenceNumber() == 0
        headers.isServerEncrypted()
    }

    @Unroll
    def "Page blob upload page AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null,
                new PageBlobAccessConditions(sequenceNumberLT, sequenceNumberLTE, sequenceNumberEqual))

        expect:
        bu.uploadPages(new PageRange().withStart(0).withEnd(511),
                Flowable.just(getRandomData(512)), bac).blockingGet().statusCode() == 201

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

    def "Page blob upload page error"() {
        setup:
        bu = cu.createPageBlobURL(generateBlobName())

        when:
        bu.uploadPages(new PageRange().withStart(0).withEnd(511), Flowable.just(getRandomData(512)),
                new BlobAccessConditions(null, new LeaseAccessConditions("id"),
                        null, null))
                .blockingGet()

        then:
        thrown(StorageException)
    }

    def "Page blob clear page"() {
        setup:
        bu.uploadPages(new PageRange().withStart(0).withEnd(511),
                Flowable.just(getRandomData(512)), null).blockingGet()

        when:
        PageBlobClearPagesHeaders headers =
                bu.clearPages(new PageRange().withStart(0).withEnd(511), null)
                        .blockingGet().headers()

        then:
        bu.getPageRanges(null, null).blockingGet().body().pageRange().size() == 0
        validateBasicHeaders(headers)
        headers.contentMD5() == null
        headers.blobSequenceNumber() == 0
    }

    @Unroll
    def "Page blob clear pages AC"() {
        setup:
        bu.uploadPages(new PageRange().withStart(0).withEnd(511),
                Flowable.just(getRandomData(512)), null).blockingGet()
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null,
                new PageBlobAccessConditions(sequenceNumberLT, sequenceNumberLTE, sequenceNumberEqual))

        expect:
        bu.clearPages(new PageRange().withStart(0).withEnd(511), bac).blockingGet().statusCode() == 201

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

    def "Page blob clear page error"() {
        setup:
        bu = cu.createPageBlobURL(generateBlobName())

        when:
        bu.clearPages(new PageRange().withStart(0).withEnd(511), null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Page blob get page ranges"() {
        setup:
        bu.uploadPages(new PageRange().withStart(0).withEnd(511),
                Flowable.just(getRandomData(512)), null).blockingGet()

        when:
        PageBlobGetPageRangesResponse response =
                bu.getPageRanges(new BlobRange(0, 512), null).blockingGet()
        PageBlobGetPageRangesHeaders headers = response.headers()

        then:
        response.statusCode() == 200
        response.body().pageRange().size() == 1
        validateBasicHeaders(headers)
        headers.blobContentLength() == 512
    }

    @Unroll
    def "Page blob get page ranges AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null,
                new PageBlobAccessConditions(sequenceNumberLT, sequenceNumberLTE, sequenceNumberEqual))

        expect:
        bu.getPageRanges(new BlobRange(0, 512), bac).blockingGet().statusCode() == 200

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

    def "Page blob get page ranges error"() {
        setup:
        bu = cu.createPageBlobURL(generateBlobName())

        when:
        bu.getPageRanges(null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Page blob get page ranges diff"() {
        setup:
        String snapshot = bu.createSnapshot(null, null).blockingGet().headers().snapshot()
        bu.uploadPages(new PageRange().withStart(0).withEnd(511),
                Flowable.just(getRandomData(512)), null).blockingGet()

        when:
        PageBlobGetPageRangesDiffResponse response =
                bu.getPageRangesDiff(new BlobRange(0, 512), snapshot, null).blockingGet()
        PageBlobGetPageRangesDiffHeaders headers = response.headers()

        then:
        response.body().pageRange().size() == 1
        validateBasicHeaders(headers)
        headers.blobContentLength() == 512
    }

    @Unroll
    def "Page blob get page ranges diff AC"() {
        setup:
        String snapshot = bu.createSnapshot(null, null).blockingGet().headers().snapshot()
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null,
                new PageBlobAccessConditions(sequenceNumberLT, sequenceNumberLTE, sequenceNumberEqual))

        expect:
        bu.getPageRangesDiff(new BlobRange(0, 512), snapshot, bac).blockingGet().statusCode() == 200

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

    def "Page blob get page ranges diff error"() {
        setup:
        bu = cu.createPageBlobURL(generateBlobName())

        when:
        bu.getPageRangesDiff(null, "snapshot", null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Page blob resize"() {
        setup:
        PageBlobResizeHeaders headers = bu.resize(1024, null).blockingGet().headers()

        expect:
        bu.getProperties(null).blockingGet().headers().contentLength() == 1024
        validateBasicHeaders(headers)
        headers.blobSequenceNumber() != null
    }

    @Unroll
    def "Page blob resize AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null,
                new PageBlobAccessConditions(sequenceNumberLT, sequenceNumberLTE, sequenceNumberEqual))

        expect:
        bu.resize(1024, bac).blockingGet().statusCode() == 200

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

    def "Page blob resize error"() {
        setup:
        bu = cu.createPageBlobURL(generateBlobName())

        when:
        bu.resize(0, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Page blob sequence number"() {
        setup:
        PageBlobUpdateSequenceNumberHeaders headers =
                bu.updateSequenceNumber(SequenceNumberActionType.UPDATE, 5, null)
                        .blockingGet().headers()

        expect:
        bu.getProperties(null).blockingGet().headers().blobSequenceNumber() == 5
        validateBasicHeaders(headers)
        headers.blobSequenceNumber() == 5
    }

    @Unroll
    def "Page blob sequence number AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null,
                new PageBlobAccessConditions(sequenceNumberLT, sequenceNumberLTE, sequenceNumberEqual))

        expect:
        bu.updateSequenceNumber(SequenceNumberActionType.UPDATE, 1, bac).blockingGet()
                .statusCode() == 200

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

    def "Page blob sequence number error"() {
        setup:
        bu = cu.createPageBlobURL(generateBlobName())

        when:
        bu.updateSequenceNumber(SequenceNumberActionType.UPDATE, 0, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Page blob start incremental copy"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.BLOB, null, null).blockingGet()
        PageBlobURL bu2 = cu.createPageBlobURL(generateBlobName())
        String snapshot = bu.createSnapshot(null, null).blockingGet().headers().snapshot()
        PageBlobCopyIncrementalHeaders headers = bu2.copyIncremental(bu.toURL(), snapshot, null)
                .blockingGet().headers()

        expect:
        bu2.getProperties(null).blockingGet().headers().isIncrementalCopy()
        validateBasicHeaders(headers)
        headers.copyId() != null
        headers.copyStatus() != null
    }

    @Unroll
    def "Page blob start incremental copy AC"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.BLOB, null, null).blockingGet()
        PageBlobURL bu2 = cu.createPageBlobURL(generateBlobName())
        String snapshot = bu.createSnapshot(null, null).blockingGet().headers().snapshot()

        expect:
        bu2.copyIncremental(bu.toURL(), snapshot, null).blockingGet().statusCode() == 202

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

    def "Page blob start incremental copy er"() {
        setup:
        bu = cu.createPageBlobURL(generateBlobName())

        when:
        bu.copyIncremental(new URL("https://www.error.com"), "snapshot", null)
                .blockingGet()

        then:
        thrown(StorageException)
    }
}
