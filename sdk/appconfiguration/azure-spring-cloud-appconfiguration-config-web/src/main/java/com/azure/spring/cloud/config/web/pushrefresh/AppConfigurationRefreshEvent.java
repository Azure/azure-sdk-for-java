// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.web.pushrefresh;

import org.springframework.context.ApplicationEvent;

/**
 * Event sent to set the App Configuration watch interval to zero.
 */
public final class AppConfigurationRefreshEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private final String endpoint;

    /**
     * Event object for when a push event is triggered from a web hook.
     * 
     * @param endpoint App Configuration Store endpoint that is requesting a refresh.
     */
    public AppConfigurationRefreshEvent(String endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
    }

    /**
     * Endpoint of the App Configuration store that triggered the Event.
     * 
     * @return the endpoint
     */
    public String getEndpoint() {
        return endpoint;
    }
}
