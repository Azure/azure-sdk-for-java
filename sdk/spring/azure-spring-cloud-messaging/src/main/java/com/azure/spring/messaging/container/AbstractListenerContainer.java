// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.container;

import com.azure.spring.messaging.listener.AzureMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;

abstract class AbstractListenerContainer implements BeanNameAware, DisposableBean, MessageListenerContainer {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractListenerContainer.class);
    private final Object lifecycleMonitor = new Object();
    private String destination;
    private String group;
    private AzureMessageHandler messageHandler;
    private boolean autoStartup = true;
    private int phase = 0;

    //Settings that are changed at runtime
    private boolean active;
    private boolean running;
    private String beanName;

    /**
     * Do start
     */
    protected abstract void doStart();

    /**
     * Do stop
     */
    protected abstract void doStop();

    /**
     * Do destroy
     */
    protected void doDestroy() {
    }

    @Override
    public void stop(Runnable callback) {
        this.stop();
        callback.run();
    }

    @Override
    public void start() {
        LOG.debug("Starting container with name {}", getBeanName());
        synchronized (this.getLifecycleMonitor()) {
            this.running = true;
            this.getLifecycleMonitor().notifyAll();
        }
        doStart();
    }

    @Override
    public void stop() {
        LOG.debug("Stopping container with name {}", getBeanName());
        synchronized (this.getLifecycleMonitor()) {
            this.running = false;
            this.getLifecycleMonitor().notifyAll();
        }
        doStop();
    }

    @Override
    public boolean isRunning() {
        synchronized (this.getLifecycleMonitor()) {
            return this.running;
        }
    }

    /**
     * Destroy
     */
    @Override
    public void destroy() {
        synchronized (this.lifecycleMonitor) {
            stop();
            this.active = false;
            doDestroy();
        }
    }

    /**
     *
     * @return The bean name.
     */
    public String getBeanName() {
        return beanName;
    }

    /**
     *
     * @param beanName The bean name.
     */
    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    /**
     *
     * @return The lifecycleMonitor.
     */
    public Object getLifecycleMonitor() {
        return lifecycleMonitor;
    }

    /**
     *
     * @return The destination.
     */
    public String getDestination() {
        return destination;
    }

    /**
     *
     * @param destination The destination.
     */
    @Override
    public void setDestination(String destination) {
        this.destination = destination;
    }

    /**
     *
     * @return The group.
     */
    public String getGroup() {
        return group;
    }

    /**
     *
     * @param group The group.
     */
    @Override
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     *
     * @return The phase.
     */
    @Override
    public int getPhase() {
        return phase;
    }

    /**
     *
     * @return The AzureMessageHandler.
     */
    @Override
    public AzureMessageHandler getMessageHandler() {
        return messageHandler;
    }

    /**
     *
     * @param messageHandler The message handler
     */
    @Override
    public void setMessageHandler(AzureMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    /**
     *
     * @return Is auto startup.
     */
    @Override
    public boolean isAutoStartup() {
        return autoStartup;
    }
}
