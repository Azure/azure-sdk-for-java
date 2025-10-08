// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;

/**
 * Optional parameters for content validation using structured message decoding.
 */
@Fluent
public class DownloadContentValidationOptions {
    private boolean enableStructuredMessageDecoding;
    private long expectedContentLength;

    /**
     * Creates a new instance of {@link DownloadContentValidationOptions}.
     */
    public DownloadContentValidationOptions() {
        this.enableStructuredMessageDecoding = false;
    }

    /**
     * Gets whether structured message decoding is enabled.
     *
     * @return {@code true} if structured message decoding is enabled, {@code false} otherwise.
     */
    public boolean isStructuredMessageDecodingEnabled() {
        return enableStructuredMessageDecoding;
    }

    /**
     * Sets whether structured message decoding is enabled.
     *
     * @param enableStructuredMessageDecoding {@code true} to enable structured message decoding, {@code false} to disable.
     * @return The updated options.
     */
    public DownloadContentValidationOptions
        setStructuredMessageDecodingEnabled(boolean enableStructuredMessageDecoding) {
        this.enableStructuredMessageDecoding = enableStructuredMessageDecoding;
        return this;
    }

    /**
     * Gets the expected content length for decoding.
     *
     * @return The expected content length.
     */
    public long getExpectedContentLength() {
        return expectedContentLength;
    }

    /**
     * Sets the expected content length for decoding.
     *
     * @param expectedContentLength The expected content length.
     * @return The updated options.
     */
    public DownloadContentValidationOptions setExpectedContentLength(long expectedContentLength) {
        this.expectedContentLength = expectedContentLength;
        return this;
    }
}
