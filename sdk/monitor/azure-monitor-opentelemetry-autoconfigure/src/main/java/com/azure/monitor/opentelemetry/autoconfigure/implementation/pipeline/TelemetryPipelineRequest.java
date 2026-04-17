// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.pipeline;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.statsbeat.TelemetryBatchMetadata;
import reactor.core.publisher.Flux;

import java.net.URL;
import java.nio.ByteBuffer;
import java.util.List;

public class TelemetryPipelineRequest {

    private volatile URL url;
    private final String connectionString;
    private final String instrumentationKey;
    private final List<ByteBuffer> byteBuffers;
    private final int contentLength;

    // Customer-facing SDKStats metadata
    private final TelemetryBatchMetadata batchMetadata;

    TelemetryPipelineRequest(URL url, String connectionString, String instrumentationKey,
        List<ByteBuffer> byteBuffers) {
        this(url, connectionString, instrumentationKey, byteBuffers, TelemetryBatchMetadata.empty());
    }

    public TelemetryPipelineRequest(URL url, String connectionString, String instrumentationKey,
        List<ByteBuffer> byteBuffers, TelemetryBatchMetadata batchMetadata) {
        this.url = url;
        this.connectionString = connectionString;
        this.instrumentationKey = instrumentationKey;
        this.byteBuffers = byteBuffers;
        contentLength = byteBuffers.stream().mapToInt(ByteBuffer::limit).sum();
        this.batchMetadata = batchMetadata != null ? batchMetadata : TelemetryBatchMetadata.empty();
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
     * Returns metadata about the telemetry batch, including item counts by type
     * and success/failure breakdowns. Returns empty metadata for batches where
     * item counting is not applicable (e.g. statsbeat, disk retries).
     */
    public TelemetryBatchMetadata getTelemetryBatchMetadata() {
        return batchMetadata;
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
