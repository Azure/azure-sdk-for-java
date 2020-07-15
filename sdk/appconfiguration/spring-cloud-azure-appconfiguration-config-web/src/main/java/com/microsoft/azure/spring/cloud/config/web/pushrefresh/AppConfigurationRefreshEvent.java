/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.web.pushrefresh;

import org.springframework.context.ApplicationEvent;

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
