// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.core.http.HttpHeaders
import com.azure.core.http.rest.Response
import com.azure.core.http.rest.VoidResponse
import com.azure.core.implementation.util.ImplUtils
import com.azure.storage.blob.models.AccessTier
import com.azure.storage.blob.models.ArchiveStatus
import com.azure.storage.blob.models.BlobAccessConditions
import com.azure.storage.blob.models.BlobHTTPHeaders
import com.azure.storage.blob.models.BlobItem
import com.azure.storage.blob.models.BlobRange
import com.azure.storage.blob.models.BlobType
import com.azure.storage.blob.models.CopyStatusType
import com.azure.storage.blob.models.DeleteSnapshotsOptionType
import com.azure.storage.blob.models.LeaseAccessConditions
import com.azure.storage.blob.models.LeaseDurationType
import com.azure.storage.blob.models.LeaseStateType
import com.azure.storage.blob.models.LeaseStatusType
import com.azure.storage.blob.models.Metadata
import com.azure.storage.blob.models.ModifiedAccessConditions
import com.azure.storage.blob.models.PublicAccessType
import com.azure.storage.blob.models.ReliableDownloadOptions
import com.azure.storage.blob.models.StorageAccountInfo
import com.azure.storage.blob.models.StorageErrorCode
import com.azure.storage.blob.models.StorageException
import com.azure.storage.blob.models.SyncCopyStatusType
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.security.MessageDigest
import java.time.OffsetDateTime

class BlobAPITest extends APISpec {
    BlobClient bu

    def setup() {
        bu = cu.getBlockBlobClient(generateBlobName())
        bu.upload(defaultInputStream.get(), defaultDataSize)
    }

    def "Download all null"() {
        when:
        ByteArrayOutputStream stream = new ByteArrayOutputStream()
        VoidResponse response = bu.downloadWithResponse(stream, null, null, null, false, null, null)
        ByteBuffer body = ByteBuffer.wrap(stream.toByteArray())
        HttpHeaders headers = response.headers()

        then:
        body == defaultData
        ImplUtils.isNullOrEmpty(getMetadataFromHeaders(headers))
        headers.value("Content-Length") != null
        headers.value("Content-Type") != null
        headers.value("Content-Range") == null
        headers.value("Content-MD5") != null
        headers.value("Content-Encoding") == null
        headers.value("Cache-Control") == null
        headers.value("Content-Disposition") == null
        headers.value("Content-Language") == null
        headers.value("x-ms-blob-sequence-number") == null
        headers.value("x-ms-blob-type") == BlobType.BLOCK_BLOB.toString()
        headers.value("x-ms-copy-completion-time") == null
        headers.value("x-ms-copy-status-description") == null
        headers.value("x-ms-copy-id") == null
        headers.value("x-ms-copy-progress") == null
        headers.value("x-ms-copy-source") == null
        headers.value("x-ms-copy-status") == null
        headers.value("x-ms-lease-duration") == null
        headers.value("x-ms-lease-state") == LeaseStateType.AVAILABLE.toString()
        headers.value("x-ms-lease-status") == LeaseStatusType.UNLOCKED.toString()
        headers.value("Accept-Ranges") == "bytes"
        headers.value("x-ms-blob-committed-block-count") == null
        headers.value("x-ms-server-encrypted") != null
        headers.value("x-ms-blob-content-md5") == null
    }

    def "Download empty file"() {
        setup:
        bu = cu.getAppendBlobClient("emptyAppendBlob")
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
    def "Download with retry range"() {
        /*
        We are going to make a request for some range on a blob. The Flux returned will throw an exception, forcing
        a retry per the ReliableDownloadOptions. The next request should have the same range header, which was generated
        from the count and offset values in HTTPGetterInfo that was constructed on the initial call to download. We
        don't need to check the data here, but we want to ensure that the correct range is set each time. This will
        test the correction of a bug that was found which caused HTTPGetterInfo to have an incorrect offset when it was
        constructed in BlobClient.download().
         */
        setup:
        BlobClient bu2 = getBlobClient(primaryCredential, bu.getBlobUrl().toString(), new MockRetryRangeResponsePolicy())

        when:
        BlobRange range = new BlobRange(2, 5L)
        ReliableDownloadOptions options = new ReliableDownloadOptions().maxRetryRequests(3)
        bu2.downloadWithResponse(new ByteArrayOutputStream(), range, options, null, false, null, null)

        then:
        /*
        Because the dummy Flux always throws an error. This will also validate that an IllegalArgumentException is
        NOT thrown because the types would not match.
         */
        def e = thrown(RuntimeException)
        e.getCause() instanceof IOException
    }

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
        BlobRange range = (count == null) ? new BlobRange(offset) : new BlobRange(offset, count)

        when:
        def outStream = new ByteArrayOutputStream()
        bu.downloadWithResponse(outStream, range, null, null, false, null, null)
        String bodyStr = outStream.toString()

        then:
        bodyStr == expectedData

        where:
        offset | count || expectedData
        0      | null  || defaultText
        0      | 5L     || defaultText.substring(0, 5)
        3      | 2L    || defaultText.substring(3, 3 + 2)
    }

    @Unroll
    def "Download AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions().ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))

        when:
        def response = bu.downloadWithResponse(new ByteArrayOutputStream(), null, null, bac, false, null, null)

        then:
        response.statusCode() == 200

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
        setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(setupBlobMatchCondition(bu, noneMatch)))

        when:
        bu.downloadWithResponse(new ByteArrayOutputStream(), null, null, bac, false, null, null).statusCode() == 206

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
        when:
        VoidResponse response = bu.downloadWithResponse(new ByteArrayOutputStream(), new BlobRange(0 ,3), null, null, true, null, null)
        byte[] contentMD5 = response.headers().value("content-md5").getBytes()

        then:
        contentMD5 == Base64.getEncoder().encode(MessageDigest.getInstance("MD5").digest(defaultText.substring(0, 3).getBytes()))
    }

    def "Download error"() {
        setup:
        bu = cu.getBlockBlobClient(generateBlobName())

        when:
        bu.download(null)

        then:
        thrown(StorageException)
    }

    def "Download snapshot"() {
        when:
        ByteArrayOutputStream originalStream = new ByteArrayOutputStream()
        bu.download(originalStream)

        BlockBlobClient bu2 = bu.asBlockBlobClient()
        BlobClient bu3 = bu.createSnapshot()
        bu2.upload(new ByteArrayInputStream("ABC".getBytes()), 3)

        then:
        ByteArrayOutputStream snapshotStream = new ByteArrayOutputStream()
        bu3.download(snapshotStream)
        snapshotStream.toByteArray() == originalStream.toByteArray()
    }

    def "Get properties default"() {
        when:
        Response<BlobProperties> response = bu.getPropertiesWithResponse(null, null, null)
        HttpHeaders headers = response.headers()

        then:
        validateBasicHeaders(headers)
        ImplUtils.isNullOrEmpty(getMetadataFromHeaders(headers))
        headers.value("x-ms-blob-type") == BlobType.BLOCK_BLOB.toString()
        headers.value("x-ms-copy-completion-time") == null // tested in "copy"
        headers.value("x-ms-copy-status-description") == null // only returned when the service has errors; cannot validate.
        headers.value("x-ms-copy-id") == null // tested in "abort copy"
        headers.value("x-ms-copy-progress") == null // tested in "copy"
        headers.value("x-ms-copy-source") == null // tested in "copy"
        headers.value("x-ms-copy-status") == null // tested in "copy"
        headers.value("x-ms-incremental-copy") == null // tested in PageBlob."start incremental copy"
        headers.value("x-ms-copy-destination-snapshot") == null // tested in PageBlob."start incremental copy"
        headers.value("x-ms-lease-duration") == null // tested in "acquire lease"
        headers.value("x-ms-lease-state") == LeaseStateType.AVAILABLE.toString()
        headers.value("x-ms-lease-status") == LeaseStatusType.UNLOCKED.toString()
        headers.value("Content-Length") != null
        headers.value("Content-Type") != null
        headers.value("Content-MD5") != null
        headers.value("Content-Encoding") == null // tested in "set HTTP headers"
        headers.value("Content-Disposition") == null // tested in "set HTTP headers"
        headers.value("Content-Language") == null // tested in "set HTTP headers"
        headers.value("Cache-Control") == null // tested in "set HTTP headers"
        headers.value("x-ms-blob-sequence-number") == null // tested in PageBlob."create sequence number"
        headers.value("Accept-Ranges") == "bytes"
        headers.value("x-ms-blob-committed-block-count") == null // tested in AppendBlob."append block"
        Boolean.parseBoolean(headers.value("x-ms-server-encrypted"))
        headers.value("x-ms-access-tier") == AccessTier.HOT.toString()
        Boolean.parseBoolean(headers.value("x-ms-access-tier-inferred"))
        headers.value("x-ms-archive-status") == null
        headers.value("x-ms-creation-time") != null
    }

    def "Get properties min"() {
        expect:
        bu.getPropertiesWithResponse(null, null, null).statusCode() == 200
    }

    @Unroll
    def "Get properties AC"() {
        setup:
        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(setupBlobLeaseCondition(bu, leaseID)))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(setupBlobMatchCondition(bu, match))
                .ifNoneMatch(noneMatch))

        expect:
        bu.getPropertiesWithResponse(bac, null, null).statusCode() == 200

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
        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(setupBlobLeaseCondition(bu, leaseID)))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(setupBlobMatchCondition(bu, noneMatch)))

        when:
        bu.getPropertiesWithResponse(bac, null, null)

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
        bu = cu.getBlockBlobClient(generateBlobName())

        when:
        bu.getPropertiesWithResponse(null, null, null)

        then:
        thrown(StorageException)
    }

    def "Set HTTP headers null"() {
        setup:
        VoidResponse response = bu.setHTTPHeadersWithResponse(null, null, null, null)

        expect:
        response.statusCode() == 200
        validateBasicHeaders(response.headers())
    }

    def "Set HTTP headers min"() {
        when:
        BlobProperties properties = bu.getProperties()
        BlobHTTPHeaders headers = new BlobHTTPHeaders()
            .blobContentEncoding(properties.contentEncoding())
            .blobContentDisposition(properties.contentDisposition())
            .blobContentType("type")
            .blobCacheControl(properties.cacheControl())
            .blobContentLanguage(properties.contentLanguage())
            .blobContentMD5(Base64.getEncoder().encode(MessageDigest.getInstance("MD5").digest(defaultData.array())))

        bu.setHTTPHeaders(headers)

        then:
        bu.getPropertiesWithResponse(null, null, null).headers().value("Content-Type") == "type"
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

        bu.setHTTPHeaders(putHeaders)

        Response<BlobProperties> response = bu.getPropertiesWithResponse(null, null, null)

        expect:
        validateBlobProperties(response, cacheControl, contentDisposition, contentEncoding, contentLanguage, contentMD5, contentType)

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
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))

        expect:
        bu.setHTTPHeadersWithResponse(null, bac, null, null).statusCode() == 200

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
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))

        when:
        bu.setHTTPHeadersWithResponse(null, bac, null, null)

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
        bu = cu.getBlockBlobClient(generateBlobName())

        when:
        bu.setHTTPHeaders(null)

        then:
        thrown(StorageException)
    }

    def "Set metadata all null"() {
        setup:
        VoidResponse response = bu.setMetadataWithResponse(null, null, null, null)

        expect:
        bu.getProperties().metadata().size() == 0
        response.statusCode() == 200
        validateBasicHeaders(response.headers())
        Boolean.parseBoolean(response.headers().value("x-ms-request-server-encrypted"))
    }

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
        bu.setMetadataWithResponse(metadata, null, null, null).statusCode() == statusCode
        bu.getProperties().metadata() == metadata

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
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))

        expect:
        bu.setMetadataWithResponse(null, bac, null, null).statusCode() == 200

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
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))

        when:
        bu.setMetadataWithResponse(null, bac, null, null)

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
        bu = cu.getBlockBlobClient(generateBlobName())

        when:
        bu.setMetadata(null)

        then:
        thrown(StorageException)
    }

    @Unroll
    def "Acquire lease"() {
        setup:
        String leaseId = bu.acquireLease(proposedID, leaseTime)

        when:
        HttpHeaders headers = bu.getPropertiesWithResponse(null, null, null).headers()

        then:
        headers.value("x-ms-lease-state") == leaseState.toString()
        headers.value("x-ms-lease-duration") == leaseDuration.toString()
        leaseId != null
        validateBasicHeaders(headers)

        where:
        proposedID                   | leaseTime || leaseState            | leaseDuration
        null                         | -1        || LeaseStateType.LEASED | LeaseDurationType.INFINITE
        null                         | 25        || LeaseStateType.LEASED | LeaseDurationType.FIXED
        UUID.randomUUID().toString() | -1        || LeaseStateType.LEASED | LeaseDurationType.INFINITE
    }

    def "Acquire lease min"() {
        setup:
        bu.acquireLeaseWithResponse(null, -1, null, null, null).statusCode() == 201
    }

    @Unroll
    def "Acquire lease AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        def mac = new ModifiedAccessConditions()
            .ifModifiedSince(modified)
            .ifUnmodifiedSince(unmodified)
            .ifMatch(match)
            .ifNoneMatch(noneMatch)

        expect:
        bu.acquireLeaseWithResponse(null, -1, mac, null, null).statusCode() == 201

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
        bu.acquireLeaseWithResponse(null, -1, mac, null, null)

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
        bu = cu.getBlockBlobClient(generateBlobName())

        when:
        bu.acquireLease(null, 20)

        then:
        thrown(StorageException)
    }

    def "Renew lease"() {
        setup:
        String leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)

        // If running in live mode wait for the lease to expire to ensure we are actually renewing it
        sleepIfRecord(16000)

        Response<String> renewLeaseResponse = bu.renewLeaseWithResponse(leaseID, null, null, null)

        expect:
        bu.getPropertiesWithResponse(null, null, null).headers().value("x-ms-lease-state") == LeaseStateType.LEASED.toString()
        validateBasicHeaders(renewLeaseResponse.headers())
        renewLeaseResponse.value() != null
    }

    def "Renew lease min"() {
        setup:
        String leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)

        expect:
        bu.renewLeaseWithResponse(leaseID, null, null, null).statusCode() == 200
    }

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
        bu.renewLeaseWithResponse(leaseID, mac, null, null).statusCode() == 200

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
        bu.renewLeaseWithResponse(leaseID, mac, null, null)

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
        bu = cu.getBlockBlobClient(generateBlobName())

        when:
        bu.renewLeaseWithResponse("id", null, null, null)

        then:
        thrown(StorageException)
    }

    def "Release lease"() {
        setup:
        String leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)

        HttpHeaders headers = bu.releaseLeaseWithResponse(leaseID, null, null, null).headers()

        expect:
        bu.getPropertiesWithResponse(null, null, null).headers().value("x-ms-lease-state") == LeaseStateType.AVAILABLE.toString()
        validateBasicHeaders(headers)
    }

    def "Release lease min"() {
        setup:
        String leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)

        expect:
        bu.releaseLeaseWithResponse(leaseID, null, null, null).statusCode() == 200
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
        bu.releaseLeaseWithResponse(leaseID, mac, null, null).statusCode() == 200

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
        bu.releaseLeaseWithResponse(leaseID, mac, null, null)

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
        bu = cu.getBlockBlobClient(generateBlobName())

        when:
        bu.releaseLeaseWithResponse("id", null, null, null)

        then:
        thrown(StorageException)
    }

    @Unroll
    def "Break lease"() {
        setup:
        bu.acquireLeaseWithResponse(getRandomUUID(), leaseTime, null, null, null)

        Response<Integer> breakLeaseResponse = bu.breakLeaseWithResponse(breakPeriod, null, null, null)
        String leaseState = bu.getPropertiesWithResponse(null, null, null).headers().value("x-ms-lease-state")

        expect:
        leaseState == LeaseStateType.BROKEN.toString() || leaseState == LeaseStateType.BREAKING.toString()
        breakLeaseResponse.value() <= remainingTime
        validateBasicHeaders(breakLeaseResponse.headers())

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
        bu.breakLeaseWithResponse(null, null, null, null).statusCode() == 202
    }

    @Unroll
    def "Break lease AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        setupBlobLeaseCondition(bu, receivedLeaseID)
        def mac = new ModifiedAccessConditions()
            .ifModifiedSince(modified)
            .ifUnmodifiedSince(unmodified)
            .ifMatch(match)
            .ifNoneMatch(noneMatch)

        expect:
        bu.breakLeaseWithResponse(null, mac, null, null).statusCode() == 202

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
        def mac = new ModifiedAccessConditions()
            .ifModifiedSince(modified)
            .ifUnmodifiedSince(unmodified)
            .ifMatch(match)
            .ifNoneMatch(noneMatch)

        when:
        bu.breakLeaseWithResponse(null, mac, null, null)

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
        bu = cu.getBlockBlobClient(generateBlobName())

        when:
        bu.breakLeaseWithResponse(null, null, null, null)

        then:
        thrown(StorageException)
    }

    def "Change lease"() {
        setup:
        String acquireLease = bu.acquireLease(getRandomUUID(), 15)
        Response<String> changeLeaseResponse = bu.changeLeaseWithResponse(acquireLease, getRandomUUID(), null, null, null)

        expect:
        bu.releaseLeaseWithResponse(changeLeaseResponse.value(), null, null, null).statusCode() == 200
        validateBasicHeaders(changeLeaseResponse.headers())
    }

    def "Change lease min"() {
        setup:
        def leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)

        expect:
        bu.changeLeaseWithResponse(leaseID, getRandomUUID(), null, null, null).statusCode() == 200
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
        bu.changeLeaseWithResponse(leaseID, getRandomUUID(), mac, null, null).statusCode() == 200

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
        bu.changeLeaseWithResponse(leaseID, getRandomUUID(), mac, null, null)

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
        bu = cu.getBlockBlobClient(generateBlobName())

        when:
        bu.changeLease("id", "id")

        then:
        thrown(StorageException)
    }

    def "Snapshot"() {
        when:
        Response<BlobClient> snapshotResponse = bu.createSnapshotWithResponse(null, null, null, null)
        BlobClient bu2 = snapshotResponse.value()

        then:
        bu2.getPropertiesWithResponse(null, null, null).statusCode() == 200
        validateBasicHeaders(snapshotResponse.headers())
    }

    def "Snapshot min"() {
        expect:
        bu.createSnapshotWithResponse(null, null, null, null).statusCode() == 201
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

        Response<BlobClient> response = bu.createSnapshotWithResponse(metadata, null, null, null)
        BlobClient bu2 = response.value()

        expect:
        response.statusCode() == 201
        bu2.getProperties().metadata() == metadata

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
        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))

        expect:
        bu.createSnapshotWithResponse(null, bac, null, null).statusCode() == 201

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
        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))


        when:
        bu.createSnapshotWithResponse(null, bac, null, null)

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
        bu = cu.getBlockBlobClient(generateBlobName())

        when:
        bu.createSnapshot()

        then:
        thrown(StorageException)
    }

    def "Copy"() {
        setup:
        BlobClient bu2 = cu.getBlockBlobClient(generateBlobName())
        HttpHeaders headers =
            bu2.startCopyFromURLWithResponse(bu.getBlobUrl(), null, null, null, null, null).headers()

        when:
        while (bu2.getPropertiesWithResponse(null, null, null).headers().value("x-ms-copy-status") == CopyStatusType.PENDING.toString()) {
            sleepIfRecord(1000)
        }
        HttpHeaders headers2 = bu2.getPropertiesWithResponse(null, null, null).headers()

        then:
        headers2.value("x-ms-copy-status") == CopyStatusType.SUCCESS.toString()
        headers2.value("x-ms-copy-completion-time") != null
        headers2.value("x-ms-copy-progress") != null
        headers2.value("x-ms-copy-source") != null
        validateBasicHeaders(headers)
        headers.value("x-ms-copy-id") != null
    }

    def "Copy min"() {
        expect:
        bu.startCopyFromURLWithResponse(bu.getBlobUrl(), null, null, null, null, null).statusCode() == 202
    }

    @Unroll
    def "Copy metadata"() {
        setup:
        BlobClient bu2 = cu.getBlockBlobClient(generateBlobName())
        Metadata metadata = new Metadata()
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2)
        }

        String status =
            bu2.startCopyFromURLWithResponse(bu.getBlobUrl(), metadata, null, null, null, null)
                .headers().value("x-ms-copy-status")

        OffsetDateTime start = OffsetDateTime.now()
        while (status != CopyStatusType.SUCCESS.toString()) {
            sleepIfRecord(1000)
            status = bu2.getPropertiesWithResponse(null, null, null).headers().value("x-ms-copy-status")
            OffsetDateTime currentTime = OffsetDateTime.now()
            if (status == CopyStatusType.FAILED.toString() || currentTime.minusMinutes(1) == start) {
                throw new Exception("Copy failed or took too long")
            }
        }

        expect:
        bu2.getProperties().metadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @Unroll
    def "Copy source AC"() {
        setup:
        BlobClient bu2 = cu.getBlockBlobClient(generateBlobName())
        match = setupBlobMatchCondition(bu, match)
        def mac = new ModifiedAccessConditions()
            .ifModifiedSince(modified)
            .ifUnmodifiedSince(unmodified)
            .ifMatch(match)
            .ifNoneMatch(noneMatch)

        expect:
        bu2.startCopyFromURLWithResponse(bu.getBlobUrl(), null, mac, null, null, null).statusCode() == 202

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
        BlobClient bu2 = cu.getBlockBlobClient(generateBlobName())
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        def mac = new ModifiedAccessConditions()
            .ifModifiedSince(modified)
            .ifUnmodifiedSince(unmodified)
            .ifMatch(match)
            .ifNoneMatch(noneMatch)

        when:
        bu2.startCopyFromURLWithResponse(bu.getBlobUrl(), null, mac, null, null, null)

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
        BlobClient bu2 = cu.getBlockBlobClient(generateBlobName())
        bu2.upload(defaultInputStream.get(), defaultDataSize)
        match = setupBlobMatchCondition(bu2, match)
        leaseID = setupBlobLeaseCondition(bu2, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))


        expect:
        bu2.startCopyFromURLWithResponse(bu.getBlobUrl(), null, null, bac, null, null).statusCode() == 202

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
        BlobClient bu2 = cu.getBlockBlobClient(generateBlobName())
        bu2.upload(defaultInputStream.get(), defaultDataSize)
        noneMatch = setupBlobMatchCondition(bu2, noneMatch)
        setupBlobLeaseCondition(bu2, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))

        when:
        bu2.startCopyFromURLWithResponse(bu.getBlobUrl(), null, null, bac, null, null)

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

    def "Abort copy lease fail"() {
        // Data has to be large enough and copied between accounts to give us enough time to abort
        bu.asBlockBlobClient()
            .upload(new ByteArrayInputStream(getRandomByteArray(8 * 1024 * 1024)), 8 * 1024 * 1024)
        // So we don't have to create a SAS.
        cu.setAccessPolicy(PublicAccessType.BLOB, null)

        ContainerClient cu2 = alternateServiceClient.getContainerClient(generateBlobName())
        cu2.create()
        BlockBlobClient bu2 = cu2.getBlockBlobClient(generateBlobName())
        bu2.upload(defaultInputStream.get(), defaultDataSize)
        String leaseID = setupBlobLeaseCondition(bu2, receivedLeaseID)

        when:
        String copyID =
            bu2.startCopyFromURLWithResponse(bu.getBlobUrl(), null, null,
                new BlobAccessConditions().leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID)), null, null).value()
        bu2.abortCopyFromURLWithResponse(copyID, new LeaseAccessConditions().leaseId(garbageLeaseID), null, null)

        then:
        def e = thrown(StorageException)
        e.statusCode() == 412
        cu2.delete()
    }

    def "Abort copy"() {
        setup:
        // Data has to be large enough and copied between accounts to give us enough time to abort
        bu.asBlockBlobClient().upload(new ByteArrayInputStream(getRandomByteArray(8 * 1024 * 1024)), 8 * 1024 * 1024)
        // So we don't have to create a SAS.
        cu.setAccessPolicy(PublicAccessType.BLOB, null)

        ContainerClient cu2 = alternateServiceClient.getContainerClient(generateBlobName())
        cu2.create()
        BlobClient bu2 = cu2.getBlobClient(generateBlobName())

        when:
        String copyID = bu2.startCopyFromURL(bu.getBlobUrl())
        VoidResponse response = bu2.abortCopyFromURLWithResponse(copyID, null, null, null)
        HttpHeaders headers = response.headers()

        then:
        response.statusCode() == 204
        headers.value("x-ms-request-id") != null
        headers.value("x-ms-version") != null
        headers.value("Date") != null
        // Normal test cleanup will not clean up containers in the alternate account.
        cu2.deleteWithResponse(null, null, null).statusCode() == 202
    }

    def "Abort copy min"() {
        setup:
        // Data has to be large enough and copied between accounts to give us enough time to abort
        bu.asBlockBlobClient().upload(new ByteArrayInputStream(getRandomByteArray(8 * 1024 * 1024)), 8 * 1024 * 1024)
        // So we don't have to create a SAS.
        cu.setAccessPolicy(PublicAccessType.BLOB, null)

        ContainerClient cu2 = alternateServiceClient.getContainerClient(generateBlobName())
        cu2.create()
        BlobClient bu2 = cu2.getBlobClient(generateBlobName())

        when:
        String copyID = bu2.startCopyFromURL(bu.getBlobUrl())

        then:
        bu2.abortCopyFromURLWithResponse(copyID, null, null, null).statusCode() == 204
    }

    def "Abort copy lease"() {
        setup:
        // Data has to be large enough and copied between accounts to give us enough time to abort
        bu.asBlockBlobClient().upload(new ByteArrayInputStream(getRandomByteArray(8 * 1024 * 1024)), 8 * 1024 * 1024)
        // So we don't have to create a SAS.
        cu.setAccessPolicy(PublicAccessType.BLOB, null)

        ContainerClient cu2 = alternateServiceClient.getContainerClient(generateContainerName())
        cu2.create()
        BlockBlobClient bu2 = cu2.getBlockBlobClient(generateBlobName())
        bu2.upload(defaultInputStream.get(), defaultDataSize)
        String leaseID = setupBlobLeaseCondition(bu2, receivedLeaseID)

        when:
        String copyID =
            bu2.startCopyFromURLWithResponse(bu.getBlobUrl(), null, null,
                new BlobAccessConditions().leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID)), null, null).value()

        then:
        bu2.abortCopyFromURLWithResponse(copyID, new LeaseAccessConditions().leaseId(leaseID), null, null).statusCode() == 204
        // Normal test cleanup will not clean up containers in the alternate account.
        cu2.delete()
    }

    def "Copy error"() {
        setup:
        bu = cu.getBlockBlobClient(generateBlobName())

        when:
        bu.startCopyFromURL(new URL("http://www.error.com"))

        then:
        thrown(StorageException)
    }

    def "Abort copy error"() {
        setup:
        bu = cu.getBlockBlobClient(generateBlobName())

        when:
        bu.abortCopyFromURL("id")

        then:
        thrown(StorageException)
    }

    def "Sync copy"() {
        setup:
        // Sync copy is a deep copy, which requires either sas or public access.
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null)
        BlobClient bu2 = cu.getBlockBlobClient(generateBlobName())
        HttpHeaders headers = bu2.copyFromURLWithResponse(bu.getBlobUrl(), null, null, null, null, null).headers()

        expect:
        headers.value("x-ms-copy-status") == SyncCopyStatusType.SUCCESS.toString()
        headers.value("x-ms-copy-id") != null
        validateBasicHeaders(headers)
    }

    def "Sync copy min"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null)
        BlobClient bu2 = cu.getBlockBlobClient(generateBlobName())

        expect:
        bu2.copyFromURLWithResponse(bu.getBlobUrl(), null, null, null, null, null).statusCode() == 202
    }

    @Unroll
    def "Sync copy metadata"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null)
        BlobClient bu2 = cu.getBlockBlobClient(generateBlobName())
        Metadata metadata = new Metadata()
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2)
        }

        when:
        bu2.copyFromURLWithResponse(bu.getBlobUrl(), metadata, null, null, null, null)

        then:
        bu2.getProperties().metadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @Unroll
    def "Sync copy source AC"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null)
        BlobClient bu2 = cu.getBlockBlobClient(generateBlobName())
        match = setupBlobMatchCondition(bu, match)
        def mac = new ModifiedAccessConditions()
            .ifModifiedSince(modified)
            .ifUnmodifiedSince(unmodified)
            .ifMatch(match)
            .ifNoneMatch(noneMatch)

        expect:
        bu2.copyFromURLWithResponse(bu.getBlobUrl(), null, mac, null, null, null).statusCode() == 202

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
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null)
        BlobClient bu2 = cu.getBlockBlobClient(generateBlobName())
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        def mac = new ModifiedAccessConditions()
            .ifModifiedSince(modified)
            .ifUnmodifiedSince(unmodified)
            .ifMatch(match)
            .ifNoneMatch(noneMatch)

        when:
        bu2.copyFromURLWithResponse(bu.getBlobUrl(), null, mac, null, null, null)

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
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null)
        BlobClient bu2 = cu.getBlockBlobClient(generateBlobName())
        bu2.upload(defaultInputStream.get(), defaultDataSize)
        match = setupBlobMatchCondition(bu2, match)
        leaseID = setupBlobLeaseCondition(bu2, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))

        expect:
        bu2.copyFromURLWithResponse(bu.getBlobUrl(), null, null, bac, null, null).statusCode() == 202

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
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null)
        BlobClient bu2 = cu.getBlockBlobClient(generateBlobName())
        bu2.upload(defaultInputStream.get(), defaultDataSize)
        noneMatch = setupBlobMatchCondition(bu2, noneMatch)
        setupBlobLeaseCondition(bu2, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))

        when:
        bu2.copyFromURLWithResponse(bu.getBlobUrl(), null, null, bac, null, null)

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
        def bu2 = cu.getBlockBlobClient(generateBlobName())

        when:
        bu2.copyFromURL(bu.getBlobUrl())

        then:
        thrown(StorageException)
    }

    def "Delete"() {
        when:
        VoidResponse response = bu.deleteWithResponse(null, null, null, null)
        HttpHeaders headers = response.headers()

        then:
        response.statusCode() == 202
        headers.value("x-ms-request-id") != null
        headers.value("x-ms-version") != null
        headers.value("Date") != null
    }

    def "Delete min"() {
        expect:
        bu.deleteWithResponse(null, null, null, null).statusCode() == 202
    }

    @Unroll
    def "Delete options"() {
        setup:
        bu.createSnapshot()
        // Create an extra blob so the list isn't empty (null) when we delete base blob, too
        BlockBlobClient bu2 = cu.getBlockBlobClient(generateBlobName())
        bu2.upload(defaultInputStream.get(), defaultDataSize)

        when:
        bu.deleteWithResponse(option, null, null, null)

        then:
        Iterator<BlobItem> blobs = cu.listBlobsFlat().iterator()

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
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))

        expect:
        bu.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, bac, null, null).statusCode() == 202

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
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))

        when:
        bu.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, bac, null, null)

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
        bu = cu.getBlockBlobClient(generateBlobName())

        when:
        bu.delete()

        then:
        thrown(StorageException)
    }

    @Unroll
    def "Set tier block blob"() {
        setup:
        ContainerClient cu = blobStorageServiceClient.getContainerClient(generateContainerName())
        BlockBlobClient bu = cu.getBlockBlobClient(generateBlobName())
        cu.create()
        bu.upload(defaultInputStream.get(), defaultData.remaining())

        when:
        VoidResponse initialResponse = bu.setTierWithResponse(tier, null, null, null)
        HttpHeaders headers = initialResponse.headers()

        then:
        initialResponse.statusCode() == 200 || initialResponse.statusCode() == 202
        headers.value("x-ms-version") != null
        headers.value("x-ms-request-id") != null
        bu.getPropertiesWithResponse(null, null, null).headers().value("x-ms-access-tier") == tier.toString()
        cu.listBlobsFlat().iterator().next().properties().accessTier() == tier

        where:
        tier               | _
        AccessTier.HOT     | _
        AccessTier.COOL    | _
        AccessTier.ARCHIVE | _
    }

    @Unroll
    def "Set tier page blob"() {
        setup:
        ContainerClient cu = premiumServiceClient.getContainerClient(generateContainerName())
        cu.create()

        PageBlobClient bu = cu.getPageBlobClient(generateBlobName())
        bu.create(512)

        when:
        bu.setTier(tier)

        then:
        bu.getPropertiesWithResponse(null, null, null).headers().value("x-ms-access-tier") == tier.toString()
        cu.listBlobsFlat().iterator().next().properties().accessTier() == tier
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
    }

    def "Set tier min"() {
        setup:
        ContainerClient cu = blobStorageServiceClient.getContainerClient(generateContainerName())
        BlockBlobClient bu = cu.getBlockBlobClient(generateBlobName())
        cu.create()
        bu.upload(defaultInputStream.get(), defaultData.remaining())

        when:
        int statusCode = bu.setTierWithResponse(AccessTier.HOT, null, null, null).statusCode()

        then:
        statusCode == 200 || statusCode == 202
    }

    def "Set tier inferred"() {
        setup:
        ContainerClient cu = blobStorageServiceClient.getContainerClient(generateBlobName())
        BlockBlobClient bu = cu.getBlockBlobClient(generateBlobName())
        cu.create()
        bu.upload(defaultInputStream.get(), defaultDataSize)

        when:
        boolean inferred1 = Boolean.parseBoolean(bu.getPropertiesWithResponse(null, null, null).headers().value("x-ms-access-tier-inferred"))
        Boolean inferredList1 = cu.listBlobsFlat().iterator().next().properties().accessTierInferred()

        bu.setTier(AccessTier.HOT)

        boolean inferred2 = Boolean.parseBoolean(bu.getPropertiesWithResponse(null, null, null).headers().value("x-ms-access-tier-inferred"))
        Boolean inferredList2 = cu.listBlobsFlat().iterator().next().properties().accessTierInferred()

        then:
        inferred1
        inferredList1
        !inferred2
        inferredList2 == null
    }

    @Unroll
    def "Set tier archive status"() {
        setup:
        ContainerClient cu = blobStorageServiceClient.getContainerClient(generateBlobName())
        BlockBlobClient bu = cu.getBlockBlobClient(generateBlobName())
        cu.create()
        bu.upload(defaultInputStream.get(), defaultDataSize)

        when:
        bu.setTier(sourceTier)
        bu.setTier(destTier)

        then:
        bu.getPropertiesWithResponse(null, null, null).headers().value("x-ms-archive-status") == status.toString()
        cu.listBlobsFlat().iterator().next().properties().archiveStatus() == status

        where:
        sourceTier         | destTier        | status
        AccessTier.ARCHIVE | AccessTier.COOL | ArchiveStatus.REHYDRATE_PENDING_TO_COOL
        AccessTier.ARCHIVE | AccessTier.HOT  | ArchiveStatus.REHYDRATE_PENDING_TO_HOT
    }

    def "Set tier error"() {
        setup:
        ContainerClient cu = blobStorageServiceClient.getContainerClient(generateBlobName())
        BlockBlobClient bu = cu.getBlockBlobClient(generateBlobName())
        cu.create()
        bu.upload(defaultInputStream.get(), defaultDataSize)

        when:
        bu.setTier(AccessTier.fromString("garbage"))

        then:
        def e = thrown(StorageException)
        e.errorCode() == StorageErrorCode.INVALID_HEADER_VALUE
    }

    def "Set tier illegal argument"() {
        when:
        bu.setTier(null)

        then:
        thrown(IllegalArgumentException)
    }

    def "Set tier lease"() {
        setup:
        ContainerClient cu = blobStorageServiceClient.getContainerClient(generateBlobName())
        BlockBlobClient bu = cu.getBlockBlobClient(generateBlobName())
        cu.create()
        bu.upload(defaultInputStream.get(), defaultDataSize)
        def leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)

        when:
        bu.setTierWithResponse(AccessTier.HOT, new LeaseAccessConditions().leaseId(leaseID), null, null)

        then:
        notThrown(StorageException)
    }

    def "Set tier lease fail"() {
        setup:
        ContainerClient cu = blobStorageServiceClient.getContainerClient(generateBlobName())
        BlockBlobClient bu = cu.getBlockBlobClient(generateBlobName())
        cu.create()
        bu.upload(defaultInputStream.get(), defaultDataSize)

        when:
        bu.setTierWithResponse(AccessTier.HOT, new LeaseAccessConditions().leaseId("garbage"), null, null)

        then:
        thrown(StorageException)
    }

    def "Undelete"() {
        setup:
        enableSoftDelete()
        bu.delete()

        when:
        HttpHeaders headers = bu.undeleteWithResponse(null, null).headers()
        bu.getProperties()

        then:
        notThrown(StorageException)
        headers.value("x-ms-request-id") != null
        headers.value("x-ms-version") != null
        headers.value("Date") != null

        disableSoftDelete() == null
    }

    def "Undelete min"() {
        setup:
        enableSoftDelete()
        bu.delete()

        expect:
        bu.undeleteWithResponse(null, null).statusCode() == 200
    }

    def "Undelete error"() {
        bu = cu.getBlockBlobClient(generateBlobName())

        when:
        bu.undelete()

        then:
        thrown(StorageException)
    }

    def "Get account info"() {
        when:
        Response<StorageAccountInfo> response = primaryServiceClient.getAccountInfoWithResponse(null, null)

        then:
        response.headers().value("Date") != null
        response.headers().value("x-ms-request-id") != null
        response.headers().value("x-ms-version") != null
        response.value().accountKind() != null
        response.value().skuName() != null
    }

    def "Get account info min"() {
        expect:
        bu.getAccountInfoWithResponse(null, null).statusCode() == 200
    }

    def "Get account info error"() {
        when:
        BlobServiceClient serviceURL = getServiceClient(primaryServiceClient.getAccountUrl().toString())

        serviceURL.getContainerClient(generateContainerName()).getBlobClient(generateBlobName()).getAccountInfo()

        then:
        thrown(StorageException)
    }
}
