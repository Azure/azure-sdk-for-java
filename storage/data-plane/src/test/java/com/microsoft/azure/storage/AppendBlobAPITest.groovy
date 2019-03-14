// Copyright (c) Microsoft Corporation. All rights reserved. 
// Licensed under the MIT License.

package com.microsoft.azure.storage

import com.microsoft.azure.storage.blob.*
import com.microsoft.azure.storage.blob.models.*
import com.microsoft.rest.v2.http.HttpPipeline
import com.microsoft.rest.v2.http.UnexpectedLengthException
import com.microsoft.rest.v2.util.FlowableUtil
import io.reactivex.Flowable
import org.junit.Assume
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.security.MessageDigest

class AppendBlobAPITest extends APISpec {
    AppendBlobURL bu

    def setup() {
        bu = cu.createAppendBlobURL(generateBlobName())
        bu.create(null, null, null, null).blockingGet()
    }

    def "Create defaults"() {
        when:
        AppendBlobCreateResponse createResponse =
                bu.create(null, null, null, null).blockingGet()

        then:
        createResponse.statusCode() == 201
        validateBasicHeaders(createResponse.headers())
        createResponse.headers().contentMD5() == null
        createResponse.headers().isServerEncrypted()
    }

    def "Create min"() {
        expect:
        bu.create().blockingGet().statusCode() == 201
    }

    def "Create error"() {
        when:
        bu.create(null, null, new BlobAccessConditions().withModifiedAccessConditions(new ModifiedAccessConditions()
                .withIfMatch("garbage")), null).blockingGet()

        then:
        thrown(StorageException)
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
        bu.create(headers, null, null, null).blockingGet()
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
        bu.create(null, metadata, null, null).blockingGet()
        BlobGetPropertiesResponse response = bu.getProperties(null, null).blockingGet()

        then:
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
        bu.create(null, null, bac, null).blockingGet().statusCode() == 201

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
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))

        when:
        bu.create(null, null, bac, null).blockingGet()

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

    def "Create context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(201, AppendBlobCreateHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        bu.create(null, null, null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    def "Append block defaults"() {
        setup:
        AppendBlobAppendBlockHeaders headers =
                bu.appendBlock(defaultFlowable, defaultDataSize,
                        null, null).blockingGet().headers()

        expect:
        FlowableUtil.collectBytesInBuffer(bu.download(null, null, false, null)
                .blockingGet().body(null)).blockingGet().compareTo(defaultData) == 0
        validateBasicHeaders(headers)
        headers.contentMD5() != null
        headers.blobAppendOffset() != null
        headers.blobCommittedBlockCount() != null
        bu.getProperties(null, null).blockingGet().headers().blobCommittedBlockCount() == 1
    }

    def "Append block min"() {
        expect:
        bu.appendBlock(defaultFlowable, defaultDataSize).blockingGet().statusCode() == 201
    }

    @Unroll
    def "Append block IA"() {
        when:
        bu.appendBlock(data, dataSize, null, null).blockingGet()

        then:
        def e = thrown(Exception)
        exceptionType.isInstance(e)

        where:
        data            | dataSize            | exceptionType
        null            | defaultDataSize     | IllegalArgumentException
        defaultFlowable | defaultDataSize + 1 | UnexpectedLengthException
        defaultFlowable | defaultDataSize - 1 | UnexpectedLengthException
    }

    def "Append block empty body"() {
        when:
        bu.appendBlock(Flowable.just(ByteBuffer.wrap(new byte[0])), 0, null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Append block null body"() {
        when:
        bu.appendBlock(Flowable.just(null), 0, null, null).blockingGet()

        then:
        thrown(NullPointerException) // Thrown by Flowable.
    }

    @Unroll
    def "Append block AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        AppendBlobAccessConditions bac = new AppendBlobAccessConditions()
                .withModifiedAccessConditions(new ModifiedAccessConditions().withIfModifiedSince(modified)
                .withIfUnmodifiedSince(unmodified).withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))
                .withAppendPositionAccessConditions(new AppendPositionAccessConditions()
                .withAppendPosition(appendPosE).withMaxSize(maxSizeLTE))

        expect:
        bu.appendBlock(defaultFlowable, defaultDataSize, bac, null)
                .blockingGet().statusCode() == 201

        where:
        modified | unmodified | match        | noneMatch   | leaseID         | appendPosE | maxSizeLTE
        null     | null       | null         | null        | null            | null       | null
        oldDate  | null       | null         | null        | null            | null       | null
        null     | newDate    | null         | null        | null            | null       | null
        null     | null       | receivedEtag | null        | null            | null       | null
        null     | null       | null         | garbageEtag | null            | null       | null
        null     | null       | null         | null        | receivedLeaseID | null       | null
        null     | null       | null         | null        | null            | 0          | null
        null     | null       | null         | null        | null            | null       | 100
    }

    @Unroll
    def "Append block AC fail"() {
        setup:
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        setupBlobLeaseCondition(bu, leaseID)

        AppendBlobAccessConditions bac = new AppendBlobAccessConditions()
                .withModifiedAccessConditions(new ModifiedAccessConditions().withIfModifiedSince(modified)
                .withIfUnmodifiedSince(unmodified).withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))
                .withAppendPositionAccessConditions(new AppendPositionAccessConditions()
                .withAppendPosition(appendPosE).withMaxSize(maxSizeLTE))

        when:
        bu.appendBlock(defaultFlowable, defaultDataSize, bac, null)
                .blockingGet().statusCode()

        then:
        thrown(StorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID        | appendPosE | maxSizeLTE
        newDate  | null       | null        | null         | null           | null       | null
        null     | oldDate    | null        | null         | null           | null       | null
        null     | null       | garbageEtag | null         | null           | null       | null
        null     | null       | null        | receivedEtag | null           | null       | null
        null     | null       | null        | null         | garbageLeaseID | null       | null
        null     | null       | null        | null         | null           | 1          | null
        null     | null       | null        | null         | null           | null       | 1
    }

    def "Append block error"() {
        setup:
        bu = cu.createAppendBlobURL(generateBlobName())

        when:
        bu.appendBlock(defaultFlowable, defaultDataSize, null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Append block context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(201, AppendBlobAppendBlockHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        bu.appendBlock(defaultFlowable, defaultDataSize, null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }


    // Grouping the below tests from PageBlobAPITest and BlockBlobAPITest together to prevent Concurrent modification exception.
    @Unroll
    def "Stage block illegal arguments"() {
        setup:
        BlockBlobURL bu = cu.createBlockBlobURL(generateBlobName())
        bu.upload(defaultFlowable, defaultDataSize, null, null,
                null, null).blockingGet()

        when:
        bu.stageBlock(blockID, data, dataSize, null, null).blockingGet()

        then:
        def e = thrown(Exception)
        exceptionType.isInstance(e)

        where:
        blockID      | data            | dataSize            | exceptionType
        null         | defaultFlowable | defaultDataSize     | IllegalArgumentException
        getBlockID() | null            | defaultDataSize     | IllegalArgumentException
        getBlockID() | defaultFlowable | defaultDataSize + 1 | UnexpectedLengthException
        getBlockID() | defaultFlowable | defaultDataSize - 1 | UnexpectedLengthException
    }


    @Unroll
    def "Upload page IA"() {
        setup:
        PageBlobURL bu = cu.createPageBlobURL(generateBlobName())
        bu.create(PageBlobURL.PAGE_BYTES, null, null, null, null, null).blockingGet()

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
    def "Upload illegal argument"() {
        BlockBlobURL bu = cu.createBlockBlobURL(generateBlobName())
        bu.upload(defaultFlowable, defaultDataSize, null, null,
                null, null).blockingGet()

        when:
        bu.upload(data, dataSize, null, null, null, null).blockingGet()

        then:
        def e = thrown(Exception)
        exceptionType.isInstance(e)

        where:
        data            | dataSize            | exceptionType
        null            | defaultDataSize     | IllegalArgumentException
        defaultFlowable | defaultDataSize + 1 | UnexpectedLengthException
        defaultFlowable | defaultDataSize - 1 | UnexpectedLengthException
    }

    def getBlockID() {
        return new String(Base64.encoder.encode(UUID.randomUUID().toString().bytes))
    }

    //Grouping the below tests together to prevent GroovyCastException.
    @Unroll
    def "Upload file headers"() {
        setup:
        BlockBlobURL bu = cu.createBlockBlobURL(generateBlobName())
        // We have to use the defaultData here so we can calculate the MD5 on the uploadBlob case.
        File file = File.createTempFile("testUpload", ".txt")
        file.deleteOnExit()
        if (fileSize == "small") {
            FileOutputStream fos = new FileOutputStream(file)
            fos.write(defaultData.array())
            fos.close()
        } else {
            file = getRandomFile(BlockBlobURL.MAX_UPLOAD_BLOB_BYTES + 10)
        }

        def channel = AsynchronousFileChannel.open(file.toPath())

        when:
        TransferManager.uploadFileToBlockBlob(channel, bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES,
            new TransferManagerUploadToBlockBlobOptions(null, new BlobHTTPHeaders()
                .withBlobCacheControl(cacheControl).withBlobContentDisposition(contentDisposition)
                .withBlobContentEncoding(contentEncoding).withBlobContentLanguage(contentLanguage)
                .withBlobContentMD5(contentMD5).withBlobContentType(contentType), null, null, null))
            .blockingGet()

        def response = bu.getProperties(null, null).blockingGet()

        then:
        validateBlobHeaders(response.headers(), cacheControl, contentDisposition, contentEncoding, contentLanguage,
            fileSize == "small" ? MessageDigest.getInstance("MD5").digest(defaultData.array()) : contentMD5,
            contentType == null ? "application/octet-stream" : contentType)
        // For uploading a block blob single-shot, the service will auto calculate an MD5 hash if not present.
        // HTTP default content type is application/octet-stream.

        cleanup:
        channel.close()

        where:
        // The MD5 is simply set on the blob for commitBlockList, not validated.
        fileSize | cacheControl | contentDisposition | contentEncoding | contentLanguage | contentMD5                                                   | contentType
        "small"  | null         | null               | null            | null            | null                                                         | null
        "small"  | "control"    | "disposition"      | "encoding"      | "language"      | MessageDigest.getInstance("MD5").digest(defaultData.array()) | "type"
        "large"  | null         | null               | null            | null            | null                                                         | null
        "large"  | "control"    | "disposition"      | "encoding"      | "language"      | MessageDigest.getInstance("MD5").digest(defaultData.array()) | "type"
    }

    @Unroll
    def "Upload file metadata"() {
        setup:
        BlockBlobURL bu = cu.createBlockBlobURL(generateBlobName())
        Metadata metadata = new Metadata()
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }
        def channel = AsynchronousFileChannel.open(getRandomFile(dataSize).toPath())

        when:
        TransferManager.uploadFileToBlockBlob(channel, bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES,
            new TransferManagerUploadToBlockBlobOptions(null, null, metadata, null, null)).blockingGet()
        BlobGetPropertiesResponse response = bu.getProperties(null, null).blockingGet()

        then:
        response.statusCode() == 200
        response.headers().metadata() == metadata

        cleanup:
        channel.close()

        where:
        dataSize                                | key1  | value1 | key2   | value2
        10                                      | null  | null   | null   | null
        10                                      | "foo" | "bar"  | "fizz" | "buzz"
        BlockBlobURL.MAX_UPLOAD_BLOB_BYTES + 10 | null  | null   | null   | null
        BlockBlobURL.MAX_UPLOAD_BLOB_BYTES + 10 | "foo" | "bar"  | "fizz" | "buzz"
    }

    def "Undelete"() {
        setup:
        BlobURL bu = cu.createBlockBlobURL(generateBlobName())
        bu.upload(defaultFlowable, defaultDataSize, null, null,
            null, null).blockingGet()
        enableSoftDelete()
        bu.delete(null, null, null).blockingGet()
        when:
        BlobUndeleteResponse response = bu.undelete(null).blockingGet()
        bu.getProperties(null, null).blockingGet()

        then:
        notThrown(StorageException)
        response.headers().requestId() != null
        response.headers().version() != null
        response.headers().date() != null

        disableSoftDelete() == null
    }

}
