package com.azure.monitor.opentelemetry.exporter.utils;

import com.azure.core.util.CoreUtils;
import io.opentelemetry.api.OpenTelemetry;
import java.util.Map;

public final class PropertyHelper {
    public static final String VERSION_STRING_PREFIX = "java";
    public static final String VERSION_STRING_APPENDER = ":";
    public static final String UNKNOWN_VERSION_VALUE = "unknown";
    private static final String SDK_VERSION = "version";
    private static final Map<String, String> properties =
        CoreUtils.getProperties("azure-monitor-opentelemetry-exporter.properties");

    private static String getOpenTelemetryApiVersion() {
        Package objPackage = OpenTelemetry.class.getPackage();
        if(objPackage != null && objPackage.getSpecificationVersion() != null) {
            return objPackage.getSpecificationVersion();
        }
        return UNKNOWN_VERSION_VALUE;
    }

    private static String getJavaVersion() {
        String version = System.getProperty("java.version");
        if(version.startsWith("1.")) {
            return version.substring(0, 3);
        } else {
            int dot = version.indexOf(".");
            if(dot != -1) {
                return version.substring(0, dot);
            }
        }
        return UNKNOWN_VERSION_VALUE;
    }

    public static String getQualifiedSdkVersionString() {
        return SdkPropertyValues.sdkVersionString;
    }

    public static String getSdkVersionNumber() {
        return SdkPropertyValues.SDK_VERSION_NUMBER;
    }

    public static String getPropertyValue(String key, String defaultValue) {
        return properties.getOrDefault(key, defaultValue);
    }

    private static class SdkPropertyValues {
        private static final String SDK_VERSION_NUMBER;
        private static volatile String sdkVersionString;

        static {
            SDK_VERSION_NUMBER = properties.getOrDefault(SDK_VERSION, UNKNOWN_VERSION_VALUE);
            sdkVersionString = VERSION_STRING_PREFIX +
                getJavaVersion() +
                VERSION_STRING_APPENDER +
                "ot" + getOpenTelemetryApiVersion() +
                VERSION_STRING_APPENDER +
                "ext" + SDK_VERSION_NUMBER;
        }

        private SdkPropertyValues() {}
    }

    private PropertyHelper() {}
}
