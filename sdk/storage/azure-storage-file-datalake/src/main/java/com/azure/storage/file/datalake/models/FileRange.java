// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import java.util.Locale;

/**
 * This is a representation of a range of bytes on a file, typically used during a download operation. This type is
 * immutable to ensure thread-safety of requests, so changing the values for a different operation requires construction
 * of a new object. Passing null as a FileRange value will default to the entire range of the file.
 */
public final class FileRange {
    private static final String RANGE_HEADER_FORMAT = "bytes=%d-%d";
    private static final String BEGIN_RANGE_HEADER_FORMAT = "bytes=%d-";

    private final long offset;
    private final Long count;

    /**
     * Specifies the download operation to start from the offset position (zero-based) and download the rest of the
     * entire file to the end.
     *
     * @param offset the zero-based position to start downloading
     * @throws IllegalArgumentException If {@code offset} is less than {@code 0}.
     */
    public FileRange(long offset) {
        this(offset, null);
    }

    /**
     * Specifies the download operation to start from the offset position (zero-based) and download the count number of
     * bytes.
     *
     * @param offset the zero-based position to start downloading
     * @param count the number of bytes to download
     * @throws IllegalArgumentException If {@code offset} or {@code count} is less than {@code 0}.
     */
    public FileRange(long offset, Long count) {
        if (offset < 0) {
            throw new IllegalArgumentException("FileRange offset must be greater than or equal to 0.");
        }
        this.offset = offset;

        if (count != null && count < 0) {
            throw new IllegalArgumentException("FileRange count must be greater than or equal to 0 if specified.");
        }
        this.count = count;
    }

    /**
     * The start of the range. Must be greater than or equal to 0.
     *
     * @return the offset for the range
     */
    public long getOffset() {
        return offset;
    }

    /**
     * How many bytes to include in the range. Must be greater than or equal to 0 if specified.
     *
     * @return the number bytes to include in the range
     */
    public Long getCount() {
        return count;
    }

    /**
     * @return A {@code String} compliant with the format of the Azure Storage x-ms-range and Range headers.
     */
    @Override
    public String toString() {
        if (this.count != null) {
            long rangeEnd = this.offset + this.count - 1;
            return String.format(Locale.ROOT, RANGE_HEADER_FORMAT, this.offset, rangeEnd);
        }

        return String.format(Locale.ROOT, BEGIN_RANGE_HEADER_FORMAT, this.offset);
    }

    /**
     * @return {@link FileRange#toString()} if {@code count} isn't {@code null} or {@code offset} isn't 0, otherwise
     * null.
     */
    public String toHeaderValue() {
        // The default values of a FileRange
        if (this.offset == 0 && this.count == null) {
            return null;
        }
        return this.toString();
    }
}
