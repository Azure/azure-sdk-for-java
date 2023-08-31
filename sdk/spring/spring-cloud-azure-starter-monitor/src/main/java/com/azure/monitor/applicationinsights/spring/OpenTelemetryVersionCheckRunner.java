// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.monitor.applicationinsights.spring;

import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This component alerts the user to the fact that the OpenTelemetry version used is not compatible
 * with the starter. One use case is Spring Boot 3 using OpenTelemetry.
 */
@Component
public class OpenTelemetryVersionCheckRunner implements CommandLineRunner {
    private static final Logger LOG = LoggerFactory.getLogger(OpenTelemetryVersionCheckRunner.class);

    // If this version is not up-to-date, a test will fail.
    public static final String STARTER_OTEL_VERSION = "1.28.0";

    private final Resource otelResource;

    public OpenTelemetryVersionCheckRunner(Resource otelResource) {
        this.otelResource = otelResource;
    }

    /**
     * To verify the OpenTelemetry version at the application start-up.
     *
     * @param args args
     */
    @Override
    public void run(String... args) {
        try {
            int starterOTelVersionAsInt = computeOTelVersionAsInt(STARTER_OTEL_VERSION);
            int currentOTelVersionAsInt = findCurrentOTelVersion();
            checkOpenTelemetryVersion(currentOTelVersionAsInt, starterOTelVersionAsInt);
        } catch (Exception e) {
            LOG.warn(
                "An unexpected issue has happened during the verification of the OpenTelemetry version.",
                e);
        }
    }

    private int findCurrentOTelVersion() {
        String currentOTelVersion = otelResource.getAttribute(ResourceAttributes.TELEMETRY_SDK_VERSION);
        return computeOTelVersionAsInt(currentOTelVersion);
    }

    private static int computeOTelVersionAsInt(String oTelVersion) {
        String oTelVersionWithoutDots = oTelVersion.replace(".", "");
        return Integer.parseInt(oTelVersionWithoutDots);
    }

    private static void checkOpenTelemetryVersion(
        int currentOTelVersionAsInt, int starterOTelVersionAsInt) {
        String beginningOfWarnMessage =
            "The OpenTelemetry version is not compatible with the spring-cloud-azure-starter-monitor dependency. The OpenTelemetry version should be "
                + STARTER_OTEL_VERSION
                + ". ";
        if (currentOTelVersionAsInt < starterOTelVersionAsInt) {
            LOG.warn(
                beginningOfWarnMessage
                    + "Please look at the spring-cloud-azure-starter-monitor documentation to fix this.");
        } else if (currentOTelVersionAsInt > starterOTelVersionAsInt) {
            LOG.warn(
                beginningOfWarnMessage
                    + "Please use the last version of spring-cloud-azure-starter-monitor and look at the spring-cloud-azure-starter-monitor documentation to fix the compatibility issue.");
        }
    }
}
