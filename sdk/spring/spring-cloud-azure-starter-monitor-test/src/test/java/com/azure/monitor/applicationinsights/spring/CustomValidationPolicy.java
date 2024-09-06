// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.monitor.applicationinsights.spring;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.FluxUtil;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.monitor.opentelemetry.exporter.implementation.localstorage.LocalStorageTelemetryPipelineListener;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

final class CustomValidationPolicy implements HttpPipelinePolicy {

    private final CountDownLatch countDown;
    volatile URL url;
    final List<TelemetryItem> actualTelemetryItems = new CopyOnWriteArrayList<>();

    CustomValidationPolicy(CountDownLatch countDown) {
        this.countDown = countDown;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        url = context.getHttpRequest().getUrl();
        Mono<byte[]> asyncBytes = FluxUtil.collectBytesInByteBufferStream(context.getHttpRequest().getBody())
            .map(LocalStorageTelemetryPipelineListener::ungzip);
        asyncBytes.subscribe(value -> {
            actualTelemetryItems.addAll(deserialize(value));
            countDown.countDown();
        });
        return next.process();
    }

    // deserialize multiple TelemetryItem raw bytes with newline delimiters to a list of TelemetryItems
    private static List<TelemetryItem> deserialize(byte[] rawBytes) {
        try (JsonReader jsonReader = JsonProviders.createReader(rawBytes)) {
            JsonToken token = jsonReader.currentToken();
            if (token == null) {
                token = jsonReader.nextToken();
            }

            List<TelemetryItem> result = new ArrayList<>();
            do {
                result.add(TelemetryItem.fromJson(jsonReader));
            } while (jsonReader.nextToken() == JsonToken.START_OBJECT);
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
