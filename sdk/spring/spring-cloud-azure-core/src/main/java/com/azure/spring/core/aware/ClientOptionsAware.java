// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.aware;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.spring.core.properties.client.HeaderProperties;

import java.time.Duration;
import java.util.List;

/**
 * Interface to be implemented by classes that wish to be aware of the client properties.
 */
public interface ClientOptionsAware {

    /**
     * Get the client configuration.
     * @return the client configuration.
     */
    Client getClient();

    /**
     * Interface to be implemented by classes that wish to describe sdk client common options.
     */
    interface Client {

        /**
         * Get the application id
         * @return the application id.
         */
        String getApplicationId();

        /**
         * Get header properties list for client header.
         * @return header properties list.
         */
        List<HeaderProperties> getHeaders();

    }

    /**
     * Interface to be implemented by classes that wish to describe a http based client sdk.
     */
    interface HttpClient extends Client, HttpLoggingOptionsAware {

        /**
         * Get the http client write timeout.
         * @return the http client write timeout.
         */
        Duration getWriteTimeout();

        /**
         * Get the http client response timeout.
         * @return the http client response timeout.
         */
        Duration getResponseTimeout();

        /**
         * Get the http client read timeout.
         * @return the http client read timeout.
         */
        Duration getReadTimeout();

        /**
         * Get the http client connect timeout.
         * @return the http client connect timeout.
         */
        Duration getConnectTimeout();

        /**
         * Get the maximum connection pool size for http client.
         * @return the maximum connection pool size
         */
        Integer getMaximumConnectionPoolSize();

        /**
         * Get the connection idle timeout for http client.
         * @return the connection idle timeout
         */
        Duration getConnectionIdleTimeout();

    }

    /**
     * Interface to be implemented by classes that wish to describe am amqp based client sdk.
     */
    interface AmqpClient extends Client {

        /**
         * Get the AMQP transport type.
         * @return the AMQP transport type.
         */
        AmqpTransportType getTransportType();
    }

}
