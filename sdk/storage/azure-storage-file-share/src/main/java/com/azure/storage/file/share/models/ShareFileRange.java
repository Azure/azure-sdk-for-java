// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.logging.ClientLogger;

import java.util.Locale;

/**
 * The range of a file in the storage file service.
 */
@Immutable
public final class ShareFileRange {
    private static final ClientLogger LOGGER = new ClientLogger(ShareFileRange.class);
    private static final String RANGE_HEADER_FORMAT = "bytes=%d-%d";
    private static final String BEGIN_RANGE_HEADER_FORMAT = "bytes=%d-";
    private final long start;
    private final Long end;

    /**
     * Create an instance of the range of a file. Specify the start the range
     * and the end defaults to the length of the file.
     *
     * @param start Specifies the start of bytes to be written.
     */
    public ShareFileRange(final long start) {
        this(start, null);
    }

    /**
     * Create an instance of the range of a file.  Both the start and end of the range must be specified.
     *
     * @param start Specifies the start of bytes to be written.
     * @param end Specifies the end of bytes to be written
     */
    public ShareFileRange(final long start, final Long end) {
        if (start < 0) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("ShareFileRange offset must be greater than or equal to 0."));
        }
        this.start = start;

        if (end != null && end < 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                new IllegalArgumentException("ShareFileRange end must be greater than or equal to 0 if specified.")));
        }
        this.end = end;
    }

    /**
     * Creates an instance of the range of a file from the passed {@link Range}.
     *
     * @param range Range object containing start and end of the file.
     */
    public ShareFileRange(final Range range) {
        this.start = range.getStart();
        this.end = range.getEnd();
    }

    /**
     * Gets the start of bytes to be written.
     *
     * @return The start of bytes to be written.
     */
    public long getStart() {
        return start;
    }

    /**
     * Gets the end of bytes to be written.
     *
     * @return The end of bytes to be written.
     */
    public Long getEnd() {
        return end;
    }

    /**
     * Gets the string format of the ShareFileRange written into request.
     *
     * @return The string format of the ShareFileRange written into request.
     */
    @Override
    public String toString() {
        if (this.end != null) {
            return String.format(Locale.ROOT, RANGE_HEADER_FORMAT, this.start, this.end);
        }

        return String.format(Locale.ROOT, BEGIN_RANGE_HEADER_FORMAT, this.start);
    }

    /**
     * Gets {@link #toString()} if {@code count} isn't {@code null} or {@code offset} isn't 0, otherwise null.
     *
     * @return {@link #toString()} if {@code count} isn't {@code null} or {@code offset} isn't 0, otherwise null.
     */
    public String toHeaderValue() {
        // The default values of a BlobRange
        if (this.start == 0 && this.end == null) {
            return null;
        }
        return this.toString();
    }
}
