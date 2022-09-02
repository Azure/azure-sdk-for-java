/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.azure.monitor.opentelemetry.exporter.implementation.pipeline;

import io.opentelemetry.sdk.common.CompletableResultCode;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public interface TelemetryPipelineListener {

    static TelemetryPipelineListener composite(TelemetryPipelineListener... delegates) {
        return new CompositeTelemetryPipelineListener(asList(delegates));
    }

    static TelemetryPipelineListener noop() {
        return NoopTelemetryPipelineListener.INSTANCE;
    }

    void onResponse(TelemetryPipelineRequest request, TelemetryPipelineResponse response);

    void onException(TelemetryPipelineRequest request, String errorMessage, Throwable throwable);

    CompletableResultCode shutdown();

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
        public void onException(
            TelemetryPipelineRequest request, String errorMessage, Throwable throwable) {
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
        public void onException(
            TelemetryPipelineRequest request, String errorMessage, Throwable throwable) {
        }

        @Override
        public CompletableResultCode shutdown() {
            return CompletableResultCode.ofSuccess();
        }
    }
}
