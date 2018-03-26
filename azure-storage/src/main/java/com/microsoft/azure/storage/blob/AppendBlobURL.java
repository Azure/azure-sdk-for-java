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


/**
 * Represents a URL to an append blob. It may be obtained by direct construction or via the create method on a
 * {@link ContainerURL} object. This class does not hold any state about a particular append blob but is instead a
 * convenient way of sending off appropriate requests to the resource on the service. Please refer to the following for
 * more information on append blobs:
 * https://docs.microsoft.com/en-us/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs
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
     * Creates a new {@code AppendBlobURL} object with its own pipeline.
     *
     * @param url
     *      A {@code URL} to an append blob.
     * @param pipeline
     *      An {@code HttpPipeline} for sending requests. Please refer to {@link StorageURL} for how to generate a
     *      default pipeline. Pipelines may also be created with custom policies if desired.
     */
    public AppendBlobURL(URL url, HttpPipeline pipeline) {
        super(url, pipeline);
    }

    /**
     * Creates a new {@link AppendBlobURL} with the given pipeline.
     *
     * @param pipeline
     *      An {@code HttpPipeline} object to process HTTP transactions.
     * @return
     *      An {@code AppendBlobURL} object with the given pipeline.
     */
    public AppendBlobURL withPipeline(HttpPipeline pipeline) {
        try {
            return new AppendBlobURL(new URL(this.storageClient.url()), pipeline);
        }
        catch (MalformedURLException e) {
            //TODO: remove
        }
        return null;
    }

    /**
     * Creates a new {@code AppendBlobURL} with the given snapshot.
     *
     * @param snapshot
     *      A {@code String} of the snapshot identifier.
     * @return
     *      An {@code AppendBlobURL} object with the given pipeline.
     */
    public AppendBlobURL withSnapshot(String snapshot) throws MalformedURLException, UnknownHostException {
        BlobURLParts blobURLParts = URLParser.parse(new URL(this.storageClient.url()));
        blobURLParts.snapshot = snapshot;
        return new AppendBlobURL(blobURLParts.toURL(), super.storageClient.httpPipeline());
    }

    /**
     * Creates a 0-length append blob. Call AppendBlock to append data to an append blob. For more information, see
     * the <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     *
     * @param headers
     *      {@link BlobHTTPHeaders}
     * @param metadata
     *      {@link Metadata}
     * @param accessConditions
     *      {@link BlobAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<AppendBlobCreateResponse> create(
            BlobHTTPHeaders headers, Metadata metadata, BlobAccessConditions accessConditions) {
        headers = headers == null ? BlobHTTPHeaders.NONE : headers;
        metadata = metadata == null ? Metadata.NONE : metadata;
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;

        return this.storageClient.generatedAppendBlobs().createWithRestResponseAsync(0, null,
                headers.getContentType(), headers.getContentEncoding(),
                headers.getContentLanguage(), headers.getContentMD5(), headers.getCacheControl(), metadata,
                accessConditions.getLeaseAccessConditions().getLeaseId(),
                headers.getContentDisposition(),
                accessConditions.getHttpAccessConditions().getIfModifiedSince(),
                accessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                accessConditions.getHttpAccessConditions().getIfMatch().toString(),
                accessConditions.getHttpAccessConditions().getIfNoneMatch().toString(),
                null);
    }

    /**
     * Commits a new block of data to the end of the existing append blob. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/append-block">Azure Docs</a>.
     *
     * @param data
     *      The data to write to the blob.
     * @param length
     *      The total length of the data.
     * @param accessConditions
     *      {@link BlobAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<AppendBlobAppendBlockResponse> appendBlock(
            Flowable<ByteBuffer> data, long length, BlobAccessConditions accessConditions) {
        accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;

        return this.storageClient.generatedAppendBlobs().appendBlockWithRestResponseAsync(data, length, null,
                accessConditions.getLeaseAccessConditions().getLeaseId(),
                accessConditions.getAppendBlobAccessConditions().getIfMaxSizeLessThanOrEqual(),
                accessConditions.getAppendBlobAccessConditions().getIfAppendPositionEquals(),
                accessConditions.getHttpAccessConditions().getIfModifiedSince(),
                accessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                accessConditions.getHttpAccessConditions().getIfMatch().toString(),
                accessConditions.getHttpAccessConditions().getIfNoneMatch().toString(),
                null);
    }
}
