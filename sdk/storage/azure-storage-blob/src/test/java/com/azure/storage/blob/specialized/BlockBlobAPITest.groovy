// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized

import com.azure.core.exception.UnexpectedLengthException
import com.azure.core.http.HttpMethod
import com.azure.core.http.HttpPipelineCallContext
import com.azure.core.http.HttpPipelineNextPolicy
import com.azure.core.http.HttpRequest
import com.azure.core.http.policy.HttpLogDetailLevel
import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.core.util.Context
import com.azure.storage.blob.APISpec
import com.azure.storage.blob.BlobServiceClientBuilder
import com.azure.storage.blob.models.AccessTier
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
import com.azure.storage.common.policy.RequestRetryOptions
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import spock.lang.Requires
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

class BlockBlobAPITest extends APISpec {
    BlockBlobClient bc
    BlockBlobAsyncClient bac
    String blobName

    def setup() {
        blobName = generateBlobName()
        bc = cc.getBlobClient(blobName).asBlockBlobClient()
        bc.upload(defaultInputStream.get(), defaultDataSize)
        bac = ccAsync.getBlobAsyncClient(generateBlobName()).asBlockBlobAsyncClient()
        bac.upload(defaultFlux, defaultDataSize)
    }

    def "Stage block"() {
        setup:
        def response = bc.stageBlockWithResponse(getBlockID(), defaultInputStream.get(), defaultDataSize, null, null, null)
        def headers = response.getHeaders()

        expect:
        response.getStatusCode() == 201
        headers.getValue("x-ms-content-crc64") != null
        headers.getValue("x-ms-request-id") != null
        headers.getValue("x-ms-version") != null
        headers.getValue("Date") != null
        Boolean.parseBoolean(headers.getValue("x-ms-request-server-encrypted"))
    }

    def "Stage block min"() {
        when:
        bc.stageBlock(getBlockID(), defaultInputStream.get(), defaultDataSize) == 201

        then:
        bc.listBlocks(BlockListType.ALL).getUncommittedBlocks().size() == 1
    }

    @Unroll
    def "Stage block illegal arguments"() {
        when:
        def blockID = (getBlockId) ? getBlockID() : null
        bc.stageBlock(blockID, data == null ? null : data.get(), dataSize)

        then:
        thrown(exceptionType)

        where:
        getBlockId   | data                       | dataSize                    | exceptionType
        false        | defaultInputStream | defaultDataSize     | StorageException
        true         | null                       | defaultDataSize     | NullPointerException
        true         | defaultInputStream | defaultDataSize + 1 | UnexpectedLengthException
        true         | defaultInputStream | defaultDataSize - 1 | UnexpectedLengthException
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
        thrown(NullPointerException)
    }

    def "Stage block lease"() {
        setup:
        def leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)

        expect:
        bc.stageBlockWithResponse(getBlockID(), defaultInputStream.get(), defaultDataSize, new LeaseAccessConditions().setLeaseId(leaseID),
            null, null).getStatusCode() == 201
    }

    def "Stage block lease fail"() {
        setup:
        setupBlobLeaseCondition(bc, receivedLeaseID)

        when:
        bc.stageBlockWithResponse(getBlockID(), defaultInputStream.get(), defaultDataSize, new LeaseAccessConditions()
            .setLeaseId(garbageLeaseID), null, null)

        then:
        def e = thrown(StorageException)
        e.getErrorCode() == StorageErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION
    }

    def "Stage block error"() {
        setup:
        bc = cc.getBlobClient(generateBlobName()).asBlockBlobClient()

        when:
        bc.stageBlock("id", defaultInputStream.get(), defaultDataSize)

        then:
        thrown(StorageException)
    }

    def "Stage block from url"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def bu2 = cc.getBlobClient(generateBlobName()).asBlockBlobClient()
        def blockID = getBlockID()

        when:
        def headers = bu2.stageBlockFromURLWithResponse(blockID, bc.getBlobUrl(), null, null, null, null, null, null).getHeaders()

        then:
        headers.getValue("x-ms-request-id") != null
        headers.getValue("x-ms-version") != null
        headers.getValue("x-ms-content-crc64") != null
        headers.getValue("x-ms-request-server-encrypted") != null

        def response = bu2.listBlocks(BlockListType.ALL)
        response.getUncommittedBlocks().size() == 1
        response.getCommittedBlocks().size() == 0
        response.getUncommittedBlocks().first().getName() == blockID

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
        def bu2 = cc.getBlobClient(generateBlobName()).asBlockBlobClient()
        def blockID = getBlockID()

        expect:
        bu2.stageBlockFromURLWithResponse(blockID, bc.getBlobUrl(), null, null, null, null, null, null).getStatusCode() == 201
    }

    @Unroll
    def "Stage block from URL IA"() {
        when:
        def blockID = (getBlockId) ? getBlockID() : null
        bc.stageBlockFromURL(blockID, sourceURL, null)

        then:
        thrown(StorageException)

        where:
        getBlockId | sourceURL
        false      | new URL("http://www.example.com")
        true       | null
    }

    def "Stage block from URL range"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def destURL = cc.getBlobClient(generateBlobName()).asBlockBlobClient()

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
        def destURL = cc.getBlobClient(generateBlobName()).asBlockBlobClient()

        when:
        destURL.stageBlockFromURLWithResponse(getBlockID(), bc.getBlobUrl(), null,
            MessageDigest.getInstance("MD5").digest(defaultData.array()), null, null, null, null)

        then:
        notThrown(StorageException)
    }

    def "Stage block from URL MD5 fail"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def destURL = cc.getBlobClient(generateBlobName()).asBlockBlobClient()

        when:
        destURL.stageBlockFromURLWithResponse(getBlockID(), bc.getBlobUrl(), null, "garbage".getBytes(),
            null, null, null, null)

        then:
        thrown(StorageException)
    }

    def "Stage block from URL lease"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def lease = new LeaseAccessConditions().setLeaseId(setupBlobLeaseCondition(bc, receivedLeaseID))

        when:
        bc.stageBlockFromURLWithResponse(getBlockID(), bc.getBlobUrl(), null, null, lease, null, null, null)

        then:
        notThrown(StorageException)
    }

    def "Stage block from URL lease fail"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def lease = new LeaseAccessConditions().setLeaseId("garbage")

        when:
        bc.stageBlockFromURLWithResponse(getBlockID(), bc.getBlobUrl(), null, null, lease, null, null, null)

        then:
        thrown(StorageException)
    }

    def "Stage block from URL error"() {
        setup:
        bc = primaryBlobServiceClient.getContainerClient(generateContainerName())
            .getBlobClient(generateBlobName())
            .asBlockBlobClient()

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

        def sourceURL = cc.getBlobClient(generateBlobName()).asBlockBlobClient()
        sourceURL.upload(defaultInputStream.get(), defaultDataSize)

        sourceIfMatch = setupBlobMatchCondition(sourceURL, sourceIfMatch)
        def smac = new SourceModifiedAccessConditions()
            .setSourceIfModifiedSince(sourceIfModifiedSince)
            .setSourceIfUnmodifiedSince(sourceIfUnmodifiedSince)
            .setSourceIfMatch(sourceIfMatch)
            .setSourceIfNoneMatch(sourceIfNoneMatch)

        expect:
        bc.stageBlockFromURLWithResponse(blockID, sourceURL.getBlobUrl(), null, null, null, smac, null, null).getStatusCode() == 201

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

        def sourceURL = cc.getBlobClient(generateBlobName()).asBlockBlobClient()
        sourceURL.upload(defaultInputStream.get(), defaultDataSize)

        def smac = new SourceModifiedAccessConditions()
            .setSourceIfModifiedSince(sourceIfModifiedSince)
            .setSourceIfUnmodifiedSince(sourceIfUnmodifiedSince)
            .setSourceIfMatch(sourceIfMatch)
            .setSourceIfNoneMatch(setupBlobMatchCondition(sourceURL, sourceIfNoneMatch))

        when:
        bc.stageBlockFromURLWithResponse(blockID, sourceURL.getBlobUrl(), null, null, null, smac, null, null).getStatusCode() == 201

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
        def blockID = getBlockID()
        bc.stageBlock(blockID, defaultInputStream.get(), defaultDataSize)
        def ids = [ blockID ] as List

        when:
        def response = bc.commitBlockListWithResponse(ids, null, null, null, null, null, null)
        def headers = response.getHeaders()

        then:
        response.getStatusCode() == 201
        validateBasicHeaders(headers)
        headers.getValue("x-ms-content-crc64")
        Boolean.parseBoolean(headers.getValue("x-ms-request-server-encrypted"))
    }

    def "Commit block list min"() {
        setup:
        def blockID = getBlockID()
        bc.stageBlock(blockID, defaultInputStream.get(), defaultDataSize)
        def ids = [ blockID ] as List

        expect:
        bc.commitBlockList(ids) != null
    }

    def "Commit block list null"() {
        expect:
        bc.commitBlockListWithResponse(null, null, null, null, null, null, null).getStatusCode() == 201
    }

    @Unroll
    def "Commit block list headers"() {
        setup:
        def blockID = getBlockID()
        bc.stageBlock(blockID, defaultInputStream.get(), defaultDataSize)
        def ids = [ blockID ] as List
        def headers = new BlobHTTPHeaders().setBlobCacheControl(cacheControl)
            .setBlobContentDisposition(contentDisposition)
            .setBlobContentEncoding(contentEncoding)
            .setBlobContentLanguage(contentLanguage)
            .setBlobContentMD5(contentMD5)
            .setBlobContentType(contentType)

        when:
        bc.commitBlockListWithResponse(ids, headers, null, null, null, null, null)
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
        def metadata = new Metadata()
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }

        when:
        bc.commitBlockListWithResponse(null, null, metadata, null, null, null, null)
        def response = bc.getPropertiesWithResponse(null, null, null)

        then:
        response.getStatusCode() == 200
        response.getValue().getMetadata() == metadata

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
        def bac = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseID))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch))


        expect:
        bc.commitBlockListWithResponse(null, null, null, null, bac, null, null).getStatusCode() == 201

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
        def bac = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseID))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch))

        when:
        bc.commitBlockListWithResponse(null, null, null, null, bac, null, null)
        then:
        def e = thrown(StorageException)
        e.getErrorCode() == StorageErrorCode.CONDITION_NOT_MET ||
            e.getErrorCode() == StorageErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION

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
        bc = cc.getBlobClient(generateBlobName()).asBlockBlobClient()

        when:
        bc.commitBlockListWithResponse(new ArrayList<String>(), null, null, null,
            new BlobAccessConditions().setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId("garbage")), null, null)

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
        blockList.getCommittedBlocks().collect { it.getName() } as Set == committedBlocks as Set
        blockList.getUncommittedBlocks().collect { it.getName() } as Set == uncommittedBlocks as Set

        (blockList.getCommittedBlocks() + blockList.getUncommittedBlocks())
            .each { assert it.getSize() == defaultDataSize }
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
        response.getCommittedBlocks().size() == committedCount
        response.getUncommittedBlocks().size() == uncommittedCount

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
        def leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)

        when:
        bc.listBlocksWithResponse(BlockListType.ALL, new LeaseAccessConditions().setLeaseId(leaseID), null, Context.NONE)

        then:
        notThrown(StorageException)
    }

    def "Get block list lease fail"() {
        setup:
        setupBlobLeaseCondition(bc, garbageLeaseID)

        when:
        bc.listBlocksWithResponse(BlockListType.ALL, new LeaseAccessConditions().setLeaseId(garbageLeaseID), null, Context.NONE)

        then:
        def e = thrown(StorageException)
        e.getErrorCode() == StorageErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION
    }

    def "Get block list error"() {
        setup:
        bc = cc.getBlobClient(generateBlobName()).asBlockBlobClient()

        when:
        bc.listBlocks(BlockListType.ALL).iterator().hasNext()

        then:
        thrown(StorageException)
    }

    def "Upload"() {
        when:
        def response = bc.uploadWithResponse(defaultInputStream.get(), defaultDataSize, null, null, null, null, null, null)

        then:
        response.getStatusCode() == 201
        def outStream = new ByteArrayOutputStream()
        bc.download(outStream)
        outStream.toByteArray() == defaultText.getBytes(StandardCharsets.UTF_8)
        validateBasicHeaders(response.getHeaders())
        response.getHeaders().getValue("Content-MD5") != null
        Boolean.parseBoolean(response.getHeaders().getValue("x-ms-request-server-encrypted"))
    }

    /* Upload From File Tests: Need to run on liveMode only since blockBlob wil generate a `UUID.randomUUID()`
       for getBlockID that will change every time test is run
     */
    @Requires({ liveMode() })
    def "Upload from file"() {
        given:
        def file = new File(this.getClass().getResource("/testfiles/uploadFromFileTestData.txt").getPath())
        def outStream = new ByteArrayOutputStream()

        when:
        bc.uploadFromFile(file.getAbsolutePath())

        then:
        bc.download(outStream)
        outStream.toByteArray() == new Scanner(file).useDelimiter("\\z").next().getBytes(StandardCharsets.UTF_8)
    }

    @Requires({ liveMode() })
    def "Upload from file with metadata"() {
        given:
        Metadata metadata = new Metadata(Collections.singletonMap("metadata", "value"))
        def file = new File(this.getClass().getResource("/testfiles/uploadFromFileTestData.txt").getPath())
        def outStream = new ByteArrayOutputStream()

        when:
        bc.uploadFromFile(file.getAbsolutePath(), null, null, metadata, null, null, null)

        then:
        metadata == bc.getProperties().getMetadata()
        bc.download(outStream)
        outStream.toByteArray() == new Scanner(file).useDelimiter("\\z").next().getBytes(StandardCharsets.UTF_8)
    }

    def "Upload min"() {
        when:
        bc.upload(defaultInputStream.get(), defaultDataSize)

        then:
        def outStream = new ByteArrayOutputStream()
        bc.download(outStream)
        outStream.toByteArray() == defaultText.getBytes(StandardCharsets.UTF_8)
    }

    @Unroll
    def "Upload illegal argument"() {
        when:
        bc.upload(data, dataSize)

        then:
        thrown(exceptionType)

        where:
        data                     | dataSize            | exceptionType
        null                     | defaultDataSize     | NullPointerException
        defaultInputStream.get() | defaultDataSize + 1 | UnexpectedLengthException
        defaultInputStream.get() | defaultDataSize - 1 | UnexpectedLengthException
    }

    def "Upload empty body"() {
        expect:
        bc.uploadWithResponse(new ByteArrayInputStream(new byte[0]), 0, null, null, null, null, null, null).getStatusCode() == 201
    }

    def "Upload null body"() {
        when:
        bc.uploadWithResponse(null, 0, null, null, null, null, null, null)

        then:
        thrown(NullPointerException)
    }

    @Unroll
    def "Upload headers"() {
        setup:
        def headers = new BlobHTTPHeaders().setBlobCacheControl(cacheControl)
            .setBlobContentDisposition(contentDisposition)
            .setBlobContentEncoding(contentEncoding)
            .setBlobContentLanguage(contentLanguage)
            .setBlobContentMD5(contentMD5)
            .setBlobContentType(contentType)

        when:
        bc.uploadWithResponse(defaultInputStream.get(), defaultDataSize, headers, null, null, null, null, null)
        def response = bc.getPropertiesWithResponse(null, null, null)

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
        def metadata = new Metadata()
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }

        when:
        bc.uploadWithResponse(defaultInputStream.get(), defaultDataSize, null, metadata, null, null, null, null)
        def response = bc.getPropertiesWithResponse(null, null, null)

        then:
        response.getStatusCode() == 200
        response.getValue().getMetadata() == metadata

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
        def bac = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseID))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch))


        expect:
        bc.uploadWithResponse(defaultInputStream.get(), defaultDataSize, null, null, null, bac, null, null).getStatusCode() == 201

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
        def bac = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseID))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch))

        when:
        bc.uploadWithResponse(defaultInputStream.get(), defaultDataSize, null, null, null, bac, null, null)

        then:
        def e = thrown(StorageException)
        e.getErrorCode() == StorageErrorCode.CONDITION_NOT_MET ||
            e.getErrorCode() == StorageErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION

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
        bc = cc.getBlobClient(generateBlobName()).asBlockBlobClient()

        when:
        bc.uploadWithResponse(defaultInputStream.get(), defaultDataSize, null, null, null,
            new BlobAccessConditions().setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId("id")),
            null, null)

        then:
        thrown(StorageException)
    }

    def "Upload with tier"() {
        setup:
        def bc = cc.getBlobClient(generateBlobName()).asBlockBlobClient()

        when:
        bc.uploadWithResponse(defaultInputStream.get(), defaultDataSize, null, null, AccessTier.COOL, null, null, null)

        then:
        bc.getProperties().getAccessTier() == AccessTier.COOL
    }

    // Only run these tests in live mode as they use variables that can't be captured.
    @Unroll
    @Requires({ liveMode() })
    def "Async buffered upload"() {
        when:
        def data = getRandomData(dataSize)
        bac.upload(Flux.just(data), bufferSize, numBuffs).block()
        data.position(0)

        then:
        // Due to memory issues, this check only runs on small to medium sized data sets.
        if (dataSize < 100 * 1024 * 1024) {
            assert collectBytesInBuffer(bac.download().block()).block() == data
        }
        bac.listBlocks(BlockListType.ALL).block().getCommittedBlocks().size() == blockCount

        where:
        dataSize          | bufferSize        | numBuffs || blockCount
        350               | 50                | 2        || 7 // Requires cycling through the same buffers multiple times.
        350               | 50                | 5        || 7 // Most buffers may only be used once.
        10 * 1024 * 1024  | 1 * 1024 * 1024   | 2        || 10 // Larger data set.
        10 * 1024 * 1024  | 1 * 1024 * 1024   | 5        || 10 // Larger number of Buffs.
        10 * 1024 * 1024  | 1 * 1024 * 1024   | 10       || 10 // Exactly enough buffer space to hold all the data.
        500 * 1024 * 1024 | 100 * 1024 * 1024 | 2        || 5 // Larger data.
        100 * 1024 * 1024 | 20 * 1024 * 1024  | 4        || 5
        10 * 1024 * 1024  | 3 * 512 * 1024    | 3        || 7 // Data does not squarely fit in buffers.
    }

    def compareListToBuffer(List<ByteBuffer> buffers, ByteBuffer result) {
        result.position(0)
        for (ByteBuffer buffer : buffers) {
            buffer.position(0)
            result.limit(result.position() + buffer.remaining())
            if (buffer != result) {
                return false
            }
            result.position(result.position() + buffer.remaining())
        }
        return result.remaining() == 0
    }

    // Only run these tests in live mode as they use variables that can't be captured.
    @Unroll
    @Requires({ liveMode() })
    def "Buffered upload chunked source"() {
        /*
        This test should validate that the upload should work regardless of what format the passed data is in because
        it will be chunked appropriately.
         */
        setup:
        def dataList = [] as List
        dataSizeList.each { size -> dataList.add(getRandomData(size)) }
        bac.upload(Flux.fromIterable(dataList), bufferSize, numBuffers).block()

        expect:
        compareListToBuffer(dataList, collectBytesInBuffer(bac.download().block()).block())
        bac.listBlocks(BlockListType.ALL).block().getCommittedBlocks().size() == blockCount

        where:
        dataSizeList          | bufferSize | numBuffers || blockCount
        [7, 7]                | 10         | 2          || 2 // First item fits entirely in the buffer, next item spans two buffers
        [3, 3, 3, 3, 3, 3, 3] | 10         | 2          || 3 // Multiple items fit non-exactly in one buffer.
        [10, 10]              | 10         | 2          || 2 // Data fits exactly and does not need chunking.
        [50, 51, 49]          | 10         | 2          || 15 // Data needs chunking and does not fit neatly in buffers. Requires waiting for buffers to be released.
        // The case of one large buffer needing to be broken up is tested in the previous test.
    }

    def "Buffered upload illegal arguments null"() {
        when:
        bac.upload(null, 4, 4)

        then:
        thrown(NullPointerException)
    }

    @Unroll
    def "Buffered upload illegal args out of bounds"() {
        when:
        bac.upload(Flux.just(defaultData), bufferSize, numBuffs)

        then:
        thrown(IllegalArgumentException)

        where:
        bufferSize                                     | numBuffs
        0                                              | 5
        BlockBlobAsyncClient.MAX_STAGE_BLOCK_BYTES + 1 | 5
        5                                              | 1
    }

    // Only run these tests in live mode as they use variables that can't be captured.
    @Unroll
    @Requires({ liveMode() })
    def "Buffered upload headers"() {
        when:
        bac.uploadWithResponse(defaultFlux, 10, 2, new BlobHTTPHeaders().setBlobCacheControl(cacheControl)
            .setBlobContentDisposition(contentDisposition)
            .setBlobContentEncoding(contentEncoding)
            .setBlobContentLanguage(contentLanguage)
            .setBlobContentMD5(contentMD5)
            .setBlobContentType(contentType),
            null, null, null).block()

        then:
        validateBlobProperties(bac.getPropertiesWithResponse(null).block(), cacheControl, contentDisposition, contentEncoding,
            contentLanguage, contentMD5, contentType == null ? "application/octet-stream" : contentType)
        // HTTP default content type is application/octet-stream.

        where:
        // The MD5 is simply set on the blob for commitBlockList, not validated.
        cacheControl | contentDisposition | contentEncoding | contentLanguage | contentMD5                                                   | contentType
        null         | null               | null            | null            | null                                                         | null
        "control"    | "disposition"      | "encoding"      | "language"      | MessageDigest.getInstance("MD5").digest(defaultData.array()) | "type"
    }

    // Only run these tests in live mode as they use variables that can't be captured.
    @Unroll
    @Requires({ liveMode() })
    def "Buffered upload metadata"() {
        setup:
        def metadata = new Metadata()
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }

        when:
        bac.uploadWithResponse(Flux.just(getRandomData(10)), 10, 10, null, metadata, null, null).block()
        def response = bac.getPropertiesWithResponse(null).block()

        then:
        response.getStatusCode() == 200
        response.getValue().getMetadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    // Only run these tests in live mode as they use variables that can't be captured.
    @Unroll
    @Requires({ liveMode() })
    def "Buffered upload AC"() {
        setup:
        bac.upload(defaultFlux, defaultDataSize).block()
        match = setupBlobMatchCondition(bac, match)
        leaseID = setupBlobLeaseCondition(bac, leaseID)
        def accessConditions = new BlobAccessConditions().setModifiedAccessConditions(
            new ModifiedAccessConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified)
                .setIfMatch(match).setIfNoneMatch(noneMatch))
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseID))

        expect:
        bac.uploadWithResponse(Flux.just(getRandomData(10)), 10, 2, null, null, null, accessConditions).block().getStatusCode() == 201

        where:
        modified | unmodified | match        | noneMatch   | leaseID
        null     | null       | null         | null        | null
        null     | newDate    | null         | null        | null
        oldDate  | null       | null         | null        | null
        null     | null       | receivedEtag | null        | null
        null     | null       | null         | garbageEtag | null
        null     | null       | null         | null        | receivedLeaseID
    }

    // Only run these tests in live mode as they use variables that can't be captured.
    @Unroll
    @Requires({ liveMode() })
    def "Buffered upload AC fail"() {
        setup:
        bac.upload(defaultFlux, defaultDataSize).block()
        noneMatch = setupBlobMatchCondition(bac, noneMatch)
        leaseID = setupBlobLeaseCondition(bac, leaseID)
        def accessConditions = new BlobAccessConditions().setModifiedAccessConditions(
            new ModifiedAccessConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified)
                .setIfMatch(match).setIfNoneMatch(noneMatch))
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseID))

        when:
        bac.uploadWithResponse(Flux.just(getRandomData(10)), 10, 2, null, null, null, accessConditions).block()

        then:
        def e = thrown(StorageException)
        e.getErrorCode() == StorageErrorCode.CONDITION_NOT_MET ||
            e.getErrorCode() == StorageErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | garbageLeaseID
    }

    /*def "Upload NRF progress"() {
        setup:
        def data = getRandomData(BlockBlobURL.MAX_UPLOAD_BLOB_BYTES + 1)
        def numBlocks = data.remaining() / BlockBlobURL.MAX_STAGE_BLOCK_BYTES
        long prevCount = 0
        def mockReceiver = Mock(IProgressReceiver)


        when:
        TransferManager.uploadFromNonReplayableFlowable(Flowable.just(data), bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES, 10,
            new TransferManagerUploadToBlockBlobOptions(mockReceiver, null, null, null, 20)).blockingGet()
        data.position(0)

        then:
        // We should receive exactly one notification of the completed progress.
        1 * mockReceiver.reportProgress(data.remaining()) */

    /*
    We should receive at least one notification reporting an intermediary value per block, but possibly more
    notifications will be received depending on the implementation. We specify numBlocks - 1 because the last block
    will be the total size as above. Finally, we assert that the number reported monotonically increases.
     */
    /*(numBlocks - 1.._) * mockReceiver.reportProgress(!data.remaining()) >> { long bytesTransferred ->
        if (!(bytesTransferred > prevCount)) {
            throw new IllegalArgumentException("Reported progress should monotonically increase")
        } else {
            prevCount = bytesTransferred
        }
    }

    // We should receive no notifications that report more progress than the size of the file.
    0 * mockReceiver.reportProgress({ it > data.remaining() })
    notThrown(IllegalArgumentException)
}*/

    def "Buffered upload network error"() {
        setup:
        /*
         This test uses a Flowable that does not allow multiple subscriptions and therefore ensures that we are
         buffering properly to allow for retries even given this source behavior.
         */
        bac.upload(Flux.just(defaultData), defaultDataSize).block()
        def nonReplayableFlux = bac.download().block()

        // Mock a response that will always be retried.
        def mockHttpResponse = getStubResponse(500, new HttpRequest(HttpMethod.PUT, new URL("https://www.fake.com")))

        // Mock a policy that will always then check that the data is still the same and return a retryable error.
        def mockPolicy = Mock(HttpPipelinePolicy) {
            process(*_) >> { HttpPipelineCallContext context, HttpPipelineNextPolicy next ->
                return collectBytesInBuffer(context.getHttpRequest().getBody())
                    .map { b ->
                    return b == defaultData
                }
                .flatMap { b ->
                    if (b) {
                        return Mono.just(mockHttpResponse)
                    }
                    return Mono.error(new IllegalArgumentException())
                }
            }
        }

        // Build the pipeline
        bac = new BlobServiceClientBuilder()
            .credential(primaryCredential)
            .endpoint(String.format(defaultEndpointTemplate, primaryCredential.getAccountName()))
            .httpClient(getHttpClient())
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
            .retryOptions(new RequestRetryOptions(null, 3, null, 500, 1500, null))
            .addPolicy(mockPolicy).buildAsyncClient()
            .getContainerAsyncClient(generateContainerName()).getBlobAsyncClient(generateBlobName()).asBlockBlobAsyncClient()

        when:
        // Try to upload the flowable, which will hit a retry. A normal upload would throw, but buffering prevents that.
        bac.upload(nonReplayableFlux, 1024, 4).block()
        // TODO: It could be that duplicates aren't getting made in the retry policy? Or before the retry policy?

        then:
        // A second subscription to a download stream will
        def e = thrown(StorageException)
        e.getStatusCode() == 500
    }

    def "Get Container Name"() {
        expect:
        containerName == bc.getContainerName()
    }

    def "Get Block Blob Name"() {
        expect:
        blobName == bc.getBlobName()
    }
}
