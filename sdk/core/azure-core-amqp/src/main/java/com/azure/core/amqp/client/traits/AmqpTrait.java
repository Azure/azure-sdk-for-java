// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.client.traits;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.util.ClientOptions;

/**
 * An {@link com.azure.core.client.traits Azure SDK for Java trait} providing a consistent interface for configuration
 * of AMQP-specific settings.
 *
 * @param <T> The concrete type that implements the trait. This is required so that fluent operations can continue
 *           to return the concrete type, rather than the trait type.
 * @see com.azure.core.amqp.client.traits
 * @see com.azure.core.client.traits
 * @see AmqpRetryOptions
 * @see AmqpTransportType
 * @see ProxyOptions
 */
public interface AmqpTrait<T extends AmqpTrait<T>> {
    /**
     * Sets the retry policy. If not specified, the default retry options are used.
     *
     * @param retryOptions The retry options to use.
     * @return Returns the same concrete type with the appropriate properties updated, to allow for fluent chaining of
     *      operations.
     */
    T retryOptions(AmqpRetryOptions retryOptions);

    /**
     * Sets the transport type by which all the communication with Azure service occurs. The default value is {@link
     * AmqpTransportType#AMQP}.
     *
     * @param transport The transport type to use.
     * @return Returns the same concrete type with the appropriate properties updated, to allow for fluent chaining of
     *      operations.
     */
    T transportType(AmqpTransportType transport);

    /**
     * Sets the proxy configuration to use. When a proxy is configured, {@link
     * AmqpTransportType#AMQP_WEB_SOCKETS} must be used for the transport type.
     *
     * @param proxyOptions The proxy configuration to use.
     * @return Returns the same concrete type with the appropriate properties updated, to allow for fluent chaining of
     *      operations.
     */
    T proxyOptions(ProxyOptions proxyOptions);

    /**
     * Allows for setting common properties such as application ID, headers, etc.
     *
     * @param clientOptions A configured instance of {@link ClientOptions}.
     * @return Returns the same concrete type with the appropriate properties updated, to allow for fluent chaining of
     *      operations.
     */
    T clientOptions(ClientOptions clientOptions);
}
