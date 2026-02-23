// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.pipeline;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import reactor.core.publisher.Flux;

import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TelemetryPipelineRequest {

    private volatile URL url;
    private final String connectionString;
    private final String instrumentationKey;
    private final List<ByteBuffer> byteBuffers;
    private final int contentLength;

    // Customer-facing SDKStats metadata: item counts by telemetry type
    private final Map<String, Long> itemCountsByType;
    private final Map<String, Long> successItemCountsByType;
    private final Map<String, Long> failureItemCountsByType;

    TelemetryPipelineRequest(URL url, String connectionString, String instrumentationKey,
        List<ByteBuffer> byteBuffers) {
        this(url, connectionString, instrumentationKey, byteBuffers, Collections.emptyMap(), Collections.emptyMap(),
            Collections.emptyMap());
    }

    public TelemetryPipelineRequest(URL url, String connectionString, String instrumentationKey,
        List<ByteBuffer> byteBuffers, Map<String, Long> itemCountsByType, Map<String, Long> successItemCountsByType,
        Map<String, Long> failureItemCountsByType) {
        this.url = url;
        this.connectionString = connectionString;
        this.instrumentationKey = instrumentationKey;
        this.byteBuffers = byteBuffers;
        contentLength = byteBuffers.stream().mapToInt(ByteBuffer::limit).sum();
        this.itemCountsByType = itemCountsByType;
        this.successItemCountsByType = successItemCountsByType;
        this.failureItemCountsByType = failureItemCountsByType;
    }

    public URL getUrl() {
        return url;
    }

    void setUrl(URL url) {
        this.url = url;
    }

    public List<ByteBuffer> getByteBuffers() {
        return byteBuffers;
    }

    public String getConnectionString() {
        return connectionString;
    }

    // used by statsbeat
    public String getInstrumentationKey() {
        return instrumentationKey;
    }

    /**
     * Returns item counts by telemetry type (e.g. "REQUEST" -> 200, "DEPENDENCY" -> 300).
     * Empty map for batches where item counting is not applicable (e.g. statsbeat, disk retries).
     */
    public Map<String, Long> getItemCountsByType() {
        return itemCountsByType;
    }

    /**
     * Returns counts of successful REQUEST/DEPENDENCY items (where isSuccess() == true).
     */
    public Map<String, Long> getSuccessItemCountsByType() {
        return successItemCountsByType;
    }

    /**
     * Returns counts of failed REQUEST/DEPENDENCY items (where isSuccess() == false).
     */
    public Map<String, Long> getFailureItemCountsByType() {
        return failureItemCountsByType;
    }

    HttpRequest createHttpRequest() {
        HttpRequest request = new HttpRequest(HttpMethod.POST, url);
        request.setBody(Flux.fromIterable(byteBuffers));
        request.setHeader(HttpHeaderName.CONTENT_LENGTH, Integer.toString(contentLength));

        // need to suppress the default User-Agent "ReactorNetty/dev", otherwise Breeze ingestionservice
        // will put that User-Agent header into the client_Browser field for all telemetry that doesn't
        // explicitly set it's own UserAgent (ideally Breeze would only have this behavior for ingestion
        // directly from browsers)
        // TODO (trask) not setting User-Agent header at all would be a better option, but haven't
        //  figured out how to do that yet
        request.setHeader(HttpHeaderName.USER_AGENT, "");
        request.setHeader(HttpHeaderName.CONTENT_ENCODING, "gzip");

        return request;
    }
}
