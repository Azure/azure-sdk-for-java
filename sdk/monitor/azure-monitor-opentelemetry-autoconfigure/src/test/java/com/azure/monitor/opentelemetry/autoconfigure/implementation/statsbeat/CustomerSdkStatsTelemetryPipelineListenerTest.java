// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.statsbeat;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.pipeline.TelemetryPipelineRequest;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.pipeline.TelemetryPipelineResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomerSdkStatsTelemetryPipelineListenerTest {

    private CustomerSdkStats customerSdkStats;
    private CustomerSdkStatsTelemetryPipelineListener listener;

    @BeforeEach
    public void init() {
        customerSdkStats = new CustomerSdkStats("unknown", "java", "3.5.1");
        listener = new CustomerSdkStatsTelemetryPipelineListener(customerSdkStats);
    }

    @Test
    public void testSuccessResponse() {
        Map<String, Long> itemCounts = new HashMap<>();
        itemCounts.put("REQUEST", 5L);
        itemCounts.put("DEPENDENCY", 3L);

        TelemetryPipelineRequest request = createRequest(itemCounts);
        TelemetryPipelineResponse response = new TelemetryPipelineResponse(200, "OK");

        listener.onResponse(request, response);

        assertThat(customerSdkStats.getSuccessCount("REQUEST")).isEqualTo(5);
        assertThat(customerSdkStats.getSuccessCount("DEPENDENCY")).isEqualTo(3);
    }

    @Test
    public void testPartialSuccessResponse206() {
        Map<String, Long> itemCounts = new HashMap<>();
        itemCounts.put("REQUEST", 8L);
        itemCounts.put("TRACE", 2L);

        TelemetryPipelineRequest request = createRequest(itemCounts);
        // 206 = partial success: some items accepted, some rejected
        TelemetryPipelineResponse response
            = new TelemetryPipelineResponse(206, "{\"itemsReceived\":10,\"itemsAccepted\":7,\"errors\":[]}");

        listener.onResponse(request, response);

        // Entire batch counted as success (failed items retried from disk with empty metadata)
        assertThat(customerSdkStats.getSuccessCount("REQUEST")).isEqualTo(8);
        assertThat(customerSdkStats.getSuccessCount("TRACE")).isEqualTo(2);
        assertThat(customerSdkStats.getDroppedCount("REQUEST", "206")).isEqualTo(0);
    }

    @Test
    public void testRetryableStatusCode429() {
        Map<String, Long> itemCounts = Collections.singletonMap("TRACE", 10L);

        TelemetryPipelineRequest request = createRequest(itemCounts);
        TelemetryPipelineResponse response = new TelemetryPipelineResponse(429, "Too Many Requests");

        listener.onResponse(request, response);

        assertThat(customerSdkStats.getRetryCount("TRACE", "429")).isEqualTo(10);
    }

    @Test
    public void testRetryableStatusCode500() {
        Map<String, Long> itemCounts = Collections.singletonMap("DEPENDENCY", 5L);

        TelemetryPipelineRequest request = createRequest(itemCounts);
        TelemetryPipelineResponse response = new TelemetryPipelineResponse(500, "Internal Server Error");

        listener.onResponse(request, response);

        assertThat(customerSdkStats.getRetryCount("DEPENDENCY", "500")).isEqualTo(5);
    }

    @Test
    public void testNonRetryableStatusCode402() {
        Map<String, Long> itemCounts = Collections.singletonMap("CUSTOM_METRIC", 20L);

        TelemetryPipelineRequest request = createRequest(itemCounts);
        TelemetryPipelineResponse response = new TelemetryPipelineResponse(402, "Payment Required");

        listener.onResponse(request, response);

        assertThat(customerSdkStats.getDroppedCount("CUSTOM_METRIC", "402")).isEqualTo(20);
    }

    @Test
    public void testTimeoutException() {
        Map<String, Long> itemCounts = Collections.singletonMap("REQUEST", 7L);

        TelemetryPipelineRequest request = createRequest(itemCounts);

        listener.onException(request, "Read timed out", new SocketTimeoutException("Read timed out"));

        assertThat(customerSdkStats.getRetryCount("REQUEST", "CLIENT_TIMEOUT")).isEqualTo(7);
    }

    @Test
    public void testNetworkException() {
        Map<String, Long> itemCounts = Collections.singletonMap("DEPENDENCY", 12L);

        TelemetryPipelineRequest request = createRequest(itemCounts);

        listener.onException(request, "Unknown host", new UnknownHostException("host.example.com"));

        assertThat(customerSdkStats.getRetryCount("DEPENDENCY", "CLIENT_EXCEPTION")).isEqualTo(12);
    }

    @Test
    public void testEmptyItemCountsSkipped() {
        TelemetryPipelineRequest request = createRequest(Collections.emptyMap());
        TelemetryPipelineResponse response = new TelemetryPipelineResponse(200, "OK");

        listener.onResponse(request, response);

        // Nothing should be tracked
        assertThat(customerSdkStats.getSuccessCount("REQUEST")).isEqualTo(0);
    }

    @Test
    public void testRedirectSkipped() {
        Map<String, Long> itemCounts = Collections.singletonMap("REQUEST", 5L);

        TelemetryPipelineRequest request = createRequest(itemCounts);
        TelemetryPipelineResponse response = new TelemetryPipelineResponse(307, "Temporary Redirect");

        listener.onResponse(request, response);

        // Redirects should not be tracked
        assertThat(customerSdkStats.getSuccessCount("REQUEST")).isEqualTo(0);
        assertThat(customerSdkStats.getRetryCount("REQUEST", "307")).isEqualTo(0);
        assertThat(customerSdkStats.getDroppedCount("REQUEST", "307")).isEqualTo(0);
    }

    @Test
    public void testReasonPhraseForStatusCode() {
        assertThat(CustomerSdkStatsTelemetryPipelineListener.getReasonPhraseForStatusCode(402))
            .isEqualTo("Exceeded daily quota");
        assertThat(CustomerSdkStatsTelemetryPipelineListener.getReasonPhraseForStatusCode(429))
            .isEqualTo("Too many requests");
        assertThat(CustomerSdkStatsTelemetryPipelineListener.getReasonPhraseForStatusCode(500))
            .isEqualTo("Internal server error");
        assertThat(CustomerSdkStatsTelemetryPipelineListener.getReasonPhraseForStatusCode(999)).isEqualTo("Unknown");
    }

    private static TelemetryPipelineRequest createRequest(Map<String, Long> itemCountsByType) {
        List<ByteBuffer> byteBuffers = Collections.singletonList(ByteBuffer.allocate(0));
        try {
            URL url = new URL("https://dc.services.visualstudio.com/v2.1/track");
            TelemetryBatchMetadata batchMetadata
                = new TelemetryBatchMetadata(itemCountsByType, Collections.emptyMap(), Collections.emptyMap());
            return new TelemetryPipelineRequest(url, "InstrumentationKey=00000000-0000-0000-0000-0FEEDDADBEEF",
                "00000000-0000-0000-0000-0FEEDDADBEEF", byteBuffers, batchMetadata);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
