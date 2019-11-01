// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.util.logging.ClientLogger;
import java.io.InputStream;
import java.util.Properties;

/**
 * Gets the SDK information for this library component.
 */
class AzureConfiguration {

    private static final ClientLogger LOGGER = new ClientLogger(AzureConfiguration.class);
    private static final String NAME;
    private static final String VERSION;
    private static final String VERSION_NOT_DEFINED = "Version not defined";
    private static final String ARTIFACTID_NOT_DEFINED = "ArtifactId not defined";
    private static final String AZURE_APPCONFIG_PROPERTIES = "azure-appconfig.properties";
    private static final String VERSION_PROPERTY_NAME = "version";
    private static final String ARTIFACT_PROPERTY_NAME = "artifact";

    static {
        String version = null;
        String artifactId = null;
        try (InputStream inputStream = AzureConfiguration.class.getClassLoader()
            .getResourceAsStream(AZURE_APPCONFIG_PROPERTIES)) {
            Properties properties = new Properties();
            properties.load(inputStream);
            version = properties.getProperty(VERSION_PROPERTY_NAME);
            artifactId = properties.getProperty(ARTIFACT_PROPERTY_NAME);
        } catch (Exception ex) {
            LOGGER.warning("Failed to get AppConfiguration version and name from properties.", ex);
        }
        VERSION = version != null ? version : VERSION_NOT_DEFINED;
        NAME = artifactId != null ? artifactId : ARTIFACTID_NOT_DEFINED;
    }

    /**
     * Returns the version of this client library.
     *
     * @return The version of this client library.
     */
    public static String getVersion() {
        return VERSION;
    }

    /**
     * Returns the name of this client library.
     *
     * @return The name of this client library.
     */
    public static String getName() {
        return NAME;
    }

    private AzureConfiguration() {
        // no instances
    }
}
