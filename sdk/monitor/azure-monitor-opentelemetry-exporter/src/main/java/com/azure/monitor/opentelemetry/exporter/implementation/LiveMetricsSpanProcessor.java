// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation;

import com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.QuickPulse;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;

public class LiveMetricsSpanProcessor implements SpanProcessor {

    private final QuickPulse quickPulse;
    private final SpanDataMapper mapper;

    public LiveMetricsSpanProcessor(QuickPulse quickPulse, SpanDataMapper mapper) {
        this.quickPulse = quickPulse;
        this.mapper = mapper;
    }

    @Override
    public void onStart(Context context, ReadWriteSpan readWriteSpan) {
    }

    @Override
    public boolean isStartRequired() {
        return false;
    }

    @Override
    public void onEnd(ReadableSpan readableSpan) {
        System.out.println("LiveMetricsSpanProcessor.onEnd");
        if (quickPulse.isEnabled()) {
            System.out.println("LiveMetricsSpanProcessor.onEnd quickPulse.isEnabled");
            // TODO (trask) can we do anything better here in terms of double conversion?
            quickPulse.add(mapper.map(readableSpan.toSpanData()));
        } else {
            System.out.println("LiveMetricsSpanProcessor.onEnd quickPulse not enabled");
        }
    }

    @Override
    public boolean isEndRequired() {
        return true;
    }
}
