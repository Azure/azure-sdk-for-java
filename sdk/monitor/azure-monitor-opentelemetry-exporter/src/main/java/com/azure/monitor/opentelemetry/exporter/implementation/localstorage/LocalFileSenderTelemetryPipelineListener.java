// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.localstorage;

import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryPipelineListener;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryPipelineRequest;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryPipelineResponse;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.StatusCode;
import io.opentelemetry.sdk.common.CompletableResultCode;

import java.io.File;

class LocalFileSenderTelemetryPipelineListener implements TelemetryPipelineListener {

    private final LocalFileLoader localFileLoader;
    private final File file;

    LocalFileSenderTelemetryPipelineListener(LocalFileLoader localFileLoader, File file) {
        this.localFileLoader = localFileLoader;
        this.file = file;
    }

    @Override
    public void onResponse(TelemetryPipelineRequest request, TelemetryPipelineResponse response) {
        int responseCode = response.getStatusCode();
        if (responseCode == 200) {
            localFileLoader.updateProcessedFileStatus(true, file);
        } else {
            localFileLoader.updateProcessedFileStatus(!StatusCode.isRetryable(responseCode), file);
        }
    }

    @Override
    public void onException(TelemetryPipelineRequest request, String errorMessage, Throwable throwable) {
        localFileLoader.updateProcessedFileStatus(false, file);
    }

    @Override
    public CompletableResultCode shutdown() {
        return CompletableResultCode.ofSuccess();
    }
}
