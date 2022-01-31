// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.client.traits;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;

/**
 * The interface for client builders that support an AMQP protocol.
 * @param <TBuilder> the type of client builder.
 */
public interface AmqpTrait<TBuilder extends AmqpTrait<TBuilder>> {
    /**
     * Sets the retry policy. If not specified, the default retry options are used.
     *
     * @param retryOptions The retry policy to use.
     *
     * @return The updated {@code TBuilder} object.
     */
    TBuilder retryOptions(AmqpRetryOptions retryOptions);

    /**
     * Sets the transport type by which all the communication with Azure service occurs. Default value is {@link
     * AmqpTransportType#AMQP}.
     *
     * @param transport The transport type to use.
     *
     * @return The updated {@code TBuilder} object.
     */
    TBuilder transportType(AmqpTransportType transport);

    /**
     * Sets the proxy configuration to use. When a proxy is configured, {@link
     * AmqpTransportType#AMQP_WEB_SOCKETS} must be used for the transport type.
     *
     * @param proxyOptions The proxy configuration to use.
     *
     * @return The updated {@code TBuilder} object.
     */
    TBuilder proxyOptions(ProxyOptions proxyOptions);
}
