// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.provider;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.spring.cloud.core.properties.client.HeaderProperties;

import java.time.Duration;
import java.util.List;

/**
 * Interface to be implemented by classes that wish to provide the client options.
 */
public interface ClientOptionsProvider {

    /**
     * Get the client configuration.
     * @return the client configuration.
     */
    ClientOptions getClient();

    /**
     * Interface to be implemented by classes that wish to describe sdk client common options.
     */
    interface ClientOptions {

        /**
         * Get the application id
         * @return the application id.
         */
        String getApplicationId();

    }

    /**
     * Interface to be implemented by classes that wish to describe a http based client sdk.
     */
    interface HttpClientOptions extends ClientOptions, HttpLoggingOptionsProvider {

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

        /**
         * Get header properties list for client header.
         * @return header properties list.
         */
        List<HeaderProperties> getHeaders();

    }

    /**
     * Interface to be implemented by classes that wish to describe an amqp based client sdk.
     */
    interface AmqpClientOptions extends ClientOptions {

        /**
         * Get the AMQP transport type.
         * @return the AMQP transport type.
         */
        AmqpTransportType getTransportType();
    }

}
