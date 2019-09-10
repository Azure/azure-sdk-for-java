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
import com.azure.storage.blob.models.RehydratePriority
import com.azure.storage.blob.models.ReliableDownloadOptions
import com.azure.storage.blob.models.StorageAccountInfo
import com.azure.storage.blob.models.StorageErrorCode
import com.azure.storage.blob.models.StorageException
import com.azure.storage.blob.models.SyncCopyStatusType
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.nio.file.FileAlreadyExistsException
import java.security.MessageDigest
import java.time.OffsetDateTime

class BlobAPITest extends APISpec {
    BlobClient bc

    def setup() {
        bc = cc.getBlockBlobClient(generateBlobName())
        bc.upload(defaultInputStream.get(), defaultDataSize)
    }

    def "Download all null"() {
        when:
        def stream = new ByteArrayOutputStream()
        def response = bc.downloadWithResponse(stream, null, null, null, false, null, null)
        def body = ByteBuffer.wrap(stream.toByteArray())
        def headers = response.headers()

        then:
        body == defaultData
        headers.toMap().keySet().stream().noneMatch({ it.startsWith("x-ms-meta-") })
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
        bc = cc.getAppendBlobClient("emptyAppendBlob")
        bc.create()

        when:
        def outStream = new ByteArrayOutputStream()
        bc.download(outStream)
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
        BlobClient bu2 = getBlobClient(primaryCredential, bc.getBlobUrl().toString(), new MockRetryRangeResponsePolicy())

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
        bc.download(outStream)
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
        bc.downloadWithResponse(outStream, range, null, null, false, null, null)
        String bodyStr = outStream.toString()

        then:
        bodyStr == expectedData

        where:
        offset | count || expectedData
        0      | null  || defaultText
        0      | 5L    || defaultText.substring(0, 5)
        3      | 2L    || defaultText.substring(3, 3 + 2)
    }

    @Unroll
    def "Download AC"() {
        setup:
        match = setupBlobMatchCondition(bc, match)
        leaseID = setupBlobLeaseCondition(bc, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions().ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))

        when:
        def response = bc.downloadWithResponse(new ByteArrayOutputStream(), null, null, bac, false, null, null)

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
        setupBlobLeaseCondition(bc, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(setupBlobMatchCondition(bc, noneMatch)))

        when:
        bc.downloadWithResponse(new ByteArrayOutputStream(), null, null, bac, false, null, null).statusCode()

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
        VoidResponse response = bc.downloadWithResponse(new ByteArrayOutputStream(), new BlobRange(0, 3), null, null, true, null, null)
        byte[] contentMD5 = response.headers().value("content-md5").getBytes()

        then:
        contentMD5 == Base64.getEncoder().encode(MessageDigest.getInstance("MD5").digest(defaultText.substring(0, 3).getBytes()))
    }

    def "Download error"() {
        setup:
        bc = cc.getBlockBlobClient(generateBlobName())

        when:
        bc.download(null)

        then:
        thrown(StorageException)
    }

    def "Download snapshot"() {
        when:
        ByteArrayOutputStream originalStream = new ByteArrayOutputStream()
        bc.download(originalStream)

        def bc2 = bc.createSnapshot()
        bc.asBlockBlobClient().upload(new ByteArrayInputStream("ABC".getBytes()), 3)

        then:
        ByteArrayOutputStream snapshotStream = new ByteArrayOutputStream()
        bc2.download(snapshotStream)
        snapshotStream.toByteArray() == originalStream.toByteArray()
    }

    def "Download to file exists"() {
        setup:
        def testFile = new File(testName + ".txt")
        if (!testFile.exists()) {
            assert testFile.createNewFile()
        }

        when:
        bc.downloadToFile(testFile.getPath())

        then:
        def ex = thrown(UncheckedIOException)
        ex.getCause() instanceof FileAlreadyExistsException

        cleanup:
        testFile.delete()
    }

    def "Download to file does not exist"() {
        setup:
        def testFile = new File(testName + ".txt")
        if (testFile.exists()) {
            assert testFile.delete()
        }

        when:
        bc.downloadToFile(testFile.getPath())
        def fileContent = new Scanner(testFile).useDelimiter("\\Z").next()

        then:
        fileContent == defaultText

        cleanup:
        testFile.delete()
    }

    def "Get properties default"() {
        when:
        def response = bc.getPropertiesWithResponse(null, null, null)
        def headers = response.headers()
        def properties = response.value()

        then:
        validateBasicHeaders(headers)
        ImplUtils.isNullOrEmpty(properties.metadata())
        properties.blobType() == BlobType.BLOCK_BLOB
        properties.copyCompletionTime() == null // tested in "copy"
        properties.copyStatusDescription() == null // only returned when the service has errors; cannot validate.
        properties.copyId() == null // tested in "abort copy"
        properties.copyProgress() == null // tested in "copy"
        properties.copySource() == null // tested in "copy"
        properties.copyStatus() == null // tested in "copy"
        !properties.isIncrementalCopy() // tested in PageBlob."start incremental copy"
        properties.copyDestinationSnapshot() == null // tested in PageBlob."start incremental copy"
        properties.leaseDuration() == null // tested in "acquire lease"
        properties.leaseState() == LeaseStateType.AVAILABLE
        properties.leaseStatus() == LeaseStatusType.UNLOCKED
        properties.blobSize() >= 0
        properties.contentType() != null
        properties.contentMD5() != null
        properties.contentEncoding() == null // tested in "set HTTP headers"
        properties.contentDisposition() == null // tested in "set HTTP headers"
        properties.contentLanguage() == null // tested in "set HTTP headers"
        properties.cacheControl() == null // tested in "set HTTP headers"
        properties.blobSequenceNumber() == null // tested in PageBlob."create sequence number"
        headers.value("Accept-Ranges") == "bytes"
        properties.committedBlockCount() == null // tested in AppendBlob."append block"
        properties.isServerEncrypted()
        properties.accessTier() == AccessTier.HOT
        properties.isAccessTierInferred()
        properties.archiveStatus() == null
        properties.creationTime() != null
    }

    def "Get properties min"() {
        expect:
        bc.getPropertiesWithResponse(null, null, null).statusCode() == 200
    }

    @Unroll
    def "Get properties AC"() {
        setup:
        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(setupBlobLeaseCondition(bc, leaseID)))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(setupBlobMatchCondition(bc, match))
                .ifNoneMatch(noneMatch))

        expect:
        bc.getPropertiesWithResponse(bac, null, null).statusCode() == 200

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
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(setupBlobLeaseCondition(bc, leaseID)))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(setupBlobMatchCondition(bc, noneMatch)))

        when:
        bc.getPropertiesWithResponse(bac, null, null)

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
        bc = cc.getBlockBlobClient(generateBlobName())

        when:
        bc.getProperties()

        then:
        thrown(StorageException)
    }

    def "Set HTTP headers null"() {
        setup:
        VoidResponse response = bc.setHTTPHeadersWithResponse(null, null, null, null)

        expect:
        response.statusCode() == 200
        validateBasicHeaders(response.headers())
    }

    def "Set HTTP headers min"() {
        setup:
        BlobProperties properties = bc.getProperties()
        BlobHTTPHeaders headers = new BlobHTTPHeaders()
            .blobContentEncoding(properties.contentEncoding())
            .blobContentDisposition(properties.contentDisposition())
            .blobContentType("type")
            .blobCacheControl(properties.cacheControl())
            .blobContentLanguage(properties.contentLanguage())
            .blobContentMD5(Base64.getEncoder().encode(MessageDigest.getInstance("MD5").digest(defaultData.array())))

        bc.setHTTPHeaders(headers)

        expect:
        bc.getProperties().contentType() == "type"
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

        bc.setHTTPHeaders(putHeaders)

        expect:
        validateBlobProperties(
            bc.getPropertiesWithResponse(null, null, null),
            cacheControl, contentDisposition, contentEncoding, contentLanguage, contentMD5, contentType)

        where:
        cacheControl | contentDisposition | contentEncoding | contentLanguage | contentMD5                                                                               | contentType
        null         | null               | null            | null            | null                                                                                     | null
        "control"    | "disposition"      | "encoding"      | "language"      | Base64.getEncoder().encode(MessageDigest.getInstance("MD5").digest(defaultData.array())) | "type"
    }


    @Unroll
    def "Set HTTP headers AC"() {
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

        expect:
        bc.setHTTPHeadersWithResponse(null, bac, null, null).statusCode() == 200

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
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        setupBlobLeaseCondition(bc, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))

        when:
        bc.setHTTPHeadersWithResponse(null, bac, null, null)

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
        bc = cc.getBlockBlobClient(generateBlobName())

        when:
        bc.setHTTPHeaders(null)

        then:
        thrown(StorageException)
    }

    def "Set metadata all null"() {
        when:
        def response = bc.setMetadataWithResponse(null, null, null, null)

        then:
        bc.getProperties().metadata().size() == 0
        response.statusCode() == 200
        validateBasicHeaders(response.headers())
        Boolean.parseBoolean(response.headers().value("x-ms-request-server-encrypted"))
    }

    def "Set metadata min"() {
        setup:
        def metadata = new Metadata()
        metadata.put("foo", "bar")

        when:
        bc.setMetadata(metadata)

        then:
        bc.getProperties().metadata() == metadata
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
        bc.setMetadataWithResponse(metadata, null, null, null).statusCode() == statusCode
        bc.getProperties().metadata() == metadata

        where:
        key1  | value1 | key2   | value2 || statusCode
        null  | null   | null   | null   || 200
        "foo" | "bar"  | "fizz" | "buzz" || 200
    }

    @Unroll
    def "Set metadata AC"() {
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

        expect:
        bc.setMetadataWithResponse(null, bac, null, null).statusCode() == 200

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
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        setupBlobLeaseCondition(bc, leaseID)

        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))

        when:
        bc.setMetadataWithResponse(null, bac, null, null)

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
        bc = cc.getBlockBlobClient(generateBlobName())

        when:
        bc.setMetadata(null)

        then:
        thrown(StorageException)
    }

    @Unroll
    def "Acquire lease"() {
        when:
        def leaseId = bc.acquireLease(proposedID, leaseTime)

        then:
        leaseId != null

        when:
        def response = bc.getPropertiesWithResponse(null, null, null)
        def properties = response.value()
        def headers = response.headers()

        then:
        properties.leaseState() == leaseState
        properties.leaseDuration() == leaseDuration
        validateBasicHeaders(headers)

        where:
        proposedID                   | leaseTime || leaseState            | leaseDuration
        null                         | -1        || LeaseStateType.LEASED | LeaseDurationType.INFINITE
        null                         | 25        || LeaseStateType.LEASED | LeaseDurationType.FIXED
        UUID.randomUUID().toString() | -1        || LeaseStateType.LEASED | LeaseDurationType.INFINITE
    }

    def "Acquire lease min"() {
        expect:
        bc.acquireLeaseWithResponse(null, -1, null, null, null).statusCode() == 201
    }

    @Unroll
    def "Acquire lease AC"() {
        setup:
        match = setupBlobMatchCondition(bc, match)
        def mac = new ModifiedAccessConditions()
            .ifModifiedSince(modified)
            .ifUnmodifiedSince(unmodified)
            .ifMatch(match)
            .ifNoneMatch(noneMatch)

        expect:
        bc.acquireLeaseWithResponse(null, -1, mac, null, null).statusCode() == 201

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
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        def mac = new ModifiedAccessConditions()
            .ifModifiedSince(modified)
            .ifUnmodifiedSince(unmodified)
            .ifMatch(match)
            .ifNoneMatch(noneMatch)

        when:
        bc.acquireLeaseWithResponse(null, -1, mac, null, null)

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
        bc = cc.getBlockBlobClient(generateBlobName())

        when:
        bc.acquireLease(null, 20)

        then:
        thrown(StorageException)
    }

    def "Renew lease"() {
        setup:
        String leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)

        // If running in live mode wait for the lease to expire to ensure we are actually renewing it
        sleepIfRecord(16000)
        Response<String> renewLeaseResponse = bc.renewLeaseWithResponse(leaseID, null, null, null)

        expect:
        bc.getProperties().leaseState() == LeaseStateType.LEASED
        validateBasicHeaders(renewLeaseResponse.headers())
        renewLeaseResponse.value() != null
    }

    def "Renew lease min"() {
        setup:
        String leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)

        expect:
        bc.renewLeaseWithResponse(leaseID, null, null, null).statusCode() == 200
    }

    @Unroll
    def "Renew lease AC"() {
        setup:
        match = setupBlobMatchCondition(bc, match)
        String leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)
        def mac = new ModifiedAccessConditions()
            .ifModifiedSince(modified)
            .ifUnmodifiedSince(unmodified)
            .ifMatch(match)
            .ifNoneMatch(noneMatch)

        expect:
        bc.renewLeaseWithResponse(leaseID, mac, null, null).statusCode() == 200

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
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        String leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)
        def mac = new ModifiedAccessConditions()
            .ifModifiedSince(modified)
            .ifUnmodifiedSince(unmodified)
            .ifMatch(match)
            .ifNoneMatch(noneMatch)

        when:
        bc.renewLeaseWithResponse(leaseID, mac, null, null)

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
        bc = cc.getBlockBlobClient(generateBlobName())

        when:
        bc.renewLease("id")

        then:
        thrown(StorageException)
    }

    def "Release lease"() {
        setup:
        String leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)
        HttpHeaders headers = bc.releaseLeaseWithResponse(leaseID, null, null, null).headers()

        expect:
        bc.getProperties().leaseState() == LeaseStateType.AVAILABLE
        validateBasicHeaders(headers)
    }

    def "Release lease min"() {
        setup:
        String leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)

        expect:
        bc.releaseLeaseWithResponse(leaseID, null, null, null).statusCode() == 200
    }

    @Unroll
    def "Release lease AC"() {
        setup:
        match = setupBlobMatchCondition(bc, match)
        String leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)
        def mac = new ModifiedAccessConditions()
            .ifModifiedSince(modified)
            .ifUnmodifiedSince(unmodified)
            .ifMatch(match)
            .ifNoneMatch(noneMatch)

        expect:
        bc.releaseLeaseWithResponse(leaseID, mac, null, null).statusCode() == 200

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
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        String leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)
        def mac = new ModifiedAccessConditions()
            .ifModifiedSince(modified)
            .ifUnmodifiedSince(unmodified)
            .ifMatch(match)
            .ifNoneMatch(noneMatch)

        when:
        bc.releaseLeaseWithResponse(leaseID, mac, null, null)

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
        bc = cc.getBlockBlobClient(generateBlobName())

        when:
        bc.releaseLease("id")

        then:
        thrown(StorageException)
    }

    @Unroll
    def "Break lease"() {
        setup:
        bc.acquireLeaseWithResponse(getRandomUUID(), leaseTime, null, null, null)

        Response<Integer> breakLeaseResponse = bc.breakLeaseWithResponse(breakPeriod, null, null, null)
        def leaseState = bc.getProperties().leaseState()

        expect:
        leaseState == LeaseStateType.BROKEN || leaseState == LeaseStateType.BREAKING
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
        setupBlobLeaseCondition(bc, receivedLeaseID)

        expect:
        bc.breakLeaseWithResponse(null, null, null, null).statusCode() == 202
    }

    @Unroll
    def "Break lease AC"() {
        setup:
        match = setupBlobMatchCondition(bc, match)
        setupBlobLeaseCondition(bc, receivedLeaseID)
        def mac = new ModifiedAccessConditions()
            .ifModifiedSince(modified)
            .ifUnmodifiedSince(unmodified)
            .ifMatch(match)
            .ifNoneMatch(noneMatch)

        expect:
        bc.breakLeaseWithResponse(null, mac, null, null).statusCode() == 202

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
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        setupBlobLeaseCondition(bc, receivedLeaseID)
        def mac = new ModifiedAccessConditions()
            .ifModifiedSince(modified)
            .ifUnmodifiedSince(unmodified)
            .ifMatch(match)
            .ifNoneMatch(noneMatch)

        when:
        bc.breakLeaseWithResponse(null, mac, null, null)

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
        bc = cc.getBlockBlobClient(generateBlobName())

        when:
        bc.breakLease()

        then:
        thrown(StorageException)
    }

    def "Change lease"() {
        setup:
        String acquireLease = bc.acquireLease(getRandomUUID(), 15)
        Response<String> changeLeaseResponse = bc.changeLeaseWithResponse(acquireLease, getRandomUUID(), null, null, null)

        expect:
        bc.releaseLeaseWithResponse(changeLeaseResponse.value(), null, null, null).statusCode() == 200
        validateBasicHeaders(changeLeaseResponse.headers())
    }

    def "Change lease min"() {
        setup:
        def leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)

        expect:
        bc.changeLeaseWithResponse(leaseID, getRandomUUID(), null, null, null).statusCode() == 200
    }

    @Unroll
    def "Change lease AC"() {
        setup:
        match = setupBlobMatchCondition(bc, match)
        String leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)
        def mac = new ModifiedAccessConditions()
            .ifModifiedSince(modified)
            .ifUnmodifiedSince(unmodified)
            .ifMatch(match)
            .ifNoneMatch(noneMatch)

        expect:
        bc.changeLeaseWithResponse(leaseID, getRandomUUID(), mac, null, null).statusCode() == 200

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
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        String leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)
        def mac = new ModifiedAccessConditions()
            .ifModifiedSince(modified)
            .ifUnmodifiedSince(unmodified)
            .ifMatch(match)
            .ifNoneMatch(noneMatch)

        when:
        bc.changeLeaseWithResponse(leaseID, getRandomUUID(), mac, null, null)

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
        bc = cc.getBlockBlobClient(generateBlobName())

        when:
        bc.changeLease("id", "id")

        then:
        thrown(StorageException)
    }

    def "Snapshot"() {
        when:
        def response = bc.createSnapshotWithResponse(null, null, null, null)

        then:
        response.value().exists()
        validateBasicHeaders(response.headers())
    }

    def "Snapshot min"() {
        bc.createSnapshotWithResponse(null, null, null, null).statusCode() == 201
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

        def response = bc.createSnapshotWithResponse(metadata, null, null, null)
        def bcSnap = response.value()

        expect:
        response.statusCode() == 201
        bcSnap.getProperties().metadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @Unroll
    def "Snapshot AC"() {
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

        expect:
        bc.createSnapshotWithResponse(null, bac, null, null).statusCode() == 201

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
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        setupBlobLeaseCondition(bc, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))


        when:
        bc.createSnapshotWithResponse(null, bac, null, null)

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
        bc = cc.getBlockBlobClient(generateBlobName())

        when:
        bc.createSnapshot()

        then:
        thrown(StorageException)
    }

    def "Copy"() {
        setup:
        def copyDestBlob = cc.getBlockBlobClient(generateBlobName())
        def headers =
            copyDestBlob.startCopyFromURLWithResponse(bc.getBlobUrl(), null, null, null, null, null, null, null).headers()

        when:
        while (copyDestBlob.getProperties().copyStatus() == CopyStatusType.PENDING) {
            sleepIfRecord(1000)
        }
        def properties = copyDestBlob.getProperties()

        then:
        properties.copyStatus() == CopyStatusType.SUCCESS
        properties.copyCompletionTime() != null
        properties.copyProgress() != null
        properties.copySource() != null
        validateBasicHeaders(headers)
        headers.value("x-ms-copy-id") != null
    }

    def "Copy min"() {
        expect:
        bc.startCopyFromURLWithResponse(bc.getBlobUrl(), null, null, null, null, null, null, null).statusCode() == 202
    }

    @Unroll
    def "Copy metadata"() {
        setup:
        BlobClient bu2 = cc.getBlockBlobClient(generateBlobName())
        Metadata metadata = new Metadata()
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2)
        }

        def status = bu2.startCopyFromURLWithResponse(bc.getBlobUrl(), metadata, null, null, null, null, null, null)
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
        BlobClient copyDestBlob = cc.getBlockBlobClient(generateBlobName())
        match = setupBlobMatchCondition(bc, match)
        def mac = new ModifiedAccessConditions()
            .ifModifiedSince(modified)
            .ifUnmodifiedSince(unmodified)
            .ifMatch(match)
            .ifNoneMatch(noneMatch)

        expect:
        copyDestBlob.startCopyFromURLWithResponse(bc.getBlobUrl(), null, null, null, mac, null, null, null).statusCode() == 202

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
        BlobClient bu2 = cc.getBlockBlobClient(generateBlobName())
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        def mac = new ModifiedAccessConditions()
            .ifModifiedSince(modified)
            .ifUnmodifiedSince(unmodified)
            .ifMatch(match)
            .ifNoneMatch(noneMatch)

        when:
        bu2.startCopyFromURLWithResponse(bc.getBlobUrl(), null, null, null, mac, null, null, null)

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
        BlobClient bu2 = cc.getBlockBlobClient(generateBlobName())
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
        bu2.startCopyFromURLWithResponse(bc.getBlobUrl(), null, null, null, null, bac, null, null).statusCode() == 202

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
        BlobClient bu2 = cc.getBlockBlobClient(generateBlobName())
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
        bu2.startCopyFromURLWithResponse(bc.getBlobUrl(), null, null, null, null, bac, null, null)

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
        setup:
        // Data has to be large enough and copied between accounts to give us enough time to abort
        bc.asBlockBlobClient()
            .upload(new ByteArrayInputStream(getRandomByteArray(8 * 1024 * 1024)), 8 * 1024 * 1024)
        // So we don't have to create a SAS.
        cc.setAccessPolicy(PublicAccessType.BLOB, null)

        ContainerClient cu2 = alternateBlobServiceClient.getContainerClient(generateBlobName())
        cu2.create()
        BlockBlobClient bu2 = cu2.getBlockBlobClient(generateBlobName())
        bu2.upload(defaultInputStream.get(), defaultDataSize)
        String leaseID = setupBlobLeaseCondition(bu2, receivedLeaseID)

        when:
        String copyID =
            bu2.startCopyFromURLWithResponse(bc.getBlobUrl(), null, null, null, null,
                new BlobAccessConditions().leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID)), null, null).value()
        bu2.abortCopyFromURLWithResponse(copyID, new LeaseAccessConditions().leaseId(garbageLeaseID), null, null)

        then:
        def e = thrown(StorageException)
        e.statusCode() == 412

        cleanup:
        cu2.delete()
    }

    def "Abort copy"() {
        setup:
        // Data has to be large enough and copied between accounts to give us enough time to abort
        bc.asBlockBlobClient().upload(new ByteArrayInputStream(getRandomByteArray(8 * 1024 * 1024)), 8 * 1024 * 1024)
        // So we don't have to create a SAS.
        cc.setAccessPolicy(PublicAccessType.BLOB, null)

        ContainerClient cu2 = alternateBlobServiceClient.getContainerClient(generateBlobName())
        cu2.create()
        BlobClient bu2 = cu2.getBlobClient(generateBlobName())

        when:
        String copyID = bu2.startCopyFromURLWithResponse(bc.getBlobUrl(), null, null, null, null, null, null, null).value()
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
        bc.asBlockBlobClient().upload(new ByteArrayInputStream(getRandomByteArray(8 * 1024 * 1024)), 8 * 1024 * 1024)
        // So we don't have to create a SAS.
        cc.setAccessPolicy(PublicAccessType.BLOB, null)

        ContainerClient cu2 = alternateBlobServiceClient.getContainerClient(generateBlobName())
        cu2.create()
        BlobClient bu2 = cu2.getBlobClient(generateBlobName())

        when:
        String copyID = bu2.startCopyFromURL(bc.getBlobUrl())

        then:
        bu2.abortCopyFromURLWithResponse(copyID, null, null, null).statusCode() == 204
    }

    def "Abort copy lease"() {
        setup:
        // Data has to be large enough and copied between accounts to give us enough time to abort
        bc.asBlockBlobClient().upload(new ByteArrayInputStream(getRandomByteArray(8 * 1024 * 1024)), 8 * 1024 * 1024)
        // So we don't have to create a SAS.
        cc.setAccessPolicy(PublicAccessType.BLOB, null)

        ContainerClient cu2 = alternateBlobServiceClient.getContainerClient(generateContainerName())
        cu2.create()
        BlockBlobClient bu2 = cu2.getBlockBlobClient(generateBlobName())
        bu2.upload(defaultInputStream.get(), defaultDataSize)
        String leaseID = setupBlobLeaseCondition(bu2, receivedLeaseID)

        when:
        String copyID =
            bu2.startCopyFromURLWithResponse(bc.getBlobUrl(), null, null, null, null,
                new BlobAccessConditions().leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID)), null, null).value()

        then:
        bu2.abortCopyFromURLWithResponse(copyID, new LeaseAccessConditions().leaseId(leaseID), null, null).statusCode() == 204
        // Normal test cleanup will not clean up containers in the alternate account.
        cu2.delete()
    }

    def "Copy error"() {
        setup:
        bc = cc.getBlockBlobClient(generateBlobName())

        when:
        bc.startCopyFromURL(new URL("http://www.error.com"))

        then:
        thrown(StorageException)
    }

    def "Abort copy error"() {
        setup:
        bc = cc.getBlockBlobClient(generateBlobName())

        when:
        bc.abortCopyFromURL("id")

        then:
        thrown(StorageException)
    }

    def "Sync copy"() {
        setup:
        // Sync copy is a deep copy, which requires either sas or public access.
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        BlobClient bu2 = cc.getBlockBlobClient(generateBlobName())

        when:
        HttpHeaders headers = bu2.copyFromURLWithResponse(bc.getBlobUrl(), null, null, null, null, null, null).headers()

        then:
        headers.value("x-ms-copy-status") == SyncCopyStatusType.SUCCESS.toString()
        headers.value("x-ms-copy-id") != null
        validateBasicHeaders(headers)
    }

    def "Sync copy min"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        BlobClient bu2 = cc.getBlockBlobClient(generateBlobName())

        expect:
        bu2.copyFromURLWithResponse(bc.getBlobUrl(), null, null, null, null, null, null).statusCode() == 202
    }

    @Unroll
    def "Sync copy metadata"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        BlobClient bu2 = cc.getBlockBlobClient(generateBlobName())
        Metadata metadata = new Metadata()
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2)
        }

        when:
        bu2.copyFromURLWithResponse(bc.getBlobUrl(), metadata, null, null, null, null, null)

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
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        BlobClient bu2 = cc.getBlockBlobClient(generateBlobName())
        match = setupBlobMatchCondition(bc, match)
        def mac = new ModifiedAccessConditions()
            .ifModifiedSince(modified)
            .ifUnmodifiedSince(unmodified)
            .ifMatch(match)
            .ifNoneMatch(noneMatch)

        expect:
        bu2.copyFromURLWithResponse(bc.getBlobUrl(), null, null, mac, null, null, null).statusCode() == 202

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
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        BlobClient bu2 = cc.getBlockBlobClient(generateBlobName())
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        def mac = new ModifiedAccessConditions()
            .ifModifiedSince(modified)
            .ifUnmodifiedSince(unmodified)
            .ifMatch(match)
            .ifNoneMatch(noneMatch)

        when:
        bu2.copyFromURLWithResponse(bc.getBlobUrl(), null, null, mac, null, null, null)

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
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        BlobClient bu2 = cc.getBlockBlobClient(generateBlobName())
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
        bu2.copyFromURLWithResponse(bc.getBlobUrl(), null, null, null, bac, null, null).statusCode() == 202

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
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        BlobClient bu2 = cc.getBlockBlobClient(generateBlobName())
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
        bu2.copyFromURLWithResponse(bc.getBlobUrl(), null, null, null, bac, null, null)

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
        def bu2 = cc.getBlockBlobClient(generateBlobName())

        when:
        bu2.copyFromURL(bc.getBlobUrl())

        then:
        thrown(StorageException)
    }

    def "Delete"() {
        when:
        VoidResponse response = bc.deleteWithResponse(null, null, null, null)
        HttpHeaders headers = response.headers()

        then:
        response.statusCode() == 202
        headers.value("x-ms-request-id") != null
        headers.value("x-ms-version") != null
        headers.value("Date") != null
    }

    def "Delete min"() {
        expect:
        bc.deleteWithResponse(null, null, null, null).statusCode() == 202
    }

    @Unroll
    def "Delete options"() {
        setup:
        bc.createSnapshot()
        // Create an extra blob so the list isn't empty (null) when we delete base blob, too
        BlockBlobClient bu2 = cc.getBlockBlobClient(generateBlobName())
        bu2.upload(defaultInputStream.get(), defaultDataSize)

        when:
        bc.deleteWithResponse(option, null, null, null)

        then:
        cc.listBlobsFlat().stream().count() == blobsRemaining

        where:
        option                            | blobsRemaining
        DeleteSnapshotsOptionType.INCLUDE | 1
        DeleteSnapshotsOptionType.ONLY    | 2
    }

    @Unroll
    def "Delete AC"() {
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

        expect:
        bc.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, bac, null, null).statusCode() == 202

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
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        setupBlobLeaseCondition(bc, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))

        when:
        bc.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, bac, null, null)

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
        bc = cc.getBlockBlobClient(generateBlobName())

        when:
        bc.delete()

        then:
        thrown(StorageException)
    }

    @Unroll
    def "Set tier block blob"() {
        setup:
        ContainerClient cc = blobServiceClient.createContainer(generateContainerName())
        BlockBlobClient bc = cc.getBlockBlobClient(generateBlobName())
        bc.upload(defaultInputStream.get(), defaultData.remaining())

        when:
        VoidResponse initialResponse = bc.setTierWithResponse(tier, null, null, null, null)
        HttpHeaders headers = initialResponse.headers()

        then:
        initialResponse.statusCode() == 200 || initialResponse.statusCode() == 202
        headers.value("x-ms-version") != null
        headers.value("x-ms-request-id") != null
        bc.getProperties().accessTier() == tier
        cc.listBlobsFlat().iterator().next().properties().accessTier() == tier

        cleanup:
        cc.delete()

        where:
        tier               | _
        AccessTier.HOT     | _
        AccessTier.COOL    | _
        AccessTier.ARCHIVE | _
    }

    @Unroll
    def "Set tier page blob"() {
        setup:
        ContainerClient cc = premiumBlobServiceClient.createContainer(generateContainerName())

        def bc = cc.getPageBlobClient(generateBlobName())
        bc.create(512)

        when:
        bc.setTier(tier)

        then:
        bc.getProperties().accessTier() == tier
        cc.listBlobsFlat().iterator().next().properties().accessTier() == tier

        cleanup:
        cc.delete()

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
        ContainerClient cc = blobServiceClient.createContainer(generateContainerName())
        BlockBlobClient bu = cc.getBlockBlobClient(generateBlobName())
        bu.upload(defaultInputStream.get(), defaultData.remaining())

        when:
        int statusCode = bc.setTierWithResponse(AccessTier.HOT, null, null, null, null).statusCode()

        then:
        statusCode == 200 || statusCode == 202

        cleanup:
        cc.delete()
    }

    def "Set tier inferred"() {
        setup:
        def cc = blobServiceClient.createContainer(generateBlobName())
        def bc = cc.getBlockBlobClient(generateBlobName())
        bc.upload(defaultInputStream.get(), defaultDataSize)

        when:
        boolean inferred1 = bc.getProperties().isAccessTierInferred()
        Boolean inferredList1 = cc.listBlobsFlat().iterator().next().properties().accessTierInferred()

        bc.setTier(AccessTier.HOT)

        boolean inferred2 = bc.getProperties().isAccessTierInferred()
        Boolean inferredList2 = cc.listBlobsFlat().iterator().next().properties().accessTierInferred()

        then:
        inferred1
        inferredList1
        !inferred2
        inferredList2 == null
    }

    @Unroll
    def "Set tier archive status"() {
        setup:
        def cc = blobServiceClient.createContainer(generateBlobName())
        def bc = cc.getBlockBlobClient(generateBlobName())
        bc.upload(defaultInputStream.get(), defaultDataSize)

        when:
        bc.setTier(sourceTier)
        bc.setTier(destTier)

        then:
        bc.getProperties().archiveStatus() == status
        cc.listBlobsFlat().iterator().next().properties().archiveStatus() == status

        where:
        sourceTier         | destTier        | priority                   | status
        AccessTier.ARCHIVE | AccessTier.COOL | RehydratePriority.STANDARD | ArchiveStatus.REHYDRATE_PENDING_TO_COOL
        AccessTier.ARCHIVE | AccessTier.HOT  | RehydratePriority.STANDARD | ArchiveStatus.REHYDRATE_PENDING_TO_HOT
        AccessTier.ARCHIVE | AccessTier.HOT  | RehydratePriority.HIGH     | ArchiveStatus.REHYDRATE_PENDING_TO_HOT
    }

    def "Set tier error"() {
        setup:
        def cc = blobServiceClient.createContainer(generateContainerName())
        def bc = cc.getBlockBlobClient(generateBlobName())
        bc.upload(defaultInputStream.get(), defaultDataSize)

        when:
        bc.setTier(AccessTier.fromString("garbage"))

        then:
        def e = thrown(StorageException)
        e.errorCode() == StorageErrorCode.INVALID_HEADER_VALUE

        cleanup:
        cc.delete()
    }

    def "Set tier illegal argument"() {
        when:
        bc.setTier(null)

        then:
        thrown(IllegalArgumentException)
    }

    def "Set tier lease"() {
        setup:

        def cc = blobServiceClient.createContainer(generateContainerName())
        def bc = cc.getBlockBlobClient(generateBlobName())
        bc.upload(defaultInputStream.get(), defaultDataSize)
        def leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)

        when:
        bc.setTierWithResponse(AccessTier.HOT, null, new LeaseAccessConditions().leaseId(leaseID), null, null)

        then:
        notThrown(StorageException)

        cleanup:
        cc.delete()
    }

    def "Set tier lease fail"() {
        setup:
        def cc = blobServiceClient.createContainer(generateContainerName())
        def bc = cc.getBlockBlobClient(generateBlobName())
        bc.upload(defaultInputStream.get(), defaultDataSize)

        when:
        bc.setTierWithResponse(AccessTier.HOT, null, new LeaseAccessConditions().leaseId("garbage"), null, null)

        then:
        thrown(StorageException)
    }

    @Unroll
    def "Copy with tier"() {
        setup:
        def bc = cc.getBlockBlobClient(generateBlobName())
        bc.uploadWithResponse(defaultInputStream.get(), defaultDataSize, null, null, tier1, null, null, null)
        def bcCopy = cc.getBlockBlobClient(generateBlobName())

        when:
        bcCopy.copyFromURLWithResponse(new URL(bc.getBlobUrl().toString() + "?" + bc.generateSAS(OffsetDateTime.now().plusHours(1), new BlobSASPermission().read(true))), null, tier2, null, null, null, null)

        then:
        bcCopy.getProperties().accessTier() == tier2

        where:
        tier1           | tier2
        AccessTier.HOT  | AccessTier.COOL
        AccessTier.COOL | AccessTier.HOT
    }

    def "Undelete"() {
        setup:
        enableSoftDelete()
        bc.delete()

        when:
        def undeleteHeaders = bc.undeleteWithResponse(null, null).headers()
        bc.getProperties()

        then:
        notThrown(StorageException)
        undeleteHeaders.value("x-ms-request-id") != null
        undeleteHeaders.value("x-ms-version") != null
        undeleteHeaders.value("Date") != null

        disableSoftDelete() == null
    }

    def "Undelete min"() {
        setup:
        enableSoftDelete()
        bc.delete()

        expect:
        bc.undeleteWithResponse(null, null).statusCode() == 200
    }

    def "Undelete error"() {
        bc = cc.getBlockBlobClient(generateBlobName())

        when:
        bc.undelete()

        then:
        thrown(StorageException)
    }

    def "Get account info"() {
        when:
        Response<StorageAccountInfo> response = primaryBlobServiceClient.getAccountInfoWithResponse(null, null)

        then:
        response.headers().value("Date") != null
        response.headers().value("x-ms-request-id") != null
        response.headers().value("x-ms-version") != null
        response.value().accountKind() != null
        response.value().skuName() != null
    }

    def "Get account info min"() {
        expect:
        bc.getAccountInfoWithResponse(null, null).statusCode() == 200
    }

    def "Get account info error"() {
        when:
        BlobServiceClient serviceURL = getServiceClient(primaryBlobServiceClient.getAccountUrl().toString())

        serviceURL.getContainerClient(generateContainerName()).getBlobClient(generateBlobName()).getAccountInfo()

        then:
        thrown(StorageException)
    }
}
