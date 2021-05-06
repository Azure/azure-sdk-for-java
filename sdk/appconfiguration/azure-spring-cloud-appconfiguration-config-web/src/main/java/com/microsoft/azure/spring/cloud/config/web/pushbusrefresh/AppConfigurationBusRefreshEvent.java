/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.web.pushbusrefresh;

import org.springframework.cloud.bus.event.Destination;
import org.springframework.cloud.bus.event.RemoteApplicationEvent;

public class AppConfigurationBusRefreshEvent extends RemoteApplicationEvent {

    private static final long serialVersionUID = 1L;

    private String endpoint;

    AppConfigurationBusRefreshEvent(String endpoint, AppConfigurationBusRefreshEndpoint source, String origin, Destination destination) {
        super("App Configuration Refresh Event", origin, destination);
        this.endpoint = endpoint;
    }

    AppConfigurationBusRefreshEvent() {
        this.endpoint = "";
    }

    /**
     * @return the endpoint
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Will only set the endpoint if one isn't already set.
     * @param endpoint the endpoint of the application that triggered the event
     */
    public void setEndpoint(String endpoint) {
        if (this.endpoint.equals("")) {
            this.endpoint = endpoint;
        }
    }

}
