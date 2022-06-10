// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.web.pushrefresh;

import org.springframework.context.ApplicationEvent;

/**
 * Event sent to set the App Configuration watch interval to zero.
 */
public final class AppConfigurationRefreshEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    /**
     * Endpoint being refreshed.
     */
    private final String endpoint;

    /**
     * Sync Token
     */
    private final String syncToken;

    /**
     * Event object for when a push event is triggered from a web hook.
     * 
     * @param endpoint App Configuration Store endpoint that is requesting a
     *                 refresh.
     * @param syncToken App Configuration sync token
     */
    public AppConfigurationRefreshEvent(String endpoint, String syncToken) {
        super(endpoint);
        this.endpoint = endpoint;
        this.syncToken = syncToken;
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
     * Sync Token for getting latest configurations.
     * 
     * @return the syncToken
     */
    public String getSyncToken() {
        return syncToken;
    }

}
