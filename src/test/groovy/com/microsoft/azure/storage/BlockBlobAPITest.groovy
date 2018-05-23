package com.microsoft.azure.storage

import com.microsoft.azure.storage.blob.BlobAccessConditions
import com.microsoft.azure.storage.blob.BlobHTTPHeaders
import com.microsoft.azure.storage.blob.BlockBlobURL
import com.microsoft.azure.storage.blob.HTTPAccessConditions
import com.microsoft.azure.storage.blob.LeaseAccessConditions
import com.microsoft.azure.storage.blob.Metadata
import com.microsoft.azure.storage.blob.models.BlobsGetPropertiesResponse
import com.microsoft.azure.storage.blob.models.BlockBlobsCommitBlockListHeaders
import com.microsoft.azure.storage.blob.models.BlockBlobsCommitBlockListResponse
import com.microsoft.azure.storage.blob.models.BlockBlobsGetBlockListResponse
import com.microsoft.azure.storage.blob.models.BlockBlobsStageBlockHeaders
import com.microsoft.azure.storage.blob.models.BlockBlobsStageBlockResponse
import com.microsoft.azure.storage.blob.models.BlockBlobsUploadHeaders
import com.microsoft.azure.storage.blob.models.BlockBlobsUploadResponse
import com.microsoft.azure.storage.blob.models.BlockListType
import com.microsoft.rest.v2.util.FlowableUtil
import io.reactivex.Flowable
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.security.MessageDigest

class BlockBlobAPITest extends APISpec {
    BlockBlobURL bu

    def setup() {
        bu = cu.createBlockBlobURL(generateBlobName())
        bu.upload(Flowable.just(defaultData), defaultText.length(), null, null,
                null).blockingGet()
    }

    def "Block blob stage block"() {
        setup:
        BlockBlobsStageBlockResponse response = bu.stageBlock(new String(Base64.encoder.encode("0000".bytes)),
                Flowable.just(defaultData), defaultData.remaining(), null)
                .blockingGet()
        BlockBlobsStageBlockHeaders headers = response.headers()

        expect:
        response.statusCode() == 201
        headers.contentMD5() != null
        headers.requestId() != null
        headers.version() != null
        headers.dateProperty() != null
        headers.isServerEncrypted()
    }

    def "Block blob stage block lease"() {
        setup:
        String leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)

        expect:
        BlockBlobsStageBlockResponse response = bu.stageBlock(new String(Base64.encoder.encode("0000".bytes)),
                Flowable.just(defaultData), defaultData.remaining(),
                new LeaseAccessConditions(leaseID)).blockingGet()
    }

    def "Block blob commit block list"() {
        setup:
        String blockID = new String(Base64.encoder.encode("0000".bytes))
        bu.stageBlock(blockID, Flowable.just(defaultData), defaultData.remaining(),
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

    @Unroll
    def "Block blob commit block list headers"() {
        setup:
        String blockID = new String(Base64.encoder.encode("0000".bytes))
        bu.stageBlock(blockID, Flowable.just(defaultData), defaultData.remaining(),
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
        response.headers().cacheControl() == cacheControl
        response.headers().contentDisposition() == contentDisposition
        response.headers().contentEncoding() == contentEncoding
        response.headers().contentMD5() == contentMD5
        // HTTP default content type is application/octet-stream
        contentType == null ? response.headers().contentType() == "application/octet-stream" :
                response.headers().contentType() == contentType

        where:
        cacheControl | contentDisposition | contentEncoding | contentLanguage | contentMD5                                                   | contentType
        null         | null               | null            | null            | null                                                         | null
        "control"    | "disposition"      | "encoding"      | "language"      | MessageDigest.getInstance("MD5").digest(defaultData.array()) | "type"
    }

    @Unroll
    def "Block blob commit block list metadata"() {
        setup:
        Metadata metadata = new Metadata()
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }
        String blockID = new String(Base64.encoder.encode("0000".bytes))
        bu.stageBlock(blockID, Flowable.just(defaultData), defaultData.remaining(),
                null).blockingGet()
        ArrayList<String> ids = new ArrayList<>()
        ids.add(blockID)

        when:
        bu.commitBlockList(ids, null, metadata, null).blockingGet()
        BlobsGetPropertiesResponse response = bu.getProperties(null).blockingGet()

        then:
        response.statusCode() == 200
        response.headers().metadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @Unroll
    def "Block blob commit block list AC"() {
        setup:
        String blockID = new String(Base64.encoder.encode("0000".bytes))
        bu.stageBlock(blockID, Flowable.just(defaultData), defaultData.remaining(),
                null).blockingGet()
        ArrayList<String> ids = new ArrayList<>()
        ids.add(blockID)
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null, null)

        expect:
        bu.commitBlockList(ids, null, null, bac).blockingGet().statusCode() == 201

        where:
        modified | unmodified | match        | noneMatch   | leaseID
        null     | null       | null         | null        | null
        oldDate  | null       | null         | null        | null
        null     | newDate    | null         | null        | null
        null     | null       | receivedEtag | null        | null
        null     | null       | null         | garbageEtag | null
        null     | null       | null         | null        | receivedLeaseID
    }

    def "Block blob get block list"() {
        setup:
        String blockID = new String(Base64.encoder.encode("0000".bytes))
        bu.stageBlock(blockID, Flowable.just(defaultData), defaultData.remaining(),
                null).blockingGet()

        when:
        BlockBlobsGetBlockListResponse response = bu.getBlockList(BlockListType.ALL, null)
                .blockingGet()

        then:
        response.body().uncommittedBlocks().get(0).name().equals(blockID)
        validateBasicHeaders(response.headers())
        response.headers().contentType() != null
        response.headers().blobContentLength() == (long) defaultText.length()
    }

    @Unroll
    def "Block blob get block list type"() {
        setup:
        String blockID = new String(Base64.encoder.encode("0000".bytes))
        bu.stageBlock(blockID, Flowable.just(defaultData), defaultData.remaining(),
                null).blockingGet()
        ArrayList<String> ids = new ArrayList<>()
        ids.add(blockID)
        bu.commitBlockList(ids, null, null, null).blockingGet()
        blockID = new String(Base64.encoder.encode("0001".bytes))
        bu.stageBlock(blockID, Flowable.just(defaultData), defaultText.length(),
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

    def "Block blob get block list lease"() {
        setup:
        String blockID = new String(Base64.encoder.encode("0000".bytes))
        bu.stageBlock(blockID, Flowable.just(defaultData), defaultData.remaining(),
                null).blockingGet()
        String leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)

        expect:
        bu.getBlockList(BlockListType.ALL, new LeaseAccessConditions(leaseID)).blockingGet().statusCode() == 200
    }

    def "Block blob upload"() {
        when:
        BlockBlobsUploadResponse response = bu.upload(Flowable.just(defaultData), defaultData.remaining(),
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
    def "Block blob upload headers"() {
        setup:
        BlobHTTPHeaders headers = new BlobHTTPHeaders(cacheControl, contentDisposition, contentEncoding,
                contentLanguage, contentMD5, contentType)

        when:
        bu.upload(Flowable.just(defaultData), defaultData.remaining(),
                headers, null, null).blockingGet()
        BlobsGetPropertiesResponse response = bu.getProperties(null).blockingGet()

        then:
        response.headers().cacheControl() == cacheControl
        response.headers().contentDisposition() == contentDisposition
        response.headers().contentEncoding() == contentEncoding
        // For uploading a block blob, the service will auto calculate an MD5 hash if not present
        response.headers().contentMD5() == MessageDigest.getInstance("MD5").digest(defaultData.array())
        // HTTP default content type is application/octet-stream
        contentType == null ? response.headers().contentType() == "application/octet-stream" :
                response.headers().contentType() == contentType

        where:
        cacheControl | contentDisposition | contentEncoding | contentLanguage | contentMD5                                                   | contentType
        null         | null               | null            | null            | null                                                         | null
        "control"    | "disposition"      | "encoding"      | "language"      | MessageDigest.getInstance("MD5").digest(defaultData.array()) | "type"
    }

    @Unroll
    def "Block blob upload metadata"() {
        setup:
        Metadata metadata = new Metadata()
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }

        when:
        bu.upload(Flowable.just(defaultData), defaultData.remaining(),
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

    @Unroll
    def "Block blob upload AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null, null)

        expect:
        bu.upload(Flowable.just(defaultData), defaultData.remaining(),
                null, null, bac).blockingGet()


        where:
        modified | unmodified | match        | noneMatch   | leaseID
        null     | null       | null         | null        | null
        oldDate  | null       | null         | null        | null
        null     | newDate    | null         | null        | null
        null     | null       | receivedEtag | null        | null
        null     | null       | null         | garbageEtag | null
        null     | null       | null         | null        | receivedLeaseID
    }
}
