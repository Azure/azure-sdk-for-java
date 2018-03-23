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

import com.microsoft.azure.storage.models.*;
import com.microsoft.rest.v2.http.HttpPipeline;
import com.microsoft.rest.v2.RestResponse;
import io.reactivex.Flowable;
import io.reactivex.Single;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Represents a URL to a block blob.
 */
public final class BlockBlobURL extends BlobURL {

    /**
     * Indicates the maximum number of bytes that can be sent in a call to putBlob.
     */
    public static final int MAX_PUT_BLOB_BYTES = 256 * Constants.MB;

    /**
     * Indicates the maximum number of bytes that can be sent in a call to putBlock.
     */
    public static final int MAX_PUT_BLOCK_BYTES = 100 * Constants.MB;

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
     * For more information, see https://docs.microsoft.com/rest/api/storageservices/put-blob.
     *
     * @param data
     *      A {@link Flowable} emitting {@link ByteBuffer} which contain the data to write to the blob.
     * @param length
     *      A {@code long} indicating how long the data is.
     * @param headers
     *      A {@link BlobHTTPHeaders} object that specifies which properties to set on the blob.
     * @param metadata
     *      A {@link Metadata} object that specifies key value pairs to set on the blob.
     * @param accessConditions
     *      A {@link BlobAccessConditions} object that specifies under which conditions the operation should
     *      complete.
     * @return
     *      The {@link Single} which emits a {@link RestResponse} containing the {@link BlobPutHeaders} and a
     *      {@code Void} body if successful.
     */
    public Single<RestResponse<BlobPutHeaders, Void>> putBlob(
            Flowable<ByteBuffer> data, long length, BlobHTTPHeaders headers, Metadata metadata,
            BlobAccessConditions accessConditions) {
        headers = headers == null ? BlobHTTPHeaders.NONE : headers;
        metadata = metadata == null ? Metadata.NONE : metadata;
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;
        // TODO: Metadata protocol layer broken.
        return this.storageClient.blobs().putWithRestResponseAsync(length, BlobType.BLOCK_BLOB, data,
                null, headers.getContentType(), headers.getContentEncoding(),
                headers.getContentLanguage(), headers.getContentMD5(), headers.getCacheControl(), metadata,
                accessConditions.getLeaseAccessConditions().getLeaseId(),
                headers.getContentDisposition(),
                accessConditions.getHttpAccessConditions().getIfModifiedSince(),
                accessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                accessConditions.getHttpAccessConditions().getIfMatch().toString(),
                accessConditions.getHttpAccessConditions().getIfNoneMatch().toString(),
                null, null, null);
    }

    /**
     * Uploads the specified block to the block blob's "staging area" to be later commited by a call to
     * PutBlockList. For more information, see https://docs.microsoft.com/rest/api/storageservices/put-block.
     *
     * @param base64BlockID
     *      A Base64 encoded {@code String} that specifies the ID for this block.
     * @param data
     *      A {@link Flowable} of {@link ByteBuffer} which contains the data to write to the block.
     * @param length
     *      A {@code long} indicating how long the data is.
     * @param leaseAccessConditions
     *      A {@link LeaseAccessConditions} object that specifies the lease on the blob if there is one.
     * @return
     *      The {@link Single} which emits a {@link RestResponse} containing the {@link BlockBlobPutBlockHeaders} and a
     *      {@code Void} body if successful.
     */
    public Single<RestResponse<BlockBlobPutBlockHeaders, Void>> putBlock(
            String base64BlockID, Flowable<ByteBuffer> data, long length,
            LeaseAccessConditions leaseAccessConditions) {
        leaseAccessConditions = leaseAccessConditions == null ? LeaseAccessConditions.NONE : leaseAccessConditions;
        return this.storageClient.blockBlobs().putBlockWithRestResponseAsync(base64BlockID, length, data,
                null, leaseAccessConditions.getLeaseId(), null);
    }

    /**
     * Returns the list of blocks that have been uploaded as part of a block blob using the specified block
     * list filter. For more information, see https://docs.microsoft.com/rest/api/storageservices/get-block-list.
     *
     * @param listType
     *      A {@link BlockListType} value specifies which type of blocks to return.
     * @param leaseAccessConditions
     *      A {@link LeaseAccessConditions} object that specifies the lease on the blob if there is one.
     * @return
     *      The {@link Single} which emits a {@link RestResponse} containing the {@link BlockBlobGetBlockListHeaders}
     *      and a {@link BlockList} body if successful.
     */
    public Single<RestResponse<BlockBlobGetBlockListHeaders, BlockList>> getBlockList(
            BlockListType listType, LeaseAccessConditions leaseAccessConditions) {
        leaseAccessConditions = leaseAccessConditions == null ? LeaseAccessConditions.NONE : leaseAccessConditions;
        return this.storageClient.blockBlobs().getBlockListWithRestResponseAsync(listType,
                null, null, leaseAccessConditions.getLeaseId(), null);
    }

    /**
     * Writes a blob by specifying the list of block IDs that make up the blob.
     * In order to be written as part of a blob, a block must have been successfully written
     * to the server in a prior PutBlock operation. You can call PutBlockList to update a blob
     * by uploading only those blocks that have changed, then committing the new and existing
     * blocks together. Any blocks not specified in the block list and permanently deleted.
     * For more information, see https://docs.microsoft.com/rest/api/storageservices/put-block-list.
     *
     * @param base64BlockIDs
     *      A {@code java.util.List} of base64 {@code String} that specifies the block IDs to be committed.
     * @param headers
     *      A {@link BlobHTTPHeaders} object that specifies which properties to set on the blob.
     * @param metadata
     *      A {@link Metadata} object that specifies key value pairs to set on the blob.
     * @param accessConditions
     *      A {@link BlobAccessConditions} object that specifies under which conditions the operation should
     *      complete.
     * @return
     *      The {@link Single} which emits a {@link RestResponse} containing the {@link BlockBlobPutBlockListHeaders}
     *      and a {@code Void} body if successful.
     */
    // TODO: Add Content-Length to swagger once the modeler knows to hide (or whatever solution).
    public Single<RestResponse<BlockBlobPutBlockListHeaders, Void>> putBlockList(
            List<String> base64BlockIDs, BlobHTTPHeaders headers, Metadata metadata,
            BlobAccessConditions accessConditions) {
        headers = headers == null ? BlobHTTPHeaders.NONE : headers;
        metadata = metadata == null ? Metadata.NONE : metadata;
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;
        return this.storageClient.blockBlobs().putBlockListWithRestResponseAsync(
                new BlockLookupList().withLatest(base64BlockIDs), null,
                headers.getCacheControl(), headers.getContentType(),headers.getContentEncoding(),
                headers.getContentLanguage(), headers.getContentMD5(), metadata,
                accessConditions.getLeaseAccessConditions().getLeaseId(), headers.getContentDisposition(),
                accessConditions.getHttpAccessConditions().getIfModifiedSince(),
                accessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                accessConditions.getHttpAccessConditions().getIfMatch().toString(),
                accessConditions.getHttpAccessConditions().getIfNoneMatch().toString(), null);
    }
}
