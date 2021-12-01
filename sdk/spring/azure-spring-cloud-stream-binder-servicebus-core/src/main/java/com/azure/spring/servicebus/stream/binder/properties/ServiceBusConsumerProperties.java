// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.stream.binder.properties;

import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.spring.integration.core.api.CheckpointMode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.context.properties.DeprecatedConfigurationProperty;

/**
 * @author Warren Zhu
 * @author Eduardo Sciullo
 */
public class ServiceBusConsumerProperties {
    private final Log logger = LogFactory.getLog(ServiceBusConsumerProperties.class);

    private int prefetchCount = 1;
    @Deprecated
    private int concurrency = 1;
    private boolean sessionsEnabled = false;
    private boolean requeueRejected = false;
    //TODO: after concurrency deleted, this can be unboxed
    private Integer maxConcurrentCalls;
    private Integer maxConcurrentSessions;
    private boolean enableAutoComplete = false;
    private ServiceBusReceiveMode serviceBusReceiveMode = ServiceBusReceiveMode.PEEK_LOCK;

    private CheckpointMode checkpointMode = CheckpointMode.RECORD;

    /**
     *
     * @return The check point mode.
     */
    public CheckpointMode getCheckpointMode() {
        return checkpointMode;
    }

    /**
     *
     * @param checkpointMode The check point mode.
     */
    public void setCheckpointMode(CheckpointMode checkpointMode) {
        this.checkpointMode = checkpointMode;
    }

    /**
     *
     * @return True if enable auto complete.
     */
    public boolean isEnableAutoComplete() {
        return enableAutoComplete;
    }

    /**
     *
     * @param enableAutoComplete Whether auto complete is enabled.
     */
    public void setEnableAutoComplete(boolean enableAutoComplete) {
        this.enableAutoComplete = enableAutoComplete;
    }

    /**
     * Controls the max concurrent calls of service bus message handler and
     * the size of fixed thread pool that handles user's business logic
     *
     * @return int, default : 1
     */
    public Integer getMaxConcurrentCalls() {
        return maxConcurrentCalls;
    }

    /**
     *
     * @param maxConcurrentCalls The max concurrent calls.
     */
    public void setMaxConcurrentCalls(int maxConcurrentCalls) {
        this.maxConcurrentCalls = maxConcurrentCalls;
    }

    /**
     * Maximum number of concurrent sessions to process at any given time
     *
     * @return int, default : 1
     */
    public Integer getMaxConcurrentSessions() {
        return maxConcurrentSessions;
    }

    /**
     *
     * @param maxConcurrentSessions The max concurrent sessions.
     */
    public void setMaxConcurrentSessions(int maxConcurrentSessions) {
        this.maxConcurrentSessions = maxConcurrentSessions;
    }

    /**
     * Prefetch count of underlying service bus client.
     *
     *
     * @return int, default : 1
     */
    public int getPrefetchCount() {
        return prefetchCount;
    }

    /**
     *
     * @param prefetchCount The prefetch count.
     */
    public void setPrefetchCount(int prefetchCount) {
        this.prefetchCount = prefetchCount;
    }

    /**
     * Controls the max concurrent calls of service bus message handler and
     * the size of fixed thread pool that handles user's business logic
     *
     * @return int, default : 1
     * @deprecated Use maxConcurrentSessions and maxConcurrentCalls.
     */
    @Deprecated
    @DeprecatedConfigurationProperty(reason = "Deprecate the concurrency and use maxConcurrentSessions and maxConcurrentCalls instead")
    public int getConcurrency() {
        return concurrency;
    }

    /**
     *
     * @param concurrency The concurrency.
     * @deprecated Use maxConcurrentSessions and maxConcurrentCalls.
     */
    @Deprecated
    public void setConcurrency(int concurrency) {
        logger.warn("Set attribute concurrency, which has been deprecated");
        this.concurrency = concurrency;
    }

    /**
     * Controls if is session aware
     *
     * @return boolean, default : false
     */
    public boolean isSessionsEnabled() {
        return sessionsEnabled;
    }

    /**
     *
     * @param sessionsEnabled Whether sessions are enabled.
     */
    public void setSessionsEnabled(boolean sessionsEnabled) {
        this.sessionsEnabled = sessionsEnabled;
    }

    /**
     * Controls if the failed messages are routed to the DLQ
     *
     * @return boolean, default : false
     */
    public boolean isRequeueRejected() {
        return requeueRejected;
    }

    /**
     *
     * @param requeueRejected Whether requeue rejected.
     */
    public void setRequeueRejected(boolean requeueRejected) {
        this.requeueRejected = requeueRejected;
    }

    /**
     *
     * @return The Service Bus receive mode.
     */
    public ServiceBusReceiveMode getServiceBusReceiveMode() {
        return serviceBusReceiveMode;
    }

    /**
     *
     * @param serviceBusReceiveMode The Service Bus receive mode.
     */
    public void setServiceBusReceiveMode(ServiceBusReceiveMode serviceBusReceiveMode) {
        this.serviceBusReceiveMode = serviceBusReceiveMode;
    }
}
