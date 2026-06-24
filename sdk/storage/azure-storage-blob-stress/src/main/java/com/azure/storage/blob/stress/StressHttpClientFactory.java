// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress;

import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;

import java.time.Duration;

/**
 * Builds Netty HTTP clients with per-payload-tier I/O timeouts for the storage stress
 * test scenarios.
 *
 * <p>Background: the Azure SDK Netty client defaults to a 60&nbsp;s {@code
 * responseTimeout} (plus 60&nbsp;s {@code readTimeout} and 60&nbsp;s {@code
 * writeTimeout}). Under fault injection, scenarios whose single logical operation
 * issues many HTTP requests (e.g. chunked 50&nbsp;MB uploads/downloads) cross a per-op
 * fault probability of &gt;50%, so the median operation time becomes dominated by the
 * 60&nbsp;s timeout firing rather than the real payload-transfer time.</p>
 *
 * <p>All three Netty timeouts (response, read, write) are set to the same per-tier
 * value:</p>
 * <ul>
 *   <li>{@code responseTimeout} fires while waiting for the response status / first
 *       byte &mdash; catches upload faults that suppress the response.</li>
 *   <li>{@code readTimeout} fires on idle intervals between body reads &mdash;
 *       catches download faults that truncate or hang the response body. Streaming
 *       downloads at network speed have continuous reads every few ms, so the short
 *       idle window only fires when the body actually stalls.</li>
 *   <li>{@code writeTimeout} fires on idle intervals between body writes &mdash;
 *       catches upload faults that stall the request body mid-stream.</li>
 * </ul>
 *
 * <p>Scenarios whose single logical iteration issues many small writes
 * (e.g. {@code AppendBlobOutputStream}, {@code PageBlobOutputStream}, or stream-based
 * uploads) can either pass a deliberately larger {@code effectivePayloadBytes} to use
 * tiered mapping or provide an explicit {@link Duration} timeout override.</p>
 */
public final class StressHttpClientFactory {

    private static final long ONE_MB = 1024L * 1024L;

    private StressHttpClientFactory() {
    }

    /**
     * Builds a Netty HTTP client whose {@code responseTimeout}, {@code readTimeout},
     * and {@code writeTimeout} are all set to the supplied timeout.
     *
     * @param timeout timeout value applied to response/read/write timeouts.
     * @return a Netty {@link HttpClient} with the supplied timeout applied.
     */
    public static HttpClient buildHttpClient(Duration timeout) {
        return new NettyAsyncHttpClientBuilder()
            .responseTimeout(timeout)
            .readTimeout(timeout)
            .writeTimeout(timeout)
            .build();
    }

    /**
     * Returns the per-tier idle timeout for the supplied effective payload size. See
     * the class JavaDoc for the tier table. Package-private for unit-testability.
     *
     * @param effectivePayloadBytes payload size used to pick the tier.
     * @return the per-tier idle timeout.
     */
    static Duration suggestedTimeout(long effectivePayloadBytes) {
        if (effectivePayloadBytes <= ONE_MB) {
            return Duration.ofSeconds(5);
        } else if (effectivePayloadBytes <= 25L * ONE_MB) {
            // Covers 4 MB (uploadPages per-page) and 25 MB single-shot block uploads.
            return Duration.ofSeconds(10);
        } else if (effectivePayloadBytes <= 50L * ONE_MB) {
            // 50 MB chunked upload/download paths can legitimately take 5-15 s per op.
            return Duration.ofSeconds(30);
        }
        // Fall back to the SDK default for anything larger.
        return Duration.ofSeconds(60);
    }
}

