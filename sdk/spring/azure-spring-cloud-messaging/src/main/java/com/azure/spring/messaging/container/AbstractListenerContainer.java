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

    protected abstract void doStart();

    protected abstract void doStop();

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

    @Override
    public void destroy() {
        synchronized (this.lifecycleMonitor) {
            stop();
            this.active = false;
            doDestroy();
        }
    }

    public String getBeanName() {
        return beanName;
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public Object getLifecycleMonitor() {
        return lifecycleMonitor;
    }

    public String getDestination() {
        return destination;
    }

    @Override
    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getGroup() {
        return group;
    }

    @Override
    public void setGroup(String group) {
        this.group = group;
    }

    @Override
    public int getPhase() {
        return phase;
    }

    @Override
    public AzureMessageHandler getMessageHandler() {
        return messageHandler;
    }

    @Override
    public void setMessageHandler(AzureMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    public boolean isAutoStartup() {
        return autoStartup;
    }
}
