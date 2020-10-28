// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.config;

import com.azure.spring.messaging.container.ListenerContainerFactory;
import com.azure.spring.messaging.container.MessageListenerContainer;
import com.azure.spring.messaging.endpoint.AzureListenerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Creates the necessary {@link MessageListenerContainer} instances for the registered {@linkplain AzureListenerEndpoint
 * endpoints}. Also manages the lifecycle of the listener containers, in particular within the lifecycle of the
 * application context.
 *
 * <p>Contrary to {@link MessageListenerContainer MessageListenerContainers}
 * created manually, listener containers managed by registry are not beans in the application context and are not
 * candidates for autowiring. Use {@link #getListenerContainers()} if you need to access this registry's listener
 * containers for management purposes.
 *
 * @author Warren Zhu
 * @see AzureListenerEndpoint
 * @see MessageListenerContainer
 * @see ListenerContainerFactory
 */
class AzureListenerEndpointRegistry
    implements DisposableBean, SmartLifecycle, ApplicationContextAware, ApplicationListener<ContextRefreshedEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(AzureListenerEndpointRegistry.class);

    private final Map<String, MessageListenerContainer> listenerContainers = new ConcurrentHashMap<>();

    @Nullable
    private ApplicationContext applicationContext;

    private boolean contextRefreshed;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext() == this.applicationContext) {
            this.contextRefreshed = true;
        }
    }

    /**
     * Return the managed {@link MessageListenerContainer} instance(s).
     */
    public Collection<MessageListenerContainer> getListenerContainers() {
        return Collections.unmodifiableCollection(this.listenerContainers.values());
    }

    /**
     * Create a message listener container for the given {@link AzureListenerEndpoint}.
     * <p>This create the necessary infrastructure to honor that endpoint
     * with regards to its configuration.
     * <p>The {@code startImmediately} flag determines if the container should be
     * started immediately.
     *
     * @param endpoint the endpoint to add
     * @param factory the listener factory to use
     * @param startImmediately start the container immediately if necessary
     * @throws IllegalStateException If another endpoint with the same id has already been registered.
     * @see #getListenerContainers()
     */
    public void registerListenerContainer(AzureListenerEndpoint endpoint,
                                          ListenerContainerFactory<?> factory,
                                          boolean startImmediately) {

        Assert.notNull(endpoint, "Endpoint must not be null");
        Assert.notNull(factory, "Factory must not be null");
        String id = endpoint.getId();
        Assert.hasText(id, "Endpoint id must be set");

        synchronized (this.listenerContainers) {
            if (this.listenerContainers.containsKey(id)) {
                throw new IllegalStateException("Another endpoint is already registered with id '" + id + "'");
            }
            MessageListenerContainer container = createListenerContainer(endpoint, factory);
            this.listenerContainers.put(id, container);
            if (startImmediately) {
                startIfNecessary(container);
            }
        }
    }

    /**
     * Create a message listener container for the given {@link AzureListenerEndpoint}.
     * <p>This create the necessary infrastructure to honor that endpoint with regards to its configuration.
     *
     * @param endpoint the endpoint to add
     * @param factory the listener factory to use
     * @see #registerListenerContainer(AzureListenerEndpoint, ListenerContainerFactory, boolean)
     */
    public void registerListenerContainer(AzureListenerEndpoint endpoint, ListenerContainerFactory<?> factory) {
        registerListenerContainer(endpoint, factory, true);
    }

    /**
     * Create and start a new container using the specified factory.
     *
     * @param endpoint The given endpoint.
     * @param factory The listener factory to use.
     * @return The created {@link MessageListenerContainer}.
     * @throws BeanInitializationException If fail to initialize the message listener container.
     * @throws IllegalStateException If phase mismatch is encountered between container factory and conta
     */
    protected MessageListenerContainer createListenerContainer(AzureListenerEndpoint endpoint,
                                                               ListenerContainerFactory<?> factory) {

        MessageListenerContainer listenerContainer = factory.createListenerContainer(endpoint);

        if (listenerContainer instanceof InitializingBean) {
            try {
                ((InitializingBean) listenerContainer).afterPropertiesSet();
            } catch (Exception ex) {
                throw new BeanInitializationException("Failed to initialize message listener container", ex);
            }
        }

        int containerPhase = listenerContainer.getPhase();
        if (containerPhase < Integer.MAX_VALUE) {  // a custom phase value
            if (this.getPhase() < Integer.MAX_VALUE && this.getPhase() != containerPhase) {
                throw new IllegalStateException("Encountered phase mismatch between container factory definitions: "
                    + this.getPhase() + " vs " + containerPhase);
            }
        }

        return listenerContainer;
    }

    @Override
    public void start() {
        for (MessageListenerContainer listenerContainer : getListenerContainers()) {
            startIfNecessary(listenerContainer);
        }
    }

    @Override
    public void stop() {
        for (MessageListenerContainer listenerContainer : getListenerContainers()) {
            listenerContainer.stop();
        }
    }

    @Override
    public boolean isAutoStartup() {
        return false;
    }

    @Override
    public void stop(Runnable callback) {
        Collection<MessageListenerContainer> listenerContainers = getListenerContainers();
        AggregatingCallback aggregatingCallback = new AggregatingCallback(listenerContainers.size(), callback);
        for (MessageListenerContainer listenerContainer : listenerContainers) {
            listenerContainer.stop(aggregatingCallback);
        }
    }

    @Override
    public boolean isRunning() {
        for (MessageListenerContainer listenerContainer : getListenerContainers()) {
            if (listenerContainer.isRunning()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Start the specified {@link MessageListenerContainer} if it should be started on startup or when start is called
     * explicitly after startup.
     *
     * @see MessageListenerContainer#isAutoStartup()
     */
    private void startIfNecessary(MessageListenerContainer listenerContainer) {
        if (this.contextRefreshed || listenerContainer.isAutoStartup()) {
            listenerContainer.start();
        }
    }

    @Override
    public void destroy() {
        for (MessageListenerContainer listenerContainer : getListenerContainers()) {
            if (listenerContainer instanceof DisposableBean) {
                try {
                    ((DisposableBean) listenerContainer).destroy();
                } catch (Throwable ex) {
                    LOG.warn("Failed to destroy message listener container", ex);
                }
            }
        }
    }

    /**
     * Return the {@link MessageListenerContainer} with the specified id or {@code null} if no such container exists.
     *
     * @param id the id of the container
     * @return the container or {@code null} if no container with that id exists
     * @see AzureListenerEndpoint#getId()
     * @see #getListenerContainerIds()
     */
    @Nullable
    public MessageListenerContainer getListenerContainer(@NonNull String id) {
        return this.listenerContainers.get(id);
    }

    /**
     * Return the ids of the managed {@link MessageListenerContainer} instance(s).
     *
     * @see #getListenerContainer(String)
     */
    public Set<String> getListenerContainerIds() {
        return Collections.unmodifiableSet(this.listenerContainers.keySet());
    }

    @Override
    public int getPhase() {
        return 0;
    }

    private static class AggregatingCallback implements Runnable {

        private final AtomicInteger count;

        private final Runnable finishCallback;

        AggregatingCallback(int count, Runnable finishCallback) {
            this.count = new AtomicInteger(count);
            this.finishCallback = finishCallback;
        }

        @Override
        public void run() {
            if (this.count.decrementAndGet() == 0) {
                this.finishCallback.run();
            }
        }
    }

}
