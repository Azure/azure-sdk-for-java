// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BulkOperationStatusTrackerTest {

    @Test(groups = { "unit" })
    public void emptyTrackerShouldHaveZeroCount() {
        BulkOperationStatusTracker tracker = new BulkOperationStatusTracker();

        assertThat(tracker.toString()).isEqualTo("[]");
    }

    @Test(groups = { "unit" })
    public void singleStatusCodeShouldCreateOneEntry() {
        BulkOperationStatusTracker tracker = new BulkOperationStatusTracker();

        tracker.recordStatusCode(429, 3200);

        assertThat(tracker.toString()).contains("429/3200");
        assertThat(tracker.toString()).contains("count=1");
    }

    @Test(groups = { "unit" })
    public void consecutiveIdenticalCodesShouldCompress() {
        BulkOperationStatusTracker tracker = new BulkOperationStatusTracker();

        tracker.recordStatusCode(429, 3200);
        tracker.recordStatusCode(429, 3200);
        tracker.recordStatusCode(429, 3200);

        assertThat(tracker.toString()).contains("429/3200");
        assertThat(tracker.toString()).contains("count=3");
    }

    @Test(groups = { "unit" })
    public void differentCodesShouldCreateSeparateEntries() {
        BulkOperationStatusTracker tracker = new BulkOperationStatusTracker();

        tracker.recordStatusCode(429, 3200);
        tracker.recordStatusCode(410, 1002);

        String result = tracker.toString();
        assertThat(result).contains("429/3200");
        assertThat(result).contains("410/1002");
    }

    @Test(groups = { "unit" })
    public void mixedCodesShouldCompressConsecutiveRuns() {
        BulkOperationStatusTracker tracker = new BulkOperationStatusTracker();

        // Run of 429/3200
        tracker.recordStatusCode(429, 3200);
        tracker.recordStatusCode(429, 3200);
        tracker.recordStatusCode(429, 3200);
        // Single 410/1002
        tracker.recordStatusCode(410, 1002);
        // Run of 429/3200 again (new entry since interrupted)
        tracker.recordStatusCode(429, 3200);
        tracker.recordStatusCode(429, 3200);

        String result = tracker.toString();
        assertThat(result).contains("count=3");
        assertThat(result).contains("410/1002");
        assertThat(result).contains("count=2");
    }

    @Test(groups = { "unit" })
    public void sameStatusCodeDifferentSubStatusCodeShouldNotCompress() {
        BulkOperationStatusTracker tracker = new BulkOperationStatusTracker();

        tracker.recordStatusCode(429, 3200);
        tracker.recordStatusCode(429, 3201);

        String result = tracker.toString();
        assertThat(result).contains("429/3200");
        assertThat(result).contains("429/3201");
    }

    @Test(groups = { "unit" })
    public void toStringShouldContainAllEntries() {
        BulkOperationStatusTracker tracker = new BulkOperationStatusTracker();

        tracker.recordStatusCode(429, 3200);
        tracker.recordStatusCode(429, 3200);
        tracker.recordStatusCode(410, 1002);

        String result = tracker.toString();
        assertThat(result).startsWith("[");
        assertThat(result).endsWith("]");
        assertThat(result).contains("429/3200");
        assertThat(result).contains("count=2");
        assertThat(result).contains("410/1002");
        assertThat(result).contains("count=1");
    }
}
