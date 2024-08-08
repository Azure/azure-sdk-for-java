// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import com.azure.monitor.opentelemetry.exporter.implementation.statsbeat.MetadataInstanceResponse;
import com.azure.monitor.opentelemetry.exporter.implementation.statsbeat.RpAttachType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Helper class for reading data from a properties file found on the class path.
 */
public final class PropertyHelper {

    private static final Logger logger = LoggerFactory.getLogger(PropertyHelper.class);

    public static final String SDK_VERSION_FILE_NAME = "ai.sdk-version.properties";

    public static final String VERSION_STRING_PREFIX = "java:";
    public static final String VERSION_PROPERTY_NAME = "version";

    public static final String UNKNOWN_VERSION_VALUE = "unknown";

    private static volatile char rpIntegrationChar;

    /**
     * Reads the properties from a properties file.
     *
     * @param name of the properties file.
     * @return A {@link Properties} object containing the properties read from the provided file.
     * @throws IOException in case
     */
    private static Properties getProperties(String name) throws IOException {
        Properties props = new Properties();
        ClassLoader classLoader = PropertyHelper.class.getClassLoader();
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }

        // Look in the class loader's default location.
        InputStream inputStream = classLoader.getResourceAsStream(name);
        if (inputStream != null) {
            try {
                props.load(inputStream);
            } finally {
                inputStream.close();
            }
        }

        return props;
    }

    /**
     * A method that loads the properties file that contains SDK-Version data.
     *
     * @return The properties or null if not found.
     */
    private static Properties getSdkVersionProperties() {
        try {
            return getProperties(SDK_VERSION_FILE_NAME);
        } catch (IOException e) {
            logger.error("Could not find sdk version file '{}'", SDK_VERSION_FILE_NAME, e);
            return new Properties();
        }
    }

    /**
     * Returns the SDK version string, the <i>version-number</i> prefixed with "java:". The
     * <i>version-number</i> is the value of the {@value #VERSION_PROPERTY_NAME} property in {@value
     * #SDK_VERSION_FILE_NAME}. If the properties file cannot be read, {@value #UNKNOWN_VERSION_VALUE}
     * is used for the <i>version-number</i>.
     *
     * @return "java:<i>version-number</i>" or "java:unknown"
     */
    public static String getQualifiedSdkVersionString() {
        return SdkPropertyValues.sdkVersionString;
    }

    public static void setSdkNamePrefix(String sdkNamePrefix) {
        SdkPropertyValues.sdkVersionString =
            sdkNamePrefix + VERSION_STRING_PREFIX + SdkPropertyValues.SDK_VERSION_NUMBER;
    }

    public static String getSdkVersionNumber() {
        return SdkPropertyValues.SDK_VERSION_NUMBER;
    }

    /** Is resource provider (Azure Spring Cloud, AppService, Azure Functions, AKS, VM...). */
    public static boolean isRpIntegration() {
        return rpIntegrationChar != 0;
    }

    public static void setRpIntegrationChar(char ch) {
        rpIntegrationChar = ch;
    }

    public static void lazyUpdateVmRpIntegration(MetadataInstanceResponse response) {
        rpIntegrationChar = 'v';
        PropertyHelper.setSdkNamePrefix(getRpIntegrationSdkNamePrefix());
        RpAttachType.setRpAttachType(RpAttachType.STANDALONE_AUTO);
    }

    public static String getRpIntegrationSdkNamePrefix() {
        StringBuilder sdkNamePrefix = new StringBuilder(4);
        sdkNamePrefix.append(rpIntegrationChar);
        if (SystemInformation.isWindows()) {
            sdkNamePrefix.append('w');
        } else if (SystemInformation.isLinux()) {
            sdkNamePrefix.append('l');
        } else {
            logger.warn("could not detect os: {}", System.getProperty("os.name"));
            sdkNamePrefix.append('u');
        }

        sdkNamePrefix.append(getRpAttachTypeChar());
        sdkNamePrefix.append('_');
        return sdkNamePrefix.toString();
    }

    private static char getRpAttachTypeChar() {
        if (RpAttachType.getRpAttachType() == RpAttachType.INTEGRATED_AUTO) {
            return 'i';
        } else if (RpAttachType.getRpAttachType() == RpAttachType.STANDALONE_AUTO) {
            return 'a';
        }
        return 'm'; // Manual (This is the default when RP integration is not used)
    }

    private static class SdkPropertyValues {
        private static final String SDK_VERSION_NUMBER;
        private static volatile String sdkVersionString;

        static {
            Properties properties = getSdkVersionProperties();
            SDK_VERSION_NUMBER = properties.getProperty(VERSION_PROPERTY_NAME, UNKNOWN_VERSION_VALUE);
            sdkVersionString = VERSION_STRING_PREFIX + SDK_VERSION_NUMBER;
        }

        private SdkPropertyValues() {
        }
    }

    private PropertyHelper() {
    }
}
