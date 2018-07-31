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
import com.microsoft.azure.storage.blob.BlockBlobURL
import com.microsoft.azure.storage.blob.HTTPAccessConditions
import com.microsoft.azure.storage.blob.LeaseAccessConditions
import com.microsoft.azure.storage.blob.Metadata
import com.microsoft.azure.storage.blob.StorageException
import com.microsoft.azure.storage.blob.models.BlobGetPropertiesResponse
import com.microsoft.azure.storage.blob.models.BlockBlobCommitBlockListHeaders
import com.microsoft.azure.storage.blob.models.BlockBlobCommitBlockListResponse
import com.microsoft.azure.storage.blob.models.BlockBlobGetBlockListResponse
import com.microsoft.azure.storage.blob.models.BlockBlobStageBlockHeaders
import com.microsoft.azure.storage.blob.models.BlockBlobStageBlockResponse
import com.microsoft.azure.storage.blob.models.BlockBlobUploadHeaders
import com.microsoft.azure.storage.blob.models.BlockBlobUploadResponse
import com.microsoft.azure.storage.blob.models.BlockListType
import com.microsoft.azure.storage.blob.models.PublicAccessType
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
        BlockBlobStageBlockResponse response = bu.stageBlock(getBlockID(), defaultFlowable, defaultDataSize,
                null).blockingGet()
        BlockBlobStageBlockHeaders headers = response.headers()

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

    def "Stage block from url"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null, null).blockingGet()
        def bu2 = cu.createBlockBlobURL(generateBlobName())
        def blockID = getBlockID()

        when:
        def response = bu2.stageBlockFromURL(blockID, bu.toURL(), null, null,
                null).blockingGet()
        def listResponse = bu2.getBlockList(BlockListType.ALL, null).blockingGet()
        bu2.commitBlockList(Arrays.asList(blockID), null, null, null).blockingGet()

        then:
        response.headers().requestId() != null
        response.headers().version() != null
        response.headers().requestId() != null
        response.headers().contentMD5() != null
        response.headers().isServerEncrypted() != null

        listResponse.body().uncommittedBlocks().get(0).name() == blockID
        listResponse.body().uncommittedBlocks().size() == 1

        FlowableUtil.collectBytesInBuffer(bu2.download(null, null, false)
                .blockingGet().body()).blockingGet() == defaultData
    }

    @Unroll
    def "Stage block from URL IA"() {
        when:
        bu.stageBlockFromURL(blockID, sourceURL, null, null, null)
                .blockingGet()

        then:
        thrown(IllegalArgumentException)

        where:
        blockID | sourceURL
        null | new URL("http://www.example.com")
        getBlockID() | null
    }

    def "Stage block from URL range"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null, null).blockingGet()
        def destURL = cu.createBlockBlobURL(generateBlobName())

        when:
        destURL.stageBlockFromURL(getBlockID(), bu.toURL(), new BlobRange(2, 3), null,
                null).blockingGet()

        then:
        destURL.getBlockList(BlockListType.ALL, null).blockingGet().body().uncommittedBlocks().get(0)
                .size() == 3
    }

    def "Stage block from URL MD5"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null, null).blockingGet()
        def destURL = cu.createBlockBlobURL(generateBlobName())

        when:
        destURL.stageBlockFromURL(getBlockID(), bu.toURL(), null,
                MessageDigest.getInstance("MD5").digest(defaultData.array()),null)
                .blockingGet()

        then:
        notThrown(StorageException)
    }

    def "Stage block from URL MD5 fail"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null, null).blockingGet()
        def destURL = cu.createBlockBlobURL(generateBlobName())

        when:
        destURL.stageBlockFromURL(getBlockID(), bu.toURL(), null, "garbage".getBytes(),
                null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Stage block from URL lease"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null, null).blockingGet()
        def lease = new LeaseAccessConditions(setupBlobLeaseCondition(bu, receivedLeaseID))

        when:
        bu.stageBlockFromURL(getBlockID(), bu.toURL(), null, null, lease).blockingGet()

        then:
        notThrown(StorageException)
    }

    def "Stage block from URL lease fail"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null, null).blockingGet()
        def lease = new LeaseAccessConditions("garbage")

        when:
        bu.stageBlockFromURL(getBlockID(), bu.toURL(), null, null, lease).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Stage block from URL error"() {
        setup:
        cu = primaryServiceURL.createContainerURL(generateContainerName())
        bu = cu.createBlockBlobURL(generateBlobName())

        when:
        bu.stageBlockFromURL(getBlockID(), bu.toURL(), null, null, null)
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
        BlockBlobCommitBlockListResponse response =
                bu.commitBlockList(ids, null, null, null).blockingGet()
        BlockBlobCommitBlockListHeaders headers = response.headers()

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
        BlobGetPropertiesResponse response = bu.getProperties(null).blockingGet()

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
        bu.stageBlock(blockID, defaultFlowable, defaultDataSize,null).blockingGet()
        bu.commitBlockList(Arrays.asList(blockID), null, null, null).blockingGet()
        String blockID2 = getBlockID()
        bu.stageBlock(blockID2, defaultFlowable, defaultDataSize, null).blockingGet()

        when:
        BlockBlobGetBlockListResponse response = bu.getBlockList(BlockListType.ALL, null)
                .blockingGet()

        then:
        response.body().committedBlocks().get(0).name() == blockID
        response.body().committedBlocks().get(0).size() == defaultDataSize
        response.body().uncommittedBlocks().get(0).name() == blockID2
        response.body().uncommittedBlocks().get(0).size() == defaultDataSize
        validateBasicHeaders(response.headers())
        response.headers().contentType() != null
        response.headers().blobContentLength() == (long) defaultDataSize
    }

    // TODO: at least two blocks per list

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
        BlockBlobGetBlockListResponse response = bu.getBlockList(type, null).blockingGet()

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
        BlockBlobUploadResponse response = bu.upload(defaultFlowable, defaultDataSize,
                null, null, null).blockingGet()
        BlockBlobUploadHeaders headers = response.headers()

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
        BlobGetPropertiesResponse response = bu.getProperties(null).blockingGet()

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
