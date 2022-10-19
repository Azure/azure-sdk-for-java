// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.implementation.config;

import com.azure.spring.messaging.implementation.listener.MessageListenerContainerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.lang.Nullable;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper bean for registering {@link AzureListenerEndpoint} with a {@link AzureListenerEndpointRegistry}.
 *
 */
public class AzureListenerEndpointRegistrar implements BeanFactoryAware, InitializingBean {

    private final List<AzureListenerEndpointDescriptor> endpointDescriptors = new ArrayList<>();
    @Nullable
    private AzureListenerEndpointRegistry endpointRegistry;
    @Nullable
    private MessageHandlerMethodFactory messageHandlerMethodFactory;
    @Nullable
    private MessageListenerContainerFactory<?> containerFactory;
    @Nullable
    private String containerFactoryBeanName;
    @Nullable
    private BeanFactory beanFactory;
    private boolean startImmediately;

    private Object mutex = this.endpointDescriptors;

    /**
     * A {@link BeanFactory} only needs to be available in conjunction with {@link #setContainerFactoryBeanName}.
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        if (beanFactory instanceof ConfigurableBeanFactory) {
            this.mutex = ((ConfigurableBeanFactory) beanFactory).getSingletonMutex();
        }
    }

    @Override
    public void afterPropertiesSet() {
        registerAllEndpoints();
    }

    /**
     * Register all {@link AzureListenerEndpoint}s under the registrar to create the associated containers.
     */
    protected void registerAllEndpoints() {
        synchronized (this.mutex) {
            for (AzureListenerEndpointDescriptor descriptor : this.endpointDescriptors) {
                Assert.state(this.endpointRegistry != null, "No AzureListenerEndpointRegistry set");
                this.endpointRegistry
                    .registerListenerContainer(descriptor.endpoint, resolveContainerFactory(descriptor));
            }
            this.startImmediately = true;  // trigger immediate startup
        }
    }

    private MessageListenerContainerFactory<?> resolveContainerFactory(AzureListenerEndpointDescriptor descriptor) {
        if (descriptor.containerFactory != null) {
            return descriptor.containerFactory;
        } else if (this.containerFactory != null) {
            return this.containerFactory;
        } else if (this.containerFactoryBeanName != null) {
            Assert.state(this.beanFactory != null, "BeanFactory must be set to obtain container factory by bean name");
            // Consider changing this if live change of the factory is required...
            this.containerFactory = this.beanFactory.getBean(this.containerFactoryBeanName, MessageListenerContainerFactory.class);
            return this.containerFactory;
        } else {
            throw new IllegalStateException(
                "Could not resolve the " + MessageListenerContainerFactory.class.getSimpleName() + " to use for ["
                    + descriptor.endpoint + "] no factory was given and no default is set.");
        }
    }

    /**
     * Register a new {@link AzureListenerEndpoint} alongside the {@link MessageListenerContainerFactory} to use to create the
     * underlying container.
     * <p>The {@code factory} may be {@code null} if the default factory has to be
     * used for that endpoint.
     *
     * @param endpoint the {@link AzureListenerEndpoint} instance to register.
     * @param factory the {@link MessageListenerContainerFactory} to use.
     */
    public void registerEndpoint(AzureListenerEndpoint endpoint, @Nullable MessageListenerContainerFactory<?> factory) {
        Assert.notNull(endpoint, "Endpoint must not be null");
        Assert.hasText(endpoint.getId(), "Endpoint id must be set");

        // Factory may be null, we defer the resolution right before actually creating the container
        AzureListenerEndpointDescriptor descriptor = new AzureListenerEndpointDescriptor(endpoint, factory);

        synchronized (this.mutex) {
            if (this.startImmediately) {  // register and start immediately
                Assert.state(this.endpointRegistry != null, "No AzureListenerEndpointRegistry set");
                this.endpointRegistry
                    .registerListenerContainer(descriptor.endpoint, resolveContainerFactory(descriptor), true);
            } else {
                this.endpointDescriptors.add(descriptor);
            }
        }
    }

    /**
     * Register a new {@link AzureListenerEndpoint} using the default {@link MessageListenerContainerFactory} to create the
     * underlying container.
     * @param endpoint the {@link AzureListenerEndpoint} instance to register.
     * @see #setContainerFactory(MessageListenerContainerFactory)
     * @see #registerEndpoint(AzureListenerEndpoint, MessageListenerContainerFactory)
     */
    public void registerEndpoint(AzureListenerEndpoint endpoint) {
        registerEndpoint(endpoint, null);
    }

    /**
     * Set the bean name of the container factory.
     * @param containerFactoryBeanName the bean name of the container factory.
     */
    public void setContainerFactoryBeanName(String containerFactoryBeanName) {
        this.containerFactoryBeanName = containerFactoryBeanName;
    }

    /**
     * Get the {@link AzureListenerEndpointRegistry}.
     * @return the {@link AzureListenerEndpointRegistry}.
     */
    public AzureListenerEndpointRegistry getEndpointRegistry() {
        return this.endpointRegistry;
    }

    /**
     * Set the {@link AzureListenerEndpointRegistry}.
     * @param endpointRegistry the {@link AzureListenerEndpointRegistry}.
     */
    public void setEndpointRegistry(AzureListenerEndpointRegistry endpointRegistry) {
        this.endpointRegistry = endpointRegistry;
    }

    /**
     * Get the {@link MessageHandlerMethodFactory}.
     * @return the {@link MessageHandlerMethodFactory}.
     */
    public MessageHandlerMethodFactory getMessageHandlerMethodFactory() {
        return this.messageHandlerMethodFactory;
    }

    /**
     * Set the {@link MessageHandlerMethodFactory}.
     * @param messageHandlerMethodFactory the {@link MessageHandlerMethodFactory}.
     */
    public void setMessageHandlerMethodFactory(MessageHandlerMethodFactory messageHandlerMethodFactory) {
        this.messageHandlerMethodFactory = messageHandlerMethodFactory;
    }

    /**
     * Set the {@link MessageListenerContainerFactory}.
     * @param containerFactory the {@link MessageListenerContainerFactory}.
     */
    public void setContainerFactory(MessageListenerContainerFactory<?> containerFactory) {
        this.containerFactory = containerFactory;
    }

    private static class AzureListenerEndpointDescriptor {

        private final AzureListenerEndpoint endpoint;

        @Nullable
        private final MessageListenerContainerFactory<?> containerFactory;

        AzureListenerEndpointDescriptor(AzureListenerEndpoint endpoint,
                                        @Nullable MessageListenerContainerFactory<?> containerFactory) {

            this.endpoint = endpoint;
            this.containerFactory = containerFactory;
        }

    }

}
