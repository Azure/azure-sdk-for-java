// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.common;

import org.springframework.lang.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Load properties from files
 */
public final class PropertyLoader {

    private static final String PROJECT_PROPERTY_FILE = "/META-INF/project.properties";

    private static final String APPLICATION_PROPERTY_FILE = "/application.properties";

    private static final String APPLICATION_YML_FILE = "/application.yml";

    private static final String TELEMETRY_CONFIG_FILE = "/telemetry.config";

    private PropertyLoader() {
    }

    /**
     * Get project version from /META-INF/project.properties
     *
     * @return String project version
     */
    public static String getProjectVersion() {
        return getPropertyByName("project.version", PROJECT_PROPERTY_FILE);
    }

    /**
     * Get telemetry instrumentation key from /telemetry.config
     *
     * @return String telemetry instrumentation key
     */
    public static String getTelemetryInstrumentationKey() {
        return getPropertyByName("telemetry.instrumentationKey", TELEMETRY_CONFIG_FILE);
    }

    /**
     * Check if telemetry is allowed
     *
     * @return boolean if telemetry is allowed
     */
    public static boolean isApplicationTelemetryAllowed() {
        String allowed = getPropertyByName("cosmosdb.telemetryAllowed", APPLICATION_PROPERTY_FILE);

        if (allowed == null) {
            allowed = getPropertyByName("telemetryAllowed", APPLICATION_YML_FILE);
        }

        //  Default, no telemetry
        if (allowed == null) {
            return false;
        } else {
            return !allowed.equalsIgnoreCase("false");
        }
    }

    private static String getPropertyByName(@NonNull String name, @NonNull String filename) {
        final Properties properties = new Properties();
        final InputStream inputStream = PropertyLoader.class.getResourceAsStream(filename);

        if (inputStream == null) {
            return null;
        }

        try {
            properties.load(inputStream);
        } catch (IOException e) {
            // Omitted
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                // Omitted
            }
        }

        return properties.getProperty(name);
    }
}
