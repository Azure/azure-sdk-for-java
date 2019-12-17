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
import com.azure.storage.blob.BlobUrlParts
import com.azure.storage.blob.ProgressReceiver
import com.azure.storage.blob.models.AccessTier
import com.azure.storage.blob.models.BlobErrorCode
import com.azure.storage.blob.models.BlobHttpHeaders
import com.azure.storage.blob.models.BlobRange
import com.azure.storage.blob.models.BlobRequestConditions
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.blob.models.BlockListType
import com.azure.storage.blob.models.CustomerProvidedKey
import com.azure.storage.blob.models.ParallelTransferOptions
import com.azure.storage.blob.models.PublicAccessType
import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.policy.RequestRetryOptions
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Requires
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.security.MessageDigest
import java.time.Duration

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
        blockBlobClient.upload(defaultInputStream.get(), defaultDataSize)
        blobAsyncClient = ccAsync.getBlobAsyncClient(generateBlobName())
        blockBlobAsyncClient = blobAsyncClient.getBlockBlobAsyncClient()
        blockBlobAsyncClient.upload(defaultFlux, defaultDataSize).block()
    }

    def "Stage block"() {
        setup:
        def response = blockBlobClient.stageBlockWithResponse(getBlockID(), defaultInputStream.get(), defaultDataSize, null, null,
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
        blockBlobClient.stageBlock(getBlockID(), defaultInputStream.get(), defaultDataSize) == 201

        then:
        blockBlobClient.listBlocks(BlockListType.ALL).getUncommittedBlocks().size() == 1
    }

    @Unroll
    def "Stage block illegal arguments"() {
        when:
        def blockID = (getBlockId) ? getBlockID() : null
        blockBlobClient.stageBlock(blockID, data == null ? null : data.get(), dataSize)

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
        blockBlobClient.stageBlock(getBlockID(), new ByteArrayInputStream(new byte[0]), 0)

        then:
        thrown(BlobStorageException)
    }

    def "Stage block transactionalMD5"() {
        setup:
        byte[] md5 = MessageDigest.getInstance("MD5").digest(defaultData.array())

        expect:
        blockBlobClient.stageBlockWithResponse(getBlockID(), defaultInputStream.get(), defaultDataSize, md5, null, null, null)
            .statusCode == 201
    }

    def "Stage block transactionalMD5 fail"() {
        when:
        blockBlobClient.stageBlockWithResponse(getBlockID(), defaultInputStream.get(), defaultDataSize,
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
        blockBlobClient.stageBlockWithResponse(getBlockID(), defaultInputStream.get(), defaultDataSize, null, leaseID, null, null)
            .getStatusCode() == 201
    }

    def "Stage block lease fail"() {
        setup:
        setupBlobLeaseCondition(blockBlobClient, receivedLeaseID)

        when:
        blockBlobClient.stageBlockWithResponse(getBlockID(), defaultInputStream.get(), defaultDataSize, null, garbageLeaseID, null,
            null)

        then:
        def e = thrown(BlobStorageException)
        e.getErrorCode() == BlobErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION
    }

    def "Stage block error"() {
        setup:
        blockBlobClient = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        blockBlobClient.stageBlock("id", defaultInputStream.get(), defaultDataSize)

        then:
        thrown(BlobStorageException)
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
        ByteBuffer.wrap(outputStream.toByteArray()) == defaultData
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
            MessageDigest.getInstance("MD5").digest(defaultData.array()), null, null, null, null)

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
        sourceURL.upload(defaultInputStream.get(), defaultDataSize)

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
        sourceURL.upload(defaultInputStream.get(), defaultDataSize)

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
        blockBlobClient.stageBlock(blockID, defaultInputStream.get(), defaultDataSize)
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
        blockBlobClient.stageBlock(blockID, defaultInputStream.get(), defaultDataSize)
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
        blockBlobClient.stageBlock(blockID, defaultInputStream.get(), defaultDataSize)
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

    @Unroll
    def "Commit block list AC"() {
        setup:
        match = setupBlobMatchCondition(blockBlobClient, match)
        leaseID = setupBlobLeaseCondition(blockBlobClient, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)


        expect:
        blockBlobClient.commitBlockListWithResponse(null, null, null, null, bac, null, null).getStatusCode() == 201

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
        noneMatch = setupBlobMatchCondition(blockBlobClient, noneMatch)
        setupBlobLeaseCondition(blockBlobClient, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        blockBlobClient.commitBlockListWithResponse(null, null, null, null, bac, null, null)
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
        blockBlobClient = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        blockBlobClient.commitBlockListWithResponse(new ArrayList<String>(), null, null, null, new BlobRequestConditions().setLeaseId("garbage"), null, null)

        then:
        thrown(BlobStorageException)
    }

    def "Get block list"() {
        setup:
        def committedBlocks = [getBlockID(), getBlockID()]
        blockBlobClient.stageBlock(committedBlocks.get(0), defaultInputStream.get(), defaultDataSize)
        blockBlobClient.stageBlock(committedBlocks.get(1), defaultInputStream.get(), defaultDataSize)
        blockBlobClient.commitBlockList(committedBlocks, true)

        def uncommittedBlocks = [getBlockID(), getBlockID()]
        blockBlobClient.stageBlock(uncommittedBlocks.get(0), defaultInputStream.get(), defaultDataSize)
        blockBlobClient.stageBlock(uncommittedBlocks.get(1), defaultInputStream.get(), defaultDataSize)
        uncommittedBlocks.sort(true)

        when:
        def blockList = blockBlobClient.listBlocks(BlockListType.ALL)

        then:
        blockList.getCommittedBlocks().collect { it.getName() } as Set == committedBlocks as Set
        blockList.getUncommittedBlocks().collect { it.getName() } as Set == uncommittedBlocks as Set

        (blockList.getCommittedBlocks() + blockList.getUncommittedBlocks())
            .each { assert it.getSize() == defaultDataSize }
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
        blockBlobClient.stageBlock(blockID, defaultInputStream.get(), defaultDataSize)
        blockBlobClient.commitBlockList([blockID], true)
        blockBlobClient.stageBlock(getBlockID(), defaultInputStream.get(), defaultDataSize)

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
        def response = blockBlobClient.uploadWithResponse(defaultInputStream.get(), defaultDataSize, null, null, null, null, null,
            null, null)

        then:
        response.getStatusCode() == 201
        def outStream = new ByteArrayOutputStream()
        blockBlobClient.download(outStream)
        outStream.toByteArray() == defaultText.getBytes(StandardCharsets.UTF_8)
        validateBasicHeaders(response.getHeaders())
        response.getHeaders().getValue("Content-MD5") != null
        Boolean.parseBoolean(response.getHeaders().getValue("x-ms-request-server-encrypted"))
    }

    /* Upload From File Tests: Need to run on liveMode only since blockBlob wil generate a `UUID.randomUUID()`
       for getBlockID that will change every time test is run
     */

    @Requires({ liveMode() })
    @Unroll
    def "Upload from file"() {
        setup:
        def file = getRandomFile(fileSize)

        when:
        // Block length will be ignored for single shot.
        StepVerifier.create(blobAsyncClient.uploadFromFile(file.getPath(), new ParallelTransferOptions(blockSize, null,
            null), null, null, null, null))
            .verifyComplete()

        then:
        def outFile = file.getPath().toString() + "result"
        def outChannel = AsynchronousFileChannel.open(Paths.get(outFile), StandardOpenOption.CREATE,
            StandardOpenOption.WRITE)
        StepVerifier.create(FluxUtil.writeFile(blobAsyncClient.download(), outChannel)).verifyComplete()
        outChannel.close()

        compareFiles(file, new File(outFile), 0, fileSize)
        StepVerifier.create(blobAsyncClient.getBlockBlobAsyncClient().listBlocks(BlockListType.COMMITTED))
            .assertNext({ assert it.getCommittedBlocks().size() == commitedBlockCount })
            .verifyComplete()

        cleanup:
        Files.delete(Paths.get(outFile))
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

    @Requires({ liveMode() })
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

    @Requires({ liveMode() })
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

    @Requires({ liveMode() })
    def "Upload from file no overwrite interrupted"() {
        setup:
        def file = getRandomFile(257 * 1024 * 1024)
        def smallFile = getRandomFile(50)
        blobAsyncClient = ccAsync.getBlobAsyncClient(generateBlobName())

        expect:
        /*
         * When the upload begins trigger an upload to write the blob after waiting 500 milliseconds so that the upload
         * fails when it attempts to put the block list.
         */
        StepVerifier.create(blobAsyncClient.uploadFromFile(file.toPath().toString())
            .doOnSubscribe({
                blobAsyncClient.uploadFromFile(smallFile.toPath().toString()).delaySubscription(Duration.ofMillis(500)).subscribe()
            }))
            .verifyErrorSatisfies({
                assert it instanceof BlobStorageException
                assert ((BlobStorageException) it).getErrorCode() == BlobErrorCode.BLOB_ALREADY_EXISTS
            })

        cleanup:
        file.delete()
        smallFile.delete()
    }

    @Requires({ liveMode() })
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

    def "Upload min"() {
        when:
        blockBlobClient.upload(defaultInputStream.get(), defaultDataSize, true)

        then:
        def outStream = new ByteArrayOutputStream()
        blockBlobClient.download(outStream)
        outStream.toByteArray() == defaultText.getBytes(StandardCharsets.UTF_8)
    }

    @Unroll
    def "Upload illegal argument"() {
        when:
        blockBlobClient.upload(data, dataSize)

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
        blockBlobClient.uploadWithResponse(defaultInputStream.get(), defaultDataSize, headers, null, null, null, null, null, null)
        def response = blockBlobClient.getPropertiesWithResponse(null, null, null)

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

    def "Upload transactionalMD5"() {
        setup:
        byte[] md5 = MessageDigest.getInstance("MD5").digest(defaultData.array())

        expect:
        blockBlobClient.uploadWithResponse(defaultInputStream.get(), defaultDataSize, null, null, null, md5, null, null, null)
            .statusCode == 201
    }

    def "Upload transactionalMD5 fail"() {
        when:
        blockBlobClient.stageBlockWithResponse(getBlockID(), defaultInputStream.get(), defaultDataSize,
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
        blockBlobClient.uploadWithResponse(defaultInputStream.get(), defaultDataSize, null, metadata, null, null, null, null, null)
        def response = blockBlobClient.getPropertiesWithResponse(null, null, null)

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
        match = setupBlobMatchCondition(blockBlobClient, match)
        leaseID = setupBlobLeaseCondition(blockBlobClient, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        blockBlobClient.uploadWithResponse(defaultInputStream.get(), defaultDataSize, null, null, null, null, bac, null, null).getStatusCode() == 201

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
        noneMatch = setupBlobMatchCondition(blockBlobClient, noneMatch)
        setupBlobLeaseCondition(blockBlobClient, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        blockBlobClient.uploadWithResponse(defaultInputStream.get(), defaultDataSize, null, null, null, null, bac, null, null)

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
        blockBlobClient = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        blockBlobClient.uploadWithResponse(defaultInputStream.get(), defaultDataSize, null, null, null, null,
            new BlobRequestConditions().setLeaseId("id"), null, null)

        then:
        thrown(BlobStorageException)
    }

    def "Upload with tier"() {
        setup:
        def bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        bc.uploadWithResponse(defaultInputStream.get(), defaultDataSize, null, null, AccessTier.COOL, null, null, null,
            null)

        then:
        bc.getProperties().getAccessTier() == AccessTier.COOL
    }

    def "Upload overwrite false"() {
        when:
        blockBlobClient.upload(defaultInputStream.get(), defaultDataSize)

        then:
        thrown(BlobStorageException)
    }

    def "Upload overwrite true"() {
        when:
        blockBlobClient.upload(defaultInputStream.get(), defaultDataSize, true)

        then:
        notThrown(Throwable)
    }

    @Requires({ liveMode() })
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
    @Requires({ liveMode() })
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
    @Requires({ liveMode() })
    def "Async buffered upload"() {
        when:
        def data = getRandomData(dataSize)
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions(bufferSize, numBuffs, null)
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

        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions(blockSize, bufferCount,
            uploadReporter)

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
    @Requires({ liveMode() })
    def "Buffered upload chunked source"() {
        /*
        This test should validate that the upload should work regardless of what format the passed data is in because
        it will be chunked appropriately.
         */
        setup:
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions(bufferSize * Constants.MB, numBuffers, null)
        def dataList = [] as List
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
    @Requires({ liveMode() })
    def "Buffered upload handle pathing"() {
        setup:
        def dataList = [] as List<ByteBuffer>
        dataSizeList.each { size -> dataList.add(getRandomData(size)) }
        def uploadOperation = blobAsyncClient.upload(Flux.fromIterable(dataList), null, true)

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
    @Requires({ liveMode() })
    def "Buffered upload handle pathing hot flux"() {
        setup:
        def dataList = [] as List<ByteBuffer>
        dataSizeList.each { size -> dataList.add(getRandomData(size)) }
        def uploadOperation = blobAsyncClient.upload(Flux.fromIterable(dataList).publish().autoConnect(), null, true)

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

    def "Buffered upload illegal arguments null"() {
        expect:
        StepVerifier.create(blobAsyncClient.upload(null, new ParallelTransferOptions(4, 4, null), true))
            .verifyErrorSatisfies({ assert it instanceof NullPointerException })
    }

    @Unroll
    def "Buffered upload illegal args out of bounds"() {
        when:
        new ParallelTransferOptions(bufferSize, numBuffs, null)

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
        def data = getRandomByteArray(dataSize)
        def contentMD5 = validateContentMD5 ? MessageDigest.getInstance("MD5").digest(data) : null
        def uploadOperation = blobAsyncClient.uploadWithResponse(Flux.just(ByteBuffer.wrap(data)), null, new BlobHttpHeaders()
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
        dataSize         | cacheControl | contentDisposition | contentEncoding | contentLanguage | validateContentMD5 | contentType
        defaultDataSize  | null         | null               | null            | null            | true               | null
        defaultDataSize  | "control"    | "disposition"      | "encoding"      | "language"      | true               | "type"
        6 * Constants.MB | null         | null               | null            | null            | false              | null
        6 * Constants.MB | "control"    | "disposition"      | "encoding"      | "language"      | true               | "type"
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
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions(10, 10, null)
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
    @Unroll
    @Requires({ liveMode() })
    def "Buffered upload AC"() {
        setup:
        blockBlobAsyncClient.upload(defaultFlux, defaultDataSize, true).block()
        match = setupBlobMatchCondition(blockBlobAsyncClient, match)
        leaseID = setupBlobLeaseCondition(blockBlobAsyncClient, leaseID)
        def requestConditions = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions(10, null, null)
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
    @Requires({ liveMode() })
    def "Buffered upload AC fail"() {
        setup:
        blockBlobAsyncClient.upload(defaultFlux, defaultDataSize, true).block()
        noneMatch = setupBlobMatchCondition(blockBlobAsyncClient, noneMatch)
        leaseID = setupBlobLeaseCondition(blockBlobAsyncClient, leaseID)
        def requestConditions = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
        def parallelTransferOptions = new ParallelTransferOptions(10, null, null)

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
    @Requires({ liveMode() })
    def "UploadBufferPool lock three or more buffers"() {
        setup:
        blockBlobAsyncClient.upload(defaultFlux, defaultDataSize, true).block()
        def leaseID = setupBlobLeaseCondition(blockBlobAsyncClient, garbageLeaseID)
        def requestConditions = new BlobRequestConditions().setLeaseId(leaseID)

        when:
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions(blockSize as int,
            numBuffers as int, null)

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

    @Requires({ liveMode() })
    def "Buffered upload network error"() {
        setup:
        /*
         This test uses a Flowable that does not allow multiple subscriptions and therefore ensures that we are
         buffering properly to allow for retries even given this source behavior.
         */
        blockBlobAsyncClient.upload(Flux.just(defaultData), defaultDataSize, true).block()

        // Mock a response that will always be retried.
        def mockHttpResponse = getStubResponse(500, new HttpRequest(HttpMethod.PUT, new URL("https://www.fake.com")))

        // Mock a policy that will always then check that the data is still the same and return a retryable error.
        def mockPolicy = { HttpPipelineCallContext context, HttpPipelineNextPolicy next ->
            return collectBytesInBuffer(context.getHttpRequest().getBody())
                .map({ it == defaultData })
                .flatMap({ it ? Mono.just(mockHttpResponse) : Mono.error(new IllegalArgumentException()) })
        }

        // Build the pipeline
        blobAsyncClient = new BlobServiceClientBuilder()
            .credential(primaryCredential)
            .endpoint(String.format(defaultEndpointTemplate, primaryCredential.getAccountName()))
            .httpClient(getHttpClient())
            .retryOptions(new RequestRetryOptions(null, 3, null, 500, 1500, null))
            .addPolicy(mockPolicy).buildAsyncClient()
            .getBlobContainerAsyncClient(generateContainerName()).getBlobAsyncClient(generateBlobName())

        when:
        // Try to upload the flowable, which will hit a retry. A normal upload would throw, but buffering prevents that.
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions(1024, 4, null)
        // TODO: It could be that duplicates aren't getting made in the retry policy? Or before the retry policy?

        then:
        // A second subscription to a download stream will
        StepVerifier.create(blobAsyncClient.upload(blockBlobAsyncClient.download(), parallelTransferOptions, true))
            .verifyErrorSatisfies({
                assert it instanceof BlobStorageException
                assert it.getStatusCode() == 500
            })
    }

    @Requires({ liveMode() })
    def "Buffered upload default no overwrite"() {
        expect:
        StepVerifier.create(blobAsyncClient.upload(defaultFlux, null))
            .verifyError(IllegalArgumentException)
    }

    @Requires({ liveMode() })
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

    @Requires({ liveMode() })
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
        "斑點"                   | "斑點"
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
}
