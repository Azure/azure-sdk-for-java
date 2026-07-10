// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.servicebus.factory;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.core.implementation.credential.descriptor.AuthenticationDescriptor;
import com.azure.spring.cloud.core.implementation.credential.descriptor.NamedKeyAuthenticationDescriptor;
import com.azure.spring.cloud.core.implementation.credential.descriptor.SasAuthenticationDescriptor;
import com.azure.spring.cloud.core.implementation.credential.descriptor.TokenAuthenticationDescriptor;
import com.azure.spring.cloud.core.implementation.factory.AbstractAzureAmqpClientBuilderFactory;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.service.implementation.servicebus.properties.ServiceBusClientCommonProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Abstract Service Bus client builder factory, it builds the {@link ServiceBusClientBuilder} sub client.
 */
abstract class AbstractServiceBusSubClientBuilderFactory<T, P extends ServiceBusClientCommonProperties> extends AbstractAzureAmqpClientBuilderFactory<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractServiceBusSubClientBuilderFactory.class);

    protected final P properties;

    private ServiceBusClientBuilder serviceBusClientBuilder;
    private final boolean shareServiceBusClientBuilder;
    private ServiceBusClientBuilderFactory serviceBusClientBuilderFactory;

    /**
     * Create a {@link AbstractServiceBusSubClientBuilderFactory} instance with the properties and the collection of
     * @{link ServiceBusClientBuilder} customizers.
     * @param properties the properties describing the service bus sub client, which could be a sender, a receiver or
     *                   a processor.
     * @param serviceClientBuilderCustomizers the collection of customizers for the service bus client builder.
     */
    AbstractServiceBusSubClientBuilderFactory(P properties,
                                              List<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder>> serviceClientBuilderCustomizers) {
        this(null, properties, serviceClientBuilderCustomizers);
    }


    AbstractServiceBusSubClientBuilderFactory(ServiceBusClientBuilder serviceBusClientBuilder,
                                              P properties) {
        this(serviceBusClientBuilder, properties, null);
        if (serviceBusClientBuilder == null) {
            LOGGER.debug("The shared ServiceBusClientBuilder instance is null, the {} instance has used a non-shared ServiceBusClientBuilder "
                + "and ignored the customizers.", this.getClass().getSimpleName());
        }
    }

    protected AbstractServiceBusSubClientBuilderFactory(ServiceBusClientBuilder serviceBusClientBuilder,
                                              P properties,
                                              List<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder>> serviceBusClientBuilderCustomizers) {
        this.properties = properties;
        if (serviceBusClientBuilder != null) {
            this.serviceBusClientBuilder = serviceBusClientBuilder;
            this.shareServiceBusClientBuilder = true;
            this.serviceBusClientBuilderFactory = null;
        } else {
            this.serviceBusClientBuilderFactory = new ServiceBusClientBuilderFactory(properties);
            if (serviceBusClientBuilderCustomizers != null) {
                serviceBusClientBuilderCustomizers.forEach(this.serviceBusClientBuilderFactory::addBuilderCustomizer);
            }
            // Don't build yet - defer until first use when ApplicationContext is available
            this.serviceBusClientBuilder = null;
            this.shareServiceBusClientBuilder = false;
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        super.setApplicationContext(applicationContext);
        // Propagate ApplicationContext to the nested ServiceBusClientBuilderFactory
        if (this.serviceBusClientBuilderFactory != null) {
            this.serviceBusClientBuilderFactory.setApplicationContext(applicationContext);
        }
    }

    @Override
    protected void configureCredential(T builder) {
        // skip to avoid overriding the parent builder's credentials.
    }

    @Override
    protected void configureConnectionString(T builder) {
        // skip to avoid overriding the parent builder's credentials.
    }

    @Override
    protected void configureDefaultCredential(T builder) {
        // skip to avoid overriding the parent builder's credentials.
    }

    protected boolean isShareServiceBusClientBuilder() {
        return shareServiceBusClientBuilder;
    }

    private boolean isInheritConfiguration() {
        // Not set defaults to true, preserving the existing behavior.
        return this.properties.getInheritConfiguration() == null || this.properties.getInheritConfiguration();
    }

    // In the non-shared path the sub-client builder applies its property-derived configuration to the underlying
    // ServiceBusClientBuilder when inherit-configuration is true (the default). Set it to false to preserve configuration
    // already set on the shared builder (e.g. ClientOptions carrying TracingOptions from a customizer). See #49742.
    private boolean shouldConfigureServiceBusClientBuilder() {
        return !isShareServiceBusClientBuilder() && isInheritConfiguration();
    }

    @Override
    protected BiConsumer<T, ProxyOptions> consumeProxyOptions() {
        return (builder, proxy) -> {
            if (shouldConfigureServiceBusClientBuilder()) {
                getServiceBusClientBuilder().proxyOptions(proxy);
            }
        };
    }

    @Override
    protected BiConsumer<T, AmqpTransportType> consumeAmqpTransportType() {
        return (builder, t) -> {
            if (shouldConfigureServiceBusClientBuilder()) {
                getServiceBusClientBuilder().transportType(t);
            }
        };
    }

    @Override
    protected BiConsumer<T, AmqpRetryOptions> consumeAmqpRetryOptions() {
        return (builder, retry) -> {
            if (shouldConfigureServiceBusClientBuilder()) {
                getServiceBusClientBuilder().retryOptions(retry);
            }
        };
    }

    @Override
    protected BiConsumer<T, ClientOptions> consumeClientOptions() {
        return (builder, client) -> {
            if (shouldConfigureServiceBusClientBuilder()) {
                getServiceBusClientBuilder().clientOptions(client);
            }
        };
    }

    @Override
    protected AzureProperties getAzureProperties() {
        return this.properties;
    }

    @Override
    protected List<AuthenticationDescriptor<?>> getAuthenticationDescriptors(T builder) {
        return Arrays.asList(
            new NamedKeyAuthenticationDescriptor(credential -> {
                if (shouldConfigureServiceBusClientBuilder()) {
                    getServiceBusClientBuilder().credential(credential);
                }
            }),
            new SasAuthenticationDescriptor(credential -> {
                if (shouldConfigureServiceBusClientBuilder()) {
                    getServiceBusClientBuilder().credential(credential);
                }
            }),
            new TokenAuthenticationDescriptor(this.tokenCredentialResolver, credential -> {
                if (shouldConfigureServiceBusClientBuilder()) {
                    getServiceBusClientBuilder().credential(credential);
                }
            })
        );
    }

    @Override
    protected BiConsumer<T, Configuration> consumeConfiguration() {
        return (builder, configuration) -> {
            if (shouldConfigureServiceBusClientBuilder()) {
                getServiceBusClientBuilder().configuration(configuration);
            }
        };
    }

    @Override
    protected BiConsumer<T, TokenCredential> consumeDefaultTokenCredential() {
        return (builder, credential) -> {
            if (shouldConfigureServiceBusClientBuilder()) {
                getServiceBusClientBuilder().credential(credential);
            }
        };
    }

    @Override
    protected BiConsumer<T, String> consumeConnectionString() {
        return (builder, connectionString) -> {
            if (shouldConfigureServiceBusClientBuilder()) {
                getServiceBusClientBuilder().connectionString(connectionString);
            }
        };
    }

    @Override
    protected void configureService(T builder) {
        if (shouldConfigureServiceBusClientBuilder()) {
            getServiceBusClientBuilder().fullyQualifiedNamespace(properties.getFullyQualifiedNamespace());
        }
    }

    protected ServiceBusClientBuilder getServiceBusClientBuilder() {
        // Lazy initialization: build only when first accessed, ensuring ApplicationContext is available
        if (serviceBusClientBuilder == null && serviceBusClientBuilderFactory != null) {
            serviceBusClientBuilder = serviceBusClientBuilderFactory.build();
        }
        return serviceBusClientBuilder;
    }
}
