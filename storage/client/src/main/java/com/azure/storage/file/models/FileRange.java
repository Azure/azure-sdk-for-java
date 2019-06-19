// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.models;

public final class FileRange {
    private long start;
    private long end;

    public FileRange(final long start, final long end) {
        this.start = start;
        this.end = end;
    }

    public long start() {
        return start;
    }

    public long start(final long start) {
        this.start = start;
        return this.start;
    }

    public long end() {
        return end;
    }

    public long end(final long end) {
        this.end = end;
        return this.end;
    }

    @Override
    public String toString() {
        return "bytes=" + start + "=" + end;
    }
}
