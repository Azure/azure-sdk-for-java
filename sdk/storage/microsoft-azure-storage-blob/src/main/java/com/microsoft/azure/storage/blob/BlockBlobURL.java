// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.blob;

import com.microsoft.azure.storage.blob.models.BlobHTTPHeaders;
import com.microsoft.azure.storage.blob.models.BlockBlobCommitBlockListResponse;
import com.microsoft.azure.storage.blob.models.BlockBlobGetBlockListResponse;
import com.microsoft.azure.storage.blob.models.BlockBlobStageBlockFromURLResponse;
import com.microsoft.azure.storage.blob.models.BlockBlobStageBlockResponse;
import com.microsoft.azure.storage.blob.models.BlockBlobUploadResponse;
import com.microsoft.azure.storage.blob.models.BlockListType;
import com.microsoft.azure.storage.blob.models.BlockLookupList;
import com.microsoft.azure.storage.blob.models.LeaseAccessConditions;
import com.microsoft.azure.storage.blob.models.SourceModifiedAccessConditions;
import com.microsoft.rest.v2.Context;
import com.microsoft.rest.v2.http.HttpPipeline;
import io.reactivex.Flowable;
import io.reactivex.Single;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;

import static com.microsoft.azure.storage.blob.Utility.postProcessResponse;

/**
 * Represents a URL to a block blob. It may be obtained by direct construction or via the create method on a
 * {@link ContainerURL} object. This class does not hold any state about a particular blob but is instead a convenient
 * way of sending off appropriate requests to the resource on the service. Please refer to the
 * <a href=https://docs.microsoft.com/en-us/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs>Azure Docs</a>
 * for more information on block blobs.
 */
public final class BlockBlobURL extends BlobURL {

    /**
     * Indicates the maximum number of bytes that can be sent in a call to upload.
     */
    public static final int MAX_UPLOAD_BLOB_BYTES = 256 * Constants.MB;

    /**
     * Indicates the maximum number of bytes that can be sent in a call to stageBlock.
     */
    public static final int MAX_STAGE_BLOCK_BYTES = 100 * Constants.MB;

    /**
     * Indicates the maximum number of blocks allowed in a block blob.
     */
    public static final int MAX_BLOCKS = 50000;

    /**
     * Creates a {@code BlockBlobURL} object pointing to the account specified by the URL and using the provided
     * pipeline to make HTTP requests.
     *
     * @param url
     *         A {@code URL} to an Azure Storage block blob.
     * @param pipeline
     *         A {@code HttpPipeline} which configures the behavior of HTTP exchanges. Please refer to
     *         {@link StorageURL#createPipeline(ICredentials, PipelineOptions)} for more information.
     */
    public BlockBlobURL(URL url, HttpPipeline pipeline) {
        super(url, pipeline);
    }

    /**
     * Creates a new {@link BlockBlobURL} with the given pipeline.
     *
     * @param pipeline
     *         An {@link HttpPipeline} object to set.
     *
     * @return A {@link BlockBlobURL} object with the given pipeline.
     */
    public BlockBlobURL withPipeline(HttpPipeline pipeline) {
        try {
            return new BlockBlobURL(new URL(this.storageClient.url()), pipeline);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new {@link BlockBlobURL} with the given snapshot.
     *
     * @param snapshot
     *         A {@code String} of the snapshot identifier.
     *
     * @return A {@link BlockBlobURL} object with the given pipeline.
     */
    public BlockBlobURL withSnapshot(String snapshot) throws MalformedURLException, UnknownHostException {
        BlobURLParts blobURLParts = URLParser.parse(new URL(this.storageClient.url()));
        blobURLParts.withSnapshot(snapshot);
        return new BlockBlobURL(blobURLParts.toURL(), super.storageClient.httpPipeline());
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     * Updating an existing block blob overwrites any existing metadata on the blob. Partial updates are not
     * supported with PutBlob; the content of the existing blob is overwritten with the new content. To
     * perform a partial update of a block blob's, use PutBlock and PutBlockList.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flowable} must produce the same data each time it is subscribed to.
     * <p>
     * For more efficient bulk-upload scenarios, please refer to the {@link TransferManager} for convenience methods.
     *
     * @param data
     *         The data to write to the blob. Note that this {@code Flowable} must be replayable if retries are enabled
     *         (the default). In other words, the Flowable must produce the same data each time it is subscribed to.
     * @param length
     *         The exact length of the data. It is important that this value match precisely the length of the data
     *         emitted by the {@code Flowable}.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=upload_download "Sample code for BlockBlobURL.upload")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Single<BlockBlobUploadResponse> upload(Flowable<ByteBuffer> data, long length) {
        return this.upload(data, length, null, null, null, null);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     * Updating an existing block blob overwrites any existing metadata on the blob. Partial updates are not
     * supported with PutBlob; the content of the existing blob is overwritten with the new content. To
     * perform a partial update of a block blob's, use PutBlock and PutBlockList.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flowable} must produce the same data each time it is subscribed to.
     * <p>
     * For more efficient bulk-upload scenarios, please refer to the {@link TransferManager} for convenience methods.
     *
     * @param data
     *         The data to write to the blob. Note that this {@code Flowable} must be replayable if retries are enabled
     *         (the default). In other words, the Flowable must produce the same data each time it is subscribed to.
     * @param length
     *         The exact length of the data. It is important that this value match precisely the length of the data
     *         emitted by the {@code Flowable}.
     * @param headers
     *         {@link BlobHTTPHeaders}
     * @param metadata
     *         {@link Metadata}
     * @param accessConditions
     *         {@link BlobAccessConditions}
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.microsoft.rest.v2.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=upload_download "Sample code for BlockBlobURL.upload")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Single<BlockBlobUploadResponse> upload(Flowable<ByteBuffer> data, long length, BlobHTTPHeaders headers,
            Metadata metadata, BlobAccessConditions accessConditions, Context context) {
        metadata = metadata == null ? new Metadata() : metadata;
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;
        context = context == null ? Context.NONE : context;

        return postProcessResponse(this.storageClient.generatedBlockBlobs().uploadWithRestResponseAsync(context,
                data, length, null, metadata, null, headers, accessConditions.leaseAccessConditions(),
                accessConditions.modifiedAccessConditions()));
    }

    /**
     * Uploads the specified block to the block blob's "staging area" to be later committed by a call to
     * commitBlockList. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block">Azure Docs</a>.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flowable} must produce the same data each time it is subscribed to.
     *
     * @param base64BlockID
     *         A Base64 encoded {@code String} that specifies the ID for this block. Note that all block ids for a given
     *         blob must be the same length.
     * @param data
     *         The data to write to the block. Note that this {@code Flowable} must be replayable if retries are enabled
     *         (the default). In other words, the Flowable must produce the same data each time it is subscribed to.
     * @param length
     *         The exact length of the data. It is important that this value match precisely the length of the data
     *         emitted by the {@code Flowable}.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=blocks "Sample code for BlockBlobURL.stageBlock")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Single<BlockBlobStageBlockResponse> stageBlock(String base64BlockID, Flowable<ByteBuffer> data,
            long length) {
        return this.stageBlock(base64BlockID, data, length, null, null);
    }

    /**
     * Uploads the specified block to the block blob's "staging area" to be later committed by a call to
     * commitBlockList. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block">Azure Docs</a>.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flowable} must produce the same data each time it is subscribed to.
     *
     * @param base64BlockID
     *         A Base64 encoded {@code String} that specifies the ID for this block. Note that all block ids for a given
     *         blob must be the same length.
     * @param data
     *         The data to write to the block. Note that this {@code Flowable} must be replayable if retries are enabled
     *         (the default). In other words, the Flowable must produce the same data each time it is subscribed to.
     * @param length
     *         The exact length of the data. It is important that this value match precisely the length of the data
     *         emitted by the {@code Flowable}.
     * @param leaseAccessConditions
     *         By setting lease access conditions, requests will fail if the provided lease does not match the active
     *         lease on the blob.
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.microsoft.rest.v2.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=blocks "Sample code for BlockBlobURL.stageBlock")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Single<BlockBlobStageBlockResponse> stageBlock(String base64BlockID, Flowable<ByteBuffer> data, long length,
            LeaseAccessConditions leaseAccessConditions, Context context) {
        context = context == null ? Context.NONE : context;

        return postProcessResponse(this.storageClient.generatedBlockBlobs().stageBlockWithRestResponseAsync(
                context, base64BlockID, length, data, null, null, null, leaseAccessConditions));
    }

    /**
     * Creates a new block to be committed as part of a blob where the contents are read from a URL. For more
     * information, see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/put-block-from-url">Azure Docs</a>.
     *
     * @param base64BlockID
     *         A Base64 encoded {@code String} that specifies the ID for this block. Note that all block ids for a given
     *         blob must be the same length.
     * @param sourceURL
     *         The url to the blob that will be the source of the copy.  A source blob in the same storage account can be
     *         authenticated via Shared Key. However, if the source is a blob in another account, the source blob must
     *         either be public or must be authenticated via a shared access signature. If the source blob is public, no
     *         authentication is required to perform the operation.
     * @param sourceRange
     *         {@link BlobRange}
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=block_from_url "Sample code for BlockBlobURL.stageBlockFromURL")]
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Single<BlockBlobStageBlockFromURLResponse> stageBlockFromURL(String base64BlockID, URL sourceURL,
            BlobRange sourceRange) {
        return this.stageBlockFromURL(base64BlockID, sourceURL, sourceRange, null,
                null, null, null);
    }

    /**
     * Creates a new block to be committed as part of a blob where the contents are read from a URL. For more
     * information, see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/put-block-from-url">Azure Docs</a>.
     *
     * @param base64BlockID
     *         A Base64 encoded {@code String} that specifies the ID for this block. Note that all block ids for a given
     *         blob must be the same length.
     * @param sourceURL
     *         The url to the blob that will be the source of the copy.  A source blob in the same storage account can
     *         be authenticated via Shared Key. However, if the source is a blob in another account, the source blob
     *         must either be public or must be authenticated via a shared access signature. If the source blob is
     *         public, no authentication is required to perform the operation.
     * @param sourceRange
     *         {@link BlobRange}
     * @param sourceContentMD5
     *         An MD5 hash of the block content from the source blob. If specified, the service will calculate the MD5
     *         of the received data and fail the request if it does not match the provided MD5.
     * @param leaseAccessConditions
     *         By setting lease access conditions, requests will fail if the provided lease does not match the active
     *         lease on the blob.
     * @param sourceModifiedAccessConditions
     *         {@link SourceModifiedAccessConditions}
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.microsoft.rest.v2.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=block_from_url "Sample code for BlockBlobURL.stageBlockFromURL")]
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Single<BlockBlobStageBlockFromURLResponse> stageBlockFromURL(String base64BlockID, URL sourceURL,
            BlobRange sourceRange, byte[] sourceContentMD5, LeaseAccessConditions leaseAccessConditions,
            SourceModifiedAccessConditions sourceModifiedAccessConditions, Context context) {
        sourceRange = sourceRange == null ? new BlobRange() : sourceRange;
        context = context == null ? Context.NONE : context;

        return postProcessResponse(
                this.storageClient.generatedBlockBlobs().stageBlockFromURLWithRestResponseAsync(context,
                        base64BlockID, 0, sourceURL, sourceRange.toHeaderValue(), sourceContentMD5,
                        null, null, leaseAccessConditions, sourceModifiedAccessConditions));
    }

    /**
     * Returns the list of blocks that have been uploaded as part of a block blob using the specified block list filter.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-block-list">Azure Docs</a>.
     *
     * @param listType
     *         Specifies which type of blocks to return.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=blocks "Sample code for BlockBlobURL.getBlockList")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Single<BlockBlobGetBlockListResponse> getBlockList(BlockListType listType) {
        return this.getBlockList(listType, null, null);
    }

    /**
     * Returns the list of blocks that have been uploaded as part of a block blob using the specified block list filter.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-block-list">Azure Docs</a>.
     *
     * @param listType
     *         Specifies which type of blocks to return.
     * @param leaseAccessConditions
     *         By setting lease access conditions, requests will fail if the provided lease does not match the active
     *         lease on the blob.
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.microsoft.rest.v2.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=blocks "Sample code for BlockBlobURL.getBlockList")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Single<BlockBlobGetBlockListResponse> getBlockList(BlockListType listType,
            LeaseAccessConditions leaseAccessConditions, Context context) {
        context = context == null ? Context.NONE : context;

        return postProcessResponse(this.storageClient.generatedBlockBlobs().getBlockListWithRestResponseAsync(
                context, listType, null, null, null, leaseAccessConditions));
    }

    /**
     * Writes a blob by specifying the list of block IDs that are to make up the blob.
     * In order to be written as part of a blob, a block must have been successfully written
     * to the server in a prior stageBlock operation. You can call commitBlockList to update a blob
     * by uploading only those blocks that have changed, then committing the new and existing
     * blocks together. Any blocks not specified in the block list and permanently deleted.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block-list">Azure Docs</a>.
     * <p>
     * For more efficient bulk-upload scenarios, please refer to the {@link TransferManager} for convenience methods.
     *
     * @param base64BlockIDs
     *         A list of base64 encode {@code String}s that specifies the block IDs to be committed.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=blocks "Sample code for BlockBlobURL.commitBlockList")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Single<BlockBlobCommitBlockListResponse> commitBlockList(List<String> base64BlockIDs) {
        return this.commitBlockList(base64BlockIDs, null, null, null, null);
    }

    /**
     * Writes a blob by specifying the list of block IDs that are to make up the blob.
     * In order to be written as part of a blob, a block must have been successfully written
     * to the server in a prior stageBlock operation. You can call commitBlockList to update a blob
     * by uploading only those blocks that have changed, then committing the new and existing
     * blocks together. Any blocks not specified in the block list and permanently deleted.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block-list">Azure Docs</a>.
     * <p>
     * For more efficient bulk-upload scenarios, please refer to the {@link TransferManager} for convenience methods.
     *
     * @param base64BlockIDs
     *         A list of base64 encode {@code String}s that specifies the block IDs to be committed.
     * @param headers
     *         {@link BlobHTTPHeaders}
     * @param metadata
     *         {@link Metadata}
     * @param accessConditions
     *         {@link BlobAccessConditions}
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.microsoft.rest.v2.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=blocks "Sample code for BlockBlobURL.commitBlockList")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Single<BlockBlobCommitBlockListResponse> commitBlockList(List<String> base64BlockIDs,
            BlobHTTPHeaders headers, Metadata metadata, BlobAccessConditions accessConditions, Context context) {
        metadata = metadata == null ? new Metadata() : metadata;
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;
        context = context == null ? Context.NONE : context;

        return postProcessResponse(this.storageClient.generatedBlockBlobs().commitBlockListWithRestResponseAsync(
                context, new BlockLookupList().withLatest(base64BlockIDs), null,
                metadata, null, headers, accessConditions.leaseAccessConditions(),
                accessConditions.modifiedAccessConditions()));
    }
}
