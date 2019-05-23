// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.blob;

/**
 * Configures the parallel download behavior for methods on the {@link TransferManager}.
 */
public final class TransferManagerDownloadFromBlobOptions {

    private final long chunkSize;

    private final IProgressReceiver progressReceiver;

    private final int parallelism;

    private final ReliableDownloadOptions reliableDownloadOptionsPerBlock;

    // Cannot be final because we may have to set this property in order to lock on the etag.
    private BlobAccessConditions accessConditions;

    public TransferManagerDownloadFromBlobOptions() {
        this(null, null, null, null, null);
    }

    /**
     * Returns an object that configures the parallel download behavior for methods on the {@link TransferManager}.
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
     * @param reliableDownloadOptions
     *         {@link ReliableDownloadOptions}
     * @param parallelism
     *         A {@code int} that indicates the maximum number of chunks to download in parallel. Must be greater
     *         than 0. May be null to accept default behavior.
     */
    public TransferManagerDownloadFromBlobOptions(Long chunkSize, IProgressReceiver progressReceiver,
            BlobAccessConditions accessConditions, ReliableDownloadOptions reliableDownloadOptions,
            Integer parallelism) {
        this.progressReceiver = progressReceiver;

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

        this.accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;
        this.reliableDownloadOptionsPerBlock = reliableDownloadOptions == null
                ? new ReliableDownloadOptions() : reliableDownloadOptions;
    }

    /**
     * The size of the chunk into which large download operations will be broken into. Note that if the chunkSize is
     * large, fewer but larger requests will be made as each REST request will download a single chunk in full. For
     * larger chunk sizes, it may be helpful to configure the{@code reliableDownloadOptions} to allow more retries.
     */
    public long chunkSize() {
        return chunkSize;
    }

    /**
     * {@link IProgressReceiver}
     */
    public IProgressReceiver progressReceiver() {
        return progressReceiver;
    }

    /**
     * A {@code int} that indicates the maximum number of chunks to download in parallel. Must be greater than 0. May be
     * null to accept default behavior.
     */
    public int parallelism() {
        return parallelism;
    }

    /**
     * {@link ReliableDownloadOptions}
     */
    public ReliableDownloadOptions reliableDownloadOptionsPerBlock() {
        return reliableDownloadOptionsPerBlock;
    }

    /**
     * {@link BlobAccessConditions}
     */
    public BlobAccessConditions accessConditions() {
        return accessConditions;
    }
}
