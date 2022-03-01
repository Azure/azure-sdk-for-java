// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.implementation.annotation;

import com.azure.spring.messaging.implementation.config.AzureListenerEndpointRegistrar;
import com.azure.spring.messaging.implementation.config.AzureListenerEndpointRegistry;
import com.azure.spring.messaging.implementation.listener.MessageListenerContainerFactory;
import com.azure.spring.messaging.implementation.config.AzureListenerEndpoint;

/**
 * Optional interface to be implemented by a Spring managed bean willing
 * to customize how Azure listener endpoints are configured. Typically, it's
 * used to define the default {@link MessageListenerContainerFactory
 * ListenerContainerFactory} to use or for registering Azure endpoints
 * in a <em>programmatic</em> fashion as opposed to the <em>declarative</em>
 * approach of using the Azure Messaging Listener annotation.
 *
 * <p>See @{@link EnableAzureMessaging} for detailed usage examples.
 *
 * @see EnableAzureMessaging
 * @see AzureListenerEndpointRegistrar
 */
@FunctionalInterface
public interface AzureListenerConfigurer {

    /**
     * Callback allowing a {@link AzureListenerEndpointRegistry
     * AzureListenerEndpointRegistry} and specific {@link AzureListenerEndpoint
     * AzureListenerEndpoint} instances to be registered against the given
     * {@link AzureListenerEndpointRegistrar}. The default
     * {@link MessageListenerContainerFactory ListenerContainerFactory}
     * can also be customized.
     *
     * @param registrar the registrar to be configured
     */
    void configureAzureListeners(AzureListenerEndpointRegistrar registrar);

}
