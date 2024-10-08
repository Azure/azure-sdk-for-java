// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.statsbeat;

import com.azure.monitor.opentelemetry.exporter.implementation.builders.StatsbeatTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryItemExporter;
import reactor.util.annotation.Nullable;

import java.util.Collections;
import java.util.function.Function;

class AttachStatsbeat extends BaseStatsbeat {

    private static final String ATTACH_METRIC_NAME = "Attach";

    private static final String UNKNOWN_RP_ID = "unknown";

    private static final String WEBSITE_SITE_NAME = "WEBSITE_SITE_NAME";
    private static final String WEBSITE_HOSTNAME = "WEBSITE_HOSTNAME";
    private static final String WEBSITE_HOME_STAMPNAME = "WEBSITE_HOME_STAMPNAME";

    private final CustomDimensions customDimensions;
    private final Function<String, String> systemGetenvFn;

    private volatile String resourceProviderId;
    private volatile MetadataInstanceResponse metadataInstanceResponse;

    AttachStatsbeat(CustomDimensions customDimensions) {
        super(customDimensions);
        this.customDimensions = customDimensions;
        this.systemGetenvFn = System::getenv;
        resourceProviderId = initResourceProviderId(customDimensions.getResourceProvider(), null, systemGetenvFn);
    }

    @Override
    protected void send(TelemetryItemExporter exporter) {
        // WEBSITE_HOSTNAME is lazily set in Linux Consumption Plan.
        if (resourceProviderId == null || resourceProviderId.isEmpty()) {
            resourceProviderId = initResourceProviderId(customDimensions.getResourceProvider(), null, systemGetenvFn);
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
        resourceProviderId = initResourceProviderId(ResourceProvider.RP_VM, response, systemGetenvFn);
    }

    // visible for testing
    static String initResourceProviderId(ResourceProvider resourceProvider, @Nullable MetadataInstanceResponse response,
        Function<String, String> envVarFn) {
        switch (resourceProvider) {
            case RP_APPSVC:
                // Linux App Services doesn't have WEBSITE_HOME_STAMPNAME yet. An ask has been submitted.
                return envVarFn.apply(WEBSITE_SITE_NAME) + "/" + envVarFn.apply(WEBSITE_HOME_STAMPNAME);

            case RP_FUNCTIONS:
                return envVarFn.apply(WEBSITE_HOSTNAME);

            case RP_VM:
                if (response != null) {
                    return response.getVmId() + "/" + response.getSubscriptionId();
                } else {
                    return UNKNOWN_RP_ID;
                }
            case RP_AKS:
                return envVarFn.apply("AKS_ARM_NAMESPACE_ID");

            case UNKNOWN:
                return UNKNOWN_RP_ID;
        }
        return UNKNOWN_RP_ID;
    }
}
