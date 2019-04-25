// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage

import com.microsoft.azure.storage.blob.*
import com.microsoft.azure.storage.blob.models.*
import com.microsoft.rest.v2.http.HttpPipeline
import com.microsoft.rest.v2.http.HttpRequest
import com.microsoft.rest.v2.policy.RequestPolicy
import com.microsoft.rest.v2.util.FlowableUtil
import io.reactivex.Flowable
import io.reactivex.Single
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.security.MessageDigest

class BlobAPITest extends APISpec {
    BlobURL bu

    def setup() {
        bu = cu.createBlockBlobURL(generateBlobName())
        bu.upload(defaultFlowable, defaultDataSize, null, null,
                null, null).blockingGet()
    }

    def "Download all null"() {
        when:
        DownloadResponse response = bu.download(null, null, false, null)
                .blockingGet()
        ByteBuffer body = FlowableUtil.collectBytesInBuffer(response.body(null)).blockingGet()
        BlobDownloadHeaders headers = response.headers()

        then:
        validateBasicHeaders(headers)
        body == defaultData
        headers.metadata().isEmpty()
        headers.contentLength() != null
        headers.contentType() != null
        headers.contentRange() == null
        headers.contentMD5() != null
        headers.contentEncoding() == null
        headers.cacheControl() == null
        headers.contentDisposition() == null
        headers.contentLanguage() == null
        headers.blobSequenceNumber() == null
        headers.blobType() == BlobType.BLOCK_BLOB
        headers.copyCompletionTime() == null
        headers.copyStatusDescription() == null
        headers.copyId() == null
        headers.copyProgress() == null
        headers.copySource() == null
        headers.copyStatus() == null
        headers.leaseDuration() == null
        headers.leaseState() == LeaseStateType.AVAILABLE
        headers.leaseStatus() == LeaseStatusType.UNLOCKED
        headers.acceptRanges() == "bytes"
        headers.blobCommittedBlockCount() == null
        headers.serverEncrypted
        headers.blobContentMD5() == null
    }

    def "Download empty file"() {
        setup:
        bu = cu.createAppendBlobURL("emptyAppendBlob")
        bu.create().blockingGet()

        when:
        def result = FlowableUtil.collectBytesInBuffer(bu.download(new BlobRange().withOffset(0), null, false, null).blockingGet().body(null)).blockingGet()

        then:
        notThrown(StorageException)
        result.remaining() == 0
    }

    /*
    This is to test the appropriate integration of DownloadResponse, including setting the correct range values on
    HTTPGetterInfo.
     */
    def "Download with retry range"() {
        /*
        We are going to make a request for some range on a blob. The Flowable returned will throw an exception, forcing
        a retry per the ReliableDownloadOptions. The next request should have the same range header, which was generated
        from the count and offset values in HTTPGetterInfo that was constructed on the initial call to download. We
        don't need to check the data here, but we want to ensure that the correct range is set each time. This will
        test the correction of a bug that was found which caused HTTPGetterInfo to have an incorrect offset when it was
        constructed in BlobURL.download().
         */
        setup:
        def mockPolicy = Mock(RequestPolicy) {
            sendAsync(_) >> { HttpRequest request ->
                if (request.headers().value("x-ms-range") != "bytes=2-6") {
                    return Single.error(new IllegalArgumentException("The range header was not set correctly on retry."))
                }
                else {
                    // ETag can be a dummy value. It's not validated, but DownloadResponse requires one
                    return Single.just(getStubResponseForBlobDownload(206, Flowable.error(new IOException()), "etag"))
                }
            }
        }
        def pipeline = HttpPipeline.build(getStubFactory(mockPolicy))
        bu = bu.withPipeline(pipeline)

        when:
        def range = new BlobRange().withOffset(2).withCount(5)
        bu.download(range, null, false, null).blockingGet().body(new ReliableDownloadOptions().withMaxRetryRequests(3))
                .blockingSubscribe()

        then:
        /*
        Because the dummy Flowable always throws an error. This will also validate that an IllegalArgumentException is
        NOT thrown because the types would not match.
         */
        def e = thrown(RuntimeException)
        e.getCause() instanceof IOException
    }

    def "Download min"() {
        expect:
        FlowableUtil.collectBytesInBuffer(bu.download().blockingGet().body(null)).blockingGet() == defaultData
    }

    @Unroll
    def "Download range"() {
        setup:
        BlobRange range = new BlobRange().withOffset(offset).withCount(count)

        when:
        ByteBuffer body = FlowableUtil.collectBytesInBuffer(
                bu.download(range, null, false, null).blockingGet().body(null)).blockingGet()
        String bodyStr = new String(body.array())

        then:
        bodyStr == expectedData

        where:
        offset | count || expectedData
        0      | null  || defaultText
        0      | 5     || defaultText.substring(0, 5)
        3      | 2     || defaultText.substring(3, 3 + 2)
    }

    @Unroll
    def "Download AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))

        expect:
        bu.download(null, bac, false, null).blockingGet().statusCode() == 200

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
    def "Download AC fail"() {
        setup:
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))

        when:
        bu.download(null, bac, false, null).blockingGet().statusCode() == 206

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

    def "Download md5"() {
        expect:
        bu.download(new BlobRange().withOffset(0).withCount(3), null, true, null).blockingGet()
                .headers().contentMD5() ==
                MessageDigest.getInstance("MD5").digest(defaultText.substring(0, 3).getBytes())
    }

    def "Download error"() {
        setup:
        bu = cu.createBlockBlobURL(generateBlobName())

        when:
        bu.download(null, null, false, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Download context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(206, BlobDownloadHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        bu.download(null, null, false, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    def "Get properties default"() {
        when:
        BlobGetPropertiesHeaders headers = bu.getProperties(null, null).blockingGet().headers()

        then:
        validateBasicHeaders(headers)
        headers.metadata().isEmpty()
        headers.blobType() == BlobType.BLOCK_BLOB
        headers.copyCompletionTime() == null // tested in "copy"
        headers.copyStatusDescription() == null // only returned when the service has errors; cannot validate.
        headers.copyId() == null // tested in "abort copy"
        headers.copyProgress() == null // tested in "copy"
        headers.copySource() == null // tested in "copy"
        headers.copyStatus() == null // tested in "copy"
        headers.isIncrementalCopy() == null // tested in PageBlob."start incremental copy"
        headers.destinationSnapshot() == null // tested in PageBlob."start incremental copy"
        headers.leaseDuration() == null // tested in "acquire lease"
        headers.leaseState() == LeaseStateType.AVAILABLE
        headers.leaseStatus() == LeaseStatusType.UNLOCKED
        headers.contentLength() != null
        headers.contentType() != null
        headers.contentMD5() != null
        headers.contentEncoding() == null // tested in "set HTTP headers"
        headers.contentDisposition() == null // tested in "set HTTP headers"
        headers.contentLanguage() == null // tested in "set HTTP headers"
        headers.cacheControl() == null // tested in "set HTTP headers"
        headers.blobSequenceNumber() == null // tested in PageBlob."create sequence number"
        headers.acceptRanges() == "bytes"
        headers.blobCommittedBlockCount() == null // tested in AppendBlob."append block"
        headers.isServerEncrypted()
        headers.accessTier() == AccessTier.HOT.toString()
        headers.accessTierInferred()
        headers.archiveStatus() == null
        headers.creationTime() != null
    }

    def "Get properties min"() {
        expect:
        bu.getProperties().blockingGet().statusCode() == 200
    }

    @Unroll
    def "Get properties AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))

        expect:
        bu.getProperties(bac, null).blockingGet().statusCode() == 200

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
    def "Get properties AC fail"() {
        setup:
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))
        when:
        bu.getProperties(bac, null).blockingGet()

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

    def "Get properties error"() {
        setup:
        bu = cu.createBlockBlobURL(generateBlobName())

        when:
        bu.getProperties(null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Get properties context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(200, BlobGetPropertiesHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        bu.getProperties(null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    def "Set HTTP headers null"() {
        setup:
        BlobSetHTTPHeadersResponse response = bu.setHTTPHeaders(null, null, null).blockingGet()

        expect:
        response.statusCode() == 200
        validateBasicHeaders(response.headers())
        response.headers().blobSequenceNumber() == null
    }

    def "Set HTTP headers min"() {
        when:
        bu.setHTTPHeaders(new BlobHTTPHeaders().withBlobContentType("type")).blockingGet()

        then:
        bu.getProperties().blockingGet().headers().contentType() == "type"
    }

    @Unroll
    def "Set HTTP headers headers"() {
        setup:
        BlobHTTPHeaders putHeaders = new BlobHTTPHeaders().withBlobCacheControl(cacheControl)
                .withBlobContentDisposition(contentDisposition)
                .withBlobContentEncoding(contentEncoding)
                .withBlobContentLanguage(contentLanguage)
                .withBlobContentMD5(contentMD5)
                .withBlobContentType(contentType)
        bu.setHTTPHeaders(putHeaders, null, null).blockingGet()

        BlobGetPropertiesHeaders receivedHeaders =
                bu.getProperties(null, null).blockingGet().headers()

        expect:
        validateBlobHeaders(receivedHeaders, cacheControl, contentDisposition, contentEncoding, contentLanguage,
                contentMD5, contentType)

        where:
        cacheControl | contentDisposition | contentEncoding | contentLanguage | contentMD5                                                                               | contentType
        null         | null               | null            | null            | null                                                                                     | null
        "control"    | "disposition"      | "encoding"      | "language"      | Base64.getEncoder().encode(MessageDigest.getInstance("MD5").digest(defaultData.array())) | "type"

    }


    @Unroll
    def "Set HTTP headers AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))

        expect:
        bu.setHTTPHeaders(null, bac, null).blockingGet().statusCode() == 200

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
    def "Set HTTP headers AC fail"() {
        setup:
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))

        when:
        bu.setHTTPHeaders(null, bac, null).blockingGet()

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

    def "Set HTTP headers error"() {
        setup:
        bu = cu.createBlockBlobURL(generateBlobName())

        when:
        bu.setHTTPHeaders(null, null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Set HTTP headers context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(200, BlobSetHTTPHeadersHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        bu.setHTTPHeaders(null, null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    def "Set metadata all null"() {
        setup:
        BlobSetMetadataResponse response = bu.setMetadata(null, null, null).blockingGet()

        expect:
        bu.getProperties(null, null).blockingGet().headers().metadata().size() == 0
        response.statusCode() == 200
        validateBasicHeaders(response.headers())
        response.headers().isServerEncrypted()
    }

    def "Set metadata min"() {
        setup:
        Metadata metadata = new Metadata()
        metadata.put("foo", "bar")

        when:
        bu.setMetadata(metadata).blockingGet()

        then:
        bu.getProperties().blockingGet().headers().metadata() == metadata
    }

    @Unroll
    def "Set metadata metadata"() {
        setup:
        Metadata metadata = new Metadata()
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2)
        }

        expect:
        bu.setMetadata(metadata, null, null).blockingGet().statusCode() == statusCode
        bu.getProperties(null, null).blockingGet().headers().metadata() == metadata

        where:
        key1  | value1 | key2   | value2 || statusCode
        null  | null   | null   | null   || 200
        "foo" | "bar"  | "fizz" | "buzz" || 200
    }

    @Unroll
    def "Set metadata AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))

        expect:
        bu.setMetadata(null, bac, null).blockingGet().statusCode() == 200

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
    def "Set metadata AC fail"() {
        setup:
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        setupBlobLeaseCondition(bu, leaseID)

        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))

        when:
        bu.setMetadata(null, bac, null).blockingGet()

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

    def "Set metadata error"() {
        setup:
        bu = cu.createBlockBlobURL(generateBlobName())

        when:
        bu.setMetadata(null, null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Set metadata context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(200, BlobSetMetadataHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        bu.setMetadata(null, null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    @Unroll
    def "Acquire lease"() {
        setup:
        BlobAcquireLeaseHeaders headers =
                bu.acquireLease(proposedID, leaseTime, null, null)
                        .blockingGet().headers()

        when:
        BlobGetPropertiesHeaders properties = bu.getProperties(null, null).blockingGet()
                .headers()

        then:
        properties.leaseState() == leaseState
        properties.leaseDuration() == leaseDuration
        headers.leaseId() != null
        validateBasicHeaders(headers)

        where:
        proposedID                   | leaseTime || leaseState            | leaseDuration
        null                         | -1        || LeaseStateType.LEASED | LeaseDurationType.INFINITE
        null                         | 25        || LeaseStateType.LEASED | LeaseDurationType.FIXED
        UUID.randomUUID().toString() | -1        || LeaseStateType.LEASED | LeaseDurationType.INFINITE
    }

    def "Acquire lease min"() {
        setup:
        bu.acquireLease(null, -1).blockingGet().statusCode() == 201
    }

    @Unroll
    def "Acquire lease AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        def mac = new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                .withIfMatch(match).withIfNoneMatch(noneMatch)

        expect:
        bu.acquireLease(null, -1, mac, null).blockingGet().statusCode() == 201

        where:
        modified | unmodified | match        | noneMatch
        null     | null       | null         | null
        oldDate  | null       | null         | null
        null     | newDate    | null         | null
        null     | null       | receivedEtag | null
        null     | null       | null         | garbageEtag
    }

    @Unroll
    def "Acquire lease AC fail"() {
        setup:
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        def mac = new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                .withIfMatch(match).withIfNoneMatch(noneMatch)

        when:
        bu.acquireLease(null, -1, mac, null).blockingGet()

        then:
        thrown(StorageException)

        where:
        modified | unmodified | match       | noneMatch
        newDate  | null       | null        | null
        null     | oldDate    | null        | null
        null     | null       | garbageEtag | null
        null     | null       | null        | receivedEtag
    }

    def "Acquire lease error"() {
        setup:
        bu = cu.createBlockBlobURL(generateBlobName())

        when:
        bu.acquireLease(null, 20, null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Acquire lease context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(201, BlobAcquireLeaseHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        bu.acquireLease(null, 20, null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    def "Renew lease"() {
        setup:
        String leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)

        Thread.sleep(16000) // Wait for the lease to expire to ensure we are actually renewing it
        BlobRenewLeaseHeaders headers = bu.renewLease(leaseID, null, null).blockingGet().headers()

        expect:
        bu.getProperties(null, null).blockingGet().headers().leaseState()
                .equals(LeaseStateType.LEASED)
        validateBasicHeaders(headers)
        headers.leaseId() != null
    }

    def "Renew lease min"() {
        setup:
        String leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)

        expect:
        bu.renewLease(leaseID).blockingGet().statusCode() == 200
    }

    @Unroll
    def "Renew lease AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        String leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)
        def mac = new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                .withIfMatch(match).withIfNoneMatch(noneMatch)

        expect:
        bu.renewLease(leaseID, mac, null).blockingGet().statusCode() == 200

        where:
        modified | unmodified | match        | noneMatch
        null     | null       | null         | null
        oldDate  | null       | null         | null
        null     | newDate    | null         | null
        null     | null       | receivedEtag | null
        null     | null       | null         | garbageEtag
    }

    @Unroll
    def "Renew lease AC fail"() {
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        String leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)
        def mac = new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                .withIfMatch(match).withIfNoneMatch(noneMatch)

        when:
        bu.renewLease(leaseID, mac, null).blockingGet()

        then:
        thrown(StorageException)

        where:
        modified | unmodified | match       | noneMatch
        newDate  | null       | null        | null
        null     | oldDate    | null        | null
        null     | null       | garbageEtag | null
        null     | null       | null        | receivedEtag
    }

    def "Renew lease error"() {
        setup:
        bu = cu.createBlockBlobURL(generateBlobName())

        when:
        bu.renewLease("id", null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Renew lease context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(200, BlobRenewLeaseHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        bu.renewLease("id", null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    def "Release lease"() {
        setup:
        String leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)

        BlobReleaseLeaseHeaders headers = bu.releaseLease(leaseID, null, null).blockingGet().headers()

        expect:
        bu.getProperties(null, null).blockingGet().headers().leaseState() == LeaseStateType.AVAILABLE
        validateBasicHeaders(headers)
    }

    def "Release lease min"() {
        setup:
        String leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)

        expect:
        bu.releaseLease(leaseID).blockingGet().statusCode() == 200
    }

    @Unroll
    def "Release lease AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        String leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)
        def mac = new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                .withIfMatch(match).withIfNoneMatch(noneMatch)

        expect:
        bu.releaseLease(leaseID, mac, null).blockingGet().statusCode() == 200

        where:
        modified | unmodified | match        | noneMatch
        null     | null       | null         | null
        oldDate  | null       | null         | null
        null     | newDate    | null         | null
        null     | null       | receivedEtag | null
        null     | null       | null         | garbageEtag
    }

    @Unroll
    def "Release lease AC fail"() {
        setup:
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        String leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)
        def mac = new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                .withIfMatch(match).withIfNoneMatch(noneMatch)

        when:
        bu.releaseLease(leaseID, mac, null).blockingGet()

        then:
        thrown(StorageException)

        where:
        modified | unmodified | match       | noneMatch
        newDate  | null       | null        | null
        null     | oldDate    | null        | null
        null     | null       | garbageEtag | null
        null     | null       | null        | receivedEtag
    }

    def "Release lease error"() {
        setup:
        bu = cu.createBlockBlobURL(generateBlobName())

        when:
        bu.releaseLease("id", null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Release lease context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(200, BlobReleaseLeaseHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        bu.releaseLease("id", null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    @Unroll
    def "Break lease"() {
        setup:
        bu.acquireLease(UUID.randomUUID().toString(), leaseTime, null, null).blockingGet()

        BlobBreakLeaseHeaders headers = bu.breakLease(breakPeriod, null, null).blockingGet().headers()
        LeaseStateType state = bu.getProperties(null, null).blockingGet().headers().leaseState()

        expect:
        state == LeaseStateType.BROKEN || state == LeaseStateType.BREAKING
        headers.leaseTime() <= remainingTime
        validateBasicHeaders(headers)

        where:
        leaseTime | breakPeriod | remainingTime
        -1        | null        | 0
        -1        | 20          | 25
        20        | 15          | 16
    }

    def "Break lease min"() {
        setup:
        setupBlobLeaseCondition(bu, receivedLeaseID)

        expect:
        bu.breakLease().blockingGet().statusCode() == 202
    }

    @Unroll
    def "Break lease AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        setupBlobLeaseCondition(bu, receivedLeaseID)
        def mac = new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                .withIfMatch(match).withIfNoneMatch(noneMatch)

        expect:
        bu.breakLease(null, mac, null).blockingGet().statusCode() == 202

        where:
        modified | unmodified | match        | noneMatch
        null     | null       | null         | null
        oldDate  | null       | null         | null
        null     | newDate    | null         | null
        null     | null       | receivedEtag | null
        null     | null       | null         | garbageEtag
    }

    @Unroll
    def "Break lease AC fail"() {
        setup:
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        setupBlobLeaseCondition(bu, receivedLeaseID)
        def mac = new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                .withIfMatch(match).withIfNoneMatch(noneMatch)

        when:
        bu.breakLease(null, mac, null).blockingGet()

        then:
        thrown(StorageException)

        where:
        modified | unmodified | match       | noneMatch
        newDate  | null       | null        | null
        null     | oldDate    | null        | null
        null     | null       | garbageEtag | null
        null     | null       | null        | receivedEtag
    }

    def "Break lease error"() {
        setup:
        bu = cu.createBlockBlobURL(generateBlobName())

        when:
        bu.breakLease(null, null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Break lease context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(202, BlobBreakLeaseHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        bu.breakLease(18, null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    def "Change lease"() {
        setup:
        String leaseID =
                bu.acquireLease(UUID.randomUUID().toString(), 15, null, null).blockingGet()
                        .headers().leaseId()
        BlobChangeLeaseHeaders headers = bu.changeLease(leaseID, UUID.randomUUID().toString(), null, null)
                .blockingGet().headers()
        leaseID = headers.leaseId()

        expect:
        bu.releaseLease(leaseID, null, null).blockingGet().statusCode() == 200
        validateBasicHeaders(headers)
    }

    def "Change lease min"() {
        setup:
        def leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)

        expect:
        bu.changeLease(leaseID, UUID.randomUUID().toString()).blockingGet().statusCode() == 200
    }

    @Unroll
    def "Change lease AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        String leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)
        def mac = new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                .withIfMatch(match).withIfNoneMatch(noneMatch)

        expect:
        bu.changeLease(leaseID, UUID.randomUUID().toString(), mac, null).blockingGet().statusCode() == 200

        where:
        modified | unmodified | match        | noneMatch
        null     | null       | null         | null
        oldDate  | null       | null         | null
        null     | newDate    | null         | null
        null     | null       | receivedEtag | null
        null     | null       | null         | garbageEtag
    }

    @Unroll
    def "Change lease AC fail"() {
        setup:
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        String leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)
        def mac = new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                .withIfMatch(match).withIfNoneMatch(noneMatch)

        when:
        bu.changeLease(leaseID, UUID.randomUUID().toString(), mac, null).blockingGet()

        then:
        thrown(StorageException)

        where:
        modified | unmodified | match       | noneMatch
        newDate  | null       | null        | null
        null     | oldDate    | null        | null
        null     | null       | garbageEtag | null
        null     | null       | null        | receivedEtag
    }

    def "Change lease error"() {
        setup:
        bu = cu.createBlockBlobURL(generateBlobName())

        when:
        bu.changeLease("id", "id", null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Change lease context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(200, BlobChangeLeaseHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        bu.changeLease("id", "newId", null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    def "Snapshot"() {
        when:
        BlobCreateSnapshotHeaders headers = bu.createSnapshot(null, null, null)
                .blockingGet().headers()

        then:
        bu.withSnapshot(headers.snapshot()).getProperties(null, null).blockingGet().statusCode() == 200
        validateBasicHeaders(headers)
    }

    def "Snapshot min"() {
        expect:
        bu.createSnapshot().blockingGet().statusCode() == 201
    }

    @Unroll
    def "Snapshot metadata"() {
        setup:
        Metadata metadata = new Metadata()
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2)
        }

        BlobCreateSnapshotResponse response = bu.createSnapshot(metadata, null, null).blockingGet()

        expect:
        response.statusCode() == 201
        bu.withSnapshot(response.headers().snapshot())
                .getProperties(null, null).blockingGet().headers().metadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @Unroll
    def "Snapshot AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))

        expect:
        bu.createSnapshot(null, bac, null).blockingGet().statusCode() == 201

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
    def "Snapshot AC fail"() {
        setup:
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))

        when:
        bu.createSnapshot(null, bac, null).blockingGet()

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

    def "Snapshot error"() {
        setup:
        bu = cu.createBlockBlobURL(generateBlobName())

        when:
        bu.createSnapshot(null, null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Snapshot context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(201, BlobCreateSnapshotHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        bu.createSnapshot(null, null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    def "Copy"() {
        setup:
        BlobURL bu2 = cu.createBlockBlobURL(generateBlobName())
        BlobStartCopyFromURLHeaders headers =
                bu2.startCopyFromURL(bu.toURL(), null, null, null, null)
                        .blockingGet().headers()

        when:
        while (bu2.getProperties(null, null).blockingGet().headers().copyStatus() == CopyStatusType.PENDING) {
            sleep(1000)
        }
        BlobGetPropertiesHeaders headers2 = bu2.getProperties(null, null).blockingGet().headers()

        then:
        headers2.copyStatus() == CopyStatusType.SUCCESS
        headers2.copyCompletionTime() != null
        headers2.copyProgress() != null
        headers2.copySource() != null
        validateBasicHeaders(headers)
        headers.copyId() != null
    }

    def "Copy min"() {
        expect:
        bu.startCopyFromURL(bu.toURL()).blockingGet().statusCode() == 202
    }

    @Unroll
    def "Copy metadata"() {
        setup:
        BlobURL bu2 = cu.createBlockBlobURL(generateBlobName())
        Metadata metadata = new Metadata()
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2)
        }

        BlobStartCopyFromURLResponse response =
                bu2.startCopyFromURL(bu.toURL(), metadata, null, null, null)
                        .blockingGet()
        waitForCopy(bu2, response.headers().copyStatus())

        expect:
        bu2.getProperties(null, null).blockingGet().headers().metadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @Unroll
    def "Copy source AC"() {
        setup:
        BlobURL bu2 = cu.createBlockBlobURL(generateBlobName())
        match = setupBlobMatchCondition(bu, match)
        def mac = new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                .withIfMatch(match).withIfNoneMatch(noneMatch)

        expect:
        bu2.startCopyFromURL(bu.toURL(), null, mac, null, null).blockingGet().statusCode() == 202

        where:
        modified | unmodified | match        | noneMatch
        null     | null       | null         | null
        oldDate  | null       | null         | null
        null     | newDate    | null         | null
        null     | null       | receivedEtag | null
        null     | null       | null         | garbageEtag
    }

    @Unroll
    def "Copy source AC fail"() {
        setup:
        BlobURL bu2 = cu.createBlockBlobURL(generateBlobName())
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        def mac = new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                .withIfMatch(match).withIfNoneMatch(noneMatch)

        when:
        bu2.startCopyFromURL(bu.toURL(), null, mac, null, null).blockingGet()

        then:
        thrown(StorageException)

        where:
        modified | unmodified | match       | noneMatch
        newDate  | null       | null        | null
        null     | oldDate    | null        | null
        null     | null       | garbageEtag | null
        null     | null       | null        | receivedEtag
    }

    @Unroll
    def "Copy dest AC"() {
        setup:
        BlobURL bu2 = cu.createBlockBlobURL(generateBlobName())
        bu2.upload(defaultFlowable, defaultDataSize, null, null,
                null, null).blockingGet()
        match = setupBlobMatchCondition(bu2, match)
        leaseID = setupBlobLeaseCondition(bu2, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))

        expect:
        bu2.startCopyFromURL(bu.toURL(), null, null, bac, null)
                .blockingGet().statusCode() == 202

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
    def "Copy dest AC fail"() {
        setup:
        BlobURL bu2 = cu.createBlockBlobURL(generateBlobName())
        bu2.upload(defaultFlowable, defaultDataSize, null, null,
                null, null).blockingGet()
        noneMatch = setupBlobMatchCondition(bu2, noneMatch)
        setupBlobLeaseCondition(bu2, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))

        when:
        bu2.startCopyFromURL(bu.toURL(), null, null, bac, null).blockingGet()

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

    def "Copy error"() {
        setup:
        bu = cu.createBlockBlobURL(generateBlobName())

        when:
        bu.startCopyFromURL(new URL("http://www.error.com"),
                null, null, null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Copy context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(202, BlobStartCopyFromURLHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        bu.startCopyFromURL(new URL("http://www.example.com"), null, null, null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    def "Abort copy"() {
        setup:
        // Data has to be large enough and copied between accounts to give us enough time to abort
        ByteBuffer data = getRandomData(8 * 1024 * 1024)
        bu.toBlockBlobURL()
                .upload(Flowable.just(data), 8 * 1024 * 1024, null, null, null, null)
                .blockingGet()
        // So we don't have to create a SAS.
        cu.setAccessPolicy(PublicAccessType.BLOB, null, null, null).blockingGet()

        ContainerURL cu2 = alternateServiceURL.createContainerURL(generateBlobName())
        cu2.create(null, null, null).blockingGet()
        BlobURL bu2 = cu2.createBlobURL(generateBlobName())

        when:
        String copyID =
                bu2.startCopyFromURL(bu.toURL(), null, null, null, null)
                        .blockingGet().headers().copyId()
        BlobAbortCopyFromURLResponse response = bu2.abortCopyFromURL(copyID, null, null).blockingGet()
        BlobAbortCopyFromURLHeaders headers = response.headers()

        then:
        response.statusCode() == 204
        headers.requestId() != null
        headers.version() != null
        headers.date() != null
        // Normal test cleanup will not clean up containers in the alternate account.
        cu2.delete(null, null).blockingGet().statusCode() == 202
    }

    def "Abort copy min"() {
        setup:
        // Data has to be large enough and copied between accounts to give us enough time to abort
        ByteBuffer data = getRandomData(8 * 1024 * 1024)
        bu.toBlockBlobURL()
                .upload(Flowable.just(data), 8 * 1024 * 1024, null, null, null, null)
                .blockingGet()
        // So we don't have to create a SAS.
        cu.setAccessPolicy(PublicAccessType.BLOB, null, null, null).blockingGet()

        ContainerURL cu2 = alternateServiceURL.createContainerURL(generateBlobName())
        cu2.create(null, null, null).blockingGet()
        BlobURL bu2 = cu2.createBlobURL(generateBlobName())

        when:
        String copyID =
                bu2.startCopyFromURL(bu.toURL(), null, null, null, null)
                        .blockingGet().headers().copyId()

        then:
        bu2.abortCopyFromURL(copyID).blockingGet().statusCode() == 204
    }

    def "Abort copy lease"() {
        setup:
        // Data has to be large enough and copied between accounts to give us enough time to abort
        ByteBuffer data = getRandomData(8 * 1024 * 1024)
        bu.toBlockBlobURL()
                .upload(Flowable.just(data), 8 * 1024 * 1024, null, null, null, null)
                .blockingGet()
        // So we don't have to create a SAS.
        cu.setAccessPolicy(PublicAccessType.BLOB, null, null, null).blockingGet()

        ContainerURL cu2 = alternateServiceURL.createContainerURL(generateBlobName())
        cu2.create(null, null, null).blockingGet()
        BlockBlobURL bu2 = cu2.createBlockBlobURL(generateBlobName())
        bu2.upload(defaultFlowable, defaultDataSize, null, null, null, null)
                .blockingGet()
        String leaseID = setupBlobLeaseCondition(bu2, receivedLeaseID)

        when:
        String copyID =
                bu2.startCopyFromURL(bu.toURL(), null, null,
                        new BlobAccessConditions().withLeaseAccessConditions(new LeaseAccessConditions()
                                .withLeaseId(leaseID)), null)
                        .blockingGet().headers().copyId()

        then:
        bu2.abortCopyFromURL(copyID, new LeaseAccessConditions().withLeaseId(leaseID), null)
                .blockingGet().statusCode() == 204
        // Normal test cleanup will not clean up containers in the alternate account.
        cu2.delete(null, null).blockingGet()
    }

    def "Abort copy lease fail"() {
        // Data has to be large enough and copied between accounts to give us enough time to abort
        ByteBuffer data = getRandomData(8 * 1024 * 1024)
        bu.toBlockBlobURL()
                .upload(Flowable.just(data), 8 * 1024 * 1024, null, null, null, null)
                .blockingGet()
        // So we don't have to create a SAS.
        cu.setAccessPolicy(PublicAccessType.BLOB, null, null, null).blockingGet()

        ContainerURL cu2 = alternateServiceURL.createContainerURL(generateBlobName())
        cu2.create(null, null, null).blockingGet()
        BlockBlobURL bu2 = cu2.createBlockBlobURL(generateBlobName())
        bu2.upload(defaultFlowable, defaultDataSize, null, null, null, null)
                .blockingGet()
        String leaseID = setupBlobLeaseCondition(bu2, receivedLeaseID)

        when:
        String copyID =
                bu2.startCopyFromURL(bu.toURL(), null, null,
                        new BlobAccessConditions().withLeaseAccessConditions(new LeaseAccessConditions()
                                .withLeaseId(leaseID)), null)
                        .blockingGet().headers().copyId()
        bu2.abortCopyFromURL(copyID, new LeaseAccessConditions().withLeaseId(garbageLeaseID), null).blockingGet()

        then:
        def e = thrown(StorageException)
        e.statusCode() == 412
        cu2.delete(null, null).blockingGet()
    }

    def "Abort copy error"() {
        setup:
        bu = cu.createBlockBlobURL(generateBlobName())

        when:
        bu.abortCopyFromURL("id", null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Abort copy context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(204, BlobAbortCopyFromURLHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        bu.abortCopyFromURL("id", null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    def "Sync copy"() {
        setup:
        // Sync copy is a deep copy, which requires either sas or public access.
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null).blockingGet()
        BlobURL bu2 = cu.createBlockBlobURL(generateBlobName())
        def headers = bu2.syncCopyFromURL(bu.toURL(), null, null,null, null).blockingGet().headers()

        expect:
        headers.copyStatus() == SyncCopyStatusType.SUCCESS
        headers.copyId() != null
        validateBasicHeaders(headers)
    }

    def "Sync copy min"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null).blockingGet()
        BlobURL bu2 = cu.createBlockBlobURL(generateBlobName())

        expect:
        bu2.syncCopyFromURL(bu.toURL()).blockingGet().statusCode() == 202
    }

    @Unroll
    def "Sync copy metadata"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null).blockingGet()
        BlobURL bu2 = cu.createBlockBlobURL(generateBlobName())
        Metadata metadata = new Metadata()
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2)
        }

        when:
        bu2.syncCopyFromURL(bu.toURL(), metadata, null, null, null).blockingGet()

        then:
        bu2.getProperties().blockingGet().headers().metadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @Unroll
    def "Sync copy source AC"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null).blockingGet()
        BlobURL bu2 = cu.createBlockBlobURL(generateBlobName())
        match = setupBlobMatchCondition(bu, match)
        def mac = new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                .withIfMatch(match).withIfNoneMatch(noneMatch)

        expect:
        bu2.syncCopyFromURL(bu.toURL(), null, mac, null, null).blockingGet().statusCode() == 202

        where:
        modified | unmodified | match        | noneMatch
        null     | null       | null         | null
        oldDate  | null       | null         | null
        null     | newDate    | null         | null
        null     | null       | receivedEtag | null
        null     | null       | null         | garbageEtag
    }

    @Unroll
    def "Sync copy source AC fail"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null).blockingGet()
        BlobURL bu2 = cu.createBlockBlobURL(generateBlobName())
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        def mac = new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                .withIfMatch(match).withIfNoneMatch(noneMatch)

        when:
        bu2.syncCopyFromURL(bu.toURL(), null, mac, null, null).blockingGet()

        then:
        thrown(StorageException)

        where:
        modified | unmodified | match       | noneMatch
        newDate  | null       | null        | null
        null     | oldDate    | null        | null
        null     | null       | garbageEtag | null
        null     | null       | null        | receivedEtag
    }

    @Unroll
    def "Sync copy dest AC"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null).blockingGet()
        BlobURL bu2 = cu.createBlockBlobURL(generateBlobName())
        bu2.upload(defaultFlowable, defaultDataSize, null, null,
                null, null).blockingGet()
        match = setupBlobMatchCondition(bu2, match)
        leaseID = setupBlobLeaseCondition(bu2, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))

        expect:
        bu2.syncCopyFromURL(bu.toURL(), null, null, bac, null).blockingGet().statusCode() == 202

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
    def "Sync copy dest AC fail"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null).blockingGet()
        BlobURL bu2 = cu.createBlockBlobURL(generateBlobName())
        bu2.upload(defaultFlowable, defaultDataSize, null, null,
                null, null).blockingGet()
        noneMatch = setupBlobMatchCondition(bu2, noneMatch)
        setupBlobLeaseCondition(bu2, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))

        when:
        bu2.syncCopyFromURL(bu.toURL(), null, null, bac, null).blockingGet()

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

    def "Sync copy error"() {
        setup:
        def bu2 = cu.createBlockBlobURL(generateBlobName())

        when:
        bu2.syncCopyFromURL(bu.toURL(), null, null, null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Sync copy context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(202, BlobCopyFromURLHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        bu.syncCopyFromURL(new URL("http://www.example.com"), null, null, null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    def "Delete"() {
        when:
        BlobDeleteResponse response = bu.delete(null, null, null).blockingGet()
        BlobDeleteHeaders headers = response.headers()

        then:
        response.statusCode() == 202
        headers.requestId() != null
        headers.version() != null
        headers.date() != null
    }

    def "Delete min"() {
        expect:
        bu.delete().blockingGet().statusCode() == 202
    }

    @Unroll
    def "Delete options"() {
        setup:
        bu.createSnapshot(null, null, null).blockingGet()
        // Create an extra blob so the list isn't empty (null) when we delete base blob, too
        BlockBlobURL bu2 = cu.createBlockBlobURL(generateBlobName())
        bu2.upload(defaultFlowable, defaultDataSize, null, null, null, null)
                .blockingGet()

        when:
        bu.delete(option, null, null).blockingGet()

        then:
        cu.listBlobsFlatSegment(null, null, null).blockingGet()
                .body().segment().blobItems().size() == blobsRemaining

        where:
        option                            | blobsRemaining
        DeleteSnapshotsOptionType.INCLUDE | 1
        DeleteSnapshotsOptionType.ONLY    | 2
    }

    @Unroll
    def "Delete AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))

        expect:
        bu.delete(DeleteSnapshotsOptionType.INCLUDE, bac, null).blockingGet().statusCode() == 202

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
    def "Delete AC fail"() {
        setup:
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))

        when:
        bu.delete(DeleteSnapshotsOptionType.INCLUDE, bac, null).blockingGet()

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

    def "Blob delete error"() {
        setup:
        bu = cu.createBlockBlobURL(generateBlobName())

        when:
        bu.delete(null, null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Delete context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(202, BlobDeleteHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        bu.delete(null, null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    @Unroll
    def "Set tier block blob"() {
        setup:
        ContainerURL cu = blobStorageServiceURL.createContainerURL(generateContainerName())
        BlockBlobURL bu = cu.createBlockBlobURL(generateBlobName())
        cu.create(null, null, null).blockingGet()
        bu.upload(defaultFlowable, defaultData.remaining(), null, null, null, null)
                .blockingGet()

        when:
        BlobSetTierResponse initialResponse = bu.setTier(tier, null, null).blockingGet()

        then:
        initialResponse.statusCode() == 200 || initialResponse.statusCode() == 202
        initialResponse.headers().version() != null
        initialResponse.headers().requestId() != null
        bu.getProperties(null, null).blockingGet().headers().accessTier() == tier.toString()
        cu.listBlobsFlatSegment(null, null, null).blockingGet().body().segment().blobItems().get(0)
                .properties().accessTier() == tier

        where:
        tier               | _
        AccessTier.HOT     | _
        AccessTier.COOL    | _
        AccessTier.ARCHIVE | _
    }

    @Unroll
    def "Set tier page blob"() {
        setup:
        ContainerURL cu = premiumServiceURL.createContainerURL(generateContainerName())
        PageBlobURL bu = cu.createPageBlobURL(generateBlobName())
        cu.create(null, null, null).blockingGet()
        bu.create(512, null, null, null, null, null).blockingGet()

        when:
        bu.setTier(tier, null, null).blockingGet()

        then:
        bu.getProperties(null, null).blockingGet().headers().accessTier() == tier.toString()
        cu.listBlobsFlatSegment(null, null, null).blockingGet().body().segment().blobItems().get(0)
                .properties().accessTier() == tier
        cu.delete(null, null).blockingGet()

        where:
        tier           | _
        AccessTier.P4  | _
        AccessTier.P6  | _
        AccessTier.P10 | _
        AccessTier.P20 | _
        AccessTier.P30 | _
        AccessTier.P40 | _
        AccessTier.P50 | _
    }

    def "Set tier min"() {
        setup:
        ContainerURL cu = blobStorageServiceURL.createContainerURL(generateContainerName())
        BlockBlobURL bu = cu.createBlockBlobURL(generateBlobName())
        cu.create(null, null, null).blockingGet()
        bu.upload(defaultFlowable, defaultData.remaining(), null, null, null, null)
                .blockingGet()

        when:
        def statusCode = bu.setTier(AccessTier.HOT).blockingGet().statusCode()

        then:
        statusCode == 200 || statusCode == 202
    }

    def "Set tier inferred"() {
        setup:
        ContainerURL cu = blobStorageServiceURL.createContainerURL(generateBlobName())
        BlockBlobURL bu = cu.createBlockBlobURL(generateBlobName())
        cu.create(null, null, null).blockingGet()
        bu.upload(defaultFlowable, defaultDataSize, null, null, null, null).blockingGet()

        when:
        boolean inferred1 = bu.getProperties(null, null).blockingGet().headers().accessTierInferred()
        Boolean inferredList1 = cu.listBlobsFlatSegment(null, null, null).blockingGet().body().segment()
                .blobItems().get(0).properties().accessTierInferred()

        bu.setTier(AccessTier.HOT, null, null).blockingGet()

        BlobGetPropertiesHeaders headers = bu.getProperties(null, null).blockingGet().headers()
        Boolean inferred2 = headers.accessTierInferred()
        Boolean inferredList2 = cu.listBlobsFlatSegment(null, null, null).blockingGet().body().segment()
                .blobItems().get(0).properties().accessTierInferred()

        then:
        inferred1
        inferredList1
        inferred2 == null
        inferredList2 == null
    }

    @Unroll
    def "Set tier archive status"() {
        setup:
        ContainerURL cu = blobStorageServiceURL.createContainerURL(generateBlobName())
        BlockBlobURL bu = cu.createBlockBlobURL(generateBlobName())
        cu.create(null, null, null).blockingGet()
        bu.upload(defaultFlowable, defaultDataSize, null, null, null, null).blockingGet()

        when:
        bu.setTier(sourceTier, null, null).blockingGet()
        bu.setTier(destTier, null, null).blockingGet()

        then:
        bu.getProperties(null, null).blockingGet().headers().archiveStatus() == status.toString()
        cu.listBlobsFlatSegment(null, null, null).blockingGet().body().segment().blobItems()
                .get(0).properties().archiveStatus()

        where:
        sourceTier         | destTier        | status
        AccessTier.ARCHIVE | AccessTier.COOL | ArchiveStatus.REHYDRATE_PENDING_TO_COOL
        AccessTier.ARCHIVE | AccessTier.HOT  | ArchiveStatus.REHYDRATE_PENDING_TO_HOT
    }

    def "Set tier error"() {
        setup:
        ContainerURL cu = blobStorageServiceURL.createContainerURL(generateBlobName())
        BlockBlobURL bu = cu.createBlockBlobURL(generateBlobName())
        cu.create(null, null, null).blockingGet()
        bu.upload(defaultFlowable, defaultDataSize, null, null, null, null).blockingGet()

        when:
        bu.setTier(AccessTier.fromString("garbage"), null, null).blockingGet()

        then:
        def e = thrown(StorageException)
        e.errorCode() == StorageErrorCode.INVALID_HEADER_VALUE
    }

    def "Set tier illegal argument"() {
        when:
        bu.setTier(null, null, null)

        then:
        thrown(IllegalArgumentException)
    }

    def "Set tier lease"() {
        setup:
        ContainerURL cu = blobStorageServiceURL.createContainerURL(generateBlobName())
        BlockBlobURL bu = cu.createBlockBlobURL(generateBlobName())
        cu.create(null, null, null).blockingGet()
        bu.upload(defaultFlowable, defaultDataSize, null, null, null, null).blockingGet()
        def leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)

        when:
        bu.setTier(AccessTier.HOT, new LeaseAccessConditions().withLeaseId(leaseID), null).blockingGet()

        then:
        notThrown(StorageException)
    }

    def "Set tier lease fail"() {
        setup:
        ContainerURL cu = blobStorageServiceURL.createContainerURL(generateBlobName())
        BlockBlobURL bu = cu.createBlockBlobURL(generateBlobName())
        cu.create(null, null, null).blockingGet()
        bu.upload(defaultFlowable, defaultDataSize, null, null, null, null).blockingGet()

        when:
        bu.setTier(AccessTier.HOT, new LeaseAccessConditions().withLeaseId("garbage"), null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Set tier context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(202, BlobSetTierHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        bu.setTier(AccessTier.HOT, null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    def "Undelete"() {
        setup:
        enableSoftDelete()
        bu.delete(null, null, null).blockingGet()

        when:
        def response = bu.undelete(null).blockingGet()
        bu.getProperties(null, null).blockingGet()

        then:
        notThrown(StorageException)
        response.headers().requestId() != null
        response.headers().version() != null
        response.headers().date() != null

        disableSoftDelete() == null
    }

    def "Undelete min"() {
        setup:
        enableSoftDelete()
        bu.delete().blockingGet()

        expect:
        bu.undelete().blockingGet().statusCode() == 200
    }

    def "Undelete error"() {
        bu = cu.createBlockBlobURL(generateBlobName())

        when:
        bu.undelete(null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Undelete context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(200, BlobUndeleteHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        bu.undelete(defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    def "Get account info"() {
        when:
        def response = primaryServiceURL.getAccountInfo(null).blockingGet()

        then:
        response.headers().date() != null
        response.headers().version() != null
        response.headers().requestId() != null
        response.headers().accountKind() != null
        response.headers().skuName() != null
    }

    def "Get account info min"() {
        expect:
        bu.getAccountInfo().blockingGet().statusCode() == 200
    }

    def "Get account info error"() {
        when:
        ServiceURL serviceURL = new ServiceURL(primaryServiceURL.toURL(),
                StorageURL.createPipeline(new AnonymousCredentials(), new PipelineOptions()))
        serviceURL.createContainerURL(generateContainerName()).createBlobURL(generateBlobName())
                .getAccountInfo(null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Get account info context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(200, BlobGetAccountInfoHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        bu.getAccountInfo(defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }
}
