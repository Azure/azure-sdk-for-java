// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.monitor.applicationinsights.spring;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.FluxUtil;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.zip.GZIPInputStream;

class CustomValidationPolicy implements HttpPipelinePolicy {
    private final ObjectMapper objectMapper = createObjectMapper();

    private final CountDownLatch countDown;
    volatile URL url;
    final Queue<TelemetryItem> actualTelemetryItems = new ConcurrentLinkedQueue<>();

    CustomValidationPolicy(CountDownLatch countDown) {
        this.countDown = countDown;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        url = context.getHttpRequest().getUrl();
        FluxUtil.collectBytesInByteBufferStream(context.getHttpRequest().getBody()).map(CustomValidationPolicy::ungzip)
            .subscribe(value -> {
                try (MappingIterator<TelemetryItem> i = objectMapper.readerFor(TelemetryItem.class).readValues(value)) {
                    i.forEachRemaining(actualTelemetryItems::add);
                    countDown.countDown();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        return next.process();
    }

    // decode gzipped request raw bytes back to original request body
    private static String ungzip(byte[] rawBytes) {
        if (rawBytes.length == 0) {
            return "";
        }
        try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(rawBytes))) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] data = new byte[1024];
            int read;
            while ((read = in.read(data, 0, data.length)) != -1) {
                baos.write(data, 0, read);
            }
            return baos.toString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // TODO need to update this when a newer version of azure-monitor-opentelemetry-exporter with azure-json is released
    // azure-sdk-for-java will always test against the source version of azure-monitor-opentelemetry-exporter in CI builds for backward compatibility.
    // JacksonJsonProvider provides a Jackson Databind modules that enables Jackson deserialization to hook into azure-json deserialization.
    // Tried it and didn't work.
    private static ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // handle JSR-310 (java 8) dates with Jackson by configuring ObjectMapper to use this
        // dependency and not (de)serialize Instant as timestamps that it does by default
        objectMapper.findAndRegisterModules().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
}
