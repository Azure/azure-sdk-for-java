// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob


import com.azure.core.http.RequestConditions
import com.azure.core.util.BinaryData
import com.azure.core.util.CoreUtils
import com.azure.core.util.polling.LongRunningOperationStatus
import com.azure.identity.DefaultAzureCredentialBuilder
import com.azure.storage.blob.models.AccessTier
import com.azure.storage.blob.models.ArchiveStatus
import com.azure.storage.blob.models.BlobBeginCopySourceRequestConditions
import com.azure.storage.blob.models.BlobErrorCode
import com.azure.storage.blob.models.BlobHttpHeaders
import com.azure.storage.blob.models.BlobRange
import com.azure.storage.blob.models.BlobRequestConditions
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.blob.models.BlobType
import com.azure.storage.blob.models.BlockListType
import com.azure.storage.blob.models.CopyStatusType
import com.azure.storage.blob.models.CustomerProvidedKey
import com.azure.storage.blob.models.DeleteSnapshotsOptionType
import com.azure.storage.blob.models.DownloadRetryOptions
import com.azure.storage.blob.models.LeaseStateType
import com.azure.storage.blob.models.LeaseStatusType
import com.azure.storage.blob.models.ObjectReplicationPolicy
import com.azure.storage.blob.models.ObjectReplicationStatus
import com.azure.storage.blob.models.ParallelTransferOptions
import com.azure.storage.blob.models.PublicAccessType
import com.azure.storage.blob.models.RehydratePriority
import com.azure.storage.blob.models.SyncCopyStatusType
import com.azure.storage.blob.options.BlobBeginCopyOptions
import com.azure.storage.blob.options.BlobCopyFromUrlOptions
import com.azure.storage.blob.options.BlobDownloadToFileOptions
import com.azure.storage.blob.options.BlobGetTagsOptions
import com.azure.storage.blob.options.BlobParallelUploadOptions
import com.azure.storage.blob.options.BlobSetAccessTierOptions
import com.azure.storage.blob.options.BlobSetTagsOptions
import com.azure.storage.blob.sas.BlobSasPermission
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues
import com.azure.storage.blob.specialized.BlobClientBase
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder
import com.azure.storage.common.Utility
import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.test.shared.TestHttpClientType
import com.azure.storage.common.test.shared.extensions.LiveOnly
import com.azure.storage.common.test.shared.extensions.PlaybackOnly
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion
import com.azure.storage.common.test.shared.policy.MockFailureResponsePolicy
import com.azure.storage.common.test.shared.policy.MockRetryRangeResponsePolicy
import reactor.core.Exceptions
import reactor.core.publisher.Hooks
import reactor.test.StepVerifier
import spock.lang.IgnoreIf
import spock.lang.Unroll
import spock.lang.Ignore

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.OpenOption
import java.nio.file.StandardOpenOption
import java.security.MessageDigest
import java.time.Duration
import java.time.OffsetDateTime

class BlobAPITest extends APISpec {
    BlobClient bc
    String blobName

    def setup() {
        blobName = generateBlobName()
        bc = cc.getBlobClient(blobName)
        bc.getBlockBlobClient().upload(data.defaultInputStream, data.defaultDataSize)
    }

    def "Upload input stream overwrite fails"() {
        when:
        bc.upload(data.defaultInputStream, data.defaultDataSize)

        then:
        thrown(BlobStorageException)
    }

    def "Upload binary data overwrite fails"() {
        when:
        bc.upload(data.defaultBinaryData)

        then:
        thrown(BlobStorageException)
    }

    def "Upload input stream overwrite"() {
        setup:
        def randomData = getRandomByteArray(Constants.KB)
        def input = new ByteArrayInputStream(randomData)

        when:
        bc.upload(input, Constants.KB, true)

        then:
        def stream = new ByteArrayOutputStream()
        bc.downloadWithResponse(stream, null, null, null, false, null, null)
        stream.toByteArray() == randomData
    }

    def "Upload binary data overwrite"() {
        setup:
        def randomData = getRandomByteArray(Constants.KB)

        when:
        bc.upload(BinaryData.fromBytes(randomData), true)

        then:
        def blobContent = bc.downloadContent()
        blobContent.toBytes() == randomData
    }

    /* Tests an issue found where buffered upload would not deep copy buffers while determining what upload path to take. */

    @Unroll
    def "Upload input stream single upload"() {
        setup:
        def randomData = getRandomByteArray(20 * Constants.KB)
        def input = new ByteArrayInputStream(randomData)

        when:
        bc.upload(input, 20 * Constants.KB, true)

        then:
        def stream = new ByteArrayOutputStream()
        bc.downloadWithResponse(stream, null, null, null, false, null, null)
        stream.toByteArray() == randomData

        where:
        size              || _
        1 * Constants.KB  || _  /* Less than copyToOutputStream buffer size, Less than maxSingleUploadSize */
        8 * Constants.KB  || _  /* Equal to copyToOutputStream buffer size, Less than maxSingleUploadSize */
        20 * Constants.KB || _  /* Greater than copyToOutputStream buffer size, Less than maxSingleUploadSize */
    }

    /* TODO (gapra): Add more tests to test large data sizes. */

    @LiveOnly
    def "Upload input stream large data"() {
        setup:
        def randomData = getRandomByteArray(20 * Constants.MB)
        def input = new ByteArrayInputStream(randomData)

        def pto = new ParallelTransferOptions().setMaxSingleUploadSizeLong(Constants.MB)

        when:
        // Uses blob output stream under the hood.
        bc.uploadWithResponse(input, 20 * Constants.MB, pto, null, null, null, null, null, null)

        then:
        notThrown(BlobStorageException)
    }

    @Unroll
    def "Upload incorrect size"() {
        when:
        bc.upload(data.defaultInputStream, dataSize, true)

        then:
        thrown(IllegalStateException)

        where:
        dataSize                 | threshold
        data.defaultDataSize + 1 | null
        data.defaultDataSize - 1 | null
        data.defaultDataSize + 1 | 1 // Test the chunked case as well
        data.defaultDataSize - 1 | 1
    }

    @Unroll
    @LiveOnly
    def "Upload numBlocks"() {
        setup:
        def randomData = getRandomByteArray(size)
        def input = new ByteArrayInputStream(randomData)

        def pto = new ParallelTransferOptions().setBlockSizeLong(maxUploadSize).setMaxSingleUploadSizeLong(maxUploadSize)

        when:
        bc.uploadWithResponse(input, size, pto, null, null, null, null, null, null)

        then:
        def blocksUploaded = bc.getBlockBlobClient().listBlocks(BlockListType.ALL).getCommittedBlocks()
        blocksUploaded.size() == (int) numBlocks

        where:
        size             | maxUploadSize || numBlocks
        0                | null          || 0
        Constants.KB     | null          || 0 // default is MAX_UPLOAD_BYTES
        Constants.MB     | null          || 0 // default is MAX_UPLOAD_BYTES
        3 * Constants.MB | Constants.MB  || 3
    }

    def "Upload return value"() {
        expect:
        bc.uploadWithResponse(new BlobParallelUploadOptions(data.defaultInputStream, data.defaultDataSize), null, null)
            .getValue().getETag() != null
    }

    def "Upload return value binary data"() {
        expect:
        bc.uploadWithResponse(new BlobParallelUploadOptions(data.defaultBinaryData), null, null)
            .getValue().getETag() != null
    }

    def "Upload InputStream no length"() {
        when:
        bc.uploadWithResponse(new BlobParallelUploadOptions(data.defaultInputStream), null, null)

        then:
        notThrown(Exception)
        bc.downloadContent().toBytes() == data.defaultBytes
    }

    def "Upload InputStream bad length"() {
        when:
        bc.uploadWithResponse(new BlobParallelUploadOptions(data.defaultInputStream, length), null, null)

        then:
        thrown(Exception)

        where:
        _ | length
        _ | 0
        _ | -100
        _ | data.defaultDataSize - 1
        _ | data.defaultDataSize + 1
    }

    def "Upload successful retry"() {
        given:
        def clientWithFailure = getBlobClient(
            env.primaryAccount.credential,
            bc.getBlobUrl(),
            new TransientFailureInjectingHttpPipelinePolicy())

        when:
        clientWithFailure.uploadWithResponse(new BlobParallelUploadOptions(data.defaultInputStream), null, null)

        then:
        notThrown(Exception)
        bc.downloadContent().toBytes() == data.defaultBytes
    }

    @LiveOnly
    // Reading from recordings will not allow for the timing of the test to work correctly.
    def "Upload timeout"() {
        setup:
        def size = 1024
        def randomData = getRandomByteArray(size)
        def input = new ByteArrayInputStream(randomData)

        when:
        bc.uploadWithResponse(input, size, null, null, null, null, null, Duration.ofNanos(5L), null)

        then:
        thrown(IllegalStateException)
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    def "Download all null"() {
        when:
        def stream = new ByteArrayOutputStream()
        bc.setTags(Collections.singletonMap("foo", "bar"))
        def response = bc.downloadWithResponse(stream, null, null, null, false, null, null)
        def body = ByteBuffer.wrap(stream.toByteArray())
        def headers = response.getDeserializedHeaders()

        then:
        body == data.defaultData
        CoreUtils.isNullOrEmpty(headers.getMetadata())
        headers.getTagCount() == 1
        headers.getContentLength() != null
        headers.getContentType() != null
        headers.getContentRange() == null
        headers.getContentMd5() != null
        headers.getContentEncoding() == null
        headers.getCacheControl() == null
        headers.getContentDisposition() == null
        headers.getContentLanguage() == null
        headers.getBlobSequenceNumber() == null
        headers.getBlobType() == BlobType.BLOCK_BLOB
        headers.getCopyCompletionTime() == null
        headers.getCopyStatusDescription() == null
        headers.getCopyId() == null
        headers.getCopyProgress() == null
        headers.getCopySource() == null
        headers.getCopyStatus() == null
        headers.getLeaseDuration() == null
        headers.getLeaseState() == LeaseStateType.AVAILABLE
        headers.getLeaseStatus() == LeaseStatusType.UNLOCKED
        headers.getAcceptRanges() == "bytes"
        headers.getBlobCommittedBlockCount() == null
        headers.isServerEncrypted() != null
        headers.getBlobContentMD5() == null
//        headers.getLastAccessedTime() /* TODO (gapra): re-enable when last access time enabled. */
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    def "Download all null streaming"() {
        when:
        def stream = new ByteArrayOutputStream()
        bc.setTags(Collections.singletonMap("foo", "bar"))
        def response = bc.downloadStreamWithResponse(stream, null, null, null, false, null, null)
        def body = ByteBuffer.wrap(stream.toByteArray())
        def headers = response.getDeserializedHeaders()

        then:
        body == data.defaultData
        CoreUtils.isNullOrEmpty(headers.getMetadata())
        headers.getTagCount() == 1
        headers.getContentLength() != null
        headers.getContentType() != null
        headers.getContentRange() == null
        headers.getContentMd5() != null
        headers.getContentEncoding() == null
        headers.getCacheControl() == null
        headers.getContentDisposition() == null
        headers.getContentLanguage() == null
        headers.getBlobSequenceNumber() == null
        headers.getBlobType() == BlobType.BLOCK_BLOB
        headers.getCopyCompletionTime() == null
        headers.getCopyStatusDescription() == null
        headers.getCopyId() == null
        headers.getCopyProgress() == null
        headers.getCopySource() == null
        headers.getCopyStatus() == null
        headers.getLeaseDuration() == null
        headers.getLeaseState() == LeaseStateType.AVAILABLE
        headers.getLeaseStatus() == LeaseStatusType.UNLOCKED
        headers.getAcceptRanges() == "bytes"
        headers.getBlobCommittedBlockCount() == null
        headers.isServerEncrypted() != null
        headers.getBlobContentMD5() == null
//        headers.getLastAccessedTime() /* TODO (gapra): re-enable when last access time enabled. */
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    def "Download all null binary data"() {
        when:
        bc.setTags(Collections.singletonMap("foo", "bar"))
        def response = bc.downloadContentWithResponse(null, null, null, null)
        def body = response.getValue()
        def headers = response.getDeserializedHeaders()

        then:
        body.toBytes() == data.defaultBytes
        CoreUtils.isNullOrEmpty(headers.getMetadata())
        headers.getTagCount() == 1
        headers.getContentLength() != null
        headers.getContentType() != null
        headers.getContentRange() == null
        headers.getContentMd5() != null
        headers.getContentEncoding() == null
        headers.getCacheControl() == null
        headers.getContentDisposition() == null
        headers.getContentLanguage() == null
        headers.getBlobSequenceNumber() == null
        headers.getBlobType() == BlobType.BLOCK_BLOB
        headers.getCopyCompletionTime() == null
        headers.getCopyStatusDescription() == null
        headers.getCopyId() == null
        headers.getCopyProgress() == null
        headers.getCopySource() == null
        headers.getCopyStatus() == null
        headers.getLeaseDuration() == null
        headers.getLeaseState() == LeaseStateType.AVAILABLE
        headers.getLeaseStatus() == LeaseStatusType.UNLOCKED
        headers.getAcceptRanges() == "bytes"
        headers.getBlobCommittedBlockCount() == null
        headers.isServerEncrypted() != null
        headers.getBlobContentMD5() == null
//        headers.getLastAccessedTime() /* TODO (gapra): re-enable when last access time enabled. */
    }

    def "Download empty file"() {
        setup:
        def bc = cc.getBlobClient("emptyAppendBlob").getAppendBlobClient()
        bc.create()

        when:
        def outStream = new ByteArrayOutputStream()
        bc.download(outStream)
        def result = outStream.toByteArray()

        then:
        notThrown(BlobStorageException)
        result.length == 0
    }

    /*
    This is to test the appropriate integration of DownloadResponse, including setting the correct range values on
    HttpGetterInfo.
     */

    def "Download with retry range"() {
        /*
        We are going to make a request for some range on a blob. The Flux returned will throw an exception, forcing
        a retry per the DownloadRetryOptions. The next request should have the same range header, which was generated
        from the count and offset values in HttpGetterInfo that was constructed on the initial call to download. We
        don't need to check the data here, but we want to ensure that the correct range is set each time. This will
        test the correction of a bug that was found which caused HttpGetterInfo to have an incorrect offset when it was
        constructed in BlobClient.download().
         */
        setup:
        def bu2 = getBlobClient(env.primaryAccount.credential, bc.getBlobUrl(), new MockRetryRangeResponsePolicy("bytes=2-6"))

        when:
        def range = new BlobRange(2, 5L)
        def options = new DownloadRetryOptions().setMaxRetryRequests(3)
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
        result == data.defaultBytes
    }

    def "Download streaming min"() {
        when:
        def outStream = new ByteArrayOutputStream()
        bc.downloadStream(outStream)
        def result = outStream.toByteArray()

        then:
        result == data.defaultBytes
    }

    def "Download binary data min"() {
        when:
        def result = bc.downloadContent()

        then:
        result.toBytes() == data.defaultBytes
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
        0      | null  || data.defaultText
        0      | 5L    || data.defaultText.substring(0, 5)
        3      | 2L    || data.defaultText.substring(3, 3 + 2)
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "Download AC"() {
        setup:
        def t = new HashMap<String, String>()
        t.put("foo", "bar")
        bc.setTags(t)
        match = setupBlobMatchCondition(bc, match)
        leaseID = setupBlobLeaseCondition(bc, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags)

        when:
        def response = bc.downloadWithResponse(new ByteArrayOutputStream(), null, null, bac, false, null, null)

        then:
        response.getStatusCode() == 200

        where:
        modified | unmodified | match        | noneMatch   | leaseID         | tags
        null     | null       | null         | null        | null            | null
        oldDate  | null       | null         | null        | null            | null
        null     | newDate    | null         | null        | null            | null
        null     | null       | receivedEtag | null        | null            | null
        null     | null       | null         | garbageEtag | null            | null
        null     | null       | null         | null        | receivedLeaseID | null
        null     | null       | null         | null        | null            | "\"foo\" = 'bar'"
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "Download AC streaming"() {
        setup:
        def t = new HashMap<String, String>()
        t.put("foo", "bar")
        bc.setTags(t)
        match = setupBlobMatchCondition(bc, match)
        leaseID = setupBlobLeaseCondition(bc, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags)

        when:
        def response = bc.downloadStreamWithResponse(new ByteArrayOutputStream(), null, null, bac, false, null, null)

        then:
        response.getStatusCode() == 200

        where:
        modified | unmodified | match        | noneMatch   | leaseID         | tags
        null     | null       | null         | null        | null            | null
        oldDate  | null       | null         | null        | null            | null
        null     | newDate    | null         | null        | null            | null
        null     | null       | receivedEtag | null        | null            | null
        null     | null       | null         | garbageEtag | null            | null
        null     | null       | null         | null        | receivedLeaseID | null
        null     | null       | null         | null        | null            | "\"foo\" = 'bar'"
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "Download AC binary data"() {
        setup:
        def t = new HashMap<String, String>()
        t.put("foo", "bar")
        bc.setTags(t)
        match = setupBlobMatchCondition(bc, match)
        leaseID = setupBlobLeaseCondition(bc, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags)

        when:
        def response = bc.downloadContentWithResponse(null, bac, null, null)

        then:
        response.getStatusCode() == 200

        where:
        modified | unmodified | match        | noneMatch   | leaseID         | tags
        null     | null       | null         | null        | null            | null
        oldDate  | null       | null         | null        | null            | null
        null     | newDate    | null         | null        | null            | null
        null     | null       | receivedEtag | null        | null            | null
        null     | null       | null         | garbageEtag | null            | null
        null     | null       | null         | null        | receivedLeaseID | null
        null     | null       | null         | null        | null            | "\"foo\" = 'bar'"
    }

    @Unroll
    def "Download AC fail"() {
        setup:
        setupBlobLeaseCondition(bc, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupBlobMatchCondition(bc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags)

        when:
        bc.downloadWithResponse(new ByteArrayOutputStream(), null, null, bac, false, null, null).getStatusCode()

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID        | tags
        newDate  | null       | null        | null         | null           | null
        null     | oldDate    | null        | null         | null           | null
        null     | null       | garbageEtag | null         | null           | null
        null     | null       | null        | receivedEtag | null           | null
        null     | null       | null        | null         | garbageLeaseID | null
        null     | null       | null        | null         | null           | "\"notfoo\" = 'notbar'"
    }

    @Unroll
    def "Download AC fail streaming"() {
        setup:
        setupBlobLeaseCondition(bc, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupBlobMatchCondition(bc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags)

        when:
        bc.downloadStreamWithResponse(new ByteArrayOutputStream(), null, null, bac, false, null, null).getStatusCode()

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID        | tags
        newDate  | null       | null        | null         | null           | null
        null     | oldDate    | null        | null         | null           | null
        null     | null       | garbageEtag | null         | null           | null
        null     | null       | null        | receivedEtag | null           | null
        null     | null       | null        | null         | garbageLeaseID | null
        null     | null       | null        | null         | null           | "\"notfoo\" = 'notbar'"
    }

    @Unroll
    def "Download AC fail binary data"() {
        setup:
        setupBlobLeaseCondition(bc, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupBlobMatchCondition(bc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags)

        when:
        bc.downloadContentWithResponse(null, bac, null, null).getStatusCode()

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID        | tags
        newDate  | null       | null        | null         | null           | null
        null     | oldDate    | null        | null         | null           | null
        null     | null       | garbageEtag | null         | null           | null
        null     | null       | null        | receivedEtag | null           | null
        null     | null       | null        | null         | garbageLeaseID | null
        null     | null       | null        | null         | null           | "\"notfoo\" = 'notbar'"
    }

    def "Download md5"() {
        when:
        def response = bc.downloadWithResponse(new ByteArrayOutputStream(), new BlobRange(0, 3), null, null, true, null, null)
        def contentMD5 = response.getDeserializedHeaders().getContentMd5()

        then:
        contentMD5 == MessageDigest.getInstance("MD5").digest(data.defaultText.substring(0, 3).getBytes())
    }

    def "Download md5 streaming"() {
        when:
        def response = bc.downloadStreamWithResponse(new ByteArrayOutputStream(), new BlobRange(0, 3), null, null, true, null, null)
        def contentMD5 = response.getDeserializedHeaders().getContentMd5()

        then:
        contentMD5 == MessageDigest.getInstance("MD5").digest(data.defaultText.substring(0, 3).getBytes())
    }

    def "Download retry default"() {
        setup:
        def failureBlobClient = getBlobClient(env.primaryAccount.credential, bc.getBlobUrl(), new MockFailureResponsePolicy(5))

        when:
        def outStream = new ByteArrayOutputStream()
        failureBlobClient.download(outStream)
        String bodyStr = outStream.toString()

        then:
        bodyStr == data.defaultText
    }

    def "Download error"() {
        setup:
        bc = cc.getBlobClient(generateBlobName())

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
            .upload(new ByteArrayInputStream("ABC".getBytes()), 3, true)

        then:
        def snapshotStream = new ByteArrayOutputStream()
        bc2.download(snapshotStream)
        snapshotStream.toByteArray() == originalStream.toByteArray()
    }

    def "Download snapshot streaming"() {
        when:
        def originalStream = new ByteArrayOutputStream()
        bc.downloadStream(originalStream)

        def bc2 = bc.createSnapshot()
        new SpecializedBlobClientBuilder()
            .blobClient(bc)
            .buildBlockBlobClient()
            .upload(new ByteArrayInputStream("ABC".getBytes()), 3, true)

        then:
        def snapshotStream = new ByteArrayOutputStream()
        bc2.downloadStream(snapshotStream)
        snapshotStream.toByteArray() == originalStream.toByteArray()
    }

    def "Download snapshot binary data"() {
        when:
        def originalContent = bc.downloadContent()

        def bc2 = bc.createSnapshot()
        new SpecializedBlobClientBuilder()
            .blobClient(bc)
            .buildBlockBlobClient()
            .upload(new ByteArrayInputStream("ABC".getBytes()), 3, true)

        then:
        def snapshotContent = bc2.downloadContent()
        snapshotContent.toBytes() == originalContent.toBytes()
    }

    def "Download to file exists"() {
        setup:
        def testFile = new File(namer.getResourcePrefix() + ".txt")
        if (!testFile.exists()) {
            assert testFile.createNewFile()
        }

        when:
        // Default overwrite is false so this should fail
        bc.downloadToFile(testFile.getPath())

        then:
        def ex = thrown(UncheckedIOException)
        ex.getCause() instanceof FileAlreadyExistsException

        cleanup:
        testFile.delete()
    }

    def "Download to file exists succeeds"() {
        setup:
        def testFile = new File(namer.getResourcePrefix() + ".txt")
        if (!testFile.exists()) {
            assert testFile.createNewFile()
        }

        when:
        bc.downloadToFile(testFile.getPath(), true)

        then:
        new String(Files.readAllBytes(testFile.toPath()), StandardCharsets.UTF_8) == data.defaultText

        cleanup:
        testFile.delete()
    }

    def "Download to file does not exist"() {
        setup:
        def testFile = new File(namer.getResourcePrefix() + ".txt")
        if (testFile.exists()) {
            assert testFile.delete()
        }

        when:
        bc.downloadToFile(testFile.getPath())

        then:
        new String(Files.readAllBytes(testFile.toPath()), StandardCharsets.UTF_8) == data.defaultText

        cleanup:
        testFile.delete()
    }

    def "Download file does not exist open options"() {
        setup:
        def testFile = new File(namer.getResourcePrefix() + ".txt")
        if (testFile.exists()) {
            assert testFile.delete()
        }

        when:
        Set<OpenOption> openOptions = new HashSet<>()
        openOptions.add(StandardOpenOption.CREATE_NEW)
        openOptions.add(StandardOpenOption.READ)
        openOptions.add(StandardOpenOption.WRITE)
        bc.downloadToFileWithResponse(testFile.getPath(), null, null, null, null, false, openOptions, null, null)

        then:
        new String(Files.readAllBytes(testFile.toPath()), StandardCharsets.UTF_8) == data.defaultText

        cleanup:
        testFile.delete()
    }

    def "Download file exist open options"() {
        setup:
        def testFile = new File(namer.getResourcePrefix() + ".txt")
        if (!testFile.exists()) {
            assert testFile.createNewFile()
        }

        when:
        Set<OpenOption> openOptions = new HashSet<>()
        openOptions.add(StandardOpenOption.CREATE)
        openOptions.add(StandardOpenOption.TRUNCATE_EXISTING)
        openOptions.add(StandardOpenOption.READ)
        openOptions.add(StandardOpenOption.WRITE)
        bc.downloadToFileWithResponse(testFile.getPath(), null, null, null, null, false, openOptions, null, null)

        then:
        new String(Files.readAllBytes(testFile.toPath()), StandardCharsets.UTF_8) == data.defaultText

        cleanup:
        testFile.delete()
    }

    @LiveOnly
    @Unroll
    def "Download file"() {
        setup:
        def file = getRandomFile(fileSize)
        bc.uploadFromFile(file.toPath().toString(), new ParallelTransferOptions().setBlockSizeLong(4 * 1024 * 1024), null, null, null, null, null)
        def outFile = new File(namer.getRandomName(60) + ".txt")
        if (outFile.exists()) {
            assert outFile.delete()
        }

        when:
        def properties = bc.downloadToFileWithResponse(outFile.toPath().toString(), null,
            new ParallelTransferOptions().setBlockSizeLong(4 * 1024 * 1024), null, null, false, null, null)

        then:
        compareFiles(file, outFile, 0, fileSize)
        properties.getValue().getBlobType() == BlobType.BLOCK_BLOB

        cleanup:
        outFile.delete()
        file.delete()

        where:
        fileSize             | _
        0                    | _ // empty file
        20                   | _ // small file
        16 * 1024 * 1024     | _ // medium file in several chunks
        8 * 1026 * 1024 + 10 | _ // medium file not aligned to block
        50 * Constants.MB    | _ // large file requiring multiple requests
        // Files larger than 2GB to test no integer overflow are left to stress/perf tests to keep test passes short.
    }

    /*
     * Tests downloading a file using a default client that doesn't have a HttpClient passed to it.
     */

    @LiveOnly
    @Unroll
    def "Download file sync buffer copy"() {
        setup:
        def containerName = generateContainerName()
        def blobServiceClient = new BlobServiceClientBuilder()
            .endpoint(env.primaryAccount.blobEndpoint)
            .credential(env.primaryAccount.credential)
            .buildClient()

        def blobClient = blobServiceClient.createBlobContainer(containerName)
            .getBlobClient(generateBlobName())


        def file = getRandomFile(fileSize)
        blobClient.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(namer.getRandomName(60) + ".txt")
        if (outFile.exists()) {
            assert outFile.delete()
        }

        when:
        def properties = blobClient.downloadToFileWithResponse(outFile.toPath().toString(), null,
            new ParallelTransferOptions().setBlockSizeLong(4 * 1024 * 1024), null, null, false, null, null)

        then:
        compareFiles(file, outFile, 0, fileSize)
        properties.getValue().getBlobType() == BlobType.BLOCK_BLOB

        cleanup:
        blobServiceClient.deleteBlobContainer(containerName)
        outFile.delete()
        file.delete()

        where:
        fileSize             | _
        0                    | _ // empty file
        20                   | _ // small file
        16 * 1024 * 1024     | _ // medium file in several chunks
        8 * 1026 * 1024 + 10 | _ // medium file not aligned to block
        50 * Constants.MB    | _ // large file requiring multiple requests
    }

    /*
     * Tests downloading a file using a default client that doesn't have a HttpClient passed to it.
     */

    @LiveOnly
    @Unroll
    def "Download file async buffer copy"() {
        setup:
        def containerName = generateContainerName()
        def blobServiceAsyncClient = new BlobServiceClientBuilder()
            .endpoint(env.primaryAccount.blobEndpoint)
            .credential(env.primaryAccount.credential)
            .buildAsyncClient()

        def blobAsyncClient = blobServiceAsyncClient.createBlobContainer(containerName).block()
            .getBlobAsyncClient(generateBlobName())

        def file = getRandomFile(fileSize)
        blobAsyncClient.uploadFromFile(file.toPath().toString(), true).block()
        def outFile = new File(namer.getRandomName(60) + ".txt")
        if (outFile.exists()) {
            assert outFile.delete()
        }

        when:
        def downloadMono = blobAsyncClient.downloadToFileWithResponse(outFile.toPath().toString(), null,
            new ParallelTransferOptions().setBlockSizeLong(4 * 1024 * 1024), null, null, false)

        then:
        StepVerifier.create(downloadMono)
            .assertNext({ it -> it.getValue().getBlobType() == BlobType.BLOCK_BLOB })
            .verifyComplete()

        compareFiles(file, outFile, 0, fileSize)

        cleanup:
        blobServiceAsyncClient.deleteBlobContainer(containerName)
        outFile.delete()
        file.delete()

        where:
        fileSize             | _
        0                    | _ // empty file
        20                   | _ // small file
        16 * 1024 * 1024     | _ // medium file in several chunks
        8 * 1026 * 1024 + 10 | _ // medium file not aligned to block
        50 * Constants.MB    | _ // large file requiring multiple requests
    }

    @LiveOnly
    @Unroll
    def "Download file range"() {
        setup:
        def file = getRandomFile(data.defaultDataSize)
        bc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(namer.getRandomName(60))
        if (outFile.exists()) {
            assert outFile.delete()
        }

        when:
        bc.downloadToFileWithResponse(outFile.toPath().toString(), range, null, null, null, false, null, null)

        then:
        compareFiles(file, outFile, range.getOffset(), range.getCount())

        cleanup:
        outFile.delete()
        file.delete()

        /*
        The last case is to test a range much much larger than the size of the file to ensure we don't accidentally
        send off parallel requests with invalid ranges.
         */
        where:
        range                                              | _
        new BlobRange(0, data.defaultDataSize)             | _ // Exact count
        new BlobRange(1, data.defaultDataSize - 1 as Long) | _ // Offset and exact count
        new BlobRange(3, 2)                                | _ // Narrow range in middle
        new BlobRange(0, data.defaultDataSize - 1 as Long) | _ // Count that is less than total
        new BlobRange(0, 10 * 1024)                        | _ // Count much larger than remaining data
    }

    /*
    This is to exercise some additional corner cases and ensure there are no arithmetic errors that give false success.
     */

    @LiveOnly
    @Unroll
    def "Download file range fail"() {
        setup:
        def file = getRandomFile(data.defaultDataSize)
        bc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(namer.getResourcePrefix())
        if (outFile.exists()) {
            assert outFile.delete()
        }

        when:
        bc.downloadToFileWithResponse(outFile.toPath().toString(), new BlobRange(data.defaultDataSize + 1), null, null, null, false,
            null, null)

        then:
        thrown(BlobStorageException)

        cleanup:
        outFile.delete()
        file.delete()
    }

    @LiveOnly
    def "Download file count null"() {
        setup:
        def file = getRandomFile(data.defaultDataSize)
        bc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(namer.getResourcePrefix())
        if (outFile.exists()) {
            assert outFile.delete()
        }

        when:
        bc.downloadToFileWithResponse(outFile.toPath().toString(), new BlobRange(0), null, null, null, false, null, null)

        then:
        compareFiles(file, outFile, 0, data.defaultDataSize)

        cleanup:
        outFile.delete()
        file.delete()
    }

    @LiveOnly
    @Unroll
    def "Download file AC"() {
        setup:
        def file = getRandomFile(data.defaultDataSize)
        bc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(namer.getResourcePrefix())
        if (outFile.exists()) {
            assert outFile.delete()
        }

        match = setupBlobMatchCondition(bc, match)
        leaseID = setupBlobLeaseCondition(bc, leaseID)
        BlobRequestConditions bro = new BlobRequestConditions().setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified).setIfMatch(match).setIfNoneMatch(noneMatch)
            .setLeaseId(leaseID)

        when:
        bc.downloadToFileWithResponse(outFile.toPath().toString(), null, null, null, bro, false, null, null)

        then:
        notThrown(BlobStorageException)

        cleanup:
        outFile.delete()
        file.delete()

        where:
        modified | unmodified | match        | noneMatch   | leaseID
        null     | null       | null         | null        | null
        oldDate  | null       | null         | null        | null
        null     | newDate    | null         | null        | null
        null     | null       | receivedEtag | null        | null
        null     | null       | null         | garbageEtag | null
        null     | null       | null         | null        | receivedLeaseID
    }

    @LiveOnly
    @Unroll
    def "Download file AC fail"() {
        setup:
        def file = getRandomFile(data.defaultDataSize)
        bc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(namer.getResourcePrefix())
        if (outFile.exists()) {
            assert outFile.delete()
        }

        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        setupBlobLeaseCondition(bc, leaseID)
        BlobRequestConditions bro = new BlobRequestConditions().setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified).setIfMatch(match).setIfNoneMatch(noneMatch)
            .setLeaseId(leaseID)

        when:
        bc.downloadToFileWithResponse(outFile.toPath().toString(), null, null, null, bro, false, null, null)

        then:
        def e = thrown(BlobStorageException)
        e.getErrorCode() == BlobErrorCode.CONDITION_NOT_MET ||
            e.getErrorCode() == BlobErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION

        cleanup:
        outFile.delete()
        file.delete()

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | garbageLeaseID
    }

    @LiveOnly
    def "Download file etag lock"() {
        setup:
        def file = getRandomFile(Constants.MB)
        bc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(namer.getResourcePrefix())
        Files.deleteIfExists(file.toPath())

        expect:
        def bac = instrument(new BlobClientBuilder()
            .pipeline(bc.getHttpPipeline())
            .endpoint(bc.getBlobUrl()))
            .buildAsyncClient()
            .getBlockBlobAsyncClient()

        /*
         * Setup the download to happen in small chunks so many requests need to be sent, this will give the upload time
         * to change the ETag therefore failing the download.
         */
        def options = new ParallelTransferOptions().setBlockSizeLong(Constants.KB)

        /*
         * This is done to prevent onErrorDropped exceptions from being logged at the error level. If no hook is
         * registered for onErrorDropped the error is logged at the ERROR level.
         *
         * onErrorDropped is triggered once the reactive stream has emitted one element, after that exceptions are
         * dropped.
         */
        Hooks.onErrorDropped({ ignored -> /* do nothing with it */ })

        /*
         * When the download begins trigger an upload to overwrite the downloading blob after waiting 500 milliseconds
         * so that the download is able to get an ETag before it is changed.
         */
        StepVerifier.create(bac.downloadToFileWithResponse(outFile.toPath().toString(), null, options, null, null, false)
            .doOnSubscribe({ bac.upload(data.defaultFlux, data.defaultDataSize, true).delaySubscription(Duration.ofMillis(500)).subscribe() }))
            .verifyErrorSatisfies({
                /*
                 * If an operation is running on multiple threads and multiple return an exception Reactor will combine
                 * them into a CompositeException which needs to be unwrapped. If there is only a single exception
                 * 'Exceptions.unwrapMultiple' will return a singleton list of the exception it was passed.
                 *
                 * These exceptions may be wrapped exceptions where the exception we are expecting is contained within
                 * ReactiveException that needs to be unwrapped. If the passed exception isn't a 'ReactiveException' it
                 * will be returned unmodified by 'Exceptions.unwrap'.
                 */
                assert Exceptions.unwrapMultiple(it).stream().anyMatch({ it2 ->
                    def exception = Exceptions.unwrap(it2)
                    if (exception instanceof BlobStorageException) {
                        assert ((BlobStorageException) exception).getStatusCode() == 412
                        return true
                    }
                })
            })

        // Give the file a chance to be deleted by the download operation before verifying its deletion
        sleep(500)
        !outFile.exists()

        cleanup:
        file.delete()
        outFile.delete()
    }

    @LiveOnly
    @Unroll
    def "Download file progress receiver"() {
        def file = getRandomFile(fileSize)
        bc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(namer.getResourcePrefix())
        if (outFile.exists()) {
            assert outFile.delete()
        }

        def mockReceiver = Mock(ProgressReceiver)

        def numBlocks = fileSize / (4 * 1024 * 1024)
        def prevCount = 0

        when:
        bc.downloadToFileWithResponse(outFile.toPath().toString(), null,
            new ParallelTransferOptions().setProgressReceiver(mockReceiver),
            new DownloadRetryOptions().setMaxRetryRequests(3), null, false, null, null)

        then:
        /*
         * Should receive at least one notification indicating completed progress, multiple notifications may be
         * received if there are empty buffers in the stream.
         */
        (1.._) * mockReceiver.reportProgress(fileSize)

        // There should be NO notification with a larger than expected size.
        0 * mockReceiver.reportProgress({ it > fileSize })

        /*
        We should receive at least one notification reporting an intermediary value per block, but possibly more
        notifications will be received depending on the implementation. We specify numBlocks - 1 because the last block
        will be the total size as above. Finally, we assert that the number reported monotonically increases.
         */
        (numBlocks - 1.._) * mockReceiver.reportProgress(!file.size()) >> { long bytesTransferred ->
            if (!(bytesTransferred >= prevCount)) {
                throw new IllegalArgumentException("Reported progress should monotonically increase")
            } else {
                prevCount = bytesTransferred
            }
        }

        // We should receive no notifications that report more progress than the size of the file.
        0 * mockReceiver.reportProgress({ it > fileSize })

        cleanup:
        file.delete()
        outFile.delete()

        where:
        fileSize             | _
        100                  | _
        8 * 1026 * 1024 + 10 | _
    }

    @Unroll
    @Ignore("Very large data sizes.")
    /* Enable once we have ability to run large resource heavy tests in CI. */
    def "Download to file blockSize"() {
        def file = getRandomFile(sizeOfData)
        bc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(namer.getResourcePrefix())
        if (outFile.exists()) {
            assert outFile.delete()
        }

        when:
        bc.downloadToFileWithResponse(new BlobDownloadToFileOptions(outFile.toPath().toString())
            .setParallelTransferOptions(new com.azure.storage.common.ParallelTransferOptions().setBlockSizeLong(downloadBlockSize))
            .setDownloadRetryOptions(new DownloadRetryOptions().setMaxRetryRequests(3)), null, null)

        then:
        notThrown(BlobStorageException)

        where:
        sizeOfData          | downloadBlockSize   || _
        5000 * Constants.MB | 5000 * Constants.MB || _ /* This was the default before. */
        6000 * Constants.MB | 6000 * Constants.MB || _ /* Trying to see if we can set it to a number greater than previous default. */
        6000 * Constants.MB | 5100 * Constants.MB || _ /* Testing chunking with a large size */
    }


    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    def "Get properties default"() {
        when:
        bc.setTags(Collections.singletonMap("foo", "bar"))
        def response = bc.getPropertiesWithResponse(null, null, null)
        def headers = response.getHeaders()
        def properties = response.getValue()

        then:
        validateBasicHeaders(headers)
        CoreUtils.isNullOrEmpty(properties.getMetadata())
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
        properties.getContentMd5() != null
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
        properties.getTagCount() == 1
        properties.getRehydratePriority() == null // tested in setTier rehydrate priority
        !properties.isSealed() // tested in AppendBlob. "seal blob"
//        properties.getLastAccessedTime() /* TODO: re-enable when last access time enabled. */
    }

    def "Get properties min"() {
        expect:
        bc.getPropertiesWithResponse(null, null, null).getStatusCode() == 200
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "Get properties AC"() {
        setup:
        def t = new HashMap<String, String>()
        t.put("foo", "bar")
        bc.setTags(t)
        def bac = new BlobRequestConditions()
            .setLeaseId(setupBlobLeaseCondition(bc, leaseID))
            .setIfMatch(setupBlobMatchCondition(bc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags)

        expect:
        bc.getPropertiesWithResponse(bac, null, null).getStatusCode() == 200

        where:
        modified | unmodified | match        | noneMatch   | leaseID         | tags
        null     | null       | null         | null        | null            | null
        oldDate  | null       | null         | null        | null            | null
        null     | newDate    | null         | null        | null            | null
        null     | null       | receivedEtag | null        | null            | null
        null     | null       | null         | garbageEtag | null            | null
        null     | null       | null         | null        | receivedLeaseID | null
        null     | null       | null         | null        | null            | "\"foo\" = 'bar'"
    }

    @Unroll
    def "Get properties AC fail"() {
        setup:
        def bac = new BlobRequestConditions()
            .setLeaseId(setupBlobLeaseCondition(bc, leaseID))
            .setIfMatch(match)
            .setIfNoneMatch(setupBlobMatchCondition(bc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags)

        when:
        bc.getPropertiesWithResponse(bac, null, null)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID        | tags
        newDate  | null       | null        | null         | null           | null
        null     | oldDate    | null        | null         | null           | null
        null     | null       | garbageEtag | null         | null           | null
        null     | null       | null        | receivedEtag | null           | null
        null     | null       | null        | null         | garbageLeaseID | null
        null     | null       | null        | null         | null           | "\"notfoo\" = 'notbar'"
    }

    /*
    This test requires two accounts that are configured in a very specific way. It is not feasible to setup that
    relationship programmatically, so we have recorded a successful interaction and only test recordings.
     */

    @PlaybackOnly
    def "Get properties ORS"() {
        setup:
        def sourceBlob = primaryBlobServiceClient.getBlobContainerClient("test1")
            .getBlobClient("javablobgetpropertiesors2blobapitestgetpropertiesors57d93407b")
        def destBlob = alternateBlobServiceClient.getBlobContainerClient("test2")
            .getBlobClient("javablobgetpropertiesors2blobapitestgetpropertiesors57d93407b")

        when:
        def sourceProperties = sourceBlob.getProperties()
        def sourceDownloadHeaders = sourceBlob.downloadWithResponse(new ByteArrayOutputStream(), null, null, null,
            false, null, null)
        def destProperties = destBlob.getProperties()
        def destDownloadHeaders = destBlob.downloadWithResponse(new ByteArrayOutputStream(), null, null, null,
            false, null, null)

        then:
        validateOR(sourceProperties.getObjectReplicationSourcePolicies(), "fd2da1b9-56f5-45ff-9eb6-310e6dfc2c80", "105f9aad-f39b-4064-8e47-ccd7937295ca")
        validateOR(sourceDownloadHeaders.getDeserializedHeaders().getObjectReplicationSourcePolicies(), "fd2da1b9-56f5-45ff-9eb6-310e6dfc2c80", "105f9aad-f39b-4064-8e47-ccd7937295ca")

        // There is a sas token attached at the end. Only check that the path is the same.
        destProperties.getCopySource().contains(new URL(sourceBlob.getBlobUrl()).getPath())
        destProperties.getObjectReplicationDestinationPolicyId() == "fd2da1b9-56f5-45ff-9eb6-310e6dfc2c80"
        destDownloadHeaders.getDeserializedHeaders().getObjectReplicationDestinationPolicyId() ==
            "fd2da1b9-56f5-45ff-9eb6-310e6dfc2c80"
    }

    def validateOR(List<ObjectReplicationPolicy> policies, String policyId, String ruleId) {
        return policies.stream()
            .filter({ policy -> policyId.equals(policy.getPolicyId()) })
            .findFirst()
            .get()
            .getRules()
            .stream()
            .filter({ rule -> ruleId.equals(rule.getRuleId()) })
            .findFirst()
            .get()
            .getStatus() == ObjectReplicationStatus.COMPLETE
    }

    // Test getting the properties from a listing

    def "Get properties error"() {
        setup:
        bc = cc.getBlobClient(generateBlobName())

        when:
        bc.getProperties()

        then:
        def ex = thrown(BlobStorageException)
        ex.getMessage().contains("BlobNotFound")
    }

    def "Set HTTP headers null"() {
        setup:
        def response = bc.setHttpHeadersWithResponse(null, null, null, null)

        expect:
        response.getStatusCode() == 200
        validateBasicHeaders(response.getHeaders())
    }

    def "Set HTTP headers min"() {
        setup:
        def properties = bc.getProperties()
        def headers = new BlobHttpHeaders()
            .setContentEncoding(properties.getContentEncoding())
            .setContentDisposition(properties.getContentDisposition())
            .setContentType("type")
            .setCacheControl(properties.getCacheControl())
            .setContentLanguage(properties.getContentLanguage())
            .setContentMd5(Base64.getEncoder().encode(MessageDigest.getInstance("MD5").digest(data.defaultBytes)))

        bc.setHttpHeaders(headers)

        expect:
        bc.getProperties().getContentType() == "type"
    }

    @Unroll
    def "Set HTTP headers headers"() {
        setup:
        def putHeaders = new BlobHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType)

        bc.setHttpHeaders(putHeaders)

        expect:
        validateBlobProperties(
            bc.getPropertiesWithResponse(null, null, null),
            cacheControl, contentDisposition, contentEncoding, contentLanguage, contentMD5, contentType)

        where:
        cacheControl | contentDisposition | contentEncoding | contentLanguage | contentMD5                                                                             | contentType
        null         | null               | null            | null            | null                                                                                   | null
        "control"    | "disposition"      | "encoding"      | "language"      | Base64.getEncoder().encode(MessageDigest.getInstance("MD5").digest(data.defaultBytes)) | "type"
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "Set HTTP headers AC"() {
        setup:
        def t = new HashMap<String, String>()
        t.put("foo", "bar")
        bc.setTags(t)
        match = setupBlobMatchCondition(bc, match)
        leaseID = setupBlobLeaseCondition(bc, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags)

        expect:
        bc.setHttpHeadersWithResponse(null, bac, null, null).getStatusCode() == 200

        where:
        modified | unmodified | match        | noneMatch   | leaseID         | tags
        null     | null       | null         | null        | null            | null
        oldDate  | null       | null         | null        | null            | null
        null     | newDate    | null         | null        | null            | null
        null     | null       | receivedEtag | null        | null            | null
        null     | null       | null         | garbageEtag | null            | null
        null     | null       | null         | null        | receivedLeaseID | null
        null     | null       | null         | null        | null            | "\"foo\" = 'bar'"
    }

    @Unroll
    def "Set HTTP headers AC fail"() {
        setup:
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        setupBlobLeaseCondition(bc, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags)

        when:
        bc.setHttpHeadersWithResponse(null, bac, null, null)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID        | tags
        newDate  | null       | null        | null         | null           | null
        null     | oldDate    | null        | null         | null           | null
        null     | null       | garbageEtag | null         | null           | null
        null     | null       | null        | receivedEtag | null           | null
        null     | null       | null        | null         | garbageLeaseID | null
        null     | null       | null        | null         | null           | "\"notfoo\" = 'notbar'"
    }

    def "Set HTTP headers error"() {
        setup:
        bc = cc.getBlobClient(generateBlobName())

        when:
        bc.setHttpHeaders(null)

        then:
        thrown(BlobStorageException)
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
        key1  | value1       | key2   | value2 || statusCode
        null  | null         | null   | null   || 200
        "foo" | "bar"        | "fizz" | "buzz" || 200
        "i0"  | "a"          | "i_"   | "a"    || 200 /* Test culture sensitive word sort */
        "foo" | "bar0, bar1" | null   | null   || 200 /* Test comma separated values */
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "Set metadata AC"() {
        setup:
        def t = new HashMap<String, String>()
        t.put("foo", "bar")
        bc.setTags(t)
        match = setupBlobMatchCondition(bc, match)
        leaseID = setupBlobLeaseCondition(bc, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        bc.setMetadataWithResponse(null, bac, null, null).getStatusCode() == 200

        where:
        modified | unmodified | match        | noneMatch   | leaseID         | tags
        null     | null       | null         | null        | null            | null
        oldDate  | null       | null         | null        | null            | null
        null     | newDate    | null         | null        | null            | null
        null     | null       | receivedEtag | null        | null            | null
        null     | null       | null         | garbageEtag | null            | null
        null     | null       | null         | null        | receivedLeaseID | null
        null     | null       | null         | null        | null            | "\"foo\" = 'bar'"
    }

    @Unroll
    def "Set metadata AC fail"() {
        setup:
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        setupBlobLeaseCondition(bc, leaseID)

        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags)

        when:
        bc.setMetadataWithResponse(null, bac, null, null)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID        | tags
        newDate  | null       | null        | null         | null           | null
        null     | oldDate    | null        | null         | null           | null
        null     | null       | garbageEtag | null         | null           | null
        null     | null       | null        | receivedEtag | null           | null
        null     | null       | null        | null         | garbageLeaseID | null
        null     | null       | null        | null         | null           | "\"notfoo\" = 'notbar'"
    }

    @Unroll
    def "Set metadata whitespace error"() {
        setup:
        def metadata = new HashMap<String, String>()
        metadata.put(key, value)

        when:
        bc.setMetadata(metadata)

        then:
        def e = thrown(Exception)
        e instanceof IllegalArgumentException || e instanceof Exceptions.ReactiveException
        // Need this second error type since for the first case, Netty throws IllegalArgumentException, and that is recorded in the playback file.
        // On Playback, the framework will throw Exceptions.ReactiveException.

        where:
        key    | value  || _
        " foo" | "bar"  || _ // Leading whitespace key
        "foo " | "bar"  || _ // Trailing whitespace key
        "foo"  | " bar" || _ // Leading whitespace value
        "foo"  | "bar " || _ // Trailing whitespace value
    }

    def "Set metadata error"() {
        setup:
        bc = cc.getBlobClient(generateBlobName())

        when:
        bc.setMetadata(null)

        then:
        thrown(BlobStorageException)
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    def "Set tags all null"() {
        when:
        def response = bc.setTagsWithResponse(new BlobSetTagsOptions(new HashMap<String, String>()), null, null)

        then:
        bc.getTags().size() == 0
        response.getStatusCode() == 204
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    def "Set tags min"() {
        setup:
        def tags = new HashMap<String, String>()
        tags.put("foo", "bar")

        when:
        bc.setTags(tags)

        then:
        bc.getTags() == tags
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "Set tags tags"() {
        setup:
        def tags = new HashMap<String, String>()
        if (key1 != null && value1 != null) {
            tags.put(key1, value1)
        }
        if (key2 != null && value2 != null) {
            tags.put(key2, value2)
        }

        expect:
        bc.setTagsWithResponse(new BlobSetTagsOptions(tags), null, null).getStatusCode() == statusCode
        bc.getTags() == tags

        where:
        key1                | value1     | key2   | value2 || statusCode
        null                | null       | null   | null   || 204
        "foo"               | "bar"      | "fizz" | "buzz" || 204
        " +-./:=_  +-./:=_" | " +-./:=_" | null   | null   || 204
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "Set tags AC"() {
        setup:
        def t = new HashMap<String, String>()
        t.put("foo", "bar")
        bc.setTags(t)
        t = new HashMap<String, String>()
        t.put("fizz", "buzz")

        expect:
        bc.setTagsWithResponse(new BlobSetTagsOptions(t).setRequestConditions(new BlobRequestConditions().setTagsConditions(tags)), null, null).getStatusCode() == 204

        where:
        tags              || _
        null              || _
        "\"foo\" = 'bar'" || _
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_07_07")
    @Unroll
    def "Set tags AC fail"() {
        setup:
        def t = new HashMap<String, String>()
        t.put("fizz", "buzz")

        when:
        bc.setTagsWithResponse(new BlobSetTagsOptions(t).setRequestConditions(new BlobRequestConditions().setTagsConditions(tags)), null, null)

        then:
        thrown(BlobStorageException)

        where:
        tags              || _
        "\"foo\" = 'bar'" || _
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "Get tags AC"() {
        setup:
        def t = new HashMap<String, String>()
        t.put("foo", "bar")
        bc.setTags(t)

        expect:
        bc.getTagsWithResponse(new BlobGetTagsOptions().setRequestConditions(new BlobRequestConditions().setTagsConditions(tags)), null, null).getStatusCode() == 200

        where:
        tags              || _
        null              || _
        "\"foo\" = 'bar'" || _
    }

    @Unroll
    def "Get tags AC fail"() {
        setup:
        def t = new HashMap<String, String>()
        t.put("fizz", "buzz")

        when:
        bc.getTagsWithResponse(new BlobGetTagsOptions().setRequestConditions(new BlobRequestConditions().setTagsConditions(tags)), null, null)

        then:
        thrown(BlobStorageException)

        where:
        tags              || _
        "\"foo\" = 'bar'" || _
    }

    def "Set tags error"() {
        setup:
        bc = cc.getBlobClient(generateBlobName())

        when:
        bc.setTags(new HashMap<String, String>())

        then:
        thrown(BlobStorageException)
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    def "Set tags lease"() {
        setup:
        def tags = new HashMap<String, String>()
        tags.put("foo", "bar")
        def leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)
        def bac = new BlobRequestConditions().setLeaseId(leaseID)

        when:
        def response = bc.setTagsWithResponse(new BlobSetTagsOptions(tags).setRequestConditions(bac), null, null)

        then:
        response.getStatusCode() == 204
        bc.getTags() == tags
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    def "Get tags lease"() {
        setup:
        def tags = new HashMap<String, String>()
        tags.put("foo", "bar")
        def leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)
        def bac = new BlobRequestConditions().setLeaseId(leaseID)
        bc.setTagsWithResponse(new BlobSetTagsOptions(tags).setRequestConditions(bac), null, null)

        when:
        def response = bc.getTagsWithResponse(new BlobGetTagsOptions().setRequestConditions(bac), null, null)

        then:
        response.getStatusCode() == 200
        response.getValue() == tags
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    def "Set tags lease fail"() {
        setup:
        def tags = new HashMap<String, String>()
        tags.put("foo", "bar")
        def bac = new BlobRequestConditions().setLeaseId(garbageLeaseID)

        when:
        bc.setTagsWithResponse(new BlobSetTagsOptions(tags).setRequestConditions(bac), null, null)

        then:
        def e = thrown(BlobStorageException)
        e.getStatusCode() == 412
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    def "Get tags lease fail"() {
        setup:
        def tags = new HashMap<String, String>()
        tags.put("foo", "bar")
        bc.setTags(tags)
        def bac = new BlobRequestConditions().setLeaseId(garbageLeaseID)

        when:
        bc.getTagsWithResponse(new BlobGetTagsOptions().setRequestConditions(bac), null, null)

        then:
        def e = thrown(BlobStorageException)
        e.getStatusCode() == 412
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

    def "getSnapshot"() {
        setup:
        def data = "test".getBytes()
        def blobName = generateBlobName()
        def bu = cc.getBlobClient(blobName).getBlockBlobClient()
        bu.upload(new ByteArrayInputStream(data), data.length)
        def snapshotId = bu.createSnapshot().getSnapshotId()

        when:
        def snapshotBlob = cc.getBlobClient(blobName, snapshotId).getBlockBlobClient()

        then:
        snapshotBlob.getSnapshotId() == snapshotId
        bu.getSnapshotId() == null
    }

    def "isSnapshot"() {
        setup:
        def data = "test".getBytes()
        def blobName = generateBlobName()
        def bu = cc.getBlobClient(blobName).getBlockBlobClient()
        bu.upload(new ByteArrayInputStream(data), data.length)
        def snapshotId = bu.createSnapshot().getSnapshotId()

        when:
        def snapshotBlob = cc.getBlobClient(blobName, snapshotId).getBlockBlobClient()

        then:
        snapshotBlob.isSnapshot()
        !bu.isSnapshot()
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

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "Snapshot AC"() {
        setup:
        def t = new HashMap<String, String>()
        t.put("foo", "bar")
        bc.setTags(t)
        match = setupBlobMatchCondition(bc, match)
        leaseID = setupBlobLeaseCondition(bc, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags)

        expect:
        bc.createSnapshotWithResponse(null, bac, null, null).getStatusCode() == 201

        where:
        modified | unmodified | match        | noneMatch   | leaseID         | tags
        null     | null       | null         | null        | null            | null
        oldDate  | null       | null         | null        | null            | null
        null     | newDate    | null         | null        | null            | null
        null     | null       | receivedEtag | null        | null            | null
        null     | null       | null         | garbageEtag | null            | null
        null     | null       | null         | null        | receivedLeaseID | null
        null     | null       | null         | null        | null            | "\"foo\" = 'bar'"
    }

    @Unroll
    def "Snapshot AC fail"() {
        setup:
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        setupBlobLeaseCondition(bc, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags)

        when:
        bc.createSnapshotWithResponse(null, bac, null, null)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID        | tags
        newDate  | null       | null        | null         | null           | null
        null     | oldDate    | null        | null         | null           | null
        null     | null       | garbageEtag | null         | null           | null
        null     | null       | null        | receivedEtag | null           | null
        null     | null       | null        | null         | garbageLeaseID | null
        null     | null       | null        | null         | null           | "\"notfoo\" = 'notbar'"
    }

    def "Snapshot error"() {
        setup:
        bc = cc.getBlobClient(generateBlobName())

        when:
        bc.createSnapshot()

        then:
        thrown(BlobStorageException)
    }

    def "Copy"() {
        setup:
        def copyDestBlob = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient()
        def poller = copyDestBlob.beginCopy(bc.getBlobUrl(), getPollingDuration(1000))

        when:
        def response = poller.blockLast()
        def properties = copyDestBlob.getProperties().block()

        then:
        properties.getCopyStatus() == CopyStatusType.SUCCESS
        properties.getCopyCompletionTime() != null
        properties.getCopyProgress() != null
        properties.getCopySource() != null

        response != null
        response.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED

        def blobInfo = response.getValue()
        blobInfo != null
        blobInfo.getCopyId() == properties.getCopyId()
    }

    def "Copy min"() {
        setup:
        def copyDestBlob = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient()

        when:
        def poller = copyDestBlob.beginCopy(bc.getBlobUrl(), getPollingDuration(1000))
        def verifier = StepVerifier.create(poller.take(1))

        then:
        verifier.assertNext({
            assert it.getValue() != null
            assert it.getValue().getCopyId() != null
            assert it.getValue().getCopySourceUrl() == bc.getBlobUrl()
            assert it.getStatus() == LongRunningOperationStatus.IN_PROGRESS || it.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED
        }).verifyComplete()
    }

    def "Copy poller"() {
        setup:
        def copyDestBlob = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient()

        when:
        def poller = copyDestBlob.beginCopy(bc.getBlobUrl(), null, null, null, null, null, getPollingDuration(1000))

        then:
        def lastResponse = poller.doOnNext({
            assert it.getValue() != null
            assert it.getValue().getCopyId() != null
            assert it.getValue().getCopySourceUrl() == bc.getBlobUrl()
        }).blockLast()

        expect:
        lastResponse != null
        lastResponse.getValue() != null

        StepVerifier.create(copyDestBlob.getProperties())
            .assertNext({
                assert it.getCopyId() == lastResponse.getValue().getCopyId()
                assert it.getCopyStatus() == CopyStatusType.SUCCESS
                assert it.getCopyCompletionTime() != null
                assert it.getCopyProgress() != null
                assert it.getCopySource() != null
                assert it.getCopyId() != null
            })
            .verifyComplete()
    }

    @Unroll
    def "Copy metadata"() {
        setup:
        def bu2 = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient()
        def metadata = new HashMap<String, String>()
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2)
        }

        when:
        def poller = bu2.beginCopy(bc.getBlobUrl(), metadata, null, null, null, null, getPollingDuration(1000))
        poller.blockLast()

        then:
        StepVerifier.create(bu2.getProperties())
            .assertNext({ assert it.getMetadata() == metadata })
            .verifyComplete()

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "Copy tags"() {
        setup:
        def bu2 = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient()
        def tags = new HashMap<String, String>()
        if (key1 != null && value1 != null) {
            tags.put(key1, value1)
        }
        if (key2 != null && value2 != null) {
            tags.put(key2, value2)
        }

        when:
        def poller = bu2.beginCopy(new BlobBeginCopyOptions(bc.getBlobUrl()).setTags(tags)
            .setPollInterval(getPollingDuration(1000)))
        poller.blockLast()

        then:
        StepVerifier.create(bu2.getTags())
            .assertNext({ assert it == tags })
            .verifyComplete()

        where:
        key1                | value1     | key2   | value2
        null                | null       | null   | null
        "foo"               | "bar"      | "fizz" | "buzz"
        " +-./:=_  +-./:=_" | " +-./:=_" | null   | null
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "Copy seal"() {
        setup:
        def appendBlobClient = cc.getBlobClient(generateBlobName()).getAppendBlobClient()
        appendBlobClient.create()
        if (source) {
            appendBlobClient.seal()
        }

        def bu2 = ccAsync.getBlobAsyncClient(generateBlobName()).getAppendBlobAsyncClient()

        when:
        def poller = bu2.beginCopy(new BlobBeginCopyOptions(appendBlobClient.getBlobUrl()).setSealDestination(destination)
            .setPollInterval(getPollingDuration(1000)))
        poller.blockLast()

        then:
        StepVerifier.create(bu2.getProperties())
            .assertNext({ assert Boolean.TRUE.equals(it.isSealed()) == destination })
            .verifyComplete()

        where:
        source | destination
        true   | true
        true   | false
        false  | true
        false  | false
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "Copy source AC"() {
        setup:
        def t = new HashMap<String, String>()
        t.put("foo", "bar")
        bc.setTags(t)
        def copyDestBlob = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient()
        match = setupBlobMatchCondition(bc, match)
        def mac = new BlobBeginCopySourceRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setTagsConditions(tags)

        when:
        def poller = copyDestBlob.beginCopy(new BlobBeginCopyOptions(bc.getBlobUrl()).setSourceRequestConditions(mac))
        def response = poller.blockLast()

        then:
        response.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED

        where:
        modified | unmodified | match        | noneMatch   | tags
        null     | null       | null         | null        | null
        oldDate  | null       | null         | null        | null
        null     | newDate    | null         | null        | null
        null     | null       | receivedEtag | null        | null
        null     | null       | null         | garbageEtag | null
        null     | null       | null         | null        | "\"foo\" = 'bar'"
    }

    @Unroll
    def "Copy source AC fail"() {
        setup:
        def copyDestBlob = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient()
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        def mac = new BlobBeginCopySourceRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setTagsConditions(tags)

        when:
        def poller = copyDestBlob.beginCopy(new BlobBeginCopyOptions(bc.getBlobUrl()).setSourceRequestConditions(mac))
        poller.blockLast()

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match       | noneMatch    | tags
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | "\"notfoo\" = 'notbar'"
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "Copy dest AC"() {
        setup:
        def bu2 = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient()
        bu2.upload(data.defaultFlux, data.defaultDataSize).block()
        def t = new HashMap<String, String>()
        t.put("foo", "bar")
        bu2.setTags(t).block()
        match = setupBlobMatchCondition(bu2, match)
        leaseID = setupBlobLeaseCondition(bu2, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags)

        when:
        def poller = bu2.beginCopy(bc.getBlobUrl(), null, null, null, null, bac, getPollingDuration(1000))
        def response = poller.blockLast()

        then:
        response.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED

        where:
        modified | unmodified | match        | noneMatch   | leaseID         | tags
        null     | null       | null         | null        | null            | null
        oldDate  | null       | null         | null        | null            | null
        null     | newDate    | null         | null        | null            | null
        null     | null       | receivedEtag | null        | null            | null
        null     | null       | null         | garbageEtag | null            | null
        null     | null       | null         | null        | receivedLeaseID | null
        null     | null       | null         | null        | null            | "\"foo\" = 'bar'"
    }

    @Unroll
    def "Copy dest AC fail"() {
        setup:
        def bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        bu2.upload(data.defaultInputStream, data.defaultDataSize)
        noneMatch = setupBlobMatchCondition(bu2, noneMatch)
        setupBlobLeaseCondition(bu2, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags)

        when:
        bu2.copyFromUrlWithResponse(bc.getBlobUrl(), null, null, null, bac, null, null)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID        | tags
        newDate  | null       | null        | null         | null           | null
        null     | oldDate    | null        | null         | null           | null
        null     | null       | garbageEtag | null         | null           | null
        null     | null       | null        | receivedEtag | null           | null
        null     | null       | null        | null         | garbageLeaseID | null
        null     | null       | null        | null         | null           | "\"notfoo\" = 'notbar'"
    }

    def "Abort copy lease fail"() {
        setup:
        // Data has to be large enough and copied between accounts to give us enough time to abort
        new SpecializedBlobClientBuilder()
            .blobClient(bc)
            .buildBlockBlobClient()
            .upload(new ByteArrayInputStream(getRandomByteArray(8 * 1024 * 1024)), 8 * 1024 * 1024, true)
        // So we don't have to create a SAS.
        cc.setAccessPolicy(PublicAccessType.BLOB, null)

        def cu2 = alternateBlobServiceClient.getBlobContainerClient(generateBlobName())
        cu2.create()
        def bu2 = cu2.getBlobClient(generateBlobName()).getBlockBlobClient()
        bu2.upload(data.defaultInputStream, data.defaultDataSize)

        def leaseId = setupBlobLeaseCondition(bu2, receivedLeaseID)
        def blobRequestConditions = new BlobRequestConditions().setLeaseId(leaseId)

        when:
        def poller = bu2.beginCopy(bc.getBlobUrl(), null, null, null, null, blobRequestConditions, getPollingDuration(500))
        def response = poller.poll()

        assert response.getStatus() != LongRunningOperationStatus.FAILED

        def blobCopyInfo = response.getValue()
        bu2.abortCopyFromUrlWithResponse(blobCopyInfo.getCopyId(), garbageLeaseID, null, null)

        then:
        def e = thrown(BlobStorageException)
        e.getStatusCode() == 412

        cleanup:
        cu2.delete()
    }

    def "Abort copy"() {
        setup:
        // Data has to be large enough and copied between accounts to give us enough time to abort
        new SpecializedBlobClientBuilder()
            .blobClient(bc)
            .buildBlockBlobClient()
            .upload(new ByteArrayInputStream(getRandomByteArray(8 * 1024 * 1024)), 8 * 1024 * 1024, true)
        // So we don't have to create a SAS.
        cc.setAccessPolicy(PublicAccessType.BLOB, null)

        def cu2 = alternateBlobServiceClient.getBlobContainerClient(generateBlobName())
        cu2.create()
        def bu2 = cu2.getBlobClient(generateBlobName())

        when:
        def poller = bu2.beginCopy(bc.getBlobUrl(), null, null, null, null, null, getPollingDuration(1000))
        def lastResponse = poller.poll()

        assert lastResponse != null
        assert lastResponse.getValue() != null

        def response = bu2.abortCopyFromUrlWithResponse(lastResponse.getValue().getCopyId(), null, null, null)
        def headers = response.getHeaders()

        then:
        response.getStatusCode() == 204
        headers.getValue("x-ms-request-id") != null
        headers.getValue("x-ms-version") != null
        headers.getValue("Date") != null

        cleanup:
        // Normal test cleanup will not clean up containers in the alternate account.
        cu2.deleteWithResponse(null, null, null).getStatusCode() == 202
    }

    def "Abort copy lease"() {
        setup:
        // Data has to be large enough and copied between accounts to give us enough time to abort
        new SpecializedBlobClientBuilder().blobClient(bc)
            .buildBlockBlobClient()
            .upload(new ByteArrayInputStream(getRandomByteArray(8 * 1024 * 1024)), 8 * 1024 * 1024, true)
        // So we don't have to create a SAS.
        cc.setAccessPolicy(PublicAccessType.BLOB, null)

        def cu2 = alternateBlobServiceClient.getBlobContainerClient(generateContainerName())
        cu2.create()
        def bu2 = cu2.getBlobClient(generateBlobName()).getBlockBlobClient()
        bu2.upload(data.defaultInputStream, data.defaultDataSize)
        def leaseId = setupBlobLeaseCondition(bu2, receivedLeaseID)
        def blobAccess = new BlobRequestConditions().setLeaseId(leaseId)

        when:
        def poller = bu2.beginCopy(bc.getBlobUrl(), null, null, null, null, blobAccess, getPollingDuration(1000))
        def lastResponse = poller.poll()

        then:
        lastResponse != null
        lastResponse.getValue() != null

        def copyId = lastResponse.getValue().getCopyId()
        bu2.abortCopyFromUrlWithResponse(copyId, leaseId, null, null).getStatusCode() == 204

        cleanup:
        // Normal test cleanup will not clean up containers in the alternate account.
        cu2.delete()
    }

    def "Copy error"() {
        setup:
        bc = cc.getBlobClient(generateBlobName())

        when:
        bc.copyFromUrl("http://www.error.com")

        then:
        thrown(BlobStorageException)
    }

    def "Abort copy error"() {
        setup:
        bc = cc.getBlobClient(generateBlobName())

        when:
        bc.abortCopyFromUrl("id")

        then:
        thrown(BlobStorageException)
    }

    def "Sync copy"() {
        setup:
        // Sync copy is a deep copy, which requires either sas or public access.
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        def headers = bu2.copyFromUrlWithResponse(bc.getBlobUrl(), null, null, null, null, null, null).getHeaders()

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
        bu2.copyFromUrlWithResponse(bc.getBlobUrl(), null, null, null, null, null, null).getStatusCode() == 202
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
        bu2.copyFromUrlWithResponse(bc.getBlobUrl(), metadata, null, null, null, null, null)

        then:
        bu2.getProperties().getMetadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "Sync copy tags"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        def tags = new HashMap<String, String>()
        if (key1 != null && value1 != null) {
            tags.put(key1, value1)
        }
        if (key2 != null && value2 != null) {
            tags.put(key2, value2)
        }

        when:
        bu2.copyFromUrlWithResponse(new BlobCopyFromUrlOptions(bc.getBlobUrl()).setTags(tags), null, null)

        then:
        bu2.getTags() == tags

        where:
        key1                | value1     | key2   | value2
        null                | null       | null   | null
        "foo"               | "bar"      | "fizz" | "buzz"
        " +-./:=_  +-./:=_" | " +-./:=_" | null   | null
    }

    @Unroll
    def "Sync copy source AC"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        match = setupBlobMatchCondition(bc, match)
        def mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)

        expect:
        bu2.copyFromUrlWithResponse(bc.getBlobUrl(), null, null, mac, null, null, null).getStatusCode() == 202

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
        def mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)

        when:
        bu2.copyFromUrlWithResponse(bc.getBlobUrl(), null, null, mac, null, null, null)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match       | noneMatch
        newDate  | null       | null        | null
        null     | oldDate    | null        | null
        null     | null       | garbageEtag | null
        null     | null       | null        | receivedEtag
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "Sync copy dest AC"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        bu2.upload(data.defaultInputStream, data.defaultDataSize)
        def t = new HashMap<String, String>()
        t.put("foo", "bar")
        bu2.setTags(t)
        match = setupBlobMatchCondition(bu2, match)
        leaseID = setupBlobLeaseCondition(bu2, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags)

        expect:
        bu2.copyFromUrlWithResponse(bc.getBlobUrl(), null, null, null, bac, null, null).getStatusCode() == 202

        where:
        modified | unmodified | match        | noneMatch   | leaseID         | tags
        null     | null       | null         | null        | null            | null
        oldDate  | null       | null         | null        | null            | null
        null     | newDate    | null         | null        | null            | null
        null     | null       | receivedEtag | null        | null            | null
        null     | null       | null         | garbageEtag | null            | null
        null     | null       | null         | null        | receivedLeaseID | null
        null     | null       | null         | null        | null            | "\"foo\" = 'bar'"
    }

    @Unroll
    def "Sync copy dest AC fail"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        bu2.upload(data.defaultInputStream, data.defaultDataSize)
        noneMatch = setupBlobMatchCondition(bu2, noneMatch)
        setupBlobLeaseCondition(bu2, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags)

        when:
        bu2.copyFromUrlWithResponse(bc.getBlobUrl(), null, null, null, bac, null, null)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID        | tags
        newDate  | null       | null        | null         | null           | null
        null     | oldDate    | null        | null         | null           | null
        null     | null       | garbageEtag | null         | null           | null
        null     | null       | null        | receivedEtag | null           | null
        null     | null       | null        | null         | garbageLeaseID | null
        null     | null       | null        | null         | null           | "\"notfoo\" = 'notbar'"
    }

    def "Sync copy error"() {
        setup:
        def bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        bu2.copyFromUrl(bc.getBlobUrl())

        then:
        thrown(BlobStorageException)
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
        bu2.upload(data.defaultInputStream, data.defaultDataSize)

        when:
        bc.deleteWithResponse(option, null, null, null)

        then:
        cc.listBlobs().stream().count() == blobsRemaining

        where:
        option                            | blobsRemaining
        DeleteSnapshotsOptionType.INCLUDE | 1
        DeleteSnapshotsOptionType.ONLY    | 2
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "Delete AC"() {
        setup:
        def t = new HashMap<String, String>()
        t.put("foo", "bar")
        bc.setTags(t)
        match = setupBlobMatchCondition(bc, match)
        leaseID = setupBlobLeaseCondition(bc, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags)

        expect:
        bc.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, bac, null, null).getStatusCode() == 202

        where:
        modified | unmodified | match        | noneMatch   | leaseID         | tags
        null     | null       | null         | null        | null            | null
        oldDate  | null       | null         | null        | null            | null
        null     | newDate    | null         | null        | null            | null
        null     | null       | receivedEtag | null        | null            | null
        null     | null       | null         | garbageEtag | null            | null
        null     | null       | null         | null        | receivedLeaseID | null
        null     | null       | null         | null        | null            | "\"foo\" = 'bar'"
    }

    @Unroll
    def "Delete AC fail"() {
        setup:
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        setupBlobLeaseCondition(bc, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags)

        when:
        bc.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, bac, null, null)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID        | tags
        newDate  | null       | null        | null         | null           | null
        null     | oldDate    | null        | null         | null           | null
        null     | null       | garbageEtag | null         | null           | null
        null     | null       | null        | receivedEtag | null           | null
        null     | null       | null        | null         | garbageLeaseID | null
        null     | null       | null        | null         | null           | "\"notfoo\" = 'notbar'"
    }

    def "Blob delete error"() {
        setup:
        bc = cc.getBlobClient(generateBlobName())

        when:
        bc.delete()

        then:
        thrown(BlobStorageException)
    }

    @Unroll
    def "Set tier block blob"() {
        setup:
        def cc = primaryBlobServiceClient.createBlobContainer(generateContainerName())
        def bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        bc.upload(data.defaultInputStream, data.defaultData.remaining())

        when:
        def initialResponse = bc.setAccessTierWithResponse(tier, null, null, null, null)
        def headers = initialResponse.getHeaders()

        then:
        initialResponse.getStatusCode() == 200 || initialResponse.getStatusCode() == 202
        headers.getValue("x-ms-version") != null
        headers.getValue("x-ms-request-id") != null
        bc.getProperties().getAccessTier() == tier
        cc.listBlobs().iterator().next().getProperties().getAccessTier() == tier

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
        cc.listBlobs().iterator().next().getProperties().getAccessTier() == tier

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
        def cc = primaryBlobServiceClient.createBlobContainer(generateContainerName())
        def bu = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        bu.upload(data.defaultInputStream, data.defaultData.remaining())

        when:
        def statusCode = bc.setAccessTierWithResponse(AccessTier.HOT, null, null, null, null).getStatusCode()

        then:
        statusCode == 200 || statusCode == 202

        cleanup:
        cc.delete()
    }

    def "Set tier inferred"() {
        setup:
        def cc = primaryBlobServiceClient.createBlobContainer(generateBlobName())
        def bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        bc.upload(data.defaultInputStream, data.defaultDataSize)

        when:
        def inferred1 = bc.getProperties().isAccessTierInferred()
        def inferredList1 = cc.listBlobs().iterator().next().getProperties().isAccessTierInferred()

        bc.setAccessTier(AccessTier.HOT)

        def inferred2 = bc.getProperties().isAccessTierInferred()
        def inferredList2 = cc.listBlobs().iterator().next().getProperties().isAccessTierInferred()

        then:
        inferred1
        inferredList1
        !inferred2
        inferredList2 == null
    }

    @Unroll
    def "Set tier archive status"() {
        setup:
        def cc = primaryBlobServiceClient.createBlobContainer(generateBlobName())
        def bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        bc.upload(data.defaultInputStream, data.defaultDataSize)

        when:
        bc.setAccessTier(sourceTier)
        bc.setAccessTier(destTier)

        then:
        bc.getProperties().getArchiveStatus() == status
        cc.listBlobs().iterator().next().getProperties().getArchiveStatus() == status

        where:
        sourceTier         | destTier        || status
        AccessTier.ARCHIVE | AccessTier.COOL || ArchiveStatus.REHYDRATE_PENDING_TO_COOL
        AccessTier.ARCHIVE | AccessTier.HOT  || ArchiveStatus.REHYDRATE_PENDING_TO_HOT
        AccessTier.ARCHIVE | AccessTier.HOT  || ArchiveStatus.REHYDRATE_PENDING_TO_HOT
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "Set tier rehydrate priority"() {
        setup:
        if (rehydratePriority != null) {
            bc.setAccessTier(AccessTier.ARCHIVE)

            bc.setAccessTierWithResponse(new BlobSetAccessTierOptions(AccessTier.HOT).setPriority(rehydratePriority), null, null)
        }

        when:
        def resp = bc.getPropertiesWithResponse(null, null, null)

        then:
        resp.getStatusCode() == 200
        resp.getValue().getRehydratePriority() == rehydratePriority

        where:
        rehydratePriority          || _
        null                       || _
        RehydratePriority.STANDARD || _
        RehydratePriority.HIGH     || _
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    def "Set tier snapshot"() {
        setup:
        def bc2 = bc.createSnapshot()

        when:
        bc2.setAccessTier(AccessTier.COOL)

        then:
        bc2.getProperties().getAccessTier() == AccessTier.COOL
        bc.getProperties().getAccessTier() != AccessTier.COOL
    }

    def "Set tier snapshot error"() {
        setup:
        bc.createSnapshotWithResponse(null, null, null, null)
        String fakeVersion = "2020-04-17T20:37:16.5129130Z"
        def bc2 = bc.getSnapshotClient(fakeVersion)

        when:
        bc2.setAccessTier(AccessTier.COOL)

        then:
        thrown(BlobStorageException)
    }

    def "Set tier error"() {
        setup:
        def cc = primaryBlobServiceClient.createBlobContainer(generateContainerName())
        def bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        bc.upload(data.defaultInputStream, data.defaultDataSize)

        when:
        bc.setAccessTier(AccessTier.fromString("garbage"))

        then:
        def e = thrown(BlobStorageException)
        e.getErrorCode() == BlobErrorCode.INVALID_HEADER_VALUE

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
        def cc = primaryBlobServiceClient.createBlobContainer(generateContainerName())
        def bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        bc.upload(data.defaultInputStream, data.defaultDataSize)
        def leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)

        when:
        bc.setAccessTierWithResponse(AccessTier.HOT, null, leaseID, null, null)

        then:
        notThrown(BlobStorageException)

        cleanup:
        cc.delete()
    }

    def "Set tier lease fail"() {
        setup:
        def cc = primaryBlobServiceClient.createBlobContainer(generateContainerName())
        def bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        bc.upload(data.defaultInputStream, data.defaultDataSize)

        when:
        bc.setAccessTierWithResponse(AccessTier.HOT, null, "garbage", null, null)

        then:
        thrown(BlobStorageException)
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    def "Set tier tags"() {
        setup:
        def cc = primaryBlobServiceClient.createBlobContainer(generateContainerName())
        def bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        bc.upload(data.defaultInputStream, data.defaultDataSize)
        def t = new HashMap<String, String>()
        t.put("foo", "bar")
        bc.setTags(t)

        when:
        bc.setAccessTierWithResponse(new BlobSetAccessTierOptions(AccessTier.HOT).setTagsConditions("\"foo\" = 'bar'"), null, null)

        then:
        notThrown(BlobStorageException)

        cleanup:
        cc.delete()
    }

    def "Set tier tags fail"() {
        setup:
        def cc = primaryBlobServiceClient.createBlobContainer(generateContainerName())
        def bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        bc.upload(data.defaultInputStream, data.defaultDataSize)

        when:
        bc.setAccessTierWithResponse(new BlobSetAccessTierOptions(AccessTier.HOT).setTagsConditions("\"foo\" = 'bar'"), null, null)

        then:
        thrown(BlobStorageException)
    }

    @Unroll
    def "Copy with tier"() {
        setup:
        def blobName = generateBlobName()
        def bc = cc.getBlobClient(blobName).getBlockBlobClient()
        bc.uploadWithResponse(data.defaultInputStream, data.defaultDataSize, null, null, tier1, null, null, null, null)
        def bcCopy = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        def sas = new BlobServiceSasSignatureValues()
            .setExpiryTime(OffsetDateTime.now().plusHours(1))
            .setPermissions(new BlobSasPermission().setReadPermission(true))
            .setContainerName(cc.getBlobContainerName())
            .setBlobName(blobName)
            .generateSasQueryParameters(env.primaryAccount.credential)
            .encode()
        bcCopy.copyFromUrlWithResponse(bc.getBlobUrl().toString() + "?" + sas, null, tier2, null, null, null, null)

        then:
        bcCopy.getProperties().getAccessTier() == tier2

        where:
        tier1           | tier2
        AccessTier.HOT  | AccessTier.COOL
        AccessTier.COOL | AccessTier.HOT
    }

    def "Undelete error"() {
        bc = cc.getBlobClient(generateBlobName())

        when:
        bc.undelete()

        then:
        thrown(BlobStorageException)
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

    def "Get Container Name"() {
        expect:
        containerName == bc.getContainerName()
    }

    def "Get Container Client"() {
        setup:
        def sasToken = cc.generateSas(
            new BlobServiceSasSignatureValues(OffsetDateTime.now().plusDays(2),
                new BlobSasPermission().setReadPermission(true)))

        // Ensure a sas token is also persisted
        cc = getContainerClient(sasToken, cc.getBlobContainerUrl())

        expect:
        // Ensure the correct endpoint
        cc.getBlobContainerUrl() == bc.getContainerClient().getBlobContainerUrl()
        // Ensure it is a functional client
        bc.getContainerClient().getProperties() != null
    }

    def "Get Blob Name"() {
        setup:
        bc = cc.getBlobClient(inputName)

        expect:
        expectedOutputName == bc.getBlobName()

        where:
        inputName                           | expectedOutputName
        "blobName"                          | "blobName" // standard names should be preserved
        Utility.urlEncode("dir1/a%20b.txt") | "dir1/a%20b.txt" // encoded names should be decoded (not double decoded
    }

    def "Get Blob Name and Build Client"() {
        when:
        BlobClient client = cc.getBlobClient(originalBlobName)
        BlobClientBase baseClient = cc.getBlobClient(client.getBlobName()).getBlockBlobClient()

        then:
        baseClient.getBlobName() == finalBlobName

        where:
        originalBlobName       | finalBlobName
        "blob"                 | "blob"
        "path/to]a blob"       | "path/to]a blob"
        "path%2Fto%5Da%20blob" | "path/to]a blob"
        "斑點"                   | "斑點"
        "%E6%96%91%E9%BB%9E"   | "斑點"
    }

    def "Builder cpk validation"() {
        setup:
        String endpoint = BlobUrlParts.parse(bc.getBlobUrl()).setScheme("http").toUrl()
        def builder = new BlobClientBuilder()
            .customerProvidedKey(new CustomerProvidedKey(Base64.getEncoder().encodeToString(getRandomByteArray(256))))
            .endpoint(endpoint)

        when:
        builder.buildClient()

        then:
        thrown(IllegalArgumentException)
    }

    def "Builder bearer token validation"() {
        setup:
        String endpoint = BlobUrlParts.parse(bc.getBlobUrl()).setScheme("http").toUrl()
        def builder = new BlobClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)

        when:
        builder.buildClient()

        then:
        thrown(IllegalArgumentException)
    }

    @IgnoreIf({ getEnv().serviceVersion != null })
    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials and auth would fail because we changed a signed header.
    def "Per call policy"() {
        bc = getBlobClient(env.primaryAccount.credential, bc.getBlobUrl(), getPerCallVersionPolicy())

        when:
        def response = bc.getPropertiesWithResponse(null, null, null)

        then:
        notThrown(BlobStorageException)
        response.getHeaders().getValue("x-ms-version") == "2017-11-09"
    }
}
