// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.statsbeat;

import com.azure.monitor.opentelemetry.exporter.implementation.builders.StatsbeatTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryItemExporter;

import javax.annotation.Nullable;
import java.util.Collections;

class AttachStatsbeat extends BaseStatsbeat {

    private static final String ATTACH_METRIC_NAME = "Attach";

    private static final String UNKNOWN_RP_ID = "unknown";

    private static final String WEBSITE_SITE_NAME = "WEBSITE_SITE_NAME";
    private static final String WEBSITE_HOSTNAME = "WEBSITE_HOSTNAME";
    private static final String WEBSITE_HOME_STAMPNAME = "WEBSITE_HOME_STAMPNAME";

    private final CustomDimensions customDimensions;
    private volatile String resourceProviderId;
    private volatile MetadataInstanceResponse metadataInstanceResponse;

    AttachStatsbeat(CustomDimensions customDimensions) {
        super(customDimensions);
        this.customDimensions = customDimensions;
        resourceProviderId = initResourceProviderId(customDimensions.getResourceProvider(), null);
    }

    @Override
    protected void send(TelemetryItemExporter exporter) {
        // WEBSITE_HOSTNAME is lazily set in Linux Consumption Plan.
        if (resourceProviderId == null || resourceProviderId.isEmpty()) {
            resourceProviderId = initResourceProviderId(customDimensions.getResourceProvider(), null);
        }

        StatsbeatTelemetryBuilder telemetryBuilder = createStatsbeatTelemetry(ATTACH_METRIC_NAME, 0);
        telemetryBuilder.addProperty("rpId", resourceProviderId);
        exporter.send(Collections.singletonList(telemetryBuilder.build()));
    }

    /**
     * Returns the unique identifier of the resource provider.
     */
    String getResourceProviderId() {
        return resourceProviderId;
    }

    MetadataInstanceResponse getMetadataInstanceResponse() {
        return metadataInstanceResponse;
    }

    void updateMetadataInstance(MetadataInstanceResponse response) {
        metadataInstanceResponse = response;
        resourceProviderId = initResourceProviderId(ResourceProvider.RP_VM, response);
    }

    // visible for testing
    static String initResourceProviderId(
        ResourceProvider resourceProvider, @Nullable MetadataInstanceResponse response) {
        switch (resourceProvider) {
            case RP_APPSVC:
                // Linux App Services doesn't have WEBSITE_HOME_STAMPNAME yet. An ask has been submitted.
                return System.getenv(WEBSITE_SITE_NAME) + "/" + System.getenv(WEBSITE_HOME_STAMPNAME);
            case RP_FUNCTIONS:
                return System.getenv(WEBSITE_HOSTNAME);
            case RP_VM:
                if (response != null) {
                    return response.getVmId() + "/" + response.getSubscriptionId();
                } else {
                    return UNKNOWN_RP_ID;
                }
            case RP_AKS: // TODO will update resourceProviderId when cluster_id becomes available from the
                // AKS AzureMetadataService extension.
            case UNKNOWN:
                return UNKNOWN_RP_ID;
        }
        return UNKNOWN_RP_ID;
    }
}
