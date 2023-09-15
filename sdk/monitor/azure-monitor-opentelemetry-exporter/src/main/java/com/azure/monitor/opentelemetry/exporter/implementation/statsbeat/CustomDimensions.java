// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.statsbeat;

import com.azure.monitor.opentelemetry.exporter.implementation.builders.StatsbeatTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.PropertyHelper;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.SystemInformation;

public class CustomDimensions {

    private volatile ResourceProvider resourceProvider;
    private volatile OperatingSystem operatingSystem;

    private final String attachType;
    private final String runtimeVersion;
    private final String language;
    private final String sdkVersion;

    // visible for testing
    CustomDimensions() {
        String qualifiedSdkVersion = PropertyHelper.getQualifiedSdkVersionString();

        if (qualifiedSdkVersion.startsWith("aw")) {
            resourceProvider = ResourceProvider.RP_APPSVC;
            operatingSystem = OperatingSystem.OS_WINDOWS;
        } else if (qualifiedSdkVersion.startsWith("al")) {
            resourceProvider = ResourceProvider.RP_APPSVC;
            operatingSystem = OperatingSystem.OS_LINUX;
        } else if (qualifiedSdkVersion.startsWith("kw")) {
            resourceProvider = ResourceProvider.RP_AKS;
            operatingSystem = OperatingSystem.OS_WINDOWS;
        } else if (qualifiedSdkVersion.startsWith("kl")) {
            resourceProvider = ResourceProvider.RP_AKS;
            operatingSystem = OperatingSystem.OS_LINUX;
        } else if (qualifiedSdkVersion.startsWith("fw")) {
            resourceProvider = ResourceProvider.RP_FUNCTIONS;
            operatingSystem = OperatingSystem.OS_WINDOWS;
        } else if (qualifiedSdkVersion.startsWith("fl")) {
            resourceProvider = ResourceProvider.RP_FUNCTIONS;
            operatingSystem = OperatingSystem.OS_LINUX;
        } else {
            resourceProvider = ResourceProvider.UNKNOWN;
            operatingSystem = initOperatingSystem();
        }

        sdkVersion = qualifiedSdkVersion.substring(qualifiedSdkVersion.lastIndexOf(':') + 1);
        runtimeVersion = System.getProperty("java.version");

        attachType = RpAttachType.getRpAttachType();
        language = "java";
    }

    public ResourceProvider getResourceProvider() {
        return resourceProvider;
    }

    public OperatingSystem getOperatingSystem() {
        return operatingSystem;
    }

    public void setResourceProvider(ResourceProvider resourceProvider) {
        this.resourceProvider = resourceProvider;
    }

    public void setOperatingSystem(OperatingSystem operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    void populateProperties(StatsbeatTelemetryBuilder telemetryBuilder, String customerIkey) {
        telemetryBuilder.addProperty("rp", resourceProvider.getValue());
        telemetryBuilder.addProperty("os", operatingSystem.getValue());
        telemetryBuilder.addProperty("attach", attachType);
        telemetryBuilder.addProperty("cikey", customerIkey);
        telemetryBuilder.addProperty("runtimeVersion", runtimeVersion);
        telemetryBuilder.addProperty("language", language);
        telemetryBuilder.addProperty("version", sdkVersion);
    }

    private static OperatingSystem initOperatingSystem() {
        if (SystemInformation.isWindows()) {
            return OperatingSystem.OS_WINDOWS;
        } else if (SystemInformation.isLinux()) {
            return OperatingSystem.OS_LINUX;
        } else {
            return OperatingSystem.OS_UNKNOWN;
        }
    }
}
