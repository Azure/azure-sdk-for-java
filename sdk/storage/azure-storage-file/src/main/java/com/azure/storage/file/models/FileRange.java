// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.models;

import java.util.Objects;

/**
 * The range of a file in the storage file service.
 */
public final class FileRange {
    private final long start;
    private final Long end;

    /**
     * Create an instance of the range of a file.  Both the start and end of the range must be specified.
     * @param start Specifies the start of bytes to be written.
     * @param end Specifies the end of bytes to be written
     */
    public FileRange(final long start, final Long end) {
        this.start = start;
        this.end = end;
    }

    /**
     * @return The start of bytes to be written.
     */
    public long start() {
        return start;
    }

    /**
     * @return The end of bytes to be written.
     */
    public Long end() {
        return end;
    }

    /**
     * @return The string format of the FileRange written into request.
     */
    @Override
    public String toString() {
        String endString = Objects.toString(end);
        return "bytes=" + String.valueOf(start) + "-" + endString;
    }
}
