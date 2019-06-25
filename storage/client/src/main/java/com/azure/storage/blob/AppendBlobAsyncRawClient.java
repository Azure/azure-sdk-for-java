// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.util.Context;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.models.AppendBlobsAppendBlockFromUrlResponse;
import com.azure.storage.blob.models.AppendBlobsAppendBlockResponse;
import com.azure.storage.blob.models.AppendBlobsCreateResponse;
import com.azure.storage.blob.models.BlobHTTPHeaders;
import com.azure.storage.blob.models.SourceModifiedAccessConditions;
import io.netty.buffer.ByteBuf;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;

import static com.azure.storage.blob.Utility.postProcessResponse;


/**
 * Represents a URL to an append blob. It may be obtained by direct construction or via the create method on a
 * {@link ContainerAsyncClient} object. This class does not hold any state about a particular append blob but is instead a
 * convenient way of sending off appropriate requests to the resource on the service. Please refer to the
 * <a href=https://docs.microsoft.com/en-us/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs>Azure Docs</a>
 */
final class AppendBlobAsyncRawClient extends BlobAsyncRawClient {

    /**
     * Indicates the maximum number of bytes that can be sent in a call to appendBlock.
     */
    public static final int MAX_APPEND_BLOCK_BYTES = 4 * Constants.MB;

    /**
     * Indicates the maximum number of blocks allowed in an append blob.
     */
    public static final int MAX_BLOCKS = 50000;

    /**
     * Creates a {@code AppendBlobAsyncRawClient} object pointing to the account specified by the URL and using the provided
     * pipeline to make HTTP requests.
     */
    AppendBlobAsyncRawClient(AzureBlobStorageImpl azureBlobStorage) {
        super(azureBlobStorage);
    }

    /**
     * Creates a 0-length append blob. Call AppendBlock to append data to an append blob. For more information, see
     * the <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     *
     * @return Emits the successful response.
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=append_blob "Sample code for AppendBlobAsyncRawClient.create")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<AppendBlobsCreateResponse> create() {
        return this.create(null, null, null);
    }

    /**
     * Creates a 0-length append blob. Call AppendBlock to append data to an append blob. For more information, see
     * the <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     *
     * @param headers          {@link BlobHTTPHeaders}
     * @param metadata         {@link Metadata}
     * @param accessConditions {@link BlobAccessConditions}
     * @return Emits the successful response.
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=append_blob "Sample code for AppendBlobAsyncRawClient.create")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<AppendBlobsCreateResponse> create(BlobHTTPHeaders headers, Metadata metadata,
                                                  BlobAccessConditions accessConditions) {
        metadata = metadata == null ? new Metadata() : metadata;
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;

        return postProcessResponse(this.azureBlobStorage.appendBlobs().createWithRestResponseAsync(null,
            null, 0, null, metadata, null, null,
            null, null, headers, accessConditions.leaseAccessConditions(),
            accessConditions.modifiedAccessConditions(), Context.NONE));
    }

    /**
     * Commits a new block of data to the end of the existing append blob. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/append-block">Azure Docs</a>.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     *
     * @param data   The data to write to the blob. Note that this {@code Flux} must be replayable if retries are enabled
     *               (the default). In other words, the Flowable must produce the same data each time it is subscribed to.
     * @param length The exact length of the data. It is important that this value match precisely the length of the data
     *               emitted by the {@code Flux}.
     * @return Emits the successful response.
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=append_blob "Sample code for AppendBlobAsyncRawClient.appendBlock")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<AppendBlobsAppendBlockResponse> appendBlock(Flux<ByteBuf> data, long length) {
        return this.appendBlock(data, length, null);
    }

    /**
     * Commits a new block of data to the end of the existing append blob. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/append-block">Azure Docs</a>.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     *
     * @param data                       The data to write to the blob. Note that this {@code Flux} must be replayable if retries are enabled
     *                                   (the default). In other words, the Flowable must produce the same data each time it is subscribed to.
     * @param length                     The exact length of the data. It is important that this value match precisely the length of the data
     *                                   emitted by the {@code Flux}.
     * @param appendBlobAccessConditions {@link AppendBlobAccessConditions}
     * @return Emits the successful response.
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=append_blob "Sample code for AppendBlobAsyncRawClient.appendBlock")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<AppendBlobsAppendBlockResponse> appendBlock(Flux<ByteBuf> data, long length,
                                                            AppendBlobAccessConditions appendBlobAccessConditions) {
        appendBlobAccessConditions = appendBlobAccessConditions == null ? new AppendBlobAccessConditions()
            : appendBlobAccessConditions;

        return postProcessResponse(this.azureBlobStorage.appendBlobs().appendBlockWithRestResponseAsync(
            null, null, data, length, null, null,
            null, null, null, null,
            appendBlobAccessConditions.leaseAccessConditions(),
            appendBlobAccessConditions.appendPositionAccessConditions(),
            appendBlobAccessConditions.modifiedAccessConditions(), Context.NONE));
    }

    /**
     * Commits a new block of data from another blob to the end of this append blob. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/append-block">Azure Docs</a>.
     * <p>
     *
     * @param sourceURL   The url to the blob that will be the source of the copy.  A source blob in the same storage account can
     *                    be authenticated via Shared Key. However, if the source is a blob in another account, the source blob
     *                    must either be public or must be authenticated via a shared access signature. If the source blob is
     *                    public, no authentication is required to perform the operation.
     * @param sourceRange The source {@link BlobRange} to copy.
     * @return Emits the successful response.
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=append_from_url "Sample code for AppendBlobAsyncRawClient.appendBlockFromUrl")]
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<AppendBlobsAppendBlockFromUrlResponse> appendBlockFromUrl(URL sourceURL, BlobRange sourceRange) {
        return this.appendBlockFromUrl(sourceURL, sourceRange, null, null,
            null);
    }

    /**
     * Commits a new block of data from another blob to the end of this append blob. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/append-block">Azure Docs</a>.
     * <p>
     *
     * @param sourceURL              The url to the blob that will be the source of the copy.  A source blob in the same storage account can
     *                               be authenticated via Shared Key. However, if the source is a blob in another account, the source blob
     *                               must either be public or must be authenticated via a shared access signature. If the source blob is
     *                               public, no authentication is required to perform the operation.
     * @param sourceRange            {@link BlobRange}
     * @param sourceContentMD5       An MD5 hash of the block content from the source blob. If specified, the service will calculate the MD5
     *                               of the received data and fail the request if it does not match the provided MD5.
     * @param destAccessConditions   {@link AppendBlobAccessConditions}
     * @param sourceAccessConditions {@link SourceModifiedAccessConditions}
     * @return Emits the successful response.
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=append_from_url "Sample code for AppendBlobAsyncRawClient.appendBlockFromUrl")]
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<AppendBlobsAppendBlockFromUrlResponse> appendBlockFromUrl(URL sourceURL, BlobRange sourceRange,
                                                                          byte[] sourceContentMD5, AppendBlobAccessConditions destAccessConditions,
                                                                          SourceModifiedAccessConditions sourceAccessConditions) {

        sourceRange = sourceRange == null ? new BlobRange(0) : sourceRange;
        destAccessConditions = destAccessConditions == null
            ? new AppendBlobAccessConditions() : destAccessConditions;

        return postProcessResponse(
            this.azureBlobStorage.appendBlobs().appendBlockFromUrlWithRestResponseAsync(null, null,
                sourceURL, 0, sourceRange.toString(), sourceContentMD5, null, null,
                destAccessConditions.leaseAccessConditions(),
                destAccessConditions.appendPositionAccessConditions(),
                destAccessConditions.modifiedAccessConditions(), sourceAccessConditions, Context.NONE));
    }
}
