// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link SearchIndexingBufferedSenderOptions}.
 */
public class SearchIndexingBufferedSenderOptionsTests {
    @Test
    public void autoFlushDefaults() {
        SearchIndexingBufferedSenderOptions<Integer> options = getBaseOptions();
        assertTrue(options.getAutoFlush());
    }

    @Test
    public void flushWindowDefaults() {
        SearchIndexingBufferedSenderOptions<Integer> options = getBaseOptions();
        assertEquals(Duration.ofSeconds(60), options.getAutoFlushInterval());
    }

    @Test
    public void invalidFlushWindowThrows() {
        SearchIndexingBufferedSenderOptions<Integer> options = getBaseOptions();
        assertThrows(NullPointerException.class, () -> options.setAutoFlushInterval(null));
    }

    @Test
    public void initialBatchActionCountDefaults() {
        SearchIndexingBufferedSenderOptions<Integer> options = getBaseOptions();
        assertEquals(512, options.getInitialBatchActionCount());
    }

    @Test
    public void invalidBatchSizeThrows() {
        SearchIndexingBufferedSenderOptions<Integer> options = getBaseOptions();
        assertThrows(IllegalArgumentException.class, () -> options.setInitialBatchActionCount(0));
        assertThrows(IllegalArgumentException.class, () -> options.setInitialBatchActionCount(-1));
    }

    @Test
    public void maxRetriesDefaults() {
        SearchIndexingBufferedSenderOptions<Integer> options = getBaseOptions();
        assertEquals(3, options.getMaxRetriesPerAction());
    }

    @Test
    public void invalidMaxRetriesThrows() {
        SearchIndexingBufferedSenderOptions<Integer> options = getBaseOptions();
        assertThrows(IllegalArgumentException.class, () -> options.setMaxRetriesPerAction(0));
        assertThrows(IllegalArgumentException.class, () -> options.setMaxRetriesPerAction(-1));
    }

    @Test
    public void retryDelayDefaults() {
        SearchIndexingBufferedSenderOptions<Integer> options = getBaseOptions();
        assertEquals(Duration.ofMillis(800), options.getThrottlingDelay());
    }

    @Test
    public void invalidRetryDelayThrows() {
        SearchIndexingBufferedSenderOptions<Integer> options = getBaseOptions();
        assertThrows(NullPointerException.class, () -> options.setThrottlingDelay(null));
        assertThrows(IllegalArgumentException.class, () -> options.setThrottlingDelay(Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> options.setThrottlingDelay(Duration.ofMillis(-1)));
    }

    @Test
    public void maxRetryDelayDefaults() {
        SearchIndexingBufferedSenderOptions<Integer> options = getBaseOptions();
        assertEquals(Duration.ofMinutes(1), options.getMaxThrottlingDelay());
    }

    @Test
    public void invalidMaxRetryDelayThrows() {
        SearchIndexingBufferedSenderOptions<Integer> options = getBaseOptions();
        assertThrows(NullPointerException.class, () -> options.setMaxThrottlingDelay(null));
        assertThrows(IllegalArgumentException.class, () -> options.setMaxThrottlingDelay(Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> options.setMaxThrottlingDelay(Duration.ofMillis(-1)));
    }

//    @Test
//    public void invalidPayloadTooLargeScaleDownThrows() {
//        SearchIndexingBufferedSenderOptions<Integer> options = getBaseOptions();
//        assertThrows(NullPointerException.class, () -> options.setPayloadTooLargeScaleDown(null));
//    }

    private SearchIndexingBufferedSenderOptions<Integer> getBaseOptions() {
        return new SearchIndexingBufferedSenderOptions<>(String::valueOf);
    }
}
