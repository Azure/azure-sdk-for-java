// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.pipeline;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;
import com.azure.core.util.tracing.Tracer;
import com.azure.monitor.opentelemetry.exporter.implementation.configuration.ConnectionString;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.StatusCode;
import io.opentelemetry.sdk.common.CompletableResultCode;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TelemetryPipeline {

    // Based on Stamp specific redirects design doc
    private static final int MAX_REDIRECTS = 10;

    private final HttpPipeline pipeline;

    // key is connectionString, value is redirectUrl
    private final Map<String, URL> redirectCache =
        Collections.synchronizedMap(new BoundedHashMap<>(100));

    public TelemetryPipeline(HttpPipeline pipeline) {
        this.pipeline = pipeline;
    }

    public CompletableResultCode send(
        List<ByteBuffer> telemetry, String connectionString, TelemetryPipelineListener listener) {

        ConnectionString connectionStringObj = ConnectionString.parse(connectionString);

        URL url =
            redirectCache.computeIfAbsent(
                connectionString, k -> getFullIngestionUrl(connectionStringObj.getIngestionEndpoint()));

        TelemetryPipelineRequest request =
            new TelemetryPipelineRequest(
                url, connectionString, connectionStringObj.getInstrumentationKey(), telemetry);

        try {
            CompletableResultCode result = new CompletableResultCode();
            sendInternal(request, listener, result, MAX_REDIRECTS);
            return result;
        } catch (Throwable t) {
            listener.onException(request, t.getMessage() + " (" + request.getUrl() + ")", t);
            return CompletableResultCode.ofFailure();
        }
    }

    private static URL getFullIngestionUrl(String ingestionEndpoint) {
        try {
            return new URL(new URL(ingestionEndpoint), "v2.1/track");
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid endpoint: " + ingestionEndpoint, e);
        }
    }

    private void sendInternal(
        TelemetryPipelineRequest request,
        TelemetryPipelineListener listener,
        CompletableResultCode result,
        int remainingRedirects) {

        // Add instrumentation key to context to use in StatsbeatHttpPipelinePolicy
        Map<Object, Object> contextKeyValues = new HashMap<>();
        contextKeyValues.put("instrumentationKey", request.getInstrumentationKey());
        contextKeyValues.put(Tracer.DISABLE_TRACING_KEY, true);

        pipeline
            .send(request.createHttpRequest(), Context.of(contextKeyValues))
            .subscribe(
                response ->
                    response
                        .getBodyAsString()
                        .switchIfEmpty(Mono.just(""))
                        .subscribe(
                            responseBody ->
                                onResponseBody(
                                    request,
                                    response,
                                    responseBody,
                                    listener,
                                    result,
                                    remainingRedirects),
                            throwable -> {
                                listener.onException(
                                    request,
                                    throwable.getMessage() + " (" + request.getUrl() + ")",
                                    throwable);
                                result.fail();
                            }),
                throwable -> {
                    listener.onException(
                        request, throwable.getMessage() + " (" + request.getUrl() + ")", throwable);
                    result.fail();
                });
    }

    private void onResponseBody(
        TelemetryPipelineRequest request,
        HttpResponse response,
        String responseBody,
        TelemetryPipelineListener listener,
        CompletableResultCode result,
        int remainingRedirects) {

        int responseCode = response.getStatusCode();

        if (StatusCode.isRedirect(responseCode) && remainingRedirects > 0) {
            String location = response.getHeaderValue("Location");
            URL locationUrl;
            try {
                locationUrl = new URL(location);
            } catch (MalformedURLException e) {
                listener.onException(request, "Invalid redirect: " + location, e);
                return;
            }
            redirectCache.put(request.getConnectionString(), locationUrl);
            request.setUrl(locationUrl);
            sendInternal(request, listener, result, remainingRedirects - 1);
            return;
        }

        listener.onResponse(request, new TelemetryPipelineResponse(responseCode, responseBody));
        if (responseCode == 200) {
            result.succeed();
        } else {
            result.fail();
        }
    }

    private static class BoundedHashMap<K, V> extends LinkedHashMap<K, V> {

        private static final long serialVersionUID = 1L;

        private final int bound;

        private BoundedHashMap(int bound) {
            this.bound = bound;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > bound;
        }
    }
}
