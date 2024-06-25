// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.http;

import com.azure.core.v2.annotation.Immutable;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.util.ClientLogger;

import java.util.Objects;

/**
 * <p>Represents a range of bytes within an HTTP resource.</p>
 *
 * <p>This class encapsulates a range of bytes that can be requested from an HTTP resource. The range starts at the
 * {@link #getOffset()} inclusively and ends at {@link #getOffset()} + {@link #getLength()} exclusively, or offset + length - 1.</p>
 *
 * <p>If {@link #getLength()} is unspecified or null, the range extends to the end of the HTTP resource.</p>
 *
 * <p>This class is useful when you want to request a specific range of bytes from an HTTP resource, such as a part of a file.
 * For example, you can use it to download a part of a file, to resume a download, or to stream a video from a specific point.</p>
 *
 * @see HttpRequest
 */
@Immutable
public final class HttpRange {
    // HttpRange can be a highly used, short-lived class, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(HttpRange.class);

    private final long offset;
    private final Long length;

    /**
     * Creates an instance of {@link HttpRange}.
     * <p>
     * This creates a range which has an unbounded length starting at the specified {@code offset}.
     *
     * @param offset The offset to begin the range.
     * @throws IllegalArgumentException If {@code offset} is less than 0.
     */
    public HttpRange(long offset) {
        this(offset, null);
    }

    /**
     * Creates an instance of {@link HttpRange}.
     *
     * @param offset The offset to begin the range.
     * @param length The length of the range.
     * @throws IllegalArgumentException If {@code offset} is less than 0 or {@code length} is non-null and is less than
     * or equal to 0.
     */
    public HttpRange(long offset, Long length) {
        if (offset < 0) {
            throw LOGGER.logThrowableAsError(new IllegalArgumentException("'offset' cannot be less than 0."));
        }

        if (length != null && length <= 0) {
            throw LOGGER
                .logThrowableAsError(new IllegalArgumentException("'length' cannot be equal to or less than 0."));
        }

        this.offset = offset;
        this.length = length;
    }

    /**
     * Gets the offset of the range.
     *
     * @return Offset of the range.
     */
    public long getOffset() {
        return offset;
    }

    /**
     * Gets the length of the range.
     * <p>
     * If the length is null the range continues to the end of the HTTP resource.
     *
     * @return Length of the range or null if range continues to the end of the HTTP resource.
     */
    public Long getLength() {
        return length;
    }

    @Override
    public int hashCode() {
        return Objects.hash(offset, length);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HttpRange)) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        HttpRange other = (HttpRange) obj;
        return other.offset == this.offset && Objects.equals(other.length, this.length);
    }

    /**
     * Gets the string representation of the range.
     * <p>
     * If length is null the returned string will be {@code "bytes=<offset>-"}, if length is not null the returned
     * string will be {@code "bytes=<offset>-<offset + length - 1>"}.
     *
     * @return The string representation of the range.
     */
    @Override
    public String toString() {
        return (length == null) ? "bytes=" + offset + "-" : "bytes=" + offset + "-" + (offset + length - 1);
    }
}
