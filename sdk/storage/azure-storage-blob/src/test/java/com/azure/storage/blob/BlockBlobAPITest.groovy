// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.core.http.HttpHeaders
import com.azure.core.http.rest.Response
import com.azure.core.http.rest.VoidResponse
import com.azure.storage.blob.models.BlobAccessConditions
import com.azure.storage.blob.models.BlobHTTPHeaders
import com.azure.storage.blob.models.BlobRange
import com.azure.storage.blob.models.BlockBlobItem
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
    BlockBlobClient bu

    def setup() {
        bu = cu.getBlockBlobClient(generateBlobName())
        bu.upload(defaultInputStream.get(), defaultDataSize)
    }

    def getBlockID() {
        return Base64.encoder.encodeToString(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8))
    }

    def "Stage block"() {
        setup:
        VoidResponse response = bu.stageBlockWithResponse(getBlockID(), defaultInputStream.get(), defaultDataSize, null, null, null)
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
        bu.stageBlockWithResponse(getBlockID(), defaultInputStream.get(), defaultDataSize, null, null, null).statusCode() == 201
    }

    @Unroll
    def "Stage block illegal arguments"() {
        when:
        bu.stageBlock(blockID, data == null ? null : data.get(), dataSize)

        then:
        def e = thrown(Exception)
        exceptionType.isInstance(e)

        where:
        blockID      | data               | dataSize            | exceptionType
        null         | defaultInputStream | defaultDataSize     | StorageException
        getBlockID() | null               | defaultDataSize     | NullPointerException
        getBlockID() | defaultInputStream | defaultDataSize + 1 | IndexOutOfBoundsException
        // TODO (alzimmer): This doesn't throw an error as the stream is larger than the stated size
        //getBlockID() | defaultInputStream   | defaultDataSize - 1 | IllegalArgumentException
    }

    def "Stage block empty body"() {
        when:
        bu.stageBlock(getBlockID(), new ByteArrayInputStream(new byte[0]), 0)

        then:
        thrown(StorageException)
    }

    def "Stage block null body"() {
        when:
        bu.stageBlock(getBlockID(), null, 0)

        then:
        thrown(StorageException)
    }

    def "Stage block lease"() {
        setup:
        String leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)

        expect:
        bu.stageBlockWithResponse(getBlockID(), defaultInputStream.get(), defaultDataSize, new LeaseAccessConditions().leaseId(leaseID),
            null, null).statusCode() == 201
    }

    def "Stage block lease fail"() {
        setup:
        setupBlobLeaseCondition(bu, receivedLeaseID)

        when:
        bu.stageBlockWithResponse(getBlockID(), defaultInputStream.get(), defaultDataSize, new LeaseAccessConditions()
            .leaseId(garbageLeaseID), null, null)

        then:
        def e = thrown(StorageException)
        e.errorCode() == StorageErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION
    }

    def "Stage block error"() {
        setup:
        bu = cu.getBlockBlobClient(generateBlobName())

        when:
        bu.stageBlock("id", defaultInputStream.get(), defaultDataSize)

        then:
        thrown(StorageException)
    }

    def "Stage block from url"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def bu2 = cu.getBlockBlobClient(generateBlobName())
        def blockID = getBlockID()

        when:
        HttpHeaders headers = bu2.stageBlockFromURLWithResponse(blockID, bu.getBlobUrl(), null, null, null, null, null, null).headers()
        Iterator<BlockItem> listResponse = bu2.listBlocks(BlockListType.ALL).iterator()
        BlockItem block = listResponse.next()
        bu2.commitBlockList(Arrays.asList(blockID))
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        bu2.download(outputStream)

        then:
        headers.value("x-ms-request-id") != null
        headers.value("x-ms-version") != null
        headers.value("x-ms-content-crc64") != null
        headers.value("x-ms-request-server-encrypted") != null


        block.name() == blockID
        !block.isCommitted()
        !listResponse.hasNext()

        ByteBuffer.wrap(outputStream.toByteArray()) == defaultData
    }

    def "Stage block from url min"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def bu2 = cu.getBlockBlobClient(generateBlobName())
        def blockID = getBlockID()

        expect:
        bu2.stageBlockFromURLWithResponse(blockID, bu.getBlobUrl(), null, null, null, null, null, null).statusCode() == 201
    }

    @Unroll
    def "Stage block from URL IA"() {
        when:
        bu.stageBlockFromURL(blockID, sourceURL, null)

        then:
        thrown(StorageException)

        where:
        blockID      | sourceURL
        null         | new URL("http://www.example.com")
        getBlockID() | null
    }

    def "Stage block from URL range"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def destURL = cu.getBlockBlobClient(generateBlobName())

        when:
        destURL.stageBlockFromURL(getBlockID(), bu.getBlobUrl(), new BlobRange(2, 3))
        Iterator<BlockItem> uncommittedBlock = destURL.listBlocks(BlockListType.UNCOMMITTED).iterator()

        then:
        uncommittedBlock.hasNext()
        uncommittedBlock.hasNext()
        uncommittedBlock.hasNext()
    }

    def "Stage block from URL MD5"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def destURL = cu.getBlockBlobClient(generateBlobName())

        when:
        destURL.stageBlockFromURLWithResponse(getBlockID(), bu.getBlobUrl(), null,
            MessageDigest.getInstance("MD5").digest(defaultData.array()), null, null, null, null)

        then:
        notThrown(StorageException)
    }

    def "Stage block from URL MD5 fail"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def destURL = cu.getBlockBlobClient(generateBlobName())

        when:
        destURL.stageBlockFromURLWithResponse(getBlockID(), bu.getBlobUrl(), null, "garbage".getBytes(),
            null, null, null, null)

        then:
        thrown(StorageException)
    }

    def "Stage block from URL lease"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def lease = new LeaseAccessConditions().leaseId(setupBlobLeaseCondition(bu, receivedLeaseID))

        when:
        bu.stageBlockFromURLWithResponse(getBlockID(), bu.getBlobUrl(), null, null, lease, null, null, null)

        then:
        notThrown(StorageException)
    }

    def "Stage block from URL lease fail"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def lease = new LeaseAccessConditions().leaseId("garbage")

        when:
        bu.stageBlockFromURLWithResponse(getBlockID(), bu.getBlobUrl(), null, null, lease, null, null, null)

        then:
        thrown(StorageException)
    }

    def "Stage block from URL error"() {
        setup:
        cu = primaryServiceURL.getContainerClient(generateContainerName())
        bu = cu.getBlockBlobClient(generateBlobName())

        when:
        bu.stageBlockFromURL(getBlockID(), bu.getBlobUrl(), null)

        then:
        thrown(StorageException)
    }

    @Unroll
    def "Stage block from URL source AC"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def blockID = getBlockID()

        def sourceURL = cu.getBlockBlobClient(generateBlobName())
        sourceURL.upload(defaultInputStream.get(), defaultDataSize)

        sourceIfMatch = setupBlobMatchCondition(sourceURL, sourceIfMatch)
        def smac = new SourceModifiedAccessConditions()
            .sourceIfModifiedSince(sourceIfModifiedSince)
            .sourceIfUnmodifiedSince(sourceIfUnmodifiedSince)
            .sourceIfMatch(sourceIfMatch)
            .sourceIfNoneMatch(sourceIfNoneMatch)

        expect:
        bu.stageBlockFromURLWithResponse(blockID, sourceURL.getBlobUrl(), null, null, null, smac, null, null).statusCode() == 201

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
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def blockID = getBlockID()

        def sourceURL = cu.getBlockBlobClient(generateBlobName())
        sourceURL.upload(defaultInputStream.get(), defaultDataSize)

        def smac = new SourceModifiedAccessConditions()
            .sourceIfModifiedSince(sourceIfModifiedSince)
            .sourceIfUnmodifiedSince(sourceIfUnmodifiedSince)
            .sourceIfMatch(sourceIfMatch)
            .sourceIfNoneMatch(setupBlobMatchCondition(sourceURL, sourceIfNoneMatch))

        when:
        bu.stageBlockFromURLWithResponse(blockID, sourceURL.getBlobUrl(), null, null, null, smac, null, null).statusCode() == 201

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
        bu.stageBlock(blockID, defaultInputStream.get(), defaultDataSize)
        ArrayList<String> ids = new ArrayList<>()
        ids.add(blockID)

        when:
        Response<BlockBlobItem> response = bu.commitBlockListWithResponse(ids, null, null, null, null, null)
        HttpHeaders headers = response.headers()

        then:
        response.statusCode() == 201
        validateBasicHeaders(headers)
        headers.value("x-ms-content-crc64")
        Boolean.parseBoolean(headers.value("x-ms-request-server-encrypted"))
    }

    def "Commit block list min"() {
        setup:
        String blockID = getBlockID()
        bu.stageBlock(blockID, defaultInputStream.get(), defaultDataSize)
        ArrayList<String> ids = new ArrayList<>()
        ids.add(blockID)

        expect:
        bu.commitBlockList(ids) != null
    }

    def "Commit block list null"() {
        expect:
        bu.commitBlockListWithResponse(null, null, null, null, null, null).statusCode() == 201
    }

    @Unroll
    def "Commit block list headers"() {
        setup:
        String blockID = getBlockID()
        bu.stageBlock(blockID, defaultInputStream.get(), defaultDataSize)
        ArrayList<String> ids = new ArrayList<>()
        ids.add(blockID)
        BlobHTTPHeaders headers = new BlobHTTPHeaders().blobCacheControl(cacheControl)
            .blobContentDisposition(contentDisposition)
            .blobContentEncoding(contentEncoding)
            .blobContentLanguage(contentLanguage)
            .blobContentMD5(contentMD5)
            .blobContentType(contentType)

        when:
        bu.commitBlockListWithResponse(ids, headers, null, null, null, null)
        Response<BlobProperties> response = bu.getPropertiesWithResponse(null, null, null)

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
        bu.commitBlockListWithResponse(null, null, metadata, null, null, null)
        Response<BlobProperties> response = bu.getPropertiesWithResponse(null, null, null)

        then:
        response.statusCode() == 200
        getMetadataFromHeaders(response.headers()) == metadata

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
        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))


        expect:
        bu.commitBlockListWithResponse(null, null, null, bac, null, null).statusCode() == 201

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
        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))

        when:
        bu.commitBlockListWithResponse(null, null, null, bac, null, null)

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
        bu = cu.getBlockBlobClient(generateBlobName())

        when:
        bu.commitBlockListWithResponse(new ArrayList<String>(), null, null,
            new BlobAccessConditions().leaseAccessConditions(new LeaseAccessConditions().leaseId("garbage")), null, null)

        then:
        thrown(StorageException)
    }

    def "Get block list"() {
        setup:
        List<String> committedBlocks = Arrays.asList(getBlockID(), getBlockID())
        bu.stageBlock(committedBlocks.get(0), defaultInputStream.get(), defaultDataSize)
        bu.stageBlock(committedBlocks.get(1), defaultInputStream.get(), defaultDataSize)
        bu.commitBlockList(committedBlocks)

        List<String> uncommittedBlocks = Arrays.asList(getBlockID(), getBlockID())
        bu.stageBlock(uncommittedBlocks.get(0), defaultInputStream.get(), defaultDataSize)
        bu.stageBlock(uncommittedBlocks.get(1), defaultInputStream.get(), defaultDataSize)
        uncommittedBlocks.sort(true)

        when:
        Iterable<BlockItem> response = bu.listBlocks(BlockListType.ALL)

        then:
        for (BlockItem block : response) {
            assert committedBlocks.contains(block.name()) || uncommittedBlocks.contains(block.name())
            assert block.size() == defaultDataSize
        }
//        for (int i = 0; i < committedBlocks.size(); i++) {
//            assert response.body().committedBlocks().get(i).name() == committedBlocks.get(i)
//            assert response.body().committedBlocks().get(i).size() == defaultDataSize
//            assert response.body().uncommittedBlocks().get(i).name() == uncommittedBlocks.get(i)
//            assert response.body().uncommittedBlocks().get(i).size() == defaultDataSize
//        }
//        validateBasicHeaders(response.headers())
//        response.headers().contentType() != null
//        response.headers().blobContentLength() == defaultDataSize * 2L
    }

    def "Get block list min"() {
        when:
        bu.listBlocks(BlockListType.ALL)

        then:
        notThrown(StorageErrorException)
    }

    @Unroll
    def "Get block list type"() {
        setup:
        String blockID = getBlockID()
        bu.stageBlock(blockID, defaultInputStream.get(), defaultDataSize)
        ArrayList<String> ids = new ArrayList<>()
        ids.add(blockID)
        bu.commitBlockList(ids)
        blockID = new String(getBlockID())
        bu.stageBlock(blockID, defaultInputStream.get(), defaultDataSize)

        when:
        Iterable<BlockItem> response = bu.listBlocks(type)

        then:
        int committed = 0
        int uncommitted = 0
        for (BlockItem item : response) {
            if (item.isCommitted()) {
                committed++
            } else {
                uncommitted++
            }
        }
        committed == committedCount
        uncommitted == uncommittedCount

        where:
        type                      | committedCount | uncommittedCount
        BlockListType.ALL         | 1              | 1
        BlockListType.COMMITTED   | 1              | 0
        BlockListType.UNCOMMITTED | 0              | 1
    }

    def "Get block list type null"() {
        when:
        bu.listBlocks(null).iterator().hasNext()

        then:
        notThrown(IllegalArgumentException)
    }

    def "Get block list lease"() {
        setup:
        String leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)

        when:
        bu.listBlocks(BlockListType.ALL, new LeaseAccessConditions().leaseId(leaseID), null).iterator().hasNext()

        then:
        notThrown(StorageException)
    }

    def "Get block list lease fail"() {
        setup:
        setupBlobLeaseCondition(bu, garbageLeaseID)

        when:
        bu.listBlocks(BlockListType.ALL, new LeaseAccessConditions().leaseId(garbageLeaseID), null).iterator().hasNext()

        then:
        def e = thrown(StorageException)
        e.errorCode() == StorageErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION
    }

    def "Get block list error"() {
        setup:
        bu = cu.getBlockBlobClient(generateBlobName())

        when:
        bu.listBlocks(BlockListType.ALL).iterator().hasNext()

        then:
        thrown(StorageException)
    }

    def "Upload"() {
        when:
        Response<BlockBlobItem> response = bu.uploadWithResponse(defaultInputStream.get(), defaultDataSize, null, null, null, null, null)
        HttpHeaders headers = response.headers()

        then:
        response.statusCode() == 201
        def outStream = new ByteArrayOutputStream()
        bu.download(outStream)
        outStream.toByteArray() == "default".getBytes(StandardCharsets.UTF_8)
        validateBasicHeaders(headers)
        headers.value("Content-MD5") != null
        Boolean.parseBoolean(headers.value("x-ms-request-server-encrypted"))
    }

    def "Upload min"() {
        expect:
        bu.uploadWithResponse(defaultInputStream.get(), defaultDataSize, null, null, null, null, null).statusCode() == 201
    }

    @Unroll
    def "Upload illegal argument"() {
        when:
        bu.upload(data, dataSize)

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
        bu.uploadWithResponse(new ByteArrayInputStream(new byte[0]), 0, null, null, null, null, null).statusCode() == 201
    }

    def "Upload null body"() {
        expect:
        bu.uploadWithResponse(null, 0, null, null, null, null, null).statusCode() == 201
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
        bu.uploadWithResponse(defaultInputStream.get(), defaultDataSize, headers, null, null, null, null)
        Response<BlobProperties> response = bu.getPropertiesWithResponse(null, null, null)

        // If the value isn't set the service will automatically set it
        contentMD5 = (contentMD5 == null) ? MessageDigest.getInstance("MD5").digest(defaultData.array()) : contentMD5
        contentType = (contentType == null) ? "application/octet-stream" : contentType

        then:
        validateBlobProperties(response, cacheControl, contentDisposition, contentEncoding, contentLanguage, contentMD5, contentType)

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
        bu.uploadWithResponse(defaultInputStream.get(), defaultDataSize, null, metadata, null, null, null)
        Response<BlobProperties> response = bu.getPropertiesWithResponse(null, null, null)

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
        bu.uploadWithResponse(defaultInputStream.get(), defaultDataSize, null, null, bac, null, null).statusCode() == 201

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
        BlobAccessConditions bac = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified)
                .ifMatch(match)
                .ifNoneMatch(noneMatch))

        when:
        bu.uploadWithResponse(defaultInputStream.get(), defaultDataSize, null, null, bac, null, null)

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
        bu = cu.getBlockBlobClient(generateBlobName())

        when:
        bu.uploadWithResponse(defaultInputStream.get(), defaultDataSize, null, null,
            new BlobAccessConditions().leaseAccessConditions(new LeaseAccessConditions().leaseId("id")),
            null, null)

        then:
        thrown(StorageException)
    }
}
