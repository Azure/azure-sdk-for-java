// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

public class PageBlobRange {
    private final long start;
    private final long end;
    private final boolean isClear;

    public PageBlobRange(long start, long end, boolean isClear) {
        this.start = start;
        this.end = end;
        this.isClear = isClear;
    }

    public long getStart() {
        return this.start;
    }

    public long getEnd() {
        return this.end;
    }

    public boolean isClear() {
        return this.isClear;
    }
}
