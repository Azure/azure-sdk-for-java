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

import com.microsoft.azure.storage.blob.models.BlobHTTPHeaders;

/**
 * Configures the parallel upload behavior for methods on the {@code TransferManager}.
 */
public class TransferManagerUploadToBlockBlobOptions {

    /**
     * An object which represents the default parallel upload options.
     */
    public static final TransferManagerUploadToBlockBlobOptions DEFAULT = new TransferManagerUploadToBlockBlobOptions(null,
            null, null, null, null);

    private final IProgressReceiver progressReceiver;

    private final BlobHTTPHeaders httpHeaders;

    private final Metadata metadata;

    private final BlobAccessConditions accessConditions;

    private final int parallelism;

    public IProgressReceiver progressReceiver() {
        return progressReceiver;
    }

    public BlobHTTPHeaders httpHeaders() {
        return httpHeaders;
    }

    public Metadata metadata() {
        return metadata;
    }

    public BlobAccessConditions accessConditions() {
        return accessConditions;
    }

    public int parallelism() {
        return parallelism;
    }

    /**
     * Creates a new object that configures the parallel upload behavior. Null may be passed to accept the default
     * behavior.
     *
     * @param progressReceiver
     *         An object that implements the {@link IProgressReceiver} interface which will be invoked periodically
     *         as bytes are sent in a PutBlock call to the BlockBlobURL. May be null if no progress reports are
     *         desired.
     * @param httpHeaders
     *         Most often used when creating a blob or setting its properties, this class contains fields for typical HTTP
     *         properties, which, if specified, will be attached to the target blob. Null may be passed to any API which takes this
     *         type to indicate that no properties should be set.
     * @param metadata
     *         {@link Metadata}
     * @param accessConditions
     *         {@link BlobAccessConditions}
     * @param parallelism
     *         A {@code int} that indicates the maximum number of blocks to upload in parallel. Must be greater than
     *         0. May be null to accept default behavior.
     */
    public TransferManagerUploadToBlockBlobOptions(IProgressReceiver progressReceiver, BlobHTTPHeaders httpHeaders,
            Metadata metadata, BlobAccessConditions accessConditions, Integer parallelism) {
        if (parallelism == null) {
            this.parallelism = Constants.TRANSFER_MANAGER_DEFAULT_PARALLELISM;
        } else if (parallelism <= 0) {
            throw new IllegalArgumentException("Parallelism must be > 0");
        } else {
            this.parallelism = parallelism;
        }

        this.progressReceiver = progressReceiver;
        this.httpHeaders = httpHeaders;
        this.metadata = metadata;
        this.accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;
    }
}
