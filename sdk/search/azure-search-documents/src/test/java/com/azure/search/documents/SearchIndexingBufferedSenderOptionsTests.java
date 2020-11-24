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
        SearchIndexingBufferedSenderOptions<Integer> options = new SearchIndexingBufferedSenderOptions<>();
        assertTrue(options.getAutoFlush());
    }

    @Test
    public void flushWindowDefaults() {
        SearchIndexingBufferedSenderOptions<Integer> options = new SearchIndexingBufferedSenderOptions<>();
        assertEquals(Duration.ofSeconds(60), options.getAutoFlushWindow());
    }

    @Test
    public void invalidFlushWindowThrows() {
        SearchIndexingBufferedSenderOptions<Integer> options = new SearchIndexingBufferedSenderOptions<>();
        assertThrows(NullPointerException.class, () -> options.setAutoFlushWindow(null));
    }

    @Test
    public void initialBatchActionCountDefaults() {
        SearchIndexingBufferedSenderOptions<Integer> options = new SearchIndexingBufferedSenderOptions<>();
        assertEquals(512, options.getInitialBatchActionCount());
    }

    @Test
    public void invalidBatchSizeThrows() {
        SearchIndexingBufferedSenderOptions<Integer> options = new SearchIndexingBufferedSenderOptions<>();
        assertThrows(IllegalArgumentException.class, () -> options.setInitialBatchActionCount(0));
        assertThrows(IllegalArgumentException.class, () -> options.setInitialBatchActionCount(-1));
    }

    @Test
    public void maxRetriesDefaults() {
        SearchIndexingBufferedSenderOptions<Integer> options = new SearchIndexingBufferedSenderOptions<>();
        assertEquals(3, options.getMaxRetries());
    }

    @Test
    public void invalidMaxRetriesThrows() {
        SearchIndexingBufferedSenderOptions<Integer> options = new SearchIndexingBufferedSenderOptions<>();
        assertThrows(IllegalArgumentException.class, () -> options.setMaxRetries(0));
        assertThrows(IllegalArgumentException.class, () -> options.setMaxRetries(-1));
    }

    @Test
    public void retryDelayDefaults() {
        SearchIndexingBufferedSenderOptions<Integer> options = new SearchIndexingBufferedSenderOptions<>();
        assertEquals(Duration.ofMillis(800), options.getRetryDelay());
    }

    @Test
    public void invalidRetryDelayThrows() {
        SearchIndexingBufferedSenderOptions<Integer> options = new SearchIndexingBufferedSenderOptions<>();
        assertThrows(NullPointerException.class, () -> options.setRetryDelay(null));
        assertThrows(IllegalArgumentException.class, () -> options.setRetryDelay(Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> options.setRetryDelay(Duration.ofMillis(-1)));
    }

    @Test
    public void maxRetryDelayDefaults() {
        SearchIndexingBufferedSenderOptions<Integer> options = new SearchIndexingBufferedSenderOptions<>();
        assertEquals(Duration.ofMinutes(1), options.getMaxRetryDelay());
    }

    @Test
    public void invalidMaxRetryDelayThrows() {
        SearchIndexingBufferedSenderOptions<Integer> options = new SearchIndexingBufferedSenderOptions<>();
        assertThrows(NullPointerException.class, () -> options.setMaxRetryDelay(null));
        assertThrows(IllegalArgumentException.class, () -> options.setMaxRetryDelay(Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> options.setMaxRetryDelay(Duration.ofMillis(-1)));
    }
}
