// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob


import com.azure.storage.blob.models.*
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.security.MessageDigest

class BlobAPITest extends APISpec {
    BlobClient bu

    def setup() {
        bu = cu.createBlockBlobClient(generateBlobName())
        bu.upload(defaultInputStream.get(), defaultDataSize)
    }

    def "Download all null"() {
        when:
        ByteArrayOutputStream stream = new ByteArrayOutputStream()
        bu.download(stream)
        ByteBuffer body = ByteBuffer.wrap(stream.toByteArray())
//        BlobDownloadHeaders headers = response.headers()

        then:
        body == defaultData
//        headers.metadata().isEmpty()
//        headers.contentLength() != null
//        headers.contentType() != null
//        headers.contentRange() == null
//        headers.contentMD5() != null
//        headers.contentEncoding() == null
//        headers.cacheControl() == null
//        headers.contentDisposition() == null
//        headers.contentLanguage() == null
//        headers.blobSequenceNumber() == null
//        headers.blobType() == BlobType.BLOCK_BLOB
//        headers.copyCompletionTime() == null
//        headers.copyStatusDescription() == null
//        headers.copyId() == null
//        headers.copyProgress() == null
//        headers.copySource() == null
//        headers.copyStatus() == null
//        headers.leaseDuration() == null
//        headers.leaseState() == LeaseStateType.AVAILABLE
//        headers.leaseStatus() == LeaseStatusType.UNLOCKED
//        headers.acceptRanges() == "bytes"
//        headers.blobCommittedBlockCount() == null
//        headers.serverEncrypted
//        headers.blobContentMD5() == null
    }

    def "Download empty file"() {
        setup:
        bu = cu.createAppendBlobClient("emptyAppendBlob")
        bu.create()

        when:
        def outStream = new ByteArrayOutputStream()
        bu.download(outStream)
        def result = outStream.toByteArray()

        then:
        notThrown(StorageException)
        result.length == 0
    }

    /*
    This is to test the appropriate integration of DownloadResponse, including setting the correct range values on
    HTTPGetterInfo.
     */
    /*def "Download with retry range"() {
        *//*
        We are going to make a request for some range on a blob. The Flux returned will throw an exception, forcing
        a retry per the ReliableDownloadOptions. The next request should have the same range header, which was generated
        from the count and offset values in HTTPGetterInfo that was constructed on the initial call to download. We
        don't need to check the data here, but we want to ensure that the correct range is set each time. This will
        test the correction of a bug that was found which caused HTTPGetterInfo to have an incorrect offset when it was
        constructed in BlobClient.download().
         *//*
        setup:
        def mockPolicy = Mock(HttpPipelinePolicy) {
            process(_ as HttpPipelineCallContext, _ as HttpPipelineNextPolicy) >> {
                HttpPipelineCallContext context, HttpPipelineNextPolicy next ->
                    HttpRequest request = context.httpRequest()
                    if (request.headers().value("x-ms-range") != "bytes=2-6") {
                        return Mono.error(new IllegalArgumentException("The range header was not set correctly on retry."))
                    }
                    else {
                        // ETag can be a dummy value. It's not validated, but DownloadResponse requires one
                        // return Mono.just(getStubResponseForBlobDownload(206, Flux.error(new IOException()), "etag"))
                    }
            }
        }

        def pipeline = HttpPipeline.builder().policies(mockPolicy).build()
        bu = bu.withPipeline(pipeline)

        when:
        def range = new BlobRange().withOffset(2).withCount(5)
        def options = new ReliableDownloadOptions().withMaxRetryRequests(3)
        bu.download(null, options, range, null, false, null)

        then:
        *//*
        Because the dummy Flux always throws an error. This will also validate that an IllegalArgumentException is
        NOT thrown because the types would not match.
         *//*
        def e = thrown(RuntimeException)
        e.getCause() instanceof IOException
    }*/

    def "Download min"() {
        when:
        def outStream = new ByteArrayOutputStream()
        bu.download(outStream)
        byte[] result = outStream.toByteArray()

        then:
        result == defaultData.array()
    }

    @Unroll
    def "Download range"() {
        setup:
        BlobRange range = new BlobRange().offset(offset).count(count)

        when:
        def outStream = new ByteArrayOutputStream()
        bu.download(outStream, null, range, null, false, null)
        String bodyStr = outStream.toString()

        then:
        bodyStr == expectedData

        where:
        offset | count || expectedData
        0      | null  || defaultText
        0      | 5     || defaultText.substring(0, 5)
        3      | 2     || defaultText.substring(3, 3 + 2)
    }

    /*@Unroll
    def "Download AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions()
            .withLeaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .withModifiedAccessConditions(new ModifiedAccessConditions().ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))

        then:
        bu.download(null, null, null, bac, false, null).statusCode() == 200

        where:
        modified | unmodified | match        | noneMatch   | leaseID
        null     | null       | null         | null        | null
        oldDate  | null       | null         | null        | null
        null     | newDate    | null         | null        | null
        null     | null       | receivedEtag | null        | null
        null     | null       | null         | garbageEtag | null
        null     | null       | null         | null        | receivedLeaseID
    }*/

    /*@Unroll
    def "Download AC fail"() {
        setup:
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions()
            .withLeaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .withModifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))

        then:
        bu.download(null, null, null, bac, false, null).statusCode() == 206

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | garbageLeaseID
    }*/

    /*def "Download md5"() {
        expect:
        bu.download(null, null, new BlobRange().withOffset(0).withCount(3), null, true, null)
            .deserializedHeaders().contentMD5() ==
             MessageDigest.getInstance("MD5").digest(defaultText.substring(0, 3).getBytes())
    }*/

    def "Download error"() {
        setup:
        bu = cu.createBlockBlobClient(generateBlobName())

        when:
        bu.download(null, null, null, null, false, null)

        then:
        thrown(StorageException)
    }

    /*def "Download context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(206, BlobDownloadHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        bu.download(null)

        then:
        notThrown(RuntimeException)
    }*/

    def "Get properties default"() {
        when:
        BlobGetPropertiesHeaders headers = bu.getProperties(null, null)

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

    /*def "Get properties min"() {
        expect:
        bu.getProperties().blockingGet().statusCode() == 200
    }*/

    @Unroll
    def "Get properties AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions()
            .withLeaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .withModifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))

        expect:
        bu.getProperties(bac, null) //.blockingGet().statusCode() == 200

        where:
        modified | unmodified | match        | noneMatch   | leaseID
        null     | null       | null         | null        | null
        oldDate  | null       | null         | null        | null
        null     | newDate    | null         | null        | null
        null     | null       | receivedEtag | null        | null
        null     | null       | null         | garbageEtag | null
        null     | null       | null         | null        | receivedLeaseID
    }

    /*@Unroll
    def "Get properties AC fail"() {
        setup:
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions()
            .withLeaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .withModifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))

        when:
        bu.getProperties(bac, null)

        then:
        thrown(StorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | garbageLeaseID
    }*/

    /*def "Get properties error"() {
        setup:
        bu = cu.createBlockBlobClient(generateBlobName())

        when:
        bu.getProperties(null, null)

        then:
        thrown(StorageException)
    }*/

    /*def "Get properties context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(200, BlobGetPropertiesHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        bu.getProperties()

        then:
        notThrown(RuntimeException)
    }*/

    /*def "Set HTTP headers null"() {
        setup:
        BlobsSetHTTPHeadersResponse response = bu.setHTTPHeaders(null, null, null)

        expect:
        response.statusCode() == 200
        validateBasicHeaders(response.headers())
        response.deserializedHeaders().blobSequenceNumber() == null
    }*/

    def "Set HTTP headers min"() {
        when:
        bu.setHTTPHeaders(new BlobHTTPHeaders().blobContentType("type"))

        then:
        bu.getProperties().contentType() == "type"
    }

    @Unroll
    def "Set HTTP headers headers"() {
        setup:
        BlobHTTPHeaders putHeaders = new BlobHTTPHeaders().blobCacheControl(cacheControl)
            .blobContentDisposition(contentDisposition)
            .blobContentEncoding(contentEncoding)
            .blobContentLanguage(contentLanguage)
            .blobContentMD5(contentMD5)
            .blobContentType(contentType)
        bu.setHTTPHeaders(putHeaders, null, null)

        BlobGetPropertiesHeaders receivedHeaders = bu.getProperties(null, null)

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
        BlobAccessConditions bac = new BlobAccessConditions()
            .withLeaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .withModifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))

        expect:
        bu.setHTTPHeaders(null, bac, null) //.blockingGet().statusCode() == 200

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
        BlobAccessConditions bac = new BlobAccessConditions()
            .withLeaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .withModifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))

        when:
        bu.setHTTPHeaders(null, bac, null)

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
        bu = cu.createBlockBlobClient(generateBlobName())

        when:
        bu.setHTTPHeaders(null, null, null)

        then:
        thrown(StorageException)
    }

    /*def "Set HTTP headers context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(200, BlobSetHTTPHeadersHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        bu.setHTTPHeaders(null)

        then:
        notThrown(RuntimeException)
    }*/

    /*def "Set metadata all null"() {
        setup:
        BlobsSetMetadataResponse response = bu.setMetadata(null, null, null)

        expect:
        bu.getProperties(null, null).metadata().size() == 0
        response.statusCode() == 200
        validateBasicHeaders(response.headers())
        response.deserializedHeaders().isServerEncrypted()
    }*/

    def "Set metadata min"() {
        setup:
        Metadata metadata = new Metadata()
        metadata.put("foo", "bar")

        when:
        bu.setMetadata(metadata)

        then:
        bu.getProperties().metadata() == metadata
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
        bu.setMetadata(metadata, null, null) //.blockingGet().statusCode() == statusCode
        bu.getProperties(null, null).metadata() == metadata

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
        BlobAccessConditions bac = new BlobAccessConditions()
            .withLeaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .withModifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))

        expect:
        bu.setMetadata(null, bac, null) //.blockingGet().statusCode() == 200

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

        BlobAccessConditions bac = new BlobAccessConditions()
            .withLeaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .withModifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))

        when:
        bu.setMetadata(null, bac, null)

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
        bu = cu.createBlockBlobClient(generateBlobName())

        when:
        bu.setMetadata(null, null, null)

        then:
        thrown(StorageException)
    }

    /*def "Set metadata context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(200, BlobSetMetadataHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        bu.setMetadata(null)

        then:
        notThrown(RuntimeException)
    }*/

    @Unroll
    def "Acquire lease"() {
        setup:
        /*BlobAcquireLeaseHeaders*/ String leaseId = bu.acquireLease(proposedID, leaseTime, null, null)

        when:
        BlobGetPropertiesHeaders properties = bu.getProperties(null, null)

        then:
        properties.leaseState() == leaseState
        properties.leaseDuration() == leaseDuration
        leaseId != null
        //validateBasicHeaders(headers)

        where:
        proposedID                   | leaseTime || leaseState            | leaseDuration
        null                         | -1        || LeaseStateType.LEASED | LeaseDurationType.INFINITE
        null                         | 25        || LeaseStateType.LEASED | LeaseDurationType.FIXED
        UUID.randomUUID().toString() | -1        || LeaseStateType.LEASED | LeaseDurationType.INFINITE
    }

    /*def "Acquire lease min"() {
        setup:
        bu.acquireLease(null, -1).blockingGet().statusCode() == 201
    }*/

    @Unroll
    def "Acquire lease AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        def mac = new ModifiedAccessConditions().ifModifiedSince(modified).ifUnmodifiedSince(unmodified)
            .ifMatch(match).ifNoneMatch(noneMatch)

        expect:
        bu.acquireLease(null, -1, mac, null) //.blockingGet().statusCode() == 201

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
        def mac = new ModifiedAccessConditions()
            .ifModifiedSince(modified)
            .ifUnmodifiedSince(unmodified)
            .ifMatch(match)
            .ifNoneMatch(noneMatch)

        when:
        bu.acquireLease(null, -1, mac, null)

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
        bu = cu.createBlockBlobClient(generateBlobName())

        when:
        bu.acquireLease(null, 20, null, null)

        then:
        thrown(StorageException)
    }

    /*def "Acquire lease context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(201, BlobAcquireLeaseHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        bu.acquireLease(null, 20)

        then:
        notThrown(RuntimeException)
    }*/

    def "Renew lease"() {
        setup:
        String leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)

        Thread.sleep(16000) // Wait for the lease to expire to ensure we are actually renewing it
        /*BlobRenewLeaseHeaders*/ String leaseId = bu.renewLease(leaseID, null, null)

        expect:
        bu.getProperties(null, null).leaseState() == LeaseStateType.LEASED
        //validateBasicHeaders(headers)
        leaseId != null
    }

    /*def "Renew lease min"() {
        setup:
        String leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)

        expect:
        bu.renewLease(leaseID).blockingGet().statusCode() == 200
    }*/

    @Unroll
    def "Renew lease AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        String leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)
        def mac = new ModifiedAccessConditions()
            .ifModifiedSince(modified)
            .ifUnmodifiedSince(unmodified)
            .ifMatch(match)
            .ifNoneMatch(noneMatch)

        expect:
        bu.renewLease(leaseID, mac, null) //.blockingGet().statusCode() == 200

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
        def mac = new ModifiedAccessConditions()
            .ifModifiedSince(modified)
            .ifUnmodifiedSince(unmodified)
            .ifMatch(match)
            .ifNoneMatch(noneMatch)

        when:
        bu.renewLease(leaseID, mac, null)

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
        bu = cu.createBlockBlobClient(generateBlobName())

        when:
        bu.renewLease("id", null, null)

        then:
        thrown(StorageException)
    }

    /*def "Renew lease context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(200, BlobRenewLeaseHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        bu.renewLease("id")

        then:
        notThrown(RuntimeException)
    }*/

    /*def "Release lease"() {
        setup:
        String leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)

        BlobReleaseLeaseHeaders headers = bu.releaseLease(leaseID, null, null)

        expect:
        bu.getProperties(null, null).leaseState() == LeaseStateType.AVAILABLE
        validateBasicHeaders(headers)
    }*/

    def "Release lease min"() {
        setup:
        String leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)

        expect:
        bu.releaseLease(leaseID) //.blockingGet().statusCode() == 200
    }

    @Unroll
    def "Release lease AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        String leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)
        def mac = new ModifiedAccessConditions()
            .ifModifiedSince(modified)
            .ifUnmodifiedSince(unmodified)
            .ifMatch(match)
            .ifNoneMatch(noneMatch)

        expect:
        bu.releaseLease(leaseID, mac, null) //.blockingGet().statusCode() == 200

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
        def mac = new ModifiedAccessConditions()
            .ifModifiedSince(modified)
            .ifUnmodifiedSince(unmodified)
            .ifMatch(match)
            .ifNoneMatch(noneMatch)

        when:
        bu.releaseLease(leaseID, mac, null)

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
        bu = cu.createBlockBlobClient(generateBlobName())

        when:
        bu.releaseLease("id", null, null)

        then:
        thrown(StorageException)
    }

    /*def "Release lease context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(200, BlobReleaseLeaseHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        bu.releaseLease("id")

        then:
        notThrown(RuntimeException)
    }*/

    @Unroll
    def "Break lease"() {
        setup:
        bu.acquireLease(UUID.randomUUID().toString(), leaseTime, null, null)

        /*BlobBreakLeaseHeaders*/ int responseLeaseTime = bu.breakLease(breakPeriod, null, null)
        LeaseStateType state = bu.getProperties(null, null).leaseState()

        expect:
        state == LeaseStateType.BROKEN || state == LeaseStateType.BREAKING
        responseLeaseTime <= remainingTime
        //validateBasicHeaders(headers)

        where:
        leaseTime | breakPeriod | remainingTime
            -1        | null        | 0
            -1        | 20          | 25
            20        | 15          | 16
    }

    /*def "Break lease min"() {
        setup:
        setupBlobLeaseCondition(bu, receivedLeaseID)


        then:
        bu.breakLease().statusCode() == 202
    }*/

    /*@Unroll
    def "Break lease AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        setupBlobLeaseCondition(bu, receivedLeaseID)
        def mac = new ModifiedAccessConditions()
            .ifModifiedSince(modified)
            .ifUnmodifiedSince(unmodified)
            .ifMatch(match)
            .ifNoneMatch(noneMatch)

        then:
        bu.breakLease(null, mac, null).statusCode() == 202

        where:
        modified | unmodified | match        | noneMatch
        null     | null       | null         | null
        oldDate  | null       | null         | null
        null     | newDate    | null         | null
        null     | null       | receivedEtag | null
        null     | null       | null         | garbageEtag
    }*/

    @Unroll
    def "Break lease AC fail"() {
        setup:
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        setupBlobLeaseCondition(bu, receivedLeaseID)
        def mac = new ModifiedAccessConditions()
            .ifModifiedSince(modified)
            .ifUnmodifiedSince(unmodified)
            .ifMatch(match)
            .ifNoneMatch(noneMatch)

        when:
        bu.breakLease(null, mac, null)

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
        bu = cu.createBlockBlobClient(generateBlobName())

        when:
        bu.breakLease(null, null, null)

        then:
        thrown(StorageException)
    }

    /*def "Break lease context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(202, BlobBreakLeaseHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        bu.breakLease(18, null, null)

        then:
        notThrown(RuntimeException)
    }*/

    def "Change lease"() {
        setup:
        String leaseID = bu.acquireLease(UUID.randomUUID().toString(), 15)
        leaseID = bu.changeLease(leaseID, UUID.randomUUID().toString())

        expect:
        bu.releaseLease(leaseID, null, null) //.blockingGet().statusCode() == 200
        //validateBasicHeaders(headers)
    }

    def "Change lease min"() {
        setup:
        def leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)

        expect:
        bu.changeLease(leaseID, UUID.randomUUID().toString()) //.blockingGet().statusCode() == 200
    }

    @Unroll
    def "Change lease AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        String leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)
        def mac = new ModifiedAccessConditions()
            .ifModifiedSince(modified)
            .ifUnmodifiedSince(unmodified)
            .ifMatch(match)
            .ifNoneMatch(noneMatch)

        expect:
        bu.changeLease(leaseID, UUID.randomUUID().toString(), mac, null) //.blockingGet().statusCode() == 200

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
        def mac = new ModifiedAccessConditions()
            .ifModifiedSince(modified)
            .ifUnmodifiedSince(unmodified)
            .ifMatch(match)
            .ifNoneMatch(noneMatch)

        when:
        bu.changeLease(leaseID, UUID.randomUUID().toString(), mac, null)

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
        bu = cu.createBlockBlobClient(generateBlobName())

        when:
        bu.changeLease("id", "id", null, null)

        then:
        thrown(StorageException)
    }

    /*def "Change lease context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(200, BlobChangeLeaseHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        bu.changeLease("id", "newId")

        then:
        notThrown(RuntimeException)
    }*/

    /*def "Snapshot"() {
        when:
        String snapshot = bu.createSnapshot(null, null, null)

        then:
        bu.withSnapshot(snapshot).getProperties(null, null).blockingGet().statusCode() == 200
        validateBasicHeaders(headers)
    }*/

    /*def "Snapshot min"() {
        expect:
        bu.createSnapshot().blockingGet().statusCode() == 201
    }*/

    /*@Unroll
    def "Snapshot metadata"() {
        setup:
        Metadata metadata = new Metadata()
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2)
        }

        BlobsCreateSnapshotResponse response = bu.createSnapshot(metadata, null, null)

        expect:
        response.statusCode() == 201
        bu.withSnapshot(response.headers().snapshot())
            .getProperties(null, null).blockingGet().headers().metadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }*/

    /*@Unroll
    def "Snapshot AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
            new ModifiedAccessConditions().ifModifiedSince(modified).ifUnmodifiedSince(unmodified)
                .ifMatch(match).ifNoneMatch(noneMatch))
            .withLeaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))

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
    }*/

    @Unroll
    def "Snapshot AC fail"() {
        setup:
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions()
            .withLeaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .withModifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))


        when:
        bu.createSnapshot(null, bac, null)

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
        bu = cu.createBlockBlobClient(generateBlobName())

        when:
        bu.createSnapshot(null, null, null)

        then:
        thrown(StorageException)
    }

    /*def "Snapshot context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(201, BlobCreateSnapshotHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        bu.createSnapshot()

        then:
        notThrown(RuntimeException)
    }*/

    /*def "Copy"() {
        setup:
        BlobClient bu2 = cu.createBlockBlobClient(generateBlobName())
        BlobStartCopyFromURLHeaders headers =
            bu2.startCopyFromURL(bu.toURL(), null, null, null, null)

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
    }*/

    /*def "Copy min"() {
        expect:
        bu.startCopyFromURL(bu.toURL()).blockingGet().statusCode() == 202
    }*/

    /*@Unroll
    def "Copy metadata"() {
        setup:
        BlobClient bu2 = cu.createBlockBlobClient(generateBlobName())
        Metadata metadata = new Metadata()
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2)
        }

        BlobsStartCopyFromURLResponse response =
            bu2.startCopyFromURL(bu.toURL(), metadata, null, null, null)
        waitForCopy(bu2, response.deserializedHeaders().copyStatus())

        expect:
        bu2.getProperties().metadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }*/

    /*@Unroll
    def "Copy source AC"() {
        setup:
        BlobClient bu2 = cu.createBlockBlobClient(generateBlobName())
        match = setupBlobMatchCondition(bu, match)
        def mac = new ModifiedAccessConditions().ifModifiedSince(modified).ifUnmodifiedSince(unmodified)
            .ifMatch(match).ifNoneMatch(noneMatch)

        expect:
        bu2.startCopyFromURL(bu.toURL(), null, mac, null, null).blockingGet().statusCode() == 202

        where:
        modified | unmodified | match        | noneMatch
        null     | null       | null         | null
        oldDate  | null       | null         | null
        null     | newDate    | null         | null
        null     | null       | receivedEtag | null
        null     | null       | null         | garbageEtag
    }*/

    /*@Unroll
    def "Copy source AC fail"() {
        setup:
        BlobClient bu2 = cu.createBlockBlobClient(generateBlobName())
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        def mac = new ModifiedAccessConditions().ifModifiedSince(modified).ifUnmodifiedSince(unmodified)
            .ifMatch(match).ifNoneMatch(noneMatch)

        when:
        bu2.startCopyFromURL(bu.toURL(), null, mac, null, null)

        then:
        thrown(StorageException)

        where:
        modified | unmodified | match       | noneMatch
        newDate  | null       | null        | null
        null     | oldDate    | null        | null
        null     | null       | garbageEtag | null
        null     | null       | null        | receivedEtag
    }*/

    /*@Unroll
    def "Copy dest AC"() {
        setup:
        BlobClient bu2 = cu.createBlockBlobClient(generateBlobName())
        bu2.upload(defaultFlux, defaultDataSize)
        match = setupBlobMatchCondition(bu2, match)
        leaseID = setupBlobLeaseCondition(bu2, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
            new ModifiedAccessConditions().ifModifiedSince(modified).ifUnmodifiedSince(unmodified)
                .ifMatch(match).ifNoneMatch(noneMatch))
            .withLeaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))

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
    }*/

    /*@Unroll
    def "Copy dest AC fail"() {
        setup:
        BlobClient bu2 = cu.createBlockBlobClient(generateBlobName())
        bu2.upload(defaultFlux, defaultDataSize)
        noneMatch = setupBlobMatchCondition(bu2, noneMatch)
        setupBlobLeaseCondition(bu2, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
            new ModifiedAccessConditions().ifModifiedSince(modified).ifUnmodifiedSince(unmodified)
                .ifMatch(match).ifNoneMatch(noneMatch))
            .withLeaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))

        when:
        bu2.startCopyFromURL(bu.toURL(), null, null, bac, null)

        then:
        thrown(StorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | garbageLeaseID
    }*/

    /*def "Abort copy lease fail"() {
        // Data has to be large enough and copied between accounts to give us enough time to abort
        ByteBuffer data = getRandomData(8 * 1024 * 1024)
        bu.toBlockBlobClient()
            .upload(Flux.just(data), 8 * 1024 * 1024, null, null, null, null)
            .blockingGet()
        // So we don't have to create a SAS.
        cu.setAccessPolicy(PublicAccessType.BLOB)

        ContainerClient cu2 = alternateServiceURL.createContainerClient(generateBlobName())
        cu2.create(null, null, null)
        BlockBlobClient bu2 = cu2.createBlockBlobClient(generateBlobName())
        bu2.upload(defaultFlux, defaultDataSize)
        String leaseID = setupBlobLeaseCondition(bu2, receivedLeaseID)

        when:
        String copyID =
            bu2.startCopyFromURL(bu.toURL(), null, null,
                new BlobAccessConditions().withLeaseAccessConditions(new LeaseAccessConditions()
                    .leaseId(leaseID)), null)
        bu2.abortCopyFromURL(copyID, new LeaseAccessConditions().leaseId(garbageLeaseID), null)

        then:
        def e = thrown(StorageException)
        e.statusCode() == 412
        cu2.delete(null, null).blockingGet()
    }*/

    /*def "Copy context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(202, BlobStartCopyFromURLHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        bu.startCopyFromURL(new URL("http://www.example.com"))

        then:
        notThrown(RuntimeException)
    }*/

    /*def "Abort copy"() {
        setup:
        // Data has to be large enough and copied between accounts to give us enough time to abort
        ByteBuffer data = getRandomData(8 * 1024 * 1024)

        bu.asBlockBlobClient().upload(new ByteArrayInputStream(data.array()), 8 * 1024 * 1024)
        // So we don't have to create a SAS.
        cu.setAccessPolicy(PublicAccessType.BLOB, null)

        ContainerClient cu2 = alternateServiceURL.createContainerClient(generateBlobName())
        cu2.create()
        BlobClient bu2 = cu2.createBlobClient(generateBlobName())

        when:
        String copyID = bu2.startCopyFromURL(bu.toURL())
        BlobsAbortCopyFromURLResponse response = bu2.abortCopyFromURL(copyID)
        BlobAbortCopyFromURLHeaders headers = response.deserializedHeaders()

        then:
        response.statusCode() == 204
        headers.requestId() != null
        headers.version() != null
        headers.dateProperty() != null
        // Normal test cleanup will not clean up containers in the alternate account.
        cu2.delete() //.blockingGet().statusCode() == 202
    }*/

    /*def "Abort copy min"() {
        setup:
        // Data has to be large enough and copied between accounts to give us enough time to abort
        ByteBuffer data = getRandomData(8 * 1024 * 1024)
        bu.asBlockBlobClient().upload(Flux.just(data), 8 * 1024 * 1024)
        // So we don't have to create a SAS.
        cu.setAccessPolicy(PublicAccessType.BLOB, null)

        ContainerClient cu2 = alternateServiceURL.createContainerClient(generateBlobName())
        cu2.create()
        BlobClient bu2 = cu2.createBlobClient(generateBlobName())

        when:
        String copyID =
            bu2.startCopyFromURL(bu.toURL(), null, null, null, null)
                .blockingGet().headers().copyId()

        then:
        bu2.abortCopyFromURL(copyID).blockingGet().statusCode() == 204
    }*/

    /*def "Abort copy lease"() {
        setup:
        // Data has to be large enough and copied between accounts to give us enough time to abort
        ByteBuffer data = getRandomData(8 * 1024 * 1024)
        bu.asBlockBlobClient()
            .upload(Flux.just(data), 8 * 1024 * 1024)
        // So we don't have to create a SAS.
        cu.setAccessPolicy(PublicAccessType.BLOB, null)

        ContainerClient cu2 = alternateServiceURL.createContainerClient(generateBlobName())
        cu2.create()
        BlockBlobClient bu2 = cu2.createBlockBlobClient(generateBlobName())
        bu2.upload(defaultFlux, defaultDataSize)
        String leaseID = setupBlobLeaseCondition(bu2, receivedLeaseID)

        when:
        String copyID =
            bu2.startCopyFromURL(bu.toURL(), null, null,
                new BlobAccessConditions().withLeaseAccessConditions(new LeaseAccessConditions()
                    .leaseId(leaseID)), null)

        then:
        bu2.abortCopyFromURL(copyID, new LeaseAccessConditions().withLeaseId(leaseID), null)
            .blockingGet().statusCode() == 204
        // Normal test cleanup will not clean up containers in the alternate account.
        cu2.delete(null, null).blockingGet()
    }*/

    def "Copy error"() {
        setup:
        bu = cu.createBlockBlobClient(generateBlobName())

        when:
        bu.startCopyFromURL(new URL("http://www.error.com"))

        then:
        thrown(StorageException)
    }

    def "Abort copy error"() {
        setup:
        bu = cu.createBlockBlobClient(generateBlobName())

        when:
        bu.abortCopyFromURL("id", null, null)

        then:
        thrown(StorageException)
    }

    /*def "Abort copy context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(204, BlobAbortCopyFromURLHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        bu.abortCopyFromURL("id")

        then:
        notThrown(RuntimeException)
    }*/

    /*def "Sync copy"() {
        setup:
        // Sync copy is a deep copy, which requires either sas or public access.
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null)
        BlobClient bu2 = cu.createBlockBlobClient(generateBlobName())
        def headers = bu2.syncCopyFromURL(bu.toURL(), null, null,null, null).blockingGet().headers()

        expect:
        headers.copyStatus() == SyncCopyStatusType.SUCCESS
        headers.copyId() != null
        validateBasicHeaders(headers)
    }*/

    /*def "Sync copy min"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null)
        BlobClient bu2 = cu.createBlockBlobClient(generateBlobName())

        expect:
        bu2.syncCopyFromURL(bu.toURL()).blockingGet().statusCode() == 202
    }*/

    /*@Unroll
    def "Sync copy metadata"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null)
        BlobClient bu2 = cu.createBlockBlobClient(generateBlobName())
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
    }*/

    /*@Unroll
    def "Sync copy source AC"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null).blockingGet()
        BlobClient bu2 = cu.createBlockBlobClient(generateBlobName())
        match = setupBlobMatchCondition(bu, match)
        def mac = new ModifiedAccessConditions().ifModifiedSince(modified).ifUnmodifiedSince(unmodified)
            .ifMatch(match).ifNoneMatch(noneMatch)

        expect:
        bu2.syncCopyFromURL(bu.toURL(), null, mac, null, null).blockingGet().statusCode() == 202

        where:
        modified | unmodified | match        | noneMatch
        null     | null       | null         | null
        oldDate  | null       | null         | null
        null     | newDate    | null         | null
        null     | null       | receivedEtag | null
        null     | null       | null         | garbageEtag
    }*/

    /*@Unroll
    def "Sync copy source AC fail"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null).blockingGet()
        BlobClient bu2 = cu.createBlockBlobClient(generateBlobName())
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        def mac = new ModifiedAccessConditions().ifModifiedSince(modified).ifUnmodifiedSince(unmodified)
            .ifMatch(match).ifNoneMatch(noneMatch)

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
    }*/

    /*@Unroll
    def "Sync copy dest AC"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null)
        BlobClient bu2 = cu.createBlockBlobClient(generateBlobName())
        bu2.upload(defaultFlux, defaultDataSize)
        match = setupBlobMatchCondition(bu2, match)
        leaseID = setupBlobLeaseCondition(bu2, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
            new ModifiedAccessConditions().ifModifiedSince(modified).ifUnmodifiedSince(unmodified)
                .ifMatch(match).ifNoneMatch(noneMatch))
            .withLeaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))

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
    }*/

    /*@Unroll
    def "Sync copy dest AC fail"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null)
        BlobClient bu2 = cu.createBlockBlobClient(generateBlobName())
        bu2.upload(defaultFlux, defaultDataSize)
        noneMatch = setupBlobMatchCondition(bu2, noneMatch)
        setupBlobLeaseCondition(bu2, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
            new ModifiedAccessConditions().ifModifiedSince(modified).ifUnmodifiedSince(unmodified)
                .ifMatch(match).ifNoneMatch(noneMatch))
            .withLeaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))

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
    }*/

    /*def "Sync copy error"() {
        setup:
        def bu2 = cu.createBlockBlobClient(generateBlobName())

        when:
        bu2.syncCopyFromURL(bu.toURL(), null, null, null, null).blockingGet()

        then:
        thrown(StorageException)
    }*/

    /*def "Sync copy context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(202, BlobCopyFromURLHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        bu.syncCopyFromURL(new URL("http://www.example.com"))

        then:
        notThrown(RuntimeException)
    }*/

    /*def "Delete"() {
        when:
        BlobsDeleteResponse response = bu.delete()
        BlobDeleteHeaders headers = response.deserializedHeaders()

        then:
        response.statusCode() == 202
        headers.requestId() != null
        headers.version() != null
        headers.dateProperty() != null
    }*/

    /*def "Delete min"() {
        expect:
        bu.delete().blockingGet().statusCode() == 202
    }*/

    @Unroll
    def "Delete options"() {
        setup:
        bu.createSnapshot()
        // Create an extra blob so the list isn't empty (null) when we delete base blob, too
        BlockBlobClient bu2 = cu.createBlockBlobClient(generateBlobName())
        bu2.upload(defaultInputStream.get(), defaultDataSize)

        when:
        bu.delete(option, null, null)

        then:
        Iterator<BlobItem> blobs = cu.listBlobsFlat(null).iterator()

        int blobCount = 0
        for ( ; blobs.hasNext(); blobCount++ )
            blobs.next()

        blobCount == blobsRemaining

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
        BlobAccessConditions bac = new BlobAccessConditions()
            .withLeaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .withModifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))

        expect:
        bu.delete(DeleteSnapshotsOptionType.INCLUDE, bac, null) //.blockingGet().statusCode() == 202

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
        BlobAccessConditions bac = new BlobAccessConditions()
            .withLeaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .withModifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))

        when:
        bu.delete(DeleteSnapshotsOptionType.INCLUDE, bac, null)

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
        bu = cu.createBlockBlobClient(generateBlobName())

        when:
        bu.delete(null, null, null)

        then:
        thrown(StorageException)
    }

    /*def "Delete context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(202, BlobDeleteHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        bu.delete()

        then:
        notThrown(RuntimeException)
    }*/

    /*@Unroll
    def "Set tier block blob"() {
        setup:
        ContainerClient cu = blobStorageServiceURL.createContainerClient(generateContainerName())
        BlockBlobClient bu = cu.createBlockBlobClient(generateBlobName())
        cu.create()
        bu.upload(defaultInputStream.get(), defaultData.remaining())

        when:
        BlobsSetTierResponse initialResponse = bu.setTier(tier)
        BlobSetTierHeaders headers = initialResponse.deserializedHeaders()

        then:
        initialResponse.statusCode() == 200 || initialResponse.statusCode() == 202
        headers.version() != null
        headers.requestId() != null
        bu.getProperties().accessTier() == tier.toString()
        cu.listBlobsFlat(null).iterator().next().properties().accessTier() == tier

        where:
        tier               | _
        AccessTier.HOT     | _
        AccessTier.COOL    | _
        AccessTier.ARCHIVE | _
    }*/

    /*@Unroll
    def "Set tier page blob"() {
        setup:
        ContainerClient cu = premiumServiceURL.createContainerClient(generateContainerName())
        PageBlobClient bu = cu.createPageBlobClient(generateBlobName())
        cu.create()
        bu.create(512)

        when:
        bu.setTier(tier, null, null)

        then:
        bu.getProperties().accessTier() == tier.toString()
        cu.listBlobsFlat(null).iterator().next().properties().accessTier() == tier
        cu.delete()

        where:
        tier           | _
        AccessTier.P4  | _
        AccessTier.P6  | _
        AccessTier.P10 | _
        AccessTier.P20 | _
        AccessTier.P30 | _
        AccessTier.P40 | _
        AccessTier.P50 | _
    }*/

    /*def "Set tier min"() {
        setup:
        ContainerClient cu = blobStorageServiceURL.createContainerClient(generateContainerName())
        BlockBlobClient bu = cu.createBlockBlobClient(generateBlobName())
        cu.create()
        bu.upload(defaultInputStream.get(), defaultData.remaining())

        when:
        def statusCode = bu.setTier(AccessTier.HOT) //.blockingGet().statusCode()

        then:
        statusCode == 200 || statusCode == 202
    }*/

    /*def "Set tier inferred"() {
        setup:
        ContainerClient cu = blobStorageServiceURL.createContainerClient(generateBlobName())
        BlockBlobClient bu = cu.createBlockBlobClient(generateBlobName())
        cu.create()
        bu.upload(defaultInputStream.get(), defaultDataSize)

        when:
        boolean inferred1 = bu.getProperties(null, null).accessTierInferred()
        Boolean inferredList1 = cu.listBlobsFlat(null).iterator().next().properties().accessTierInferred()

        bu.setTier(AccessTier.HOT, null, null)

        BlobGetPropertiesHeaders headers = bu.getProperties(null, null)
        Boolean inferred2 = headers.accessTierInferred()
        Boolean inferredList2 = cu.listBlobsFlat(null).iterator().next().properties().accessTierInferred()

        then:
        inferred1
            inferredList1
        inferred2 == null
        inferredList2 == null
    }*/

    /*@Unroll
    def "Set tier archive status"() {
        setup:
        ContainerClient cu = blobStorageServiceURL.createContainerClient(generateBlobName())
        BlockBlobClient bu = cu.createBlockBlobClient(generateBlobName())
        cu.create(null, null, null, defaultContext)
        bu.upload(defaultInputStream.get(), defaultDataSize)

        when:
        bu.setTier(sourceTier)
        bu.setTier(destTier)

        then:
        bu.getProperties().archiveStatus() == status.toString()
        cu.listBlobsFlat(null).iterator().next().properties().archiveStatus()

        where:
        sourceTier         | destTier        | status
        AccessTier.ARCHIVE | AccessTier.COOL | ArchiveStatus.REHYDRATE_PENDING_TO_COOL
        AccessTier.ARCHIVE | AccessTier.HOT  | ArchiveStatus.REHYDRATE_PENDING_TO_HOT
    }*/

    /*def "Set tier error"() {
        setup:
        ContainerClient cu = blobStorageServiceURL.createContainerClient(generateBlobName())
        BlockBlobClient bu = cu.createBlockBlobClient(generateBlobName())
        cu.create()
        bu.upload(defaultInputStream.get(), defaultDataSize)

        when:
        bu.setTier(AccessTier.fromString("garbage"), null, null)

        then:
        def e = thrown(StorageException)
        e.errorCode() == StorageErrorCode.INVALID_HEADER_VALUE
    }*/

    def "Set tier illegal argument"() {
        when:
        bu.setTier(null, null, null)

        then:
        thrown(IllegalArgumentException)
    }

    /*def "Set tier lease"() {
        setup:
        ContainerClient cu = blobStorageServiceURL.createContainerClient(generateBlobName())
        BlockBlobClient bu = cu.createBlockBlobClient(generateBlobName())
        cu.create()
        bu.upload(defaultInputStream.get(), defaultDataSize)
        def leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)

        when:
        bu.setTier(AccessTier.HOT, new LeaseAccessConditions().leaseId(leaseID), null)

        then:
        notThrown(StorageException)
    }*/

    /*def "Set tier lease fail"() {
        setup:
        ContainerClient cu = blobStorageServiceURL.createContainerClient(generateBlobName())
        BlockBlobClient bu = cu.createBlockBlobClient(generateBlobName())
        cu.create()
        bu.upload(defaultInputStream.get(), defaultDataSize)

        when:
        bu.setTier(AccessTier.HOT, new LeaseAccessConditions().leaseId("garbage"), null)

        then:
        thrown(StorageException)
    }*/

    /*def "Set tier context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(202, BlobSetTierHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        bu.setTier(AccessTier.HOT)

        then:
        notThrown(RuntimeException)
    }*/

    /*def "Undelete"() {
        setup:
        enableSoftDelete()
        bu.delete(null, null, null)

        when:
        def response = bu.undelete(null)
        bu.getProperties(null, null)

        then:
        notThrown(StorageException)
        response.headers().requestId() != null
        response.headers().version() != null
        response.headers().date() != null

        disableSoftDelete() == null
    }*/

    def "Undelete min"() {
        setup:
        enableSoftDelete()
        bu.delete()

        expect:
        bu.undelete() //.blockingGet().statusCode() == 200
    }

    def "Undelete error"() {
        bu = cu.createBlockBlobClient(generateBlobName())

        when:
        bu.undelete(null)

        then:
        thrown(StorageException)
    }

    /*def "Undelete context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(200, BlobUndeleteHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        bu.undelete(null)

        then:
        notThrown(RuntimeException)
    }*/

    def "Get account info"() {
        when:
        def response = primaryServiceURL.getAccountInfo()

        then:
        response.dateProperty() != null
        response.version() != null
        response.requestId() != null
        response.accountKind() != null
        response.skuName() != null
    }

    def "Get account info min"() {
        expect:
        bu.getAccountInfo() //.statusCode() == 200
    }

    /*def "Get account info error"() {
        when:
        BlobServiceClient serviceURL = BlobServiceClient.builder() new ServiceURL(primaryServiceURL.toURL(),
            StorageURL.createPipeline(new AnonymousCredentials(), new PipelineOptions()))
        serviceURL.createContainerClient(generateContainerName()).createBlobClient(generateBlobName())
            .getAccountInfo(null)

        then:
        thrown(StorageException)
    }*/

    /*def "Get account info context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(200, BlobGetAccountInfoHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        bu.getAccountInfo(null)

        then:
        notThrown(RuntimeException)
    }*/
}
