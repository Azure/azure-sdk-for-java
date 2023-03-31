// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.web.implementation.pushbusrefresh;

import org.springframework.cloud.bus.event.Destination;
import org.springframework.cloud.bus.event.RemoteApplicationEvent;

/**
 * Event sent to all instances registered to the Bus to set the App Configuration watch interval to zero.
 */
public final class AppConfigurationBusRefreshEvent extends RemoteApplicationEvent {

    private static final long serialVersionUID = 1L;

    /**
     * Endpoint being refreshed.
     */
    private String endpoint;
    
    /**
     * Sync Token
     */
    private final String syncToken;

    /**
     * Event object for when a push event is triggered from a web hook. This event is sent to all registered application instances.
     *
     * @param endpoint App Configuration Store endpoint that is requesting a refresh.
     * @param syncToken App Configuration sync token
     */
    AppConfigurationBusRefreshEvent(String endpoint, String syncToken, AppConfigurationBusRefreshEndpoint source,
            String origin, Destination destination) {
        super("App Configuration Refresh Event", origin, destination);
        this.endpoint = endpoint;
        this.syncToken = syncToken;
    }

    AppConfigurationBusRefreshEvent() {
        this.endpoint = "";
        this.syncToken = "";
    }

    /**
     * Endpoint of the App Configuration store that triggered the Event.
     *
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
        if ("".equals(this.endpoint)) {
            this.endpoint = endpoint;
        }
    }

    /**
     * @return the syncToken
     */
    public String getSyncToken() {
        return syncToken;
    }

}
