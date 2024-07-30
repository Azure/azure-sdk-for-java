// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.FluxUtil;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.zip.GZIPInputStream;

import static com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryItemSerialization.deserialize;

final class CustomValidationPolicy implements HttpPipelinePolicy {

    private final CountDownLatch countDown;
    private volatile URL url;
    private final List<TelemetryItem> actualTelemetryItems = new CopyOnWriteArrayList<>();

    CustomValidationPolicy(CountDownLatch countDown) {
        this.countDown = countDown;
    }

    @Override
    public Mono<HttpResponse> process(
        HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        url = context.getHttpRequest().getUrl();
        Mono<String> asyncBytes =
            FluxUtil.collectBytesInByteBufferStream(context.getHttpRequest().getBody())
                .map(CustomValidationPolicy::ungzip);
        asyncBytes.subscribe(
            value -> {
                try {
                    actualTelemetryItems.addAll(deserialize(value.getBytes(StandardCharsets.UTF_8)));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    countDown.countDown();
                }
            });
        return next.process();
    }

    URL getUrl() {
        return url;
    }

    List<TelemetryItem> getActualTelemetryItems() {
        return actualTelemetryItems;
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
            return baos.toString(StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
