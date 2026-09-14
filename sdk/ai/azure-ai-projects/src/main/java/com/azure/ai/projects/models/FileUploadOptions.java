// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.models;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/** Options for uploading local files to project-managed blob storage. */
@Fluent
public final class FileUploadOptions {
    private Pattern filePattern;
    private Consumer<BlobContainerClientBuilder> blobClientConfiguration;
    private Consumer<BlobParallelUploadOptions> blobUploadConfiguration;

    /** Creates upload options with no filename filter and overwrite enabled. */
    public FileUploadOptions() {
    }

    /**
     * Gets the pattern searched against each filename during folder uploads.
     * @return the pattern, or null to upload all files.
     */
    public Pattern getFilePattern() {
        return filePattern;
    }

    /**
     * Sets a pattern searched against filenames, not their relative paths. Ignored for a single file.
     * @param filePattern the pattern, or null for all files.
     * @return these options.
     */
    public FileUploadOptions setFilePattern(Pattern filePattern) {
        this.filePattern = filePattern;
        return this;
    }

    /**
     * Gets the blob client configuration callback.
     * @return the callback, or null.
     */
    public Consumer<BlobContainerClientBuilder> getBlobClientConfiguration() {
        return blobClientConfiguration;
    }

    /**
     * Configures the blob client's transport, retry and logging options. The service-provided SAS endpoint is
     * applied after this callback. Do not configure project credentials on this client.
     * @param configuration the callback, or null for defaults.
     * @return these options.
     */
    public FileUploadOptions setBlobClientConfiguration(Consumer<BlobContainerClientBuilder> configuration) {
        this.blobClientConfiguration = configuration;
        return this;
    }

    /**
     * Gets the callback applied to each file's blob upload options.
     * @return the callback, or null.
     */
    public Consumer<BlobParallelUploadOptions> getBlobUploadConfiguration() {
        return blobUploadConfiguration;
    }

    /**
     * Configures each upload's headers, metadata, transfer settings and request conditions. Uploads overwrite
     * existing blobs by default; set an If-None-Match condition of "*" to reject existing blobs.
     * @param configuration the callback, or null for defaults. A fresh options instance is supplied for each file.
     * @return these options.
     */
    public FileUploadOptions setBlobUploadConfiguration(Consumer<BlobParallelUploadOptions> configuration) {
        this.blobUploadConfiguration = configuration;
        return this;
    }
}
