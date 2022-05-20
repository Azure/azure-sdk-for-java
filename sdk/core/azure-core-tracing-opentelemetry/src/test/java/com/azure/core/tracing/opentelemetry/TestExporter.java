// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class TestExporter implements SpanExporter {

    private final List<SpanData> exportedSpans = new CopyOnWriteArrayList<>();

    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
        exportedSpans.addAll(spans);
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode flush() {
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
        return CompletableResultCode.ofSuccess();
    }

    public List<SpanData> getSpans() {
        return exportedSpans;
    }
}
