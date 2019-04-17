// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.blob;

import com.microsoft.azure.storage.blob.models.BlobHTTPHeaders;

/**
 * Configures the parallel upload behavior for methods on the {@link TransferManager}.
 */
public class TransferManagerUploadToBlockBlobOptions {

    /**
     * An object which represents the default parallel upload options.
     */
    public static final TransferManagerUploadToBlockBlobOptions DEFAULT = new TransferManagerUploadToBlockBlobOptions(
            null, null, null, null, null);

    private final IProgressReceiver progressReceiver;

    private final BlobHTTPHeaders httpHeaders;

    private final Metadata metadata;

    private final BlobAccessConditions accessConditions;

    private final int parallelism;

    /**
     * Creates a new object that configures the parallel upload behavior. Null may be passed to accept the default
     * behavior.
     *
     * @param progressReceiver
     *         {@link IProgressReceiver}
     * @param httpHeaders
     *         Most often used when creating a blob or setting its properties, this class contains fields for typical
     *         HTTP properties, which, if specified, will be attached to the target blob. Null may be passed to any API.
     * @param metadata
     *         {@link Metadata}
     * @param accessConditions
     *         {@link BlobAccessConditions}
     * @param parallelism
     *         A {@code int} that indicates the maximum number of blocks to upload in parallel. Must be greater than 0.
     *         May be null to accept default behavior.
     */
    public TransferManagerUploadToBlockBlobOptions(IProgressReceiver progressReceiver, BlobHTTPHeaders httpHeaders,
            Metadata metadata, BlobAccessConditions accessConditions, Integer parallelism) {
        this.progressReceiver = progressReceiver;
        if (parallelism == null) {
            this.parallelism = Constants.TRANSFER_MANAGER_DEFAULT_PARALLELISM;
        } else if (parallelism <= 0) {
            throw new IllegalArgumentException("Parallelism must be > 0");
        } else {
            this.parallelism = parallelism;
        }

        this.httpHeaders = httpHeaders;
        this.metadata = metadata;
        this.accessConditions = accessConditions == null ? BlobAccessConditions.NONE : accessConditions;
    }

    /**
     * {@link IProgressReceiver}
     */
    public IProgressReceiver progressReceiver() {
        return progressReceiver;
    }

    /**
     * Most often used when creating a blob or setting its properties, this class contains fields for typical HTTP
     * properties, which, if specified, will be attached to the target blob. Null may be passed to any API.
     */
    public BlobHTTPHeaders httpHeaders() {
        return httpHeaders;
    }

    /**
     * {@link Metadata}
     */
    public Metadata metadata() {
        return metadata;
    }

    /**
     * {@link BlobAccessConditions}
     */
    public BlobAccessConditions accessConditions() {
        return accessConditions;
    }

    /**
     * A {@code int} that indicates the maximum number of blocks to upload in parallel. Must be greater than 0. May be
     * null to accept default behavior.
     */
    public int parallelism() {
        return parallelism;
    }
}
