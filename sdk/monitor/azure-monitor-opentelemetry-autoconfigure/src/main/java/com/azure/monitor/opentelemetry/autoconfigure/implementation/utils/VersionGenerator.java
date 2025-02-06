// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.utils;

import com.azure.core.util.CoreUtils;

import java.util.Map;

/**
 * This class contains several utility functions to populate sdk version string
 */
public final class VersionGenerator {
    private static final String UNKNOWN_VERSION_VALUE = "unknown";

    private static final String sdkVersionString;

    static {
        String componentName = null;
        String componentVersion = null;

        Map<String, String> springDistroProperties
            = CoreUtils.getProperties("azure-spring-cloud-azure-starter-monitor.properties");
        String springDistroVersion = springDistroProperties.get("version");
        if (springDistroVersion != null) {
            componentName = "dss";
            componentVersion = springDistroVersion;
        }

        Map<String, String> quarkusProperties = CoreUtils.getProperties("quarkus-exporter.properties");
        String quarkusVersion = quarkusProperties.get("version");
        if (quarkusVersion != null) {
            componentName = "dsq";
            componentVersion = quarkusVersion;
        }

        if (componentName == null) {
            componentName = "ext";
            Map<String, String> otelAutoconfigureProperties
                = CoreUtils.getProperties("azure-monitor-opentelemetry-autoconfigure.properties");
            componentVersion = otelAutoconfigureProperties.get("version");
        }

        sdkVersionString = getPrefix() + "java" + getJavaVersion() + getJavaRuntime() + ":" + "otel"
            + getOpenTelemetryApiVersion() + ":" + componentName + componentVersion;
    }

    private static String getPrefix() {
        return getResourceProvider() + getOs() + "_";
    }

    private static String getResourceProvider() {
        if ("java".equals(System.getenv("FUNCTIONS_WORKER_RUNTIME"))) {
            return "f";
        } else if (!Strings.isNullOrEmpty(System.getenv("WEBSITE_SITE_NAME"))) {
            return "a";
        } else if (!Strings.isNullOrEmpty(System.getenv("APPLICATIONINSIGHTS_SPRINGCLOUD_SERVICE_ID"))) {
            // Spring Cloud needs to be checked before AKS since it runs on AKS
            return "s";
        } else if (!Strings.isNullOrEmpty(System.getenv("AKS_ARM_NAMESPACE_ID"))) {
            return "k";
        }
        return "u";
    }

    private static String getOs() {
        if (SystemInformation.isWindows()) {
            return "w";
        } else if (SystemInformation.isLinux()) {
            return "l";
        }
        return "u";
    }

    /**
     * This method returns sdk version string as per the below format javaX:otelY:extZ X = Java
     * version, Y = opentelemetry version, Z = exporter version
     *
     * @return sdkVersionString.
     */
    public static String getSdkVersion() {
        return sdkVersionString;
    }

    private static String getJavaVersion() {
        return System.getProperty("java.version");
    }

    private static String getJavaRuntime() {
        if (isGraalVmNative()) {
            return "!native";
        }
        return "";
    }

    private static boolean isGraalVmNative() {
        String imageCode = System.getProperty("org.graalvm.nativeimage.imagecode");
        return imageCode != null;
    }

    private static String getOpenTelemetryApiVersion() {
        Map<String, String> properties = CoreUtils.getProperties("io/opentelemetry/api/version.properties");
        if (properties == null) {
            return UNKNOWN_VERSION_VALUE;
        }
        String version = properties.get("sdk.version");
        return version != null ? version : UNKNOWN_VERSION_VALUE;
    }

    private VersionGenerator() {
    }
}
