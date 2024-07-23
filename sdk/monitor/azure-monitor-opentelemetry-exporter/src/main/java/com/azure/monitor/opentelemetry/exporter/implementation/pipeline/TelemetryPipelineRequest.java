// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.pipeline;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import reactor.core.publisher.Flux;

import java.net.URL;
import java.nio.ByteBuffer;
import java.util.List;

public class TelemetryPipelineRequest {

    private static final HttpHeaderName CONTENT_LENGTH = HttpHeaderName.fromString("Content-Length");
    private static final HttpHeaderName USER_AGENT = HttpHeaderName.fromString("User-Agent");
    private static final HttpHeaderName CONTENT_ENCODING = HttpHeaderName.fromString("Content-Encoding");
    private volatile URL url;
    private final String connectionString;
    private final String instrumentationKey;
    private final List<ByteBuffer> byteBuffers;
    private final int contentLength;

    TelemetryPipelineRequest(
        URL url, String connectionString, String instrumentationKey, List<ByteBuffer> byteBuffers) {
        this.url = url;
        this.connectionString = connectionString;
        this.instrumentationKey = instrumentationKey;
        this.byteBuffers = byteBuffers;
        contentLength = byteBuffers.stream().mapToInt(ByteBuffer::limit).sum();
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

    HttpRequest createHttpRequest() {
        HttpRequest request = new HttpRequest(HttpMethod.POST, url);
        request.setBody(Flux.fromIterable(byteBuffers));
        request.setHeader(CONTENT_LENGTH, Integer.toString(contentLength));

        // need to suppress the default User-Agent "ReactorNetty/dev", otherwise Breeze ingestionservice
        // will put that User-Agent header into the client_Browser field for all telemetry that doesn't
        // explicitly set it's own UserAgent (ideally Breeze would only have this behavior for ingestion
        // directly from browsers)
        // TODO (trask) not setting User-Agent header at all would be a better option, but haven't
        //  figured out how to do that yet
        request.setHeader(USER_AGENT, "");
        request.setHeader(CONTENT_ENCODING, "gzip");

        return request;
    }
}
