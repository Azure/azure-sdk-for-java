// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.statsbeat;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;
import com.azure.monitor.opentelemetry.exporter.implementation.NoopTracer;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.ThreadPoolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

class AzureMetadataService implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(AzureMetadataService.class);

    private static final ScheduledExecutorService scheduledExecutor = Executors
        .newSingleThreadScheduledExecutor(ThreadPoolUtils.createDaemonThreadFactory(AzureMetadataService.class));

    // this version has the smallest payload.
    private static final String API_VERSION = "api-version=2017-08-01";
    private static final String JSON_FORMAT = "format=json";
    private static final String BASE_URL = "http://169.254.169.254/metadata/instance/compute";
    private static final String ENDPOINT = BASE_URL + "?" + API_VERSION + "&" + JSON_FORMAT;

    private final AttachStatsbeat attachStatsbeat;
    private final CustomDimensions customDimensions;
    private final HttpPipeline httpPipeline;
    private final Consumer<MetadataInstanceResponse> vmMetadataServiceCallback;

    AzureMetadataService(AttachStatsbeat attachStatsbeat, CustomDimensions customDimensions,
        Consumer<MetadataInstanceResponse> vmMetadataServiceCallback) {
        this.attachStatsbeat = attachStatsbeat;
        this.customDimensions = customDimensions;
        this.httpPipeline = new HttpPipelineBuilder().tracer(new NoopTracer()).build();
        this.vmMetadataServiceCallback = vmMetadataServiceCallback;
    }

    void scheduleWithFixedDelay(long interval) {
        // Querying Azure Metadata Service is required for every 15 mins since VM id will get updated
        // frequently.
        // Starting and restarting a VM will generate a new VM id each time.
        // Statsbeat can be shutdown before this call, so check if it is still running.
        if (!scheduledExecutor.isShutdown() && !scheduledExecutor.isTerminated()) {
            scheduledExecutor.scheduleWithFixedDelay(this, 60, interval, TimeUnit.SECONDS);
        }
    }

    void shutdown() {
        logger.debug("Shutting down Azure Metadata Service.");
        scheduledExecutor.shutdown();
    }

    // only used by tests
    void updateMetadata(String response) throws IOException {
        updateMetadata(MetadataInstanceResponse.fromJson(response));
    }

    // visible for testing
    private void updateMetadata(MetadataInstanceResponse metadataInstanceResponse) {
        vmMetadataServiceCallback.accept(metadataInstanceResponse);
        attachStatsbeat.updateMetadataInstance(metadataInstanceResponse);
        customDimensions.setResourceProviderVm();

        // osType from the Azure Metadata Service has a higher precedence over the running app’s
        // operating system.
        String osType = metadataInstanceResponse.getOsType();
        switch (osType) {
            case "Windows":
                customDimensions.setOperatingSystem(OperatingSystem.OS_WINDOWS);
                break;

            case "Linux":
                customDimensions.setOperatingSystem(OperatingSystem.OS_LINUX);
                break;

            default:
                // unknown, ignore
        }
    }

    @Override
    public void run() {
        HttpRequest request = new HttpRequest(HttpMethod.GET, ENDPOINT);
        request.setHeader(HttpHeaderName.fromString("Metadata"), "true");
        HttpResponse response;
        try {
            response = httpPipeline.sendSync(request, Context.NONE);
        } catch (RuntimeException e) {
            logger.debug("Shutting down AzureMetadataService scheduler: is not running on Azure VM or VMSS");
            logger.trace(e.getMessage(), e);
            scheduledExecutor.shutdown();
            return;
        }

        if (response == null) {
            // this shouldn't happen, the mono should complete with a response or a failure
            throw new AssertionError("http response mono returned empty");
        }
        String json = response.getBodyAsString().block();
        if (json == null) {
            // this shouldn't happen, the mono should complete with a response or a failure
            throw new AssertionError("response body mono returned empty");
        }

        MetadataInstanceResponse metadataInstanceResponse;
        try {
            metadataInstanceResponse = MetadataInstanceResponse.fromJson(json);
        } catch (IOException e) {
            logger.debug("Shutting down AzureMetadataService scheduler:"
                + " error parsing response from Azure Metadata Service: {}", json, e);
            scheduledExecutor.shutdown();
            return;
        }

        updateMetadata(metadataInstanceResponse);
    }
}
