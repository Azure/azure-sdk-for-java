// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.statsbeat;

import com.azure.monitor.opentelemetry.exporter.implementation.builders.StatsbeatTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.PropertyHelper;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.Strings;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.SystemInformation;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.VersionGenerator;

public class CustomDimensions {

    private volatile ResourceProvider resourceProvider;
    private volatile OperatingSystem operatingSystem;

    private final String attachType;
    private final String runtimeVersion;
    private final String language;
    private final String sdkVersion;

    // visible for testing
    CustomDimensions() {
        if ("java".equals(System.getenv("FUNCTIONS_WORKER_RUNTIME"))) {
            resourceProvider = ResourceProvider.RP_FUNCTIONS;
        } else if (!Strings.isNullOrEmpty(System.getenv("WEBSITE_SITE_NAME"))) {
            resourceProvider = ResourceProvider.RP_APPSVC;
        } else if (!Strings.isNullOrEmpty(System.getenv("KUBERNETES_SERVICE_HOST"))) {
            resourceProvider = ResourceProvider.RP_AKS;
        } else if (!Strings.isNullOrEmpty(System.getenv("APPLICATIONINSIGHTS_SPRINGCLOUD_SERVICE_ID"))) {
            resourceProvider = ResourceProvider.RP_SPRING_CLOUD;
        } else {
            resourceProvider = ResourceProvider.UNKNOWN;
        }

        operatingSystem = initOperatingSystem();
        sdkVersion = initSdkVersion();
        runtimeVersion = System.getProperty("java.version");

        attachType = RpAttachType.getRpAttachTypeString();
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

    // package protected - visible for test
    static String initSdkVersion() {
        if (RpAttachType.getRpAttachType() == RpAttachType.MANUAL) {
            return VersionGenerator.getSdkVersion();
        }
        String qualifiedSdkVersionString = PropertyHelper.getQualifiedSdkVersionString();
        return qualifiedSdkVersionString.substring(qualifiedSdkVersionString.lastIndexOf(':')+1);
    }
}
