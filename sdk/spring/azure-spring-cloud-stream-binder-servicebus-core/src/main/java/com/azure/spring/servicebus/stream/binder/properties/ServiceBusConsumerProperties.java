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

    public CheckpointMode getCheckpointMode() {
        return checkpointMode;
    }

    public void setCheckpointMode(CheckpointMode checkpointMode) {
        this.checkpointMode = checkpointMode;
    }

    public boolean isEnableAutoComplete() {
        return enableAutoComplete;
    }

    public void setEnableAutoComplete(boolean enableAutoComplete) {
        this.enableAutoComplete = enableAutoComplete;
    }

    /**
     * Controls the max concurrent calls of service bus message handler and
     * the size of fixed thread pool that handles user's business logic
     *
     * <p>
     * @return int, default : 1
     */
    public Integer getMaxConcurrentCalls() {
        return maxConcurrentCalls;
    }

    public void setMaxConcurrentCalls(int maxConcurrentCalls) {
        this.maxConcurrentCalls = maxConcurrentCalls;
    }

    /**
     * Maximum number of concurrent sessions to process at any given time
     *
     * <p>
     * @return int, default : 1
     */
    public Integer getMaxConcurrentSessions() {
        return maxConcurrentSessions;
    }

    public void setMaxConcurrentSessions(int maxConcurrentSessions) {
        this.maxConcurrentSessions = maxConcurrentSessions;
    }

    /**
     * Prefetch count of underlying service bus client.
     *
     * <p>
     *
     * @return int, default : 1
     */
    public int getPrefetchCount() {
        return prefetchCount;
    }

    public void setPrefetchCount(int prefetchCount) {
        this.prefetchCount = prefetchCount;
    }

    /**
     * Controls the max concurrent calls of service bus message handler and
     * the size of fixed thread pool that handles user's business logic
     *
     * <p>
     * @return int, default : 1
     * @deprecated Use maxConcurrentSessions and maxConcurrentCalls.
     */
    @Deprecated
    @DeprecatedConfigurationProperty(reason = "Deprecate the concurrency and use maxConcurrentSessions and maxConcurrentCalls instead")
    public int getConcurrency() {
        return concurrency;
    }

    @Deprecated
    public void setConcurrency(int concurrency) {
        logger.warn("Set attribute concurrency, which has been deprecated");
        this.concurrency = concurrency;
    }

    /**
     * Controls if is session aware
     *
     * <p>
     * @return boolean, default : false
     */
    public boolean isSessionsEnabled() {
        return sessionsEnabled;
    }

    public void setSessionsEnabled(boolean sessionsEnabled) {
        this.sessionsEnabled = sessionsEnabled;
    }

    /**
     * Controls if the failed messages are routed to the DLQ
     *
     * <p>
     * @return boolean, default : false
     */
    public boolean isRequeueRejected() {
        return requeueRejected;
    }

    public void setRequeueRejected(boolean requeueRejected) {
        this.requeueRejected = requeueRejected;
    }

    public ServiceBusReceiveMode getServiceBusReceiveMode() {
        return serviceBusReceiveMode;
    }

    public void setServiceBusReceiveMode(ServiceBusReceiveMode serviceBusReceiveMode) {
        this.serviceBusReceiveMode = serviceBusReceiveMode;
    }
}
