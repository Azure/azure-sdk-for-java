// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.messaging.config;

import com.microsoft.azure.spring.messaging.annotation.AzureMessageListener;
import com.microsoft.azure.spring.messaging.annotation.EnableAzureMessaging;
import com.microsoft.azure.spring.messaging.container.ListenerContainerFactory;
import com.microsoft.azure.spring.messaging.endpoint.AzureListenerEndpoint;

/**
 * Optional interface to be implemented by a Spring managed bean willing
 * to customize how Azure listener endpoints are configured. Typically
 * used to define the default {@link ListenerContainerFactory
 * ListenerContainerFactory} to use or for registering Azure endpoints
 * in a <em>programmatic</em> fashion as opposed to the <em>declarative</em>
 * approach of using the @{@link AzureMessageListener} annotation.
 *
 * <p>See @{@link EnableAzureMessaging} for detailed usage examples.
 *
 * @author Warren Zhu
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
     * {@link ListenerContainerFactory ListenerContainerFactory}
     * can also be customized.
     *
     * @param registrar the registrar to be configured
     */
    void configureAzureListeners(AzureListenerEndpointRegistrar registrar);

}
