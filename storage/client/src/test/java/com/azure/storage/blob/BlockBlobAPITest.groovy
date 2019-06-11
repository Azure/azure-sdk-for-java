// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob


import com.azure.storage.blob.models.*
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.security.MessageDigest

class BlockBlobAPITest extends APISpec {
    BlockBlobClient bu

    def setup() {
        bu = cu.createBlockBlobClient(generateBlobName())
        bu.upload(defaultInputStream, defaultDataSize)
    }

    def getBlockID() {
        return new String(Base64.encoder.encode(UUID.randomUUID().toString().bytes))
    }

    def "Stage block"() {
        setup:
        bu.stageBlock(getBlockID(), defaultInputStream, defaultDataSize)
//        BlockBlobStageBlockHeaders headers = response.headers()
//
//        expect:
//        notThrown(StorageException)
//        response.statusCode() == 201
//        headers.contentMD5() != null
//        headers.requestId() != null
//        headers.version() != null
//        headers.date() != null
//        headers.isServerEncrypted()
    }

//    def "Stage block min"() {
//        expect:
//        bu.stageBlock(getBlockID(), defaultInputStream, defaultDataSize).blockingGet().statusCode() == 201
//    }

    @Unroll
    def "Stage block illegal arguments"() {
        when:
        bu.stageBlock(blockID, data, dataSize).blockingGet()

        then:
        def e = thrown(Exception)
        exceptionType.isInstance(e)

        where:
        blockID      | data            | dataSize            | exceptionType
        null         | defaultInputStream | defaultDataSize     | IllegalArgumentException
        getBlockID() | null            | defaultDataSize     | IllegalArgumentException
        getBlockID() | defaultInputStream | defaultDataSize + 1 | UnexpectedLengthException
        getBlockID() | defaultInputStream | defaultDataSize - 1 | UnexpectedLengthException
    }

    def "Stage block empty body"() {
        when:
        bu.stageBlock(getBlockID(), new ByteArrayInputStream(new byte[0]), 0)
            .blockingGet()

        then:
        thrown(StorageException)
    }

    def "Stage block null body"() {
        when:
        bu.stageBlock(getBlockID(), null, 0).blockingGet()

        then:
        thrown(NullPointerException) // Thrown by Flux.just().
    }

//    def "Stage block lease"() {
//        setup:
//        String leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)
//
//        expect:
//        bu.stageBlock(getBlockID(), defaultFlux, defaultDataSize, new LeaseAccessConditions().withLeaseId(leaseID),
//            null).blockingGet().statusCode() == 201
//    }
//
//    def "Stage block lease fail"() {
//        setup:
//        setupBlobLeaseCondition(bu, receivedLeaseID)
//
//        when:
//        bu.stageBlock(getBlockID(), defaultFlux, defaultDataSize, new LeaseAccessConditions()
//            .withLeaseId(garbageLeaseID), null).blockingGet()
//
//        then:
//        def e = thrown(StorageException)
//        e.errorCode() == StorageErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION
//    }
//
//    def "Stage block error"() {
//        setup:
//        bu = cu.createBlockBlobURL(generateBlobName())
//
//        when:
//        bu.stageBlock("id", defaultFlux, defaultDataSize, null, null)
//            .blockingGet()
//
//        then:
//        thrown(StorageException)
//    }
//
//    def "Stage block context"() {
//        setup:
//        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(201, BlockBlobStageBlockHeaders)))
//
//        bu = bu.withPipeline(pipeline)
//
//        when:
//        // No service call is made. Just satisfy the parameters.
//        bu.stageBlock("id", defaultFlux, defaultDataSize, null, defaultContext).blockingGet()
//
//        then:
//        notThrown(RuntimeException)
//    }

//    def "Stage block from url"() {
//        setup:
//        cu.setAccessPolicy(PublicAccessType.CONTAINER, null)
//        def bu2 = cu.createBlockBlobClient(generateBlobName())
//        def blockID = getBlockID()
//
//        when:
//        def response = bu2.stageBlockFromURL(blockID, bu.toURL(), null)
//        def listResponse = bu2.listBlocks(BlockListType.ALL, null, null).blockingGet()
//        bu2.commitBlockList(Arrays.asList(blockID), null, null, null, null).blockingGet()
//
//        then:
//        response.headers().requestId() != null
//        response.headers().version() != null
//        response.headers().requestId() != null
//        response.headers().contentMD5() != null
//        response.headers().isServerEncrypted() != null
//
//        listResponse.body().uncommittedBlocks().get(0).name() == blockID
//        listResponse.body().uncommittedBlocks().size() == 1
//
//        FluxUtil.collectBytesInBuffer(bu2.download(null, null, false, null)
//            .blockingGet().body(null)).blockingGet() == defaultData
//    }
//
//    def "Stage block from url min"() {
//        setup:
//        cu.setAccessPolicy(PublicAccessType.CONTAINER, null, null, null).blockingGet()
//        def bu2 = cu.createBlockBlobURL(generateBlobName())
//        def blockID = getBlockID()
//
//        expect:
//        bu2.stageBlockFromURL(blockID, bu.toURL(), null).blockingGet().statusCode() == 201
//    }
//
//    @Unroll
//    def "Stage block from URL IA"() {
//        when:
//        bu.stageBlockFromURL(blockID, sourceURL, null, null, null, null, null)
//            .blockingGet()
//
//        then:
//        thrown(IllegalArgumentException)
//
//        where:
//        blockID      | sourceURL
//        null         | new URL("http://www.example.com")
//        getBlockID() | null
//    }
//
//    def "Stage block from URL range"() {
//        setup:
//        cu.setAccessPolicy(PublicAccessType.CONTAINER, null, null, null).blockingGet()
//        def destURL = cu.createBlockBlobURL(generateBlobName())
//
//        when:
//        destURL.stageBlockFromURL(getBlockID(), bu.toURL(), new BlobRange().withOffset(2).withCount(3), null, null,
//            null, null).blockingGet()
//
//        then:
//        destURL.listBlocks(BlockListType.ALL, null, null).blockingGet().body().uncommittedBlocks().get(0)
//            .size() == 3
//    }
//
//    def "Stage block from URL MD5"() {
//        setup:
//        cu.setAccessPolicy(PublicAccessType.CONTAINER, null, null, null).blockingGet()
//        def destURL = cu.createBlockBlobURL(generateBlobName())
//
//        when:
//        destURL.stageBlockFromURL(getBlockID(), bu.toURL(), null,
//            MessageDigest.getInstance("MD5").digest(defaultData.array()), null, null, null).blockingGet()
//
//        then:
//        notThrown(StorageException)
//    }
//
//    def "Stage block from URL MD5 fail"() {
//        setup:
//        cu.setAccessPolicy(PublicAccessType.CONTAINER, null, null, null).blockingGet()
//        def destURL = cu.createBlockBlobURL(generateBlobName())
//
//        when:
//        destURL.stageBlockFromURL(getBlockID(), bu.toURL(), null, "garbage".getBytes(),
//            null, null, null).blockingGet()
//
//        then:
//        thrown(StorageException)
//    }
//
//    def "Stage block from URL lease"() {
//        setup:
//        cu.setAccessPolicy(PublicAccessType.CONTAINER, null, null, null).blockingGet()
//        def lease = new LeaseAccessConditions().withLeaseId(setupBlobLeaseCondition(bu, receivedLeaseID))
//
//        when:
//        bu.stageBlockFromURL(getBlockID(), bu.toURL(), null, null, lease, null, null).blockingGet()
//
//        then:
//        notThrown(StorageException)
//    }
//
//    def "Stage block from URL lease fail"() {
//        setup:
//        cu.setAccessPolicy(PublicAccessType.CONTAINER, null, null, null).blockingGet()
//        def lease = new LeaseAccessConditions().withLeaseId("garbage")
//
//        when:
//        bu.stageBlockFromURL(getBlockID(), bu.toURL(), null, null, lease, null, null).blockingGet()
//
//        then:
//        thrown(StorageException)
//    }
//
//    def "Stage block from URL error"() {
//        setup:
//        cu = primaryServiceURL.createContainerURL(generateContainerName())
//        bu = cu.createBlockBlobURL(generateBlobName())
//
//        when:
//        bu.stageBlockFromURL(getBlockID(), bu.toURL(), null, null, null, null, null)
//            .blockingGet()
//
//        then:
//        thrown(StorageException)
//    }
//
//    def "Stage block from URL context"() {
//        setup:
//        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(201, BlockBlobStageBlockFromURLHeaders)))
//
//        bu = bu.withPipeline(pipeline)
//
//        when:
//        // No service call is made. Just satisfy the parameters.
//        bu.stageBlockFromURL("id", bu.toURL(), null, null, null, null, defaultContext).blockingGet()
//
//        then:
//        notThrown(RuntimeException)
//    }
//
//    @Unroll
//    def "Stage block from URL source AC"() {
//        setup:
//        cu.setAccessPolicy(PublicAccessType.CONTAINER, null, null, null).blockingGet()
//        def blockID = getBlockID()
//
//        def sourceURL = cu.createBlockBlobURL(generateBlobName())
//        sourceURL.upload(defaultFlux, defaultDataSize).blockingGet()
//
//        sourceIfMatch = setupBlobMatchCondition(sourceURL, sourceIfMatch)
//        def smac = new SourceModifiedAccessConditions()
//            .withSourceIfModifiedSince(sourceIfModifiedSince)
//            .withSourceIfUnmodifiedSince(sourceIfUnmodifiedSince)
//            .withSourceIfMatch(sourceIfMatch)
//            .withSourceIfNoneMatch(sourceIfNoneMatch)
//
//        expect:
//        bu.stageBlockFromURL(blockID, sourceURL.toURL(), null, null, null, smac, null).blockingGet().statusCode() == 201
//
//        where:
//        sourceIfModifiedSince | sourceIfUnmodifiedSince | sourceIfMatch | sourceIfNoneMatch
//        null                  | null                    | null          | null
//        oldDate               | null                    | null          | null
//        null                  | newDate                 | null          | null
//        null                  | null                    | receivedEtag  | null
//        null                  | null                    | null          | garbageEtag
//    }
//
//    @Unroll
//    def "Stage block from URL source AC fail"() {
//        setup:
//        cu.setAccessPolicy(PublicAccessType.CONTAINER, null, null, null).blockingGet()
//        def blockID = getBlockID()
//
//        def sourceURL = cu.createBlockBlobURL(generateBlobName())
//        sourceURL.upload(defaultFlux, defaultDataSize).blockingGet()
//
//        sourceIfNoneMatch = setupBlobMatchCondition(sourceURL, sourceIfNoneMatch)
//        def smac = new SourceModifiedAccessConditions()
//            .withSourceIfModifiedSince(sourceIfModifiedSince)
//            .withSourceIfUnmodifiedSince(sourceIfUnmodifiedSince)
//            .withSourceIfMatch(sourceIfMatch)
//            .withSourceIfNoneMatch(sourceIfNoneMatch)
//
//        when:
//        bu.stageBlockFromURL(blockID, sourceURL.toURL(), null, null, null, smac, null).blockingGet().statusCode() == 201
//
//        then:
//        thrown(StorageException)
//
//        where:
//        sourceIfModifiedSince | sourceIfUnmodifiedSince | sourceIfMatch | sourceIfNoneMatch
//        newDate               | null                    | null          | null
//        null                  | oldDate                 | null          | null
//        null                  | null                    | garbageEtag   | null
//        null                  | null                    | null          | receivedEtag
//    }

    def "Commit block list"() {
        setup:
        String blockID = getBlockID()
        bu.stageBlock(blockID, defaultInputStream, defaultDataSize)
        ArrayList<String> ids = new ArrayList<>()
        ids.add(blockID)

        when:
        BlockBlobCommitBlockListHeaders headers =
            bu.commitBlockList(ids).blockingGet()

        then:
//        response.statusCode() == 201
        validateBasicHeaders(headers)
        headers.contentMD5()
        headers.isServerEncrypted()
    }

    def "Commit block list min"() {
        setup:
        String blockID = getBlockID()
        bu.stageBlock(blockID, defaultInputStream, defaultDataSize)
        ArrayList<String> ids = new ArrayList<>()
        ids.add(blockID)

        expect:
        bu.commitBlockList(ids) != null
    }

//    def "Commit block list null"() {
//        expect:
//        bu.commitBlockList(null,)
//            .blockingGet().statusCode() == 201
//    }
//
//    @Unroll
//    def "Commit block list headers"() {
//        setup:
//        String blockID = getBlockID()
//        bu.stageBlock(blockID, defaultFlux, defaultDataSize,
//            null, null).blockingGet()
//        ArrayList<String> ids = new ArrayList<>()
//        ids.add(blockID)
//        BlobHTTPHeaders headers = new BlobHTTPHeaders().withBlobCacheControl(cacheControl)
//            .withBlobContentDisposition(contentDisposition)
//            .withBlobContentEncoding(contentEncoding)
//            .withBlobContentLanguage(contentLanguage)
//            .withBlobContentMD5(contentMD5)
//            .withBlobContentType(contentType)
//
//        when:
//        bu.commitBlockList(ids, headers, null, null, null).blockingGet()
//        BlobGetPropertiesResponse response = bu.getProperties(null, null).blockingGet()
//
//        then:
//        response.statusCode() == 200
//        validateBlobHeaders(response.headers(), cacheControl, contentDisposition, contentEncoding, contentLanguage,
//            contentMD5, contentType == null ? "application/octet-stream" : contentType)
//        // HTTP default content type is application/octet-stream
//
//        where:
//        cacheControl | contentDisposition | contentEncoding | contentLanguage | contentMD5                                                   | contentType
//        null         | null               | null            | null            | null                                                         | null
//        "control"    | "disposition"      | "encoding"      | "language"      | MessageDigest.getInstance("MD5").digest(defaultData.array()) | "type"
//    }
//
//    @Unroll
//    def "Commit block list metadata"() {
//        setup:
//        Metadata metadata = new Metadata()
//        if (key1 != null) {
//            metadata.put(key1, value1)
//        }
//        if (key2 != null) {
//            metadata.put(key2, value2)
//        }
//
//        when:
//        bu.commitBlockList(null, null, metadata, null, null).blockingGet()
//        BlobGetPropertiesResponse response = bu.getProperties(null, null).blockingGet()
//
//        then:
//        response.statusCode() == 200
//        response.headers().metadata() == metadata
//
//        where:
//        key1  | value1 | key2   | value2
//        null  | null   | null   | null
//        "foo" | "bar"  | "fizz" | "buzz"
//    }
//
//    @Unroll
//    def "Commit block list AC"() {
//        setup:
//        match = setupBlobMatchCondition(bu, match)
//        leaseID = setupBlobLeaseCondition(bu, leaseID)
//        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
//            new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
//                .withIfMatch(match).withIfNoneMatch(noneMatch))
//            .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))
//
//        expect:
//        bu.commitBlockList(null, null, null, bac, null).blockingGet().statusCode() == 201
//
//        where:
//        modified | unmodified | match        | noneMatch   | leaseID
//        null     | null       | null         | null        | null
//        oldDate  | null       | null         | null        | null
//        null     | newDate    | null         | null        | null
//        null     | null       | receivedEtag | null        | null
//        null     | null       | null         | garbageEtag | null
//        null     | null       | null         | null        | receivedLeaseID
//    }
//
//    @Unroll
//    def "Commit block list AC fail"() {
//        setup:
//        noneMatch = setupBlobMatchCondition(bu, noneMatch)
//        setupBlobLeaseCondition(bu, leaseID)
//        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
//            new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
//                .withIfMatch(match).withIfNoneMatch(noneMatch))
//            .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))
//
//        when:
//        bu.commitBlockList(null, null, null, bac, null).blockingGet()
//
//        then:
//        def e = thrown(StorageException)
//        e.errorCode() == StorageErrorCode.CONDITION_NOT_MET ||
//            e.errorCode() == StorageErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION
//
//        where:
//        modified | unmodified | match       | noneMatch    | leaseID
//        newDate  | null       | null        | null         | null
//        null     | oldDate    | null        | null         | null
//        null     | null       | garbageEtag | null         | null
//        null     | null       | null        | receivedEtag | null
//        null     | null       | null        | null         | garbageLeaseID
//    }
//
//    def "Commit block list error"() {
//        setup:
//        bu = cu.createBlockBlobURL(generateBlobName())
//
//        when:
//        bu.commitBlockList(new ArrayList<String>(), null, null, new BlobAccessConditions().withLeaseAccessConditions(
//            new LeaseAccessConditions().withLeaseId("garbage")), null).blockingGet()
//
//        then:
//        thrown(StorageException)
//    }
//
//    def "Commit block list info context"() {
//        setup:
//        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(201, BlockBlobCommitBlockListHeaders)))
//
//        bu = bu.withPipeline(pipeline)
//
//        when:
//        // No service call is made. Just satisfy the parameters.
//        bu.commitBlockList(new ArrayList<String>(), null, null, null, defaultContext).blockingGet()
//
//        then:
//        notThrown(RuntimeException)
//    }

    def "Get block list"() {
        setup:
        List<String> committedBlocks = Arrays.asList(getBlockID(), getBlockID())
        bu.stageBlock(committedBlocks.get(0), defaultInputStream, defaultDataSize)
        bu.stageBlock(committedBlocks.get(1), defaultInputStream, defaultDataSize)
        bu.commitBlockList(committedBlocks)

        List<String> uncommittedBlocks = Arrays.asList(getBlockID(), getBlockID())
        bu.stageBlock(uncommittedBlocks.get(0), defaultInputStream, defaultDataSize)
        bu.stageBlock(uncommittedBlocks.get(1), defaultInputStream, defaultDataSize)
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

//    def "Get block list min"() {
//        expect:
//        bu.listBlocks(BlockListType.ALL).blockingGet().statusCode() == 200
//    }

    @Unroll
    def "Get block list type"() {
        setup:
        String blockID = getBlockID()
        bu.stageBlock(blockID, defaultInputStream, defaultDataSize)
        ArrayList<String> ids = new ArrayList<>()
        ids.add(blockID)
        bu.commitBlockList(ids)
        blockID = new String(getBlockID())
        bu.stageBlock(blockID, defaultInputStream, defaultDataSize)

        when:
        Iterable<BlockItem> response = bu.listBlocks(type)

        then:
        int committed = 0
        int uncommitted = 0
        for (BlockItem item : response) {
            if (item.isCommitted()) {
                committed ++
            } else {
                uncommitted ++
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

//    def "Get block list type null"() {
//        when:
//        bu.listBlocks(null, null, null).blockingGet()
//
//        then:
//        thrown(IllegalArgumentException)
//    }
//
//    def "Get block list lease"() {
//        setup:
//        String leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)
//
//        expect:
//        bu.listBlocks(BlockListType.ALL, new LeaseAccessConditions().withLeaseId(leaseID), null)
//            .blockingGet().statusCode() == 200
//    }
//
//    def "Get block list lease fail"() {
//        setup:
//        setupBlobLeaseCondition(bu, garbageLeaseID)
//
//        when:
//        bu.listBlocks(BlockListType.ALL, new LeaseAccessConditions().withLeaseId(garbageLeaseID), null).blockingGet()
//
//        then:
//        def e = thrown(StorageException)
//        e.errorCode() == StorageErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION
//    }
//
//    def "Get block list error"() {
//        setup:
//        bu = cu.createBlockBlobURL(generateBlobName())
//
//        when:
//        bu.listBlocks(BlockListType.ALL, null, null).blockingGet()
//
//        then:
//        thrown(StorageException)
//    }
//
//    def "Get block list context"() {
//        setup:
//        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(200, BlockBlobGetBlockListHeaders)))
//
//        bu = bu.withPipeline(pipeline)
//
//        when:
//        // No service call is made. Just satisfy the parameters.
//        bu.listBlocks(BlockListType.ALL, null, defaultContext).blockingGet()
//
//        then:
//        notThrown(RuntimeException)
//    }

    def "Upload"() {
        when:
        BlockBlobUploadHeaders headers = bu.upload(defaultInputStream, defaultDataSize)

        then:
//        response.statusCode() == 201
        def outStream = new ByteArrayOutputStream()
        bu.download(outStream)
        ByteBuffer.wrap(outStream.toByteArray()) == defaultData
        validateBasicHeaders(headers)
        headers.contentMD5() != null
        headers.isServerEncrypted()
    }

//    def "Upload min"() {
//        expect:
//        bu.upload(defaultFlux, defaultDataSize).blockingGet().statusCode() == 201
//    }

//    @Unroll
//    def "Upload illegal argument"() {
//        when:
//        bu.upload(data, dataSize, null, null, null, null).blockingGet()
//
//        then:
//        def e = thrown(Exception)
//        exceptionType.isInstance(e)
//
//        where:
//        data            | dataSize            | exceptionType
//        null            | defaultDataSize     | IllegalArgumentException
//        defaultFlux | defaultDataSize + 1 | UnexpectedLengthException
//        defaultFlux | defaultDataSize - 1 | UnexpectedLengthException
//    }
//
//    def "Upload empty body"() {
//        expect:
//        bu.upload(Flux.just(ByteBuffer.wrap(new byte[0])), 0, null, null,
//            null, null).blockingGet().statusCode() == 201
//    }
//
//    def "Upload null body"() {
//        when:
//        bu.upload(Flux.just(null), 0, null, null, null, null).blockingGet()
//
//        then:
//        thrown(NullPointerException) // Thrown by Flux.just().
//    }

    @Unroll
    def "Upload headers"() {
        setup:
        BlobHTTPHeaders headers = new BlobHTTPHeaders().withBlobCacheControl(cacheControl)
            .withBlobContentDisposition(contentDisposition)
            .withBlobContentEncoding(contentEncoding)
            .withBlobContentLanguage(contentLanguage)
            .withBlobContentMD5(contentMD5)
            .withBlobContentType(contentType)

        when:
        bu.upload(defaultInputStream, defaultDataSize,
            headers, null, null, null, null)
        BlobGetPropertiesHeaders responseHeaders = bu.getProperties(null, null)

        then:
        validateBlobHeaders(responseHeaders, cacheControl, contentDisposition, contentEncoding, contentLanguage,
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
        bu.upload(defaultInputStream, defaultDataSize,
            null, metadata, null, null, null)
        BlobGetPropertiesHeaders responseHeaders = bu.getProperties(null, null).blockingGet()

        then:
//        response.statusCode() == 200
        response.headers().metadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

//    @Unroll
//    def "Upload AC"() {
//        setup:
//        match = setupBlobMatchCondition(bu, match)
//        leaseID = setupBlobLeaseCondition(bu, leaseID)
//        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
//            new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
//                .withIfMatch(match).withIfNoneMatch(noneMatch))
//            .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))
//
//        expect:
//        bu.upload(defaultFlux, defaultDataSize,
//            null, null, bac, null).blockingGet().statusCode() == 201
//
//        where:
//        modified | unmodified | match        | noneMatch   | leaseID
//        null     | null       | null         | null        | null
//        oldDate  | null       | null         | null        | null
//        null     | newDate    | null         | null        | null
//        null     | null       | receivedEtag | null        | null
//        null     | null       | null         | garbageEtag | null
//        null     | null       | null         | null        | receivedLeaseID
//    }
//
//    @Unroll
//    def "Upload AC fail"() {
//        setup:
//        noneMatch = setupBlobMatchCondition(bu, noneMatch)
//        setupBlobLeaseCondition(bu, leaseID)
//        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
//            new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
//                .withIfMatch(match).withIfNoneMatch(noneMatch))
//            .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))
//
//        when:
//        bu.upload(defaultFlux, defaultDataSize, null, null, bac, null).blockingGet()
//
//        then:
//        def e = thrown(StorageException)
//        e.errorCode() == StorageErrorCode.CONDITION_NOT_MET ||
//            e.errorCode() == StorageErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION
//
//        where:
//        modified | unmodified | match       | noneMatch    | leaseID
//        newDate  | null       | null        | null         | null
//        null     | oldDate    | null        | null         | null
//        null     | null       | garbageEtag | null         | null
//        null     | null       | null        | receivedEtag | null
//        null     | null       | null        | null         | garbageLeaseID
//    }

//    def "Upload error"() {
//        setup:
//        bu = cu.createBlockBlobURL(generateBlobName())
//
//        when:
//        bu.upload(defaultFlux, defaultDataSize, null, null,
//            new BlobAccessConditions().withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId("id")),
//            null).blockingGet()
//
//        then:
//        thrown(StorageException)
//    }
//
//    def "Upload context"() {
//        setup:
//        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(201, BlockBlobUploadHeaders)))
//
//        bu = bu.withPipeline(pipeline)
//
//        when:
//        // No service call is made. Just satisfy the parameters.
//        bu.upload(defaultFlux, defaultDataSize, null, null, null, defaultContext).blockingGet()
//
//        then:
//        notThrown(RuntimeException)
//    }
}
