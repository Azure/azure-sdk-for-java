// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.util.logging.ClientLogger;
import java.io.IOException;
import java.util.Properties;

/*
 * Gets the SDK information for this library component.
 */
class QueueConfiguration {
    //TODO: Eventually remove these hardcoded strings with https://github.com/Azure/azure-sdk-for-java/issues/3141
    private final ClientLogger logger = new ClientLogger(QueueClient.class);
    private final Properties properties = new Properties();

    public QueueConfiguration() {
        loadProperties();
    }

    private void loadProperties() {
        try {
            properties.load(this.getClass().getClassLoader().getResourceAsStream("client.properties"));
        } catch (IOException e) {
            throw logger.logExceptionAsError(new RuntimeException("Please check the client properties for queue module. "
                + "Error Details: " + e.getMessage()));
        }
    }

    public String getName() {
        String name = properties.getProperty("artifactId");
        if (name == null || name.contains("${")) {
            throw logger.logExceptionAsError(new RuntimeException("Please check the client properties for queue module name. "
                + "Module Name: " + name));
        }
        return name;
    }

    public String getVersion() {
        String version = properties.getProperty("version");
        if (version == null || version.contains("${")) {
            throw logger.logExceptionAsError(new RuntimeException("Please check the client properties for queue module version. "
                + "Module Version: " + version));
        }
        return version;
    }
}
