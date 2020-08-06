/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.jms;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;

@Validated
@ConfigurationProperties("spring.cloud.azure.servicebus.jms")
public class AzureServiceBusJMSProperties {

    private String connectionString;

    private String clientId;

    private int idleTimeout = 3600000;

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
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
            throw new IllegalArgumentException("'spring.cloud.azure.servicebus.jms.connection-string' " +
                    "should be provided");
        }
        if (!StringUtils.hasText(clientId)) {
            throw new IllegalArgumentException("'spring.cloud.azure.servicebus.jms.client-id' " +
                    "should be provided");
        }
        if (!StringUtils.hasText(idleTimeout + "")) {
            throw new IllegalArgumentException("'spring.cloud.azure.servicebus.jms.idle-timeout' " +
                    "should be provided");
        }
    }
}
