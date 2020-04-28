// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Util class to load property files.
 */
public class PropertyLoader {
    private static final String PROJECT_PROPERTY_FILE = "/META-INF/project.properties";

    private static final String TELEMETRY_CONFIG_FILE = "/telemetry.config";

    private static String getProperty(String file, String property) {
        try (InputStream inputStream = PropertyLoader.class.getResourceAsStream(file)) {
            if (inputStream != null) {
                final Properties properties = new Properties();
                properties.load(inputStream);

                return properties.getProperty(property);
            }
        } catch (IOException e) {
            // Omitted
        }

        return "unknown";
    }

    public static String getProjectVersion() {
        return getProperty(PROJECT_PROPERTY_FILE, "project.version");
    }

    public static String getTelemetryInstrumentationKey() {
        return getProperty(TELEMETRY_CONFIG_FILE, "telemetry.instrumentationKey");
    }
}
