// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.aware;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.spring.core.properties.client.HeaderProperties;

import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * Interface to be implemented by classes that wish to be aware of the client properties.
 */
public interface ClientAware {

    Client getClient();

    /**
     * Interface to be implemented by classes that wish to describe sdk client common options.
     */
    interface Client {

        String getApplicationId();

        List<HeaderProperties> getHeaders();

    }

    /**
     * Interface to be implemented by classes that wish to describe a http based client sdk.
     */
    interface HttpClient extends Client {

        Duration getWriteTimeout();

        Duration getResponseTimeout();

        Duration getReadTimeout();

        Duration getConnectTimeout();

        Integer getMaximumConnectionPoolSize();

        Duration getConnectionIdleTimeout();

        HttpLogging getLogging();

    }

    /**
     * Interface to be implemented by classes that wish to describe am amqp based client sdk.
     */
    interface AmqpClient extends Client {

        AmqpTransportType getTransportType();
    }

    /**
     * Interface to be implemented by classes that wish to describe logging options in http-based client sdks.
     *
     * For example, if you want to log the http request or response, you could set the level to
     * {@link HttpLogDetailLevel#BASIC} or some other levels.
     */
    interface HttpLogging {

        /**
         * Gets the level of detail to log on HTTP messages.
         * @return the http log detail level.
         */
        HttpLogDetailLevel getLevel();

        /**
         * Gets the whitelisted headers that should be logged.
         * @return The list of whitelisted headers.
         */
        Set<String> getAllowedHeaderNames();

        /**
         * Gets the whitelisted query parameters.
         * @return The list of whitelisted query parameters.
         */
        Set<String> getAllowedQueryParamNames();

        /**
         * Gets flag to allow pretty printing of message bodies.
         * @return whether to pretty print the message bodies.
         */
        Boolean getPrettyPrintBody();

    }
}
