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
package com.microsoft.azure.storage.blob;

import com.microsoft.azure.storage.blob.models.*;
import com.microsoft.rest.v2.http.HttpPipeline;
import io.reactivex.Flowable;
import io.reactivex.Single;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;

import static com.microsoft.azure.storage.blob.Utility.*;

/**
 * Represents a URL to a block blob. It may be obtained by direct construction or via the create method on a
 * {@link ContainerURL} object. This class does not hold any state about a particular blob but is instead a convenient
 * way of sending off appropriate requests to the resource on the service. Please refer to the
 * <a href=https://docs.microsoft.com/en-us/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs>Azure Docs</a>
 * for more information on block blobs.
 *
 */
public final class BlockBlobURL extends BlobURL {

    /**
     * Indicates the maximum number of bytes that can be sent in a call to upload.
     */
    public static final int MAX_PUT_BLOB_BYTES = 256 * Constants.MB;

    /**
     * Indicates the maximum number of bytes that can be sent in a call to stageBlock.
     */
    public static final int MAX_STAGE_BLOCK_BYTES = 100 * Constants.MB;

    /**
     * Indicates the maximum number of blocks allowed in a block blob.
     */
    public static final int MAX_BLOCKS = 50000;

    /**
     * Creates a new {@link BlockBlobURL} object.
     *
     * @param url
     *      A {@code java.net.URL} to a block blob.
     * @param pipeline
     *      An {@link HttpPipeline} for sending requests.
     */
    public BlockBlobURL(URL url, HttpPipeline pipeline) {
        super(url, pipeline);
    }

    /**
     * Creates a new {@link BlockBlobURL} with the given pipeline.
     *
     * @param pipeline
     *      An {@link HttpPipeline} object to set.
     * @return
     *      A {@link BlockBlobURL} object with the given pipeline.
     */
    public BlockBlobURL withPipeline(HttpPipeline pipeline) {
        try {
            return new BlockBlobURL(new URL(this.storageClient.url()), pipeline);
        } catch (MalformedURLException e) {
            // TODO: remove
        }
        return null;
    }

    /**
     * Creates a new {@link BlockBlobURL} with the given snapshot.
     *
     * @param snapshot
     *      A {@code String} of the snapshot identifier.
     * @return
     *      A {@link BlockBlobURL} object with the given pipeline.
     */
    public BlockBlobURL withSnapshot(String snapshot) throws MalformedURLException, UnknownHostException {
        BlobURLParts blobURLParts = URLParser.parse(new URL(this.storageClient.url()));
        blobURLParts.snapshot = snapshot;
        return new BlockBlobURL(blobURLParts.toURL(), super.storageClient.httpPipeline());
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     * Updating an existing block blob overwrites any existing metadata on the blob. Partial updates are not
     * supported with PutBlob; the content of the existing blob is overwritten with the new content. To
     * perform a partial update of a block blob's, use PutBlock and PutBlockList.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     *
     * For more efficient bulk-upload scenarios, please refer to the {@link TransferManager} for convenience methods.
     *
     * @apiNote
     * ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=upload_download "Sample code for BlockBlobURL.upload")] \n
     * For more samples, please see the [Samples file] (https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     *
     * @param data
     *      The data to write to the blob.
     * @param length
     *      The exact length of the data. It is important that this value match precisely the length of the data
     *      emitted by the {@code Flowable}.
     * @param headers
     *      {@link BlobHTTPHeaders}
     * @param metadata
     *      {@link Metadata}
     * @param accessConditions
     *      {@link BlobAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<BlockBlobUploadResponse> upload(
            Flowable<ByteBuffer> data, long length, BlobHTTPHeaders headers, Metadata metadata,
            BlobAccessConditions accessConditions) {
        headers = headers == null ? BlobHTTPHeaders.NONE : headers;
        metadata = metadata == null ? Metadata.NONE : metadata;
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;
        return addErrorWrappingToSingle(this.storageClient.generatedBlockBlobs().uploadWithRestResponseAsync(
                data, length, null,
                headers.getContentType(),
                headers.getContentEncoding(),
                headers.getContentLanguage(),
                headers.getContentMD5(),
                headers.getCacheControl(),
                metadata,
                accessConditions.getLeaseAccessConditions().getLeaseId(),
                headers.getContentDisposition(),
                accessConditions.getHttpAccessConditions().getIfModifiedSince(),
                accessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                accessConditions.getHttpAccessConditions().getIfMatch().toString(),
                accessConditions.getHttpAccessConditions().getIfNoneMatch().toString(),
                null));
    }

    /**
     * Uploads the specified block to the block blob's "staging area" to be later committed by a call to
     * commitBlockList. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block">Azure Docs</a>.
     *
     * @apiNote
     * ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=blocks "Sample code for BlockBlobURL.stageBlock")] \n
     * For more samples, please see the [Samples file] (https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     *
     * @param base64BlockID
     *      A Base64 encoded {@code String} that specifies the ID for this block. Note that all block ids for a given
     *      blob must be the same length.
     * @param data
     *      The data to write to the block.
     * @param length
     *      The exact length of the data. It is important that this value match precisely the length of the data
     *      emitted by the {@code Flowable}.
     * @param leaseAccessConditions
     *      {@link LeaseAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<BlockBlobStageBlockResponse> stageBlock(
            String base64BlockID, Flowable<ByteBuffer> data, long length, LeaseAccessConditions leaseAccessConditions) {
        leaseAccessConditions = leaseAccessConditions == null ? LeaseAccessConditions.NONE : leaseAccessConditions;

        return addErrorWrappingToSingle(this.storageClient.generatedBlockBlobs().stageBlockWithRestResponseAsync(
                base64BlockID, length, data,null, leaseAccessConditions.getLeaseId(), null));
    }

    /**
     * Creates a new block to be committed as part of a blob where the contents are read from a URL. For more
     * information, see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/put-block-from-url">Azure Docs</a>.
     *
     * @apiNote
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=block_from_url "Sample code for BlockBlobURL.stageBlockFromURL")]
     *
     * @param base64BlockID
     *      A Base64 encoded {@code String} that specifies the ID for this block. Note that all block ids for a given
     *      blob must be the same length.
     * @param sourceURL
     *      The url to the blob that will be the source of the copy.  A source blob in the same storage account can be
     *      authenticated via Shared Key. However, if the source is a blob in another account, the source blob must
     *      either be public or must be authenticated via a shared access signature. If the source blob is public, no
     *      authentication is required to perform the operation.
     * @param sourceRange
     *      {@link BlobRange}
     * @param sourceContentMD5
     *      An MD5 hash of the block content from the source blob. If specified, the service will calculate the MD5 of
     *      the received data and fail the request if it does not match the provided MD5.
     * @param leaseAccessConditions
     *      {@link LeaseAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<BlockBlobStageBlockFromURLResponse> stageBlockFromURL(
            String base64BlockID, URL sourceURL, BlobRange sourceRange, byte[] sourceContentMD5,
            LeaseAccessConditions leaseAccessConditions) {
        leaseAccessConditions = leaseAccessConditions == null ? LeaseAccessConditions.NONE : leaseAccessConditions;
        sourceRange = sourceRange == null ? BlobRange.DEFAULT : sourceRange;

        return addErrorWrappingToSingle(
                this.storageClient.generatedBlockBlobs().stageBlockFromURLWithRestResponseAsync(
                        base64BlockID, 0, sourceURL, sourceRange.toString(), sourceContentMD5,
                        null, leaseAccessConditions.getLeaseId(), null));
    }

    /**
     * Returns the list of blocks that have been uploaded as part of a block blob using the specified block list filter.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-block-list">Azure Docs</a>.
     *
     * @apiNote
     * ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=blocks "Sample code for BlockBlobURL.getBlockList")] \n
     * For more samples, please see the [Samples file] (https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     *
     * @param listType
     *      Specifies which type of blocks to return.
     * @param leaseAccessConditions
     *      {@link LeaseAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<BlockBlobGetBlockListResponse> getBlockList(
            BlockListType listType, LeaseAccessConditions leaseAccessConditions) {
        leaseAccessConditions = leaseAccessConditions == null ? LeaseAccessConditions.NONE : leaseAccessConditions;

        return addErrorWrappingToSingle(this.storageClient.generatedBlockBlobs().getBlockListWithRestResponseAsync(
                listType, null, null, leaseAccessConditions.getLeaseId(), null));
    }

    /**
     * Writes a blob by specifying the list of block IDs that are to make up the blob.
     * In order to be written as part of a blob, a block must have been successfully written
     * to the server in a prior stageBlock operation. You can call commitBlockList to update a blob
     * by uploading only those blocks that have changed, then committing the new and existing
     * blocks together. Any blocks not specified in the block list and permanently deleted.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block-list">Azure Docs</a>.
     *
     * For more efficient bulk-upload scenarios, please refer to the {@link TransferManager} for convenience methods.
     *
     * @apiNote
     * ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=blocks "Sample code for BlockBlobURL.commitBlockList")] \n
     * For more samples, please see the [Samples file] (https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     *
     * @param base64BlockIDs
     *      A list of base64 encode {@code String}s that specifies the block IDs to be committed.
     * @param headers
     *      {@link BlobHTTPHeaders}
     * @param metadata
     *      {@link Metadata}
     * @param accessConditions
     *      {@link BlobAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<BlockBlobCommitBlockListResponse> commitBlockList(
            List<String> base64BlockIDs, BlobHTTPHeaders headers, Metadata metadata,
            BlobAccessConditions accessConditions) {
        headers = headers == null ? BlobHTTPHeaders.NONE : headers;
        metadata = metadata == null ? Metadata.NONE : metadata;
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;
        return addErrorWrappingToSingle(this.storageClient.generatedBlockBlobs().commitBlockListWithRestResponseAsync(
                new BlockLookupList().withLatest(base64BlockIDs), null,
                headers.getCacheControl(),
                headers.getContentType(),
                headers.getContentEncoding(),
                headers.getContentLanguage(),
                headers.getContentMD5(),
                metadata,
                accessConditions.getLeaseAccessConditions().getLeaseId(),
                headers.getContentDisposition(),
                accessConditions.getHttpAccessConditions().getIfModifiedSince(),
                accessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                accessConditions.getHttpAccessConditions().getIfMatch().toString(),
                accessConditions.getHttpAccessConditions().getIfNoneMatch().toString(), null));
    }

    //TODO: stageBlockFromURL
}
