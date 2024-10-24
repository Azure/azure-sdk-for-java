// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.pipeline;

import io.opentelemetry.sdk.common.CompletableResultCode;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public interface TelemetryPipelineListener {

    void onResponse(TelemetryPipelineRequest request, TelemetryPipelineResponse response);

    void onException(TelemetryPipelineRequest request, String errorMessage, Throwable throwable);

    CompletableResultCode shutdown();

    static TelemetryPipelineListener composite(TelemetryPipelineListener... delegates) {
        return new CompositeTelemetryPipelineListener(asList(delegates));
    }

    static TelemetryPipelineListener noop() {
        return NoopTelemetryPipelineListener.INSTANCE;
    }

    class CompositeTelemetryPipelineListener implements TelemetryPipelineListener {

        private final List<TelemetryPipelineListener> delegates;

        public CompositeTelemetryPipelineListener(List<TelemetryPipelineListener> delegates) {
            this.delegates = delegates;
        }

        @Override
        public void onResponse(TelemetryPipelineRequest request, TelemetryPipelineResponse response) {
            for (TelemetryPipelineListener delegate : delegates) {
                delegate.onResponse(request, response);
            }
        }

        @Override
        public void onException(TelemetryPipelineRequest request, String errorMessage, Throwable throwable) {
            for (TelemetryPipelineListener delegate : delegates) {
                delegate.onException(request, errorMessage, throwable);
            }
        }

        @Override
        public CompletableResultCode shutdown() {
            List<CompletableResultCode> results = new ArrayList<>();
            for (TelemetryPipelineListener delegate : delegates) {
                results.add(delegate.shutdown());
            }
            return CompletableResultCode.ofAll(results);
        }
    }

    class NoopTelemetryPipelineListener implements TelemetryPipelineListener {

        static final TelemetryPipelineListener INSTANCE = new NoopTelemetryPipelineListener();

        @Override
        public void onResponse(TelemetryPipelineRequest request, TelemetryPipelineResponse response) {
        }

        @Override
        public void onException(TelemetryPipelineRequest request, String errorMessage, Throwable throwable) {
        }

        @Override
        public CompletableResultCode shutdown() {
            return CompletableResultCode.ofSuccess();
        }
    }
}
