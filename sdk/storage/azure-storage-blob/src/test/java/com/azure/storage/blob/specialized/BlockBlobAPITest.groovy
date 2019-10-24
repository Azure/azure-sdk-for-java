// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized

import com.azure.core.exception.UnexpectedLengthException
import com.azure.core.http.HttpMethod
import com.azure.core.http.HttpPipelineCallContext
import com.azure.core.http.HttpPipelineNextPolicy
import com.azure.core.http.HttpRequest
import com.azure.core.http.policy.HttpLogDetailLevel
import com.azure.core.http.policy.BlobLogOptions
import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.core.implementation.util.FluxUtil
import com.azure.core.util.Context
import com.azure.storage.blob.APISpec
import com.azure.storage.blob.BlobAsyncClient
import com.azure.storage.blob.BlobClient
import com.azure.storage.blob.BlobServiceClientBuilder
import com.azure.storage.blob.ProgressReceiver
import com.azure.storage.blob.models.AccessTier
import com.azure.storage.blob.models.BlobErrorCode
import com.azure.storage.blob.models.BlobHttpHeaders
import com.azure.storage.blob.models.BlobRange
import com.azure.storage.blob.models.BlobRequestConditions
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.blob.models.BlockListType
import com.azure.storage.blob.models.ParallelTransferOptions
import com.azure.storage.blob.models.PublicAccessType
import com.azure.storage.common.policy.RequestRetryOptions
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Requires
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

class BlockBlobAPITest extends APISpec {
    BlockBlobClient bc
    BlockBlobAsyncClient bac
    BlobAsyncClient blobac
    BlobClient blobClient
    String blobName

    def setup() {
        blobName = generateBlobName()
        blobClient = cc.getBlobClient(blobName)
        bc = blobClient.getBlockBlobClient()
        bc.upload(defaultInputStream.get(), defaultDataSize)
        blobac = ccAsync.getBlobAsyncClient(generateBlobName())
        bac = blobac.getBlockBlobAsyncClient()
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
        getBlockId | data               | dataSize            | exceptionType
        false      | defaultInputStream | defaultDataSize     | BlobStorageException
        true       | null               | defaultDataSize     | NullPointerException
        true       | defaultInputStream | defaultDataSize + 1 | UnexpectedLengthException
        true       | defaultInputStream | defaultDataSize - 1 | UnexpectedLengthException
    }

    def "Stage block empty body"() {
        when:
        bc.stageBlock(getBlockID(), new ByteArrayInputStream(new byte[0]), 0)

        then:
        thrown(BlobStorageException)
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
        bc.stageBlockWithResponse(getBlockID(), defaultInputStream.get(), defaultDataSize, leaseID, null, null)
            .getStatusCode() == 201
    }

    def "Stage block lease fail"() {
        setup:
        setupBlobLeaseCondition(bc, receivedLeaseID)

        when:
        bc.stageBlockWithResponse(getBlockID(), defaultInputStream.get(), defaultDataSize, garbageLeaseID, null, null)

        then:
        def e = thrown(BlobStorageException)
        e.getErrorCode() == BlobErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION
    }

    def "Stage block error"() {
        setup:
        bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        bc.stageBlock("id", defaultInputStream.get(), defaultDataSize)

        then:
        thrown(BlobStorageException)
    }

    def "Stage block from url"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        def blockID = getBlockID()

        when:
        def headers = bu2.stageBlockFromUrlWithResponse(blockID, bc.getBlobUrl(), null, null, null, null, null, null).getHeaders()

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
        def bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        def blockID = getBlockID()

        expect:
        bu2.stageBlockFromUrlWithResponse(blockID, bc.getBlobUrl(), null, null, null, null, null, null).getStatusCode() == 201
    }

    @Unroll
    def "Stage block from URL IA"() {
        when:
        def blockID = (getBlockId) ? getBlockID() : null
        bc.stageBlockFromUrl(blockID, sourceURL, null)

        then:
        thrown(exceptionType)

        where:
        getBlockId | sourceURL                | exceptionType
        false      | "http://www.example.com" | BlobStorageException
        true       | null                     | IllegalArgumentException
    }

    def "Stage block from URL range"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def destURL = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        destURL.stageBlockFromUrl(getBlockID(), bc.getBlobUrl(), new BlobRange(2, 3))
        def blockList = destURL.listBlocks(BlockListType.UNCOMMITTED)

        then:
        blockList.getCommittedBlocks().size() == 0
        blockList.getUncommittedBlocks().size() == 1
    }

    def "Stage block from URL MD5"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def destURL = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        destURL.stageBlockFromUrlWithResponse(getBlockID(), bc.getBlobUrl(), null,
            MessageDigest.getInstance("MD5").digest(defaultData.array()), null, null, null, null)

        then:
        notThrown(BlobStorageException)
    }

    def "Stage block from URL MD5 fail"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def destURL = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        destURL.stageBlockFromUrlWithResponse(getBlockID(), bc.getBlobUrl(), null, "garbage".getBytes(),
            null, null, null, null)

        then:
        thrown(BlobStorageException)
    }

    def "Stage block from URL lease"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)

        when:
        bc.stageBlockFromUrlWithResponse(getBlockID(), bc.getBlobUrl(), null, null, setupBlobLeaseCondition(bc, receivedLeaseID), null, null, null)

        then:
        notThrown(BlobStorageException)
    }

    def "Stage block from URL lease fail"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)

        when:
        bc.stageBlockFromUrlWithResponse(getBlockID(), bc.getBlobUrl(), null, null, "garbage", null, null, null)

        then:
        thrown(BlobStorageException)
    }

    def "Stage block from URL error"() {
        setup:
        bc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName())
            .getBlobClient(generateBlobName())
            .getBlockBlobClient()

        when:
        bc.stageBlockFromUrl(getBlockID(), bc.getBlobUrl(), null)

        then:
        thrown(BlobStorageException)
    }

    @Unroll
    def "Stage block from URL source AC"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def blockID = getBlockID()

        def sourceURL = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        sourceURL.upload(defaultInputStream.get(), defaultDataSize)

        sourceIfMatch = setupBlobMatchCondition(sourceURL, sourceIfMatch)
        def smac = new BlobRequestConditions()
            .setIfModifiedSince(sourceIfModifiedSince)
            .setIfUnmodifiedSince(sourceIfUnmodifiedSince)
            .setIfMatch(sourceIfMatch)
            .setIfNoneMatch(sourceIfNoneMatch)

        expect:
        bc.stageBlockFromUrlWithResponse(blockID, sourceURL.getBlobUrl(), null, null, null, smac, null, null).getStatusCode() == 201

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

        def sourceURL = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        sourceURL.upload(defaultInputStream.get(), defaultDataSize)

        def smac = new BlobRequestConditions()
            .setIfModifiedSince(sourceIfModifiedSince)
            .setIfUnmodifiedSince(sourceIfUnmodifiedSince)
            .setIfMatch(sourceIfMatch)
            .setIfNoneMatch(setupBlobMatchCondition(sourceURL, sourceIfNoneMatch))

        when:
        bc.stageBlockFromUrlWithResponse(blockID, sourceURL.getBlobUrl(), null, null, null, smac, null, null).getStatusCode() == 201

        then:
        thrown(BlobStorageException)

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
        def ids = [blockID] as List

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
        def ids = [blockID] as List

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
        def ids = [blockID] as List
        def headers = new BlobHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType)

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
        def metadata = new HashMap<String, String>()
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
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)


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
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        bc.commitBlockListWithResponse(null, null, null, null, bac, null, null)
        then:
        def e = thrown(BlobStorageException)
        e.getErrorCode() == BlobErrorCode.CONDITION_NOT_MET ||
            e.getErrorCode() == BlobErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION

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
        bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        bc.commitBlockListWithResponse(new ArrayList<String>(), null, null, null, new BlobRequestConditions().setLeaseId("garbage"), null, null)

        then:
        thrown(BlobStorageException)
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
        notThrown(BlobStorageException)
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
        bc.listBlocksWithResponse(BlockListType.ALL, leaseID, null, Context.NONE)

        then:
        notThrown(BlobStorageException)
    }

    def "Get block list lease fail"() {
        setup:
        setupBlobLeaseCondition(bc, garbageLeaseID)

        when:
        bc.listBlocksWithResponse(BlockListType.ALL, garbageLeaseID, null, Context.NONE)

        then:
        def e = thrown(BlobStorageException)
        e.getErrorCode() == BlobErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION
    }

    def "Get block list error"() {
        setup:
        bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        bc.listBlocks(BlockListType.ALL).iterator().hasNext()

        then:
        thrown(BlobStorageException)
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
        blobClient.uploadFromFile(file.getAbsolutePath())

        then:
        bc.download(outStream)
        outStream.toByteArray() == new Scanner(file).useDelimiter("\\z").next().getBytes(StandardCharsets.UTF_8)
    }

    @Requires({ liveMode() })
    def "Upload from file with metadata"() {
        given:
        def metadata = Collections.singletonMap("metadata", "value")
        def file = new File(this.getClass().getResource("/testfiles/uploadFromFileTestData.txt").getPath())
        def outStream = new ByteArrayOutputStream()

        when:
        blobClient.uploadFromFile(file.getAbsolutePath(), null, null, metadata, null, null, null)

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
        def headers = new BlobHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType)

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
        def metadata = new HashMap<String, String>()
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
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)


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
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        bc.uploadWithResponse(defaultInputStream.get(), defaultDataSize, null, null, null, bac, null, null)

        then:
        def e = thrown(BlobStorageException)
        e.getErrorCode() == BlobErrorCode.CONDITION_NOT_MET ||
            e.getErrorCode() == BlobErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION

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
        bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        bc.uploadWithResponse(defaultInputStream.get(), defaultDataSize, null, null, null,
            new BlobRequestConditions().setLeaseId("id"), null, null)

        then:
        thrown(BlobStorageException)
    }

    def "Upload with tier"() {
        setup:
        def bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        bc.uploadWithResponse(defaultInputStream.get(), defaultDataSize, null, null, AccessTier.COOL, null, null, null)

        then:
        bc.getProperties().getAccessTier() == AccessTier.COOL
    }

    @Requires({ liveMode() })
    def "Async buffered upload empty"() {
        when:
        def emptyUploadVerifier = StepVerifier.create(blobac.upload(Flux.just(ByteBuffer.wrap(new byte[0])), new ParallelTransferOptions()))

        then:
        emptyUploadVerifier.assertNext({
            assert it.getETag() != null
        }).verifyComplete()

        StepVerifier.create(blobac.download()).assertNext({
            assert it.remaining() == 0
        }).verifyComplete()
    }

    @Unroll
    @Requires({ liveMode() })
    def "Async buffered upload empty buffers"() {
        when:
        def uploadVerifier = StepVerifier.create(blobac.upload(Flux.fromIterable([buffer1, buffer2, buffer3]), new ParallelTransferOptions()))

        then:
        uploadVerifier.assertNext({
            assert it.getETag() != null
        }).verifyComplete()

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(blobac.download())).assertNext({
            assert it == expectedDownload
        }).verifyComplete()

        where:
        buffer1                                                   | buffer2                                               | buffer3                                                    || expectedDownload
        ByteBuffer.wrap("Hello".getBytes(StandardCharsets.UTF_8)) | ByteBuffer.wrap(" ".getBytes(StandardCharsets.UTF_8)) | ByteBuffer.wrap("world!".getBytes(StandardCharsets.UTF_8)) || "Hello world!".getBytes(StandardCharsets.UTF_8)
        ByteBuffer.wrap("Hello".getBytes(StandardCharsets.UTF_8)) | ByteBuffer.wrap(" ".getBytes(StandardCharsets.UTF_8)) | ByteBuffer.wrap(new byte[0])                               || "Hello ".getBytes(StandardCharsets.UTF_8)
        ByteBuffer.wrap("Hello".getBytes(StandardCharsets.UTF_8)) | ByteBuffer.wrap(new byte[0])                          | ByteBuffer.wrap("world!".getBytes(StandardCharsets.UTF_8)) || "Helloworld!".getBytes(StandardCharsets.UTF_8)
        ByteBuffer.wrap(new byte[0])                              | ByteBuffer.wrap(" ".getBytes(StandardCharsets.UTF_8)) | ByteBuffer.wrap("world!".getBytes(StandardCharsets.UTF_8)) || " world!".getBytes(StandardCharsets.UTF_8)
    }

    // Only run these tests in live mode as they use variables that can't be captured.
    @Unroll
    @Requires({ liveMode() })
    def "Async buffered upload"() {
        when:
        def data = getRandomData(dataSize)
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSize(bufferSize).setNumBuffers(numBuffs)
        blobac.upload(Flux.just(data), parallelTransferOptions).block()
        data.position(0)

        then:
        // Due to memory issues, this check only runs on small to medium sized data sets.
        if (dataSize < 100 * 1024 * 1024) {
            assert collectBytesInBuffer(bac.download()).block() == data
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

    /*      Reporter for testing Progress Receiver
    *        Will count the number of reports that are triggered         */

    class Reporter implements ProgressReceiver {
        private final long blockSize
        private long reportingCount

        Reporter(long blockSize) {
            this.blockSize = blockSize
        }

        @Override
        void reportProgress(long bytesTransferred) {
            assert bytesTransferred % blockSize == 0
            this.reportingCount += 1
        }

        long getReportingCount() {
            return this.reportingCount
        }
    }
    // Only run these tests in live mode as they use variables that can't be captured.
    @Unroll
    @Requires({ liveMode() })
    def "Buffered upload with reporter"() {
        when:
        def uploadReporter = new Reporter(blockSize)

        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSize(blockSize).setNumBuffers(bufferCount).setProgressReceiver(uploadReporter)

        def response = blobac
            .uploadWithResponse(Flux.just(getRandomData(size)), parallelTransferOptions, null, null, null, null)
            .block()

        then:
        response.getStatusCode() == 201
        uploadReporter.getReportingCount() == (long) (size / blockSize)

        where:
        size        | blockSize | bufferCount
        10          | 10        | 8
        20          | 1         | 5
        100         | 50        | 2
        1024 * 1024 | 1024      | 100
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
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSize(bufferSize).setNumBuffers(numBuffers)
        def dataList = [] as List
        dataSizeList.each { size -> dataList.add(getRandomData(size)) }
        blobac.upload(Flux.fromIterable(dataList), parallelTransferOptions).block()

        expect:
        compareListToBuffer(dataList, collectBytesInBuffer(bac.download()).block())
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
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSize(4).setNumBuffers(4)
        blobac.upload(null, parallelTransferOptions, true).block()

        then:
        thrown(NullPointerException)
    }

    @Unroll
    def "Buffered upload illegal args out of bounds"() {
        when:
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSize(bufferSize).setNumBuffers(numBuffs)
        blobac.upload(Flux.just(defaultData), parallelTransferOptions, true).block()

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
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSize(10)
        blobac.uploadWithResponse(defaultFlux, parallelTransferOptions, new BlobHttpHeaders()
            .setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType),
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
        def metadata = [:] as Map<String, String>
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }

        when:
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSize(10).setNumBuffers(10)
        blobac.uploadWithResponse(Flux.just(getRandomData(10)), parallelTransferOptions, null, metadata, null, null).block()
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
        def accessConditions = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSize(10)
        blobac.uploadWithResponse(Flux.just(getRandomData(10)), parallelTransferOptions, null, null, null, accessConditions).block().getStatusCode() == 201

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
        def accessConditions = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSize(10)
        blobac.uploadWithResponse(Flux.just(getRandomData(10)), parallelTransferOptions, null, null, null, accessConditions).block()

        then:
        def e = thrown(BlobStorageException)
        e.getErrorCode() == BlobErrorCode.CONDITION_NOT_MET ||
            e.getErrorCode() == BlobErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | garbageLeaseID
    }

    // UploadBufferPool used to lock when the number of failed stageblocks exceeded the maximum number of buffers
    // (discovered when a leaseId was invalid)
    @Unroll
    @Requires({ liveMode() })
    def "UploadBufferPool lock three or more buffers"() {
        setup:
        bac.upload(defaultFlux, defaultDataSize).block()
        def leaseID = setupBlobLeaseCondition(bac, garbageLeaseID)
        def accessConditions = new BlobRequestConditions().setLeaseId(leaseID)

        when:
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSize(blockSize as int)
            .setNumBuffers(numBuffers as int)
        blobac.uploadWithResponse(Flux.just(getRandomData(dataLength as int)), parallelTransferOptions, null, null,
            null, accessConditions).block()

        then:
        thrown(BlobStorageException)

        where:
        dataLength | blockSize | numBuffers
        16         | 7         | 2
        16         | 5         | 2
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
        def nonReplayableFlux = bac.download()

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
        blobac = new BlobServiceClientBuilder()
            .credential(primaryCredential)
            .endpoint(String.format(defaultEndpointTemplate, primaryCredential.getAccountName()))
            .httpClient(getHttpClient())
            .blobLogOptions(new BlobLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .retryOptions(new RequestRetryOptions(null, 3, null, 500, 1500, null))
            .addPolicy(mockPolicy).buildAsyncClient()
            .getBlobContainerAsyncClient(generateContainerName()).getBlobAsyncClient(generateBlobName())

        when:
        // Try to upload the flowable, which will hit a retry. A normal upload would throw, but buffering prevents that.
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSize(1024).setNumBuffers(4)
        blobac.upload(nonReplayableFlux, parallelTransferOptions, true).block()
        // TODO: It could be that duplicates aren't getting made in the retry policy? Or before the retry policy?

        then:
        // A second subscription to a download stream will
        def e = thrown(BlobStorageException)
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

    def "Get Blob Name and Build Client"() {
        when:
        def client = cc.getBlobClient(originalBlobName)
        def blockClient = cc.getBlobClient(client.getBlobName()).getBlockBlobClient()

        then:
        blockClient.getBlobName() == finalBlobName

        where:
        originalBlobName       | finalBlobName
        "blob"                 | "blob"
        "path/to]a blob"       | "path/to]a blob"
        "path%2Fto%5Da%20blob" | "path/to]a blob"
        "斑點"                 | "斑點"
        "%E6%96%91%E9%BB%9E"   | "斑點"
    }

    @Requires({liveMode()})
    def "BlobClient overwrite false"() {
        setup:
        def file = new File(this.getClass().getResource("/testfiles/uploadFromFileTestData.txt").getPath())

        when:
        blobClient.uploadFromFile(file.getPath())

        then:
        thrown(IllegalArgumentException)
    }

    @Requires({liveMode()})
    def "BlobClient overwrite true"() {
        setup:
        def file = new File(this.getClass().getResource("/testfiles/uploadFromFileTestData.txt").getPath())

        when:
        blobClient.uploadFromFile(file.getPath(), true)

        then:
        notThrown(Throwable)
    }

    def "Upload overwrite false"() {
        when:
        bc.upload(defaultInputStream.get(), defaultDataSize)

        then:
        thrown(BlobStorageException)
    }

    def "Upload overwrite true"() {
        when:
        bc.upload(defaultInputStream.get(), defaultDataSize, true)

        then:
        notThrown(Throwable)
    }
}
