// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.core.implementation.util.ImplUtils
import com.azure.core.util.Context
import com.azure.storage.blob.models.AccessTier
import com.azure.storage.blob.models.ArchiveStatus
import com.azure.storage.blob.models.BlobAccessConditions
import com.azure.storage.blob.models.BlobHTTPHeaders
import com.azure.storage.blob.models.BlobRange
import com.azure.storage.blob.models.BlobType
import com.azure.storage.blob.models.CopyStatusType
import com.azure.storage.blob.models.DeleteSnapshotsOptionType
import com.azure.storage.blob.models.LeaseAccessConditions
import com.azure.storage.blob.models.LeaseStateType
import com.azure.storage.blob.models.LeaseStatusType
import com.azure.storage.blob.models.ModifiedAccessConditions
import com.azure.storage.blob.models.PublicAccessType
import com.azure.storage.blob.models.RehydratePriority
import com.azure.storage.blob.models.ReliableDownloadOptions
import com.azure.storage.blob.models.StorageErrorCode
import com.azure.storage.blob.models.StorageException
import com.azure.storage.blob.models.SyncCopyStatusType
import com.azure.storage.blob.specialized.BlobClientBase
import com.azure.storage.blob.specialized.BlobServiceSasSignatureValues
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder
import reactor.test.StepVerifier
import spock.lang.Ignore
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.nio.file.FileAlreadyExistsException
import java.security.MessageDigest
import java.time.OffsetDateTime

class BlobAPITest extends APISpec {
    BlobClientBase bc
    String blobName

    def setup() {
        blobName = generateBlobName()
        bc = cc.getBlobClient(blobName).getBlockBlobClient()
        bc.upload(defaultInputStream.get(), defaultDataSize)
    }

    def "Download all null"() {
        when:
        def stream = new ByteArrayOutputStream()
        def response = bc.downloadWithResponse(stream, null, null, null, false, null, null)
        def body = ByteBuffer.wrap(stream.toByteArray())
        def headers = response.getHeaders()

        then:
        body == defaultData
        headers.toMap().keySet().stream().noneMatch({ it.startsWith("x-ms-meta-") })
        headers.getValue("Content-Length") != null
        headers.getValue("Content-Type") != null
        headers.getValue("Content-Range") == null
        headers.getValue("Content-MD5") != null
        headers.getValue("Content-Encoding") == null
        headers.getValue("Cache-Control") == null
        headers.getValue("Content-Disposition") == null
        headers.getValue("Content-Language") == null
        headers.getValue("x-ms-blob-sequence-number") == null
        headers.getValue("x-ms-blob-type") == BlobType.BLOCK_BLOB.toString()
        headers.getValue("x-ms-copy-completion-time") == null
        headers.getValue("x-ms-copy-status-description") == null
        headers.getValue("x-ms-copy-id") == null
        headers.getValue("x-ms-copy-progress") == null
        headers.getValue("x-ms-copy-source") == null
        headers.getValue("x-ms-copy-status") == null
        headers.getValue("x-ms-lease-duration") == null
        headers.getValue("x-ms-lease-state") == LeaseStateType.AVAILABLE.toString()
        headers.getValue("x-ms-lease-status") == LeaseStatusType.UNLOCKED.toString()
        headers.getValue("Accept-Ranges") == "bytes"
        headers.getValue("x-ms-blob-committed-block-count") == null
        headers.getValue("x-ms-server-encrypted") != null
        headers.getValue("x-ms-blob-content-md5") == null
    }

    def "Download empty file"() {
        setup:
        bc = cc.getBlobClient("emptyAppendBlob").getAppendBlobClient()
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
    HttpGetterInfo.
     */
    def "Download with retry range"() {
        /*
        We are going to make a request for some range on a blob. The Flux returned will throw an exception, forcing
        a retry per the ReliableDownloadOptions. The next request should have the same range header, which was generated
        from the count and offset values in HttpGetterInfo that was constructed on the initial call to download. We
        don't need to check the data here, but we want to ensure that the correct range is set each time. This will
        test the correction of a bug that was found which caused HttpGetterInfo to have an incorrect offset when it was
        constructed in BlobClient.download().
         */
        setup:
        def bu2 = getBlobClient(primaryCredential, bc.getBlobUrl(), new MockRetryRangeResponsePolicy())

        when:
        def range = new BlobRange(2, 5L)
        def options = new ReliableDownloadOptions().maxRetryRequests(3)
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
        def result = outStream.toByteArray()

        then:
        result == defaultData.array()
    }

    @Unroll
    def "Download range"() {
        setup:
        def range = (count == null) ? new BlobRange(offset) : new BlobRange(offset, count)

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
        def bac = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseID))
            .setModifiedAccessConditions(new ModifiedAccessConditions().setIfModifiedSince(modified)
                .setIfUnmodifiedSince(unmodified)
                .setIfMatch(match)
                .setIfNoneMatch(noneMatch))

        when:
        def response = bc.downloadWithResponse(new ByteArrayOutputStream(), null, null, bac, false, null, null)

        then:
        response.getStatusCode() == 200

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
        def bac = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseID))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfModifiedSince(modified)
                .setIfUnmodifiedSince(unmodified)
                .setIfMatch(match)
                .setIfNoneMatch(setupBlobMatchCondition(bc, noneMatch)))

        when:
        bc.downloadWithResponse(new ByteArrayOutputStream(), null, null, bac, false, null, null).getStatusCode()

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
        def response = bc.downloadWithResponse(new ByteArrayOutputStream(), new BlobRange(0, 3), null, null, true, null, null)
        def contentMD5 = response.getHeaders().getValue("content-md5").getBytes()

        then:
        contentMD5 == Base64.getEncoder().encode(MessageDigest.getInstance("MD5").digest(defaultText.substring(0, 3).getBytes()))
    }

    def "Download error"() {
        setup:
        bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        bc.download(null)

        then:
        thrown(NullPointerException)
    }

    def "Download snapshot"() {
        when:
        def originalStream = new ByteArrayOutputStream()
        bc.download(originalStream)

        def bc2 = bc.createSnapshot()
        new SpecializedBlobClientBuilder()
            .blobClient(bc)
            .buildBlockBlobClient()
            .upload(new ByteArrayInputStream("ABC".getBytes()), 3)

        then:
        def snapshotStream = new ByteArrayOutputStream()
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
        def headers = response.getHeaders()
        def properties = response.getValue()

        then:
        validateBasicHeaders(headers)
        ImplUtils.isNullOrEmpty(properties.getMetadata())
        properties.getBlobType() == BlobType.BLOCK_BLOB
        properties.getCopyCompletionTime() == null // tested in "copy"
        properties.getCopyStatusDescription() == null // only returned when the service has errors; cannot validate.
        properties.getCopyId() == null // tested in "abort copy"
        properties.getCopyProgress() == null // tested in "copy"
        properties.getCopySource() == null // tested in "copy"
        properties.getCopyStatus() == null // tested in "copy"
        !properties.isIncrementalCopy() // tested in PageBlob."start incremental copy"
        properties.getCopyDestinationSnapshot() == null // tested in PageBlob."start incremental copy"
        properties.getLeaseDuration() == null // tested in "acquire lease"
        properties.getLeaseState() == LeaseStateType.AVAILABLE
        properties.getLeaseStatus() == LeaseStatusType.UNLOCKED
        properties.getBlobSize() >= 0
        properties.getContentType() != null
        properties.getContentMD5() != null
        properties.getContentEncoding() == null // tested in "set HTTP headers"
        properties.getContentDisposition() == null // tested in "set HTTP headers"
        properties.getContentLanguage() == null // tested in "set HTTP headers"
        properties.getCacheControl() == null // tested in "set HTTP headers"
        properties.getBlobSequenceNumber() == null // tested in PageBlob."create sequence number"
        headers.getValue("Accept-Ranges") == "bytes"
        properties.getCommittedBlockCount() == null // tested in AppendBlob."append block"
        properties.isServerEncrypted()
        properties.getAccessTier() == AccessTier.HOT
        properties.isAccessTierInferred()
        properties.getArchiveStatus() == null
        properties.getCreationTime() != null
    }

    def "Get properties min"() {
        expect:
        bc.getPropertiesWithResponse(null, null, null).getStatusCode() == 200
    }

    @Unroll
    def "Get properties AC"() {
        setup:
        def bac = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(setupBlobLeaseCondition(bc, leaseID)))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfModifiedSince(modified)
                .setIfUnmodifiedSince(unmodified)
                .setIfMatch(setupBlobMatchCondition(bc, match))
                .setIfNoneMatch(noneMatch))

        expect:
        bc.getPropertiesWithResponse(bac, null, null).getStatusCode() == 200

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
        def bac = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(setupBlobLeaseCondition(bc, leaseID)))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfModifiedSince(modified)
                .setIfUnmodifiedSince(unmodified)
                .setIfMatch(match)
                .setIfNoneMatch(setupBlobMatchCondition(bc, noneMatch)))

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
        bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        bc.getProperties()

        then:
        thrown(StorageException)
    }

    def "Set HTTP headers null"() {
        setup:
        def response = bc.setHTTPHeadersWithResponse(null, null, null, null)

        expect:
        response.getStatusCode() == 200
        validateBasicHeaders(response.getHeaders())
    }

    def "Set HTTP headers min"() {
        setup:
        def properties = bc.getProperties()
        def headers = new BlobHTTPHeaders()
            .setBlobContentEncoding(properties.getContentEncoding())
            .setBlobContentDisposition(properties.getContentDisposition())
            .setBlobContentType("type")
            .setBlobCacheControl(properties.getCacheControl())
            .setBlobContentLanguage(properties.getContentLanguage())
            .setBlobContentMD5(Base64.getEncoder().encode(MessageDigest.getInstance("MD5").digest(defaultData.array())))

        bc.setHTTPHeaders(headers)

        expect:
        bc.getProperties().getContentType() == "type"
    }

    @Unroll
    def "Set HTTP headers headers"() {
        setup:
        def putHeaders = new BlobHTTPHeaders().setBlobCacheControl(cacheControl)
            .setBlobContentDisposition(contentDisposition)
            .setBlobContentEncoding(contentEncoding)
            .setBlobContentLanguage(contentLanguage)
            .setBlobContentMD5(contentMD5)
            .setBlobContentType(contentType)

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
        def bac = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseID))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfModifiedSince(modified)
                .setIfUnmodifiedSince(unmodified)
                .setIfMatch(match)
                .setIfNoneMatch(noneMatch))

        expect:
        bc.setHTTPHeadersWithResponse(null, bac, null, null).getStatusCode() == 200

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
        def bac = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseID))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfModifiedSince(modified)
                .setIfUnmodifiedSince(unmodified)
                .setIfMatch(match)
                .setIfNoneMatch(noneMatch))

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
        bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        bc.setHTTPHeaders(null)

        then:
        thrown(StorageException)
    }

    def "Set metadata all null"() {
        when:
        def response = bc.setMetadataWithResponse(null, null, null, null)

        then:
        bc.getProperties().getMetadata().size() == 0
        response.getStatusCode() == 200
        validateBasicHeaders(response.getHeaders())
        Boolean.parseBoolean(response.getHeaders().getValue("x-ms-request-server-encrypted"))
    }

    def "Set metadata min"() {
        setup:
        def metadata = new HashMap<String, String>()
        metadata.put("foo", "bar")

        when:
        bc.setMetadata(metadata)

        then:
        bc.getProperties().getMetadata() == metadata
    }

    @Unroll
    def "Set metadata metadata"() {
        setup:
        def metadata = new HashMap<String, String>()
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2)
        }

        expect:
        bc.setMetadataWithResponse(metadata, null, null, null).getStatusCode() == statusCode
        bc.getProperties().getMetadata() == metadata

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
        def bac = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseID))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfModifiedSince(modified)
                .setIfUnmodifiedSince(unmodified)
                .setIfMatch(match)
                .setIfNoneMatch(noneMatch))

        expect:
        bc.setMetadataWithResponse(null, bac, null, null).getStatusCode() == 200

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

        def bac = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseID))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfModifiedSince(modified)
                .setIfUnmodifiedSince(unmodified)
                .setIfMatch(match)
                .setIfNoneMatch(noneMatch))

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
        bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        bc.setMetadata(null)

        then:
        thrown(StorageException)
    }

    def "Snapshot"() {
        when:
        def response = bc.createSnapshotWithResponse(null, null, null, null)

        then:
        response.getValue().exists()
        validateBasicHeaders(response.getHeaders())
    }

    def "Snapshot min"() {
        bc.createSnapshotWithResponse(null, null, null, null).getStatusCode() == 201
    }

    @Unroll
    def "Snapshot metadata"() {
        setup:
        def metadata = new HashMap<String, String>()
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2)
        }

        def response = bc.createSnapshotWithResponse(metadata, null, null, null)
        def bcSnap = response.getValue()

        expect:
        response.getStatusCode() == 201
        bcSnap.getProperties().getMetadata() == metadata

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
        def bac = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseID))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfModifiedSince(modified)
                .setIfUnmodifiedSince(unmodified)
                .setIfMatch(match)
                .setIfNoneMatch(noneMatch))

        expect:
        bc.createSnapshotWithResponse(null, bac, null, null).getStatusCode() == 201

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
        def bac = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseID))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfModifiedSince(modified)
                .setIfUnmodifiedSince(unmodified)
                .setIfMatch(match)
                .setIfNoneMatch(noneMatch))


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
        bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        bc.createSnapshot()

        then:
        thrown(StorageException)
    }

    def "Copy"() {
        setup:
        def copyDestBlob = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        def headers =
            copyDestBlob.copyFromURLWithResponse(new URL(bc.getBlobUrl()), null, null, null, null, null, null).getHeaders()

        when:
        while (copyDestBlob.getProperties().getCopyStatus() == CopyStatusType.PENDING) {
            sleepIfRecord(1000)
        }
        def properties = copyDestBlob.getProperties()

        then:
        properties.getCopyStatus() == CopyStatusType.SUCCESS
        properties.getCopyCompletionTime() != null
        properties.getCopyProgress() != null
        properties.getCopySource() != null
        validateBasicHeaders(headers)
        headers.getValue("x-ms-copy-id") != null
    }

    def "Copy min"() {
        expect:
        bc.copyFromURLWithResponse(new URL(bc.getBlobUrl()), null, null, null, null, null, null).getStatusCode() == 202
    }

    def "Copy poller"() {
        setup:
        def copyDestBlob = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        def poller = copyDestBlob.beginCopyFromUrl(new URL(bc.getBlobUrl()), null, null, null, null, null, null);

        when:
        def verifier = StepVerifier.create(poller.getObserver())

        then:
        verifier.assertNext({
            assert it.getValue() != null
            assert it.getValue().getCopyId() != null
            assert it.getValue().getCopySourceUrl() == bc.getBlobUrl()
        }).verifyComplete()

        expect:
        def properties = copyDestBlob.getProperties()

        properties.getCopyStatus() == CopyStatusType.SUCCESS
        properties.getCopyCompletionTime() != null
        properties.getCopyProgress() != null
        properties.getCopySource() != null
        properties.getCopyId() != null

        def lastResponse = poller.getLastPollResponse()
        lastResponse != null
        lastResponse.getValue() != null
        lastResponse.getValue().getCopyId() == properties.getCopyId()
    }

    @Unroll
    def "Copy metadata"() {
        setup:
        def bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        def metadata = new HashMap<String, String>()
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2)
        }

        def status = bu2.copyFromURLWithResponse(new URL(bc.getBlobUrl()), metadata, null, null, null, null, null)
            .getHeaders().getValue("x-ms-copy-status")

        def start = OffsetDateTime.now()
        while (status != CopyStatusType.SUCCESS.toString()) {
            sleepIfRecord(1000)
            status = bu2.getPropertiesWithResponse(null, null, null).getHeaders().getValue("x-ms-copy-status")
            def currentTime = OffsetDateTime.now()
            if (status == CopyStatusType.FAILED.toString() || currentTime.minusMinutes(1) == start) {
                throw new Exception("Copy failed or took too long")
            }
        }

        expect:
        bu2.getProperties().getMetadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @Unroll
    def "Copy source AC"() {
        setup:
        def copyDestBlob = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        match = setupBlobMatchCondition(bc, match)
        def mac = new ModifiedAccessConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)

        expect:
        copyDestBlob.copyFromURLWithResponse(new URL(bc.getBlobUrl()), null, null, mac, null, null, null).getStatusCode() == 202

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
        def bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        def mac = new ModifiedAccessConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)

        when:
        bu2.copyFromURLWithResponse(new URL(bc.getBlobUrl()), null, null, mac, null, null, null)

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
        def bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        bu2.upload(defaultInputStream.get(), defaultDataSize)
        match = setupBlobMatchCondition(bu2, match)
        leaseID = setupBlobLeaseCondition(bu2, leaseID)
        def bac = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseID))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfModifiedSince(modified)
                .setIfUnmodifiedSince(unmodified)
                .setIfMatch(match)
                .setIfNoneMatch(noneMatch))


        expect:
        bu2.copyFromURLWithResponse(new URL(bc.getBlobUrl()), null, null, null, bac, null, null).getStatusCode() == 202

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
        def bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        bu2.upload(defaultInputStream.get(), defaultDataSize)
        noneMatch = setupBlobMatchCondition(bu2, noneMatch)
        setupBlobLeaseCondition(bu2, leaseID)
        def bac = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseID))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfModifiedSince(modified)
                .setIfUnmodifiedSince(unmodified)
                .setIfMatch(match)
                .setIfNoneMatch(noneMatch))

        when:
        bu2.copyFromURLWithResponse(new URL(bc.getBlobUrl()), null, null, null, bac, null, null)

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
        new SpecializedBlobClientBuilder()
            .blobClient(bc)
            .buildBlockBlobClient()
            .upload(new ByteArrayInputStream(getRandomByteArray(8 * 1024 * 1024)), 8 * 1024 * 1024)
        // So we don't have to create a SAS.
        cc.setAccessPolicy(PublicAccessType.BLOB, null)

        def cu2 = alternateBlobServiceClient.getBlobContainerClient(generateBlobName())
        cu2.create()
        def bu2 = cu2.getBlobClient(generateBlobName()).getBlockBlobClient()
        bu2.upload(defaultInputStream.get(), defaultDataSize)
        def leaseID = setupBlobLeaseCondition(bu2, receivedLeaseID)

        when:
        def copyID = bu2.copyFromURLWithResponse(new URL(bc.getBlobUrl()), null, null, null,
            new BlobAccessConditions().setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseID)), null, null).getValue()
        bu2.abortCopyFromURLWithResponse(copyID, new LeaseAccessConditions().setLeaseId(garbageLeaseID), null, null)

        then:
        def e = thrown(StorageException)
        e.getStatusCode() == 412

        cleanup:
        cu2.delete()
    }

    def "Abort copy"() {
        setup:
        // Data has to be large enough and copied between accounts to give us enough time to abort
        new SpecializedBlobClientBuilder().blobClient(bc)
            .buildBlockBlobClient()
            .upload(new ByteArrayInputStream(getRandomByteArray(8 * 1024 * 1024)), 8 * 1024 * 1024)
        // So we don't have to create a SAS.
        cc.setAccessPolicy(PublicAccessType.BLOB, null)

        def cu2 = alternateBlobServiceClient.getBlobContainerClient(generateBlobName())
        cu2.create()
        def bu2 = cu2.getBlobClient(generateBlobName())

        when:
        def copyID = bu2.copyFromURLWithResponse(new URL(bc.getBlobUrl()), null, null, null, null, null, null).getValue()
        def response = bu2.abortCopyFromURLWithResponse(copyID, null, null, null)
        def headers = response.getHeaders()

        then:
        response.getStatusCode() == 204
        headers.getValue("x-ms-request-id") != null
        headers.getValue("x-ms-version") != null
        headers.getValue("Date") != null
        // Normal test cleanup will not clean up containers in the alternate account.
        cu2.deleteWithResponse(null, null, null).getStatusCode() == 202
    }

    def "Abort copy min"() {
        setup:
        // Data has to be large enough and copied between accounts to give us enough time to abort
        new SpecializedBlobClientBuilder().blobClient(bc)
            .buildBlockBlobClient()
            .upload(new ByteArrayInputStream(getRandomByteArray(8 * 1024 * 1024)), 8 * 1024 * 1024)
        // So we don't have to create a SAS.
        cc.setAccessPolicy(PublicAccessType.BLOB, null)

        def cu2 = alternateBlobServiceClient.getBlobContainerClient(generateBlobName())
        cu2.create()
        def bu2 = cu2.getBlobClient(generateBlobName())

        when:
        def copyID = bu2.copyFromURL(new URL(bc.getBlobUrl()))

        then:
        bu2.abortCopyFromURLWithResponse(copyID, null, null, null).getStatusCode() == 204
    }

    def "Abort copy lease"() {
        setup:
        // Data has to be large enough and copied between accounts to give us enough time to abort
        new SpecializedBlobClientBuilder().blobClient(bc)
            .buildBlockBlobClient()
            .upload(new ByteArrayInputStream(getRandomByteArray(8 * 1024 * 1024)), 8 * 1024 * 1024)
        // So we don't have to create a SAS.
        cc.setAccessPolicy(PublicAccessType.BLOB, null)

        def cu2 = alternateBlobServiceClient.getBlobContainerClient(generateContainerName())
        cu2.create()
        def bu2 = cu2.getBlobClient(generateBlobName()).getBlockBlobClient()
        bu2.upload(defaultInputStream.get(), defaultDataSize)
        def leaseID = setupBlobLeaseCondition(bu2, receivedLeaseID)

        when:
        def copyID =
            bu2.copyFromURLWithResponse(new URL(bc.getBlobUrl()), null, null, null,
                new BlobAccessConditions().setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseID)),
                null, Context.NONE).getValue()

        then:
        bu2.abortCopyFromURLWithResponse(copyID, new LeaseAccessConditions().setLeaseId(leaseID), null, null).getStatusCode() == 204
        // Normal test cleanup will not clean up containers in the alternate account.
        cu2.delete()
    }

    def "Copy error"() {
        setup:
        bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        bc.copyFromURL(new URL("http://www.error.com"))

        then:
        thrown(StorageException)
    }

    def "Abort copy error"() {
        setup:
        bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        bc.abortCopyFromURL("id")

        then:
        thrown(StorageException)
    }

    def "Sync copy"() {
        setup:
        // Sync copy is a deep copy, which requires either sas or public access.
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        def headers = bu2.copyFromURLWithResponse(new URL(bc.getBlobUrl()), null, null, null, null, null, null).getHeaders()

        then:
        headers.getValue("x-ms-copy-status") == SyncCopyStatusType.SUCCESS.toString()
        headers.getValue("x-ms-copy-id") != null
        validateBasicHeaders(headers)
    }

    def "Sync copy min"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        expect:
        bu2.copyFromURLWithResponse(new URL(bc.getBlobUrl()), null, null, null, null, null, null).getStatusCode() == 202
    }

    @Unroll
    def "Sync copy metadata"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        def metadata = new HashMap<String, String>()
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2)
        }

        when:
        bu2.copyFromURLWithResponse(new URL(bc.getBlobUrl()), metadata, null, null, null, null, null)

        then:
        bu2.getProperties().getMetadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @Unroll
    def "Sync copy source AC"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        match = setupBlobMatchCondition(bc, match)
        def mac = new ModifiedAccessConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)

        expect:
        bu2.copyFromURLWithResponse(new URL(bc.getBlobUrl()), null, null, mac, null, null, null).getStatusCode() == 202

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
        def bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        def mac = new ModifiedAccessConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)

        when:
        bu2.copyFromURLWithResponse(new URL(bc.getBlobUrl()), null, null, mac, null, null, null)

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
        def bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        bu2.upload(defaultInputStream.get(), defaultDataSize)
        match = setupBlobMatchCondition(bu2, match)
        leaseID = setupBlobLeaseCondition(bu2, leaseID)
        def bac = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseID))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfModifiedSince(modified)
                .setIfUnmodifiedSince(unmodified)
                .setIfMatch(match)
                .setIfNoneMatch(noneMatch))

        expect:
        bu2.copyFromURLWithResponse(new URL(bc.getBlobUrl()), null, null, null, bac, null, null).getStatusCode() == 202

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
        def bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        bu2.upload(defaultInputStream.get(), defaultDataSize)
        noneMatch = setupBlobMatchCondition(bu2, noneMatch)
        setupBlobLeaseCondition(bu2, leaseID)
        def bac = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseID))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfModifiedSince(modified)
                .setIfUnmodifiedSince(unmodified)
                .setIfMatch(match)
                .setIfNoneMatch(noneMatch))

        when:
        bu2.copyFromURLWithResponse(new URL(bc.getBlobUrl()), null, null, null, bac, null, null)

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
        def bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        bu2.copyFromURL(new URL(bc.getBlobUrl()))

        then:
        thrown(StorageException)
    }

    def "Delete"() {
        when:
        def response = bc.deleteWithResponse(null, null, null, null)
        def headers = response.getHeaders()

        then:
        response.getStatusCode() == 202
        headers.getValue("x-ms-request-id") != null
        headers.getValue("x-ms-version") != null
        headers.getValue("Date") != null
    }

    def "Delete min"() {
        expect:
        bc.deleteWithResponse(null, null, null, null).getStatusCode() == 202
    }

    @Unroll
    def "Delete options"() {
        setup:
        bc.createSnapshot()
        // Create an extra blob so the list isn't empty (null) when we delete base blob, too
        def bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
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
        def bac = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseID))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfModifiedSince(modified)
                .setIfUnmodifiedSince(unmodified)
                .setIfMatch(match)
                .setIfNoneMatch(noneMatch))

        expect:
        bc.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, bac, null, null).getStatusCode() == 202

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
        def bac = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseID))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfModifiedSince(modified)
                .setIfUnmodifiedSince(unmodified)
                .setIfMatch(match)
                .setIfNoneMatch(noneMatch))

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
        bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        bc.delete()

        then:
        thrown(StorageException)
    }

    @Unroll
    def "Set tier block blob"() {
        setup:
        def cc = blobServiceClient.createBlobContainer(generateContainerName())
        def bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        bc.upload(defaultInputStream.get(), defaultData.remaining())

        when:
        def initialResponse = bc.setAccessTierWithResponse(tier, null, null, null, null)
        def headers = initialResponse.getHeaders()

        then:
        initialResponse.getStatusCode() == 200 || initialResponse.getStatusCode() == 202
        headers.getValue("x-ms-version") != null
        headers.getValue("x-ms-request-id") != null
        bc.getProperties().getAccessTier() == tier
        cc.listBlobsFlat().iterator().next().getProperties().getAccessTier() == tier

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
        def cc = premiumBlobServiceClient.createBlobContainer(generateContainerName())

        def bc = cc.getBlobClient(generateBlobName()).getPageBlobClient()
        bc.create(512)

        when:
        bc.setAccessTier(tier)

        then:
        bc.getProperties().getAccessTier() == tier
        cc.listBlobsFlat().iterator().next().getProperties().getAccessTier() == tier

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
        def cc = blobServiceClient.createBlobContainer(generateContainerName())
        def bu = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        bu.upload(defaultInputStream.get(), defaultData.remaining())

        when:
        def statusCode = bc.setAccessTierWithResponse(AccessTier.HOT, null, null, null, null).getStatusCode()

        then:
        statusCode == 200 || statusCode == 202

        cleanup:
        cc.delete()
    }

    def "Set tier inferred"() {
        setup:
        def cc = blobServiceClient.createBlobContainer(generateBlobName())
        def bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        bc.upload(defaultInputStream.get(), defaultDataSize)

        when:
        def inferred1 = bc.getProperties().isAccessTierInferred()
        def inferredList1 = cc.listBlobsFlat().iterator().next().getProperties().isAccessTierInferred()

        bc.setAccessTier(AccessTier.HOT)

        def inferred2 = bc.getProperties().isAccessTierInferred()
        def inferredList2 = cc.listBlobsFlat().iterator().next().getProperties().isAccessTierInferred()

        then:
        inferred1
        inferredList1
        !inferred2
        inferredList2 == null
    }

    @Unroll
    def "Set tier archive status"() {
        setup:
        def cc = blobServiceClient.createBlobContainer(generateBlobName())
        def bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        bc.upload(defaultInputStream.get(), defaultDataSize)

        when:
        bc.setAccessTier(sourceTier)
        bc.setAccessTier(destTier)

        then:
        bc.getProperties().getArchiveStatus() == status
        cc.listBlobsFlat().iterator().next().getProperties().getArchiveStatus() == status

        where:
        sourceTier         | destTier        | priority                   | status
        AccessTier.ARCHIVE | AccessTier.COOL | RehydratePriority.STANDARD | ArchiveStatus.REHYDRATE_PENDING_TO_COOL
        AccessTier.ARCHIVE | AccessTier.HOT  | RehydratePriority.STANDARD | ArchiveStatus.REHYDRATE_PENDING_TO_HOT
        AccessTier.ARCHIVE | AccessTier.HOT  | RehydratePriority.HIGH     | ArchiveStatus.REHYDRATE_PENDING_TO_HOT
    }

    def "Set tier error"() {
        setup:
        def cc = blobServiceClient.createBlobContainer(generateContainerName())
        def bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        bc.upload(defaultInputStream.get(), defaultDataSize)

        when:
        bc.setAccessTier(AccessTier.fromString("garbage"))

        then:
        def e = thrown(StorageException)
        e.getErrorCode() == StorageErrorCode.INVALID_HEADER_VALUE

        cleanup:
        cc.delete()
    }

    def "Set tier illegal argument"() {
        when:
        bc.setAccessTier(null)

        then:
        thrown(NullPointerException)
    }

    def "Set tier lease"() {
        setup:

        def cc = blobServiceClient.createBlobContainer(generateContainerName())
        def bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        bc.upload(defaultInputStream.get(), defaultDataSize)
        def leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)

        when:
        bc.setAccessTierWithResponse(AccessTier.HOT, null, new LeaseAccessConditions().setLeaseId(leaseID), null, null)

        then:
        notThrown(StorageException)

        cleanup:
        cc.delete()
    }

    def "Set tier lease fail"() {
        setup:
        def cc = blobServiceClient.createBlobContainer(generateContainerName())
        def bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        bc.upload(defaultInputStream.get(), defaultDataSize)

        when:
        bc.setAccessTierWithResponse(AccessTier.HOT, null, new LeaseAccessConditions().setLeaseId("garbage"), null, null)

        then:
        thrown(StorageException)
    }

    @Unroll
    def "Copy with tier"() {
        setup:
        def bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        bc.uploadWithResponse(defaultInputStream.get(), defaultDataSize, null, null, tier1, null, null, null)
        def bcCopy = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        def sas = new BlobServiceSasSignatureValues()
            .setExpiryTime(OffsetDateTime.now().plusHours(1))
            .setPermissions(new BlobSasPermission().setReadPermission(true))
            .setCanonicalName(bc.getBlobUrl().toString(), primaryCredential.getAccountName())
            .generateSasQueryParameters(primaryCredential)
            .encode()
        bcCopy.copyFromURLWithResponse(new URL(bc.getBlobUrl().toString() + "?" + sas), null, tier2, null, null, null, null)

        then:
        bcCopy.getProperties().getAccessTier() == tier2

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
        def undeleteHeaders = bc.undeleteWithResponse(null, null).getHeaders()
        bc.getProperties()

        then:
        notThrown(StorageException)
        undeleteHeaders.getValue("x-ms-request-id") != null
        undeleteHeaders.getValue("x-ms-version") != null
        undeleteHeaders.getValue("Date") != null

        disableSoftDelete() == null
    }

    def "Undelete min"() {
        setup:
        enableSoftDelete()
        bc.delete()

        expect:
        bc.undeleteWithResponse(null, null).getStatusCode() == 200
    }

    def "Undelete error"() {
        bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        bc.undelete()

        then:
        thrown(StorageException)
    }

    def "Get account info"() {
        when:
        def response = primaryBlobServiceClient.getAccountInfoWithResponse(null, null)

        then:
        response.getHeaders().getValue("Date") != null
        response.getHeaders().getValue("x-ms-request-id") != null
        response.getHeaders().getValue("x-ms-version") != null
        response.getValue().getAccountKind() != null
        response.getValue().getSkuName() != null
    }

    def "Get account info min"() {
        expect:
        bc.getAccountInfoWithResponse(null, null).getStatusCode() == 200
    }

    def "Get account info error"() {
        when:
        def serviceURL = getServiceClient(primaryBlobServiceClient.getAccountUrl())

        serviceURL.getBlobContainerClient(generateContainerName()).getBlobClient(generateBlobName()).getAccountInfo()

        then:
        thrown(IllegalArgumentException)
    }

    def "Get Container Name"() {
        expect:
        containerName == bc.getContainerName()
    }

    def "Get Blob Name"() {
        expect:
        blobName == bc.getBlobName()
    }
}
