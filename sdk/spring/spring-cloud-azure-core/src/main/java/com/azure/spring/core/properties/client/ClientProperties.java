// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.client;

import com.azure.spring.core.aware.ClientAware;

import java.util.ArrayList;
import java.util.List;

/**
 * Properties shared by all azure service client builders.
 */
public class ClientProperties implements ClientAware.Client {

    private String applicationId;

    private final List<HeaderProperties> headers = new ArrayList<>();

    private final LoggingProperties logging = new LoggingProperties();

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public List<HeaderProperties> getHeaders() {
        return headers;
    }

    public LoggingProperties getLogging() {
        return logging;
    }

}
