// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.utils;

import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

/**
 * Util class to load property files.
 */
public class PropertyLoader {
    private static final String PROJECT_PROPERTY_FILE = "/META-INF/project.properties";

    /**
     * To load a property value from the file.
     * @param file The source file to load properties from.
     * @param property The property name.
     * @return The property value.
     */
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

    /**
     * To load a {@link Properties} Object from the properties file under classpath
     *
     * @param file The source file to load properties from.
     * @throws UncheckedIOException If an I/O error occurs.
     * @return The {@link Properties} Object.
     */
    public static Properties loadPropertiesFromClassPath(String file) {
        try {
            return PropertiesLoaderUtils.loadAllProperties(file);
        } catch (IOException exception) {
            throw new UncheckedIOException("Fail to load " + file, exception);
        }
    }
}
