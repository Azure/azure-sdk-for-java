// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jms;

import com.azure.servicebus.jms.ServiceBusJmsConnectionFactory;

/**
 * A provider for specifying the desired {@link ServiceBusJmsConnectionFactory} class to be used.
 * <p>
 * Implement this interface and define it as a bean to inject a custom subclass of ServiceBusJmsConnectionFactory.
 * This allows you to use a custom connection factory implementation with additional functionality beyond the
 * standard Service Bus JMS connection factory.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * @Configuration
 * public class CustomJmsConfiguration {
 *     @Bean
 *     public ServiceBusJmsConnectionFactoryClassProvider connectionFactoryClassProvider() {
 *         return () -> CustomServiceBusJmsConnectionFactory.class;
 *     }
 * }
 *
 * public class CustomServiceBusJmsConnectionFactory extends ServiceBusJmsConnectionFactory {
 *     public CustomServiceBusJmsConnectionFactory(String connectionString, ServiceBusJmsConnectionFactorySettings settings) {
 *         super(connectionString, settings);
 *         // Add custom initialization
 *     }
 *
 *     public CustomServiceBusJmsConnectionFactory(TokenCredential tokenCredential, String host, ServiceBusJmsConnectionFactorySettings settings) {
 *         super(tokenCredential, host, settings);
 *         // Add custom initialization
 *     }
 *
 *     // Add custom methods or override existing ones
 * }
 * }</pre>
 * </p>
 * <p>
 * <strong>Requirements:</strong>
 * <ul>
 *   <li>The custom class must extend {@link ServiceBusJmsConnectionFactory}</li>
 *   <li>The custom class must have a constructor accepting {@code (String, ServiceBusJmsConnectionFactorySettings)}
 *       for connection string-based authentication</li>
 *   <li>The custom class must have a constructor accepting {@code (TokenCredential, String, ServiceBusJmsConnectionFactorySettings)}
 *       for passwordless authentication</li>
 * </ul>
 * </p>
 *
 * @since 6.1.0
 */
@FunctionalInterface
public interface ServiceBusJmsConnectionFactoryClassProvider {

    /**
     * Get the class of the ServiceBusJmsConnectionFactory to be instantiated.
     * The class must extend {@link ServiceBusJmsConnectionFactory} and have the required constructors.
     *
     * @return The class to be used for creating the ServiceBusJmsConnectionFactory instance.
     */
    Class<? extends ServiceBusJmsConnectionFactory> getConnectionFactoryClass();
}
