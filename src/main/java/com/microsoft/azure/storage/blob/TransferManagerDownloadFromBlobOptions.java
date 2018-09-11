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

/**
 * Configures the parallel download behavior for methods on the {@code TransferManager}.
 */
public final class TransferManagerDownloadFromBlobOptions {

    /**
     * The default download options.
     */
    public static final TransferManagerDownloadFromBlobOptions DEFAULT =
            new TransferManagerDownloadFromBlobOptions(null, null, null, null, null);

    private final long chunkSize;

    private final IProgressReceiver progressReceiver;
    private final int parallelism;
    private final ReliableDownloadOptions reliableDownloadOptionsPerBlock;
    // Cannot be final because we may have to set this property in order to lock on the etag.
    private BlobAccessConditions accessConditions;

    public long chunkSize() {
        return chunkSize;
    }

    public IProgressReceiver progressReceiver() {
        return progressReceiver;
    }

    public int parallelism() {
        return parallelism;
    }

    public ReliableDownloadOptions reliableDownloadOptionsPerBlock() {
        return reliableDownloadOptionsPerBlock;
    }

    public BlobAccessConditions accessConditions() {
        return accessConditions;
    }

    /**
     * Returns an object that configures the parallel download behavior for methods on the {@code TransferManager}.
     *
     * @param chunkSize
     *         The size of the chunk into which large download operations will be broken into. Note that if the
     *         chunkSize is large, fewer but larger requests will be made as each REST request will download a
     *         single chunk in full. For larger chunk sizes, it may be helpful to configure the
     *         {@code reliableDownloadOptions} to allow more retries.
     * @param progressReceiver
     *         {@link IProgressReceiver}
     * @param accessConditions
     *         {@link BlobAccessConditions}
     * @param parallelism
     *         A {@code int} that indicates the maximum number of chunks to download in parallel. Must be greater
     *         than 0. May be null to accept default behavior.
     * @param reliableDownloadOptions
     *         {@link ReliableDownloadOptions}
     */
    public TransferManagerDownloadFromBlobOptions(Long chunkSize, IProgressReceiver progressReceiver,
            BlobAccessConditions accessConditions, Integer parallelism, ReliableDownloadOptions reliableDownloadOptions) {
        if (chunkSize != null) {
            Utility.assertInBounds("chunkSize", chunkSize, 1, Long.MAX_VALUE);
            this.chunkSize = chunkSize;
        } else {
            this.chunkSize = TransferManager.BLOB_DEFAULT_DOWNLOAD_BLOCK_SIZE;
        }

        if (parallelism != null) {
            Utility.assertInBounds("parallelism", parallelism, 1, Integer.MAX_VALUE);
            this.parallelism = parallelism;
        } else {
            this.parallelism = Constants.TRANSFER_MANAGER_DEFAULT_PARALLELISM;
        }

        this.accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;
        this.progressReceiver = progressReceiver;
        this.reliableDownloadOptionsPerBlock = reliableDownloadOptions == null ?
                new ReliableDownloadOptions() : reliableDownloadOptions;
    }
}
