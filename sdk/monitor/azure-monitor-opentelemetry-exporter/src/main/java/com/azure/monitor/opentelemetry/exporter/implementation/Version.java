package com.azure.monitor.opentelemetry.exporter.implementation;

import com.azure.core.util.CoreUtils;
import io.opentelemetry.api.OpenTelemetry;

import java.util.Map;

public final class Version {
    public static final String UNKNOWN_VERSION_VALUE = "unknown";

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

    public static String getArtifactName() {
        return artifactName;
    }

    public static String getArtifactVersion() {
        return artifactVersion;
    }

    public static String getSdkVersion() {
        return sdkVersionString;
    }

    private static String getJavaVersion() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.8")) {
            return "1.8";
        }
        int dot = version.indexOf(".");
        if (dot != -1) {
            return version.substring(0, dot);
        }
        return version;
    }

    private static String getOpenTelemetryApiVersion() {
        Package objPackage = OpenTelemetry.class.getPackage();
        if (objPackage == null) {
            return null;
        }
        String version = objPackage.getSpecificationVersion();
        return version != null ? version : UNKNOWN_VERSION_VALUE;
    }

    private Version() {
    }
}
