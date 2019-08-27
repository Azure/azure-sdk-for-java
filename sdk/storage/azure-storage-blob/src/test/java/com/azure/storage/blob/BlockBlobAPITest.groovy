// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


package com.azure.storage.blob

import com.azure.core.http.HttpHeaders
import com.azure.storage.blob.models.BlobAccessConditions
import com.azure.storage.blob.models.BlobHTTPHeaders
import com.azure.storage.blob.models.BlobRange
import com.azure.storage.blob.models.BlockItem
import com.azure.storage.blob.models.BlockListType
import com.azure.storage.blob.models.LeaseAccessConditions
import com.azure.storage.blob.models.Metadata
import com.azure.storage.blob.models.ModifiedAccessConditions
import com.azure.storage.blob.models.PublicAccessType
import com.azure.storage.blob.models.SourceModifiedAccessConditions
import com.azure.storage.blob.models.StorageErrorCode
import com.azure.storage.blob.models.StorageErrorException
import com.azure.storage.blob.models.StorageException
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

class BlockBlobAPITest extends APISpec {
    BlockBlobClient bc

    def setup() {
        bc = cc.getBlockBlobClient(generateBlobName())
        bc.upload(defaultInputStream.get(), defaultDataSize)
    }

    def "Stage block"() {
        setup:
        def response = bc.stageBlockWithResponse(getBlockID(), defaultInputStream.get(), defaultDataSize, null, null, null)
        HttpHeaders headers = response.headers()

        expect:
        response.statusCode() == 201
        headers.value("x-ms-content-crc64") != null
        headers.value("x-ms-request-id") != null
        headers.value("x-ms-version") != null
        headers.value("Date") != null
        Boolean.parseBoolean(headers.value("x-ms-request-server-encrypted"))
    }

    def "Stage block min"() {
        expect:
        bc.stageBlockWithResponse(getBlockID(), defaultInputStream.get(), defaultDataSize, null, null, null).statusCode() == 201
    }

    @Unroll
    def "Stage block illegal arguments"() {
        when:
        String blockID = (getBlockId) ? getBlockID() : null
        bc.stageBlock(blockID, data == null ? null : data.get(), dataSize)

        then:
        def e = thrown(Exception)
        exceptionType.isInstance(e)

        where:
        getBlockId   | data                 | dataSize            | exceptionType
        false        | defaultInputStream   | defaultDataSize     | StorageException
        true         | null                 | defaultDataSize     | NullPointerException
        true         | defaultInputStream   | defaultDataSize + 1 | IndexOutOfBoundsException
        // TODO (alzimmer): This doesn't throw an error as the stream is larger than the stated size
        //true         | defaultInputStream   | defaultDataSize - 1 | IllegalArgumentException
    }

    def "Stage block empty body"() {
        when:
        bc.stageBlock(getBlockID(), new ByteArrayInputStream(new byte[0]), 0)

        then:
        thrown(StorageException)
    }

    def "Stage block null body"() {
        when:
        bc.stageBlock(getBlockID(), null, 0)

        then:
        thrown(StorageException)
    }

    def "Stage block lease"() {
        setup:
        String leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)

        expect:
        bc.stageBlockWithResponse(getBlockID(), defaultInputStream.get(), defaultDataSize, new LeaseAccessConditions().leaseId(leaseID),
            null, null).statusCode() == 201
    }

    def "Stage block lease fail"() {
        setup:
        setupBlobLeaseCondition(bc, receivedLeaseID)

        when:
        bc.stageBlockWithResponse(getBlockID(), defaultInputStream.get(), defaultDataSize, new LeaseAccessConditions()
            .leaseId(garbageLeaseID), null, null)

        then:
        def e = thrown(StorageException)
        e.errorCode() == StorageErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION
    }

    def "Stage block error"() {
        setup:
        bc = cc.getBlockBlobClient(generateBlobName())

        when:
        bc.stageBlock("id", defaultInputStream.get(), defaultDataSize)

        then:
        thrown(StorageException)
    }

    def "Stage block from url"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def bu2 = cc.getBlockBlobClient(generateBlobName())
        def blockID = getBlockID()

        when:
        def headers = bu2.stageBlockFromURLWithResponse(blockID, bc.getBlobUrl(), null, null, null, null, null, null).headers()

        then:
        headers.value("x-ms-request-id") != null
        headers.value("x-ms-version") != null
        headers.value("x-ms-content-crc64") != null
        headers.value("x-ms-request-server-encrypted") != null

        def response = bu2.listBlocks(BlockListType.ALL)
        response.uncommittedBlocks().size() == 1
        response.committedBlocks().size() == 0
        response.uncommittedBlocks().first().name() == blockID

        when:
        bu2.commitBlockList(Arrays.asList(blockID))
        def outputStream = new ByteArrayOutputStream()
        bu2.download(outputStream)

        then:
        ByteBuffer.wrap(outputStream.toByteArray()) == defaultData
    }

    def "Stage block from url min"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def bu2 = cc.getBlockBlobClient(generateBlobName())
        def blockID = getBlockID()

        expect:
        bu2.stageBlockFromURLWithResponse(blockID, bc.getBlobUrl(), null, null, null, null, null, null).statusCode() == 201
    }

    @Unroll
    def "Stage block from URL IA"() {
        when:
        String blockID = (getBlockId) ? getBlockID() : null
        bc.stageBlockFromURL(blockID, sourceURL, null)

        then:
        thrown(StorageException)

        where:
        getBlockId   | sourceURL
        false        | new URL("http://www.example.com")
        true         | null
    }

    def "Stage block from URL range"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def destURL = cc.getBlockBlobClient(generateBlobName())

        when:
        destURL.stageBlockFromURL(getBlockID(), bc.getBlobUrl(), new BlobRange(2, 3))
        Iterator<BlockItem> uncommittedBlock = destURL.listBlocks(BlockListType.UNCOMMITTED).iterator()

        then:
        uncommittedBlock.hasNext()
        uncommittedBlock.hasNext()
        uncommittedBlock.hasNext()
    }

    def "Stage block from URL MD5"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def destURL = cc.getBlockBlobClient(generateBlobName())

        when:
        destURL.stageBlockFromURLWithResponse(getBlockID(), bc.getBlobUrl(), null,
            MessageDigest.getInstance("MD5").digest(defaultData.array()), null, null, null, null)

        then:
        notThrown(StorageException)
    }

    def "Stage block from URL MD5 fail"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def destURL = cc.getBlockBlobClient(generateBlobName())

        when:
        destURL.stageBlockFromURLWithResponse(getBlockID(), bc.getBlobUrl(), null, "garbage".getBytes(),
            null, null, null, null)

        then:
        thrown(StorageException)
    }

    def "Stage block from URL lease"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def lease = new LeaseAccessConditions().leaseId(setupBlobLeaseCondition(bc, receivedLeaseID))

        when:
        bc.stageBlockFromURLWithResponse(getBlockID(), bc.getBlobUrl(), null, null, lease, null, null, null)

        then:
        notThrown(StorageException)
    }

    def "Stage block from URL lease fail"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def lease = new LeaseAccessConditions().leaseId("garbage")

        when:
        bc.stageBlockFromURLWithResponse(getBlockID(), bc.getBlobUrl(), null, null, lease, null, null, null)

        then:
        thrown(StorageException)
    }

    def "Stage block from URL error"() {
        setup:
        bc = primaryBlobServiceClient.getContainerClient(generateContainerName()).getBlockBlobClient(generateBlobName())

        when:
        bc.stageBlockFromURL(getBlockID(), bc.getBlobUrl(), null)

        then:
        thrown(StorageException)
    }

    @Unroll
    def "Stage block from URL source AC"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def blockID = getBlockID()

        def sourceURL = cc.getBlockBlobClient(generateBlobName())
        sourceURL.upload(defaultInputStream.get(), defaultDataSize)

        sourceIfMatch = setupBlobMatchCondition(sourceURL, sourceIfMatch)
        def smac = new SourceModifiedAccessConditions()
            .sourceIfModifiedSince(sourceIfModifiedSince)
            .sourceIfUnmodifiedSince(sourceIfUnmodifiedSince)
            .sourceIfMatch(sourceIfMatch)
            .sourceIfNoneMatch(sourceIfNoneMatch)

        expect:
        bc.stageBlockFromURLWithResponse(blockID, sourceURL.getBlobUrl(), null, null, null, smac, null, null).statusCode() == 201

        where:
        sourceIfModifiedSince | sourceIfUnmodifiedSince | sourceIfMatch | sourceIfNoneMatch
        null                  | null                    | null          | null
        oldDate               | null                    | null          | null
        null                  | newDate                 | null          | null
        null                  | null                    | receivedEtag  | null
        null                  | null                    | null          | garbageEtag
    }

    @Unroll
    def "Stage block from URL source AC fail"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def blockID = getBlockID()

        def sourceURL = cc.getBlockBlobClient(generateBlobName())
        sourceURL.upload(defaultInputStream.get(), defaultDataSize)

        def smac = new SourceModifiedAccessConditions()
            .sourceIfModifiedSince(sourceIfModifiedSince)
            .sourceIfUnmodifiedSince(sourceIfUnmodifiedSince)
            .sourceIfMatch(sourceIfMatch)
            .sourceIfNoneMatch(setupBlobMatchCondition(sourceURL, sourceIfNoneMatch))

        when:
        bc.stageBlockFromURLWithResponse(blockID, sourceURL.getBlobUrl(), null, null, null, smac, null, null).statusCode() == 201

        then:
        thrown(StorageException)

        where:
        sourceIfModifiedSince | sourceIfUnmodifiedSince | sourceIfMatch | sourceIfNoneMatch
        newDate               | null                    | null          | null
        null                  | oldDate                 | null          | null
        null                  | null                    | garbageEtag   | null
        null                  | null                    | null          | receivedEtag
    }

    def "Commit block list"() {
        setup:
        String blockID = getBlockID()
        bc.stageBlock(blockID, defaultInputStream.get(), defaultDataSize)
        ArrayList<String> ids = new ArrayList<>()
        ids.add(blockID)

        when:
        def response = bc.commitBlockListWithResponse(ids, null, null, null, null, null)
        def headers = response.headers()

        then:
        response.statusCode() == 201
        validateBasicHeaders(headers)
        headers.value("x-ms-content-crc64")
        Boolean.parseBoolean(headers.value("x-ms-request-server-encrypted"))
    }

    def "Commit block list min"() {
        setup:
        String blockID = getBlockID()
        bc.stageBlock(blockID, defaultInputStream.get(), defaultDataSize)
        ArrayList<String> ids = new ArrayList<>()
        ids.add(blockID)

        expect:
        bc.commitBlockList(ids) != null
    }

    def "Commit block list null"() {
        expect:
        bc.commitBlockListWithResponse(null, null, null, null, null, null).statusCode() == 201
    }

    @Unroll
    def "Commit block list headers"() {
        setup:
        String blockID = getBlockID()
        bc.stageBlock(blockID, defaultInputStream.get(), defaultDataSize)
        ArrayList<String> ids = new ArrayList<>()
        ids.add(blockID)
        BlobHTTPHeaders headers = new BlobHTTPHeaders().blobCacheControl(cacheControl)
            .blobContentDisposition(contentDisposition)
            .blobContentEncoding(contentEncoding)
            .blobContentLanguage(contentLanguage)
            .blobContentMD5(contentMD5)
            .blobContentType(contentType)

        when:
        bc.commitBlockListWithResponse(ids, headers, null, null, null, null)
        def response = bc.getPropertiesWithResponse(null, null, null)

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType

        then:
        validateBlobProperties(response, cacheControl, contentDisposition, contentEncoding, contentLanguage, contentMD5, contentType)

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
        bc.commitBlockListWithResponse(null, null, metadata, null, null, null)
        def response = bc.getPropertiesWithResponse(null, null, null)

        then:
        response.statusCode() == 200
        response.value().metadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @Unroll
    def "Commit block list AC"() {
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
        bc.commitBlockListWithResponse(null, null, null, bac, null, null).statusCode() == 201

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
        bc.commitBlockListWithResponse(null, null, null, bac, null, null)
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
        bc = cc.getBlockBlobClient(generateBlobName())

        when:
        bc.commitBlockListWithResponse(new ArrayList<String>(), null, null,
            new BlobAccessConditions().leaseAccessConditions(new LeaseAccessConditions().leaseId("garbage")), null, null)

        then:
        thrown(StorageException)
    }

    def "Get block list"() {
        setup:
        def committedBlocks = [getBlockID(), getBlockID()]
        bc.stageBlock(committedBlocks.get(0), defaultInputStream.get(), defaultDataSize)
        bc.stageBlock(committedBlocks.get(1), defaultInputStream.get(), defaultDataSize)
        bc.commitBlockList(committedBlocks)

        def uncommittedBlocks = [getBlockID(), getBlockID()]
        bc.stageBlock(uncommittedBlocks.get(0), defaultInputStream.get(), defaultDataSize)
        bc.stageBlock(uncommittedBlocks.get(1), defaultInputStream.get(), defaultDataSize)
        uncommittedBlocks.sort(true)

        when:
        def blockList = bc.listBlocks(BlockListType.ALL)

        then:
        blockList.committedBlocks().collect { it.name() } as Set == committedBlocks as Set
        blockList.uncommittedBlocks().collect { it.name() } as Set == uncommittedBlocks as Set

        (blockList.committedBlocks() + blockList.uncommittedBlocks())
            .each { assert it.size() == defaultDataSize }
    }

    def "Get block list min"() {
        when:
        bc.listBlocks(BlockListType.ALL)

        then:
        notThrown(StorageErrorException)
    }

    @Unroll
    def "Get block list type"() {
        setup:
        def blockID = getBlockID()
        bc.stageBlock(blockID, defaultInputStream.get(), defaultDataSize)
        bc.commitBlockList([blockID])
        bc.stageBlock(getBlockID(), defaultInputStream.get(), defaultDataSize)

        when:
        def response = bc.listBlocks(type)

        then:
        response.committedBlocks().size() == committedCount
        response.uncommittedBlocks().size() == uncommittedCount

        where:
        type                      | committedCount | uncommittedCount
        BlockListType.ALL         | 1              | 1
        BlockListType.COMMITTED   | 1              | 0
        BlockListType.UNCOMMITTED | 0              | 1
    }

    def "Get block list type null"() {
        when:
        bc.listBlocks(null).iterator().hasNext()

        then:
        notThrown(IllegalArgumentException)
    }

    def "Get block list lease"() {
        setup:
        String leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)

        when:
        bc.listBlocksWithResponse(BlockListType.ALL, new LeaseAccessConditions().leaseId(leaseID), null)

        then:
        notThrown(StorageException)
    }

    def "Get block list lease fail"() {
        setup:
        setupBlobLeaseCondition(bc, garbageLeaseID)

        when:
        bc.listBlocksWithResponse(BlockListType.ALL, new LeaseAccessConditions().leaseId(garbageLeaseID), null)

        then:
        def e = thrown(StorageException)
        e.errorCode() == StorageErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION
    }

    def "Get block list error"() {
        setup:
        bc = cc.getBlockBlobClient(generateBlobName())

        when:
        bc.listBlocks(BlockListType.ALL).iterator().hasNext()

        then:
        thrown(StorageException)
    }

    def "Upload"() {
        when:
        def response = bc.uploadWithResponse(defaultInputStream.get(), defaultDataSize, null, null, null, null, null)

        then:
        response.statusCode() == 201
        def outStream = new ByteArrayOutputStream()
        bc.download(outStream)
        outStream.toByteArray() == "default".getBytes(StandardCharsets.UTF_8)
        validateBasicHeaders(response.headers())
        response.headers().value("Content-MD5") != null
        Boolean.parseBoolean(response.headers().value("x-ms-request-server-encrypted"))
    }

    def "Upload min"() {
        expect:
        bc.uploadWithResponse(defaultInputStream.get(), defaultDataSize, null, null, null, null, null).statusCode() == 201
    }

    @Unroll
    def "Upload illegal argument"() {
        when:
        bc.upload(data, dataSize)

        then:
        def e = thrown(Exception)
        exceptionType.isInstance(e)

        where:
        data                     | dataSize            | exceptionType
        null                     | defaultDataSize     | NullPointerException
        defaultInputStream.get() | defaultDataSize + 1 | IndexOutOfBoundsException
        // This doesn't error as it isn't reading the entire stream which is valid in the new client
        // defaultInputStream.get() | defaultDataSize - 1 | StorageErrorException
    }

    def "Upload empty body"() {
        expect:
        bc.uploadWithResponse(new ByteArrayInputStream(new byte[0]), 0, null, null, null, null, null).statusCode() == 201
    }

    def "Upload null body"() {
        expect:
        bc.uploadWithResponse(null, 0, null, null, null, null, null).statusCode() == 201
    }

    @Unroll
    def "Upload headers"() {
        setup:
        BlobHTTPHeaders headers = new BlobHTTPHeaders().blobCacheControl(cacheControl)
            .blobContentDisposition(contentDisposition)
            .blobContentEncoding(contentEncoding)
            .blobContentLanguage(contentLanguage)
            .blobContentMD5(contentMD5)
            .blobContentType(contentType)

        when:
        bc.uploadWithResponse(defaultInputStream.get(), defaultDataSize, headers, null, null, null, null)
        def response = bc.getPropertiesWithResponse(null, null, null)

        // If the value isn't set the service will automatically set it
        contentMD5 = (contentMD5 == null) ? MessageDigest.getInstance("MD5").digest(defaultData.array()) : contentMD5
        contentType = (contentType == null) ? "application/octet-stream" : contentType

        then:
        validateBlobProperties(response, cacheControl, contentDisposition, contentEncoding, contentLanguage, contentMD5, contentType)

        where:
        cacheControl | contentDisposition | contentEncoding | contentLanguage | contentMD5                                                    | contentType
        null         | null               | null            | null            | null                                                          | null
        "control"    | "disposition"      | "encoding"      | "language"      | MessageDigest.getInstance("MD5").digest(defaultData.array())  | "type"
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
        bc.uploadWithResponse(defaultInputStream.get(), defaultDataSize, null, metadata, null, null, null)
        def response = bc.getPropertiesWithResponse(null, null, null)

        then:
        response.statusCode() == 200
        response.value().metadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @Unroll
    def "Upload AC"() {
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
        bc.uploadWithResponse(defaultInputStream.get(), defaultDataSize, null, null, bac, null, null).statusCode() == 201

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
        bc.uploadWithResponse(defaultInputStream.get(), defaultDataSize, null, null, bac, null, null)

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
        bc = cc.getBlockBlobClient(generateBlobName())

        when:
        bc.uploadWithResponse(defaultInputStream.get(), defaultDataSize, null, null,
            new BlobAccessConditions().leaseAccessConditions(new LeaseAccessConditions().leaseId("id")),
            null, null)

        then:
        thrown(StorageException)
    }
}
