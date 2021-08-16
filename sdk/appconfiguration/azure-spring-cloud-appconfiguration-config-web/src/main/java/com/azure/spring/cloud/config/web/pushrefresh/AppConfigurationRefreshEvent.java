// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.web.pushrefresh;

import org.springframework.context.ApplicationEvent;

/**
 * Event sent to set the App Configuration watch interval to zero.
 */
public class AppConfigurationRefreshEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private final String endpoint;

    public AppConfigurationRefreshEvent(String endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
    }

    /**
     * @return the endpoint
     */
    public String getEndpoint() {
        return endpoint;
    }
}
