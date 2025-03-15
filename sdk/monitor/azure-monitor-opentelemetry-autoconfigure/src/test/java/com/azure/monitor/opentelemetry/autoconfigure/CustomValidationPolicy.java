// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.FluxUtil;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.localstorage.LocalStorageTelemetryPipelineListener;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.TelemetryItem;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.TestUtils;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

final class CustomValidationPolicy implements HttpPipelinePolicy {

    private final CountDownLatch countDown;
    private volatile URL url;
    private final List<TelemetryItem> actualTelemetryItems = new CopyOnWriteArrayList<>();

    CustomValidationPolicy(CountDownLatch countDown) {
        this.countDown = countDown;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        URL url = context.getHttpRequest().getUrl();
        String stringUrl = url.toString();
        if (!stringUrl.contains("livediagnostics.monitor.azure.com/QuickPulseService.svc")) {
            this.url = url;
        }
        Mono<byte[]> asyncBytes = FluxUtil.collectBytesInByteBufferStream(context.getHttpRequest().getBody())
            .map(LocalStorageTelemetryPipelineListener::ungzip);
        asyncBytes.subscribe(value -> {
            actualTelemetryItems.addAll(TestUtils.deserialize(value));
            countDown.countDown();
        });
        return next.process();
    }

    URL getUrl() {
        return url;
    }

    List<TelemetryItem> getActualTelemetryItems() {
        return actualTelemetryItems;
    }
}
