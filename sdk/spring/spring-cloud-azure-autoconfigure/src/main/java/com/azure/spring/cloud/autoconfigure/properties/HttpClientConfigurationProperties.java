// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.properties;

import com.azure.spring.core.properties.client.ClientProperties;
import com.azure.spring.core.properties.client.HttpClientProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This class is only for configuration-processor to find the sub-namespace of logging. The {@link LoggingProperties} is
 * defined as static inner class in {@link ClientProperties} and {@link HttpClientProperties} extends the
 * {@link ClientProperties}, so when the processor process the {@link HttpClientProperties} class it will skip the
 * logging field for it's an external class.
 */
public class HttpClientConfigurationProperties extends HttpClientProperties {

    @NestedConfigurationProperty
    private final LoggingProperties logging = new LoggingProperties();

    @Override
    public LoggingProperties getLogging() {
        return logging;
    }

}
