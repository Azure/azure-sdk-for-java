package com.microsoft.azure.storage

import com.microsoft.azure.storage.blob.BlobAccessConditions
import com.microsoft.azure.storage.blob.BlobHTTPHeaders
import com.microsoft.azure.storage.blob.BlockBlobURL
import com.microsoft.azure.storage.blob.HTTPAccessConditions
import com.microsoft.azure.storage.blob.LeaseAccessConditions
import com.microsoft.azure.storage.blob.Metadata
import com.microsoft.azure.storage.blob.StorageException
import com.microsoft.azure.storage.blob.models.BlobsGetPropertiesResponse
import com.microsoft.azure.storage.blob.models.BlockBlobsCommitBlockListHeaders
import com.microsoft.azure.storage.blob.models.BlockBlobsCommitBlockListResponse
import com.microsoft.azure.storage.blob.models.BlockBlobsGetBlockListResponse
import com.microsoft.azure.storage.blob.models.BlockBlobsStageBlockHeaders
import com.microsoft.azure.storage.blob.models.BlockBlobsStageBlockResponse
import com.microsoft.azure.storage.blob.models.BlockBlobsUploadHeaders
import com.microsoft.azure.storage.blob.models.BlockBlobsUploadResponse
import com.microsoft.azure.storage.blob.models.BlockListType
import com.microsoft.azure.storage.blob.models.StorageErrorCode
import com.microsoft.rest.v2.util.FlowableUtil
import io.reactivex.Flowable
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.security.MessageDigest

class BlockBlobAPITest extends APISpec {
    BlockBlobURL bu

    def setup() {
        bu = cu.createBlockBlobURL(generateBlobName())
        bu.upload(defaultFlowable, defaultDataSize, null, null,
                null).blockingGet()
    }

    def getBlockID() {
        return new String(Base64.encoder.encode(UUID.randomUUID().toString().bytes))
    }

    def "Stage block"() {
        setup:
        BlockBlobsStageBlockResponse response = bu.stageBlock(getBlockID(), defaultFlowable, defaultDataSize,
                null).blockingGet()
        BlockBlobsStageBlockHeaders headers = response.headers()

        expect:
        response.statusCode() == 201
        headers.contentMD5() != null
        headers.requestId() != null
        headers.version() != null
        headers.date() != null
        headers.isServerEncrypted()
    }

    @Unroll
    def "Stage block illegal arguments"() {
        when:
        bu.stageBlock(blockID, data, dataSize, null).blockingGet()

        then:
        thrown(IllegalArgumentException)

        where:
        blockID      | data            | dataSize
        null         | defaultFlowable | defaultDataSize
        getBlockID() | null            | defaultDataSize
        getBlockID() | defaultFlowable | defaultDataSize + 1
        getBlockID() | defaultFlowable | defaultDataSize - 1
    }

    def "Stage block empty body"() {
        when:
        bu.stageBlock(getBlockID(), Flowable.just(ByteBuffer.wrap(new byte[0])), 0, null)
                .blockingGet()

        then:
        thrown(StorageException)
    }

    def "Stage block null body"() {
        when:
        bu.stageBlock(getBlockID(), Flowable.just(null), 0, null).blockingGet()

        then:
        thrown(NullPointerException) // Thrown by Flowable.just().
    }

    def "Stage block lease"() {
        setup:
        String leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)

        expect:
        bu.stageBlock(getBlockID(), defaultFlowable, defaultDataSize, new LeaseAccessConditions(leaseID))
                .blockingGet().statusCode() == 201
    }

    def "Stage block lease fail"() {
        setup:
        setupBlobLeaseCondition(bu, receivedLeaseID)

        when:
        bu.stageBlock(getBlockID(), defaultFlowable, defaultDataSize, new LeaseAccessConditions(garbageLeaseID))
                .blockingGet()

        then:
        def e = thrown(StorageException)
        e.errorCode() == StorageErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION
    }

    def "Stage block error"() {
        setup:
        bu = cu.createBlockBlobURL(generateBlobName())

        when:
        bu.stageBlock("id", defaultFlowable, defaultDataSize, null)
                .blockingGet()

        then:
        thrown(StorageException)
    }

    def "Commit block list"() {
        setup:
        String blockID = getBlockID()
        bu.stageBlock(blockID, defaultFlowable, defaultDataSize,
                null).blockingGet()
        ArrayList<String> ids = new ArrayList<>()
        ids.add(blockID)

        when:
        BlockBlobsCommitBlockListResponse response =
                bu.commitBlockList(ids, null, null, null).blockingGet()
        BlockBlobsCommitBlockListHeaders headers = response.headers()

        then:
        response.statusCode() == 201
        validateBasicHeaders(headers)
        headers.contentMD5()
        headers.isServerEncrypted()
    }

    def "Commit block list null"() {
        expect:
        bu.commitBlockList(null, null, null, null)
                .blockingGet().statusCode() == 201
    }

    @Unroll
    def "Commit block list headers"() {
        setup:
        String blockID = getBlockID()
        bu.stageBlock(blockID, defaultFlowable, defaultDataSize,
                null).blockingGet()
        ArrayList<String> ids = new ArrayList<>()
        ids.add(blockID)
        BlobHTTPHeaders headers = new BlobHTTPHeaders(cacheControl, contentDisposition, contentEncoding,
                contentLanguage, contentMD5, contentType)

        when:
        bu.commitBlockList(ids, headers, null, null).blockingGet()
        BlobsGetPropertiesResponse response = bu.getProperties(null).blockingGet()

        then:
        response.statusCode() == 200
        validateBlobHeaders(response.headers(), cacheControl, contentDisposition, contentEncoding, contentLanguage,
                contentMD5, contentType == null ? "application/octet-stream" : contentType)
        // HTTP default content type is application/octet-stream

        where:
        cacheControl | contentDisposition | contentEncoding | contentLanguage | contentMD5                                                   | contentType
        null         | null               | null            | null            | null                                                         | null
        "control"    | "disposition"      | "encoding"      | "language"      | MessageDigest.getInstance("MD5").digest(defaultData.array()) | "type"
    }

    @Unroll
    def "Commit block list metadata"() {
        setup:
        Metadata metadata = new Metadata()
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }

        when:
        bu.commitBlockList(null, null, metadata, null).blockingGet()
        BlobsGetPropertiesResponse response = bu.getProperties(null).blockingGet()

        then:
        response.statusCode() == 200
        response.headers().metadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    def "Commit block list metadata fail"() {
        setup:
        Metadata metadata = new Metadata()
        metadata.put("!nvalid", "value")

        when:
        bu.commitBlockList(null, null, metadata, null).blockingGet()

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    def "Commit block list AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null, null)

        expect:
        bu.commitBlockList(null, null, null, bac).blockingGet().statusCode() == 201

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
    def "Commit block list AC fail"() {
        setup:
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null, null)

        when:
        bu.commitBlockList(null, null, null, bac).blockingGet()

        then:
        def e = thrown(StorageException)
        e.errorCode() == StorageErrorCode.CONDITION_NOT_MET ||
                e.errorCode() == StorageErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | garbageLeaseID
    }

    def "Commit block list error"() {
        setup:
        bu = cu.createBlockBlobURL(generateBlobName())

        when:
        bu.commitBlockList(new ArrayList<String>(), null, null, new BlobAccessConditions(
                null, new LeaseAccessConditions("garbage"), null,
                null)).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Get block list"() {
        setup:
        String blockID = getBlockID()
        bu.stageBlock(blockID, defaultFlowable, defaultDataSize,
                null).blockingGet()

        when:
        BlockBlobsGetBlockListResponse response = bu.getBlockList(BlockListType.ALL, null)
                .blockingGet()

        then:
        response.body().uncommittedBlocks().get(0).name() == blockID
        validateBasicHeaders(response.headers())
        response.headers().contentType() != null
        response.headers().blobContentLength() == (long) defaultDataSize
    }

    @Unroll
    def "Get block list type"() {
        setup:
        String blockID = getBlockID()
        bu.stageBlock(blockID, defaultFlowable, defaultDataSize,
                null).blockingGet()
        ArrayList<String> ids = new ArrayList<>()
        ids.add(blockID)
        bu.commitBlockList(ids, null, null, null).blockingGet()
        blockID = new String(getBlockID())
        bu.stageBlock(blockID, defaultFlowable, defaultDataSize,
                null).blockingGet()

        when:
        BlockBlobsGetBlockListResponse response = bu.getBlockList(type, null).blockingGet()

        then:
        response.body().committedBlocks().size() == committedCount
        response.body().uncommittedBlocks().size() == uncommittedCount

        where:
        type                      | committedCount | uncommittedCount
        BlockListType.ALL         | 1              | 1
        BlockListType.COMMITTED   | 1              | 0
        BlockListType.UNCOMMITTED | 0              | 1
    }

    def "Get block list type null"() {
        when:
        bu.getBlockList(null, null).blockingGet()

        then:
        thrown(IllegalArgumentException)
    }

    def "Get block list lease"() {
        setup:
        String leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)

        expect:
        bu.getBlockList(BlockListType.ALL, new LeaseAccessConditions(leaseID)).blockingGet().statusCode() == 200
    }

    def "Get block list lease fail"() {
        setup:
        setupBlobLeaseCondition(bu, garbageLeaseID)

        when:
        bu.getBlockList(BlockListType.ALL, new LeaseAccessConditions(garbageLeaseID)).blockingGet()

        then:
        def e = thrown(StorageException)
        e.errorCode() == StorageErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION
    }

    def "Get block list error"() {
        setup:
        bu = cu.createBlockBlobURL(generateBlobName())

        when:
        bu.getBlockList(BlockListType.ALL, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Upload"() {
        when:
        BlockBlobsUploadResponse response = bu.upload(defaultFlowable, defaultDataSize,
                null, null, null).blockingGet()
        BlockBlobsUploadHeaders headers = response.headers()

        then:
        response.statusCode() == 201
        FlowableUtil.collectBytesInBuffer(
                bu.download(null, null, false).blockingGet().body())
                .blockingGet() == defaultData
        validateBasicHeaders(headers)
        headers.contentMD5() != null
        headers.isServerEncrypted()
    }

    @Unroll
    def "Upload illegal argument"() {
        when:
        bu.upload(data, dataSize, null, null, null).blockingGet()

        then:
        thrown(IllegalArgumentException)

        where:
        data            | dataSize
        null            | defaultDataSize
        defaultFlowable | defaultDataSize + 1
        defaultFlowable | defaultDataSize - 1
    }

    def "Upload empty body"() {
        expect:
        bu.upload(Flowable.just(ByteBuffer.wrap(new byte[0])), 0, null, null,
                null).blockingGet().statusCode() == 201
    }

    def "Upload null body"() {
        when:
        bu.upload(Flowable.just(null), 0, null, null, null).blockingGet()

        then:
        thrown(NullPointerException) // Thrown by Flowable.just().
    }

    @Unroll
    def "Upload headers"() {
        setup:
        BlobHTTPHeaders headers = new BlobHTTPHeaders(cacheControl, contentDisposition, contentEncoding,
                contentLanguage, contentMD5, contentType)

        when:
        bu.upload(defaultFlowable, defaultDataSize,
                headers, null, null).blockingGet()
        BlobsGetPropertiesResponse response = bu.getProperties(null).blockingGet()

        then:
        validateBlobHeaders(response.headers(), cacheControl, contentDisposition, contentEncoding, contentLanguage,
                MessageDigest.getInstance("MD5").digest(defaultData.array()),
                contentType == null ? "application/octet-stream" : contentType)
        // For uploading a block blob, the service will auto calculate an MD5 hash if not present
        // HTTP default content type is application/octet-stream

        where:
        cacheControl | contentDisposition | contentEncoding | contentLanguage | contentMD5                                                   | contentType
        null         | null               | null            | null            | null                                                         | null
        "control"    | "disposition"      | "encoding"      | "language"      | MessageDigest.getInstance("MD5").digest(defaultData.array()) | "type"
    }

    @Unroll
    def "Upload metadata"() {
        setup:
        Metadata metadata = new Metadata()
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }

        when:
        bu.upload(defaultFlowable, defaultDataSize,
                null, metadata, null).blockingGet()
        BlobsGetPropertiesResponse response = bu.getProperties(null).blockingGet()

        then:
        response.statusCode() == 200
        response.headers().metadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    def "Upload metadata fail"() {
        setup:
        Metadata metadata = new Metadata()
        metadata.put("!nvalid", "value")

        when:
        bu.upload(defaultFlowable, defaultDataSize, null, metadata, null).blockingGet()

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    def "Upload AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null, null)

        expect:
        bu.upload(defaultFlowable, defaultDataSize,
                null, null, bac).blockingGet().statusCode() == 201

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
    def "Upload AC fail"() {
        setup:
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null, null)

        when:
        bu.upload(defaultFlowable, defaultDataSize, null, null, bac).blockingGet()

        then:
        def e = thrown(StorageException)
        e.errorCode() == StorageErrorCode.CONDITION_NOT_MET ||
                e.errorCode() == StorageErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | garbageLeaseID
    }

    def "Upload error"() {
        setup:
        bu = cu.createBlockBlobURL(generateBlobName())

        when:
        bu.upload(defaultFlowable, defaultDataSize, null, null,
                new BlobAccessConditions(null, new LeaseAccessConditions("id"),
                        null, null)).blockingGet()

        then:
        thrown(StorageException)
    }
}
