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

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import com.azure.core.util.CoreUtils;
import java.util.Map;

/** This class contains several utility functions to populate sdk version string */
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

        sdkVersionString =
            "java"
                + getJavaVersion()
                + ":"
                + "otel"
                + getOpenTelemetryApiVersion()
                + ":"
                + "ext"
                + artifactVersion;
    }

    /**
     * This method returns artifact name.
     *
     * @return artifactName.
     */
    public static String getArtifactName() {
        return artifactName;
    }

    /**
     * This method returns artifact version.
     *
     * @return artifactVersion.
     */
    public static String getArtifactVersion() {
        return artifactVersion;
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

    private static String getOpenTelemetryApiVersion() {
        Map<String, String> properties =
            CoreUtils.getProperties("io/opentelemetry/api/version.properties");
        if (properties == null) {
            return UNKNOWN_VERSION_VALUE;
        }
        String version = properties.get("sdk.version");
        return version != null ? version : UNKNOWN_VERSION_VALUE;
    }

    private VersionGenerator() {}
}
