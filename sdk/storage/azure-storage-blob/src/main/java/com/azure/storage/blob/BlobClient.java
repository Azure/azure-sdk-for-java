// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.rest.Response;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobHTTPHeaders;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.specialized.AppendBlobClient;
import com.azure.storage.blob.specialized.BlobClientBase;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.blob.specialized.PageBlobClient;
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder;
import com.azure.storage.common.Utility;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * This class provides a client that contains generic blob operations for Azure Storage Blobs. Operations allowed by
 * the client are downloading and copying a blob, retrieving and setting metadata, retrieving and setting HTTP headers,
 * and deleting and un-deleting a blob.
 *
 * <p>
 * This client is instantiated through {@link BlobClientBuilder} or retrieved via
 * {@link BlobContainerClient#getBlobClient(String) getBlobClient}.
 *
 * <p>
 * For operations on a specific blob type (i.e append, block, or page) use
 * {@link #getAppendBlobClient() getAppendBlobClient}, {@link #getBlockBlobClient() getBlockBlobClient}, or
 * {@link #getPageBlobClient() getPageBlobClient} to construct a client that allows blob specific operations.
 *
 * <p>
 * Please refer to the <a href=https://docs.microsoft.com/en-us/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs>Azure
 * Docs</a> for more information.
 */
@ServiceClient(builder = BlobClientBuilder.class)
public class BlobClient extends BlobClientBase {
    private final ClientLogger logger = new ClientLogger(BlobClient.class);
    private final BlobAsyncClient client;

    /**
     * Package-private constructor for use by {@link BlobClientBuilder}.
     *
     * @param client the async blob client
     */
    BlobClient(BlobAsyncClient client) {
        super(client);
        this.client = client;
    }

    /**
     * Creates a new {@link BlobClient} linked to the {@code snapshot} of this blob resource.
     *
     * @param snapshot the identifier for a specific snapshot of this blob
     * @return a {@link BlobClient} used to interact with the specific snapshot.
     */
    @Override
    public BlobClient getSnapshotClient(String snapshot) {
        return new BlobClient(client.getSnapshotClient(snapshot));
    }

    /**
     * Creates a new {@link AppendBlobClient} associated to this blob.
     *
     * @return a {@link AppendBlobClient} associated to this blob.
     */
    public AppendBlobClient getAppendBlobClient() {
        return new SpecializedBlobClientBuilder()
            .blobClient(this)
            .buildAppendBlobClient();
    }

    /**
     * Creates a new {@link BlockBlobClient} associated to this blob.
     *
     * @return a {@link BlockBlobClient} associated to this blob.
     */
    public BlockBlobClient getBlockBlobClient() {
        return new SpecializedBlobClientBuilder()
            .blobClient(this)
            .buildBlockBlobClient();
    }

    /**
     * Creates a new {@link PageBlobClient} associated to this blob.
     *
     * @return a {@link PageBlobClient} associated to this blob.
     */
    public PageBlobClient getPageBlobClient() {
        return new SpecializedBlobClientBuilder()
            .blobClient(this)
            .buildPageBlobClient();
    }

    /**
     * Creates a new block blob or updates the content of an existing block blob.
     * <p>
     * Updating an existing block blob overwrites any existing metadata on the blob. Partial updates are not supported
     * with this method; the content of the existing blob is overwritten with the new content. To perform a partial
     * update of a block blob's, use {@link BlockBlobAsyncClient#stageBlock(String, Flux, long) stageBlock} and {@link
     * BlockBlobAsyncClient#commitBlockList(List)}. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block">Azure Docs for Put Block</a> and the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block-list">Azure Docs for Put Block List</a>.
     * <p>
     * The data passed need not support multiple subscriptions/be replayable as is required in other upload methods when
     * retries are enabled, and the length of the data need not be known in advance. Therefore, this method should
     * support uploading any arbitrary data source, including network streams. This behavior is possible because this
     * method will perform some internal buffering as configured by the blockSize and numBuffers parameters, so while
     * this method may offer additional convenience, it will not be as performant as other options, which should be
     * preferred when possible.
     * <p>
     * Typically, the greater the number of buffers used, the greater the possible parallelism when transferring the
     * data. Larger buffers means we will have to stage fewer blocks and therefore require fewer IO operations. The
     * trade-offs between these values are context-dependent, so some experimentation may be required to optimize inputs
     * for a given scenario.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobClient.upload#InputStream-ParallelTransferOptions}
     *
     * @param data The data to write to the blob.
     * @param parallelTransferOptions {@link ParallelTransferOptions} used to configure buffered uploading.
     * @return A reactive response containing the information of the uploaded block blob.
     */
    public BlockBlobItem upload(InputStream data, ParallelTransferOptions parallelTransferOptions) {
        return uploadWithResponse(data, parallelTransferOptions, null, null, null, null, null).getValue();
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob. Updating an existing block blob
     * overwrites any existing metadata on the blob. Partial updates are not supported with this method; the content of
     * the existing blob is overwritten with the new content. To perform a partial update of a block blob's, use {@link
     * BlockBlobAsyncClient#stageBlock(String, Flux, long) stageBlock} and
     * {@link BlockBlobAsyncClient#commitBlockList(List)}, which this method uses internally. For more information,
     * see the <a href="https://docs.microsoft.com/rest/api/storageservices/put-block">Azure
     * Docs for Put Block</a> and the <a href="https://docs.microsoft.com/rest/api/storageservices/put-block-list">Azure
     * Docs for Put Block List</a>.
     * <p>
     * The data passed need not support multiple subscriptions/be replayable as is required in other upload methods when
     * retries are enabled, and the length of the data need not be known in advance. Therefore, this method should
     * support uploading any arbitrary data source, including network streams. This behavior is possible because this
     * method will perform some internal buffering as configured by the blockSize and numBuffers parameters, so while
     * this method may offer additional convenience, it will not be as performant as other options, which should be
     * preferred when possible.
     * <p>
     * Typically, the greater the number of buffers used, the greater the possible parallelism when transferring the
     * data. Larger buffers means we will have to stage fewer blocks and therefore require fewer IO operations. The
     * trade-offs between these values are context-dependent, so some experimentation may be required to optimize inputs
     * for a given scenario.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobClient.uploadWithResponse#InputStream-ParallelTransferOptions-BlobHTTPHeaders-Map-AccessTier-BlobAccessConditions-Duration}
     *
     * @param data The data to write to the blob.
     * @param parallelTransferOptions {@link ParallelTransferOptions} used to configure buffered uploading.
     * @param headers {@link BlobHTTPHeaders}
     * @param metadata Metadata to associate with the blob.
     * @param tier {@link AccessTier} for the destination blob.
     * @param accessConditions {@link BlobAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return A reactive response containing the information of the uploaded block blob.
     */
    public Response<BlockBlobItem> uploadWithResponse(InputStream data, ParallelTransferOptions parallelTransferOptions,
        BlobHTTPHeaders headers, Map<String, String> metadata, AccessTier tier, BlobAccessConditions accessConditions,
        Duration timeout) {

        Flux<ByteBuffer> fbb = Utility.convertStreamToByteBuffer(data, parallelTransferOptions.getBlockSize());

        Mono<Response<BlockBlobItem>> upload = client.uploadWithResponse(fbb, parallelTransferOptions, headers,
            metadata, tier, accessConditions);

        try {
            return Utility.blockWithOptionalTimeout(upload, timeout);
        } catch (UncheckedIOException e) {
            throw logger.logExceptionAsError(e);
        }
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobClient.uploadFromFile#String}
     *
     * @param filePath Path of the file to upload
     * @throws IOException If an I/O error occurs
     */
    public void uploadFromFile(String filePath) throws IOException {
        uploadFromFile(filePath, null, null, null, null, null, null);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobClient.uploadFromFile#String-ParallelTransferOptions-BlobHTTPHeaders-Map-AccessTier-BlobAccessConditions-Duration}
     *
     * @param filePath Path of the file to upload
     * @param parallelTransferOptions {@link ParallelTransferOptions} to use to upload from file. Number of parallel
     *        transfers parameter is ignored.
     * @param headers {@link BlobHTTPHeaders}
     * @param metadata Metadata to associate with the blob.
     * @param tier {@link AccessTier} for the uploaded blob
     * @param accessConditions {@link BlobAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @throws UncheckedIOException If an I/O error occurs
     */
    public void uploadFromFile(String filePath, ParallelTransferOptions parallelTransferOptions,
        BlobHTTPHeaders headers, Map<String, String> metadata, AccessTier tier, BlobAccessConditions accessConditions,
        Duration timeout) {
        Mono<Void> upload = this.client.uploadFromFile(
            filePath, parallelTransferOptions, headers, metadata, tier, accessConditions);

        try {
            Utility.blockWithOptionalTimeout(upload, timeout);
        } catch (UncheckedIOException e) {
            throw logger.logExceptionAsError(e);
        }
    }
}
