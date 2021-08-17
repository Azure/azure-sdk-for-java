// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.utils;

import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Properties;

/**
 * Util class to load property files.
 */
public class PropertyLoader {

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
