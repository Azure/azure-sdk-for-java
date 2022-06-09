// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import com.azure.core.util.CoreUtils;
import java.util.Map;

/**
 * This class contains several utility functions to populate sdk version string
 */
public final class VersionGenerator {
    private static final String UNKNOWN_VERSION_VALUE = "unknown";

    private static final String artifactName;
    private static final String artifactVersion;

    private static final String sdkVersionString;

    static {
        Map<String, String> properties =
            CoreUtils.getProperties("azure-monitor-opentelemetry-exporter.properties");

        artifactName = properties.get("name");
        artifactVersion = properties.get("version");

        sdkVersionString = "java" +
            getJavaVersion() +
            ":" +
            "ot" + getOpenTelemetryApiVersion() +
            ":" +
            "ext" + artifactVersion;
    }

    /**
     * This method returns artifact name.
     * @return artifactName.
     */
    public static String getArtifactName() {
        return artifactName;
    }

    /**
     * This method returns artifact version.
     * @return artifactVersion.
     */
    public static String getArtifactVersion() {
        return artifactVersion;
    }

    /**
     * This method returns sdk version string as per the below format
     * javaX:otelY:extZ
     * X = Java version, Y = opentelemetry version, Z = exporter version
     * @return sdkVersionString.
     */
    public static String getSdkVersion() {
        return sdkVersionString;
    }

    private static String getJavaVersion() {
        return System.getProperty("java.version");
    }

    private static String getOpenTelemetryApiVersion() {
        Map<String, String> properties =
            CoreUtils.getProperties("io/opentelemetry/api/version.properties");
        if (properties == null) {
            return UNKNOWN_VERSION_VALUE;
        }
        String version = properties.get("sdk.version");
        return version != null ? version : UNKNOWN_VERSION_VALUE;
    }

    private VersionGenerator() {
    }
}
