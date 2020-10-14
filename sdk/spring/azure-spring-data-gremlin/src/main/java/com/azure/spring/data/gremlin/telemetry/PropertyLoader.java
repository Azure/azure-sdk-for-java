// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.telemetry;

import org.springframework.lang.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class PropertyLoader {

    private PropertyLoader() {

    }

    private static final String PROJECT_PROPERTY_FILE = "/META-INF/project.properties";

    private static final String TELEMETRY_CONFIG_FILE = "/telemetry.config";

    public static String getProjectVersion() {
        return getPropertyByName("project.version", PROJECT_PROPERTY_FILE);
    }

    public static String getTelemetryInstrumentationKey() {
        return getPropertyByName("telemetry.instrumentationKey", TELEMETRY_CONFIG_FILE);
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
