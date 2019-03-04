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

import com.microsoft.azure.storage.blob.models.AppendBlobAppendBlockResponse;
import com.microsoft.azure.storage.blob.models.AppendBlobCreateResponse;
import com.microsoft.azure.storage.blob.models.BlobHTTPHeaders;
import com.microsoft.rest.v2.Context;
import com.microsoft.rest.v2.http.HttpPipeline;
import io.reactivex.Flowable;
import io.reactivex.Single;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import static com.microsoft.azure.storage.blob.Utility.addErrorWrappingToSingle;


/**
 * Represents a URL to an append blob. It may be obtained by direct construction or via the create method on a
 * {@link ContainerURL} object. This class does not hold any state about a particular append blob but is instead a
 * convenient way of sending off appropriate requests to the resource on the service. Please refer to the
 * <a href=https://docs.microsoft.com/en-us/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs>Azure Docs</a>
 */
public final class AppendBlobURL extends BlobURL {

    /**
     * Indicates the maximum number of bytes that can be sent in a call to appendBlock.
     */
    public static final int MAX_APPEND_BLOCK_BYTES = 4 * Constants.MB;

    /**
     * Indicates the maximum number of blocks allowed in an append blob.
     */
    public static final int MAX_BLOCKS = 50000;

    /**
     * Creates a {@code AppendBlobURL} object pointing to the account specified by the URL and using the provided
     * pipeline to make HTTP requests.
     *
     * @param url
     *         A {@code URL} to an Azure Storage append blob.
     * @param pipeline
     *         A {@code HttpPipeline} which configures the behavior of HTTP exchanges. Please refer to
     *         {@link StorageURL#createPipeline(ICredentials, PipelineOptions)} for more information.
     */
    public AppendBlobURL(URL url, HttpPipeline pipeline) {
        super(url, pipeline);
    }

    /**
     * Creates a new {@link AppendBlobURL} with the given pipeline.
     *
     * @param pipeline
     *         An {@code HttpPipeline} object to process HTTP transactions.
     *
     * @return An {@code AppendBlobURL} object with the given pipeline.
     */
    public AppendBlobURL withPipeline(HttpPipeline pipeline) {
        try {
            return new AppendBlobURL(new URL(this.storageClient.url()), pipeline);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Creates a new {@code AppendBlobURL} with the given snapshot.
     *
     * @param snapshot
     *         A {@code String} of the snapshot identifier.
     *
     * @return An {@code AppendBlobURL} object with the given pipeline.
     */
    public AppendBlobURL withSnapshot(String snapshot) throws MalformedURLException, UnknownHostException {
        BlobURLParts blobURLParts = URLParser.parse(new URL(this.storageClient.url()));
        blobURLParts.withSnapshot(snapshot);
        return new AppendBlobURL(blobURLParts.toURL(), super.storageClient.httpPipeline());
    }

    /**
     * Creates a 0-length append blob. Call AppendBlock to append data to an append blob. For more information, see
     * the <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=append_blob "Sample code for AppendBlobURL.create")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Single<AppendBlobCreateResponse> create() {
        return this.create(null, null, null, null);
    }

    /**
     * Creates a 0-length append blob. Call AppendBlock to append data to an append blob. For more information, see
     * the <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     *
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
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=append_blob "Sample code for AppendBlobURL.create")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Single<AppendBlobCreateResponse> create(BlobHTTPHeaders headers, Metadata metadata,
            BlobAccessConditions accessConditions, Context context) {
        metadata = metadata == null ? Metadata.NONE : metadata;
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;
        context = context == null ? Context.NONE : context;

        return addErrorWrappingToSingle(this.storageClient.generatedAppendBlobs().createWithRestResponseAsync(context,
                0, null, metadata, null, headers, accessConditions.leaseAccessConditions(),
                accessConditions.modifiedAccessConditions()));
    }

    /**
     * Commits a new block of data to the end of the existing append blob. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/append-block">Azure Docs</a>.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flowable} must produce the same data each time it is subscribed to.
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
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=append_blob "Sample code for AppendBlobURL.appendBlock")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Single<AppendBlobAppendBlockResponse> appendBlock(Flowable<ByteBuffer> data, long length) {
        return this.appendBlock(data, length, null, null);
    }

    /**
     * Commits a new block of data to the end of the existing append blob. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/append-block">Azure Docs</a>.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flowable} must produce the same data each time it is subscribed to.
     *
     * @param data
     *         The data to write to the blob. Note that this {@code Flowable} must be replayable if retries are enabled
     *         (the default). In other words, the Flowable must produce the same data each time it is subscribed to.
     * @param length
     *         The exact length of the data. It is important that this value match precisely the length of the data
     *         emitted by the {@code Flowable}.
     * @param appendBlobAccessConditions
     *         {@link AppendBlobAccessConditions}
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
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=append_blob "Sample code for AppendBlobURL.appendBlock")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Single<AppendBlobAppendBlockResponse> appendBlock(Flowable<ByteBuffer> data, long length,
            AppendBlobAccessConditions appendBlobAccessConditions, Context context) {
        appendBlobAccessConditions = appendBlobAccessConditions == null ? AppendBlobAccessConditions.NONE :
                appendBlobAccessConditions;
        appendBlobAccessConditions = appendBlobAccessConditions == null
                ? AppendBlobAccessConditions.NONE : appendBlobAccessConditions;
        context = context == null ? Context.NONE : context;

        return addErrorWrappingToSingle(this.storageClient.generatedAppendBlobs().appendBlockWithRestResponseAsync(
                context, data, length, null, null, null, appendBlobAccessConditions.leaseAccessConditions(),
                appendBlobAccessConditions.appendPositionAccessConditions(),
                appendBlobAccessConditions.modifiedAccessConditions()));
    }
}
