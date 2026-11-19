// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link SearchIndexingBufferedSenderBuilder}.
 */
@Execution(ExecutionMode.CONCURRENT)
public class SearchIndexingBufferedSenderBuilderTests {
    private static final Map<String, Duration> FOOL_SPOTBUGS = new HashMap<>();

    @Test
    public void invalidFlushWindowThrows() {
        SearchIndexingBufferedSenderBuilder<Map<String, Object>> options = getBaseOptions();
        Duration interval = FOOL_SPOTBUGS.get("interval");
        assertThrows(NullPointerException.class, () -> options.autoFlushInterval(interval));
    }

    @Test
    public void invalidBatchSizeThrows() {
        SearchIndexingBufferedSenderBuilder<Map<String, Object>> options = getBaseOptions();
        assertThrows(IllegalArgumentException.class, () -> options.initialBatchActionCount(0));
        assertThrows(IllegalArgumentException.class, () -> options.initialBatchActionCount(-1));
    }

    @Test
    public void invalidMaxRetriesThrows() {
        SearchIndexingBufferedSenderBuilder<Map<String, Object>> options = getBaseOptions();
        assertThrows(IllegalArgumentException.class, () -> options.maxRetriesPerAction(0));
        assertThrows(IllegalArgumentException.class, () -> options.maxRetriesPerAction(-1));
    }

    @Test
    public void invalidRetryDelayThrows() {
        SearchIndexingBufferedSenderBuilder<Map<String, Object>> options = getBaseOptions();
        Duration throttlingDelay = FOOL_SPOTBUGS.get("throttlingDelay");
        assertThrows(NullPointerException.class, () -> options.throttlingDelay(throttlingDelay));
        assertThrows(IllegalArgumentException.class, () -> options.throttlingDelay(Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> options.throttlingDelay(Duration.ofMillis(-1)));
    }

    @Test
    public void invalidMaxRetryDelayThrows() {
        SearchIndexingBufferedSenderBuilder<Map<String, Object>> options = getBaseOptions();
        Duration maxThrottlingDelay = FOOL_SPOTBUGS.get("maxThrottlingDelay");
        assertThrows(NullPointerException.class, () -> options.maxThrottlingDelay(maxThrottlingDelay));
        assertThrows(IllegalArgumentException.class, () -> options.maxThrottlingDelay(Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> options.maxThrottlingDelay(Duration.ofMillis(-1)));
    }

    //    @Test
    //    public void invalidPayloadTooLargeScaleDownThrows() {
    //        SearchIndexingBufferedSenderOptions<Integer> options = getBaseOptions();
    //        assertThrows(NullPointerException.class, () -> options.setPayloadTooLargeScaleDown(null));
    //    }

    private SearchIndexingBufferedSenderBuilder<Map<String, Object>> getBaseOptions() {
        return new SearchIndexingBufferedSenderBuilder<>();
    }
}
