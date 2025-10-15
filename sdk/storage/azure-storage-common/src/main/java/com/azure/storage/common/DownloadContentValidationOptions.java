// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common;

import com.azure.core.annotation.Fluent;

/**
 * Options for content validation during download operations.
 */
@Fluent
public final class DownloadContentValidationOptions {
    private boolean enableStructuredMessageValidation;
    private boolean enableMd5Validation;

    /**
     * Creates a new instance of DownloadContentValidationOptions.
     */
    public DownloadContentValidationOptions() {
        this.enableStructuredMessageValidation = false;
        this.enableMd5Validation = false;
    }

    /**
     * Gets whether structured message validation is enabled.
     *
     * @return true if structured message validation is enabled, false otherwise.
     */
    public boolean isStructuredMessageValidationEnabled() {
        return enableStructuredMessageValidation;
    }

    /**
     * Sets whether structured message validation is enabled.
     * When enabled, downloads will use CRC64 checksums embedded in structured messages for content validation.
     *
     * @param enableStructuredMessageValidation true to enable structured message validation, false to disable.
     * @return The updated DownloadContentValidationOptions object.
     */
    public DownloadContentValidationOptions
        setStructuredMessageValidationEnabled(boolean enableStructuredMessageValidation) {
        this.enableStructuredMessageValidation = enableStructuredMessageValidation;
        return this;
    }

    /**
     * Gets whether MD5 validation is enabled.
     *
     * @return true if MD5 validation is enabled, false otherwise.
     */
    public boolean isMd5ValidationEnabled() {
        return enableMd5Validation;
    }

    /**
     * Sets whether MD5 validation is enabled.
     * When enabled, downloads will use MD5 checksums for content validation.
     *
     * @param enableMd5Validation true to enable MD5 validation, false to disable.
     * @return The updated DownloadContentValidationOptions object.
     */
    public DownloadContentValidationOptions setMd5ValidationEnabled(boolean enableMd5Validation) {
        this.enableMd5Validation = enableMd5Validation;
        return this;
    }
}
