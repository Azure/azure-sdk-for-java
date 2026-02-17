// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Tracks the history of status codes for a bulk operation, including successes, failures, and retries.
 * Consecutive operations with the same (statusCode, subStatusCode) pair are compressed into a single entry
 * with a count and time range.
 */
public final class BulkOperationStatusTracker {

    private final List<StatusCodeEntry> entries;
    private int lastStatusCode;
    private int lastSubStatusCode;

    public BulkOperationStatusTracker() {
        this.entries = new ArrayList<>();
        this.lastStatusCode = -1;
        this.lastSubStatusCode = -1;
    }

    /**
     * Records a status code and sub-status code for this operation.
     * If the last entry has the same codes, increments its count and updates endTime.
     * Otherwise, creates a new entry.
     */
    public void recordStatusCode(int statusCode, int subStatusCode) {
        if (!this.entries.isEmpty()
            && this.lastStatusCode == statusCode
            && this.lastSubStatusCode == subStatusCode) {

            StatusCodeEntry last = this.entries.get(this.entries.size() - 1);
            last.record();
            return;
        }

        this.lastStatusCode = statusCode;
        this.lastSubStatusCode = subStatusCode;
        this.entries.add(new StatusCodeEntry(statusCode, subStatusCode));
    }

    @Override
    public String toString() {
        if (this.entries.isEmpty()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < this.entries.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(this.entries.get(i).toString());
        }
        sb.append("]");
        return sb.toString();
    }

    static final class StatusCodeEntry {
        private final int statusCode;
        private final int subStatusCode;
        private int count;
        private final Instant startTime;
        private Instant endTime;

        StatusCodeEntry(int statusCode, int subStatusCode) {
            this.statusCode = statusCode;
            this.subStatusCode = subStatusCode;
            this.count = 1;
            this.startTime = Instant.now();
            this.endTime = this.startTime;
        }

        void record() {
            this.count += 1;
            this.endTime = Instant.now();
        }

        @Override
        public String toString() {
            return "(" + this.statusCode + "/" + this.subStatusCode
                + ", count=" + this.count
                + ", start=" + this.startTime
                + ", end=" + this.endTime + ")";
        }
    }
}
