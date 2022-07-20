// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

/**
 * The options for the downloadTo operations.
 */
public class DownloadToFileOptions {

    /**
     * Parallel download options object.
     */
    private ParallelDownloadOptions parallelDownloadOptions;

    /**
     * Overwrite If file/stream exists
     */
    private boolean overwrite;

    /**
     * Get the ParallelDownloadOptions
     * @return ParallelDownloadOptions
     */
    public ParallelDownloadOptions getParallelDownloadOptions() {
        return parallelDownloadOptions;
    }

    /**
     * Overwrite value.
     * @return the overwrite value.
     */
    public boolean isOverwrite() {
        return overwrite;
    }

    /**
     * Set the parallel download options.
     *
     * @param parallelDownloadOptions parallel download options.
     * @return the DownloadToOptions object itself.
     */
    public DownloadToFileOptions setParallelDownloadOptions(ParallelDownloadOptions parallelDownloadOptions) {
        this.parallelDownloadOptions = parallelDownloadOptions;
        return this;
    }

    /**
     * Set the overwrite value.
     *
     * @param overwrite the overwrite value.
     * @return the DownloadToOptions object itself.
     */
    public DownloadToFileOptions setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
        return this;
    }
}
