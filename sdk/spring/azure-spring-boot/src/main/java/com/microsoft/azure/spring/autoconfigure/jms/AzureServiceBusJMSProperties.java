// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.autoconfigure.jms;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;

/**
 * {@link ConfigurationProperties} for configuring Azure Service Bus JMS.
 */
@Validated
@ConfigurationProperties("spring.jms.servicebus")
public class AzureServiceBusJMSProperties {

    private String connectionString;

    /**
     * JMS clientID
     */
    private String topicClientId;

    private int idleTimeout = 1800000;

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getTopicClientId() {
        return topicClientId;
    }

    public void setTopicClientId(String topicClientId) {
        this.topicClientId = topicClientId;
    }

    public int getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    @PostConstruct
    public void validate() {

        if (!StringUtils.hasText(connectionString)) {
            throw new IllegalArgumentException("'spring.jms.servicebus.connection-string' should be provided");
        }

    }
}
