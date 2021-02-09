// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.util.serializer.TypeReference;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link SearchClientBuilder.SearchIndexingBufferedSenderBuilder}.
 */
public class SearchIndexingBufferedSenderBuilderTests {
    private static final TypeReference<Map<String, Object>> DOCUMENT_TYPE = new TypeReference<Map<String, Object>>() {
    };

    @Test
    public void invalidFlushWindowThrows() {
        SearchClientBuilder.SearchIndexingBufferedSenderBuilder<Map<String, Object>> options = getBaseOptions();
        assertThrows(NullPointerException.class, () -> options.autoFlushInterval(null));
    }

    @Test
    public void invalidBatchSizeThrows() {
        SearchClientBuilder.SearchIndexingBufferedSenderBuilder<Map<String, Object>> options = getBaseOptions();
        assertThrows(IllegalArgumentException.class, () -> options.initialBatchActionCount(0));
        assertThrows(IllegalArgumentException.class, () -> options.initialBatchActionCount(-1));
    }

    @Test
    public void invalidMaxRetriesThrows() {
        SearchClientBuilder.SearchIndexingBufferedSenderBuilder<Map<String, Object>> options = getBaseOptions();
        assertThrows(IllegalArgumentException.class, () -> options.maxRetriesPerAction(0));
        assertThrows(IllegalArgumentException.class, () -> options.maxRetriesPerAction(-1));
    }

    @Test
    public void invalidRetryDelayThrows() {
        SearchClientBuilder.SearchIndexingBufferedSenderBuilder<Map<String, Object>> options = getBaseOptions();
        assertThrows(NullPointerException.class, () -> options.throttlingDelay(null));
        assertThrows(IllegalArgumentException.class, () -> options.throttlingDelay(Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> options.throttlingDelay(Duration.ofMillis(-1)));
    }

    @Test
    public void invalidMaxRetryDelayThrows() {
        SearchClientBuilder.SearchIndexingBufferedSenderBuilder<Map<String, Object>> options = getBaseOptions();
        assertThrows(NullPointerException.class, () -> options.maxThrottlingDelay(null));
        assertThrows(IllegalArgumentException.class, () -> options.maxThrottlingDelay(Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> options.maxThrottlingDelay(Duration.ofMillis(-1)));
    }

//    @Test
//    public void invalidPayloadTooLargeScaleDownThrows() {
//        SearchIndexingBufferedSenderOptions<Integer> options = getBaseOptions();
//        assertThrows(NullPointerException.class, () -> options.setPayloadTooLargeScaleDown(null));
//    }

    private SearchClientBuilder.SearchIndexingBufferedSenderBuilder<Map<String, Object>> getBaseOptions() {
        return new SearchClientBuilder().bufferedSender(DOCUMENT_TYPE);
    }
}
