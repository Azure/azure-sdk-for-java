// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.properties;

import com.azure.spring.core.properties.client.AmqpClientProperties;
import com.azure.spring.core.properties.client.ClientProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This class is only for configuration-processor to find the sub-namespace of logging. The {@link LoggingProperties} is
 * defined as static inner class in {@link ClientProperties} and {@link AmqpClientProperties} extends the
 * {@link ClientProperties}, so when the processor process the {@link AmqpClientProperties} class it will skip the
 * logging field for it's an external class.
 */
public class AmqpClientConfigurationProperties extends AmqpClientProperties {

    @NestedConfigurationProperty
    private final LoggingProperties logging = new LoggingProperties();

    @Override
    public LoggingProperties getLogging() {
        return logging;
    }
}
