// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;

/**
 * The base implementation for the {@link MessageListenerContainer}.
 */
public abstract class AbstractMessageListenerContainer implements MessageListenerContainer, BeanNameAware {

    /**
     * Creates an instance of {@link AbstractMessageListenerContainer}.
     */
    public AbstractMessageListenerContainer() {
    }

    private static final Logger LOG = LoggerFactory.getLogger(AbstractMessageListenerContainer.class);

    /**
     * Life cycle monitor.
     */
    protected final Object lifecycleMonitor = new Object();

    // Settings that are changed at runtime
    private boolean active;

    private volatile boolean running = false;

    private String beanName;

    /**
     * Prepare the underlying SDK processor client and start it for subscribing.
     */
    protected abstract void doStart();

    /**
     * Stop the underlying SDK processor client.
     */
    protected abstract void doStop();

    @Override
    public void stop(Runnable callback) {
        this.stop();
        callback.run();
    }

    @Override
    public void start() {
        LOG.debug("Starting container with name {}", getBeanName());
        synchronized (this.lifecycleMonitor) {
            this.running = true;
            this.lifecycleMonitor.notifyAll();
        }
        doStart();
        this.active = true;
    }

    @Override
    public void stop() {
        LOG.debug("Stopping container with name {}", getBeanName());

        synchronized (this.lifecycleMonitor) {
            this.running = false;
            this.lifecycleMonitor.notifyAll();
        }
        doStop();
    }

    @Override
    public boolean isRunning() {
        synchronized (this.lifecycleMonitor) {
            return this.running;
        }
    }

    /**
     * Return the bean name.
     * @return the bean name.
     */
    public String getBeanName() {
        return beanName;
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    @Override
    public int getPhase() {
        return 0;
    }

    /**
     * Whether this listener container has been started, no matter whether it is stopped later.
     * @return whether this container is active.
     */
    public boolean isActive() {
        return active;
    }
}
