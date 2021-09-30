// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized

import com.azure.core.exception.UnexpectedLengthException
import com.azure.core.http.HttpMethod
import com.azure.core.http.HttpPipelineCallContext
import com.azure.core.http.HttpPipelineNextPolicy
import com.azure.core.http.HttpRequest
import com.azure.core.util.Context
import com.azure.core.util.FluxUtil
import com.azure.identity.DefaultAzureCredentialBuilder
import com.azure.storage.blob.APISpec
import com.azure.storage.blob.BlobAsyncClient
import com.azure.storage.blob.BlobClient
import com.azure.storage.blob.BlobServiceClientBuilder
import com.azure.storage.blob.BlobServiceVersion
import com.azure.storage.blob.BlobUrlParts
import com.azure.storage.blob.ProgressReceiver
import com.azure.storage.blob.models.AccessTier
import com.azure.storage.blob.models.BlobErrorCode
import com.azure.storage.blob.models.BlobHttpHeaders
import com.azure.storage.blob.options.BlobGetTagsOptions
import com.azure.storage.blob.options.BlobParallelUploadOptions
import com.azure.storage.blob.models.BlobRange
import com.azure.storage.blob.models.BlobRequestConditions
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.blob.options.BlobUploadFromUrlOptions
import com.azure.storage.blob.options.BlockBlobCommitBlockListOptions
import com.azure.storage.blob.options.BlockBlobListBlocksOptions
import com.azure.storage.blob.options.BlockBlobSimpleUploadOptions
import com.azure.storage.blob.models.BlockListType
import com.azure.storage.blob.models.CustomerProvidedKey
import com.azure.storage.blob.models.ParallelTransferOptions
import com.azure.storage.blob.models.PublicAccessType
import com.azure.storage.blob.options.BlobUploadFromFileOptions
import com.azure.storage.blob.sas.BlobContainerSasPermission
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues
import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.policy.RequestRetryOptions
import com.azure.storage.common.test.shared.extensions.LiveOnly
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.IgnoreIf
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.security.MessageDigest
import java.time.OffsetDateTime

class BlockBlobAPITest extends APISpec {
    BlockBlobClient blockBlobClient
    BlockBlobAsyncClient blockBlobAsyncClient
    BlobAsyncClient blobAsyncClient
    BlobClient blobClient
    String blobName

    def setup() {
        blobName = generateBlobName()
        blobClient = cc.getBlobClient(blobName)
        blockBlobClient = blobClient.getBlockBlobClient()
        blockBlobClient.upload(data.defaultInputStream, data.defaultDataSize, true)
        blobAsyncClient = ccAsync.getBlobAsyncClient(generateBlobName())
        blockBlobAsyncClient = blobAsyncClient.getBlockBlobAsyncClient()
        blockBlobAsyncClient.upload(data.defaultFlux, data.defaultDataSize, true).block()
    }

    def "Stage block"() {
        setup:
        def response = blockBlobClient.stageBlockWithResponse(getBlockID(), data.defaultInputStream, data.defaultDataSize, null, null,
            null, null)
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
        blockBlobClient.stageBlock(getBlockID(), data.defaultInputStream, data.defaultDataSize) == 201

        then:
        blockBlobClient.listBlocks(BlockListType.ALL).getUncommittedBlocks().size() == 1
    }

    @Unroll
    def "Stage block illegal arguments"() {
        when:
        def blockID = (getBlockId) ? getBlockID() : null
        blockBlobClient.stageBlock(blockID, data == null ? null : stream, dataSize)

        then:
        thrown(exceptionType)

        where:
        getBlockId | stream                  | dataSize                 | exceptionType
        false      | data.defaultInputStream | data.defaultDataSize     | BlobStorageException
        true       | null                    | data.defaultDataSize     | NullPointerException
        true       | data.defaultInputStream | data.defaultDataSize + 1 | UnexpectedLengthException
        true       | data.defaultInputStream | data.defaultDataSize - 1 | UnexpectedLengthException
    }

    def "Stage block empty body"() {
        when:
        blockBlobClient.stageBlock(getBlockID(), new ByteArrayInputStream(new byte[0]), 0)

        then:
        thrown(BlobStorageException)
    }

    def "Stage block transactionalMD5"() {
        setup:
        byte[] md5 = MessageDigest.getInstance("MD5").digest(data.defaultBytes)

        expect:
        blockBlobClient.stageBlockWithResponse(getBlockID(), data.defaultInputStream, data.defaultDataSize, md5, null, null, null)
            .statusCode == 201
    }

    def "Stage block transactionalMD5 fail"() {
        when:
        blockBlobClient.stageBlockWithResponse(getBlockID(), data.defaultInputStream, data.defaultDataSize,
            MessageDigest.getInstance("MD5").digest("garbage".getBytes()), null, null, null)

        then:
        def e = thrown(BlobStorageException)
        e.getErrorCode() == BlobErrorCode.MD5MISMATCH
    }

    def "Stage block null body"() {
        when:
        blockBlobClient.stageBlock(getBlockID(), null, 0)

        then:
        thrown(NullPointerException)
    }

    def "Stage block lease"() {
        setup:
        def leaseID = setupBlobLeaseCondition(blockBlobClient, receivedLeaseID)

        expect:
        blockBlobClient.stageBlockWithResponse(getBlockID(), data.defaultInputStream, data.defaultDataSize, null, leaseID, null, null)
            .getStatusCode() == 201
    }

    def "Stage block lease fail"() {
        setup:
        setupBlobLeaseCondition(blockBlobClient, receivedLeaseID)

        when:
        blockBlobClient.stageBlockWithResponse(getBlockID(), data.defaultInputStream, data.defaultDataSize, null, garbageLeaseID, null,
            null)

        then:
        def e = thrown(BlobStorageException)
        e.getErrorCode() == BlobErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION
    }

    def "Stage block error"() {
        setup:
        blockBlobClient = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        blockBlobClient.stageBlock("id", data.defaultInputStream, data.defaultDataSize)

        then:
        thrown(BlobStorageException)
    }

    def "Stage block retry on transient failure"() {
        setup:
        def clientWithFailure = getBlobClient(
            environment.primaryAccount.credential,
            blobClient.getBlobUrl(),
            new TransientFailureInjectingHttpPipelinePolicy()
        ).getBlockBlobClient()

        when:
        def data = getRandomByteArray(10)
        def blockId = getBlockID()
        clientWithFailure.stageBlock(blockId, new ByteArrayInputStream(data), data.size())
        blobClient.getBlockBlobClient().commitBlockList([blockId] as List<String>, true)

        then:
        def os = new ByteArrayOutputStream()
        blobClient.download(os)
        os.toByteArray() == data
    }

    def "Stage block from url"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        def blockID = getBlockID()

        when:
        def headers = bu2.stageBlockFromUrlWithResponse(blockID, blockBlobClient.getBlobUrl(), null, null, null, null, null, null).getHeaders()

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
        ByteBuffer.wrap(outputStream.toByteArray()) == data.defaultData
    }

    def "Stage block from url min"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        def blockID = getBlockID()

        expect:
        bu2.stageBlockFromUrlWithResponse(blockID, blockBlobClient.getBlobUrl(), null, null, null, null, null, null).getStatusCode() == 201
    }

    @Unroll
    def "Stage block from URL IA"() {
        when:
        def blockID = (getBlockId) ? getBlockID() : null
        blockBlobClient.stageBlockFromUrl(blockID, sourceURL, null)

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
        destURL.stageBlockFromUrl(getBlockID(), blockBlobClient.getBlobUrl(), new BlobRange(2, 3))
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
        destURL.stageBlockFromUrlWithResponse(getBlockID(), blockBlobClient.getBlobUrl(), null,
            MessageDigest.getInstance("MD5").digest(data.defaultBytes), null, null, null, null)

        then:
        notThrown(BlobStorageException)
    }

    def "Stage block from URL MD5 fail"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def destURL = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        destURL.stageBlockFromUrlWithResponse(getBlockID(), blockBlobClient.getBlobUrl(), null, "garbage".getBytes(),
            null, null, null, null)

        then:
        thrown(BlobStorageException)
    }

    def "Stage block from URL lease"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)

        when:
        blockBlobClient.stageBlockFromUrlWithResponse(getBlockID(), blockBlobClient.getBlobUrl(), null, null, setupBlobLeaseCondition(blockBlobClient, receivedLeaseID), null, null, null)

        then:
        notThrown(BlobStorageException)
    }

    def "Stage block from URL lease fail"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)

        when:
        blockBlobClient.stageBlockFromUrlWithResponse(getBlockID(), blockBlobClient.getBlobUrl(), null, null, "garbage", null, null, null)

        then:
        thrown(BlobStorageException)
    }

    def "Stage block from URL error"() {
        setup:
        blockBlobClient = primaryBlobServiceClient.getBlobContainerClient(generateContainerName())
            .getBlobClient(generateBlobName())
            .getBlockBlobClient()

        when:
        blockBlobClient.stageBlockFromUrl(getBlockID(), blockBlobClient.getBlobUrl(), null)

        then:
        thrown(BlobStorageException)
    }

    @Unroll
    def "Stage block from URL source AC"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def blockID = getBlockID()

        def sourceURL = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        sourceURL.upload(data.defaultInputStream, data.defaultDataSize)

        sourceIfMatch = setupBlobMatchCondition(sourceURL, sourceIfMatch)
        def smac = new BlobRequestConditions()
            .setIfModifiedSince(sourceIfModifiedSince)
            .setIfUnmodifiedSince(sourceIfUnmodifiedSince)
            .setIfMatch(sourceIfMatch)
            .setIfNoneMatch(sourceIfNoneMatch)

        expect:
        blockBlobClient.stageBlockFromUrlWithResponse(blockID, sourceURL.getBlobUrl(), null, null, null, smac, null, null).getStatusCode() == 201

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
        sourceURL.upload(data.defaultInputStream, data.defaultDataSize)

        def smac = new BlobRequestConditions()
            .setIfModifiedSince(sourceIfModifiedSince)
            .setIfUnmodifiedSince(sourceIfUnmodifiedSince)
            .setIfMatch(sourceIfMatch)
            .setIfNoneMatch(setupBlobMatchCondition(sourceURL, sourceIfNoneMatch))

        when:
        blockBlobClient.stageBlockFromUrlWithResponse(blockID, sourceURL.getBlobUrl(), null, null, null, smac, null, null).getStatusCode() == 201

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
        blockBlobClient.stageBlock(blockID, data.defaultInputStream, data.defaultDataSize)
        def ids = [blockID] as List

        when:
        def response = blockBlobClient.commitBlockListWithResponse(ids, null, null, null, null, null, null)
        def headers = response.getHeaders()

        then:
        response.getStatusCode() == 201
        validateBasicHeaders(headers)
        headers.getValue("x-ms-content-crc64")
        Boolean.parseBoolean(headers.getValue("x-ms-request-server-encrypted"))
    }

    def "Commit block list min"() {
        setup:
        blockBlobClient = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        def blockID = getBlockID()
        blockBlobClient.stageBlock(blockID, data.defaultInputStream, data.defaultDataSize)
        def ids = [blockID] as List

        expect:
        blockBlobClient.commitBlockList(ids) != null
    }

    def "Commit block list min no overwrite"() {
        when:
        blockBlobClient.commitBlockList([])

        then:
        def e = thrown(BlobStorageException)
        e.getErrorCode() == BlobErrorCode.BLOB_ALREADY_EXISTS
    }

    def "Commit block list overwrite"() {
        when:
        blockBlobClient.commitBlockList([], true)

        then:
        notThrown(BlobStorageException)
    }

    def "Commit block list null"() {
        expect:
        blockBlobClient.commitBlockListWithResponse(null, null, null, null, null, null, null).getStatusCode() == 201
    }

    @Unroll
    def "Commit block list headers"() {
        setup:
        def blockID = getBlockID()
        blockBlobClient.stageBlock(blockID, data.defaultInputStream, data.defaultDataSize)
        def ids = [blockID] as List
        def headers = new BlobHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType)

        when:
        blockBlobClient.commitBlockListWithResponse(ids, headers, null, null, null, null, null)
        def response = blockBlobClient.getPropertiesWithResponse(null, null, null)

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType

        then:
        validateBlobProperties(response, cacheControl, contentDisposition, contentEncoding, contentLanguage, contentMD5, contentType)

        where:
        cacheControl | contentDisposition | contentEncoding | contentLanguage | contentMD5                                                 | contentType
        null         | null               | null            | null            | null                                                       | null
        "control"    | "disposition"      | "encoding"      | "language"      | MessageDigest.getInstance("MD5").digest(data.defaultBytes) | "type"
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
        blockBlobClient.commitBlockListWithResponse(null, null, metadata, null, null, null, null)
        def response = blockBlobClient.getPropertiesWithResponse(null, null, null)

        then:
        response.getStatusCode() == 200
        response.getValue().getMetadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "Commit block list tags"() {
        setup:
        def tags = new HashMap<String, String>()
        if (key1 != null) {
            tags.put(key1, value1)
        }
        if (key2 != null) {
            tags.put(key2, value2)
        }

        when:
        blockBlobClient.commitBlockListWithResponse(new BlockBlobCommitBlockListOptions(null).setTags(tags), null, null)
        def response = blockBlobClient.getTagsWithResponse(new BlobGetTagsOptions(), null, null)

        then:
        response.getStatusCode() == 200
        response.getValue() == tags

        where:
        key1                | value1     | key2   | value2
        null                | null       | null   | null
        "foo"               | "bar"      | "fizz" | "buzz"
        " +-./:=_  +-./:=_" | " +-./:=_" | null   | null
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "Commit block list AC"() {
        setup:
        def t = new HashMap<String, String>()
        t.put("foo", "bar")
        blockBlobClient.setTags(t)
        match = setupBlobMatchCondition(blockBlobClient, match)
        leaseID = setupBlobLeaseCondition(blockBlobClient, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags)

        expect:
        blockBlobClient.commitBlockListWithResponse(null, null, null, null, bac, null, null).getStatusCode() == 201

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
    def "Commit block list AC fail"() {
        setup:
        noneMatch = setupBlobMatchCondition(blockBlobClient, noneMatch)
        setupBlobLeaseCondition(blockBlobClient, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags)

        when:
        blockBlobClient.commitBlockListWithResponse(null, null, null, null, bac, null, null)
        then:
        def e = thrown(BlobStorageException)
        e.getErrorCode() == BlobErrorCode.CONDITION_NOT_MET ||
            e.getErrorCode() == BlobErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION

        where:
        modified | unmodified | match       | noneMatch    | leaseID        | tags
        newDate  | null       | null        | null         | null           | null
        null     | oldDate    | null        | null         | null           | null
        null     | null       | garbageEtag | null         | null           | null
        null     | null       | null        | receivedEtag | null           | null
        null     | null       | null        | null         | garbageLeaseID | null
        null     | null       | null        | null         | null           | "\"notfoo\" = 'notbar'"
    }

    def "Commit block list error"() {
        setup:
        blockBlobClient = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        blockBlobClient.commitBlockListWithResponse(new ArrayList<String>(), null, null, null, new BlobRequestConditions().setLeaseId("garbage"), null, null)

        then:
        thrown(BlobStorageException)
    }

    def "Get block list"() {
        setup:
        def committedBlocks = [getBlockID(), getBlockID()]
        blockBlobClient.stageBlock(committedBlocks.get(0), data.defaultInputStream, data.defaultDataSize)
        blockBlobClient.stageBlock(committedBlocks.get(1), data.defaultInputStream, data.defaultDataSize)
        blockBlobClient.commitBlockList(committedBlocks, true)

        def uncommittedBlocks = [getBlockID(), getBlockID()]
        blockBlobClient.stageBlock(uncommittedBlocks.get(0), data.defaultInputStream, data.defaultDataSize)
        blockBlobClient.stageBlock(uncommittedBlocks.get(1), data.defaultInputStream, data.defaultDataSize)
        uncommittedBlocks.sort(true)

        when:
        def blockList = blockBlobClient.listBlocks(BlockListType.ALL)

        then:
        blockList.getCommittedBlocks().collect { it.getName() } as Set == committedBlocks as Set
        blockList.getUncommittedBlocks().collect { it.getName() } as Set == uncommittedBlocks as Set

        (blockList.getCommittedBlocks() + blockList.getUncommittedBlocks())
            .each { assert it.getSizeLong() == data.defaultDataSize }
    }

    def "Get block list min"() {
        when:
        blockBlobClient.listBlocks(BlockListType.ALL)

        then:
        notThrown(BlobStorageException)
    }

    @Unroll
    def "Get block list type"() {
        setup:
        def blockID = getBlockID()
        blockBlobClient.stageBlock(blockID, data.defaultInputStream, data.defaultDataSize)
        blockBlobClient.commitBlockList([blockID], true)
        blockBlobClient.stageBlock(getBlockID(), data.defaultInputStream, data.defaultDataSize)

        when:
        def response = blockBlobClient.listBlocks(type)

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
        blockBlobClient.listBlocks(null).iterator().hasNext()

        then:
        notThrown(IllegalArgumentException)
    }

    def "Get block list lease"() {
        setup:
        def leaseID = setupBlobLeaseCondition(blockBlobClient, receivedLeaseID)

        when:
        blockBlobClient.listBlocksWithResponse(BlockListType.ALL, leaseID, null, Context.NONE)

        then:
        notThrown(BlobStorageException)
    }

    def "Get block list lease fail"() {
        setup:
        setupBlobLeaseCondition(blockBlobClient, garbageLeaseID)

        when:
        blockBlobClient.listBlocksWithResponse(BlockListType.ALL, garbageLeaseID, null, Context.NONE)

        then:
        def e = thrown(BlobStorageException)
        e.getErrorCode() == BlobErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    def "Get block list tags"() {
        setup:
        def t = new HashMap<String, String>()
        t.put("foo", "bar")
        blockBlobClient.setTags(t)

        when:
        blockBlobClient.listBlocksWithResponse(new BlockBlobListBlocksOptions(BlockListType.ALL).setIfTagsMatch("\"foo\" = 'bar'"), null, Context.NONE)

        then:
        notThrown(BlobStorageException)
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    def "Get block list tags fail"() {
        when:
        blockBlobClient.listBlocksWithResponse(new BlockBlobListBlocksOptions(BlockListType.ALL).setIfTagsMatch("\"notfoo\" = 'notbar'"), null, Context.NONE)

        then:
        def e = thrown(BlobStorageException)
        e.getErrorCode() == BlobErrorCode.CONDITION_NOT_MET
    }

    def "Get block list error"() {
        setup:
        blockBlobClient = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        blockBlobClient.listBlocks(BlockListType.ALL).iterator().hasNext()

        then:
        thrown(BlobStorageException)
    }

    def "Upload"() {
        when:
        def response = blockBlobClient.uploadWithResponse(data.defaultInputStream, data.defaultDataSize, null, null, null, null, null,
            null, null)

        then:
        response.getStatusCode() == 201
        def outStream = new ByteArrayOutputStream()
        blockBlobClient.download(outStream)
        outStream.toByteArray() == data.defaultText.getBytes(StandardCharsets.UTF_8)
        validateBasicHeaders(response.getHeaders())
        response.getHeaders().getValue("Content-MD5") != null
        Boolean.parseBoolean(response.getHeaders().getValue("x-ms-request-server-encrypted"))
    }

    /* Upload From File Tests: Need to run on liveMode only since blockBlob wil generate a `UUID.randomUUID()`
       for getBlockID that will change every time test is run
     */
    @LiveOnly
    @Unroll
    def "Upload from file"() {
        setup:
        def file = getRandomFile(fileSize)

        when:
        // Block length will be ignored for single shot.
        StepVerifier.create(blobAsyncClient.uploadFromFile(file.getPath(), new ParallelTransferOptions().setBlockSizeLong(blockSize),
            null, null, null, null))
            .verifyComplete()

        then:
        def outFile = new File(file.getPath().toString() + "result")
        outFile.createNewFile()

        def outStream = new FileOutputStream(outFile)
        outStream.write(FluxUtil.collectBytesInByteBufferStream(blobAsyncClient.download()).block())
        outStream.close()

        compareFiles(file, outFile, 0, fileSize)
        StepVerifier.create(blobAsyncClient.getBlockBlobAsyncClient().listBlocks(BlockListType.COMMITTED))
            .assertNext({ assert it.getCommittedBlocks().size() == commitedBlockCount })
            .verifyComplete()

        cleanup:
        outFile.delete()
        file.delete()

        where:
        fileSize                                       | blockSize       || commitedBlockCount
        0                                              | null            || 0  // Size is too small to trigger stage block uploading
        10                                             | null            || 0  // Size is too small to trigger stage block uploading
        10 * Constants.KB                              | null            || 0  // Size is too small to trigger stage block uploading
        50 * Constants.MB                              | null            || 0  // Size is too small to trigger stage block uploading
        BlockBlobAsyncClient.MAX_UPLOAD_BLOB_BYTES + 1 | null            || Math.ceil((BlockBlobClient.MAX_UPLOAD_BLOB_BYTES + 1) / BlobAsyncClient.BLOB_DEFAULT_HTBB_UPLOAD_BLOCK_SIZE) // HTBB optimizations should trigger when file size is >100MB and defaults are used.
        101 * Constants.MB                             | 4 * 1024 * 1024 || 0  // Size is too small to trigger stage block uploading
    }

    @LiveOnly
    def "Upload from file with metadata"() {
        given:
        def metadata = Collections.singletonMap("metadata", "value")
        def file = getRandomFile(Constants.KB)
        def outStream = new ByteArrayOutputStream()

        when:
        blobClient.uploadFromFile(file.getAbsolutePath(), null, null, metadata, null, null, null)

        then:
        metadata == blockBlobClient.getProperties().getMetadata()
        blockBlobClient.download(outStream)
        outStream.toByteArray() == Files.readAllBytes(file.toPath())

        cleanup:
        file.delete()
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @LiveOnly
    def "Upload from file with tags"() {
        given:
        def tags = Collections.singletonMap(namer.getRandomName(20), namer.getRandomName(20))
        def file = getRandomFile(Constants.KB)
        def outStream = new ByteArrayOutputStream()

        when:
        blobClient.uploadFromFileWithResponse(new BlobUploadFromFileOptions(file.getAbsolutePath()).setTags(tags), null,
            null)

        then:
        tags == blockBlobClient.getTags()
        blockBlobClient.download(outStream)
        outStream.toByteArray() == Files.readAllBytes(file.toPath())

        cleanup:
        file.delete()
    }

    @LiveOnly
    def "Upload from file default no overwrite"() {
        when:
        def file = getRandomFile(50)
        blobClient.uploadFromFile(file.toPath().toString())

        then:
        def e = thrown(BlobStorageException)
        e.getErrorCode() == BlobErrorCode.BLOB_ALREADY_EXISTS

        and:
        def uploadVerifier = StepVerifier.create(blobAsyncClient.uploadFromFile(getRandomFile(50).toPath().toString()))

        then:
        uploadVerifier.verifyError(BlobStorageException)

        cleanup:
        file.delete()
    }

    @LiveOnly
    def "Upload from file overwrite"() {
        when:
        def file = getRandomFile(50)
        blobClient.uploadFromFile(file.toPath().toString(), true)

        then:
        notThrown(BlobStorageException)

        and:
        def uploadVerifier = StepVerifier.create(blobAsyncClient.uploadFromFile(getRandomFile(50).toPath().toString(), true))

        then:
        uploadVerifier.verifyComplete()

        cleanup:
        file.delete()
    }

    /*
     * Reports the number of bytes sent when uploading a file. This is different than other reporters which track the
     * number of reportings as upload from file hooks into the loading data from disk data stream which is a hard-coded
     * read size.
     */

    class FileUploadReporter implements ProgressReceiver {
        private long reportedByteCount

        @Override
        void reportProgress(long bytesTransferred) {
            this.reportedByteCount = bytesTransferred
        }

        long getReportedByteCount() {
            return this.reportedByteCount
        }
    }

    @Unroll
    @LiveOnly
    def "Upload from file reporter"() {
        when:
        def uploadReporter = new FileUploadReporter()
        def file = getRandomFile(size)

        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxConcurrency(bufferCount)
            .setProgressReceiver(uploadReporter).setMaxSingleUploadSizeLong(blockSize - 1)

        then:
        StepVerifier.create(blobAsyncClient.uploadFromFile(file.toPath().toString(), parallelTransferOptions,
            null, null, null, null))
            .verifyComplete()

        uploadReporter.getReportedByteCount() == size

        cleanup:
        file.delete()

        where:
        size              | blockSize         | bufferCount
        10 * Constants.MB | 10 * Constants.MB | 8
        20 * Constants.MB | 1 * Constants.MB  | 5
        10 * Constants.MB | 5 * Constants.MB  | 2
        10 * Constants.MB | 10 * Constants.KB | 100
        100               | 1 * Constants.MB  | 2
    }

    @Unroll
    @LiveOnly
    def "Upload from file options"() {
        setup:
        def file = getRandomFile(dataSize)

        when:
        blobClient.uploadFromFile(file.toPath().toString(),
            new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(singleUploadSize), null, null, null, null, null)

        then:
        blobClient.getBlockBlobClient()
            .listBlocks(BlockListType.COMMITTED).getCommittedBlocks().size() == expectedBlockCount


        cleanup:
        file.delete()

        where:
        dataSize                                       | singleUploadSize | blockSize || expectedBlockCount
        BlockBlobAsyncClient.MAX_UPLOAD_BLOB_BYTES - 1 | null             | null      || 0 // Test that the default for singleUploadSize is the maximum
        BlockBlobAsyncClient.MAX_UPLOAD_BLOB_BYTES + 1 | null             | null      || Math.ceil(((double) BlockBlobAsyncClient.MAX_UPLOAD_BLOB_BYTES + 1) / (double) BlobClient.BLOB_DEFAULT_HTBB_UPLOAD_BLOCK_SIZE) // "". This also validates the default for blockSize
        100                                            | 50               | null      || 1 // Test that singleUploadSize is respected
        100                                            | 50               | 20        || 5 // Test that blockSize is respected
    }

    def "Upload min"() {
        when:
        blockBlobClient.upload(data.defaultInputStream, data.defaultDataSize, true)

        then:
        def outStream = new ByteArrayOutputStream()
        blockBlobClient.download(outStream)
        outStream.toByteArray() == data.defaultText.getBytes(StandardCharsets.UTF_8)
    }

    @Unroll
    def "Upload illegal argument"() {
        when:
        blockBlobClient.upload(stream, dataSize)

        then:
        thrown(exceptionType)

        where:
        stream                   | dataSize                 | exceptionType
        null                     | data.defaultDataSize     | NullPointerException
        data.defaultInputStream | data.defaultDataSize + 1 | UnexpectedLengthException
        data.defaultInputStream | data.defaultDataSize - 1 | UnexpectedLengthException
    }

    def "Upload empty body"() {
        expect:
        blockBlobClient.uploadWithResponse(new ByteArrayInputStream(new byte[0]), 0, null, null, null, null, null, null, null)
            .getStatusCode() == 201
    }

    def "Upload null body"() {
        when:
        blockBlobClient.uploadWithResponse(null, 0, null, null, null, null, null, null, null)

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
        blockBlobClient.uploadWithResponse(data.defaultInputStream, data.defaultDataSize, headers, null, null, null, null, null, null)
        def response = blockBlobClient.getPropertiesWithResponse(null, null, null)

        // If the value isn't set the service will automatically set it
        contentMD5 = (contentMD5 == null) ? MessageDigest.getInstance("MD5").digest(data.defaultBytes) : contentMD5
        contentType = (contentType == null) ? "application/octet-stream" : contentType

        then:
        validateBlobProperties(response, cacheControl, contentDisposition, contentEncoding, contentLanguage, contentMD5, contentType)

        where:
        cacheControl | contentDisposition | contentEncoding | contentLanguage | contentMD5                                                        | contentType
        null         | null               | null            | null            | null                                                              | null
        "control"    | "disposition"      | "encoding"      | "language"      | MessageDigest.getInstance("MD5").digest(data.defaultBytes) | "type"
    }

    def "Upload transactionalMD5"() {
        setup:
        byte[] md5 = MessageDigest.getInstance("MD5").digest(data.defaultBytes)

        expect:
        blockBlobClient.uploadWithResponse(data.defaultInputStream, data.defaultDataSize, null, null, null, md5, null, null, null)
            .statusCode == 201
    }

    def "Upload transactionalMD5 fail"() {
        when:
        blockBlobClient.stageBlockWithResponse(getBlockID(), data.defaultInputStream, data.defaultDataSize,
            MessageDigest.getInstance("MD5").digest("garbage".getBytes()), null, null, null)

        then:
        def e = thrown(BlobStorageException)
        e.getErrorCode() == BlobErrorCode.MD5MISMATCH
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
        blockBlobClient.uploadWithResponse(data.defaultInputStream, data.defaultDataSize, null, metadata, null, null, null, null, null)
        def response = blockBlobClient.getPropertiesWithResponse(null, null, null)

        then:
        response.getStatusCode() == 200
        response.getValue().getMetadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "Upload tags"() {
        setup:
        def tags = new HashMap<String, String>()
        if (key1 != null) {
            tags.put(key1, value1)
        }
        if (key2 != null) {
            tags.put(key2, value2)
        }

        when:
        blockBlobClient.uploadWithResponse(new BlockBlobSimpleUploadOptions(data.defaultInputStream, data.defaultDataSize)
            .setTags(tags), null, null)
        def response = blockBlobClient.getTagsWithResponse(new BlobGetTagsOptions(), null, null)

        then:
        response.getStatusCode() == 200
        response.getValue() == tags

        where:
        key1                | value1     | key2   | value2
        null                | null       | null   | null
        "foo"               | "bar"      | "fizz" | "buzz"
        " +-./:=_  +-./:=_" | " +-./:=_" | null   | null
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "Upload AC"() {
        setup:
        def t = new HashMap<String, String>()
        t.put("foo", "bar")
        blockBlobClient.setTags(t)
        match = setupBlobMatchCondition(blockBlobClient, match)
        leaseID = setupBlobLeaseCondition(blockBlobClient, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags)

        expect:
        blockBlobClient.uploadWithResponse(data.defaultInputStream, data.defaultDataSize, null, null, null, null, bac, null, null).getStatusCode() == 201

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
    def "Upload AC fail"() {
        setup:
        noneMatch = setupBlobMatchCondition(blockBlobClient, noneMatch)
        setupBlobLeaseCondition(blockBlobClient, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags)

        when:
        blockBlobClient.uploadWithResponse(data.defaultInputStream, data.defaultDataSize, null, null, null, null, bac, null, null)

        then:
        def e = thrown(BlobStorageException)
        e.getErrorCode() == BlobErrorCode.CONDITION_NOT_MET ||
            e.getErrorCode() == BlobErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION

        where:
        modified | unmodified | match       | noneMatch    | leaseID        | tags
        newDate  | null       | null        | null         | null           | null
        null     | oldDate    | null        | null         | null           | null
        null     | null       | garbageEtag | null         | null           | null
        null     | null       | null        | receivedEtag | null           | null
        null     | null       | null        | null         | garbageLeaseID | null
        null     | null       | null        | null         | null           | "\"notfoo\" = 'notbar'"
    }

    def "Upload error"() {
        setup:
        blockBlobClient = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        blockBlobClient.uploadWithResponse(data.defaultInputStream, data.defaultDataSize, null, null, null, null,
            new BlobRequestConditions().setLeaseId("id"), null, null)

        then:
        thrown(BlobStorageException)
    }

    def "Upload with tier"() {
        setup:
        def bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        bc.uploadWithResponse(data.defaultInputStream, data.defaultDataSize, null, null, AccessTier.COOL, null, null, null,
            null)

        then:
        bc.getProperties().getAccessTier() == AccessTier.COOL
    }

    def "Upload overwrite false"() {
        when:
        blockBlobClient.upload(data.defaultInputStream, data.defaultDataSize)

        then:
        thrown(BlobStorageException)
    }

    def "Upload overwrite true"() {
        when:
        blockBlobClient.upload(data.defaultInputStream, data.defaultDataSize, true)

        then:
        notThrown(Throwable)
    }

    def "Upload retry on transient failure"() {
        setup:
        def clientWithFailure = getBlobClient(
            environment.primaryAccount.credential,
            blobClient.getBlobUrl(),
            new TransientFailureInjectingHttpPipelinePolicy()
        ).getBlockBlobClient()

        when:
        def data = getRandomByteArray(10)
        clientWithFailure.upload(new ByteArrayInputStream(data), data.size(), true)

        then:
        def os = new ByteArrayOutputStream()
        blobClient.download(os)
        os.toByteArray() == data
    }

    @LiveOnly
    def "Async buffered upload empty"() {

        expect:
        StepVerifier.create(blobAsyncClient.upload(Flux.just(ByteBuffer.wrap(new byte[0])), null, true))
            .assertNext({ assert it.getETag() != null })
            .verifyComplete()

        StepVerifier.create(blobAsyncClient.download())
            .assertNext({ assert it.remaining() == 0 })
            .verifyComplete()
    }

    @Unroll
    @LiveOnly
    def "Async buffered upload empty buffers"() {
        expect:
        StepVerifier.create(blobAsyncClient.upload(Flux.fromIterable([buffer1, buffer2, buffer3]), null, true))
            .assertNext({ assert it.getETag() != null })
            .verifyComplete()

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(blobAsyncClient.download()))
            .assertNext({ assert it == expectedDownload })
            .verifyComplete()

        where:
        buffer1                                                   | buffer2                                               | buffer3                                                    || expectedDownload
        ByteBuffer.wrap("Hello".getBytes(StandardCharsets.UTF_8)) | ByteBuffer.wrap(" ".getBytes(StandardCharsets.UTF_8)) | ByteBuffer.wrap("world!".getBytes(StandardCharsets.UTF_8)) || "Hello world!".getBytes(StandardCharsets.UTF_8)
        ByteBuffer.wrap("Hello".getBytes(StandardCharsets.UTF_8)) | ByteBuffer.wrap(" ".getBytes(StandardCharsets.UTF_8)) | ByteBuffer.wrap(new byte[0])                               || "Hello ".getBytes(StandardCharsets.UTF_8)
        ByteBuffer.wrap("Hello".getBytes(StandardCharsets.UTF_8)) | ByteBuffer.wrap(new byte[0])                          | ByteBuffer.wrap("world!".getBytes(StandardCharsets.UTF_8)) || "Helloworld!".getBytes(StandardCharsets.UTF_8)
        ByteBuffer.wrap(new byte[0])                              | ByteBuffer.wrap(" ".getBytes(StandardCharsets.UTF_8)) | ByteBuffer.wrap("world!".getBytes(StandardCharsets.UTF_8)) || " world!".getBytes(StandardCharsets.UTF_8)
    }

    // Only run these tests in live mode as they use variables that can't be captured.
    @Unroll
    @LiveOnly
        def "Async buffered upload"() {
            setup:
            def blobAsyncClient = getPrimaryServiceClientForWrites(bufferSize)
                .getBlobContainerAsyncClient(blobAsyncClient.getContainerName())
                .getBlobAsyncClient(blobAsyncClient.getBlobName())

            when:
            def data = getRandomData(dataSize)
            ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(bufferSize).setMaxConcurrency(numBuffs).setMaxSingleUploadSizeLong(4 * Constants.MB)
            blobAsyncClient.upload(Flux.just(data), parallelTransferOptions, true).block()
            data.position(0)

            then:
            // Due to memory issues, this check only runs on small to medium sized data sets.
            if (dataSize < 100 * 1024 * 1024) {
            StepVerifier.create(collectBytesInBuffer(blockBlobAsyncClient.download()))
                .assertNext({ assert it == data })
                .verifyComplete()
        }

        StepVerifier.create(blockBlobAsyncClient.listBlocks(BlockListType.ALL))
            .assertNext({ assert it.getCommittedBlocks().size() == blockCount })
            .verifyComplete()

        where:
        dataSize           | bufferSize        | numBuffs || blockCount
        35 * Constants.MB  | 5 * Constants.MB  | 2        || 7 // Requires cycling through the same buffers multiple times.
        35 * Constants.MB  | 5 * Constants.MB  | 5        || 7 // Most buffers may only be used once.
        100 * Constants.MB | 10 * Constants.MB | 2        || 10 // Larger data set.
        100 * Constants.MB | 10 * Constants.MB | 5        || 10 // Larger number of Buffs.
        10 * Constants.MB  | 1 * Constants.MB  | 10       || 10 // Exactly enough buffer space to hold all the data.
        50 * Constants.MB  | 10 * Constants.MB | 2        || 5 // Larger data.
        10 * Constants.MB  | 2 * Constants.MB  | 4        || 5
        10 * Constants.MB  | 3 * Constants.MB  | 3        || 4 // Data does not squarely fit in buffers.
    }

    def "Async upload binary data"() {
        when:
        blobAsyncClient.upload(data.defaultBinaryData, true).block()

        then:
        StepVerifier.create(blockBlobAsyncClient.downloadContent())
                .assertNext({ assert it.toBytes() == data.defaultBinaryData.toBytes() })
                .verifyComplete()
    }

    @Unroll
    @LiveOnly
    def "Async buffered upload computeMd5"() {
        setup:
        def byteBufferList = []
        for (def i = 0; i < byteBufferCount; i++) {
            byteBufferList.add(getRandomData(size))
        }
        Flux<ByteBuffer> flux = Flux.fromIterable(byteBufferList)
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setMaxSingleUploadSizeLong(maxSingleUploadSize)
            .setBlockSizeLong(blockSize)

        expect:
        blobAsyncClient.uploadWithResponse(new BlobParallelUploadOptions(flux).setParallelTransferOptions(parallelTransferOptions).setComputeMd5(true)).block().getStatusCode() == 201

        where:
        size         | maxSingleUploadSize | blockSize          | byteBufferCount
        Constants.KB | null                | null               | 1                  // Simple case where uploadFull is called.
        Constants.KB | Constants.KB        | 500 * Constants.KB | 1000               // uploadChunked 2 blocks staged
        Constants.KB | Constants.KB        | 5 * Constants.KB   | 1000               // uploadChunked 100 blocks staged
    }

    def "Async upload binary data with response"() {
        expect:
        blobAsyncClient.uploadWithResponse(new BlobParallelUploadOptions(data.defaultBinaryData)).block().getStatusCode() == 201
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
    @LiveOnly
    def "Buffered upload with reporter"() {
        setup:
        def blobAsyncClient = getPrimaryServiceClientForWrites(blockSize)
            .getBlobContainerAsyncClient(blobAsyncClient.getContainerName())
            .getBlobAsyncClient(blobAsyncClient.getBlobName())

        when:
        def uploadReporter = new Reporter(blockSize)

        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxConcurrency(bufferCount)
            .setProgressReceiver(uploadReporter).setMaxSingleUploadSizeLong(4 * Constants.MB)

        then:
        StepVerifier.create(blobAsyncClient.uploadWithResponse(Flux.just(getRandomData(size)), parallelTransferOptions,
            null, null, null, null))
            .assertNext({
                assert it.getStatusCode() == 201

                /*
                 * Verify that the reporting count is equal or greater than the size divided by block size in the case
                 * that operations need to be retried. Retry attempts will increment the reporting count.
                 */
                assert uploadReporter.getReportingCount() >= (long) (size / blockSize)
            }).verifyComplete()

        where:
        size              | blockSize          | bufferCount
        10 * Constants.MB | 10 * Constants.MB  | 8
        20 * Constants.MB | 1 * Constants.MB   | 5
        10 * Constants.MB | 5 * Constants.MB   | 2
        10 * Constants.MB | 512 * Constants.KB | 20
    }

    // Only run these tests in live mode as they use variables that can't be captured.
    @Unroll
    @LiveOnly
    def "Buffered upload chunked source"() {
        /*
        This test should validate that the upload should work regardless of what format the passed data is in because
        it will be chunked appropriately.
         */
        setup:
        def blobAsyncClient = getPrimaryServiceClientForWrites(bufferSize * Constants.MB)
            .getBlobContainerAsyncClient(blobAsyncClient.getContainerName())
            .getBlobAsyncClient(blobAsyncClient.getBlobName())
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(bufferSize * Constants.MB).setMaxConcurrency(numBuffers).setMaxSingleUploadSizeLong(4 * Constants.MB)
        def dataList = [] as List<ByteBuffer>
        dataSizeList.each { size -> dataList.add(getRandomData(size * Constants.MB)) }
        def uploadOperation = blobAsyncClient.upload(Flux.fromIterable(dataList), parallelTransferOptions, true)

        expect:
        StepVerifier.create(uploadOperation.then(collectBytesInBuffer(blockBlobAsyncClient.download())))
            .assertNext({ assert compareListToBuffer(dataList, it) })
            .verifyComplete()

        StepVerifier.create(blockBlobAsyncClient.listBlocks(BlockListType.ALL))
            .assertNext({ assert it.getCommittedBlocks().size() == blockCount })
            .verifyComplete()

        where:
        dataSizeList          | bufferSize | numBuffers || blockCount
        [7, 7]                | 10         | 2          || 2 // First item fits entirely in the buffer, next item spans two buffers
        [3, 3, 3, 3, 3, 3, 3] | 10         | 2          || 3 // Multiple items fit non-exactly in one buffer.
        [10, 10]              | 10         | 2          || 2 // Data fits exactly and does not need chunking.
        [50, 51, 49]          | 10         | 2          || 15 // Data needs chunking and does not fit neatly in buffers. Requires waiting for buffers to be released.
        // The case of one large buffer needing to be broken up is tested in the previous test.
    }

    @Unroll
    @LiveOnly
    def "Buffered upload handle pathing"() {
        setup:
        def dataList = [] as List<ByteBuffer>
        dataSizeList.each { size -> dataList.add(getRandomData(size)) }
        def uploadOperation = blobAsyncClient.upload(Flux.fromIterable(dataList),
            new ParallelTransferOptions().setMaxSingleUploadSizeLong(4 * Constants.MB), true)

        expect:
        StepVerifier.create(uploadOperation.then(collectBytesInBuffer(blockBlobAsyncClient.download())))
            .assertNext({ assert compareListToBuffer(dataList, it) })
            .verifyComplete()

        StepVerifier.create(blockBlobAsyncClient.listBlocks(BlockListType.ALL))
            .assertNext({ assert it.getCommittedBlocks().size() == blockCount })
            .verifyComplete()

        where:
        dataSizeList                         | blockCount
        [4 * Constants.MB + 1, 10]           | 2
        [4 * Constants.MB]                   | 0
        [10, 100, 1000, 10000]               | 0
        [4 * Constants.MB, 4 * Constants.MB] | 2
    }

    @Unroll
    @LiveOnly
    def "Buffered upload handle pathing hot flux"() {
        setup:
        def dataList = [] as List<ByteBuffer>
        dataSizeList.each { size -> dataList.add(getRandomData(size)) }
        def uploadOperation = blobAsyncClient.upload(Flux.fromIterable(dataList).publish().autoConnect(),
            new ParallelTransferOptions().setMaxSingleUploadSizeLong(4 * Constants.MB), true)

        expect:
        StepVerifier.create(uploadOperation.then(collectBytesInBuffer(blockBlobAsyncClient.download())))
            .assertNext({ assert compareListToBuffer(dataList, it) })
            .verifyComplete()

        StepVerifier.create(blockBlobAsyncClient.listBlocks(BlockListType.ALL))
            .assertNext({ assert it.getCommittedBlocks().size() == blockCount })
            .verifyComplete()

        where:
        dataSizeList                         | blockCount
        [4 * Constants.MB + 1, 10]           | 2
        [4 * Constants.MB]                   | 0
        [10, 100, 1000, 10000]               | 0
        [4 * Constants.MB, 4 * Constants.MB] | 2
    }

    @Unroll
    @LiveOnly
    def "Buffered upload handle pathing hot flux with transient failure"() {
        setup:
        def clientWithFailure = getBlobAsyncClient(
            environment.primaryAccount.credential,
            blobAsyncClient.getBlobUrl(),
            new TransientFailureInjectingHttpPipelinePolicy()
        )

        when:
        def dataList = [] as List<ByteBuffer>
        dataSizeList.each { size -> dataList.add(getRandomData(size)) }
        def uploadOperation = clientWithFailure.upload(Flux.fromIterable(dataList).publish().autoConnect(),
            new ParallelTransferOptions().setMaxSingleUploadSizeLong(4 * Constants.MB), true)

        then:
        StepVerifier.create(uploadOperation.then(collectBytesInBuffer(blockBlobAsyncClient.download())))
            .assertNext({ assert compareListToBuffer(dataList, it) })
            .verifyComplete()

        StepVerifier.create(blockBlobAsyncClient.listBlocks(BlockListType.ALL))
            .assertNext({ assert it.getCommittedBlocks().size() == blockCount })
            .verifyComplete()

        where:
        dataSizeList                         | blockCount
        [10, 100, 1000, 10000]               | 0
        [4 * Constants.MB + 1, 10]           | 2
        [4 * Constants.MB, 4 * Constants.MB] | 2
    }

    @Unroll
    @LiveOnly
    def "Buffered upload sync handle pathing with transient failure"() {
        /*
        This test ensures that although we no longer mark and reset the source stream for buffered upload, it still
        supports retries in all cases for the sync client.
         */
        setup:
        def clientWithFailure = getBlobClient(
            environment.primaryAccount.credential,
            blobClient.getBlobUrl(),
            new TransientFailureInjectingHttpPipelinePolicy()
        )

        def data = getRandomByteArray(dataSize)
        clientWithFailure.uploadWithResponse(new ByteArrayInputStream(data), dataSize,
            new ParallelTransferOptions().setMaxSingleUploadSizeLong(2 * Constants.MB)
                .setBlockSizeLong(2 * Constants.MB), null, null, null, null, null, null)

        expect:
        def os = new ByteArrayOutputStream(dataSize)
        blobClient.download(os)
        data == os.toByteArray()

        blobClient.getBlockBlobClient().listBlocks(BlockListType.ALL).getCommittedBlocks().size() == blockCount

        where:
        dataSize              | blockCount
        11110                 | 0
        2 * Constants.MB + 11 | 2
    }

    def "Buffered upload illegal arguments null"() {
        expect:
        StepVerifier.create(blobAsyncClient.upload(null, new ParallelTransferOptions().setBlockSizeLong(4).setMaxConcurrency(4), true))
            .verifyErrorSatisfies({ assert it instanceof NullPointerException })
    }

    @Unroll
    def "Buffered upload illegal args out of bounds"() {
        when:
        new ParallelTransferOptions()
            .setBlockSizeLong(bufferSize)
            .setMaxConcurrency(numBuffs)

        then:
        thrown(IllegalArgumentException)

        where:
        bufferSize                                          | numBuffs
        0                                                   | 5
        BlockBlobAsyncClient.MAX_STAGE_BLOCK_BYTES_LONG + 1 | 5
        5                                                   | 0
    }

    // Only run these tests in live mode as they use variables that can't be captured.
    @Unroll
    @LiveOnly
    def "Buffered upload headers"() {
        when:
        def bytes = getRandomByteArray(dataSize)
        def contentMD5 = validateContentMD5 ? MessageDigest.getInstance("MD5").digest(bytes) : null
        def uploadOperation = blobAsyncClient.uploadWithResponse(Flux.just(ByteBuffer.wrap(bytes)), new ParallelTransferOptions().setMaxSingleUploadSizeLong(4 * Constants.MB), new BlobHttpHeaders()
            .setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType),
            null, null, null)

        then:
        StepVerifier.create(uploadOperation.then(blockBlobAsyncClient.getPropertiesWithResponse(null)))
            .assertNext({
                assert validateBlobProperties(it, cacheControl, contentDisposition, contentEncoding, contentLanguage,
                    contentMD5, contentType == null ? "application/octet-stream" : contentType)
            }).verifyComplete()
        // HTTP default content type is application/octet-stream.

        where:
        // Depending on the size of the stream either Put Blob or Put Block List will be used.
        // Put Blob will implicitly calculate the MD5 whereas Put Block List won't.
        dataSize              | cacheControl | contentDisposition | contentEncoding | contentLanguage | validateContentMD5 | contentType
        data.defaultDataSize  | null         | null               | null            | null            | true               | null
        data.defaultDataSize  | "control"    | "disposition"      | "encoding"      | "language"      | true               | "type"
        6 * Constants.MB      | null         | null               | null            | null            | false              | null
        6 * Constants.MB      | "control"    | "disposition"      | "encoding"      | "language"      | true               | "type"
    }

    // Only run these tests in live mode as they use variables that can't be captured.
    @Unroll
    @LiveOnly
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
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(10).setMaxConcurrency(10)
        def uploadOperation = blobAsyncClient.uploadWithResponse(Flux.just(getRandomData(10)),
            parallelTransferOptions, null, metadata, null, null)

        then:
        StepVerifier.create(uploadOperation.then(blobAsyncClient.getPropertiesWithResponse(null)))
            .assertNext({
                assert it.getStatusCode() == 200
                assert it.getValue().getMetadata() == metadata
            }).verifyComplete()

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    // Only run these tests in live mode as they use variables that can't be captured.
    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    @LiveOnly
    def "Buffered upload tags"() {
        setup:
        def tags = new HashMap<String, String>()
        if (key1 != null) {
            tags.put(key1, value1)
        }
        if (key2 != null) {
            tags.put(key2, value2)
        }

        when:
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions(10, 10, null)
        def uploadOperation = blobAsyncClient.uploadWithResponse(
            new BlobParallelUploadOptions(Flux.just(getRandomData(10)))
                .setParallelTransferOptions(parallelTransferOptions).setTags(tags))

        then:
        StepVerifier.create(uploadOperation.then(blobAsyncClient.getTagsWithResponse(null)))
            .assertNext({
                assert it.getStatusCode() == 200
                assert it.getValue() == tags
            }).verifyComplete()

        where:
        key1                | value1     | key2   | value2
        null                | null       | null   | null
        "foo"               | "bar"      | "fizz" | "buzz"
        " +-./:=_  +-./:=_" | " +-./:=_" | null   | null
    }

    @Unroll
    @LiveOnly
    def "Buffered upload options"() {
        setup:
        def data = getRandomData(dataSize)

        when:
        blobAsyncClient.uploadWithResponse(Flux.just(data),
            new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(singleUploadSize), null, null, null, null).block()

        then:
        blobAsyncClient.getBlockBlobAsyncClient()
            .listBlocks(BlockListType.COMMITTED).block().getCommittedBlocks().size() == expectedBlockCount

        where:
        dataSize                                       | singleUploadSize | blockSize || expectedBlockCount
        BlockBlobAsyncClient.MAX_UPLOAD_BLOB_BYTES - 1 | null             | null      || 0 // Test that the default for singleUploadSize is the maximum
        BlockBlobAsyncClient.MAX_UPLOAD_BLOB_BYTES + 1 | null             | null      || Math.ceil(((double) BlockBlobAsyncClient.MAX_UPLOAD_BLOB_BYTES + 1) / (double) BlobClient.BLOB_DEFAULT_UPLOAD_BLOCK_SIZE) // "". This also validates the default for blockSize
        100                                            | 50               | null      || 1 // Test that singleUploadSize is respected
        100                                            | 50               | 20        || 5 // Test that blockSize is respected
    }

    // Only run these tests in live mode as they use variables that can't be captured.
    @Unroll
    @LiveOnly
    def "Buffered upload AC"() {
        setup:
        blockBlobAsyncClient.upload(data.defaultFlux, data.defaultDataSize, true).block()
        match = setupBlobMatchCondition(blockBlobAsyncClient, match)
        leaseID = setupBlobLeaseCondition(blockBlobAsyncClient, leaseID)
        def requestConditions = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(10)
        StepVerifier.create(blobAsyncClient.uploadWithResponse(Flux.just(getRandomData(10)), parallelTransferOptions,
            null, null, null, requestConditions))
            .assertNext({ assert it.getStatusCode() == 201 })
            .verifyComplete()

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
    @LiveOnly
    def "Buffered upload AC fail"() {
        setup:
        blockBlobAsyncClient.upload(data.defaultFlux, data.defaultDataSize, true).block()
        noneMatch = setupBlobMatchCondition(blockBlobAsyncClient, noneMatch)
        leaseID = setupBlobLeaseCondition(blockBlobAsyncClient, leaseID)
        def requestConditions = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
        def parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(10)

        expect:
        StepVerifier.create(blobAsyncClient.uploadWithResponse(Flux.just(getRandomData(10)), parallelTransferOptions, null, null, null, requestConditions))
            .verifyErrorSatisfies({
                assert it instanceof BlobStorageException
                def storageException = (BlobStorageException) it
                assert storageException.getErrorCode() == BlobErrorCode.CONDITION_NOT_MET ||
                    storageException.getErrorCode() == BlobErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION
            })

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
    @LiveOnly
    def "UploadBufferPool lock three or more buffers"() {
        setup:
        blockBlobAsyncClient.upload(data.defaultFlux, data.defaultDataSize, true).block()
        def leaseID = setupBlobLeaseCondition(blockBlobAsyncClient, garbageLeaseID)
        def requestConditions = new BlobRequestConditions().setLeaseId(leaseID)

        when:
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSizeLong(blockSize)
            .setMaxConcurrency(numBuffers as int)

        then:
        StepVerifier.create(blobAsyncClient.uploadWithResponse(Flux.just(getRandomData(dataLength)),
            parallelTransferOptions, null, null, null, requestConditions))
            .verifyErrorSatisfies({ assert it instanceof BlobStorageException })

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

    @LiveOnly
    def "Buffered upload network error"() {
        setup:
        /*
         This test uses a Flowable that does not allow multiple subscriptions and therefore ensures that we are
         buffering properly to allow for retries even given this source behavior.
         */
        blockBlobAsyncClient.upload(Flux.just(data.defaultData), data.defaultDataSize, true).block()

        // Mock a response that will always be retried.
        def mockHttpResponse = getStubResponse(500, new HttpRequest(HttpMethod.PUT, new URL("https://www.fake.com")))

        // Mock a policy that will always then check that the data is still the same and return a retryable error.
        def mockPolicy = { HttpPipelineCallContext context, HttpPipelineNextPolicy next ->
            return collectBytesInBuffer(context.getHttpRequest().getBody())
                .map({ it == data.defaultData })
                .flatMap({ it ? Mono.just(mockHttpResponse) : Mono.error(new IllegalArgumentException()) })
        }

        // Build the pipeline
        blobAsyncClient = new BlobServiceClientBuilder()
            .credential(environment.primaryAccount.credential)
            .endpoint(environment.primaryAccount.blobEndpoint)
            .retryOptions(new RequestRetryOptions(null, 3, null, 500, 1500, null))
            .addPolicy(mockPolicy).buildAsyncClient()
            .getBlobContainerAsyncClient(generateContainerName()).getBlobAsyncClient(generateBlobName())

        when:
        // Try to upload the flowable, which will hit a retry. A normal upload would throw, but buffering prevents that.
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(1024).setMaxConcurrency(4)
        // TODO: It could be that duplicates aren't getting made in the retry policy? Or before the retry policy?

        then:
        // A second subscription to a download stream will
        StepVerifier.create(blobAsyncClient.upload(blockBlobAsyncClient.download(), parallelTransferOptions, true))
            .verifyErrorSatisfies({
                assert it instanceof BlobStorageException
                assert it.getStatusCode() == 500
            })
    }

    @LiveOnly
    def "Buffered upload default no overwrite"() {
        expect:
        StepVerifier.create(blobAsyncClient.upload(data.defaultFlux, null))
            .verifyError(IllegalArgumentException)
    }

    def "Upload binary data no overwrite"() {
        expect:
        StepVerifier.create(blobAsyncClient.upload(data.defaultBinaryData))
            .verifyError(IllegalArgumentException)
    }

    @LiveOnly
    def "Buffered upload no overwrite interrupted"() {
        setup:
        def smallFile = getRandomFile(50)

        /*
         * Setup the data stream to trigger a small upload upon subscription. This will happen once the upload method
         * has verified whether a blob with the given name already exists, so this will trigger once uploading begins.
         */
        def data = Flux.just(getRandomData(Constants.MB)).repeat(257)
            .doOnSubscribe({ blobAsyncClient.uploadFromFile(smallFile.toPath().toString()).subscribe() })
        blobAsyncClient = ccAsync.getBlobAsyncClient(generateBlobName())

        expect:
        StepVerifier.create(blobAsyncClient.upload(data, null))
            .verifyErrorSatisfies({
                assert it instanceof BlobStorageException
                assert ((BlobStorageException) it).getErrorCode() == BlobErrorCode.BLOB_ALREADY_EXISTS
            })
        cleanup:
        smallFile.delete()
    }

    @LiveOnly
    def "Buffered upload overwrite"() {
        when:
        def file = getRandomFile(50)
        blobClient.uploadFromFile(file.toPath().toString(), true)

        then:
        notThrown(BlobStorageException)

        and:
        def uploadVerifier = StepVerifier.create(blobAsyncClient.uploadFromFile(getRandomFile(50).toPath().toString(), true))

        then:
        uploadVerifier.verifyComplete()

        cleanup:
        file.delete()
    }

    def "Buffered upload nonMarkableStream"() {
        setup:
        def file = getRandomFile(10)
        def fileStream = new FileInputStream(file)
        def outFile = getRandomFile(10)

        when:
        blobClient.upload(fileStream, file.size(), true)

        then:
        blobClient.downloadToFile(outFile.toPath().toString(), true)
        compareFiles(file, outFile, 0, file.size())
    }

    def "Get Container Name"() {
        expect:
        containerName == blockBlobClient.getContainerName()
    }

    def "Get Block Blob Name"() {
        expect:
        blobName == blockBlobClient.getBlobName()
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

    def "Builder cpk validation"() {
        setup:
        String endpoint = BlobUrlParts.parse(blockBlobClient.getBlobUrl()).setScheme("http").toUrl()
        def builder = new SpecializedBlobClientBuilder()
            .customerProvidedKey(new CustomerProvidedKey(Base64.getEncoder().encodeToString(getRandomByteArray(256))))
            .endpoint(endpoint)

        when:
        builder.buildBlockBlobClient()

        then:
        thrown(IllegalArgumentException)
    }

    def "Builder bearer token validation"() {
        setup:
        String endpoint = BlobUrlParts.parse(blockBlobClient.getBlobUrl()).setScheme("http").toUrl()
        def builder = new SpecializedBlobClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)

        when:
        builder.buildBlockBlobClient()

        then:
        thrown(IllegalArgumentException)
    }

    @IgnoreIf( { getEnvironment().serviceVersion != null } )
    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials and auth would fail because we changed a signed header.
    def "Per call policy"() {
        setup:
        def specialBlob = getSpecializedBuilder(environment.primaryAccount.credential, blockBlobClient.getBlobUrl(), getPerCallVersionPolicy())
            .buildBlockBlobClient()

        when:
        def response = specialBlob.getPropertiesWithResponse(null, null, null)

        then:
        notThrown(BlobStorageException)
        response.getHeaders().getValue("x-ms-version") == "2017-11-09"
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2020_04_08")
    def "Upload from Url min"() {
        setup:
        def sourceBlob = primaryBlobServiceClient.getBlobContainerClient(containerName).getBlobClient(generateBlobName())
        sourceBlob.upload(data.defaultInputStream, data.defaultDataSize)
        def sas = sourceBlob.generateSas(new BlobServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)))
        if (blockBlobClient.exists()) {
            blockBlobClient.delete()
        }

        when:
        def blockBlobItem = blockBlobClient.uploadFromUrl(sourceBlob.getBlobUrl() + "?" + sas)
        def os = new ByteArrayOutputStream()
        blockBlobClient.download(os)

        then:
        notThrown(BlobStorageException)
        blockBlobItem != null
        blockBlobItem.ETag != null
        blockBlobItem.lastModified != null
        os.toByteArray() == data.defaultBytes
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2020_04_08")
    def "Upload from Url overwrite"() {
        setup:
        def sourceBlob = primaryBlobServiceClient.getBlobContainerClient(containerName).getBlobClient(generateBlobName())
        sourceBlob.upload(data.defaultInputStream, data.defaultDataSize)
        def sas = sourceBlob.generateSas(new BlobServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)))
        blockBlobClient.upload(new ByteArrayInputStream(), 0, true)

        when:
        def blockBlobItem = blockBlobClient.uploadFromUrl(sourceBlob.getBlobUrl() + "?" + sas, true)
        def os = new ByteArrayOutputStream()
        blockBlobClient.download(os)

        then:
        notThrown(BlobStorageException)
        blockBlobItem != null
        blockBlobItem.ETag != null
        blockBlobItem.lastModified != null
        os.toByteArray() == data.defaultBytes
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2020_04_08")
    def "Upload from Url overwrite fails on existing blob"() {
        setup:
        def sourceBlob = primaryBlobServiceClient.getBlobContainerClient(containerName).getBlobClient(generateBlobName())
        sourceBlob.upload(data.defaultInputStream, data.defaultDataSize)
        def sas = sourceBlob.generateSas(new BlobServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)))
        blockBlobClient.upload(new ByteArrayInputStream(), 0, true)

        when:
        blockBlobClient.uploadFromUrl(sourceBlob.getBlobUrl() + "?" + sas, false)

        then:
        def e = thrown(BlobStorageException)
        e.getErrorCode() == BlobErrorCode.BLOB_ALREADY_EXISTS

        when:
        blockBlobClient.uploadFromUrl(sourceBlob.getBlobUrl() + "?" + sas)

        then:
        e = thrown(BlobStorageException)
        e.getErrorCode() == BlobErrorCode.BLOB_ALREADY_EXISTS
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2020_04_08")
    def "Upload from Url max"() {
        setup:
        def sourceBlob = primaryBlobServiceClient.getBlobContainerClient(containerName).getBlobClient(generateBlobName())
        sourceBlob.upload(data.defaultInputStream, data.defaultDataSize)
        sourceBlob.setHttpHeaders(new BlobHttpHeaders().setContentLanguage("en-GB"))
        byte[] sourceBlobMD5 = MessageDigest.getInstance("MD5").digest(data.defaultBytes)
        def sourceProperties = sourceBlob.getProperties()
        def sas = sourceBlob.generateSas(new BlobServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)))
        blockBlobClient.upload(new ByteArrayInputStream(), 0, true)
        def destinationPropertiesBefore = blockBlobClient.getProperties()

        when:
        def options = new BlobUploadFromUrlOptions(sourceBlob.getBlobUrl() + "?" + sas)
            .setContentMd5(sourceBlobMD5)
            .setCopySourceBlobProperties(true)
            .setDestinationRequestConditions(new BlobRequestConditions().setIfMatch(destinationPropertiesBefore.getETag()))
            .setSourceRequestConditions(new BlobRequestConditions().setIfMatch(sourceProperties.getETag()))
            .setHeaders(new BlobHttpHeaders().setContentType("text"))
            .setTier(AccessTier.COOL)
        def response = blockBlobClient.uploadFromUrlWithResponse(options, null, null)
        def destinationProperties = blobClient.getProperties()
        def os = new ByteArrayOutputStream()
        blockBlobClient.download(os)

        then:
        notThrown(BlobStorageException)
        response != null
        response.request != null
        response.headers != null
        def blockBlobItem = response.value
        blockBlobItem != null
        blockBlobItem.ETag != null
        blockBlobItem.lastModified != null
        os.toByteArray() == data.defaultBytes
        destinationProperties.getContentLanguage() == "en-GB"
        destinationProperties.getContentType() == "text"
        destinationProperties.getAccessTier() == AccessTier.COOL
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2020_04_08")
    def "Upload from with invalid source MD5"() {
        setup:
        def sourceBlob = primaryBlobServiceClient.getBlobContainerClient(containerName).getBlobClient(generateBlobName())
        sourceBlob.upload(data.defaultInputStream, data.defaultDataSize)
        byte[] sourceBlobMD5 = MessageDigest.getInstance("MD5").digest("garbage".getBytes(StandardCharsets.UTF_8))
        def sas = sourceBlob.generateSas(new BlobServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)))
        blockBlobClient.upload(new ByteArrayInputStream(), 0, true)

        when:
        def options = new BlobUploadFromUrlOptions(sourceBlob.getBlobUrl() + "?" + sas)
            .setContentMd5(sourceBlobMD5)
        def response = blockBlobClient.uploadFromUrlWithResponse(options, null, null)

        then:
        def e = thrown(BlobStorageException)
        e.getErrorCode() == BlobErrorCode.MD5MISMATCH
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2020_04_08")
    @Unroll
    def "Upload from Url source request conditions"() {
        setup:
        def sourceBlob = primaryBlobServiceClient.getBlobContainerClient(containerName).getBlobClient(generateBlobName())
        sourceBlob.upload(data.defaultInputStream, data.defaultDataSize)
        def sas = sourceBlob.generateSas(new BlobServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)))
        blockBlobClient.upload(new ByteArrayInputStream(), 0, true)

        when:
        def options = new BlobUploadFromUrlOptions(sourceBlob.getBlobUrl() + "?" + sas).setSourceRequestConditions(requestConditions)
        blockBlobClient.uploadFromUrlWithResponse(options, null, null)

        then:
        def e = thrown(BlobStorageException)
        e.getErrorCode() == errorCode

        where:
        requestConditions                                                                    | errorCode
        new BlobRequestConditions().setIfMatch("dummy")                                      | BlobErrorCode.SOURCE_CONDITION_NOT_MET
        new BlobRequestConditions().setIfModifiedSince(OffsetDateTime.now().plusSeconds(10)) | BlobErrorCode.CANNOT_VERIFY_COPY_SOURCE
        new BlobRequestConditions().setIfUnmodifiedSince(OffsetDateTime.now().minusDays(1))  | BlobErrorCode.CANNOT_VERIFY_COPY_SOURCE
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2020_04_08")
    @Unroll
    def "Upload from Url destination request conditions"() {
        setup:
        def sourceBlob = primaryBlobServiceClient.getBlobContainerClient(containerName).getBlobClient(generateBlobName())
        sourceBlob.upload(data.defaultInputStream, data.defaultDataSize)
        def sas = sourceBlob.generateSas(new BlobServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)))
        blockBlobClient.upload(new ByteArrayInputStream(), 0, true)
        if (requestConditions.getLeaseId() != null) {
            createLeaseClient(blobClient).acquireLease(60)
        }

        when:
        def options = new BlobUploadFromUrlOptions(sourceBlob.getBlobUrl() + "?" + sas).setDestinationRequestConditions(requestConditions)
        blockBlobClient.uploadFromUrlWithResponse(options, null, null)

        then:
        def e = thrown(BlobStorageException)
        e.getErrorCode() == errorCode

        where:
        requestConditions                                                                    | errorCode
        new BlobRequestConditions().setIfMatch("dummy")                                      | BlobErrorCode.TARGET_CONDITION_NOT_MET
        new BlobRequestConditions().setIfNoneMatch("*")                                      | BlobErrorCode.BLOB_ALREADY_EXISTS
        new BlobRequestConditions().setIfModifiedSince(OffsetDateTime.now().plusDays(10))    | BlobErrorCode.CONDITION_NOT_MET
        new BlobRequestConditions().setIfUnmodifiedSince(OffsetDateTime.now().minusDays(1))  | BlobErrorCode.CONDITION_NOT_MET
        new BlobRequestConditions().setLeaseId("9260fd2d-34c1-42b5-9217-8fb7c6484bfb")       | BlobErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION
    }
}
